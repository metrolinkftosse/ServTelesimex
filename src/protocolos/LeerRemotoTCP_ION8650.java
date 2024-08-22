/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemota_ION8650;
import Datos.TramasRemotalElsterA1800;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.ERegistroEvento;
import Entidades.Electura;
import Entidades.EtipoCanal;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgarcia
 */
public class LeerRemotoTCP_ION8650 {

    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();//fecha para incluir en el traifile
    String seriemedidor = "";//serial de medidor
    String fechaactual = "";//fecha actual del sistema
    Timestamp tsfechaactual; //timestamp fecha actual
    Timestamp deltatimesync1;//tiempo de para calculo de delay NTP
    Timestamp deltatimesync2; //tiempo de para calculo de delay NTP
    private long ndesfasepermitido = 0;//desfase tiempo NTP
    private long desfase;
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
    boolean ACK;
    boolean firstPacket;
    boolean tramaOK = false, delayed = false, uncompleteF = false;
    //variable que indica si lee
    public volatile boolean leer = true;//indicador de lectura realizada
    String numeroPuerto; //puerto de la IP
    int numeroReintentos = 4;
    private int intentosReenvioLastFrame = 0;
    int nreintentos = 0; //intento actual completo
    int velocidadPuerto; //velocidad del puerto
    private int packetNum = 0;
    long timeout;//timeout  para las peticiones
    private long pwdLong;
    private int user;
    private int nminutes;
    boolean portconect = false;//indicador de conexion al socket
    long tiemporetransmision = 0; //tiempo de espera para retransmison
    int actualReintento = 0; //reintento actual
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

    private String[] fragment1;
    private String[] fragment2;
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
    int numcanales = 4; //numero de canales
    int intervalo = 0; //intervalo de datos 15,30,60.
    private int dirfis;
    private int dirBroadcast = 65535;
    private int dirDefOrig = 21760;
    private int dirOrig;
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    Abortar objabortar;//variable que controla la opcion de abortar la comunicacion
    String usuario = "admin";
    int reinicio = 0;
    TramasRemota_ION8650 tramas = new TramasRemota_ION8650();//obejto con las tramas de envio
    //estados protocolo
    boolean inicioSesion = false;
    boolean comunicacion = false;
    boolean serialnumber;
    boolean listManager;
    boolean dataRecordManager;
    boolean activeDataRecordsModules;
    boolean activeConfInputs;
    boolean periodModule;
    boolean labelsDataRecordsModules;
    boolean calibrationPulseManager;
    boolean transformedRatios;
    boolean logPositionEvents;
    boolean logPositionCounterModules;
    boolean realTime;
    boolean timeZone;
    boolean loadProfile;
    boolean events;
    boolean logout = false;
    private boolean primerBloque;
    private boolean nextBlock = false;
    public boolean complemento = false;
    private boolean perfilIncompleto = false;
    private boolean dataManagerFound = false;
    private boolean calibrationPulseManagerFound = false;
    int resolucion = 1;
    int intercambio = 0;//valor que diferencia las solicitudes del perfil de carga segun explicacion del protocolo
    private byte[] sendNothing = {(byte) 0xFF};
    private int reqSeq = 0;
    private int gCounter = 0;
    private int idxDataRecordManager = 0;
    private int idxCalibrationPulseManager = 0;
    private int idxModuleRevenueLog;
    private int step = 0;
    private final Object monitor = new Object();
    private final String label = "LeerTCP_ION";

    private ZoneId zid;
    int reenvios = 0;
    private List<byte[]> listManagerL = new ArrayList();
    private List<byte[]> activeModulesL = new ArrayList();
    private List<String> activeModulesStr = new ArrayList();
    private List<byte[]> inputs = new ArrayList();
    private List<byte[]> answersPeriod = new ArrayList();
    private List<String> labelChannelsStr = new ArrayList();
    private List<byte[]> pulseManagerLogsL = new ArrayList();
    private List<String> pulseManagerLogsStr = new ArrayList();
    private List<byte[]> pulseManagerLogSubRegisters = new ArrayList();
    private List<Double> ktValuesL = new ArrayList();
    private List<Double> transformedRatiosL = new ArrayList();
    private List<byte[]> positionAndDepthRecords = new ArrayList();
    private List<Integer> positionAndDepthValues = new ArrayList();
    private List<Integer> positionValueEvent = new ArrayList();
    private String[] partialDataArr;
    private String[] partialEventArr;
    private List<String> temporalDataList = new ArrayList();
    private List<ERegistroEvento> temporalEventList = new ArrayList();
    private int nCanales = 4;
    private int intPeriod = 15;
    private int nRegistros = 1;
    private int lenBytesReg = 2;
    private int idxListManager = 0;
    private String hexDate;
    private int offSetTZ;
    private int lastReg;
    private int lastRegEvent;
    private int nBytes = 0;
    private Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    private ArrayList<EtipoCanal> vtipocanal;
    byte[] tramaBase;
    List<String[]> frames = new ArrayList<>();
    private ArrayList<Object[]> profileElementsV = new ArrayList<>();
    private static final int[] tableCRC16 = {0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241,
        0xc601, 0x06c0, 0x0780, 0xc741, 0x0500, 0xc5c1, 0xc481, 0x0440,
        0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40,
        0x0a00, 0xcac1, 0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841,
        0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00, 0xdbc1, 0xda81, 0x1a40,
        0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41,
        0x1400, 0xd4c1, 0xd581, 0x1540, 0xd701, 0x17c0, 0x1680, 0xd641,
        0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081, 0x1040,
        0xf001, 0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240,
        0x3600, 0xf6c1, 0xf781, 0x3740, 0xf501, 0x35c0, 0x3480, 0xf441,
        0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41,
        0xfa01, 0x3ac0, 0x3b80, 0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840,
        0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0, 0x2a80, 0xea41,
        0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40,
        0xe401, 0x24c0, 0x2580, 0xe541, 0x2700, 0xe7c1, 0xe681, 0x2640,
        0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041,
        0xa001, 0x60c0, 0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240,
        0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501, 0x65c0, 0x6480, 0xa441,
        0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41,
        0xaa01, 0x6ac0, 0x6b80, 0xab41, 0x6900, 0xa9c1, 0xa881, 0x6840,
        0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80, 0xba41,
        0xbe01, 0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40,
        0xb401, 0x74c0, 0x7580, 0xb541, 0x7700, 0xb7c1, 0xb681, 0x7640,
        0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041,
        0x5000, 0x90c1, 0x9181, 0x5140, 0x9301, 0x53c0, 0x5280, 0x9241,
        0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1, 0x9481, 0x5440,
        0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40,
        0x5a00, 0x9ac1, 0x9b81, 0x5b40, 0x9901, 0x59c0, 0x5880, 0x9841,
        0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40,
        0x4e00, 0x8ec1, 0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41,
        0x4400, 0x84c1, 0x8581, 0x4540, 0x8701, 0x47c0, 0x4680, 0x8641,
        0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040};

