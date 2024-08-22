/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasLocalActaris;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.Electura;
import Entidades.EtipoCanal;
import Util.SynHoraNTP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dperez
 */
public class LeerRemotoTCPActarisSL7000 extends Thread {

    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    String seriemedidor = "";
    boolean rutinaCorrecta = false;
    InputStream input;
    OutputStream output;
    long tiempo = 500;
    String cadenahex = "";
    TramasLocalActaris tramaactaris = new TramasLocalActaris();
    boolean aviso = false;
    String password = "";
    int indx = 0;
    EMedidor med;
    ControlProcesos cp;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lacumulados;
    boolean lconfhora;
    public boolean leer = true;
    String numeroPuerto;
    int numeroReintentos = 4;
    int ReintentoFRMR = 0; // vic 24-04-2020
    int ReinicioFRMR = 0; // vic 24-04-2020
    int nreintentos = 0;
    int velocidadPuerto;
    long timeout;
    int ndias;
    String fechaActual;
    String lastMsg;
    boolean portconect = false;
    int reintentoadp = 0;
    Thread port = null;
    Thread tEscritura = null;
    Thread tReinicio = null;
    boolean inicia1 = false;
    public boolean cierrapuerto = false;
    Socket socket;
    private volatile boolean escucha = false;
    Thread tLectura;
    private int reintentoconexion = 0;
    boolean llegotramaini = false;
    Date dateIniReq;
    Timestamp time = null; //tiempo de NTP
    Timestamp tsfechaactual;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    long ndesfasepermitido = 0;
    private long desfase;
    boolean solicitar = false; //variable de control de la sync
    //estados
    //*************************************
    int numBytesDir = 1;
    boolean lA = false;
    boolean lC = false;
    boolean SNRMUA = false;
    boolean lcontraseña = false;
    boolean lfirmware = false;
    boolean lserialnumber = false;
    boolean lfechaactual = false;
    boolean lperiodoInt = false;
    boolean linfoperfil = false;
    boolean lfechaactual2 = false;
    boolean lfechaactual3 = false;
    boolean lperfilcarga = false;
    boolean lconsumosAcumulados = false;
    boolean ltarifas = false;
    boolean lPowerFailureElements = false;
    boolean lReset = false;
    boolean lterminar = false;
    boolean lfechasync = false;
    boolean lsincroniza = false;// variable que me indica que debe sincronizar la fecha
    //************************************
    boolean lecturacorrecta = true; //Ajuste para mejor manejo del estado de leido
    boolean enviando = false;
    boolean reenviando = false;
    boolean fixOffset = false;
    byte[] ultimatramaEnviada = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    SimpleDateFormat sdfLP = new SimpleDateFormat("yyyyMMdd");
    int numcanales = 2;
    int intervalo = 0;
    int factorIntervalo = 0;
    int ndia = 3;
    int dirfis = 0;
    int dirlog = 0;
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
    int reinicio = 0;
    int ns = 0;
    int nr = 0;
    int nrEsperado = 0;
    int nsEsperado = 0;

    //variables para fecha y hora 
    private final ZoneId zid;

