package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaElgama300;
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
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static protocolos.LeerRemotoTCP_ZMG310.calcularnuevocrcI;
import static protocolos.LeerRemotoTCP_ZMG310.calculoFCS;
import static protocolos.LeerRemotoTCP_ZMG310.validacionCRCHCS;

/**
 *
 * @author Metrolink
 */
public class LeerRemotoTCPElgama300 extends Thread {

    int bytesRecortados = 0;
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    String seriemedidor = "";
    String datoserial = "";
    InputStream input;
    OutputStream output;
    long tiempo = 500;
    String cadenahex = "";
    TramasRemotaElgama300 tramasGama300 = new TramasRemotaElgama300();
    boolean aviso = false;
    String password = "";
    String password2 = "";
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
    int ReintentoFRMR = 0; // vic 05-07-19
    int ReinicioFRMR = 0; // vic 05-07-19
    int velocidadPuerto;
    long timeout;
    int ndias;
    Date fechaActual;
    //*** Conf Hora***///
    Timestamp time = null; //tiempo de NTP
    Timestamp tsfechaactual;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    private long desfase;
    long ndesfasepermitido = 7200;
    boolean solicitar; //variable de control de la sync
    boolean portconect = false;
    Thread tEscritura = null;
    Thread tReinicio = null;
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
    boolean linfoperfil = false;
    boolean lconstants = false;
    boolean lpowerLost = false;
    boolean lperfilcarga = false;
    boolean lterminar = false;
    boolean lReset = false;
    boolean lfechasync = false;
    boolean enviando = false;
    boolean reenviando = false;
    boolean resetRetransmision = false;
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
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    public boolean complemento = false;
    public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
    public boolean eventocompleto = true;
    int reinicio = 0;
    int ns = 0;
    int nr = 0;
    int nrEsperado = 0;
    int nsEsperado = 0;

    private ZoneId zid;
    private static String zona;
    boolean ultimbloquePerfil = false;
    Vector<String> vPerfilCarga;
    Vector<String> vEventos;
    Vector<String> infoPerfil;
    ArrayList<String> obis;
    HashMap<String, String> shortNames = new HashMap<>();
    ArrayList<String> conskePerfil;
    ArrayList<String> clase;
    ArrayList<String> unidad;
    boolean constanteOk = false;
    private int periodoIntegracion = 15;
    private boolean primerbloque;
    private boolean dataBlockResult = false;
    private boolean lastDataBlock = false;
    private boolean nextFrame = false;
    private byte[] blockNumber = new byte[2];
    byte users[] = {(byte) 0x41};//03-05
    private int numBytesDir = 1;
    private int dirCliente;
    private int indxuser = 0;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    Abortar objabortar;
    private String usuario = "admin";
    List<String> listEventos;
    List<String> listObjetos;
    ERegistroEvento regEvento;
    public short pila[] = new short[10];
    public int i1, j1, l1;
    int indexConstant = 0;

    //datos para el perfil
    SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Electura lec;
    public int indicecanal = 0;
    public int indxlec = 0; //vic octubre
    public boolean primerintervalo; //vic octubre
    Timestamp fprimerintervalo;// vic octubre
    Date fechaEvento = null;
    Timestamp fechaintervalo = null;
    Timestamp ultimoIntervalo = null;
    Timestamp fechaCero = null;
    EConstanteKE econske = null;
    String canal = "";
    Vector<Electura> vlec;
    Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    ArrayList<EtipoCanal> vtipocanal;
    ArrayList<Double> lecgama300 = new ArrayList<>(); //= new double[lconske.size()]; // vic octubre
    private final Object monitor = new Object();
    private final String label = "LeerTCPGAMA300";
   private boolean tramaOK = false;