    //constructor para la recepcion de datos desde la interfaz de lectura
    public LeerRemotoTCP_ION8650(EMedidor med, boolean perfil, boolean eventos, boolean registros, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) {

        this.med = med;
        this.cp = cp;
        this.usuario = usuario;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        try {
            File f = new File(cp.rutalogs + sdfarchivo.format(new Date()) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
            if (!f.exists()) {
                f.mkdirs();
            }
            file = new File(cp.rutalogs + sdfarchivo.format(new Date()) + "/" + this.med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        jinit();
    }

    private void jinit() {
        escribir("Equipo llamador: " + cp.obtenerMac());
        escribir("Telesimex version: " + cp.getVersion());
        escribir("Window Id: " + cp.getWindowId());
        try {
            tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
            //configuracion de medidor
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            reenvios = numeroReintentos;
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = lconfHora ? med.getPassword2() : med.getPassword();
            password = password == null ? "" : password;
            if (!password.isEmpty()) {
                pwdLong = Long.parseLong(password);
            }
            timeout = med.getTimeout() * 1000;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica().equals("0") ? "" + dirBroadcast : med.getDireccionFisica()));
            dirOrig = Integer.parseInt((med.getDireccionLogica().equals("0") ? "" + dirDefOrig : med.getDireccionLogica()));
            user = Integer.parseInt((med.getDireccionCliente() == null ? "1" : med.getDireccionCliente()));
            lconske = cp.buscarConstantesKe(med.getnSerie());//se toman los valores de las constantes 
            tramaBase = password.isEmpty() ? tramas.getHeader() : tramas.getHeaderPwd();
            lconske.forEach((consKe) -> {
                escribir("" + consKe.getCanal());
            });
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            vtipocanal.forEach((canal) -> {
                escribir("" + canal.getUnidad());
            });
            abrePuerto();
        } catch (Exception e) {
            leer = false;
            escribir(getErrorString(e.getStackTrace(), 3));        
        }
    }

    private void abrePuerto() {
        tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
        try {
            if (!objabortar.labortar) {
                if (reintentoconexion < numeroReintentos) { //3) { //parametrizable con reintentos de conf_medidor                    
                    socket = new Socket(med.getDireccionip(), Integer.valueOf(med.getPuertoip()));
                    med.setFecha(cp.findUltimafechaLec(med.getnSerie()));

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
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Numero de intentos de conexion agotado");
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

    //metodo igual a iniciacomunicacion1 solo que cambia de hilo iniciador
    private void iniciacomunicacion() throws Exception {
        loadProfile = false;
        //tiemporetransmision = 15000 + tiempo;
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
                inicioSesion = true;
                reqSeq++;
                System.out.println("Envia Solicitud Inicio Session");
                //se envia la primera trama de comuniccion
                byte[] data = concatFrames(asignaDireccion(tramaBase), tramas.getContactingDevice());
                setHeaderParams(data);
                data = calculaCRC16(data);
                System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                enviaTrama2_2(data, "=> Solicitud de inicio de session");
            }
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
                        escribir(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void procesaCadena() {

        byte[] readBuffer = new byte[2047];
        byte[] auxBuffer = new byte[2047];
        int idxFrame = 0;
        int numbytes;
        byte[] begin = {0, 0};
        boolean uncomplete = true;
        boolean beginOk = false;
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
                    if (realTime) {
                        deltatimesync1 = obtenerHora();
                    }
                    endT = System.currentTimeMillis() + timeout;
                    numbytes = input.read(readBuffer);
                    System.out.println("Arriving: " + tramas.encode(readBuffer, numbytes));
                    if (idxFrame == 0) {
                        if (numbytes == 1) {
                            begin[0] = readBuffer[0];
                        } else {
                            begin[0] = readBuffer[0];
                            begin[1] = readBuffer[1];
                            beginOk = (begin[0] == (byte) 0xBB && begin[1] == (byte) 0xBB);
                        }
                        System.out.println(" " + Arrays.toString(begin));
                    }
                    if (numbytes >= 6 && idxFrame == 0) {
                        if (begin[0] == (byte) 0xBB && begin[1] == (byte) 0xBB) {
                            int actualFrameLength = (int) (readBuffer[5] & 0xFF);
                            System.out.println("Lenght: " + actualFrameLength);
                            frameLengthOk = actualFrameLength == numbytes - 7;
                        }
                    }
                    System.out.println("begin: " + beginOk + " frameLengthOk: " + frameLengthOk);
                    if (!beginOk || !frameLengthOk) {
                        System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                        idxFrame += numbytes;
                        System.out.println("Buffer Auxiliar: " + tramas.encode(auxBuffer, idxFrame));
                        if (idxFrame >= 6) {
                            if (begin[0] == (byte) 0xBB && begin[1] == (byte) 0xBB) {
                                int actualFrameLength = (int) (auxBuffer[5] & 0xFF);
                                System.out.println("Lenght: " + actualFrameLength);
                                frameLengthOk = actualFrameLength == idxFrame - 7;
                                System.out.println("frameLengthOk: " + frameLengthOk);
                            }
                            if (frameLengthOk) {
                                System.out.println("Salir del bucle de escucha forma 2.");
                                uncomplete = false;
                                enviando = false;
                                reenviando = false;
                                monitor.notifyAll();
                                break;
                            }
                        }
                    } else if (idxFrame == 0) {
                        System.out.println("Salir del bucle de escucha forma 1.");
                        auxBuffer = readBuffer;
                        idxFrame = numbytes;
                        uncomplete = false;
                        enviando = false;
                        reenviando = false;
                        monitor.notifyAll();
                    } else {
                        System.out.println("Salir del bucle de escucha forma 2.");
                        uncomplete = false;
                        enviando = false;
                        reenviando = false;
                        monitor.notifyAll();
                    }
                }
                if (!socket.isClosed() && uncomplete) {
                    System.out.println("Se vencío el timeout de respuesta sin recibir nada");
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
    private void revisaTrama(String[] vectorhex) {
        tramaOK = false;
        uncompleteF = false;
        delayed = false;
        int fixedLen = 7;
        int indxPktLen = 5;
        int indxReqSeq = 14;
        boolean validaCRC;
        if ((vectorhex[0].equalsIgnoreCase("BB") && vectorhex[1].equalsIgnoreCase("BB"))) { // se verifica si llega el BBBB
            if (vectorhex.length >= 16) {//posee cabecera?
                if ((vectorhex.length - fixedLen) == (Integer.parseInt((vectorhex[indxPktLen]), 16))) { //validamos campo Packet Len                    
                    if (nextBlock || Integer.parseInt(vectorhex[indxReqSeq] + vectorhex[indxReqSeq + 1], 16) == reqSeq) { //Validamos Secuencias de Peticiones y Respuestas
                        validaCRC = (validaCRC16(vectorhex));
                        if (validaCRC) {//validamos CRC
                            tramaOK = true;
                            frames.add(vectorhex);
                        } else {// CRC
                            escribir("BAD CRC");
                            frames.add(vectorhex);
                        }
                    } else {
                        tramaOK = true;
                        delayed = true;
                        escribir("Wrong Sequence Number");
                        frames.add(vectorhex);
                    }
                } else {// LEN                       
                    tramaOK = true;
                    escribir("Error longitud de trama");
                    // Indica si la trama que llego es más corta que el length definitido en la cabecera de la trama por tanto incompleta.
                    boolean shorter = (vectorhex.length - fixedLen) < (Integer.parseInt((vectorhex[indxPktLen]), 16));
                    if (shorter) {
                        escribir("Trama incompleta");
                        fragment1 = vectorhex;
                        uncompleteF = true;
                        frames.add(vectorhex);
                    } else {
                        escribir("Más de una trama");
                        List<Integer> idxList = idxSplitArray(vectorhex);
                        escribir("Posiciones de posibles tramas: " + idxList);
                        frames = getFrames(vectorhex, idxList);
                    }
                }
            } else {
                tramaOK = true;
                escribir("Error trama sin cabecera");
                fragment1 = vectorhex;
                uncompleteF = true;
                frames.add(vectorhex);
            }
        } else {
            escribir("Error trama inicio");
            frames.add(vectorhex);
        }
    }

    private void revisaTramaIndividual(String[] vectorhex) {
        tramaOK = false;
        int fixedLen = 7;
        int indxPktLen = 5;
        int indxReqSeq = 14;
        boolean validaCRC;

        if ((vectorhex[0].equalsIgnoreCase("BB") && vectorhex[1].equalsIgnoreCase("BB"))) { // se verifica si llega el BBBB
            if (vectorhex.length >= 16) {//posee cabecera?
                if ((vectorhex.length - fixedLen) == (Integer.parseInt((vectorhex[indxPktLen]), 16))) { //validamos campo Packet Len
                    if (Integer.parseInt(vectorhex[indxReqSeq] + vectorhex[indxReqSeq + 1], 16) == reqSeq) { //Validamos Secuencias de Peticiones y Respuestas
                        validaCRC = (validaCRC16(vectorhex));
                        if (validaCRC) {//validamos CRC
                            tramaOK = true;
                        } else {// CRC
                            escribir("BAD CRC");
                        }
                    } else {
                        escribir("Wrong Sequence Number");
                    }
                } else {
                    escribir("Error longitud de trama");
                }
            } else {
                escribir("Error trama sin cabecera");
            }
        } else {
            escribir("Error trama inicio");
        }
    }

    private void revisarInicioSession(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                dirfis = Integer.parseInt(vectorhex[7] + vectorhex[6], 16);
                String[] answerASCII = Arrays.copyOfRange(vectorhex, idxData, vectorhex.length - 3);
                String word = "";
                for (String str : answerASCII) {
                    char asciiChar = (char) Integer.parseInt(str, 16);
                    word += asciiChar;
                }
                if (!word.contains("Invalid Password")) {
                    enviaSerial();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Contraseña Errónea");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión Contraseña Errónea");                        
                        escribir("Estado Lectura No leido");
                        cerrarLog("Desconexión Contraseña Errónea", false);
                        cerrarPuerto();
                        leer = false;
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaSerial() {
        inicioSesion = false;
        serialnumber = true;
        reqSeq++;
        byte[] data = concatFrames(asignaDireccion(tramaBase), tramas.getSerial());
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud de Número Serial");
    }

    private void revisarSerialNumber(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                String[] answerASCII = Arrays.copyOfRange(vectorhex, idxData + 5, vectorhex.length - 3);
                String word = "";
                for (String str : answerASCII) {
                    char asciiChar = (char) Integer.parseInt(str, 16);
                    word += asciiChar;
                }
                escribir("Serail obtenido: " + word);
                word = word.replace("-", "");
                if (word.equalsIgnoreCase(seriemedidor)) {
                    escribir("Serial Correcto: " + word);
                    enviaListManager();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Numero de serial incorrecto");                        
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Leer_ION8650", "Desconexion Error de serial");
                        escribir("Estado lectura No leido");                        
                        cerrarLog("Desconexion Error de serial", false);
                        cerrarPuerto();
                        leer = false;
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaListManager() {
        serialnumber = false;
        listManager = true;
        reqSeq++;
        byte[] data = concatFrames(asignaDireccion(tramaBase), concatFrames(tramas.getListManager(), tramas.getLogs()));
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud Administrador de Listas");
    }

    private void revisarListManager(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                byte[] featureManager;
                for (int i = idxData + 5; i < vectorhex.length - 2; i += 2) {
                    featureManager = new byte[]{(byte) Integer.parseInt(vectorhex[i], 16), (byte) Integer.parseInt(vectorhex[i + 1], 16)};
                    escribir("Feature Manager: " + tramas.encode(featureManager, featureManager.length));
                    listManagerL.add(featureManager);
                }
                enviaDataRecordManager();
                break;
                /*
                if (word.equalsIgnoreCase(seriemedidor)) {
                    escribir("Serial Correcto: " + word);
                    enviaListManager();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Numero de serial incorrecto");
                        cerrarPuerto();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Leer_ION8650", "Desconexion Error de serial");
                        escribir("Estado lectura No leido");                        
                        cerrarLog("Desconexion Error de serial", false);
                        leer = false;
                        break;
                    } else {
                        frameCounter++;
                    }
                }
                 */
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaDataRecordManager() {
        listManager = false;
        dataRecordManager = true;
        reqSeq++;
        byte[] reqType = tramas.getIdRecord();
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        byte[] featureManager = new byte[2];
        for (int i = gCounter; i < listManagerL.size(); i++) {
            if (payloadReq.size() < 206) {
                featureManager = listManagerL.get(i);
                payloadReq.add(featureManager[0]);
                payloadReq.add(featureManager[1]);
                payloadReq.add(reqType[0]);
                payloadReq.add(reqType[1]);
                payloadReq.add(reqType[2]);
            } else {
                idxListManager += i;
                break;
            }
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud Administrador de Registro de Datos");
    }

    private void revisarDataRecordManager(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                int idDataRecordManager = 537;
                int calibrationPulseManagerI = 574;
                int currIdRecord;
                for (int i = idxData + 2; i < vectorhex.length - 2; i += 3) {
                    currIdRecord = Integer.parseInt(vectorhex[i] + vectorhex[i + 1], 16) & 0xFFFF;
                    //escribir("Current Record: " + currIdRecord);
                    if (currIdRecord == idDataRecordManager) {
                        dataManagerFound = true;
                        idxDataRecordManager = gCounter;
                    }
                    if (currIdRecord == calibrationPulseManagerI) {
                        calibrationPulseManagerFound = true;
                        idxCalibrationPulseManager = gCounter;
                    }
                    if (dataManagerFound && calibrationPulseManagerFound) {
                        break;
                    }
                    gCounter++;
                }
                if (dataManagerFound && calibrationPulseManagerFound) {
                    escribir("Data Recorder Manager: " + tramas.encode(listManagerL.get(idxDataRecordManager), 2));
                    escribir("Calibration Pulse Manager: " + tramas.encode(listManagerL.get(idxCalibrationPulseManager), 2));
                    enviaActiveDataRecordsModules();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        if (idxListManager < listManagerL.size()) {
                            enviaDataRecordManager();
                        } else {
                            escribir("Data Record Manager no encontrado");                            
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Leer_ION8650", "Desconexion Error data Record Manager no encontrado");
                            escribir("Estado lectura No leido");                            
                            cerrarLog("Desconexion Error data Record Manager no encontrado", false);
                            cerrarPuerto();
                            leer = false;
                        }
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaActiveDataRecordsModules() {
        dataRecordManager = false;
        activeDataRecordsModules = true;
        reqSeq++;
        byte[] iniReq = {(byte) 0xF6};
        byte[] data = concatFrames(asignaDireccion(tramaBase), concatFrames(concatFrames(iniReq, listManagerL.get(idxDataRecordManager)), tramas.getLogs()));
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud Modulos Actualmente Activos");
    }

    private void revisarActiveDataRecordsModules(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                if (vectorhex[idxData + 1].equalsIgnoreCase("FE")) {
                    byte[] activeModule;
                    int regIdx = idxData + 2;
                    while (regIdx < vectorhex.length - 3) {
                        int len1 = Integer.parseInt( vectorhex[regIdx], 16 ) & 0x0F;
                        int len2 = Integer.parseInt( vectorhex[regIdx], 16 ) & 0xFF; 
                        int expLen = vectorhex.length - regIdx - 3;
                        if ( len1 == expLen || len2 == expLen ) {
                            break;
                        } else {
                            regIdx ++;
                        }
                    }
                    for (int i = regIdx + 1; i < vectorhex.length - 2; i += 2) {
                        activeModule = new byte[]{(byte) Integer.parseInt(vectorhex[i], 16), (byte) Integer.parseInt(vectorhex[i + 1], 16)};
                        escribir("Active Module: " + tramas.encode(activeModule, activeModule.length));
                        activeModulesL.add(activeModule);
                    }
                    enviaActiveModulesDescription();
                    break;
                } else if (vectorhex[idxData + 1].equalsIgnoreCase("FA")) {
                    String[] answerASCII = Arrays.copyOfRange(vectorhex, idxData + 1, vectorhex.length - 2);
                    String word = "";
                    for (String str : answerASCII) {
                        word += str;
                    }
                    String[] modulesNames = word.split("FA");
                    modulesNames = Arrays.copyOfRange(modulesNames, 1, modulesNames.length);
                    for (String name : modulesNames) {
                        String wordASCII = "";
                        int countLen = 2;
                        boolean save = false;
                        for (int c = 0; c < name.length(); c += 2) {
                            if (save) {
                                if (Integer.parseInt(name.substring(c, c + 2), 16) == 0) {//Criterio de parada    
                                    escribir("Modulo Activo: " + wordASCII);
                                    activeModulesStr.add(wordASCII);
                                } else {
                                    wordASCII += (char) Integer.parseInt(name.substring(c, c + 2), 16);
                                }
                            } else if ( (Integer.parseInt(name.substring(c, c + 2), 16) & 0x0F) == (name.length() - countLen) / 2 || (Integer.parseInt(name.substring(c, c + 2), 16) & 0xFF) == (name.length() - countLen) / 2 ) {
                                save = true;
                            }
                            countLen += 2;
                        }
                    }
                    enviaActiveConfInputs();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Respuesta No Esperada");
                        handleNACK();
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaActiveModulesDescription() {
        reqSeq++;
        byte[] reqDescription = {tramas.getDescription()};
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        byte[] activeModule = new byte[2];
        for (int i = 0; i < activeModulesL.size(); i++) {
            activeModule = activeModulesL.get(i);
            payloadReq.add(activeModule[0]);
            payloadReq.add(activeModule[1]);
            payloadReq.add(reqDescription[0]);
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud Descripcion de Modulos Actualmente Activos");
    }

    public void enviaActiveConfInputs() {

        activeDataRecordsModules = false;
        activeConfInputs = true;
        reqSeq++;
        int idx = 0;
        byte[] reqInputs = tramas.getInputs();
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        byte[] activeModule = new byte[2];
        for (String name : activeModulesStr) {
            if (name.equalsIgnoreCase("Revenue Log")) {
                idxModuleRevenueLog = idx;
                activeModule = activeModulesL.get(idx);
                payloadReq.add(activeModule[0]);
                payloadReq.add(activeModule[1]);
                payloadReq.add(reqInputs[0]);
                payloadReq.add(reqInputs[1]);
                payloadReq.add(reqInputs[2]);
                break;
            }
            idx++;
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud  Configuración Activa de las entradas");
    }

    private void revisarActiveConfInputs(String[] vectorhexO) throws ParseException {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                byte[] currInput;
                for (int i = idxData + 5; i < vectorhex.length - 2; i += 2) {
                    currInput = new byte[]{(byte) Integer.parseInt(vectorhex[i], 16), (byte) Integer.parseInt(vectorhex[i + 1], 16)};
                    escribir("Input: " + (i - idxData - 4) + " tiene la dirección: $" + tramas.encode(currInput, currInput.length));
                    inputs.add(currInput);
                }
                step++;
                enviaPeriodModule(step);
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaPeriodModule(int stepL) {
        activeConfInputs = false;
        periodModule = true;
        reqSeq++;
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        byte[] reqRecordInput = null;
        byte[] inputAddr = new byte[2];
        switch (stepL) {
            case 1:
                reqRecordInput = tramas.getRecordOfInput();
                inputAddr = inputs.get(17);
                break;
            case 2:
                reqRecordInput = tramas.getLogs();
                inputAddr = answersPeriod.get(0);
                break;
            case 3:
                reqRecordInput = new byte[]{tramas.getValueLogs()};
                inputAddr = answersPeriod.get(1);
                break;
        }
        payloadReq.add(inputAddr[0]);
        payloadReq.add(inputAddr[1]);
        for (byte b : reqRecordInput) {
            payloadReq.add(b);
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud Periodo de Integracion, paso : " + step);
    }

    private void revisarPeriodModule(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                switch (step) {
                    case 1:
                        byte[] ans = {(byte) Integer.parseInt(vectorhex[idxData + 2], 16), (byte) Integer.parseInt(vectorhex[idxData + 3], 16)};
                        answersPeriod.add(ans);
                        step++;
                        enviaPeriodModule(step);
                        break;
                    case 2:
                        byte[] ans2 = {(byte) Integer.parseInt(vectorhex[idxData + 3], 16), (byte) Integer.parseInt(vectorhex[idxData + 4], 16)};
                        answersPeriod.add(ans2);
                        step++;
                        enviaPeriodModule(step);
                        break;
                    case 3:
                        intPeriod = (int) ieeeToFloat(vectorhex[idxData + 2] + vectorhex[idxData + 3] + vectorhex[idxData + 4] + vectorhex[idxData + 5]) / 60;
                        escribir("Periodo de Integración (min): " + intPeriod);
                        step = 0;
                        enviaLabelsDataRecordsModules();
                        break;
                }
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaLabelsDataRecordsModules() {
        periodModule = false;
        labelsDataRecordsModules = true;
        reqSeq++;
        byte[] reqDescription = {tramas.getDescription()};
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        int channCounter = 0;
        for (byte[] inputL : inputs) {
            if (inputL[0] != 0 || inputL[1] != 0) {
                payloadReq.add(inputL[0]);
                payloadReq.add(inputL[1]);
                payloadReq.add(reqDescription[0]);
                channCounter++;
            } else {
                break;
            }
        }
        nCanales = channCounter;
        escribir("Número de Canales: " + nCanales);
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud Etiquetas canales");
    }

    private void revisarLabelsDataRecordModules(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                if (vectorhex[idxData + 1].equalsIgnoreCase("FA")) {
                    String[] answerASCII = Arrays.copyOfRange(vectorhex, idxData + 1, vectorhex.length - 2);
                    String word = "";
                    for (String str : answerASCII) {
                        word += str;
                    }
                    String[] channels = word.split("FA");
                    channels = Arrays.copyOfRange(channels, 1, channels.length);
                    int channelCounter = 0;
                    for (String channel : channels) {
                        channelCounter++;
                        String wordASCII = "";
                        int countLen = 2;
                        boolean save = false;
                        for (int c = 0; c < channel.length(); c += 2) {
                            if (save) {
                                if (Integer.parseInt(channel.substring(c, c + 2), 16) == 0) {//Criterio de parada 
                                    escribir("Canal " + channelCounter + " : " + wordASCII);
                                    labelChannelsStr.add(wordASCII);
                                    if (!unitSelector(wordASCII).equalsIgnoreCase("No Unit")) {
                                        Object[] data = new Object[2];
                                        data[0] = channelCounter;
                                        data[1] = unitSelector(wordASCII);
                                        profileElementsV.add(data);
                                    }
                                } else {
                                    wordASCII += (char) Integer.parseInt(channel.substring(c, c + 2), 16);
                                }
                            } else if ( (Integer.parseInt(channel.substring(c, c + 2), 16) & 0x0F) == (channel.length() - countLen) / 2 || (Integer.parseInt(channel.substring(c, c + 2), 16) & 0xFF) == (channel.length() - countLen) / 2) {
                                save = true;
                            }
                            countLen += 2;
                        }
                    }
                    step++;
                    enviaCalibrationPulseManager(step);
                    break;
                }
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaCalibrationPulseManager(int stepL) {
        labelsDataRecordsModules = false;
        calibrationPulseManager = true;
        reqSeq++;
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        switch (stepL) {
            case 1:
                payloadReq.add(listManagerL.get(idxCalibrationPulseManager)[0]);
                payloadReq.add(listManagerL.get(idxCalibrationPulseManager)[1]);
                payloadReq.add(tramas.getLogs()[0]);
                payloadReq.add(tramas.getLogs()[1]);
                payloadReq.add(tramas.getLogs()[2]);
                break;
            case 2:
                for (byte[] log : pulseManagerLogsL) {
                    payloadReq.add(log[0]);
                    payloadReq.add(log[1]);
                    payloadReq.add(tramas.getDescription());
                }
                break;
            case 3:
                for (byte[] log : pulseManagerLogsL) {
                    payloadReq.add(log[0]);
                    payloadReq.add(log[1]);
                    payloadReq.add(tramas.getLogs()[0]);
                    payloadReq.add(tramas.getLogs()[1]);
                    payloadReq.add(tramas.getLogs()[2]);
                }
                break;
            case 4:
                for (byte[] subR : pulseManagerLogSubRegisters) {
                    payloadReq.add(subR[0]);
                    payloadReq.add(subR[1]);
                    payloadReq.add(tramas.getValueLogs());
                }
                break;
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Obtener valores Kt de los administradores de generadores de impulsos de calibración, Paso : " + step);
    }

    private void revisarCalibrationPulseManager(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                String word = "";
                switch (step) {
                    case 1:
                        if (vectorhex[idxData + 1].equalsIgnoreCase("FE")) {
                            byte[] pulseManagerLogs;
                            int regIdx = idxData + 2;
                            while (regIdx < vectorhex.length - 3) {
                                int len1 = Integer.parseInt(vectorhex[regIdx], 16) & 0x0F;
                                int len2 = Integer.parseInt(vectorhex[regIdx], 16) & 0xFF;
                                int expLen = vectorhex.length - regIdx - 3;
                                if (len1 == expLen || len2 == expLen) {
                                    break;
                                } else {
                                    regIdx++;
                                }
                            }                            
                            for (int i = regIdx + 1; i < vectorhex.length - 2; i += 2) {
                                pulseManagerLogs = new byte[]{(byte) Integer.parseInt(vectorhex[i], 16), (byte) Integer.parseInt(vectorhex[i + 1], 16)};
                                escribir("Registro de gestores de pulsadores: " + tramas.encode(pulseManagerLogs, pulseManagerLogs.length));
                                pulseManagerLogsL.add(pulseManagerLogs);
                            }                        
                            step++;
                            enviaCalibrationPulseManager(step);
                            break;
                        }
                    case 2:
                        String[] answerASCII = Arrays.copyOfRange(vectorhex, idxData + 1, vectorhex.length - 2);
                        word = "";
                        for (String str : answerASCII) {
                            word += str;
                        }
                        String[] pulseMgrsDesc = word.split("FA");
                        pulseMgrsDesc = Arrays.copyOfRange(pulseMgrsDesc, 1, pulseMgrsDesc.length);
                        int pulseMgrCounter = 0;
                        for (String pulseMgr : pulseMgrsDesc) {
                            pulseMgrCounter++;
                            String wordASCII = "";
                            int countLen = 2;
                            boolean save = false;
                            for (int c = 0; c < pulseMgr.length(); c += 2) {
                                if (save) {
                                    if (Integer.parseInt(pulseMgr.substring(c, c + 2), 16) == 0) {//Criterio de parada 
                                        escribir("Registro Gestor de Pulso " + pulseMgrCounter + " : " + wordASCII);
                                        pulseManagerLogsStr.add(wordASCII);
                                    } else {
                                        wordASCII += (char) Integer.parseInt(pulseMgr.substring(c, c + 2), 16);
                                    }
                                } else if ( (Integer.parseInt(pulseMgr.substring(c, c + 2), 16) & 0x0F) == (pulseMgr.length() - countLen) / 2 || (Integer.parseInt(pulseMgr.substring(c, c + 2), 16) & 0xFF) == (pulseMgr.length() - countLen) / 2) {
                                    save = true;
                                }
                                countLen += 2;
                            }
                        }
                        step++;
                        enviaCalibrationPulseManager(step);
                        break;
                    case 3:
                        String[] subRegisters = Arrays.copyOfRange(vectorhex, idxData + 1, vectorhex.length - 2);
                        for (String subReg : subRegisters) {
                            word += subReg;
                        }
                        String[] pulseMgrsSubR = word.split("FE");
                        pulseMgrsSubR = Arrays.copyOfRange(pulseMgrsSubR, 1, pulseMgrsSubR.length);
                        for (String pulseMgrSubR : pulseMgrsSubR) {
                            byte[] subR = {(byte) Integer.parseInt(pulseMgrSubR.substring(6, 8), 16), (byte) Integer.parseInt(pulseMgrSubR.substring(8, 10), 16)};
                            pulseManagerLogSubRegisters.add(subR);
                        }
                        step++;
                        enviaCalibrationPulseManager(step);
                        break;
                    case 4:
                        String[] ktValues = Arrays.copyOfRange(vectorhex, idxData + 1, vectorhex.length - 2);
                        for (String ktVal : ktValues) {
                            word += ktVal;
                        }
                        String[] channelKtValues = word.split("34");
                        channelKtValues = Arrays.copyOfRange(channelKtValues, 1, channelKtValues.length);
                        for (String channelKtVal : channelKtValues) {
                            double value = ieeeToFloat(channelKtVal.substring(0, 2) + channelKtVal.substring(2, 4) + channelKtVal.substring(4, 6) + channelKtVal.substring(6, 8));
                            ktValuesL.add(value);
                        }
                        step = 0;
                        enviaTransformedRatios();
                        break;
                }
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    this.handleNACK();
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    public void enviaTransformedRatios() {
        calibrationPulseManager = false;
        transformedRatios = true;
        reqSeq++;
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        for (int i = 0; i < 4; i++) {
            payloadReq.add((byte) 0x70);
            payloadReq.add((byte) i);
            payloadReq.add(tramas.getValueLogs());
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Obtener las relaciones de transformación");
    }

    private void revisarTransformedRatios(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                String[] ratiosData = Arrays.copyOfRange(vectorhexO, idxData + 1, vectorhexO.length - 2);
                //System.out.println("Ratios Array: " + Arrays.toString(ratiosData));
                ArrayList<Integer> indxs = findCoincidence(ratiosData, "34");
                //System.out.println("Indices de coincidencia: " + indxs.toString());
                for (int i = 0; i < indxs.size(); i++) {
                    int currRatIdx = indxs.get(i);
                    String capturedRatio = ratiosData[currRatIdx + 1] + ratiosData[currRatIdx + 2] + ratiosData[currRatIdx + 3] + ratiosData[currRatIdx + 4];
                    escribir("Captured Ratio (Hex): " + capturedRatio);
                    double value = ieeeToFloat(capturedRatio);
                    transformedRatiosL.add(value);
                }
                escribir("PT factor:" + (transformedRatiosL.get(0) / transformedRatiosL.get(1)));
                escribir("CT factor:" + (transformedRatiosL.get(2) / transformedRatiosL.get(3)));
                step++;
                enviaLogPosition(step);
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }    

    public void enviaLogPosition(int stepL) {
        transformedRatios = false;
        logPositionCounterModules = true;
        reqSeq++;
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        byte[] dataRecMgr = this.activeModulesL.get(this.idxModuleRevenueLog);
        switch (stepL) {
            case 1:
                payloadReq.add(dataRecMgr[0]);
                payloadReq.add(dataRecMgr[1]);
                payloadReq.add(tramas.getLogPositionCounter()[0]);
                payloadReq.add(tramas.getLogPositionCounter()[1]);
                payloadReq.add(tramas.getLogPositionCounter()[2]);
                break;
            case 2:
                payloadReq.add(dataRecMgr[0]);
                payloadReq.add(dataRecMgr[1]);
                payloadReq.add(tramas.getLogs()[0]);
                payloadReq.add(tramas.getLogs()[1]);
                payloadReq.add(tramas.getLogs()[2]);
                break;
            case 3:
                byte[] positionRec = positionAndDepthRecords.get(0);
                byte[] depthRec = positionAndDepthRecords.get(1);
                payloadReq.add(positionRec[0]);
                payloadReq.add(positionRec[1]);
                payloadReq.add(tramas.getValuePositionLogs());
                payloadReq.add(depthRec[0]);
                payloadReq.add(depthRec[1]);
                payloadReq.add(tramas.getValueLogs());
                break;
        }
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Obtener contador de posición de registro y profundidad del mismo");
    }

    private void revisarLogPosition(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                switch (step) {
                    case 1:
                        if (vectorhex[idxData + 1].equalsIgnoreCase("FE")) {                            
                            int regIdx = idxData + 2;
                            while (regIdx < vectorhex.length - 3) {
                                int len1 = Integer.parseInt(vectorhex[regIdx], 16) & 0x0F;
                                int len2 = Integer.parseInt(vectorhex[regIdx], 16) & 0xFF;
                                int expLen = vectorhex.length - regIdx - 3;
                                if (len1 == expLen || len2 == expLen) {
                                    break;
                                } else {
                                    regIdx++;
                                }
                            }
                            byte[] positionRec = {(byte) Integer.parseInt(vectorhex[regIdx + 1], 16), (byte) Integer.parseInt(vectorhex[regIdx + 2], 16)};
                            positionAndDepthRecords.add(positionRec);
                            step++;
                            enviaLogPosition(step);
                            break;
                        }                        
                    case 2:
                        if (vectorhex[idxData + 1].equalsIgnoreCase("FE")) {
                            int regIdx = idxData + 2;
                            while (regIdx < vectorhex.length - 3) {
                                int len1 = Integer.parseInt(vectorhex[regIdx], 16) & 0x0F;
                                int len2 = Integer.parseInt(vectorhex[regIdx], 16) & 0xFF;
                                int expLen = vectorhex.length - regIdx - 3;
                                if (len1 == expLen || len2 == expLen) {
                                    break;
                                } else {
                                    regIdx++;
                                }
                            }
                            byte[] depthRec = {(byte) Integer.parseInt(vectorhex[regIdx + 1], 16), (byte) Integer.parseInt(vectorhex[regIdx + 2], 16)};
                            positionAndDepthRecords.add(depthRec);
                            step++;
                            enviaLogPosition(step);
                            break;
                        }
                    case 3:
                        int pos = idxData + 1;
                        int trail;
                        while (pos < vectorhex.length - 2) {
                            trail = Integer.parseInt(vectorhex[pos], 16) & 0x0F;
                            pos++;
                            String value = "";
                            for (int i = 0; i < trail; i++) {
                                value += vectorhex[pos + i];
                                escribir("Hex Value: " + value);
                            }
                            positionAndDepthValues.add(Integer.parseInt(value, 16));
                            pos += trail;
                        }
                        escribir("El registro del registrador de datos con manejador "
                                + tramas.encode(activeModulesL.get(idxModuleRevenueLog), 2)
                                + " tiene la posición del registro "
                                + positionAndDepthValues.get(0)
                                + " y la profundidad "
                                + positionAndDepthValues.get(1));
                        step = 0;
                        enviaRealTime();
                        break;
                }
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaRealTime() {       
        logPositionCounterModules = false;
        realTime = true;
        reqSeq++;
        byte[] data = concatFrames(asignaDireccion(tramaBase), tramas.getRealTime());
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud de Fecha");
    }

    private void revisarRealTime(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                hexDate = vectorhex[idxData + 2] + vectorhex[idxData + 3] + vectorhex[idxData + 4] + vectorhex[idxData + 5];
                enviaTimeZone();
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaTimeZone() {
        realTime = false;
        timeZone = true;
        reqSeq++;
        byte[] data = concatFrames(asignaDireccion(tramaBase), tramas.getTimeZone());
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud de Desfase Horario");
    }

    private void revisarTimeZone(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                offSetTZ = this.getSignedNumber(Integer.parseInt(vectorhex[idxData + 2] + vectorhex[idxData + 3], 16), 16);
                if (!medidorDesfasado(hexDate, offSetTZ)) {
                    /*if (lconfHora) {
                        //Método para actualizar la fecha del medidor
                    } else*/ if (leventos) {
                        enviaLogPositionEvents();
                    } else if (lperfil) {
                        perfilIncompleto = true;
                        enviaLoadProfile(true);                        
                    } else {
                        cerrarPuerto();
                        escribir("Ninguna opción seleccionada");
                        cerrarLog("Leido", true);
                        leer = false;
                    }
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        cerrarPuerto();
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error por Desfase");
                        cerrarLog("Desconexion Error por Desfase", false);
                        leer = false;                        
                        break;
                    } else {
                        frameCounter++;
                    }
                }
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    
    public void enviaLogPositionEvents() {
        timeZone = false;
        logPositionEvents = true;
        reqSeq++;
        List<Byte> payloadReq = new ArrayList();
        payloadReq.add((byte) 0xF6);
        byte[] dataEv = tramas.getEvents();
        payloadReq.add(dataEv[0]);
        payloadReq.add(dataEv[1]);
        payloadReq.add(tramas.getValuePositionLogs());
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), payloadReqArr);
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Obtener contador de posición de registro de Eventos");
    }

    private void revisarLogPositionEvents(String[] vectorhexO) {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                int trail = Integer.parseInt(vectorhex[idxData + 1], 16) & 0x0F;
                String value = "";
                for (int i = 0; i < trail; i++) {
                    value += vectorhex[idxData + (i + 2)];
                    escribir("Hex Value: " + value);
                }
                this.positionValueEvent.add(Integer.parseInt(value, 16));                                    
                escribir("Posición del registro de eventos: " + positionValueEvent.get(0));
                enviaEventos(true);
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }
    
    private void enviaEventos(boolean first) {
        logPositionEvents = false;
        primerBloque = true;
        events = true;        
        nBytes = 0;
        reqSeq++;
        byte[] reqEvents = tramas.getLoadProfile();
        reqEvents[1] = tramas.getEvents()[0];
        reqEvents[2] = tramas.getEvents()[1];
        List<Byte> payloadReq = new ArrayList();
        int it;
        String beginPositionStr;
        String endPositionStr;
        nRegistros = 50;
        escribir("No. Registros a solicitar: " + nRegistros);
        if (first) {
            beginPositionStr = Integer.toHexString(this.positionValueEvent.get(0) - (nRegistros + 1));
        } else {
            beginPositionStr = Integer.toHexString((lastRegEvent + 1));
        }
        endPositionStr = Integer.toHexString(positionValueEvent.get(0) - 1);

        if (beginPositionStr.length() % 2 != 0) {
            beginPositionStr = "0" + beginPositionStr;
        }

        if (endPositionStr.length() % 2 != 0) {
            endPositionStr = "0" + endPositionStr;
        }
        escribir("Position Inicial: " + beginPositionStr);
        escribir("Position Final: " + endPositionStr);
        //Rellenar si es un número impar con cero a la izquierda.
        byte[] beginPositionB = new byte[beginPositionStr.length() / 2];
        byte[] endPositionB = new byte[endPositionStr.length() / 2];
        for (int idx = 0; idx < beginPositionB.length; idx++) {
            beginPositionB[idx] = (byte) (Integer.parseInt(beginPositionStr.substring(idx * 2, (2 + idx * 2)), 16) & 0xFF);
        }
        for (int idx = 0; idx < endPositionB.length; idx++) {
            endPositionB[idx] = (byte) (Integer.parseInt(endPositionStr.substring(idx * 2, (2 + idx * 2)), 16) & 0xFF);
        }
        lenBytesReg = beginPositionB.length;

        for (int i = 0; i < 2; i++) {
            it = 0;
            payloadReq.add((byte) (96 + lenBytesReg));
            if (i == 0) {
                while (it < lenBytesReg) {
                    payloadReq.add(beginPositionB[it]);
                    it++;
                }
            } else if (i == 1) {
                while (it < lenBytesReg) {
                    payloadReq.add(endPositionB[it]);
                    it++;
                }
            }
        }
        payloadReq.add((byte) 0xF3);
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), concatFrames(reqEvents, payloadReqArr));
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud de Eventos");
    }
    
    
    private void revisarEventos(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                int lengthSubBlock;
                if (primerBloque) {
                    lengthSubBlock = vectorhex.length - (idxData + 2) - 2;
                    int lengthFullBlock = Integer.parseInt(vectorhex[idxData - 1] + vectorhex[idxData], 16) - 1;
                    primerBloque = false;
                    partialEventArr = new String[lengthFullBlock];
                    System.arraycopy(vectorhex, idxData + 2, partialEventArr, 0, lengthSubBlock);
                    nBytes += lengthSubBlock;
                } else {
                    lengthSubBlock = (Integer.parseInt(vectorhex[5], 16) & 0xFF) - 7;
                    System.arraycopy(vectorhex, 12, partialEventArr, nBytes, vectorhex.length - 14);
                    nBytes += lengthSubBlock;
                }
                byte[] blockSeq = {(byte) (Integer.parseInt(vectorhex[10], 16) & 0x0F), (byte) Integer.parseInt(vectorhex[11], 16)};
                enviaNextBlock(blockSeq);
                if (blockSeq[0] == 0 && blockSeq[1] == 0) {
                    escribir("Bloque completo: " + Arrays.toString(partialEventArr));
                    //Agregar el array de datos parcial a la estructura completa
                    nextBlock = false;
                    //Encuentra último registro
                    String[] eventsArr = splitArrayEvents(partialEventArr, 0xF3);
                    if (eventsArr.length > 0) {
                        findEvents(eventsArr, temporalEventList);
                        String lastRegStr = findLastReg(eventsArr[eventsArr.length - 1]);
                        lastRegEvent = Integer.parseInt(lastRegStr, 16) & 0xFFFFFFFF;
                        if (lastRegEvent == (this.positionValueEvent.get(0) - 1)) {
                            if (temporalEventList.size() > 0) {
                                events = false;
                                System.out.println("Eventos Completado");
                                escribir("Eventos Completado");
                                String ultRegStr = Integer.toHexString(positionValueEvent.get(0));
                                if (ultRegStr.length() % 2 != 0) {
                                    ultRegStr = "0" + ultRegStr;
                                }
                                cp.AlmacenaEventos_ION(temporalEventList);
                                if (lperfil) {
                                    enviaLoadProfile(true);
                                } else {
                                    events = false;
                                    cerrarPuerto();
                                    cerrarLog("Leido", true);
                                    leer = false;
                                }
                            } else {
                                events = false;
                                cerrarPuerto();
                                cerrarLog("Leido sin eventos", true);
                                leer = false;
                            }
                        } else {
                            enviaEventos(false);
                        }
                    } else {
                        events = false;
                        cerrarPuerto();
                        cerrarLog("Data sin Eventos", true);
                        leer = false;
                    }

                } else {
                    nextBlock = true;
                }
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    
    private void enviaLoadProfile(boolean first) {
        timeZone = false;
        events = false;
        loadProfile = true;
        primerBloque = true;
        nBytes = 0;
        reqSeq++;
        byte[] reqLoadProfile = tramas.getLoadProfile();
        reqLoadProfile[1] = positionAndDepthRecords.get(0)[0];
        reqLoadProfile[2] = positionAndDepthRecords.get(0)[1];
        List<Byte> payloadReq = new ArrayList();
        int it;
        String beginPositionStr;
        String endPositionStr;
        nRegistros = calculaRegistros(med.getFecha(), desfase, intPeriod);
        escribir("No. Registros a solicitar: " + nRegistros);
        if (first) {
            beginPositionStr = Integer.toHexString(positionAndDepthValues.get(0) - (nRegistros + 1));
        } else {
            beginPositionStr = Integer.toHexString((lastReg + 1));
        }
        endPositionStr = Integer.toHexString(positionAndDepthValues.get(0) - 1);

        if (beginPositionStr.length() % 2 != 0) {
            beginPositionStr = "0" + beginPositionStr;
        }

        if (endPositionStr.length() % 2 != 0) {
            endPositionStr = "0" + endPositionStr;
        }
        escribir("Position Inicial: " + beginPositionStr);
        escribir("Position Final: " + endPositionStr);
        //Rellenar si es un número impar con cero a la izquierda.
        byte[] beginPositionB = new byte[beginPositionStr.length() / 2];
        byte[] endPositionB = new byte[endPositionStr.length() / 2];
        for (int idx = 0; idx < beginPositionB.length; idx++) {
            beginPositionB[idx] = (byte) (Integer.parseInt(beginPositionStr.substring(idx * 2, (2 + idx * 2)), 16) & 0xFF);
        }
        for (int idx = 0; idx < endPositionB.length; idx++) {
            endPositionB[idx] = (byte) (Integer.parseInt(endPositionStr.substring(idx * 2, (2 + idx * 2)), 16) & 0xFF);
        }
        lenBytesReg = beginPositionB.length;
        for (int i = 0; i < 2; i++) {
            it = 0;
            payloadReq.add((byte) (96 + lenBytesReg));
            if (i == 0) {
                while (it < lenBytesReg) {
                    payloadReq.add(beginPositionB[it]);
                    it++;
                }
            } else if (i == 1) {
                while (it < lenBytesReg) {
                    payloadReq.add(endPositionB[it]);
                    it++;
                }
            }
        }
        payloadReq.add((byte) 0xF3);
        byte[] payloadReqArr = new byte[payloadReq.size()];
        for (int i = 0; i < payloadReq.size(); i++) {
            payloadReqArr[i] = payloadReq.get(i);
        }
        byte[] data = concatFrames(asignaDireccion(tramaBase), concatFrames(reqLoadProfile, payloadReqArr));
        setHeaderParams(data);
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud de Perfil de Carga");
    }

    private void revisarLoadProfile(String[] vectorhexO) throws Exception {
        if (uncompleteF) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int idxData;
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            idxData = password.isEmpty() ? 35 : 43;
            if (tramaOK && !uncompleteF && !delayed) {
                int lengthSubBlock;
                if (primerBloque) {
                    lengthSubBlock = vectorhex.length - (idxData + 2) - 2;
                    int lengthFullBlock = Integer.parseInt(vectorhex[idxData - 1] + vectorhex[idxData], 16) - 1;
                    primerBloque = false;
                    partialDataArr = new String[lengthFullBlock];
                    System.arraycopy(vectorhex, idxData + 2, partialDataArr, 0, lengthSubBlock);
                    nBytes += lengthSubBlock;
                } else {
                    lengthSubBlock = (Integer.parseInt(vectorhex[5], 16) & 0xFF) - 7;
                    System.arraycopy(vectorhex, 12, partialDataArr, nBytes, vectorhex.length - 14);
                    nBytes += lengthSubBlock;
                }
                byte[] blockSeq = {(byte) (Integer.parseInt(vectorhex[10], 16) & 0x0F), (byte) Integer.parseInt(vectorhex[11], 16)};
                enviaNextBlock(blockSeq);
                if (blockSeq[0] == 0 && blockSeq[1] == 0) {
                    escribir("Bloque completo: " + Arrays.toString(partialDataArr));
                    //Agregar el array de datos parcial a la estructura completa
                    nextBlock = false;
                    temporalDataList.addAll(Arrays.asList(partialDataArr));
                    //Encuentra último registro
                    //int offset = nCanales * 5 + 11;
                    String lastRegStr = findLastRegLP(partialDataArr);
                    /*
                    for (int i = 0; i < lenBytesReg; i++) {
                        lastRegStr = partialDataArr[partialDataArr.length - offset - (i + 1)] + lastRegStr;
                    }
                    */
                    if (lastRegStr.isEmpty()) {
                        escribir("Último registro no encontrado, se procesa hasta la obtenido");
                        loadProfile = false;
                        cerrarPuerto();
                        escribir("Perfil Incompleto");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Intercambio de datos con el medidor incompleto");
                        String[] dataArr = new String[temporalDataList.size()];
                        temporalDataList.toArray(dataArr);
                        perfilIncompleto = true;
                        String ultRegStr = Integer.toHexString(positionAndDepthValues.get(0));
                        if (ultRegStr.length() % 2 != 0) {
                            ultRegStr = "0" + ultRegStr;
                        }
                        cp.AlmacenaPerfil_ION(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), dataArr, intPeriod, nCanales, profileElementsV, file);
                        cerrarLog("Leido Incompleto", true);
                        leer = false;
                        break;
                    }
                    escribir("Último Registro  del bloque: " + lastRegStr);
                    lastReg = Integer.parseInt(lastRegStr, 16) & 0xFFFFFFFF;
                    if (lastReg == (positionAndDepthValues.get(0) - 1)) {
                        //Perfil de carga completado
                        loadProfile = false;
                        cerrarPuerto();
                        System.out.println("Perfil Completado");
                        escribir("Perfil Completado");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Intercambio de datos con el medidor OK");
                        String[] dataArr = new String[temporalDataList.size()];
                        temporalDataList.toArray(dataArr);
                        perfilIncompleto = false;
                        String ultRegStr = Integer.toHexString(positionAndDepthValues.get(0));
                        if (ultRegStr.length() % 2 != 0) {
                            ultRegStr = "0" + ultRegStr;
                        }
                        cp.AlmacenaPerfil_ION(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), dataArr, intPeriod, nCanales, profileElementsV, file);
                        cerrarLog("Leido", true);
                        leer = false;
                        break;
                    } else {
                        enviaLoadProfile(false);
                    }
                } else {
                    nextBlock = true;
                }
                break;
            } else {
                if (frameCounter == framesQty) {
                    escribir("Trama incorrecta");
                    handleNACK();
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaNextBlock(byte[] blockSeq) {
        byte[] data = asignaDireccion(tramas.getNextOrLogout());
        data[4] = (byte) 0xE0;
        data[10] = blockSeq[0];
        data[11] = blockSeq[1];
        data = calculaCRC16(data);
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        if (blockSeq[0] == 0 && blockSeq[1] == 0) {
            escribir("=> " + tramas.encode(data, data.length));
            enviaTrama(data);
        } else {
            enviaTrama2_2(data, "=> Next Block");
        }
    }

    private void interpretaCadena(String cadenahex) throws ParseException, Exception {
        try {
            frames = new ArrayList<>();
            escribir("<= " + cadenahex);
            System.out.println("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            //protocolo 
            if (inicioSesion) {//inicio de session y envio de passsword
                revisarInicioSession(vectorhex);
            } else if (serialnumber) {
                revisarSerialNumber(vectorhex);
            } else if (listManager) {
                revisarListManager(vectorhex);
            } else if (dataRecordManager) {
                revisarDataRecordManager(vectorhex);
            } else if (activeDataRecordsModules) {
                revisarActiveDataRecordsModules(vectorhex);
            } else if (activeConfInputs) {
                revisarActiveConfInputs(vectorhex);
            } else if (periodModule) {
                revisarPeriodModule(vectorhex);
            } else if (labelsDataRecordsModules) {
                revisarLabelsDataRecordModules(vectorhex);
            } else if (calibrationPulseManager) {
                revisarCalibrationPulseManager(vectorhex);
            } else if (transformedRatios) {
                revisarTransformedRatios(vectorhex);
            } else if (logPositionCounterModules) {
                revisarLogPosition(vectorhex);
            } else if (realTime) {
                revisarRealTime(vectorhex);
            } else if (timeZone) {
                revisarTimeZone(vectorhex);
            } else if (logPositionEvents) {
                revisarLogPositionEvents(vectorhex);
            } else if (events) {
                revisarEventos(vectorhex);
            } else if (loadProfile) {
                revisarLoadProfile(vectorhex);
            } else {
                //cierra el puerto
                escribir("Error de recepcion");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Leer_ION8650", "Desconexion Error recepcion");
                escribir("Estado lectura No leido");
                cerrarLog("Desconexion Error recepcion", false);
                leer = false;
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

    public String findLastReg(String data) {
        String lastRegStr = data.substring(2, 2 + lenBytesReg*2);
        escribir("Último Registro  del bloque: " + lastRegStr);
        return lastRegStr;
    }
    
    public String findLastRegLP(String[] data) {
        for (int idx = data.length - 3; idx > 0; idx--) {
            if ( data[idx].equalsIgnoreCase("63") ) {
                idx--;
                if ( data[idx].equalsIgnoreCase("F3") ) {
                    //Registro Encontrado
                    return ""+data[idx+2] + data[idx+3] + data[idx+4];
                }
            } else if ( data[idx].equalsIgnoreCase("62") ) {
                idx--;
                if ( data[idx].equalsIgnoreCase("F3") ) {
                    //Registro Encontrado
                    return ""+data[idx+2] + data[idx+3];
                }
            }
        }
        return "";
    }
    
    public static String[] splitArrayEvents(String[] arrayData, int pattern) {        
        List<String> splitList = new ArrayList();
        String tempStr = "";
        for (int i = 0; i < arrayData.length - 1; i++){
            if ( ( Integer.parseInt(arrayData[i], 16) & 0xFF )== pattern && (Integer.parseInt(arrayData[i + 1], 16) & 0xFF) == pattern) {
                splitList.add(tempStr);
                tempStr = "";
                i ++;
            } else {
                tempStr += arrayData[i];
            }
        }        
        String[] dataArr = new String[splitList.size()];
        splitList.toArray(dataArr);
        return dataArr;
    }
    
    public void findEvents(String[] eventsArr, List<ERegistroEvento> eventList) {
        ERegistroEvento eventEntity = new ERegistroEvento();
        for (String event : eventsArr) { 
            String powerUpStr = "506f77657220557000";
            String powerDownStr = "506f77657220446f776e00";
            boolean powerUp = event.contains(powerUpStr);
            boolean powerDown = event.contains(powerDownStr);
            if (powerDown) {
                eventEntity.setVcserie(seriemedidor);
                eventEntity.setVctipo("0001");
                eventEntity.setVcfechacorte(getFechaEventos(event));
            }            
            if (powerUp) {
                eventEntity.setVcfechareconexion(getFechaEventos(event));
                eventList.add(eventEntity);
                eventEntity = new ERegistroEvento();
            }
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
            escribir(getErrorString(e.getStackTrace(), 3));
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
                                }
                                if (bytes[0] != (byte) 0xFF) {
                                    escribir(des);
                                    escribir("=> " + tramas.encode(trama, trama.length));
                                }
                                try {
                                    monitor.notifyAll();
                                    enviaTrama(trama);
                                    monitor.wait();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    escribir("Error enviando trama tipo 2_2");
                                }
                            } else {
                                System.out.println("Sale enviatrama2");
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
                            if (loadProfile & perfilIncompleto) {
                                if (temporalDataList.size() > 0) {
                                    String[] dataArr = new String[temporalDataList.size()];
                                    temporalDataList.toArray(dataArr);
                                    AlmacenarRegistrosIncompletos(dataArr);
                                }
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
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion proceso abortado");
                                    cerrarLog("Abortado", false);
                                    leer = false;
                                }
                            } else {
                                escribir("Numero de reintentos agotado");
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado");
                                escribir("Estado Lectura No leido");
                                cerrarLog("Desconexion Numero de reintentos agotado", false);
                                leer = false;
                            }
                        }
                    } catch (Exception e) {
                        cerrarPuerto();
                        escribir("Error en hilo envia trama");
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
            if (bytes[0] != (byte) 0xFF) {
                output.write(bytes, 0, bytes.length);
                output.flush();
            } else {
                escribir("Es necesario sensar el buffer de escucha nuevamente para completar trama o para capturar tramas desfasadas");
            }

        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }

    }   

    private String unitSelector(String strCanal) {
        switch (strCanal) {
            case "kWh del int":
                return "kWhD";
            case "kWh rec int":
                return "kWhR";
            case "kVARh del int":
                return "kVarhD";
            case "kVARh rec int":
                return "kVarhR";
            default:
                return "No Unit";
        }
    }

    private void setHeaderParams(byte[] data) {
        // Total Length
        data[5] = (byte) ((data.length - 5) & 0xFF);
        //data[11]
        //Session Length 
        data[12] = (byte) (((data.length - 12) >> 8) & 0xFF);
        data[13] = (byte) ((data.length - 12) & 0xFF);
        //Request Sequential
        data[14] = (byte) ((reqSeq >> 8) & 0xFF);
        data[15] = (byte) (reqSeq & 0xFF);
        //
        data[23] = (byte) ((dirfis >> 8) & 0xFF);
        data[24] = (byte) (dirfis & 0xFF);

        if (!password.isEmpty()) {
            // User
            data[25] = (byte) ((user >> 8) & 0xFF);
            data[26] = (byte) (user & 0xFF);
            //Password
            data[27] = (byte) ((pwdLong >> 40) & 0xFF);
            data[28] = (byte) ((pwdLong >> 32) & 0xFF);
            data[29] = (byte) ((pwdLong >> 24) & 0xFF);
            data[30] = (byte) ((pwdLong >> 16) & 0xFF);
            data[31] = (byte) ((pwdLong >> 8) & 0xFF);
            data[32] = (byte) (pwdLong & 0xFF);
            //Length Req
            data[42] = (byte) ((data.length - 43) & 0xFF);
        } else {
            //Length Req
            data[34] = (byte) ((data.length - 35) & 0xFF);
        }

    }

    private byte[] calculaCRC16(byte[] data) {
        byte[] crcB = new byte[2];
        int crc = 0xFFFF;
        for (int i = 4; i < data.length; i++) {
            crc = (crc >>> 8) ^ tableCRC16[(crc ^ data[i]) & 0xff];
        }
        crcB[0] = (byte) (crc & 0xFF);
        crcB[1] = (byte) ((crc >> 8) & 0xFF);
        return concatFrames(data, crcB);
    }

    private static boolean validaCRC16(String[] dataR) {
        byte[] crcB = new byte[2];
        byte[] crcData = {(byte) (Integer.parseInt(dataR[dataR.length - 2], 16) & 0xFF), (byte) (Integer.parseInt(dataR[dataR.length - 1], 16) & 0xFF)};
        //System.out.println(""+crcData[0]+","+crcData[1]);
        int crc = 0xFFFF;
        for (int i = 4; i < dataR.length - 2; i++) {
            System.out.println("" + dataR[i]);
            crc = (crc >>> 8) ^ tableCRC16[(crc ^ (byte) (Integer.parseInt(dataR[i], 16) & 0xFF)) & 0xff];
        }
        crcB[0] = (byte) (crc & 0xFF);
        crcB[1] = (byte) ((crc >> 8) & 0xFF);
        //System.out.println(""+crcB[0]+","+crcB[1]);
        return (crcB[0] == crcData[0] && crcB[1] == crcData[1]);
    }

    private byte[] asignaDireccion(byte[] burnFrame) {
        burnFrame[6] = (byte) (dirOrig & 0xFF);
        burnFrame[7] = (byte) ((dirOrig >> 8) & 0xFF);
        if (!inicioSesion) {
            burnFrame[8] = (byte) (dirfis & 0xFF);
            burnFrame[9] = (byte) ((dirfis >> 8) & 0xFF);
        }
        return burnFrame;
    }

    private String[] concatFrames(String[] first, String[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private byte[] concatFrames(byte[] first, byte[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public void AlmacenarRegistrosIncompletos(String[] dataArr) {
        try {
            cp.AlmacenaPerfil_ION(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), dataArr, intPeriod, nCanales, profileElementsV, file);
        } catch (Exception e) {
        }
        escribir("Estado Lectura Leido Incompleto");
    }

    public void almacenaEventos() {

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

    private ArrayList<Integer> findCoincidence(String[] arr, String pattern) {
        ArrayList<Integer> coincidencesIdx = new ArrayList();
        for ( int i = 0; i < arr.length; i++ ) {
            if ( arr[i].equals(pattern) ) {
                coincidencesIdx.add(i);
            }
        }
        return coincidencesIdx;
    }
    
    private double ieeeToFloat(String hexString) throws Exception {
        hexString = hexString.replace(" ", "");
        /* 32-bit */
        if (hexString.length() == 8) {
            return Float.intBitsToFloat(Integer.parseUnsignedInt(hexString, 16));
        } /* 64-bit */ else if (hexString.length() == 16) {
            return Double.longBitsToDouble(Long.parseUnsignedLong(hexString, 16));
        } /* An exception thrown for mismatched strings */ else {
            throw new Exception("Does not represent internal bits of a floating-point number");
        }
    }

    private boolean medidorDesfasado(String hexDate, int offsetTZ) {
        boolean desfasado = false;
        deltatimesync2 = obtenerHora();
        escribir("Fecha Colombia: " + deltatimesync2);
        Long milliseg = Long.parseLong(hexDate, 16) * 1000L;
        Timestamp fechaActualTS = new Timestamp(milliseg - (offsetTZ * 1000));
        escribir("Fecha del medidor: " + fechaActualTS);
        escribir("Offset del medidor en segundos: " + offsetTZ);
        desfase = (obtenerHora().getTime() - (fechaActualTS.getTime() + ((int) (deltatimesync2.getTime() - deltatimesync1.getTime())))) / 1000;
        escribir("Desfase calculado en segundos: " + desfase);
        if (Math.abs(desfase) > ndesfasepermitido) {
            escribir("No se solicitara el perfil de carga");
            System.out.println("No se solicitara el perfil de carga");
            desfasado = true;
        }
        cp.actualizaDesfase(desfase, med.getnSerie());
        return desfasado;
    }
    
    private Timestamp getFechaEventos(String evento) {
        String firstPart = evento.substring(8, 16);
        String secondPart = evento.substring(16, 24);
        escribir("Fecha evento hexadecimal: " + firstPart + secondPart);
        Long millisegVal = (Long.parseLong(firstPart, 16)*2000L) + Long.parseLong(secondPart, 16)/2147000;
        return new Timestamp(millisegVal);
    }

    private int getSignedNumber(int n, int nBitsUsed) {
        boolean negative = (n >> (nBitsUsed - 1)) == 1;
        //System.out.println("Negative? " + negative);
        if (negative) {
            return (-1) * ((~n & (int) (Math.pow(2, nBitsUsed - 1) - 1)) + 1);
        } else {
            return n;
        }
    }

    private int calculaRegistros(Timestamp ufecLec, long desfaseL, int periodoInt) {
        escribir("Periodo a leer: ");
        escribir("\tDesde la fecha de última lectura: " + ufecLec);
        escribir("\tHasta la fecha de actual: " + obtenerHora());
        long diffInMillies = Math.abs((obtenerHora().getTime() - (desfaseL * 1000)) - ufecLec.getTime());
        long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        nminutes = (int) diff;
        escribir("Númmero de minutos de diferencia: " + nminutes);
        escribir("Periodo de integración: " + periodoInt + " minutos");
        return (int) Math.ceil((float) nminutes / (float) periodoInt);
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

    private List<Integer> idxSplitArray(String[] arrayHex) {
        List<Integer> idxList = new ArrayList<>();
        int idx = 0;
        for (String str : arrayHex) {
            if (str.equals("BB") && arrayHex[idx + 1].equals("BB")) {
                idxList.add(idx);
            }
            idx++;
        }
        return idxList;
    }

    private List<String[]> getFrames(String[] arrayHex, List<Integer> idxList) {
        List<String[]> nListHex = new ArrayList<>();
        idxList.add(arrayHex.length);
        System.out.println("Idx List: " + idxList);
        for (Integer i = 0; i < idxList.size() - 1; i++) {
            System.out.println("I: " + i);
            String[] arrayStrTemp = new String[idxList.get(i + 1) - idxList.get(i)];
            System.out.println("" + idxList.get(i) + "-" + idxList.get(i + 1));
            System.arraycopy(arrayHex, idxList.get(i), arrayStrTemp, 0, idxList.get(i + 1) - idxList.get(i));
            nListHex.add(arrayStrTemp);
        }
        return nListHex;
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
    
    private void handleNACK() {
        if (intentosReenvioLastFrame < numeroReintentos) {
            intentosReenvioLastFrame++;
            if (uncompleteF || delayed) {
                enviaTrama2_2(sendNothing, "Espera tramas retrasadas");//No envía nada
            } else {
                //Se debería enviar un NACK, pero nunca se contó con documentación de este protocolo, por tanto se desconoce como se rechaza la respuesta al medidor.
                escribir("Trama erronea, NACK desconocido, lectura abortada");
                cerrarPuerto();
                leer = false;
                //Buscar como mandar un NACK
                //enviaTrama2_2(null, "NAK", (byte) 0x15);//Envia nak
            }
        } else {
            intentosReenvioLastFrame = 0;
            escribir("Numero de reintentos agotado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado");
            escribir("Estado Lectura No leido");
            cerrarLog("Desconexion Numero de reintentos agotado", false);
            cerrarPuerto();
            leer = false;
        }
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
