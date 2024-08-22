/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaHexing;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.ERegistro;
import Entidades.ERegistroEvento;
import Entidades.Electura;
import Entidades.EtipoCanal;
import Util.SynHoraNTP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.sql.SQLException;
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
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static protocolos.LeerRemotoTCPTecunly.calcularnuevocrcI;

/**
 *
 * @author Lenovo
 */
public class LeerRemotoTCPHexing extends Thread {
    //variables constructor
    EMedidor med;
    ControlProcesos cp;
    private String usuario = "admin";
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    File file;
    boolean existearchivo = false;
    boolean lperfil; //       Booleanos 
    boolean leventos; //      checkbox 
    boolean lregistros; //       de
    boolean lconfhora; //    Telesimex
    boolean aviso = false;
    int indx = 0;
    Abortar objabortar;
    //variables para obtención de parámetros del medidor en jinit
    private int indxuser = 0;
    private int indxUsrGPRS = 0;
    int tipoconexion; //identificar comunicación vía GPRS interno
    String numeroPuerto;
    int numeroReintentos = 3;
    int ReintentoFRMR = 0; //  Manejo de reintentos en
    int ReinicioFRMR = 0; //   validatipotrama por FRMR
    int ReintentoRLRQ = 0;
    int velocidadPuerto;
    long timeout;
    int ndias;
    int diasaleer = 0;
    int ndiaReg;
    int nmesReg;
    int ndiaseventos;
    String password = "";
    String password2 = "";
    String seriemedidor = "";
    int numcanales = 2;
    int dirfis = 1;
    int dirlog = 1;
    boolean setdirfis = false;//dirfis
    int numBytesDir = 1; //número de bytes para la dirección del medidor
    int users[] = {1}; //dirección origen - administrador para Hexing es "1" (solo para HLS) - Reading "2" (solo para LLS)
    boolean lPhyAdrs = false;
    int InvokeIDandParity = 193; //0xC1 := 193
    String[] wrapperGPRS = {""}; //Wrapper GPRS para Hexing
    private long desfase;
    long ndesfasepermitido = 7200;
    Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    ArrayList<EtipoCanal> vtipocanal;
    long tiempo = 500;    
    String tiporegistros = "0";
    ArrayList<EtipoCanal> vtiporegistros; 
    Vector<Electura> vlec;
//    ArrayList<ERegistro> vreg;
    //Variables en abrepuerto
    private int reintentoconexion = -1;
    int reinicio = 0;
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Socket socket;
    private volatile boolean escucha = true;
    Thread tLectura;
    boolean portconect = false;
    InputStream input;
    OutputStream output;
    public boolean leer = true;
    //variables en escribir
    Date d = new Date();
    RandomAccessFile fr;
    //variable en escucha
    //variables en procesacadena
    String cadenahex = "";
    TramasRemotaHexing tramas = new TramasRemotaHexing();
    //variables en iniciacom
    long tiemporetransmision = 0;
    byte[] ultimatramaEnviada = null;
    //variables para control de procesamiento de respuestas
    boolean lfechasync = false;
    boolean lSNRMUA = false;
    boolean lARRQ = false;
    boolean lserialnumber = false;
    boolean lfechaactual = false;
    boolean lfechaactual2 = false;
    boolean lperiodoIntegracion = false;
    //boolean lentradasenuso = false; 
    boolean linfoperfil = false;
    boolean lconstants = false;
    boolean lpowerLost = false;
    boolean lphyaddress = false;
    boolean lperfilcarga = false;
    boolean lregis = false; //registros
    boolean regisdia = false; //registros diarios
    boolean regismes = false; //registros mensuales
    boolean lRelReq = false;
    boolean lprecierre = false; //antes lterminar
    boolean lcierrapuertos = false;
    boolean lReset = false;
    boolean prereset = false;
    boolean enviando = false;
    boolean reenviando = false;
    //variables en enviatramas
    Thread tEscritura = null;
    Thread tReinicio = null;
    public boolean cierrapuerto = false;
    //variables control de tramas
    int ns = 0;
    int nr = 0;
    int nrEsperado = 0;
    int nsEsperado = 0;
    //variables para interpretación y procesamiento de datos
    public short pila[] = new short[10];
    public int i1, j1, l1;
    int bytesRecortados = 0;
    int bytesRecortadosPerfil = 0;
    int bytesRecortadosRegistros = 0;
    int recortaFormato, recortaFormatoE, recortaFormatoP, recortaFormatoR = 0;
    String[] vectorDatarecibido = new String[0];
    String datoserial = "";
    SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Date fechaActual;
    private int periodoIntegracion = 15;
    int intervalo = 0;
    Vector<String> infoPerfil;          // variables generales
    ArrayList<String> obis;             //    para
    ArrayList<String> conske;           // infoperfil
    ArrayList<String> clase;            //     = 
    ArrayList<String> unidad;           //  canales
    //Específicos
    ArrayList<String> obisPerfil;       //    Variables
    ArrayList<String> conskePerfil;     //      para
    ArrayList<String> clasePerfil;      //   canales de
    ArrayList<String> unidadPerfil;     //  Perfil de carga
    int intervaloRdia = 0;
    ArrayList<String> obisRdia;         //    Variables
    ArrayList<String> conskeRdia;       //      para
    ArrayList<String> claseRdia;        //   canales de
    ArrayList<String> unidadRdia;       //  Registros diarios
    int intervaloRmes = 0;
    ArrayList<String> obisRmes;         //    Variables
    ArrayList<String> conskeRmes;       //      para
    ArrayList<String> claseRmes;        //   canales de
    ArrayList<String> unidadRmes;       //  Registros mensuales
    int indexConstant = 0;
    private boolean primerbloque;
    public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
    ERegistroEvento regEvento;
    Date fechaEvento = null;
    List<ERegistroEvento> listRegEventos;
    public int indicecanal = 0;
    Timestamp fechaintervalo = null;
    Timestamp ultimoIntervalo = null;
    Timestamp fechaCero = null;
    EConstanteKE econske = null;
    String canal = "";
    Electura lec;
    boolean constanteOk = false;
    //control revision de tramas
    public boolean tramaOK = false;
    private int indxLength = 2;
    private int indxControl = indxLength + numBytesDir + 2;
    private int indxhcs1 = indxControl + 1;
    private int indxhcs2 = indxhcs1 + 1;
    private int indxSacarDatos = indxhcs2 + 1; //Desde donde inicia el E6E600
    private int indxData = indxSacarDatos + 3;
    int ReintentoRevisionTrama = 0;
    //variables para fecha y hora
    
    
    private ZoneId zid;
    private static String zona;
    Timestamp time = null; //tiempo de NTP
    Timestamp tsfechaactual;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM/dd HH");
    SimpleDateFormat sdfid = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat sdfih = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat sdfd = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sdfh = new SimpleDateFormat("HHmmss");
    SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
    //variables para obtención de datos (perfil, registros, eventos)
    List<String> listEventos; //No se usa
    ArrayList<String> vRegistros;
    ArrayList<String> vRegistrosdia;
    ArrayList<String> vRegistrosmes;
    ArrayList<String> vPerfilCarga;
    ArrayList<ERegistro> vreg;
    ERegistro reg;
    boolean procesaIncompleto = false; //para casos de errores en procesamiento de las peticiones principales
    boolean procesaIncompletoPerfil = false;
    boolean procesaIncompletoEventos = false;
    boolean procesaIncompletoRegistros = false;
    int registrosprocesados, registrosprocesadosE = 0;
    int registrosAprocesar = 0;
    public boolean primerintervalo; 
    Timestamp fprimerintervalo;
    ArrayList<Double> lecInterval = new ArrayList<>();
    public int indxlec = 0;
    boolean clockSincronizado = false;
    //variables adicionales para control de envío de peticiones
    boolean solicitar;
    boolean completabloque = false;
    boolean concatena = false;
    boolean bloqueincorrecto = false;
    byte[] bloqueRecibidoAnt = null;
    int bloqueRecibidoInt = 0;
    int reintentoBloque = 0;
    //Distición entre Hexings
    String sOBISPerfil="0001180300FF";
    int clasePerfilInt=7;
    String sOBISRegistro="0000620100FF";
    private final Object monitor = new Object();
//    String sOBISEventos="0000636200FF";
    private final String label = "LeerTCPHexingWrapper";
    