    public LeerRemotoTCPElgama300(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) {
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
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            password2 = med.getPassword2();
            timeout = 1000 * med.getTimeout();
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "0" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "0" : med.getDireccionLogica()));
            numBytesDir = Integer.parseInt((med.getBytesdireccion() == null ? "4" : med.getBytesdireccion()));
            dirCliente = Integer.parseInt((med.getDireccionCliente() == null ? "16" : med.getDireccionCliente()));
            users[0] = (byte) ((dirCliente * 2) + 1);
            lconske = cp.buscarConstantesKeLong(med.getnSerie());//se toman los valores de las constantes 
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            shortNames.put("0108", "0110");
            shortNames.put("0208", "0210");
            shortNames.put("0308", "0310");
            shortNames.put("0408", "0410");
            shortNames.put("0F08", "0010");
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
                }
                if (!socket.isClosed() && uncomplete) {
                    reenviando = true;
                    enviando = false;
                    monitor.notifyAll();
                    return;
                }
            }

            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramasGama300.encode(auxBuffer, idxFrame);

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
            } else if (lserialnumber) {
                revisarSerial(vectorhex);
            } else if (lfechaactual) {
                revisarFecAct(vectorhex);
            } else if (lpowerLost) {
                revisarEventos(vectorhex);
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
            } else if (lterminar) { //cerrar puertos
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
            //VIC - Modo E
            while (i1 < trama.length) {
                while (pila[0] > 0) {
                    if (clasificarTrama(trama)) {
                        almacenar = false;
                        if (j1 == 10) {
                            //System.out.println("Error en la Pila de datos");
                            //System.out.println("----------------------------");
                            correcto = false;
                            break;
                        }
                        if (revisarIndice(i1, trama.length)) {

                            if (Short.parseShort(trama[i1], 16) == 1 || Short.parseShort(trama[i1], 16) == 2 || Integer.parseInt(trama[i1 + 1], 16) >= 128) {
                                revisar = false;
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
                                k = (++k) % 10;
                                pila[j1 - 1]--;
                                l1++;
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
                            //VIC Modo E
                            if (j1 == 1) {
                                almacenar = true;
                                if (pila[j1 - 1] != 0) {
                                    if (!operaReadResponse(trama)) {
                                        correcto = false;
                                        break;
                                    }
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
            //System.out.println("termina interpretacion");
        } else {
            correcto = false;
            //System.out.println("clasificar trama " + false);
        }

        if (correcto) {
            //System.out.println("Proceso completado sin errores\n");
//            resetRetransmision=true;
            if (resetRetransmision) {
                reintentoconexion--; //para no agotar las retransmisiones dado que es un error del medidor
                resetRetransmision = false;
            }
        } else {
            //System.out.println("Proceso completado con errores\n");
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

    public boolean clasificarTrama(String[] trama) {
        if (i1 + 2 < trama.length) {
            if ("E6E700".equals(trama[i1] + trama[i1 + 1] + trama[i1 + 2])) {
                //System.out.println("LLC Command Response");
                if (i1 + 3 < trama.length) {
                    i1 = i1 + 3;
                    switch (Short.parseShort(trama[i1], 16)) {
                        case 12: {
                            //System.out.println("Read Response \n----------------------------");
                            if (i1 + 3 < trama.length) {
                                i1 = i1 + 1;//antes 3
                            } else {
                                //System.out.println("----------------------------");
                                //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                //System.out.println("----------------------------");
                                return false;
                            }
                            //System.out.println("j1: " + j1);
                            if (j1 == 0) {
                                pila[j1] = 1;
                                j1++;
                                //System.out.println("pila[0]: " + pila[0]);
                                //System.out.println("j1: " + j1);
                            }
                            //System.out.println("j1: " + j1);
                            if (j1 == 1) {
//                                        VIC - Modo E
                                pila[j1] = Short.parseShort(trama[i1], 16);
                                j1++;
                                i1++;
                                //System.out.println("pila[1]: " + pila[1]);
                                //System.out.println("j1: " + j1);
                                //System.out.println("i1: " + i1);
                            }

                            return operaReadResponse(trama);
                        }
                        case 97: {
                            //System.out.println("AARE APDU (No esta realizado aun)\n----------------------------");
                            return false;
                        }
                        case 196: {
                            switch (Short.parseShort(trama[i1 + 1], 16)) {
                                case 1: {
                                    //System.out.println("Get Normal Response \n----------------------------");
                                    if (i1 + 3 < trama.length) {
                                        i1 = i1 + 3;
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
                                        i1 = i1 + 12;
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

        return true;
    }

    public boolean operaReadResponse(String trama[]) {
//        if (j1 == 1 && revisarIndice(i1, trama.length)) {
        if (revisarIndice(i1, trama.length)) {
            switch (Short.parseShort(trama[i1], 16)) {
                case 0: {
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

    public boolean procesaDato(String dato, int tipoTrama) {

        switch (tipoTrama) {
            case 2: {
                dato = Hex2ASCII(dato);
                //System.out.println(dato);
                datoserial = dato;
                break;
            }
            case 3: {
                try {
                    fechaActual = fecha.parse(Hex2Date(dato));
                    dato = sdf3.format(fechaActual);

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                //System.out.println("Fecha actual " + dato);
                break;
            }
            case 4: {
                try {
                    periodoIntegracion = Integer.parseInt(dato, 16) / 60;
                    //System.out.println("\nPeriodo de Integración " + periodoIntegracion);
                    intervalo = periodoIntegracion;

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                break;
            }

            case 5: {
                if (l1 == 0) {
                    clase.add(dato);
                    //System.out.println("\nClase " + dato);
                }
                if (l1 == 1) {
                    obis.add(dato);
                    //System.out.println("\nObis " + dato);
                }
                //l1++;
                break;
            }
            case 20: {
                if (l1 == 0) {
                    conskePerfil.add(dato);
                    //System.out.println("\nEscala " + dato);
                }
                if (l1 == 1) {
                    unidad.add(dato);
                    //System.out.println("\nUnidad " + dato);
                    constanteOk = true;
                }
                break;
            }
            case 22: {

                if (l1 == 0) {
                    indicecanal = 0;
                    indxlec = 0; //vic octubre
                    try {
                        //System.out.println("dato original " + dato);
                        dato = sdf2.format(fecha.parse(Hex2Date(dato)));
                        //System.out.println("Fecha intervalo " + dato);
                        fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                        if (fechaintervalo.getMinutes() % intervalo != 0) {
                            dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
                            fechaintervalo = new Timestamp(sdf2.parse(dato).getTime() + (60000 * intervalo)); //se aproxima al intervalo anterior y luego se le suma uno
                            //System.out.println("Fecha intervalo aproximado" + dato);
                        }
                        if (ultimoIntervalo == null) {
                            ultimoIntervalo = fechaintervalo;
                        }

                        if (primerintervalo) { //vic octubre
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
                                        //System.out.println("fecha intervalo en 0 " + fechaCero);
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
                                                        //vic Se guardan los ceros en las fechas anteriores y no se guarda lecaux
//                                                        lec.setLecaux(lec.lec);
                                                        if (!fprimerintervalo.equals(fechaintervalo)) {
                                                            lec.fecha = new Timestamp(fechaCero.getTime() - (60000 * intervalo));
//                                                            lec.lec = lec.lec-lecgama300.get(indxlec);
                                                            vlec.add(lec);
//                                                            lecgama300.set(indxlec,lec.lecaux);
                                                        } else {
//                                                            lecgama300.add(indxlec,lec.lecaux);
                                                        }
//                                                        indxlec++;
                                                    } else {
                                                        //System.out.println("Constante");
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
                    try {
                        dato = String.valueOf(Long.parseLong(dato, 16));
                        //System.out.println(dato);
                        econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(indicecanal), 16));
                        if (econske != null) {
                            canal = "";
                            for (EtipoCanal et : vtipocanal) {
                                if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(indicecanal), 16)) {
                                    canal = et.getUnidad();
                                    //System.out.println("Canal " + et.getCanal() + " Unidad " + canal);
                                    break;
                                }
                            }
                            if (fechaintervalo != null && canal.length() > 0) {
                                if (constanteOk) {
                                    //System.out.println("valor " + trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                    //System.out.println("Unidad " + unidad.get(indicecanal));
                                    ////System.out.println("Complemento a 2 ="+ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF)));
                                    lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                                    if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                        lec.lec = lec.lec / 1000.0; // vic 27-09-19 - debido al soft propietario debe ser diez mil
                                    }
                                    //vic octubre
                                    lec.setLecaux(lec.lec);
                                    if (!fprimerintervalo.equals(fechaintervalo)) {
                                        lec.fecha = new Timestamp(fechaintervalo.getTime() - (60000 * intervalo));
                                        lec.lec = lec.lec - lecgama300.get(indxlec);
                                        vlec.add(lec);
                                        lecgama300.set(indxlec, lec.lecaux);
                                    } else {
                                        lecgama300.add(indxlec, lec.lecaux);
                                    }

                                    indxlec++;

                                } else { //vic 26-04
                                    //System.out.println("Sin Scaler-unit");
                                    //System.out.println("\nConversion desde base de datos: ");
                                    //System.out.println("valor " + trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                    //System.out.println("Unidad " + canal);
                                    lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                                    if (canal.contains("k") || canal.contains("K")) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                        lec.lec = lec.lec / 10000.0; // vic 27-09-19 - debido al soft propietario debe ser diez mil
                                    }
                                    //vic octubre
                                    lec.setLecaux(lec.lec);
                                    if (!fprimerintervalo.equals(fechaintervalo)) {
                                        lec.fecha = new Timestamp(fechaintervalo.getTime() - (60000 * intervalo));
                                        lec.lec = lec.lec - lecgama300.get(indxlec);
                                        vlec.add(lec);
////                                            System.out.println("no es primero");

                                        lecgama300.set(indxlec, lec.lecaux);
                                    } else {
                                        lecgama300.add(indxlec, lec.lecaux);

                                    }
                                    indxlec++;
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
                if (l1 == 0) {
                    try {
                        if (regEvento == null) {
                            regEvento = new ERegistroEvento();
                            regEvento.vcserie = seriemedidor;
                            regEvento.vctipo = "0001";
                        }

                        fechaEvento = fecha.parse(Hex2Date(dato));
//                        regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());

                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                } else if (l1 == 1) {
                    try {

                        if (dato.equals("01")) {
                            regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                        } else if (dato.equals("00")) {
                            regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
                        }

//                        fechaEvento = fecha.parse(Hex2Date(dato));
//                        regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
                        if (regEvento.vcfechacorte != null && regEvento.vcfechareconexion != null) {

                            //System.out.println(fecha.format(regEvento.vcfechacorte) + " - " + fecha.format(regEvento.vcfechareconexion));
                            regEvento.vcserie = seriemedidor;
                            regEvento.vctipo = "0001";                            
                            cp.actualizaEvento(regEvento, null);
                            regEvento = new ERegistroEvento();
                            regEvento.vcserie = seriemedidor;
                            regEvento.vctipo = "0001";
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }

                //l1++;
                break;
            }
            case 26: {
                if (l1 == 0) {

                    //System.out.println("\ndato1 " + dato);
                }
                if (l1 == 1) {

                    //System.out.println("\ndato2 " + dato);

                }
                if (l1 == 2) {

                    //System.out.println("\ndato3 " + dato);

                } else {
                    //System.out.println("\ndatoNN " + dato);
                }
                break;
            }
            default: {
                //System.out.println("No es posible interpretar los datos");
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
            //System.out.println("----------------------------\nTrama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados\n----------------------------");
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
        rxFrameChecking(vectorhex, "SNRM");
        if (tramaOK) {
            if (vectorhex[4 + numBytesDir].equals("73") || vectorhex[4 + numBytesDir].equals("63")) {
                ns = 0;
                nr = 0;
                escribir("NsPc " + ns + " NrPc " + nr);
                nrEsperado = ns + 1;
                nsEsperado = 0;
                lSNRMUA = false;
                lARRQ = true;
                byte trama[] = crearAARQ(tramasGama300.getAarq(), (lconfhora ? password2 : password)); //AARQ
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcI(trama, numBytesDir);
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "AARQ");
            } else {
                reiniciaComunicacion(true);
            }
        }
    }

    public void revisarAARQ(String[] vectorhex) {
        String peticion = "AARQ";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            if (vectorhex[27 + numBytesDir].equals("00") && vectorhex[34 + numBytesDir].equals("00")) {
                lARRQ = false;
                lserialnumber = true;
                byte trama[] = tramasGama300.getSerial();//                                            
                trama = asignaDirecciones(trama);
                trama[4 + numBytesDir] = I_CTRL(nr, ns);
                trama = calcularnuevocrcI(trama, numBytesDir);//                                            
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "=> Numero de Serial");
            } else {
                //reiniciamos
                byte trama[] = tramasGama300.getLogout();
                trama = asignaDirecciones(trama);
                indxuser++;
                lARRQ = false;
                if (vectorhex[34 + numBytesDir].equals("0D")) {
                    if (indxuser < users.length) {
                        escribir("Error de autenticacion");
                        lterminar = true;
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "");
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de autenticacion");
                        cerrarPuerto(true);
                        escribir("Desconexion - Error de autenticacion");
                        cerrarLog("Desconexion Error de autenticacionn", false);
                        leer = false;
                    }
                } else {
                    if (indxuser < users.length) {
                        escribir("Fallo interpretacion de AARE");
                        lReset = true;
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "Logout");
                    } else {
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion - Fallo en interpretacion de AARE");
                        cerrarPuerto(true);
                        escribir("Desconexion - Fallo en interpretacion de AARE");
                        cerrarLog("Desconexion Error de autenticacion", false);
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
            String[] vectorData = sacarDatos(vectorhex, 7 + numBytesDir); //VIC se debe cambiar a 11
            try {
                boolean continuar;
                interpretaDatos(vectorData, 2);
                datoserial = "" + Long.parseLong(datoserial);
                if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {//VIC 
                    escribir("Numero serial " + datoserial);
                    continuar = true;
                } else {
                    escribir("Numero serial incorrecto");
                    continuar = false;
                }
                if (continuar) {
                    lfechaactual = true;
                    byte trama[] = tramasGama300.getFechaactual();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Solicitud de fecha actual");
                } else {
                    escribir("Numero de serial invalido");
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                    byte trama[] = tramasGama300.getLogout();
                    lReset = true;
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                }

            } catch (Exception e) {
                escribir("Error al validar el numero serial");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de serial");
                byte trama[] = tramasGama300.getLogout();
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
            lfechaactual = false;
            String[] data = sacarDatos(vectorhex, 7 + numBytesDir);//VIC
            try {
                if (interpretaDatos(data, 3)) {
                    if (lconfhora) {
                        //**** conf hora
                        try {
                            byte trama[] = tramasGama300.getConfhora();
                            time = obtenerHora();
                            String fechaL = sdf.format(new Date(time.getTime()));
                            String lfecha = Integer.toHexString(Integer.parseInt(fechaL.substring(0, 4)));
                            while (lfecha.length() < 4) {
                                lfecha = "0" + lfecha;
                            }
                            //VIC se debe suma uno a cada uno de 27 a 38 no 26 a 37
                            trama[22] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            trama[23] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
                            trama[24] = (byte) (Integer.parseInt(fechaL.substring(4, 6)) & 0xFF);// mes 
                            trama[25] = (byte) (Integer.parseInt(fechaL.substring(6, 8)) & 0xFF);//dia
                            trama[26] = (byte) (((time.getDay()) == 0 ? 7 : (time.getDay())) & 0xFF);// dia de la semana
                            //trama[33] = (byte) (((time.getDay()-1)==0? 7:(time.getDay()-1)) & 0xFF);// dia de la semana
                            trama[27] = (byte) (Integer.parseInt(fechaL.substring(8, 10)) & 0xFF); // hora 
                            trama[28] = (byte) (Integer.parseInt(fechaL.substring(10, 12)) & 0xFF); // min
                            trama[29] = (byte) (Integer.parseInt(fechaL.substring(12, 14)) & 0xFF); // seg
                            trama[30] = (byte) 0x00;//FF; // centesimas
                            trama[31] = (byte) 0x00;//80;
                            trama[32] = (byte) 0x00; // desviacion 2 bytes
                            trama[33] = (byte) 0xFF;//00; // status
                            lfechaactual2 = false;
                            lfechasync = true;
                            trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                            trama = asignaDirecciones(trama);
                            trama = calcularnuevocrcI(trama, numBytesDir);
                            ultimatramaEnviada = trama;
                            enviaTrama2(trama, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));
                        } catch (Exception e) {
                            escribir("Error al generar peticion de sincronizacion");
                            escribir(getErrorString(e.getStackTrace(), 3));
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Error al generar peticion de sincronizacion");
                            byte trama[] = tramasGama300.getLogout();
                            lReset = true;
                            trama = calcularnuevocrcRR(trama);
                            enviaTrama2(trama, "Logout");
                        }
                    } else if (lperfil & eventocompleto) {
                        lperiodoIntegracion = true;
                        byte trama[] = tramasGama300.getPeriodoint();
                        trama = asignaDirecciones(trama);
                        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                        trama = calcularnuevocrcI(trama, numBytesDir);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "=> Solicitud de periodo de integracion");
                    } else if (leventos) {
                        eventocompleto = false;
                        lpowerLost = true;
                        listEventos = new ArrayList<>();
                        byte trama[] = tramasGama300.getPowerLost();
                        trama = asignaDirecciones(trama);
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
                    byte trama[] = tramasGama300.getLogout();
                    lReset = true;
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                }
            } catch (Exception e) {
                escribir("Error interpretacion fecha");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Error interpretacion fecha");
                byte trama[] = tramasGama300.getLogout();
                lReset = true;
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarPeriodoInt(String[] vectorhex) {
        String peticion = "Periodo Integracion";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lperiodoIntegracion = false;
            String[] data = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            if (interpretaDatos(data, 4)) {
                linfoperfil = true;
                obis = new ArrayList<>();
                conskePerfil = new ArrayList<>();
                unidad = new ArrayList<>();
                clase = new ArrayList<>();
                infoPerfil = new Vector<>();
                byte trama[] = tramasGama300.getConfperfil();
                trama = asignaDirecciones(trama);
                trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                trama = calcularnuevocrcI(trama, numBytesDir);
                ultimatramaEnviada = trama;
                enviaTrama2(trama, "=> Solicitud de configuracion del perfil de carga");
            } else {
                escribir("Negacion de peticion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion negacion de peticion");
                byte trama[] = tramasGama300.getLogout();
                lReset = true;
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

      public void revisarInfoPerfil(String[] vectorhex) {
        String peticion = "Canales";
        rxFrameChecking(vectorhex, peticion);
        if (tramaOK) {//tiene cabecera                    
            escribir("NrM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1));
            if (nrEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1)) {                
                String vdata[] = null;
                nr++;
                if (nr > 7) {
                    nr = 0;
                }
                nsEsperado++;
                if (nsEsperado > 7) {
                    nsEsperado = 0;
                }
                if (primerbloque) {
                    dataBlockResult = (vectorhex[12 + numBytesDir].equals("02"));
                    nextFrame = ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08);
                    if (dataBlockResult) {
                        ns++;
                        if (ns > 7) {
                            ns = 0;
                        }

                        nrEsperado++;
                        if (nrEsperado > 7) {
                            nrEsperado = 0;
                        }
                        lastDataBlock = vectorhex[13 + numBytesDir].equals("01");
                        blockNumber[0] = Byte.parseByte(vectorhex[14 + numBytesDir], 16);
                        blockNumber[1] = Byte.parseByte(vectorhex[15 + numBytesDir], 16);
                        String dataLLC[] = {"E6", "E7", "00", "0C"};
                        vdata = concatFrames(dataLLC, sacarDatos(vectorhex, 18 + numBytesDir));
                    } else {
                        vdata = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
                    }
                    primerbloque = false;
                    escribir("NsPc " + ns + " NrPc " + nr);
                } else {
                    if (dataBlockResult && !nextFrame) {
                        ns++;
                        if (ns > 7) {
                            ns = 0;
                        }
                        nrEsperado++;
                        if (nrEsperado > 7) {
                            nrEsperado = 0;
                        }
                        lastDataBlock = vectorhex[13 + numBytesDir].equals("01");
                        blockNumber[0] = (byte) (Integer.parseInt(vectorhex[14 + numBytesDir], 16) & 0xFF);
                        blockNumber[1] = (byte) (Integer.parseInt(vectorhex[15 + numBytesDir], 16) & 0xFF);
                        vdata = sacarDatos(vectorhex, (lastDataBlock ? 17 : 18) + numBytesDir);
                    } else {
                        nextFrame = ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08);
                        vdata = sacarDatos(vectorhex, 7 + numBytesDir);
                    }
                    escribir("NsPc " + ns + " NrPc " + nr);
                }
                infoPerfil.addAll(Arrays.asList(vdata));
                if (nextFrame) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                    byte trama[] = tramasGama300.getRR();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = RR_CTRL(nr); //cuando envia RR aumenta nr, pero mantiene a ns
                    trama = calcularnuevocrcRR(trama);
                    ultimatramaEnviada = trama;
                    peticion = "RR";
                    enviaTrama2(trama, "=> Envia " + peticion);
                } else if (dataBlockResult) {
                    if (lastDataBlock) {
                        ns++;
                        if (ns > 7) {
                            ns = 0;
                        }
                        nrEsperado++;
                        if (nrEsperado > 7) {
                            nrEsperado = 0;
                        }
                        finalizaInfoPerfil();
                    } else {
                        byte trama[] = tramasGama300.getTreqNextBlock(blockNumber);
                        trama = asignaDirecciones(trama);
                        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                        trama = calcularnuevocrcI(trama, numBytesDir);
                        ultimatramaEnviada = trama;
                        peticion = "Req Next Data Block";
                        enviaTrama2(trama, "=> Envia " + peticion);
                    }
                } else {
                    ns++;
                    if (ns > 7) {
                        ns = 0;
                    }
                    nrEsperado++;
                    if (nrEsperado > 7) {
                        nrEsperado = 0;
                    }
                    finalizaInfoPerfil();
                }
            } else {
                escribir("\nNo son los ns y nr esperados");
                handleReenvio(peticion);
            }
        } 
    }

    public boolean enviaConstant() {
        //peticion scaler-unit       
        String sn = null;
        boolean enviar = true;
        lconstants = true;
        escribir("Clase " + indexConstant + ": " + clase.get(indexConstant));
        while (true) {
            if (clase.get(indexConstant).equals("0003")) {
                sn = shortNames.get(obis.get(indexConstant).substring(4, 8));
                escribir("Short Name: " + sn);
                if (sn != null) {
                    break;
                }
            }
            conskePerfil.add("0");
            unidad.add("0");
            indexConstant++;
            if (indexConstant > (clase.size() - 1)) {
                enviar = false;
                break;
            }
        }
        if (enviar) {
            byte[] trama = construirConstant(sn);
            ultimatramaEnviada = trama;            
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(),label, "Envío petición - Constantes " + (indexConstant + 1));
            enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
            return true;
        } else {
            return false;
        }
    }

    //finaliza revisar lista
    public void revisarConstants(String[] vectorhex) {
        Boolean keep = true;
        String peticion = "Configuracion Perfil Carga";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            String vdata[] = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            conskePerfil.add(vdata[vdata.length - 3]);
            unidad.add(vdata[vdata.length - 1]);
            constanteOk = true;
            indexConstant++;
            while (keep) {
                if (indexConstant > (obis.size() - 1)) {
                    time = this.obtenerHora();
                    escribir("Sincronizando hora con Equipo Local ");
                    deltatimesync1 = this.obtenerHora();
                    escribir(" Vector constantes " + conskePerfil.toString());
                    lconstants = false;
                    lfechaactual2 = true;
                    byte trama[] = tramasGama300.getFechaactual();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;
                    enviaTrama2(trama, "=> Solicitud de fecha actual");
                    break;
                } else {
                    //se solicita el siguiente OBIS
                    keep = !enviaConstant(); //no se lleva indexConstant a cero
                }
            }
        }
    }

    public void revisarFecAct2(String[] vectorhex) throws ParseException {
        String peticion = "Fecha Actual 2";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lfechaactual2 = false;
            deltatimesync2 = this.obtenerHora();
            String[] dataTrama = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
            if (interpretaDatos(dataTrama, 3)) {
                escribir("Fecha actual de medidor " + fechaActual);
                deltatimesync2 = this.obtenerHora();
                escribir("Fecha actual de medidor " + fechaActual);                
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
                        byte trama[] = tramasGama300.getPerfilcarga();
                        trama = asignaDirecciones(trama);
                        //byte 47 fecha ini
                        Date fechaUltLec = new Date(med.getFecha().getTime());
                        String fechaI = sdf.format(this.getDSpecificDate(false, 1, "H", fechaUltLec));
                        escribir("Fecha inicio peticion pefil: " + sdfarchivo.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                        String lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(0, 4)));
                        while (lfecha.length() < 4) {
                            lfecha = "0" + lfecha;
                        }
                        //VIC perfilc 1
                        trama[38 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        trama[39 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(4, 6)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        trama[40 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaI.substring(6, 8)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        trama[41 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        //vic octubre 29-10-19
                        trama[43 + numBytesDir] = (byte) (Integer.parseInt(fechaI.substring(8, 10)) & 0xFF); // hora 
                        trama[44 + numBytesDir] = (byte) (Integer.parseInt(fechaI.substring(10, 12)) & 0xFF); // min
                        trama[45 + numBytesDir] = (byte) (Integer.parseInt(fechaI.substring(12, 14)) & 0xFF); // seg
                        String fechaF;
                        if (ndias > 30) {
                            fechaF = this.getSpecificDate(sdf, true, 30, "D", fechaI);
                            escribir("Fecha final de perfil de carga " + this.getSpecificDate(sdf3, false, ndias - 31, "D"));
                        } else {
                            fechaF = sdf.format(this.getDCurrentDate());
                            escribir("Fecha final de perfil de carga " + sdf3.format(this.getDCurrentDate()));
                        }
                        lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(0, 4)));
                        while (lfecha.length() < 4) {
                            lfecha = "0" + lfecha;
                        }
                        //VIC perfilc 2
                        trama[52 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        trama[53 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(4, 6)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        trama[54 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                        lfecha = Integer.toHexString(Integer.parseInt(fechaF.substring(6, 8)));
                        while (lfecha.length() < 2) {
                            lfecha = "0" + lfecha;
                        }
                        trama[55 + numBytesDir] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);

                        //vic octubre 29-10-19
                        trama[57 + numBytesDir] = (byte) (Integer.parseInt(fechaF.substring(8, 10)) & 0xFF); // hora 
                        trama[58 + numBytesDir] = (byte) (Integer.parseInt(fechaF.substring(10, 12)) & 0xFF); // min
                        trama[59 + numBytesDir] = (byte) (Integer.parseInt(fechaF.substring(12, 14)) & 0xFF); // seg

                        vPerfilCarga = new Vector<>();
                        primerbloque = true;
                        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                        trama = calcularnuevocrcI(trama, numBytesDir);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "Solicitud de pefil de carga");
                    } else {
                        escribir("Desfase horario no se solicitara el perfil de carga");
                        lfechaactual2 = false;
                        lterminar = true;
                        byte trama[] = tramasGama300.getLogout();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviaTrama2(trama, "Logout");
                    }
                }
            } else {
                escribir("Negacion de peticion");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion negacion de peticion");
                byte trama[] = tramasGama300.getLogout();
                lReset = true;
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            }
        }
    }

    public void revisarPerfil(String[] vectorhex) {
        String peticion = "Perfil de carga";
        rxFrameChecking(vectorhex, peticion);
        if (tramaOK) {//tiene cabecera        
            if ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x01) == 0x00) { //es informacion
                escribir("NrM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1));
                if (nrEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1)) {                    
                    lperfilcompleto = true;
                    nr++;
                    if (nr > 7) {
                        nr = 0;
                    }
                    nsEsperado++;
                    if (nsEsperado > 7) {
                        nsEsperado = 0;
                    }
                    String vdata[] = null;
                    if (primerbloque) {
                        dataBlockResult = (vectorhex[12 + numBytesDir].equals("02"));
                        nextFrame = ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08);
                        if (dataBlockResult) {
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }

                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            lastDataBlock = vectorhex[13 + numBytesDir].equals("01");
                            blockNumber[0] = Byte.parseByte(vectorhex[14 + numBytesDir], 16);
                            blockNumber[1] = Byte.parseByte(vectorhex[15 + numBytesDir], 16);
                            String dataLLC[] = {"E6", "E7", "00", "0C"};
                            vdata = concatFrames(dataLLC, sacarDatos(vectorhex, 18 + numBytesDir));
                            bytesRecortados = bytesRecortados + 18 + numBytesDir - 5;
                        } else {
                            ns++;
                            vdata = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
                            bytesRecortados = bytesRecortados + 7 + numBytesDir - 1;
                        }
                        primerbloque = false;
                        escribir("NsPc " + ns + " NrPc " + nr);
                    } else {
                        if (dataBlockResult && !nextFrame) {
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }
                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            lastDataBlock = vectorhex[13 + numBytesDir].equals("01");
                            blockNumber[0] = (byte) (Integer.parseInt(vectorhex[14 + numBytesDir], 16) & 0xFF);
                            blockNumber[1] = (byte) (Integer.parseInt(vectorhex[15 + numBytesDir], 16) & 0xFF);
                            vdata = sacarDatos(vectorhex, (lastDataBlock ? 17 : 18) + numBytesDir);
                            bytesRecortados = bytesRecortados + (lastDataBlock ? 17 : 18) + numBytesDir - 1;
                        } else {
                            nextFrame = ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08);
                            vdata = sacarDatos(vectorhex, 7 + numBytesDir);
                            bytesRecortados = bytesRecortados + 7 + numBytesDir - 1;
                        }
                        escribir("NsPc " + ns + " NrPc " + nr);
                    }
                    vPerfilCarga.addAll(Arrays.asList(vdata));

                    if (nextFrame) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                        byte trama[] = tramasGama300.getRR();
                        trama = asignaDirecciones(trama);
                        trama[4 + numBytesDir] = RR_CTRL(nr); //cuando envia RR aumenta nr, pero mantiene a ns
                        trama = calcularnuevocrcRR(trama);
                        ultimatramaEnviada = trama;
                        peticion = "RR";
                        enviaTrama2(trama, "=> Envia " + peticion);
                    } else if (dataBlockResult) {
                        if (lastDataBlock) {
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }
                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            finalizaPerfil();
                        } else {
                            byte trama[] = tramasGama300.getTreqNextBlock(blockNumber);
                            trama = asignaDirecciones(trama);
                            trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                            trama = calcularnuevocrcI(trama, numBytesDir);
                            ultimatramaEnviada = trama;
                            peticion = "Req Next Data Block";
                            enviaTrama2(trama, "=> Envia " + peticion);
                        }
                    } else {
                        ns++;
                        if (ns > 7) {
                            ns = 0;
                        }
                        nrEsperado++;
                        if (nrEsperado > 7) {
                            nrEsperado = 0;
                        }
                        finalizaPerfil();
                    }
                } else {
                    escribir("\nNo son los ns y nr esperados");
                    handleReenvio(peticion);
                }
            } else if ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0F) == 0x01) {//es rr
                lperfilcompleto = false;
                lperfilcarga = false;
                if (leventos) {
                    lpowerLost = true;
                    primerbloque = true;
                    listEventos = new ArrayList<>();
                    byte trama[] = tramasGama300.getPowerLost();
                    trama = asignaDirecciones(trama);
                    trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
                    trama = calcularnuevocrcI(trama, numBytesDir);
                    ultimatramaEnviada = trama;                    
                    enviaTrama2(trama, "=> Solicitud de eventos");
                } else {
                    lterminar = true;
                    byte trama[] = tramasGama300.getLogout();
                    asignaDirecciones(trama);
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                }
            } else {
                if (vectorhex[4 + numBytesDir].equals("73")) {
                    escribir("UA inesperado");
                    lterminar = true;
                    byte trama[] = tramasGama300.getLogout();
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                } else {
                    validarTipoTrama(vectorhex[4 + numBytesDir]);
                }
            }  
        }
    }

    public void revisarEventos(String[] vectorhex) throws ParseException {
        String peticion = "Eventos";
        rxFrameChecking(vectorhex, peticion);
        if (tramaOK) {//tiene cabecera        
            if ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x01) == 0x00) { //es informacion
                escribir("NrM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1));
                if (nrEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1)) {                    
                    nr++;
                    if (nr > 7) {
                        nr = 0;
                    }
                    nsEsperado++;
                    if (nsEsperado > 7) {
                        nsEsperado = 0;
                    }
                    String vdata[] = null;
                    if (primerbloque) {
                        dataBlockResult = (vectorhex[12 + numBytesDir].equals("02"));
                        nextFrame = ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08);
                        if (dataBlockResult) {
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }

                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            lastDataBlock = vectorhex[13 + numBytesDir].equals("01");
                            blockNumber[0] = Byte.parseByte(vectorhex[14 + numBytesDir], 16);
                            blockNumber[1] = Byte.parseByte(vectorhex[15 + numBytesDir], 16);
                            String dataLLC[] = {"E6", "E7", "00", "0C"};
                            vdata = concatFrames(dataLLC, sacarDatos(vectorhex, 18 + numBytesDir));
                        } else {
                            ns++;
                            vdata = sacarDatos(vectorhex, 7 + numBytesDir); //VIC
                        }
                        primerbloque = false;
                        escribir("NsPc " + ns + " NrPc " + nr);
                    } else {
                        if (dataBlockResult && !nextFrame) {
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }
                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            lastDataBlock = vectorhex[13 + numBytesDir].equals("01");
                            blockNumber[0] = (byte) (Integer.parseInt(vectorhex[14 + numBytesDir], 16) & 0xFF);
                            blockNumber[1] = (byte) (Integer.parseInt(vectorhex[15 + numBytesDir], 16) & 0xFF);
                            vdata = sacarDatos(vectorhex, (lastDataBlock ? 17 : 18) + numBytesDir);
                        } else {
                            nextFrame = ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08);
                            vdata = sacarDatos(vectorhex, 7 + numBytesDir);
                        }
                        escribir("NsPc " + ns + " NrPc " + nr);
                    }
                    listEventos.addAll(Arrays.asList(vdata));

                    if (nextFrame) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                        byte trama[] = tramasGama300.getRR();
                        trama = asignaDirecciones(trama);
                        trama[4 + numBytesDir] = RR_CTRL(nr); //cuando envia RR aumenta nr, pero mantiene a ns
                        trama = calcularnuevocrcRR(trama);
                        ultimatramaEnviada = trama;
                        peticion = "RR";
                        enviaTrama2(trama, "=> Envia " + peticion);
                    } else if (dataBlockResult) {
                        if (lastDataBlock) {
                            ns++;
                            if (ns > 7) {
                                ns = 0;
                            }
                            nrEsperado++;
                            if (nrEsperado > 7) {
                                nrEsperado = 0;
                            }
                            finalizaEventos();
                        } else {
                            byte trama[] = tramasGama300.getTreqNextBlock(blockNumber);
                            trama = asignaDirecciones(trama);
                            trama[4 + numBytesDir] = I_CTRL(nr, ns);
                            trama = calcularnuevocrcI(trama, numBytesDir);
                            ultimatramaEnviada = trama;
                            peticion = "Req Next Data Block";
                            enviaTrama2(trama, "=> Envia " + peticion);
                        }
                    } else {
                        ns++;
                        if (ns > 7) {
                            ns = 0;
                        }
                        nrEsperado++;
                        if (nrEsperado > 7) {
                            nrEsperado = 0;
                        }
                        finalizaEventos();
                    }
                } else {
                    escribir("\nNo son los ns y nr esperados");
                    handleReenvio(peticion);
                }
            } else if ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0F) == 0x01) {//es rr
                eventocompleto = false;
                lpowerLost = false;
                lterminar = true;
                byte trama[] = tramasGama300.getLogout();
                asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviaTrama2(trama, "Logout");
            } else {
                if (vectorhex[4 + numBytesDir].equals("73")) {
                    escribir("UA inesperado");
                    lterminar = true;
                    byte trama[] = tramasGama300.getLogout();
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcRR(trama);
                    enviaTrama2(trama, "Logout");
                } else {
                    validarTipoTrama(vectorhex[4 + numBytesDir]);
                }
            }
        }
    }

    public void revisarLogout(String[] vectorhex) {
        String peticion = "Logout";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        lterminar = false;
        cerrarPuerto(false);
        boolean l = false;
        if (lperfil) {

            if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                try {
                    procesaDatos();
                    cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                    String fechaultima = "";
                    fechaultima = sgflect.format(ultimoIntervalo);
                    cp.actualizaFechaLectura(seriemedidor, fechaultima);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                l = true;
            }
        }
        if (leventos) {
            if (listEventos != null && listEventos.size() > 0) {
                try {
                    procesaInfoEventos();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
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
            escribir("Estado Lectura Leido");
            cerrarLog("Leido", true);
        } else {
            escribir("Estado Lectura No leido");
            cerrarLog("No leido", false);
        }
        leer = false;

    }

    public void revisarReset(String[] vectorhex) {
        String peticion = "Logout Reset";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        lReset = false;
        reiniciaComunicacion(false);
    }

    public void revisarConfHora(String[] vectorhex) {
        String peticion = "Sincronizacion";
        rxFrameChecking(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            lfechasync = false;
            if (Integer.parseInt(vectorhex[13+numBytesDir], 16) == 0) {
                escribir("Se sincronizó la hora correctamente");
            } else {
                escribir("No fue posible sincronizar la hora");
            }
            lterminar = true;
            byte trama[] = tramasGama300.getLogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            enviaTrama2(trama, "");
        }
    }
    
    private void enviaFechaActual(boolean first) {                
        byte trama[] = tramasGama300.getFechaactual();
        //byte trama[] = tramasGama300.getGetRequestFechaActual();
        trama = asignaDirecciones(trama);
        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
        trama = calcularnuevocrcI(trama, numBytesDir);
        if (first) {
            lfechaactual = true;
        } else {
            lfechaactual2 = true;
        }        
        ultimatramaEnviada = trama;        
        enviaTrama2(trama, "=> Solicitud de fecha actual");
    }
    
    public void finalizaInfoPerfil() {
        procesaInfoPerfil();
        linfoperfil = false;
        escribir("Cantidad de canales internos del medidor: " + obis.size());
        escribir("Vector de canales internos del medidor: " + obis.toString());
        time = obtenerHora();
        escribir("Máx. desfase permitido: " + ndesfasepermitido);
        deltatimesync1 = obtenerHora();
        escribir("Vector constantes: " + conskePerfil.toString());
        enviaFechaActual(false);
        /*
        lconstants = true;
        indexConstant = 0;
        while ((obis.size() - 1) >= indexConstant && !clase.get(indexConstant).equals("0003")) {
            conskePerfil.add("0");
            unidad.add("0");
            indexConstant++;
        }        
        enviaConstant();
        */
    }
    
    public void finalizaPerfil() {
        ns++;
        if (ns > 7) {
            ns = 0;
        }
        nrEsperado++;
        if (nrEsperado > 7) {
            nrEsperado = 0;
        }
        lperfilcarga = false;
        lperfilcompleto = false;

        if (leventos) {
            eventocompleto = false;
            lpowerLost = true;
            primerbloque = true;
            listEventos = new ArrayList<>();
            byte trama[] = tramasGama300.getPowerLost();
            trama = asignaDirecciones(trama);
            trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC
            trama = calcularnuevocrcI(trama, numBytesDir);
            ultimatramaEnviada = trama;
            enviaTrama2(trama, "=> Solicitud de eventos");
        } else {
            lterminar = true;
            byte trama[] = tramasGama300.getLogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            enviaTrama2(trama, "Logout");
        }
    }

    public void finalizaEventos() {
        eventocompleto = true;
        lpowerLost = false;
        lterminar = true;
        byte trama[] = tramasGama300.getLogout();
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcRR(trama);
        enviaTrama2(trama, "Logout");
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

                lSNRMUA = true;
                byte[] trama = tramasGama300.getSnrm();
                trama = asignaDirecciones(trama);
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
            byte trama[] = tramasGama300.getLogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            escribir("Envia Logout sin espera de respuesta");
            escribir("=> " + tramasGama300.encode(trama, trama.length));
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
                                escribir("=> " + tramasGama300.encode(trama, trama.length));
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
                            cerrarPuerto(true);
                            monitor.notifyAll();
                            if (lperfilcarga) {
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

    private void procesaInfoPerfil() {
        String[] data = infoPerfil.toArray(new String[infoPerfil.size()]);
        interpretaDatos(data, 5);
    }

    private void procesaDatos() {
        ultimoIntervalo = null;
        primerintervalo = true; // vic octubre
        String[] data = vPerfilCarga.toArray(new String[vPerfilCarga.size()]);
        interpretaDatos(data, 22);
    }

    private String[] cortarTrama(String[] vectorhex, int tamaño) {
        String nuevoVector[] = new String[tamaño + 2];
        System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
        for (int i = 0; i < nuevoVector.length; i++) {
            System.out.print(" " + nuevoVector[i]);
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
            //VIC 04-10-19
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
        try {
            switch (numBytesDir) {
                case 1: // solo dirección lógica   
                    frameTemp = new byte[frame.length - 3];
                    System.arraycopy(frame, 0, frameTemp, 0, 4);
                    System.arraycopy(frame, 7, frameTemp, 4, frame.length - 7);
                    newLength = (int) ((((frame[1] << 8) & 0x0700) + (frame[2] & 0xFF)) - 3);
                    //System.out.println("Nueva longitud: " + newLength);
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    if (sdirlog.length() == 1) {
                        sdirlog = "0" + sdirlog;
                    }
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
                    escribir("La cantidad de bytes de dirección en HDLC solo puede ser de 1, 2 o 4 bytes, no existe otra cantidad permitida.");
                    return frame;
            }
        } catch (Exception e) {
            return frame;
        }
    }

    public String[] sacarDatos(String[] vectorhex, int inicio) {
        int tam = (Integer.parseInt((vectorhex[1] + vectorhex[2]), 16) & 0x7FF);
        String nuevoVector[] = new String[tam - inicio - 1];
        System.arraycopy(vectorhex, inicio, nuevoVector, 0, nuevoVector.length);
        return nuevoVector;
    }

    private byte[] crearAARQ(byte[] t3, String pass) {
        ArrayList<String> trama = new ArrayList<>();
        trama.add("7E");
        trama.add("A0");
        if (pass == null) {
            t3 = tramasGama300.getAarq2();
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
        //tamaño Data      
        if (pass != null) {
            tramabyte[15] = (byte) (Integer.parseInt(Integer.toHexString((46 + pass.length())), 16) & 0xFF);
            //Tamaño Password
            tramabyte[41] = (byte) (Integer.parseInt(Integer.toHexString(pass.length() + 2), 16) & 0xFF);
            tramabyte[43] = (byte) (Integer.parseInt(Integer.toHexString(pass.length()), 16) & 0xFF);
        }

        tramabyte[tramabyte.length - 8] = (byte) (Integer.parseInt(Integer.toHexString(24), 16) & 0xFF);
        tramabyte[tramabyte.length - 7] = (byte) (Integer.parseInt(Integer.toHexString(2), 16) & 0xFF);
        tramabyte[tramabyte.length - 6] = (byte) (Integer.parseInt(Integer.toHexString(32), 16) & 0xFF);
        // Max PDU Size 
        tramabyte[tramabyte.length - 5] = (byte) (Integer.parseInt(Integer.toHexString(0), 16) & 0xFF);//Parametrizable desde Base de datos
        tramabyte[tramabyte.length - 4] = (byte) (Integer.parseInt(Integer.toHexString(0), 16) & 0xFF);


        return tramabyte;
    }

    private byte[] construirConstant(String sn) {
        byte trama[] = tramasGama300.getConstant();
        trama = asignaDirecciones(trama);

        trama[4 + numBytesDir] = I_CTRL(nr, ns);//VIC y va de 17 a 22 no de 16 a 21
        trama[trama.length - 5] = (byte) (Integer.parseInt(sn.substring(0, 2), 16) & 0xFF);
        trama[trama.length - 4] = (byte) (Integer.parseInt(sn.substring(2, 4), 16) & 0xFF);
        trama = calcularnuevocrcI(trama, numBytesDir);
        return trama;
    }

    public String convertStringToHex(String str) {
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

    public Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }

    public Date getDCurrentDate() throws ParseException {
        return Date.from(ZonedDateTime.now(zid).toInstant());
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
    
    public String[] concatFrames(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
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
        byte[] trama = tramasGama300.getFRMR();
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
