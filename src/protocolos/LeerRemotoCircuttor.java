

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasCircuitor;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.ERegistroEvento;
import Entidades.Electura;
import Entidades.EtipoCanal;
import Util.SynHoraNTP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Metrolink
 */
public class LeerRemotoCircuttor extends Thread {

    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();//fecha para incluir en el traifile
    String seriemedidor = "";//serial de medidor
    String fechaactual = "";//fecha actual del sistema
    Timestamp tsfechaactual; //timestamp fecha actual
    Timestamp tspeticion;//fecha inicial perfil de carga
    Timestamp tsrecepcion; //fecha final de perfil de carga
    Timestamp deltatimesync1;//tiempo de para calculo de delay NTP
    Timestamp deltatimesync2; //tiempo de para calculo de delay NTP
    Timestamp time = null; //tiempo de NTP
    long ndesfasepermitido = 7200;//desfase tiempo NTP
    long tiempo = 500; //delat del puerto
    InputStream input; //buffer de entrada
    OutputStream output; //buffer de salida
    String cadenahex = ""; //cadena de recepcion en el puerto (Datos)
    int indx = 0; //indice la tabla (GUI) para cambio de estado
    String password = ""; //password1
    String password2 = ""; //password2
    String ASDU = "";
    String descripcion = "";
    EMedidor med; //obejto medidor con datos
    ControlProcesos cp; //clase control procesos SQL
    //opciones perfil, eventos, registros, configuracion de hora
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lconfHora;
    //variable que indica si lee
    public boolean leer = true;//indicador de lectura realizada
    String numeroPuerto; //puerto de la IP
    int numeroReintentos = 4;
    int nreintentos = 0; //intento actual completo
    int velocidadPuerto; //velocidad del puerto
    long timeout;//timeout  para las peticiones
    int ndias; //numero de dias calculado
    boolean portconect = false;//indicador de conexion al socket
    long tiemporetransmision = 0; //tiempo de espera para retransmison
    int acksEsperados;
    //Hilos para envios de tramas 
    Thread tEscritura = null;
    Thread tReinicio = null;
    public boolean cierrapuerto = false;//identificador d epuerto cerrado
    Socket socket;
    private volatile boolean escucha = true;//variable de control de escuchar el puerto
    Thread tLectura;//hilo para escuchar las recepciones de las tramas
    private int reintentoconexion = 0; //numero de reintentos de conexion
    boolean aviso = false; //control  de cambios de estado
    ArrayList<String> vconfperfil = new ArrayList<>();
    //arreglo de unidades
    ArrayList<String> vunidades = new ArrayList<>();
    String[] vPerfil = null; //vector con bloque de perfil de carga completo
    ArrayList<String[]> arrayperfil = new ArrayList<>();    // vector con los bloques del perfil
    String[] vEventos = null; //vector de blque de eventos
    ArrayList<String[]> arrayeventos = new ArrayList<>();// vector con los bloques de los eventos
    //variables de control de envio
    boolean enviando = false;
    boolean reenviando = false;
    boolean firstC = false;
    byte[] ultimatramaEnviada = null;//ultima trama enviada
    public String lastDate = null;
    //formatos de fechas
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    SimpleDateFormat sdf4 = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfacceso = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    int numcanales = 2; //numero de canales
    int intervalo = 0; //intervalo de datos 15,30,60.
    int dirfis = 0;
    int dirlog = 0;
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    Abortar objabortar;//variable que controla la opcion de abortar la comunicacion
    String usuario = "admin";
    int reinicio = 0;
    TramasCircuitor tramas = new TramasCircuitor();//obejto con las tramas de envio
    //estados protocolo
    boolean iniciosession = false;
    boolean comunicacion = false;
    boolean serialnumber;
    boolean confcanales;
    boolean elemntosPerfil;
    boolean preperfilcarga;
    boolean perfilcarga;
    boolean regeventos;
    boolean configuracionhora;
    boolean fecactual;
    boolean confPerfil;
    boolean logout = false;
    public String tramaIncompleta = "";
    public boolean complemento = false;
    private boolean perfilincompleto = false;
    boolean solicitar = false; //variable de control de la sync
    int resolucion = 1;
    int intercambio = 0;//valor que diferencia las solicitudes del perfil de carga segun explicacion del protocolo
    private final Object monitor = new Object();    
    ZoneId zid;
    int reenvios = 0;
    long desfase;
    private Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    private ArrayList<EtipoCanal> vtipocanal;
    private final String label = "LeerTCPCircuttor";

