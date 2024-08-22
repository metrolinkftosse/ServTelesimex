/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemoto_SEL735;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.ERegistroEvento;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nicol
 */
public class LeerRemotoTCP_SEL735 {
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
    //long tiempo = 500; //delat del puerto
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
    boolean tramaOK = false, delayed = false, eot = false;
    //variable que indica si lee
    public volatile boolean leer = true;//indicador de lectura realizada
    String numeroPuerto; //puerto de la IP
    int numeroReintentos = 4;
    private int intentosReenvioLastFrame = 0;
    int nreintentos = 0; //intento actual completo
    int velocidadPuerto; //velocidad del puerto
    long timeout;//timeout  para las peticiones
    private int user;
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
    private String fechaMed;
    private String horaMed;
    private String fechaCompletaMed;
    //formatos de fechas
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    SimpleDateFormat sdf4 = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf5 = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    SimpleDateFormat sdf6 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    SimpleDateFormat sdf7 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfacceso = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    int numcanales = 4; //numero de canales
    int intervalo = 0; //intervalo de datos 15,30,60.
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    Abortar objabortar;//variable que controla la opcion de abortar la comunicacion
    String usuario = "admin";
    int reinicio = 0;
    TramasRemoto_SEL735 tramas = new TramasRemoto_SEL735();//obejto con las tramas de envio
    //estados protocolo
    boolean cambiaUsuario = false;
    boolean inicioSesion = false;
    boolean contrasena = false;
    boolean displaySettings;
    boolean loadProfileSettings;
    boolean loadProfileSettingsR;
    boolean date;
    boolean time;
    boolean loadProfile;
    boolean events;
    boolean confHora;
    boolean realTime;
    private boolean primerBloque;    
    private boolean inTx;
    public boolean complemento = false;
    private boolean perfilIncompleto = false;
    int resolucion = 1;
    int intercambio = 0;//valor que diferencia las solicitudes del perfil de carga segun explicacion del protocolo
    private byte[] sendNothing = {(byte) 0xFF};
    private byte[] CR = {(byte) 0x0D};
    private byte[] C = {(byte) 0x43};
    private byte[] ackB = {(byte) 0x06};
    private byte[] nackB = {(byte) 0x15};
    private int reqSeq = 0;
    private final Object monitor = new Object();

    private ZoneId zid;
    int reenvios = 0;
    int stepConfHora = 1;
    String toSetDate = "";
    String toSetTime = "";

    private String displaySettingsStr = "";    
    private String loadProfileSettingsStr = "";
    private String loadProfileSettingsRStr = "";
    private String eventsStr = "";
    private ArrayList<String> channelsName = new ArrayList<>();
    private ArrayList<String> channelsFunction = new ArrayList<>();
    private ArrayList<Integer> channelsType = new ArrayList<>();
    private ArrayList<Double> channelsMultiplier = new ArrayList<>();
    private ArrayList<Double> channelsScalar = new ArrayList<>();
    private List<String> loadProfileData = new ArrayList();
    private List<ERegistroEvento> temporalEventList = new ArrayList();
    private LinkedHashMap<String, String> displaySettingsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, String> loadProfileSettings_1 = new LinkedHashMap<>();
    private LinkedHashMap<String, String> loadProfileSettings_R = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> channelName_Value = new LinkedHashMap<>();
    private LinkedHashMap<Timestamp, LinkedHashMap<String, Double>> dataLDP = new LinkedHashMap<>();
    private int nCanales = 4;
    private int intPeriod = 15;
    private HashMap<String, Integer> channelsCode = new HashMap<>();
    private Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    private HashMap<Integer, EConstanteKE> consKE_Map = new HashMap<>();
    private HashMap<String, EtipoCanal> tipoCanal_Map = new HashMap<>();
    private ArrayList<EtipoCanal> vtipocanal;
    List<String[]> frames = new ArrayList<>();
    private final String label = "LeerTCP_SEL735";

    private static final int[] tableCRC16 = {0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
        0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
        0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
        0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
        0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
        0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
        0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
        0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
        0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
        0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
        0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
        0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
        0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
        0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
        0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
        0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
        0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
        0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
        0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
        0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
        0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
        0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
        0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
    };
        
