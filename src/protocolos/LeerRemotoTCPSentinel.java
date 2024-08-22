/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaSentinel;
import Entidades.Abortar;
import Entidades.ELogCall;
import Entidades.EMedidor;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Metrolink
 */
public class LeerRemotoTCPSentinel extends Thread {

    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    String seriemedidor = "";
    private boolean lookupAgain = false;
    InputStream input;
    OutputStream output;
    long tiempo = 500;
    String cadenahex = "";
    boolean aviso = false;
    String password = "";
    String[] vPass;
    private int nivelacceso = 0;
    private int expSeq = 0;
    String fechaactual = "";
    int indx = 0;
    EMedidor med;
    ControlProcesos cp;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lconfhora;
    public boolean leer = true;
    String numeroPuerto;
    int numeroReintentos = 4;
    int nreintentos = 0;
    int velocidadPuerto;
    long timeout;
    private int timeoutChannel = 255;
    private int ndias;
    private int nminutes;
    private int offset = 0;
    private int ElementCount = 0;
    private boolean portconect = false;
    private Thread tEscritura = null;
    private Thread tReinicio = null;
    public boolean cierrapuerto = false;
    Socket socket;
    private volatile boolean escucha = true;
    Thread tLectura;
    String cadenahexAnterior = "";
    private int reintentoconexion = 0;
    int intentoescuchar = 0;
    File file;
    RandomAccessFile fr;
    private int lastFrameLen = 0; 
    private short lastFrameChecksum = 0;
    byte[] ultimatramaEnviada = null;
    byte[] ultimoSimpleM = null;
    byte id = (byte) 0x01;
    String idS = "00";
    byte version = (byte) 0x00; //0 --> ANSI C12.18 ó ANSI C12.21, 1 --> ANSI C12.22
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdfconfhora = new SimpleDateFormat("yyyyMMddHHmm");
    int numCanales = 2;
    int intervalos = 15;
    boolean spoling = false; //false = 00 , true= 20
    boolean existearchivo = false;
    boolean seEspera06 = false;    
    private boolean primerbloque;
    byte bitSpoling = (byte) 0x00;
    private boolean siguienteTrama = false;
    public String tramaIncompleta = "";
    public boolean tramaOK = false;
    public boolean uncomplete = false;
    public boolean delayed = false;
    public boolean haveNoise = false;
    public boolean empty = false;
    public boolean complemento = false;
    boolean enviando = false;
    boolean reenviando = false;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfactual = new SimpleDateFormat("yyyy-MM-dd 24HH:mm");
    private boolean perfilincompleto = false;
    Abortar objabortar;
    private String usuario = "admin";
    TramasRemotaSentinel tramasentinel = new TramasRemotaSentinel();
    int reinicio = 0;
    //estados
    boolean lidentificacion = false;
    boolean lnegociacion = false;
    boolean lsession = false;
    boolean lautenticacion = false;
    boolean ltimeout = false;
    boolean lserialnumber = false;
    boolean lwritefechaactual = false;
    boolean lreadfechaactual = false;
    boolean lconfeventos = false;
    boolean lreadeventos = false;
    boolean lwriteconfperfilcarga = false;
    boolean lreadconfperfilcarga = false;
    boolean lreadperfilcarga = false;
    boolean lwriteconfhora = false;
    boolean lreadconfhora = false;
    boolean logoff = false;
    boolean lreset = false;
    byte[] badcrc = { (byte) 0x15 };
    byte[] ack = { (byte) 0x06 };
    //eventos
    private int contador;
    private int contadorEventos = 0;
    int tamañototalEventos = 0;
    int tamañoacumuladolEventos = 0;
    Vector<String[]> vEventos;
    public Vector<String> desgloseEventos;
    //perfil de carga
    String[] fragment1;
    String[] fragment2;
    private int intentosNACK = 0;
    private int reintentoReenvio = 0;
    private int lastvalidblockindex;
    private int NumBloque = 1;
    private int countCalculated;
    private int offsetCalculated;
    private int intervalsinthelastblock;
    private int numeroMaximoIntervalosPorBloque = 128;
    private int cabeceraBloque = 20;
    private int numBytesPorCanal = 2;
    private int indiceBloqueDesired = 0;
    private int numBytesPorBloque;
    private long desfase;
    public int tamañobloque;
    public int tamañototalbloque;
    public ArrayList<String> desglosePerfil;
    ArrayList<String> profileDataTemp;
    ArrayList<ArrayList<String>> profileData;
    //NTP
    Timestamp deltatimesync1;//tiempo de para calculo de delay NTP
    Timestamp deltatimesync2; //tiempo de para calculo de delay NTP
    Timestamp time = null; //tiempo de NTP
    long ndesfasepermitido = 0;//desfase tiempo NTP
    String dirfis = "0";
    String dirlog = "0";
    boolean solicitar; //variable de control de la sync
    Timestamp tsfechaactual; //timestamp fecha actual
        
    List<String[]> frames = new ArrayList<>();

    private ZoneId zid;

    private final Object monitor = new Object();
    private final String label = "LeerTCPSentinel";

