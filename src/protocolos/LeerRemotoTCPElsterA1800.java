/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;
import Control.ControlProcesos;
import Datos.DES2;

import Datos.TramasRemotalElsterA1800;
import Entidades.Abortar;
import Entidades.ELogCall;

import Entidades.EMedidor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.net.Socket;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import javax.comm.*;

public class LeerRemotoTCPElsterA1800 extends Thread {

    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    boolean verificaSerial = true;
    int nivelacceso = 0;
    SerialPort serialPort;
//    Enumeration portList; //vic 13-08-19
    InputStream input;
    OutputStream output;
    String cadenahex = "";
    TramasRemotalElsterA1800 tramasElster = new TramasRemotalElsterA1800();
    DES2 des2 = new DES2();
    byte[] user;
    boolean aviso = false;
    int indx = 0;
    String password = "";
    EMedidor med;
    ControlProcesos cp;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lacumulados;
    boolean lconfhora;
    public boolean leer = true;
    String numeroPuerto;
    
    int velocidadPuerto;
    long timeout;
    private int ndias;
    private int nminutes;
    int offset = 0;
    int ElementCount = 0;
    String eCountI = "";
    private int expSeq = 0;
    boolean portconect = false;
    Thread tEscritura = null;
    Thread tReinicio = null;
    boolean inicia1 = false;
    public boolean cierrapuerto = false;
    Socket socket;
    private volatile boolean escucha = true;
    Thread tLectura;
    private int numeroReintentos = 4;
    private int reintentoconexion = 0;
    private int reintentoReenvio = 0;
    //Estados
    boolean lcomandoI = false;
    boolean lidentidad = false;
    boolean lvelocidad = false;
    boolean ltiming = false;
    boolean llogon = false;
    boolean lautenticacion = false;
    boolean lST06 = false;
    boolean lST05 = false;
    boolean lST055factual = false;
    boolean lST061 = false;
    boolean lST055factual2 = false;
    boolean lST053 = false;
    boolean lST062 = false;
    boolean lMT017 = false;
    boolean lST00 = false;
    boolean lST021 = false;
    boolean lST022 = false;
    boolean lST023 = false;
    boolean lST027 = false;
    boolean lST028 = false;
    boolean lST015 = false;
    boolean lMT015 = false;
    boolean lMT016 = false;
    boolean lST063 = false;
    boolean lST076 = false;
    boolean lST007 = false;
    boolean lST008 = false;
    boolean lST064Perfil = false;
    boolean llogoff = false;
    boolean enviando = false;
    boolean reenviando = false;
    byte[] ultimatramaEnviada = null;
    byte[] ultimoSimpleM = null;
    byte id = (byte) 0x01;
    String idS = "00";
    byte version = (byte) 0x00; //0 --> ANSI C12.18 ó ANSI C12.21, 1 --> ANSI C12.22
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    int numCanales = 2;
    int intervalos = 15;
    boolean spoling = false; //false = 00 , true= 20
    boolean isticket = false; // contiene ticket o no contiene
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean seEspera06 = false;
    boolean haveNoise = false;
    byte bitSpoling = (byte) 0x00;
    public boolean tramaOK = false;
    public boolean uncomplete = false;
    public boolean delayed = false;
    public int idxCopyFrom;
    public int tamañobloque;
    public int tamañototalbloque;
    ArrayList<String> profileDataTemp;
    ArrayList<ArrayList<String>> profileData;
    Vector<String[]> EventData;
    String[] vSt062;
    Vector<String[]> vMT017;
    Vector<String[]> vST015;
    Vector<String[]> vST022;
    Vector<String[]> vST023;
    Vector<String[]> vST027;
    Vector<String[]> vST028;
    String[] regMt017;
    String[] regSt015;
    String[] regST022;
    String[] regST023;
    String[] regST027;
    String[] regST028;
    String[] fragment1;
    String[] fragment2;
    String External_Multiplier;
    String Ext_Mult_Scale_Factor;
    String Instrumentation_Scale_Factor;
    public Vector<String> vtotalMT017;
    public Vector<String> vtotalST015;
    public String regST062[];
    public ArrayList<String> desglosePerfil;
    public Vector<String> desgloseEventos;
    public String vdesgloseSt023;
    public String vdesgloseSt028;
    String OrdenCanales[];
    String OrdenSumations[];
    String OrdenDemands[];
    String OrdenPresentDemands[];
    String OrdenPresentValues[];
    int OrdenFasesPresentValues[];
    public String LP_CTRL_INT_FMT_CDE1;
    public int NBR_BLK_INTS_SET1;
    private boolean primerbloque;
    public String fechaactual;
    public String fechaactualsync;
    String seriemedidor = "";
    public int FORMAT_CONTROL_1; //formato registros
    private int NBR_PRESENT_VALUES;
    private int NBR_SUMMATIONS;
    private int NBR_PRESENT_DEMANDS;
    private int NBR_SELF_READS;
    private int NBR_COIN_VALUES;
    private int NBR_DEMANDS;
    private int NBR_OCCUR;
    private int REG_FUNC1_FLAG;
    private String[] vSumationSelect;
    private String[] vDemandSelect;
    private String[] vCoincidentSelect;
    private String[] vDemandAssoc;
    private String[] vPresentdemandSelect;
    private String[] vPresentValues;
    int tamañototalMT017 = 0;
    int tamañoacumuladolMT017 = 0;
    int contadorST064 = 0;
    private int intentosNACK = 0;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    private boolean perfilincompleto = false;
    Abortar objabortar;
    private String usuario = "admin";
    private Timestamp time = null; //tiempo de NTP
    private Timestamp tsfechaactual;
    private Timestamp deltatimesync1;
    private Timestamp deltatimesync2;
    private long ndesfasepermitido = 0;
    private long desfase;
    String dirfis = "0";
    String dirlog = "0";
    boolean solicitar; //variable de control de la sync
    List<String[]> frames = new ArrayList<>();

    private ZoneId zid;

    private final Object monitor = new Object();
    private final String label = "LeerTCPElsterA1800";

    public LeerRemotoTCPElsterA1800(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean confhora, boolean acumulados, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, String usuario, ZoneId zid, long ndesfase) {
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
        lconfhora = confhora;

        leventos = eventos;
        if (lperfil) {
            leventos = true;            
        }
        lregistros = registros;
        lacumulados = acumulados;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        jinit();
    }