    public LeerRemotoTCP_SEL735(EMedidor med, boolean perfil, boolean eventos, boolean registros, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) {

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
            password = med.getPassword();
            user = Integer.parseInt((med.getDireccionCliente() == null ? "0" : med.getDireccionCliente()));
            byte[] defPwd = tramas.getPasswords().get(user);
            password = password == null ? tramas.encode(defPwd, defPwd.length) : password;
            timeout = med.getTimeout() * 1000;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            channelsCode.put("WH3_DEL", 1);
            channelsCode.put("WH3_REC", 2);
            channelsCode.put("QH3_DEL", 3);
            channelsCode.put("QH3_REC", 4);
            channelsCode.put("VH3", 5);
            channelsCode.put("IH3", 6);

            lconske = cp.buscarConstantesKe(med.getnSerie());//se toman los valores de las constantes 
            lconske.forEach((consKe) -> {
                escribir("" + consKe.getCanal());
                consKE_Map.put(consKe.getCanal(), consKe);
            });
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            vtipocanal.forEach((canal) -> {
                escribir("" + canal.getUnidad());
                tipoCanal_Map.put(canal.getCanal(), canal);
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

    private void iniciacomunicacion() throws Exception {
        //tiemporetransmision = 15000 + tiempo;//establece el tiempo de retransmision
        loadProfile = false;// pone el estado de perfil de carga.
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
                System.out.println("Envia Solicitud Inicio Session");
                //se envia la primera trama de comuniccion
                byte[] data = tramas.getUsers().get(user).clone();
                System.out.println("=> " + tramas.encode(data, data.length));
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
        byte[] readBuffer = new byte[!events? 2047 : 65536];
        int numbytes = 0, idxFrame = 0;
        boolean completar = false;
        try {
            synchronized (monitor) {
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                boolean expTime = true;
                //si el puerto tiene datos llenamos el buffer con lo que se encuentra en el puerto.
                while (!socket.isClosed() && System.currentTimeMillis() < endT) {
                    monitor.wait(250);
                    if (input.available() == 0) {
                        continue;
                    }
                    expTime = false;
                    if (realTime) {
                        deltatimesync1 = obtenerHora();
                    }               
                    endT = System.currentTimeMillis() + timeout;
                    if (completar) {
                        numbytes = input.read(readBuffer, idxFrame, readBuffer.length - idxFrame);
                    } else {
                        numbytes = input.read(readBuffer);
                    }
                    if (readBuffer[0] == 1 || readBuffer[0] == 2) { // Quiere decir que es una Tx YMODEM  
                        int expectedLen = readBuffer[0] == 1 ? 133 : 1029;
                        if (!completar) {                            
                            if (numbytes < expectedLen) {
                                escribir("Trama incompleta");
                                idxFrame = numbytes;
                                completar = true;
                                continue;
                            } else if (numbytes > expectedLen) { // Caracteres duplicados
                                Object[] ans = cleanConsecutiveDuplicates(readBuffer, (byte) 0xFF, numbytes);
                                readBuffer = (byte[]) ans[0];
                                numbytes = (int) ans[1];
                                if (numbytes < expectedLen) {
                                    continue;
                                }
                            }
                        } else {
                            idxFrame += numbytes;
                            if (idxFrame < expectedLen) {
                                escribir("Trama incompleta");
                                completar = true;
                                continue;
                            } else if (idxFrame > expectedLen) { // Caracteres duplicados
                                Object[] ans = cleanConsecutiveDuplicates(readBuffer, (byte) 0xFF, idxFrame);
                                readBuffer = (byte[]) ans[0];
                                idxFrame = (int) ans[1];
                                if (idxFrame < expectedLen) {
                                    continue;
                                }
                            }
                        }                        
                    }                                        
                    enviando = false;
                    reenviando = false;
                    monitor.notifyAll();
                    break;
                }
                if (!socket.isClosed() && expTime) {
                    escribir("Se vencío el timeout de respuesta sin recibir nada");
                    reenviando = true;
                    enviando = false;
                    monitor.notifyAll();
                    return;
                }
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramas.encode(readBuffer, numbytes);
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

    //Revisa solo tramas que se encuentre bajo el protocolo YMODEM
    private void revisaTrama(String[] vectorhex) {
        tramaOK = false;
        delayed = false;
        boolean validaCRC;

        if ((vectorhex[0].equalsIgnoreCase("01") || vectorhex[0].equalsIgnoreCase("02"))) { // Se verifica el inicio de la trama             
            if ((Integer.parseInt(vectorhex[1], 16) & 0xFF) == reqSeq && (Integer.parseInt(vectorhex[2], 16) & 0xFF) == (255 - reqSeq)) { // Validamos Secuencias de Respuestas         
                validaCRC = (validaCRC16(vectorhex));
                if (validaCRC) {//validamos CRC
                    tramaOK = true;
                    frames.add(vectorhex);
                } else {// CRC
                    escribir("Posibble Consecutive FF Byte Duplication");
                    vectorhex = cleanConsecutiveDuplicates(vectorhex, "FF");
                    validaCRC = (validaCRC16(vectorhex));
                    if (validaCRC) {
                        tramaOK = true;                        
                    } else {
                       escribir("BAD CRC"); 
                    }
                    frames.add(vectorhex);
                }
            } else {
                tramaOK = true;
                delayed = true;
                escribir("Wrong Sequence Number");
                frames.add(vectorhex);
            }
        } else if ((Integer.parseInt(vectorhex[0], 16) & 0xFF) == 4 && vectorhex.length == 1) { //EOT End Of Tx
            tramaOK = true;
            eot = true;
            reqSeq = 0;
            escribir("Fin de la Transmisión");
            frames.add(vectorhex);
        } else if (primerBloque) {
            tramaOK = true;
            primerBloque = false;
            reqSeq = 0;
            escribir("Primer Bloque");
            frames.add(vectorhex);
        } else {
            if (!eot) {
                escribir("Error trama inicio");
            }             
            frames.add(vectorhex);
        }
    }
    
    private String[] cleanConsecutiveDuplicates(String[] arrStr, String str2Find) {
        ArrayList<String> cleanFrameArrL = new ArrayList<>();
        for (int i = 0; i < arrStr.length; i++) {
            cleanFrameArrL.add(arrStr[i]);
            if (arrStr[i].equalsIgnoreCase(str2Find)) {
                if (arrStr[i].equalsIgnoreCase(arrStr[i + 1])) {
                    i++;
                }
            }
        } 
        String[] cleanFrameArr = new String[cleanFrameArrL.size()];
        cleanFrameArr = cleanFrameArrL.toArray(cleanFrameArr);
        return cleanFrameArr;
    }
    
    private Object[] cleanConsecutiveDuplicates(byte[] arrByte, byte byte2Find, int len) {
        byte[] cleanFrameArr = new byte[arrByte.length];
        Integer newLen = 0;
        for (int i = 0, j = 0; i < len; i++ , j++) {
            cleanFrameArr[j] = arrByte[i];
            newLen ++;
            if (arrByte[i] == byte2Find) {
                if (arrByte[i] == arrByte[i + 1]) {
                    i++;
                }
            }
        } 
        Object[] ans = new Object[]{cleanFrameArr, newLen};        
        return ans;
    }

    private void revisarInicioSession(String[] vectorhexO) {
        String[] answerASCII = vectorhexO;
        String word = "";
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        if (word.contains("Password: ?")) {
            enviaPassword();
        } else {
            escribir("Trama incorrecta");
            handleForwarding("Inicio de Sesión");
        }
    }

    private void enviaPassword() {
        inicioSesion = false;
        contrasena = true;
        System.out.println("Envía autenticación");
        if (lconfHora && cambiaUsuario) {
            byte[] defPwd = tramas.getPasswords().get(user);
            password = med.getPassword2() == null ? tramas.encode(defPwd, defPwd.length) : med.getPassword2();
        }
        //se envia la primera trama de comuniccion             
        byte[] data = concatFrames(password.getBytes(), new byte[]{ 0x0D } );
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Solicitud de Autenticación");
    }

    private void revisarPassword(String[] vectorhexO) {
        String[] answerASCII = vectorhexO;
        String word = "";
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        if (word.contains("Level")) {
            if (lconfHora && cambiaUsuario) {
                contrasena = false;
                stepConfHora = 1;
                enviaConfHora();
            } else {
                enviaDisplaySettings();                
            }            
        } else if (word.contains("Invalid Password")) {
            escribir("Contraseña Errónea.");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion contraseña errónea");            
            escribir("Estado Lectura No leido");
            cerrarLog("Desconexion contraseña errónea", false);
            cerrarPuerto();
            leer = false;
        } else {
            escribir("Respuesta desconocida.");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Respuesta desconocida.");            
            escribir("Estado Lectura No leido");
            cerrarLog("Desconexion Respuesta desconocida.", false);
            cerrarPuerto();
            leer = false;
        }
    }

    private void enviaDisplaySettings() {        
        contrasena = false;
        displaySettings = true;
        primerBloque = true;
        System.out.println("Display Settings");
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getDisplaySetting();
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Display Settings");
    }

    private void revisarDisplaySettings(String[] vectorhexO) {        
        String[] answerASCII = Arrays.copyOfRange(vectorhexO, primerBloque ? 6 : 0, vectorhexO.length);
        String word = "";
        String obtainedSerial;
        primerBloque = false;
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        String flagMsg2Find = "Press RETURN to continue";
        int idxSearchFlag = word.length() - 1 - flagMsg2Find.length();
        if (word.indexOf(flagMsg2Find, idxSearchFlag) != -1) {
            displaySettingsStr += word.substring(0, idxSearchFlag);
            enviaContinue();
        } else {
            displaySettingsStr += word.substring(0, word.length() - 7);
            processDisplaySettings(displaySettingsStr);
            obtainedSerial = displaySettingsMap.get("MID").trim() + " " +  displaySettingsMap.get("TID").trim();
            escribir("Serial obtenido: " + obtainedSerial);
            if (obtainedSerial.equalsIgnoreCase(seriemedidor)) {
                escribir("Serial Correcto");
                enviaLoadProfileSettings();
            } else {
                escribir("Serial incorrecto");                
                escribir("Estado Lectura No leido");
                cerrarLog("Serial incorrecto", false);
                cerrarPuerto();
                leer = false;
            }            
        }
    }

    private void processDisplaySettings(String word) {
        String regex = "([^\\s:=]+)(\\s*:=\\s*)((?:(?!\\s{2}).)*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher;
        // Split the text by newlines and iterate through each line
        String[] lines = word.split("\n");
        System.out.println("" + Arrays.toString(lines));
        for (String line : lines) {
            // Split each line by whitespace
            matcher = pattern.matcher(line);

            while (matcher.find()) {
                String fullMatch = matcher.group(0);
                String[] arrKeyValue = fullMatch.split(":=");
                displaySettingsMap.put(arrKeyValue[0].trim(), arrKeyValue[1].trim());
            }
        }
        // Print the parameters and their values
        for (String key : displaySettingsMap.keySet()) {
            escribir(key + " => " + displaySettingsMap.get(key));
        }
    }

    private void enviaContinue() {        
        byte[] data = CR;
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> NextPacket");
    }

    private void enviaLoadProfileSettings() {        
        displaySettings = false;
        loadProfileSettings = true;
        primerBloque = true;
        inTx = false;
        reqSeq = 0;
        System.out.println("Load Profile Settings 1");
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getLoadProfileSettings();
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Load Profile Settings 1");
    }

    private void revisarLoadProfileSettings_1(String[] vectorhexO) {
        revisaTrama(vectorhexO);
        String[] answerASCII = vectorhexO;
        String word = "";
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        if (!eot) {            
            if (!inTx && word.contains("Ready to send file")) {
                enviaC(1);
                return;
            }
            if (!inTx && word.contains("Transfer Aborted")) {                
                enviaLoadProfileSettings_R();
                return;
            }
        } else {
            if (!inTx && word.contains("Transfer Complete")) {                                
                eot = false;                
                enviaLoadProfileSettings_R();
                return;
            }
        }

        if (tramaOK && !delayed) {
            if (!inTx && word.contains("SET_1.TXT")) {                
                enviaC(2);
                reqSeq++;
                inTx = true;
            } else if (inTx && !eot) {                
                loadProfileSettingsStr += word.substring(3, word.length() - 2);
                enviaC(3);
                reqSeq++;
            } else if (inTx && eot) {
                processLoadProfileSettings(loadProfileSettingsStr, false);                
                enviaC(2);
                inTx = false;
            } else if (eot) {
                enviaC(3);                
            } else {
                escribir("Respuesta inesperada");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Respuesta inesperada - Load Prfoile Settings");                
                escribir("Estado Lectura No leido");
                cerrarLog("Respuesta inesperada - Load Prfoile Settings", false);
                cerrarPuerto();
                leer = false;
            }
        } else {
            escribir("Trama incorrecta");
            handleReject();
        }
    }

    private void enviaC(int opc) {
        byte[] data;
        switch (opc) {
            case 1:
                data = C;
                break;
            case 2:
                data = this.concatFrames(ackB, C);
                break;
            case 3:
                data = ackB;
                break;
            default:
                escribir("Opción inesperada en envío tipo de continue para Tx de archivo. Revisar");
                return;
        }
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Continue");
    }

    private void enviaLoadProfileSettings_R() {        
        loadProfileSettings = false;
        loadProfileSettingsR = true;
        primerBloque = true;
        inTx = false;
        reqSeq = 0;
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getLoadProfileSettingsR();
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Load Profile Settings R");
    }

    private void revisarLoadProfileSettings_R(String[] vectorhexO) {
        revisaTrama(vectorhexO);
        String[] answerASCII = vectorhexO;
        String word = "";
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        if (!eot) {
            if (!inTx && word.contains("Ready to send file")) {
                enviaC(1);
                return;
            }
            if (!inTx && word.contains("Transfer Aborted")) {                
                enviaFecha();
                return;
            }
            
        } else {
            if (!inTx && word.contains("Transfer Complete")) {
                eot = false;                
                enviaFecha();
                return;
            }
        }

        if (tramaOK && !delayed) {
            if (!inTx && word.contains("SET_R.TXT")) {                
                enviaC(2);
                reqSeq++;
                inTx = true;
            } else if (inTx && !eot) {                
                loadProfileSettingsRStr += word.substring(3, word.length() - 2);
                enviaC(3);
                reqSeq++;
            } else if (inTx && eot) {
                processLoadProfileSettings(loadProfileSettingsRStr, true);                
                enviaC(2);
                inTx = false;
            } else if (eot) {
                enviaC(3);
            } else {
                escribir("Respuesta inesperada");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Respuesta inesperada - Load Prfoile Settings");                
                escribir("Estado Lectura No leido");
                cerrarLog("Respuesta inesperada - Load Prfoile Settings", false);
                cerrarPuerto();
                leer = false;
            }
        } else {
            escribir("Trama incorrecta");
            handleReject();
        }
    }

    private void processLoadProfileSettings(String word, boolean first) {
        String regex = "([A-Z\\d_-]*,\\\".+\\\")|([A-Z\\d_-]*\\=.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher;
        // Split the text by newlines and iterate through each line
        String[] lines = word.split("\n");
        System.out.println("" + Arrays.toString(lines));
        for (String line : lines) {
            // Split each line by whitespace
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                String fullMatch = matcher.group(0);
                String[] arrKeyValue = fullMatch.split("[=,]");
                if (first) {
                    loadProfileSettings_1.put(arrKeyValue[0].trim(), arrKeyValue[1].trim().replace("\"", ""));
                } else {
                    loadProfileSettings_R.put(arrKeyValue[0].trim(), arrKeyValue[1].trim().replace("\"", ""));
                }
            }
        }
        // Print the parameters and their values
        for (String key : first ? loadProfileSettings_1.keySet() : loadProfileSettings_R.keySet()) {
            escribir(key + " => " + (first ? loadProfileSettings_1.get(key) : loadProfileSettings_R.get(key)));
        }
    }

    private void enviaFecha() {        
        loadProfileSettingsR = false;
        date = true;
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getDate();
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Date");
    }

    private void revisarFecha(String[] vectorhexO) {
        fechaMed = extractData(vectorhexO).replace("\n", "").replace("\r", "");
        enviaHora();
    }

    private void enviaHora() {        
        date = false;
        time = true;
        realTime = true;
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getTime();
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Time");
    }

    private void revisarHora(String[] vectorhexO) {
        realTime = false;
        String word[] = extractData(vectorhexO).split("MINSM");
        horaMed = word[0].trim();
        fechaCompletaMed = fechaMed + " " + horaMed;
        System.out.println("Fecha medidor: " + fechaCompletaMed);
        if (!medidorDesfasado(fechaCompletaMed)) {
            if (leventos) {
                //Método Eventos
                enviaEvents();
            } else if (lperfil) {
                perfilIncompleto = true;
                enviaLoadProfile();
            } else {
                cerrarPuerto();
                escribir("Ninguna opción seleccionada");
                cerrarLog("Leido", true);
                leer = false;
            }
        } else if (lconfHora) {
            //Método para actualizar la fecha del medidor
            time = false;
            user = 1;
            cambiaUsuario();                
        }   else {            
            cerrarPuerto();
            escribir("Desconexion Error por Desfase");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error por Desfase");
            cerrarLog("Desconexion Error por Desfase", false);
            leer = false;            
        }
    }

    private boolean medidorDesfasado(String fechaHoraMed) {
        boolean desfasado = false;
        try {
            deltatimesync2 = obtenerHora();
            escribir("Fecha Colombia: " + deltatimesync2);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(deltatimesync2.getTime());
            int month = cal.get(Calendar.MONTH);
            int firstD = Integer.parseInt( fechaHoraMed.substring(0, 2));
            int secondD = Integer.parseInt( fechaHoraMed.substring(3, 5));
            Timestamp fechaActualTS;
            if ( firstD > 12) {
                fechaActualTS = new Timestamp(sdf7.parse(fechaHoraMed).getTime());
            } else if (secondD > 12) {
                fechaActualTS = new Timestamp(sdf5.parse(fechaHoraMed).getTime());
            } else {                                
                if (month == firstD) {
                    fechaActualTS = new Timestamp(sdf5.parse(fechaHoraMed).getTime());
                } else {
                    fechaActualTS = new Timestamp(sdf7.parse(fechaHoraMed).getTime());
                }
            }
            escribir("Fecha del medidor: " + fechaActualTS);
            desfase = (obtenerHora().getTime() - (fechaActualTS.getTime() + ((int) (deltatimesync2.getTime() - deltatimesync1.getTime())))) / 1000;
            escribir("Desfase calculado en segundos: " + desfase);
            if (Math.abs(desfase) > ndesfasepermitido) {
                escribir("No se solicitara el perfil de carga");
                desfasado = true;
            }
        } catch (Exception e) {
            escribir("Error al calcular desfase, por tanto no se continuará.");
            escribir(getErrorString(e.getStackTrace(), 3));
            desfasado = true;
        }
        try {
            cp.actualizaDesfase(desfase, med.getnSerie());
        } catch (Exception e) {
            escribir("Error al actualizar desfase en DB.");
            escribir(getErrorString(e.getStackTrace(), 3));
        }
        return desfasado;
    }

    private void enviaLoadProfile() {        
        time = false;
        events = false;
        loadProfile = true;
        primerBloque = true;
        inTx = false;
        reqSeq = 0;
        byte[] data = tramas.getLoadProfile();
        String fechaIni = sdf6.format(new Date(med.getFecha().getTime())) + ":00";
        String fechaFin = sdf6.format(new Date(obtenerHora().getTime())) + ":00";
        //FECHA INICIAL
        //Mes
        data[23] = (byte) fechaIni.charAt(0);
        data[24] = (byte) fechaIni.charAt(1);
        //Día
        data[26] = (byte) fechaIni.charAt(3);
        data[27] = (byte) fechaIni.charAt(4);
        //Año
        data[29] = (byte) fechaIni.charAt(6);
        data[30] = (byte) fechaIni.charAt(7);
        data[31] = (byte) fechaIni.charAt(8);
        data[32] = (byte) fechaIni.charAt(9);
        //Hora
        data[34] = (byte) fechaIni.charAt(11);
        data[35] = (byte) fechaIni.charAt(12);
        //Minutos
        data[37] = (byte) fechaIni.charAt(14);
        data[38] = (byte) fechaIni.charAt(15);
        //Segundos
        data[40] = (byte) fechaIni.charAt(17);
        data[41] = (byte) fechaIni.charAt(18);
        //FECHA FINAL
        //Mes
        data[43] = (byte) fechaFin.charAt(0);
        data[44] = (byte) fechaFin.charAt(1);
        //Día
        data[46] = (byte) fechaFin.charAt(3);
        data[47] = (byte) fechaFin.charAt(4);
        //Año
        data[49] = (byte) fechaFin.charAt(6);
        data[50] = (byte) fechaFin.charAt(7);
        data[51] = (byte) fechaFin.charAt(8);
        data[52] = (byte) fechaFin.charAt(9);
        //Hora
        data[54] = (byte) fechaFin.charAt(11);
        data[55] = (byte) fechaFin.charAt(12);
        //Minutos
        data[57] = (byte) fechaFin.charAt(14);
        data[58] = (byte) fechaFin.charAt(15);
        //Segundos
        data[60] = (byte) fechaFin.charAt(17);
        data[61] = (byte) fechaFin.charAt(18);
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Load Profile");
    }

    private void revisarLoadProfile(String[] vectorhexO) throws Exception {
        revisaTrama(vectorhexO);
        String[] answerASCII = vectorhexO;
        String word = "";        
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        if (!eot) {
            if (!inTx && word.contains("Ready to send file")) {
                enviaC(1);
                return;
            }
        } else {
            if (!inTx && word.contains("Transfer Complete")) {
                loadProfile = false;
                cerrarPuerto();
                escribir("Perfil Completado");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Intercambio de datos con el medidor OK");                
                cp.AlmacenaPerfil_SEL735(med.getnSerie(), consKE_Map, tipoCanal_Map, channelsCode, med.getFecha(), dataLDP, intPeriod, file);                
                leer = false;
                return;
            }
        }
        if (tramaOK && !delayed) {
            if (!inTx && word.contains("LDP1_DATA.BIN")) {                
                enviaC(2);
                reqSeq++;
                inTx = true;
            } else if (inTx && !eot) {                
                loadProfileData.addAll(Arrays.asList(Arrays.copyOfRange(vectorhexO, 3, vectorhexO.length - 2)));
                enviaC(3);
                reqSeq++;
            } else if (inTx && eot) {
                String[] dataArr = new String[loadProfileData.size()];
                loadProfileData.toArray(dataArr);
                processLoadProfileData(dataArr);                
                enviaC(2);
                inTx = false;
            } else if (eot) {
                enviaC(3);
            } else {
                escribir("Respuesta inesperada");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Respuesta inesperada - Load Prfoile Data");                
                escribir("Estado Lectura No leido");
                cerrarLog("Respuesta inesperada - Load Prfoile Data", false);
                cerrarPuerto();
                leer = false;
            }

        } else {
            escribir("Trama incorrecta");
            handleReject();
        }
    }

    private void processLoadProfileData(String[] data) {
        String recordType = "";
        int lengthData;
        String[] payload = {};
        long checksum;
        for (int i = 0; i < data.length - 1; i++) {
            try {
                recordType = data[i] + data[i + 1];
                escribir("Record Type: " + recordType);
                i += 2;
                lengthData = Integer.parseInt(data[i] + data[i + 1], 16) & 0xFFFF;
                escribir("Payload length: " + lengthData);
                i += 2;
                payload = Arrays.copyOfRange(data, i, i + lengthData - 2);
                i += lengthData - 2;
                checksum = Long.parseLong(data[i] + data[i + 1], 16) & 0xFFFF;
                escribir("Checksum founded: " + checksum);
                i += 1;
                if (checksum != (generaChecksum(payload) + 1)) {
                    escribir("Checksum incorrecto");
                    break;
                }
                sortOut(recordType, payload);
            } catch (IndexOutOfBoundsException iobE ) {
                escribir("Data Insuficiente");
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
    }

    private String extractData(String[] vectorhex) {
        boolean extract = false;
        String data = "";
        for (String ch : vectorhex) {
            if ((Integer.parseInt(ch, 16) & 0xFF) == 2) {
                extract = true;
                continue;
            }
            if (extract) {
                if ((Integer.parseInt(ch, 16) & 0xFF) == 3) {
                    break;
                }
                char asciiChar = (char) Integer.parseInt(ch, 16);
                data += asciiChar;
            }
        }
        return data;
    }

    private long generaChecksum(String[] trama) {
        long checkSum = 0;
        for (String _byte : trama) {
            checkSum += (Integer.parseInt(_byte, 16) & 0xFF);
        }
        checkSum = checkSum & 0xFFFF;
        System.out.println("Checksum calculated: " + checkSum);
        return checkSum;
    }

    private void sortOut(String recordType, String[] data) {
        switch (Integer.parseInt(recordType, 16) & 0xFF) {
            case 100:
                processTable0064(data);
                break;
            case 102:
                processTable0066(data);
                break;
            case 103:
                processTable0067(data);
                break;
            default:
                break;
        }
    }

    public void processTable0064(String[] data) { //Meter Configuration
        int currentYear = Integer.parseInt(data[0] + data[1], 16) & 0xFFFF;
        int currentJulienDay = Integer.parseInt(data[2] + data[3], 16) & 0xFFFF;
        long tenthsMilisSinceMidnight = Long.parseLong(data[4] + data[5] + data[6] + data[7], 16) & 0xFFFFFFFF;
        Timestamp currentTimestamp = buildTimestamp(currentYear, currentJulienDay, tenthsMilisSinceMidnight);
        escribir("Fecha actual Medidor: " + currentTimestamp);
        try {
            float PTR = (float) ieeeToFloat(data[33] + data[34] + data[35] + data[36]);
            escribir("PTR: " + PTR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            float CTR = (float) ieeeToFloat(data[41] + data[42] + data[43] + data[44]);
            escribir("CTR: " + CTR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        boolean PRI_SCA = (Integer.parseInt(data[125], 16) & 0xFF) != 0;
        escribir("PRI_SCA: " + PRI_SCA);

        int firstLDPRec_Year = Integer.parseInt(data[129] + data[130], 16) & 0xFFFF;
        int firstLDPRec_JulienDay = Integer.parseInt(data[131] + data[132], 16) & 0xFFFF;
        long tenthsMilisSinceMidnight_FR = Long.parseLong(data[133] + data[134] + data[135] + data[136], 16) & 0xFFFFFFFF;
        Timestamp firstLDPRec_Timestamp = buildTimestamp(firstLDPRec_Year, firstLDPRec_JulienDay, tenthsMilisSinceMidnight_FR);
        escribir("Fecha primer registro de Perfil de Carga: " + firstLDPRec_Timestamp);

        int lastLDPRec_Year = Integer.parseInt(data[137] + data[138], 16) & 0xFFFF;
        int lastLDPRec_JulienDay = Integer.parseInt(data[139] + data[140], 16) & 0xFFFF;
        long tenthsMilisSinceMidnight_LR = Long.parseLong(data[141] + data[142] + data[143] + data[144], 16) & 0xFFFFFFFF;
        Timestamp lastLDPRec_Timestamp = buildTimestamp(lastLDPRec_Year, lastLDPRec_JulienDay, tenthsMilisSinceMidnight_LR);
        escribir("Fecha último registro de Perfil de Carga: " + lastLDPRec_Timestamp);

        long availableLDPRecords = Long.parseLong(data[145] + data[146] + data[147] + data[148], 16) & 0xFFFFFFFF;
        escribir("Número de registro de Perfil de Carga disponibles o dentro del rango de fechas: " + availableLDPRecords);

        int interval = (Integer.parseInt(data[149] + data[150], 16) & 0xFFFF) / 60;
        intPeriod = interval;
        escribir("Periodo de Integración: " + interval);

        nCanales = Integer.parseInt(data[151] + data[152], 16) & 0xFFFF;
        escribir("Canales habilitados: " + nCanales);

        boolean zeroCond;
        boolean doubleZeroCond = false;
        int idxData = 153;
        int state = 0, counterChannel = 0;
        String word = "";
        //System.out.println("Longitud data: " + data.length);
        while (data.length > idxData) {
            while (!doubleZeroCond && counterChannel < nCanales) {
                //System.out.println("Index Data: " + idxData);
                //System.out.println("Counter Channel: " + counterChannel);
                if (Integer.parseInt(data[idxData], 16) == 0 && state == 0) {
                    zeroCond = true;
                    channelsName.add(word);
                    word = "";
                    idxData++;
                } else if (Integer.parseInt(data[idxData], 16) == 0 && state == 1) {
                    zeroCond = true;
                    channelsFunction.add(word);
                    word = "";
                    idxData++;
                } else if (state == 2) {
                    zeroCond = false;
                    int type = Integer.parseInt(data[idxData] + data[idxData + 1] + data[idxData + 2] + data[idxData + 3], 16) & 0xFFFFFFFF;
                    channelsType.add(type);
                    counterChannel++;
                    idxData += 4;
                } else if (state == 3) {
                    zeroCond = false;
                    try {
                        double mult = ieeeToFloat(data[idxData] + data[idxData + 1] + data[idxData + 2] + data[idxData + 3]);
                        channelsMultiplier.add(mult);
                        counterChannel++;
                        idxData += 4;
                    } catch (Exception ex) {
                        channelsMultiplier.add(0.0);
                        counterChannel++;
                        idxData += 4;
                        ex.printStackTrace();
                    }
                } else if (state == 4) {
                    zeroCond = false;
                    try {
                        double scalar = ieeeToFloat(data[idxData] + data[idxData + 1] + data[idxData + 2] + data[idxData + 3]);
                        channelsScalar.add(scalar);
                        counterChannel++;
                        idxData += 4;
                    } catch (Exception ex) {
                        channelsScalar.add(0.0);
                        counterChannel++;
                        idxData += 4;
                        ex.printStackTrace();
                    }
                } else {
                    zeroCond = false;
                    word += (char) Integer.parseInt(data[idxData], 16);
                    idxData++;
                }
                //System.out.println("Zero Cond: " + zeroCond);
                doubleZeroCond = (zeroCond && Integer.parseInt(data[idxData], 16) == 0);
                if (doubleZeroCond) {
                    idxData++;
                }
                //System.out.println("Double Zero Cond: " +doubleZeroCond);
            }
            counterChannel = 0;
            doubleZeroCond = false;
            switch (state) {
                case 0:
                    state = 1;
                    break;
                case 1:
                    state = 2;
                    break;
                case 2:
                    state = 3;
                    break;
                case 3:
                    state = 4;
                    break;
                case 4:
                    escribir("Finaliza procesamiento");
                    break;
                default:
                    escribir("Esto no debería suceder, revisar algoritmo de procesamiento");
            }
        }
        escribir("ChannelName: " + channelsName);

        // Print contents of ArrayList2
        escribir("ChannelFunction: " + channelsFunction);

        // Print contents of ArrayList3
        escribir("ChannelType: " + channelsType);

        // Print contents of ArrayList4
        escribir("ChannelMultiplier: " + channelsMultiplier);

        // Print contents of ArrayList5
        escribir("ChannelScalar: " + channelsScalar);
    }

    public void processTable0066(String[] data) { // Meter Status

    }

    public void processTable0067(String[] data) { //LDP Data
        int idxData = 0, channelCounter = 0;        
        Timestamp recordTimestamp;
        SimpleDateFormat innerSDF = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        while (data.length > idxData) {
            byte status = Byte.parseByte(data[idxData], 16) ;
            recordStatus(status);
            int year = Integer.parseInt(data[idxData + 1] + data[idxData + 2], 16) & 0xFFFF;
            int julienDay = Integer.parseInt(data[idxData + 3] + data[idxData + 4], 16) & 0xFFFF;
            long tenthsMilisSinceMidnight = Long.parseLong(data[idxData + 5] + data[idxData + 6] + data[idxData + 7] + data[idxData + 8], 16) & 0xFFFFFFFF;
            recordTimestamp = buildTimestamp(year, julienDay, tenthsMilisSinceMidnight);
            Timestamp boundedTSRecord;
            try {
                boundedTSRecord = new Timestamp( innerSDF.parse( innerSDF.format( new Date( recordTimestamp.getTime() ) ) ).getTime() );
            } catch (ParseException ex) {
                boundedTSRecord = recordTimestamp;
            }
            double channelVal;
            idxData += 9;
            while (channelCounter < nCanales) {
                if (consKE_Map.get(channelsCode.getOrDefault(channelsName.get(channelCounter), -1)) != null) {
                    try {
                        channelVal = ieeeToFloat(data[idxData] + data[idxData + 1] + data[idxData + 2] + data[idxData + 3]);
                        channelName_Value.put(channelsName.get(channelCounter), channelVal);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        channelVal = 0.0;
                        channelName_Value.put(channelsName.get(channelCounter), channelVal);                        
                    }
                }
                channelCounter++;
                idxData += 4;
            }
            channelCounter = 0;
            dataLDP.put(boundedTSRecord, channelName_Value);
            channelName_Value = new LinkedHashMap<>();
        }
    }

    public Timestamp buildTimestamp(int year, int julienDay, long tenthsMilis) {
        int milis = (int) ((tenthsMilis / 10) % 1000);
        int seconds = (int) ((tenthsMilis / (10 * 1000)) % 60);
        int minutes = (int) ((tenthsMilis / (10 * 1000 * 60)) % 60);
        int hour = (int) (tenthsMilis / (10 * 1000 * 60 * 60));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, julienDay);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, milis);
        return new Timestamp(cal.getTimeInMillis());
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
    
    public byte getBit(byte _byte, int position) {
        return (byte) ((_byte >> position) & 1);
    }

    public void recordStatus(byte status) {
        for ( int i = 0; i < 8; i++ ) {
            if ( getBit(status, i) == 1 ) {
                sortOutStatus(i);
            }
        }   
    }
    
    private void sortOutStatus( int flag ){
        switch ( flag ) {
                case 0:
                    escribir("Daylight-saving time in effect");
                    break;
                case 1:
                    escribir("Power fail within interval (missing data)");
                    break;
                case 2:
                    escribir("Clock reset forward during interval");
                    break;  
                case 3:
                    escribir("Clock reset backwards during interval");
                    break;
                case 4:
                    escribir("Skipped interval (used for invalid or corrupted data)");
                    break;
                case 5:        
                    escribir("TEST mode data");
                    break;
                case 6:
                    escribir("Data Overwrite (data erased during LDP read)");
                default:
                    escribir("Esto no debería suceder, revisar algoritmo de procesamiento");
            }
    }
    
    private void enviaEvents() {
        time = false;
        loadProfile = false;
        events = true;        
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getEvents();
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Eventos");
    }

    private void revisarEvents(String[] vectorhexO) {        
        String[] answerASCII = Arrays.copyOfRange(vectorhexO, primerBloque ? 6 : 0, vectorhexO.length);        
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            eventsStr += asciiChar;
        }
        processEvents(temporalEventList);
        if (temporalEventList.size() > 0) {
            events = false;
            System.out.println("Eventos Completado");
            escribir("Eventos Completado");            
            try {                
                cp.AlmacenaEventos_SEL735(temporalEventList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (lperfil) {
                enviaLoadProfile();
            } else {
                events = false;
                cerrarPuerto();
                try {                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cerrarLog("Leido", true);
                leer = false;
            }
        } else {
            events = false;
            cerrarPuerto();
            try {                
            } catch (Exception e) {
                e.printStackTrace();
            }
            cerrarLog("Leido sin eventos", true);
            leer = false;
        }
    }
    
    private void processEvents(List<ERegistroEvento> eventList) {
        ERegistroEvento eventEntity = new ERegistroEvento();
        SimpleDateFormat sdfEvent = new SimpleDateFormat("MM/dd/yy  HH:MM:ss.SSS");
        String regex = "([\\d]{2}\\/[\\d]{2}\\/[\\d]{2}  [\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3}   )((Power loss)|(Power restored))";;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(eventsStr);
        while (matcher.find()) {
            String eventTimestamp = matcher.group(1).trim();
            if ( !matcher.group(3).isEmpty() ) { //Power loss
                eventEntity.setVcserie(seriemedidor);
                eventEntity.setVctipo("0001");                                
                try {
                    eventEntity.setVcfechacorte(getFechaEventos(eventTimestamp, sdfEvent));
                } catch (ParseException ex) {
                    eventEntity = new ERegistroEvento();
                }
            } else if (!matcher.group(4).isEmpty()) {
                try {
                    //Power restored
                    eventEntity.setVcfechareconexion(getFechaEventos(eventTimestamp, sdfEvent));
                    eventList.add(eventEntity);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }                
                eventEntity = new ERegistroEvento();
            }            
        }             
    }
    
    private Timestamp getFechaEventos(String dateStr, SimpleDateFormat sdfEvent) throws ParseException {
        return new Timestamp(sdfEvent.parse(dateStr).getTime());
    }

    private void cambiaUsuario() {
        cambiaUsuario = true;
        inicioSesion = true;
        System.out.println("Escalamiento a Nivel E");
        //se envia la primera trama de comuniccion
        byte[] data = tramas.getUsers().get(user).clone();
        System.out.println("=> " + tramas.encode(data, data.length));
        ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
        enviaTrama2_2(data, "=> Escalamiento a Nivel E");
    }

    private void enviaConfHora() {
        confHora = true;
        String lfecha;
        Timestamp now = obtenerHora();
        lfecha = sdf.format(new Date(now.getTime()));
        byte data[];
        switch (stepConfHora) {
            case 1:
                toSetDate = lfecha.substring(2, 4)+"/"+lfecha.substring(4, 6)+"/"+lfecha.substring(0, 2);
                data = tramas.getSetDate();
                //Mes
                data[4] = (byte) (Integer.parseInt("" + lfecha.charAt(2)) & 0xFF);
                data[5] = (byte) (Integer.parseInt("" + lfecha.charAt(3)) & 0xFF);
                //Día
                data[7] = (byte) (Integer.parseInt("" + lfecha.charAt(4)) & 0xFF);
                data[8] = (byte) (Integer.parseInt("" + lfecha.charAt(5)) & 0xFF);
                //Año
                data[10] = (byte) (Integer.parseInt("" + lfecha.charAt(0)) & 0xFF);
                data[11] = (byte) (Integer.parseInt("" + lfecha.charAt(1)) & 0xFF);
                System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                enviaTrama2_2(data, "=> Establecer Fecha");
                break;
            case 2:    
                toSetTime = lfecha.substring(6, 12);
                data = tramas.getSetTime();
                //Mes
                data[4] = (byte) (Integer.parseInt("" + lfecha.charAt(6)) & 0xFF);
                data[5] = (byte) (Integer.parseInt("" + lfecha.charAt(7)) & 0xFF);
                //Día
                data[7] = (byte) (Integer.parseInt("" + lfecha.charAt(8)) & 0xFF);
                data[8] = (byte) (Integer.parseInt("" + lfecha.charAt(9)) & 0xFF);
                //Año
                data[10] = (byte) (Integer.parseInt("" + lfecha.charAt(10)) & 0xFF);
                data[11] = (byte) (Integer.parseInt("" + lfecha.charAt(11)) & 0xFF);
                System.out.println("=> " + tramas.encode(data, data.length));
                ultimatramaEnviada = data;//se almacena la ultima trama que se enviara
                enviaTrama2_2(data, "=> Establecer Hora");
                break;   
        }        
    }

    private void revisarConfHora(String[] vectorhexO) {
        String[] answerASCII = vectorhexO;
        String word = "";
        for (String str : answerASCII) {
            char asciiChar = (char) Integer.parseInt(str, 16);
            word += asciiChar;
        }
        if (stepConfHora == 1) {
            if (word.contains("DAT " + toSetDate)) {
                cerrarPuerto();
                escribir("Sincronización Fecha Exitosa");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Sincronización Fecha Exitosa");
                leer = false;
            } else if (word.contains("Invalid")) {
                escribir("Operación no exitosa");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Sincronización Fecha no exitosa");                
                escribir("Estado Lectura No leido");
                cerrarLog("Seteo Fecha no exitoso", false);
                cerrarPuerto();
                leer = false;
            } else {
                escribir("Respuesta desconocida.");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Respuesta desconocida.");                
                escribir("Estado Lectura No leido");
                cerrarLog("Desconexion Respuesta desconocida.", false);
                cerrarPuerto();
                leer = false;
            }
        } else if (stepConfHora == 2) {
            if (word.contains("TIM " + toSetTime)) {
                cerrarPuerto();
                escribir("Sincronización Hora Exitosa");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Sincronización Hora Exitosa");
                leer = false;
            } else if (word.contains("Invalid")) {
                escribir("Operación no exitosa");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Sincronazición Hora no exitosa");                
                escribir("Estado Lectura No leido");
                cerrarLog("Seteo Hora no exitoso", false);
                cerrarPuerto();
                leer = false;
            } else {
                escribir("Respuesta desconocida.");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Respuesta desconocida.");                
                escribir("Estado Lectura No leido");
                cerrarLog("Desconexion Respuesta desconocida.", false);
                cerrarPuerto();
                leer = false;
            }
        }
    }

    private void interpretaCadena(String cadenahex) throws ParseException, Exception {
        try {
            frames = new ArrayList<>();
            escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            //protocolo 
            if (inicioSesion) {//inicio de session y envio de passsword
                revisarInicioSession(vectorhex);
            } else if (contrasena) {
                revisarPassword(vectorhex);
            } else if (displaySettings) {
                revisarDisplaySettings(vectorhex);
            } else if (loadProfileSettings) {
                revisarLoadProfileSettings_1(vectorhex);
            } else if (loadProfileSettingsR) {
                revisarLoadProfileSettings_R(vectorhex);
            } else if (date) {
                revisarFecha(vectorhex);
            } else if (time) {
                revisarHora(vectorhex);
            } else if (loadProfile) {
                revisarLoadProfile(vectorhex);
            } else if (events) {
                revisarEvents(vectorhex);
            } else if (confHora) {
                revisarConfHora(vectorhex);
            } else {
                //cierra el puerto
                escribir("Error de recepcion");
                cerrarPuerto();
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error recepcion");
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
            e.printStackTrace();
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
                                if (loadProfileData.size() > 0) {
                                    String[] dataArr = new String[loadProfileData.size()];
                                    loadProfileData.toArray(dataArr);
                                    processLoadProfileData(dataArr);
                                    if ( !dataLDP.isEmpty() ) {
                                        cp.AlmacenaPerfil_SEL735(med.getnSerie(), consKE_Map, tipoCanal_Map, channelsCode, med.getFecha(), dataLDP, intPeriod, file);                    
                                    }                                                                    }
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

    private static boolean validaCRC16(String[] dataR) {
        byte[] crcB = new byte[2];
        byte[] crcData = {(byte) (Integer.parseInt(dataR[dataR.length - 2], 16) & 0xFF), (byte) (Integer.parseInt(dataR[dataR.length - 1], 16) & 0xFF)};
        int crc = 0x0000;
        int poly = 0x1021;
        for (int i = 3; i < dataR.length - 2; i++) {
            int _byte = Integer.parseInt(dataR[i], 16) & 0xFF;
            for (int j = 0; j < 8; j++) {
                boolean bit = ((_byte >> (7 - j) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= poly;
                }
            }
        }
        crc &= 0xFFFF;        
        crcB[1] = (byte) (crc & 0xFF);
        crcB[0] = (byte) ((crc >> 8) & 0xFF);
        System.out.println("" + crcB[0] + "," + crcB[1]);
        return (crcB[0] == crcData[0] && crcB[1] == crcData[1]);
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
    
    private void handleReject() {
        if (intentosReenvioLastFrame < numeroReintentos) {
            intentosReenvioLastFrame++;
            if (delayed) {
                enviaTrama2_2(sendNothing, "Espera tramas retrasadas");//No envía nada
            } else {
                enviaTrama2_2(nackB, "NACK");//Envia nack
            }
        } else {
            intentosReenvioLastFrame = 0;
            escribir("Numero de reintentos agotado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado - Load Profile Settings");
            escribir("Estado Lectura No leido");
            cerrarLog("Desconexion Numero de reintentos agotado", false);
            cerrarPuerto();
            leer = false;
        }
    }

    private void handleForwarding(String peticion) {
        if (intentosReenvioLastFrame < numeroReintentos) {
            intentosReenvioLastFrame++;
            enviaTrama2_2(ultimatramaEnviada, "Reenvía " + peticion);
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