    public LeerRemotoTCPHexing(EMedidor med, boolean perfil, boolean eventos, boolean registros, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase){
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
            indxuser = 0;
            indxUsrGPRS = 0;
            tipoconexion = med.getTipoconexion();
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            //System.out.println("Número de reintentos :"+numeroReintentos);
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            password2 = med.getPassword2();
            timeout = med.getTimeout() * 1000;
            ndias = med.getNdias();
            ndiaReg = med.getNdiasreg();
            nmesReg = med.getNmesreg();
            ndiaseventos = med.getNdiaseventos();
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "17" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "1" : med.getDireccionLogica()));
            users[0] = Integer.parseInt((med.getDireccionCliente() == null ? "2" : med.getDireccionCliente()));
            wrapperGPRS[0] = "000100" + ((byte) ((users[0] >> 4) & 0x0F)) + "" + ((byte) (users[0] & 0x0F)) + "0001";
            numBytesDir = med.getBytesdireccion().equals("5") ? 1 : Integer.parseInt(med.getBytesdireccion());//5 implica que llegó null
            numBytesDir = tipoconexion == 0 ? 4 : numBytesDir;
            indxControl = indxLength + numBytesDir + 2;
            indxhcs1 = indxControl + 1;
            indxhcs2 = indxhcs1 + 1;
            indxSacarDatos = indxhcs2 + 1; //Desde donde inicia el E6E600
            indxData = indxSacarDatos + 3;
            lconske = cp.buscarConstantesKeLong(med.getnSerie());//se toman los valores de las constantes 
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            indxData = (tipoconexion == 0 ? 8 : indxData); //Index Data para GPRS interno
            abrePuerto();
            tiempo = 1000; //Escucha
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
    
    private void escribir(String dato) {
        try {
            if (file != null) {
                d = Calendar.getInstance().getTime();
                fr = new RandomAccessFile(file, "rw");
                fr.seek(fr.length());
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write(dato.trim().getBytes(), 0, dato.trim().getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    
    public void reiniciaComunicacion(boolean sendBye) {
        try {
            escribir("Reinicia Comunicacion");
            cerrarPuerto(sendBye);
            Thread.sleep(1000);
            if (reintentoconexion <= numeroReintentos) {
                if (!objabortar.labortar) {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion reinicio de comunicacion");
                    tReinicio = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
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
    
    private void iniciacomunicacion() throws Exception {
        tiemporetransmision = timeout*1000; //vic 06-08-19
        lfechasync = false;
        InvokeIDandParity = 193;
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
                if (tipoconexion == 0){
                    lSNRMUA = false;
                    lARRQ = true;
//                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label,  "Interpreta Trama SNRM - Correcto");
                    byte trama[] = crearAARQ(tramas.getAarq(), password); //AARQ 21-10-19
                    trama = construirTramaGPRS(trama);
                    ultimatramaEnviada = trama;                    
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label,  "Envío petición - AARQ");
                    enviaTrama2(trama, "AARQ");
                } else {
                    lSNRMUA = true;
                    byte[] trama = tramas.getSnrm2();
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcI(trama);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "SNRM");
                }
            } else {
                interrumpirHilo(tLectura);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
                cerrarPuerto(false);
                cerrarLog("Medidor no configurado", true);
                leer = false;
          }
        }
    }
  
    private void enviaTrama2(byte[] bytes, String descripcion) {
            
        final byte[] trama = bytes;
        final String des = descripcion;   
        enviando = true;
        tEscritura = new Thread() {
            @Override
            public void run() {
                synchronized(monitor){                    
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
                                    escribir("Error enviando trama tipo 2_2");
                                }
                            } else {
                                t = false;
                            }
                            if (reenviando && intentosRetransmision <= 2){
                            intentosRetransmision ++;                                                        
                            } else if (reenviando && intentosRetransmision > 2) {
                                interrumpirHilo(tLectura);
                                escribir("Numero de reenvios agotado");
                                enviando = false;
                                t = false;
                                cierrapuerto = true;
                            } else{
                                return;                            
                            }                              
                        }
                        if (cierrapuerto) {
                            cierrapuerto = false;
                            cerrarPuerto(true);
                            monitor.notifyAll();
                            escribir("Inicia procesamiento de contingencia en caso de datos a procesar");
                            almacenaDatos();                                
                            if (reintentoconexion <= numeroReintentos) {
                                if (!objabortar.labortar) {
                                    if(lSNRMUA){
                                        dirfis=65535;
                                        lPhyAdrs = true;
                                    }
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
                        cerrarPuerto(true);
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
    
    private void enviaTrama(byte[] bytes) {
        try {
            if (bytes[0] != (byte) 0x00) {
                output.write(bytes, 0, bytes.length);
                output.flush();
            } else {
                escribir("Es necesario sensar el buffer de escucha nuevamente para completar trama o para capturar tramas desfasadas");
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }
    
    public void cerrarPuerto(boolean sendBye) {
        if (sendBye) {
            byte trama[] = tipoconexion == 0 ? tramas.getPrelogout() : tramas.getLogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            escribir("Envia " + (tipoconexion == 0 ? "PreLogout":"Logout") + " sin espera de respuesta");
            escribir("=> " + tramas.encode(trama, trama.length));
            enviaTrama(trama);
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
    
    private void procesaCadena() throws InterruptedException {
        byte[] readBuffer = new byte[2047];
        byte[] auxBuffer = new byte[2047];
        int idxFrame = 0;
        int numbytes;
        byte begin, end;
        byte[] beginWrapper = new byte[6];
        byte[] beginWrapperC = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01};
        if (!lRelReq) {
            beginWrapperC[5] = (byte) users[0];
        }
        //System.out.println("Wrapper de Comparación: "+tramas.encode(beginWrapperC, beginWrapperC.length));
        boolean uncomplete = true;
        boolean beginOk = false;
        boolean endOk;
        boolean frameLengthOk = false;
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
                    if (tipoconexion == 2) {//HDLC - DLMS
                        if (idxFrame == 0) {
                            begin = readBuffer[0];
                            beginOk = begin == 126;
                        }
                        end = readBuffer[numbytes - 1];
                        endOk = end == 126;
                        if (numbytes >= 3 && idxFrame == 0) {
                            int actualFrameLength = (int) (((readBuffer[1] << 8) & 0x0700) + (readBuffer[2] & 0xFF));
                            frameLengthOk = actualFrameLength == numbytes - 2;
                        }
                        if (!beginOk || !endOk || !frameLengthOk) {
                            System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                            idxFrame += numbytes;
                            if (idxFrame >= 3) {
                                int actualFrameLength = (int) (((auxBuffer[1] << 8) & 0x0700) + (auxBuffer[2] & 0xFF));
                                frameLengthOk = actualFrameLength == idxFrame - 2;
                                if (frameLengthOk) {
                                    uncomplete = false;
                                    enviando = false;
                                    reenviando = false;
                                    monitor.notifyAll();
                                    break;
                                }
                            }
                        } else if (idxFrame == 0) {
                            auxBuffer = readBuffer;
                            idxFrame = numbytes;
                            uncomplete = false;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                        } else {
                            uncomplete = false;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                        }
                    } else if (tipoconexion == 0) {// Wrapper
                        int actualFrameLength = 0;
                        if (idxFrame == 0) {
                            if (numbytes >= 6) {
                                System.arraycopy(readBuffer, 0, beginWrapper, 0, 6);
                                beginOk = Arrays.equals(beginWrapper, beginWrapperC);
                                if (numbytes >= 8) {
                                    actualFrameLength = (int) (((readBuffer[6] << 8) & 0x0700) + (readBuffer[7] & 0xFF));
                                    frameLengthOk = actualFrameLength == numbytes - 8;
                                }
                            } else {
                                System.arraycopy(readBuffer, 0, beginWrapper, 0, numbytes);
                            }
                        } else if (idxFrame < 8) {
                            if ((idxFrame + numbytes) >= 6) {
                                int whatsMissing = 6 - idxFrame;
                                System.arraycopy(readBuffer, 0, beginWrapper, idxFrame, whatsMissing);
                                beginOk = Arrays.equals(beginWrapper, beginWrapperC);
                                if ((idxFrame + numbytes) >= 8) {
                                    actualFrameLength = (int) (((readBuffer[whatsMissing + 1] << 8) & 0x0700) + (readBuffer[whatsMissing + 2] & 0xFF));
                                    frameLengthOk = actualFrameLength == (idxFrame + numbytes) - 8;
                                }

                            } else {
                                System.arraycopy(readBuffer, 0, beginWrapper, idxFrame, numbytes);
                            }
                        } else {
                            frameLengthOk = actualFrameLength == (idxFrame + numbytes) - 8;
                        }

                        if (!beginOk || !frameLengthOk) {
                            System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                            idxFrame += numbytes;
                        } else if (idxFrame == 0) {
                            auxBuffer = readBuffer;
                            idxFrame = numbytes;
                            uncomplete = false;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                        } else {
                            System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                            idxFrame += numbytes;
                            uncomplete = false;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                        }
                    }
                }
                if (!socket.isClosed() && uncomplete) {
                    if (tipoconexion == 0 && (lprecierre || lRelReq)) {
                        escribir(lprecierre ? "No hay respuesta al Prelogout" : "No hay respuesta al Release Request");
                        lprecierre = false;
                        lRelReq = false;
                        enviando = false;
                        reenviando = false;
                        monitor.notifyAll();
                        if (prereset) {
                            lReset = false;
                            prereset = false;
                            escribir("Inicia procesamiento de contingencia en caso de datos a procesar");
                            almacenaDatos();
                            reiniciaComunicacion(false);
                        } else {
                            String aviso = "";
                            lcierrapuertos = false;
                            cerrarPuerto(false);
                            boolean l = false;
                            almacenaDatos();
                            if (!lperfil && !leventos && !lregistros) {
                                l = true;
                            }
                            if (lconfhora && clockSincronizado) {
                                l = true;
                            }
                            if (l || vlec.size() > 0 || registrosprocesadosE > 0 || (vRegistrosdia != null && vRegistrosdia.size() > 0) || (vRegistrosmes != null && vRegistrosmes.size() > 0)) {
                                if (procesaIncompletoPerfil || procesaIncompletoRegistros || procesaIncompletoEventos) {
                                    if (procesaIncompletoPerfil) {
                                        aviso = aviso + "- P. ";
                                    } else if (procesaIncompletoRegistros) {
                                        aviso = aviso + "- R. ";
                                    } else if (procesaIncompletoEventos) {
                                        aviso = aviso + "- E. ";
                                    }
                                    aviso = "Leido - Incompleto" + aviso;
                                } else {
                                    aviso = "Leido";
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Verificación estado de lectura - " + aviso);
                                escribir("Estado Lectura " + aviso);
                                cerrarLog("Leido", true);
                                //med.MedLeido = true;
                            } else {
                                if (procesaIncompleto) {
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Verificación estado de lectura - No Leído");
                                    escribir("Estado Lectura No leido");
                                    cerrarLog("No leido", false);
                                } else {
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Verificación estado de lectura - Leído - Sin Datos");
                                    escribir("Estado Lectura Leido - Sin Datos");
                                    cerrarLog("Leido", true);
                                }
                            }
                            leer = false;
                        }
                        return;
                    } else {
                        reenviando = true;
                        enviando = false;
                        monitor.notifyAll();
                        return;
                    }
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
            cerrarPuerto(true);
            reenviando = false;
            enviando = false;
            synchronized (monitor) {
                monitor.notifyAll();
            } 
        }
  }
    
    private void interpretaCadena(String cadenahex) throws Exception {
        try {
            escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (lSNRMUA) { //SNRM
                revisarSNRM(vectorhex);
            } else if (lARRQ) {
                revisarAARQ(vectorhex);
            } else if (lphyaddress) {
                revisarPhyAddress(vectorhex);
            } else if (lpowerLost) {
                revisarEventos(vectorhex);
            } else if (lserialnumber) {
                revisarSerial(vectorhex);
            } else if (lfechaactual) {
                revisarFecAct(vectorhex);
            } else if (lperiodoIntegracion) {
                revisarPeriodoInt(vectorhex);
            } else if (linfoperfil) {
                revisarInfoPerfil(vectorhex);
            } else if (lconstants) {
                revisarConstants(vectorhex);
            } else if (lfechaactual2) {
                revisarFecAct2(vectorhex);
            } else if (lperfilcarga) {
                revisarPerfil(vectorhex);
            } else if (lregis) { // Registros
                revisarRegistros(vectorhex);
            } else if (lRelReq) {
                revisarReleaseRequest(vectorhex);
            } else if (lprecierre) { //pre cierre
                revisarPrelogout(vectorhex);
            } else if (lcierrapuertos) { //cerrar puertos
                revisarLogout(vectorhex);
            } else if (lReset) {//estado reset para los casos de contraseña mala y negacio
                revisarReset(vectorhex);
            } else if (lfechasync) {
                revisarConfHora(vectorhex);
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
    
    public boolean interpretaDatos(String[] trama, int tipoTrama) {
        vlec = new Vector<>();
        boolean revisar, almacenar, detNumCanales, correcto;
        correcto = true;
        procesaIncompleto = false;
        int k, mod;
        detNumCanales = false;
        String cadenaTemp;
        String cadenas[] = new String[10];
        i1 = j1 = k = l1 = mod = 0;
        if (tipoTrama == 5 || tipoTrama == 22 || tipoTrama == 23 || tipoTrama == 24) {
            detNumCanales = true;
        } else {
            mod = 4;
        }
        try {
            if (clasificarTrama(trama)) {
                while (i1 < trama.length) {
                    while (pila[0] > 0) {
//                        if (clasificarTrama(trama)) {
                            almacenar = false;
                            if (j1 == 10) {
                                //System.out.println("Error en la Pila de datos");
                                //System.out.println("----------------------------");
                                correcto = false;
                                break;
                            }
                            if (revisarIndice(i1, trama.length)) {
                                if (Integer.parseInt(trama[i1], 16) == 1 || Integer.parseInt(trama[i1], 16) == 2 || Integer.parseInt(trama[i1 + 1], 16) >= 128) {
                                    revisar = false;
                                    if (Integer.parseInt(trama[i1 + 1], 16) >= 128){//casos en que es un dato y no un tamanio
                                        short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
                                        short temp = devLong[Integer.parseInt(trama[i1], 16)];
                                        if (temp != 0) {//tiene un tamanio fijo
                                           revisar = true; 
                                        }
                                    }
                                } else {
                                    revisar = true;
                                }
                                i1++;
                                if (!aumentaPila(trama)) {
                                    correcto = false;
                                    break;
                                }
                                if (revisar) {
                                    cadenaTemp = "";
                                    while (pila[j1 - 1] > 0) {

                                        if (revisarIndice(i1, trama.length)) {
                                            cadenaTemp = cadenaTemp + trama[i1];
                                            i1++;
    ////                                        System.out.println("i1 en revisar: "+i1);
                                            pila[j1 - 1]--;
                                        } else {
                                            correcto = false;
                                            break;
                                        }
                                    }
                                    if (!correcto) {
                                        break;
                                    }
                                    j1--;
                                    cadenas[k] = cadenaTemp;
                                    if (!procesaData(cadenas[k], tipoTrama)) {
                                        correcto = false;
                                    }
                                    k = (++k) % 10;
                                    pila[j1 - 1]--;
                                    l1++;//tefa
                                }
                                while (j1 != 1 && pila[j1 - 1] == 0) {
                                    j1--;
                                    pila[j1 - 1]--;
                                    if (pila[j1 - 1] != 0 && revisar) {
                                        revisar = false;
                                        almacenar = true;
                                    }
                                }
                                //hasta aqui termina
                                if (almacenar) {
                                    //System.out.println("----------------------------");
                                    if (detNumCanales) {
                                        l1 = 0;
                                    }
                                }
                            } else {
                                correcto = false;
                                break;
                            }
//                        } else {
//                            correcto = false;
//                            break;
//                        }
                    }
                    if (!correcto) {
                        break;
                    }
                    if (pila[j1 - 1] != 0) {
                        pila[j1 - 1]--;
                        j1--;
                    }

                }
                //System.out.println("termina interpretacion");
            } else {
                correcto = false;
                //System.out.println("clasificar trama " + false);
            }

            if (correcto) {
                //System.out.println("Proceso completado sin errores\n");
                procesaIncompleto = false;
            } else {
                //System.out.println("Proceso completado con errores\n");
                procesaIncompleto = true;
            }
//        } catch(ArrayIndexOutOfBoundsException excepcion) {
//            System.err.println(excepcion.getMessage());
////            System.out.println("BoundsException - Proceso completado con errores\n");
//            escribir("Procesamiento completado con errores");
//            return false;
        } catch (Exception e) {
//            System.err.println(e.getCause());
           escribir(getErrorString(e.getStackTrace(), 3));
            if (recortaFormato == 0){
                //System.out.println("Proceso completado con errores\n");
                escribir("Procesamiento completado con errores");
            }
            procesaIncompleto = true;
            return true;
        }
        return correcto;
    }

    public boolean aumentaPila(String[] trama) {
        short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
        short temp = devLong[Integer.parseInt(trama[i1 - 1], 16)];
        int longi = Integer.parseInt(trama[i1], 16);
        String tramaTemp = "";
        if (temp == 0) {
            if (revisarIndice(i1, trama.length)) {
                if (longi < 128) {
                    if (Integer.parseInt(trama[i1 - 1], 16) == 4) {
                        pila[j1] = (short) Math.ceil(longi / 8.0);
                    } else {
                        pila[j1] = Short.parseShort(trama[i1], 16);
                    }
                } else {
                    longi = longi % 128;
                    //longi = 1
                    for (int m = 0; m < longi; m++) {
                        if (revisarIndice(i1 + m + 1, trama.length)) {
                            tramaTemp = tramaTemp + trama[i1 + m + 1];
                        } else {
                            return false;
                        }
                    }
                    pila[j1] = Short.parseShort(tramaTemp, 16);
                    i1 = i1 + longi;
                }
            } else {
                return false;
            }
        } else {
            pila[j1] = temp;
            i1--;
        }
        i1++;
        j1++;
        return true;
    }
    
    public boolean revisarIndice(int indice, int longTrama) {
////        System.out.println("indice: "+indice+" - long: "+longTrama+" - bytes recortados: "+bytesRecortados);
        if (indice == (longTrama + bytesRecortados)) {
            //System.out.println("----------------------------\nTrama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados\n----------------------------");
            return false;
        } else {
            return true;
        }
    }
    
    public boolean clasificarTrama(String[] trama) {
        boolean continuar = false;
        if (tipoconexion != 0){
            if (i1 + 2 < trama.length) {
                if ("E6E700".equals(trama[i1] + trama[i1 + 1] + trama[i1 + 2])) {
                    //System.out.println("LLC Command Response");
                    if (i1 + 3 < trama.length) {
                        i1 = i1 + 3;
                        continuar = true;
                    } else {
                        //System.out.println("----------------------------");
                        //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                        //System.out.println("----------------------------");
                        return false;
                    }
                } else {
                    if (i1 == 0) {
                        //System.out.println("No es un LLC Response\n----------------------------");
                        return false;
                    }
                }

            }
        }  else {
            continuar = true;
        }
        if (continuar){
            switch (Short.parseShort(trama[i1], 16)) {
                case 12: {
                    //System.out.println("Read Response");
                    //System.out.println("----------------------------");
                    if (i1 + 1 < trama.length) {
                        i1 = i1 + 1;
                    } else {
                        //System.out.println("----------------------------");
                        //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                        //System.out.println("----------------------------");
                        return false;
                    }

                    pila[j1] = Short.parseShort(trama[i1], 16);
                    j1++;
                    i1++;
                    return operaReadResponse(trama);
                }
                case 97: {
                    //System.out.println("AARE APDU (No esperado)\n----------------------------");
                    return false;
                }
                case 196: {
                    switch (Short.parseShort(trama[i1 + 1], 16)) {
                        case 1: {
                            //System.out.println("Get Normal Response \n----------------------------");
                            if (i1 + 3 < trama.length) {
                                String dato = "";
                                i1 = i1 + 3;
                                if (Integer.parseInt(trama[i1+1], 16) == 1 || Integer.parseInt(trama[i1+1], 16) == 2 || Integer.parseInt(trama[i1 + 2], 16) >= 128) {
                                    if (Short.parseShort(trama[i1 + 2], 16) >= 128) {
                                        for (int k = 1; k <= (Integer.parseInt(trama[i1 + 2], 16)% 128); k++) {
                                            dato = dato + trama[i1 + 2 + k];
                                        }
                                    } else {
                                        dato = trama[i1 + 2];
                                    }
                                    //System.out.println("RegistrosAprocesar: "+Integer.parseInt(dato, 16));
                                    registrosAprocesar = Integer.parseInt(dato, 16);
                                }else {
                                    //System.out.println("tipo dato i1: "+trama[i1+1]+" tipo dato i1+1: "+trama[i1+2]);
                                }
                            } else {
                                //System.out.println("----------------------------");
                                //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                //System.out.println("----------------------------");
                                return false;
                            }

                            if (j1 == 0) {
                                pila[j1] = 1;
                                j1++;
                            }

                            return operaReadResponse(trama);
                        }
                        case 2: {
                            //System.out.println("Get Block Transfer Response \n----------------------------");
                            if (i1 + 12 < trama.length) {
                                int recortar = 0;//VIC
                                String dato = "";
                                    if (Short.parseShort(trama[i1 + 9], 16) >= 128) {
                                        recortar = (Integer.parseInt(trama[i1 + 9], 16)% 128);
                                        i1 = i1 + 10+ recortar;//vic hoy  

                                    }else{
                                      i1 = i1 + 10;//vic hoy  antes era +12
                                    }
                                    //
                                    if (Integer.parseInt(trama[i1], 16) == 1 || Integer.parseInt(trama[i1], 16) == 2 || Integer.parseInt(trama[i1 + 1], 16) >= 128) {
                                        if (Short.parseShort(trama[i1 + 1], 16) >= 128) {
                                            for (int k = 1; k <= (Integer.parseInt(trama[i1 + 1], 16)% 128); k++) {
                                                dato = dato + trama[i1 + 1 + k];
                                            }
                                        } else {
                                            dato = trama[i1 +1];
                                        }
                                        registrosAprocesar = Integer.parseInt(dato, 16);
                                    }
                                    //
//                                            if (Short.parseShort(trama[i1 + 1], 16) >= 128) {
//                                                String dato = "";
//                                                for (int k = 1; k <= (Integer.parseInt(trama[i1 + 1], 16)% 128); k++) {
//                                                    dato = dato + trama[i1 + 1 + k];
//                                                }
//                                                registrosAprocesar = Integer.parseInt(dato, 16);
//                                            }
                            } else {
                                //System.out.println("----------------------------");
                                //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                //System.out.println("----------------------------");
                                return false;
                            }
                            if (j1 == 0) {
                                pila[j1] = 1;
                                j1++;
                            }
                            return true;
                        }

                        default: {
                            //System.out.println("Unknown Response Code \n----------------------------");
                        }

                    }

                    return false;
                }
                default: {
                    //System.out.println("Codigo xDLMS-APDU Erroneo\n----------------------------");
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean operaReadResponse(String trama[]) {
        if (j1 == 1 && revisarIndice(i1, trama.length)) {
            switch (Short.parseShort(trama[i1], 16)) {
                case 0: {
                    //System.out.println("Datos success!\n----------------------------");
                    i1++;
                    return true;
                }
                case 1: {
                    //System.out.println("Error al Acceder a los Datos\n----------------------------");
                    return false;
                }
                case 2: {
                    //System.out.println("Resultado en Bloques de Datos (No realizado aun)\n----------------------------");
                    return false;
                }
                case 3: {
                    //System.out.println("Numero de Bloque (No realizado aun)\n----------------------------");
                    return false;
                }
                default: {
                    //System.out.println("Codigo de Respuesta Erroneo\n----------------------------");
                    return false;
                }
            }
        } else {
            return false;
        }
    }
    
    public boolean procesaData(String dato, int tipoTrama) {
        switch (tipoTrama) {
            case 2: { //serial
                dato = Hex2ASCII(dato);
                //System.out.println(dato);
                datoserial = dato;
                break;
            }
            case 3: { //clock
                try {
                    fechaActual = fecha.parse(Hex2Date(dato));
                    dato = sdf3.format(fechaActual);

                } catch (Exception e) {
                    escribir(getErrorString(e.getStackTrace(), 3));
                }
                //System.out.println("Fecha actual " + dato);
                break;
            }
            case 4: { //periodo de integración
                try {
//                    if(!lentradasenuso){
                    periodoIntegracion = Integer.parseInt(dato, 16) / 60;                    
                    if (regisdia){
                        if(periodoIntegracion==0){
                            periodoIntegracion=1440;//minutos en un día.
                        }
                        intervaloRdia = periodoIntegracion;
                    } else if (regismes){
                        if(periodoIntegracion==0){
                            periodoIntegracion=43200;//minutos en un mes de 30 días.
                        }
                        intervaloRmes = periodoIntegracion;
                    } else {
                        intervalo = periodoIntegracion;
                    }
                    //System.out.println("\nPeriodo de Integracion: " + periodoIntegracion);
//                    lentradasenuso=true;
//                    }else{
//                      entradasEnUso = Integer.parseInt(dato, 16);  
////                      System.out.println("\nEntradas en uso: " + entradasEnUso);
//                      lperiodoIntegracion = false; 
//                      lentradasenuso=false;
//                    }
                } catch (Exception e) {
                    escribir(getErrorString(e.getStackTrace(), 3));
                }
                break;
            }
            case 5: { //canales
                if (l1 == 0) {
                    clase.add(dato);
                    //System.out.println("\nClase " + dato);
                }
                if (l1 == 1) {
                    obis.add(dato);
                    //System.out.println("\nObis " + dato);
                }
                break;
            }
            case 7: {
                try {
                    dirfis = Integer.parseInt(dato, 16);
                    cp.actualizaDirFis(med.getnSerie(), dirfis);
                    //System.out.println("\nDireccion Fisica: " + dirfis);
                    
                } catch (Exception e) {
                    setdirfis = false;
                }
                break;
            }
            case 20: { //constantes 
                if (l1 == 0) {
                    conske.add(dato);
                    //System.out.println("\nEscala " + dato);
                }
                if (l1 == 1) {
                    unidad.add(dato);
                    //System.out.println("\nUnidad " + dato);
                    constanteOk = true; //(debe ser true)
                }
                break;
            }
            case 22: { //perfil de carga
                procesaDataPerfil(dato);
                break;
            }
            case 23: { //Registros - OJO EN DESARROLLO
                procesaDataRegistros(dato);
                break;
            }
            case 24: { //eventos
                procesaDataEventos(dato);
                break;
            }
            default: {
                //System.out.println("No es posible interpretar los datos");
                return false;
            }
        }
        return true;
    }
    
    public void procesaDataPerfil(String dato){
        if (l1 == 0) {
            indicecanal = 0;
            indxlec = 0;
            try {
                registrosprocesados++;
                //System.out.println("-> Dato original: " + dato);
                dato = sdf2.format(fecha.parse(Hex2Date(dato)));
                //System.out.println("Fecha intervalo: " + dato);
                fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                if (fechaintervalo.getMinutes() % intervalo != 0) {
                    dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
                    fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                    //System.out.println("Fecha intervalo aproximado: " + dato);
                }
                if (ultimoIntervalo == null) {
                    ultimoIntervalo = fechaintervalo;
                }
                if (primerintervalo){ 
                    fprimerintervalo = fechaintervalo;
                    primerintervalo = false;
                }
                //manejo de huecos
                int aumento = 0;
                fechaCero = null;
                try {
                    if (ultimoIntervalo != null) {//si tiene una fecha inicial
                        aumento = (int) Math.abs(((fechaintervalo.getTime() - ultimoIntervalo.getTime()) / 60000) / intervalo) - 1;//se calcula si el intervalo actual es superior e 1 intervalo de integracion 
                        if (aumento > 0) {//obtiene el numero de intervalos a mover
                            for (int i = 0; i < aumento; i++) {
                                fechaCero = new Timestamp(ultimoIntervalo.getTime() + (60000 * intervalo) * (i + 1));//movemos la fecha por cada intervalo faltante
                                //System.out.println("Fecha intervalo en 0: " + fechaCero);
                                for (int k = 0; k < obis.size(); k++) {//se recorren la cantidad de canales programados en el medidor
                                    try {
                                        econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(k), 16)); //buscamos el valor de la constante creada en telesimex
                                        if (econske != null) {
                                            canal = "";
                                            for (EtipoCanal et : vtipocanal) {//buscamos la unidad del canal
                                                if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(k), 16)) {
                                                    canal = et.getUnidad();
                                                    break;
                                                }
                                            }
                                            if (canal.length() > 0) {
                                                lec = new Electura(fechaCero, med.getnSerie(), Long.parseLong(obis.get(k), 16), 0.0, 0, intervalo, canal);//lectura del canal en 0                                                    
                                                if(!(obis.get(k).substring(6,8).equals("1D"))&&!(Integer.parseInt(med.getMarcaMedidor().getCodigo())==22)){//canal no es interval
                                                    if (!fprimerintervalo.equals(fechaintervalo)){ 
//                                                        lec.fecha = new Timestamp(fechaCero.getTime()-(60000 * intervalo));
                                                        vlec.add(lec);
                                                    }
                                                } else{
                                                    vlec.add(lec);
                                                }
                                            } else {
                                                //System.out.println("Constante");
                                            }

                                        }
                                    } catch (Exception e) {
                                        escribir(getErrorString(e.getStackTrace(), 3));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    escribir(getErrorString(e.getStackTrace(), 3));
                }
                ultimoIntervalo = fechaintervalo;

            } catch (Exception e) {
                escribir(getErrorString(e.getStackTrace(), 3));
            }
        } else {
            indicecanal++;
            try {
                //System.out.println("Dato antes: "+dato);
                dato = String.valueOf(Long.parseLong(dato, 16));
                //System.out.println("-> Dato original: "+dato);
                econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(indicecanal), 16));
                //System.out.println("Consulta sobre canal: "+Long.parseLong(obis.get(indicecanal), 16));
                //escribir("Consulta sobre canal: "+Long.parseLong(obis.get(indicecanal), 16));
                if (econske != null) {
                    canal = "";
                    for (EtipoCanal et : vtipocanal) {
                        if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(indicecanal), 16)) {
                            canal = et.getUnidad();
                            //System.out.println("Almacena Canal: " + et.getCanal() + " - Unidad: " + canal);
                            //escribir("Almacena Canal: " + et.getCanal() + " - Unidad: " + canal);
                            break;
                        }
                    }
                    if (fechaintervalo != null && canal.length() > 0) {
                        if (constanteOk){
                            //System.out.println("valor: " + cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                            //System.out.println("Unidad: " + unidad.get(indicecanal));
                            lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                            if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                lec.lec = lec.lec / 1000.0;
                            }
                            if(!(obis.get(indicecanal).substring(6,8).equals("1D"))&&!(Integer.parseInt(med.getMarcaMedidor().getCodigo())==22)){//canal no es interval
                                lec.setLecaux(lec.lec);
                                if (!fprimerintervalo.equals(fechaintervalo)){
                                    lec.lec = lec.lec-lecInterval.get(indxlec);
                                    vlec.add(lec);
                                    lecInterval.set(indxlec,lec.lecaux);
                                }else{
                                    lecInterval.add(indxlec,lec.lecaux);
                                }
                                indxlec++;
                            } else {
                                vlec.add(lec);
                            }
                        }else{ //
                            //System.out.println("Sin Scaler-unit");
                            //System.out.println("\nConversion desde base de datos: ");
                            //System.out.println("valor: " +cp.trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                            //System.out.println("Unidad: " + canal);
                            lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), cp.trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                            if (canal.contains("k")||canal.contains("K")) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                lec.lec = lec.lec / 1000.0;
                            }
                            if(!(obis.get(indicecanal).substring(6,8).equals("1D"))&&!(Integer.parseInt(med.getMarcaMedidor().getCodigo())==22)){
                                lec.setLecaux(lec.lec);
                                if (!fprimerintervalo.equals(fechaintervalo)){
//                                    lec.fecha = new Timestamp(fechaintervalo.getTime()-(60000 * intervalo));
                                    lec.lec = lec.lec-lecInterval.get(indxlec);
                                    vlec.add(lec);
                                    lecInterval.set(indxlec,lec.lecaux);
                                }else{
                                    lecInterval.add(indxlec,lec.lecaux);
                                }
                                indxlec++;
                            } else {
                                vlec.add(lec);
                            }
                        }
                    }
                } else {
                    //System.out.println("Canal: "+Long.parseLong(obis.get(indicecanal), 16)+" No almacenado");
                    //escribir("Canal: "+Long.parseLong(obis.get(indicecanal), 16)+" No almacenado");
                }
            } catch (Exception e) {
                e.printStackTrace();
                //System.out.println("procesaPerfil");
            }

        }
    }
    
    public void procesaDataRegistros(String dato){ //En desarrollo
        if (l1 == 0) {
            indicecanal = 0;
            try {
                ////System.out.println(" indicecanal " + indicecanal);
                //System.out.println("dato original: " + dato);
                dato = sdf2.format(fecha.parse(Hex2Date(dato)));
                //System.out.println("Fecha intervalo: " + dato);
                fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                //revisar si aproximar o no el intervalo para registros
//                        if (fechaintervalo.getMinutes() % intervalo != 0) {
//                            dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
//                            fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
////                            System.out.println("Fecha intervalo aproximado" + dato);
//                        }
                if (ultimoIntervalo == null) {
                    ultimoIntervalo = fechaintervalo;
                }
                //manejo de huecos para registros
                int aumento = 0;
                fechaCero = null;
                        try {
                            if (ultimoIntervalo != null) {//si tiene una fecha inicial
                                aumento = (int) Math.abs(((fechaintervalo.getTime() - ultimoIntervalo.getTime()) / 60000) / intervalo) - 1;//se calcula si el intervalo actual es superior e 1 intervalo de integracion 
                                if (aumento > 0) {//obtiene el numero de intervalos a mover
                                    for (int i = 0; i < aumento; i++) {
                                        fechaCero = new Timestamp(ultimoIntervalo.getTime() + (60000 * intervalo) * (i + 1));//movemos la fecha por cada intervalo faltante
                                        //System.out.println("fecha intervalo en 0 " + fechaCero);
                                        for (int k = 0; k < obis.size(); k++) {//se recorren la cantidad de canales programados en el medidor
                                            try {
                                                canal = "";
                                                for (EtipoCanal et : vtiporegistros) {//buscamos la unidad del canal
                                                    if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(k), 16)) {
                                                        canal = et.getUnidad();
                                                        break;
                                                    }
                                                }
                                                if (canal.length() > 0) {
                                                    //lectura del canal en 0
                                                    reg = new ERegistro( med.getnSerie(),  tiporegistros,  fechaCero.getYear() + 1900, fechaCero.getMonth() + 1,  fechaCero.getDate(),  0.0,  Long.parseLong(obis.get(k), 16),  canal,  0.0);
                                                    vreg.add(reg);
                                                } else {
                                                    //System.out.println("Constante");
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
                ultimoIntervalo = fechaintervalo;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            indicecanal++;
            ////System.out.println(" indicecanal " + indicecanal);
            try {
                dato = String.valueOf(Long.parseLong(dato, 16));
                //System.out.println(dato);
                    canal = "";
                    for (EtipoCanal et : vtiporegistros) {
////                                System.out.println("Canal tcun " + et.getCanal());
////                                System.out.println("OBIS " + Long.parseLong(obis.get(indicecanal), 16));
                        if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(indicecanal), 16)) {
                            canal = et.getUnidad();
                            //System.out.println("Canal: " + et.getCanal() + " - Unidad: " + canal);
                            break;
                        }
                    }
                    if (fechaintervalo != null && canal.length() > 0) {
                            //System.out.println("valor: " + cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conske.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                            //System.out.println("Unidad: " + unidad.get(indicecanal));
                            reg = new ERegistro( med.getnSerie(),  tiporegistros,  fechaintervalo.getYear() + 1900, fechaintervalo.getMonth() + 1,  fechaintervalo.getDate(),  Double.parseDouble(dato),  Long.parseLong(obis.get(indicecanal), 16),  canal,  cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conske.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                            if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                reg.energia = reg.energia / 1000.0;
                            }
                            vreg.add(reg);
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    
    public void procesaDataEventos(String dato){ //verificar los códigos de eventos - 25 down - 26 up
        if (l1 == 0) {
            try {
                registrosprocesados++;
                fechaEvento = fecha.parse(Hex2Date(dato));
                //System.out.println("Procesa registro del: " + fechaEvento);
            } catch (Exception e) {
                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
        } else if (l1 == 1) {
            try {
                if (dato.equals("4C")) {
                    //System.out.println("Cod.: 76 - Power up: " + fechaEvento);
                    regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
                    if (regEvento.vcfechacorte != null && regEvento.vcfechareconexion != null) {
                        //System.out.println(fecha.format(regEvento.vcfechacorte) + "-" + fecha.format(regEvento.vcfechareconexion));
                        escribir("Almacena corte y reconexión: "+fecha.format(regEvento.vcfechacorte) + " - " + fecha.format(regEvento.vcfechareconexion));
                        regEvento.vcserie = seriemedidor;
                        regEvento.vctipo = "0001";                        
                        cp.actualizaEvento(regEvento, null);
                        //System.out.println("termina actualiza");
//                        listRegEventos.add(regEvento); //la lista en realidad no se usa, ya que se almacena en la base cada par corte-reconexión que se interpreta
                    }
                } else if (dato.equals("4B")) {
                    //System.out.println("Cod.: 75 - Power down: " + fechaEvento);
//                    regEvento.vcfechareconexion = null;
                    regEvento = new ERegistroEvento();
                    regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                } else {// Códigos restantes para el perfil de eventos Power Quality
                    //Descomentar para guardar el resto de eventos //VIC 12-07-19
                    //System.out.println("Codigo de evento: "+Integer.parseInt(dato,16)); 
                    regEvento = new ERegistroEvento();
                    regEvento.vcfechacorte = new Timestamp(fechaEvento.getTime());
                    regEvento.vcfechareconexion = regEvento.vcfechacorte;
                    escribir("Evento no almacenado: Cod. "+Integer.parseInt(dato,16)+" - "+fecha.format(regEvento.vcfechacorte));
                    regEvento.vctipo = dato;
//                    if (regEvento.vcfechacorte != null && regEvento.vctipo != null) {
//                        //Descomentar para guardar el resto de eventos //VIC 12-07-19
//////                                System.out.println(fecha.format(regEvento.vcfechacorte) + ", " + regEvento.vctipo);
//                        regEvento.vcserie = seriemedidor;
//                        //Descomentar para guardar el resto de eventos //VIC 12-07-19////                                
////                                cp.actualizaEvento(regEvento, null);
//////                                System.out.println("termina actualiza");
//                        listRegEventos.add(regEvento);
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
        }
        //
    }
    
    public String[] revisarHeaderYcrc(String[] vectorhex, String peticion) {
        if (tipoconexion == 0) {
            String tamauxGPRS = vectorhex[6] + vectorhex[7]; //Para verificar lenght de GPRS
            if (vectorhex.length > 7) {//tiene cabecera
                if (vectorhex.length == (Integer.parseInt(tamauxGPRS, 16) + indxData)) {
                    ReintentoRevisionTrama = 0;
                    tramaOK = true;
                    return vectorhex;
                } else {
                    escribir("Error trama incompleta");
                    handleReenvio(peticion);
                }
            } else {//trama incompleta
                escribir("Error trama Sin cabecera");
                handleReenvio(peticion);
            }
        } else {
            vectorhex = cortarTrama(vectorhex, (Integer.parseInt((vectorhex[indxLength - 1] + vectorhex[indxLength]), 16) & 0x7FF));
            if (validacionCRCHCS(vectorhex)) {
                if (validacionCRCFCS(vectorhex)) {
                    ReintentoRevisionTrama = 0;
                    tramaOK = true;
                    return vectorhex;
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - Error de checksum");
                    escribir("BAD FCS");
                    handleReenvio(peticion);
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " -Error de checksum");
                escribir("BAD HCS");
                handleReenvio(peticion);
            }
        }
        return vectorhex;
    }
    
    public void enviaCompletaTrama(String[] vectorhex, String peticion){
        try{
            if (!vectorhex[1].equals("A0")) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                byte trama[] = tramas.getRR();
                trama = asignaDirecciones(trama);
                trama[indxControl] = RR_CTRL(nr); //cuando envia RR aumenta nr, pero mantiene a ns
                ns--; 
                nrEsperado--; 
                trama = calcularnuevocrcRR(trama);
                ns++; 
                nrEsperado++; 
                enviaTrama2(trama, "=> Envia Recieved Ready RR");
            }else{
                enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud "+peticion);
            }
        } catch (Exception e) {
            escribir("Error al recibir "+peticion);
            enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud "+peticion);
        } 
    }
    
    public void revisarSNRM(String[] vectorhex) throws SQLException {
        vectorhex = revisarHeaderYcrc(vectorhex, "SNRM");
        if (tramaOK) {
            if (vectorhex[indxControl].equals("73") || vectorhex[indxControl].equals("63")) {
                tramaOK = false;
                ns = 0;
                nr = 0;
                escribir("NsPc " + ns + " NrPc " + nr);
                nrEsperado = ns + 1;
                nsEsperado = 0;
                lSNRMUA = false;
                lARRQ = true;
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama SNRM - Correcto");
                if (lPhyAdrs) {
                    lPhyAdrs = false;
                    obtieneDirFis(vectorhex);
                }
                byte trama[] = crearAARQ(tramas.getAarq(), password);
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcI(trama);
                ultimatramaEnviada = trama;
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - AARQ");
                enviaTrama2(trama, "AARQ");
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error al interpretar SNRM");
                reiniciaComunicacion(true);
            }
        }
    }
    
    public void revisarControlByte(String[] vectorhex, String peticion){ 
        if (tipoconexion != 0){
            if (tramaOK){
                tramaOK = false;
                if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x01) == 0x00) { //es informacion
                    //validamos secuencia
                    escribir("NrM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1)) {
                        nr++;
                        if (nr > 7) {
                            nr = 0;
                        }
                        ns++;
                        if (ns > 7) {
                            ns = 0;
                        }
                        nsEsperado++;
                        if (nsEsperado > 7) {
                            nsEsperado = 0;
                        }
                        nrEsperado++;
                        if (nrEsperado > 7) {
                            nrEsperado = 0;
                        }
                        escribir("NsPc " + ns + " NrPc " + nr);
                        ReintentoRevisionTrama=0;
                        tramaOK = true;
                    } else {
                        escribir("No son los ns y nr esperados");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama "+ peticion+" - Error de protocolo");
                        handleReenvio(peticion);
                    }
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama "+ peticion+" - Error de protocolo");
                    if (vectorhex[indxControl].equals("73")) {
                        handleReenvio(peticion);
                    } else {
                        validarTipoTrama(vectorhex[indxControl]);
                    }
                }
            } 
        }
    }
    
    public void revisarAARQ(String[] vectorhex) {
        String peticion = "AARQ";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;//indxData
            if (vectorhex[indxData].equals("61")) {//si no es AARE no continua con la interpretacion
                ReintentoRLRQ = 0;
                if (vectorhex[indxData + 17].equals("00") && vectorhex[indxData + 24].equals("00")) { //VIC 15-10-20
                    lARRQ = false;
                    lpowerLost = false;
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - Correcta");
                    if (lPhyAdrs) {
                        enviaPhyAddress();
                    } else {
                        enviaSerial();
                    }
                } else {
                    if (vectorhex[indxData + 24].equals("0D")) {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - Error de autenticacion");
                        escribir("\nError de autenticación");
                        lARRQ = false;
                        lpowerLost = false; //vic 12-06-19
                        lperiodoIntegracion = false; //vic 12-06-19
                        if (tipoconexion == 0 || tipoconexion == 2) {
                            lprecierre = true;
                            prereset = false;
                            enviaPrelogout();
                        } else {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales");
                            cerrarPuerto(true);
                            escribir("\nDesconexion - Error de autenticacion");
                            cerrarLog("Desconexion Error de autenticacion", false);
                            leer = false;
                        }
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - Error de autenticación - fallo en interpretación de AARE");
                        escribir("Error de autenticacion - fallo en interpretación de AARE");
                        lARRQ = false;
                        lpowerLost = false;
                        lperiodoIntegracion = false;
                        if (tipoconexion == 0 || tipoconexion == 2) {//Cómo aún no se ha aumentado el índice, pero ya se está validando que quedén más usuaios por probar, debemos agegarle 1 a la comparación.
                            lprecierre = true;
                            prereset = false;
                            enviaPrelogout();
                        } else {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales");
                            cerrarPuerto(true);
                            escribir("Estado Lectura No leido");
                            cerrarLog("Desconexion Error de autenticacion", false);
                            leer = false;
                        }
                    }
                }
            } else if (vectorhex[indxData].equals("D8")) {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - Error petición no liberada");
                escribir("Error petición no liberada");
                lARRQ = false;
                lpowerLost = false;
                lperiodoIntegracion = false;
                if (tipoconexion == 0 || tipoconexion == 2) {//Cómo aún no se ha aumentado el índice, pero ya se está validando que quedén más usuaios por probar, debemos agegarle 1 a la comparación.                            
                    lRelReq = true;
                    prereset = true;
                    enviaReleaseRequest(0);
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales");
                    cerrarPuerto(true);
                    escribir("Estado Lectura No leido");
                    cerrarLog("Desconexion Error de autenticacion", false);
                    leer = false;
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - No AARE - fallo en interpretación de AARE");
                escribir("No AARE - fallo en interpretación de AARE");
                lARRQ = false;
                lpowerLost = false;
                lperiodoIntegracion = false;
                if (tipoconexion == 0 || tipoconexion == 2) {//Cómo aún no se ha aumentado el índice, pero ya se está validando que quedén más usuaios por probar, debemos agegarle 1 a la comparación.
                    lprecierre = true;
                    prereset = false;
                    enviaPrelogout();
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales");
                    cerrarPuerto(true);
                    escribir("Estado Lectura No leido");
                    cerrarLog("Desconexion Error de autenticacion", false);
                    leer = false;
                }
            }
        }
    }

    public void enviaPhyAddress(){
        lphyaddress = true;
        byte trama[] = tramas.getSerial();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = cambiaClassnOBIS(trama,23,"0000160000FF");
        trama = calcularnuevocrcI(trama);
        ultimatramaEnviada = trama;        
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Dirección Física");
        enviaTrama2(trama, "=> Dirección Física");
    }
    
    public void revisarPhyAddress(String[] vectorhex) {
        String name = "Dirección Física";
        vectorhex = revisarHeaderYcrc(vectorhex, name);
        if (tramaOK) {
            tramaOK = false;
            if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x01) == 0x00) { //es informacion
                escribir("NrM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1));
                if (nrEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1)) {
                    nr++;
                    if (nr > 7) {
                        nr = 0;
                    }
                    ns++;
                    if (ns > 7) {
                        ns = 0;
                    }
                    nsEsperado++;
                    if (nsEsperado > 7) {
                        nsEsperado = 0;
                    }
                    nrEsperado++;
                    if (nrEsperado > 7) {
                        nrEsperado = 0;
                    }
                    escribir("NsPc " + ns + " NrPc " + nr);
                    lserialnumber = false;
                    String[] vectorData = sacarDatos(vectorhex, indxSacarDatos); //VIC debe ser 9 no 8
                    interpretaDatos(vectorData, 7);
                } else {
                    escribir("No son los ns y nr esperados");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta trama " + name, "Error de protocolo");
                    enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud direccion fisica");
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta trama " + name, "Error de protocolo");
                if (vectorhex[indxControl].equals("73")) {
                    enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud direccion fisica");
                } else {
                    validarTipoTrama(vectorhex[indxControl]);
                }
            }
        }
    }

    
    public void enviaSerial(){ 
        lserialnumber = true;
        byte trama[] = tramas.getSerial();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama); 
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
        ultimatramaEnviada = trama;        
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Serial");    
        enviaTrama2(trama, "=> Numero de Serial");
    }
    
    public void revisarSerial(String[] vectorhex) {
        String peticion = "Serial";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lserialnumber = false;
            String[] vectorData = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
            try {
                boolean continuar;
                interpretaDatos(vectorData, 2);
                datoserial = "" + Long.parseLong(datoserial);
                if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {
                    escribir("Numero serial " + datoserial);
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Serial obtenido: " + datoserial);
                    continuar = true;
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Serial incorrecto! Obtenido: " + datoserial + " En base de datos: " + seriemedidor);
                    escribir("Numero serial incorrecto");
                    continuar = false;
                }
                if (continuar) {
                    enviaFechaactual(1);
                } else {
                    escribir("Numero de serial no valido");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Serial incorrecto");
                    lprecierre = true;
                    prereset = true;
                    enviaPrelogout();
                }
            } catch (Exception e) {
                escribir("Error al validar el numero serial");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error al interpretar " + peticion);
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        }
    }

    public void enviaFechaactual(int nfecha){
        lfechaactual = lfechaactual2 = false;
        if(nfecha == 1){
            lfechaactual = true;
        } else if (nfecha == 2){
            lfechaactual2 = true;
        }
        byte trama[] = tramas.getFechaactual();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama);
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
        ultimatramaEnviada = trama;        
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Fecha Actual");
        enviaTrama2(trama, "=> Solicitud de fecha actual");
    }
    
    public void revisarFecAct(String[] vectorhex) {
        String peticion = "Fecha Actual";
        vectorhex = revisarHeaderYcrc (vectorhex,peticion);
        revisarControlByte(vectorhex,peticion);
        if (tramaOK){
            tramaOK = false;
            String[] data = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
            try {
                if (interpretaDatos(data, 3)) {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama "+peticion+" - Fecha obtenida: "+sdf3.format(fechaActual));
                    lfechaactual = false;
                    if (lconfhora) {
                        try {
                            lfechaactual2 = false;
                            enviaConfHora();
                        } catch (Exception e) {
                            e.printStackTrace();
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error obteniendo hora local");
                            escribir("Error en obtencion de NTP");
                            lprecierre = true;
                            prereset = true;
                            enviaPrelogout();
                        }
                    } else if (lperfil || lregistros) {
                        if(!lperfil){
                            regisdia = true;
                        }
                        enviaPeriodoDeIntegracion();
                    } else if (leventos) {
                        if (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22){
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                            cerrarPuerto(true);                            
                            escribir("Eventos no habilitado");
                            escribir("Leido");
                            cerrarLog("Leido", true);
                            leer = false;
                        } else {
                            enviaEventos(true);
                        }
                    } else {
                        escribir("Sin peticiones adicionales");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                        cerrarPuerto(true);                        
                        escribir("Leido");
                        cerrarLog("Leido", true);
                        leer = false;
                    }
                } else {
                    escribir("Error en obtencion de fecha");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error interpretando hora del medidor");
                    lprecierre = true;
                    prereset = true;
                    enviaPrelogout();
                }
            } catch (Exception e) {
                escribir("Error en obtencion de fecha");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error al interpretar "+peticion);
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        }
    }

    public void enviaConfHora() throws ParseException{
        time = obtenerHora();
        byte trama[] = tramas.getConfhora();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);        
        String fecha = sdf.format(new Date(time.getTime()));
        String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
        while (lfecha.length() < 4) {
            lfecha = "0" + lfecha;
        }
        //Bytes para ubicación de la fecha y hora
        int indxhora = indxhcs2 + 19;
        trama[indxhora] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
        trama[indxhora+1] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
        trama[indxhora+2] = (byte) (Integer.parseInt(fecha.substring(4, 6)) & 0xFF);// mes 
        trama[indxhora+3] = (byte) (Integer.parseInt(fecha.substring(6, 8)) & 0xFF);//dia
        trama[indxhora+4] = (byte) (((time.getDay()) == 0 ? 7 : (time.getDay())) & 0xFF);// dia de la semana
        trama[indxhora+5] = (byte) (Integer.parseInt(fecha.substring(8, 10)) & 0xFF); // confHora 
        trama[indxhora+6] = (byte) (Integer.parseInt(fecha.substring(10, 12)) & 0xFF); // min
        trama[indxhora+7] = (byte) (Integer.parseInt(fecha.substring(12, 14)) & 0xFF); // seg
        trama[indxhora+8] = (byte) 0xFF; // centesimas
        trama[indxhora+9] = (byte) 0x80;
        trama[indxhora+10] = (byte) 0x00; // desviacion 2 bytes
        trama[indxhora+11] = (byte) 0x00; // status
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Configuración hora: "+fecha);
        lfechasync = true;
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama);
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
        ultimatramaEnviada = trama;        
        enviaTrama2(trama, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));
    }
    
    public void enviaPeriodoDeIntegracion(){
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Periodo de integración");
        lperiodoIntegracion = true;
        byte trama[] = tramas.getPeriodoint();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22?cambiaClassnOBIS(trama,clasePerfilInt,sOBISPerfil):trama);
        if(regisdia&&Integer.parseInt(med.getMarcaMedidor().getCodigo())==22){
            regisdia=false;
            regismes=true;
        }
        if (regisdia){
            trama = cambiaClassnOBIS(trama,7,"0100630200FF");
        } else if (regismes){
            trama = trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22?cambiaClassnOBIS(trama,clasePerfilInt,sOBISRegistro):cambiaClassnOBIS(trama,7,"0000620100FF"));
        }
        trama = calcularnuevocrcI(trama);
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
        ultimatramaEnviada = trama;        
        enviaTrama2(trama, "=> Solicitud de periodo de integracion");
    }
    
    public void enviaEventos(boolean rango) throws ParseException{
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Eventos");
        primerbloque = true;
        bloqueRecibidoInt = 0;
        lpowerLost = true;
        listEventos = new ArrayList<>();
        byte trama[] = tramas.getPowerQuality();
        if(rango){
            trama = cambiaClassnOBIS(tramas.getPerfilcarga(),7,"0000636204FF");
        }
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        if(rango){
            ndias = this.calculaDiasALeer(regisdia, regismes, lpowerLost); //calcula ndias
            //calculaDiasALeer(); //calcula ndias
            String peticion = "Power Quality Request";
            String fecha = sdf.format(new Date(new Date().getTime() - (long) (86400000) * (ndias))); 
            escribir("Fecha inicio de "+peticion+": " + fecha);
            trama = asignaFechainiDeFiltroPorRango(trama, fecha);
            //fecha final******************************************
            fecha = sdf.format(new Date(new Date().getTime() + (long) (86400000)));
            escribir("Fecha final de "+peticion+": " + fecha);
            trama = asignaFechafinDeFiltroPorRango(trama, fecha);
        }
        trama = calcularnuevocrcI(trama);
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
        ultimatramaEnviada = trama;        
        enviaTrama2(trama, "=> Solicitud de eventos");
    }
    
    
    public void revisarConfHora(String[] vectorhex) {
        String peticion = "Sincronizacion de reloj";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lfechasync = false;
            lprecierre = true;
            if (Integer.parseInt(vectorhex[indxData + 3], 16) == 0) {
                clockSincronizado = true;
                escribir("Se sincronizó la hora correctamente");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Sincronización realizada");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
            } else {
                escribir("No fue posible sincronizar la hora");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Sincronización realizada");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales");
                prereset = true;
            }
            enviaPrelogout();
        }
    }
    
    public void revisarPeriodoInt(String[] vectorhex) {
        String peticion = "Periodo de integración";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lperiodoIntegracion = false;
            String[] data = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
            if (interpretaDatos(data, 4)) {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - " + peticion + " obtenido: " + periodoIntegracion);
                enviaInfoPerfil();
            } else {
                escribir("Negacion de peticion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error al interpretar " + peticion);
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        }
    }
    
    public void enviaInfoPerfil(){
        linfoperfil = true;
        obis = new ArrayList<String>();
        conske = new ArrayList<String>();
        unidad = new ArrayList<String>();
        clase = new ArrayList<String>();
        infoPerfil = new Vector<String>();
        byte trama[] = tramas.getConfperfil();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22?cambiaClassnOBIS(trama,clasePerfilInt,sOBISPerfil):trama);
        if (regisdia){
            trama = cambiaClassnOBIS(trama,7,"0100630200FF");      
        } else if (regismes){
            trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22?cambiaClassnOBIS(trama,clasePerfilInt,sOBISRegistro):cambiaClassnOBIS(trama,7,"0000620100FF"));
        }
        trama = calcularnuevocrcI(trama);
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
        ultimatramaEnviada = trama;        
        enviaTrama2(trama, "=> Solicitud de configuracion de "+ (regisdia==true?"regs dia" : (regismes==true?"regs mes" : "perfil")));
    }

    public void revisarInfoPerfil(String[] vectorhex) {
        String peticion = "Canales";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            linfoperfil = false;
            String vdata[] = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos)); //VIC
            infoPerfil.addAll(Arrays.asList(vdata));
            if (vectorhex[indxData + 1].equals("01")) { //sin bloque 15-10-20
                procesaInfoPerfil();
                linfoperfil = false;
                indexConstant = 0;
                if (obis.size() > 0) {
                    String obisaux = obis.get(0);
                    for (int j = 1; j < obis.size(); j++) {
                        obisaux = obisaux + ";" + obis.get(j);
                    }
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - " + peticion + " obtenidos: " + obisaux);
                    escribir("Cantidad de canales internos del medidor: " + obis.size());
                    escribir("Vector de canales internos del medidor: " + obis.toString());
                    while ((obis.size() - 1) >= indexConstant && !clase.get(indexConstant).equals("0003")) {
                        conske.add("0");
                        unidad.add("0");
                        indexConstant++;
                    }
                    enviaConstant();
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Sin canales ");
                    if (regisdia || regismes) {
                        lprecierre = true;
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales - Sin canales desde el medidor");
                        escribir("Sin canales desde el medidor - no se solicitara " + peticion);
                        enviaPrelogout();
                    } else {
                        enviaFechaactual(2);
                    }
                }
            } else {//con bloques
                if (!enviaREQ_NEXT(vectorhex)) {
                    procesaInfoPerfil();
                    linfoperfil = false;
                    indexConstant = 0;
                    if (obis.size() > 0) {
                        String obisaux = obis.get(0);
                        for (int j = 1; j < obis.size(); j++) {
                            obisaux = obisaux + ";" + obis.get(j);
                        }
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - " + peticion + " obtenidos: " + obisaux);
                        escribir("Cantidad de canales internos del medidor: " + obis.size());
                        escribir("Vector de canales internos del medidor: " + obis.toString());
                        while ((obis.size() - 1) >= indexConstant && !clase.get(indexConstant).equals("0003")) {
                            conske.add("0");
                            unidad.add("0");
                            indexConstant++;
                        }
                        enviaConstant();
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Sin canales ");
                        if (regisdia || regismes) {
                            lprecierre = true;
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales - Sin canales desde el medidor");
                            escribir("Sin canales desde el medidor - no se solicitara " + peticion);
                            enviaPrelogout();
                        } else {
                            enviaFechaactual(2);
                        }
                    }
                }
            }
        }
    }

    private void procesaInfoPerfil() {
        String[] data = infoPerfil.toArray(new String[infoPerfil.size()]);
        interpretaDatos(data, 5);
    }
    
    public boolean enviaConstant(){
        boolean enviar = true;
        lconstants = true;
        while (!clase.get(indexConstant).equals("0003")) {
            conske.add("0");
            unidad.add("0");
            indexConstant++; 
            if (indexConstant > (clase.size() - 1)){
                 enviar = false;
                 break; 
            }           
        }
        if (enviar) {
            byte[] trama = construirConstant(obis.get(indexConstant));
            trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
            ultimatramaEnviada = trama;            
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Constantes " + (indexConstant + 1));
            enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
            return false;
        } else {
            return true;
        }
        
    }
    
    public void revisarConstants(String[] vectorhex) {
        Boolean keep = true;
        String peticion = "Constantes";
        vectorhex = revisarHeaderYcrc (vectorhex,peticion);
        revisarControlByte(vectorhex,peticion);
        if (tramaOK){
            tramaOK = false;
            String vdata[] = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos)); //VIC
            interpretaDatos(vdata, 20);
            indexConstant++;
            while (keep) {
                if (indexConstant > (obis.size() - 1)) {
                    time = obtenerHora(); 
                    deltatimesync1 = obtenerHora(); 
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - " + peticion + " obtenidas: " + conske.toString());
                    lconstants = false;
                    //Backups de info para los canales de cada tipo
                    if (regisdia) {
                        obisRdia = obis;
                        conskeRdia = conske;
                        unidadRdia = unidad;
                        claseRdia = clase;
                    } else if (regismes) {
                        obisRmes = obis;
                        conskeRmes = conske;
                        unidadRmes = unidad;
                        claseRmes = clase;
                    } else {
                        obisPerfil = obis;
                        conskePerfil = conske;
                        unidadPerfil = unidad;
                        clasePerfil = clase;
                    }
                    enviaFechaactual(2);
                    break;
                } else {
                    //se solicita el siguiente OBIS
                    keep = enviaConstant(); //no se lleva indexConstant a cero
                }
            }            
        }
    }
    
    public void revisarFecAct2(String[] vectorhex) throws ParseException {
        String name = "Fecha Actual";
        vectorhex = revisarHeaderYcrc (vectorhex,name);
        revisarControlByte(vectorhex,name);
        if (tramaOK){
            tramaOK = false;
            String peticion = "";
            deltatimesync2 = obtenerHora();
            String[] dataTrama = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos)); //VIC
            if (interpretaDatos(dataTrama, 3)) {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama "+name+" - Fecha obtenida: "+sdf3.format(fechaActual));
                escribir("Fecha actual del medidor: " + fechaActual);
                try{ 
                    deltatimesync2 = obtenerHora();
                } catch (Exception e) {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama "+name+" - Error obteniendo hora local");
                    escribir(getErrorString(e.getStackTrace(), 3));
                }
                lfechaactual2 = false;
                if (lperfil || lregistros) {//se solicita perfil de carga o registros
                    peticion = (regisdia == true ? "Registros diarios" : (regismes == true ? "Registros mensuales" : "Perfil de carga"));
                    ndias = this.calculaDiasALeer(regisdia, regismes, lpowerLost); //calcula ndias
                    solicitar = true;
                    try {
                        tsfechaactual = new Timestamp(fechaActual.getTime());
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
                        byte trama[];
                        if(regisdia||regismes){
                            if (regisdia){
                                vRegistrosdia = new ArrayList<>();
                            } else {
                                vRegistrosmes = new ArrayList<>();
                            }
                            lregis = true;
                            trama = tramas.getRegistros();                 
                        } else {
                            vPerfilCarga = new ArrayList<>();
                            lperfilcarga = true;
                            trama = tramas.getPerfilcarga();
                        }
                        try {
                            trama = asignaDirecciones(trama);
                            trama = asignaInvokeIDandParity(trama);
                            Date fechaUltLec = new Date(med.getFecha().getTime());                    
                            String fechaIni = ((regisdia || regismes) ? this.getSpecificDate(sdf, false, ndias, "D") : sdf.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                            escribir("Fecha inicio de " + peticion + ": " + sdf3.format(sdf.parse(fechaIni)));
                            trama = asignaFechainiDeFiltroPorRango(trama, fechaIni);
                            //fecha final******************************************
                            Date fechaFin;
                            String sfechaFin = "";
                            if (ndias > 30) {
                                fechaFin = this.getDSpecificDate(true, 30, "D", sdf.parse(fechaIni));
                                sfechaFin = this.getSpecificDate(sdf, true, 30, "D", fechaIni);
                            } else {
                                fechaFin = this.getDCurrentDate();
                                sfechaFin = sdf.format(this.getDCurrentDate());
                            }
                            
                            escribir("Fecha final de " + peticion + ": " + sfechaFin);
                            trama = asignaFechafinDeFiltroPorRango(trama, sfechaFin);                            
                            trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22?cambiaClassnOBIS(trama,clasePerfilInt,sOBISPerfil):trama);
                            if (regisdia){
                                trama = cambiaClassnOBIS(trama,7,"0100630200FF");
                            } else if (regismes){
                                trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22?cambiaClassnOBIS(trama,clasePerfilInt,sOBISRegistro):cambiaClassnOBIS(trama,7,"0000620100FF"));
                            }
                            primerbloque = true;
                            bloqueRecibidoInt = 0;
                            trama[indxControl] = I_CTRL(nr, ns);//VIC
                            trama = calcularnuevocrcI(trama);
                            trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
                            ultimatramaEnviada = trama;                            
                            enviaTrama2(trama, "Solicitud de "+peticion+" ");
                        } catch (Exception e) {
                            escribir(getErrorString(e.getStackTrace(), 3));
                            escribir("Error creando petición de perfil de carga");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error procesando petición de perfil");
                            lprecierre = true;
                            prereset = true;
                            enviaPrelogout();
                        }
                    } else {
                        lfechaactual2 = false;
                        lprecierre = true;
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales por desfase horario");
                        escribir("Desfase horario no se solicitara "+peticion);
                        enviaPrelogout();
                    }
                }
            } else {
                escribir("Negacion de peticion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error interpretando hora del medidor");
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        }
    }
    
    public int calculaDiasALeer(boolean regisdia, boolean regismes, boolean lpowerLost) throws ParseException {
        if (med.getFecha() != null) {
            if (regisdia) {
                ndias = ndiaReg; //Número de días de registro diario
            } else if (regismes) {
                ndias = nmesReg * 30; //Número de días de registro mensual
            } else if (lpowerLost) {
                ndias = ndiaseventos;
            } else {
                try {
                    escribir("Fecha actual Colombia: " + this.getDCurrentDate());
                    if (med.getFecha() != null) {
                        escribir("Fecha Ultima Lectura " + med.getFecha());
                        long diffInMillies = Math.abs(this.getDSpecificDate(true, 1, "D").getTime() - med.getFecha().getTime());
                        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        ndias = (int) diff;
                    }
                } catch (Exception e) {                    
                    escribir("Error calculando días de diferencia. Se tomará el máximo de días (30) permitido por el aplicativo por defecto.");
                    escribir(getErrorString(e.getStackTrace(), 3));
                    ndias = 30;
                }
            }
            return ndias;
        } else {
            return 10;
        }
        
    }
 
    private byte[] asignaFechainiDeFiltroPorRango(byte trama[], String fecha) {
        String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
        while (lfecha.length() < 4) {
            lfecha = "0" + lfecha;
        }
        trama[indxControl + 42] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); //año
        trama[indxControl + 43] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
        while (lfecha.length() < 2) {
            lfecha = "0" + lfecha;
        }
        trama[indxControl + 44] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); //mes
        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
        while (lfecha.length() < 2) {
            lfecha = "0" + lfecha;
        }
        trama[indxControl + 45] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); //dia
        trama[indxControl + 47] = (byte) (Integer.parseInt(fecha.substring(8, 10)) & 0xFF); // hora 
        trama[indxControl + 48] = (byte) (Integer.parseInt(fecha.substring(10, 12)) & 0xFF); // min
        trama[indxControl + 49] = (byte) (Integer.parseInt(fecha.substring(12, 14)) & 0xFF); // seg
        return trama;
    }
    
    private byte[] asignaFechafinDeFiltroPorRango(byte trama[], String fecha) {
        String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
        while (lfecha.length() < 4) {
            lfecha = "0" + lfecha;
        }
        trama[indxControl + 56] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); //año
        trama[indxControl + 57] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
        while (lfecha.length() < 2) {
            lfecha = "0" + lfecha;
        }
        trama[indxControl + 58] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); //mes
        lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
        while (lfecha.length() < 2) {
            lfecha = "0" + lfecha;
        }
        trama[indxControl + 59] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); //dia
        trama[indxControl + 61] = (byte) (Integer.parseInt(fecha.substring(8, 10)) & 0xFF); // hora 
        trama[indxControl + 62] = (byte) (Integer.parseInt(fecha.substring(10, 12)) & 0xFF); // min
        trama[indxControl + 63] = (byte) (Integer.parseInt(fecha.substring(12, 14)) & 0xFF); // seg
        return trama;
    }
    
    public void revisarPerfil(String[] vectorhex) {
        String peticion = "Perfil de carga";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlBytePerfil(vectorhex, peticion);
        if (tramaOK) {
            try {
                tramaOK = false;
                lperfilcompleto = true;
                String vdata[] = null;
                if (primerbloque || completabloque) {//completabloque cuando se recibe un bloque con 
                    if (vectorhex[indxSacarDatos].equals("B0") & vectorhex[indxSacarDatos + 1].equals("3F") & tipoconexion != 0) {
                        escribir("Recorta formato desconocido: " + vectorhex[indxSacarDatos] + " - " + vectorhex[indxSacarDatos + 1]);
                        vdata = sacarDatos(vectorhex, indxSacarDatos + 36);
                        bytesRecortados = bytesRecortados + indxSacarDatos + 36 - 1;
                        recortaFormatoP++;
                    } else {
                        vdata = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
                        bytesRecortados = bytesRecortados + (tipoconexion == 0 ? indxData - 1 : indxSacarDatos - 1);
                    }
                    concatena = true;
                } else {
                    int longi = Integer.parseInt(vectorhex[(tipoconexion == 0 ? indxData + 9 : indxSacarDatos + 12)], 16);
                    int recortar = (tipoconexion == 0 ? indxData + 9 : indxSacarDatos + 12);
                    if (longi >= 128) {
                        recortar = recortar + (longi % 128);
                    }
                    bytesRecortados = bytesRecortados + recortar;
                    vdata = sacarDatos(vectorhex, recortar + 1);
                    vectorDatarecibido = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
                    concatena = false;
                }
                vPerfilCarga.addAll(Arrays.asList(vdata));
                if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {//vectorhex[1].equals("A9")) {//bloque con segmentación
                    escribir("Bloque segmentado");
                    //                vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                    if (concatena) {
                        String nuevoVector[] = new String[vectorDatarecibido.length + vdata.length];
                        System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                        System.arraycopy(vdata, 0, nuevoVector, vectorDatarecibido.length, vdata.length);
                        vectorDatarecibido = nuevoVector;
                    }
                    completabloque = true;
                    enviaRR();
                } else if (completabloque) {
                    escribir("Completa bloque");
                    completabloque = false;
                    if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque una vez lo completa
                        if (!enviaPerfilPorEntradas() && !bloqueincorrecto) { //se desconoce si es util para este medidor
                            lperfilcompleto = false;
                            lperfilcarga = false;
                            lregis = false;
                            concatena = false;
                            bytesRecortadosPerfil = bytesRecortados;
                            bytesRecortados = 0;
                            if (lregistros) {
                                regisdia = true;
                                enviaPeriodoDeIntegracion(); //pide todos los datos para registros diarios (regisdia en true)
                            } else if (leventos) {
                                if (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 22) {
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                    cerrarPuerto(true);
                                    escribir("Eventos no habilitado");
                                    escribir("Leido");
                                    cerrarLog("Leido", true);
                                    leer = false;
                                } else {
                                    enviaEventos(true);
                                }
                            } else {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                lprecierre = true;
                                enviaPrelogout();
                            }
                        } else {
                            escribir("Bloque incorrecto");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Bloque de perfil incorrecto");
                            lprecierre = true;
                            prereset = true;
                            lperfilcompleto = false;
                            lperfilcarga = false;
                            lregis = false;
                            concatena = false;
                            bytesRecortadosPerfil = bytesRecortados;
                            bytesRecortados = 0;
                            enviaPrelogout();
                        }
                    }
                } else {
                    if (concatena) {
                        vectorDatarecibido = vdata;//ojo -18-09-2020
                    }
                    if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque como se acaba de recibir
                        if (!enviaPerfilPorEntradas() && !bloqueincorrecto) { //se desconoce si es util para este medidor
                            lperfilcompleto = false;
                            lperfilcarga = false;
                            lregis = false;
                            concatena = false;
                            bytesRecortadosPerfil = bytesRecortados;
                            bytesRecortados = 0;
                            if (lregistros) {
                                regisdia = true;
                                enviaPeriodoDeIntegracion(); //pide todos los datos para registros diarios (regisdia en true)
                            } else if (leventos) {
                                if (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 22) {
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                    cerrarPuerto(true);
                                    escribir("Eventos no habilitado");
                                    escribir("Leido");
                                    cerrarLog("Leido", true);
                                    leer = false;
                                } else {
                                    enviaEventos(true);
                                }
                            } else {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                lprecierre = true;
                                enviaPrelogout();
                            }
                        } else {
                            escribir("Bloque incorrecto");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Bloque de perfil incorrecto");
                            lprecierre = true;
                            prereset = true;
                            lperfilcompleto = false;
                            lperfilcarga = false;
                            lregis = false;
                            concatena = false;
                            bytesRecortadosPerfil = bytesRecortados;
                            bytesRecortados = 0;
                            enviaPrelogout();
                        }
                    }
                }
            } catch (Exception e) {
                escribir("Error procesando perfil");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Error procesando perfil");
                lprecierre = true;
                prereset = true;
                lperfilcompleto = false;
                lperfilcarga = false;
                lregis = false;
                concatena = false;
                bytesRecortadosPerfil = bytesRecortados;
                bytesRecortados = 0;
                enviaPrelogout();
            }
        }
    }
    
    
    public void revisarControlBytePerfil(String[] vectorhex, String peticion){
        try {
            if (tipoconexion == 0){
                tramaOK = true;
            } else {
                if (tramaOK){
                    tramaOK = false;
                    if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x01) == 0x00) { //es informacion
                        escribir("NrM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }
                            nsEsperado++;
                            if (nsEsperado > 7) {
                                nsEsperado = 0;
                            }
                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            ReintentoRevisionTrama=0;
                            tramaOK = true;
                        } else {
                            escribir("No son los ns y nr esperados");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama "+peticion+" - Error de protocolo");
                            handleReenvio(peticion);
                        }
                    } else if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0F) == 0x01) {//es rr
                        lperfilcompleto = false;
                        if(lperfilcarga){
                            lperfilcarga = false;
                            if (lregistros){
                                regisdia = true;
                                enviaPeriodoDeIntegracion(); //pide todos los datos para registros diarios (regisdia en true)
                            } else if (leventos) {
                                if (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22){
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                    cerrarPuerto(true);                                    
                                    escribir("Eventos no habilitado");
                                    escribir("Leido");
                                    cerrarLog("Leido", true);
                                    leer = false;
                                } else {
                                    enviaEventos(true);
                                }
                            } else {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                lprecierre = true;
                                enviaPrelogout();
                            }
                        } else if(lregis){
                            lregis = false;
                            if (leventos) {
                                if (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22){
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                    cerrarPuerto(true);                                    
                                    escribir("Eventos no habilitado");
                                    escribir("Leido");
                                    cerrarLog("Leido", true);
                                    leer = false;
                                } else {
                                    enviaEventos(true);
                                }
                            } else {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                lprecierre = true;
                                enviaPrelogout();
                            }
                        }
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama "+peticion+" - Error de protocolo");
                        if (vectorhex[indxControl].equals("73")) {
                            if (ReintentoRevisionTrama<numeroReintentos){
                                ReintentoRevisionTrama++;
                                enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud "+ peticion);
                            } else {
                                ReintentoRevisionTrama=0;
                                lprecierre = true;
                                escribir("Termina lectura por incidencia en error de protocolo");
                                enviaPrelogout();
                            }
                        } else {
                            validarTipoTrama(vectorhex[indxControl]);
                        }
                    }
                }
            }
        } catch (Exception e){
            escribir(getErrorString(e.getStackTrace(), 3));
            tramaOK = false;
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama "+peticion+" - Error de protocolo");
            lprecierre = true;
            escribir("Error de protocolo \n Exception: "+e.toString());
            enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud "+ peticion);
        }
    }

    //Se desconoce si es util para este medidor, aún
    public boolean enviaPerfilPorEntradas(){
        boolean enviaPerfilEntradas = true;
//        if(vectorhex[16].equals("01")&vectorhex[17].equals("00")&(0<entradasEnUso)){
//        byte trama[] = tramas.getPerfilporentradas();
//        int numentradas = (ndias*24*60*60)/(periodoIntegracion*60);
//        int nentradasfin = entradasEnUso;
//        //entradasEnUso-numentradas es el limite inicial
////        System.out.println("Entradas debido a rango: " + numentradas);
//        if(entradasEnUso < numentradas){
//          numentradas = entradasEnUso-1;  
//          nentradasfin = 0;
////          System.out.println("Entradas a pedir: "+entradasEnUso);
//        }else{
////        System.out.println("Entradas a pedir: "+numentradas);
//        }
//        String nentradasini = Integer.toHexString(entradasEnUso - numentradas).toUpperCase();
//        while (nentradasini.length() < 8) {
//               nentradasini = "0" + nentradasini;
//           }
//       trama[29] = (byte) (Integer.parseInt(nentradasini.substring(0, 2), 16) & 0xFF);
//       trama[30] = (byte) (Integer.parseInt(nentradasini.substring(2, 4), 16) & 0xFF);
//       trama[31] = (byte) (Integer.parseInt(nentradasini.substring(4, 6), 16) & 0xFF);
//       trama[32] = (byte) (Integer.parseInt(nentradasini.substring(6, 8), 16) & 0xFF);
//
//        //entradasEnUso es el limite final
//        String nentradas = Integer.toHexString(nentradasfin).toUpperCase();
//        while (nentradas.length() < 8) {
//               nentradas = "0" + nentradas;
//           }
//       trama[34] = (byte) (Integer.parseInt(nentradas.substring(0, 2), 16) & 0xFF);
//       trama[35] = (byte) (Integer.parseInt(nentradas.substring(2, 4), 16) & 0xFF);
//       trama[36] = (byte) (Integer.parseInt(nentradas.substring(4, 6), 16) & 0xFF);
//       trama[37] = (byte) (Integer.parseInt(nentradas.substring(6, 8), 16) & 0xFF);
//
//       vPerfilCarga = new ArrayList<String>();
//       trama = asignaDireciones(trama);
//       primerbloque = true;
//       trama[indxControl] = I_CTRL(nr, ns);//VIC
//       trama = calcularnuevocrcI(trama);
//       ultimatramaEnviada = trama;//       
//       enviaTrama2(trama, "Solicitud de perfil de carga - filtro por entradas");
//        } else {
            enviaPerfilEntradas = false;
//        }
        return enviaPerfilEntradas;
    }
    
    public void enviaRR(){
        byte trama[] = tramas.getRR();
        trama = asignaDirecciones(trama);
        trama[indxControl] = RR_CTRL(nr);//VIC
        ns--;
        nrEsperado--;
        if (ns < 0) {
            ns = 7;
        }
        if (nrEsperado < 0) {
            nrEsperado = 7;
        }
        trama = calcularnuevocrcRR(trama);
        ultimatramaEnviada = trama;
        enviaTrama2(trama, "=> Envia Recieved Ready RR");
    }

    public void revisarRegistros(String[] vectorhex) throws ParseException {
        String peticion = (regisdia == true ? "Registros diarios" : "Registros mensuales");
        vectorhex = revisarHeaderYcrc (vectorhex,peticion);
        revisarControlBytePerfil(vectorhex,peticion);
        if (tramaOK){
            tramaOK = false;
            lperfilcompleto = true;
            String vdata[] = null;
            if (primerbloque||completabloque) {//completabloque cuando se recibe un bloque con 
                if(vectorhex[indxSacarDatos].equals("B0")&vectorhex[indxSacarDatos+1].equals("3F")&tipoconexion!=0){
                    escribir("Recorta formato desconocido: "+vectorhex[indxSacarDatos]+" - "+vectorhex[indxSacarDatos+1]);
                    vdata = sacarDatos(vectorhex, indxSacarDatos+36);
                    bytesRecortados = bytesRecortados + indxSacarDatos+36-1; 
                    recortaFormatoR++;
                } else {
                    vdata = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
                    bytesRecortados = bytesRecortados + (tipoconexion == 0 ? indxData-1 : indxSacarDatos-1);
                }
                concatena = true;
            } else {
                int longi = Integer.parseInt(vectorhex[(tipoconexion == 0 ? indxData+9 : indxSacarDatos+12)], 16);
                int recortar = (tipoconexion == 0 ? indxData+9 : indxSacarDatos+12);
                if (longi >= 128) {
                    recortar = recortar + (longi % 128);
                }
                bytesRecortados = bytesRecortados + recortar;
                vdata = sacarDatos(vectorhex, recortar + 1);
                vectorDatarecibido = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
                concatena = false;
            }
            if(regisdia){
                vRegistrosdia.addAll(Arrays.asList(vdata));
            } else if (regismes){
                vRegistrosmes.addAll(Arrays.asList(vdata));
            }
            if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08){//vectorhex[1].equals("A9")) {//bloque con segmentación
                escribir("Bloque segmentado");
//                vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                if(concatena){
                    String nuevoVector[] = new String[vectorDatarecibido.length + vdata.length];
                    System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                    System.arraycopy(vdata, 0, nuevoVector, vectorDatarecibido.length, vdata.length);
                    vectorDatarecibido = nuevoVector;
                }
                completabloque = true;
                enviaRR();
            } else if (completabloque){
                escribir("Completa bloque");
                completabloque = false;
                if (!enviaREQ_NEXT(vectorDatarecibido)){//pide el bloque una vez lo completa
                    if (!bloqueincorrecto){
                        lperfilcompleto = false;
                        lregis = false;
                        if(regisdia){
                            regisdia = false;
                            regismes = true;
                        } else {
                           regismes = (regismes?!regismes:regismes);//si viene true ya se leyó 
                        }  
                        concatena = false;
                        bytesRecortadosRegistros = bytesRecortados;
                        bytesRecortados = 0;
                        if (regisdia||regismes){
                          enviaPeriodoDeIntegracion(); //pide todos los datos para registros mensuales (regismes en true)  
                        } else if (leventos) {
                            if (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22){
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                cerrarPuerto(true);                                
                                escribir("Eventos no habilitado");
                                escribir("Leido");
                                cerrarLog("Leido", true);
                                leer = false;
                            } else {
                                enviaEventos(true);
                            }
                        } else {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                            lprecierre = true;
                            enviaPrelogout();
                        }
                    }  else {
                        escribir("Bloque incorrecto");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Bloque de perfil incorrecto");
                        lprecierre = true;
                        prereset = true;
                        enviaPrelogout();
                    }
                }     
            } else {
                if(concatena){
                    vectorDatarecibido = vdata;//ojo -18-09-2020
                }
                if (!enviaREQ_NEXT(vectorDatarecibido)){//pide el bloque como se acaba de recibir
                    if(!bloqueincorrecto){
                        lperfilcompleto = false;
                        lregis = false;
                        if(regisdia){
                            regisdia = false;
                            regismes = true;
                        } else {
                           regismes = (regismes?!regismes:regismes);//si viene true ya se leyó 
                        }  
                        concatena = false;
                        bytesRecortadosRegistros = bytesRecortados;
                        bytesRecortados = 0;
                        if (regisdia||regismes){
                          enviaPeriodoDeIntegracion(); //pide todos los datos para registros mensuales (regismes en true)  
                        } else if (leventos) {
                            if (Integer.parseInt(med.getMarcaMedidor().getCodigo())==22){
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                                cerrarPuerto(true);                                
                                escribir("Eventos no habilitado");
                                escribir("Leido");
                                cerrarLog("Leido", true);
                                leer = false;
                            } else {
                                enviaEventos(true);
                            }
                        } else {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                            lprecierre = true;
                            enviaPrelogout();
                        }
                    } else {
                        escribir("Bloque incorrecto");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Bloque de perfil incorrecto");
                        lprecierre = true;
                        prereset = true;
                        enviaPrelogout();
                    }
                }    
            }
        }
    }
    
    public void revisarEventos(String[] vectorhex) throws ParseException {
        String peticion = "Eventos";
        vectorhex = revisarHeaderYcrc (vectorhex,peticion);
        revisarControlByte(vectorhex,peticion);
        if (tramaOK){
            tramaOK = false;
            String[] vectorData = null;
            if (primerbloque||completabloque) {//completabloque cuando se recibe un bloque con 
                if(vectorhex[indxSacarDatos].equals("B0")&vectorhex[indxSacarDatos+1].equals("3F")&tipoconexion!=0){
                    escribir("Recorta formato desconocido: "+vectorhex[indxSacarDatos]+" - "+vectorhex[indxSacarDatos+1]);
                    vectorData = sacarDatos(vectorhex, indxSacarDatos+36);
                    bytesRecortados = bytesRecortados + indxSacarDatos+36-1; 
                    recortaFormatoE++;
                } else {
                    vectorData = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
                    bytesRecortados = bytesRecortados + (tipoconexion == 0 ? indxData-1 : indxSacarDatos-1);
                }
                concatena = true;
            } else {
                int longi = Integer.parseInt(vectorhex[(tipoconexion == 0 ? indxData+9 : indxSacarDatos+12)], 16);
                int recortar = (tipoconexion == 0 ? indxData+9 : indxSacarDatos+12);
                if (longi >= 128) {
                    recortar = recortar + (longi % 128);
                }
                bytesRecortados = bytesRecortados + recortar;
                vectorData = sacarDatos(vectorhex, recortar + 1);
                vectorDatarecibido = (tipoconexion == 0 ? sacarDatos(vectorhex, indxData) : sacarDatos(vectorhex, indxSacarDatos));
                concatena = false;
            }
            listEventos.addAll(Arrays.asList(vectorData));
            if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08){//vectorhex[1].equals("A9")) {//bloque con segmentación
                escribir("Bloque segmentado");
//                vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                if(concatena){
                    String nuevoVector[] = new String[vectorDatarecibido.length + vectorData.length];
                    System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                    System.arraycopy(vectorData, 0, nuevoVector, vectorDatarecibido.length, vectorData.length);
                    vectorDatarecibido = nuevoVector;//Lo que hace es completar el bloque de capa de aplicación 
                }
                completabloque = true;
                enviaRR();
            } else if (completabloque){
                escribir("Completa bloque");
                completabloque = false;
                if (!enviaREQ_NEXT(vectorDatarecibido)){//pide el bloque una vez lo completa
                    if (!bloqueincorrecto){
                        lpowerLost = false;
                        lprecierre = true;
                        concatena = false;
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                        enviaPrelogout();
                    } else {
                        escribir("Bloque incorrecto");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Bloque de perfil incorrecto");
                        lprecierre = true;
                        prereset = true;
                        enviaPrelogout();
                    }
                }      
            } else { 
                if(concatena){
                    vectorDatarecibido = vectorData;//ojo -18-09-2020
                }
                if (!enviaREQ_NEXT(vectorDatarecibido)){//pide el bloque como se acaba de recibir
                    if(!bloqueincorrecto){
                        lpowerLost = false;
                        lprecierre = true;
                        concatena = false;
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leído - Sin envío de peticiones adicionales");
                        enviaPrelogout();
                    } else {
                        escribir("Bloque incorrecto");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexión - Bloque de perfil incorrecto");
                        lprecierre = true;
                        prereset = true;
                        enviaPrelogout();
                    }
                }
            }             
        }
    }
    
    public void enviaReleaseRequest(int reason){
        ReintentoRLRQ++;
        byte trama[] = tramas.getRelReq();
        trama[trama.length-4] = (byte) (Integer.parseInt(Integer.toHexString(reason), 16) & 0xFF);
        trama = asignaDirecciones(trama);        
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama); 
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);             
        enviaTrama2(trama, "Release Request");
    }
    
    
    public void revisarReleaseRequest(String[] vectorhex) {
        String peticion = "Release Request";
        revisarHeaderYcrc (vectorhex,peticion);
        if (tramaOK){
            tramaOK = false;
            if (vectorhex[indxData].equals("63") && vectorhex[indxData + 4].equals("00")){
                lRelReq = false;
                prereset = false;
                escribir("Preparado para reiniciar sesion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama "+peticion+" - Preparado para reiniciar sesion");
                if (tipoconexion == 0){
                    try {
                        revisarReset(vectorhex);
                    } catch (Exception e) {
                        escribir(getErrorString(e.getStackTrace(), 3));
                    }
                } else {
                    lcierrapuertos = true;
                    enviaLogout();
                }
            } else {
                if(vectorhex[indxData].equals("63") && vectorhex[indxData + 4].equals("01")){
                    escribir("Negación de liberación de solicitud");
                } else {
                    escribir("Respuesta a liberación de solicitud desconocida");
                }
                if (ReintentoRLRQ<numeroReintentos){
                    escribir((ReintentoRLRQ==1?"Cambio a RLRQ para liberación de solicitud de Alta Prioridad":"RLRQ de Alta prioridad"));
                    enviaReleaseRequest(1);
                } else {
                    escribir("Liberación de solicitud no aceptada");
                    ReintentoRLRQ=0;
                    if (tipoconexion == 0) {
                        revisarReset(vectorhex);              
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "No leído - Sin envío de peticiones adicionales");
                        cerrarPuerto(true);                        
                        escribir("Estado Lectura No leido");
                        cerrarLog("Desconexion RLRQ", false);
                        leer = false;
                    }
                } 
            }
        }
    }
    
    public void enviaPrelogout(){
        byte trama[] = tramas.getPrelogout();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama); 
        trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);             
        enviaTrama2(trama, "preLogout");
    }
    
    public void revisarPrelogout(String[] vectorhex) {
        String peticion = "preLogout";
        revisarHeaderYcrc(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lprecierre = false;
            escribir("Preparado para cerrar sesion");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta Trama " + peticion + " - Preparado para cerrar sesion");
            if (prereset) {
                lReset = true;
            } else {
                lcierrapuertos = true;
            }
            if (tipoconexion == 0) {
                if (lReset) {
                    revisarReset(vectorhex);
                } else {
                    revisarLogout(vectorhex);
                }
            } else {
                enviaLogout();
            }
        }
    }
    
    public void enviaLogout(){        
        byte trama[] = tramas.getLogout();
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcRR(trama);
        enviaTrama2(trama, "Logout");
    }
    
    public void revisarLogout(String[] vectorhex) {
        String peticion = "Logout";
        revisarHeaderYcrc(vectorhex, peticion);
        tramaOK = false;
        lcierrapuertos = false;
        cerrarPuerto(false);
        boolean l = false;
        almacenaDatos();
        if (!lperfil && !leventos && !lregistros) {
            l = true;
        }
        if (lconfhora && clockSincronizado) {
            l = true;
        }
        if (l || vlec.size() > 0 || registrosprocesadosE > 0 || (vRegistrosdia != null && vRegistrosdia.size() > 0) || (vRegistrosmes != null && vRegistrosmes.size() > 0)) {            
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Verificación estado de lectura - " + aviso);
            escribir("Estado Lectura " + aviso);
            cerrarLog("Leido", true);
        } else {
            if (procesaIncompleto) {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Verificación estado de lectura - No Leído");
                escribir("Estado Lectura No leido");
                cerrarLog("No leido", false);
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Verificación estado de lectura - Leído - Sin Datos");
                escribir("Estado Lectura Leido - Sin Datos");
                cerrarLog("Leido", true);
            }
        }
        leer = false;
    }

    public void revisarReset(String[] vectorhex) {
        String peticion = "Logout para reinicio";
        revisarHeaderYcrc(vectorhex, peticion);
        tramaOK = false;
        lReset = false;
        prereset = false;
        escribir("Inicia procesamiento de contingencia en caso de datos a procesar");
        almacenaDatos();
        reiniciaComunicacion(false);

    }

    //calculos para construcción o interpretación de tramas
    private String[] cortarTrama(String[] vectorhex, int tamaño) {
        String nuevoVector[] = new String[tamaño + 2];
        System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
        for (int i = 0; i < nuevoVector.length; i++) {
            System.out.print(" " + nuevoVector[i]);
        }
        return nuevoVector;
    }

    private byte[] calcularnuevocrcRR(byte[] siguientetrama) {
        siguientetrama[indxLength] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
        byte[] data = new byte[siguientetrama.length - 4];
        for (int i = 0; i < data.length; i++) {
            data[i] = siguientetrama[i + 1];
        }
        int crc = calculoFCS(data);
        String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
        //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
        if (stxcrc.length() == 3) {
            stxcrc = "0" + stxcrc;
        } else if (stxcrc.length() == 2) {
            stxcrc = "00" + stxcrc;
        } else if (stxcrc.length() == 1) {
            stxcrc = "000" + stxcrc;
        }
        siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
        siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);

        return siguientetrama;
    }

    public byte[] calcularnuevocrcI(byte[] siguientetrama) {
        try {
            siguientetrama[indxLength] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
            byte[] data = new byte[indxControl];
            for (int i = 0; i < data.length; i++) {
                data[i] = siguientetrama[i + 1];
            }
            int crc = calculoFCS(data);
            String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
            //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
            if (stxcrc.length() == 3) {
                stxcrc = "0" + stxcrc;
            } else if (stxcrc.length() == 2) {
                stxcrc = "00" + stxcrc;
            } else if (stxcrc.length() == 1) {
                stxcrc = "000" + stxcrc;
            }
            siguientetrama[indxhcs1] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF); //21-10-19
            siguientetrama[indxhcs2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF); 
            data = new byte[siguientetrama.length - 4];
            for (int i = 0; i < data.length; i++) {
                data[i] = siguientetrama[i + 1];
            }
            crc = calculoFCS(data);
            stxcrc = "" + Integer.toHexString(crc).toUpperCase();
            //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
            if (stxcrc.length() == 3) {
                stxcrc = "0" + stxcrc;
            } else if (stxcrc.length() == 2) {
                stxcrc = "00" + stxcrc;
            } else if (stxcrc.length() == 1) {
                stxcrc = "000" + stxcrc;
            }
            siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }        
        return siguientetrama;
    }

    public boolean validacionCRCHCS(String[] data) {
        boolean lcrc = false;
        byte b[] = new byte[indxControl];
        for (int j = 0; j < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        String stx = data[indxhcs1] + "" + data[indxhcs2]; 
        String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
        //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
        if (stxcrc.length() == 3) {
            stxcrc = "0" + stxcrc;
        } else if (stxcrc.length() == 2) {
            stxcrc = "00" + stxcrc;
        } else if (stxcrc.length() == 1) {
            stxcrc = "000" + stxcrc;
        }
        if (stx.equals(stxcrc)) {
            lcrc = true;
        }
        return lcrc;
    }

    public static int calculoFCS(byte[] bytes) {
        int crcaux1 = 0x00000000;      //auxiliar para el calculo final
        int crc = 0x0000FFFF;          // initial value
        int polynomial = 0x1021;   // polinomio generador 0001 0000 0010 0001  (0, 5, 12)  CRC16-CCITT
        //revertir la trama de bytes
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (Integer.reverse(bytes[i]) >>> (Integer.SIZE - Byte.SIZE));
        }
        //Calculo del fcs
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        //garantizar que el FCS sea de 16 bits
        crc &= 0x0000ffff;
        //invertir el FCS
        crc = ~crc;
        //reflejar las tramas en grupos de 16 bits usando el metodo reflect
        crc = reflect(crc, true);
        crcaux1 = (short) (((crc >> 8) & 0x000000ff) + (crc << 8 & 0x0000ff00));
        crcaux1 = crcaux1 & 0x0000FFFF;
        return crcaux1;
    }
    
    public static short reflect(int crc, boolean order) {
        //refleja la parte baja de los 'bitnum' los bits de "CRC"
        //ENVIAR TRUE SI SE DEBE REFLEJAR EN EL ORDER Y ENVIAR FALSE SI SE REFLEJA 8 BITS
        short crcaux, i, j = 1, crcout = 0;
        crcaux = (short) (Integer.reverse(crc) >>> (16));
        if (!order) {
            crcaux = (short) (((crcaux >> 8) & 0x000000ff) + (crcaux << 8 & 0x0000ff00));
        }
        crcout = (short) (crcaux & 0x00000000FFFF);
        return crcout;
    }

    private static boolean validacionCRCFCS(String[] data) {
        boolean lcrc = false;
        byte b[] = new byte[data.length - 4];
        for (int j = 0; j
                < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        String stx = data[data.length - 3] + "" + data[data.length - 2];
        String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
        //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
        if (stxcrc.length() == 3) {
            stxcrc = "0" + stxcrc;
        } else if (stxcrc.length() == 2) {
            stxcrc = "00" + stxcrc;
        } else if (stxcrc.length() == 1) {
            stxcrc = "000" + stxcrc;
        }
        if (stx.equals(stxcrc)) {
            lcrc = true;
        }
        return lcrc;
    }
    private byte[] asignaDirecciones(byte[] frame) {
        byte[] frameTemp;
        int newLength;
        int logAddr = numBytesDir == 1? (dirlog * 2) + 1 :(dirlog * 2);
        int phyAddr = (dirfis * 2) + 1;
        String sdirlog;
        String sdirfis;
        //System.out.println("Número de Bytes de Dirección: " + numBytesDir);
        try {
            switch (numBytesDir) {
                case 1: // solo dirección lógica   
                    //System.out.println("Longitud trama temporal: "+(frame.length - 3));
                    frameTemp = new byte[frame.length - 3];
                    System.arraycopy(frame, 0, frameTemp, 0, 4);
                    System.arraycopy(frame, 7, frameTemp, 4, frame.length - 7);
                    newLength = (int) ((((frame[1] << 8) & 0x0700) + (frame[2] & 0xFF)) - 3);
                    //System.out.println("Nueva longitud: "+newLength);
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    if (sdirlog.length() == 1){
                        sdirlog = "0" + sdirlog;   
                    }
                    //System.out.println("Dirección lógica : "+sdirlog);
                    frameTemp[1] = (byte) ((byte) 0xA0 + (byte) (newLength & 0x0700));
                    frameTemp[2] = (byte) (newLength & 0xFF);
                    frameTemp[3] = (byte) (Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF);
                    frameTemp[4] = (byte) ((users[indxuser]*2) + 1);
                    return frameTemp;
                case 2: // Dirección lógica (1 byte) y dirección física ( 1 byte)
                    frameTemp = new byte[frame.length - 2];
                    System.arraycopy(frame, 0, frameTemp, 0, 5);
                    System.arraycopy(frame, 7, frameTemp, 5, frame.length - 7);
                    newLength = (int) ((((frame[1] << 8) & 0x0700) + (frame[2] & 0xFF)) - 2);
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    sdirfis = Integer.toHexString(phyAddr).toUpperCase();
                    if (sdirlog.length() == 1){
                        sdirlog = "0" + sdirlog;   
                    }
                    if (sdirfis.length() == 1){
                        sdirfis = "0" + sdirfis;   
                    }
                    frameTemp[1] = (byte) ((byte) 0xA0 + (byte) (newLength & 0x0700));
                    frameTemp[2] = (byte) (newLength & 0xFF);
                    frameTemp[3] = (byte) (Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF);
                    frameTemp[4] = (byte) (Integer.parseInt(sdirfis.substring(0, 2), 16) & 0xFF);
                    frameTemp[5] = (byte) ((users[indxuser]*2) + 1);
                    return frameTemp;
                case 4: // Dirección lógica (2 bytes) y dirección física (2 bytes)
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    sdirfis = Integer.toHexString(phyAddr).toUpperCase();
                    while (sdirlog.length() < 4) {
                        sdirlog = "0" + sdirlog;
                    }
                    while (sdirfis.length() < 4) {
                        sdirfis = "0" + sdirfis;
                    }
                    frame[3] = (byte) ((Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF) << 1);
                    frame[4] = (byte) (Integer.parseInt(sdirlog.substring(2, 4), 16) & 0xFF);
                    frame[5] = (byte) ((Integer.parseInt(sdirfis.substring(0, 2), 16) & 0xFF) << 1);
                    frame[6] = (byte) (Integer.parseInt(sdirfis.substring(2, 4), 16) & 0xFF);
                    frame[7] = (byte) ((users[indxuser]*2) + 1);
                    return frame;
                default:
                    //System.out.println("La cantidad de bytes de dirección en HDLC solo puede ser de 1, 2 o 4 bytes, no existe otra cantidad permitida.");
                    escribir("La cantidad de bytes de dirección en HDLC solo puede ser de 1, 2 o 4 bytes, no existe otra cantidad permitida.");
                    return frame;
            }
        } catch (Exception e){
            //System.out.println(e.getStackTrace()[0]);
            return frame;
        }
    }

    
    private ArrayList<String> seleccionaNumBytesDir( ArrayList<String> tr){
        //casos número de bytes //se agregan los bytes de la dirección del medidor
        switch (numBytesDir) {
            case 1: {
                //sólo se tiene dirección lógica y se desplaza con uno a la izquierda
                String sdirlog = Integer.toHexString(((dirlog*2)+1)& 0xFF).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                tr.add(sdirlog);
                break;
            }
            case 2: {
                //Se tiene dirección lógica en la parte alta y física en la baja
                //La lógica se desplaza con cero a la izquierda
                String sdirlog = Integer.toHexString((dirlog*2)& 0xFF).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                tr.add(sdirlog);
                //la física se desplaza con uno a la izquierda
                String sdirfis = Integer.toHexString(((dirfis*2)+1)& 0xFF).toUpperCase();
                while (sdirfis.length() < 2) {
                    sdirfis = "0" + sdirfis;
                }
                tr.add(sdirfis);
                break;
            }
            case 4: {
                //Se tiene dirección lógica en la parte alta y física en la baja
                //La parte alta y baja de la lógica se desplaza con cero a la izquierda
                String sdirlog = Integer.toHexString(((dirlog*2)& 0xFF00)>>7).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                //System.out.println("sdirlogL: "+sdirlog);
                tr.add(sdirlog);
                sdirlog = Integer.toHexString(((dirlog*2)& 0xFF)).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                //System.out.println("sdirlogH: "+sdirlog);
                tr.add(sdirlog);
                //la parte alta de la física se desplaza con cero a la izquierda y la baja con uno
                String sdirfis = Integer.toHexString(((dirfis*2)& 0xFF00)>>7).toUpperCase();
                while (sdirfis.length() < 2) {
                    sdirfis = "0" + sdirfis;
                }
                //System.out.println("sdirfisL: "+sdirfis);
                tr.add(sdirfis);
                sdirfis = Integer.toHexString(((dirfis*2)+1)& 0xFF).toUpperCase();
                while (sdirfis.length() < 2) {
                    sdirfis = "0" + sdirfis;
                }
                //System.out.println("sdirfisH: "+sdirfis);
                tr.add(sdirfis);
                break;
            }
            default: {
                //System.out.println("Número de bytes erróneo\n----------------------------");
                tr.add("03");
            }
        }
        return tr;
    }
    
    private void obtieneDirFis(String[] vectorhex) throws SQLException{
        String sDirObtenida="";
        int nBytes=(numBytesDir!=1?numBytesDir/2:0);
        for (int j = indxLength+1+nBytes; j < indxLength+1+numBytesDir; j++) { 
            sDirObtenida=sDirObtenida+vectorhex[j+1];
        }
        //System.out.println("sDirObtenida: "+sDirObtenida);
        int PhyAdd= Integer.parseInt(String.valueOf(Long.parseLong(sDirObtenida, 16))) ;
        //System.out.println("sDirObtenidaInt: "+PhyAdd);
        String sdirfisL = Integer.toHexString((PhyAdd/2)& 0xFF).toUpperCase();
        //casos número de bytes //se agregan los bytes de la dirección del medidor
        switch (numBytesDir) {
            case 1: {
                //System.out.println("No emplea Physical address \n----------------------------");
                break;
            }
            case 2: {
                //Se tiene dirección lógica en la parte alta y física en la baja
                //La lógica se desplaza con cero a la izquierda
                //System.out.println("sdirfis: "+sdirfisL);
                PhyAdd= Integer.parseInt(String.valueOf(Long.parseLong(sdirfisL, 16)),16);
                //System.out.println("dirfisInt: "+PhyAdd);
                break;
            }
            case 4: {
                //Se tiene dirección lógica en la parte alta y física en la baja
                //La parte alta y baja de la lógica se desplaza con cero a la izquierda
                //System.out.println("sdirfisL: "+sdirfisL);
                String sdirfisH = Integer.toHexString(((PhyAdd/4)>>8)& 0xFF).toUpperCase();
                //System.out.println("sdirfisH: "+sdirfisH);
                //System.out.println("sdirfis: "+sdirfisH+sdirfisL);
    ////            System.out.println("dirfisInt: "+String.valueOf(Long.parseLong(sdirfisH+sdirfisL, 16)));
                PhyAdd= Integer.parseInt(String.valueOf(Long.parseLong(sdirfisH+sdirfisL, 16)));
                //System.out.println("dirfisInt: "+PhyAdd);
                break;
            }
            default: {
                //System.out.println("Número de bytes erróneo\n----------------------------");
            }
        }
        if(PhyAdd!=dirfis){//si es diferente lo actualiza
            escribir("Asignación nueva de direccion fisica - Anterior: "+dirfis+ " - Nueva: "+PhyAdd);
//            dirfis = PhyAdd;
//            cp.actualizaDirFis(med.getnSerie(), dirfis);
            //System.out.println("\nDireccion Fisica: " + dirfis);
        }
    }
    
    private byte[] cambiaClassnOBIS(byte[] trama, int clase, String sOBIS) { //0100010800FF
        //clase
        trama[indxControl+10] = (byte) (Integer.parseInt(Integer.toHexString(clase), 16) & 0xFF);
        //A
        trama[indxControl+11] = (byte) (Integer.parseInt(sOBIS.substring(0, 2), 16) & 0xFF);
        //B
        trama[indxControl+12] = (byte) (Integer.parseInt(sOBIS.substring(2, 4), 16) & 0xFF);
        //C
        trama[indxControl+13] = (byte) (Integer.parseInt(sOBIS.substring(4, 6), 16) & 0xFF);
        //D
        trama[indxControl+14] = (byte) (Integer.parseInt(sOBIS.substring(6, 8), 16) & 0xFF);
        //E
        trama[indxControl+15] = (byte) (Integer.parseInt(sOBIS.substring(8, 10), 16) & 0xFF);
        //F
        trama[indxControl+16] = (byte) (Integer.parseInt(sOBIS.substring(10, 12), 16) & 0xFF);
        return trama;
    }
    
    
    private byte[] asignaInvokeIDandParity(byte[] trama){
        //Para Hexing el invonke ID and Parity incrementa de 0xC1 (193) a 0xCF (207)
        int posInvokeIDandParity = indxControl+8; 
        trama[posInvokeIDandParity] = (byte) (Integer.parseInt(Integer.toHexString(InvokeIDandParity), 16) & 0xFF);
        InvokeIDandParity++;
        if(InvokeIDandParity == 208){
            InvokeIDandParity = 193;
        }
        return trama;
    }
    
    private byte[] reconstruirTrama (byte [] t){ //21-10-19
        ArrayList<String> trama = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            trama.add(Integer.toHexString(t[i] & 0xFF).toUpperCase());
        }
        for (int i = 5; i < t.length; i++) {
            trama.add(Integer.toHexString(t[i] & 0xFF).toUpperCase());
        }
        //copia trama a arreglo de bytes
        byte[] tramabyte = new byte[trama.size()];
        int i = 0;
        for (String tr : trama) {
            tramabyte[i] = (byte) (Integer.parseInt(tr, 16) & 0xFF);
            i++;
        }
        tramabyte[3] = (byte) (Integer.parseInt(Integer.toHexString((dirlog*2)+1), 16) & 0xFF);
        
        return tramabyte;
    }
    
    private byte[] crearAARQ(byte[] t3, String pass) {
        ArrayList<String> trama = new ArrayList<>();
        trama.add("7E");
        trama.add("A0");
        //pass = null; //vic 27-06-19 ojo!
        if (pass == null) {
            //System.out.println("/n Entre");
            t3 = tramas.getAarq();

//        trama.add(Integer.toHexString((63 + 8)).toUpperCase());
            for (int i = 2; i < t3.length; i++) {//vic 44
                trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
            }
        } else {            
            for (int i = 2; i < 60; i++) {//vic 44                
                trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
            }
            //pass
            for (int i = 0; i < pass.length(); i++) {
                trama.add(convertStringToHex(pass.substring(i, i + 1)).toUpperCase());
            }

            //datos 2da parte
            for (int i = 60 + pass.length(); i < t3.length; i++) {//vic 52
                trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
            }
        }
        //copia trama a arreglo de bytes
        byte[] tramabyte = new byte[trama.size()];
        int i = 0;
        for (String t : trama) {
            tramabyte[i] = (byte) (Integer.parseInt(t, 16) & 0xFF);
            i++;
        }

        if (pass == null) {
            //tamaño Data
            tramabyte[15] = (byte) (Integer.parseInt(Integer.toHexString((50 + 7)), 16) & 0xFF);//(byte) (Integer.parseInt(Integer.toHexString((46 + pass.length())), 16) & 0xFF);
            tramabyte[69] = (byte) (Integer.parseInt(Integer.toHexString(27), 16) & 0xFF);
            tramabyte[70] = (byte) (Integer.parseInt(Integer.toHexString(32), 16) & 0xFF);
            tramabyte[71] = (byte) (Integer.parseInt(Integer.toHexString(0), 16) & 0xFF);
        } else {
            //tamaño Data            
            tramabyte[15] = (byte) (Integer.parseInt(Integer.toHexString((62 + pass.length())), 16) & 0xFF);
            //Tamaño Password
            tramabyte[57] = (byte) (Integer.parseInt(Integer.toHexString(pass.length() + 2), 16) & 0xFF);
            tramabyte[59] = (byte) (Integer.parseInt(Integer.toHexString(pass.length()), 16) & 0xFF);
            //conformance-block
            tramabyte[tramabyte.length - 8] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);
            tramabyte[tramabyte.length - 7] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);
            tramabyte[tramabyte.length - 6] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);
            // Max PDU Size 
            tramabyte[tramabyte.length - 5] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);//Parametrizable desde Base de datos
            tramabyte[tramabyte.length - 4] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);
        }

        return tramabyte;
    }
    
    private byte[] construirTramaGPRS(byte[] trTCP) {
        ArrayList<String> trama = new ArrayList<>();
        //wrapper TCP
        for (int i = 0; i < wrapperGPRS[indxUsrGPRS].length(); i=i+2) {
            trama.add(wrapperGPRS[indxUsrGPRS].substring(i, i + 2).toUpperCase());
        }
        if(lRelReq){trama.set(3, "01");}
        //lenght
        trama.add("00");
        trama.add("00");
        //data
        for (int i = indxSacarDatos + 3; i < trTCP.length-3; i++) {//indx de TCP + 3 (después de E6 E6 00)
            trama.add(Integer.toHexString(trTCP[i] & 0xFF).toUpperCase());
        }
        //copia trama a arreglo de bytes
        byte[] tramabyte = new byte[trama.size()];
        int i = 0;
        for (String t : trama) {
            tramabyte[i] = (byte) (Integer.parseInt(t, 16) & 0xFF);
            i++;
        }
        //tamaño Data
        tramabyte[7] = (byte) (Integer.parseInt(Integer.toHexString(tramabyte.length - 8), 16) & 0xFF);
        
        return tramabyte;
    }
    
    private byte[] construirConstant(String obis) {
        //System.out.println("\nOBIS a solicitar: " + obis);
        byte trama[] = tramas.getConstant();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = cambiaClassnOBIS(trama,3,obis);
        trama = calcularnuevocrcI(trama);
        return trama;
    }
    
    public boolean enviaREQ_NEXT(String[] vectorhex){
        boolean enviaREQ = true;
        vectorDatarecibido = new String[0];
        int indxAux = (tipoconexion == 0 ? 1 : 4);
        if (vectorhex[indxAux].equals("02")){//bloques en capa de aplicacion
            int bloquerecibidoaux = Integer.parseInt(vectorhex[indxAux+3] + vectorhex[indxAux+4] + vectorhex[indxAux+5] + vectorhex[indxAux+6], 16);
//            int bloqueanterioraux = Integer.parseInt(bloqueRecibidoAnt[0] + bloqueRecibidoAnt[1] + bloqueRecibidoAnt[2] + bloqueRecibidoAnt[3],16);
            if (bloquerecibidoaux>bloqueRecibidoInt){//bloque recibido es mayor al anterior
                if(vectorhex[indxAux+2].equals("00")) {
                    byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[indxAux+3], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[indxAux+4], 16) & 0xFF),
                        (byte) (Integer.parseInt(vectorhex[indxAux+5], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[indxAux+6], 16) & 0xFF)};
                    bloqueRecibidoAnt = bloqueRecibido;
                    bloqueRecibidoInt = bloquerecibidoaux;
                    byte trama[] = crearREQ_NEXT(bloqueRecibido);
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Siguiente bloque");
                    trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
                    ultimatramaEnviada = trama;
                    reintentoBloque=0;
                    primerbloque = false;
                    bloqueincorrecto = false;
                    enviaTrama2(trama, "=> Envia Next Data Block Request");
                } else {
                    bloqueincorrecto = false;//bloque esperado es ultimo bloque
                    enviaREQ = false;
                }
            } else {//no es el bloque esperado
                if (reintentoBloque<numeroReintentos){
                    reintentoBloque++;
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Envío petición - Reenvío Siguiente Bloque");
                    enviaTrama2(ultimatramaEnviada, "=> Re-Envia Next Data Block Request");
                } else {
                    reintentoBloque=0;
                    bloqueincorrecto = true;
                    enviaREQ = false;
                }
            }
        } else {//no es division por bloques
            bloqueincorrecto = false;
            enviaREQ = false;
        }
        return enviaREQ;
    }
    
    private byte[] crearREQ_NEXT(byte[] bloqueRecibido) {
        byte trama[] = tramas.getREQ_NEXT();
        trama = asignaDirecciones(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        System.arraycopy(bloqueRecibido, 0, trama, indxControl + 9, bloqueRecibido.length);
        trama = calcularnuevocrcI(trama);

        return trama;
    }
    
    public String convertStringToHex(String str) {
        ////System.out.println("cadena a transformar " + str);
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
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

    public String Hex2Date(String dato) {
        String temp = "";
        temp = temp + Integer.parseInt(dato.substring(6, 8), 16) + "/";
        temp = temp + Integer.parseInt(dato.substring(4, 6), 16) + "/";
        temp = temp + Integer.parseInt(dato.substring(0, 4), 16) + " ";
        temp = temp + Integer.parseInt(dato.substring(10, 12), 16) + ":";
        temp = temp + Integer.parseInt(dato.substring(12, 14), 16) + ":";
        temp = temp + Integer.parseInt(dato.substring(14, 16), 16);
        return temp;
    }
    
    public byte RR_CTRL(int nr) {
        return (byte) (((byte) (nr << 5)) | 0x11);
    }

    public byte I_CTRL(int nr, int ns) {
        return (byte) ((byte) ((byte) ((nr << 5) | (ns << 1)) | 0x10) & 0xFE);
    }

     private void validarTipoTrama(String tipotrama) {
        switch (tipotrama) {
            case "97":
                escribir("Frame Reject. Desconexion");  
                break;
            case "1F":
                escribir("Estado Lectura No leido");                
                break;
            default:
                escribir("Trama no reconocida");
                break;                                     
        }
        reiniciaComunicacion(true);    
    }
    
    public void almacenaDatos() {        
        try {
            if (lperfil) {
                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                    //System.out.println("vPerfilCarga: "+vPerfilCarga);                    
                    registrosAprocesar = registrosprocesados = 0;
                    recortaFormato = recortaFormatoP;
                    procesaPerfil();
                    ////System.out.println("Registros de Perfil: "+registrosAprocesar+" - Registros procesados: "+registrosprocesados+ " - Recorte: "+recortaFormato);
                    escribir("Registros: " + registrosAprocesar + " - Registros procesados: " + registrosprocesados + " - Recorte: " + recortaFormato);
                    if (registrosprocesados == registrosAprocesar - recortaFormato) {
                        procesaIncompletoPerfil = false;
                    } else {
                        procesaIncompletoPerfil = procesaIncompleto;
                    }
                    try {
                        cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                        if (fechaintervalo != null) {
                            if (fechaintervalo.after(med.getFecha())) {//med.getFecha())) {
                                cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                                escribir("Actualiza fecha: " + sdf3.format(new Date(fechaintervalo.getTime())));
                            } else {
                                escribir("No actualiza fecha - " + sdf3.format(new Date(fechaintervalo.getTime())));
                            }
                        }
//                    l = true;                        
                        escribir("Almacena Perfil");
                    } catch (Exception e) {
                        escribir(getErrorString(e.getStackTrace(), 3));                      
                        escribir("Error almacenando Perfil");
                    }
                } else {                    
                    escribir("Sin datos de perfil para almacenar");
                }
            }
            if (lregistros) {
                if ((vRegistrosdia != null && vRegistrosdia.size() > 0) || (vRegistrosmes != null && vRegistrosmes.size() > 0)) {
                    try {                        
                        procesaRegistros();
                        procesaIncompletoRegistros = procesaIncompleto;                        
                        escribir("Almacena Registros");
                    } catch (Exception e) {
                        escribir(getErrorString(e.getStackTrace(), 3));                       
                        escribir("Error almacenando Registros");
                    }
                } else {                    
                    escribir("Sin datos de registros para almacenar");
                }
            }
            if (leventos) {
                if (listEventos != null && listEventos.size() > 0) {
                    try {                        
                        registrosAprocesar = registrosprocesados = 0;
                        recortaFormato = recortaFormatoE;
                        procesaInfoEventos();
                        //System.out.println("Registros de Eventos: " + registrosAprocesar + " - Registros procesados: " + registrosprocesados + " - Recorte: " + recortaFormatoE);
                        escribir("Registros de Eventos: " + registrosAprocesar + " - Registros procesados: " + registrosprocesados + " - Recorte: " + recortaFormatoE);
                        registrosprocesadosE = registrosprocesados;
                        if (registrosprocesados == registrosAprocesar - recortaFormato) {
                            procesaIncompletoEventos = false;
                        } else {
                            procesaIncompletoEventos = procesaIncompleto;
                        }                        
                        escribir("Almacena Eventos");
                    } catch (Exception e) {
                        escribir(getErrorString(e.getStackTrace(), 3));                       
                        escribir("Error almacenando Eventos");
                    }
                } else {                    
                    escribir("Sin datos de eventos para almacenar");
                }
            }
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }

    }
    
    private void procesaPerfil() {
        primerintervalo = true;
        String[] data = vPerfilCarga.toArray(new String[vPerfilCarga.size()]);
        //Toma los backups de la info de los canales y los pasa a los genéricos
        obis = obisPerfil;
        conske = conskePerfil;
        unidad = unidadPerfil;
        clase = clasePerfil;
        bytesRecortados = bytesRecortadosPerfil;
        interpretaDatos(data, 22);
    }
    
    private void procesaRegistros() {
        bytesRecortados = bytesRecortadosRegistros;
        if(vRegistrosdia.size()>0){
            String[] datardia = vRegistrosdia.toArray(new String[vRegistrosdia.size()]);
            tiporegistros = "0"; //diarios es 0
            vtiporegistros = cp.obtenerTipoRegistros(med.getMarcaMedidor().getCodigo(), tiporegistros, null); //consulta los canales de los registros mensuales
            intervalo = intervaloRdia;
            //Toma los backups de la info de los canales y los pasa a los genéricos
            obis = obisRdia;
            conske = conskeRdia;
            unidad = unidadRdia;
            clase = claseRdia;
            interpretaDatos(datardia, 23);
        } else if (vRegistrosmes.size()>0 ){
            String[] datarmes = vRegistrosmes.toArray(new String[vRegistrosmes.size()]);
            tiporegistros = "1"; //mensual es 1
            vtiporegistros = cp.obtenerTipoRegistros(med.getMarcaMedidor().getCodigo(), tiporegistros, null); //consulta los canales de los registros mensuales
            intervalo = intervaloRmes;
            //Toma los backups de la info de los canales y los pasa a los genéricos
            obis = obisRmes;
            conske = conskeRmes;
            unidad = unidadRmes;
            clase = claseRmes;
            interpretaDatos(datarmes, 23);
        } else {
            //System.out.println("Sin registros para almacenar");
            escribir("Sin registros para almacenar");
        }
    }
    
    public void procesaInfoEventos() throws Exception {
        String[] arrayEventos = listEventos.toArray(new String[listEventos.size()]);
        interpretaDatos(arrayEventos, 24);
    }
    
    public String[] sacarDatos(String[] vectorhex, int inicio) {
        int tam = (tipoconexion == 0 ? ((Integer.parseInt((vectorhex[6] + vectorhex[7]), 16) & 0x7FF) + indxData - inicio): (Integer.parseInt((vectorhex[1] + vectorhex[2]), 16) & 0x7FF) - inicio - 1);
        String nuevoVector[] = new String[tam];
////        System.out.println("tam vectorhex: "+vectorhex.length+" - tam data: "+(Integer.parseInt((vectorhex[6] + vectorhex[7]), 16) & 0x7FF)+" - iDa: "+indxData+" - inicio: "+inicio+" -  tam: "+tam);
        System.arraycopy(vectorhex, inicio, nuevoVector, 0, nuevoVector.length);
        ////System.out.println("\nDATA\n");
        //System.out.println("\n" + Arrays.toString(nuevoVector));
        return nuevoVector;
    }
    
    
    public Date getDCurrentDate() throws ParseException {
        return Date.from(ZonedDateTime.now(zid).toInstant());
    }

    private Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
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

    public String getCurrentDate(SimpleDateFormat isdf) throws ParseException {
        return isdf.format(Date.from(ZonedDateTime.now(zid).toInstant()));
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
    
    private void handleReenvio(String peticion) {
        if (ReintentoRevisionTrama < numeroReintentos) {
            ReintentoRevisionTrama++;
            enviaTrama2(ultimatramaEnviada, "=> Envia Solicitud " + peticion);
        } else {
            ReintentoRevisionTrama = 0;
            lprecierre = true;
            escribir("Termina lectura por incidencia en error de protocolo");
            enviaPrelogout();
        }
    }

    private void handleReject() {
        if (ReintentoFRMR <= numeroReintentos) {
            enviaReject();
        } else {
            ReintentoFRMR = 0;            
            cerrarPuerto(true);
            escribir("Reintentos FRMR agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Reintentos FRMR agotados");
            cerrarLog("Reintentos FRMR agotados", false);
            leer = false;
        }
    }
    
    private void enviaReject() {
        byte[] trama = tramas.getFRMR();
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcI(trama);
        ReintentoFRMR ++;
        enviaTrama2(trama, "Reject");
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