    private void jinit() {
        escribir("Equipo llamador: " + cp.obtenerMac());
        escribir("Telesimex version: " + cp.getVersion());
        escribir("Window Id: " + cp.getWindowId());        
        try { //vic 13-08-19
            tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = lconfhora ? med.getPassword2() : med.getPassword();
            timeout = med.getTimeout() * 1000;
            seriemedidor = med.getnSerie();
            ndias = med.getNdias();
            numCanales = med.getNcanales();
            dirfis = med.getDireccionFisica() == null ? "0" : med.getDireccionFisica();
            id = (byte) (Integer.parseInt(dirfis) & 0xFF);
            idS = Integer.toHexString(id);
            idS = idS.length() < 2 ? "0" + idS : idS;
            dirlog = med.getDireccionLogica() == null ? "0" : med.getDireccionLogica();
            version = (byte) (Integer.parseInt(dirlog, 16) & 0x03);
            abrePuerto();
        } catch (Exception e) {
            leer = false;
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

   private void abrePuerto() {
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

    private void iniciacomunicacion() throws Exception {
        lST064Perfil = false;
        lST076 = false;
        perfilincompleto = false;
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
            lcomandoI = true;
            inicia1 = false;
            enviaTrama2(tramasElster.getIcommand(), "ICOMAND", (byte) 0x00);
        } else {
            interrumpirHilo(tLectura);
            escribir("Medidor no configurado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
            cerrarPuerto(false);
            cerrarLog("Medidor no configurado", false);
            leer = false;
        }
    }

    private void procesaCadena() {

        Object[] dataExtBuffer = new Object[2];
        byte[] readBuffer = new byte[2047];
        byte[] tempBuffer = new byte[2047];
        int numbytes = 0;
        int auxNumbytes = 0;
        boolean expTime = true;
        boolean extendTime = false;
        try {
            synchronized (monitor) {
                monitor.wait(1000);
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                while (!socket.isClosed() && System.currentTimeMillis() < endT) {
                    monitor.wait(200);
                    if (input.available() > 0) {
                        auxNumbytes = input.read(tempBuffer);
                        if (auxNumbytes > 1) {
                            if (numbytes != 0) {
                                System.arraycopy(tempBuffer, 0, readBuffer, numbytes, auxNumbytes);
                                expTime = false;
                                numbytes += auxNumbytes;
                                break;
                            } else {
                                System.arraycopy(tempBuffer, 0, readBuffer, 0, auxNumbytes);
                                numbytes = auxNumbytes;
                                expTime = false;
                                break;
                            }
                        } else if (auxNumbytes == 1) {
                            if (tempBuffer[0] == (byte) 0x06) {
                                readBuffer[0] = tempBuffer[0];
                                numbytes = auxNumbytes;
                            } else {
                                readBuffer[0] = tempBuffer[0];
                                numbytes = auxNumbytes;
                                break;
                            }
                        }
                    }
                }
                if (!socket.isClosed() && expTime) {
                    if (ultimoSimpleM == null) {
                        if (!lcomandoI) {
                            reenviando = true;
                            enviando = false;
                            monitor.notifyAll();
                            return;
                        } else {
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                            enviaIdentidad();
                            return;
                        }
                    } else {
                        dataExtBuffer = esperaCadena();
                        boolean reenvio = (boolean) dataExtBuffer[2];
                        if (reenvio) {
                            reenviando = true;
                            enviando = false;
                            monitor.notifyAll();
                            return;
                        } else {
                            extendTime = true;
                            enviando = false;
                            reenviando = false;
                            monitor.notifyAll();
                        }
                    }
                } else {
                    enviando = false;
                    reenviando = false;
                    monitor.notifyAll();
                }
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            if (extendTime) {
                byte[] auxBuffer = (byte[]) dataExtBuffer[1];
                int tempNumBytes = (int) dataExtBuffer[0];
                cadenahex = tramasElster.encode(auxBuffer, tempNumBytes);
            } else {
                cadenahex = tramasElster.encode(readBuffer, numbytes);
            }
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

    private Object[] esperaCadena() {
        Object[] ans = new Object[3];
        byte[] readBuffer = new byte[2047];
        byte[] tempBuffer = new byte[2047];
        int numbytes = 0;
        int auxNumbytes = 0;
        boolean expTime = true;
        try {
            synchronized (monitor) {
                monitor.wait(1000);
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                while (!socket.isClosed() && System.currentTimeMillis() < endT) {
                    monitor.wait(200);
                    if (input.available() > 0) {
                        auxNumbytes = input.read(tempBuffer);
                        if (auxNumbytes > 1) {
                            if (numbytes != 0) {
                                System.arraycopy(tempBuffer, 0, readBuffer, numbytes, auxNumbytes);
                                expTime = false;
                                numbytes += auxNumbytes;
                                break;
                            } else {
                                System.arraycopy(tempBuffer, 0, readBuffer, 0, auxNumbytes);
                                numbytes = auxNumbytes;
                                expTime = false;
                                break;
                            }
                        } else if (auxNumbytes == 1) {
                            if (tempBuffer[0] == (byte) 0x06) {
                                readBuffer[0] = tempBuffer[0];
                                numbytes = auxNumbytes;
                            }
                        }
                    }
                }
            }
            if (!socket.isClosed() && expTime) {
                ans[0] = 1;
                ans[1] = new byte[]{(byte) 0x00};
                ans[2] = true;
                return ans;
            } else {
                ans[0] = numbytes;
                ans[1] = readBuffer;
                ans[2] = false;
                return ans;
            }
        } catch (Exception e) {
            ans[0] = 1;
            ans[1] = new byte[]{(byte) 0x00};
            ans[2] = true;
            return ans;
        }
    }

    private void enviaIdentidad() throws InterruptedException {        
        lcomandoI = false;
        lidentidad = true;
        spoling = false;
        bitSpoling = 0x00;
        escribir("ENVIA identidad");
        byte[] tramaidentidad = tramasElster.getIdentidad();
        seEspera06 = true;
        tramaidentidad[1] = id;
        tramaidentidad[2] = (byte) ((tramaidentidad[2] + version) & 0x23);
        byte tramanueva[] = calcularnuevocrc(tramaidentidad);
        synchronized (monitor) {
            monitor.wait(100);
            enviaTrama2(tramanueva, "", (byte) 0x06);
        }

    }

    private void enviaTrama2(byte[] bytes, String descripcion, byte confirm) {
        final byte[] trama = bytes;
        final boolean simpleM = trama == null;
        final byte[] confirmResp = {confirm};
        final String des = descripcion;
        final boolean c = (confirm == (byte) (0x06) || confirm == (byte) (0x15) || confirm == (byte) (0x00));
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
                                } else if (intentosNACK != 0 && des.equalsIgnoreCase("NACK")) {
                                    escribir("Reintento NACK");
                                }
                                escribir(des);
                                if (c && intentosRetransmision == 0) {
                                    if (simpleM) {
                                        ultimoSimpleM = confirmResp;
                                        monitor.notifyAll();
                                        enviaTrama(confirmResp);
                                        escribir("=> " + Arrays.toString(confirmResp));
                                        monitor.wait();
                                    } else {
                                        enviaTrama(confirmResp);
                                        escribir("=> " + Arrays.toString(confirmResp));
                                        monitor.wait(100);
                                    }
                                } else if (intentosRetransmision != 0 && simpleM) {
                                    ultimoSimpleM = confirmResp;
                                    monitor.notifyAll();
                                    enviaTrama(confirmResp);
                                    escribir("=> " + Arrays.toString(confirmResp));
                                    monitor.wait();
                                }
                                if (!simpleM) {
                                    ultimoSimpleM = null;
                                    ultimatramaEnviada = trama;
                                    escribir(des);
                                    escribir("=> " + tramasElster.encode(trama, trama.length));
                                    try {
                                        monitor.notifyAll();
                                        enviaTrama(trama);
                                        monitor.wait();
                                    } catch (Exception e) {
                                        System.err.println(e.getMessage());
                                        escribir("Error enviando trama tipo 2_2");
                                    }
                                }
                            } else {
                                t = false;
                            }
                            if (reenviando && intentosRetransmision <= 2) {
                                intentosRetransmision++;
                            } else if ((reenviando && intentosRetransmision > 2) || intentosNACK > 3) {
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
                            cerrarPuerto(true);
                            monitor.notifyAll();
                            if (lST064Perfil & perfilincompleto) {
                                //guardamos lecturas hasta donde esten 
                                try {
                                    desglosaST076();
                                    almacenaPerfilIncompleto();
                                    med.MedLeido = true;
                                    cerrarLog("Leido Incompleto", true);
                                    leer = false;
                                    return;
                                } catch (Exception e) {
                                    escribir("Error en procesamiento de perfil de carga y eventos");
                                    escribir(getErrorString(e.getStackTrace(), 3));
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
                    } catch (InterruptedException e) {
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

    public void reiniciaComunicacion() {
        try {
            if (lST064Perfil & perfilincompleto) {
                //guardamos lecturas hasta donde esten                
                escribir("Desglosa ST 076");
                desglosaST076();
                almacenaPerfilIncompleto();
            }
            Thread.sleep(1000);
            cerrarPuerto(true);
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

    public void cerrarPuerto(boolean sendBye) {    
        if (sendBye) {
            sendByeWithoutAnswer();
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

    private void interpretaCadena(String cadenahex) {

        try {
            frames = new ArrayList<>();
            escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (lcomandoI) {
                revisarComandoI(vectorhex, cadenahex);
            } else if (lidentidad) {
                revisarIdentidad(vectorhex);
            } else if (lvelocidad) {
                revisarVelocidad(vectorhex);
            } else if (llogon) {
                revisarLogon(vectorhex);
            } else if (lautenticacion) {
                revisarAutenticacion(vectorhex);
            } else if (ltiming) {
                revisarTiming(vectorhex);
            } else if (lST06) {
                revisarST06(vectorhex);
            } else if (lST05) { //Numero de serie
                revisarST05(vectorhex);
            } else if (lST055factual) {// Fecha actual
                revisarST055(vectorhex);
            } else if (lST061) {
                revisarST061(vectorhex);
            } else if (lST055factual2) { //fecha actual 2
                revisarST055_2(vectorhex);
            } else if (lST053) {
                revisarST053(vectorhex);
            } else if (lST062) {
                revisarST062(vectorhex);
            } else if (lST00) {
                revisarST00(vectorhex);//**
            } else if (lMT017) {
                revisarMT017(vectorhex);
            } else if (lST015) {//constantes
                revisarST015(vectorhex);
            } else if (lMT015) {
                revisarMT015(vectorhex);
            } else if (lMT016) {
                revisarMT016(vectorhex);
            } else if (lST063) {
                revisarST063(vectorhex);
            } else if (lST076) {
                revisarST076(vectorhex);
            } else if (lST021) {
                revisarST021(vectorhex);
            } else if (lST022) {
                revisarST022(vectorhex);
            } else if (lST023) {
                revisarST023(vectorhex);
            } else if (lST027) {
                revisarST027(vectorhex);
            } else if (lST028) {
                revisarST028(vectorhex);
            } else if (lST064Perfil) {//perfil de carga
                revisarST064(vectorhex);
            } else if (lST007) {
                revisarST007(vectorhex);
            } else if (lST008) {
                revisarST008(vectorhex);
            } else if (llogoff) {
                revisarLogoff(vectorhex);
            }
            vectorhex = null;
        } catch (Exception e) {
            cerrarPuerto(true);
            escribir("Estado lectura No leido");
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Error al interpretar la respuesta.");
            cerrarLog("Error en la etapa de interpretación", false);
            leer = false;      
        }
    }

    private byte[] calcularnuevocrc(byte[] siguientetrama) {
        byte[] data = new byte[siguientetrama.length - 2];
        System.arraycopy(siguientetrama, 0, data, 0, data.length);
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
        siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
        siguientetrama[siguientetrama.length - 1] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
        return siguientetrama;
    }

    private boolean validacionCRCFCS(String[] data, int len) {
        boolean lcrc = false;
        byte b[] = new byte[len - 3];
        for (int j = 0; j < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        String stx = data[len - 2] + "" + data[len - 1];
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

    private boolean validacionCRCFCS2(String[] data, int len) { //valida el crc sin el 06 en la cabecera
        boolean lcrc = false;
        byte b[] = new byte[len - 2];
        for (int j = 0; j < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
//        String stx = data[data.length - 2] + "" + data[data.length - 1];
        String stx = data[len - 2] + "" + data[len - 1];
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

    public int calculoFCS(byte[] bytes) {
        int crcaux1 = 0x00000000;      //auxiliar para el calculo final
        int crc = 0x0000FFFF;          // initial value
        int polynomial = 0x1021;   // polinomio generador 0001 0000 0010 0001  (0, 5, 12)  CRC16-CCITT
        //revertir la trama de bytes
        for (int i = 0; i
                < bytes.length; i++) {
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
        crc = reflect(crc, true);
        crcaux1 = (short) (((crc >> 8) & 0x000000ff) + (crc << 8 & 0x0000ff00));
        crcaux1 = crcaux1 & 0x0000FFFF;
        return crcaux1;
    }

    public short reflect(int crc, boolean order) {
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

    public void escribir(String dato) {
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
            System.err.println(e.getMessage());//vic 13-08-19
            try {
                fr.close();
            } catch (Exception exc) {
                System.err.println(exc.getMessage());
            }
        }
    }

    private void desglosaMT017() {
        vtotalMT017 = new Vector<String>();
        String tramaMT017 = "";
        String totalMT017 = "";
        for (int p = 0; p < vMT017.size(); p++) {
            tramaMT017 = "";
            for (String get : vMT017.get(p)) {
                tramaMT017 += get;
            }
            if (tramaMT017.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                tramaMT017 = tramaMT017.substring(20, tramaMT017.length() - 4);
            } else {//es continuacion de bloques o retransmision de la primera trama
                if (p == 0) {//es la primera trama
                    tramaMT017 = tramaMT017.substring(18, tramaMT017.length() - 4);
                } else {//es continuacion de bloques
                    if (tramaMT017.substring(6, 8).equals("00")) {//es el ultimo bloque
                        tramaMT017 = tramaMT017.substring(12, tramaMT017.length() - 6);
                    } else {//es bloques intermedios
                        tramaMT017 = tramaMT017.substring(12, tramaMT017.length() - 4);
                    }
                }
            }
            vtotalMT017.add(tramaMT017);
        }
        for (int v = 0; v < vtotalMT017.size(); v++) {
            totalMT017 += vtotalMT017.get(v);
        }
        regMt017 = new String[totalMT017.length() / 2];
        int i = 0;
        for (int v = 0; v < totalMT017.length() - 2; v += 2) {
            regMt017[i] = totalMT017.substring(v, v + 2);            
            i++;
        }

    }

    private void desglosaST062() {
        try {
            if (vSt062[0].equals("06")) {//es con 06
                regST062 = new String[vSt062.length - 13];
                System.arraycopy(vSt062, 10, regST062, 0, regST062.length);
            } else {//es con EE
                regST062 = new String[vSt062.length - 12];
                System.arraycopy(vSt062, 9, regST062, 0, regST062.length);
            }
            LP_CTRL_INT_FMT_CDE1 = regST062[(numCanales * 3)];
            escribir("Formato canales: " + (Integer.parseInt(LP_CTRL_INT_FMT_CDE1, 16) & 0xFF));
        } catch (Exception e) {
            escribir("Error procesando Tabla 062");
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void organizarCanales() {
        OrdenCanales = new String[numCanales];
        String tipocanal = "";
        String valorunidad;
        for (int i = 0; i < numCanales; i++) {
            valorunidad = regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7)];
            if (valorunidad.equals("00")) {//es energia activa
                if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                    tipocanal = "kWhD";
                } else if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                    tipocanal = "kWhR";
                } else if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                    tipocanal = "kWhR";
                } else if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                    tipocanal = "kWhD";
                } else {
                    tipocanal = "99";
                }
            } else if (valorunidad.equals("01")) {//es energia reactiva
                if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                    tipocanal = "kVarhD";
                } else if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                    tipocanal = "kVarhD";
                } else if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                    tipocanal = "kVarhR";
                } else if ((((byte) Integer.parseInt(regMt017[((Integer.parseInt(regST062[(i * 3) + 1], 16)) * 7) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                    tipocanal = "kVarhR";
                } else {
                    tipocanal = "99";
                }
            } else {
                tipocanal = "99";
            }
            OrdenCanales[i] = tipocanal;
        }
    }

    private void desglosaST076() {
        try {
            desgloseEventos = new Vector<>();
            String tramadesglose = "";
            String tramatotaldata = "";
            for (int p = 0; p < EventData.size(); p++) {
                tramadesglose = "";
                for (String get : EventData.get(p)) {
                    tramadesglose += get;
                }
                if (tramadesglose.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                    escribir("NBR_VALID_ENTRIES :" + Integer.parseInt(tramadesglose.substring(24, 26) + tramadesglose.substring(22, 24), 16));
                    escribir("LAST_ENTRY_SEQ_NBR :" + Integer.parseInt(tramadesglose.substring(36, 38) + tramadesglose.substring(34, 36) + tramadesglose.substring(32, 34) + tramadesglose.substring(30, 32), 16));
                    escribir("NBR_UNREAD_ENTRIES :" + Integer.parseInt(tramadesglose.substring(40, 42) + tramadesglose.substring(38, 40), 16));
                    tramadesglose = tramadesglose.substring(42, tramadesglose.length() - 4);
                } else {//es continuacion de bloques o retransmision de la primera trama
                    if (p == 0) {//es la primera trama retransmitida
                        escribir("NBR_VALID_ENTRIES :" + Integer.parseInt(tramadesglose.substring(22, 24) + tramadesglose.substring(20, 22), 16));
                        escribir("LAST_ENTRY_SEQ_NBR :" + Integer.parseInt(tramadesglose.substring(34, 36) + tramadesglose.substring(32, 34) + tramadesglose.substring(30, 32) + tramadesglose.substring(28, 30), 16));
                        escribir("NBR_UNREAD_ENTRIES :" + Integer.parseInt(tramadesglose.substring(38, 40) + tramadesglose.substring(36, 38), 16));
                        tramadesglose = tramadesglose.substring(40, tramadesglose.length() - 4);
                    } else {//es continuacion de bloques
                        if (tramadesglose.substring(6, 8).equals("00")) {//es el ultimo bloque
                            tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 6);
                        } else {//es bloques intermedios
                            tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 4);
                        }
                    }
                }
                tramatotaldata = tramatotaldata + "" + tramadesglose;
            }
            cp.almancenaEventosElster(tramatotaldata, seriemedidor);

        } catch (Exception e) {
            escribir("Error procesando tabla 076");
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void desglosaST022() {
        try {
            vSumationSelect = new String[NBR_SUMMATIONS];
            vDemandSelect = new String[NBR_DEMANDS];
            if (NBR_COIN_VALUES > 0) {
                vCoincidentSelect = new String[NBR_COIN_VALUES];
                vDemandAssoc = new String[NBR_COIN_VALUES];
            }
            System.arraycopy(regST022, 0, vSumationSelect, 0, NBR_SUMMATIONS);
            System.arraycopy(regST022, NBR_SUMMATIONS, vDemandSelect, 0, NBR_DEMANDS);
            if (NBR_COIN_VALUES > 0) {
                System.arraycopy(regST022, NBR_SUMMATIONS + NBR_DEMANDS, vCoincidentSelect, 0, NBR_COIN_VALUES);
                System.arraycopy(regST022, NBR_SUMMATIONS + NBR_DEMANDS + NBR_COIN_VALUES, vDemandAssoc, 0, NBR_COIN_VALUES);
            }
        } catch (Exception e) {
            escribir("Error procesando tabla 022");
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void desglosaST023() {
        vdesgloseSt023 = "";
        for (String str : regST023) {
            vdesgloseSt023 += str;
        }
    }
    //metodo que entrega el orden de las tasas para los registros

    private void organizaSumations() {
        OrdenSumations = new String[NBR_SUMMATIONS];
        try {
            String tipocanal = "";
            String valorunidad;
            for (int i = 0; i < NBR_SUMMATIONS; i++) {
                valorunidad = "";
                valorunidad = regMt017[7 * Integer.parseInt(vSumationSelect[i], 16)];
                if (valorunidad.equals("00")) {//es energia activa
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "kWhD";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "kWhR";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "kWhR";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "kWhD";
                    } else {
                        tipocanal = "99";
                    }
                } else if (valorunidad.equals("01")) {//es energia reactiva
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "kVarhD";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "kVarhD";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "kVarhR";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "kVarhR";
                    } else {
                        tipocanal = "99";
                    }
                } else {
                    tipocanal = "99";
                }
                OrdenSumations[i] = tipocanal;
            }
        } catch (Exception e) {
            escribir("Error procesando summations");
            escribir(getErrorString(e.getStackTrace(), 3));
        }

    }

    private void organizaDemands() {
        OrdenDemands = new String[NBR_DEMANDS];
        try {
            String tipocanal = "";
            String valorunidad;
            for (int i = 0; i < NBR_DEMANDS; i++) {
                valorunidad = regMt017[7 * Integer.parseInt(vDemandSelect[i], 16)];
                if (valorunidad.equals("00")) {//es energia activa
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "KWd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "KWr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "KWr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "KWd";
                    } else {
                        tipocanal = "99";
                    }
                } else if (valorunidad.equals("01")) {//es energia reactiva
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "KVARd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "KVARd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "KVARr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "KVARr";
                    } else {
                        tipocanal = "99";
                    }
                } else {
                    //validamos el tipo de lectura
                    tipocanal = valorunidad;
                }
                OrdenDemands[i] = tipocanal;
            }
        } catch (Exception e) {
            escribir("Error procesando demands");
            escribir(getErrorString(e.getStackTrace(), 3));
        }

    }

    private void organizaPresentDemands() {
        OrdenPresentDemands = new String[NBR_PRESENT_DEMANDS];
        try {
            String tipocanal = "";
            String valorunidad;
            for (int i = 0; i < NBR_PRESENT_DEMANDS; i++) {
                valorunidad = regMt017[7 * Integer.parseInt(vPresentdemandSelect[i], 16)];
                if (valorunidad.equals("00")) {//es energia activa
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "KWd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "KWr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "KWr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "KWd";
                    } else {
                        tipocanal = "99";
                    }
                } else if (valorunidad.equals("01")) {//es energia reactiva
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "KVARd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "KVARd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "KVARr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "KVARr";
                    } else {
                        tipocanal = "99";
                    }
                } else {
                    //validamos el tipo de lectura
                    tipocanal = valorunidad;
                }
                OrdenPresentDemands[i] = tipocanal;
            }
        } catch (Exception e) {
            escribir("Error procesando present demands");
            escribir(getErrorString(e.getStackTrace(), 3));
        }

    }

    private void organizaPresentValues() {
        OrdenPresentValues = new String[NBR_PRESENT_VALUES];
        OrdenFasesPresentValues = new int[NBR_PRESENT_VALUES];
        try {
            String tipocanal = "";
            String valorunidad;
            int valorFase;
            for (int i = 0; i < NBR_PRESENT_VALUES; i++) {
                valorunidad = regMt017[7 * Integer.parseInt(vPresentValues[i], 16)];
                valorFase = ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0xE0) >> 5 & 0x0007);
                if (valorunidad.equals("00")) {//es energia activa
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "KWd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "KWr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "KWr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "KWd";
                    } else {
                        tipocanal = "99";
                    }
                } else if (valorunidad.equals("01")) {//es energia reactiva
                    if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x01) == 0x01) {
                        tipocanal = "KVARd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x02) == 0x02) {
                        tipocanal = "KVARd";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x04) == 0x04) {
                        tipocanal = "KVARr";
                    } else if ((((byte) Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentValues[i], 16)) + 1], 16) & 0xFF) & 0x08) == 0x08) {
                        tipocanal = "KVARr";
                    } else {
                        tipocanal = "99";
                    }
                } else {
                    //validamos el tipo de lectura
                    tipocanal = valorunidad;
                }
                OrdenPresentValues[i] = tipocanal;
                OrdenFasesPresentValues[i] = valorFase;
            }
        } catch (Exception e) {
            escribir("Error procesando tabla present values");
            escribir(getErrorString(e.getStackTrace(), 3));
        }

    }

