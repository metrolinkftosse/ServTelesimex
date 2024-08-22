package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaTecun;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.EInstantaneos;
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
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static protocolos.LeerRemotoTCPElgama300.calcularnuevocrcI;
import static protocolos.LeerRemotoTCPElgama300.validacionCRCHCS;

/**
 *
 * @author Metrolink
 */
public class LeerRemotoTCPTecunly extends Thread {

  int reintentosUtilizados;// reintentos utlizados
  int bytesRecortados = 0;
  Timestamp tiempoinicial; //inicio de comunicacion
  Timestamp tiempofinal;//fin de comunicacion
  Date d = new Date();
  String seriemedidor = "";
  String estadorele = ""; //vic 09-10-19
  String datoserial = "";
  InputStream input;
  OutputStream output;
  long tiempo = 500;
  String cadenahex = "";
  TramasRemotaTecun tramastcum = new TramasRemotaTecun();
  boolean aviso = false;
  String password = "";
  String password2 = "";
  String newPass = "87654321"; //vic 10-10-19 password que se recibe de la caja de texto de la interfaz
  int indx = 0;
  EMedidor med;
  ControlProcesos cp;
  boolean lperfil;
  boolean leventos;
  boolean lregistros;
  boolean lconfhora;
  boolean ldisc = false; // estados de los check box para corte y reconexión  vic 08-10-19
  boolean lconn = false;
  boolean loutS = false;
  boolean lchPass = false; // vic 10-10-19 Check box para cambiar el password
  boolean lPhyAdrs = false; // Dirección Física
  public boolean leer = true;
  String numeroPuerto;
  int numeroReintentos = 4;
  int nreintentos = 0;
  int ReintentoFRMR = 0; // vic 05-07-19
  int ReinicioFRMR = 0; // vic 05-07-19
  int velocidadPuerto;
  long timeout;
  int ndias;
  int diasaleer = 0;
  Date fechaActual;
  //*** Conf Hora***///
  Timestamp time = null; //tiempo de ZID
  Timestamp tsfechaactual;
  Timestamp deltatimesync1;
  Timestamp deltatimesync2;
  long ndesfasepermitido = 7200;
  private long desfase;
  boolean solicitar; //variable de control de la sync
  boolean portconect = false;
  int reintentoadp = 0;
  Thread port = null;
  Thread tEscritura = null;
  Thread tReinicio = null;
  Thread port3 = null;
  boolean inicia1 = false;
  public boolean cierrapuerto = false;
  Socket socket;
  private volatile boolean escucha = true;
  Thread tLectura;
  private int reintentoconexion = 0;
  //****** Estados******//
  boolean lSNRMUA = false;
  boolean lARRQ = false;
  boolean lserialnumber = false;
  boolean lfechaactual = false;
  boolean lfechaactual2 = false;
  boolean lperiodoIntegracion = false;
  boolean lentradasenuso = false; //vic 30-09-19 homogeneidad con telesimex móvil
  boolean linfoperfil = false;
  boolean lconstants = false;
  boolean lpowerLost = false;
  boolean lperfilcarga = false;
  boolean ldisconnect = false; //corte reconexión vic 08-10-19
  boolean lconnect = false;
  boolean loutState = false;
  boolean lchangePass = false; // vic 10-10-19
  boolean lterminar = false;
  boolean lReset = false;
  boolean lfechasync = false;
  boolean lphyaddress = false; //dirección física
  boolean enviando = false;
  boolean reenviando = false;
  byte[] ultimatramaEnviada = null;
  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
  SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
  SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM/dd HH");
  SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
  int numcanales = 2;
  int intervalo = 0;
  int factorIntervalo = 0;
  int ndia = 3;
  int dirfis = 0;
  int dirlog = 0;
  boolean setdirfis = false;//dirfis
  File file;
  RandomAccessFile fr;
  boolean existearchivo = false;
  public boolean tramaOK = false; //Dirfis
  public String tramaIncompleta = "";
  public boolean complemento = false;
  public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
  int reinicio = 0;
  int ns = 0;
  int nr = 0;
  int nrEsperado = 0;
  int nsEsperado = 0;
  boolean ultimbloquePerfil = false;
  boolean constanteOk = false; //vic 30-09-19 homogeneidad con telesimex móvil
  boolean canalesOk = false; //vic 02-09-19
  Vector<String> vPerfilCarga;
  Vector<String> vEventos;
  Vector<String> infoPerfil;
  ArrayList<String> obis;
  ArrayList<String> conskePerfil;
  ArrayList<String> clase;
  ArrayList<String> unidad;
  private int nintervalosperfil = 0;
  private int nintervaloseventos = 0;
  private int periodoIntegracion = 15;
  private int entradasEnUso = 0; //vic 30-09-19 homogeneidad con telesimex móvil
  private boolean primerbloque;
  private boolean ultimobloqueEventos = false;
  byte users[] = {(byte) 0x61};
  private int indxuser = 0;
  private int numBytesDir = 1;
    private int dirCliente;
  SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
  Abortar objabortar;
  private String usuario = "admin";
  List<String> listEventos;
  List<ERegistroEvento> listRegEventos;
  ERegistroEvento regEvento;
  public short pila[] = new short[10];
  public int i1, j1, l1;
  int indexConstant = 0;
  
  //Series 50
  String sOBISPerfil="0100630100FF";
  int clasePerfil=7;
  String sOBISEventos="0000636200FF";
  int claseEventos=7;
  
  //control revision de tramas
    private static int indxLength = 2;
    private static int indxControl = 6;
    private static int indxhcs1 = 7;
    private static int indxhcs2 = 8;
    private static int indxSacarDatos = 9;
    
    //control de intervalos/acumulados perfil
    public boolean primerintervalo; 
    Timestamp fprimerintervalo;
    ArrayList<Double> lecInterval = new ArrayList<>();
    public int indxlec = 0;
    ZoneId zid;
    private final Object monitor = new Object();
    private String label = "LeerTCPTecun";

  public LeerRemotoTCPTecunly(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) {
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
        this.lconfhora = lconfhora;
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        jinit();
    }

    private void jinit() {
        try {
            indxuser = 0;
            users[0] = (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 13 ? (byte) 0x61 : (byte) 0x03);
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            password2 = med.getPassword2();
            timeout = med.getTimeout() * 1000;
            ndias = med.getNdias();
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "0" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "0" : med.getDireccionLogica()));
            numBytesDir = Integer.parseInt((med.getBytesdireccion() == null ? "4" : med.getBytesdireccion()));
            dirCliente = Integer.parseInt((med.getDireccionCliente() == null ? "16" : med.getDireccionCliente()));
            lconske = cp.buscarConstantesKeLong(med.getnSerie());//se toman los valores de las constantes 
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
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
                    System.out.println("Arriving: " + tramastcum.encode(readBuffer, numbytes));
                    if (idxFrame == 0) {
                        begin = readBuffer[0];
                        beginOk = begin == 126;
                        System.out.println(" " + begin);
                    }
                    end = readBuffer[numbytes - 1];
                    endOk = end == 126;
                    System.out.println(" " + end);
                    if (numbytes >= 3 && idxFrame == 0) {
                        int actualFrameLength = (int) (((readBuffer[1] << 8) & 0x0700) + (readBuffer[2] & 0xFF));
                        System.out.println("Lenght: " + actualFrameLength);
                        frameLengthOk = actualFrameLength == numbytes - 2;
                    }
                    System.out.println("begin: " + beginOk + " endOk: " + endOk + " frameLengthOk: " + frameLengthOk);
                    if (!beginOk || !endOk || !frameLengthOk) {
                        System.arraycopy(readBuffer, 0, auxBuffer, idxFrame, numbytes);
                        idxFrame += numbytes;
                        System.out.println("Buffer Auxiliar: " + tramastcum.encode(auxBuffer, idxFrame));
                        if (idxFrame >= 3) {
                            int actualFrameLength = (int) (((auxBuffer[1] << 8) & 0x0700) + (auxBuffer[2] & 0xFF));
                            System.out.println("Lenght: " + actualFrameLength);
                            frameLengthOk = actualFrameLength == idxFrame - 2;
                            System.out.println("frameLengthOk: " + frameLengthOk);
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
                    reenviando = true;
                    enviando = false;
                    monitor.notifyAll();
                    return;
                }
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramastcum.encode(auxBuffer, idxFrame);
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
            System.out.println("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");

            if (lSNRMUA) { //SNRM
                revisarSNRM(vectorhex);
            } else if (lARRQ) {
                revisarAARQ(vectorhex);
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
            } else if (ldisconnect) { // corte
                revisarDisconnect(vectorhex);
            } else if (lconnect) { // reconexion
                revisarConnect(vectorhex);
            } else if (loutState) { // estadorele
                revisarOutState(vectorhex);
            } else if (lchangePass) { // cambio de password
                revisarChangePass(vectorhex);
            } else if (lphyaddress) {
                revisarPhyAddress(vectorhex);//dirfis
            } else if (lterminar) { //cerrar puertos
                revisarLogout(vectorhex);
            } else if (lReset) {//estado reset para los casos de contraseña mala y negacion
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
    
    private void rxFrameChecking(String[] vectorhex, String peticion) {
        tramaOK = false;
        if (vectorhex.length > 7) { //Header Checking
            Integer frameLength = Integer.parseInt(vectorhex[1] + vectorhex[2], 16) & 0x7FF;
            if (frameLength == (vectorhex.length - 2)) {//Size Checking
                if (vectorhex[0].equals("7E") && vectorhex[frameLength + 1].equals("7E")) {// Flags checking
                    vectorhex = cortarTrama(vectorhex, frameLength);
                    if (validacionCRCHCS(vectorhex, numBytesDir)) {
                        if (validacionCRCFCS(vectorhex)) {
                            tramaOK = true;
                        } else {
                            escribir("BAD FCS");
                            handleReject();
                        }
                    } else {
                        escribir("BAD HCS");
                        handleReject();
                    }
                } else {
                    escribir("Error trama inicio y final");
                    handleReject();
                }
            } else {
                escribir("Error trama incompleta");
                handleReenvio(peticion);
            }
        } else {
            escribir("Sin cabecera");           
            handleReenvio(peticion);
        }
    }
    
    public void revisarControlByte(String[] vectorhex, String peticion) {
        if (tramaOK) {
            tramaOK = false;
            if ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x01) == 0x00) { //es informacion
                //validamos secuencia
                escribir("NrM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1));
                if (nrEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1)) {
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
                    escribir("NsEsperado " + nsEsperado + " NrPc " + nrEsperado);
                    tramaOK = true;
                } else {
                    //no son los ns y nr esperados
                    escribir("Número de secuencia no esperados");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - Números de secuencia no esperados");
                    handleReject();
                }
            } else {
                escribir("No es información");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Interpreta trama " + peticion + " - No es informacion");
                if (vectorhex[4 + numBytesDir].equals("73")) {
                    handleReenvio(peticion);
                } else {
                    validarTipoTrama(vectorhex[4 + numBytesDir]);
                }
            }
        }
    }
        

