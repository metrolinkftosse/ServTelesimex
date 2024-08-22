/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasLocalActaris;
import Entidades.Abortar;
import Entidades.EConfModem;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Util.SynHoraNTP;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.Vector;
import javax.comm.*;
import org.apache.commons.io.input.CountingInputStream;

/**
 *
 * @author dperez
 */
public class LeerRemotoPSTNActarisSL7000 implements Runnable, SerialPortEventListener {

    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    String usuario = "admin";
    Date d = new Date();
    Abortar objabortar;
    SerialPort serialPort;
    Enumeration portList;
    private CommPortIdentifier portId;
    boolean portFound = false;
    String seriemedidor = "";
    boolean rutinaCorrecta = false;
    CountingInputStream input;
    BufferedOutputStream output;
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
    boolean lconfhora;
    public boolean leer = true;
    String numeroPuerto;
    int numeroReintentos = 4;
    int nreintentos = 0;
    int velocidadPuerto;
    long timeout;
    int ndias;
    String fechaActual;
    boolean portconect = false;
    long tiemporetransmision = 0;
    Timestamp time = null; //tiempo de NTP
    Timestamp tsfechaactual;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    long ndesfasepermitido = 0;
    boolean solicitar; //variable de control de la sync
    int actualReintento = 0;
    int reintentoadp = 0;
    Thread port = null;
    Thread port2 = null;
    Thread port3 = null;
    boolean inicia1 = false;
    public boolean cierrapuerto = false;
    Socket socket;
    boolean escucha = true;
    Thread escuchar;
    private int reintentoconexion = 0;
    boolean llegotramaini = false;
    boolean esperandoconexion = true;
    //estados
    //*************************************
    boolean lA = false;
    boolean lC = false;
    boolean SNRMUA = false;
    boolean lcontraseña = false;
    boolean lfirmware = false;
    boolean lserialnumber = false;
    boolean lfechaactual = false;
    boolean lparamperfil = false;
    boolean linfoperfil = false;
    boolean lfechaactual2 = false;
    boolean lfechaactual3 = false;
    boolean lperfilcarga = false;
    boolean lPowerFailureElements = false;
    boolean lterminar = false;
    private boolean lReset = false;
    boolean lfechasync = false;
    //************************************
    boolean enviando = false;
    boolean reenviando = false;
    byte[] ultimatramaEnviada = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    int numcanales = 2;
    int intervalo = 0;
    int factorIntervalo = 0;
    int ndia = 3;
    int dirfis = 0;
    int dirlog = 0;
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    public String tramaIncompleta = "";
    public boolean complemento = false;
    public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
    int reinicio = 0;
    int ns = 0;
    int nr = 0;
    int nrEsperado = 0;
    int nsEsperado = 0;
    boolean ultimbloquePerfil = false;
    boolean primeraTramaBloque = false;
    int numerobloque;
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
    private boolean primerbloque;
    private boolean ultimobloqueEventos = false;
    Vector<EConfModem> confModem;
    int indiceconexion = 0;
    boolean conexionModem = false;
    boolean desbloqueo = false;
    boolean lconfiguracion = false;
    int actualUltimatrama = 0;
    long runvar = 0;
    int reintentosUltimatrama = 5;
    byte users[] = {(byte) 0x03, (byte) 0x05, (byte) 0x07, (byte) 0x0F, (byte) 0xF5};
    private int indxuser = 0;
    private boolean ultimatramabloque = false;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    ZoneId zid;

