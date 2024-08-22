/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasElgamaEPQS;
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
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Metrolink
 */
public class LeerElgamaEPQS {

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
    Timestamp todayNoonGlobal = null;

    long ndesfasepermitido = 0;//desfase tiempo NTP
    InputStream input; //buffer de entrada
    OutputStream output; //buffer de salida
    String cadenahex = ""; //cadena de recepcion en el puerto (Datos)
    int indx = 0; //indice la tabla (GUI) para cambio de estado
    String password = ""; //password1
    String password2 = ""; //password2
    EMedidor med; //obejto medidor con datos
    ControlProcesos cp; //clase control procesos SQL
    //opciones perfil, eventos, registros, configuracion de hora
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lconfHora;
    boolean firstLP_Req = false;
    //variable que indica si lee
    public boolean leer = true;//indicador de lectura realizada
    String numeroPuerto; //puerto de la IP
    int numeroReintentos = 4;
    int nreintentos = 0; //intento actual completo
    int velocidadPuerto; //velocidad del puerto
    long timeout;//timeout  para las peticiones
    int ndias; //numero de dias calculado
    int nDiasI = 0;
    int ndia = 3; //valor del dia para el perfil de carga
    int ndiaReg = 5; //numero de dias default para traer los acumulados por dias
    int nmesReg = 3; //numero de meses default para traer los acumulados por mes
    int ncanal = 1; //canal para el perfil
    int parte = 0; //parte perfil
    int parteI = 0;
    boolean portconect = false;//indicador de conexion al socket
    //Hilos para envios de tramas 

    Thread tEscritura = null;
    Thread tReinicio = null;

    public boolean cierrapuerto = false;//identificador d epuerto cerrado
    TramasElgamaEPQS tramas = new TramasElgamaEPQS();
    Socket socket;
    private volatile boolean escucha = true;//variable de control de escuchar el puerto
    Thread tLectura;//hilo para escuchar las recepciones de las tramas
    private int reintentoconexion = 0; //numero de reintentos de conexion
    boolean aviso = false; //control  de cambios de estado
    //variables de control de envio
    boolean enviando = false;
    boolean reenviando = false;
    byte[] ultimatramaEnviada = null;//ultima trama enviada
    //formatos de fechas
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf4 = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfacceso = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    int numcanales = 2; //numero de canales
    int intervalo = 0; //intervalo de datos 15,30,60.
    int adp = 1;
    int reintentoadp = 0;
    int reintentoCRC = 0;
    //variables para el log
    File file;
    RandomAccessFile fr;
    Abortar objabortar;//variable que controla la opcion de abortar la comunicacion
    String usuario = "admin";
    int reinicio = 0;
    public String tramaIncompleta = "";
    public boolean complemento = false;
    private boolean perfilincompleto = false;
    boolean solicitar = false; //variable de control de la sync
    //estados    
    boolean modelo;
    boolean fechactual;
    boolean perfilcarga;
    boolean registronumeventos;
    boolean registroeventos;
    boolean registrosdiarios;
    boolean registrosmensuales;
    boolean confhora1;
    boolean confhora2;
    String vecSerieMedidor[] = new String[6];
    public ArrayList<String[]> loadProfile1 = new ArrayList<String[]>();//canal 1
    public ArrayList<String[]> loadProfile2 = new ArrayList<String[]>();//canal 2
    public ArrayList<String[]> loadProfile3 = new ArrayList<String[]>();//canal 3
    public ArrayList<String[]> loadProfile4 = new ArrayList<String[]>();//canal 4
    public ArrayList<String[]> loadProfile5 = new ArrayList<String[]>();//canal 5
    public ArrayList<String[]> loadProfile6 = new ArrayList<String[]>();//canal 6
    public ArrayList<String[]> loadProfile7 = new ArrayList<String[]>();//canal 7
    public ArrayList<String[]> loadProfile8 = new ArrayList<String[]>();//canal 8
    public ArrayList<String[]> RegistersDProfile1 = new ArrayList<String[]>();//canal 1
    public ArrayList<String[]> RegistersDProfile2 = new ArrayList<String[]>();//canal 2
    public ArrayList<String[]> RegistersDProfile3 = new ArrayList<String[]>();//canal 3
    public ArrayList<String[]> RegistersDProfile4 = new ArrayList<String[]>();//canal 4
    public ArrayList<String[]> RegistersDProfile5 = new ArrayList<String[]>();//canal 5
    public ArrayList<String[]> RegistersDProfile6 = new ArrayList<String[]>();//canal 6
    public ArrayList<String[]> RegistersMProfile1 = new ArrayList<String[]>();//canal 1
    public ArrayList<String[]> RegistersMProfile2 = new ArrayList<String[]>();//canal 2
    public ArrayList<String[]> RegistersMProfile3 = new ArrayList<String[]>();//canal 3
    public ArrayList<String[]> RegistersMProfile4 = new ArrayList<String[]>();//canal 4
    public ArrayList<String[]> RegistersMProfile5 = new ArrayList<String[]>();//canal 5
    public ArrayList<String[]> RegistersMProfile6 = new ArrayList<String[]>();//canal 6
    //array de eventos
    public ArrayList<String[]> events = new ArrayList<String[]>();
    Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    int nCantEventos = 0;
    long npeticionesConfHora = 0;
    long desfase;
    int valorDesfase = 50;
    private final Object monitor = new Object();
    private ZoneId zid;
    private final String label = "LeerElgamaEPQS";