    public boolean interpretaDatos(String[] trama, int tipoTrama) {

        vlec = new Vector<Electura>();
        boolean revisar, almacenar, detNumCanales, correcto;
        correcto = true;
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
        if (clasificarTrama(trama)) {
            while (i1 < trama.length) {
                while (pila[0] > 0) {
                    if (clasificarTrama(trama)) {
                        almacenar = false;
                        if (j1 == 10) {
                            System.out.println("Error en la Pila de datos");
                            System.out.println("----------------------------");
                            correcto = false;
                            break;
                        }
                        if (revisarIndice(i1, trama.length)) {
//                            System.out.println("Short.parseShort(trama[i1], 16): "+Integer.parseInt(trama[i1], 16)+" il:"+ i1);
                            if (Integer.parseInt(trama[i1], 16) == 1 || Integer.parseInt(trama[i1], 16) == 2 || Integer.parseInt(trama[i1 + 1], 16) >= 128) {
                                revisar = false;
                                if (Integer.parseInt(trama[i1 + 1], 16) >= 128) {//casos en que es un dato y no un tamanio
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
//                            System.out.println("revisar: "+revisar);
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
//                                        System.out.println("i1 en revisar: "+i1);
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
                                if (!procesaDato(cadenas[k], tipoTrama)) {
                                    correcto = false;
                                }
//                                System.out.println("sale de procesaDato");
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
                            //lo quito estefania
//              if (j1 == 1) {
//                almacenar = true;
//                if (pila[j1 - 1] != 0) {
//                  if (!operaReadResponse(trama)) {
//                    correcto = false;
//                    break;
//                  }
//                }
//              }
                            //hasta aqui termina
                            if (almacenar) {
                                System.out.println("----------------------------");
                                if (detNumCanales) {
                                    l1 = 0;
                                }
                            }
                        } else {
                            correcto = false;
                            break;
                        }
                    } else {
                        correcto = false;
                        break;
                    }
                }
                if (!correcto) {
                    break;
                }
                if (pila[j1 - 1] != 0) {
                    pila[j1 - 1]--;
                    j1--;
                }

            }
            System.out.println("termina interpretacion");
        } else {
            correcto = false;
            System.out.println("clasificar trama " + false);
        }

        if (correcto) {
            System.out.println("Proceso completado sin errores\n");
        } else {
            System.out.println("Proceso completado con errores\n");
        }
        return correcto;
    }

    public boolean aumentaPila(String[] trama) {
        short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
//        System.out.println("i1 iniciando aumentapila: "+ i1+"trama -1: "+ Integer.parseInt(trama[i1 - 1], 16));
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

    public boolean clasificarTrama(String[] trama) {
        if (i1 + 2 < trama.length) {
            if ("E6E700".equals(trama[i1] + trama[i1 + 1] + trama[i1 + 2])) {
                System.out.println("LLC Command Response");
                if (i1 + 3 < trama.length) {
                    i1 = i1 + 3;
                    switch (Short.parseShort(trama[i1], 16)) {
                        case 12: {
                            System.out.println("Read Response");
                            System.out.println("----------------------------");
                            if (i1 + 1 < trama.length) {
                                i1 = i1 + 1;
                            } else {
                                System.out.println("----------------------------");
                                System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                System.out.println("----------------------------");
                                return false;
                            }

                            pila[j1] = Short.parseShort(trama[i1], 16);
                            j1++;
                            i1++;
                            return operaReadResponse(trama);
                        }
                        case 97: {
                            System.out.println("AARE APDU (No esta realizado aun)\n----------------------------");
                            return false;
                        }
                        case 196: {
                            switch (Short.parseShort(trama[i1 + 1], 16)) {
                                case 1: {
                                    System.out.println("Get Normal Response \n----------------------------");
                                    if (i1 + 3 < trama.length) {
                                        i1 = i1 + 3;
                                    } else {
                                        System.out.println("----------------------------");
                                        System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                        System.out.println("----------------------------");
                                        return false;
                                    }

                                    if (j1 == 0) {
                                        pila[j1] = 1;
                                        j1++;
                                    }

                                    return operaReadResponse(trama);
                                }
                                case 2: {
                                    System.out.println("Get Block Transfer Response \n----------------------------");
                                    if (i1 + 12 < trama.length) {
                                        int recortar = 0;//VIC
                                        if (Short.parseShort(trama[i1 + 9], 16) >= 128) {
                                            System.out.println("Short.parseShort(trama[i1 + 9], 16): " + Short.parseShort(trama[i1 + 9], 16));
                                            recortar = (Integer.parseInt(trama[i1 + 9], 16) % 128);
                                            System.out.println("recortar: " + recortar);
                                            i1 = i1 + 10 + recortar;//vic hoy  

                                        } else {
                                            i1 = i1 + 10;//vic hoy  antes era +12
                                        }
//                                            System.out.println("i1 en case2: "+i1);

                                    } else {
                                        System.out.println("----------------------------");
                                        System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                        System.out.println("----------------------------");
                                        return false;
                                    }
                                    if (j1 == 0) {
                                        pila[j1] = 1;
                                        j1++;
                                    }
                                    return true;
                                }

                                default: {
                                    System.out.println("Unknown Response Code \n----------------------------");
                                }

                            }

                            return false;
                        }
                        default: {
                            System.out.println("Codigo xDLMS-APDU Erroneo\n----------------------------");
                            return false;
                        }
                    }

                } else {
                    System.out.println("----------------------------");
                    System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                    System.out.println("----------------------------");
                    return false;
                }
            } else {
                if (i1 == 0) {
                    System.out.println("No es un LLC Response\n----------------------------");
                    return false;
                }
            }

        }
//        System.out.println("sale clasificar trama\n----------------------------");
        return true;
    }

    public boolean operaReadResponse(String trama[]) {
        if (j1 == 1 && revisarIndice(i1, trama.length)) {
            switch (Short.parseShort(trama[i1], 16)) {
                case 0: {
                    System.out.println("Datos success!\n----------------------------");
                    i1++;
                    return true;
                }
                case 1: {
                    System.out.println("Error al Acceder a los Datos\n----------------------------");
                    return false;
                }
                case 2: {
                    System.out.println("Resultado en Bloques de Datos (No realizado aun)\n----------------------------");
                    return false;
                }
                case 3: {
                    System.out.println("Numero de Bloque (No realizado aun)\n----------------------------");
                    return false;
                }
                default: {
                    System.out.println("Codigo de Respuesta Erroneo\n----------------------------");
                    return false;
                }
            }
        } else {
            return false;
        }
    }
    //datos para el perfil
    SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Electura lec;
    public int indicecanal = 0;
    Date fechaEvento = null;
    Timestamp fechaintervalo = null;
    Timestamp ultimoIntervalo = null;
    Timestamp fechaCero = null;
    EConstanteKE econske = null;
    String canal = "";
    Vector<Electura> vlec;
    Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    ArrayList<EtipoCanal> vtipocanal;

    public boolean procesaDato(String dato, int tipoTrama) {

        switch (tipoTrama) {
            case 2: {
                dato = Hex2ASCII(dato);
                System.out.println(dato);
                datoserial = dato;
                break;
            }
            case 3: {
                try {
                    fechaActual = fecha.parse(Hex2Date(dato));
                    dato = sdf3.format(fechaActual);

                } catch (Exception e) {
                }
                System.out.println("Fecha actual " + dato);
                break;
            }
            case 4: {
                try {
                    if (!lentradasenuso) {
                        periodoIntegracion = Integer.parseInt(dato, 16) / 60;
                        System.out.println("\nPeriodo de Integracion: " + periodoIntegracion);
                        intervalo = periodoIntegracion;
                        lentradasenuso = true;
                    } else {
                        entradasEnUso = Integer.parseInt(dato, 16);
                        System.out.println("\nEntradas en uso: " + entradasEnUso);
                        lperiodoIntegracion = false;
                        lentradasenuso = false;
                    }

                } catch (Exception e) {
                }
                break;
            }

            case 5: {
                if (l1 == 0) {
                    clase.add(dato);
                    System.out.println("\nClase " + dato);
                }
                if (l1 == 1) {
                    obis.add(dato);
                    System.out.println("\nObis " + dato);
                    canalesOk = true;
                }
                //l1++;
                break;
            }
            case 6: { // vic 09-10-19 estado rele
                switch (dato) {
                    case "01":
                        estadorele = "CONNECTED";
                        break;
                    case "00":
                        estadorele = "DISCONNECTED";
                        break;
                }
                break;
            }
            case 7: {
                try {
                    dirfis = Integer.parseInt(dato, 16);
                    cp.actualizaDirFis(med.getnSerie(), dirfis);
                    System.out.println("\nDireccion Fisica: " + dirfis);

                } catch (Exception e) {
                    setdirfis = false;
                }
                break;
            }
            case 20: {
                if (l1 == 0) {
                    conskePerfil.add(dato);
                    System.out.println("\nEscala " + dato);
                }
                if (l1 == 1) {
                    unidad.add(dato);
                    System.out.println("\nUnidad " + dato);
                    constanteOk = true; //ojo vic 15-05 (debe ser true)
                }
                break;
            }
            case 22: {

                if (l1 == 0) {
                    indicecanal = 0;
                    indxlec = 0;
                    try {
                        //System.out.println(" indicecanal " + indicecanal);
                        System.out.println("dato original " + dato);
                        dato = sdf2.format(fecha.parse(Hex2Date(dato)));
                        System.out.println("Fecha intervalo " + dato);
                        fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                        if (fechaintervalo.getMinutes() % intervalo != 0) {
                            dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
                            fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                            System.out.println("Fecha intervalo aproximado" + dato);
                        }
                        if (ultimoIntervalo == null) {
                            ultimoIntervalo = fechaintervalo;
                        }
                        if (primerintervalo) {
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
                                        System.out.println("fecha intervalo en 0 " + fechaCero);
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
                                                        if (!(obis.get(k).substring(6, 8).equals("1D")) && (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21)) {//canal no es interval
                                                            if (!fprimerintervalo.equals(fechaintervalo)) {
                                                                //                                                        lec.fecha = new Timestamp(fechaCero.getTime()-(60000 * intervalo));
                                                                vlec.add(lec);
                                                            }
                                                        } else {
                                                            vlec.add(lec);
                                                        }
                                                    } else {
                                                        System.out.println("Constante");
                                                    }

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
                    //System.out.println(" indicecanal " + indicecanal);
                    try {
                        dato = String.valueOf(Long.parseLong(dato, 16));
                        System.out.println(dato);
                        //System.out.println("Canal " + Long.parseLong(obis.get(indicecanal), 16));
                        econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(indicecanal), 16));
                        if (econske != null) {
                            //tiene la constante
                            // System.out.println("Constante encontrada " + econske.getCanal2());
                            canal = "";
                            for (EtipoCanal et : vtipocanal) {
//                                System.out.println("Canal tcun " + et.getCanal());
//                                System.out.println("OBIS " + Long.parseLong(obis.get(indicecanal), 16));
                                if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(indicecanal), 16)) {
                                    canal = et.getUnidad();
                                    System.out.println("Canal " + et.getCanal() + " Unidad " + canal);
                                    break;
                                }
                            }
                            if (fechaintervalo != null && canal.length() > 0) {
                                if (constanteOk) {
                                    System.out.println("valor " + trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                    System.out.println("Unidad " + unidad.get(indicecanal));
                                    //System.out.println("Complemento a 2 ="+ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF)));
                                    lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                                    if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                        lec.lec = lec.lec / 1000.0;
                                    }
                                    if (!(obis.get(indicecanal).substring(6, 8).equals("1D")) && (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21)) {//canal no es interval
                                        lec.setLecaux(lec.lec);
                                        if (!fprimerintervalo.equals(fechaintervalo)) {
                                            //                                    lec.fecha = new Timestamp(fechaintervalo.getTime()-(60000 * intervalo));
                                            lec.lec = lec.lec - lecInterval.get(indxlec);
                                            vlec.add(lec);
                                            lecInterval.set(indxlec, lec.lecaux);
                                        } else {
                                            lecInterval.add(indxlec, lec.lecaux);
                                        }
                                        indxlec++;
                                    } else {
                                        vlec.add(lec);
                                    }
                                } else { //vic 26-04
                                    System.out.println("Sin Scaler-unit");
                                    System.out.println("\nConversion desde base de datos: ");
                                    System.out.println("valor " + trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                    System.out.println("Unidad " + canal);
                                    //System.out.println("Complemento a 2 ="+ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF)));
                                    lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                                    if (canal.contains("k") || canal.contains("K")) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                        lec.lec = lec.lec / 1000.0;
                                    }
                                    if (!(obis.get(indicecanal).substring(6, 8).equals("1D")) && (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21)) {//canal no es interval
                                        lec.setLecaux(lec.lec);
                                        if (!fprimerintervalo.equals(fechaintervalo)) {
                                            //                                    lec.fecha = new Timestamp(fechaintervalo.getTime()-(60000 * intervalo));
                                            lec.lec = lec.lec - lecInterval.get(indxlec);
                                            vlec.add(lec);
                                            lecInterval.set(indxlec, lec.lecaux);
                                        } else {
                                            lecInterval.add(indxlec, lec.lecaux);
                                        }
                                        indxlec++;
                                    } else {
                                        vlec.add(lec);
                                    }

                                }

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                break;
            }
            case 24: {
//                if (l1 == 0) {
//                    try {
//                        if (regEvento == null) {
//                            regEvento = new ERegistroEvento();
//                            regEvento.vcserie = seriemedidor;
//                            regEvento.vctipo = "0001";
//                        }
//
//                        fechaEvento = fecha.parse(Hex2Date(dato));
//                        regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
//
//                    } catch (Exception e) {
//                    }
//                } else if (l1 == 1) {
//                    try {
//                        fechaEvento = fecha.parse(Hex2Date(dato));
//                        regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
//
//                        if (regEvento.vcfechacorte != null && regEvento.vcfechareconexion != null) {
//
//                            System.out.println(fecha.format(regEvento.vcfechacorte) + " - " + fecha.format(regEvento.vcfechareconexion));
//                            regEvento.vcserie = seriemedidor;
//                            regEvento.vctipo = "0001";
//                                
//                            cp.actualizaEvento(regEvento, null);//                            
//                            listRegEventos.add(regEvento);
//                            regEvento = new ERegistroEvento();
//                            regEvento.vcserie = seriemedidor;
//                            regEvento.vctipo = "0001";
//                        }
//                    } catch (Exception e) {
//                    }
//                }

                //l1++;
                if (l1 == 0) {
                    try {
                        fechaEvento = fecha.parse(Hex2Date(dato));
                        System.out.println("Procesa registro del: " + fechaEvento);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (l1 == 1) {
                    try {
                        if (Integer.parseInt(dato, 16) == 2) {
                            System.out.println("Cod.: 2 - Power up: " + fechaEvento);
                            regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
                            if (regEvento.vcfechacorte != null && regEvento.vcfechareconexion != null) {
                                System.out.println(fecha.format(regEvento.vcfechacorte) + "-" + fecha.format(regEvento.vcfechareconexion));
                                escribir("Almacena corte y reconexión: " + fecha.format(regEvento.vcfechacorte) + " - " + fecha.format(regEvento.vcfechareconexion));
                                regEvento.vcserie = seriemedidor;
                                regEvento.vctipo = "0001";                                
                                cp.actualizaEvento(regEvento, null);
                                System.out.println("termina actualiza");
                                //                        listRegEventos.add(regEvento); //la lista en realidad no se usa, ya que se almacena en la base cada par corte-reconexión que se interpreta
                            }
                        } else if (Integer.parseInt(dato, 16) == 1) {
                            System.out.println("Cod.: 1 - Power down: " + fechaEvento);
                            //                    regEvento.vcfechareconexion = null;
                            regEvento = new ERegistroEvento();
                            regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                        } else {// Códigos restantes para el perfil de eventos Power Quality
                            //Descomentar para guardar el resto de eventos //VIC 12-07-19
                            System.out.println("Codigo de evento: " + Integer.parseInt(dato, 16));
                            regEvento = new ERegistroEvento();
                            regEvento.vcfechacorte = new Timestamp(fechaEvento.getTime());
                            regEvento.vcfechareconexion = regEvento.vcfechacorte;
                            escribir("Evento no almacenado: Cod. " + Integer.parseInt(dato, 16) + " - " + fecha.format(regEvento.vcfechacorte));
                            regEvento.vctipo = dato;
                            //                    if (regEvento.vcfechacorte != null && regEvento.vctipo != null) {
                            //                        //Descomentar para guardar el resto de eventos //VIC 12-07-19
                            ////                                System.out.println(fecha.format(regEvento.vcfechacorte) + ", " + regEvento.vctipo);
                            //                        regEvento.vcserie = seriemedidor;
                            //                        //Descomentar para guardar el resto de eventos //VIC 12-07-19
                           ////                                
                            ////                                cp.actualizaEvento(regEvento, null);
                            ////                                System.out.println("termina actualiza");
                            //                        listRegEventos.add(regEvento);
                            //                    }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }            
            default: {
                System.out.println("No es posible interpretar los datos");
                return false;
            }
        }
        return true;
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

    public boolean revisarIndice(int indice, int longTrama) {
        if (indice == (longTrama + bytesRecortados)) {
            System.out.println("----------------------------\nTrama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados\n----------------------------");
            return false;
        } else {
            return true;
        }
    }

    public int ActarisComplemento2(byte b) {
        int c = 0;
        if (b != 0) {
            if ((b & 0x80) != 0) {
                c = -1 * (~b + 1);
            } else {
                c = b;
            }
        }
        return c;
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

    public void revisarSNRM(String[] vectorhex) {
        String peticion = "SNRM";
        rxFrameChecking(vectorhex, peticion);
        if (tramaOK) {
            if (vectorhex[indxControl].equals("73") || vectorhex[indxControl].equals("63")) {
                ns = 0;
                nr = 0;
                escribir("NsPc " + ns + " NrPc " + nr);
                nrEsperado = ns + 1;
                nsEsperado = 0;
                lSNRMUA = false;
                lARRQ = true;
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta Trama " + peticion, "Correcto");
                byte trama[] = crearAARQ(tramastcum.getAarq(), password); //AARQ
                if (lPhyAdrs) {
                    trama = reconstruirTrama(crearAARQ(tramastcum.getAarq(), password));
                } else {
                    trama = asignaDirecciones(trama);
                }
                trama = calcularnuevocrcI(trama, numBytesDir);
                ultimatramaEnviada = trama;
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Envío petición", "AARQ");
                enviaTrama2(trama, "");
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Desconexión", "Error al interpretar SNRM");
                reiniciaComunicacion(true);
            }
        }
    }

    public void revisarAARQ(String[] vectorhex) {
        String peticion = "AARQ";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            if (vectorhex[indxControl + 23].equals("00") && vectorhex[indxControl + 30].equals("00")) { //VIC 22-10-19                                        
                lARRQ = false;
                lpowerLost = false;
                if (lPhyAdrs) {
                    lphyaddress = true;
                    byte trama[] = reconstruirTrama(tramastcum.getPhyaddress());
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Dirección Física");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta trama AARQ", "Correcta");
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Envío petición", "Serial");
                    lserialnumber = true;
                    byte trama[] = tramastcum.getSerial();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Numero de Serial");
                }
            } else {
                //reiniciamos
                byte trama[] = tramastcum.getLogout();
                trama = asignaDirecciones(trama);
                indxuser++;
                lARRQ = false;
                lpowerLost = false;
                lperiodoIntegracion = false;
                if (vectorhex[indxControl + 30].equals("0D")) {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta trama AARQ", "Error de autenticacion");
                    System.out.println("\nError de autenticacion");
                    if (indxuser < users.length) {
                        lReset = true;
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "");
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "No leído", "Sin envío de peticiones adicionales");
                        cerrarPuerto(true);
                        escribir("Desconexion - Error de autenticacion");
                        cerrarLog("Error de identificacion", false);
                        leer = false;
                    }
                } else {
                    escribir("Error de autenticacion - fallo en interpretación de AARE");
                    System.out.println("\nError de autenticacion - fallo en interpretación de AARE");
                    if (indxuser < users.length) {
                        lReset = true;
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "");
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de autenticacion");
                        cerrarPuerto(true);
                        escribir("Estado Lectura No leido");
                        cerrarLog("Desconexion Error de autenticacionn", false);
                        leer = false;
                    }
                }
            }
        }
    }

    public void revisarSerial(String[] vectorhex) {
        String peticion = "Serial";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lserialnumber = false;
            byte trama[] = null;
            try {
                String[] vectorData = sacarDatos(vectorhex, 7 + numBytesDir); //VIC debe ser 9 no 8
                boolean continuar;
                interpretaDatos(vectorData, 2);
                datoserial = "" + Long.parseLong(datoserial);
                escribir("Serial obtenido " + datoserial);
                escribir("Serial Medidor " + seriemedidor);
                if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {//VIC 
                    escribir("Numero serial " + datoserial);
                    continuar = true;
                } else {
                    escribir("Numero serial incorrecto");
                    continuar = false;
                }
                if (continuar) {
                    escribir("Serial correcto");
                    lfechaactual = true;
                    trama = tramastcum.getFechaactual();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Solicitud de fecha actual");
                } else {
                    System.out.println("Serial incorrecto");
                    escribir("Numero de serial no valido");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                    trama = tramastcum.getLogout();
                    lReset = true;
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                }
            } catch (Exception e) {
                escribir("Error al validar el numero serial");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                trama = tramastcum.getLogout();
                lReset = true;
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarFecAct(String[] vectorhex) {
        String peticion = "Fecha Actual";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            try {
                lfechaactual = false;
                String[] data = sacarDatos(vectorhex, 7 + numBytesDir);//VIC
                if (interpretaDatos(data, 3)) {
                    if (lconfhora) {
                        //**** conf hora
                        try {
                            byte trama[] = tramastcum.getConfhora();
                            time = new Timestamp(Calendar.getInstance().getTimeInMillis());
                            System.out.println("\nSincronizando hora con equipo local ");
                            System.out.println("\nFecha " + time);
                            String fecha = sdf.format(new Date(time.getTime()));
                            System.out.println("\nFecha a actualizaar " + fecha);
                            String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                            while (lfecha.length() < 4) {
                                lfecha = "0" + lfecha;
                            }
                            //VIC se debe suma uno a cada uno de 27 a 38 no 26 a 37
                            trama[25 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            trama[26 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
                            trama[27 + numBytesDir] = (byte) (Integer.parseInt(fecha.substring(4, 6)) & 0xFF);// mes 
                            trama[28 + numBytesDir] = (byte) (Integer.parseInt(fecha.substring(6, 8)) & 0xFF);//dia
                            trama[29 + numBytesDir] = (byte) (((time.getDay()) == 0 ? 7 : (time.getDay())) & 0xFF);// dia de la semana
                            //trama[33] = (byte) (((time.getDay()-1)==0? 7:(time.getDay()-1)) & 0xFF);// dia de la semana
                            trama[30 + numBytesDir] = (byte) (Integer.parseInt(fecha.substring(8, 10)) & 0xFF); // hora 
                            trama[31 + numBytesDir] = (byte) (Integer.parseInt(fecha.substring(10, 12)) & 0xFF); // min
                            trama[32 + numBytesDir] = (byte) (Integer.parseInt(fecha.substring(12, 14)) & 0xFF); // seg
                            trama[33 + numBytesDir] = (byte) 0xFF; // centesimas
                            trama[34 + numBytesDir] = (byte) 0x80;
                            trama[35 + numBytesDir] = (byte) 0x00; // desviacion 2 bytes
                            trama[36 + numBytesDir] = (byte) 0x00; // status

                            lfechaactual2 = false;
                            lfechasync = true;
                            trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                            asignaDirecciones(trama);
                            trama = calcularnuevocrcI(trama, numBytesDir);
                            ultimatramaEnviada = trama;
                            enviaTrama2(trama, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));
                        } catch (Exception e) {
                            escribir("Error al generar peticion de sincronizacion");
                            escribir(getErrorString(e.getStackTrace(), 3));
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Error al generar peticion de sincronizacion");
                            byte trama[] = tramastcum.getLogout();
                            lReset = true;
                            trama = calcularnuevocrcRR(trama);
                            enviaTrama2(trama, "Logout");
                        }
                    } else if (lperfil) {
                        lperiodoIntegracion = true;
                        byte trama[] = tramastcum.getPeriodoint();
                        trama = asignaDirecciones(trama);
                        trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21 ? cambiaClassnOBIS(trama, clasePerfil, sOBISPerfil) : trama);
                        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                        trama = calcularnuevocrcI(trama, numBytesDir);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "=> Solicitud de periodo de integracion");
                    } else if (leventos) {
                        lpowerLost = true;
                        listEventos = new ArrayList<>();
                        byte trama[] = tramastcum.getPowerLost();
                        trama = asignaDirecciones(trama);
                        trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21 ? cambiaClassnOBIS(trama, claseEventos, sOBISEventos) : trama);
                        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC es 6 no 5
                        trama = calcularnuevocrcI(trama, numBytesDir);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "=> Solicitud de eventos");
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Leido sin opciones");
                        cerrarPuerto(true);
                        escribir("Leido");
                        cerrarLog("Leido", true);
                        leer = false;
                    }
                } else {
                    escribir("Error en obtencion de fecha");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion error de fecha");
                    byte trama[] = tramastcum.getLogout();
                    lReset = true;
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                }
            } catch (Exception e) {
                escribir("Error interpretacion fecha");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Error interpretacion fecha");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarPeriodoInt(String[] vectorhex) {
        String peticion = "Periodo de Integración";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lperiodoIntegracion = false;
            String[] data = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            if (interpretaDatos(data, 4)) {
                if (lentradasenuso) {
                    byte trama[] = tramastcum.getEntradasperfil();
                    trama = asignaDirecciones(trama);
                    trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21 ? cambiaClassnOBIS(trama, clasePerfil, sOBISPerfil) : trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Solicitud de entradas en uso");

                } else {
                    linfoperfil = true;
                    obis = new ArrayList<String>();
                    conskePerfil = new ArrayList<String>();
                    unidad = new ArrayList<String>();
                    clase = new ArrayList<String>();
                    infoPerfil = new Vector<String>();
                    byte trama[] = tramastcum.getConfperfil();
                    trama = asignaDirecciones(trama);
                    trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21 ? cambiaClassnOBIS(trama, clasePerfil, sOBISPerfil) : trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Solicitud de configuracion del perfil de carga");
                }
            } else {
                escribir("Negacion de peticion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion negacion de peticion");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarInfoPerfil(String[] vectorhex) {
        String peticion = "Configuracion Perfil Carga";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            String vdata[] = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            infoPerfil.addAll(Arrays.asList(vdata));
            if (vectorhex[11 + numBytesDir].equals("01")) { //sin bloque
                procesaInfoPerfil();
                linfoperfil = false;
                if (canalesOk) {
                    lconstants = true;
                    indexConstant = 0;
                    escribir("Cantidad de canales internos del medidor: " + obis.size());
                    escribir("Vector de canales internos del medidor: " + obis.toString());
                    while (!clase.get(indexConstant).equals("0003")) {
                        conskePerfil.add("0");
                        unidad.add("0");
                        indexConstant++;
                    }
                    byte[] trama = construirConstant(obis.get(indexConstant));
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
                } else {
                    escribir("Medidor sin configuracion de canales - no se pedira el perfil");
                    lterminar = true;
                    byte trama[] = tramastcum.getLogout();
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                }
            } else {//con bloques
                if (vectorhex[11 + numBytesDir].equals("02") & vectorhex[13 + numBytesDir].equals("00")) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                    byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[14 + numBytesDir], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[15 + numBytesDir], 16) & 0xFF),
                        (byte) (Integer.parseInt(vectorhex[16 + numBytesDir], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[17 + numBytesDir], 16) & 0xFF)};

                    byte trama[] = crearREQ_NEXT(bloqueRecibido);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Envia Next Data Block Request");
                } else {
                    procesaInfoPerfil();
                    linfoperfil = false;
                    indexConstant = 0;
                    if (canalesOk) {
                        lconstants = true;
                        escribir("Cantidad de canales internos del medidor: " + obis.size());
                        escribir("Vector de canales internos del medidor: " + obis.toString());
                        while (!clase.get(indexConstant).equals("0003")) {
                            conskePerfil.add("0");
                            unidad.add("0");
                            indexConstant++;
                        }
                        byte[] trama = construirConstant(obis.get(indexConstant));
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
                    } else {
                        escribir("Medidor sin configuracion de canales - no se pedira el perfil");
                        lterminar = true;
                        byte trama[] = tramastcum.getLogout();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "Logout");
                    }
                }
            }
        }
    }

    public void revisarConstants(String[] vectorhex) {
        String peticion = "Constantes";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            String vdata[] = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            interpretaDatos(vdata, 20);
            indexConstant++;
            if (indexConstant > (obis.size() - 1)) {
                time = new Timestamp(Calendar.getInstance().getTimeInMillis());
                escribir("Sincronizando hora con Equipo Local ");
                System.out.println("\nmax desfase permitido" + ndesfasepermitido);
                System.out.println("\nFecha Local" + time);
                deltatimesync1 = new Timestamp(Calendar.getInstance().getTimeInMillis());
                System.out.println("\n Vector constantes " + conskePerfil.toString());
                lconstants = false;
                lfechaactual2 = true;
                byte trama[] = tramastcum.getFechaactual();
                trama = asignaDirecciones(trama);
                trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                trama = calcularnuevocrcI(trama, numBytesDir);
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "=> Solicitud de fecha actual");
            } else {
                //se solicita el siguiente obis
                while (!clase.get(indexConstant).equals("0003")) {
                    conskePerfil.add("0");
                    unidad.add("0");
                    indexConstant++;
                }
                byte[] trama = construirConstant(obis.get(indexConstant));
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
            }
        }
    }

    public void revisarFecAct2(String[] vectorhex) throws ParseException {
        String peticion = "Fecha Actual 2";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            deltatimesync2 = new Timestamp(Calendar.getInstance().getTimeInMillis());
            String[] dataTrama = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            if (interpretaDatos(dataTrama, 3)) {
                escribir("Fecha actual de medidor " + fechaActual);
                System.out.println("Fecha actual de medidor " + fechaActual);
                deltatimesync2 = new Timestamp(Calendar.getInstance().getTimeInMillis());
                escribir("Fecha actual de medidor " + fechaActual);
                System.out.println("Fecha actual de medidor " + fechaActual);
                lfechaactual2 = false;
                if (lperfil) {//se solicita perfil de carga1
                    try {
                        escribir("Fecha actual Colombia: " + this.getDCurrentDate());
                        if (med.getFecha() != null) {
                            escribir("Fecha Ultima Lectura " + med.getFecha());
                            long diffInMillies = Math.abs(this.getDSpecificDate(true, 1, "D").getTime() - med.getFecha().getTime());
                            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                            ndias = (int) diff;
                            escribir("Numero de dias a leer: " + ndias);
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
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
                        lperfilcarga = true;
                        byte trama[] = tramastcum.getPerfilcarga();
                        trama = asignaDirecciones(trama);
                        trama = (Integer.parseInt(med.getMarcaMedidor().getCodigo()) == 21 ? cambiaClassnOBIS(trama, clasePerfil, sOBISPerfil) : trama);
                        Date fechaUltLec = new Date(med.getFecha().getTime());
                        String fechaI = sdf.format(this.getDSpecificDate(false, 1, "H", fechaUltLec));
                        System.out.println("fechaI: " + fechaI);
                        System.out.println("Fecha inicio peticion pefil: " + sdfarchivo.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                        escribir("Fecha inicio peticion pefil: " + sdfarchivo.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                        String lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(0, 4)));
                        while (lfecha.length() < 4) {
                            lfecha = "0" + lfecha;
                        }
                        System.out.println("Año: " + lfecha);
                        trama[46 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        trama[47 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(4, 6)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        System.out.println("Mes: " + lfecha);
                        trama[48 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(6, 8)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        System.out.println("Día: " + lfecha);
                        trama[49 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        trama[51 + numBytesDir] = (byte) (Integer.parseInt(fechaI.substring(8, 10)) & 0xFF); // hora 
                        trama[52 + numBytesDir] = (byte) (Integer.parseInt(fechaI.substring(10, 12)) & 0xFF); // min
                        trama[53 + numBytesDir] = (byte) (Integer.parseInt(fechaI.substring(12, 14)) & 0xFF); // seg
                        String fechaF;
                        if (ndias > 30) {
                            fechaF = this.getSpecificDate(sdf, true, 30, "D", fechaI);
                            escribir("Fecha final de perfil de carga " + this.getSpecificDate(sdf3, false, ndias - 31, "D"));
                        } else {
                            fechaF = sdf.format(this.getDCurrentDate());
                            escribir("Fecha final de perfil de carga " + sdf3.format(this.getDCurrentDate()));
                        }
                        System.out.println("Fecha F: " + fechaF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(0, 4)));
                        while (lfecha.length() < 4) {
                            lfecha = "0" + lfecha;
                        }
                        trama[60 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        trama[61 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(4, 6)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        trama[62 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(6, 8)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        trama[63 + numBytesDir] = (byte) ((Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF) + 1);

                        trama[65 + numBytesDir] = (byte) (Integer.parseInt(fechaF.substring(8, 10)) & 0xFF); // hora
                        trama[66 + numBytesDir] = (byte) (0x00); // min
                        trama[67 + numBytesDir] = (byte) (0x00); // seg

                        vPerfilCarga = new Vector<>();
                        primerbloque = true;
                        trama[4 + numBytesDir] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama, numBytesDir);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "Solicitud de pefil de carga");
                    } else {
                        escribir("Desfase horario no se solicitara el perfil de carga");
                        lfechaactual2 = false;
                        lterminar = true;
                        byte trama[] = tramastcum.getLogout();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "Logout");
                    }
                }
            } else {
                escribir("Negacion de peticion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion negacion de peticion");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarPerfil(String[] vectorhex) {
        String peticion = "Perfil de Carga";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lperfilcompleto = true;
            String vdata[] = null;
            if (primerbloque) {
                vdata = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
                bytesRecortados = 8;//VIC
                primerbloque = false;
            } else {
                int longi = Integer.parseInt(vectorhex[19 + numBytesDir], 16); //VIC es 21 no 20
                int recortar = 19 + numBytesDir;//VIC
                if (longi >= 128) {
                    recortar = recortar + (longi % 128);
                }
                bytesRecortados = bytesRecortados + recortar;
                vdata = sacarDatos(vectorhex, recortar + 1);
            }
            vPerfilCarga.addAll(Arrays.asList(vdata));
            //VIC es 15 no 14 y el bloque desde 16 - tambien se debe verificar que sea tipo response data block
            if (vectorhex[11 + numBytesDir].equals("01") & vectorhex[13 + numBytesDir].equals("01")) {
                int accessResult = Integer.parseInt(vectorhex[14 + numBytesDir], 16);
                revisaDataAccessResult(accessResult);
                lperfilcarga = false;
                lterminar = true;
                byte trama[] = tramastcum.getLogout();
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
                return;
            }
            if (vectorhex[11 + numBytesDir].equals("02") & vectorhex[13 + numBytesDir].equals("00")) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[14 + numBytesDir], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[15 + numBytesDir], 16) & 0xFF),
                    (byte) (Integer.parseInt(vectorhex[16 + numBytesDir], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[17 + numBytesDir], 16) & 0xFF)};

                byte trama[] = crearREQ_NEXT(bloqueRecibido);
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "=> Envia Next Data Block Request");
            } else {
                if (vectorhex[14 + numBytesDir].equals("01") & vectorhex[15 + numBytesDir].equals("00") & (0 < entradasEnUso)) {//no presenta perfil por rango
                    byte trama[] = tramastcum.getPerfilporentradas();
                    int numentradas = (ndias * 24 * 60 * 60) / (periodoIntegracion * 60);
                    int nentradasfin = entradasEnUso;
                    //entradasEnUso-numentradas es el limite inicial
                    escribir("Entradas debido a rango: " + numentradas);
                    if (entradasEnUso < numentradas) {
                        numentradas = entradasEnUso - 1;
                        nentradasfin = 0;
                        escribir("Entradas a pedir: " + entradasEnUso);
                    } else {
                        escribir("Entradas a pedir: " + numentradas);
                    }
                    String nentradasini = Integer.toHexString(entradasEnUso - numentradas).toUpperCase();
                    while (nentradasini.length() < 8) {
                        nentradasini = "0" + nentradasini;
                    }
                    trama[27 + numBytesDir] = (byte) (Integer.parseInt(nentradasini.substring(0, 2), 16) & 0xFF);
                    trama[28 + numBytesDir] = (byte) (Integer.parseInt(nentradasini.substring(2, 4), 16) & 0xFF);
                    trama[29 + numBytesDir] = (byte) (Integer.parseInt(nentradasini.substring(4, 6), 16) & 0xFF);
                    trama[30 + numBytesDir] = (byte) (Integer.parseInt(nentradasini.substring(6, 8), 16) & 0xFF);

                    //entradasEnUso es el limite final
                    String nentradas = Integer.toHexString(nentradasfin).toUpperCase();
                    while (nentradas.length() < 8) {
                        nentradas = "0" + nentradas;
                    }
                    trama[32 + numBytesDir] = (byte) (Integer.parseInt(nentradas.substring(0, 2), 16) & 0xFF);
                    trama[33 + numBytesDir] = (byte) (Integer.parseInt(nentradas.substring(2, 4), 16) & 0xFF);
                    trama[34 + numBytesDir] = (byte) (Integer.parseInt(nentradas.substring(4, 6), 16) & 0xFF);
                    trama[35 + numBytesDir] = (byte) (Integer.parseInt(nentradas.substring(6, 8), 16) & 0xFF);

                    vPerfilCarga = new Vector<String>();
                    trama = asignaDirecciones(trama);
                    primerbloque = true;
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "Solicitud de perfil de carga - filtro por entradas");

                } else {//es la ultima trama por lo tanto se envia una rr
                    byte trama[] = tramastcum.getRR();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = RR_CTRL(nr);//VIC
                    trama = calcularnuevocrcRR(trama);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Envia Recieved Ready RR");
                }
            }
        }
    }