    public LeerRemotoPSTNActarisSL7000(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, ZoneId zid) {
        this.med = med;
        this.confModem = confmodem;
        this.cp = cp;
        this.zid = zid;
        File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
        try {
        if (!f.exists()) {
            f.mkdirs();
        }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        //System.out.println(this.med.getMarcaMedidor().getNombre());
//        file = new File(cp.rutalogs + "" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");
        file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");

        try {
            if (file.exists()) {
                existearchivo = true;
            } else {
                //System.out.println();
            }
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

    public void run() {
        try {
            Thread.sleep(runvar);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    public void jinit() {
        tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        if (confModem.size() > 0) {
            indxuser = 0;
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            portList = CommPortIdentifier.getPortIdentifiers();
            timeout = (long) (med.getTimeout());
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "0" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "0" : med.getDireccionLogica()));


            while (portList.hasMoreElements()) {
                portId = (CommPortIdentifier) portList.nextElement();
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (portId.getName().equals(numeroPuerto)) {
                        portFound = true;
                        //System.out.println("Puerto " + numeroPuerto);
                        break;
                    }
                }
            }
            try {
                //System.out.println("puerto " + portFound);
                if (portFound) {
                    //abre puerto
                    abrePuerto();
                    tiempo = 1000;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (portconect) {
                iniciacomunicacion();
            } else {
                cerrarLog("No conectado", false);
                leer = false;
            }

        } else {
            try {
                String mensaje = "Error en configuracion no se encontro configuracion para el modem " + med.getMarcaModem().getNombre() + " y la marca " + med.getMarcaMedidor().getNombre();
                escribir(mensaje);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            cerrarLog("Modem no configurado", false);
            leer = false;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (!rutinaCorrecta) {
            switch (event.getEventType()) {
                case SerialPortEvent.BI:

                case SerialPortEvent.OE:

                case SerialPortEvent.FE:

                case SerialPortEvent.PE:

                case SerialPortEvent.CD:

                case SerialPortEvent.CTS:

                case SerialPortEvent.DSR:

                case SerialPortEvent.RI:

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    break;

                case SerialPortEvent.DATA_AVAILABLE: {
                    //hay datos...
                    try {
                        Thread.sleep(tiempo * 2);
                    } catch (Exception e) {
                        System.err.println("Error en hilo Data_Avaliable");
                    }
                    procesaCadena();
                }
            }
        }
    }

    public void abrePuerto() {
        reintentosUtilizados++;
        portconect = false;
        actualUltimatrama = 0;
        try {
            //parametrizacion del puerto, lo abre a 1200 baud
            try {
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            CommPort puerto = portId.open("AbreSerial" + med.getnSerie(), velocidadPuerto);
            serialPort = (SerialPort) puerto;
            portconect = true;
            if (aviso) {
                //lm.jtablemedidores.setValueAt("Abre puerto..", indx, 3);
                try {
                    //lm.mdc.fireTableDataChanged();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }

            }
            serialPort.setSerialPortParams(velocidadPuerto, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            input = new CountingInputStream(serialPort.getInputStream());
            output = new BufferedOutputStream(serialPort.getOutputStream());
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnOutputEmpty(true);
        } catch (Exception e) {
            escribir(e.getMessage());
            if (portconect) {
                cerrarPuerto();
            }
            if (aviso) {
                //lm.jtablemedidores.setValueAt("No leido", indx, 3);
                try {
                    //lm.mdc.fireTableDataChanged();
                } catch (Exception ef) {
                }
            }

            //e.printStackTrace();
        }

    }

    public void iniciacomunicacion() {
        tiemporetransmision = 5000;
        try {
            String mensaje = "INICIO DE SESSION --" + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()) + "--";
            escribir(mensaje);
            mensaje = "Medidor: "+med.getMarcaMedidor().getNombre()+ ", Serie: "+med.getnSerie();
            escribir(mensaje);
            mensaje = "Velocidad de conexion " + med.getVelocidadpuerto().getDescripcion();
            escribir(mensaje);
            mensaje = "Puerto " + med.getPuertocomm();
            escribir(mensaje);
            mensaje = "Direccion IP/Puerto" + med.getDireccionip() + "/" + med.getPuertoip();
            escribir(mensaje);
            mensaje = "Numero de reintentos " + med.getReintentos();
            escribir(mensaje);
            mensaje = "Numero de dias default " + med.getNdias();
            escribir(mensaje);
            mensaje = "Timeout " + med.getTimeout();
            escribir(mensaje);
            mensaje = "Password " + med.getPassword();
            escribir(mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (portconect) {
            escribir("Puerto Abierto " + med.getPuertocomm());
            if (med.isLconfigurado()) {
                //si esta configurado iniciamos la omunicacion con el modem
                if (med.getTipoconexion() == 1) {
                    //pstn
                    //modificacion desbloqueo de modem
                    actualReintento++;
                    indiceconexion = 0;
                    lconfiguracion = false;
                    inicia1 = false;
                    byte[] vtramaini = cp.ConvertASCIItoHex(cp.convertStringToHex("+++"));
                    try {
                        String mensaje = "=> Envio +++";
                        escribir(mensaje);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    conexionModem = true;
                    desbloqueo = true;
                    enviaTramaInicial(vtramaini);
                    while (enviando) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    }
                    try {
                        Thread.sleep((long) 4000);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    if (inicia1) {
                        String tramainicial = confModem.get(0).getPeticion0();
                        tramainicial = tramainicial.replace("2E", "0D");
                        ////System.out.println("No respondio No carrier iniciamos la comunicacion" + tramainicial);
                        try {
                            if (aviso) {
                                //lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                                //lm.mdc.fireTableDataChanged();
                            }
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                        String envia = "=> Trama AT " + tramainicial;
                        escribir(envia);
                        byte[] vtramainicial = cp.ConvertASCIItoHex(tramainicial);
                        indiceconexion++;
                        desbloqueo = false;
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }

                        enviaTrama(vtramainicial);
                    }
                } else {
                    cerrarPuerto();
                    cerrarLog("No configurado", false);
                    escribir("Estado Lectura No Leido");
                    leer = false;
                }
            } else {
                //si no esta configurado cerramos el puerto
                cerrarPuerto();
                cerrarLog("No configurado", false);
                escribir("Estado Lectura No Leido");
                leer = false;
            }
        } else {
            cerrarLog("No configurado", false);
            leer = false;
        }
    }

    private void enviaTrama2(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;
        enviando = true;
        if (port != null) {
            if (port.isAlive()) {
                port.stop();
                port = null;
            }
        }
        port = new Thread() {
            @Override
            public void run() {
                try {
                    int intentosRetransmision = 0;
                    boolean t = true;
                    while (t) {
                        if (enviando) {
                            if (intentosRetransmision != 0) {
                                escribir("TimeOut, Intento de reenvio..");
                            }
                            escribir(des);
                            //System.out.println(des);
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramaactaris.encode(trama, trama.length));
                            //System.out.println(tramaactaris.encode(trama, trama.length));
                            enviaTrama(trama);
                            intentosRetransmision++;
                        } else {
                            t = false;
                        }
                        if (intentosRetransmision > 4) {
                            escribir("Numero de reenvios agotado");
                            //reiniciaComunicacion();
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {
                            Thread.sleep(tiemporetransmision);
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;
                        cerrarPuerto();
                        if (lperfilcarga) {
                            if (!lperfilcompleto) {
                                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                                    procesaInfoPerfil();
                                    procesaDatos();
                                }
                            }
                            lperfilcarga = false;
                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //terminahilos();
                            escribir("Numero de reintentos agotado");
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            escribir("Estado Lectura No Leido");
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    avisoStr("No Leido");
                    escribir("Error en hilo de envia trama tipo 2");
                    cerrarPuerto();
                    leer = false;
                }
            }
        };
        port.start();
    }

    private void procesaCadena() {
        //tamaño buffer
        byte[] readBuffer = new byte[96000];
        try {
            int numbytes = 0;
            //si el puerto tiene datos llenamos el buffer con lo que se encuentra en el puerto.
            while (input.available() > 0) {
                numbytes = input.read(readBuffer);
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramaactaris.encode(readBuffer, numbytes);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
            //luego de tener la trama desglosada byte x byte continuamos a interpretarla
            if (cadenahex.length() > 0) {
                //System.out.println("LLega dato");
                interpretaCadena(cadenahex);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviaTramaInicial(byte[] bytes) {
        final byte[] trama = bytes;

        enviando = true;
        if (port2 != null) {
            if (port2.isAlive()) {
                port2.stop();
                port2 = null;
            }
        }
        port2 = new Thread() {
            public void run() {
                try {
                    int intentosRetransmision = 0;
                    boolean t = true;
                    while (t) {
                        if (enviando) {
                            try {
                                String mensaje = "=> Envio +++";
                                //System.out.println();
                                escribir(mensaje);
                            } catch (Exception e) {
                                 e.printStackTrace();
                            }
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramaactaris.encode(trama, trama.length));
                            //System.out.println(tramaactaris.encode(trama, trama.length));
                            enviaTrama(trama);
                            intentosRetransmision++;
                        } else {
                            inicia1 = false;
                            t = false;
                        }
                        if (intentosRetransmision > 5) {
                            escribir("Numero de reenvios agotado");
                            t = false;
                            enviando = false;
                            inicia1 = true;
                        } else {
                            sleep(2000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    avisoStr("No Leido");
                    escribir("Error en hilo de envia trama inicial");
                    cerrarPuerto();
                    leer = false;
                }
            }
        };
        port2.start();
    }

    private void enviaTramaUltima(byte[] bytes, String descripcion) {
        actualUltimatrama++;

        if (actualUltimatrama > reintentosUltimatrama) {
            cerrarPuerto();
            if (numeroReintentos >= actualReintento) {
                if (!objabortar.labortar) {
                    abrePuerto();
                    iniciacomunicacion();
                } else {
                    escribir("Proceso Abortado");
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {
                cerrarLog("Desconexion Numero de reintentos agotado", false);
                escribir("Estado Lectura No Leido");
                leer = false;
            }
        } else {
            final byte[] trama = bytes;
            final byte[] asd = {(byte) 0x15};
            final String des = descripcion;
            reenviando = true;

            if (port3 != null) {
                if (port3.isAlive()) {
                    port3.stop();
                    port3 = null;
                }
            }
            port3 = new Thread() {
                @Override
                public void run() {
                    try {
                        rutinaCorrecta = false;
                        boolean reenviar = false;
                        boolean t = true;
                        escribir("Esperando Retransmision..");
                        while (t) {
                            if (reenviando) {
                                boolean salir = true;
                                int intentosalir = 0;
                                while (salir) {
                                    if (!reenviando || intentosalir > ((7500 / 500) * 2)) {
                                        //System.out.println("Sale enviarTramaultima");
                                        salir = false;
                                        if (!reenviando) {
                                            t = false;
                                        } else {
                                            reenviar = true;
                                            t = false;
                                        }
                                    } else {
                                        intentosalir++;
                                        //System.out.println("Esperando... enviarTramaultima");
                                        sleep(500);
                                    }
                                }

                            } else {
                                //System.out.println("Llego algo Sale enviatramaUltima");
                                t = false;
                            }
                        }
                        //validamos si se acabo el tiempo para reenviar la ultima trama
                        if (reenviar) {
                            //enviamos la ultima trama
                            complemento = false;

                            enviaTrama3(trama, des);
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        avisoStr("No Leido");
                    escribir("Error en hilo de envia trama ultima");
                    cerrarPuerto();
                    leer = false;
                    }
                }
            };
            port3.start();
        }
    }

    private void enviaTrama3(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;
        enviando = true;
        if (port != null) {
            if (port.isAlive()) {
                port.stop();
                port = null;
            }
        }
        port = new Thread() {
            @Override
            public void run() {
                try {
                    int intentosRetransmision = 0;
                    boolean t = true;
                    while (t) {
                        if (enviando) {
                            if (intentosRetransmision != 0) {
                                escribir("TimeOut, Intento de reenvio..");
                            }
                            escribir(des);
                            //System.out.println(des);
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramaactaris.encode(trama, trama.length));
                            //System.out.println(tramaactaris.encode(trama, trama.length));
                            enviaTrama(trama);

                        } else {
                            //System.out.println("Sale enviatrama2");
                            t = false;
                        }
                        if (intentosRetransmision > 4) {
                            escribir("Numero de reenvios agotado");
                            //reiniciaComunicacion();
                            //System.out.println("Numero de reenvios agotado");
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {
                            intentosRetransmision++;
                            boolean salir = true;
                            int intentosalir = 0;
                            while (salir) {
                                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                                    //System.out.println("Sale enviar");
                                    salir = false;
                                    if (!enviando) {
                                        t = false;
                                    }

                                } else {
                                    intentosalir++;
                                    //System.out.println("Esperando... enviatrama2");
                                    sleep(500);
                                }
                            }
                            //sleep(tiemporetransmision);
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;
                        if (lperfilcarga) {
                            if (!lperfilcompleto) {
                                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                                        procesaInfoPerfil();
                                        procesaDatos();

                                }
                            }
                            lperfilcarga = false;
                        }
                        cerrarPuerto();
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            escribir("Estado Lectura No Leido");
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    avisoStr("No Leido");
                    escribir("Error en hilo de envia trama tipo 3");
                    cerrarPuerto();
                    leer = false;
                }
            }
        };
        port.start();
    }

    private void enviaTrama(byte[] bytes) {
        try {
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal
            if (output != null) {

                output.flush();
                //output.close();
            }
//            output = socket.getOutputStream();
            output.write(bytes, 0, bytes.length);
            output.flush();
            //output.close();
            rutinaCorrecta = false;
            //System.out.println("Termina Envio");
            //System.out.println("\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarPuerto() {
        try {
            if (output != null) {
                ////System.out.println("Cierra salida");
                output.flush();
                output.close();
            }
            if (input != null) {
                ////System.out.println("Cierra entrada");
                input.close();
            }
            if (serialPort != null) {
                ////System.out.println("Cierra puerto");
                serialPort.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {

            String mensaje = "FIN DE SESSION --" + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()) + "--";
            escribir(mensaje);
            escribir("\r\n\n");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            enviando = false;
            lconfiguracion = false;
            portconect = false;
            try {
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("Cerrando puerto..", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            Thread.sleep((long) 4000);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void interpretaCadena(String cadenahex) {
        //System.out.println("\n");
        //System.out.println("Tramma llegada ");
        //System.out.println(cadenahex);
        //System.out.println("Fin Tramma llegada ");
        rutinaCorrecta = true;
        enviando = false;
        if (conexionModem) {
            if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52") && !desbloqueo) {
                //no carrier para los casos de que no se llegan los datos
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                if (nreintentos >= numeroReintentos) {
                    nreintentos = 0;
                    cerrarPuerto();
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                } else {
                    nreintentos++;
                    cerrarPuerto();
                    if (numeroReintentos >= actualReintento) {
                        abrePuerto();
                        iniciacomunicacion();
                    } else {
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }
                }
            } else if (indiceconexion == 0 && desbloqueo) {
                String tramainicial = confModem.get(0).getPeticion0();
                tramainicial = tramainicial.replace("2E", "0D");

                byte[] vtramainicial = cp.ConvertASCIItoHex(tramainicial);
                indiceconexion++;
                desbloqueo = false;
                enviaTrama(vtramainicial);
            } else if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52") && desbloqueo) {
                //no carrier para el caso de no recibir datos para el desbloqueo del modem
                String tramainicial = confModem.get(0).getPeticion0();
                tramainicial = tramainicial.replace("2E", "0D");

                byte[] vtramainicial = cp.ConvertASCIItoHex(tramainicial);
                indiceconexion++;
                desbloqueo = false;
                enviaTrama(vtramainicial);

            } else if (cadenahex.contains("4E 4F 20 44 49 41 4C 54 4F 4E 45")) {//no dialtone
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("No Dialtone", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                if (nreintentos >= numeroReintentos) {
                    nreintentos = 0;
                    cerrarPuerto();
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                } else {
                    nreintentos++;
                    cerrarPuerto();
                    if (numeroReintentos >= actualReintento) {
                        abrePuerto();
                        iniciacomunicacion();
                    } else {
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }
                }
            } else if (cadenahex.contains("42 55 53 59")) {//busy
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("Ocupado", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                if (nreintentos >= numeroReintentos) {
                    nreintentos = 0;
                    cerrarPuerto();
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                } else {

                    nreintentos++;
                    cerrarPuerto();
                    if (numeroReintentos >= actualReintento) {
                        abrePuerto();
                        iniciacomunicacion();
                    } else {
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }
                }

            } else if (cadenahex.contains("43 4F 4E 4E 45 43 54")) { //conected
                escribir("<= Conectado.. " + cadenahex);
                avisoStr("Conectado");
                lconfiguracion = true;

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                conexionModem = false;
                tiemporetransmision = (timeout) * 1000;
                SNRMUA = true;
                byte[] trama = tramaactaris.getT1();
                trama = asignaDireciones(trama, dirlog, dirfis);
                trama = calcularnuevocrcI(trama);
                ultimatramaEnviada = tramaactaris.getT1();
                enviaTrama2(trama, "");
            } else if (cadenahex.contains("4F 4B")) {//ok
                escribir("<= OK " + cadenahex);
                nreintentos = 0;
                String trama = confModem.get(0).getpeticion(indiceconexion);

                if (trama.equals("")) {
                    byte[] vtrama = null;
                    if (med.getTipoconexion() == 1) {
                        vtrama = tramaactaris.StringToHexMarcadoPSTN(med.getNumtelefonico().trim());
                    } else {
                        vtrama = tramaactaris.StringToHexMarcadoGPRS(med.getDireccionip().trim() + "/" + med.getPuertoip().trim());
                    }
                    esperandoconexion = true;
                    enviaTramaConexion(vtrama, "=> Solicitud de conexion Numero telefono " + med.getNumtelefonico());

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }

                    tiemporetransmision = 5000;
                    trama = trama.replace("2E", "0D");
                    byte[] vtrama = cp.ConvertASCIItoHex(trama);
                    indiceconexion++;
                    enviaTrama2(vtrama, "=> Envio Trama AT " + trama);
                }
            } else if (cadenahex.contains("4E 4F 20 41 4E 53 57 45 52")) { //NO answer
                //escribir("<= Sin respuesta " + cadenahex);
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("No answer", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                if (nreintentos >= numeroReintentos) {
                    nreintentos = 0;
                    cerrarPuerto();
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                } else {

                    nreintentos++;
                    cerrarPuerto();
                    if (numeroReintentos >= actualReintento) {
                        if (!objabortar.labortar) {
                            abrePuerto();
                            iniciacomunicacion();
                        } else {
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }
                }
            } else if (cadenahex.contains("41 54 44 50") || cadenahex.contains("41 54 44 54")) {
                rutinaCorrecta = false;

            } else {
                if (cadenahex.trim().length() > 0) {
                    cerrarPuerto();
                    if (numeroReintentos >= actualReintento) {
                        if (!objabortar.labortar) {
                            abrePuerto();
                            iniciacomunicacion();
                        } else {
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }
                }

            }
        } else {
            escribir("Recibe <= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (cadenahex.equals("41 54 0D")) {
                reiniciaComuniacion();
            } else if (SNRMUA) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (Integer.parseInt(vectorhex[2], 16) <= vectorhex.length) {
                        if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                            vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
                            if (validacionCRCHCS(vectorhex)) {
                                if (validacionCRCFCS(vectorhex)) {
                                    if (vectorhex[8].equals("73") || vectorhex[8].equals("63")) {
                                        ns = 0;
                                        nr = 0;
                                        escribir("NsPc " + ns + " NrPc " + nr);
                                        reintentoadp = 0;
                                        nrEsperado = ns + 1;
                                        nsEsperado = 0;
                                        SNRMUA = false;
                                        lcontraseña = true;

                                        byte trama[] = crearAARQ(tramaactaris.getT3(), password); //AARQ
                                        trama = asignaDireciones(trama, dirlog, dirfis);
                                        trama = calcularnuevocrcI(trama);

                                        ultimatramaEnviada = trama;
                                        avisoStr("AARQ");
                                        enviaTrama2(trama, "");
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                } else {
                                    //badFCS
                                    //System.out.println("BADFCS");
                                    escribir("BAD FCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud SNRM");

                                }
                            } else {
                                //badHCS
                                //System.out.println("BADHCS");
                                escribir("BAD HCS");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud SNRM");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("NO 7E");
                            escribir("Error trama inicio y final");
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud SNRM");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud SNRM");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud SNRM");
                }
            } else if (lcontraseña) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
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
                                                    reintentoadp = 0;
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "");

                                                } else {
                                                    //no son los ns y nr esperados
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud AARQ");
                                                }
                                            } else {
                                                //reiniciamos
                                                if (vectorhex[38].equals("0D")) {
                                                    escribir("error de autenticacion");
                                                    indxuser++;
                                                    if (indxuser < users.length) {
                                                        lcontraseña = false;
                                                        lReset = true;
                                                        byte trama[] = tramaactaris.getTfinaliza();
                                                        asignaDireciones(trama, dirlog, dirfis);
                                                        trama = calcularnuevocrcRR(trama);
                                                        enviaTrama2(trama, "");
                                                    } else {
                                                        cerrarPuerto();
                                                        avisoStr("No Leido");
                                                        escribir("Desconexion - Error de autenticacion");
                                                        cerrarLog("Error de identificacion de usuario", false);
                                                        leer = false;
                                                    }
                                                } else {
                                                    //System.out.println("Error de protocolo - Fallo en interpretacion de AARE");
                                                    cerrarPuerto();
                                                    avisoStr("No Leido");
                                                    escribir("Desconexion - Fallo en interpretacion de AARE");
                                                    cerrarLog("Fallo en interpretacion de AARE", false);
                                                    leer = false;
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
                                                escribir("NsPc " + ns + " NrPc " + nr);
                                                reintentoadp = 0;
                                                lcontraseña = false;
                                                lfirmware = true;
                                                byte trama[] = tramaactaris.getTfirmware();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama[8] = I_CTRL(nr, ns);
                                                trama = calcularnuevocrcI(trama);
                                                ultimatramaEnviada = trama;
                                                avisoStr("Firmware");
                                                enviaTrama2(trama, "=> Solicitud Firmware");
                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud AARQ");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud SNRM");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud AARQ");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
                            }
                        } else {
                            escribir("trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama Sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
                }
            } else if (lfirmware) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                                vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
                                if (validacionCRCHCS(vectorhex)) {
                                    if (validacionCRCFCS(vectorhex)) {
                                        if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                                            if (vectorhex[17].equals("00")) {//ack data
                                                escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                                                if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                                                    nr++;
                                                    if (nr > 7) {
                                                        nr = 0;
                                                    }
                                                    escribir("NsPc " + ns + " NrPc " + nr);
                                                    reintentoadp = 0;
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Received Ready RR");
                                                } else {
                                                    //no son los ns y nr esperados
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                                                }
                                            } else {
                                                escribir("Negacion Peticion");
                                                lfirmware = false;
                                                lReset = true;
                                                byte trama[] = tramaactaris.getTfinaliza();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama = calcularnuevocrcRR(trama);
                                                enviaTrama2(trama, "");
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
                                                reintentoadp = 0;
                                                lfirmware = false;
                                                lfechaactual = true;
                                                byte trama[] = tramaactaris.getTfechaHora();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama[8] = I_CTRL(nr, ns);
                                                trama = calcularnuevocrcI(trama);
                                                ultimatramaEnviada = trama;
                                                avisoStr("Fecha Actual");
                                                enviaTrama2(trama, "=>Solicitud Fecha Actual");
                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                                }
                            } else {
                                //trama NO /E
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y fin");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                    }
                } else {//trama Sin cabecera
                    escribir("Error trama Sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Firmware");
                }
            } else if (lserialnumber) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                                vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
                                if (validacionCRCHCS(vectorhex)) {
                                    if (validacionCRCFCS(vectorhex)) {
                                        if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                                            if (vectorhex[17].equals("00")) {//ack data
                                                escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                                                if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                                                    nr++;
                                                    if (nr > 7) {
                                                        nr = 0;
                                                    }
                                                    escribir("NsPc " + ns + " NrPc " + nr);
                                                    reintentoadp = 0;
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=>Envia Recieved Ready RR");
                                                } else {
                                                    //no son los ns y nr esperados
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud serial");
                                                }
                                            } else {
                                                escribir("Negacion Peticion");
                                                lserialnumber = false;
                                                lReset = true;
                                                byte trama[] = tramaactaris.getTfinaliza();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama = calcularnuevocrcRR(trama);
                                                enviaTrama2(trama, "");
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
                                                reintentoadp = 0;
                                                lserialnumber = false;
                                                lparamperfil = true;
                                                byte trama[] = tramaactaris.getTparametrosperfil1();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama[8] = I_CTRL(nr, ns);
                                                trama = calcularnuevocrcI(trama);
                                                ultimatramaEnviada = trama;
                                                avisoStr("Parametros Perfil");
                                                enviaTrama2(trama, "=> Solicitud parametros del perfil ");
                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Serial");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Serial");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Numero de serial");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Numero de serial");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Numero de serial");
                            }
                        } else {
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Numero de serial");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Numero de serial");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Numero de serial");
                }
            } else if (lfechaactual) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                                vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
                                if (validacionCRCHCS(vectorhex)) {
                                    if (validacionCRCFCS(vectorhex)) {
                                        if ((Integer.parseInt(vectorhex[8], 16) & 0x01) == 0x00) { //es informacion
                                            if (vectorhex[17].equals("00")) {//ack data
                                                escribir("NrM " + ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1));
                                                if (nrEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[8], 16) & 0x0E) >> 1)) {
                                                    nr++;
                                                    if (nr > 7) {
                                                        nr = 0;
                                                    }
                                                    escribir("NsPc " + ns + " NrPc " + nr);
                                                    reintentoadp = 0;
                                                    //capturamos la fecha actual del medidor
                                                    String StrFechaActual = Integer.parseInt(vectorhex[14] + vectorhex[15], 16) + "/"
                                                            + "" + Integer.parseInt(vectorhex[16], 16) + "/"
                                                            + "" + Integer.parseInt(vectorhex[17], 16);
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Recieved Ready RR");
                                                } else {
                                                    //no son los ns y nr esperados
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha actual");
                                                }
                                            } else {
                                                escribir("Negacion Peticion");
                                                lfechaactual = false;
                                                lReset = true;
                                                byte trama[] = tramaactaris.getTfinaliza();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama = calcularnuevocrcRR(trama);
                                                enviaTrama2(trama, "");
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
                                                reintentoadp = 0;
                                                lfechaactual = false;
                                                lserialnumber = true;
                                                byte trama[] = tramaactaris.getTserie();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama[8] = I_CTRL(nr, ns);
                                                trama = calcularnuevocrcI(trama);
                                                ultimatramaEnviada = trama;
                                                avisoStr("Serial Number");
                                                enviaTrama2(trama, "=> Solicitud Numero Serial");
                                            } else {
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                                                //no es el esperado
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error Trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                            }
                        } else {
                            escribir("Error Trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                        }
                    } else {
                        //trama incompleta
                        escribir("Error Trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error Trama Sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Fecha Actual");
                }
            } else if (lparamperfil) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
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
                                                    reintentoadp = 0;
                                                    periodoIntegracion = Integer.parseInt(vectorhex[21] + vectorhex[22], 16);
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Recieved Ready RR");
                                                } else {
                                                    //no son los ns y nr esperados
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud parametros perfil");
                                                }
                                            } else {
                                                escribir("Negacion Peticion");
                                                lparamperfil = false;
                                                lReset = true;
                                                byte trama[] = tramaactaris.getTfinaliza();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama = calcularnuevocrcRR(trama);
                                                enviaTrama2(trama, "");
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
                                                reintentoadp = 0;
                                                lparamperfil = false;
                                                linfoperfil = true;
                                                infoPerfil = new Vector<String>();
                                                primerbloque = true;
                                                byte trama[] = tramaactaris.getTinfoPerfil();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama[8] = I_CTRL(nr, ns);
                                                trama = calcularnuevocrcI(trama);
                                                ultimatramaEnviada = trama;
                                                avisoStr("Informacion Perfil");
                                                enviaTrama2(trama, "=> Solicitud Informacion Perfil de carga");
                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud parametros perfil");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud parametros perfil");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Parametros del perfil");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Parametros del perfil");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Parametros del perfil");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Parametros del perfil");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Parametros del perfil");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Parametros del perfil");
                }
            } else if (linfoperfil) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
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
                                                    reintentoadp = 0;
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
                                                        //System.out.println("Numero de canales " + numcanales);
                                                        poscorte = 22;
                                                    }
                                                    for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                        System.out.print(" " + vectorhex[i]);
                                                        infoPerfil.add(vectorhex[i]);
                                                    }
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Recieved Ready RR");
                                                } else {
                                                    //no son los ns y nr esperados
                                                    //System.out.println("no es el esperado");
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion perfil");
                                                }
                                            } else {
                                                escribir("Negacion Peticion");
                                                linfoperfil = false;
                                                lReset = true;
                                                byte trama[] = tramaactaris.getTfinaliza();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama = calcularnuevocrcRR(trama);
                                                enviaTrama2(trama, "");
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
                                                reintentoadp = 0;
                                                try {
                                                    String serverntp = cp.buscarParametrosString(4);
                                                    if (serverntp.length() > 0) {
                                                        SynHoraNTP objsync = new SynHoraNTP();
                                                        String fechantp = objsync.obtenerfechaNTP(serverntp);
                                                        if (fechantp.length() > 0) {
                                                            SimpleDateFormat objsdfsync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            time = new Timestamp(objsdfsync.parse(fechantp).getTime());
                                                            //System.out.println("Sincronizando hora con servidor " + serverntp);
                                                            escribir("Sincronizando hora con servidor " + serverntp);
                                                            escribir("Fecha NTP" + time);
                                                            //System.out.println("max desfase permitido" + ndesfasepermitido);
                                                            //System.out.println("Fecha NTP" + time);
                                                        }
                                                    }
                                                    deltatimesync1 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                                                } catch (Exception e) {
                                                    System.err.println(e.getMessage());
                                                }
                                                linfoperfil = false;
                                                lfechaactual2 = true;
                                                byte trama[] = tramaactaris.getTfechaHora2();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama[8] = I_CTRL(nr, ns);
                                                trama = calcularnuevocrcI(trama);
                                                ultimatramaEnviada = trama;
                                                avisoStr("Fecha Actual");
                                                enviaTrama2(trama, "=> Solicitud fecha Actual");
                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion perfil");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion perfil");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion del perfil");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion del perfil");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion del perfil");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion del perfil");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion del perfil");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud informacion del perfil");
                }
            } else if (lfechaactual2) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (Integer.parseInt(vectorhex[2], 16) <= vectorhex.length) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
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
                                                    solicitar = true;
                                                    escribir("NsPc " + ns + " NrPc " + nr);
                                                    reintentoadp = 0;
                                                    deltatimesync2 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                                                    fechaActual = Integer.parseInt(vectorhex[30] + vectorhex[31], 16) + "/"
                                                            + "" + Integer.parseInt(vectorhex[32], 16) + "/"
                                                            + "" + Integer.parseInt(vectorhex[33], 16) + " "
                                                            + "" + Integer.parseInt(vectorhex[35], 16) + ":"
                                                            + "" + Integer.parseInt(vectorhex[36], 16) + ":"
                                                            + "" + Integer.parseInt(vectorhex[37], 16);
                                                    escribir("Fecha actual de medidor " + fechaActual);
                                                    try {
                                                        escribir("Fecha actual Medidor " + sdf3.parse(fechaActual).toString());
                                                        escribir("Fecha actual PC " + Date.from(ZonedDateTime.now(zid).toInstant()).toString());
                                                        if (med.getFecha() != null) {
                                                            escribir("Fecha Ultima Lectura " + med.getFecha());
                                                            Long actual = new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()).getTime();
                                                            Long ultimamed = med.getFecha().getTime();
                                                            Long calculo = actual - ultimamed;
                                                            int diasaleer = (int) (calculo / 86400000);
                                                            if (calculo % 86400000 > 0) {
                                                                diasaleer = diasaleer + 1;
                                                            }
                                                            if (diasaleer > 0) {
                                                                if (diasaleer > 30) {
                                                                    ndias = 30;
                                                                } else {
                                                                    ndias = diasaleer;
                                                                }
                                                            }
                                                            escribir("Numero de dias leer calculado " + (ndias + 1));
                                                            //System.out.println("Numero de dias leer calculado " + (ndias + 1));
                                                        }
                                                    } catch (Exception e) {
                                                        System.err.println(e.getMessage());
                                                    }
                                                    try {
                                                        tsfechaactual = new Timestamp(sdf3.parse(fechaActual).getTime());
                                                        //System.out.println("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2) / 1000));
                                                        escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                                                        //System.out.println("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                                                        escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                                                        if (Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000) > ndesfasepermitido) {
                                                            solicitar = false;
                                                            escribir("No se solicitara el perfil de carga");
                                                            //System.out.println("No se solicitara el perfil de carga");
                                                        }
                                                        cp.actualizaDesfase(((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000), med.getnSerie(), null);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Recieved Ready RR");
                                                } else {
                                                    //no son los ns y nr esperados
                                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");

                                                }
                                            } else {
                                                escribir("Negacion Peticion");
                                                lfechaactual2 = false;
                                                lReset = true;
                                                byte trama[] = tramaactaris.getTfinaliza();
                                                asignaDireciones(trama, dirlog, dirfis);
                                                trama = calcularnuevocrcRR(trama);
                                                enviaTrama2(trama, "");
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
                                                reintentoadp = 0;
                                                lfechaactual2 = false;
                                                if (lperfil) {
                                                    if (solicitar) {
                                                        lperfilcarga = true;
                                                        lperfilcompleto = false;
                                                        vPerfilCarga = new Vector<String>();

                                                        byte trama[] = tramaactaris.getTperfil1();
                                                        asignaDireciones(trama, dirlog, dirfis);
                                                        trama[8] = I_CTRL(nr, ns);
                                                        try {
                                                            String fecha = sdf.format(new Date(Date.from(ZonedDateTime.now(zid).toInstant()).getTime() - (long) (86400000) * ndias));
                                                            //System.out.println("Fecha peticion perfil  " + sdf3.format(new Date(Date.from(ZonedDateTime.now(zid).toInstant()).getTime() - (long) (86400000) * ndias)));
                                                            escribir("Fecha peticion de perfil de carga " + fecha);
                                                            String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                                                            while (lfecha.length() < 4) {
                                                                lfecha = "0" + lfecha;
                                                            }
                                                            trama[33] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                                            trama[34] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                                                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                                                            while (lfecha.length() < 2) {
                                                                lfecha = "0" + lfecha;
                                                            }
                                                            trama[35] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                                                            while (lfecha.length() < 2) {
                                                                lfecha = "0" + lfecha;
                                                            }
                                                            trama[36] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        trama = calcularnuevocrcI(trama);
                                                        primeraTramaBloque = true;
                                                        nintervalosperfil = 0;
                                                        lprimerBloque = true;
                                                        numerobloque = 0;
                                                        ultimbloquePerfil = false;
                                                        ultimatramaEnviada = trama;
                                                        avisoStr("Perfil Carga");
                                                        enviaTrama2(trama, "=> Solicitud de perfil de carga");
                                                    } else {
                                                        lterminar = true;
                                                        byte trama[] = tramaactaris.getTfinaliza();
                                                        asignaDireciones(trama, dirlog, dirfis);
                                                        trama = calcularnuevocrcRR(trama);
                                                        escribir("Estado lectura perfil no solicitado por desfase de hora");
                                                        enviaTrama2(trama, "");
                                                    }

                                                } else if (leventos) {//solicitud Eventos
                                                    byte trama[] = tramaactaris.getPowerFailureElements();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = I_CTRL(nr, ns);
                                                    trama = calcularnuevocrcI(trama);
                                                    lprimerBloque = true;
                                                    primeraTramaBloque = true;
                                                    lPowerFailureElements = true;
                                                    nintervaloseventos = 0;
                                                    ultimobloqueEventos = false;
                                                    vEventos = new Vector<String>();
                                                    ultimatramaEnviada = trama;
                                                    avisoStr("Eventos");
                                                    enviaTrama2(trama, "=> Solicitud Eventos powerFail");
                                                } else if (lconfhora) {
                                                    byte data[] = tramaactaris.getTfechaSync();
                                                    avisoStr("Sync Reloj");
                                                    String serverntp = cp.buscarParametrosString(4);
                                                    if (serverntp.length() > 0) {
                                                        SynHoraNTP objsync = new SynHoraNTP();
                                                        String fechantp = objsync.obtenerfechaNTP(serverntp);
                                                        if (fechantp.length() > 0) {
                                                            SimpleDateFormat objsdfsync = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            Timestamp time = null;
                                                            try {
                                                                time = new Timestamp(objsdfsync.parse(fechantp).getTime());
                                                                //System.out.println("Sincronizando hora con servidor " + serverntp);
                                                                //System.out.println("Fecha " + time);
                                                            } catch (Exception e) {
                                                                //System.out.println("Error SNTP Sincronizando hora con equipo local ");
                                                                e.printStackTrace();
                                                                time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                                                            }
                                                            String fecha = sdf.format(new Date(time.getTime()));
                                                            //System.out.println("Fecha a actualizaar " + fecha);
                                                            String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                                                            while (lfecha.length() < 4) {
                                                                lfecha = "0" + lfecha;
                                                            }
                                                            data[29] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                                            data[30] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
                                                            data[31] = (byte) (Integer.parseInt(fecha.substring(4, 6)) & 0xFF);// mes 
                                                            data[32] = (byte) (Integer.parseInt(fecha.substring(6, 8)) & 0xFF);//dia
                                                            data[33] = (byte) (((time.getDay()) == 0 ? 7 : (time.getDay())) & 0xFF);// dia de la semana
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
                                                            asignaDireciones(data, dirlog, dirfis);
                                                            data = calcularnuevocrcI(data);
                                                            ultimatramaEnviada = data;
                                                            //System.out.println("Envio trama de cambio de hora");
                                                            //System.out.println(tramaactaris.encode(data, data.length));
                                                            enviaTrama2(data, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));
                                                        } else {
                                                            avisoStr("Cerrando puerto");
                                                            cerrarPuerto();
                                                            escribir("Estado lectura No Syncronizado");
                                                            avisoStr("No Leido");
                                                            cerrarLog("No sincronizado", false);
                                                            leer = false;
                                                        }

                                                    } else {
                                                        avisoStr("Cerrando puerto");
                                                        cerrarPuerto();
                                                        escribir("Estado lectura No Syncronizado");
                                                        avisoStr("No Leido");
                                                        cerrarLog("No sincronizado", false);
                                                        leer = false;
                                                    }
                                                } else {//desconectar
                                                    lterminar = true;
                                                    byte trama[] = tramaactaris.getTfinaliza();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama = calcularnuevocrcRR(trama);
                                                    enviaTrama2(trama, "");
                                                }
                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                }
            } else if (lperfilcarga) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
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
                                                                        //System.out.println("numero de intervalos del perfil de carga " + Integer.parseInt(longbytes2, 16));
                                                                    } else {
                                                                        longbytes2 = vectorhex[25 + (tamaño_longitud)];
                                                                        nintervalosperfil = Integer.parseInt(longbytes2, 16);
                                                                        //System.out.println("numero de intervalos del perfil de carga " + Integer.parseInt(longbytes2, 16));
                                                                        poscorte = 26 + (tamaño_longitud);
                                                                    }
                                                                }
                                                                for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                                    System.out.print(" " + vectorhex[i]);
                                                                    vPerfilCarga.add(vectorhex[i]);
                                                                }
                                                                System.out.print("\n");
                                                                //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);

                                                            } else {
                                                                int tamaño_longitud = (Integer.parseInt(vectorhex[23], 16));
                                                                //System.out.println("tamaño en bytes del perfil de carga " + tamaño_longitud);
                                                                poscorte = 24;
                                                                //System.out.println("asdasd2");
                                                                if (lprimerBloque) {
                                                                    lprimerBloque = false;
                                                                    int tamañointervalos = (Integer.parseInt(vectorhex[25], 16));
                                                                    //System.out.println("numero de intervalos del perfil de carga " + tamañointervalos);
                                                                    //System.out.println("asdasd");
                                                                    poscorte = 26;
                                                                }
                                                                //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                                for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                                    System.out.print(" " + vectorhex[i]);
                                                                    vPerfilCarga.add(vectorhex[i]);
                                                                }
                                                                System.out.print("\n");
                                                            }
                                                            if (vectorhex[17].equals("01")) {//validamos si es el ultimo data block
                                                                ultimbloquePerfil = true;
                                                            }
                                                            analizaTrama = true;
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
                                                                poscorte = 20 + tamaño_longitud;
                                                                nintervalosperfil = Integer.parseInt(longbytesperfil, 16);
                                                                //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                            } else {
                                                                String longbytesperfil = vectorhex[19];
                                                                nintervalosperfil = Integer.parseInt(longbytesperfil, 16);
                                                                //System.out.println("numero de intervalos del perfil de carga " + Integer.parseInt(longbytesperfil, 16));
                                                                poscorte = 20;
                                                                //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                            }
                                                            for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                                System.out.print(" " + vectorhex[i]);
                                                                vPerfilCarga.add(vectorhex[i]);
                                                            }
                                                            System.out.print("\n");
                                                            analizaTrama = true;
                                                        } else {
                                                            escribir("Negacion del perfil de carga");
                                                            analizaTrama = false;
                                                        }

                                                    }
                                                } else {
                                                    poscorte = 11;
                                                    //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                    for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                        System.out.print(" " + vectorhex[i]);
                                                        vPerfilCarga.add(vectorhex[i]);
                                                    }
                                                    System.out.print("\n");

                                                    //almacenamos directo
                                                    analizaTrama = true;
                                                }
                                                if (analizaTrama) {
                                                    nr++;
                                                    if (nr > 7) {
                                                        nr = 0;
                                                    }
                                                    escribir("NsPc " + ns + " NrPc " + nr);
                                                    reintentoadp = 0;
                                                    if (!vectorhex[1].equals("A0")) {
                                                        nsEsperado++;
                                                        if (nsEsperado > 7) {
                                                            nsEsperado = 0;
                                                        }
                                                    } else {
                                                        ultimatramabloque = true;
                                                    }
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Reacieved Ready");
                                                } else {
                                                    escribir("Negacion Peticion");
                                                    lperfilcarga = false;
                                                    lReset = true;
                                                    byte trama[] = tramaactaris.getTfinaliza();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama = calcularnuevocrcRR(trama);
                                                    enviaTrama2(trama, "");
                                                }
                                            } else {
                                                //no son los ns y nr esperados
                                                enviaTramaUltima(ultimatramaEnviada, "=> RR");
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
                                                    reintentoadp = 0;
                                                    if (ultimbloquePerfil) {//es el ultimo bloque del perfil por lo tanto terminamos co
                                                        lperfilcarga = false;
                                                        lperfilcompleto = true;
                                                        if (leventos) {
                                                            byte trama[] = tramaactaris.getPowerFailureElements();
                                                            asignaDireciones(trama, dirlog, dirfis);
                                                            trama[8] = I_CTRL(nr, ns);
                                                            trama = calcularnuevocrcI(trama);
                                                            lprimerBloque = true;
                                                            primeraTramaBloque = true;
                                                            lPowerFailureElements = true;
                                                            nintervaloseventos = 0;
                                                            ultimobloqueEventos = false;
                                                            vEventos = new Vector<String>();
                                                            ultimatramaEnviada = trama;
                                                            avisoStr("Eventos");
                                                            enviaTrama2(trama, "=> Solicitud Eventos powerFail");
                                                        } else {
                                                            ultimbloquePerfil = false;

                                                            lterminar = true;
                                                            byte trama[] = tramaactaris.getTfinaliza();
                                                            asignaDireciones(trama, dirlog, dirfis);
                                                            trama = calcularnuevocrcRR(trama);
                                                            enviaTrama2(trama, "");
                                                        }
                                                    } else {// aun faltan bloques por lo tanto se solicita el bloque
                                                        numerobloque++;
                                                        byte trama[] = tramaactaris.getTbloquePerfil();
                                                        asignaDireciones(trama, dirlog, dirfis);
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
                                                        avisoStr("Bloques de perfil");
                                                        ultimatramaEnviada = trama;
                                                        enviaTrama2(trama, "=Solicitud de bloque numero " + numerobloque);
                                                    }
                                                } else {
                                                    escribir("Error al recibir perfil");
                                                    cerrarPuerto();
                                                    if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                                                        try {
                                                            procesaInfoPerfil();
                                                            procesaDatos();
                                                        } catch (Exception e) {
                                                            System.err.println(e.getMessage());
                                                            escribir("Error procesando datos de perfil");
                                                        }
                                                    }
                                                    if (numeroReintentos >= actualReintento) {
                                                        abrePuerto();
                                                        iniciacomunicacion();
                                                    } else {
                                                        avisoStr("No Leido");
                                                        escribir("Estado Lectura No Leido");
                                                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                                                        leer = false;
                                                    }
                                                }

                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error Trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error Trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error Trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error Trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud bloque perfil de carga");
                }
            } else if (lPowerFailureElements) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
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
                                                            //System.out.println("numero de intervalos eventos " + Integer.parseInt(longbytesperfil, 16));
                                                            poscorte = 20 + tamaño_longitud;
                                                            nintervaloseventos = Integer.parseInt(longbytesperfil, 16);
                                                            //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                        } else {
                                                            String longbytesperfil = vectorhex[19];
                                                            nintervaloseventos = Integer.parseInt(longbytesperfil, 16);
                                                            //System.out.println("numero de intervalos eventos " + Integer.parseInt(longbytesperfil, 16));
                                                            poscorte = 20;
                                                            //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                        }
                                                        for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                            System.out.print(" " + vectorhex[i]);
                                                            vEventos.add(vectorhex[i]);
                                                        }
                                                        System.out.print("\n");
                                                        analizatrama = true;
                                                    } else {
                                                        analizatrama = false;
                                                    }

                                                } else {
                                                    poscorte = 11;
                                                    //System.out.println("Inicia en " + vectorhex[poscorte] + " " + vectorhex[poscorte + 1]);
                                                    for (int i = poscorte; i < vectorhex.length - 3; i++) {
                                                        System.out.print(" " + vectorhex[i]);
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
                                                    reintentoadp = 0;
                                                    if (!vectorhex[1].equals("A0")) {
                                                        nsEsperado++;
                                                        if (nsEsperado > 7) {
                                                            nsEsperado = 0;
                                                        }
                                                    } else {
                                                        ultimatramabloque = true;
                                                    }
                                                    byte trama[] = tramaactaris.getRR();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama[8] = RR_CTRL(nr);
                                                    trama = calcularnuevocrcRR(trama);
                                                    ultimatramaEnviada = trama;
                                                    enviaTrama2(trama, "=> Envia Reacieved Ready");
                                                } else {
                                                    escribir("Negacion Peticion");
                                                    lPowerFailureElements = false;
                                                    lReset = true;
                                                    byte trama[] = tramaactaris.getTfinaliza();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama = calcularnuevocrcRR(trama);
                                                    enviaTrama2(trama, "");
                                                }
                                            } else {
                                                //no son los ns y nr esperados
                                                enviaTramaUltima(ultimatramaEnviada, "=> RR");
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
                                                    reintentoadp = 0;
                                                    lPowerFailureElements = false;
                                                    lterminar = true;
                                                    byte trama[] = tramaactaris.getTfinaliza();
                                                    asignaDireciones(trama, dirlog, dirfis);
                                                    trama = calcularnuevocrcRR(trama);
                                                    enviaTrama2(trama, "");
                                                } else {
                                                    escribir("Error al recibir eventos");
                                                    cerrarPuerto();
                                                    if (numeroReintentos >= actualReintento) {
                                                        abrePuerto();
                                                        iniciacomunicacion();
                                                    } else {
                                                        avisoStr("No Leido");
                                                        escribir("Estado Lectura No Leido");
                                                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                                                        leer = false;
                                                    }
                                                }

                                            } else {
                                                //no es el esperado
                                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                                            }
                                        } else if ((Integer.parseInt(vectorhex[8], 16) & 0x0F) == 0x05) { //es rnr
                                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                                        } else {
                                            validarTipoTrama(vectorhex[8]);
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error Trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Eventos");
                }
            } else if (lterminar) {
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length >= Integer.parseInt(vectorhex[2], 16)) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                                if (validacionCRCHCS(vectorhex)) {
                                    if (validacionCRCFCS(vectorhex)) {
                                        lterminar = false;
                                        cerrarPuerto();
                                        procesaInfoPerfil();
                                        if (lperfil) {
                                            avisoStr("Procesando PerfilCarga");
                                            procesaDatos();
                                        }
                                        if (leventos) {
                                            avisoStr("Procesando Eventos");
                                            procesaInfoEventos();
                                        }

                                        avisoStr("Leido");

                                        escribir("Estado Lectura Leido");
                                        med.MedLeido = true;
                                        cerrarLog("Leido", true);
                                        leer = false;
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                }
            } else if (lReset) {//estado reset para los casos de contraseña mala y negacio
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (Integer.parseInt(vectorhex[2], 16) <= vectorhex.length) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                                vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
                                if (validacionCRCHCS(vectorhex)) {
                                    if (validacionCRCFCS(vectorhex)) {
                                        lReset = false;
                                        cerrarPuerto();
                                        if (numeroReintentos >= actualReintento) {
                                            abrePuerto();
                                            iniciacomunicacion();
                                        } else {
                                            avisoStr("No Leido");
                                            escribir("Estado Lectura No Leido");
                                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                                            leer = false;
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Terminate");
                }
            } else if (lfechasync) {//peticion fecha actual para sincronizar
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    vectorhex = tramaIncompleta.split(" ");
                }
                if (vectorhex.length > 7) {//tiene cabecera
                    if (Integer.parseInt(vectorhex[2], 16) <= vectorhex.length) {
                        if ((Integer.parseInt(vectorhex[2], 16) + 2) <= vectorhex.length) {
                            if (vectorhex[0].equals("7E") && vectorhex[Integer.parseInt(vectorhex[2], 16) + 1].equals("7E")) {
                                vectorhex = cortarTrama(vectorhex, Integer.parseInt(vectorhex[2], 16));
                                if (validacionCRCHCS(vectorhex)) {
                                    if (validacionCRCFCS(vectorhex)) {
                                        if (vectorhex.length >= 17 && vectorhex[17].equals("00")) {//verificamos si viene el valor del aceptacion de la hora
                                            lfechasync = false;
                                            lterminar = true;
                                            byte trama[] = tramaactaris.getTfinaliza();
                                            asignaDireciones(trama, dirlog, dirfis);
                                            trama = calcularnuevocrcRR(trama);
                                            enviaTrama2(trama, "Logout");
                                        } else {
                                            //no configuro?
                                            lfechasync = false;
                                            lReset = true;
                                            byte trama[] = tramaactaris.getTfinaliza();
                                            asignaDireciones(trama, dirlog, dirfis);
                                            trama = calcularnuevocrcRR(trama);
                                            enviaTrama2(trama, "Logout");
                                        }
                                    } else {
                                        //badFCS
                                        //System.out.println("BADFCS");
                                        escribir("BAD FCS");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Syncronizacion Fecha Actual");
                                    }
                                } else {
                                    //badHCS
                                    //System.out.println("BADHCS");
                                    escribir("BAD HCS");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Syncronizacion Fecha Actual");
                                }
                            } else {
                                //trama incompleta
                                //System.out.println("NO 7E");
                                escribir("Error trama inicio y final");
                                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Syncronizacion Fecha Actual");
                            }
                        } else {
                            //trama incompleta
                            //System.out.println("Incompleta");
                            escribir("Error trama incompleta");
                            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Syncronizacion Fecha Actual");
                        }
                    } else {
                        //trama incompleta
                        //System.out.println("Incompleta");
                        escribir("Error trama incompleta");
                        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Syncronizacion Fecha Actual");
                    }
                } else {//trama incompleta
                    //System.out.println("Sin cabecera");
                    escribir("Error trama sin cabecera");
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Syncronizacion Fecha Actual");
                }
            }
        }

    }

    private boolean validaTrama(String[] vectorhex) {
        boolean isok = false;
        if (vectorhex[0].equals("7E") & vectorhex[vectorhex.length - 1].equals("7E")) {
            isok = true;
        }
        return isok;
    }

    private boolean validacionCRCHCS(String[] data) {
        boolean lcrc = false;
        byte b[] = new byte[8];
        for (int j = 0; j
                < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = calculoFCS(b);
        ////System.out.println("valor crc cal" + crc);
        String stx = data[9] + "" + data[10];
        String stxcrc = "" + Integer.toHexString(crc).toUpperCase();
        //si el valor tiene 0 a la izq al obtener el entero no los tiene en cuenta por lo que aaca se complentan
        if (stxcrc.length() == 3) {
            stxcrc = "0" + stxcrc;
        } else if (stxcrc.length() == 2) {
            stxcrc = "00" + stxcrc;
        } else if (stxcrc.length() == 1) {
            stxcrc = "000" + stxcrc;
        }
        ////System.out.println("hcs trama " + stx);
        ////System.out.println("hcs calculado " + stxcrc);
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
////        System.out.println("crc normal negado");
////        System.out.println(Integer.toHexString(crc & 0xFFFF));

        //reflejar las tramas en grupos de 16 bits usando el metodo reflect
////        System.out.println("crc reflejado por short");
        crc = reflect(crc, true);
////        System.out.println(Integer.toHexString(crc & 0xFFFF));

////        System.out.println("RESULTADO FINAL");
        crcaux1 = (short) (((crc >> 8) & 0x000000ff) + (crc << 8 & 0x0000ff00));
////        System.out.println(Integer.toHexString(crcaux1 & 0xFFFF));
        crcaux1 = crcaux1 & 0x0000FFFF;
        return crcaux1;
    }

    public static short reflect(int crc, boolean order) {
        //refleja la parte baja de los 'bitnum' los bits de "CRC"
        //ENVIAR TRUE SI SE DEBE REFLEJAR EN EL ORDER Y ENVIAR FALSE SI SE REFLEJA 8 BITS

        short crcaux, i, j = 1, crcout = 0;
        //System.out.println(Integer.reverse(crc));
        //buffaux[i] = (short) (Integer.reverse(buff[i]) >>> (8));
        crcaux = (short) (Integer.reverse(crc) >>> (16));
        if (!order) {
            crcaux = (short) (((crcaux >> 8) & 0x000000ff) + (crcaux << 8 & 0x0000ff00));
        }

        crcout = (short) (crcaux & 0x00000000FFFF);

        return crcout;

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

    private byte[] calculaPWD(byte[] t3, String password) {
        try {
            t3[44] = (byte) (Integer.parseInt(convertStringToHex(password.substring(0, 1)), 16) & 0xFF);
            t3[45] = (byte) (Integer.parseInt(convertStringToHex(password.substring(1, 2)), 16) & 0xFF);
            t3[46] = (byte) (Integer.parseInt(convertStringToHex(password.substring(2, 3)), 16) & 0xFF);
            t3[47] = (byte) (Integer.parseInt(convertStringToHex(password.substring(3, 4)), 16) & 0xFF);
            t3[48] = (byte) (Integer.parseInt(convertStringToHex(password.substring(4, 5)), 16) & 0xFF);
            t3[49] = (byte) (Integer.parseInt(convertStringToHex(password.substring(5, 6)), 16) & 0xFF);
            t3[50] = (byte) (Integer.parseInt(convertStringToHex(password.substring(6, 7)), 16) & 0xFF);
            t3[51] = (byte) (Integer.parseInt(convertStringToHex(password.substring(7, 8)), 16) & 0xFF);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t3;
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

    private byte[] calcularnuevocrcI(byte[] siguientetrama) {
        try {
            byte[] data = new byte[8];
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
            //System.out.println("Nuevo HCF" + stxcrc);
            siguientetrama[9] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[10] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
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
            //System.out.println("Nuevo fcs " + stxcrc);
            siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return siguientetrama;
    }

    private byte[] crearAARQ(byte[] t3, String pass) {
        Vector<String> trama = new Vector<String>();
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

    public void reiniciaComuniacion() {
        try {
            cerrarPuerto();
            if (numeroReintentos >= actualReintento) {
                if (!objabortar.labortar) {
                    abrePuerto();
                    iniciacomunicacion();
                } else {
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {

                cerrarLog("Desconexion Numero de reintentos agotado", false);
                leer = false;

            }
        } catch (Exception eex) {
            eex.printStackTrace();
        }
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
            System.err.println(e.getMessage());
        }
    }

    public void avisoStr(String str) {
        try {
            if (aviso) {
                //lm.jtablemedidores.setValueAt(str, indx, 3);
                //lm.mdc.fireTableDataChanged();
            }
        } catch (Exception e) {
        }

    }

    private byte[] asignaDireciones(byte[] trama, int dirlog, int dirfis) {
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

    private void validarTipoTrama(String tipotrama) {
        if (tipotrama.equals("97")) {
            reintentoadp += 1;
            if (reintentoadp <= 5) {
                enviaTramaUltima(ultimatramaEnviada, "Frame Reject enviando reenviando ultima trama");
            } else {
                reintentoadp = 0;
                reiniciaComuniacion();
            }
        } else if (tipotrama.equals("1F")) {
            avisoStr("No leido");
            escribir("Estado Lectura No Leido");
            cerrarPuerto();
            if (numeroReintentos >= actualReintento) {
                abrePuerto();
                iniciacomunicacion();
            } else {
                avisoStr("No Leido");
                escribir("Estado Lectura No Leido");
                cerrarLog("Desconexion Numero de reintentos agotado", false);
                leer = false;
            }

        } else {
            escribir("Valida Trama: No es información");
            enviaTramaUltima(ultimatramaEnviada, "Reenviando ultima trama");
        }
    }

    public byte RR_CTRL(int nr) {
        return (byte) (((byte) (nr << 5)) | 0x11);
    }

    public byte I_CTRL(int nr, int ns) {
        return (byte) ((byte) ((byte) ((nr << 5) | (ns << 1)) | 0x10) & 0xFE);
    }

    private void procesaInfoPerfil() {
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
                System.err.println(e.getMessage());
            }

            //System.out.println("ke de canal " + i + " " + conskePerfil[i]);
            //System.out.println("unidad de canal " + i + " " + unidades[i]);
        }

    }

    private void procesaDatos() {
        cp.AlmacenaPerfilActaris(seriemedidor, vPerfilCarga, obis, conskePerfil, unidades, nintervalosperfil, periodoIntegracion, fechaActual, file, med.getFecha());
    }

    private void procesaInfoEventos() {
        cp.AlmacenaEventosActaris(seriemedidor, vEventos, nintervaloseventos);
    }

    private void enviaTramaConexion(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;
        lconfiguracion = false;
        if (port != null) {
            if (port.isAlive()) {
                port.stop();
                port = null;
            }
        }
        if (port3 != null) {
            if (port3.isAlive()) {
                port3.stop();
                port3 = null;
            }
        }

        port3 = new Thread() {
            public void run() {
                try {
                    int i = 0;
                    boolean entro = true;
                    boolean reinicia = true;
                    escribir(des);
                    //System.out.println(des);
                    //System.out.println("Envia trama =>");
                    escribir("=> " + tramaactaris.encode(trama, trama.length));
                    //System.out.println(tramaactaris.encode(trama, trama.length));
                    enviaTrama(trama);
                    while (entro) {
                        if (lconfiguracion) {
                            entro = false;
                            esperandoconexion = false;

                        } else {
                            if (i < 120) {
                                i++;
                                Thread.sleep(1000);
                            } else {

                                entro = false;
                                esperandoconexion = false;
                                reinicia = false;
                            }

                        }
                    }
                    if (!reinicia && i > 119) {
                        escribir("Tiempo agotado para conexion.. ");
                        if (serialPort != null) {
                            cerrarPuerto();
                            if (numeroReintentos >= actualReintento) {
                                if (!objabortar.labortar) {
                                    abrePuerto();
                                    iniciacomunicacion();
                                } else {
                                    escribir("Proceso Abortado");
                                    cerrarLog("Abortado", false);
                                    leer = false;
                                }
                            } else {
                                escribir("Numero de reintentos agostado..");
                                cerrarLog("Desconexion Numero de reintentos agotado", false);
                                escribir("Estado Lectura No Leido");
                                leer = false;

                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        port3.start();

    }

    private String[] cortarTrama(String[] vectorhex, int tamaño) {
        String nuevoVector[] = new String[tamaño + 2];
        System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
        ////System.out.println("Nueva trama");
        for (int i = 0; i < nuevoVector.length; i++) {
            System.out.print(" " + nuevoVector[i]);
        }
        return nuevoVector;

    }

    public void terminaHilos() {
        try {
            port.interrupt();
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }
        try {
            port2.interrupt();
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }
        try {
            port3.interrupt();
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }
        port = null;
        port2 = null;
        port3 = null;
    }

    public void cerrarLog(String status, boolean lexito) {
        tiempofinal = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        ELogCall log = new ELogCall();
        log.setDfecha(Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime()));
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
        log.setTipoCall("2");// es programado
        cp.saveLogCall(log, null);
    }
}