    //constructor para la recepcion de datos desde la interfaz de lectura
    public LeerElgamaEPQS(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean confHora, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, String usuario, ZoneId zid, long ndesfase) {
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
        lconfHora = confHora;
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
            //configuracion de medidor
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            password2 = med.getPassword2();
            timeout = med.getTimeout() * 1000;
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            String serie = seriemedidor;
            lconske = cp.buscarConstantesKe(med.getnSerie());//se toman los valores de las constantes 
            //se completa el numero de 0 a la izq para el numero del serial
            while (serie.length() < 12) {
                serie = "0" + serie;
            }
            //se convierte el serial en un vector little endian.
            int p = 0;
            for (int j = 5; j >= 0; j--) {
                vecSerieMedidor[j] = serie.substring(p, p + 2);
                p += 2;
            }
            //calculo del desfase permitido    
            escribir("Desfase Parametrizado: " + ndesfasepermitido);
            //abre el socket
            abrePuerto();
        } catch (Exception e) {
            leer = false;
            escribir( getErrorString(e.getStackTrace(), 3) );
        }
    }

    private void abrePuerto() {
        tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
        try {
            //verifica que no se ha abortado la comunicacion
            if (!objabortar.labortar) {
                if (reintentoconexion < numeroReintentos) { //3) { //parametrizable con reintentos de conf_medidor
                    //abre socket                
                    socket = new Socket(med.getDireccionip(), Integer.parseInt(med.getPuertoip()));                    
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
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion por numero de reintentos");
                    escribir("Número de reintentos agotado");          
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion Cancelacion de operacion");
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
        //tamaño buffer
        byte[] readBuffer = new byte[256000];
        byte[] auxBuffer = new byte[256000];
        boolean frameOk = false;
        boolean uncomplete = false;
        int idxBuffer = 0;
        int idxRepFrame = 0;
        int numbytes = 0;
        int lenFrame;
        try {
            synchronized (monitor) {
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                //si el puerto tiene datos llenamos el buffer con lo que se encuentra en el puerto.
                while (!socket.isClosed() && System.currentTimeMillis() < endT) {
                    monitor.wait(1000);
                    if (input.available() == 0) {
                        continue;
                    }
                    numbytes = input.read(readBuffer);
                    if (uncomplete) {
                        System.arraycopy(readBuffer, 0, auxBuffer, idxBuffer, numbytes);
                        idxBuffer += numbytes;
                        if (idxBuffer == (auxBuffer[0] & 0xFF)) {
                            frameOk = true;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                            break;
                        }
                    } else {
                        for (byte bait : readBuffer) {
                            lenFrame = bait & 0xFF;
                            boolean extraCondition = idxRepFrame < ultimatramaEnviada.length ? (bait == ultimatramaEnviada[idxRepFrame]) : false;
                            if (numbytes > lenFrame || extraCondition) { //Tramas pegadas
                                idxRepFrame += 1;
                                numbytes -= 1;
                            } else if (numbytes < lenFrame) { //Trama Incompleta 
                                System.arraycopy(readBuffer, 0, auxBuffer, 0, numbytes);
                                idxBuffer = numbytes;
                                uncomplete = true;
                                break;
                            } else {
                                System.arraycopy(readBuffer, idxBuffer, auxBuffer, 0, numbytes);
                                idxBuffer = numbytes;
                                frameOk = true;
                                break;
                            }
                        }
                        if (frameOk) {
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                            break;
                        } else if (uncomplete) {
                            startT = System.currentTimeMillis();
                            endT = startT + timeout;
                        } else {
                            break;
                        }
                    }
                }
                if (!socket.isClosed() && !frameOk) {
                    escribir("Se vencío el timeout de respuesta sin recibir nada ó la trama ha llegado incompleta.");
                    reenviando = true;
                    enviando = false;
                    monitor.notifyAll();
                    return;
                }
            }

            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramas.encode(auxBuffer, idxBuffer);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
            //luego de tener la trama desglosada byte x byte continuamos a interpretarla
            if (cadenahex.length() > 0) {
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

    private void interpretaCadena(String cadenahex) {
        try {
            escribir("Recibe <= " + cadenahex);
            //vector de tramas
            String[] vectorhex = cadenahex.split(" ");
            if (modelo) {
                validaModelo(vectorhex);
            } else if (fechactual) {
                validaFecha(vectorhex);
            } else if (perfilcarga) {
                validaPerfil(vectorhex);
            } else if (registronumeventos) {
                validaNumEventos(vectorhex);
            } else if (registroeventos) {
                validaEventos(vectorhex);
            } else if (registrosdiarios) {
                validaRegistrosDiarios(vectorhex);
            } else if (registrosmensuales) {
                validaRegistrosMensuales(vectorhex);
            } else if (confhora1) {
                validarConfHora1(vectorhex);
            } else if (confhora2) {
                validarConfhora2(vectorhex);
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

    private void iniciacomunicacion() throws Exception {
        //perfilcarga = false;// pone el estado de perfil de carga.
        if (portconect) {
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
                if (reintentoconexion != 0) {
                    Timestamp actufecha = cp.findUltimafechaLec(med.getnSerie());
                    if (actufecha != null) {
                        med.setFecha(actufecha);
                    } else {
                        throw new Exception("No se pudo actualizar la fecha de última lectura. Reintento cancelado");
                    }
                }             
                //numero controlado que va aumentando al llegar al numero de reintentos se reinicia y pasa al siguiente medidor
                modelo = true;
                adp = 1;
                byte[] data = tramas.getModelo();
                data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                ultimatramaEnviada = data;
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(data, "Solicitud de Modelo");
            } else {
                interrumpirHilo(tLectura);
                escribir("Medidor no configurado");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
                cerrarPuerto();                 
                cerrarLog("Medidor no configurado", false);
                leer = false;
            }
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
                                }
                                escribir(des);
                                escribir("=> " + tramas.encode(trama, trama.length));
                                try {
                                    monitor.notifyAll();
                                    enviaTrama(trama);
                                    monitor.wait();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
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
                                    if (reinicio == 0) {
                                        reinicio = 1;
                                    }
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
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion proceso abortado");
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
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Error al interpretar la respuesta.");
                        cerrarLog("Error en hilo envia trama", false);
                        leer = false;
                    }
                }
            }
        };
        tEscritura.start();
    }

    private void enviaTrama(byte[] bytes) {
        try {
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal                        
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private int CRCElgama(byte[] tramaelgama) {
        int[] table = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,};
        byte[] bytes = new byte[tramaelgama.length - 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = tramaelgama[i];
        }
        int crc = 0x0000;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        int crcAux;
        //////System.out.println("CRC2 " + Integer.toHexString(crc2));
        crcAux = (((crc) << 8) | ((crc) >>> 8)) & 0x0000ffff;
        return crcAux;
    }

    private boolean validaCRCElgama(String[] vectorhex, int len) {
        boolean ok = false;
        try {
            byte[] bytes = new byte[len];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (Integer.parseInt(vectorhex[i], 16) & 0xFF);
            }
            int crccalculado = CRCElgama(bytes);
            String crcc = Integer.toHexString(crccalculado);
            while (crcc.length() < 4) {
                crcc = "0" + crcc;
            }

            if (crcc.toUpperCase().equals(vectorhex[len - 2].toUpperCase() + "" + vectorhex[len - 1].toUpperCase())) {
                ok = true;
            } else {
                ok = false;
            }
        } catch (Exception e) {
            ok = false;
        }
        return ok;
    }

    public byte[] contruirTramaEPQS(byte[] trama, String[] serial, int adp) {

        byte[] data = trama;
        data[1] = (byte) (Integer.parseInt(serial[0], 16) & 0xFF);
        data[2] = (byte) (Integer.parseInt(serial[1], 16) & 0xFF);
        data[3] = (byte) (Integer.parseInt(serial[2], 16) & 0xFF);
        data[4] = (byte) (Integer.parseInt(serial[3], 16) & 0xFF);
        data[5] = (byte) (Integer.parseInt(serial[4], 16) & 0xFF);
        data[6] = (byte) (Integer.parseInt(serial[5], 16) & 0xFF);
        data[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
        int crcCalculado = CRCElgama(data);
        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
        while (calculo.length() < 4) {
            calculo = "0" + calculo;
        }
        data[data.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
        data[data.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
        return data;
    }

    public void AlmacenarRegistrosIncompletos() {
    }

    private void validaModelo(String vectorhex[]) {

        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                adp += 1;
                adp = adp % 9;
                byte[] data = tramas.getFechaactual();
                data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                ultimatramaEnviada = data;
                modelo = false;
                fechactual = true;
                SimpleDateFormat objsdfsync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                time = obtenerFecha_Hora();
                escribir("Fecha: " + time);
                deltatimesync1 = obtenerFecha_Hora();
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(data, "Solicitud de Fecha Actual");
            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud de Modelo");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud de Modelo");
            }
        }
    }

    private void validaFecha(String[] vectorhex) {

        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                adp += 1;
                adp = adp % 9;
                deltatimesync2 = obtenerFecha_Hora();
                intervalo = (Integer.parseInt(vectorhex[14] + vectorhex[13], 16) / 60);
                ////System.out.println("Intervalo " + intervalo);
                byte[] fecha = {(byte) (Integer.parseInt(vectorhex[12], 16) & 0xFF),
                    (byte) (Integer.parseInt(vectorhex[11], 16) & 0xFF),
                    (byte) (Integer.parseInt(vectorhex[10], 16) & 0xFF),
                    (byte) (Integer.parseInt(vectorhex[9], 16) & 0xFF)};
                int year = (int) ((fecha[0] & 0xFE) >> 1);
                int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                int dia = (int) ((fecha[1] & 0x1F));
                int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                ////System.out.println("Fecha actual Medidor: " + year + "/" + completarCeros(month, 2) + "/" + completarCeros(dia, 2) + " " + completarCeros(hora, 2) + ":" + completarCeros(min, 2) + ":" + completarCeros(seg, 2));
                ////System.out.println("Fecha Actual Estacion de trabajo " + new Timestamp(new Date().getTime()));
                fechaactual = year + "/" + completarCeros(month, 2) + "/" + completarCeros(dia, 2) + " " + completarCeros(hora, 2) + ":" + completarCeros(min, 2) + ":" + completarCeros(seg, 2);
                //VALIDAMOS HORA ACTUAL DEL MEDIDOR CON SINCRONIZACION A LA SIC
                try {
                    tsfechaactual = new Timestamp(sdf3.parse(fechaactual).getTime());
                    escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                    escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                    desfase = (time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000;
                    if (Math.abs(desfase) > ndesfasepermitido) {
                        solicitar = false;
                        escribir("No se solicitara el perfil de carga");
                    } else { 
                        solicitar = true;
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
                fechactual = false;
                if (lconfHora) {
                    desfase = ((time.getTime() - tsfechaactual.getTime() - ((long) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000);
                    npeticionesConfHora = Math.abs((long) (desfase / 50));
                    if (desfase % 50 != 0) {
                        npeticionesConfHora++;
                    }
                    byte[] data = tramas.getConfhora1();
                    //calculamos el valor del desfase
                    if (Math.abs(desfase) < 50) {
                        valorDesfase = (int) desfase % 50;
                    } else if (desfase < 0) {
                        valorDesfase = -50;
                    } else {
                        valorDesfase = 50;
                    }
                    data[27] = (byte) (valorDesfase & 0xFF);
                    cp.saveAcceso(med.getnSerie(), "2", sdfacceso.format(new Date(tsfechaactual.getTime())), sdfacceso.format(new Date(time.getTime())), usuario, null);
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    confhora1 = true;
                    ultimatramaEnviada = data;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de configuracion de hora de medidor ");
                } else if (lperfil) {
                    if (solicitar) {
                        try {
                            escribir("Fecha Actual Estacion de trabajo " + new Timestamp(new Date().getTime()));
                            escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaactual).getTime()));
                            if (med.getFecha() != null) {
                                Timestamp lastReadOffset = new Timestamp(med.getFecha().getTime() + (desfase * 1000));
                                escribir("Fecha Ultima Lectura corregida con Offset: " + lastReadOffset);
                                Date todayDate = new Date();
                                String justDate = sdf5.format(todayDate) + " 00:00:00";
                                Timestamp todayWithoutHours = new Timestamp(sdfacceso.parse(justDate).getTime());
                                escribir("Last Read: " + lastReadOffset);
                                escribir("Fecha hoy: " + todayWithoutHours);
                                Timestamp todayNoon = new Timestamp(todayWithoutHours.getTime() + (long) 43200000);
                                todayNoonGlobal = todayNoon;
                                if (lastReadOffset.before(todayWithoutHours)) {// Si lastRead no es del día de hoy
                                    escribir("Last read que no es el del día de hoy.");
                                    //Como no es el del día de hoy, debemos calcular los días hacía atrás y la parte del día.
                                    Long millisegDiff = todayWithoutHours.getTime() - lastReadOffset.getTime();
                                    float nDiasDiff = (float) millisegDiff / 86400000f;
                                    escribir("Días de diferencia: " + nDiasDiff);
                                    ndias = (int) Math.ceil(nDiasDiff);
                                    parte = intervalo > 15 ? 0 : (nDiasDiff % 1) <= 0.5 ? 1 : 0;
                                } else {
                                    escribir("Last read del día de hoy.");
                                    ndias = 0;
                                    escribir("Fecha Medio día del día actual: " + todayNoon);
                                    parte = intervalo > 15 ? 0 : (lastReadOffset.before(todayNoon) ? 0 : 1);
                                }

                                if (ndias > 0) {
                                    if (ndias > 35) {
                                        ndias = 35;
                                    }
                                }
                                nDiasI = ndias;
                                parteI = parte;
                                escribir("Días necesarios a solicitar: " + ndias);
                                escribir("Parte inicial a pedir: " + parte);
                                firstLP_Req = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (lconske.size() > 0) {
                            //ndias = ndias + 1;
                            ncanal = 0;
                            byte[] data = tramas.getPerfilCarga();
                            ndia = ndias;
                            perfilcarga = true;
                            //verificamos el canal
                            loadProfile1 = new ArrayList<String[]>();
                            loadProfile2 = new ArrayList<String[]>();
                            loadProfile3 = new ArrayList<String[]>();
                            loadProfile4 = new ArrayList<String[]>();
                            loadProfile5 = new ArrayList<String[]>();
                            loadProfile6 = new ArrayList<String[]>();
                            loadProfile7 = new ArrayList<String[]>();
                            loadProfile8 = new ArrayList<String[]>();
                            data = solicitarCanal(data, ndia, parte, ncanal);
                            data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                            ultimatramaEnviada = data;
                            reintentoCRC = 0;
                            reintentoadp = 0;
                            enviaTrama2_2(data, "Solicitud de Perfil de carga parte " + parte + " del canal " + ncanal + " del dia " + ndia);
                        } else {
                            //cerramos session
                            cerrarPuerto();
                            escribir("Estado lectura No leido canales no configurados");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion canales no configurados");
                            cerrarLog("Canales no Configurados", false);
                            leer = false;
                        }
                    } else {
                        //cerramos session
                        cerrarPuerto();
                        escribir("Estado lectura No leido por desfase de medidor");
                        //AlmacenarRegistros();
                        cerrarLog("No leido", false);
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion por desfase horario");
                        leer = false;
                    }
                } else if (leventos) {
                    byte[] data = tramas.getNumeventos();
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    ultimatramaEnviada = data;
                    registronumeventos = true;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de Numero de Eventos");
                } else if (lregistros) {
                    if (lconske.size() > 0) {
                        RegistersDProfile1 = new ArrayList<String[]>();
                        RegistersDProfile2 = new ArrayList<String[]>();
                        RegistersDProfile3 = new ArrayList<String[]>();
                        RegistersDProfile4 = new ArrayList<String[]>();
                        RegistersDProfile5 = new ArrayList<String[]>();
                        RegistersDProfile6 = new ArrayList<String[]>();
                        byte[] data = tramas.getRegistrosdia();
                        ndiaReg = med.getNdiasreg() - 1;
                        ncanal = 0;
                        registrosdiarios = true;
                        data = solicitarRegistroCanal(data, ndiaReg, ncanal);
                        data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                        ultimatramaEnviada = data;
                        reintentoCRC = 0;
                        reintentoadp = 0;
                        enviaTrama2_2(data, "Solicitud de Registros Diarios Canal " + ncanal + " Reg " + ndiaReg);
                    } else {
                        //cerramos session
                        cerrarPuerto();
                        escribir("Estado lectura No leido canales no configurados");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion canales no configurados");
                        cerrarLog("Canales no configurados", false);
                        leer = false;
                    }

                } else {
                    //cerramos session
                    cerrarPuerto();
                    escribir("Estado lectura No leido por desfase de medidor");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion desfase de medidor");
                    cerrarLog("Leido", false);
                    leer = false;
                }
            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private void validaPerfil(String[] vectorhex) {

        if (vectorhex.length > 0 && vectorhex.length > 7) {//validamos si se puede verificar el adp
            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                //validar crc
                if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                    reintentoadp = 0;
                    adp += 1;
                    adp = adp % 9;
                    perfilincompleto = true;
                    if (Integer.parseInt(vectorhex[8], 16) == 2) {//viene los datos de lo contrario es una negacion de los datos
                        //********
                        String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                        System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                        ////System.out.println("Trama capturada: " + Arrays.toString(trama));
                        switch (ncanal) {
                            case 0:
                                loadProfile1.add(trama);
                                break;
                            case 1:
                                loadProfile2.add(trama);
                                break;
                            case 2:
                                loadProfile3.add(trama);
                                break;
                            case 3:
                                loadProfile4.add(trama);
                                break;
                            case 4:
                                loadProfile5.add(trama);
                                break;
                            case 5:
                                loadProfile6.add(trama);
                                break;
                            case 6:
                                loadProfile7.add(trama);
                                break;
                            case 7:
                                loadProfile8.add(trama);
                                break;
                            default:
                                break;
                        }
                    }
                    boolean lsolicitar = true;
                    if (intervalo == 15) {
                        if (parte == 0) {
                            if (firstLP_Req) {
                                firstLP_Req = false;
                            }
                            if (ndia <= 0) {
                                Timestamp currentTime = new Timestamp(new Date().getTime());
                                Timestamp noonPlusOneInterval = new Timestamp(todayNoonGlobal.getTime() + (intervalo * 60000));
                                if (currentTime.after(noonPlusOneInterval)) { // Ya es más de medio día + un intervalo entonces se debe pedir la parte 1 (2da parte).
                                    parte = 1;
                                } else {
                                    ncanal++;
                                    if (ncanal >= lconske.size()) {
                                        ndia--; //cambiamos de dia
                                        lsolicitar = false;
                                    }
                                }
                            } else {
                                parte = 1;
                            }
                        } else {
                            if (!firstLP_Req) {
                                parte = 0;
                            }
                            ncanal++;
                            if (ncanal >= lconske.size()) {
                                ndia--; //cambiamos de dia
                                if (ndia >= 0) {
                                    ncanal = 0;
                                    parte = 0;
                                } else {
                                    lsolicitar = false;
                                }
                            }
                        }
                    } else {
                        ncanal++;
                        if (ncanal >= lconske.size()) {
                            ndia--; //cambiamos de dia
                            if (ndia >= 0) {
                                ncanal = 0;
                            } else {
                                lsolicitar = false;
                            }
                        }
                    }

                    if (lsolicitar) {
                        //verificamos el canal
                        byte[] data = tramas.getPerfilCarga();
                        data = solicitarCanal(data, ndia, parte, ncanal);
                        data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                        ultimatramaEnviada = data;
                        reintentoCRC = 0;
                        reintentoadp = 0;
                        enviaTrama2_2(data, "Solicitud de Perfil de carga parte " + parte + " del canal " + ncanal + " del dia " + ndia);
                    } else {
                        perfilincompleto = false;
                        perfilcarga = false;
                        if (leventos) {
                            byte[] data = tramas.getNumeventos();
                            data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                            ultimatramaEnviada = data;
                            registronumeventos = true;
                            reintentoCRC = 0;
                            reintentoadp = 0;
                            enviaTrama2_2(data, "Solicitud de Numero de Eventos");
                        } else if (lregistros) {
                            RegistersDProfile1 = new ArrayList<String[]>();
                            RegistersDProfile2 = new ArrayList<String[]>();
                            RegistersDProfile3 = new ArrayList<String[]>();
                            RegistersDProfile4 = new ArrayList<String[]>();
                            RegistersDProfile5 = new ArrayList<String[]>();
                            RegistersDProfile6 = new ArrayList<String[]>();
                            byte[] data = tramas.getRegistrosdia();
                            ndiaReg = med.getNdiasreg() - 1;
                            ncanal = 0;
                            registrosdiarios = true;
                            data = solicitarRegistroCanal(data, ndiaReg, ncanal);
                            data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                            ultimatramaEnviada = data;
                            reintentoCRC = 0;
                            reintentoadp = 0;
                            enviaTrama2_2(data, "Solicitud de Registros Diarios Canal " + ncanal + " Reg " + ndiaReg);
                        } else {
                            //cierra puerto
                            cerrarPuerto();
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Lectura OK, Reintentos " + reintentoconexion);
                            escribir("Estado lectura leido");
                            if (!loadProfile1.isEmpty()) {
                                if (AlmacenarPerfil()) {
                                    med.MedLeido = true;
                                    cerrarLog("Leido", true);
                                } else {
                                    med.MedLeido = false;
                                    cerrarLog("No Leido", true);
                                }
                            } else {
                                med.MedLeido = false;
                                cerrarLog("No Leido", true);
                            }                            
                            leer = false;                            
                        }
                    }
                } else {
                    if (reintentoCRC > 2) {
                        reintentoCRC = 0;
                        cerrarPuerto();
                        escribir("Estado lectura No leido");
                        //AlmacenarRegistros();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                        cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                        leer = false;
                    } else {
                        reintentoCRC++;
                        escribir("CRC Incorrecto");
                        escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                        reintentoCRC = 0;
                        reintentoadp = 0;
                        enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                    }
                }
            } else {//no es el adp correcto                        
                if (reintentoadp > 2) {
                    reintentoadp = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                    cerrarLog("ADP Incorrecto", false);
                    leer = false;
                } else {
                    reintentoadp++;
                    escribir("ADP Incorrecto");
                    escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    public void validaNumEventos(String[] vectorhex) {
        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                adp += 1;
                adp = adp % 9;
                nCantEventos = Integer.parseInt(vectorhex[14] + vectorhex[13], 16) - 1;
                escribir("Numero de registro de eventos " + (nCantEventos + 1));
                ////System.out.println("Numero de registro de eventos " + (nCantEventos + 1));
                registronumeventos = false;
                if (nCantEventos < 0) {//no existe eventos
                    if (lregistros) {
                        if (lconske.size() > 0) {
                            //solicitamos registros
                            RegistersDProfile1 = new ArrayList<String[]>();
                            RegistersDProfile2 = new ArrayList<String[]>();
                            RegistersDProfile3 = new ArrayList<String[]>();
                            RegistersDProfile4 = new ArrayList<String[]>();
                            RegistersDProfile5 = new ArrayList<String[]>();
                            RegistersDProfile6 = new ArrayList<String[]>();
                            byte[] data = tramas.getRegistrosdia();
                            ndiaReg = med.getNdiasreg() - 1;
                            ncanal = 0;
                            registrosdiarios = true;
                            data = solicitarRegistroCanal(data, ndiaReg, ncanal);
                            data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                            ultimatramaEnviada = data;
                            reintentoCRC = 0;
                            reintentoadp = 0;
                            enviaTrama2_2(data, "Solicitud de Registros Diarios Canal " + ncanal + " Reg " + ndiaReg);
                        } else {
                            //cerramos session
                            cerrarPuerto();
                            escribir("Estado lectura No leido canales no configurados");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion canales no configurados");
                            cerrarLog("Canales no configurados", false);
                            leer = false;
                        }
                    } else {
                        //cierra puerto
                        cerrarPuerto();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Lectura OK, Reintentos " + reintentoconexion);
                        escribir("Estado lectura leido");
                        if (!loadProfile1.isEmpty()) {
                            if (AlmacenarPerfil()) {
                                med.MedLeido = true;
                                cerrarLog("Leido", true);
                            } else {
                                med.MedLeido = false;
                                cerrarLog("No Leido", true);
                            }
                        } else {
                            med.MedLeido = false;
                            cerrarLog("No Leido", true);
                        }
                        leer = false;
                    }
                } else {
                    registroeventos = true;
                    events = new ArrayList<String[]>();
                    byte[] data = tramas.getEventos();
                    String hex = Integer.toHexString(nCantEventos).toUpperCase();
                    while (hex.length() < 4) {
                        hex = "0" + hex;
                    }
                    data[10] = (byte) (Integer.parseInt(hex.substring(0, 2), 16) & 0xFF);
                    data[11] = (byte) (Integer.parseInt(hex.substring(2, 4), 16) & 0xFF);
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    ultimatramaEnviada = data;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de Numero de Eventos " + nCantEventos);
                }

            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private void validaEventos(String[] vectorhex) {
        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                adp += 1;
                adp = adp % 9;

                events.add(vectorhex);
                nCantEventos = nCantEventos - 12;
                if (nCantEventos < 0) {//si el registro es menor a 0 se solicita lo siguiente
                    registroeventos = false;
                    if (lregistros) {
                        RegistersDProfile1 = new ArrayList<String[]>();
                        RegistersDProfile2 = new ArrayList<String[]>();
                        RegistersDProfile3 = new ArrayList<String[]>();
                        RegistersDProfile4 = new ArrayList<String[]>();
                        RegistersDProfile5 = new ArrayList<String[]>();
                        RegistersDProfile6 = new ArrayList<String[]>();
                        byte[] data = tramas.getRegistrosdia();
                        ndiaReg = med.getNdiasreg() - 1;
                        ncanal = 0;
                        registrosdiarios = true;
                        data = solicitarRegistroCanal(data, ndiaReg, ncanal);
                        data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                        ultimatramaEnviada = data;
                        reintentoCRC = 0;
                        reintentoadp = 0;
                        enviaTrama2_2(data, "Solicitud de Registros Diarios Canal " + ncanal + " Nreg " + ndiaReg);
                    } else {
                        //cierra puerto
                        cerrarPuerto();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Lectura OK, Reintentos " + reintentoconexion);
                        escribir("Estado lectura leido");
                        AlmacenaEventos();
                        if (!loadProfile1.isEmpty()) {
                            if (AlmacenarPerfil()) {
                                med.MedLeido = true;
                                cerrarLog("Leido", true);
                            } else {
                                med.MedLeido = false;
                                cerrarLog("No Leido", true);
                            }
                        } else {
                            med.MedLeido = false;
                            cerrarLog("No Leido", true);
                        }
                        leer = false;
                    }
                } else {
                    byte[] data = tramas.getEventos();
                    String hex = Integer.toHexString(nCantEventos).toUpperCase();
                    while (hex.length() < 4) {
                        hex = "0" + hex;
                    }
                    data[10] = (byte) (Integer.parseInt(hex.substring(0, 2), 16) & 0xFF);
                    data[11] = (byte) (Integer.parseInt(hex.substring(2, 4), 16) & 0xFF);
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    ultimatramaEnviada = data;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de Numero de Eventos " + nCantEventos);
                }

            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private void validaRegistrosDiarios(String[] vectorhex) {
        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                adp += 1;
                adp = adp % 9;
                String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                if (ncanal == 0) {
                    RegistersDProfile1.add(trama);
                } else if (ncanal == 1) {
                    RegistersDProfile2.add(trama);
                } else if (ncanal == 2) {
                    RegistersDProfile3.add(trama);
                } else if (ncanal == 3) {
                    RegistersDProfile4.add(trama);
                } else if (ncanal == 4) {
                    RegistersDProfile5.add(trama);
                } else if (ncanal == 5) {
                    RegistersDProfile6.add(trama);
                }
                boolean lsolicitar = true;
                if (ndiaReg - 6 < 0) {
                    ncanal++;
                    ndiaReg = med.getNdiasreg() - 1;
                } else {
                    ndiaReg -= 6;
                }

                if (ncanal >= lconske.size()) {//al camabiar de canal verificamos que no supere el numero de canales programados en constantes si se supera se cambia de di
                    lsolicitar = false;
                }
                if (lsolicitar) {
                    //se solicita el siguiente canal programado
                    byte[] data = tramas.getRegistrosdia();
                    data = solicitarRegistroCanal(data, ndiaReg, ncanal);
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    ultimatramaEnviada = data;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de Registros Diarios Canal " + ncanal + " Reg " + ndiaReg);
                } else {
                    //se solicitan los registros mensuales
                    RegistersMProfile1 = new ArrayList<String[]>();
                    RegistersMProfile2 = new ArrayList<String[]>();
                    RegistersMProfile3 = new ArrayList<String[]>();
                    RegistersMProfile4 = new ArrayList<String[]>();
                    RegistersMProfile5 = new ArrayList<String[]>();
                    RegistersMProfile6 = new ArrayList<String[]>();
                    registrosdiarios = false;
                    byte[] data = tramas.getRegistrosmes();
                    nmesReg = med.getNmesreg() - 1;
                    ncanal = 0;
                    data = solicitarRegistroCanal(data, nmesReg, ncanal);
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    ultimatramaEnviada = data;
                    registrosmensuales = true;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de Registros Mensuales " + ncanal + " Reg " + nmesReg);
                }
            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private void validaRegistrosMensuales(String[] vectorhex) {
        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                adp += 1;
                adp = adp % 9;
                String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                if (ncanal == 0) {
                    RegistersMProfile1.add(trama);
                } else if (ncanal == 1) {
                    RegistersMProfile2.add(trama);
                } else if (ncanal == 2) {
                    RegistersMProfile3.add(trama);
                } else if (ncanal == 3) {
                    RegistersMProfile4.add(trama);
                } else if (ncanal == 4) {
                    RegistersMProfile5.add(trama);
                } else if (ncanal == 5) {
                    RegistersMProfile6.add(trama);
                }
                boolean lsolicitar = true;
                if (nmesReg - 6 < 0) {
                    ncanal++;
                    nmesReg = med.getNmesreg() - 1;
                } else {
                    nmesReg -= 6;
                }
                if (ncanal >= lconske.size()) {//al camabiar de canal verificamos que no supere el numero de canales programados en constantes si se supera se cambia de di
                    lsolicitar = false;
                }
                if (lsolicitar) {
                    //se solicita el siguiente canal programado
                    byte[] data = tramas.getRegistrosmes();
                    data = solicitarRegistroCanal(data, nmesReg, ncanal);
                    data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                    ultimatramaEnviada = data;
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(data, "Solicitud de Registros Mensuales Canal " + ncanal + " Reg " + nmesReg);
                } else {
                    //cierra puerto
                    cerrarPuerto();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Lectura OK, Reintentos " + reintentoconexion);
                    escribir("Estado lectura leido");
                    AlmacenaRegistros();
                    AlmacenaEventos();
                    if (!loadProfile1.isEmpty()) {
                        if (AlmacenarPerfil()) {
                            med.MedLeido = true;
                            cerrarLog("Leido", true);
                        } else {
                            med.MedLeido = false;
                            cerrarLog("No Leido", true);
                        }
                    } else {
                        med.MedLeido = false;
                        cerrarLog("No Leido", true);
                    }
                    leer = false;
                }
            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private void validarConfHora1(String[] vectorhex) {
        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 4) {//es el adp correcto y el comando es ECO
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                //adp += 1;
                adp = adp % 9;

                //se revisan los datos de ZA1 encriptados con DES
                byte[] ZA1 = new byte[8];
                byte[] ZA2 = new byte[8];
                int i = 0;
                for (int j = 9; j < 17; j++) {
                    ZA1[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                    i++;
                }
                ////System.out.println("ZA1 Medidor " + tramas.encode(ZA1, ZA1.length));
                i = 0;

                for (int j = 17; j < 25; j++) {
                    ZA2[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                    i++;
                }
                try {
                    String zao = "FF FF FF FF A9 EB 8C 84";
                    String DESpassword[] = {"20", "20", "20", "20", "20", "20", "20", "20"};
                    ////System.out.println("Password: " + password);
                    for (int k = 0; k < (password.trim().length()); k++) {
                        DESpassword[k] = convertStringToHex(password.trim().substring(k, k + 1));
                    }
                    ////System.out.println("" + Arrays.toString(DESpassword));
                    byte ZA1new[] = tramas.encrypt(zao, DESpassword);
                    ////System.out.println("Random encript " + tramas.encode(ZA1new, ZA1new.length));
                    ////System.out.println("ZA2 Medidor " + tramas.encode(ZA2, ZA2.length));
                    if (tramas.encode(ZA1, ZA1.length).equals(tramas.encode(ZA1new, ZA1new.length))) {
                        confhora1 = false;
                        confhora2 = true;
                        byte data[] = tramas.getConfhora2();
                        byte ZA2new[] = tramas.encrypt(tramas.encode(ZA2, ZA2.length), DESpassword);
                        ////System.out.println("ZA2 Encriptado " + tramas.encode(ZA2new, ZA2new.length));
                        for (int k = 0; k < 8; k++) {
                            data[k + 17] = ZA2new[k];
                        }
                        data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                        ultimatramaEnviada = data;
                        reintentoCRC = 0;
                        reintentoadp = 0;
                        enviaTrama2_2(data, "Solicitud confirmacion Configuracion hora");
                    } else {
                        ////System.out.println("Error de autenticacion");
                        cerrarPuerto();
                        escribir("Desconexion - Error de autenticacion");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion Error de autenticacion");
                        cerrarLog("Desconexion Error de autenticacion", false);
                        leer = false;
                    }
                } catch (Exception e) {
                    escribir(getErrorString(e.getStackTrace(), 3));
                    cerrarPuerto();
                }
            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private void validarConfhora2(String[] vectorhex) {
        if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
            //validar crc
            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                reintentoadp = 0;
                //adp += 1;
                adp = adp % 9;
                if (Integer.parseInt(vectorhex[8], 16) == 10) { //ARJ
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion negacion de configuracion de hora");
                    cerrarLog("Negacion de configuracion de hora", false);
                    leer = false;
                } else {
                    byte[] ZA1 = new byte[8];
                    byte[] ZA2 = new byte[8];
                    int i = 0;
                    for (int j = 9; j < 17; j++) {
                        ZA1[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                        i++;
                    }
                    ////System.out.println("ZA1 Medidor " + tramas.encode(ZA1, ZA1.length));
                    i = 0;
                    for (int j = 17; j < 25; j++) {
                        ZA2[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                        i++;
                    }
                    ////System.out.println("ZA2 Medidor " + tramas.encode(ZA2, ZA2.length));
                    if (tramas.encode(ZA2, ZA2.length).equals(tramas.encode(ZA1, ZA1.length))) {
                        confhora2 = false;
                        npeticionesConfHora--;

                        boolean ok = true;
                        if (npeticionesConfHora == 0) {//es la ultima peticion por lo tanto se suma o resta el modulo 
                            valorDesfase = (int) desfase % 50;
                            if (valorDesfase == 0) {
                                ok = false;
                            }
                        } else if (desfase < 0) {
                            valorDesfase = -50;
                        } else {
                            valorDesfase = 50;
                        }
                        if (ok) {
                            byte data[] = tramas.getConfhora1();
                            confhora1 = true;
                            data[27] = (byte) (valorDesfase & 0xFF);
                            data = contruirTramaEPQS(data, vecSerieMedidor, adp);
                            ultimatramaEnviada = data;
                            reintentoCRC = 0;
                            reintentoadp = 0;
                            enviaTrama2_2(data, "Solicitud de configuracion de hora de medidor ");
                        } else {
                            confhora1 = false;
                            cerrarPuerto();
                            escribir("Estado Configuracion remota exitosa");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Medidor sincronizado");
                            cerrarLog("Sincronizado", true);
                            leer = false;
                        }

                    } else {
                        cerrarPuerto();
                        escribir("Estado lectura No Configurado");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion negacion de configuracion de hora");
                        cerrarLog("Negacion de configuracion de hora", false);
                        leer = false;
                    }
                }
            } else {
                if (reintentoCRC > 2) {
                    reintentoCRC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura No leido");
                    //AlmacenarRegistros();
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion CRC invalido, numero de reintentos agotado.");
                    cerrarLog("Numero de reintentos para validar CRC excedidos", false);
                    leer = false;
                } else {
                    reintentoCRC++;
                    escribir("CRC Incorrecto");
                    escribir("=> Envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    reintentoCRC = 0;
                    reintentoadp = 0;
                    enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
                }
            }
        } else {//no es el adp correcto                        
            if (reintentoadp > 2) {
                reintentoadp = 0;
                cerrarPuerto();
                escribir("Estado lectura No leido");
                //AlmacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion ADP erroneo, numero de reintento agotado.");
                cerrarLog("ADP Incorrecto", false);
                leer = false;
            } else {
                reintentoadp++;
                escribir("ADP Incorrecto");
                escribir("=> Espera a envio de ultima trama " + tramas.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                reintentoCRC = 0;
                reintentoadp = 0;
                enviaTrama2_2(ultimatramaEnviada, "Solicitud Fecha Actual");
            }
        }
    }

    private byte[] solicitarCanal(byte[] trama, int ndia, int parte, int ncanal) {
        byte data[] = trama;
        String ca = Integer.toHexString(lconske.get(ncanal).getCanal());
        data[9] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
        int idx = ndia * 128;
        if (parte == 1) {
            idx = idx + 1;
        }
        String idx0 = Integer.toHexString(idx);
        while (idx0.length() < 4) {
            idx0 = "0" + idx0;
        }
        data[10] = (byte) (Integer.parseInt(idx0.substring(0, 2), 16) & 0xFF);
        data[11] = (byte) (Integer.parseInt(idx0.substring(2, 4), 16) & 0xFF);
        return data;
    }

    private byte[] solicitarRegistroCanal(byte[] trama, int ndias, int ncanal) {
        byte data[] = trama;
        int valor = (ndias * 8) + (lconske.get(ncanal).getCanal() - 24);
        String idx0 = Integer.toHexString(valor);
        while (idx0.length() < 4) {
            idx0 = "0" + idx0;
        }
        data[10] = (byte) (Integer.parseInt(idx0.substring(0, 2), 16) & 0xFF);
        data[11] = (byte) (Integer.parseInt(idx0.substring(2, 4), 16) & 0xFF);
        return data;
    }

    private boolean AlmacenarPerfil() {
        return almacenaPerfilCargaNCanales();
    }

    private boolean almacenaPerfilCargaNCanales() {
        try {
            Vector<Electura> vlec = new Vector<>();
            String fechaActualizada = "";
            ArrayList<String> data1 = obtenerDataCanal(loadProfile1);
            ArrayList<String> data2 = obtenerDataCanal(loadProfile2);
            ArrayList<String> data3 = obtenerDataCanal(loadProfile3);
            ArrayList<String> data4 = obtenerDataCanal(loadProfile4);
            ArrayList<String> data5 = obtenerDataCanal(loadProfile5);
            ArrayList<String> data6 = obtenerDataCanal(loadProfile6);
            ArrayList<String> data7 = obtenerDataCanal(loadProfile7);
            ArrayList<String> data8 = obtenerDataCanal(loadProfile8);

            if (numcanales >= 2 && data1.size() > 0) {
                //////System.out.println("Almacena canal 1");
                fechaActualizada = lecturaELGAMANEPQS(data1, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(0).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 2 && data2.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data2, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(1).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 3 && data3.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data3, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(2).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 4 && data4.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data4, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(3).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 5 && data5.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data5, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(4).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 6 && data6.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data6, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(5).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 7 && data7.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data7, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(6).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }
            if (numcanales >= 8 && data8.size() > 0) {
                fechaActualizada = lecturaELGAMANEPQS(data8, seriemedidor, med.getMarcaMedidor().getCodigo(), lconske.get(7).getCanal(), fechaactual, fechaActualizada, intervalo, nDiasI, parteI, numcanales, vlec);
            }

            //////System.out.println("Inicio almacenamiento " + seriemedidor + " " + new Date()); 
            cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
            if (fechaActualizada.length() > 0) {
                SimpleDateFormat fechaAactualizarSDF = new SimpleDateFormat("yyMMddHHmmss");
                String fechaAactualizar = fechaActualizada;
                Date fechaAactualizarDate = new SimpleDateFormat("yy/MM/dd HH:mm:ss").parse(fechaAactualizar);
                fechaAactualizar = fechaAactualizarSDF.format(new Date(fechaAactualizarDate.getTime()));
                cp.actualizaFechaLectura(seriemedidor, fechaAactualizar);
            }
            return true;
        } catch (Exception e) {
            getErrorString(e.getStackTrace(), 3);
            return false;
        }
    }

    public String lecturaELGAMANEPQS(ArrayList<String> data, String seriemedidor, String marca, int numcanal, String fechaActual, String fechaActualizada, int intervalo, int ndias, int parteI, int totalcanales, Vector<Electura> vlec) throws Exception {
        SimpleDateFormat sdfactual = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yy-MM-dd");
        Timestamp tsactual = new Timestamp(sdfactual.parse(fechaActual).getTime());
        EConstanteKE econske = null;
        Connection conn = cp.getConn();
        ArrayList<EtipoCanal> vtipocanal = cp.obtenerTipoCanales(marca);
        long fechalec = 0;
        String fechalectura = "";
        int parteL = parteI;
        int diasleer = ndias;
        int nIntervals;
        for (int idat = 0; idat < data.size(); idat++) {
            if (idat == 0) {
                fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                fechalectura = sdf2.format(new Date(fechalec));
            } else {
                if (intervalo == 15) {
                    parteL = parteL == 0 ? 1 : 0;
                    if (parteL == 0) {
                        diasleer--;
                        fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                        fechalectura = sdf2.format(new Date(fechalec));
                    }
                } else {
                    diasleer--;
                    fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                    fechalectura = sdf2.format(new Date(fechalec));
                }
            }
            nIntervals = findNIntervals(parteL, intervalo);

            //desglosar datos
            ////System.out.println("Lecturas");
            String trama = data.get(idat);
            //Vector<Electura> vlec = new Vector<Electura>();
            SimpleDateFormat dfABB = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            SimpleDateFormat spf = new SimpleDateFormat("yy-MM-dd HH:mm");
            int tiempoL = intervalo;
            Timestamp timeL = null;
            Timestamp feactual = new Timestamp(dfABB.parse(fechaActual).getTime() - 1800000);

            double energia = 0.0;
            String hora = "";
            String min = "";
            Electura lec = null;
            String valorv = "";
            String canal = "";
            String unidad = trama.substring(14, 16);
            double divisor = Math.pow(10, Integer.valueOf(trama.substring(12, 14), 16));
            double multiplo = Long.parseLong(trama.substring(10, 12) + trama.substring(8, 10) + trama.substring(6, 8) + trama.substring(4, 6) + trama.substring(2, 4) + trama.substring(0, 2), 16) / Math.pow(2, 32);
            //Timestamp ultimafechalec = cp.findUltimafechaLec(seriemedidor);
            Timestamp ultimafechalec = med.getFecha();
            for (int i = 16; i < trama.length(); i += 4) {
                byte[] valor = {(byte) (Integer.parseInt(trama.substring(i + 2, i + 4), 16) & 0xFF), (byte) (Integer.parseInt(trama.substring(i, i + 2), 16) & 0xFF)};
                int tiempolec = tiempoL * nIntervals;
                hora = "" + ((int) (tiempolec / 60));
                min = "" + (int) (tiempolec % 60);
                String ts = fechalectura + " " + hora + ":" + min;
                timeL = new Timestamp(spf.parse(ts).getTime()); //se corre 15 min debido a que en el protocolo se explica que el primer dato del bloque corresponde a las 00:15 por lo tanto se mueve 15 min la fecha para implicar el dato
                valorv = trama.substring(i + 2, i + 4) + "" + (trama.substring(i, i + 2));
                energia = (((double) Integer.parseInt(valorv, 16)) * multiplo) / divisor;
                if (unidad.toUpperCase().contains("00")) {
                    energia = energia / 1000;
                } else if (unidad.toUpperCase().contains("6D")) {
                    energia = energia * 1000;
                }
                if (timeL.before(feactual)) { //validamos si la fecha es despues de la fecha de la ultima lectura
                    try {
                        econske = cp.buscarConske(lconske, numcanal);
                        for (EtipoCanal et : vtipocanal) {
                            if (Integer.parseInt(et.getCanal()) == numcanal) {
                                canal = et.getUnidad();
                                break;
                            }
                        }
                        if (econske != null) {
//                            throw new Exception("prueba");
                            lec = new Electura(timeL, seriemedidor, numcanal, trasnformarEnergia(energia, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), energia, intervalo, canal);
                            vlec.add(lec);
                            if (numcanal == lconske.get(lconske.size() - 1).getCanal()) {
                                if (ultimafechalec != null) {
                                    if (timeL.after(ultimafechalec)) {
                                        fechaActualizada = dfABB.format(new Date(timeL.getTime()));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        getErrorString(e.getStackTrace(), 3);                       
                    }
                }
                nIntervals++;
            }
        }
        return fechaActualizada;
    }

    private int findNIntervals(int parteL, int intervalo) {
        int n;
        if (parteL == 1) {
            n = ((60 / intervalo) * 12) + 1;
        } else {
            n = 1;
        }
        return n;
    }

    private double trasnformarEnergia(double lectura, double conske, double multiplo, double divisor) {
        double energia = 0;
        try {
            energia = ((lectura * conske) * multiplo) / divisor;
        } catch (Exception e) {
            e.printStackTrace();
            energia = -1;
        }
        return energia;
    }

    private ArrayList<String> obtenerDataCanal(ArrayList<String[]> loadprofile) {
        String datapf1 = "";
        //perfil de carga de canal1
        for (int pf = 0; pf < loadprofile.size(); pf++) {
            for (int j = 19; j < loadprofile.get(pf).length - 2; j++) {
                datapf1 += "" + loadprofile.get(pf)[j];
            }
        }
        ArrayList<String> data1 = new ArrayList<>();
        while (datapf1.length() > 0) {
            if (datapf1.length() >= 208) {
                data1.add(datapf1.substring(0, 208));
                datapf1 = datapf1.substring(208, datapf1.length());
            } else {
                data1.add(datapf1.substring(0, datapf1.length()));
                datapf1 = "";
            }
        }
        return data1;
    }

    private void AlmacenaEventos() {
        String data = obtenerDataEventos(events);
        String fechadesenergizacion = "";
        String fechaenergizacion = "";
        String fechacorte = "";
        String fechareconexion = "";
        String estado = "";
        ERegistroEvento evento = null;
        for (int i = 0; i < data.length(); i += 16) {
            estado = data.substring(i + 10, i + 12) + data.substring(i + 8, i + 10);
            if (Integer.parseInt(estado, 16) % 8 == 0) {//es desconexion
                fechadesenergizacion = "1";//desenergiza
                if (fechaenergizacion.length() > 0) {//antes habia una energizacion por lo tanto es un corte
                    byte[] fecha = {(byte) (Integer.parseInt(data.substring(i + 6, i + 8), 16) & 0xFF),
                        (byte) (Integer.parseInt(data.substring(i + 4, i + 6), 16) & 0xFF),
                        (byte) (Integer.parseInt(data.substring(i + 2, i + 4), 16) & 0xFF),
                        (byte) (Integer.parseInt(data.substring(i, i + 2), 16) & 0xFF)};
                    int year = (int) ((fecha[0] & 0xFE) >> 1);
                    int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                    int dia = (int) ((fecha[1] & 0x1F));
                    int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                    int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                    int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                    fechacorte = (year + "/" + completarCeros(month, 2) + "/" + completarCeros(dia, 2) + " " + completarCeros(hora, 2) + ":" + completarCeros(min, 2) + ":" + completarCeros(seg, 2));
                    fechaenergizacion = "";
                }
            } else {
                //es reconexion                
                fechaenergizacion = "1";
                if (fechadesenergizacion.length() > 0) {//antes habia una desenergizacion por lo tanto es un corte
                    byte[] fecha = {(byte) (Integer.parseInt(data.substring(i + 6, i + 8), 16) & 0xFF),
                        (byte) (Integer.parseInt(data.substring(i + 4, i + 6), 16) & 0xFF),
                        (byte) (Integer.parseInt(data.substring(i + 2, i + 4), 16) & 0xFF),
                        (byte) (Integer.parseInt(data.substring(i, i + 2), 16) & 0xFF)};
                    int year = (int) ((fecha[0] & 0xFE) >> 1);
                    int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                    int dia = (int) ((fecha[1] & 0x1F));
                    int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                    int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                    int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                    fechareconexion = (year + "/" + completarCeros(month, 2) + "/" + completarCeros(dia, 2) + " " + completarCeros(hora, 2) + ":" + completarCeros(min, 2) + ":" + completarCeros(seg, 2));
                    if (fechacorte.length() > 0) {
                        try {
                            evento = new ERegistroEvento();
                            evento.setVcserie(med.getnSerie());
                            evento.setVcfechacorte(new Timestamp(sdf3.parse(fechacorte).getTime()));
                            evento.setVcfechareconexion(new Timestamp(sdf3.parse(fechareconexion).getTime()));
                            evento.setVctipo("0001");
                            cp.actualizaEvento(evento, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        fechareconexion = "";
                        fechacorte = "";
                    }
                    fechadesenergizacion = "";
                }
            }
        }
    }

    private String obtenerDataEventos(ArrayList<String[]> events) {
        //ArrayList<String> data = new ArrayList<String>();
        String dataevent = "";
        //perfil de carga de canal1
        for (int pf = 0; pf < events.size(); pf++) {
            for (int j = 15; j < events.get(pf).length - 2; j++) {
                dataevent += "" + events.get(pf)[j];
            }
        }
        return dataevent;
    }

    public String completarCeros(int valor, int numceros) {
        String data = "" + valor;
        while (data.length() < numceros) {
            data = "0" + data;
        }
        return data;
    }

    public void AlmacenaRegistros() {
        String registrosd1 = obtenerDataRegistros(RegistersDProfile1);
        String registrosd2 = obtenerDataRegistros(RegistersDProfile2);
        String registrosd3 = obtenerDataRegistros(RegistersDProfile3);
        String registrosd4 = obtenerDataRegistros(RegistersDProfile4);
        String registrosd5 = obtenerDataRegistros(RegistersDProfile5);
        String registrosd6 = obtenerDataRegistros(RegistersDProfile6);

        String registrosm1 = obtenerDataRegistros(RegistersMProfile1);
        String registrosm2 = obtenerDataRegistros(RegistersMProfile2);
        String registrosm3 = obtenerDataRegistros(RegistersMProfile3);
        String registrosm4 = obtenerDataRegistros(RegistersMProfile4);
        String registrosm5 = obtenerDataRegistros(RegistersMProfile5);
        String registrosm6 = obtenerDataRegistros(RegistersMProfile6);

        if (numcanales >= 1) {
            if (registrosd1.length() > 0) {
                lecturaRegistrosEPQS(registrosd1, med.getMarcaMedidor().getCodigo(), 0, med.getnSerie(), lconske.get(0).getCanal(), fechaactual, med.getNdiasreg() - 1);
            }
            if (registrosm1.length() > 0) {
                lecturaRegistrosEPQS(registrosm1, med.getMarcaMedidor().getCodigo(), 1, med.getnSerie(), lconske.get(0).getCanal(), fechaactual, med.getNmesreg() - 1);
            }
        }
        if (numcanales >= 2) {
            if (registrosd2.length() > 0) {
                lecturaRegistrosEPQS(registrosd2, med.getMarcaMedidor().getCodigo(), 0, med.getnSerie(), lconske.get(1).getCanal(), fechaactual, med.getNdiasreg() - 1);
            }
            if (registrosm2.length() > 0) {
                lecturaRegistrosEPQS(registrosm2, med.getMarcaMedidor().getCodigo(), 1, med.getnSerie(), lconske.get(1).getCanal(), fechaactual, med.getNmesreg() - 1);
            }
        }
        if (numcanales >= 3) {
            if (registrosd3.length() > 0) {
                lecturaRegistrosEPQS(registrosd3, med.getMarcaMedidor().getCodigo(), 0, med.getnSerie(), lconske.get(2).getCanal(), fechaactual, med.getNdiasreg() - 1);
            }
            if (registrosm3.length() > 0) {
                lecturaRegistrosEPQS(registrosm3, med.getMarcaMedidor().getCodigo(), 1, med.getnSerie(), lconske.get(2).getCanal(), fechaactual, med.getNmesreg() - 1);
            }
        }
        if (numcanales >= 4) {
            if (registrosd4.length() > 0) {
                lecturaRegistrosEPQS(registrosd4, med.getMarcaMedidor().getCodigo(), 0, med.getnSerie(), lconske.get(3).getCanal(), fechaactual, med.getNdiasreg() - 1);
            }
            if (registrosm4.length() > 0) {
                lecturaRegistrosEPQS(registrosm4, med.getMarcaMedidor().getCodigo(), 1, med.getnSerie(), lconske.get(3).getCanal(), fechaactual, med.getNmesreg() - 1);
            }
        }
        if (numcanales >= 5) {
            if (registrosd5.length() > 0) {
                lecturaRegistrosEPQS(registrosd5, med.getMarcaMedidor().getCodigo(), 0, med.getnSerie(), lconske.get(4).getCanal(), fechaactual, med.getNdiasreg() - 1);
            }
            if (registrosm5.length() > 0) {
                lecturaRegistrosEPQS(registrosm5, med.getMarcaMedidor().getCodigo(), 1, med.getnSerie(), lconske.get(4).getCanal(), fechaactual, med.getNmesreg() - 1);
            }
        }
        if (numcanales >= 6) {
            if (registrosd6.length() > 0) {
                lecturaRegistrosEPQS(registrosd6, med.getMarcaMedidor().getCodigo(), 0, med.getnSerie(), lconske.get(5).getCanal(), fechaactual, med.getNdiasreg() - 1);
            }
            if (registrosm6.length() > 0) {
                lecturaRegistrosEPQS(registrosm6, med.getMarcaMedidor().getCodigo(), 1, med.getnSerie(), lconske.get(5).getCanal(), fechaactual, med.getNmesreg() - 1);
            }
        }

    }

    private String obtenerDataRegistros(ArrayList<String[]> loadprofile) {
        //ArrayList<String> data = new ArrayList<String>();
        String datapf1 = "";
        //perfil de carga de canal1
        for (int pf = 0; pf < loadprofile.size(); pf++) {
            for (int j = 13; j < loadprofile.get(pf).length - 2; j++) {
                datapf1 += "" + loadprofile.get(pf)[j];
            }
        }
        return datapf1;
    }

    private void lecturaRegistrosEPQS(String trama, String marca, int tiporegistro, String serie, int numcanal, String fechaactual, int diasRegistros) {

        try {
            Calendar c = Calendar.getInstance();
            int i = 0;
            int numgrupos = 0;
            String valorv = "";
            int registrosProcesados = 0;//numero de registros procesados..(si el modulo 6 es igual a 0 se debe verificar la cantidad de grupos
            String unidad = "";//unidad en que vienen los registros
            String valorunidad = "";
            double divisor = 0.0;//valor para mover el punto decimal
            double multiplo = 0.0;//valor multiplicador de la lectura
            int ndiasleer = diasRegistros;
            Timestamp tsactual = new Timestamp(sdf3.parse(fechaactual).getTime());
            long fechalec = 0;
            String fechalectura = "";
            Timestamp timelectura = null;
            SimpleDateFormat sdfreg = new SimpleDateFormat("yy-MM-dd");
            SimpleDateFormat spf = new SimpleDateFormat("yy-MM-dd HH:mm");
            EConstanteKE econske = null;
            while (i < trama.length()) {
                if (registrosProcesados % 6 == 0) {
                    //numero de tarifas asociadas
                    numgrupos = Integer.parseInt(trama.substring(i, i + 2), 16);
                    i += 6;
                    unidad = trama.substring(i + 14, i + 16);
                    divisor = Math.pow(10, Integer.valueOf(trama.substring(i + 12, i + 14), 16));
                    multiplo = Long.parseLong(trama.substring(i + 10, i + 12) + trama.substring(i + 8, i + 10) + trama.substring(i + 6, i + 8) + trama.substring(i + 4, i + 6) + trama.substring(i + 2, i + 4) + trama.substring(i, i + 2), 16) / Math.pow(2, 32);
                    i += 16;
                }
                double energia = 0.0;
                valorv = "";//valor de la energia en bigEndian
                boolean almacena = true;
                for (int j = 0; j < numgrupos; j++) {
                    valorv = trama.substring(i + 6, i + 8) + "" + trama.substring(i + 4, i + 6) + "" + trama.substring(i + 2, i + 4) + "" + trama.substring(i, i + 2);
                    if (!valorv.equals("FFFFFFFF")) {
                        energia = energia + ((((double) Integer.parseInt(valorv, 16)) * multiplo) / divisor);
                    } else {
                        almacena = false;
                    }
                    i += 8;
                }

                if (unidad.toUpperCase().contains("00")) {
                    energia = energia / 1000;
                } else if (unidad.toUpperCase().contains("6D")) {
                    energia = energia * 1000;
                }
                if (tiporegistro == 0) {
                    fechalec = tsactual.getTime() - ((long) (86400000) * ndiasleer);
                } else {
                    c.set(tsactual.getYear() + 1900, tsactual.getMonth() + 1, 1);
                    c.add(Calendar.MONTH, ((ndiasleer + 1) * (-1)));
                    fechalec = c.getTime().getTime();
                }
                fechalectura = sdfreg.format(new Date(fechalec)) + " 00:00";;
                timelectura = new Timestamp(spf.parse(fechalectura).getTime());
                if (almacena) {
                    valorunidad = cp.getipoRegistro(marca, "" + numcanal, "" + tiporegistro, null);

                    econske = cp.buscarConske(lconske, numcanal);
                    if (econske != null && valorunidad.length() > 0) {
                        ////System.out.println("Serie " + serie + " año " + (timelectura.getYear() + 1900) + " Mes " + (timelectura.getMonth() + 1) + " dia " + timelectura.getDate() + "pulsos " + energia + " energia " + (energia * econske.getMultiplo()));
                        cp.actualizaRegistro(serie, tiporegistro, (timelectura.getYear() + 1900), (timelectura.getMonth() + 1), timelectura.getDate(), new Timestamp(new Date().getTime()), energia, String.valueOf(numcanal), valorunidad, energia * econske.getMultiplo(), null);
                    }
                }
                ndiasleer--;
                registrosProcesados++;
            }
        } catch (Exception e) {
            getErrorString(e.getStackTrace(), 3);
        }
    }

    public String obtenerDia(Calendar cal) {
        String offset = "";
        String calendario = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
        if (calendario.toUpperCase().equals("SUNDAY")) {
            offset = "01";

        } else if (calendario.toUpperCase().equals("MONDAY")) {
            offset = "02";

        } else if (calendario.toUpperCase().equals("TUESDAY")) {
            offset = "03";

        } else if (calendario.toUpperCase().equals("WEDNESDAY")) {
            offset = "04";

        } else if (calendario.toUpperCase().equals("THURSDAY")) {
            offset = "05";

        } else if (calendario.toUpperCase().equals("FRIDAY")) {
            offset = "06";

        } else if (calendario.toUpperCase().equals("SATURDAY")) {
            offset = "07";
        }
        return offset;
    }

    public String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
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

    public Timestamp obtenerFecha_Hora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
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