    private void desglosaST027() {
        try {
            vPresentdemandSelect = new String[NBR_PRESENT_DEMANDS];
            vPresentValues = new String[NBR_PRESENT_VALUES];
            System.arraycopy(regST027, 0, vPresentdemandSelect, 0, NBR_PRESENT_DEMANDS);
            System.arraycopy(regST027, NBR_PRESENT_DEMANDS, vPresentValues, 0, NBR_PRESENT_DEMANDS);
        } catch (Exception e) {
            escribir("Error procesando tabla 027");
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private byte getSpoling(byte spoling) {
        if (spoling == 0x00) {
            bitSpoling = 0x20;
            return 0x20;
        } else {
            bitSpoling = 0x00;
            return 0x00;
        }
    }

    private void desglosaST028() {
        vdesgloseSt028 = "";
        for (String str : regST028) {
            vdesgloseSt028 += str;
        }
    }

    public int obtenerElementCount() {
        escribir("Desglosa ST062");
        desglosaST062();
        int velementcount = 0;
        int int_status = ((int) ((NBR_BLK_INTS_SET1 + 7) / 8));
        int extend_status = (int) (numCanales / 2);
        if (extend_status == 0) {
            extend_status = 1;
        }
        int tamCanales = 2;
        switch ((Integer.parseInt(LP_CTRL_INT_FMT_CDE1, 16) & 0xFF)) {
            case 16:
                tamCanales = 2;
                break;
            case 64:
                tamCanales = 6;
                break;
            case 128:
                tamCanales = 5;
                break;
            default:
                tamCanales = 2;
                break;

        }
        velementcount = (((numCanales * tamCanales) + extend_status + 1) * NBR_BLK_INTS_SET1) + 5 + int_status;
        return velementcount;
    }

    public int obtenerOffsetD() {
        return (ndias * (24 / ((NBR_BLK_INTS_SET1 * intervalos) / 60))) * (ElementCount);
    }

    public int obtenerOffsetM() {
        return ((int) Math.ceil((float) nminutes / (float) (NBR_BLK_INTS_SET1 * intervalos))) * (ElementCount);
    }

    private void revisarComandoI(String[] vectorhexO, String cadenahex) {
        if (vectorhexO.length >= 1) {
            if (cadenahex.contains("50 53 45 4D")) {
                inicia1 = false;
                //es un A1800
                lcomandoI = false;
                lidentidad = true;
                spoling = false;
                bitSpoling = 0x00;
                escribir("ENVIA identidad");                
                byte[] tramaidentidad = tramasElster.getIdentidad();
                seEspera06 = true;
                intentosNACK = 0;
                reintentoReenvio = 0;
                tramaidentidad[1] = id;
                tramaidentidad[2] = version;
                byte tramanueva[] = calcularnuevocrc(tramaidentidad);
                enviaTrama2(tramanueva, "", (byte) 0x06);
            } else {
                if (reintentoReenvio < numeroReintentos) {
                    reintentoReenvio++;
                    escribir("No PSEM");
                    enviaTrama2(tramasElster.getIcommand(), "ICOMAND", (byte) 0x00);
                } else {
                    inicia1 = false;
                    //es un A1800
                    lcomandoI = false;
                    lidentidad = true;
                    spoling = false;
                    bitSpoling = 0x00;
                    escribir("ENVIA identidad");                    
                    byte[] tramaidentidad = tramasElster.getIdentidad();
                    seEspera06 = true;
                    intentosNACK = 0;
                    reintentoReenvio = 0;
                    tramaidentidad[1] = id;
                    tramaidentidad[2] = version;
                    byte tramanueva[] = calcularnuevocrc(tramaidentidad);
                    enviaTrama2(tramanueva, "", (byte) 0x06);
                }
            }
        } else {
            reiniciaComunicacion();
        }
    }

    private void revisaTrama(String[] vectorhex) {
        tramaOK = false;
        uncomplete = false;
        delayed = false;
        seEspera06 = vectorhex[0].equals("06");
        haveNoise = seEspera06 ? !(vectorhex[1].equals("EE")) : !(vectorhex[0].equals("EE") && vectorhex[1].equalsIgnoreCase(idS));
        int indxheader = (seEspera06 ? 7 : 6);
        int indxlen = (seEspera06 ? 9 : 8);
        int indxlen1 = (seEspera06 ? 5 : 4);
        int indxlen2 = (seEspera06 ? 6 : 5);
        int indxbitSpoling = (seEspera06 ? 3 : 2);
        boolean validaCRC;

        if ((seEspera06 && vectorhex[0].equals("06")) || (!seEspera06 && vectorhex[0].equals("EE"))) { // se verifica si llega el 06 o EE
            if (vectorhex.length > indxheader) {//posee cabecera?
                if ((vectorhex.length - indxlen) == (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16))) { //validamos campo len
                    validaCRC = (seEspera06 ? validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16) + indxlen)) : validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16) + indxlen)));
                    if (validaCRC) {//validamos CRC
                        if (((((byte) Integer.parseInt(vectorhex[indxbitSpoling], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            tramaOK = true;
                            frames.add(vectorhex);                        
                        } else {//bit Spoling
                            tramaOK = true;
                            delayed = true;
                            escribir("BAD Bit Spoling");
                            frames.add(vectorhex);
                        }
                    } else {// CRC
                        escribir("BAD CRC");
                        frames.add(vectorhex);
                    }
                } else {// LEN    
                    if (!haveNoise) {
                        tramaOK = true;
                        escribir("Error longitud de trama");
                        // Indica si la trama que llego es más corta que el length definitido en la cabecera de la trama por tanto incompleta.
                        boolean shorter = (vectorhex.length - indxlen) < (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16));
                        if (shorter) {
                            escribir("Trama incompleta");
                            fragment1 = vectorhex;
                            uncomplete = true;
                            frames.add(vectorhex);
                        } else {
                            escribir("Más de una trama");
                            List<Integer> idxList = idxSplitArray(vectorhex);
                            //System.out.println("Index List: " + idxList);
                            escribir("Posiciones de posibles tramas: " + idxList);
                            frames = getFrames(vectorhex, idxList);
                            seEspera06 = false;
                        }
                    } else {
                        tramaOK = true;
                        escribir("Trama con Ruido");
                        fragment1 = null;
                        uncomplete = true;
                        frames.add(vectorhex);
                    }
                }
            } else {
                tramaOK = true;
                escribir("Error trama sin cabecera");
                fragment1 = vectorhex;
                uncomplete = true;
                frames.add(vectorhex);
            }
        } else {
            escribir("Error trama inicio");
            frames.add(vectorhex);
        }
    }

    private void revisaTramaIndividual(String[] vectorhex) {
        tramaOK = false;
        seEspera06 = false;
        haveNoise = seEspera06 ? !(vectorhex[1].equals("EE")) : !(vectorhex[0].equals("EE") && vectorhex[1].equalsIgnoreCase(idS));
        int indxheader = (seEspera06 ? 7 : 6);
        int indxlen = (seEspera06 ? 9 : 8);
        int indxlen1 = (seEspera06 ? 5 : 4);
        int indxlen2 = (seEspera06 ? 6 : 5);
        int indxbitSpoling = (seEspera06 ? 3 : 2);
        boolean validaCRC;

        if ((seEspera06 && vectorhex[0].equals("06")) || (!seEspera06 && vectorhex[0].equals("EE"))) { // se verifica si llega el 06 o EE
            if (vectorhex.length > indxheader) {//posee cabecera?
                if ((vectorhex.length - indxlen) == (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16))) { //validamos campo len
                    validaCRC = (seEspera06 ? validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16) + indxlen)) : validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[indxlen1] + vectorhex[indxlen2]), 16) + indxlen)));
                    if (validaCRC) {//validamos CRC
                        if (llogoff || ((((byte) Integer.parseInt(vectorhex[indxbitSpoling], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            tramaOK = true;
                        } else {//bit Spoling
                            escribir("BAD Bit Spoling");
                        }
                    } else {// CRC
                        escribir("BAD CRC");
                    }
                } else {// LEN    
                    if (!haveNoise) {
                        escribir("Error longitud de trama");
                        escribir("Trama incompleta");
                    } else {
                        escribir("Trama con Ruido");
                    }
                }
            } else {
                escribir("Error trama sin cabecera");
            }
        } else {
            escribir("Error trama inicio");
        }
    }

    private void revisarIdentidad(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxTicket;
        int idxSeq;
        String peticion = "Identidad";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxTicket = (seEspera06 ? 11 : 10);
            idxSeq = (seEspera06 ? 4 : 3);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data                    
                    escribir("Numero de secuencia " + vectorhex[idxSeq]);
                    //verificamos  el byte feature                   
                    isticket = (vectorhex[indxTicket].equals("02"));//Con ticket true, else false.
                    int ticketLen = Integer.parseInt(vectorhex[indxTicket + 3], 16);
                    byte[] ticket = new byte[ticketLen];
                    int i = 0;
                    for (int j = indxTicket + 4; j < indxTicket + ticketLen + 4; j++) {
                        ticket[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                        i++;
                    }
                    if (isticket) {
                        String ticketS = tramasElster.encode(ticket, ticket.length); //vic 14-08-19
                        escribir("ticket " + tramasElster.encode(ticket, ticket.length));
                        String vpass[] = password.trim().split("-");
                        if (vpass.length == 2) {
                            nivelacceso = Integer.parseInt(vpass[1]);
                        }
                        user = TramasRemotalElsterA1800.encrypt(ticketS, vpass[0]);
                        escribir("ENC " + tramasElster.encode(user, user.length));
                        escribir("Nivel de acceso " + nivelacceso);
                    }
                    intentosNACK = 0;
                    enviaVelocidad();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);                        
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaVelocidad() {
        //enviamos el dato con el id selecionado
        escribir("Envia velocidad");
        byte trama[] = tramasElster.getVelocidad();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lidentidad = false;
        lvelocidad = true;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void revisarVelocidad(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Velocidad";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data
                    byte trama[] = tramasElster.getTiming();
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    trama[8] = 0x14;
                    trama[9] = (byte) ((timeout / 1000) + 1);
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    lvelocidad = false;
                    ltiming = true;
                    escribir("Envia Timing");
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarLogon(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Logon";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00")) {
                    escribir("Numero de secuencia " + vectorhex[4]);
                    if (isticket) {
                        //hay autenticacion
                        byte trama[] = tramasElster.getAutenticacion();
                        trama[1] = id;
                        trama[2] = getSpoling(bitSpoling);
                        trama[2] = (byte) ((trama[2] + version) & 0x23);
                        int i = 0;
                        trama[8] = (byte) (Integer.parseInt(Integer.toHexString(nivelacceso), 16) & 0xFF);
                        try {
                            for (int j = 9; j < 17; j++) {
                                trama[j] = user[i];
                                i++;
                            }
                        } catch (Exception e) {
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }

                        byte nuevatrama[] = calcularnuevocrc(trama);
                        llogon = false;
                        lautenticacion = true;
                        escribir("Envia autenticacion");
                        reintentoReenvio = 0;
                        intentosNACK = 0;
                        enviaTrama2(nuevatrama, "", (byte) (0x06));
                        break;
                    } else {
                        //se salta la trama de autenticacion a ST06
                        byte trama[] = tramasElster.getST06();
                        trama[1] = id;
                        trama[2] = getSpoling(bitSpoling);
                        trama[2] = (byte) ((trama[2] + version) & 0x23);
                        byte nuevatrama[] = calcularnuevocrc(trama);
                        llogon = false;
                        lST06 = true;
                        primerbloque = true;
                        seEspera06 = true;
                        escribir("Envia ST06");
                        enviaTrama2(nuevatrama, "", (byte) 0x06);
                        break;
                    }
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {                       
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarAutenticacion(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }

        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Autenticacion";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00")) { //ack del data
                    escribir("Numero de secuencia " + vectorhex[4]);
                    byte trama[] = tramasElster.getST06();
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    lautenticacion = false;
                    lST06 = true;
                    primerbloque = true;
                    escribir("ENVIA ST06");
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    verificaSerial = true;
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        //valida ack del data
                        escribir("Desconexion - Error de autenticacion");
                        escribir("Revisar nivel de acceso y contraseña");
                        reiniciaComunicacion();
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {                        
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {                        
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarTiming(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Timing";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (vectorhex[indxACK].equals("00") || vectorhex[indxACK].equals("0A")) { //ack del data
                    byte trama[] = tramasElster.getLogon();
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    ltiming = false;
                    llogon = true;
                    escribir("Envia Logon");
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {                        
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST06(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        String peticion = "ST06";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (!vectorhex[indxBlock].equals("00")) {
                        if (vectorhex[indxACK].equals("00")) {
                            if (verificaSerial) {
                                if (validaSerial(vectorhex)) {
                                    verificaSerial = false;
                                }
                            }
                            if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                                expSeq = primerbloque ? expSeq : expSeq - 1;
                                primerbloque = false;
                                getSpoling(bitSpoling);
                                enviaTrama2(null, "ACK", (byte) 0x06);
                                break;
                            } else {
                                if (frameCounter == framesQty) {
                                    escribir("Secuencia incorrecta");
                                    handleACK();
                                } else {
                                    frameCounter++;
                                }
                            }
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Recibe NACK");
                                handleForwarding(peticion);
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        if (!verificaSerial) {
                            //********sincronizamos con SIC*****************
                            time = obtenerHora();
                            deltatimesync1 = obtenerHora();
                            //***** fin sincronizacion con SIC *****
                            byte trama[] = tramasElster.getST55FechaAct();
                            trama[1] = id;
                            trama[2] = getSpoling(bitSpoling);
                            trama[2] = (byte) ((trama[2] + version) & 0x23);
                            byte nuevatrama[] = calcularnuevocrc(trama);
                            lST06 = false;
                            lST055factual = true;
                            escribir("Envia ST055");
                            seEspera06 = true;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            enviaTrama2(nuevatrama, "", (byte) 0x06);
                            break;
                        } else {
                            byte trama[] = tramasElster.getST05();
                            trama[1] = id;
                            trama[2] = getSpoling(bitSpoling);
                            trama[2] = (byte) ((trama[2] + version) & 0x23);
                            byte nuevatrama[] = calcularnuevocrc(trama);
                            lST06 = false;
                            lST05 = true;
                            escribir("Envia ST005");
                            seEspera06 = true;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            enviaTrama2(nuevatrama, "", (byte) 0x06);
                            break;
                        }
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST05(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxSerial;
        String peticion = "ST05";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxSerial = (seEspera06 ? 22 : 21);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    boolean continuar = false;
                    try {
                        String datoserial = vectorhex[indxSerial] + vectorhex[indxSerial + 1] + vectorhex[indxSerial + 2] + vectorhex[indxSerial + 3] + vectorhex[indxSerial + 4] + vectorhex[indxSerial + 5] + vectorhex[indxSerial + 6] + vectorhex[indxSerial + 7];
                        datoserial = Hex2ASCII(datoserial);
                        if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {//seriemedidor.equals(datoserial)) {
                            escribir("Numero de serial " + datoserial);
                            continuar = true;
                        } else {
                            escribir("Numero de serial incorrecto");
                        }
                    } catch (Exception e) {
                        escribir(getErrorString(e.getStackTrace(), 3));
                        escribir("Excepción en revisar serial: " + e.getStackTrace()[0] + "L");
                    }
                    if (continuar) {
                        //********sincronizamos con SIC*****************
                        time = obtenerHora();
                        deltatimesync1 = obtenerHora();
                        //***** fin sincronizacion con SIC *****
                        byte trama[] = tramasElster.getST55FechaAct();
                        trama[1] = id;
                        trama[2] = getSpoling(bitSpoling);
                        trama[2] = (byte) ((trama[2] + version) & 0x23);
                        byte nuevatrama[] = calcularnuevocrc(trama);
                        lST05 = false;
                        lST055factual = true;
                        escribir("Envia ST055");
                        reintentoReenvio = 0;
                        intentosNACK = 0;
                        enviaTrama2(nuevatrama, "", (byte) 0x06);
                        break;
                    } else {
                        if (frameCounter == framesQty) {
                            escribir("Desconexion Error de serial");
                            cerrarPuerto(true);                            
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                            cerrarLog("Desconexion Error de serial", false);
                            leer = false;                            
                        } else {
                            frameCounter++;
                        }
                    }
                } else {//nack
                    if (frameCounter == framesQty) {                        
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST055(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxfecha;
        String peticion = "ST055";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxfecha = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                //System.out.println("Revisando la fecha actual");
                if (vectorhex[indxACK].equals("00")) { //ack del data
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    deltatimesync2 = obtenerHora();
                    //fecha actual
                    String año = "" + Integer.parseInt(vectorhex[indxfecha], 16);
                    String mes = "" + Integer.parseInt(vectorhex[indxfecha + 1], 16);
                    if (mes.length() == 1) {
                        mes = "0" + mes;
                    }
                    String dia = "" + Integer.parseInt(vectorhex[indxfecha + 2], 16);
                    if (dia.length() == 1) {
                        dia = "0" + dia;
                    }
                    String hora = "" + Integer.parseInt(vectorhex[indxfecha + 3], 16);
                    if (hora.length() == 1) {
                        hora = "0" + hora;
                    }
                    String min = "" + Integer.parseInt(vectorhex[indxfecha + 4], 16);
                    if (min.length() == 1) {
                        min = "0" + min;
                    }
                    String seg = "" + Integer.parseInt(vectorhex[indxfecha + 5], 16);
                    if (seg.length() == 1) {
                        seg = "0" + seg;
                    }
                    fechaactualsync = "" + año + mes + dia + hora + min + seg;
                    try {
                        escribir("Fecha actual Medidor " + sdf.parse(fechaactualsync).toString());
                        escribir("Fecha actual PC " + new Date().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    solicitar = true;
                    try {
                        tsfechaactual = new Timestamp(sdf.parse(fechaactualsync).getTime());
                        escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                        escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                        desfase = (time.getTime() - (tsfechaactual.getTime() + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)))) / 1000;
                        if (Math.abs(desfase) > ndesfasepermitido) {
                            solicitar = false;
                            escribir("No se solicitara el perfil de carga");
                        }
                    } catch (Exception e) {
                        escribir("Error al calcular desfase, por tanto no se continuará.");
                        escribir(getErrorString(e.getStackTrace(), 3));
                        solicitar = false;  
                    }
                    
                    lST055factual = false;
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    if (solicitar) {
                        try {
                            cp.actualizaDesfase(desfase, med.getnSerie());
                        } catch (Exception e) {
                            escribir("Error al actualizar desfase en DB.");
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }
                        if (lconfhora) {
                            enviaConfHoraST007();
                            break;
                        } else {
                            enviaST061();
                            break;
                        }
                    } else {
                        if (frameCounter == framesQty) {
                            escribir("Desconexion Error por Desfase");
                            cerrarPuerto(true);
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error por Desfase");
                            cerrarLog("Desconexion Error por Desfase", false);
                            leer = false;                            
                        } else {
                            frameCounter++;
                        }
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaConfHoraST007() {
        String lfecha = "";
        time = obtenerHora();
        lfecha = sdf.format(new Date(time.getTime()));
        byte trama[] = tramasElster.getST007();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        trama[13] = (byte) 0x00;
        trama[14] = (byte) 0x03;
        trama[15] = (byte) (Integer.parseInt(lfecha.substring(0, 2)) & 0xFF);
        trama[16] = (byte) (Integer.parseInt(lfecha.substring(2, 4)) & 0xFF);
        trama[17] = (byte) (Integer.parseInt(lfecha.substring(4, 6)) & 0xFF);
        trama[18] = (byte) (Integer.parseInt(lfecha.substring(6, 8)) & 0xFF);
        trama[19] = (byte) (Integer.parseInt(lfecha.substring(8, 10)) & 0xFF);
        trama[20] = (byte) (Integer.parseInt(lfecha.substring(10, 12)) & 0xFF);
        trama[21] = (byte) 0x00;
        trama = validaCheckSum(trama, 11);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST007 = true;
        escribir(" Solicitud de Configuracion de hora " + time);
        seEspera06 = true;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST061() {
        byte trama[] = tramasElster.getST61ConfiPerfil();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST061 = true;
        escribir("Envia ST061");
        seEspera06 = true;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void revisarST061(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxConst061;
        String peticion = "ST061";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxConst061 = (seEspera06 ? 19 : 18);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    numCanales = Integer.parseInt(vectorhex[indxConst061 + 2], 16);
                    intervalos = Integer.parseInt(vectorhex[indxConst061 + 3], 16);
                    NBR_BLK_INTS_SET1 = Integer.parseInt(vectorhex[indxConst061 + 1] + vectorhex[indxConst061], 16);
                    escribir("Numero de canales " + numCanales);
                    escribir("Intervalos de tiempo " + intervalos);
                    escribir("Número de Intervalos por Bloque: " + NBR_BLK_INTS_SET1);
                    byte trama[] = tramasElster.getST55FechaAct();
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    lST061 = false;
                    lST055factual2 = true;
                    escribir("Envia ST055");
                    seEspera06 = true;
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                        
                    }                    
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST055_2(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxfecha;
        String peticion = "ST055_2";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxfecha = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    //fecha actual
                    String año = "" + Integer.parseInt(vectorhex[indxfecha], 16);
                    String mes = "" + Integer.parseInt(vectorhex[indxfecha + 1], 16);
                    if (mes.length() == 1) {
                        mes = "0" + mes;
                    }
                    String dia = "" + Integer.parseInt(vectorhex[indxfecha + 2], 16);
                    if (dia.length() == 1) {
                        dia = "0" + dia;
                    }
                    String hora = "" + Integer.parseInt(vectorhex[indxfecha + 3], 16);
                    if (hora.length() == 1) {
                        hora = "0" + hora;
                    }
                    String min = "" + Integer.parseInt(vectorhex[indxfecha + 4], 16);
                    if (min.length() == 1) {
                        min = "0" + min;
                    }
                    String seg = "" + Integer.parseInt(vectorhex[indxfecha + 5], 16);
                    if (seg.length() == 1) {
                        seg = "0" + seg;
                    }
                    fechaactual = "" + año + mes + dia + hora + min + seg;
                    try {
                        escribir("Fecha actual Medidor " + sdf.parse(fechaactual).toString());
                        escribir("Fecha actual PC " + obtenerHora());
                        if (med.getFecha() != null) {
                            escribir("Fecha Ultima Lectura " + med.getFecha());
                            long diffInMillies = Math.abs((obtenerHora().getTime() - (desfase * 1000)) - med.getFecha().getTime());
                            long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
                            nminutes = (int) diff;
                            escribir("Numero de minutos de diferencia calculados: " + nminutes);
                        }
                    } catch (Exception e) {
                    }
                    byte trama[] = tramasElster.getST53TimeOffset();
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    lST055factual2 = false;
                    lST053 = true;
                    escribir("Envia ST053");
                    seEspera06 = true;
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST053(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        String peticion = "ST053";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    byte trama[] = tramasElster.getST62LpCtrl();
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    lST053 = false;
                    lST062 = true;
                    escribir("Envia ST062");
                    seEspera06 = true;
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST062(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        int indxLenConst;
        String peticion = "ST062";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            indxLenConst = (seEspera06 ? 9 : 8);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00")) { //ack del data
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    vSt062 = new String[(Integer.parseInt(vectorhex[indxLen] + vectorhex[indxLen + 1], 16)) + indxLenConst];
                    System.arraycopy(vectorhex, 0, vSt062, 0, vSt062.length);
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    if (lregistros || lacumulados) {//permite registros pide la ST00
                        enviaST00();
                    } else {//no requiere registros pide la MT017
                        enviaMT017();
                    }
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {                        
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaST00() {
        byte trama[] = tramasElster.getST00();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST062 = false;
        lST00 = true;
        seEspera06 = true;
        escribir("Envia ST00");
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaMT015() {
        byte trama[] = tramasElster.getMT015PrimMeteringInfo();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST015 = false;
        lMT015 = true;
        idxCopyFrom = 0;
        escribir("Envia MT015");
        seEspera06 = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaMT016() {
        byte trama[] = tramasElster.getMT16MeteringInfo();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lMT015 = false;
        lMT016 = true;
        escribir("Envia MT016");
        seEspera06 = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaMT017() {
        byte trama[] = tramasElster.getMT17();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST062 = false;
        lMT017 = true;
        primerbloque = true;
        tamañoacumuladolMT017 = 0;
        vMT017 = new Vector<String[]>();
        seEspera06 = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        escribir("Envia MT017");
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void revisarST00(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxFMT_CTRL1;
        String peticion = "ST00";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxFMT_CTRL1 = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                    escribir("Número de secuencia inicial " + vectorhex[indxBlock]);
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa                                               
                    if (!vectorhex[indxBlock].equals("00")) {
                        escribir("Número de secuencia: " + vectorhex[indxBlock]);
                        FORMAT_CONTROL_1 = Integer.parseInt(vectorhex[indxFMT_CTRL1], 16);
                        getSpoling(bitSpoling);
                        enviaTrama2(null, "ACK", (byte) 0x06);
                    } else {
                        lST00 = false;
                        enviaMT017();
                    }
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarMT017(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        int indxLenConst;
        String peticion = "MT017";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            indxLenConst = (seEspera06 ? 9 : 8);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                    escribir("numero de secuencia inicial " + vectorhex[indxBlock]);
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa                                               
                    if (!vectorhex[indxBlock].equals("00")) {
                        escribir("Número de secuencia: " + vectorhex[indxBlock]);
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            if (primerbloque) {
                                seEspera06 = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                //guardamos el tamaño total de la tabla MT017
                                tamañototalMT017 = Integer.parseInt((vectorhex[indxLen + 2] + vectorhex[indxLen + 3] + vectorhex[indxLen + 4]), 16);
                                tamañoacumuladolMT017 = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            } else {
                                tamañoacumuladolMT017 += Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            }
                            String[] tramatemp = new String[(Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst];
                            System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst);
                            vMT017.add(tramatemp);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            seEspera06 = false;
                            primerbloque = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        tamañoacumuladolMT017 += Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        String[] tramatemp = new String[(Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst];
                        System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst);
                        vMT017.add(tramatemp);
                        if (tamañototalMT017 == tamañoacumuladolMT017 - 4) {
                            enviaST015();
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                //se envia sin 06 no se guardo bien la trama
                                //System.out.println("No se guarda bien la trama");
                                escribir("No se guarda la trama correctamente");
                                vMT017 = new Vector<>();
                                tamañoacumuladolMT017 = 0;
                                tamañototalMT017 = 0;
                                primerbloque = true;
                                seEspera06 = true;
                                enviaTrama2(ultimatramaEnviada, "Solicitud MT017", (byte) 0x00);
                                break;
                            } else {
                                frameCounter++;
                            }
                        }
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST015(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        String peticion = "ST015";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (!vectorhex[indxBlock].equals("00")) {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            if (primerbloque) {
                                seEspera06 = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                regSt015 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                            }
                            System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regSt015, idxCopyFrom, (primerbloque ? seqLen - 3 : seqLen));
                            idxCopyFrom += (primerbloque ? seqLen - 3 : seqLen);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            seEspera06 = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                this.handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        if (primerbloque) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            regSt015 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                        }
                        System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regSt015, idxCopyFrom, (primerbloque ? seqLen - 4 : seqLen - 1));
                        enviaMT015();
                        break;
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        seEspera06 = false;
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarMT015(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxConstMT015;
        int indxlen;//Siempre se usará con 8, porque sólo se usa para EE
        String peticion = "MT015";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxConstMT015 = (seEspera06 ? 10 : 9);
            indxlen = (seEspera06 ? 9 : 8);//Siempre se usará con 8, porque sólo se usa para EE
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00") || (!seEspera06 && vectorhex.length - indxlen != 1)) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    Ext_Mult_Scale_Factor = vectorhex[indxConstMT015];
                    escribir("Factor de Escalado: " + Integer.parseInt(Ext_Mult_Scale_Factor, 16));
                    External_Multiplier = vectorhex[indxConstMT015 + 4] + vectorhex[indxConstMT015 + 3] + vectorhex[indxConstMT015 + 2] + vectorhex[indxConstMT015 + 1];
                    escribir("Factor de Multiplicaciòn: " + Integer.parseInt(External_Multiplier, 16));
                    enviaMT016();
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {                        
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarMT016(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxConstMT016;
        String peticion = "MT016";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxConstMT016 = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    Instrumentation_Scale_Factor = vectorhex[indxConstMT016];
                    enviaST063();
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            }
            if (frameCounter == framesQty) {
                if (vectorhex[0].equals("15")) {
                    escribir("Recibe NACK");
                    handleForwarding(peticion);
                } else {
                    escribir("Trama incorrecta");
                    handleNACK();
                }
            } else {
                frameCounter++;
            }
        }
    }

    private void revisarST063(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxBlock;
        int indxLenConst;//dependencia con 06 o EE desconocida.
        String peticion = "ST063";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxBlock = (seEspera06 ? 4 : 3);
            indxLenConst = seEspera06 ? 9 : 8;//dependencia con 06 o EE desconocida.
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                primerbloque = false;
                escribir("Numero de secuencia " + vectorhex[indxBlock]);
                reintentoReenvio = 0;
                intentosNACK = 0;
                if (leventos) {
                    enviaEventosST076();
                    break;
                } else if (lregistros || lacumulados) {
                    reintentoReenvio = 0;
                    intentosNACK = 0;
                    lST063 = false;
                    enviaST021();
                    break;
                } else if (lperfil) {
                    if (Integer.parseInt(vectorhex[indxLenConst], 16) + (seEspera06 ? 13 : 12) == vectorhex.length) { //viene data
                        enviaPerfilMT064();
                        break;
                    } else {
                        if (frameCounter == framesQty) {
                            escribir("Respuesta ST063 sin data");
                            reiniciaComunicacion();
                        } else {
                            frameCounter++;
                        }
                    }
                } else {
                    escribir("Sin solicitudes adicionales");
                    //cerrar
                    byte trama[] = tramasElster.getLogoff();
                    seEspera06 = true;
                    trama[1] = id;
                    trama[2] = getSpoling(bitSpoling);
                    trama[2] = (byte) ((trama[2] + version) & 0x23);
                    lST063 = false;
                    llogoff = true;
                    byte nuevatrama[] = calcularnuevocrc(trama);
                    enviaTrama2(nuevatrama, "", (byte) 0x06);
                    break;
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaEventosST076() {
        byte trama[] = tramasElster.getST76Eventos();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST063 = false;
        lST076 = true;
        EventData = new Vector<String[]>();
        escribir("Envia ST076");
        primerbloque = true;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaPerfilMT064() {
        ElementCount = obtenerElementCount();
        offset = obtenerOffsetM();
        String codigo = Integer.toHexString(offset).toUpperCase();
        escribir("Offset" + codigo);
        while (codigo.length() < 6) {
            codigo = "0" + codigo;
        }
        String ecount = Integer.toHexString(ElementCount).toUpperCase();
        eCountI = ecount;
        escribir("Element count " + ecount);
        while (ecount.length() < 4) {
            ecount = "0" + ecount;
        }
        byte trama[] = tramasElster.getMT64Perfil();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        //off set
        trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
        trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
        trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
        //element count
        trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
        trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
        byte nuevatrama[] = calcularnuevocrc(trama);
        escribir("Envia MT064");
        lST063 = false;//viene de revisa 063
        lST076 = false;//viene de revisa076
        lST064Perfil = true;
        tamañototalbloque = 0;
        tamañobloque = 0;
        desglosePerfil = new ArrayList<>();
        primerbloque = true;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void revisarST076(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        int indxLenConst;
        String peticion = "ST076";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            indxLenConst = (seEspera06 ? 9 : 8);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (vectorhex[indxBlock].equals("00")) {
                        String[] tramatemp = new String[(Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst];
                        System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst);
                        EventData.add(tramatemp);
                        if (lregistros || lacumulados) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            lST076 = false;
                            enviaST021();
                            break;
                        } else if (lperfil) {
                            if ((vectorhex.length - indxLenConst) != 1) { //viene data
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                enviaPerfilMT064();
                                break;
                            } else {
                                if (frameCounter == framesQty) {
                                    escribir("Respuesta ST076 sin data");
                                    reiniciaComunicacion();
                                    break;
                                } else {
                                    frameCounter++;
                                }
                            }
                        } else {
                            escribir("Sin solicitudes adicionales");
                            byte trama[] = tramasElster.getLogoff();
                            seEspera06 = true;
                            trama[1] = id;
                            trama[2] = getSpoling(bitSpoling);
                            trama[2] = (byte) ((trama[2] + version) & 0x23);
                            lST076 = false;
                            llogoff = true;
                            byte nuevatrama[] = calcularnuevocrc(trama);
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            enviaTrama2(nuevatrama, "", (byte) 0x06);
                            break;
                        }
                    } else {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            String[] tramatemp = new String[(Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst];
                            System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst);
                            EventData.add(tramatemp);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            seEspera06 = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void enviaST015() {
        byte trama[] = tramasElster.getST15Constantes();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lMT017 = false;
        lST015 = true;
        primerbloque = true;
        seEspera06 = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        idxCopyFrom = 0;
        vST015 = new Vector<>();
        escribir("Envia ST015");
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST021() {
        byte trama[] = tramasElster.getST021();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lST021 = true;
        escribir("Envia ST021");
        seEspera06 = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST022() {
        byte trama[] = tramasElster.getST022();
        trama[1] = id;
        if (seEspera06) {
            trama[2] = getSpoling(bitSpoling);
        } else {
            if (spoling) {
                spoling = false;
                trama[2] = (byte) 0x00;
            } else {
                spoling = true;
                trama[2] = (byte) 0x20;
            }
        }
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        idxCopyFrom = 0;
        lST021 = false;
        lST022 = true;
        vST022 = new Vector<String[]>();

        escribir("Envia ST022");
        primerbloque = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST023() {
        byte trama[] = tramasElster.getST023();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        idxCopyFrom = 0;
        lST022 = false;
        lST023 = true;
        vST023 = new Vector<String[]>();
        escribir("Envia ST023");
        primerbloque = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST027() {
        byte trama[] = tramasElster.getST027();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        idxCopyFrom = 0;
        lST023 = false;
        lST027 = true;
        vST027 = new Vector<String[]>();
        escribir("Envia ST027");
        primerbloque = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST028() {
        byte trama[] = tramasElster.getST028();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        idxCopyFrom = 0;
        lST027 = false;
        lST028 = true;
        vST028 = new Vector<String[]>();
        escribir("Envia ST028");
        primerbloque = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void enviaST063() {
        byte trama[] = tramasElster.getST63LpStatus();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lMT016 = false;
        lST063 = true;
        escribir("Envia ST063");
        seEspera06 = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
    }

    private void revisarST021(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxData;
        String peticion = "ST021";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxData = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa                                               
                    escribir("Número de secuencia: " + vectorhex[indxBlock]);
                    NBR_SELF_READS = Integer.parseInt(vectorhex[indxData + 2], 16);
                    NBR_SUMMATIONS = Integer.parseInt(vectorhex[indxData + 3], 16);
                    NBR_DEMANDS = Integer.parseInt(vectorhex[indxData + 4], 16);
                    NBR_COIN_VALUES = Integer.parseInt(vectorhex[indxData + 5], 16);
                    NBR_PRESENT_DEMANDS = Integer.parseInt(vectorhex[indxData + 8], 16);
                    NBR_PRESENT_VALUES = Integer.parseInt(vectorhex[indxData + 9], 16);
                    NBR_OCCUR = Integer.parseInt(vectorhex[indxData + 6], 16);
                    REG_FUNC1_FLAG = Integer.parseInt(vectorhex[indxData], 16);
                    enviaST022();
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST022(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        String peticion = "ST022";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                    escribir("Número de secuencia inicial " + vectorhex[indxBlock]);
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa  
                    escribir("Número de secuencia " + vectorhex[indxBlock]);
                    if (!vectorhex[indxBlock].equals("00")) {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            if (primerbloque) {
                                seEspera06 = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                regST022 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                            }
                            System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST022, idxCopyFrom, (primerbloque ? seqLen - 3 : seqLen));
                            idxCopyFrom += (primerbloque ? seqLen - 3 : seqLen);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            seEspera06 = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        if (primerbloque) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            regST022 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                        }
                        System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST022, idxCopyFrom, (primerbloque ? seqLen - 4 : seqLen - 1));
                        enviaST023();
                        break;
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST023(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        String peticion = "ST023";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            if (tramaOK && !uncomplete && !delayed) {
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (!vectorhex[indxBlock].equals("00")) {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            if (primerbloque) {
                                seEspera06 = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                regST023 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                            }
                            System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST023, idxCopyFrom, (primerbloque ? seqLen - 3 : seqLen));
                            idxCopyFrom += (primerbloque ? seqLen - 3 : seqLen);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            seEspera06 = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        if (primerbloque) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            regST023 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                        }
                        System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST023, idxCopyFrom, (primerbloque ? seqLen - 4 : seqLen - 1));
                        if (lregistros) {
                            enviaST027();
                        } else if (lacumulados) {
                            byte trama[] = tramasElster.getLogoff();
                            seEspera06 = true;
                            trama[1] = id;
                            trama[2] = getSpoling(bitSpoling);
                            trama[2] = (byte) ((trama[2] + version) & 0x23);
                            lST023 = false;
                            llogoff = true;
                            byte nuevatrama[] = calcularnuevocrc(trama);
                            enviaTrama2(nuevatrama, "", (byte) 0x06);
                        }
                        break;
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST027(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        String peticion = "ST027";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (!vectorhex[indxBlock].equals("00")) {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            if (primerbloque) {
                                seEspera06 = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                regST027 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                            }
                            System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST027, idxCopyFrom, (primerbloque ? seqLen - 3 : seqLen));
                            idxCopyFrom += (primerbloque ? seqLen - 3 : seqLen);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            seEspera06 = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        if (primerbloque) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            regST027 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                        }
                        System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST027, idxCopyFrom, (primerbloque ? seqLen - 4 : seqLen - 1));
                        enviaST028();
                        break;
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST028(String[] vectorhexO) {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        String peticion = "ST028";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (!vectorhex[indxBlock].equals("00")) {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                            if (primerbloque) {
                                seEspera06 = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                regST028 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                            }
                            System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST028, idxCopyFrom, (primerbloque ? seqLen - 3 : seqLen));
                            idxCopyFrom += (primerbloque ? seqLen - 3 : seqLen);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            seEspera06 = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        if (primerbloque) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            regST028 = new String[Integer.parseInt(vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16)];//Tamaño total de la data
                        }
                        System.arraycopy(vectorhex, (primerbloque ? indxLen + 5 : indxLen + 2), regST028, idxCopyFrom, (primerbloque ? seqLen - 4 : seqLen - 1));
                        if (lperfil) {
                            enviaPerfilMT064();
                        } else {
                            escribir("Sin solicitudes adicionales");
                            //cerrar
                            byte trama[] = tramasElster.getLogoff();
                            seEspera06 = true;
                            trama[1] = id;
                            trama[2] = getSpoling(bitSpoling);
                            trama[2] = (byte) ((trama[2] + version) & 0x23);
                            lST028 = false;
                            llogoff = true;
                            byte nuevatrama[] = calcularnuevocrc(trama);
                            enviaTrama2(nuevatrama, "", (byte) 0x06);
                        }
                        break;
                    }
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }
    }

    private void revisarST064(String[] vectorhexO) {

        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        int indxLenConst;
        String peticion = "ST064";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxBlock = (seEspera06 ? 4 : 3);
            indxLen = (seEspera06 ? 5 : 4);
            indxLenConst = (seEspera06 ? 9 : 8);
            if (tramaOK && !uncomplete && !delayed) {
                //tramaOK = false;
                perfilincompleto = true;
                if (primerbloque) {
                    boolean belongs = Integer.parseInt(eCountI, 16) == Integer.parseInt(vectorhex[indxLenConst - 1] + vectorhex[indxLenConst], 16);
                    if (!belongs) {
                        if (frameCounter == framesQty) {
                            escribir("No pertenece a las tramas del perfil de carga con E Count: " + eCountI);
                            handleJustNACK();
                            break;
                        } else {
                            frameCounter++;
                            continue;
                        }
                    }
                    //contadorST064 = Integer.parseInt(vectorhex[indxBlock], 16);
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                    escribir("numero de secuencia inicial " + vectorhex[indxBlock]);
                }
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data
                    if (!vectorhex[indxBlock].equals("00")) {
                        escribir("Número de secuencia: " + vectorhex[indxBlock]);
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            if ((Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) == 1) {//viene un nack
                                if (frameCounter == framesQty) {
                                    escribir("El medidor rechaza la petición");
                                    if (primerbloque) {
                                        handleForwarding(peticion);
                                    } else {
                                        handleJustNACK();
                                    }
                                    break;
                                } else {
                                    frameCounter++;
                                }
                            } else {
                                int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                                if (primerbloque) {
                                    seEspera06 = false;
                                    reintentoReenvio = 0;
                                    intentosNACK = 0;
                                    profileDataTemp = new ArrayList<>();
                                    tamañototalbloque = Integer.parseInt(vectorhex[indxLen + 2] + vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16);
                                    tamañobloque = seqLen;
                                } else {
                                    tamañobloque += seqLen;
                                }
                                String[] trama = new String[primerbloque ? seqLen - 3 : seqLen];
                                System.arraycopy(vectorhex, primerbloque ? indxLen + 5 : indxLen + 2, trama, 0, primerbloque ? seqLen - 3 : seqLen);
                                profileDataTemp.addAll(Arrays.asList(trama));
                                expSeq = primerbloque ? expSeq : expSeq - 1;
                                escribir("Secuencia esperada: " + expSeq);
                                seEspera06 = false;
                                primerbloque = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                getSpoling(bitSpoling);
                                //contadorST064--;
                                enviaTrama2(null, "ACK", (byte) 0x06);
                                break;
                            }
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleACK();
                            } else {
                                frameCounter++;
                            }
                        }
                    } else {
                        int seqLen = Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16);
                        if (primerbloque) {
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            profileDataTemp = new ArrayList<>();
                            tamañototalbloque = Integer.parseInt(vectorhex[indxLen + 2] + vectorhex[indxLen + 3] + vectorhex[indxLen + 4], 16);
                            tamañobloque = seqLen;
                        } else {
                            tamañobloque += seqLen;
                        }
                        if ((tamañobloque - 4) == tamañototalbloque) {
                            String[] trama = new String[primerbloque ? seqLen - 4 : seqLen - 1];
                            System.arraycopy(vectorhex, primerbloque ? indxLen + 5 : indxLen + 2, trama, 0, primerbloque ? seqLen - 4 : seqLen - 1);
                            profileDataTemp.addAll(Arrays.asList(trama));
                            desglosePerfil.add(String.join("", profileDataTemp));
                            //validamos el checksum por hacer
                            offset = offset - ElementCount;
                            if (offset < 0) {//es la ultima trama del perfil de carga
                                byte tramaL[] = tramasElster.getLogoff();
                                primerbloque = true;
                                seEspera06 = true;
                                tramaL[1] = id;
                                tramaL[2] = getSpoling(bitSpoling);
                                tramaL[2] = (byte) ((tramaL[2] + version) & 0x23);
                                lST064Perfil = false;
                                llogoff = true;
                                byte nuevatrama[] = calcularnuevocrc(tramaL);
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                perfilincompleto = false;
                                enviaTrama2(nuevatrama, "", (byte) 0x06);
                                break;
                            } else {
                                //se calcula el siguiente bloque
                                byte tramaL[] = tramasElster.getMT64Perfil();
                                tramaL[1] = id;
                                tramaL[2] = getSpoling(bitSpoling);
                                tramaL[2] = (byte) ((tramaL[2] + version) & 0x23);
                                String codigo = Integer.toHexString(offset).toUpperCase();
                                while (codigo.length() < 6) {
                                    codigo = "0" + codigo;
                                }
                                escribir("Offset" + codigo);
                                String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                while (ecount.length() < 4) {
                                    ecount = "0" + ecount;
                                }
                                //Offset
                                tramaL[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                tramaL[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                tramaL[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                //Element count
                                tramaL[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                tramaL[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                byte nuevatrama[] = calcularnuevocrc(tramaL);
                                seEspera06 = true;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                primerbloque = true;
                                enviaTrama2(nuevatrama, "", (byte) 0x06);
                                break;
                            }
                        } else {
                            //bloque no esta completo
                            if (frameCounter == framesQty) {
                                escribir("tamaño bloque incompleto tamaño total " + tamañototalbloque + " tamaño bloque " + tamañobloque);
                                desglosePerfil = new ArrayList<>();
                                //profileData = new ArrayList<>();
                                profileDataTemp = new ArrayList<>();
                                tamañobloque = 0;
                                tamañototalbloque = 0;
                                primerbloque = true;
                                seEspera06 = true;
                                enviaTrama2(ultimatramaEnviada, "Solicitud de " + peticion, (byte) 0x00);
                                break;
                            } else {
                                frameCounter++;
                            }
                        }
                    }
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            } else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }
            }
        }

    }

    private void revisarLogoff(String[] vectorhex) {
        if (uncomplete) {
            fragment2 = vectorhex;
            vectorhex = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhex); // En el logoff valdría la pena revisar la trama? 
        String peticion = "Logout";
        if (tramaOK && !uncomplete && !delayed || llogoff) {
            tramaOK = false;
            llogoff = false;
            cerrarPuerto(false);
            escribir("Logout");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
            if (leventos) {
                desglosaST076();
            }
            if (lregistros || lacumulados || lperfil) {
                escribir("Desglosa MT 017");
                desglosaMT017();
            }
            if (lregistros) {
                desglosaST022();
                organizaSumations();
                organizaDemands();
                desglosaST023();
                desglosaST027();
                organizaPresentDemands();
                organizaPresentValues();
                desglosaST028();
                escribir("ST023: " + vdesgloseSt023);
                escribir("Number Summations: " + NBR_SUMMATIONS + "Number Demands: " + NBR_DEMANDS);
                escribir("Orden Summations: " + Arrays.toString(OrdenSumations));
                escribir("Summation Select: " + Arrays.toString(vSumationSelect));
                escribir("Orden Demands: " + Arrays.toString(OrdenDemands));
                escribir("Demand Select: " + Arrays.toString(vDemandSelect));
                escribir("Number Occurrencies: " + NBR_OCCUR);
                escribir("ST028: " + vdesgloseSt028);
                escribir("Number Present Demands: " + NBR_PRESENT_DEMANDS + "Number Present Values: " + NBR_PRESENT_VALUES);
                escribir("Orden Present Demands: " + Arrays.toString(OrdenPresentDemands));
                escribir("Present Demands Select: " + Arrays.toString(vPresentdemandSelect));
                escribir("Orden Present Values: " + Arrays.toString(OrdenPresentValues));
                escribir("Present Values: " + Arrays.toString(vPresentValues));
                escribir("Orden Fases Present Values: " + Arrays.toString(OrdenFasesPresentValues));
                cp.almacenaST023(med.getnSerie(), med.getMarcaMedidor().getCodigo(), fechaactual, vdesgloseSt023, NBR_SUMMATIONS, NBR_DEMANDS, OrdenSumations, vSumationSelect, regMt017, OrdenDemands, vDemandSelect, NBR_OCCUR, REG_FUNC1_FLAG);
                cp.almacenaST028(med.getnSerie(), med.getMarcaMedidor().getCodigo(), fechaactual, vdesgloseSt028, NBR_PRESENT_DEMANDS, NBR_PRESENT_VALUES, OrdenPresentDemands, vPresentdemandSelect, regMt017, OrdenPresentValues, vPresentValues, OrdenFasesPresentValues);                
                med.MedLeido = true;
                cerrarLog("Leido", true);
                leer = false;
            }

            if (lacumulados) {
                desglosaST022();
                organizaSumations();
                organizaDemands();
                desglosaST023();
                escribir("ST023: " + vdesgloseSt023);
                escribir("Number Summations: " + NBR_SUMMATIONS + "Number Demands: " + NBR_DEMANDS);
                escribir("Orden Summations: " + Arrays.toString(OrdenSumations));
                escribir("Summation Select: " + Arrays.toString(vSumationSelect));
                cp.almacenaST023_Acum(med.getnSerie(), fechaactual, vdesgloseSt023, NBR_SUMMATIONS, NBR_DEMANDS, NBR_OCCUR, REG_FUNC1_FLAG, OrdenSumations, vSumationSelect, regMt017, file);                
                med.MedLeido = true;
                cerrarLog("Leido", true);
                leer = false;
            }
            if (lperfil) {
                try {                    
                    escribir("Organizando Canales");
                    organizarCanales();
                    escribir("Almacena Perfil de Carga");
                    cp.AlmacenaPerfilCargaElster(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), fechaactual, NBR_BLK_INTS_SET1, numCanales, LP_CTRL_INT_FMT_CDE1, desglosePerfil, intervalos, regST062, regSt015, regMt017, External_Multiplier, Ext_Mult_Scale_Factor, Instrumentation_Scale_Factor, OrdenCanales, file);
                    if (cp.reinicioPorVerificaVolcado) {
                        escribir("Reinicio de contingencia por inconsistencia en verificacion");
                        escribir("Ultimo registro de volcado en: " + cp.fecVerificaVolcado + " con " + cp.intervVerificaVolcado + " intervalos");
                        reiniciaComunicacion();
                    } else {
                        escribir("Almacenamiento correcto");
                        escribir("Ultimo registro de volcado en: " + cp.fecVerificaVolcado + " con " + cp.intervVerificaVolcado + " intervalos");
                        if (cp.noLeidoPorVerificaVolcado) {
                            escribir("Error actualizando LastRead: Volcado no encontrado en verificacion");                            
                            cerrarLog("Volcado no encontrado en verificacion", true);
                        } else {                            
                            med.MedLeido = true;
                            cerrarLog("Leido", true);
                        }
                        leer = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    escribir("Error procesando o almacenando perfil");
                    escribir("Ultimo registro de volcado en: " + cp.fecVerificaVolcado + " con " + cp.intervVerificaVolcado + " intervalos");
                    reiniciaComunicacion();
                }
            }

            if (!lregistros && !lacumulados && !lperfil) {                
                med.MedLeido = true;
                cerrarLog("Leido", true);
            }
        } else {
            if (vectorhex[0].equals("15")) {
                escribir("Recibe NACK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleNACK();
            }
        }
    }

    private void revisarST007(String[] vectorhex) {
        if (vectorhex[0].equals(seEspera06 ? "06" : "EE")) {
            if (vectorhex.length > (seEspera06 ? 7 : 6)) {//pose cabecera?
                if ((vectorhex.length - (seEspera06 ? 9 : 8)) >= (Integer.parseInt((vectorhex[(seEspera06 ? 5 : 4)] + vectorhex[(seEspera06 ? 6 : 5)]), 16))) { //validamos campo len
                    if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[(seEspera06 ? 5 : 4)] + vectorhex[(seEspera06 ? 6 : 5)]), 16)) + 9)) {//validamos CRC
                        if (((((byte) Integer.parseInt(vectorhex[(seEspera06 ? 3 : 2)], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            if (vectorhex[(seEspera06 ? 7 : 6)].equals("00")) { //ack del data            
                                byte trama[] = tramasElster.getST008();
                                trama[1] = id;
                                trama[2] = getSpoling(bitSpoling);
                                trama[2] = (byte) ((trama[2] + version) & 0x23);
                                lST007 = false;
                                lST008 = true;
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                perfilincompleto = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                escribir("Envia ST008");
                                enviaTrama2(nuevatrama, "", (byte) 0x06);
                            } else {
                                escribir("Recibe NACK");
                                handleForwarding(usuario);
                            }
                        } else {
                            escribir("Trama incorrecta");
                            handleJustNACK();
                        }
                    } else {//badcrc;
                        escribir("Trama incorrecta");
                        handleJustNACK();
                    }
                } else { //incompleta
                    escribir("Trama incorrecta");
                    handleJustNACK();
                }
            } else { //sin cabecera
                escribir("Trama incorrecta");
                handleJustNACK();
            }
        } else {//llego algo diferente de 06 o EE
            escribir("Trama incorrecta");
            handleJustNACK();
        }
    }

    public void revisarST008(String vectorhex[]) {
        if (vectorhex[0].equals(seEspera06 ? "06" : "EE")) {
            if (vectorhex.length > (seEspera06 ? 7 : 6)) {//pose cabecera?
                if ((vectorhex.length - (seEspera06 ? 9 : 8)) >= (Integer.parseInt((vectorhex[(seEspera06 ? 5 : 4)] + vectorhex[(seEspera06 ? 6 : 5)]), 16))) { //validamos campo len
                    if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[(seEspera06 ? 5 : 4)] + vectorhex[(seEspera06 ? 6 : 5)]), 16)) + 9)) {//validamos CRC
                        if (((((byte) Integer.parseInt(vectorhex[(seEspera06 ? 3 : 2)], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            if (vectorhex[(seEspera06 ? 7 : 6)].equals("00")) { //ack del data
                                byte trama[] = tramasElster.getLogoff();
                                seEspera06 = true;
                                trama[1] = id;
                                trama[2] = getSpoling(bitSpoling);
                                trama[2] = (byte) ((trama[2] + version) & 0x23);
                                lST008 = false;
                                llogoff = true;
                                try {                                    
                                } catch (Exception e) {
                                }
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                perfilincompleto = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                escribir("Envia Logout");
                                enviaTrama2(nuevatrama, "", (byte) 0x06);
                            } else {
                                escribir("Recibe NACK");
                                handleForwarding("Solicitud ST008");
                            }
                        } else {
                            escribir("Trama incorrecta");
                            handleJustNACK();
                        }
                    } else {//badcrc
                        escribir("Trama incorrecta");
                        handleJustNACK();
                    }
                } else { //incompleta
                    escribir("Trama incorrecta");
                    handleJustNACK();
                }
            } else { //sin cabecera
                escribir("Trama incorrecta");
                handleJustNACK();
            }
        } else {//llego algo diferente de 06 o EE
            escribir("Trama incorrecta");
            handleJustNACK();
        }
    }

    private void almacenaPerfilIncompleto() {
        escribir("Desglosa MT 017");
        desglosaMT017();
        escribir("Organizando Canales");
        organizarCanales();
        escribir("Almacena Perfil de Carga");
        cp.AlmacenaPerfilCargaElster(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), fechaactual, NBR_BLK_INTS_SET1, numCanales, LP_CTRL_INT_FMT_CDE1, desglosePerfil, intervalos, regST062, regSt015, regMt017, External_Multiplier, Ext_Mult_Scale_Factor, Instrumentation_Scale_Factor, OrdenCanales, file);
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

    public void terminaHilos() {
        try {
            tEscritura.interrupt();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("" + e.getStackTrace()[0]);
            System.err.println("" + e.getStackTrace()[1]);
            System.err.println("" + e.getStackTrace()[2]);
        }
        try {
            tReinicio.interrupt();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("" + e.getStackTrace()[0]);
            System.err.println("" + e.getStackTrace()[1]);
            System.err.println("" + e.getStackTrace()[2]);
        }        
        tEscritura = null;
        tReinicio = null;
    }

    public byte[] validaCheckSum(byte[] vectorhex, int inicio) {
        byte[] data = vectorhex;
        try {
            //verificamos si es de longitud fija
            int dato = 0x00;

            for (int i = inicio; i < data.length - 3; i++) {
                dato = dato + data[i];
            }
            dato = dato & 0xFF;
            dato = (complemento2Int(dato) & 0xFF);
            data[data.length - 3] = (byte) (dato & 0xFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private int complemento2Int(int i) {
        return ((~i) + 1);
    }

    private Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }

    private void cerrarLog(String status, boolean lexito) {
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

    private Date getDSpecificDate(boolean backForward, int val, String opc) {
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

    private String buscarSerialElster(String vectorhexT) {
        vectorhexT = vectorhexT.replace(", ", "");
        String[] arrayHex = vectorhexT.split("20");
        List<String> listaHex = Arrays.asList(arrayHex);
        List<String> nListaHex = removeEmpty(listaHex);
        return Hex2ASCII(nListaHex.get(1));
    }

    private List<String> removeEmpty(List<String> listaHex) {
        List<String> nListaHex = new ArrayList<>();
        int idx = 0;
        List<Integer> idxList = new ArrayList<>();
        for (String str : listaHex) {
            if (str.isEmpty()) {
            } else {
                idxList.add(idx);
            }
            idx++;
        }
        for (Integer idx2Add : idxList) {
            nListaHex.add(listaHex.get(idx2Add));
        }
        return nListaHex;
    }

    private boolean validaSerial(String[] vectorhex) {
        boolean continuar = false;
        try {
            String datoserial = buscarSerialElster(Arrays.toString(vectorhex));
            if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {//seriemedidor.equals(datoserial)) {
                escribir("Numero de serial " + datoserial);
                continuar = true;
            } else {
                escribir("Numero de serial incorrecto");
            }
        } catch (Exception e) {
            e.printStackTrace();
            escribir("Excepción en revisar serial: " + e.getStackTrace()[0] + "L");
        }
        return continuar;
    }

    private List<Integer> idxSplitArray(String[] arrayHex) {
        List<Integer> idxList = new ArrayList<>();
        int idx = 0;
        for (String str : arrayHex) {
            if (str.equals("EE")) {
                idxList.add(idx);
            }
            idx++;
        }
        return idxList;
    }

    private List<String[]> getFrames(String[] arrayHex, List<Integer> idxList) {
        List<String[]> nListHex = new ArrayList<>();
        idxList.add(arrayHex.length);
        for (Integer i = 0; i < idxList.size() - 1; i++) {
            String[] arrayStrTemp = new String[idxList.get(i + 1) - idxList.get(i)];
            System.arraycopy(arrayHex, idxList.get(i), arrayStrTemp, 0, idxList.get(i + 1) - idxList.get(i));
            nListHex.add(arrayStrTemp);
        }
        return nListHex;
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

    private void sendByeWithoutAnswer() {
        byte trama[] = tramasElster.getLogoff();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        calcularnuevocrc(trama);
        escribir("Envia  Logout sin espera de respuesta");
        escribir("=> " + tramasElster.encode(trama, trama.length));
        enviaTrama(trama);
    }

    private void handleForwarding(String peticion) {
        if (reintentoReenvio <= numeroReintentos) {
            reintentoReenvio ++;
            enviaTrama2(ultimatramaEnviada, "Solicitud de " + peticion, (byte) 0x00);            
        } else {
            reintentoReenvio = 0;
            escribir("Intentos de reenvío agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Intentos de reenvío agotados");                          
            cerrarPuerto(true);
            leer = false;
        }
    }
    
    private void handleNACK() {
        seEspera06 = false;
        if (intentosNACK < numeroReintentos) {
            intentosNACK++;
            if (uncomplete || delayed) {
                enviaTrama2(null, "Escuchamos Buffer", (byte) 0x00);//No envía nada
            } else {
                enviaTrama2(null, "NACK", (byte) 0x15);//Envia NACK
            }
        } else {
            intentosNACK = 0;
            escribir("Intentos de reenvío agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Intentos de reenvío agotados");
            cerrarPuerto(true);
            leer = false;
        }
    }
    
    private void handleJustNACK() {
        seEspera06 = false;
        if (intentosNACK < numeroReintentos) {
            intentosNACK++;
            enviaTrama2(null, "NACK", (byte) 0x15);//Envia NACK
        } else {
            intentosNACK = 0;
            escribir("Intentos de reenvío agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Intentos de reenvío agotados");
            cerrarPuerto(true);
            leer = false;
        }
    }
    
    private void handleACK() {
        seEspera06 = false;
        if (intentosNACK < numeroReintentos) {
            intentosNACK++;            
            enviaTrama2(null, "ACK", (byte) 0x06);//Envia NACK
        } else {
            intentosNACK = 0;
            escribir("Intentos de reenvío agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Intentos de reenvío agotados");
            cerrarPuerto(true);
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
