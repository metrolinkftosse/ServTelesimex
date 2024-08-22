/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaIskraMT174;
import Entidades.Abortar;
import Entidades.EInstantaneos;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Metrolink
 */
public class LeerRemotoIskraMT174 extends Thread {

    private int reintentoconexion = 0;// reintentos utlizados
    int intentosPass = 0;
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    String seriemedidor = "";
    String fechaactual = "";
    Timestamp tsfechaactual;
    Timestamp tspeticion;
    Timestamp tsrecepcion;
    Timestamp tspeticioneventos;
    Timestamp tsrecepcioneventos;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    Timestamp time = null; //tiempo de NTP
    long ndesfasepermitido = 0;
    private long desfase;
    long tiempo = 500;
    private long blockDurationMillis = 21600000;
    InputStream input;
    OutputStream output;
    String cadenahex = "";
    TramasRemotaIskraMT174 tramasiskra = new TramasRemotaIskraMT174(); //maestro de tramas
    int indx = 0; //indice la tabla
    String password = ""; //password
    String password2 = "";
    String serial = "";
    EMedidor med;
    ControlProcesos cp;
    //opciones perfil, eventos, registros, configuracion de hora
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean errorMsg = false;
    //variable que indica si lee
    public boolean leer = true;
    //parametros de conexion puerto conexion,
    String numeroPuerto;
    int numeroReintentos = 4;
    int nreintentos = 0; //intento actual completo
    int velocidadPuerto; //velocidad del puerto
    long timeout;
    int ndias;
    int npeticiones = 0;//numero de peticiones que se realizaran al perfil de carga.
    int npeticioneseventos = 0;//numero de peticiones que se realizaran al perfil de carga.
    boolean portconect = false;
    Thread tEscritura = null;
    Thread tReinicio = null;
    int reintentosbadBCC = 0;
    public boolean cierrapuerto = false;
    private boolean perfilincompleto = false;
    public Socket socket;
    private volatile boolean escucha = true;//variable de control de escuchar el puerto
    Thread tLectura;
    boolean aviso = false; //control  de cambios de estado
    byte[] ack = {(byte) 0x06};
    byte[] nak = {(byte) 0x15};
    //Estados 
    boolean presentacionIni = false;
    boolean presentacion = false;
    boolean comunicacion = false;
    boolean contraseña = false;
    boolean serialmedidor = false;
    boolean fechaactualdate = false;
    boolean fechaactualtime = false;
    boolean perfilcarga = false;
    boolean saltobloque = false; //para trama incompleta vic 13-09-19
    public boolean tramaOK = false; //para revisión de tramas
    boolean registroeventos = false;
    boolean syncReloj = false;
    boolean logout = false;
    //vectores de datos
    String[] vPerfil = null;
    ArrayList<String[]> arrayperfil = new ArrayList<>();
    String[] vEventos = null;
    ArrayList<String[]> arrayeventos = new ArrayList<>();
    //variables de control de envio
    boolean enviando = false;
    boolean reenviando = false;
    byte[] ultimatramaEnviada = null;
    //formatos de fechas
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    private final SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    private final SimpleDateFormat sdf4 = new SimpleDateFormat("yyMMddHHmmss");
    private final SimpleDateFormat sdfacceso = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    int numcanales = 2; //numero de canales
    int intervalo = 0; //intervalo de datos.
    //variables para el log
    File file;
    RandomAccessFile fr;
    Abortar objabortar;
    String usuario = "admin";
    int reinicio = 0;

    //Actualización
    int contadorReinicios = 0;//camilo 20-05-21
    SimpleDateFormat sdf5 = new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS");
    public int contadorTramasIncompletas; //Para controlar número de reenvíos por trama incompleta Camilo 26-04-21
    public int contadorTramasIncompletasEv; //Para controlar número de reenvíos por trama incompleta Camilo 26-04-21
    boolean flagConexion = false;
    //Actualización 3
    int conteoBloqueos = 0;
    ZoneId zid;
    private final Object monitor = new Object();

    //Variables Datos FPOT Camilo
    String fpot1 = "";
    String voltajel1 = "";
    String corrientel1 = "";
    String fpot2 = "";
    String voltajel2 = "";
    String corrientel2 = "";
    String fpot3 = "";
    String voltajel3 = "";
    String corrientel3 = "";
    //--------------------------------
    //Control Factor de Potencia--------------------------------
    int lineCounter = 1;
    boolean factorEnPerfil = false; //Si pide factor de potencia con perfil de carga
    boolean lVoltaje = false; //Estado factor de potencia camilo
    boolean lCorriente = false; //Estado factor de potencia camilo
    boolean lFactPot = false; //Estado factor de potencia camilo
    boolean lCorr = false; // Instantáneos para factor de potencia Camilo
    boolean lFPot = false; // Instantáneos para factor de potencia Camilo
    String avisoTemp = "";
    Double datoDouble = 0.0;
    Double fp1 = 0.0;
    Double fp2 = 0.0;
    Double fp3 = 0.0;
    Double fp1Neg = 1.0;
    Double fp2Neg = 1.0;
    Double fp3Neg = 1.0;
    EInstantaneos inst;
    Double anguloDesfasePermitido;
    double datoFPOT = 0.0;
    private Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    private ArrayList<EtipoCanal> vtipocanal;
    private final String label = "LeerTCPIskraMT174";
    //-----------------------