    //constructor para la recepcion de datos desde la interfaz de lectura
    public LeerRemotoCircuttor(EMedidor med, boolean perfil, boolean eventos, boolean registros, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, String usuario, ZoneId zid, long ndesfase) {

        this.med = med;
        this.cp = cp;
        //creacion del archivo trailfile
        try {
            File f = new File(cp.rutalogs + sdfarchivo.format(new Date()) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
        
            if (!f.exists()) {
                f.mkdirs();
            }
            file = new File(cp.rutalogs + sdfarchivo.format(new Date()) + "/" + this.med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        //verificamos que se solicito a travez de la interfaz de lectura
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        this.aviso = aviso;
        //control de interfaz
        //control de abortar
        this.objabortar = objabortar;
        //indice de tabla de lectura
        this.indx = indx;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        jinit();
    }

    private void jinit() {
        try {
            tiempoinicial = obtenerHora();
            //configuracion de medidor
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            reenvios = numeroReintentos;
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            password2 = med.getPassword2();
            timeout = med.getTimeout() * 1000;
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "1" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "1" : med.getDireccionLogica()));
            lconske = cp.buscarConstantesKe(med.getnSerie());//se toman los valores de las constantes 
            for (EConstanteKE consKe : lconske) {
                escribir("" + consKe.getCanal());
            }
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            for (EtipoCanal canal : vtipocanal) {
                escribir("" + canal.getUnidad());
            }
            //abre el socket
            abrePuerto();
        } catch (Exception e) {
            leer = false;
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void abrePuerto() {
        try {
            //verifica que no se ha abortado la comunicacion
            if (!objabortar.labortar) {
                if (reintentoconexion < numeroReintentos) { //3) { //parametrizable con reintentos de conf_medidor
                    //abre socket
                    ////System.out.println("Conectando.. " + med.getDireccionip() + ":" + med.getPuertoip());                    
                    socket = new Socket(med.getDireccionip(), Integer.parseInt(med.getPuertoip()));
                    ////System.out.println("Conectado " + med.getDireccionip() + ":" + med.getPuertoip());                    
                    //establece timeout para conexion de 35 seg
                    socket.setSoTimeout(35000);
                    portconect = !socket.isClosed();
                    if (portconect) {
                        try {
                            //Se crea salidas y entradas
                            input = socket.getInputStream();
                            output = socket.getOutputStream();
                        } catch (IOException e) {
                            escribir("Problemas al generar los flujos de entrada y salida");
                            portconect = false;
                            System.err.println("Error Input/output");
                        }
                    }
                    //metodo para escuchar el puerto
                    try {
                        //escuchamos el puerto para interpretar la tramas del protocolo
                        if (portconect) {
                            ////System.out.println("Inicia Escucha");
                            tLectura = new Thread() {
                                @Override
                                public void run() {
                                    escribir("Abriendo Hilo escucha");
                                    escucha();
                                    escribir("Cerrando Hilo escucha");
                                }
                            };
                            tLectura.start();
                        }
                    } catch (Exception e) {
                        portconect = false;
                        interrumpirHilo(tLectura);
                        System.err.println(e.getMessage());
                    }
                    Thread.sleep(2000);
                    if (portconect) {
                        if (!objabortar.labortar) {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Conexion Satisfactoria");
                            iniciacomunicacion();
                        } else {
                            interrumpirHilo(tLectura);
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        escribir("Socket cerrado");
                        throw new Exception("Socket cerrado");
                    }
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion por numero de reintentos");
                    escribir("Número de reintentos agotado");                 
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Cancelacion de operacion");
                escribir("Operación abortada");
                cerrarLog("Desconexion Cancelacion de operacion", false);                
                leer = false;
            }
        } catch (Exception e) {
            interrumpirHilo(tLectura);
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No conectado");
            escribir("ERROR de comunicacion - " + e.getMessage());
            reintentoconexion++;
            abrePuerto();
        }
    }

    private void iniciacomunicacion() throws Exception {
        if (med.isLconfigurado()) {
            try {
                //se imprimen los dato de configuracion en el archivo traifile
                String mensaje = "INICIO DE SESSION --" + new Timestamp(new Date().getTime()) + "--";
                escribir(mensaje);
                mensaje = "Marca medidor: " + med.getMarcaMedidor().getNombre();
                escribir(mensaje);
                mensaje = "Puerto TCP " + med.getPuertoip();
                escribir(mensaje);
                mensaje = "Direccion IP " + med.getDireccionip();
                escribir(mensaje);
                mensaje = "Numero de reintentos " + med.getReintentos();
                escribir(mensaje);
                mensaje = "Numero de dias default " + med.getNdias();
                escribir(mensaje);
                mensaje = "Timeout " + med.getTimeout();
                escribir(mensaje);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                escribir("Error Escribiendo Cabecera de Log de Comunicaciones");
            }
            //numero controlado que va aumentando al llegar al numero de reintentos se reinicia y pasa al siguiente medidor
            if (reintentoconexion != 0) {
                Timestamp actufecha = cp.findUltimafechaLec(med.getnSerie());
                if (actufecha != null) {
                    med.setFecha(actufecha);
                } else {
                    throw new Exception("No se pudo actualizar la fecha de última lectura. Reintento cancelado");
                }
            }
            iniciosession = true;
            //se envia la primera trama de comuniccion
            byte[] data = asignaDireccion(tramas.getInicio());
            String pwd = "";
            for (int i = 0; i < password.length(); i++) {
                if (!password.substring(i, i + 1).equals("0")) {
                    pwd = password.substring(i);
                    break;
                }
            }
            String vpassword = ("" + Integer.toHexString(Integer.decode(pwd))).toUpperCase();
            while (vpassword.length() < 8) {
                vpassword = "0" + vpassword;
            }
            data[13] = (byte) (Integer.parseInt(vpassword.substring(6, 8), 16) & 0xFF);
            data[14] = (byte) (Integer.parseInt(vpassword.substring(4, 6), 16) & 0xFF);
            data[15] = (byte) (Integer.parseInt(vpassword.substring(2, 4), 16) & 0xFF);
            data[16] = (byte) (Integer.parseInt(vpassword.substring(0, 2), 16) & 0xFF);
            data = calculaChecksum(data);
            ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
            enviaTrama2_2(data, "=> Solicitud de inicio de session");
        } else {
            interrumpirHilo(tLectura);
            escribir("Medidor no configurado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
            cerrarPuerto();
            cerrarLog("Medidor no configurado", false);
            leer = false;
        }
    }

    private void escucha() {
        try {
            synchronized (monitor) {
                escucha = true;
                //consiste en un ciclo infinito que procesa las peticiones cuando se abra el conducto para escuchar
                while (escucha) {
                    monitor.wait();
                    //metodo que procesa el buffer de entrada del socket
                    procesaCadena();
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void procesaCadena() {

        byte[] readBuffer = new byte[2047];
        byte[] auxBuffer = new byte[2047];
        int idxFrame = 0;
        int numbytes;
        byte begin = 0, end = 0;
        boolean uncomplete = true;
        boolean beginOk = false;
        boolean endOk;
        boolean frameLengthOk = false;
        try {
            synchronized (monitor) {
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                //si el puerto tiene datos llenamos el buffer con lo que se encuentra en el puerto.
                while (!socket.isClosed() && uncomplete && System.currentTimeMillis() < endT) {
                    monitor.wait(100);
                    if (input.available() == 0) {
                        continue;
                    }
                    endT = System.currentTimeMillis() + timeout;
                    numbytes = input.read(readBuffer);
                    ////System.out.println("Arriving: " + tramas.encode(readBuffer, numbytes));
                    if (idxFrame == 0) {
                        begin = readBuffer[0];
                        beginOk = begin == 104 || begin == 16;
                        ////System.out.println(" " + begin);
                    }
                    end = readBuffer[numbytes - 1];
                    endOk = end == 22;
                    ////System.out.println(" " + end);
                    if (numbytes >= 2 && idxFrame == 0) {
                        if (begin == 104) {
                            int actualFrameLength = (int) (readBuffer[1] & 0xFF);
                            ////System.out.println("Lenght: " + actualFrameLength);
                            frameLengthOk = actualFrameLength == numbytes - 6;
                        } else if (begin == 16) {
                            frameLengthOk = numbytes == 6;
                        }
                    }
                    ////System.out.println("begin: " + beginOk + " endOk: " + endOk + " frameLengthOk: " + frameLengthOk);
                    if (!beginOk || !endOk || !frameLengthOk) {
                        System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                        idxFrame += numbytes;
                        ////System.out.println("Buffer Auxiliar: " + tramas.encode(auxBuffer, idxFrame));
                        if (idxFrame >= 3) {
                            if (begin == 104) {
                                int actualFrameLength = (int) (auxBuffer[1] & 0xFF);
                                ////System.out.println("Lenght: " + actualFrameLength);
                                frameLengthOk = actualFrameLength == idxFrame - 6;
                                ////System.out.println("frameLengthOk: " + frameLengthOk);
                            } else if (begin == 16) {
                                frameLengthOk = idxFrame == 6;
                            }
                            if (frameLengthOk) {
                                //Solo debería validar el ASDU para algunos casos por ejemplo si es trama de longitud variable 104 o sí estamos en el estado correcto                                 
                                boolean notInEvStates = !iniciosession && !comunicacion && !configuracionhora && !logout;//Validamos si no estamos en uno de los estados no evaluables por ASDU.
                                ////System.out.println("Inicio Sesión: " + iniciosession);
                                ////System.out.println("Comunicación: " + comunicacion);
                                ////System.out.println("Configuración hora: " + configuracionhora);
                                ////System.out.println("Logout: " + logout);
                                ////System.out.println("Not In Evaluable States: " + notInEvStates);
                                if (begin == 104 && notInEvStates) {
                                    if (this.validacionASDU(auxBuffer[7], ASDU, descripcion) && ( !ASDU.equals("8C") && !ASDU.equals("01") )) {  //Validamos ASDU pero también que no estamos en Estado de Perfil de Carga ni en Eventos                                     
                                        ////System.out.println("Salir del bucle de escucha forma 2.");
                                        if (!firstC) {
                                            timeout = timeout * 3;
                                            firstC = true;
                                        }
                                        uncomplete = false;
                                        enviando = false;
                                        reenviando = false;
                                        monitor.notifyAll();
                                        break;
                                    } else if (this.validacionASDU(auxBuffer[7], ASDU, descripcion) && ASDU.equals("8C")) {// Estamos en el estado de Perfil de Carga
                                        //En este estado para evitar desfases de respuestas que tienen el mismo ASDU validamos que el checksum sea diferente entre respuestas consecutivas.
                                        String rxCurrDate = "" + auxBuffer[idxFrame - 3] + "" + auxBuffer[idxFrame - 4] + "" + auxBuffer[idxFrame - 5] + "" + auxBuffer[idxFrame - 6] + "" + auxBuffer[idxFrame - 7];
                                        if (lastDate == null ? true : !lastDate.equals(rxCurrDate)) {
                                            lastDate = "" + auxBuffer[idxFrame - 3] + "" + auxBuffer[idxFrame - 4] + "" + auxBuffer[idxFrame - 5] + "" + auxBuffer[idxFrame - 6] + "" + auxBuffer[idxFrame - 7]; //Actualizamos o seteamos por primera vez al Byte Checksum

                                            ////System.out.println("Salir del bucle de escucha forma 2.");
                                            if (!firstC) {
                                                timeout = timeout * 3;
                                                firstC = true;
                                            }
                                            uncomplete = false;
                                            enviando = false;
                                            reenviando = false;
                                            monitor.notifyAll();
                                            break;
                                        } else {
                                            //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                            readBuffer = new byte[2047];
                                            auxBuffer = new byte[2047];
                                            idxFrame = 0;
                                            begin = 0;
                                            end = 0;
                                            uncomplete = true;
                                            beginOk = false;
                                            frameLengthOk = false;
                                            startT = System.currentTimeMillis();
                                            endT = startT + 3 * timeout;
                                        }
                                    } else {
                                        //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                        readBuffer = new byte[2047];
                                        auxBuffer = new byte[2047];
                                        idxFrame = 0;
                                        begin = 0;
                                        end = 0;
                                        uncomplete = true;
                                        beginOk = false;
                                        frameLengthOk = false;
                                        startT = System.currentTimeMillis();
                                        endT = startT + 3 * timeout;
                                    }
                                } else { //Debemos validar la cantidad de ACKs que posiblemente se repitan, si se repiten debemos reiniciar el ciclo de escucha.
                                    ////System.out.println("ACKs Esperados: " + acksEsperados);
                                    ////System.out.println("Begin: " + begin);
                                    if (acksEsperados > 0 && begin == 16) {
                                        acksEsperados--;
                                        //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                        readBuffer = new byte[2047];
                                        auxBuffer = new byte[2047];
                                        idxFrame = 0;
                                        begin = 0;
                                        end = 0;
                                        uncomplete = true;
                                        beginOk = false;
                                        frameLengthOk = false;
                                        startT = System.currentTimeMillis();
                                        endT = startT + 3 * timeout;
                                    } else {
                                        ////System.out.println("Salir del bucle de escucha forma 2.");
                                        uncomplete = false;
                                        enviando = false;
                                        reenviando = false;
                                        monitor.notifyAll();
                                        break;
                                    }
                                }

                            }
                        }
                    } else if (idxFrame == 0) {
                        //Solo debería validar el ASDU para algunos casos por ejemplo si es trama de longitud variable 104 o sí estamos en el estado correcto                                                          
                        boolean notInEvStates = !iniciosession && !comunicacion && !configuracionhora && !logout; //Validamos si no estamos en uno de los estados no evaluables por ASDU.
                        ////System.out.println("Inicio Sesión: " + iniciosession);
                        ////System.out.println("Comunicación: " + comunicacion);
                        ////System.out.println("Configuración hora: " + configuracionhora);
                        ////System.out.println("Logout: " + logout);
                        ////System.out.println("Not In Evaluable States: " + notInEvStates);
                        if (begin == 104 && notInEvStates) {
                            if (this.validacionASDU(readBuffer[7], ASDU, descripcion) && ( !ASDU.equals("8C") && !ASDU.equals("01") )) {
                                ////System.out.println("Salir del bucle de escucha forma 1.");
                                auxBuffer = readBuffer;
                                idxFrame = numbytes;
                                uncomplete = false;
                                enviando = false;
                                reenviando = false;
                                monitor.notifyAll();
                            } else if (this.validacionASDU(readBuffer[7], ASDU, descripcion) && ASDU.equals("8C")) {// Estamos en el estado de Perfil de Carga
                                //En este estado para evitar desfases de respuestas que tienen el mismo ASDU validamos que el checksum sea diferente entre respuestas consecutivas.
                                String rxCurrDate = "" + readBuffer[numbytes - 3] + "" + readBuffer[numbytes - 4] + "" + readBuffer[numbytes - 5] + "" + readBuffer[numbytes - 6] + "" + readBuffer[numbytes - 7];
                                if (lastDate == null ? true : !lastDate.equals(rxCurrDate)) {
                                    lastDate = "" + readBuffer[numbytes - 3] + "" + readBuffer[numbytes - 4] + "" + readBuffer[numbytes - 5] + "" + readBuffer[numbytes - 6] + "" + readBuffer[numbytes - 7];
                                    ////System.out.println("Salir del bucle de escucha forma 1.");
                                    auxBuffer = readBuffer;
                                    idxFrame = numbytes;
                                    uncomplete = false;
                                    enviando = false;
                                    reenviando = false;
                                    monitor.notifyAll();
                                } else {
                                    //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                    readBuffer = new byte[2047];
                                    auxBuffer = new byte[2047];
                                    idxFrame = 0;
                                    begin = 0;
                                    end = 0;
                                    uncomplete = true;
                                    beginOk = false;
                                    frameLengthOk = false;
                                    startT = System.currentTimeMillis();
                                    endT = startT + 3 * timeout;
                                }
                            } else {
                                //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                readBuffer = new byte[2047];
                                auxBuffer = new byte[2047];
                                idxFrame = 0;
                                begin = 0;
                                end = 0;
                                uncomplete = true;
                                beginOk = false;
                                frameLengthOk = false;
                                startT = System.currentTimeMillis();
                                endT = startT + 3 * timeout;
                            }
                        } else {
                            ////System.out.println("ACKs Esperados: " + acksEsperados);
                            ////System.out.println("Begin: " + begin);
                            if (acksEsperados > 0 && begin == 16) {
                                acksEsperados--;
                                //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                readBuffer = new byte[2047];
                                auxBuffer = new byte[2047];
                                idxFrame = 0;
                                begin = 0;
                                end = 0;
                                uncomplete = true;
                                beginOk = false;
                                frameLengthOk = false;
                                startT = System.currentTimeMillis();
                                endT = startT + 3 * timeout;
                            } else {
                                ////System.out.println("Salir del bucle de escucha forma 1.");
                                auxBuffer = readBuffer;
                                idxFrame = numbytes;
                                uncomplete = false;
                                enviando = false;
                                reenviando = false;
                                monitor.notifyAll();
                            }
                        }

                    } else {
                        //Solo debería validar el ASDU para algunos casos por ejemplo si es trama de longitud variable 104 o sí estamos en el estado correcto   
                        ////System.out.println("Inicio Sesión: " + iniciosession);
                        ////System.out.println("Comunicación: " + comunicacion);
                        ////System.out.println("Configuración hora: " + configuracionhora);
                        ////System.out.println("Logout: " + logout);
                        boolean notInEvStates = !iniciosession && !comunicacion && !configuracionhora && !logout; //Validamos si no estamos en uno de los estados no evaluables por ASDU.
                        if (begin == 104 && notInEvStates) {
                            if (this.validacionASDU(auxBuffer[7], ASDU, descripcion) && ( !ASDU.equals("8C") && !ASDU.equals("01") )) {
                                ////System.out.println("Salir del bucle de escucha forma 2.");
                                if (!firstC) {
                                    timeout = timeout * 3;
                                    firstC = true;
                                }
                                uncomplete = false;
                                enviando = false;
                                reenviando = false;
                                monitor.notifyAll();
                            } else if (this.validacionASDU(auxBuffer[7], ASDU, descripcion) && ASDU.equals("8C")) {// Estamos en el estado de Perfil de Carga
                                //En este estado para evitar desfases de respuestas que tienen el mismo ASDU validamos que el checksum sea diferente entre respuestas consecutivas.
                                String rxCurrDate = "" + auxBuffer[idxFrame - 3] + "" + auxBuffer[idxFrame - 4] + "" + auxBuffer[idxFrame - 5] + "" + auxBuffer[idxFrame - 6] + "" + auxBuffer[idxFrame - 7];
                                if (lastDate == null ? true : !lastDate.equals(rxCurrDate)) {
                                    lastDate = "" + auxBuffer[idxFrame - 3] + "" + auxBuffer[idxFrame - 4] + "" + auxBuffer[idxFrame - 5] + "" + auxBuffer[idxFrame - 6] + "" + auxBuffer[idxFrame - 7]; //Actualizamos o seteamos por primera vez al Byte Checksum
                                    ////System.out.println("Salir del bucle de escucha forma 2.");
                                    if (!firstC) {
                                        timeout = timeout * 3;
                                        firstC = true;
                                    }
                                    uncomplete = false;
                                    enviando = false;
                                    reenviando = false;
                                    monitor.notifyAll();
                                } else {
                                    //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                    readBuffer = new byte[2047];
                                    auxBuffer = new byte[2047];
                                    idxFrame = 0;
                                    begin = 0;
                                    end = 0;
                                    uncomplete = true;
                                    beginOk = false;
                                    frameLengthOk = false;
                                    startT = System.currentTimeMillis();
                                    endT = startT + 3 * timeout;
                                }
                            } else {
                                //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                readBuffer = new byte[2047];
                                auxBuffer = new byte[2047];
                                idxFrame = 0;
                                begin = 0;
                                end = 0;
                                uncomplete = true;
                                beginOk = false;
                                frameLengthOk = false;
                                startT = System.currentTimeMillis();
                                endT = startT + 3 * timeout;
                            }
                        } else {
                            ////System.out.println("ACKs Esperados: " + acksEsperados);
                            ////System.out.println("Begin: " + begin);
                            if (acksEsperados > 0 && begin == 16) {
                                acksEsperados--;
                                //Reiniciamos Buffers, Enteros, Booleanos y Timeout
                                readBuffer = new byte[2047];
                                auxBuffer = new byte[2047];
                                idxFrame = 0;
                                begin = 0;
                                end = 0;
                                uncomplete = true;
                                beginOk = false;
                                frameLengthOk = false;
                                startT = System.currentTimeMillis();
                                endT = startT + 3 * timeout;
                            } else {
                                ////System.out.println("Salir del bucle de escucha forma 2.");
                                uncomplete = false;
                                enviando = false;
                                reenviando = false;
                                monitor.notifyAll();
                            }
                        }
                    }
                }
                if (!socket.isClosed() && uncomplete) {//&& !frameOk) {
                    ////System.out.println("Se vencío el timeout de respuesta sin recibir nada.");
                    escribir("Se vencío el timeout de respuesta sin recibir nada");
                    reenviando = true;
                    enviando = false;
                    monitor.notifyAll();
                    return;
                }
            }

            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramas.encode(auxBuffer, idxFrame);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
            //luego de tener la trama desglosada byte x byte continuamos a interpretarla
            if (cadenahex.length() > 0) {
                ////System.out.println("LLega dato");
                interpretaCadena(cadenahex);
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
            cerrarPuerto();
            reenviando = false;
            enviando = false;
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }
    //interpretacion de tramas

    private void revisarInicioSession(String[] vectorhex) {
        if (validaCheckSum(vectorhex)) {//valida checksum
            if ((((byte) Integer.parseInt(vectorhex[1], 16) & 0xFF) & 0x0F) == 0x00) {//validamos ack
                ////System.out.println("Envia Solicitud de datos");                
                //se envia la trama de solicitud de datos
                iniciosession = false;
                comunicacion = true;
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                enviaTrama2_2(data, "=> Solicitud de inicio de session");
            } else if ((((byte) Integer.parseInt(vectorhex[1], 16) & 0xFF) & 0x0F) == 0x01 || (((byte) Integer.parseInt(vectorhex[1], 16) & 0xFF) & 0x0F) == 0x09) {//valida nack
                escribir("NACK");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Negacion del servicio");
                escribir("Desconexion - Error de autenticacion");                
                cerrarLog("Desconexion Negacion del servicio", false);
                leer = false;
            } else {
                escribir("Error de Validacion ACK");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Negacion del servicio");
                escribir("Desconexion - Error de autenticacion");                
                cerrarLog("Desconexion Negacion del servicio", false);
                leer = false;
            }
        } else {
            escribir("Error de checksum");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de checksum");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error checksum", false);
            leer = false;
        }
    }

    private void revisarComunicacion(String[] vectorhex) {
        if (validaCheckSum(vectorhex)) {//valida checksum
            if (((Integer.parseInt(vectorhex[4], 16) % 32)) == 8) {//validamos ack
                comunicacion = false;
                serialnumber = true;
                descripcion = "Serie";
                this.ASDU = "47";                
                byte[] data = asignaDireccion(tramas.getSerialnumber());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                enviaTrama2_2(data, "=> Solicitud de numero de serial");
            } else if (((Integer.parseInt(vectorhex[4], 16) % 32)) == 9) {//validamos nack
                escribir("NACK");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Negacion del servicio");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Negacion del servicio", false);
                leer = false;
            } else {
                escribir("Error de Validacion ACK");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Negacion del servicio");
                escribir("Estado lectura No leido");
                cerrarLog("Desconexion Negacion del servicio", false);                
                leer = false;
            }
        } else {
            escribir("Error de checksum");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
            cerrarPuerto();
            escribir("Estado lectura No leido");
            cerrarLog("Desconexion Error checksum", false);            
            leer = false;
        }
    }

    private void revisarSerialNumber(String[] vectorhex) {
        if (vectorhex[0].equals("10")) {//es un ack
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data = calculaChecksum(data);
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                enviaTrama2_2(data, "=> Solicitud de envio de informacion");
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            if (validaCheckSum(vectorhex)) {//valida checksum
                if (vectorhex.length >= 18) {
                    String serial = "" + Integer.parseInt((vectorhex[18] + vectorhex[17] + vectorhex[16] + vectorhex[15]), 16);
                    if (seriemedidor.equals(serial)) {
                        reenvios = numeroReintentos;
                        serialnumber = false;
                        confcanales = true;
                        descripcion = "Configuración de Canales";
                        this.ASDU = "F8";
                        byte data[] = asignaDireccion(tramas.getConfcanales());
                        data = calculaChecksum(data);
                        ////System.out.println("=> " + tramas.encode(data, data.length));
                        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                        
                        enviaTrama2_2(data, "=> Solicitud de numero de configuracion de canales");
                    } else {
                        escribir("Numero de serial incorrecto");
                        cerrarPuerto();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                        escribir("Estado lectura No leido");                        
                        cerrarLog("Desconexion Error de serial", false);
                        leer = false;
                    }
                } else {
                    escribir("Error de datos");
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de datos");
                    escribir("Estado lectura No leido");                    
                    cerrarLog("Desconexion Error checksum", false);
                    leer = false;
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion de datos");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error de recepcion de datos", false);
            leer = false;
        }
    }

    private void revisarConfCanales(String[] vectorhex) {
        if (vectorhex[0].equals("10")) {//es un ack
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                vconfperfil = new ArrayList<>();
                vunidades = new ArrayList<>();
                enviaTrama2_2(data, "=> Solicitud de envio de informacion");
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                confcanales = false;
                reenvios = numeroReintentos;
                for (int i = 14; i < 22; i++) {
                    ////System.out.println("Canal " + vectorhex[i]);
                    vconfperfil.add(vectorhex[i]);
                    ////System.out.println("Unidad " + validaUnidad(vectorhex[i]));
                    vunidades.add(validaUnidad(vectorhex[i]));
                }
                escribir("Cantidad de canales internos del medidor: " + vconfperfil.size());
                escribir("Vector de canales internos del medidor: " + vconfperfil.toString());
                confPerfil = true;
                descripcion = "Configuración de Perfil";
                this.ASDU = "8E";
                byte data[] = asignaDireccion(tramas.getConfperfil());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                
                enviaTrama2_2(data, "=> Solicitud de envio configuracion de perfil de carga");

            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error Recepcion");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error de recepcion", false);
            leer = false;
        }
    }

    private void revisarConfPerfil(String[] vectorhex) {
        descripcion = "Configuración de Perfil";
        if (vectorhex[0].equals("10")) {//es un ack
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                       
                enviaTrama2_2(data, "=> Solicitud de envio de informacion");
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                confPerfil = false;
                reenvios = numeroReintentos;
                if (vectorhex.length > 59) {
                    elemntosPerfil = true;
                    descripcion = "Elementos de Perfil";
                    this.ASDU = "9D";                    
                    byte data[] = asignaDireccion(tramas.getElementosPerfil());
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                    enviaTrama2_2(data, "=> Solicitud de elementos del perfil");
                } else {
                    escribir("Error de Recepcion de trama");
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
                    escribir("Estado lectura No leido");                    
                    cerrarLog("Desconexion Error recepcion", false);
                    leer = false;
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error recepcion", false);
            leer = false;
        }
    }

    private void revisarElementosPerfil(String[] vectorhex) {
        if (vectorhex[0].equals("10")) {//es un ack
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                      
                enviaTrama2_2(data, "=> Solicitud de envio de informacion");
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            if (validaCheckSum(vectorhex)) {//valida checksum
                //pendiente Evaluar Causa de transmision de lo contrario abortar.
                if (vectorhex[9].equals("05")) {//causa de transmision.
                    //byte 15                        
                    if (((byte) Integer.parseInt(vectorhex[14], 16) & 0x80) == 0x00) {
                        //tiene dos decimales
                        ////System.out.println("decimales 2");
                        resolucion = 100;
                    } else {
                        //tiene 4 decimales
                        ////System.out.println("decimales 4");
                        resolucion = 10000;
                    }
                    intervalo = Integer.parseInt(vectorhex[14], 16) & 0x3F;
                    escribir("Periodo de integracion " + intervalo);
                    reenvios = numeroReintentos;
                    elemntosPerfil = false;
                    fecactual = true;
                    descripcion = "Fecha y Hora del Medidor";
                    this.ASDU = "48";
                    byte data[] = asignaDireccion(tramas.getFecha());
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                    
                    enviaTrama2_2(data, "=> Solicitud de fecha actual");
                } else {
                    escribir("Error de causa de transmision");
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error causa de transmision");
                    escribir("Estado lectura No leido");                    
                    cerrarLog("Desconexion Error causa de transmision", false);
                    leer = false;
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error Recepcion");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error recepcion", false);
            leer = false;
        }
    }

    private boolean validacionASDU(byte rxASDU, String ASDU, String descripcion) {
        Boolean ASDUValido = false;
        boolean exception = ( ASDU.equals("8C") && (rxASDU & 0xFF) == 190 ) || ( ASDU.equals("01") && (rxASDU & 0xFF) == 102 );//Es porque finalizó el perfil de carga o los Eventos.
        ////System.out.println("Exception: " + exception);
        ////System.out.println("RxASDU: " + (rxASDU & 0xFF) + " ASDU esperado: " + (Integer.parseInt(ASDU, 16) & 0xFF));
        if ((rxASDU & 0xFF) == (Integer.parseInt(ASDU, 16) & 0xFF) || exception) {// Valida ASDU para elementos de perfil.            
            ASDUValido = true;
        } else {
            escribir("Error de validacion de ASDU para " + descripcion);
        }
        return ASDUValido;
    }

    private void revisarFecActual(String[] vectorhex) throws ParseException {
        if (vectorhex[0].equals("10")) {//es un ack
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                try {
                    escribir("Desfase Permitido " + ndesfasepermitido);
                    time = new Timestamp(this.getDCurrentDate().getTime());
                    escribir("max desfase permitido " + ndesfasepermitido);
                    escribir("Fecha NTP" + time);
                    ////System.out.println("max desfase permitido " + ndesfasepermitido);
                    ////System.out.println("Fecha NTP" + time);
                    deltatimesync1 = new Timestamp(this.getDCurrentDate().getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    //time = new Timestamp(Calendar.getInstance().getTimeInMillis());
                    time = new Timestamp(this.getDCurrentDate().getTime());
                    ////System.out.println("Sincronizando hora con equipo local " + time);
                    escribir("Sincronizando hora con equipo local " + time);
                    escribir("Fecha equipo local" + time);
                    escribir("max desfase permitido " + ndesfasepermitido);
                    ////System.out.println("max desfase permitido " + ndesfasepermitido);

                }
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                        
                enviaTrama2_2(data, "=> Solicitud de envio de informacion");
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                if (vectorhex.length > 19) {
                    String dia = validarOffSet(vectorhex[17]);
                    //yy/MM/dd HH:mm:ss
                    String hora = validarOffSet(vectorhex[16]);
                    fechaactual = completarCeros(Integer.parseInt(vectorhex[19], 16), 2) + "/" + completarCeros(Integer.parseInt(vectorhex[18], 16), 2) + "/" + completarCeros(Integer.parseInt(dia, 16), 2) + " " + completarCeros(Integer.parseInt(hora, 16), 2) + ":" + completarCeros(Integer.parseInt(vectorhex[15], 16), 2) + ":" + completarCeros(Integer.parseInt(vectorhex[14] + vectorhex[13], 16), 5).substring(0, 2);
                    ////System.out.println("Fecha " + fechaactual);
                    escribir("Fecha " + fechaactual);

                    //VALIDAMOS HORA ACTUAL DEL MEDIDOR CON SINCRONIZACION A LA SIC
                    solicitar = true;
                    try {
                        tsfechaactual = new Timestamp(sdf3.parse(fechaactual).getTime());
                        escribir("Delta time: " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                        desfase = (time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000;
                        escribir("Diferencia SEG NTP: " + desfase);
                        if (Math.abs(desfase) > ndesfasepermitido) {
                            solicitar = false;
                            escribir("Desfase Permitido: " + ndesfasepermitido);
                            escribir("No se solicitara perfil de carga");
                        }
                    } catch (Exception e) {            
                        escribir("Error al calcular desfase, por tanto no se continuará.");
                        escribir(getErrorString(e.getStackTrace(), 3));
                        solicitar = false;                        
                    }     
                    if (solicitar) {
                         try {
                            cp.actualizaDesfase(desfase, med.getnSerie());
                        } catch (Exception e) {
                            escribir("Error al actualizar desfase en DB.");
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }
                    }
                    fecactual = false;
                    reenvios = numeroReintentos;
                    if (lperfil) {
                        try {
                            escribir("Fecha Actual Estacion de trabajo " + new Timestamp(new Date().getTime()));
                            escribir("Fecha Actual Medidor " + new Timestamp(sdf3.parse(fechaactual).getTime()));
                            if (med.getFecha() != null) {
                                escribir("Fecha Ultima Lectura " + med.getFecha());
                                long diffInMillies = Math.abs(this.getDSpecificDate(true, 1, "D").getTime() - med.getFecha().getTime());
                                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                                ndias = (int) diff;
                                escribir("Numero de dias leer calculado " + ndias);
                            }
                        } catch (Exception e) {
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }

                        if (solicitar) {                            
                            perfilcarga = true;
                            descripcion = "Perfil de Carga";
                            this.ASDU = "BE";                            
                            byte data[] = asignaDireccion(tramas.getSolicitudPerfil());
                            try {
                                //**************Fecha inicial*****************************************
                                Date fechaUltLec = new Date(med.getFecha().getTime());
                                String fechaI = sdf.format(this.getDSpecificDate(false, 1, "H", fechaUltLec));
                                ////System.out.println("fechaI: " + fechaI);
                                ////System.out.println("Fecha inicio peticion pefil: " + sdf3.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                                escribir("Fecha inicio peticion pefil: " + sdf3.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                                /*
                                        //**************Fecha inicial*****************************************
                                        Timestamp fechaini = new Timestamp((new Date().getTime() - (long) (86400000L * ndias)));
                                        String fecha = sdf.format(new Date(fechaini.getTime()));
                                        ////System.out.println("Fecha calculada " + new Date(new Date().getTime() - (long) (86400000L * ndias)));
                                        ////System.out.println("Fecha peticion pefil  " + sdf3.format(new Date((new Date().getTime() - (long) (86400000L * ndias)))));
                                        escribir("Fecha inicial peticion de perfil de carga " + fecha);
                                 */
                                //calculo year
                                String lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(0, 2)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion de year
                                data[18] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                ///*************************************
                                //calculo mes
                                lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(2, 4)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion mess
                                data[17] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                //**************************************
                                //calculo dia
                                lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(4, 6)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                byte offset = calcularOffset(new Timestamp(this.getDSpecificDate(false, 1, "H", fechaUltLec).getTime()), lfecha);
                                //asignacion dia                                                                       
                                data[16] = (byte) offset;
                                //**************************************
                                //calculo hora
                                lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(6, 8)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion hora                                                                       
                                data[15] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                //**************************************
                                //calculo min
                                lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(8, 10)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion min                                                                       
                                data[14] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);

                                //*********************calculo de fecha final*************************  
                                String fechaF;
                                Timestamp fechaF_TS;
                                if (ndias > 30) {
                                    fechaF = this.getSpecificDate(sdf, true, 30, "D", fechaI);
                                    escribir("Fecha final de perfil de carga " + this.getSpecificDate(sdf3, false, ndias - 31, "D"));
                                } else {
                                    fechaF = sdf.format(this.getDCurrentDate());
                                    escribir("Fecha final de perfil de carga " + sdf3.format(this.getDCurrentDate()));
                                }
                                fechaF_TS = new Timestamp(sdf.parse(fechaF).getTime());
                                /*
                                        Timestamp fechafin = new Timestamp((new Date().getTime()));
                                        fecha = sdf.format(new Date(fechafin.getTime()));
                                        escribir("Fecha final peticion de perfil de carga " + fecha);
                                 */
                                lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(0, 2)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion de year
                                data[23] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                //**************************************
                                //calculo mes
                                lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(2, 4)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion mess
                                data[22] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                //**************************************
                                //calculo dia
                                lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(4, 6)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion dia      
                                byte offset2 = calcularOffset(fechaF_TS, lfecha);
                                data[21] = (byte) offset2;
                                //**************************************
                                //calculo hora
                                lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(6, 8)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion hora                                                                       
                                data[20] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                //**************************************
                                //calculo min
                                lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(8, 10)));
                                while (lfecha.length() < 2) {
                                    lfecha = "0" + lfecha;
                                }
                                //asignacion min                                                                       
                                data[19] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                //**************************************
                                intercambio = 0;
                                //data[12]= 0x17;
                                //cambio diego uso de timeout
                                //tiempo = 500 * timeout;
                                data = calculaChecksum(data);
                                ////System.out.println("=> " + tramas.encode(data, data.length));
                                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                                enviaTrama2_2(data, "=> Solicitud de perfil de carga");
                            } catch (Exception e) {
                                escribir(getErrorString(e.getStackTrace(), 3));
                                cerrarPuerto();
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error solicitud perfil de carga");
                                escribir("Estado lectura No leido");                                
                                cerrarLog("Desconexion Error solicitud de perfil de carga", false);
                                leer = false;
                            }
                        } else {
                            cerrarPuerto();
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Desfase horario");
                            escribir("Estado lectura No leido");                            
                            cerrarLog("Desconexion Desfase horario", false);
                            leer = false;
                        }
                    } else if (leventos) {
                        //*******Solicitud de eventos
                        regeventos = true;
                        descripcion = "Eventos";
                        this.ASDU = "66";                        
                        byte[] data = asignaDireccion(tramas.getDatoseventos());
                        int ndiaseventos = med.getNdiaseventos();
                        try {
                            //**************Fecha inicial*****************************************
                            Timestamp fechaini = new Timestamp((new Date().getTime() - (long) (86400000L * ndiaseventos)));
                            String fecha = sdf.format(new Date(fechaini.getTime()));
                            ////System.out.println("Fecha calculada eventos" + new Date(new Date().getTime() - (long) (86400000L * ndiaseventos)));
                            ////System.out.println("Fecha peticion eventos  " + sdf3.format(new Date((new Date().getTime() - (long) (86400000L * ndiaseventos)))));
                            escribir("Fecha peticion de registro de eventos " + fecha);
                            //calculo year
                            String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 2)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion de year
                            data[17] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //calculo mes
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(2, 4)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion mess
                            data[16] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //calculo dia
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            byte offset = calcularOffset(fechaini, lfecha);
                            //asignacion dia                                                                       
                            data[15] = (byte) offset;
                            //calculo hora
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion hora                                                                       
                            data[14] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //calculo min
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(8, 10)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion min                                                                       
                            data[13] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //*********************calculo de fecha final*************************
                            Timestamp fechafin = new Timestamp(new Date().getTime());
                            fecha = sdf.format(new Date(fechafin.getTime()));

                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 2)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion de year
                            data[22] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //calculo mes
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(2, 4)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion mess
                            data[21] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //calculo dia
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            byte offset2 = calcularOffset(fechafin, lfecha);
                            //asignacion dia                                                                       
                            data[20] = (byte) offset2;
                            //calculo hora
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion hora                                                                       
                            data[19] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //calculo min
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(8, 10)));
                            while (lfecha.length() < 2) {
                                lfecha = "0" + lfecha;
                            }
                            //asignacion min                                                                       
                            data[18] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            intercambio = 0;
                            data = calculaChecksum(data);
                            ////System.out.println("=> " + tramas.encode(data, data.length));
                            ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                            enviaTrama2_2(data, "=> Solicitud de envio de Registro de eventos");
                        } catch (Exception e) {
                            escribir(getErrorString(e.getStackTrace(), 3));
                            cerrarPuerto();
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error solicitud Eventos");
                            escribir("Estado lectura No leido");                            
                            cerrarLog("Desconexion Error solicitud eventos", false);
                            leer = false;
                        }
                    } else if (lconfHora) {
                        //****************Configuracion de hora*****//
                        try {
                            time = this.obtenerHora();
                        } catch (Exception e) {
                            e.printStackTrace();
                            time = this.obtenerHora();
                            ////System.out.println("Sincronizando hora con equipo local " + time);
                            escribir("Sincronizando hora con equipo local " + time);
                            escribir("Fecha equipo local" + time);
                        }
                        byte data[] = asignaDireccion(tramas.getSynchora());
                        String fecha = sdf4.format(new Date(time.getTime()));
                        ////System.out.println("Fecha a syncronizar" + new Date(time.getTime()));
                        escribir("Fecha peticion de sincronizacion " + fecha);
                        //calculo year
                        String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 2)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        //asignacion de year
                        data[19] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        //calculo mes
                        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(2, 4)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        //asignacion mess
                        data[18] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        //calculo dia
                        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        byte offset = calcularOffset(time, lfecha);
                        //asignacion dia                                                                       
                        data[17] = (byte) offset;
                        //calculo hora
                        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        //asignacion hora                                                                       
                        data[16] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        //calculo min
                        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(8, 10)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        //asignacion min                                                                       
                        data[15] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);

                        //calculo seg 
                        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(10, 12) + "000"));
                        while (lfecha.length() < 4) {
                            lfecha = "0" + lfecha;
                        }
                        //asignacion seg                                                                       
                        data[14] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                        data = calculaChecksum(data);
                        //asignacion mseg

                        configuracionhora = true;
                        descripcion = "Sincronización de Reloj";
                        ////System.out.println("=> " + tramas.encode(data, data.length));
                        cp.saveAcceso(med.getnSerie(), "2", sdfacceso.format(new Date(tsfechaactual.getTime())), sdfacceso.format(new Date(time.getTime())), usuario, null);
                        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                        enviaTrama2_2(data, "=> Solicitud de envio de configuracion de hora");
                    } else {
                        //******finalizacion de session*******//
                        logout = true;
                        byte[] data = asignaDireccion(tramas.getLogout());
                        data = calculaChecksum(data);
                        ////System.out.println("=> " + tramas.encode(data, data.length));
                        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                        enviaTrama2_2(data, "=> Solicitud de fin de session");
                    }
                } else {
                    escribir("Error de Recepcion de trama");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
                    cerrarPuerto();
                    escribir("Estado lectura No leido");                    
                    cerrarLog("Desconexion Error recepcion", false);
                    leer = false;
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error recepcion", false);
            leer = false;
        }
    }

    private void revisarPerfilCarga(String[] vectorhex) {
        if (vectorhex[0].equals("10")) {//es un ack
            if (validaCheckSum(vectorhex)) {//valida checksum
                if (vectorhex[1].substring(1, 2).equals("0")) { //es un ack
                    byte[] data = asignaDireccion(tramas.getSolicitud());
                    data[1] = (byte) 0x5B;
                    intercambio = 0;
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara                       
                    arrayperfil = new ArrayList<>();
                    enviaTrama2_2(data, "=> Solicitud de envio de informacion");
                } else {
                    escribir("Error datos no disponibles");
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error Datos no disponibles");
                    escribir("Estado lectura No leido");                    
                    cerrarLog("Desconexion Error datos no disponibles", false);
                    leer = false;
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum                    
                //se debe validar la informacion  entre las cuales deben ser
                /* 1. Verificar la causa de transmision 07 es afirmacion de perfil de carga, 0B para recepcion de datos y 0A 
                     2. Verificar si el inicio de informacion
                     3. Validar el conntrol de ultima trama de envio
                     4. Validar el bit que cambia de 0 a 1 si envio con 0 debe responder con 0 y viceversa*/
                perfilincompleto = true;
                if (vectorhex[9].equals("07")) {//causa de transmision.
                    byte control = 0x5B;
                    // debe cambiar el intercambio
                    if (intercambio == 0) {
                        control = 0x7B;
                        intercambio = 1;
                    } else {
                        control = 0x5B;
                        intercambio = 0;
                    }
                    this.ASDU = "8C";
                    byte[] data = asignaDireccion(tramas.getSolicitud());
                    data[1] = control;
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                    enviaTrama2_2(data, "=> Solicitud de envio de datos");
                } else {
                    String bin = Integer.toBinaryString((Integer.parseInt(vectorhex[4], 16)));
                    while (bin.length() < 8) {
                        bin = "0" + bin;
                    }
                    //se valida el esperado
                    if (bin.substring(4, bin.length()).equals("1000")) { //vienen datos
                        if (vectorhex[9].equals("0A")) {//es la ultima trama         
                            perfilcarga = false;
                            perfilincompleto = false;
                            //se valida si se solicita los eventos o se termina la session  
                            //**** Solicitud de eventos****//
                            if (leventos) {// se solicita los eventos
                                regeventos = true;
                                descripcion = "Eventos";
                                this.ASDU = "01";                                
                                byte[] data = asignaDireccion(tramas.getDatoseventos());
                                int ndiaseventos = med.getNdiaseventos();
                                try {
                                    //**************Fecha inicial*****************************************
                                    Timestamp fechaini = new Timestamp((new Date().getTime() - (long) (86400000L * ndiaseventos)));
                                    String fecha = sdf.format(new Date(fechaini.getTime()));
                                    ////System.out.println("Fecha calculada eventos" + new Date(new Date().getTime() - (long) (86400000L * ndiaseventos)));
                                    ////System.out.println("Fecha peticion eventos  " + sdf3.format(new Date((new Date().getTime() - (long) (86400000L * ndiaseventos)))));
                                    escribir("Fecha peticion de registro de eventos " + fecha);
                                    //calculo year
                                    String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 2)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion de year
                                    data[17] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //calculo mes
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(2, 4)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion mess
                                    data[16] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //calculo dia
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    byte offset = calcularOffset(fechaini, lfecha);
                                    //asignacion dia                                                                       
                                    data[15] = (byte) offset;
                                    //calculo hora
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion hora                                                                       
                                    data[14] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //calculo min
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(8, 10)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion min                                                                       
                                    data[13] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //*********************calculo de fecha final*************************
                                    Timestamp fechafin = new Timestamp(new Date().getTime());
                                    fecha = sdf.format(new Date(fechafin.getTime()));

                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 2)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion de year
                                    data[22] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //calculo mes
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(2, 4)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion mess
                                    data[21] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //calculo dia
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    byte offset2 = calcularOffset(fechafin, lfecha);
                                    //asignacion dia                                                                       
                                    data[20] = (byte) offset2;
                                    //calculo hora
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion hora                                                                       
                                    data[19] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    //calculo min
                                    lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(8, 10)));
                                    while (lfecha.length() < 2) {
                                        lfecha = "0" + lfecha;
                                    }
                                    //asignacion min                                                                       
                                    data[18] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    intercambio = 0;
                                    data = calculaChecksum(data);
                                    ////System.out.println("=> " + tramas.encode(data, data.length));
                                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                                    enviaTrama2_2(data, "=> Solicitud de envio de Registro de eventos");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    cerrarPuerto();
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error solicitud eventos");
                                    escribir("Estado lectura No leido");                                    
                                    cerrarLog("Desconexion Error solicitud eventos", false);
                                    leer = false;
                                }
                            } else {
                                //finaliza la session.
                                logout = true;
                                byte[] data = asignaDireccion(tramas.getLogout());
                                data = calculaChecksum(data);
                                ////System.out.println("=> " + tramas.encode(data, data.length));
                                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                                enviaTrama2_2(data, "=> Solicitud de fin de session");
                            }
                        } else {
                            arrayperfil.add(vectorhex);
                            byte control = 0x5B;
                            // debe cambiar el intercambio
                            if (intercambio == 0) {
                                control = 0x7B;
                                intercambio = 1;
                            } else {
                                intercambio = 0;
                            }
                            byte[] data = asignaDireccion(tramas.getSolicitud());
                            data[1] = control;
                            data = calculaChecksum(data);
                            ////System.out.println("=> " + tramas.encode(data, data.length));
                            ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                            enviaTrama2_2(data, "=> Solicitud de envio de datos");
                        }
                    } else {
                        escribir("Error trama no esperada");
                        perfilcarga = false;
                        logout = true;
                        byte[] data = asignaDireccion(tramas.getLogout());
                        data = calculaChecksum(data);
                        ////System.out.println("=> " + tramas.encode(data, data.length));
                        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                        enviaTrama2_2(data, "=> Solicitud de fin de session");
                    }
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error recepcion", false);
            leer = false;
        }
    }

    private void revisarConfiguracionHora(String[] vectorhex) {
        if (vectorhex[0].equals("10")) {//es un ack
            if (validaCheckSum(vectorhex)) {//valida checksum                
                if (validaACK(vectorhex)) {
                    byte[] data = asignaDireccion(tramas.getSolicitud());
                    data[1] = (byte) 0x5B;
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                    enviaTrama2_2(data, "=> Solicitud de envio de informacion");                    
                } else {
                    escribir("Error de configuracion de hora");
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error configuracion hora");
                    escribir("Estado lectura No leido");                    
                    cerrarLog("Desconexion Error configuracion hora", false);
                    leer = false;
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {
            if (validaCheckSum(vectorhex)) {
                if (validaACK(vectorhex)) {
                    configuracionhora = false;
                    logout = true;
                    byte[] data = asignaDireccion(tramas.getLogout());
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                    enviaTrama2_2(data, "=> Solicitud de fin de session");
                } else {
                    escribir("Error de configuracion de hora");
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error configuracion hora");
                    escribir("Estado: Establecimiento de fecha y hora no exitoso.");                    
                    cerrarLog("Desconexion Error configuracion hora", false);
                    leer = false;
                }                
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error recepcion", false);
            leer = false;
        }
    }

    private void revisarRegEventos(String[] vectorhex) {
        if (vectorhex[0].equals("10")) {//es un ack
            if (validaCheckSum(vectorhex)) {//valida checksum
                byte[] data = asignaDireccion(tramas.getSolicitud());
                data[1] = (byte) 0x5B;
                data = calculaChecksum(data);
                ////System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                arrayeventos = new ArrayList<>();
                enviaTrama2_2(data, "=> Solicitud de envio de informacion");
            } else {
                escribir("Error de checksum");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                cerrarPuerto();
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else if (vectorhex[0].equals("68")) {//es una trama de datos
            //if (validaTamaño(vectorhex)) {//validamos tamaño de la trama
            if (validaCheckSum(vectorhex)) {//valida checksum
                if (vectorhex[9].equals("0D")) {//causa de transmision datos no disponibles .
                    escribir("Datos No disponibles");
                    escribir("Estado lectura No leido");
                    regeventos = false;
                    //finaliza la session.
                    logout = true;
                    byte[] data = asignaDireccion(tramas.getLogout());
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                    enviaTrama2_2(data, "=> Solicitud de fin de session");
                } else if (vectorhex[9].equals("07")) {//causa de transmision.
                    byte control = 0x5B;
                    // debe cambiar el intercambio
                    if (intercambio == 0) {
                        control = 0x7B;
                        intercambio = 1;
                    } else {
                        control = 0x5B;
                        intercambio = 0;
                    }
                    this.ASDU = "01";
                    byte[] data = asignaDireccion(tramas.getSolicitud());
                    data[1] = control;
                    data = calculaChecksum(data);
                    ////System.out.println("=> " + tramas.encode(data, data.length));
                    ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                    enviaTrama2_2(data, "=> Solicitud de envio de datos");
                } else {
                    String bin = Integer.toBinaryString((Integer.parseInt(vectorhex[4], 16)));
                    while (bin.length() < 8) {
                        bin = "0" + bin;
                    }
                    //se valida el esperado
                    if (bin.substring(4, bin.length()).equals("1000")) { //vienen datos
                        if (vectorhex[9].equals("0A")) {//es la ultima trama         
                            regeventos = false;
                            //finaliza la session.
                            logout = true;
                            byte[] data = asignaDireccion(tramas.getLogout());
                            data = calculaChecksum(data);
                            ////System.out.println("=> " + tramas.encode(data, data.length));
                            ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                            enviaTrama2_2(data, "=> Solicitud de fin de session");
                        } else {
                            arrayeventos.add(vectorhex);
                            byte control = 0x5B;
                            // debe cambiar el intercambio
                            if (intercambio == 0) {
                                control = 0x7B;
                                intercambio = 1;
                            } else {
                                intercambio = 0;
                            }
                            byte[] data = asignaDireccion(tramas.getSolicitud());
                            data[1] = control;
                            data = calculaChecksum(data);
                            ////System.out.println("=> " + tramas.encode(data, data.length));
                            ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                            enviaTrama2_2(data, "=> Solicitud de envio de datos");
                        }
                    } else {
                        escribir("Error trama no esperada");
                        regeventos = false;
                        logout = true;
                        byte[] data = asignaDireccion(tramas.getLogout());
                        data = calculaChecksum(data);
                        ////System.out.println("=> " + tramas.encode(data, data.length));
                        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                        enviaTrama2_2(data, "=> Solicitud de fin de session");
                    }
                }
            } else {
                escribir("Error de checksum");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
                escribir("Estado lectura No leido");                
                cerrarLog("Desconexion Error checksum", false);
                leer = false;
            }
        } else {
            escribir("Error de recepcion");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error checksum", false);
            leer = false;
        }
    }

    private void revisarLogout(String[] vectorhex) {
        if (validaCheckSum(vectorhex)) {//valida checksum
            cerrarPuerto();            
            if (lperfil) {
                //almacenamos perfil de carga
                try {
                    almacenaPerfil();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (leventos) {
                //eventos
                try {
                    almacenaEventos();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (lregistros) {
                //almacena registros totales
            }            
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura correcta");
            med.MedLeido = true;
            cerrarLog("Leido", true);
            leer = false;
        } else {
            escribir("Error de checksum");
            cerrarPuerto();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error checksum");
            escribir("Estado lectura No leido");            
            cerrarLog("Desconexion Error checksum", false);
            leer = false;
        }
    }

    private void interpretaCadena(String cadenahex) throws ParseException {
        try {
            escribir("Recibe <= " + cadenahex);
            //vector de tramas
            String[] vectorhex = cadenahex.split(" ");
            //protocolo 
            if (iniciosession) {//inicio de session y envio de passsword
                revisarInicioSession(vectorhex);
            } else if (comunicacion) {
                revisarComunicacion(vectorhex);
            } else if (serialnumber) {
                revisarSerialNumber(vectorhex);
            } else if (confcanales) {
                revisarConfCanales(vectorhex);
            } else if (confPerfil) {
                revisarConfPerfil(vectorhex);
            } else if (elemntosPerfil) {
                revisarElementosPerfil(vectorhex);
            } else if (fecactual) {
                revisarFecActual(vectorhex);
            } else if (perfilcarga) {
                revisarPerfilCarga(vectorhex);
            } else if (configuracionhora) {
                revisarConfiguracionHora(vectorhex);
            } else if (regeventos) {
                revisarRegEventos(vectorhex);
            } else if (logout) {
                revisarLogout(vectorhex);
            } else {
                throw new Exception("Estado desconocido");
            }
        } catch (Exception e) {
            cerrarPuerto();
            escribir("Estado lectura No leido");
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Error al interpretar la respuesta.");
            cerrarLog("Error en la etapa de interpretación", false);
            leer = false;
        }
    }

    public void escribir(String dato) {
        try {
            if (file != null) {
                d = Calendar.getInstance().getTime();
                fr = new RandomAccessFile(file, "rw");
                fr.seek(fr.length());
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write(dato.getBytes(), 0, dato.getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.close();
            }
        } catch (Exception e) {
        }
    }

    public void cerrarPuerto() {
        try {
            if (output != null) {
                output.flush();
                output.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            escucha = false;
            if (socket != null) {
                socket.close();
            }
        } catch (Exception p) {
            System.err.println(p.getMessage());
        }
        try {
            String mensaje = "FIN DE SESSION --" + new Timestamp(new Date().getTime()) + "--";
            escribir(mensaje);
            escribir("\r\n\n");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            enviando = false;
            reenviando = false;
            portconect = false;
            Thread.sleep((long) 5000);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    //metodo de envio de tramas y reenvios para los casos de reconexion (segunda vuelta)
    private void enviaTrama2_2(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;
        enviando = true;
        tEscritura = new Thread() {
            @Override
            public void run() {
                synchronized (monitor) {
                    try {
                        int intentosRetransmision = 0;
                        boolean t = true;
                        while (t) {
                            if (enviando || reenviando) {
                                if (intentosRetransmision != 0) {
                                    escribir("TimeOut, Intento de reenvio..");
                                    if (trama[0] == 104) {
                                        acksEsperados++;
                                    }
                                }
                                escribir(des);
                                escribir("=> " + tramas.encode(trama, trama.length));
                                try {
                                    monitor.notifyAll();
                                    enviaTrama(trama);
                                    monitor.wait();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    escribir("Error enviando trama tipo 2_2");
                                }
                            } else {
                                t = false;
                            }
                            if (reenviando && intentosRetransmision <= 2) {
                                intentosRetransmision++;
                            } else if (reenviando && intentosRetransmision > 2) {
                                interrumpirHilo(tLectura);
                                escribir("Numero de reenvios agotado");
                                enviando = false;
                                t = false;
                                cierrapuerto = true;
                            } else {
                                return;
                            }
                        }
                        if (cierrapuerto) {
                            cierrapuerto = false;
                            cerrarPuerto();
                            monitor.notifyAll();
                            if (perfilcarga & perfilincompleto) {
                                AlmacenarRegistrosIncompletos();
                            }
                            if (reintentoconexion <= numeroReintentos) {
                                if (!objabortar.labortar) {
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion timeout");
                                    tReinicio = new Thread() {
                                        @Override
                                        public void run() {
                                            reintentoconexion ++;
                                            abrePuerto();
                                        }
                                    };
                                    tReinicio.start();
                                } else {
                                    escribir("Proceso Abortado");
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Proceso Abortado");
                                    cerrarLog("Abortado", false);
                                    leer = false;
                                }
                            } else {
                                ////System.out.println("Reintentos agotados.");
                                escribir("Numero de reintentos agotado");
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado");                                
                                escribir("Estado Lectura No leido");
                                cerrarLog("Desconexion Numero de reintentos agotado", false);
                                leer = false;
                            }
                        }
                    } catch (Exception e) {
                        cerrarPuerto();
                        escribir("Error en hilo envia trama tipo");
                        escribir(getErrorString(e.getStackTrace(), 3));
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Error en hilo de envio de data.");
                        cerrarLog("Error en hilo envia trama", false);
                        leer = false;
                    }
                }
            }
        };
        tEscritura.start();
    }

    //metodo envio basico
    private void enviaTrama(byte[] bytes) {
        try {
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal                        
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    public boolean validaACK(String[] vectorhex) {
        boolean valida = false;
        if (vectorhex[0].equals("10")) {
            if ((Integer.parseInt(vectorhex[1], 16) % 32) == 8 || (Integer.parseInt(vectorhex[1], 16) % 32) == 0) {//validamos ack
                valida = true;
            }
        } else if (vectorhex[0].equals("68")) {
            if ((Integer.parseInt(vectorhex[4], 16) % 32) == 8 || (Integer.parseInt(vectorhex[4], 16) % 32) == 0) {//validamos ack
                valida = true;
            }
        }

        return valida;
    }

    public boolean validaCheckSum(String[] vectorhex) {
        boolean valida = false;
        try {
            //verificamos si es de longitud fija
            int dato = 0;

            if (vectorhex[0].equals("10")) {
                for (int i = 1; i < vectorhex.length - 2; i++) {
                    dato = dato + (Integer.parseInt(vectorhex[i], 16));
                }
                if ((dato % 256) == Integer.parseInt(vectorhex[vectorhex.length - 2], 16)) {
                    valida = true;
                }
            } else if (vectorhex[0].equals("68")) {
                for (int i = 4; i < vectorhex.length - 2; i++) {
                    dato = dato + (Integer.parseInt(vectorhex[i], 16));
                }
                if ((dato % 256) == Integer.parseInt(vectorhex[vectorhex.length - 2], 16)) {
                    valida = true;
                }
            } else {
                //no se puede validar
                valida = false;
            }
        } catch (Exception e) {
            valida = false;
            e.printStackTrace();
        }
        return valida;
    }

    public boolean validaTamaño(String[] vectorhex) {
        boolean valida = false;

        try {
            if (vectorhex[0].equals("68")) {
                int tamaño = Integer.parseInt(vectorhex[1], 16);
                int j = 0;
                for (int i = 4; i < vectorhex.length - 2; i++) {
                    j++;
                }
                ////System.out.println("Tamaño Trama " + Integer.parseInt(vectorhex[1], 16));
                ////System.out.println("Tamaño datos " + j);
                if (j == tamaño) {
                    valida = true;
                }
            } else if (vectorhex[0].equals("10")) {
                if (vectorhex.length == 6) {
                    valida = true;
                }
            } else {
                valida = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valida;
    }

    public byte[] calculaChecksum(byte[] data) {
        byte dato = 0x00;
        if (data[0] == 0x10) {
            for (int i = 1; i < data.length - 2; i++) {
                //////System.out.println("Checksum "+Integer.toHexString(0x100 | dato).substring(1).toUpperCase());
                dato += (data[i]);
            }
            ////System.out.println("Checksum " + Integer.toHexString(0x100 | dato).substring(1).toUpperCase());
            data[data.length - 2] = (byte) (dato % 0x100);
        } else if (data[0] == 0x68) {
            for (int i = 4; i < data.length - 2; i++) {
                //////System.out.println("suma  "+Integer.toHexString(dato).toUpperCase());
                dato += (data[i]);
            }
            ////System.out.println("Checksum " + Integer.toHexString(0x100 | dato).substring(1).toUpperCase());
            data[data.length - 2] = (byte) (dato % 0x100);
        }
        return data;
    }
    
    public String[] Cortar(String vectorhex[]) {
        String nuevoVector[] = vectorhex;
        int indiceInicio = 0;
        if (vectorhex.length >= 3) {
            for (int i = 0; i < vectorhex.length - 2; i++) {
                if (vectorhex[i].equals("68") || vectorhex[i].equals("10")) {
                    nuevoVector = new String[vectorhex.length - indiceInicio];
                    System.arraycopy(vectorhex, i, nuevoVector, 0, nuevoVector.length);
                    break;
                } else {
                    indiceInicio++;
                }
            }
        }
        ////System.out.println("Trama cortada " + Arrays.toString(vectorhex));
        return nuevoVector;
    }

    public void AlmacenarRegistrosIncompletos() {
        try {
            almacenaPerfil();
        } catch (Exception e) {
        }
        try {            
        } catch (Exception e) {
        }
        escribir("Estado Lectura Leido Incompleto");
    }

    public void almacenaEventos() {
        String desglose = "";
        //juntamos todos los bloques en una cadena para programar byte por byte
        for (String data[] : arrayeventos) {
            if (data[0].equals("68")) {//es una trama de longitud variable.
                int tam = Integer.parseInt(data[1], 16) - 3;
                String vperfil[] = new String[tam];
                System.arraycopy(data, 7, vperfil, 0, vperfil.length);
                for (String str : vperfil) {
                    desglose = desglose + str;
                }
            }
        }
        int j = 0;
        String descon = "";
        String recon = "";
        ERegistroEvento registroEvento = null;
        while (j < desglose.length()) {
            //numero de eventos
            int neventos = 0;
            j += 2;
            neventos = Integer.parseInt(desglose.substring(j, j + 2), 16);
            j += 10;
            for (int p = 1; p <= neventos; p++) {
                String spa = "" + Integer.parseInt(desglose.substring(j, j + 2), 16);
                String spq = "" + (int) (Integer.parseInt(desglose.substring(j + 2, j + 4), 16) / 2);
                j += 4;
                //fecha y hora
                //String fecha = Integer.parseInt(desglose.substring(j + 12, j + 14), 16) + "" + completarCeros(Integer.parseInt(desglose.substring(j + 10, j + 12), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 8, j + 10)), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 6, j + 8)), 16), 2) + "" + completarCeros(Integer.parseInt(desglose.substring(j + 4, j + 6), 16), 2) + "" + completarCeros(Integer.parseInt(desglose.substring(j + 2, j + 4), 16), 2);
                String fecha = Integer.parseInt(desglose.substring(j + 12, j + 14), 16) + "" + completarCeros(Integer.parseInt(desglose.substring(j + 10, j + 12), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 8, j + 10)), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 6, j + 8)), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 4, j + 6)), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 2, j + 4)), 16), 2);
                ////System.out.println("Fecha Evento " + fecha);
                j += 14;
                if (spa.equals("1") && spq.equals("1")) {
                    //Reconexion
                    if (descon.length() > 0) {// ya se tiene fecha de un evento de desconexion por lo tanto se almacena la fecha de reconexion.
                        recon = fecha;
                        ////System.out.println("Reconexion");
                    }
                } else if (spa.equals("1") && spq.equals("2")) {
                    //Reconexion
                    if (descon.length() > 0) {// ya se tiene fecha de un evento de desconexion por lo tanto se almacena la fecha de reconexion.
                        recon = fecha;
                        ////System.out.println("Reconexion");
                    }
                } else if (spa.equals("3") && spq.equals("0")) {
                    //Desconexion
                    descon = fecha;
                    ////System.out.println("Descionexion");
                }
                if (recon.length() > 0 && descon.length() > 0) {
                    try {
                        ////System.out.println("Evento desconex " + descon + "  ///   Reconex" + recon);
                        registroEvento = new ERegistroEvento();
                        registroEvento.setVcfechacorte(new Timestamp(sdf4.parse(descon).getTime()));
                        registroEvento.setVcfechareconexion(new Timestamp(sdf4.parse(recon).getTime()));
                        registroEvento.setVctipo("0001");
                        registroEvento.setVcserie(med.getnSerie());
                        cp.actualizaEvento(registroEvento, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    descon = "";
                    recon = "";
                }
            }
        }
    }

    public void almacenaPerfil() {
        String desglose = "";
        //juntamos todos los bloques en una cadena para programar byte por byte
        for (String data[] : arrayperfil) {
            if (data[0].equals("68")) {//es una trama de longitud variable.
                int tam = Integer.parseInt(data[1], 16) - 3;
                //escribir("Array data "+Arrays.toString(data));
                String vperfil[] = new String[tam];
                System.arraycopy(data, 7, vperfil, 0, vperfil.length);
                //escribir("Array vperfil "+Arrays.toString(vperfil));
                for (String str : vperfil) {
                    desglose = desglose + str;
                }
            }
        }
        //escribir("Desglose: "+desglose);
        //recorremos la cadena avanzando de 2 caracteres.
        int j = 0, tries = 0;
        boolean isValid = false;
        ArrayList<String> lecturas;
        ArrayList<Boolean> lecturasValidas;
        Vector<Electura> arraylec = new Vector<>();
        Electura lec;
        EConstanteKE econske = null;
        //Vector<EConstanteKE> lconske = cp.buscarConstantesKe(med.getnSerie(), null);//se toman los valores de las constantes 
        //ArrayList<EtipoCanal> vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo(), null); //se toman los valores de los tipos de canales configurados        
        Timestamp timeufec = null;
        String canal = "";
        Timestamp ultimoIntervalo = null;

        while (j < desglose.length()) {
            //caputramos el numero de objetos de informacion.
            //////System.out.println("nuevo bloque indice en " + j);
            int nintervalos = 0;
            j += 2;
            nintervalos = Integer.parseInt(desglose.substring(j, j + 2), 16);
            escribir("Número de Intervalos: "+nintervalos);
            j += 10;
            //direccion del objeto 9=8 canales, 10=6canales, 11=3 Canales
            //String valorbin = "";
            for (int p = 1; p <= nintervalos; p++) {
                String direcionobjeto = desglose.substring(j, j + 2);
                //escribir("Dirección objeto: "+direcionobjeto);
                int ncanales = 0;
                ncanales = direcionobjeto.equals("09") ? 8 : ncanales;
                ncanales = direcionobjeto.equals("10") ? 6 : ncanales;
                ncanales = direcionobjeto.equals("11") ? 3 : ncanales;
                lecturas = new ArrayList<>();
                lecturasValidas = new ArrayList<>();
                //recorremos los canales obteniendo la lectura con bloques de 5 bytes en litle endian
                if (ncanales > 0) {
                    j += 2;
                }
                escribir("Número Canales: "+ ncanales);
                for (int i = 0; i < ncanales; i++) {
                    //valorbin = Integer.toBinaryString(Integer.parseInt(desglose.substring(j + 8, j + 10), 16));
                    /*
                    while (valorbin.length() < 8) {
                        valorbin = "0" + valorbin;
                    }
                    */
//////                    System.out.println("desglose: "+desglose.substring(j + 6, j + 8) + desglose.substring(j + 4, j + 6) + desglose.substring(j + 2, j + 4) + desglose.substring(j, j + 2));
//////                    System.out.println("resolución: "+resolucion);
                    isValid = true ;
                    //isValid = !isBitSet( desglose.substring( j + 8, j + 10), 7 ) ;
                    if (isValid) {
                        lecturas.add(desglose.substring(j + 6, j + 8) + desglose.substring(j + 4, j + 6) + desglose.substring(j + 2, j + 4) + desglose.substring(j, j + 2));
                        lecturasValidas.add(true);
                    } else {
                        lecturas.add("00000000");
                        lecturasValidas.add(false);
                    }                                        
                    j += 10;
                }
                //capturamos la fecha en formato yyMMddHHmm
                String fecha = completarCeros(Integer.parseInt(desglose.substring(j + 8, j + 10), 16), 2) + "" + completarCeros(Integer.parseInt(desglose.substring(j + 6, j + 8), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 4, j + 6)), 16), 2) + "" + completarCeros(Integer.parseInt(validarOffSet(desglose.substring(j + 2, j + 4)), 16), 2) + "" + (Integer.parseInt(desglose.substring(j, j + 2), 16) == 0 ? "00" : Integer.parseInt(desglose.substring(j, j + 2), 16));
                escribir("Fecha intervalo: " + fecha);                
                int aumento = 0;
                //*************validacion de huecos
                Timestamp d = null;
                try {
                    if (ultimoIntervalo != null) {//si tiene una fecha inicial
                        //escribir("Último Intervalo: "+ultimoIntervalo);
                        if (new Timestamp(sdf.parse(fecha).getTime()).after(ultimoIntervalo)) {//la lectura es mayor al ultimo intervalo
                            aumento = (int) Math.abs(((new Timestamp(sdf.parse(fecha).getTime()).getTime() - ultimoIntervalo.getTime()) / 60000) / intervalo) - 1;
                            //escribir("Aumento: "+aumento);
                        }
                        if (aumento > 0) {//obtiene el numero de intervalos a mover
                            for (int i = 0; i < aumento; i++) {
                                d = new Timestamp(ultimoIntervalo.getTime() + (60000 * intervalo) * (i + 1));//movemos la fecha
                                //escribir("fecha intervalo en 0 (hueco) " + fecha);
                                for (int k = 0; k < ncanales; k++) {
                                    try {
                                        econske = cp.buscarConske(lconske, Integer.parseInt(vconfperfil.get(k), 16)); //buscamos el valor de la constante creada en telesimex
                                        if (econske != null) {
                                            canal = "";                                            
                                            for (EtipoCanal et : vtipocanal) {
                                                if (Integer.parseInt(et.getCanal()) == Integer.parseInt(vconfperfil.get(k), 16)) {
                                                    canal = et.getUnidad();
                                                    break;
                                                }
                                            }
                                            lec = new Electura(d, med.getnSerie(), Integer.parseInt(vconfperfil.get(k), 16), 0.0, 0, intervalo, canal);
                                            escribir("Lectura fecha: " + lec.getFecha() + ",  Canal: " + lec.getCanal() + ", Lectura nula sin trasnformar: " + 0.0 + ", lec: " + 0.0 + ", unidad: " + lec.getVccanal() + ", Intervalo: " + intervalo);
                                            arraylec.add(lec);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //**********Termina validacion huecos
                j += 10;
                //recorremos los canales para construir el objeto de lectura
                for (int i = 0; i < ncanales; i++) {
                    try {
                        econske = cp.buscarConske(lconske, Integer.parseInt(vconfperfil.get(i), 16)); //buscamos el valor de la constante creada en telesimex
                        //////System.out.println("EconsKe: "+econske);
                        if (econske != null) {
                            canal = "";
                            for (EtipoCanal et : vtipocanal) {
                                //escribir("Entidad Canal: " + Integer.parseInt(et.getCanal()));
                                //escribir("Canal Medidor: " + Integer.parseInt(vconfperfil.get(i), 16));
                                if (Integer.parseInt(et.getCanal()) == Integer.parseInt(vconfperfil.get(i), 16)) {
                                    canal = et.getUnidad();
                                    //escribir("MATCH");
                                    break;
                                }
                            }
                            lec = new Electura(new Timestamp(sdf.parse(fecha).getTime()), med.getnSerie(), Integer.parseInt(vconfperfil.get(i), 16), ((((double) Integer.parseInt(lecturas.get(i), 16)) / resolucion) * econske.getMultiplo() * econske.getPesopulso()) / econske.getDivisor(), (Integer.parseInt(lecturas.get(i), 16)), intervalo, canal);
                            if ( !lecturasValidas.get(i) ) { // Si no es valida
                                cp.escribirLog(usuario, obtenerHora(), med.getnSerie(), label, "Lectura Invalida, Intervalo: " + lec.getFecha() + ", Canal: " + lec.getCanal() );                            
                            }
                            escribir("Lectura fecha: " + lec.getFecha() + ", Canal: " + lec.getCanal() + ", Lectura sin transformar: " + lecturas.get(i) + ", lec: " + lec.getLec() + ", unidad: " + lec.getVccanal() + ", Intervalo: " + intervalo);
                            timeufec = new Timestamp(sdf.parse(fecha).getTime());
                            arraylec.add(lec);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ultimoIntervalo = timeufec;// variable que tiene como objetivo guardar el ultimo intervalo obtenido para comparar con el nuevo que viene
            }
            //completamos el intervalo y volvemos al inicio del siguiente bloque.
        }
        try {
            cp.actualizaLectura(arraylec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
            escribir("Actual fecha lectura: " + timeufec);
            if (timeufec != null) {
                cp.actualizaFechaLectura(med.getnSerie(), sdf4.format(new Date(timeufec.getTime())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String completarCeros(int valor, int numceros) {
        String data = "" + valor;
        while (data.length() < numceros) {
            data = "0" + data;
        }
        return data;
    }

    public String validaUnidad(String dato) {
        String retdato = "";
        switch (dato) {
            case "01":
                retdato = "kWhD";
                break;
            case "02":
                retdato = "kWhR";
                break;
            case "03":
                retdato = "kVarhD";
                break;
            case "04":
                retdato = "kVarhD";
                break;
            case "05":
                retdato = "kVarhR";
                break;
            case "06":
                retdato = "kVarhR";
                break;
        }
        return retdato;
    }

    public String validarOffSet(String value) {
        String offset = "";
        if (Integer.parseInt(value, 16) >= 32 && Integer.parseInt(value, 16) <= 63) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0x20);
            offset = Integer.toHexString(data).toUpperCase();
        } else if (Integer.parseInt(value, 16) >= 64 && Integer.parseInt(value, 16) <= 95) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0x40);
            offset = Integer.toHexString(data).toUpperCase();
        } else if (Integer.parseInt(value, 16) >= 96 && Integer.parseInt(value, 16) <= 127) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0x60);
            offset = Integer.toHexString(data).toUpperCase();
        } else if (Integer.parseInt(value, 16) >= 128 && Integer.parseInt(value, 16) <= 159) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0x80);
            offset = Integer.toHexString(data).toUpperCase();
        } else if (Integer.parseInt(value, 16) >= 160 && Integer.parseInt(value, 16) <= 191) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0xA0);
            offset = Integer.toHexString(data).toUpperCase();
        } else if (Integer.parseInt(value, 16) >= 192 && Integer.parseInt(value, 16) <= 223) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0xC0);
            offset = Integer.toHexString(data).toUpperCase();
        } else if (Integer.parseInt(value, 16) >= 224 && Integer.parseInt(value, 16) <= 255) {
            byte data = (byte) ((Integer.parseInt(value, 16) & 0xFF) - 0xE0);
            offset = Integer.toHexString(data).toUpperCase();
        } else {
            offset = value;
        }
        ////System.out.println("Off set " + offset);
        ////System.out.println("Dia " + Integer.parseInt(offset, 16));
        return offset;
    }

    public byte calcularOffset(Timestamp ts, String value) {
        byte offset = 0x00;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(ts.getTime());
            String calendario = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
            if (calendario.toUpperCase().equals("SUNDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0xE0);

            } else if (calendario.toUpperCase().equals("MONDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0x20);

            } else if (calendario.toUpperCase().equals("TUESDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0x40);

            } else if (calendario.toUpperCase().equals("WEDNESDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0x60);

            } else if (calendario.toUpperCase().equals("THURSDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0x80);

            } else if (calendario.toUpperCase().equals("FRIDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0xA0);

            } else if (calendario.toUpperCase().equals("SATURDAY")) {
                offset = (byte) ((Integer.parseInt(value, 16) & 0xFF) + 0xC0);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return offset;
    }

    public void terminaHilos() {
        try {
            tEscritura.interrupt();
        } catch (Exception e) {
        }
        try {
            tReinicio.interrupt();
        } catch (Exception e) {
        }        
        tEscritura = null;
        tReinicio = null;
    }
    
    public Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }

    public Date getDCurrentDate() throws ParseException {
        return Date.from(ZonedDateTime.now(zid).toInstant());
    }

    public String getSpecificDate(SimpleDateFormat isdf, boolean backForward, int val, String opc, String specificDate) throws ParseException {
        String ans;
        switch (opc) {
            case "D":
                if (!backForward) {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().minusDays(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().plusDays(val).atZone(zid).toInstant()));
                }
                break;
            case "H":
                if (!backForward) {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().minusHours(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().plusHours(val).atZone(zid).toInstant()));
                }
                break;
            case "M":
                if (!backForward) {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().minusMinutes(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().plusMinutes(val).atZone(zid).toInstant()));
                }
                break;
            case "S":
                if (!backForward) {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().minusSeconds(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(Instant.ofEpochMilli(isdf.parse(specificDate).getTime()).atZone(zid).toLocalDateTime().plusSeconds(val).atZone(zid).toInstant()));
                }
                break;
            default:
                ans = isdf.format(isdf.parse(specificDate));
                break;
        }
        return ans;
    }

    public String getSpecificDate(SimpleDateFormat isdf, boolean backForward, int val, String opc) {
        String ans;
        switch (opc) {
            case "D":
                if (!backForward) {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusDays(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusDays(val).atZone(zid).toInstant()));
                }
                break;
            case "H":
                if (!backForward) {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusHours(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusHours(val).atZone(zid).toInstant()));
                }
                break;
            case "M":
                if (!backForward) {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusMinutes(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusMinutes(val).atZone(zid).toInstant()));
                }
                break;
            case "S":
                if (!backForward) {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusSeconds(val).atZone(zid).toInstant()));
                } else {
                    ans = isdf.format(Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusSeconds(val).atZone(zid).toInstant()));
                }
                break;
            default:
                ans = isdf.format(Date.from(ZonedDateTime.now(zid).toInstant()));
                break;
        }
        return ans;
    }

    public Date getDSpecificDate(boolean backForward, int val, String opc) {
        Date ans;
        switch (opc) {
            case "D":
                if (!backForward) {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusDays(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusDays(val).atZone(zid).toInstant());
                }
                break;
            case "H":
                if (!backForward) {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusHours(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusHours(val).atZone(zid).toInstant());
                }
                break;
            case "M":
                if (!backForward) {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusMinutes(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusMinutes(val).atZone(zid).toInstant());
                }
                break;
            case "S":
                if (!backForward) {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().minusSeconds(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(ZonedDateTime.now(zid).toLocalDateTime().plusSeconds(val).atZone(zid).toInstant());
                }
                break;
            default:
                ans = Date.from(ZonedDateTime.now(zid).toInstant());
                break;
        }
        return ans;
    }

    public Date getDSpecificDate(boolean backForward, int val, String opc, Date specificDate) {
        Date ans;
        switch (opc) {
            case "D":
                if (!backForward) {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().minusDays(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().plusDays(val).atZone(zid).toInstant());
                }
                break;
            case "H":
                if (!backForward) {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().minusHours(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().plusHours(val).atZone(zid).toInstant());
                }
                break;
            case "M":
                if (!backForward) {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().minusMinutes(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().plusMinutes(val).atZone(zid).toInstant());
                }
                break;
            case "S":
                if (!backForward) {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().minusSeconds(val).atZone(zid).toInstant());
                } else {
                    ans = Date.from(Instant.ofEpochMilli(specificDate.getTime()).atZone(zid).toLocalDateTime().plusSeconds(val).atZone(zid).toInstant());
                }
                break;
            default:
                ans = specificDate;
                break;
        }
        return ans;
    }

    public boolean isBitSet(String hexValue, int bitNumber) {
        int val = Integer.valueOf(hexValue, 16);
        return (val & (1 << bitNumber)) != 0;
    }
    
    public byte[] asignaDireccion(byte[] burnFrame) {
        if (burnFrame[0] == 16) {//Trama de longitud fija, direccionamiento en posiciones 2 y 3 del array.
            burnFrame[2] = (byte) (dirfis & 0xFF);
            burnFrame[3] = (byte) ((dirfis >> 8) & 0xFF);            
        } else if (burnFrame[0] == 104) { // Trama de longitud variable, direccionamiento en posiciones 5 y 6  para punto de medida y  posiciones 10 y 11 para punto de enlace.
            burnFrame[5] = (byte) (dirfis & 0xFF);
            burnFrame[6] = (byte) ((dirfis >> 8) & 0xFF);
            burnFrame[10] = (byte) (dirlog & 0xFF);
            burnFrame[11] = (byte) ((dirlog >> 8) & 0xFF);
        }
        return burnFrame;
    }

    public void cerrarLog(String status, boolean lexito) {
        tiempofinal = new Timestamp(Calendar.getInstance().getTimeInMillis());
        ELogCall log = new ELogCall();
        log.setDfecha(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        log.setSerie(med.getnSerie());
        log.setFechaini(tiempoinicial);
        log.setFechafin(tiempofinal);
        log.setStatus(status);
        log.setLperfil(lperfil);
        log.setLeventos(leventos);
        log.setLregistros(lregistros);
        log.setNduracion((int) (tiempofinal.getTime() - tiempoinicial.getTime()));
        log.setNreintentos(reintentoconexion);
        log.setVccoduser(usuario);
        log.setLexito(lexito);
        cp.saveLogCall(log, null);
    }
    

    public void interrumpirHilo(Thread t) {
        try {
            t.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getErrorString(StackTraceElement ste[], int depthOfError) {
        String error = "";
        int len = ste.length >= depthOfError ? depthOfError : ste.length;
        for (int i = 0; i < len; i++) {
            error += ste[i];
        }
        return error;
    }
}