    public void revisarEventos(String[] vectorhex) throws ParseException {
        String peticion = "Eventos";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            String[] vectorData = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            listEventos.addAll(Arrays.asList(vectorData));
            if ((Integer.parseInt(vectorhex[13 + numBytesDir], 16) & 0x01) == 0x00) {
                byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[14 + numBytesDir], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[15 + numBytesDir], 16) & 0xFF),
                    (byte) (Integer.parseInt(vectorhex[16 + numBytesDir], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[17 + numBytesDir], 16) & 0xFF)};

                byte trama[] = crearREQ_NEXT(bloqueRecibido);
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "=> Envia Next Data Block Request");
            } else {
                lpowerLost = false;
                lterminar = true;
                byte trama[] = tramastcum.getLogout();
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    //nuevos vic 08-10-19 corte y reconexión
    public void revisarDisconnect(String[] vectorhex) throws ParseException {
        String peticion = "Corte";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            //String[] vectorData = sacarDatos(vectorhex, 9); //VIC debe ser 9 no 8
            try {
                if (vectorhex[13 + numBytesDir].equals("00")) {
                    escribir("Success !!!");
                } else {
                    escribir("Fail");
                }
                ldisconnect = false;
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                byte trama[] = tramastcum.getLogout();
                lterminar = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");

            } catch (Exception e) {
                System.out.println("Error al solicitar el corte");
                escribir("Error al solicitar el corte");
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarConnect(String[] vectorhex) throws ParseException {
        String peticion = "Reconexion";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            //String[] vectorData = sacarDatos(vectorhex, 9); //VIC debe ser 9 no 8
            try {

                if (vectorhex[13 + numBytesDir].equals("00")) {
                    System.out.println("\nSuccess !!!");
                    escribir("Success !!!");
                } else {
                    System.out.println("\nFail");
                    escribir("Fail");
                }
                lconnect = false;
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                byte trama[] = tramastcum.getLogout();
                lterminar = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");

            } catch (Exception e) {
                System.out.println("Error al solicitar la reconexion");
                escribir("Error al solicitar la reconexion");
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarOutState(String[] vectorhex) throws ParseException {
        String peticion = "Estado Rele";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            try {
                String[] vectorData = sacarDatos(vectorhex, 7 + numBytesDir); //VIC debe ser 9 no 8
                interpretaDatos(vectorData, 6);
                loutState = false;
                System.out.println("Estado rele: " + estadorele);
                escribir("Estado rele: " + estadorele);
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                byte trama[] = tramastcum.getLogout();
                lterminar = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");

            } catch (Exception e) {
                System.out.println("Error al verificar el estado del rele");
                escribir("Error al verificar el estado del rele");
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    // vic 10-10-19 revisarChangePass
    public void revisarChangePass(String[] vectorhex) throws ParseException {
        String peticion = "Cambio Contrasena";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            //String[] vectorData = sacarDatos(vectorhex, 9); //VIC debe ser 9 no 8
            try {

                if (vectorhex[13 + numBytesDir].equals("00")) {
                    System.out.println("\nSuccess !!!");
                    escribir("Success !!!");
                    cp.actualizaPassword(med.getnSerie(), newPass, null);
                    System.out.println("\nActualiza password en la configuracion");
                    escribir("Actualiza password en la configuracion");
                } else {
                    System.out.println("\nFail");
                    escribir("Fail");
                }
                lchangePass = false;
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                byte trama[] = tramastcum.getLogout();
                lterminar = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");

            } catch (Exception e) {
                System.out.println("Error al solicitar la reconexion");
                escribir("Error al solicitar la reconexion");
                //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                byte trama[] = tramastcum.getLogout();
                lReset = true;
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarLogout(String[] vectorhex) {
        String peticion = "Logout";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        lterminar = false;
        cerrarPuerto(false);
        //cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
        boolean l = false;
        if (lperfil) {

            if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                try {
                    procesaDatos();
                    cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                    if (fechaintervalo != null) {
                        if (fechaintervalo.after(med.getFecha())) {//med.getFecha())) {
                            cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                l = true;
            }
        }
//                                if (lregistros) {//                                    
//                                    if (vRegistros != null && vRegistros.size() > 0) {
//                                        try {
//                                            procesaRegistros();
//                                            if (fechaintervalo != null) {
//                                                cp.actualizaRegistro(vreg, sgflect.format(new Date(fechaintervalo.getTime())), null);
//                                                if (fechaintervalo.after(fechalectura)){//med.getFecha())) {
//                                                  cp.actualizaFechaLectura2(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())),lm, null);
//                                                }
//                                            } 
//
//                                            
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        l = true;
//                                    }
//                                }
        if (leventos) {
            if (listEventos != null && listEventos.size() > 0) {
                try {
                    procesaInfoEventos();
                } catch (Exception e) {
                }
                l = true;
            }
        }
        if (!lperfil && !leventos) {
            l = true;
        }
        if (lconfhora) {
            l = true;
        }
        if (l) {
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Verificación estado de lectura", "Leído");
            escribir("Estado Lectura Leido");
            cerrarLog("Leido", true);
            //med.MedLeido = true;
        } else {
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Verificación estado de lectura", "No Leído");
            escribir("Estado Lectura No Leido");
            cerrarLog("No Leido", false);
        }
        leer = false;

    }

    public void revisarReset(String[] vectorhex) {
        String peticion = "Reset";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        lReset = false;
        if (setdirfis) {
            indxLength = 2;
            indxControl = indxLength + numBytesDir + 2;
            indxhcs1 = indxControl + 1;
            indxhcs2 = indxControl + 2;
            indxSacarDatos = indxControl + 3;
        }
        reiniciaComunicacion(false);

    }

    public void revisarConfHora(String[] vectorhex) {
        String peticion = "Sincronizacion";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lfechasync = false;
            escribir("NsPc " + ns + " NrPc " + nr);
            if (Integer.parseInt(vectorhex[13 + numBytesDir], 16) == 0) {//VIC es 15 no 14
                System.out.println("\nSe sincronizo la hora correctamente");
                escribir("Se sincronizo la hora correctamente");
                lterminar = true;
            } else {
                System.out.println("\nNo fue posible sincronizar la hora");
                escribir("No fue posible sincronizar la hora");
                lReset = true;
            }
            byte trama[] = tramastcum.getLogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            enviaTrama2(trama, "Logout");
        }
    }

    public void revisarPhyAddress(String[] vectorhex) {
        String peticion = "Dirección Física";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {

            lserialnumber = false;
            try {
                String[] vectorData = sacarDatos(vectorhex, indxSacarDatos); //VIC debe ser 9 no 8
                interpretaDatos(vectorData, 7);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta Trama " + peticion, peticion + " obtenida: " + dirfis);
                lphyaddress = false;
                lReset = true;
                lPhyAdrs = false;
                byte trama[] = tramastcum.getLogout();
                if (setdirfis) {
                    reintentoconexion--;
                    trama = reconstruirTrama(tramastcum.getLogout());
                } else {
                    trama = asignaDirecciones(trama);
                }
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "");
            } catch (Exception e) {
                System.out.println("Error al interpretar " + peticion);
                escribir("Error al interpretar " + peticion);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Desconexión", "Error al interpretar " + peticion);
                byte trama[] = tramastcum.getLogout();
                if (setdirfis) {
                    reintentoconexion--;
                    trama = reconstruirTrama(tramastcum.getLogout());
                } else {
                    trama = asignaDirecciones(trama);
                }
                lReset = true;
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void procesaInfoEventos() throws Exception {
        String[] arrayEventos = listEventos.toArray(new String[listEventos.size()]);
        interpretaDatos(arrayEventos, 24);
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
        lfechasync = false;
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
                
                if (lSNRMUA) {
                    lPhyAdrs = true;
                } else {
                    lSNRMUA = true;
                }
                indxLength = 2;
                indxControl = indxLength + numBytesDir + 2;
                indxhcs1 = indxControl + 1;
                indxhcs2 = indxControl + 2;
                indxSacarDatos = indxControl + 3;
                setdirfis = lPhyAdrs;

                byte[] trama = tramastcum.getSnrm();
                if (lPhyAdrs) {
                    trama = reconstruirTrama(tramastcum.getSnrm());
                } else {
                    trama = asignaDirecciones(trama);
                }
                trama = calcularnuevocrcI(trama, numBytesDir);
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "");
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
    
    public void cerrarPuerto(boolean sendBye) {
        if (sendBye) {
            byte trama[] = tramastcum.getLogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            escribir("Envia Logout sin espera de respuesta");
            escribir("=> " + tramastcum.encode(trama, trama.length));
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

    private void enviaTrama2(byte[] bytes, String descripcion) {
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
                                escribir("=> " + tramastcum.encode(trama, trama.length));
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
                                System.out.println("Numero de reenvios agotado");
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
                            if (lperfilcarga) {
                                if (!lperfilcompleto) {
                                    if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                                        procesaInfoPerfil();
                                        try {
                                            procesaDatos();
                                        } catch (Exception e) {
                                            escribir("Error en procesamiento de perfil");
                                            escribir(getErrorString(e.getStackTrace(), 3));
                                        }
                                        try {
                                            if (vlec.size() > 0) {
                                                cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                                                try {
                                                    if (fechaintervalo != null) {
                                                        if (fechaintervalo.after(med.getFecha())) {
                                                            cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    escribir(getErrorString(e.getStackTrace(), 3));
                                                    escribir("Error actualizando fecha: " + sgflect.format(new Date(fechaintervalo.getTime())));
                                                }
                                            } else {
                                                escribir("Sin datos para almacenar");
                                            }
                                        } catch (Exception e) {
                                            escribir("Error en procesamiento de perfil");
                                            escribir(getErrorString(e.getStackTrace(), 3));
                                        }
                                    }
                                }
                                lperfilcarga = false;
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
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal                      
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    private void procesaInfoPerfil() {
        String[] data = infoPerfil.toArray(new String[infoPerfil.size()]);
        interpretaDatos(data, 5);
    }

    private void procesaDatos() {
        primerintervalo = true;
        String[] data = vPerfilCarga.toArray(new String[vPerfilCarga.size()]);
        interpretaDatos(data, 22);
    }

    private String[] cortarTrama(String[] vectorhex, int tamaño) {
        String nuevoVector[] = new String[tamaño + 2];
        System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
        for (String nuevoVector1 : nuevoVector) {
            System.out.print(" " + nuevoVector1);
        }
        return nuevoVector;
    }

    private byte[] calcularnuevocrcRR(byte[] siguientetrama) {
        siguientetrama[2] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
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

    public static byte[] calcularnuevocrcI(byte[] siguientetrama, int nBytesDir) {
        try {
            siguientetrama[2] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
            byte[] data = new byte[4 + nBytesDir];
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
            System.out.println("\nNuevo HCS: " + stxcrc);
            siguientetrama[4 + nBytesDir + 1] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[4 + nBytesDir + 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
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
            System.out.println("\nNuevo FCS " + stxcrc);
            siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return siguientetrama;
    }


    public static boolean validacionCRCHCS(String[] data, int numBytesDir) {
        boolean lcrc = false;
        byte b[] = new byte[4 + numBytesDir];
        for (int j = 0; j
                < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        //System.out.println("valor crc cal" + crc);
        String stx = data[4 + numBytesDir + 1] + "" + data[4 + numBytesDir + 2];
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
        //System.out.println("valor crc cal" + crc);
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
        //System.out.println("fcs trama " + stx);
        //System.out.println("fcs calculado " + stxcrc);
        if (stx.equals(stxcrc)) {
            lcrc = true;
        }
        return lcrc;
    }
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public static String encode(byte[] b, int ancho) {

        StringBuilder s = new StringBuilder(2 * b.length);

        for (int i = 0; i < ancho; i++) {

            int v = b[i] & 0xff;
            if (i != 0) {
                s.append(" ");
            }
            s.append((char) Hexhars[v >> 4]);
            s.append((char) Hexhars[v & 0xf]);
        }

        return s.toString().toUpperCase();
    }

    private byte[] asignaDirecciones(byte[] frame) {
        byte[] frameTemp;
        int newLength;
        int logAddr = numBytesDir == 1 ? (dirlog * 2) + 1 : (dirlog * 2);
        int phyAddr = (dirfis * 2) + 1;
        String sdirlog;
        String sdirfis;
        System.out.println("Número de Bytes de Dirección: " + numBytesDir);
        try {
            switch (numBytesDir) {
                case 1: // solo dirección lógica   
                    System.out.println("Longitud trama temporal: " + (frame.length - 3));
                    frameTemp = new byte[frame.length - 3];
                    System.arraycopy(frame, 0, frameTemp, 0, 4);
                    System.arraycopy(frame, 7, frameTemp, 4, frame.length - 7);
                    newLength = (int) ((((frame[1] << 8) & 0x0700) + (frame[2] & 0xFF)) - 3);
                    System.out.println("Nueva longitud: " + newLength);
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    if (sdirlog.length() == 1) {
                        sdirlog = "0" + sdirlog;
                    }
                    System.out.println("Dirección lógica : " + sdirlog);
                    frameTemp[1] = (byte) ((byte) 0xA0 + (byte) (newLength & 0x0700));
                    frameTemp[2] = (byte) (newLength & 0xFF);
                    frameTemp[3] = (byte) (Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF);
                    frameTemp[4] = users[indxuser];
                    return frameTemp;
                case 2: // Dirección lógica (1 byte) y dirección física ( 1 byte)
                    frameTemp = new byte[frame.length - 2];
                    System.arraycopy(frame, 0, frameTemp, 0, 5);
                    System.arraycopy(frame, 7, frameTemp, 5, frame.length - 7);
                    newLength = (int) ((((frame[1] << 8) & 0x0700) + (frame[2] & 0xFF)) - 2);
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    sdirfis = Integer.toHexString(phyAddr).toUpperCase();
                    if (sdirlog.length() == 1) {
                        sdirlog = "0" + sdirlog;
                    }
                    if (sdirfis.length() == 1) {
                        sdirfis = "0" + sdirfis;
                    }
                    frameTemp[1] = (byte) ((byte) 0xA0 + (byte) (newLength & 0x0700));
                    frameTemp[2] = (byte) (newLength & 0xFF);
                    frameTemp[3] = (byte) (Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF);
                    frameTemp[4] = (byte) (Integer.parseInt(sdirfis.substring(0, 2), 16) & 0xFF);
                    frameTemp[5] = users[indxuser];
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
                    frame[7] = users[indxuser];
                    return frame;
                default:
                    System.out.println("La cantidad de bytes de dirección en HDLC solo puede ser de 1, 2 o 4 bytes, no existe otra cantidad permitida.");
                    escribir("La cantidad de bytes de dirección en HDLC solo puede ser de 1, 2 o 4 bytes, no existe otra cantidad permitida.");
                    return frame;
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace()[0]);
            return frame;
        }
    }
    
    public String[] sacarDatos(String[] vectorhex, int inicio) {
        int tam = (Integer.parseInt((vectorhex[1] + vectorhex[2]), 16) & 0x7FF);
        String nuevoVector[] = new String[tam - inicio - 1];
        System.arraycopy(vectorhex, inicio, nuevoVector, 0, nuevoVector.length);
        //System.out.println("\nDATA\n");
        System.out.println("\n" + Arrays.toString(nuevoVector));
        return nuevoVector;
    }

    private byte[] reconstruirTrama(byte[] t) { //21-10-19
        ArrayList<String> trama = new ArrayList<>();
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
        tramabyte[3] = (byte) (Integer.parseInt(Integer.toHexString((dirlog * 2) + 1), 16) & 0xFF);
        tramabyte[4] = (byte) users[indxuser];

        return tramabyte;
    }

    private byte[] cambiaClassnOBIS(byte[] trama, int clase, String sOBIS) { //0100010800FF
        //clase
        trama[indxControl + 10] = (byte) (Integer.parseInt(Integer.toHexString(clase), 16) & 0xFF);
        //A
        trama[indxControl + 11] = (byte) (Integer.parseInt(sOBIS.substring(0, 2), 16) & 0xFF);
        //B
        trama[indxControl + 12] = (byte) (Integer.parseInt(sOBIS.substring(2, 4), 16) & 0xFF);
        //C
        trama[indxControl + 13] = (byte) (Integer.parseInt(sOBIS.substring(4, 6), 16) & 0xFF);
        //D
        trama[indxControl + 14] = (byte) (Integer.parseInt(sOBIS.substring(6, 8), 16) & 0xFF);
        //E
        trama[indxControl + 15] = (byte) (Integer.parseInt(sOBIS.substring(8, 10), 16) & 0xFF);
        //F
        trama[indxControl + 16] = (byte) (Integer.parseInt(sOBIS.substring(10, 12), 16) & 0xFF);
        return trama;
    }

    private byte[] crearAARQ(byte[] t3, String pass) {
        ArrayList<String> trama = new ArrayList<>();
        trama.add("7E");
        trama.add("A0");
        if (pass == null) {
            System.out.println("/n Entre");
            t3 = tramastcum.getAarq();
            for (int i = 2; i < t3.length; i++) {//vic 44
                trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
            }
        } else {
            for (int i = 2; i < 44; i++) {//vic 44                
                trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
            }
            //pass
            for (int i = 0; i < pass.length(); i++) {
                trama.add(convertStringToHex(pass.substring(i, i + 1)).toUpperCase());
            }
            //datos 2da parte
            for (int i = 44 + pass.length(); i < t3.length; i++) {//vic 52
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
            tramabyte[15] = (byte) (Integer.parseInt(Integer.toHexString((48 + 7)), 16) & 0xFF);//(byte) (Integer.parseInt(Integer.toHexString((46 + pass.length())), 16) & 0xFF);
            tramabyte[69] = (byte) (Integer.parseInt(Integer.toHexString(0), 16) & 0xFF);
            tramabyte[70] = (byte) (Integer.parseInt(Integer.toHexString(28), 16) & 0xFF);
            tramabyte[71] = (byte) (Integer.parseInt(Integer.toHexString(31), 16) & 0xFF);
        } else {
            //tamaño Data            
            tramabyte[15] = (byte) (Integer.parseInt(Integer.toHexString((47 + pass.length())), 16) & 0xFF);
            //Tamaño Password
            tramabyte[41] = (byte) (Integer.parseInt(Integer.toHexString(pass.length() + 2), 16) & 0xFF);
            tramabyte[43] = (byte) (Integer.parseInt(Integer.toHexString(pass.length()), 16) & 0xFF);
            //conformance-block
            /*
            tramabyte[tramabyte.length - 8] = (byte) (Integer.parseInt(Integer.toHexString(0), 16) & 0xFF);
            tramabyte[tramabyte.length - 7] = (byte) (Integer.parseInt(Integer.toHexString(26), 16) & 0xFF);
            tramabyte[tramabyte.length - 6] = (byte) (Integer.parseInt(Integer.toHexString(29), 16) & 0xFF);
             */

            tramabyte[tramabyte.length - 8] = (byte) (Integer.parseInt(Integer.toHexString(0), 16) & 0xFF);
            tramabyte[tramabyte.length - 7] = (byte) (Integer.parseInt(Integer.toHexString(28), 16) & 0xFF);
            tramabyte[tramabyte.length - 6] = (byte) (Integer.parseInt(Integer.toHexString(31), 16) & 0xFF);
            // Max PDU Size 
            tramabyte[tramabyte.length - 5] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);//Parametrizable desde Base de datos
            tramabyte[tramabyte.length - 4] = (byte) (Integer.parseInt(Integer.toHexString(255), 16) & 0xFF);
        }

        return tramabyte;
    }

    // vic 10-10-19 crearGetChPass
    private byte[] crearGetChPass(byte[] tchpass, String newpass) {
        ArrayList<String> trama = new ArrayList<>();

        //copiar inicio de trama hasta antes del tamaño de la pass
        for (int i = 0; i < 26; i++) {
            trama.add(Integer.toHexString(tchpass[i] & 0xFF).toUpperCase());
        }

        //tamaño de la pass
        trama.add(Integer.toHexString((newpass.length())).toUpperCase());

        //password
        for (int i = 0; i < newpass.length(); i++) {
            trama.add(convertStringToHex(newpass.substring(i, i + 1)).toUpperCase());
        }
        //datos 2da parte
        for (int i = tchpass.length - 3; i < tchpass.length; i++) {
            trama.add(Integer.toHexString(tchpass[i] & 0xFF).toUpperCase());
        }
        //copia trama a arreglo de bytes
        byte[] tramabyte = new byte[trama.size()];
        int i = 0;
        for (String t : trama) {
            tramabyte[i] = (byte) (Integer.parseInt(t, 16) & 0xFF);
            i++;
        }
        //tamaño HLDC
        tramabyte[2] = (byte) (Integer.parseInt(Integer.toHexString(tramabyte.length - 2), 16) & 0xFF);

        return tramabyte;
    }

    private byte[] crearREQ_NEXT(byte[] bloqueRecibido) {

        byte trama[] = tramastcum.getREQ_NEXT();
        trama = asignaDirecciones(trama);
        trama[4+numBytesDir] = I_CTRL(nr, ns);//VIC
        System.arraycopy(bloqueRecibido, 0, trama, 13+numBytesDir, bloqueRecibido.length);// desde 15 no 14
        trama = calcularnuevocrcI(trama, numBytesDir);

        return trama;
    }

    private byte[] construirConstant(String obis) {
        System.out.println("\nOBIS a solicitar " + obis);
        byte trama[] = tramastcum.getConstant();
        trama = asignaDirecciones(trama);
        trama[4+numBytesDir] = I_CTRL(nr, ns);//VIC y va de 17 a 22 no de 16 a 21
        trama[15+numBytesDir] = (byte) (Integer.parseInt(obis.substring(0, 2), 16) & 0xFF);
        trama[16+numBytesDir] = (byte) (Integer.parseInt(obis.substring(2, 4), 16) & 0xFF);
        trama[17+numBytesDir] = (byte) (Integer.parseInt(obis.substring(4, 6), 16) & 0xFF);
        trama[18+numBytesDir] = (byte) (Integer.parseInt(obis.substring(6, 8), 16) & 0xFF);
        trama[19+numBytesDir] = (byte) (Integer.parseInt(obis.substring(8, 10), 16) & 0xFF);
        trama[20+numBytesDir] = (byte) (Integer.parseInt(obis.substring(10, 12), 16) & 0xFF);
        trama = calcularnuevocrcI(trama, numBytesDir);
        return trama;
    }

    public String convertStringToHex(String str) {
        //System.out.println("cadena a transformar " + str);
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
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
    
    
    public void revisaDataAccessResult(int data){
        switch (data) {
            case 0: 
                escribir("Data Access Result: success");
                break;
                
            case 1: 
                escribir("Data Access Result: hardware-fault");
                break;
                
            case 2: 
                escribir("Data Access Result: temporary-failure");
                break;
                
            case 3: 
                escribir("Data Access Result: read-write-denied");
                break;

            case 4: 
                escribir("Data Access Result: object-undefined");
                break;
            
            case 9: 
                escribir("Data Access Result: object-class-inconsistent");
                break;
            
            case 11: 
                escribir("Data Access Result: object-unavailable");
                break;
            
            case 12: 
                escribir("Data Access Result: type-unmatched");
                break;
            
            case 13: 
                escribir("Data Access Result: scope-of-access-violated");
                break;
             
            case 14: 
                escribir("Data Access Result: data-block-unavailable");
                break;
            
            case 15: 
                escribir("Data Access Result: long-get-aborted");
                break;
            
            case 16: 
                escribir("Data Access Result: no-long-get-in-progress");
                break;
            
            case 17: 
                escribir("Data Access Result: long-set-aborted");
                break;
            
            case 18: 
                escribir("Data Access Result: no-long-set-in-progress");
                break;
            
            case 19: 
                escribir("Data Access Result: data-block-number-invalid");
                break;
            
            case 250: 
                escribir("Data Access Result: other-reason");
                break;
               
            default: // 
                escribir("Data Access Result: NO DEFINIDO");
                break;
        }
    }        
    
    
    public Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }

    public Date getDCurrentDate() throws ParseException {
        return Date.from(ZonedDateTime.now(zid).toInstant());
    }

    private byte[] adecuarFechasFiltro(String iniDate, String finDate) throws ParseException {

        byte[] byteDates = new byte[25];
        GregorianCalendar fechaCalendario = new GregorianCalendar();
        fechaCalendario.setTime(new Timestamp(sdf.parse(iniDate).getTime()));
        int diaSemana = fechaCalendario.get(Calendar.DAY_OF_WEEK);
        String lfecha = Integer.toHexString(Integer.parseInt(iniDate.substring(0, 4)));
        while (lfecha.length() < 4) {
            lfecha = "0" + lfecha;
        }
        //Bytes para ubicación de la fecha y hora
        byteDates[0] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
        byteDates[1] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
        byteDates[2] = (byte) (Integer.parseInt(iniDate.substring(4, 6)) & 0xFF);// mes 
        byteDates[3] = (byte) (Integer.parseInt(iniDate.substring(6, 8)) & 0xFF);//dia
        byteDates[4] = (byte) (((diaSemana - 1) == 0 ? 7 : (diaSemana - 1)) & 0xFF);// dia de la semana
        byteDates[5] = (byte) (Integer.parseInt(iniDate.substring(8, 10)) & 0xFF); // Hora 
        byteDates[6] = (byte) (Integer.parseInt(iniDate.substring(10, 12)) & 0xFF); // min
        byteDates[7] = (byte) (Integer.parseInt(iniDate.substring(12, 14)) & 0xFF); // seg
        byteDates[8] = (byte) 0xFF; // centesimas
        byteDates[9] = (byte) 0x80;
        byteDates[10] = (byte) 0x00; // desviacion 2 bytes
        byteDates[11] = (byte) 0x00; // status              

        fechaCalendario.setTime(new Timestamp(sdf.parse(finDate).getTime()));
        diaSemana = fechaCalendario.get(Calendar.DAY_OF_WEEK);
        lfecha = Integer.toHexString(Integer.parseInt(finDate.substring(0, 4)));
        while (lfecha.length() < 4) {
            lfecha = "0" + lfecha;
        }

        byteDates[12] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
        byteDates[13] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
        byteDates[14] = (byte) (Integer.parseInt(finDate.substring(4, 6)) & 0xFF);// mes 
        byteDates[15] = (byte) (Integer.parseInt(finDate.substring(6, 8)) & 0xFF);//dia
        byteDates[16] = (byte) (((diaSemana - 1) == 0 ? 7 : (diaSemana - 1)) & 0xFF);// dia de la semana
        byteDates[17] = (byte) (Integer.parseInt(finDate.substring(8, 10)) & 0xFF); // Hora 
        byteDates[18] = (byte) (Integer.parseInt(finDate.substring(10, 12)) & 0xFF); // min
        byteDates[19] = (byte) (Integer.parseInt(finDate.substring(12, 14)) & 0xFF); // seg
        byteDates[20] = (byte) 0xFF; // centesimas
        byteDates[21] = (byte) 0x80;
        byteDates[22] = (byte) 0x00; // desviacion 2 bytes
        byteDates[23] = (byte) 0x00; // status

        return byteDates;
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
        log.setNreintentos(reintentosUtilizados);
        log.setVccoduser(usuario);
        log.setLexito(lexito);
        cp.saveLogCall(log, null);
    }
    
    private void handleReenvio(String msg) {
        if (ReintentoFRMR <= numeroReintentos) {
            ReintentoFRMR ++;            
            enviaTrama2(ultimatramaEnviada, "=> " + msg);
        } else {
            ReintentoFRMR = 0;            
            cerrarPuerto(true);
            escribir("Reintentos de reenvios agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Reintentos de reenvios agotados");
            cerrarLog("Reintentos de reenvios agotados", false);
            leer = false;
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
        byte[] trama = tramastcum.getFRMR();
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcI(trama, numBytesDir);
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