    boolean ultimbloquePerfil = false;
    boolean primeraTramaBloque = false;
    int numerobloque;
    ArrayList<String> acumuladosArrL;
    Vector<String> vPerfilCarga;
    Vector<String> vEventos;
    Vector<String> infoPerfil;
    String[] obis;
    String[] conskePerfil;
    String[] unidades;
    private boolean lprimerBloque;//es el primer bloque del perfil de carga 
    private int nintervalosperfil = 0;
    private int nintervaloseventos = 0;
    private int periodoIntegracion = 15;
    private int nOBIS_acumulado = 1;
    private Timestamp tsTarifas;
    private Timestamp[][] tsAcumulados = new Timestamp[4][4];
    private double[][] valAcumulados_Tarifas = new double[4][4];
    private String[] obisEnergias_Acumuladas = {"0101010800FF", "0101020800FF", "0101030800FF", "0101040800FF"};
    private String[] unidadesEnergias_Acumuladas = {"kWhD", "kWhR", "kVarhD", "kVarhR"};
    private boolean rrAcumulados;
    private boolean primerBloqueTarifas;
    private boolean primerbloque;
    byte users[] = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x07, (byte) 0x7A};
    byte pass[] = {(byte) 0x00};
    private int indxuser = 0;
    private boolean ultimatramabloque = false;

    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    Abortar objabortar;
    private String usuario = "admin";
    private final Object monitor = new Object();
    private final String label = "LeerTCPActaris";

    public LeerRemotoTCPActarisSL7000(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean acumulados, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) {
        this.med = med;
        this.cp = cp;
        this.usuario = usuario;
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
        lacumulados = acumulados;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        jinit();
    }

    private void jinit() {
        try {
            tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
            indxuser = 0;
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            timeout = med.getTimeout() * 1000;
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "17" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "17" : med.getDireccionLogica()));
            numBytesDir = med.getBytesdireccion().equals("5") ? 1 : Integer.parseInt(med.getBytesdireccion());//5 implica que llegó null
            numBytesDir = med.getTipoconexion() == 0 ? 4 : numBytesDir;
            users[0] = med.getDireccionCliente().equalsIgnoreCase("1") ? 0x01 : (byte) Integer.parseInt(med.getDireccionCliente());
            nOBIS_acumulado = 1;
            rrAcumulados = false;
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
                    if (lconsumosAcumulados && !rrAcumulados) {
                        tsAcumulados[0][nOBIS_acumulado - 1] = obtenerHora();
                    }
                    if (ltarifas && primerBloqueTarifas) {
                        tsTarifas = obtenerHora();
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
                    if (fixOffset) {
                        fixOffset = false;
                        uncomplete = false;
                        enviando = false;
                        reenviando = false;
                        monitor.notifyAll();
                        monitor.wait(300);
                        escribir("Re transmitir Ultima Peticion");
                        retransmitir();
                    } else {
                        escribir("Se vencío el timeout de respuesta sin recibir nada");
                        reenviando = true;
                        enviando = false;
                        monitor.notifyAll();
                    }
                    return;
                }
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            fixOffset = false;
            cadenahex = tramaactaris.encode(auxBuffer, idxFrame);

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

    private void retransmitir() {
        enviatrama2_2(ultimatramaEnviada, lastMsg);
    }

    private void iniciacomunicacion() throws Exception {
        lsincroniza = false;
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

                ultimatramabloque = false;

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

            SNRMUA = true;
            byte[] trama = tramaactaris.getT1();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcI(trama);
            ultimatramaEnviada = tramaactaris.getT1();
            enviatrama2_2(trama, "SNRM");
        } else {
            interrumpirHilo(tLectura);
            escribir("Medidor no configurado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
            cerrarPuerto(false);
            cerrarLog("Medidor no configurado", false);
            leer = false;
        }
    }

    private void enviatrama2_2(byte[] bytes, String descripcion) {
        enviando = true;
        tEscritura = new Thread() {
            @Override
            public void run() {
                byte[] trama = bytes;
                String des = descripcion;
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
                                escribir("=> " + tramaactaris.encode(trama, trama.length));
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
                                if (lfirmware) {
                                    lfirmware = false;
                                    lfechaactual = true;
                                    intentosRetransmision = 0;
                                    escribir("NsPc " + ns + " NrPc " + nr);
                                    trama = tramaactaris.getTfechaHora();
                                    trama = asignaDirecciones(trama);
                                    trama[8] = I_CTRL(nr, ns);
                                    trama = calcularnuevocrcI(trama);
                                    ultimatramaEnviada = trama;
                                    des = "=>Solicitud Fecha Actual";
                                } else {
                                    interrumpirHilo(tLectura);
                                    escribir("Número de reenvios agotado");
                                    enviando = false;
                                    t = false;
                                    cierrapuerto = true;
                                }
                            } else {
                                return;
                            }
                        }
                        if (cierrapuerto) {
                            cierrapuerto = false;
                            cerrarPuerto(true);
                            monitor.notifyAll();
                            monitor.wait(1000);
                            if (lperfilcarga) {
                                escribir("Inicia Contingencia");
                                procesaInfoPerfil();
                                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                                    try {
                                        procesaDatos();
                                    } catch (Exception e) {
                                        lecturacorrecta = false;
                                        escribir("Error en procesamiento de perfil");
                                        escribir(getErrorString(e.getStackTrace(), 3));
                                    }
                                }
                                if (leventos) {
                                    if (vEventos != null && vEventos.size() > 0) {
                                        try {
                                            procesaInfoEventos();
                                            lecturacorrecta = cp.isLecturacorrecta();
                                        } catch (Exception e) {
                                            lecturacorrecta = false;
                                            escribir("Error en procesamiento de eventos");
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

    public void cerrarPuerto(boolean sendBye) {           
        if (sendBye) {
            byte trama[] = tramaactaris.getTfinaliza();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            escribir("Envia Logout sin espera de respuesta");
            escribir("=> " + tramaactaris.encode(trama, trama.length));
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

    private void interpretaCadena(String cadenahex) throws Exception {
        try {
            escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (SNRMUA) {
                revisarSNRM(vectorhex);
            } else if (lcontraseña) {
                revisarPassword(vectorhex);
            } else if (lfirmware) {
                revisarFirmware(vectorhex);
            } else if (lserialnumber) {
                revisarSerial(vectorhex);
            } else if (lfechaactual) {
                revisarFechaActual(vectorhex);
            } else if (lperiodoInt) {
                revisarPeriodoInt(vectorhex);
            } else if (linfoperfil) {
                revisarInfoPerfil(vectorhex);
            } else if (lfechaactual2) {
                revisarFecha2(vectorhex);
            } else if (lperfilcarga) {
                revisarPerfil(vectorhex);
            } else if (lPowerFailureElements) {//eventos           
                revisarEventos(vectorhex);
            } else if (lconsumosAcumulados) {
                revisarAcumulados(vectorhex);
            } else if (ltarifas) {
                revisarTarifas(vectorhex);
            } else if (lterminar) {
                revisarLogout(vectorhex);
            } else if (lReset) {//estado reset para los casos de contraseña mala y negacio            
                revisarReset(vectorhex);
            } else if (lfechasync) {
                revisarFechaSync(vectorhex);
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
    
    private void handleReenvio(String msg) {
        if (ReintentoFRMR <= numeroReintentos) {
            ReintentoFRMR ++;
            enviatrama2_2(ultimatramaEnviada, "=> " + msg);
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
        byte[] trama = tramaactaris.getFRMR();
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcI(trama);
        ReintentoFRMR ++;
        enviatrama2_2(trama, "Reject");
    }

    private void revisarSNRM(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if (vectorhex[8].equals("73") || vectorhex[8].equals("63")) {
                    ns = 0;
                    nr = 0;
                    escribir("NsPc " + ns + " NrPc " + nr);
                    nrEsperado = ns + 1;
                    nsEsperado = 0;
                    SNRMUA = false;
                    lcontraseña = true;

                    byte trama[] = crearAARQ(tramaactaris.getT3(), password); //AARQ
                    trama = asignaDirecciones(trama);
                    trama = calcularnuevocrcI(trama);

                    ultimatramaEnviada = trama;
                    enviatrama2_2(trama, "");
                } else {
                    reiniciaComunicacion(true);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarPassword(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    //validamos secuencia
                    escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                    if (vectorhex[38].equals("00") && vectorhex[31].equals("00")) {
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "");
                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud AARQ";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        //reiniciamos
                        byte trama[] = tramaactaris.getTfinaliza();
                        trama = asignaDirecciones(trama);
                        indxuser++;
                        if (vectorhex[38].equals("0D")) {
                            //System.out.println("Error de autenticación");
                            if ((indxuser) < users.length) {
                                escribir("Error de autenticacion");
                                lcontraseña = false;
                                lReset = true;
                                trama = calcularnuevocrcRR(trama);
                                enviatrama2_2(trama, "");
                            } else {
                                lcontraseña = false;
                                lterminar = true;
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de autenticacion");
                                trama = calcularnuevocrcRR(trama);
                                enviatrama2_2(trama, "");
                                escribir("Desconexion - Error de autenticacion");
                                cerrarLog("Desconexion Error de autenticacion", false);
                            }
                        } else {
                            escribir("Error de autenticacion - fallo en interpretación de AARE");
                            //System.out.println("\nError de autenticacion - fallo en interpretación de AARE");
                            if ((indxuser) < users.length) {
                                lcontraseña = false;
                                lReset = true;
                                trama = calcularnuevocrcRR(trama);
                                enviatrama2_2(trama, "");
                            } else {
                                lcontraseña = false;
                                lterminar = true;
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error de autenticacion");
                                trama = calcularnuevocrcRR(trama);
                                enviatrama2_2(trama, "");
                                escribir("Desconexion - Error de autenticacion");
                                cerrarLog("Desconexion Error de autenticacion", false);
                            }
                        }
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        lcontraseña = false;
                        lfirmware = true;
                        escribir("NsPc " + ns + " NrPc " + nr);
                        byte trama[] = tramaactaris.getTfirmware();
                        trama = asignaDirecciones(trama);
                        trama[8] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Solicitud Firmware");
                    } else {
                        lastMsg = "=> RR AARQ";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio(" Envia Solicitud AARQ");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarFirmware(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    if (vectorhex[17].equals("00")) {
                        escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Received Ready RR");
                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud Firmware";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        escribir("Negacion peticion");
                        lfirmware = false;
                        lserialnumber = true;
                        escribir("NsPc " + ns + " NrPc " + nr);
                        byte trama[] = tramaactaris.getTserie();
                        trama = asignaDirecciones(trama);
                        trama[8] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Solicitud Numero Serial");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        lfirmware = false;
                        lserialnumber = true;
                        escribir("NsPc " + ns + " NrPc " + nr);
                        byte trama[] = tramaactaris.getTserie();
                        trama = asignaDirecciones(trama);
                        trama[8] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Solicitud Numero Serial");
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Firmware";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Firmware");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarSerial(String[] vectorhex) throws SQLException {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    if (vectorhex[17].equals("00")) {
                        escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            byte trama[] = tramaactaris.getRR();
                            int tamaño = Integer.parseInt(vectorhex[19], 16);
                            boolean continuar = false;
                            String datoserial = "";
                            for (int i = 0; i < tamaño; i++) {
                                datoserial = datoserial + vectorhex[20 + i];
                            }
                            datoserial = Hex2ASCII(datoserial);
                            if (seriemedidor.equals(datoserial)) {
                                cp.actualizaDirCliente(seriemedidor, (users[indxuser]) & 0xFF);
                                escribir("Numero de serial " + datoserial);
                                continuar = true;
                            } else {
                                escribir("Numero de serial incorrecto");
                            }
                            if (continuar) {
                                trama = asignaDirecciones(trama);
                                trama[8] = RR_CTRL(nr);
                                trama = calcularnuevocrcRR(trama);
                                ultimatramaEnviada = trama;
                                enviatrama2_2(trama, "=>Envia Recieved Ready RR");
                            } else {
                                lserialnumber = false;
                                lterminar = true;
                                trama = tramaactaris.getTfinaliza();
                                trama = asignaDirecciones(trama);
                                trama = calcularnuevocrcRR(trama);                                
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion serial incorrecto");
                                escribir("Desconexion - Error de Serial");
                                cerrarLog("Desconexion Error de serial", false);
                                enviatrama2_2(trama, "");
                            }

                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud de Serial";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        escribir("Negacion de peticion");
                        lserialnumber = false;
                        lterminar = true;
                        byte trama[] = tramaactaris.getTfinaliza();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviatrama2_2(trama, "");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        if (lacumulados) {
                            nOBIS_acumulado = 1;
                            enviaTotalAcumulado();
                        } else {
                            lfechaactual = true;
                            byte trama[] = tramaactaris.getTfechaHora();
                            trama = asignaDirecciones(trama);
                            trama[8] = I_CTRL(nr, ns);
                            trama = calcularnuevocrcI(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=>Solicitud Fecha Actual");                            
                        }
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Serial";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Serial");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarFechaActual(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    if (vectorhex[17].equals("00")) {
                        escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            //capturamos la fecha actual del medidor
                            String StrFechaActual = Integer.parseInt(vectorhex[14] + vectorhex[15], 16) + "/"
                                    + "" + Integer.parseInt(vectorhex[16], 16) + "/"
                                    + "" + Integer.parseInt(vectorhex[17], 16);
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Recieved Ready RR");
                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud Fecha Actual";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        escribir("Negacion de peticion");
                        lfechaactual = false;
                        lterminar = true;
                        byte trama[] = tramaactaris.getTfinaliza();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviatrama2_2(trama, "");
                    }

                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        lfechaactual = false;
                        lperiodoInt = true;
                        byte trama[] = tramaactaris.getTparametrosperfil1();
                        trama = asignaDirecciones(trama);
                        trama[8] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Solicitud parametros del perfil ");
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Fecha Actual";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Fecha Actual");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarPeriodoInt(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    if (vectorhex[17].equals("00")) {
                        escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            periodoIntegracion = Integer.parseInt(vectorhex[21] + vectorhex[22], 16);
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Recieved Ready RR");
                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud de Parametros de Perfil";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        escribir("Negacion de peticion");
                        lperiodoInt = false;
                        lterminar = true;
                        byte trama[] = tramaactaris.getTfinaliza();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviatrama2_2(trama, "");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        lperiodoInt = false;
                        linfoperfil = true;
                        infoPerfil = new Vector<String>();
                        primerbloque = true;
                        byte trama[] = tramaactaris.getTinfoPerfil();
                        trama = asignaDirecciones(trama);
                        trama[8] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Solicitud Informacion Perfil de carga");
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Parametros de Perfil";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud de Parametros de Perfil");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarInfoPerfil(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    boolean analizatrama = false;
                    if (primerbloque) {
                        if (vectorhex[17].equals("00")) {
                            analizatrama = true;
                        }
                    } else {
                        analizatrama = true;
                    }
                    if (analizatrama) {
                        escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            if (!vectorhex[1].equals("A0")) {
                                nsEsperado++;
                                if (nsEsperado > 7) {
                                    nsEsperado = 0;
                                }
                            }
                            int poscorte = 11;
                            if (primerbloque) {
                                primerbloque = false;
                                numcanales = (Integer.parseInt(vectorhex[21], 16) - 2) / 2;
                                poscorte = 22;
                            }
                            for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                infoPerfil.add(vectorhex[i]);
                            }
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Recieved Ready RR");
                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud Informacion Perfil";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        escribir("Negacion de peticion");
                        linfoperfil = false;
                        lterminar = true;
                        byte trama[] = tramaactaris.getTfinaliza();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviatrama2_2(trama, "");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        try {
                            time = obtenerHora();
                            escribir("Fecha " + zid.getId() + ": " + time);
                            deltatimesync1 = obtenerHora();
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            escribir("Error obteniendo Fecha con Id de Zona");
                        }
                        escribir("NsPc " + ns + " NrPc " + nr);
                        linfoperfil = false;
                        lfechaactual2 = true;
                        byte trama[] = tramaactaris.getTfechaHora2();
                        trama = asignaDirecciones(trama);
                        trama[8] = I_CTRL(nr, ns);
                        trama = calcularnuevocrcI(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Solicitud fecha Actual");
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Informacion de Perfil";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud informacion perfil");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarFecha2(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    if (vectorhex[17].equals("00")) {
                        escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                        if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            deltatimesync2 = obtenerHora();
                            fechaActual = Integer.parseInt(vectorhex[30] + vectorhex[31], 16) + "/"
                                    + "" + Integer.parseInt(vectorhex[32], 16) + "/"
                                    + "" + Integer.parseInt(vectorhex[33], 16) + " "
                                    + "" + Integer.parseInt(vectorhex[35], 16) + ":"
                                    + "" + Integer.parseInt(vectorhex[36], 16) + ":"
                                    + "" + Integer.parseInt(vectorhex[37], 16);
                            escribir("Fecha actual de medidor " + fechaActual);
                            try {
                                escribir("Fecha actual Colombia: " + this.getDCurrentDate());
                                escribir("Fecha Ultima Lectura " + med.getFecha());
                                if (med.getFecha() != null) {
                                    long diffInMillies = Math.abs(this.getDSpecificDate(true, 1, "D").getTime() - med.getFecha().getTime());
                                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                                    ndias = (int) diff;
                                    escribir("Numero de dias leer calculado " + ndias);
                                }
                            } catch (Exception e) {
                                escribir(getErrorString(e.getStackTrace(), 3));
                            }
                            try {
                                tsfechaactual = new Timestamp(sdf3.parse(fechaActual).getTime());
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
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Recieved Ready RR");
                        } else {
                            //no son los ns y nr esperados
                            lastMsg = "=> Envia Solicitud Fecha Actual";
                            fixOffset = true;
                            enviatrama2_2(pass, "Corregir Desfase");
                        }
                    } else {
                        escribir("Negacion de peticion");
                        lfechaactual2 = false;
                        lterminar = true;
                        byte trama[] = tramaactaris.getTfinaliza();
                        trama = asignaDirecciones(trama);
                        trama = calcularnuevocrcRR(trama);
                        enviatrama2_2(trama, "");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        lfechaactual2 = false;
                        if (lperfil) {
                            if (solicitar) {                                
                                lperfilcarga = true;
                                lperfilcompleto = false;
                                vPerfilCarga = new Vector<String>();
                                tiempo = 1000 * timeout;
                                byte trama[] = tramaactaris.getTperfil1();
                                trama = asignaDirecciones(trama);
                                trama[8] = I_CTRL(nr, ns);
                                try {
                                    byte[] dateParams;
                                    Date fechaUltLec = new Date(med.getFecha().getTime());
                                    dateIniReq = this.getDSpecificDate(false, 1, "H", fechaUltLec);
                                    String fechaIni = sdf.format(dateIniReq);
                                    escribir("Fecha Ultima Lectura desde la que se solicitarà el Perfil de carga: " + sdf3.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                                    
                                    String fechaFin;
                                    if (ndias > 30) {
                                        fechaFin = this.getSpecificDate(sdf, true, 30, "D", fechaIni);
                                        escribir("Fecha final de perfil de carga " + this.getSpecificDate(sdf3, false, ndias - 31, "D"));
                                    } else {
                                        fechaFin = sdf.format(this.getDSpecificDate(true, 23, "H"));
                                        escribir("Fecha final de perfil de carga " + sdf3.format(this.getDSpecificDate(true, 1, "D")));
                                    }
                       
                                    dateParams = adecuarFechasFiltro(fechaIni, fechaFin);
                                    System.arraycopy(dateParams, 0, trama, 33, 12);
                                    System.arraycopy(dateParams, 12, trama, 47, 12);
                                } catch (Exception e) {
                                    escribir(getErrorString(e.getStackTrace(), 3));
                                    cerrarPuerto(true);
                                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error solicitud perfil de carga");
                                    escribir("Estado lectura No leido");
                                    cerrarLog("Desconexion Error solicitud de perfil de carga", false);
                                    leer = false;
                                }
                                trama = calcularnuevocrcI(trama);
                                primeraTramaBloque = true;
                                nintervalosperfil = 0;
                                lprimerBloque = true;
                                numerobloque = 0;
                                ultimbloquePerfil = false;
                                ultimatramaEnviada = trama;
                                enviatrama2_2(trama, "=> Solicitud de perfil de carga");
                            } else {
                                lterminar = true;
                                byte trama[] = tramaactaris.getTfinaliza();
                                trama = asignaDirecciones(trama);
                                trama = calcularnuevocrcRR(trama);
                                escribir("Estado lectura perfil no solicitado por desfase de hora");
                                enviatrama2_2(trama, "");
                            }
                        } else if (leventos) {//solicitud Eventos
                            byte trama[] = tramaactaris.getPowerFailureElements();
                            trama = asignaDirecciones(trama);
                            trama[8] = I_CTRL(nr, ns);
                            trama = calcularnuevocrcI(trama);
                            lprimerBloque = true;
                            primeraTramaBloque = true;
                            lPowerFailureElements = true;
                            nintervaloseventos = 0;
                            vEventos = new Vector<String>();
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Solicitud Eventos powerFail");
                        } else if (lconfhora) {
                            byte data[] = tramaactaris.getTfechaSync();
                            Timestamp currTime = obtenerHora();
                            String fecha = sdf.format(new Date(currTime.getTime()));
                            //System.out.println("Fecha a actualizaar " + fecha);
                            String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                            while (lfecha.length() < 4) {
                                lfecha = "0" + lfecha;
                            }
                            data[29] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            data[30] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
                            data[31] = (byte) (Integer.parseInt(fecha.substring(4, 6)) & 0xFF);// mes 
                            data[32] = (byte) (Integer.parseInt(fecha.substring(6, 8)) & 0xFF);//dia
                            data[33] = (byte) (((currTime.getDay()) == 0 ? 7 : (currTime.getDay())) & 0xFF);// dia de la semana
                            //data[33] = (byte) (((time.getDay()-1)==0? 7:(time.getDay()-1)) & 0xFF);// dia de la semana
                            data[34] = (byte) (Integer.parseInt(fecha.substring(8, 10)) & 0xFF); // hora 
                            data[35] = (byte) (Integer.parseInt(fecha.substring(10, 12)) & 0xFF); // min
                            data[36] = (byte) (Integer.parseInt(fecha.substring(12, 14)) & 0xFF); // seg
                            data[37] = (byte) 0xFF; // centesimas
                            data[38] = (byte) 0x80;
                            data[39] = (byte) 0x00; // desviacion 2 bytes
                            data[40] = (byte) 0x00; // status

                            lfechasync = true;
                            data[8] = I_CTRL(nr, ns);
                            data = asignaDirecciones(data);
                            data = calcularnuevocrcI(data);
                            ultimatramaEnviada = data;
                            //System.out.println("Envio trama de cambio de hora");
                            //System.out.println(tramaactaris.encode(data, data.length));
                            cp.saveAcceso(med.getnSerie(), "2", sdf3.format(new Date(tsfechaactual.getTime())), sdf3.format(new Date(currTime.getTime())), usuario, null);
                            enviatrama2_2(data, "=> Configuracion de hora " + sdf3.format(new Date(currTime.getTime())));
                        } else {//desconectar
                            lterminar = true;
                            byte trama[] = tramaactaris.getTfinaliza();
                            trama = asignaDirecciones(trama);
                            trama = calcularnuevocrcRR(trama);
                            enviatrama2_2(trama, "");
                        }
                    }  else {
                        //no es el esperado
                        lastMsg = "=> RR Informacion de Perfil";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud informacion perfil");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarPerfil(String[] vectorhex) {

        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                        int poscorte = 0;
                        boolean analizaTrama = false;
                        if (primeraTramaBloque) { //es la primera trama del bloque
                            primeraTramaBloque = false;
                            if (vectorhex[14].equals("C4") && vectorhex[15].equals("02")) {//es respuesta con data block
                                //validamos si es el primer bloque del perfil
                                if (vectorhex[22].equals("00")) { //verificamos negacion del perfil
                                    if (Integer.parseInt(vectorhex[23], 16) > 127) {
                                        int tamaño_longitud = (Integer.parseInt(vectorhex[23], 16) & 0x7F);
                                        //System.out.println(tamaño_longitud);
                                        String longbytesperfil = "";
                                        for (int i = 0; i < tamaño_longitud; i++) {
                                            longbytesperfil = longbytesperfil + vectorhex[24 + i];
                                        }
                                        String longbytes2 = "";
                                        //System.out.println("tamaño en bytes del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                        escribir("tamaño en bytes del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                        poscorte = 23 + (tamaño_longitud) + 1;
                                        if (lprimerBloque) {
                                            lprimerBloque = false;
                                            //System.out.println("valor " + vectorhex[25 + (tamaño_longitud)]);
                                            if (Integer.parseInt(vectorhex[25 + (tamaño_longitud)], 16) > 127) {
                                                int tamañointervalos = (Integer.parseInt(vectorhex[25 + (tamaño_longitud)], 16) & 0x7F);
                                                for (int i = 0; i < tamañointervalos; i++) {
                                                    longbytes2 = longbytes2 + vectorhex[26 + tamaño_longitud + i];
                                                }
                                                poscorte = 26 + (tamaño_longitud + tamañointervalos);
                                                nintervalosperfil = Integer.parseInt(longbytes2, 16);
                                                escribir("Numero de intervalos del perfil de carga " + Integer.parseInt(longbytes2, 16));
                                            } else {
                                                longbytes2 = vectorhex[25 + (tamaño_longitud)];
                                                nintervalosperfil = Integer.parseInt(longbytes2, 16);
                                                poscorte = 26 + (tamaño_longitud);
                                            }
                                        }
                                        for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                            vPerfilCarga.add(vectorhex[i]);
                                        }
                                        analizaTrama = true;
                                    } else {
                                        int tamaño_longitud = (Integer.parseInt(vectorhex[23], 16));
                                        escribir("tamaño en bytes del perfil de carga " + tamaño_longitud);
                                        poscorte = 24;
                                        //// System.out.println("asdasd2");
                                        if (lprimerBloque) {
                                            lprimerBloque = false;
                                            int tamañointervalos = (Integer.parseInt(vectorhex[25], 16));
                                            poscorte = 26;
                                        }
                                        for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                            vPerfilCarga.add(vectorhex[i]);
                                        }
                                        analizaTrama = true;
                                    }
                                    if (vectorhex[17].equals("01")) {//validamos si es el ultimo data block
                                        ultimbloquePerfil = true;
                                    }
                                } else {
                                    escribir("Negacion Perfil Carga");
                                    analizaTrama = false;
                                }
                            } else if (vectorhex[14].equals("C4") && vectorhex[15].equals("01")) { //viene sin data block
                                if (vectorhex[17].equals("00")) { //verificamos negacion del perfil
                                    ultimbloquePerfil = true;
                                    if (Integer.parseInt(vectorhex[19], 16) > 127) {
                                        int tamaño_longitud = (Integer.parseInt(vectorhex[19], 16) & 0x7F);
                                        String longbytesperfil = "";
                                        for (int i = 0; i < tamaño_longitud; i++) {
                                            longbytesperfil = longbytesperfil + vectorhex[20 + i];
                                        }
                                        //System.out.println("numero de intervalos del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                        escribir("numero de intervalos del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                        poscorte = 20 + tamaño_longitud;
                                        nintervalosperfil = Integer.parseInt(longbytesperfil, 16);
                                        //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                    } else {
                                        String longbytesperfil = vectorhex[19];
                                        nintervalosperfil = Integer.parseInt(longbytesperfil, 16);
                                        //System.out.println("numero de intervalos del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                        escribir("numero de intervalos del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                        poscorte = 20;
                                        //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                    }
                                    for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                        vPerfilCarga.add(vectorhex[i]);
                                    }
                                    analizaTrama = true;
                                } else {
                                    escribir("Negacion del perfil de carga");
                                    analizaTrama = false;
                                }
                            }
                        } else {
                            poscorte = 11;
                            for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                vPerfilCarga.add(vectorhex[i]);
                            }
                            analizaTrama = true;
                            //almacenamos directo
                        }
                        if (analizaTrama) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            if (!vectorhex[1].equals("A0")) {
                                nsEsperado++;
                                if (nsEsperado > 7) {
                                    nsEsperado = 0;
                                }
                            } else {
                                ultimatramabloque = true;
                            }
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Received Ready");
                        } else {
                            tiempo = 1000;
                            lperfilcarga = false;
                            lReset = true;
                            byte trama[] = tramaactaris.getTfinaliza();
                            trama = asignaDirecciones(trama);
                            trama = calcularnuevocrcRR(trama);
                            enviatrama2_2(trama, "");
                        }
                    } else {
                        //no son los ns y nr esperados
                        lastMsg = "=> Perfil de Carga";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
                        if (ultimatramabloque) {
                            ultimatramabloque = false;
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
                            if (ultimbloquePerfil) {//es el ultimo bloque del perfil por lo tanto terminamos co
                                lperfilcarga = false;
                                tiempo = 1000;
                                lperfilcompleto = true;
                                if (leventos) {
                                    byte trama[] = tramaactaris.getPowerFailureElements();
                                    trama = asignaDirecciones(trama);
                                    trama[8] = I_CTRL(nr, ns);
                                    trama = calcularnuevocrcI(trama);
                                    lprimerBloque = true;
                                    primeraTramaBloque = true;
                                    lPowerFailureElements = true;
                                    nintervaloseventos = 0;
                                    vEventos = new Vector<String>();
                                    ultimatramaEnviada = trama;
                                    enviatrama2_2(trama, "=> Solicitud Eventos powerFail");
                                } else {
                                    ultimbloquePerfil = false;
                                    lterminar = true;
                                    byte trama[] = tramaactaris.getTfinaliza();
                                    trama = asignaDirecciones(trama);
                                    trama = calcularnuevocrcRR(trama);
                                    enviatrama2_2(trama, "Logout");
                                }
                            } else {// aun faltan bloques por lo tanto se solicita el bloque
                                numerobloque++;
                                byte trama[] = tramaactaris.getTbloquePerfil();
                                trama = asignaDirecciones(trama);
                                String tramabloque = Integer.toHexString(numerobloque);
                                while (tramabloque.length() < 8) {
                                    tramabloque = "0" + tramabloque;
                                }
                                trama[17] = (byte) (Integer.parseInt(tramabloque.substring(0, 2), 16) & 0xFF);
                                trama[18] = (byte) (Integer.parseInt(tramabloque.substring(2, 4), 16) & 0xFF);
                                trama[19] = (byte) (Integer.parseInt(tramabloque.substring(4, 6), 16) & 0xFF);
                                trama[20] = (byte) (Integer.parseInt(tramabloque.substring(6, 8), 16) & 0xFF);
                                trama[8] = I_CTRL(nr, ns);
                                primeraTramaBloque = true;
                                trama = calcularnuevocrcI(trama);
                                ultimatramaEnviada = trama;
                                enviatrama2_2(trama, "=Solicitud de bloque numero " + numerobloque);
                            }
                        } else {
                            escribir("error al recibir perfil");
                            if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                                try {
                                    procesaInfoPerfil();
                                    procesaDatos();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    escribir("Error procesando datos de perfil");
                                }
                            }
                            reiniciaComunicacion(true);
                        }
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Perfil de Carga";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");                        
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Perfil de Carga");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarEventos(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                        int poscorte = 0;
                        boolean analizatrama = false;
                        if (primeraTramaBloque) { //es la primera trama del bloque
                            if (vectorhex[17].equals("00")) {
                                primeraTramaBloque = false;
                                if (Integer.parseInt(vectorhex[19], 16) > 127) {
                                    int tamaño_longitud = (Integer.parseInt(vectorhex[19], 16) & 0x7F);
                                    String longbytesperfil = "";
                                    for (int i = 0; i < tamaño_longitud; i++) {
                                        longbytesperfil = longbytesperfil + vectorhex[20 + i];
                                    }
                                    poscorte = 20 + tamaño_longitud;
                                    nintervaloseventos = Integer.parseInt(longbytesperfil, 16);
                                } else {
                                    String longbytesperfil = vectorhex[19];
                                    nintervaloseventos = Integer.parseInt(longbytesperfil, 16);
                                    poscorte = 20;
                                }
                                for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                    vEventos.add(vectorhex[i]);
                                }
                                analizatrama = true;
                            } else {
                                escribir("Negacion de peticion");
                                analizatrama = false;
                            }
                        } else {
                            poscorte = 11;
                            for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                vEventos.add(vectorhex[i]);
                            }
                            analizatrama = true;
                        }
                        if (analizatrama) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            if (!vectorhex[1].equals("A0")) {
                                nsEsperado++;
                                if (nsEsperado > 7) {
                                    nsEsperado = 0;
                                }
                            } else {
                                ultimatramabloque = true;
                            }
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Receieved Ready");
                        } else {
                            lPowerFailureElements = false;
                            lReset = true;
                            byte trama[] = tramaactaris.getTfinaliza();
                            trama = asignaDirecciones(trama);
                            trama = calcularnuevocrcRR(trama);
                            enviatrama2_2(trama, "");
                        }
                    } else {
                        //no son los ns y nr esperados
                        lastMsg = "=> Envia Eventos";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
                        if (ultimatramabloque) {
                            ultimatramabloque = false;
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
                            lPowerFailureElements = false;
                            lterminar = true;
                            byte trama[] = tramaactaris.getTfinaliza();
                            trama = asignaDirecciones(trama);
                            trama = calcularnuevocrcRR(trama);
                            enviatrama2_2(trama, "");
                        } else {
                            escribir("Error al recibir eventos");
                            reiniciaComunicacion(true);
                        }
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Eventos";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");                        
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Eventos");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarAcumulados(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x01) == 0x00) { //es informacion
                    escribir("NrM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[4 + numBytesDir], 16) & 0x0E) >> 1)) {
                        nr++;
                        if (nr > 7) {
                            nr = 0;
                        }
                        escribir("NsPc " + ns + " NrPc " + nr);
                        if (vectorhex[13 + numBytesDir].equals("00")) {
                            String valueStr = extractValue(vectorhex);
                            valAcumulados_Tarifas[0][nOBIS_acumulado - 1] = Long.parseLong(valueStr, 16) / 1000.0;
                        } else {
                            escribir("Negación de Petición");
                            valAcumulados_Tarifas[0][nOBIS_acumulado - 1] = 0.0;
                        }
                        rrAcumulados = true;
                        byte trama[] = tramaactaris.getRR();
                        trama = asignaDirecciones(trama);
                        trama[4 + numBytesDir] = RR_CTRL(nr);
                        trama = calcularnuevocrcRR(trama);
                        ultimatramaEnviada = trama;
                        enviatrama2_2(trama, "=> Envia Recieved Ready RR");
                    } else {
                        //no son los ns y nr esperados
                        lastMsg = "=> Envia Solicitud Acumulados";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
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
                        rrAcumulados = false;
                        escribir("NsPc " + ns + " NrPc " + nr);
                        if (nOBIS_acumulado < 4) {
                            nOBIS_acumulado++;
                            enviaTotalAcumulado();
                        } else {
                            lconsumosAcumulados = false;
                            enviaTarifas();                                                        
                        } 
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Acumulados";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");                        
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Acumulados");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }
    
    private void revisarTarifas(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                    escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                        int poscorte = 0;
                        boolean analizaTrama = false;
                        if (primeraTramaBloque) { //es la primera trama del bloque
                            primeraTramaBloque = false;
                            primerBloqueTarifas = false;
                             if (vectorhex[14].equals("C4") && vectorhex[15].equals("01")) { //viene sin data block
                                if (vectorhex[17].equals("00")) { //verificamos negacion del perfil
                                    ultimbloquePerfil = true;
                                    poscorte = 18;
                                    for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                        acumuladosArrL.add(vectorhex[i]);
                                    }
                                    analizaTrama = true;
                                } else {
                                    escribir("Negacion del perfil de carga");
                                    analizaTrama = false;
                                }
                            }
                        } else {
                            poscorte = 11;
                            for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                acumuladosArrL.add(vectorhex[i]);
                            }
                            analizaTrama = true;
                            //almacenamos directo
                        }
                        if (analizaTrama) {
                            nr++;
                            if (nr > 7) {
                                nr = 0;
                            }
                            escribir("NsPc " + ns + " NrPc " + nr);
                            if (!vectorhex[1].equals("A0")) {
                                nsEsperado++;
                                if (nsEsperado > 7) {
                                    nsEsperado = 0;
                                }
                            } else {
                                ultimatramabloque = true;
                            }
                            byte trama[] = tramaactaris.getRR();
                            trama = asignaDirecciones(trama);
                            trama[8] = RR_CTRL(nr);
                            trama = calcularnuevocrcRR(trama);
                            ultimatramaEnviada = trama;
                            enviatrama2_2(trama, "=> Envia Received Ready");
                        } else {
                            tiempo = 1000;
                            ltarifas = false;
                            lterminar = true;
                            byte trama[] = tramaactaris.getTfinaliza();
                            trama = asignaDirecciones(trama);
                            trama = calcularnuevocrcRR(trama);
                            enviatrama2_2(trama, "");
                        }
                    } else {
                        //no son los ns y nr esperados
                        lastMsg = "=> Tarifas";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x01) {//es rr
                    escribir("NrM " + (Integer.parseInt(vectorhex[8], 16) & 0x0F));
                    if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5)) {
                        if (ultimatramabloque) {
                            ultimatramabloque = false;
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
                            if (ultimbloquePerfil) {//es el ultimo bloque del perfil por lo tanto terminamos co
                                ltarifas = false;
                                tiempo = 1000;
                                ultimbloquePerfil = false;
                                lterminar = true;
                                byte trama[] = tramaactaris.getTfinaliza();
                                trama = asignaDirecciones(trama);
                                trama = calcularnuevocrcRR(trama);
                                enviatrama2_2(trama, "Logout");
                            } else {// aun faltan bloques por lo tanto se solicita el bloque
                                numerobloque++;
                                byte trama[] = tramaactaris.getTbloquePerfil();
                                trama = asignaDirecciones(trama);
                                String tramabloque = Integer.toHexString(numerobloque);
                                //System.out.println("Solicitud bloque " + numerobloque);
                                while (tramabloque.length() < 8) {
                                    tramabloque = "0" + tramabloque;
                                }
                                //System.out.println("Solicitud bloque " + tramabloque);
                                trama[17] = (byte) (Integer.parseInt(tramabloque.substring(0, 2), 16) & 0xFF);
                                trama[18] = (byte) (Integer.parseInt(tramabloque.substring(2, 4), 16) & 0xFF);
                                trama[19] = (byte) (Integer.parseInt(tramabloque.substring(4, 6), 16) & 0xFF);
                                trama[20] = (byte) (Integer.parseInt(tramabloque.substring(6, 8), 16) & 0xFF);
                                trama[8] = I_CTRL(nr, ns);
                                primeraTramaBloque = true;
                                trama = calcularnuevocrcI(trama);
                                ultimatramaEnviada = trama;
                                enviatrama2_2(trama, "=Solicitud de bloque numero " + numerobloque);
                            }
                        } else {
                            escribir("Error al recibir Tarifas");                            
                            reiniciaComunicacion(true);
                        }
                    } else {
                        //no es el esperado
                        lastMsg = "=> RR Tarifas";
                        fixOffset = true;
                        enviatrama2_2(pass, "Corregir Desfase");
                    }
                } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                    handleReenvio("Envia Solicitud Tarifas");
                } else {
                    validarTipoTrama(vectorhex[8]);
                }
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarLogout(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                lterminar = false;
                cerrarPuerto(false);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
                boolean l = false;
                if (lperfil) {
                    procesaInfoPerfil();
                    if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                        try {
                            procesaDatos();
                        } catch (Exception e) {
                            lecturacorrecta = false;
                            escribir("Error en procesamiento de perfil");
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }
                        l = true;
                    }
                }
                if (leventos) {
                    if (vEventos != null && vEventos.size() > 0) {
                        try {
                            procesaInfoEventos();
                            lecturacorrecta = cp.isLecturacorrecta();
                        } catch (Exception e) {
                            lecturacorrecta = false;
                            escribir("Error en procesamiento de eventos");
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }
                        l = true;
                    }
                }
                if (lacumulados) {
                    procesaAcumulados();
                }
                if (!lperfil && !leventos) {
                    l = true;
                }
                if (lconfhora) {
                    l = true;
                }
                if (l && lecturacorrecta) { //antes solo l                        
                    escribir("Estado Lectura Leido");
                    cerrarLog("Leido", true);
                    med.MedLeido = true;
                } else {
                    cerrarLog("No leido", false);
                    escribir("Estado Lectura No leido");
                }
                leer = false;
            }  else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarReset(String[] vectorhex) {
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                lReset = false;
                reiniciaComunicacion(false);
            }  else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    private void revisarFechaSync(String[] vectorhex) {
        //peticion fecha actual para sincronizar
        vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
        if (validacionCRCHCS(vectorhex)) {
            if (validacionCRCFCS(vectorhex)) {
                if (vectorhex.length >= 17 && vectorhex[17].equals("00")) {//verificamos si viene el valor del aceptacion de la hora
                    escribir("Establecimiento de fecha y hora exitoso.");
                } else {
                    escribir("Establecimiento de fecha y hora no exitoso.");
                }
                lfechasync = false;
                lterminar = true;
                byte trama[] = tramaactaris.getTfinaliza();
                trama = asignaDirecciones(trama);
                trama = calcularnuevocrcRR(trama);
                enviatrama2_2(trama, "Logout");
            } else {
                escribir("BAD FCS");
                handleReject();
            }
        } else {
            escribir("BAD HCS");
            handleReject();
        }
    }

    public String extractValue(String[] vectorhex) {
        int dataType = Integer.parseInt(vectorhex[14 + numBytesDir], 16);
        switch (dataType) {
            case 5:
                return "" + vectorhex[15 + numBytesDir] + vectorhex[16 + numBytesDir] + vectorhex[17 + numBytesDir] + vectorhex[18 + numBytesDir];
            case 6:
                return "" + vectorhex[15 + numBytesDir] + vectorhex[16 + numBytesDir] + vectorhex[17 + numBytesDir] + vectorhex[18 + numBytesDir];
            case 13:
                return "" + vectorhex[15 + numBytesDir];
            case 15:
                return "" + vectorhex[15 + numBytesDir];
            case 16:
                return "" + vectorhex[15 + numBytesDir] + vectorhex[16 + numBytesDir];
            case 17:
                return "" + vectorhex[15 + numBytesDir];
            case 18:
                return "" + vectorhex[15 + numBytesDir] + vectorhex[16 + numBytesDir];
            case 20:
                return "" + vectorhex[15 + numBytesDir] + vectorhex[16 + numBytesDir] + vectorhex[17 + numBytesDir] + vectorhex[18 + numBytesDir] + vectorhex[19 + numBytesDir] + vectorhex[20 + numBytesDir] + vectorhex[21 + numBytesDir] + vectorhex[22 + numBytesDir];
            case 21:
                return "" + vectorhex[15 + numBytesDir] + vectorhex[16 + numBytesDir] + vectorhex[17 + numBytesDir] + vectorhex[18 + numBytesDir] + vectorhex[19 + numBytesDir] + vectorhex[20 + numBytesDir] + vectorhex[21 + numBytesDir] + vectorhex[22 + numBytesDir];
            case 22:
                return "" + vectorhex[15 + numBytesDir];
            default:
                escribir("Tipo de dato no soportado");
                return "00";
        }
    }

    public byte[] calcularnuevocrcI(byte[] siguientetrama) {
        int indxLength = 2;
        int indxControl = indxLength + numBytesDir + 2;
        int indxhcs = indxControl + 1;
        try {
            siguientetrama[indxLength] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
            byte[] data = new byte[indxControl];
            for (int i = 0; i < data.length; i++) {
                data[i] = siguientetrama[i + 1];
            }
            int crc = calculoFCS(data);
            String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
            //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
            switch (stxcrc.length()) {
                case 3:
                    stxcrc = "0" + stxcrc;
                    break;
                case 2:
                    stxcrc = "00" + stxcrc;
                    break;
                case 1:
                    stxcrc = "000" + stxcrc;
                    break;
                default:
                    break;
            }
            siguientetrama[indxhcs] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF); //21-10-19
            siguientetrama[indxhcs + 1] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
            data = new byte[siguientetrama.length - 4];
            for (int i = 0; i < data.length; i++) {
                data[i] = siguientetrama[i + 1];
            }
            crc = calculoFCS(data);
            stxcrc = "" + Integer.toHexString(crc).toUpperCase();
            //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
            switch (stxcrc.length()) {
                case 3:
                    stxcrc = "0" + stxcrc;
                    break;
                case 2:
                    stxcrc = "00" + stxcrc;
                    break;
                case 1:
                    stxcrc = "000" + stxcrc;
                    break;
                default:
                    break;
            }
            siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return siguientetrama;
    }

    private byte[] calcularnuevocrcRR(byte[] siguientetrama) {
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

    private boolean validacionCRCHCS(String[] data) {
        boolean lcrc = false;
        byte b[] = new byte[8];
        for (int j = 0; j
                < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        String stx = data[9] + "" + data[10];
        String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
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

    private boolean validacionCRCFCS(String[] data) {
        boolean lcrc = false;
        byte b[] = new byte[data.length - 4];
        for (int j = 0; j
                < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        ////System.out.println("valor crc cal" + crc);
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

    private byte[] asignaDirecciones(byte[] frame) {
        byte[] frameTemp;
        int newLength;
        int logAddr = numBytesDir == 1 ? (dirlog * 2) + 1 : (dirlog * 2);
        int phyAddr = (dirfis * 2) + 1;
        String sdirlog;
        String sdirfis;
        //System.out.println("Número de Bytes de Dirección: " + numBytesDir);
        try {
            switch (numBytesDir) {
                case 1: // solo dirección lógica   
                    frameTemp = new byte[frame.length - 3];
                    System.arraycopy(frame, 0, frameTemp, 0, 4);
                    System.arraycopy(frame, 7, frameTemp, 4, frame.length - 7);
                    newLength = (int) ((((frame[1] << 8) & 0x0700) + (frame[2] & 0xFF)) - 3);
                    sdirlog = Integer.toHexString(logAddr).toUpperCase();
                    if (sdirlog.length() == 1) {
                        sdirlog = "0" + sdirlog;
                    }
                    frameTemp[1] = (byte) ((byte) 0xA0 + (byte) (newLength & 0x0700));
                    frameTemp[2] = (byte) (newLength & 0xFF);
                    frameTemp[3] = (byte) (Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF);
                    frameTemp[4] = (byte) ((users[indxuser] * 2) + 1);
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
                    frameTemp[5] = (byte) ((users[indxuser] * 2) + 1);
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
                    frame[7] = (byte) ((users[indxuser] * 2) + 1);
                    return frame;
                default:
                    escribir("La cantidad de bytes de dirección en HDLC solo puede ser de 1, 2 o 4 bytes, no existe otra cantidad permitida.");
                    return frame;
            }
        } catch (Exception e) {
            //System.out.println(e.getStackTrace()[0]);
            return frame;
        }
    }

    /*
    private byte[] asignaDirecciones(byte[] trama, int dirlog, int dirfis) {
        int direcionlogica = dirlog * 2;

        int direcionfisica = (dirfis * 2) + 1;
        
        String sdirlog = Integer.toHexString(direcionlogica).toUpperCase();
        while (sdirlog.length() < 4) {
            sdirlog = "0" + sdirlog;
        }
        String sdirfis = Integer.toHexString(direcionfisica).toUpperCase();
        while (sdirfis.length() < 4) {
            sdirfis = "0" + sdirfis;
        }
        trama[3] = (byte) (Integer.parseInt(sdirlog.substring(0, 2), 16) & 0xFF);
        trama[4] = (byte) (Integer.parseInt(sdirlog.substring(2, 4), 16) & 0xFF);
        trama[5] = (byte) (Integer.parseInt(sdirfis.substring(0, 2), 16) & 0xFF);
        trama[6] = (byte) (Integer.parseInt(sdirfis.substring(2, 4), 16) & 0xFF);
        trama[7] = users[indxuser];
        return trama;
    }
     */
    private byte[] crearAARQ(byte[] t3, String pass) {
        Vector<String> trama = new Vector<>();
        trama.add("7E");
        trama.add("A0");
        //datos
        trama.add(Integer.toHexString((46 + pass.length())).toUpperCase());
        for (int i = 3; i < 44; i++) {
            trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
        }
        //password
        for (int i = 0; i < pass.length(); i++) {
            trama.add(convertStringToHex(pass.substring(i, i + 1)).toUpperCase());
        }
        //datos 2da parte
        for (int i = 52; i < t3.length; i++) {
            trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
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
        //tamaño Data
        tramabyte[15] = (byte) (Integer.parseInt(Integer.toHexString((46 + pass.length())), 16) & 0xFF);
        //tamaño password
        tramabyte[43] = (byte) (Integer.parseInt(Integer.toHexString(pass.length()), 16) & 0xFF);
        //tamaño password +2
        tramabyte[41] = (byte) (Integer.parseInt(Integer.toHexString(pass.length() + 2), 16) & 0xFF);

        return tramabyte;
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

    public byte RR_CTRL(int nr) {
        return (byte) (((byte) (nr << 5)) | 0x11);
    }

    public byte I_CTRL(int nr, int ns) {
        return (byte) ((byte) ((byte) ((nr << 5) | (ns << 1)) | 0x10) & 0xFE);
    }

    private void validarTipoTrama(String tipotrama) {
        switch (tipotrama) {
            case "97":
                //VIC
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

    private void enviaTotalAcumulado() {
        byte trama[] = tramaactaris.getGetRequestAcumulados();
        trama = asignaDirecciones(trama);
        trama[4 + numBytesDir] = I_CTRL(nr, ns);
        trama[17 + numBytesDir] = (byte) (nOBIS_acumulado & 0xFF);
        trama = calcularnuevocrcI(trama);
        lconsumosAcumulados = true;
        ultimatramaEnviada = trama;
        enviatrama2_2(trama, "=> Solicitud Consumos Acumulados");
    }

    private void enviaTarifas() {
        primeraTramaBloque = true;
        nintervalosperfil = 0;
        lprimerBloque = true;
        numerobloque = 0;
        ultimbloquePerfil = false;
        primerBloqueTarifas = true;
        acumuladosArrL = new ArrayList<>();
        byte trama[] = tramaactaris.getTariffSummary();
        trama = asignaDirecciones(trama);
        trama[4 + numBytesDir] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama);
        ltarifas = true;
        ultimatramaEnviada = trama;
        enviatrama2_2(trama, "=> Solicitud Tarifas");
    }

    private void procesaAcumulados() {
        procesarTarifas();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                escribir("Registro acumulado: " + (j + 1) + ", tarifa: " + i + "-> Fecha: " + tsAcumulados[i][j] + ", Valor: " + valAcumulados_Tarifas[i][j]);
            }
        }
        cp.almacenarAcumulados(valAcumulados_Tarifas, tsAcumulados, obisEnergias_Acumuladas, unidadesEnergias_Acumuladas, seriemedidor, file);
    }
    
    private void procesarTarifas() {
        boolean[] decisionArr;
        int tamano;
        String valueToCatch;
        boolean isTheValue = false;
        int idxCanal = 0, idxTarifa = 0;       
        String[] acumuladosArr = new String[acumuladosArrL.size()];
        acumuladosArr = acumuladosArrL.toArray(acumuladosArr);
        for(int i = 0; i < acumuladosArr.length; i++) {
            decisionArr = findDataType(acumuladosArr[i]);
            if (decisionArr[0]) { // Tiene especificador de tamaño?              
                i ++;
                tamano = Integer.parseInt(acumuladosArr[i]);
            } else { // Tamano fijo según el tipo de dato
                tamano = findFixedLength(acumuladosArr[i]);
            }
            System.out.println("Tamaño: " + tamano);
            if (decisionArr[1]) { // Debemos capturar algún valor?
                valueToCatch = "";
                i++;
                for (int j = 0; j < tamano; j++) {
                    valueToCatch += acumuladosArr[i+j];
                }                
                i += tamano-1;
                escribir("Catched Value: " + valueToCatch);
                if (isTheValue) {
                    isTheValue = false;
                    escribir("Valor acumulado del Canal " + idxCanal + " y tarifa " + idxTarifa + " :" + Long.parseLong(valueToCatch, 16) / 1000.0);
                    tsAcumulados[idxTarifa][idxCanal - 1] = tsTarifas;
                    valAcumulados_Tarifas[idxTarifa][idxCanal - 1] = Long.parseLong(valueToCatch, 16) / 1000.0;
                }
                int idx = valueToCatch.length() - 2;
                String _lastByte = valueToCatch.substring(idx);
                if ( tamano == 6 && _lastByte.equalsIgnoreCase("FF") ) {//Es un obis
                    escribir("Es un OBIS");
                    idxCanal = Integer.parseInt(valueToCatch.substring(4,6), 16);
                    idxTarifa = Integer.parseInt(valueToCatch.substring(8,10), 16);
                    isTheValue = true;
                }
            }
        }
    }

    private boolean[] findDataType(String _byteStr) {
        int _byte = Integer.parseInt(_byteStr, 16);
        switch(_byte) {
            case 1:
                System.out.println("Array");
                return new boolean[] {true, false};                
            case 2:
                System.out.println("Structure");
                return new boolean[] {true, false}; 
            case 6:
                System.out.println("uInt32");
                return new boolean[] {false, true};                
            case 9:
                System.out.println("Octet String");
                return new boolean[] {true, true};               
            case 15:
                System.out.println("Int8");
                return new boolean[] {false, true}; 
            case 22:
                System.out.println("Enum");
                return new boolean[] {false, true};                 
            default:
                return new boolean[] {false, false};                
        }
    }
    
    private int findFixedLength(String _byteStr) {
        int _byte = Integer.parseInt(_byteStr, 16);
        switch(_byte) {
            case 6:
                return 4;                            
            case 15:
                return 1; 
            case 22:
                return 1;               
            default:
                return 1;            
        }
    }
    
    private void procesaInfoPerfil() {
        try {
            int pos = 0;
            obis = new String[numcanales];
            conskePerfil = new String[numcanales];
            unidades = new String[numcanales];
            for (int i = 0; i < numcanales; i++) {
                pos = pos + 2;
                obis[i] = infoPerfil.get(pos + 2) + infoPerfil.get(pos + 3);
                pos = pos + 9;
                conskePerfil[i] = String.valueOf(Math.pow(10, cp.ActarisComplemento2(((byte) (Integer.parseInt(infoPerfil.get(pos), 16) & 0xFF)))));
                pos = pos + 2;
                unidades[i] = infoPerfil.get(pos);
                pos++;
            }
            escribir("Configuracion de canales");
            for (int i = 0; i < numcanales; i++) {
                escribir("Canal " + i);
                escribir("codigo obis de canal " + i + " " + Integer.parseInt(obis[i], 16));
                //System.out.println("codigo obis de canal " + i + " " + Integer.parseInt(obis[i], 16));
                escribir("ke de canal " + i + " " + conskePerfil[i]);
                try {
                    escribir("Descripcion " + cp.obtenerDescripcionCanalActaris(String.valueOf(Integer.parseInt(obis[i], 16)), null));
                    escribir("unidad de canal " + i + " " + cp.obtenerUnidadActaris(String.valueOf(Integer.parseInt(obis[i], 16)), null));
                } catch (Exception e) {
                    lecturacorrecta = false;
                    escribir("Error en procesamiento de configuracion de perfil");
                    escribir(getErrorString(e.getStackTrace(), 3));
                }
            }
        } catch (Exception e) {
            lecturacorrecta = false;
            escribir("Error en procesamiento de configuracion de perfil");
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    //new
    public void AlmacenaPerfilActaris(String seriemedidor, Vector<String> vPerfilCarga, String[] infoPerfil, String[] conskePerfil, String[] unidades, int intervalos, int periodoIntegracion, Timestamp fechaIniReq) {
        try {
            escribir("INICIO Procesamiento de perfil de carga " + new Date());
            Connection conn = cp.getConn();
            Vector<EConstanteKE> constantes = cp.buscarConstantesKe(seriemedidor);
            int constantesmatch = 0;
            boolean validNulls;
            for (String infoPerfil1 : infoPerfil) {
                if (buscarConske(constantes, Integer.parseInt(infoPerfil1, 16)) != null) {
                    constantesmatch++;
                }
            }
            //System.out.println("Número de constantes encontradas en configuración de perfil: " + constantesmatch);
            escribir("Número de constantes encontradas en configuración de perfil: " + constantesmatch);

            Vector<EtipoCanal> ltiposcanal = cp.buscarTipoCanalesActaris("2");
            Vector<Electura> vlec = new Vector<Electura>();

            SimpleDateFormat sgfactaris = new SimpleDateFormat("yyMMddHHmmss");
            SimpleDateFormat df3 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Timestamp red1 = null;
            Timestamp red2 = null;

            int ncanales = infoPerfil.length;

            Timestamp fechaactual = null;
            Timestamp fechatempmilis = null;
            Timestamp ufeclec = cp.findUltimafechaLec(seriemedidor);
            Electura lec = null;
            Timestamp fechaintervalo = null;
            Timestamp fultimointervalo = null;
            EConstanteKE econske = null;

            Vector<String> dataPerfilCarga = vPerfilCarga;
            int pos = 0;
            int hora = 0;
            int min = 0;
            String fecha = Integer.parseInt((dataPerfilCarga.get(6) + dataPerfilCarga.get(7)), 16) + "/" + Integer.parseInt(dataPerfilCarga.get(8), 16) + "/" + Integer.parseInt(dataPerfilCarga.get(9), 16);
            ////System.out.println("Fecha inicio perfil de carga "+fecha);
            if (!dataPerfilCarga.get(11).equals("FF")) {
                //capturamos la hora y el minuto
                hora = Integer.parseInt(dataPerfilCarga.get(11), 16);
                min = Integer.parseInt(dataPerfilCarga.get(12), 16);
                if (Integer.parseInt(dataPerfilCarga.get(12), 16) % periodoIntegracion != 0) {
                    min = Integer.parseInt(dataPerfilCarga.get(12), 16) - (Integer.parseInt(dataPerfilCarga.get(12), 16) % periodoIntegracion);//                   
                }
                fecha = fecha + " " + hora + ":" + min;
            } else {
                if (periodoIntegracion == 60) {
                    fecha = fecha + " 01:00"; // el rango del día va de 1:00 am a 12:00 am (pasando por las 12 del medio día)
                } else {
                    fecha = fecha + " 00:" + periodoIntegracion; //00:00 es en realidad el último intervalo del día, se inicia desde las 00:pi am hasta 12:(60-pi) am
                }
            }
            boolean ingresa;
            int nintervalo = 0;
            String fechadia = null;
            String fechadiahoras = fecha;
            Timestamp fechadiaT = new Timestamp(df3.parse(fechadiahoras).getTime());
            Timestamp comparafecha = fechadiaT; //para comparar las cuatro fechas de cada registro
            fultimointervalo = fechadiaT;
            Timestamp fechaCero = null;
            //System.out.println("Procesa primer dia: " + fechadiaT);
            escribir("Procesa primer dia: " + fechadiaT);
            int intervalodia = 0;
            String fechatemp = null;
            int indxlec = 0;
            int minutos = 0;
            boolean fechanula = false;
            boolean primerintervalo = true;
            //**** Modificacion lecturas incompletas del perfil
            //en caso de error guarda lo que tenga en el vector de lecturas
            try {
                for (int j = 0; j < intervalos; j++) {
                    int eltos = Integer.parseInt(dataPerfilCarga.get(pos + 1), 16);
                    int cuenta = 0;
                    pos = pos + 2;
                    boolean lecturavalida = true;
                    fechanula = true;
                    validNulls = false;
                    //Procesa fecha
                    while (cuenta < eltos - ncanales) {//elementos del registro menos el número de canales me dará el número de elementos adicionales (ceros o fechas)
                        if (dataPerfilCarga.get(pos).equals("00")) {
                            cuenta++;
                            pos = pos + 1;
//                            fechanula = true;
                        } else if (dataPerfilCarga.get(pos).equals("02") && dataPerfilCarga.get(pos + 1).equals("02")) {//cambio de dia o evento                            
                            fechanula = false;//dici
                            cuenta++;
                            if (!dataPerfilCarga.get(pos + 4).equals("FF"))//cambio de dia
                            {
                                fechadia = "" + Integer.parseInt(dataPerfilCarga.get(pos + 4) + dataPerfilCarga.get(pos + 5), 16);//ano
                                fechadia = fechadia + "/" + Integer.parseInt(dataPerfilCarga.get(pos + 6), 16);//mes
                                fechadia = fechadia + "/" + Integer.parseInt(dataPerfilCarga.get(pos + 7), 16);//dia
                                if (!dataPerfilCarga.get(pos + 9).equals("FF")) {// con hora
                                    //capturamos la hora y el minuto
                                    hora = Integer.parseInt(dataPerfilCarga.get(pos + 9), 16);
                                    if (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion != 0) {
                                        minutos = periodoIntegracion + (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) - (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion));//                   
                                    } else {
                                        minutos = Integer.parseInt(dataPerfilCarga.get(pos + 10), 16);
                                    }
                                    minutos = (minutos == 60 ? 0 : minutos);
                                    fechadiahoras = fechadia + " " + hora + ":" + minutos;
                                } else {//sin hora
                                    if (periodoIntegracion == 60) {
                                        fechadiahoras = fechadia + " 01:00"; // el rango del día va de 1:00 am a 12:00 am (pasando por las 12 del medio día)
                                    } else {
                                        fechadiahoras = fechadia + " 00:" + periodoIntegracion; //00:00 es en realidad el último intervalo del día, se inicia desde las 00:pI am hasta 12:(60-pI) am
                                    }
                                }
                                Timestamp fechadiaActual = new Timestamp(df3.parse(fechadiahoras).getTime());
                                if (fechadiaT.getYear() != fechadiaActual.getYear() || fechadiaT.getMonth() != fechadiaActual.getMonth() || fechadiaT.getDate() != fechadiaActual.getDate()) {
                                    fechadiaT = new Timestamp(df3.parse(fechadiahoras).getTime()); //cambio de día
                                    escribir("Procesa dia: " + fechadiaT);
                                    intervalodia = 0;
                                }

                                if (dataPerfilCarga.get(pos + 18).equals("40") || dataPerfilCarga.get(pos + 18).equals("80")) {//modificacion eventos dentro del perfil PRUEBA                                    
                                    escribir("Evento tipo " + dataPerfilCarga.get(pos + 18) + " - En la fecha y hora: " + fechadiahoras);
                                }
                                comparafecha = (fechadiaActual.getTime() > comparafecha.getTime() ? fechadiaActual : comparafecha);
                            } else//evento
                            {
                                fechatemp = fechadia + " " + Integer.parseInt(dataPerfilCarga.get(pos + 9), 16);//hora
                                if (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion != 0) {
                                    minutos = periodoIntegracion + (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) - (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion));//                   
                                } else {
                                    minutos = Integer.parseInt(dataPerfilCarga.get(pos + 10), 16);
                                }
                                if (minutos == 60) {
                                    minutos = 0;
                                    fechatemp = fechadia + " " + (Integer.parseInt(dataPerfilCarga.get(pos + 9), 16) + 1);//hora
                                }
                                // los minutos se desplazan hacia la derecha
                                fechatemp = fechatemp + ":" + minutos;
                                //System.out.println("Evento tipo " + dataPerfilCarga.get(pos + 18) + " - En la fecha y hora: " + fechatemp);
                                escribir("Evento tipo " + dataPerfilCarga.get(pos + 18) + " - En la fecha y hora: " + fechatemp);
                                fechatempmilis = new Timestamp(df3.parse(fechatemp).getTime());
                                comparafecha = (fechatempmilis.getTime() > comparafecha.getTime() ? fechatempmilis : comparafecha);
                            }
                            pos += 19; //siguiente elemento después de procesar una fecha
                        } else {
                            fechanula = false;//dici
                            lecturavalida = false; //fecha no procesada
                        }
                    }
                    //Fecha del intervalo
                    if (fechanula) {
                        //fultimointervalo nunca es nulo inicialmente, ya que siempre llegará una fecha en el primer registro y fechanula es false
                        fechaintervalo = new Timestamp(fultimointervalo.getTime() + (60000L * (long) periodoIntegracion));
                    } else {
                        fechaintervalo = comparafecha;
                    }
                    //Nulos                    
                    red1 = fultimointervalo; //new Timestamp(df3.parse(fechaini).getTime());
                    red2 = fechaintervalo; //new Timestamp(df3.parse(fechafin).getTime());                    
                    if (red2.after(new Timestamp(fechaIniReq.getTime() + (60000L * (long) periodoIntegracion)))) {
                        validNulls = true;
                        if (red1.before(new Timestamp(fechaIniReq.getTime() + (60000L * (long) periodoIntegracion)))) {
                            red1 = new Timestamp(fechaIniReq.getTime() + (60000L * (long) periodoIntegracion));
                        }
                    }
                    if (red1.getMinutes() % periodoIntegracion != 0) {
                        red1.setMinutes((red1.getMinutes() - (red1.getMinutes() % periodoIntegracion)));
                    }
                    if (red2.getMinutes() % periodoIntegracion != 0) {
                        red2.setMinutes((red2.getMinutes() - (red2.getMinutes() % periodoIntegracion)));
                    }
                    //escribir("Revisa nulos entre las fechas aproximadas: " + red1 + " y " + red2);
                    int aumento = 0;//Math.abs((redondeo(fechafin)-redondeo(fechaini))) /periodoIntegracion-1;
//                                aumento = (int) Math.abs((((red2.getTime() - red1.getTime()) / 60000) / periodoIntegracion)) - 1;
                    aumento = (int) (((red2.getTime() - red1.getTime()) / 60000) / periodoIntegracion) - 1;
                    if (aumento > 0 && validNulls) {
                        //if (aumento > 0) {
                        for (int i = 0; i < aumento; i++) {
//                                        fechaintervalo = new Timestamp(fechalec.getTime() + ((60000 * periodoIntegracion) * (intervalodia + i)));
                            fechaCero = new Timestamp(fultimointervalo.getTime() + (60000L * (long) periodoIntegracion) * (i + 1));
                            //escribir("Fecha Lectura nulo: " + fechaCero);
//                                        canalesmedidor = 0;
                            for (int k = 0; k < ncanales; k++) {
                                lec = new Electura(fechaCero, seriemedidor, Integer.parseInt(infoPerfil[k], 16),
                                        0, 0, periodoIntegracion, obtenerUnidadActaris(ltiposcanal, String.valueOf(Integer.parseInt(infoPerfil[k], 16))));
                                ingresa = true;
                                if (ingresa) {
                                    //almacenamos la lectura
                                    //validamos si la fecha es despues de la fecha de la ultima lectura                                                
                                    econske = buscarConske(constantes, Integer.parseInt(infoPerfil[k], 16));
                                    if (econske != null) {
                                        lec.setLec(trasnformarEnergia(lec.getLec(), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                        if (lec.vccanal.toUpperCase().contains("K")) {
                                            lec.setLec(lec.getLec() / 1000);
                                        }
                                        //System.out.println("Registro nulo en: " + lec.getFecha());
                                        escribir("Registro nulo en: " + lec.getFecha());
                                        vlec.add(lec);
                                    }
                                }
                            }
                        }
                        intervalodia = intervalodia + aumento;
                        nintervalo = nintervalo + aumento;
                        fultimointervalo = fechaCero;
                    }//Termina nulos  
                    // Procesa canales y almacena lectura
//                    canalesmedidor = 0;
                    for (int i = 0; i < ncanales; i++) {//
                        ingresa = false;
                        if (dataPerfilCarga.get(pos).equals("10")) {
                            lec = new Electura(fechaintervalo, seriemedidor, Integer.parseInt(infoPerfil[i], 16),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2), 16) * Double.parseDouble(conskePerfil[i])),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2), 16) * Double.parseDouble(conskePerfil[i])),
                                    periodoIntegracion, obtenerUnidadActaris(ltiposcanal, String.valueOf(Integer.parseInt(infoPerfil[i], 16))));
                            pos = pos + 3;
                            ingresa = true;
                        } else if (dataPerfilCarga.get(pos).equals("05")) {
                            lec = new Electura(fechaintervalo, seriemedidor, Integer.parseInt(infoPerfil[i], 16),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2) + dataPerfilCarga.get(pos + 3) + dataPerfilCarga.get(pos + 4), 16) * Double.parseDouble(conskePerfil[i])),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2) + dataPerfilCarga.get(pos + 3) + dataPerfilCarga.get(pos + 4), 16) * Double.parseDouble(conskePerfil[i])),
                                    periodoIntegracion, obtenerUnidadActaris(ltiposcanal, String.valueOf(Integer.parseInt(infoPerfil[i], 16))));//                       
                            pos = pos + 5;
                            ingresa = true;
                        }
                        if (lecturavalida) {
                            if (ingresa) {
                                //almacenamos la lectura
                                //validamos si la fecha es despues de la fecha de la ultima lectura                                
                                econske = buscarConske(constantes, Integer.parseInt(infoPerfil[i], 16));
                                if (econske != null) {
                                    lec.setLec(trasnformarEnergia(lec.getLec(), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                    if (lec.vccanal.toUpperCase().contains("K")) {
                                        lec.setLec(lec.getLec() / 1000);
                                    }
                                    //*** modificacion para actualizar la fecha d eultima lectura correctamente      
                                    fechaactual = lec.getFecha();
                                    //Caso no observable
                                    if (!primerintervalo && fechaintervalo.getTime() == fultimointervalo.getTime()) {//fecha actual igual a la fecha pasada 
                                        indxlec = vlec.size() - constantesmatch; //obtiene la posición del intervalo pasado
                                        lec.setLec(vlec.get(indxlec).getLec() + lec.getLec()); //toma la lectura del intervalo pasado y lo suma al actual
                                        lec.setPulso(vlec.get(indxlec).getPulso() + lec.getLec()); //toma el pulso del intervalo pasado y lo suma al actual
                                        vlec.remove(indxlec);// elimina el registro del intervalo pasado ya que ya fue sumado
                                        //escribir("(Almacena) Elimina y suma intervalo en: " + lec.getFecha());
                                        vlec.add(lec);//almacena la suma
                                    } else {
                                        //escribir("Registro en: " + lec.getFecha());
                                        vlec.add(lec);
                                    }
                                }
                            }
                            lec = null;
                        }
                    }
                    if (lecturavalida) {
                        nintervalo++;
                        intervalodia++;
                    }
                    fultimointervalo = fechaintervalo;
                    primerintervalo = false;
                }
                escribir(nintervalo + " de " + intervalos + " procesados");
            } catch (Exception e) {
                //Modificacion en caso de error en desbordamiento por perfil incompleto se guarda lo que este en el vector de perfil
                e.printStackTrace();
                lecturacorrecta = false;
                escribir("Error de desborde de perfil de carga incompleto");
                escribir("Inicia contingencia");
                cp.actualizaLecturaDesfase(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                lecturacorrecta = cp.isLecturacorrecta();
                if (fechaactual != null) {
                    if (fechaactual.after(ufeclec)) {
                        escribir("Actualiza fecha de ultima lectura con perfil incompleto" + sgfactaris.format(new Date(fechaactual.getTime())));
                        cp.actualizaFechaLectura(seriemedidor, sgfactaris.format(new Date(fechaactual.getTime())));
                    }
                }
                escribir("Prepara reinicio de contingencia por perfil incompleto");
            }
            cp.actualizaLecturaDesfase(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
            if (fechaactual != null) {
                if (fechaactual.after(ufeclec)) {
                    escribir("Actualiza fecha de ultima lectura " + sgfactaris.format(new Date(fechaactual.getTime())));
                    cp.actualizaFechaLectura(seriemedidor, sgfactaris.format(new Date(fechaactual.getTime())));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //cierrra new

    public EConstanteKE buscarConske(Vector<EConstanteKE> constantes, int canal) {
        EConstanteKE res = null;
        for (EConstanteKE buscar : constantes) {
            if (buscar.getCanal() == canal) {
                res = buscar;
                break;
            }
        }
        return res;
    }

    private String obtenerUnidadActaris(Vector<EtipoCanal> ltiposcanal, String canal) {
        String res = null;
        for (EtipoCanal buscar : ltiposcanal) {
            if (buscar.getCanal().equals(canal)) {
                res = buscar.getUnidad();
                break;
            }
        }
        return res;
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

    private void procesaDatos() {
        AlmacenaPerfilActaris(seriemedidor, vPerfilCarga, obis, conskePerfil, unidades, nintervalosperfil, periodoIntegracion, new Timestamp(dateIniReq.getTime()));
    }

    private void procesaInfoEventos() {
        cp.AlmacenaEventosActaris(seriemedidor, vEventos, nintervaloseventos);
    }

    private String[] cortarTrama(String[] vectorhex, int tamaño) {
        String nuevoVector[] = new String[tamaño + 2];
        System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
        ////System.out.println("Nueva trama");
        for (int i = 0; i < nuevoVector.length; i++) {
            //System.out.print(" " + nuevoVector[i]);
        }
        return nuevoVector;
    }

    public void terminaHilos() {
        try {
            port.interrupt();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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
        port = null;
        tEscritura = null;
        tReinicio = null;

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
        return resultado.trim();
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