    public LeerRemotoTCPSentinel(EMedidor med, boolean perfil, boolean eventos, boolean registros, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, ZoneId zid, long ndesfase) {
        this.med = med;
        this.cp = cp;
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
        lconfhora = false;
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
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword() == null ? "" : med.getPassword();
            vPass = password.trim().split("-");
            if (vPass.length == 2) {
                nivelacceso = Integer.parseInt(vPass[1]);
                password = vPass[0];
            } else {
                nivelacceso = 2;
            }
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
            tiempo = 1000;
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
                    try {
                        byte[] data = {(byte) 0x2B, (byte) 0x2B, (byte) 0x2B};
                        enviaTrama(data);
                    } catch (Exception e) {
                        e.printStackTrace();
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

    private void procesaCadena() {
        //tamaño buffer
        Object[] dataExtBuffer = new Object[2];
        byte[] readBuffer = new byte[2047];
        byte[] tempBuffer = new byte[2047];
        int numbytes = 0;
        int auxNumbytes = 0;
        boolean expTime = true;
        boolean extendTime = false;
        try {
            synchronized(monitor){
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
                if (!socket.isClosed()) {
                    if (expTime) {
                        if (ultimoSimpleM == null) {
                            reenviando = true;
                            enviando = false;
                            monitor.notifyAll();
                            return;
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
                } else {
                    return;
                }
            }
            do {
                if (extendTime) {
                    byte[] auxBuffer = Arrays.copyOfRange( ( (byte[]) dataExtBuffer[1] ), 0, ( (int) dataExtBuffer[0] ));                                        
                    auxBuffer = limpiaDuplicados(auxBuffer, auxBuffer.length);
                    cadenahex = tramasentinel.encode(auxBuffer, auxBuffer.length);
                } else {
                    readBuffer = limpiaDuplicados(Arrays.copyOfRange(readBuffer, 0, numbytes), numbytes);
                    cadenahex = tramasentinel.encode(readBuffer, readBuffer.length);
                }
                if (lookupAgain) {
                    dataExtBuffer = esperaCadena();
                    extendTime = true;
                }
            } while (lookupAgain);
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
    
    private byte[] limpiaDuplicados(byte[] frame, int numBytes) {
        if (frame[0] == 0x06 && numBytes == 1) {
            lookupAgain = true;
            return frame;
        }
        if (frame[0] == 0x15 || frame[0] == 0x00) {
            lookupAgain = false;
            return frame;
        }
        lookupAgain = false;
        escribir("Trama recibida: " + tramasentinel.encode(frame, numBytes));
        boolean finish = true;
        boolean hasACK = frame[0] == (byte) 0x06;
        int indxbitSpoling = hasACK ? 3 : 2;
        byte[] cleanFrame = Arrays.copyOfRange(frame, 0, numBytes);
        if (lastFrameLen != 0 && lastFrameChecksum != 0) {
            escribir("Longitud de la última trama recibida: " + lastFrameLen);
            escribir("Ultimo Checksum Recibido: " + lastFrameChecksum);
            if (lastFrameLen <= (hasACK ? numBytes - 1 : numBytes)) {
                //Validamos que no se encuentre contenida en la trama que nos acaba de llegar
                byte currBitSpoling = (byte) ((frame[indxbitSpoling] & 0x20) >> 5);
                boolean bitSpolingExp = currBitSpoling == (bitSpoling >> 5);
                short currChecksum = (short) ((frame[lastFrameLen - (hasACK ? 1 : 2)] << 8) + (frame[lastFrameLen - (hasACK ? 0 : 1)]));
                boolean sameChecksum = currChecksum == lastFrameChecksum;
                escribir("Bit Spoling Actual: " + currBitSpoling);
                escribir("Checksum Actual: " + currChecksum);
                if (!bitSpolingExp && sameChecksum) { //Trama Repetida
                    finish = false;
                    escribir("Trama Reptida");
                    try {
                        cleanFrame = Arrays.copyOfRange(frame, lastFrameLen, numBytes);
                    } catch (Exception e) {
                        finish = true;
                        lookupAgain = true;
                        escribir("No hay más datos \nBuscar nuevamente...");
                    }
                } else {
                    int indxlen1 = (hasACK ? 5 : 4);
                    int indxlen2 = (hasACK ? 6 : 5);
                    lastFrameLen = 8 + (((frame[indxlen1] << 8) + frame[indxlen2]) & 0xFF);
                    lastFrameChecksum = (short) (((frame[frame.length - 2] << 8) + (frame[frame.length - 1])) & 0xFFFF);
                    escribir("Trama no repetida");
                }
            } else {
                int indxlen1 = (hasACK ? 5 : 4);
                int indxlen2 = (hasACK ? 6 : 5);
                lastFrameLen = 8 + (((frame[indxlen1] << 8) + frame[indxlen2]) & 0xFF);
                lastFrameChecksum = (short) (((frame[frame.length - 2] << 8) + (frame[frame.length - 1])) & 0xFFFF);
                escribir("Respues Anterior Recibida de mayor tamaño que la respuesta Actual");
            }
        } else {
            int indxlen1 = (hasACK ? 5 : 4);
            int indxlen2 = (hasACK ? 6 : 5);
            lastFrameLen = 8 + (((frame[indxlen1] << 8) + frame[indxlen2]) & 0xFF);
            lastFrameChecksum = (short) (((frame[frame.length - 2] << 8) + (frame[frame.length - 1])) & 0xFFFF);
            escribir("Longitud de la primera trama recibida: " + lastFrameLen);
            escribir("Checksum de la primera Trama recibido: " + lastFrameChecksum);
        }
        escribir("Longitud de la última trama recibida: " + lastFrameLen);
        escribir("Ultimo Checksum Recibido: " + lastFrameChecksum);
        escribir("Trama Limpia: " + tramasentinel.encode(cleanFrame, cleanFrame.length));
        return finish ? cleanFrame : limpiaDuplicados(cleanFrame, cleanFrame.length);
    }

    private void interpretaCadena(String cadenahex) throws InterruptedException, ParseException {
        try {
            frames = new ArrayList<>();
            escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (lidentificacion) {//identificacion de COM 
                revisarIdentificacion(vectorhex);
            } else if (lnegociacion) {
                revisarNegociacionVelocidad(vectorhex);
            } else if (lsession) {
                revisionSession(vectorhex);
            } else if (lautenticacion) {
                revisionAutenticacion(vectorhex);
            } else if (ltimeout) {
                revisionTimeout(vectorhex);
            } else if (lserialnumber) {
                revisarSerialNumber(vectorhex);
            } else if (lwritefechaactual) {
                revisarWriteFecha(vectorhex);
            } else if (lreadfechaactual) {
                revisarReadFecha(vectorhex);
            } else if (lconfeventos) {
                revisarConEventos(vectorhex);
            } else if (lreadeventos) {
                revisarReadEventos(vectorhex);
            } else if (lwriteconfperfilcarga) {
                revisarWriteConfPerfilCarga(vectorhex);
            } else if (lreadconfperfilcarga) {
                revisarReadConfPerfilCarga(vectorhex);
            } else if (lreadperfilcarga) {
                revisarReadPerfilCarga(vectorhex);
            } else if (lwriteconfhora) {
                writeSincronizacionReloj(vectorhex);
            } else if (lreadconfhora) {
                readSincronizacionReloj(vectorhex);
            } else if (logoff) {
                revisarLogOff(vectorhex);
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

    private void iniciacomunicacion() throws InterruptedException, Exception {        
        lreadconfperfilcarga = false;
        lreadeventos = false;
        perfilincompleto = false;
        
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
                enviaIdentificacion();
            } else {
                interrumpirHilo(tLectura);
                escribir("Medidor no configurado");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
                cerrarPuerto(false);
                cerrarLog("Medidor no configurado", false);
                leer = false;
            }
        }
    }
    
    public void reiniciaComunicacion() {
        try {
            if (lreadperfilcarga & perfilincompleto) {
                //guardamos lecturas hasta donde esten                
                almacenaDatos();
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
    
    private void enviaIdentificacion() throws InterruptedException {
        spoling = false;
        bitSpoling = 0x00;
        escribir("Envia Identificacion");       
        byte[] tramaidentidad = tramasentinel.getIdentidad();
        tramaidentidad[1] = id;
        tramaidentidad[2] = version;
        byte tramanueva[] = calcularnuevocrc(tramaidentidad);
        lidentificacion = true;
        reintentoReenvio = 0;
        intentosNACK = 0;        
        enviaTrama2(tramanueva, "Identidad", (byte) 0x00);
    }
    
    private void enviaNegociacionVelocidad() throws InterruptedException {
        escribir("Envia Negociacion de Velocidad");   
        byte trama[] = tramasentinel.getNegotiation();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lidentificacion = false;
        lnegociacion = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de Negociacion", (byte) 0x06);
    }
    
    
    public void enviaSesion() throws InterruptedException {
        escribir("Envia Sesion");
        byte trama[] = tramasentinel.getSession();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        trama[8] =  (byte) (nivelacceso & 0xFF);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lnegociacion = false;
        lsession = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de Session", (byte) 0x06);
    }
    
    public void enviaAutenticacion() throws InterruptedException {
        escribir("Envia Autenticacion");
        byte trama[] = tramasentinel.getAutenticate();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        for (int i = 0; i < password.trim().length(); i++) {
            trama[7 + i] = (byte) (Integer.parseInt(convertStringToHex(password.substring(i, i + 1)), 16) & 0xFF);
        }
        byte nuevatrama[] = calcularnuevocrc(trama);
        lsession = false;
        lautenticacion = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de Autenticacion", (byte) (0x06));
    }
    
    public void enviaWaitService() throws InterruptedException {
        escribir("Envia Wait Service");
        byte trama[] = tramasentinel.getWaitService();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        trama[7] = (byte) (timeoutChannel & 0xFF);
        byte nuevatrama[] = calcularnuevocrc(trama);        
        lautenticacion = false;                
        ltimeout = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de TimeOut", (byte) 0x06);
    }
    
    public void enviaSerial() throws InterruptedException {
        escribir("Envia Serial");
        byte trama[] = tramasentinel.getSerialnumber();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        ltimeout = false;        
        lserialnumber = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de Serial", (byte) 0x06);
    }

    
    public void enviaWFechaActual() throws InterruptedException {
        escribir("Write Fecha Actual");
        byte trama[] = tramasentinel.getWritefecha();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lserialnumber = false;
        lwritefechaactual = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de fecha actual (Escritura)", (byte) 0x06);
    }
    
    public void enviaRFechaActual() throws InterruptedException {
        escribir("Read Fecha Actual");
        byte trama[] = tramasentinel.getReadfecha();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lwritefechaactual = false;
        lreadfechaactual = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de fecha actual", (byte) 0x06);
    }
    
    public void enviaWConfHora() throws InterruptedException {
        escribir("Actualizando Fecha");
        time = obtenerHora();
        escribir("Fecha a Actulizar: " + time);
        ZoneOffset zone = obtenerOffset();
        long offsetSeg = zone.getTotalSeconds();
        long minutos = (time.getTime() + offsetSeg * 1000L) / 60000L;
        int seg = (int) ((time.getTime() % 60000) / 1000L);
        String date = Long.toHexString(minutos);
        while (date.length() < 8) {
            date = "0" + date;
        }
        escribir("Valor en hex " + date);
        byte trama[] = tramasentinel.getWriteConfHora();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        trama[15] = (byte) (Integer.parseInt(date.substring(6, 8), 16) & 0xFF);
        trama[16] = (byte) (Integer.parseInt(date.substring(4, 6), 16) & 0xFF);
        trama[17] = (byte) (Integer.parseInt(date.substring(2, 4), 16) & 0xFF);
        trama[18] = (byte) (Integer.parseInt(date.substring(0, 2), 16) & 0xFF);
        trama[19] = (byte) (seg & 0xFF);
        trama = validaCheckSum(trama, 11);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lreadfechaactual = false;
        lwriteconfhora = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud configuracion de hora ", (byte) 0x06);
    }
    
    public void enviaRConfHora() throws InterruptedException {
        escribir("Confirmacion de Configuracion de Hora");
        byte trama[] = tramasentinel.getReadConfHora();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lwriteconfhora = false;
        lreadconfhora = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de confirmacion de configuracion de hora", (byte) 0x06);

    }
    
    public void enviaWConfPerfil() throws InterruptedException {
        escribir("Configuración Perfil W");      
        byte trama[] = tramasentinel.getWriteConfPerfil();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lreadfechaactual = false;
        lreadeventos = false;
        lwriteconfperfilcarga = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud configuracion de perfil de carga (Escritura)", (byte) 0x06);
    }  
    
    public void enviaRConfPerfil() {        
        escribir("Configuracion Perfil R");
        byte trama[] = tramasentinel.getReadConfPerfil();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lwriteconfperfilcarga = false;
        lreadconfperfilcarga = true;       
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud de configuracion de perfil de carga (lectura)", (byte) 0x06);
    }

    public void enviaConfEventos() throws InterruptedException {
        escribir("Configuracion Eventos");
        byte trama[] = tramasentinel.getConfEvents();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lreadfechaactual = false;
        lconfeventos = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "Solicitud configuracion de eventos", (byte) 0x06);
    }
     
    
    public void enviaEventos() throws InterruptedException {
        escribir("Eventos");       
        byte trama[] = tramasentinel.getEvents();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lconfeventos = false;
        lreadeventos = true;
        vEventos = new Vector<>();  
        reintentoReenvio = 0;
        intentosNACK = 0;
        primerbloque = true;
        enviaTrama2(nuevatrama, "Solicitud de Eventos", (byte) 0x06);
    }
    
     public void enviaPerfilCarga(String offsetHex, String ecount) throws InterruptedException {
        escribir("Solicitud Perfil de Carga");
        byte trama[] = offsetHex.length() > 6 ? tramasentinel.getReadPerfilExt() : tramasentinel.getReadPerfil();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        if (offsetHex.length() > 6) {
            trama[5] = 0x09;
            trama[9] = (byte) (Integer.parseInt(offsetHex.substring(0, 2), 16) & 0xFF);
            trama[10] = (byte) (Integer.parseInt(offsetHex.substring(2, 4), 16) & 0xFF);
            trama[11] = (byte) (Integer.parseInt(offsetHex.substring(4, 6), 16) & 0xFF);
            trama[12] = (byte) (Integer.parseInt(offsetHex.substring(6, 8), 16) & 0xFF);
            //element count
            trama[13] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
            trama[14] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
        } else {
            //off set
            trama[9] = (byte) (Integer.parseInt(offsetHex.substring(0, 2), 16) & 0xFF);
            trama[10] = (byte) (Integer.parseInt(offsetHex.substring(2, 4), 16) & 0xFF);
            trama[11] = (byte) (Integer.parseInt(offsetHex.substring(4, 6), 16) & 0xFF);
            //element count
            trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
            trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
        }                
        byte nuevatrama[] = calcularnuevocrc(trama);
        lreadconfperfilcarga = false;
        lreadperfilcarga = true;
        tamañototalbloque = 0;
        tamañobloque = 0;             
        primerbloque = true;
        enviaTrama2(nuevatrama, "Solicitud de perfil de carga", (byte) 0x06);
    }

    public void enviaLogoff() throws InterruptedException {
        escribir("Envia LogOff");
        byte trama[] = tramasentinel.getLogoff();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        byte nuevatrama[] = calcularnuevocrc(trama);
        lreadperfilcarga = false;
        lreadeventos = false;
        lreadconfhora = false;
        logoff = true;
        reintentoReenvio = 0;
        intentosNACK = 0;
        enviaTrama2(nuevatrama, "", (byte) 0x06);
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

    public void terminaHilos() {
        try {
            tEscritura.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            tReinicio.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }     
        tEscritura = null;
        tReinicio = null;    
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
                                        escribir("=> " + tramasentinel.encode(confirmResp, confirmResp.length));
                                        monitor.wait();
                                    } else {
                                        enviaTrama(confirmResp);
                                        escribir("=> " + tramasentinel.encode(confirmResp, confirmResp.length));
                                        monitor.wait(100);
                                    }
                                } else if (intentosRetransmision != 0 && simpleM) {
                                    ultimoSimpleM = confirmResp;
                                    monitor.notifyAll();
                                    enviaTrama(confirmResp);
                                    escribir("=> " + tramasentinel.encode(confirmResp, confirmResp.length));
                                    monitor.wait();
                                }
                                if (!simpleM) {
                                    ultimoSimpleM = null;
                                    ultimatramaEnviada = trama;
                                    escribir(des);
                                    escribir("=> " + tramasentinel.encode(trama, trama.length));
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
                            if (lreadperfilcarga & perfilincompleto) {
                                //guardamos lecturas hasta donde esten                                
                                almacenaDatos();                                
                                med.MedLeido = true;
                                cerrarLog(desglosePerfil.isEmpty() ? "No Leido" : "Leido Incompleto", true);
                                leer = false;
                                return;
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

    private byte getSpoling(byte spoling) {
        if (spoling == 0x00) {
            bitSpoling = 0x20;
            return 0x20;
        } else {
            bitSpoling = 0x00;
            return 0x00;
        }
    }

    public String Hex2ASCII(String dato) {
        String resultado = "";
        String temp1;
        int temp2;
        for (int k = 0; k < dato.length(); k = k + 2) {
            temp1 = "" + dato.charAt(k) + dato.charAt(k + 1);
            temp2 = Integer.parseInt(temp1, 16);
            resultado = resultado + (char) temp2;
        }
        return resultado.trim();
    }
      
    private void revisarIdentificacion(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;        
        int idxSeq;
        String peticion = "Identificacion";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            idxSeq = (seEspera06 ? 4 : 3);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00")){
                    escribir("Numero de secuencia " + vectorhex[idxSeq]);
                    enviaNegociacionVelocidad();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            }  else {
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

    private void revisarNegociacionVelocidad(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Negociacion";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }  
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                if ( vectorhex[indxACK].equals("00")){
                    escribir("Tamaño Maximo de Paquete soportado: "+Integer.parseInt( vectorhex[indxACK+1] + vectorhex[indxACK+2] , 16));                
                    escribir("Maximo Numero de Paquetes Soportados: "+Integer.parseInt(vectorhex[indxACK+3], 16));
                    escribir("Tasa de Transmision en Baudios/s: "+ 300* ( 2^( Integer.parseInt(vectorhex[indxACK+4],16) - 1 )) );
                    enviaSesion();
                    break;
                }  else {
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
                        this.handleForwarding(peticion);
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

    private void revisionSession(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }        
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Sesion";
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
                    enviaAutenticacion();
                    break;
                }  else {
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                }
            }  else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        this.handleForwarding(peticion);
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

    private void revisionAutenticacion(String[] vectorhexO) throws InterruptedException {
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
                if (vectorhex[indxACK].equals("00")) {
                    enviaWaitService();
                    break;
                } else {
                    if (frameCounter == framesQty) {
                        //valida ack del data
                        escribir("Desconexion - Error de autenticacion");
                        escribir("Revisar nivel de acceso y contraseña");
                        reiniciaComunicacion();
                        break;
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
        }//ack del data        
    }

    private void revisionTimeout(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Timeout";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00") || vectorhex[indxACK].equals("0A")) {
                    if (lreadconfperfilcarga) {
                        ltimeout = false;
                        String offsetHex = Integer.toHexString(offset).toUpperCase();
                        while (offsetHex.length() < 6) {
                            offsetHex = "0" + offsetHex;
                        }
                        escribir("Offset (Hex): " + offsetHex);
                        String ecount = Integer.toHexString(ElementCount).toUpperCase();
                        while (ecount.length() < 4) {
                            ecount = "0" + ecount;
                        }
                        escribir("Element count (Hex): " + ecount);
                        desglosePerfil = new ArrayList<>();
                        enviaPerfilCarga(offsetHex, ecount);
                    } else {
                        enviaSerial();
                    }                    
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
                        handleNACK();
                    }
                } else {
                    frameCounter++;
                }
            }
        }        
    }

    private void revisarSerialNumber(String[] vectorhexO) throws InterruptedException {
         if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxSerial;
        String peticion = "Serial";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxSerial = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00")) {
                    boolean continuar = false;
                    try {
                        String datoserial = "";
                        for (int i = indxSerial; i <= indxSerial+10; i++) {
                            datoserial = datoserial + vectorhex[i];
                        }
                        datoserial = Hex2ASCII(datoserial);
                        if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {//seriemedidor.equals(datoserial)) {
                            escribir("Numero de serial " + datoserial);
                            continuar = true;
                        } else {
                            escribir("Numero de serial incorrecto");
                            escribir("Numero de serial encontrado: " + datoserial);
                        }
                    } catch (Exception e) {
                        escribir(getErrorString(e.getStackTrace(), 3));
                        escribir("Excepción en revisar serial: " + e.getStackTrace()[0] + "L");
                    }
                    if (continuar) {
                        enviaWFechaActual();
                        break;
                    }  else {
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
                } else {
                    frameCounter++;
                }
            }
        }       
    }

    private void revisarWriteFecha(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Write Fecha";
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
                    escribir("Desfase Permitido: " + ndesfasepermitido);   
                    deltatimesync1 = obtenerHora();
                    time = obtenerHora();
                    escribir("Fecha Actual Equipo: "+deltatimesync1);
                    enviaRFechaActual();
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                } 
            }  else {
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

    private void revisarReadFecha(String[] vectorhexO) throws ParseException, InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxfecha;
        String peticion = "ReadFecha";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxfecha = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00")) {
                    int tam = Integer.parseInt(vectorhex[indxACK + 1] + vectorhex[indxACK + 2], 16);
                    String strdate = "";
                    for (int i = indxfecha; i < (indxfecha + tam); i++) {
                        strdate = vectorhex[i] + strdate;
                    }
                    escribir("Fecha: "+ strdate);
                    //Se suma con los minutos transcurridos hasta el 01 de Enero a las 00:00 del año 2000.
                    long intdate = (Long.parseLong(strdate, 16) * 1000) + sdf.parse("000101000000").getTime();
                    fechaactual = sdf.format(new Date(new Timestamp(intdate).getTime()));
                    try {
                        solicitar = true;
                        deltatimesync2 = obtenerHora();
                        tsfechaactual = new Timestamp(sdf.parse(fechaactual).getTime());
                        escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                        escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - (tsfechaactual.getTime() + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)))) / 1000));
                        desfase = (time.getTime() - (tsfechaactual.getTime() + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)))) / 1000;
                        if ( Math.abs(desfase) > ndesfasepermitido) {
                            solicitar = false;
                            escribir("No se solicitara el perfil de carga");
                        }                                             
                    } catch (Exception e) {
                        escribir("Error al calcular desfase, por tanto no se continuará.");
                        escribir(getErrorString(e.getStackTrace(), 3));
                        solicitar = false;
                    }
                    escribir("Fecha actual Medidor " + sdf.parse(fechaactual).toString());
                    escribir("Fecha actual PC " + this.obtenerHora());
                    if (solicitar) {
                        try {
                            cp.actualizaDesfase(desfase, med.getnSerie());
                        } catch (Exception e) {
                            escribir("Error al actualizar desfase en DB.");
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }
                        if (med.getFecha() != null) {
                            escribir("Fecha Ultima Lectura " + med.getFecha());
                            long diffInMillies = Math.abs( ( this.obtenerHora().getTime() - (desfase*1000) ) - med.getFecha().getTime());
                            long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
                            nminutes = (int) diff;
                            escribir("Numero de minutos de diferencia calculados: " + nminutes);
                        }
                        if (lconfhora) {
                            enviaWConfHora();
                            break;
                        } else if (leventos) { //Continua con Perfil
                            enviaConfEventos();
                            break;
                        } else if (lperfil) {
                            enviaWConfPerfil();
                            break;
                        } else {
                            escribir("No se ha seleccionado ninguna opción");
                            enviaLogoff();
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

    private void revisarConEventos(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Eventos";
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
                    enviaEventos();
                    break;
                }  else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                } 
            }  else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        this.handleForwarding(peticion);
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

    private void revisarReadEventos(String[] vectorhexO) throws InterruptedException {
         
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        int indxLenConst;
        String peticion = "Eventos";
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
                if (primerbloque) {
                    expSeq = Integer.parseInt(vectorhex[indxBlock], 16) - 1;
                }                
                if (vectorhex[indxACK].equals("00") || !seEspera06) { //ack del data - cuando es EE no se evalúa
                    escribir("Numero de secuencia " + vectorhex[indxBlock]);
                    if (vectorhex[indxBlock].equals("00")) {
                        String[] tramatemp = new String[(Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst];
                        System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst);
                        vEventos.add(tramatemp);
                        if (lperfil) {
                            if ((vectorhex.length - indxLenConst) != 1) { //viene data
                                this.enviaWConfPerfil();//Conf Perfil
                                break;
                            } else {
                                if (frameCounter == framesQty) {
                                    escribir("Respuesta Eventos sin data");
                                    reiniciaComunicacion();
                                    break;
                                } else {
                                    frameCounter++;
                                }
                            }
                        } else {
                            escribir("Sin solicitudes adicionales");
                            enviaLogoff();
                            break;
                        }
                    } else {
                        if (primerbloque ? true : expSeq == Integer.parseInt(vectorhex[indxBlock], 16)) {
                            String[] tramatemp = new String[(Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst];
                            System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[indxLen] + vectorhex[indxLen + 1]), 16)) + indxLenConst);
                            vEventos.add(tramatemp);
                            expSeq = primerbloque ? expSeq : expSeq - 1;
                            primerbloque = false;
                            reintentoReenvio = 0;
                            intentosNACK = 0;
                            siguienteTrama = true;
                            getSpoling(bitSpoling);
                            enviaTrama2(null, "ACK_Next", (byte) 0x06);
                            break;
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleJustNACK();
                                break;
                            } else {
                                frameCounter++;
                            }
                        }
                    }
                }  else {//nack
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

    private void revisarWriteConfPerfilCarga(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Eventos";
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
                    enviaRConfPerfil();
                    break;
                }  else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                } 
            }  else {
                if (frameCounter == framesQty) {
                    if (vectorhex[0].equals("15")) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        escribir("Trama incorrecta");
                        this.handleNACK();
                    }
                    break;
                } else {
                    frameCounter++;
                }                            
            }
        }
    }

    private void revisarReadConfPerfilCarga(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }        
        revisaTrama(vectorhexO);
        int indxACK;
        int indxData;
        String peticion = "Configuracion Perfil Carga";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            indxData = (seEspera06 ? 10 : 9);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00")) {
                    numCanales = Integer.parseInt(vectorhex[indxData], 16);
                    escribir("Num canales " + numCanales);
                    intervalos = Integer.parseInt(vectorhex[indxData+1], 16);
                    escribir("intervalos " + intervalos);
                    lastvalidblockindex = Integer.parseInt(vectorhex[indxData+3], 16);
                    escribir("Last Valid Block Index " + lastvalidblockindex);
                    intervalsinthelastblock = Integer.parseInt(vectorhex[indxData+4], 16);
                    escribir("Intervals in last block " + intervalsinthelastblock);
                    escribir("Numero maximo de intervalos por bloque " + numeroMaximoIntervalosPorBloque);
                    int NumTotalInter = nminutes / intervalos;
                    int NumInter = intervalsinthelastblock;
                    while (NumInter <= NumTotalInter) {
                        NumInter = NumInter + numeroMaximoIntervalosPorBloque;
                        NumBloque++;
                    }
                    numBytesPorBloque = cabeceraBloque + (numeroMaximoIntervalosPorBloque * numBytesPorCanal * (numCanales + 1));
                    escribir("Numero maximo de Bytes por Bloque: " + numBytesPorBloque);
                    indiceBloqueDesired = lastvalidblockindex - NumBloque + 1;
                    indiceBloqueDesired = indiceBloqueDesired < 0 ? 0 : indiceBloqueDesired;                    
                    escribir("Index Block Desired: " + indiceBloqueDesired);
                    offset = indiceBloqueDesired * numBytesPorBloque;
                    escribir("Offset: " + offset);
                    offsetCalculated = offset;
                    countCalculated = (lastvalidblockindex - indiceBloqueDesired) * numBytesPorBloque + (cabeceraBloque + (intervalsinthelastblock * 2 * (numCanales + 1)));
                    escribir("Count: " + countCalculated);
                    String offsetHex = Integer.toHexString(offset).toUpperCase();
                    while (offsetHex.length() < 6) {
                        offsetHex = "0" + offsetHex;
                    }
                    escribir("Offset (Hex): " + offsetHex);
                    ElementCount = numBytesPorBloque;
                    //ElementCount = 788;                    
                    enviaWaitService();
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

    private void revisarReadPerfilCarga(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        int indxBlock;
        int indxLen;
        int indxLenConst;
        String peticion = "Perfil Carga";
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
                perfilincompleto = true;
                if (primerbloque) {
                    boolean belongs = ElementCount == Integer.parseInt(vectorhex[indxLenConst - 1] + vectorhex[indxLenConst], 16);
                    if (!belongs) {
                        if (frameCounter == framesQty) {
                            escribir("No pertenece a las tramas del perfil de carga con E Count: " + ElementCount);
                            handleJustNACK();
                            break;
                        } else {
                            frameCounter++;
                            continue;
                        }
                    }
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
                                } else {
                                    frameCounter++;
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
                                String[] trama = new String[primerbloque ? seqLen - 3 : seqLen];
                                System.arraycopy(vectorhex, primerbloque ? indxLen + 5 : indxLen + 2, trama, 0, primerbloque ? seqLen - 3 : seqLen);
                                profileDataTemp.addAll(Arrays.asList(trama));
                                expSeq = primerbloque ? expSeq : expSeq - 1;
                                escribir("Secuencia esperada: " + expSeq);
                                primerbloque = false;
                                reintentoReenvio = 0;
                                intentosNACK = 0;
                                siguienteTrama = true;
                                getSpoling(bitSpoling);
                                enviaTrama2(null, "ACK_Next", (byte) 0x06);
                                break;
                            }
                        } else {
                            if (frameCounter == framesQty) {
                                escribir("Secuencia incorrecta");
                                handleJustNACK();
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
                            offset = offset + ElementCount;
                            if (offset > (offsetCalculated + countCalculated)) {//es la ultima trama del perfil de carga
                                enviaLogoff();                               
                                perfilincompleto = false;
                                break;
                            } else {
                                String offsetHex = Integer.toHexString(offset).toUpperCase();
                                while (offsetHex.length() < 6) {
                                    offsetHex = "0" + offsetHex;
                                }
                                escribir("Offset: " + offsetHex);
                                String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                while (ecount.length() < 4) {
                                    ecount = "0" + ecount;
                                }     
                                escribir("Element Count: "+ ecount);
                                enviaPerfilCarga(offsetHex, ecount);
                                break;
                            }
                        } else {
                            //bloque no esta completo
                            if (frameCounter == framesQty) {
                                escribir("tamaño bloque incompleto tamaño total " + tamañototalbloque + " tamaño bloque " + tamañobloque);
                                desglosePerfil = new ArrayList<>();
                                profileDataTemp = new ArrayList<>();
                                tamañobloque = 0;
                                tamañototalbloque = 0;
                                primerbloque = true;
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
                } else {
                    frameCounter++;
                }
            }       
        }
    }

    private void writeSincronizacionReloj(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Write Sinc. Reloj";
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
                    enviaRConfHora();
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                } 
            }  else {
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

    private void revisarLogOff(String[] vectorhex) {
        if (uncomplete) {
            fragment2 = vectorhex;
            vectorhex = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhex); // En el logoff valdría la pena revisar la trama? 
        String peticion = "Logout";
        tramaOK = false;
        logoff = false;
        cerrarPuerto(false);
        escribir("Logout");
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
        almacenaDatos();
        cerrarLog("Leido", true);
        leer = false;

    }

    private void readSincronizacionReloj(String[] vectorhexO) throws InterruptedException {
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxACK;
        String peticion = "Eventos";
        int framesQty = frames.size();
        int frameCounter = 1;
        for (String[] vectorhex : frames) {
            escribir("Trama a probar: " + Arrays.toString(vectorhex));
            if (framesQty > 1) {//Quiere decir que no se les alcanzo a validar a cada trama su CRC y el Bit Spoiling
                revisaTramaIndividual(vectorhex);
            }
            indxACK = (seEspera06 ? 7 : 6);
            if (tramaOK && !uncomplete && !delayed) {
                if (vectorhex[indxACK].equals("00") && vectorhex[indxACK+6].equals("00")) { 
                    escribir("Procedimiento realizado");
                    enviaLogoff();
                    break;
                } else {//nack
                    if (frameCounter == framesQty) {
                        escribir("Recibe NACK");
                        handleForwarding(peticion);
                    } else {
                        frameCounter++;
                    }
                } 
            }  else {
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

    private void almacenaDatos() {
        if (leventos) {
            desglosaEventos();
        }
        if (lperfil) {
            if ( !desglosePerfil.isEmpty() ) {
                cp.AlmacenaPerfilSentinel(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), desglosePerfil, intervalos, intervalsinthelastblock, NumBloque, file);
            } else {
                escribir("No se alcanzó a completar ningún bloque");
            }
        }
    }    
    private void desglosaEventos() {
        desgloseEventos = new Vector<>();
        String tramadesglose = "";
        String tramatotaldata = "";
        //System.out.println("Numero de tramas " + vEventos.size());
        for (int p = 0; p < vEventos.size(); p++) {
            tramadesglose = "";
            for (int l = 0; l < vEventos.get(p).length; l++) {
                tramadesglose += vEventos.get(p)[l];
            }
            //System.out.println("Tramas desglose " + p + " => " + tramadesglose);
            if (p == 0) {//es la primera trama
                if (tramadesglose.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                    tramadesglose = tramadesglose.substring(42, tramadesglose.length() - 4);
                } else {//es retransmision de la primera trama
                    tramadesglose = tramadesglose.substring(40, tramadesglose.length() - 4);
                }
            } else {//es continuacion de bloques
                if (tramadesglose.substring(6, 8).equals("00")) {//es el ultimo bloque
                    tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 6);
                } else {//es bloques intermedios
                    tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 4);
                }

            }
            tramatotaldata = tramatotaldata + "" + tramadesglose;
        }
        cp.almancenaEventosSentinel(tramatotaldata, seriemedidor);
    }

    public String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
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

    public int complemento2Int(int i) {
        return ((~i) + 1);
    }

    public String completarCeros(String valor, int numceros) {
        String data = "" + valor;
        while (data.length() < numceros) {
            data = "0" + data;
        }
        return data;
    }
    
    private void revisaTramaIndividual(String[] vectorhex){
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
                        if (logoff || ((((byte) Integer.parseInt(vectorhex[indxbitSpoling], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            tramaOK = true;                       
                        } else {//bit Spoling
                            if (siguienteTrama) {
                                escribir("BAD Bit Spoling - Siguiente trama");                             
                            } else {
                                escribir("BAD Bit Spoling");                               
                            }
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
                            if (siguienteTrama) {
                                escribir("BAD Bit Spoling - Siguiente trama");
                                frames.add(vectorhex);                                
                            } else {
                                escribir("BAD Bit Spoling");
                                frames.add(vectorhex);                                
                            }
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
    
    private Timestamp obtenerHora() {            
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }
    
    private ZoneOffset obtenerOffset(){
        LocalDateTime now = LocalDateTime.now();
        return zid.getRules().getOffset(now);
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
        //System.out.println("Idx List: " + idxList);
        for (Integer i = 0; i < idxList.size() - 1; i++) {
            //System.out.println("I: " + i);
            String[] arrayStrTemp = new String[idxList.get(i + 1) - idxList.get(i)];
            //System.out.println("" + idxList.get(i) + "-" + idxList.get(i + 1));
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
    
    public void cerrarLog(String status,boolean lexito) {
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
        log.setNreintentos(reintentosUtilizados);
        log.setVccoduser(usuario);
        log.setLexito(lexito);
        cp.saveLogCall(log, null);
    }
    
    private void sendByeWithoutAnswer() {
        byte trama[] = tramasentinel.getLogoff();
        trama[1] = id;
        trama[2] = getSpoling(bitSpoling);
        trama[2] = (byte) ((trama[2] + version) & 0x23);
        calcularnuevocrc(trama);
        escribir("Envia  Logout sin espera de respuesta");
        escribir("=> " + tramasentinel.encode(trama, trama.length));
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
                enviaTrama2(null, "Escucha Buffer", (byte) 0x00);//No envía nada
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