    public LeerRemotoIskraMT174(EMedidor med, boolean perfil, boolean factorPotencia, boolean eventos, boolean registros, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, String usuario, ZoneId zid, long ndesfase) {
        this.med = med;
        this.cp = cp;
        this.usuario = usuario;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        try {
            File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
            if (!f.exists()) {
                f.mkdirs();
            }
            file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        lFPot = factorPotencia;
        lperfil = perfil;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        jinit();
    }
    //iniciador de comunicacion 

    private void jinit() {
        try {
            tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
            //configuracion de equipos 
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            password2 = med.getPassword2();
            timeout = med.getTimeout() * 1000;
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            serial = seriemedidor;
            factorEnPerfil = cp.getFactorEnPerfil(null, serial);
            while (serial.length() < 8) {
                serial = "0" + serial;
            }
            flagConexion = false;
            anguloDesfasePermitido = cp.getAnguloDesfasePermitido(null);
            lconske = cp.buscarConstantesKe(med.getnSerie());//se toman los valores de las constantes 
            for (EConstanteKE consKe : lconske) {
                escribir("" + consKe.getCanal());
            }
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            for (EtipoCanal canal : vtipocanal) {
                escribir("" + canal.getUnidad());
            }

            abrePuerto();
            tiempo = 1000;
        } catch (Exception e) {
            leer = false;
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }
    //metodo que abre el puerto

    private void abrePuerto() {
        try {
            if (!objabortar.labortar) {
                if (reintentoconexion < numeroReintentos) { //3) { //parametrizable con reintentos de conf_medidor                    
                    //System.out.println("Conectando.. " + med.getDireccionip() + ":" + Integer.valueOf(med.getPuertoip()));
                    socket = new Socket(med.getDireccionip(), Integer.valueOf(med.getPuertoip())); //vic 19-09-19

                    //System.out.println("Conectado " + med.getDireccionip() + ":" + med.getPuertoip());
                    socket.setSoTimeout(35000);
                    if (!socket.isClosed()) {
                        portconect = true;
                    } else {
                        portconect = false;
                    }
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

    //metodo que escucha las peticiones
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
    //cambia la cadena recibida en bytes a una cadena de tipo string para procesar mas eficiente.

    private void procesaCadena() {
        //tamaño buffer        
        byte[] readBuffer = new byte[8192];
        byte[] auxBuffer = new byte[8192];
        byte[] tempBuffer;
        int idxFrame = 0;
        int numbytes;
        byte begin = 0;
        byte end = 0;
        boolean uncomplete = true;
        boolean beginOk = false;
        boolean endOk = false;
        boolean keep = false;
        int counter = 0;
        errorMsg = false;
        try {
            synchronized (monitor) {
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                while (!socket.isClosed() && uncomplete && System.currentTimeMillis() < endT) {
                    monitor.wait(200);
                    if (input.available() == 0) {
                        continue;
                    }
                    endT = System.currentTimeMillis() + timeout;
                    numbytes = input.read(readBuffer);
                    //System.out.println("Arriving: "+tramasiskra.encode(readBuffer, numbytes));
                    if (idxFrame == 0) {
                        begin = readBuffer[0];
                        if (begin == 21) {
                            //System.out.println("NAK");                           
                        }
                        if (contraseña || syncReloj) {
                            beginOk = begin == 1 || begin == 6 || begin == 21;
                        } else {
                            beginOk = begin == 47 || begin == 1 || begin == 2;
                        }

                        ////System.out.println(" "+begin);
                    }
                    if (beginOk) {
                        end = numbytes >= 2 ? readBuffer[numbytes - 2] : readBuffer[numbytes - 1];
                        ////System.out.println(" "+end);
                        if (!keep) {
                            switch (begin) {
                                case 1:
                                    endOk = end == 3;
                                    break;
                                case 2:
                                    endOk = end == 3;
                                    break;
                                case 6:
                                    endOk = true;
                                    break;
                                case 21:
                                    endOk = true;
                                    break;
                                case 47:
                                    endOk = end == 13;
                                    break;
                                default:
                                    //System.out.println("La trama no comienza correctamente.");
                                    break;
                            }
                        }
                    } else {
                        if (readBuffer[1] == 1 || readBuffer[1] == 2) {
                            //System.out.println("Removiendo el primer Byte.");
                            tempBuffer = removeElement(readBuffer, 0);
                            readBuffer = tempBuffer;
                            numbytes -= 1;
                            beginOk = true;
                            end = numbytes >= 2 ? readBuffer[numbytes - 2] : readBuffer[numbytes - 1];
                            ////System.out.println(" "+end);
                            endOk = end == 3;
                        } else {
                            //System.out.println("El inicio de la trama no ha sido el correcto.");
                            reenviando = true;
                            enviando = false;
                            monitor.notifyAll();
                            return;
                        }
                    }
                    //System.out.println("begin: "+beginOk+" endOk: "+endOk);
                    if (!beginOk || !endOk) {
                        System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                        idxFrame += numbytes;
                        //System.out.println("Buffer Auxiliar: "+tramasiskra.encode(auxBuffer, idxFrame)+" Idx Frame :"+idxFrame);

                    } else if (idxFrame == 0) {
                        if (readBuffer[6] == 69 && readBuffer[7] == 82 && readBuffer[8] == 82 && readBuffer[9] == 79 && readBuffer[10] == 82) {
                            //System.out.println("El medidor ha enviado un mensaje de ERROR");
                            errorMsg = true;
                        }
                        //System.out.println("Salir del bucle de escucha forma 1.");
                        auxBuffer = readBuffer;
                        idxFrame = numbytes;
                        uncomplete = false;
                        enviando = false;
                        reenviando = false;
                        monitor.notifyAll();
                    } else {
                        //System.out.println("Salir del bucle de escucha forma 2."+numbytes);                        
                        System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                        idxFrame += numbytes;
                        //System.out.println("Buffer Auxiliar: " + tramasiskra.encode(auxBuffer, idxFrame) + " idxFrame: " + idxFrame);
                        if (numbytes >= 2) {
                            if (auxBuffer[6] == 69 && auxBuffer[7] == 82 && auxBuffer[8] == 82 && auxBuffer[9] == 79 && auxBuffer[10] == 82) {
                                //System.out.println("El medidor ha enviado un mensaje de ERROR");
                                errorMsg = true;
                            }
                            uncomplete = false;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                        } else if (numbytes == 1) {
                            counter++;
                            if (counter == 1) {
                                keep = true;
                            } else if (counter == 2) {
                                if (auxBuffer[6] == 69 && auxBuffer[7] == 82 && auxBuffer[8] == 82 && auxBuffer[9] == 79 && auxBuffer[10] == 82) {
                                    //System.out.println("El medidor ha enviado un mensaje de ERROR");
                                    errorMsg = true;
                                }
                                keep = false;
                                uncomplete = false;
                                enviando = false;
                                reenviando = false;
                                monitor.notifyAll();
                            }
                        }
                    }
                }
                if (!socket.isClosed() && uncomplete) {
                    if (Arrays.equals(ultimatramaEnviada, tramasiskra.getDesconexion())) {
                        //System.out.println("Se vencio el timeout de respuesta dado que no existe respuesta para una peticion de desconexion");
                        cerrarPuerto(false);
                        reenviando = false;
                        enviando = false;
                        monitor.notifyAll();
                        return;
                    } else {
                        //Se vencío el timeout de respuesta sin recibir nada
                        //System.out.println("Se vencio el timeout de respuesta sin recibir nada");
                        reenviando = true;
                        enviando = false;
                        monitor.notifyAll();
                        return;
                    }
                }
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramasiskra.encode(auxBuffer, idxFrame);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
            //luego de tener la trama desglosada byte x byte continuamos a interpretarla
            if (cadenahex.length() > 0) {
                //System.out.println("Llega dato");
                interpretaCadena(cadenahex);
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
            cerrarPuerto(true);
            reenviando = false;
            enviando = false;
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    private void revisaPresentacionIni(String cadenahex, String[] vectorhex) {
        //identificacion de fin de trama  
        try {
            if (cadenahex.contains("2F 49 53 6B 35 4D 54 31 37 34 2D 30 30 30")) {
                if (cadenahex.contains("30 2E 39 2E 31")) { //actualización 3
                    escribir("Bloqueo en la comunicacion");
                    byte data[] = tramasiskra.getDesconexion();
                    ultimatramaEnviada = data;
                    enviaTrama2_2(data, "=> Desconexion");
                } else {
                    presentacionIni = false;
                    presentacion = true;
                    byte[] data = tramasiskra.getComunicacion();
                    ultimatramaEnviada = data;
                    enviaTrama2_2(data, "=> Solicitud de Comunicacion");
                }
            } else if (vectorhex[0].equals("15")) { //camilo 20-05-21
                byte data[] = tramasiskra.getDesconexion();
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Desconexión");
            } else {
                //no es la identificacion de iskra
                escribir("Identificación fallida");
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2_2(ultimatramaEnviada, "=> Reenvio Solicitud de identificación");
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Identifiacion fallida");
                    reintentosbadBCC = 0;
                    presentacionIni = false;
                    escribir("Estado lectura No leido");
                    reiniciaComunicacion();
                }
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion - Error procesando presentacion");
            reintentosbadBCC = 0;
            presentacionIni = false;
            escribir("Estado lectura No leido");
            reiniciaComunicacion();
        }
    }

    private void revisaPresentacion(String cadenahex, String[] vectorhex) {
        //identificacion de fin de trama  
        try {
            if (cadenahex.contains("01 50 30 02 28")) { //Camilo 24-05-21

                presentacion = false;
                enviaPass();
            } else if (vectorhex[0].equals("15")) { //camilo 20-05-21
                byte data[] = tramasiskra.getDesconexion();
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Envío de trama de desconexión");
            } else {
                //no es la identificacion de iskra
                escribir("Establecer modo de comunicacion fallido");
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2_2(ultimatramaEnviada, "=> Reenvio Solicitud de establecimiento de modo de comunicacion");
                } else {
                    escribir("Desconexion establecer Modo de comunicación fallido");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion establecer Modo de comunicación fallido");
                    reintentosbadBCC = 0;
                    presentacion = false;
                    escribir("Estado lectura No leido");
                    reiniciaComunicacion();
                }
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion - Error procesando presentacion");
            reintentosbadBCC = 0;
            presentacion = false;
            escribir("Estado lectura No leido");
            reiniciaComunicacion();
        }
    }

    private void revisaComunicacion(String cadenahex, String[] vectorhex) {
        try {
            if (cadenahex.contains("30 2E 39 2E 31")) { //actualización 3
                escribir("Bloqueo en la comunicacion");
                byte data[] = tramasiskra.getDesconexion();
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Desconexion");
            } else if (vectorhex.length > 1 && vectorhex[0].equals("01") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin                
                if (validarBCC(vectorhex)) {
                    enviaPass();
                } else {
                    try {
                        //System.out.println("BAD BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    reintentosbadBCC++;
                    if (reintentosbadBCC < 4) {
                        enviaTrama2_2(ultimatramaEnviada, "=> Reenvio Solicitud de modo comunicacion");
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error BCC");
                        reintentosbadBCC = 0;
                        comunicacion = false;
                        escribir("Estado lectura No leido");
                        reiniciaComunicacion();
                    }
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2_2(ultimatramaEnviada, "=> Reenvio Solicitud de modo comunicacion");
                } else {
                    comunicacion = false;
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error BCC");
                    reintentosbadBCC = 0;
                    escribir("Estado lectura No leido");
                    reiniciaComunicacion();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion - Error procesando presentacion");
            reintentosbadBCC = 0;
            presentacion = false;
            escribir("Estado lectura No leido");
            reiniciaComunicacion();
        }
    }

    private void enviaPass() {
        byte data[] = tramasiskra.getPassword();

        data[5] = (byte) (Integer.parseInt(convertStringToHex(password.substring(0, 1)), 16) & 0xFF);
        data[6] = (byte) (Integer.parseInt(convertStringToHex(password.substring(1, 2)), 16) & 0xFF);
        data[7] = (byte) (Integer.parseInt(convertStringToHex(password.substring(2, 3)), 16) & 0xFF);
        data[8] = (byte) (Integer.parseInt(convertStringToHex(password.substring(3, 4)), 16) & 0xFF);
        data[9] = (byte) (Integer.parseInt(convertStringToHex(password.substring(4, 5)), 16) & 0xFF);
        data[10] = (byte) (Integer.parseInt(convertStringToHex(password.substring(5, 6)), 16) & 0xFF);
        data[11] = (byte) (Integer.parseInt(convertStringToHex(password.substring(6, 7)), 16) & 0xFF);
        data[12] = (byte) (Integer.parseInt(convertStringToHex(password.substring(7, 8)), 16) & 0xFF);

        data[data.length - 1] = calcularBCC(data);
        comunicacion = false;
        contraseña = true;
        ultimatramaEnviada = data;
        enviaTrama2_2(data, "=> Solicitud de password");
    }

    private void revisaPass(String cadenahex, String[] vectorhex) {
        if (cadenahex.contains("06")) {
            enviaSerial();
        } else if (vectorhex[0].equals("15")) { //actualización
            intentosPass++;
            //System.out.println("Recibe NACK");
            if (intentosPass < 2) {
                enviaTrama2_2(ultimatramaEnviada, "=> Solicitud de password");
            } else {
                //System.out.println("Error password");
                escribir("BAD PASSWORD");
                contraseña = false;
                cerrarPuerto(true);
                escribir("Desconexion - Error de autenticacion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de autenticacion");
                cerrarLog("Desconexion Error de autenticacion", false);
                leer = false;
            }
        } else {
            //System.out.println("Error password");
            escribir("BAD PASSWORD");
            contraseña = false;
            cerrarPuerto(true);
            escribir("Desconexion - Error de autenticacion");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de autenticacion");
            cerrarLog("Desconexion Error de autenticacion", false);
            leer = false;
        }
    }

    private void enviaSerial() {
        contraseña = false;
        serialmedidor = true;
        byte[] data = tramasiskra.getSeriemedidor();
        data[data.length - 1] = calcularBCC(data);
        ultimatramaEnviada = data;
        enviaTrama2_2(data, "=> Solicitud serial de medidor");
    }

    private void revisaSerialMedidor(String[] vectorhex) {
        String peticion = "Serial Medidor";
        vectorhex = revisaTrama2(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            String datoserial = "";
            for (int i = 0; i < 8; i++) {
                datoserial = datoserial + vectorhex[2 + i];
            }
            datoserial = Hex2ASCII(datoserial);
            if (seriemedidor.equals(datoserial)) {
                serialmedidor = false;
                escribir("Numero de serial " + datoserial);
                inst = new EInstantaneos(seriemedidor, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                if (lFPot) { //Valores instantaneos
                    enviaFPot1();
                } else {
                    enviaFechaActual();
                }
            } else {
                escribir("Numero de serial incorrecto");
                serialmedidor = false;
                cerrarPuerto(true);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Serial Incorrecto");
                escribir("Estado lectura No leido");
                cerrarLog("Desconexion Error de serial", false);
                leer = false;
            }
        }
    }

    private void enviaFPot1() {
        byte data[] = tramasiskra.getFPL1();
        data[data.length - 1] = calcularBCC(data);
        ultimatramaEnviada = data;
        lFactPot = true;
        lFPot = false;
        enviaTrama2_2(data, "=> Solicitud Instantáneo FP1");
    }

    private double sacarDatosFPOT(String[] vectorhex) {
        List listaHex = Arrays.asList(vectorhex);
        ////System.out.println(listaHex);
        if (listaHex.contains("28") && listaHex.contains("29") && tramaOK) {
            tramaOK = false;
            boolean inicio = false;
            boolean fin = false;
            String instantaneo = "";
            for (Object i : listaHex) {
                if (String.valueOf(i).equals("29")) {
                    inicio = false;
                    fin = true;
                }
                if (inicio) {
                    if (i.equals("2A")) {
                        break;
                    }
                    instantaneo += i;
                }
                if (String.valueOf(i).equals("28")) {
                    inicio = true;
                }
            }
            ////System.out.println("ins" +instantaneo);
            instantaneo = Hex2ASCII(instantaneo);
            ////System.out.println("ins2" +instantaneo);
            datoFPOT = Double.parseDouble(instantaneo);
            ////System.out.println("datofpot" +datoFPOT);
        }
        return datoFPOT;
    }

    private void revisaFPot(String[] vectorhex) {
        String peticion = "Factor de Potencia";
        vectorhex = revisaTrama2(vectorhex, peticion);
        try {
            if (tramaOK) {
                lFactPot = false;
                datoDouble = sacarDatosFPOT(vectorhex);
                tramaOK = false;
                if (datoDouble <= 1 && datoDouble >= -1) {
                    //System.out.println("Factor de Potencia correcto");
                    //System.out.println(datoDouble);
                    enviaVoltaje();
                } else {
                    //System.out.println("Factor de Potencia incorrecto, se ignorarán el resto de instantáneos.");
                    escribir("Error al obtener Factor de Potencia, se ignorarán el resto de instantáneos.");
                    enviaFechaActual();
                }
            } else {
                //System.out.println("Trama incorrecta, se ignorarán el resto de instantáneos.");
                escribir("Trama incorrecta, se ignorarán el resto de instantáneos.");
                enviaFechaActual();
            }//Condicional tramaOk  
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Factor de Potencia incorrecto, se ignorarán el resto de instantáneos.");
            escribir("Error al obtener Factor de Potencia, se ignorarán el resto de instantáneos.");
            enviaFechaActual();
        }
    }

    private void enviaVoltaje() {
        byte data[] = null;
        lFactPot = false;
        lVoltaje = true;
        double angulo = Math.abs(Math.acos(datoDouble));
        switch (lineCounter) {
            case 1: {

                inst.setFp1(angulo);
                data = tramasiskra.getVL1();
                avisoTemp = "VL1";
                break;
            }
            case 2: {
                inst.setFp2(angulo);
                data = tramasiskra.getVL2();
                avisoTemp = "VL2";
                break;
            }
            case 3: {
                inst.setFp3(angulo);
                data = tramasiskra.getVL3();
                avisoTemp = "VL3";
                break;
            }
        }
        data[data.length - 1] = calcularBCC(data);
        ultimatramaEnviada = data;
        enviaTrama2_2(data, "=> Solicitud Instantáneo " + avisoTemp);
    }

    private void revisaVoltaje(String[] vectorhex) {
        String peticion = "Voltaje Instantáneo";
        vectorhex = revisaTrama2(vectorhex, peticion);
        try {
            if (tramaOK) {
                lVoltaje = false;
                datoDouble = sacarDatosFPOT(vectorhex);
                tramaOK = false;
                if (datoDouble >= 0) {
                    //System.out.println("Voltaje Instantáneo correcto");
                    enviaCorriente();
                } else {
                    //System.out.println("Voltaje Instantáneo Incorrecto, se ignorarán el resto de instantáneos.");
                    escribir("Error al obtener Voltaje Instantáneo, se ignorarán el resto de instantáneos.");
                    enviaFechaActual();
                }
            } else {
                //System.out.println("Trama incorrecta, se ignorarán el resto de instantáneos.");
                escribir("Trama incorrecta, se ignorarán el resto de instantáneos.");
                enviaFechaActual();
            }//Condicional tramaOk  
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Voltaje Instantáneo incorrecto, se ignorarán el resto de instantáneos.");
            escribir("Error al obtener Voltaje Instantáneo, se ignorarán el resto de instantáneos.");
            enviaFechaActual();
        }
    }

    private void enviaCorriente() {
        byte data[] = null;
        lVoltaje = false;
        lCorriente = true;
        switch (lineCounter) {
            case 1: {
                inst.setV1(datoDouble);
                data = tramasiskra.getIL1();
                avisoTemp = "IL1";
                break;
            }
            case 2: {
                inst.setV2(datoDouble);
                data = tramasiskra.getIL2();
                avisoTemp = "IL2";
                break;
            }
            case 3: {
                inst.setV3(datoDouble);
                data = tramasiskra.getIL3();
                avisoTemp = "IL3";
                break;
            }
        }
        data[data.length - 1] = calcularBCC(data);
        ultimatramaEnviada = data;
        enviaTrama2_2(data, "=> Solicitud Instantáneo " + avisoTemp);
    }

    private void revisaCorriente(String[] vectorhex) {
        String peticion = "Corriente Instantánea";
        vectorhex = revisaTrama2(vectorhex, peticion);
        try {
            if (tramaOK) {
                lCorriente = false;
                datoDouble = sacarDatosFPOT(vectorhex);
                tramaOK = false;
                if (datoDouble >= 0) {
                    //System.out.println("Corriente Instantánea correcto");
                    enviaFPot2y3();
                } else {
                    //System.out.println("Corriente Instantánea Incorrecta, se ignorarán el resto de instantáneos.");
                    escribir("Error al obtener la Corriente Instantánea, se ignorarán el resto de instantáneos.");
                    enviaFechaActual();
                }
            } else {
                //System.out.println("Trama incorrecta, se ignorarán el resto de instantáneos.");
                escribir("Trama incorrecta, se ignorarán el resto de instantáneos.");
                enviaFechaActual();
            }//Condicional tramaOk  
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Corriente Instantánea incorrecto, se ignorarán el resto de instantáneos.");
            escribir("Error al obtener Corriente Instantánea, se ignorarán el resto de instantáneos.");
            enviaFechaActual();
        }
    }

    private void enviaFPot2y3() throws Exception {
        byte data[] = null;
        switch (lineCounter) {
            case 1: {
                inst.setI1(datoDouble);
                data = tramasiskra.getFPL2();
                avisoTemp = "FPL2";
                lFactPot = lFPot = true;
                lineCounter++;
                lCorriente = false;
                data[data.length - 1] = calcularBCC(data);
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Instantáneo " + avisoTemp);
                break;
            }
            case 2: {
                inst.setI2(datoDouble);
                data = tramasiskra.getFPL3();
                avisoTemp = "FPL3";
                lFactPot = lFPot = true;
                lineCounter++;
                lCorriente = false;
                data[data.length - 1] = calcularBCC(data);
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Instantáneo " + avisoTemp);
                break;
            }
            case 3: {
                inst.setI3(datoDouble);
                lineCounter = 1;
                lCorriente = false;
                //lógica para estimar factor de potencia ok
                //El límite para factor de potencia OK, es 0.574 que equivale a un ángulo de 55 grados
                //dejando una tolerancia de 2.5 grados por fase.
                //PARA FPOT
                //double fpotpermitido = Math.abs(Math.cos(anguloDesfasePermitido));
                ////System.out.println(inst.getFp1());
                //System.out.println(inst.getFp1());
                //System.out.println(inst.getFp2());
                //System.out.println(inst.getFp3());
                //System.out.println(inst.getV1());
                //System.out.println(inst.getV2());
                //System.out.println(inst.getV3());
                //System.out.println(inst.getI1());
                //System.out.println(inst.getI2());
                //System.out.println(inst.getI3());
                if (inst.getFp1() <= anguloDesfasePermitido && inst.getFp2() <= anguloDesfasePermitido && inst.getFp3() <= anguloDesfasePermitido) {
                    inst.setFpOK("OK");
                } else {
                    inst.setFpOK("DESF");
                }

                //PARA ÁNGULOS                                                        
//                    if (inst.getFp1() < 0) {
//                        fp1Neg = -1.0;
//                        fp1 = -1 * inst.getFp1();
//                    } else {
//                        fp1 = inst.getFp1();
//                    }
//                    if (inst.getFp2() < 0) {
//                        fp2Neg = -1.0;
//                        fp2 = -1 * inst.getFp2();
//                    } else {
//                        fp2 = inst.getFp2();
//                    }
//                    if (inst.getFp3() < 0) {
//                        fp3Neg = -1.0;
//                        fp3 = -1 * inst.getFp3();
//                    } else {
//                        fp3 = inst.getFp3();
//                    }
//                    if (fp1 <= anguloDesfasePermitido && fp2 <= anguloDesfasePermitido && fp3 <= anguloDesfasePermitido) {
//                        inst.setFpOK("OK");
//                    } else {
//                        inst.setFpOK("DESF");
//                    }
//                    inst.setFp1(fp1Neg * fp1);
//                    inst.setFp2(fp2Neg * fp3);
//                    inst.setFp3(fp3Neg * fp3);
                cp.ingresaInstantaneos(inst);
                datoDouble = datoDouble = 0.0;
                if (lFPot && !factorEnPerfil) {
                    enviaFechaActual();
                } else if (leventos) {
                    data = tramasiskra.getEventos();
                    npeticioneseventos = (int) (med.getNdiaseventos() / 5);
                    if (npeticioneseventos == 0) {
                        npeticioneseventos = 1;
                    }
                    //System.out.println("Numero de bloques a solicitar eventos" + npeticioneseventos);
                    long ndiaseventos = (tsfechaactual.getTime() - (86400000L * (long) med.getNdiaseventos()));
                    String fechapeticion = "";
                    tspeticioneventos = new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(ndiaseventos))).getTime());

                    //System.out.println("Fecha peticion inicial" + tspeticioneventos);
                    fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                    if (npeticioneseventos > 1) {
                        tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
                        //System.out.println("Fecha peticion final" + tsrecepcioneventos);
                        data[9] = (byte) 0x30;
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                        String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
                        data[21] = (byte) 0x30;
                        data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                        data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                        data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                        data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                        data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                        data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                        data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                        data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                        data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                        data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                    } else {
                        data = tramasiskra.getEventosfinal();
                        data[9] = (byte) 0x30;
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                    }

                    vEventos = null;
                    arrayeventos = new ArrayList<String[]>();
                    registroeventos = true;
                    data[data.length - 1] = calcularBCC(data);
                    enviaTrama2_2(data, "=> Solicitud de eventos " + fechapeticion);
                    ultimatramaEnviada = data;
                    enviaTrama2_2(data, "=> Solicitud Registro de Eventos");
                } else {
                    fechaactualtime = false;
                    cerrarPuerto(true);
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
                    escribir("Estado lectura Leido");
                    //System.out.println("Almacenando Datos");                        
                    almacenarRegistros();
                    med.MedLeido = true;
                    cerrarLog("Leido", true);
                    leer = false;
                }
                break;
            }
        }
    }

    private void enviaFechaActual() {
        byte data[] = tramasiskra.getFechaactual();
        data[data.length - 1] = calcularBCC(data);
        ultimatramaEnviada = data;
        fechaactualdate = true;
        enviaTrama2_2(data, "=> Solicitud Fecha Actual");
    }

    private void revisaFechaActual(String[] vectorhex) {
        String peticion = "Fecha actual";
        vectorhex = revisaTrama2(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            fechaactual = vectorhex[3] + vectorhex[4] + vectorhex[5] + vectorhex[6] + vectorhex[7] + vectorhex[8];
            fechaactualdate = false;
            escribir("Desfase Permitido " + ndesfasepermitido);
            time = obtenerHora();
            deltatimesync1 = new Timestamp(Calendar.getInstance().getTimeInMillis());
            enviaHoraActual();
        }
    }

    private void enviaHoraActual() {
        byte data[] = tramasiskra.getFechatime();
        data[data.length - 1] = calcularBCC(data);
        ultimatramaEnviada = data;
        fechaactualtime = true;
        enviaTrama2_2(data, "=> Solicitud Hora Actual");
    }

    private void calculaDiasALeer(Timestamp fechasys) throws ParseException {
        try {
            if (med.getFecha() != null) {
                Calendar lastReadJustDate = Calendar.getInstance();
                lastReadJustDate.setTimeInMillis(med.getFecha().getTime());
                lastReadJustDate.set(Calendar.HOUR_OF_DAY, 0);
                lastReadJustDate.set(Calendar.MINUTE, 0);
                lastReadJustDate.set(Calendar.SECOND, 0);
                lastReadJustDate.set(Calendar.MILLISECOND, 0);
                escribir("Fecha Ultima Lectura " + med.getFecha());
                //System.out.println("Fecha Ultima Lectura " + med.getFecha());
                Long actual = fechasys.getTime() - (desfase * 1000); // Milisegundos de la fecha actual
                Long ultimamed = med.getFecha().getTime(); // Milisegundos de la fecha de última lectura
                Long dayInMillis = (long) 86400000; // Un día en Milisegundos
                Long hourInMillis = (long) 3600000;
                Long lastReadMillis = ultimamed - (ultimamed % hourInMillis);
                Long daysDiffInMillis = actual - lastReadJustDate.getTimeInMillis();
                int diasaleer = (int) Math.ceil((double) daysDiffInMillis / (double) dayInMillis) - 1;
                //System.out.println("dias calculados " + diasaleer);
                npeticiones = diasaleer;
                escribir("Numero de dias leer calculado " + (diasaleer));
                blockDurationMillis = dayInMillis;
                tspeticion = new Timestamp(lastReadMillis);
                if (diasaleer > 0) {
                    tsrecepcion = new Timestamp(lastReadJustDate.getTimeInMillis() + blockDurationMillis);
                }
            } else {
                //pedimos los dias por defecto
                ndias = med.getNdias();
                escribir("Numero de dias leer calculado por defecto" + (med.getNdias()));
                tspeticion = new Timestamp((sdfarchivo.parse(sdfarchivo.format(new Date(fechasys.getTime()))).getTime()) - ((long) (86400000) * ndias));
                npeticiones = ((ndias) * 4) - 1; //vic 16-09-19 before: ((ndias) * 2) - 1 si es 15 min 96    
                tsrecepcion = new Timestamp(tspeticion.getTime() + blockDurationMillis);
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
            escribir("Error calculando numero de dias a leer - Selecciona 1 día por defecto");
            tspeticion = new Timestamp((sdfarchivo.parse(sdfarchivo.format(new Date(fechasys.getTime()))).getTime()) - ((long) (86400000)));
            npeticiones = ((1) * 4) - 1; //vic 16-09-19 before: ((ndias) * 2) - 1 si es 15 min 96
            tsrecepcion = new Timestamp(tspeticion.getTime() + blockDurationMillis);

        } finally {
            escribir("Numero de intervalos a solicitar" + npeticiones);
            //System.out.println("Numero de intervalos a solicitar" + npeticiones);
            escribir("Fecha peticion inicial" + tspeticion);
            //System.out.println("Fecha peticion inicial" + tspeticion);
            if (npeticiones > 0) {
                escribir("Fecha peticion final" + tsrecepcion);
                //System.out.println("Fecha peticion final" + tsrecepcion);
            }
        }
    }

    private void revisaHora(String[] vectorhex) throws ParseException {
        String peticion = "Hora actual";
        vectorhex = revisaTrama2(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            fechaactual = Hex2ASCII(fechaactual + vectorhex[2] + vectorhex[3] + vectorhex[4] + vectorhex[5] + vectorhex[6] + vectorhex[7]);
            //System.out.println("Valor Fecha " + fechaactual);
            Timestamp fechasys = new Timestamp(Calendar.getInstance().getTimeInMillis());
            //********Cambio para ajuste de reloj***********//
            boolean solicitar = true;
            try {
                deltatimesync2 = new Timestamp(Calendar.getInstance().getTimeInMillis());
                tsfechaactual = new Timestamp(sdf4.parse(fechaactual).getTime());
                escribir("Fecha Actual Medidor " + tsfechaactual);
                escribir("Fecha Actual Estacion de trabajo " + fechasys);
                try {
                    escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                    escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                    desfase = (time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000;
                    if (Math.abs(desfase) > ndesfasepermitido) {
                        solicitar = false;//OJO
                        escribir("No se solicitara el perfil de carga");
                    } else {
                        solicitar = true;
                    }
                    cp.actualizaDesfase(desfase, med.getnSerie());
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            ///***************************Fin ajuste sincronizacion *****************************//
            fechaactualtime = false;
            if (lperfil) {// selecion perfil de carga
                if (solicitar) {
                    enviaPerfil(fechasys);
                } else {
                    med.MedLeido = true;
                    Desconexion("Desfase - Medidor no sincronizado");
                }
            } else if (leventos) {// solo seleciono registro de eventos
                enviaEventos();
            } else {// no seleciono nada por lo tanto termina comunicacion.
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK sin opciones");
                cerrarPuerto(true);
                escribir("Estado lectura Leido");
                cerrarLog("Leido", true);
                leer = false;
            }
        }
    }

    private void enviaPerfil(Timestamp fechasys) {
        try {
            calculaDiasALeer(fechasys);
            //debemos organizar la fecha en que se pedira el perfil y se usaran tiempo completos es decir si son las 3 am se pide desde las 0:15 de ese dia 
            String fechapeticion = sdf.format(new Date(tspeticion.getTime()));
            byte[] data = tramasiskra.getPerfil();
            //System.out.println("Tamaño peticion perfil carga " + data.length);
            if (npeticiones == 0) {//hereeee
                //se solicita el perfil sin fecha final
                data = tramasiskra.getPerfilfinal();
                data[9] = (byte) 0x30;
                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
            } else {
                //si es un perfil intermedio  se solicita en un rango de fechas
                data[9] = (byte) 0x30;
                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                String fechafin = sdf.format(new Date(tsrecepcion.getTime()));
                data[21] = (byte) 0x30;
                data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
            }
            data[data.length - 1] = calcularBCC(data);
            ultimatramaEnviada = data;
            perfilcarga = true;
            vPerfil = null;
            arrayperfil = new ArrayList<>();
            ultimatramaEnviada = data;
            tiempo = timeout;
            enviaTrama2_2(data, "=> Solicitud Perfil de carga");
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
            escribir("Error obteniendo solicitud de Perfil de carga");
            Desconexion("Error obteniendo solicitud de Perfil de carga");
        }
    }

    private void revisaPerfil(String[] vectorhex) throws ParseException {
        String peticion = "Perfil de Carga";
        vectorhex = revisaTrama2(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            npeticiones--;
            // if (!saltobloque){
            try {
                if (!errorMsg) {
                    vPerfil = new String[vectorhex.length - 3];
                    System.arraycopy(vectorhex, 1, vPerfil, 0, vPerfil.length);
                    arrayperfil.add(vPerfil);
                }
            } catch (Exception e) {
                cerrarPuerto(true);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Lectura incompleta");
                almacenarRegistros();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Almacena perfil parcial");
                escribir("Estado lectura No leido");
                reintentoconexion++;
                abrePuerto();
                return;
            }
            // saltobloque = false;
            //numero de peticiones del perfil se detienen cuando llega a -1 eso quiere decir que se solicita la ultima trama de perfil
            if (npeticiones >= 0) {
                perfilincompleto = true;
                String fechaTramaRecibida = "";
                if (!errorMsg) {
                    fechaTramaRecibida = "20" + Hex2ASCII(vectorhex[7]) + Hex2ASCII(vectorhex[8]) + "-" + Hex2ASCII(vectorhex[9]) + Hex2ASCII(vectorhex[10]) + "-" + Hex2ASCII(vectorhex[11]) + Hex2ASCII(vectorhex[12]) + " " + Hex2ASCII(vectorhex[13]) + Hex2ASCII(vectorhex[14]) + ":00:00.0";
                    //System.out.println("fechaTramaRecibida: " + fechaTramaRecibida + "\n" + "tspeticion: " + String.valueOf(tspeticion));
                }
//                                if(String.valueOf(tspeticion).equals(fechaTramaRecibida)){
                if (!errorMsg ? Timestamp.valueOf(fechaTramaRecibida).getTime() >= tspeticion.getTime() : true) {
                    tspeticion = tsrecepcion;
                    String fechapeticion = sdf.format(new Date(tspeticion.getTime()));
                    escribir("Fecha peticion inicial" + tspeticion);
                    //System.out.println("Fecha peticion inicial" + tspeticion);
                    byte[] data = tramasiskra.getPerfil();
                    if (npeticiones == 0) {
                        //se solicita el perfil sin fecha final
                        data = tramasiskra.getPerfilfinal();
                        data[9] = (byte) 0x30;
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);

                    } else {
                        //si es un perfil intermedio  se solicita en un rango de fechas
                        data[9] = (byte) 0x30;
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);

                        //tsrecepcion = new Timestamp(tspeticion.getTime() + 43200000);
                        tsrecepcion = new Timestamp(tspeticion.getTime() + blockDurationMillis); // blockDurationMillis vic 16-09-19 se cambio a peticiones de 15 minutos 900000
                        escribir("Fecha peticion final" + tsrecepcion);
                        //System.out.println("Fecha peticion final" + tsrecepcion);
                        String fechafin = sdf.format(new Date(tsrecepcion.getTime()));
                        data[21] = (byte) 0x30;
                        data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                        data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                        data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                        data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                        data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                        data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                        data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                        data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                        data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                        data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                    }
                    data[data.length - 1] = calcularBCC(data);
                    ultimatramaEnviada = data;
                    enviaTrama2_2(data, "=> Solicitud Perfil de carga " + fechapeticion);
                } else {
                    escribir("Intervalo recibido erroneo");
                    reintentosbadBCC++;
                    if (reintentosbadBCC < 4) {
                        enviaTrama2_2(ultimatramaEnviada, "=> Reenvio Solicitud de perfil de carga");
                    } else {
                        reintentosbadBCC = 0;
                        cerrarPuerto(true);
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Lectura incompleta");
                        almacenarRegistros();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Almacena perfil parcial");
                        escribir("Estado lectura No leido");
                        reintentoconexion++;
                        abrePuerto();
                    }
                }
            } else {
                //cuando sea -1 solicitamos los demas valores o cerramos el puerto.
                tiempo = timeout;
                perfilcarga = false;
                perfilincompleto = false;
                inst = new EInstantaneos(seriemedidor, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                if (factorEnPerfil) {
                    enviaFPot1();
                } else if (leventos) {
                    byte[] data = tramasiskra.getEventos();
                    npeticioneseventos = (int) (med.getNdiaseventos() / 5);
                    if (npeticioneseventos == 0) {
                        npeticioneseventos = 1;
                    }
                    //System.out.println("Numero de bloques a solicitar eventos" + npeticioneseventos);
                    long ndiaseventos = (tsfechaactual.getTime() - (86400000L * (long) med.getNdiaseventos()));
                    String fechapeticion = "";
                    tspeticioneventos = new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(ndiaseventos))).getTime());
                    //System.out.println("Fecha peticion inicial" + tspeticioneventos);
                    fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                    if (npeticioneseventos > 1) {
                        tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
                        //System.out.println("Fecha peticion final" + tsrecepcioneventos);
                        data[9] = (byte) 0x30;
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                        String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
                        data[21] = (byte) 0x30;
                        data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                        data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                        data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                        data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                        data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                        data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                        data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                        data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                        data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                        data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                    } else {
                        data = tramasiskra.getEventosfinal();
                        data[9] = (byte) 0x30;
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                    }
                    vEventos = null;
                    arrayeventos = new ArrayList<String[]>();
                    registroeventos = true;
                    data[data.length - 1] = calcularBCC(data);
                    enviaTrama2_2(data, "=> Solicitud de eventos " + fechapeticion);
                    ultimatramaEnviada = data;
                    enviaTrama2_2(data, "=> Solicitud Registro de Eventos");
                } else {
                    fechaactualtime = false;
                    cerrarPuerto(true);
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
                    escribir("Estado lectura Leido");
                    //System.out.println("Almacenando Datos");                    
                    almacenarRegistros();
                    med.MedLeido = true;
                    cerrarLog("Leido", true);
                    leer = false;
                }
            }
        }
    }

    private void enviaEventos() throws ParseException {
        byte[] data = tramasiskra.getEventos();
        npeticioneseventos = (int) (med.getNdiaseventos() / 5);
        if (npeticioneseventos == 0) {
            npeticioneseventos = 1;
        }
        //System.out.println("Numero de bloques a solicitar eventos" + npeticioneseventos);
        long ndiaseventos = (tsfechaactual.getTime() - (86400000L * (long) med.getNdiaseventos()));
        String fechapeticion = "";
        tspeticioneventos = new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(ndiaseventos))).getTime());

        //System.out.println("Fecha peticion inicial" + tspeticioneventos);
        fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
        if (npeticioneseventos > 1) {
            tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
            //System.out.println("Fecha peticion final" + tsrecepcioneventos);
            data[9] = (byte) 0x30;
            data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
            data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
            data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
            data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
            data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
            data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
            data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
            data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
            data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
            data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
            String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
            data[21] = (byte) 0x30;
            data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
            data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
            data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
            data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
            data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
            data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
            data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
            data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
            data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
            data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
        } else {
            data = tramasiskra.getEventosfinal();
            data[9] = (byte) 0x30;
            data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
            data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
            data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
            data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
            data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
            data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
            data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
            data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
            data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
            data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
        }

        vEventos = null;
        arrayeventos = new ArrayList<String[]>();
        registroeventos = true;
        data[data.length - 1] = calcularBCC(data);
        enviaTrama2_2(data, "=> Solicitud de eventos " + fechapeticion);
        ultimatramaEnviada = data;
        enviaTrama2_2(data, "=> Solicitud Registro de Eventos");
    }

    private void revisaEventos(String[] vectorhex) {
        String peticion = "Eventos";
        vectorhex = revisaTrama2(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            vEventos = new String[vectorhex.length - 3];
            System.arraycopy(vectorhex, 1, vEventos, 0, vEventos.length);
            npeticioneseventos--;
            arrayeventos.add(vEventos);
            if (npeticioneseventos > 0) {
                tspeticioneventos = tsrecepcioneventos;
                //System.out.println("Fecha peticion inicial" + tspeticioneventos);
                byte[] data = tramasiskra.getEventos();
                String fechapeticion = "";
                if (npeticioneseventos == 1) {
                    fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                    data = tramasiskra.getEventosfinal();
                    data[9] = (byte) 0x30;
                    data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                    data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                    data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                    data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                    data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                    data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                    data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                    data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                    data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                    data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                } else {

                    tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
                    //System.out.println("Fecha peticion final" + tsrecepcioneventos);
                    fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                    data[9] = (byte) 0x30;
                    data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                    data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                    data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                    data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                    data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                    data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                    data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                    data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                    data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                    data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                    String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
                    data[21] = (byte) 0x30;
                    data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                    data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                    data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                    data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                    data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                    data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                    data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                    data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                    data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                    data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                }
                data[data.length - 1] = calcularBCC(data);
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Solicitud de eventos " + fechapeticion);
            } else {
                registroeventos = false;
                cerrarPuerto(true);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
                almacenarRegistros();
                med.MedLeido = true;
                cerrarLog("Leido", false);
                leer = false;
            }
        }         
    }

    private void enviaSync() {
        byte data[] = tramasiskra.getSincreloj();
        syncReloj = true;
        try {
            time = obtenerHora();
            String fecha = sdf4.format(new Date(time.getTime()));
            data[10] = (byte) 0x30;
            data[11] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(0, 1)), 16) & 0xFF);
            data[12] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(1, 2)), 16) & 0xFF);
            data[13] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(2, 3)), 16) & 0xFF);
            data[14] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(3, 4)), 16) & 0xFF);
            data[15] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(4, 5)), 16) & 0xFF);
            data[16] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(5, 6)), 16) & 0xFF);
            data[17] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(6, 7)), 16) & 0xFF);
            data[18] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(7, 8)), 16) & 0xFF);
            data[19] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(8, 9)), 16) & 0xFF);
            data[20] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(9, 10)), 16) & 0xFF);
            data[21] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(10, 11)), 16) & 0xFF);
            data[22] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(11, 12)), 16) & 0xFF);

            data[data.length - 1] = calcularBCC(data);
            ultimatramaEnviada = data;
            cp.saveAcceso(med.getnSerie(), "2", sdfacceso.format(new Date(tsfechaactual.getTime())), sdfacceso.format(new Date(time.getTime())), usuario, null);
            enviaTrama2_2(data, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));
        } catch (Exception e) {
            Desconexion("Error obteniendo fecha y hora actual");
        }
    }

    private void revisaSyncClock(String cadenahex) {
        if (cadenahex.equals("06")) {
            cerrarPuerto(true);
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Sincronizacion OK");
            escribir("Estado Syncronizacion confirmado");
            med.MedLeido = true;
            cerrarLog("Sincronizado", false);
            leer = false;
        } else {
            Desconexion("Sincronizacion de reloj no confirmada");
        }
    }

    private String[] revisaTrama2(String[] vectorhex, String peticion) {
        if ((vectorhex.length <= 3) || (vectorhex.length == 1 && !vectorhex[0].equals("02"))) {
            //System.out.println("trama incompleta");
            reenvioTrama(peticion, "Error de protocolo");
        } else {
            boolean continuar = false;
            if (vectorhex[0].equals("15")) {
                //System.out.println("Recorta trama - byte 15");
                String[] auxv = new String[vectorhex.length - 1];
                System.arraycopy(vectorhex, 1, auxv, 0, auxv.length);
                vectorhex = auxv;
                cadenahex = cadenahex.substring(3, cadenahex.length());
            }
            if (vectorhex[0].equals("02")) {

                if (vectorhex[vectorhex.length - 2].equals("03")) {
//                    if(!(vectorhex[6].equals("45") && vectorhex[7].equals("52") && vectorhex[8].equals("52"))){
//                        continuar = true; 
//                    }
                    continuar = true;
                } else {
                    reenvioTrama(peticion, "Error de protocolo");
                }
                if (continuar) {//verifica inicio de trama y fin
                    if (validarBCC(vectorhex)) {
                        tramaOK = true;
                    } else {
                        reenvioTrama(peticion, "Error BCC");
                    }
                }// continuar      
            } else {
                reenvioTrama(peticion, "Error de protocolo");
            }
        }// un byte
        return vectorhex;
    }

    private void reenvioTrama(String peticion, String msjCausa) {
        reintentosbadBCC++;
        if (reintentosbadBCC < 4) {
            enviaTrama2_2(ultimatramaEnviada, "=> Reenvio Solicitud de " + peticion);
        } else {
            reintentosbadBCC = 0;
            Reinicio(msjCausa);
        }
    }

    private void reiniciaComunicacion() {
        try {
            cerrarPuerto(true);
            if (reintentoconexion <= numeroReintentos) {
                if (!objabortar.labortar) {
                    tReinicio = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                System.err.println(ex.getMessage());
                            }
                            reintentoconexion++;
                            abrePuerto();
                        }
                    };
                    tReinicio.start();
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion proceso abortado");
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado");
                cerrarLog("Desconexion Numero de reintentos agotado", false);
                leer = false;
            }
        } catch (Exception e) {
            escribir("Error en hilo de reinicio");
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Error al reiniciar la comunicación.");
            leer = false;
        }
    }

    private void Desconexion(String msjCausa) {
        escribir("Desconexión por " + msjCausa);
        cerrarPuerto(true);
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión por " + msjCausa);
        escribir("Estado lectura No leido");
        cerrarLog("Desconexión por " + msjCausa, false);
        leer = false;
    }

    private void Reinicio(String msjCausa) {
        escribir("Reinicio por " + msjCausa);
        reintentosbadBCC = 0;
        cerrarPuerto(true);
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion por " + msjCausa);
        escribir("Estado lectura No leido");
        reintentoconexion++;
        abrePuerto();
    }

    //metodo que interpreta las cadenas
    private void interpretaCadena(String cadenahex) throws InterruptedException {
        try {
            //System.out.println(cadenahex);
            escribir("Recibe <= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            //Estados de comunicacion
            if (presentacionIni) {   //presentacion
                revisaPresentacionIni(cadenahex, vectorhex);
            } else if (presentacion) {
                revisaPresentacion(cadenahex, vectorhex);
            } else if (comunicacion) {//modo de comunicacion
                revisaComunicacion(cadenahex, vectorhex);
            } else if (contraseña) { //password para lectura.
                revisaPass(cadenahex, vectorhex);
            } else if (serialmedidor) { //serial del medidor
                revisaSerialMedidor(vectorhex);
            } else if (fechaactualdate) {//fecha actual dia/mes/año
                revisaFechaActual(vectorhex);
            } else if (fechaactualtime) { //fecha actual hora/min
                revisaHora(vectorhex);
            } else if (perfilcarga) { //perfil de carga.
                revisaPerfil(vectorhex);
            } else if (registroeventos) {// registro de eventos
                revisaEventos(vectorhex);
            } else if (syncReloj) {//syncronizacion de reloj.
                revisaSyncClock(cadenahex);
            } else if (lVoltaje) {
                revisaVoltaje(vectorhex);//factor de potencia
            } else if (lCorriente) {
                revisaCorriente(vectorhex);//factor de potencia
            } else if (lFactPot) {
                revisaFPot(vectorhex);//factor de potencia
            } else {
                throw new Exception("Estado desconocido");
            }
        } catch (Exception e) {
            cerrarPuerto(true);
            escribir("Estado lectura No leido");
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Error al interpretar la respuesta.");
            cerrarLog("Error en la etapa de interpretación", false);
            leer = false;
        }
    }
    //metodo que escribe en el traifile.

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
            System.err.println(e.getMessage());
        }
    }
    //metodo que cierra el socket entras y salidas

    public void cerrarPuerto(boolean sendDisc) {
        if (sendDisc) {
            try {
                enviaTrama(tramasiskra.getDesconexion());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
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
                            if (enviando) {
                                if (intentosRetransmision != 0) {
                                    escribir("TimeOut, Intento de reenvio..");
                                }
                                escribir(des);
                                escribir("=> " + tramasiskra.encode(trama, trama.length));
                                try {
                                    monitor.notifyAll();
                                    enviaTrama(trama);
                                    monitor.wait();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    escribir("Error enviando trama en envia trama tipo 2_2");
                                }
                            } else {
                                t = false;
                            }
                            if (reenviando && intentosRetransmision <= 2) {
                                intentosRetransmision++;
                                enviando = true;
                            } else if (reenviando && intentosRetransmision > 2) {
                                interrumpirHilo(tLectura);
                                escribir("Numero de reenvios agotado");
                                enviando = false;
                                t = false;
                                cierrapuerto = true;
                            } else {
                                if (!cierrapuerto) {
                                    return;
                                } else {
                                    t = false;
                                }

                            }
                        }
                        if (cierrapuerto) {
                            cierrapuerto = false;
                            cerrarPuerto(true);
                            monitor.notifyAll();
                            if (perfilcarga || registroeventos) {
                                escribir("Busca datos para almacenar por contingencia");
                                almacenarRegistros();
                            }
                            if (reintentoconexion <= numeroReintentos) {
                                if (!objabortar.labortar) {
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion timeout");
                                    tReinicio = new Thread() {
                                        @Override
                                        public void run() {
                                            reintentoconexion++;
                                            abrePuerto();
                                        }
                                    };
                                    tReinicio.start();
                                } else {
                                    escribir("Proceso Abortado");
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Aborto de operacion");
                                    cerrarLog("Abortado", false);
                                    leer = false;
                                }
                            } else {
                                escribir("Estado Lectura No leido");
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de envios agotado");
                                cerrarLog("Desconexion Numero de reintentos agotado", false);
                                leer = false;
                            }
                        }
                    } catch (Exception e) {
                        cerrarPuerto(true);
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
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void iniciacomunicacion() throws InterruptedException, Exception {

        perfilcarga = false;
        if (portconect) {
            if (med.isLconfigurado()) {
                try {
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
                presentacionIni = true;
                byte[] data = tramasiskra.getIdentificacion2();
                data[2] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(0, 1)), 16) & 0xFF);
                data[3] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(1, 2)), 16) & 0xFF);
                data[4] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(2, 3)), 16) & 0xFF);
                data[5] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(3, 4)), 16) & 0xFF);
                data[6] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(4, 5)), 16) & 0xFF);
                data[7] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(5, 6)), 16) & 0xFF);
                data[8] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(6, 7)), 16) & 0xFF);
                data[9] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(7, 8)), 16) & 0xFF);
                enviaTrama2_2(data, "=> Identificación");
            } else {
                interrumpirHilo(tLectura);
                escribir("Medidor no configurado");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
                cerrarPuerto(true);                
                cerrarLog("Medidor no configurado", false);
                leer = false;
            }
        }
    }

    public byte[] removeElement(byte[] arr, int index) {
        byte[] arrDestination = new byte[arr.length - 1];
        int remainingElements = arr.length - (index + 1);
        System.arraycopy(arr, 0, arrDestination, 0, index);
        System.arraycopy(arr, index + 1, arrDestination, index, remainingElements);
        //System.out.println("Elements -- "  + Arrays.toString(arrDestination));
        return arrDestination;
    }

    public String Hex2ASCII(String dato) {
        String resultado = "";
        String temp1;
        int temp2;
        for (int k = 0; k < dato.length(); k = k + 2) {
            temp1 = "";
            temp1 = temp1 + dato.charAt(k) + dato.charAt(k + 1);
            temp2 = Integer.parseInt(temp1, 16);
            resultado = resultado + (char) temp2;
        }
        return resultado;
    }

    private boolean validarBCC(String data[]) {
        boolean exito = false;
        byte[] vdata = new byte[data.length];
        for (int i = 0; i < vdata.length; i++) {
            vdata[i] = (byte) (Integer.parseInt(data[i], 16) & 0xFF);
        }
        byte valor = calcularBCC(vdata);
        if (valor == vdata[data.length - 1]) {
            exito = true;
        }
        return exito;
    }

    private byte calcularBCC(byte data[]) {
        byte valor = 0x00;
        //hacemos una copia de la peticion quitando el h01 y el bcc de la trama de copia
        byte databcc[] = new byte[data.length - 2];

        byte resultado = 0x00;
        //obtenemos realizamos la copia de los valores
        for (int i = 0; i < databcc.length; i++) {
            databcc[i] = data[i + 1];
        }
        //realizamos los XOR de cada uno de los datos
        for (int i = 0; i < databcc.length - 1; i++) {
            if (i == 0) {
                resultado = (byte) (databcc[i] ^ databcc[i + 1]);
            } else {
                resultado = (byte) (resultado ^ databcc[i + 1]);
            }
        }
        //sumamos a nivel de bits con XOR cada uno de los bits hasta el 7 el valor de resultado
        byte valorfinal = (byte) 0x00;
        for (int i = 0; i < 8; i++) {
            if (i == 0) {
                valorfinal = (byte) (resultado & ((i + 1) & 0xFF));
            } else {
                valorfinal = (byte) (valorfinal & ((i + 1) & 0xFF));
            }
        }
        if (valorfinal == 0x00) {
            valor = (byte) (resultado | 0x00);
        } else {
            valor = (byte) (resultado | 0x80);
        }
        return valor;
    }

    public String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
    }

    public void terminaHilos() {
        try {
            tEscritura.interrupt();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            tReinicio.interrupt();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        tEscritura = null;
        tReinicio = null;

    }

    public void almacenarRegistros() {

        try {
            // escribir(String.valueOf(lperfil));
            if (lperfil) {//Almacena PerfilCarga
                escribir("Almacenando perfil de carga");
                Electura lec = null;
                Vector<Electura> arraylec = new Vector<Electura>();
                Timestamp fechaintervalo = null;
                Timestamp timeufec = null;
                ArrayList<String> canales = new ArrayList<>();
                //Vector<EConstanteKE> lconske = cp.buscarConstantesKe(med.getnSerie(), null);//se toman los valores de las constantes 
                //ArrayList<EtipoCanal> vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo(), null); //se toman los valores de los tipos de canales configurados
                int ncontadorintervalos = 0; //variable que realiza el control de los intervalos para obtener la fecha del intervalo apartir de la fecha base
                EConstanteKE econske = null;
                Timestamp ufec = null;
                Timestamp ultimointervalo = null;
                try {
                    ufec = cp.findUltimafechaLec(med.getnSerie());
                    ultimointervalo = ufec;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                if (ufec == null) {
                    ultimointervalo = tsfechaactual;
                }
                String idvar = "";//variable que controla la unidad de los canales
                ////System.out.println("Tamaño data perfil " + arrayperfil.size());
                //System.out.println("Size: "+arrayperfil.size());
                for (String vectorperfil[] : arrayperfil) {
                    int i = 0;
                    ////System.out.println("Tamaño de informacion " + vectorperfil.length);
                    ////System.out.println("Valor de bloque => " + Arrays.toString(vectorperfil));
                    while (i < vectorperfil.length) {
                        if ((vectorperfil[i] + vectorperfil[i + 1] + vectorperfil[i + 2] + vectorperfil[i + 3]).equals("502E3031")) {//es inicio de bloque.
                            ncontadorintervalos = 0;//reiniciamos el contador de intervalos para solo tener en cuenta los intervalos siguientes apartir de la fecha encontrada
                            ////System.out.println("Inicio de Bloque");
                            i += 5;
                            if ((vectorperfil[i] + vectorperfil[i + 1] + vectorperfil[i + 2] + vectorperfil[i + 3] + vectorperfil[i + 4]).equals("4552524F52")) {//no vienen datos error
                                i += 6;
                                ////System.out.println("Sin Informacion");
                            } else {
                                //fechaintervalo = null;
                                //formato fecha yyyMMddHHmm
                                try {
                                    //se toma el valor de la fecha  en el formato 
                                    long data = sdf.parse(Hex2ASCII(vectorperfil[i + 1]) + Hex2ASCII(vectorperfil[i + 2]) + Hex2ASCII(vectorperfil[i + 3]) + Hex2ASCII(vectorperfil[i + 4]) + Hex2ASCII(vectorperfil[i + 5]) + Hex2ASCII(vectorperfil[i + 6])
                                            + Hex2ASCII(vectorperfil[i + 7]) + Hex2ASCII(vectorperfil[i + 8]) + Hex2ASCII(vectorperfil[i + 9]) + Hex2ASCII(vectorperfil[i + 10])).getTime();
                                    fechaintervalo = new Timestamp(data); //se convierte el formato en timestamp no se toman los segundos                                    
                                    //System.out.println("Fecha Intervalo " + fechaintervalo);                                    
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                i += 19;
                                intervalo = Integer.parseInt(Hex2ASCII(vectorperfil[i]) + Hex2ASCII(vectorperfil[i + 1])); //se obtiene el valor de los intervalos 15,30,60
                                //se aproxima la fecha con el intervalo (periodo), si es necesario
                                if (fechaintervalo.getMinutes() % intervalo != 0) {
                                    String sfecha = sdf2.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
                                    fechaintervalo = new Timestamp(sdf.parse(sfecha).getTime());
                                    //System.out.println("Fecha intervalo aproximado" + fechaintervalo);
                                }
                                //es un intervalo de lectura
                                i += 4;
                                numcanales = Integer.parseInt(Hex2ASCII(vectorperfil[i])); //del bloque se revisa el numero de canales
                                i += 2;
                                canales = new ArrayList<>();
                                for (int n = 0; n < numcanales; n++) {
                                    //se hace un ciclo para idenfiticar los valores de los canales
                                    String canal = "";
                                    if (vectorperfil[i].equals("28")) {//inicia valor inicio de parentesis (
                                        ////System.out.println("Inicio Intervalo codigos");
                                        i++;
                                        while (!vectorperfil[i].equals("29")) { //concatena valores hasta encontrar un fin de dato parentesis )
                                            canal = canal + vectorperfil[i]; //concatena el valor
                                            i++;
                                        }
                                        ////System.out.println("canal " + Hex2ASCII(canal).replace(".", "0"));
                                        canales.add(Hex2ASCII(canal).replace(".", "0")); //el valor viene en hexa y se debe pasar a ascii y luego eliminar el punto y convertirlo en 0 para que se convierta en un canal telesimex
                                        i++;
                                    }
                                    //continua en el ciclo para obtener las unidades que se omiten por que solo se toman las de telesimex
                                    if (vectorperfil[i].equals("28")) {//inicia valor inicio parentesis (
                                        i++;
                                        while (!vectorperfil[i].equals("29")) { //si encontramos el valor final ) parentesis  nos saltamos estos valores
                                            i++;
                                        }
                                        i++;
                                    }
                                }
                                if ((vectorperfil[i] + vectorperfil[i + 1]).equals("0D0A")) {//buscamos el fin de linea
                                    i += 2;
                                    ////System.out.println("Fin Intervalo");
                                }
                                //verificamos si la fechaactual del intervalo supera a la ultima fecha de lectura en el doble si lo supera es por que existe un hueco en el perfil de carga que debe rellenarse
                                if (ultimointervalo != null && fechaintervalo != null) {
                                    ////System.out.println("fecha intervalo " + fechaintervalo);
                                    ////System.out.println("fecha ultimo intervalo " + ultimointervalo);
                                    ////System.out.println("Valor diferencia entre intervalos " + (fechaintervalo.getTime() - ultimointervalo.getTime()));
                                    if (fechaintervalo.getTime() - ultimointervalo.getTime() > (intervalo * 60000)) {
                                        ////System.out.println("existe hueco en el perfil");
                                        //calculamos el numero de intervalos que debemos rellenar                                        
                                        long intervaloscero = ((fechaintervalo.getTime() - ultimointervalo.getTime()) / (intervalo * 60000)) - 1;
                                        ////System.out.println("numero de intervalor en cero " + intervaloscero);
                                        for (int k = 1; k <= intervaloscero; k++) {
                                            Timestamp tscero = new Timestamp(ultimointervalo.getTime() + (intervalo * 60000 * k));
                                            //System.out.println("intervalo en 0 " + tscero);
                                            for (int n = 0; n < numcanales; n++) {
                                                String nlec = "0";
                                                econske = cp.buscarConske(lconske, Integer.parseInt(canales.get(n))); //buscamos el valor de la constante creada en telesimex
                                                if (econske != null) {
                                                    idvar = "";
                                                    //buscamos el idvar del canal que es el valor de la unidad para cada uno de los canales
                                                    for (EtipoCanal et : vtipocanal) {
                                                        if (Integer.parseInt(et.getCanal()) == Integer.parseInt(canales.get(n))) {
                                                            idvar = et.getUnidad();
                                                            break;
                                                        }
                                                    }
                                                    //si los 2 valores constante y idvar existen? se almacena la lecturas
                                                    if (idvar.length() > 0) {
                                                        lec = new Electura(new Timestamp(tscero.getTime()), med.getnSerie(), Integer.parseInt(canales.get(n)), (Double.valueOf(nlec) * econske.getMultiplo()), Double.valueOf(nlec), intervalo, idvar);
                                                        arraylec.add(lec);
                                                        //timeufec = new Timestamp((fechaintervalo.getTime() + (intervalo * 60000 * ncontadorintervalos)));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ultimointervalo = fechaintervalo;
                            }

                        } else {
                            //vienen datos   
                            ////System.out.println("Hay Datos de canales");
                            String nlec = "";
                            for (int n = 0; n < numcanales; n++) {
                                nlec = "";
                                if (vectorperfil[i].equals("28")) {//inicia valor lecturas
                                    ////System.out.println("Inicio Intervalo datos");
                                    i++;
                                    while (!vectorperfil[i].equals("29")) { //este ciclo se debe hacer hasta que encuentre un 29 o ) parentesis que es donde termina el dato                                       
                                        nlec = nlec + vectorperfil[i]; //concatena el valor hasta que termine
                                        i++;
                                    }
                                    nlec = Hex2ASCII(nlec); //el valor viene en hexa pero en realidad son ascii por lo que se hace la conversion
                                    i++;
                                    econske = cp.buscarConske(lconske, Integer.parseInt(canales.get(n))); //buscamos el valor de la constante creada en telesimex
                                    if (econske != null) {
                                        idvar = "";
                                        //buscamos el idvar del canal que es el valor de la unidad para cada uno de los canales
                                        for (EtipoCanal et : vtipocanal) {
                                            if (Integer.parseInt(et.getCanal()) == Integer.parseInt(canales.get(n))) {
                                                idvar = et.getUnidad();
                                                break;
                                            }
                                        }
                                        //si los 2 valores constante y idvar existen? se almacena la lecturas
                                        if (idvar.length() > 0) {
                                            lec = new Electura(new Timestamp((fechaintervalo.getTime() + (intervalo * 60000 * ncontadorintervalos))), med.getnSerie(), Integer.parseInt(canales.get(n)), (Double.valueOf(nlec) * econske.getMultiplo()), Double.valueOf(nlec), intervalo, idvar);
                                            arraylec.add(lec);
                                            timeufec = new Timestamp((fechaintervalo.getTime() + (intervalo * 60000 * ncontadorintervalos)));
                                            //System.out.println(timeufec);
                                            ultimointervalo = lec.getFecha();
                                        }
                                    }
                                } else {
                                    ////System.out.println("Error en almacenamiento");                                    
                                }
                            }
                            if ((vectorperfil[i] + vectorperfil[i + 1]).equals("0D0A")) {//buscamos el fin de linea
                                i += 2;
                            }
                            ncontadorintervalos++;
                        }
                    }
                }
                try {
                    cp.actualizaLectura(arraylec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                    if (timeufec != null) {
                        cp.actualizaFechaLectura(med.getnSerie(), sdf4.format(new Date(timeufec.getTime())));
                    }
                } catch (Exception e) {
                    escribir(getErrorString(e.getStackTrace(), 3));
                }
            }
            if (leventos) {//almacena Eventos
                escribir("Almacenando eventos");
                String tramaconexion = "";
                String tramadesconexion = "";
                boolean guardar = false;
                ERegistroEvento eregeven = null;
                for (String vectoreventos[] : arrayeventos) {
                    int i = 0;
                    ////System.out.println("Tamaño de informacion " + vectoreventos.length);
                    ////System.out.println("Valor de bloque => " + Arrays.toString(vectoreventos));
                    while (i < vectoreventos.length) {
                        //obtenemos el valor de la fecha                                              
                        if ((i + 4) > vectoreventos.length || (vectoreventos[i] + vectoreventos[i + 1] + vectoreventos[i + 2] + vectoreventos[i + 3] + vectoreventos[i + 4]).equals("4552524F52")) {
                            //viene error en el bloque
                            ////System.out.println("Error de bloque ");
                            i += 6;
                        } else if ((vectoreventos[i] + vectoreventos[i + 1] + vectoreventos[i + 2] + vectoreventos[i + 3]).equals("502E3938")) {//es inicio de bloque.
                            i += 5;
                        } else {
                            //obtenemos fecha del evento
                            String fecha = Hex2ASCII(vectoreventos[i + 2] + vectoreventos[i + 3] + vectoreventos[i + 4] + vectoreventos[i + 5] + vectoreventos[i + 6] + vectoreventos[i + 7] + vectoreventos[i + 8] + vectoreventos[i + 9] + vectoreventos[i + 10] + vectoreventos[i + 11] + vectoreventos[i + 12] + vectoreventos[i + 13]);
                            i += 16;
                            String codigo = Hex2ASCII(vectoreventos[i] + vectoreventos[i + 1] + vectoreventos[i + 2] + vectoreventos[i + 3]);
                            ////System.out.println("Fecha Evento " + fecha);
                            ////System.out.println("Codigo evento " + codigo);
                            if (codigo.equals("0080")) {
                                tramadesconexion = fecha;
                                tramaconexion = "";
                                ////System.out.println("Evento de corte");
////                                 System.out.println("Fecha corte " + fecha);
                            }
                            if (codigo.equals("0040")) {
                                tramaconexion = fecha;
////                                System.out.println("Evento de reconexion");
////                               System.out.println("Fecha reconexion " + fecha);
                            }
                            i += 5;
                            if ((vectoreventos[i] + vectoreventos[i + 1]).equals("0D0A")) {
                                i += 2;
                            }
                            if (tramaconexion.length() > 0 && tramadesconexion.length() > 0) {
                                guardar = true;
                            }
                            if (guardar) {
                                eregeven = new ERegistroEvento();
                                eregeven.setVcfechacorte(new Timestamp(sdf4.parse(tramadesconexion).getTime()));
                                eregeven.setVcfechareconexion(new Timestamp(sdf4.parse(tramaconexion).getTime()));
                                eregeven.setVcserie(med.getnSerie());
                                eregeven.setVctipo("0001");
                                cp.actualizaEvento(eregeven, null);
                                tramaconexion = "";
                                tramadesconexion = "";
                                guardar = false;
                            }
                        }
                    }
                    i = 0;
                }
            } else {
                //System.out.println("Datos encontrados almacenados");
            }
        } catch (Exception e) {
            e.printStackTrace();
            escribir("Error procesando datos");
        }
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

    public Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }

    public Date getDCurrentDate() throws ParseException {
        return Date.from(ZonedDateTime.now(zid).toInstant());
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
