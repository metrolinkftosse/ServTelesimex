/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.DES2;

import Datos.TramasRemotalElsterA1800;
import Entidades.Abortar;
import Entidades.EConfModem;
import Entidades.ELogCall;
import Entidades.EMedidor;
//import Rutinas.AppletLecturaMedidores;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
public class LeerRemotoPSTNElsterA1800 implements Runnable, SerialPortEventListener {
    //**Estandar
    
    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Abortar objabortar;
    Date d = new Date();
    long runvar = 0;
    boolean rutinaCorrecta = false;
    String seriemedidor = "";
    String vecSerieMedidor[] = new String[6];
    CountingInputStream input;
    BufferedOutputStream output;
    long tiempo = 500;
    String numeroPuerto = "COM4";
    int velocidadPuerto = 1200;
    String password;
    int numCanales = 2;
    int ndias;
    int numeroReintentos = 4;
    int nreintentos = 0;
    SerialPort serialPort;
    Enumeration portList;
    private CommPortIdentifier portId;
    boolean portFound = false;
    boolean portconect = false;
    byte[] user;
    String cadenahex = "";
    long timeout;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean conexionModem = false;
    int indiceconexion = 0;
    ControlProcesos cp;
    Vector<EConfModem> confModem;
    EMedidor med;
    boolean enviando = false;
    byte[] ultimatramaEnviada = null;
    int reintentosbadCRC = 0;
    public boolean leer = true;
    boolean aviso = false;
    //AppletLecturaMedidores lm;
    int indx = 0;
    String fechaActual = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    //****************
    int intervalos = 15;
    long tiemporetransmision = 6000;
    int actualReintento = 0;
    boolean inicia1 = false;
    boolean desbloqueo = false;
    Thread port = null;
    Thread port2;
    Thread port3;
    boolean cierrapuerto = false;
    boolean esperandoconexion = true;
    boolean seEspera06 = false;
    boolean siguienteTrama = false;
    boolean complemento = false;
    public String tramaIncompleta = "";
    boolean reenviando = false;
    //***propios de protocolo
    int intentoescuchar = 0;
    boolean llegotramaini = false;
    int offset = 0;
    int ElementCount = 0;
    TramasRemotalElsterA1800 tramasElster = new TramasRemotalElsterA1800();
    Datos.DES2 des2 = new Datos.DES2();
    int opcionidentidad = 0;
    byte id = (byte) 0x01;
    boolean spoling = false; //false = 00 , true= 20
    byte bitSpoling = (byte) 0x00;
    boolean isticket = false; // contiene ticket o no contiene
    public int tamañobloque;
    public int tamañototalbloque;
    String External_Multiplier;
    String Ext_Mult_Scale_Factor;
    String Instrumentation_Scale_Factor;
    public String LP_CTRL_INT_FMT_CDE1;
    public int NBR_BLK_INTS_SET1;
    private boolean primerbloque;
    int tamañototalMT017 = 0;
    int contadorMT017 = 14;
    int contadorMT023 = 1;
    int tamañoacumuladolMT017 = 0;
    int contadorST064 = 0;
    public String fechaactual;
    public int FORMAT_CONTROL_1; //formato registros
    public int FORMAT_CONTROL_2; //formato registros
    private int NBR_PRESENT_VALUES;
    private int NBR_SUMMATIONS;
    private int NBR_PRESENT_DEMANDS;
    private int NBR_SELF_READS;
    private int NBR_COIN_VALUES;
    private int NBR_DEMANDS;
    private int NBR_OCCUR;
    private int REG_FUNC1_FLAG;
    int nivelacceso = 0;
    int reinicio = 0;
    //*****Estados
    boolean lconfiguracion = false;
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
    boolean lST064Perfil = false;
    boolean llogoff = false;
    //******Vectores
    Vector<String[]> profileDataTemp;
    Vector<String[]> profileData;
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
    private String[] vSumationSelect;
    private String[] vDemandSelect;
    private String[] vCoincidentSelect;
    private String[] vDemandAssoc;
    private String[] vPresentdemandSelect;
    private String[] vPresentValues;
    byte[] badcrc = {(byte) 0x15};
    //***Renvios
    int reintentosUltimatrama = 3;
    int actualUltimatrama = 0;
    private int contador15;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("");
    private boolean perfilincompleto = false;
    private String usuario = "admin";
    ZoneId zid;

//    public LeerRemotoPSTNElsterA1800(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, boolean registros, AppletLecturaMedidores lm, int indx, ControlProcesos cp,Abortar objabortar,boolean aviso) {
    public LeerRemotoPSTNElsterA1800(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, boolean registros, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, ZoneId zid) {
        this.med = med;
        this.confModem = confmodem;
        this.cp = cp;
        this.zid = zid;
        File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
        if (!f.exists()) {
            f.mkdirs();
        }
        file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");
//            file = new File(cp.rutalogs + "" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");

        try {
            if (file.exists()) {
                existearchivo = true;
            } else {
                //System.out.println();
            }
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }
        lperfil = perfil;
        leventos = eventos;
        if (lperfil) {
            leventos = true;
        }
        lregistros = false;
        this.aviso = aviso;
        this.objabortar = objabortar;
        //this.lm = lm;
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

    private void jinit() {
        tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        if (confModem.size() > 0) {

            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            portList = CommPortIdentifier.getPortIdentifiers();
            timeout = (long) (med.getTimeout());
            ndias = med.getNdias();
            seriemedidor = med.getnSerie();
            numCanales = med.getNcanales();

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
                //e.printStackTrace();
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
                        Thread.sleep(tiempo);
                    } catch (Exception e) {
                        System.err.println("Error en hilo Data_Avaliable");
                    }
                    if (!objabortar.labortar) {
                        procesaCadena();
                    }
                }
            }
        }
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
            cadenahex = tramasElster.encode(readBuffer, numbytes);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte                        
            interpretaCadena(cadenahex);


        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void interpretaCadena(String cadenahex) {
        rutinaCorrecta = true;
        enviando = false;
        reenviando = false;
        //conectando con el modem
        if (conexionModem) {
            if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52") && !desbloqueo) {
                //no carrier para los casos de que no se llegan los datos
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion modem sin portadora");
                escribir("<= " + cadenahex);
                escribir("No carrier");
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
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                enviaTrama(vtramainicial);
            } else if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52") && desbloqueo) {
                //no carrier para el caso de no recibir datos para el desbloqueo del modem
                String tramainicial = confModem.get(0).getPeticion0();
                tramainicial = tramainicial.replace("2E", "0D");
                
                byte[] vtramainicial = cp.ConvertASCIItoHex(tramainicial);
                indiceconexion++;
                desbloqueo = false;
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                enviaTrama(vtramainicial);

            } else if (cadenahex.contains("4E 4F 20 44 49 41 4C 54 4F 4E 45")) {//no dialtone
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion sin linea");
                
                escribir("<= Sin tono " + cadenahex);

                if (nreintentos >= numeroReintentos) {
                    nreintentos = 0;
                    cerrarPuerto();
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    escribir("Estado lectura No leido");
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
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion modem ocupado");
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("Ocupado", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                    escribir("<= Ocupado " + cadenahex);
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
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("Conectado", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                //se conecto al modem
                lconfiguracion = true;
                lcomandoI = true;
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                conexionModem = false;
                //enviando = true;
                //System.out.println("enviamos trama 1");
                tiemporetransmision = timeout * 10000;
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("Comunicando..", indx, 3);
                    try {
                        //lm.mdc.fireTableDataChanged();
                    } catch (Exception e) {
                    }
                }
                byte[] tramaelgama = tramasElster.getIcommand();

                ultimatramaEnviada = tramaelgama;
                enviaTrama1(tramaelgama, "=> Solicitud de Configuracion");
            } else if (cadenahex.contains("4F 4B")) {//ok
                escribir("<= OK " + cadenahex);
                nreintentos = 0;
                String trama = confModem.get(0).getpeticion(indiceconexion);

                if (trama.equals("")) {
                    byte[] vtrama = null;
                    if (med.getTipoconexion() == 1) {
                        vtrama = tramasElster.StringToHexMarcadoPSTN(med.getNumtelefonico().trim());
                    } else {
                        vtrama = tramasElster.StringToHexMarcadoGPRS(med.getDireccionip().trim() + "/" + med.getPuertoip().trim());
                    }
                    esperandoconexion = true;
                    enviaTramaConexion(vtrama, "=> Solicitud de conexion Numero telefono " + med.getNumtelefonico());

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }

                    tiemporetransmision = 8000;
                    trama = trama.replace("2E", "0D");
                    byte[] vtrama = cp.ConvertASCIItoHex(trama);
                    indiceconexion++;
                    enviaTrama1(vtrama, "=> Envio Trama AT " + trama);
                }
            } else if (cadenahex.contains("4E 4F 20 41 4E 53 57 45 52")) { //NO answer
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion modem no responde");
                escribir("<= Sin respuesta " + cadenahex);
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
                            escribir("Proceso Abortado");
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        escribir("Estado de lectura no leido");
                        leer = false;
                    }
                }
            } else if (cadenahex.contains("41 54 44 54")) {
                rutinaCorrecta = false;
            } else {
                cerrarPuerto();
                if (numeroReintentos >= actualReintento) {
                    if (!objabortar.labortar) {
                        abrePuerto();
                        iniciacomunicacion();
                    } else {
                        escribir("Proceso Abortado");
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                        cerrarLog("Abortado", false);
                        leer = false;
                    }
                } else {
                   
                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    escribir("Estado de lectura no leido");
                    leer = false;
                }
            }
        } else {//conexion con medidor
            escribir("Recibe <= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (lcomandoI) {
                revisarComando(vectorhex, cadenahex);
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
                revisarST00(vectorhex);
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
            } else if (llogoff) {
                revisarLogoff(vectorhex);
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
                fr.write(dato.trim().getBytes(), 0, dato.trim().getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.close();
            }
        } catch (Exception e) {
        }
    }

    private void abrePuerto() {
        portconect = false;
        actualUltimatrama = 0;
        try {
            if (!objabortar.labortar) {
                //parametrizacion del puerto, lo abre a 1200 baud
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }
                CommPort puerto = portId.open("AbreSerial" + med.getnSerie(), velocidadPuerto);
                serialPort = (SerialPort) puerto;
                portconect = true;
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("Abre puerto..", indx, 3);
                    try {
                        //lm.mdc.fireTableDataChanged();
                    } catch (Exception e) {
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
            } else {
                //System.out.println("Sale abre puerto");
                avisoStr("Abortado");
                cerrarLog("Abortado", false);
                leer = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion Error al comunicar con modem");
            if (portconect) {
                cerrarPuerto();
            }

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
                serialPort.removeEventListener();
                serialPort.close();
            }
        } catch (Exception p) {
        }
        try {

            String mensaje = "FIN DE SESSION --" + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()) + "--";
            escribir(mensaje);
            escribir("\r\n\n");

        } catch (Exception e) {
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
            //e.printStackTrace();
        }
    }

    private void iniciacomunicacion() {
        tiemporetransmision = 5000;
        lST064Perfil = false;
        lST076 = false;
        perfilincompleto = false;
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
            if (actualReintento != 0) {
                Timestamp actufecha = cp.findUltimafechaLec(med.getnSerie());
                if (actufecha != null) {
                    med.setFecha(actufecha);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
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
                    bitSpoling = 0x00;
                    inicia1 = false;
                    byte[] vtramaini = cp.ConvertASCIItoHex(cp.convertStringToHex("+++"));
                    try {
                        String mensaje = "=> Envio +++";
                        escribir(mensaje);
                    } catch (Exception e) {
                    }
                    conexionModem = true;
                    desbloqueo = true;
                    enviaTramaInicial(vtramaini);
                    while (enviando) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                    }
                    try {
                        Thread.sleep((long) 4000);
                    } catch (Exception e) {
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
                        }
                        String envia = "=> Trama AT " + tramainicial;
                        escribir(envia);
                        byte[] vtramainicial = cp.ConvertASCIItoHex(tramainicial);
                        indiceconexion++;
                        desbloqueo = false;
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
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
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion medidor no configurado");
                cerrarPuerto();
                cerrarLog("No configurado", false);
                escribir("Estado Lectura No Leido");
                leer = false;
            }
        } else {
            cerrarLog("No conectado", false);
            leer = false;
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
                    int i = 0;
                    boolean entro = true;
                    //System.out.println("Envia trama =>");
                    escribir("=> " + tramasElster.encode(trama, trama.length));
                    //System.out.println(tramasElster.encode(trama, trama.length));
                    enviaTrama(trama);
                    while (entro) {
                        if (!desbloqueo) {
                            entro = false;
                            //inicia1 = true;
                        } else {
                            if (i < 10) {
                                i++;
                                sleep(1000);
                            } else {
                                entro = false;
                                inicia1 = true;
                                enviando = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        port2.start();

    }

    private void enviaTramack(String descripcion) {
        final byte[] ini = {(byte) 0x06};
        final byte[] ini2 = {(byte) 0x15};
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
                                escribir("=> " + tramasElster.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                //System.out.println("Envia trama =>");
                                //System.out.println(tramasElster.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                intentosRetransmision++;
                                enviaTrama(ultimatramaEnviada);
                            } else {
                                escribir("=> 06");
                                //System.out.println("Envia trama =>");
                                //System.out.println(tramasElster.encode(ini, ini.length));
                                intentosRetransmision++;
                                enviaTrama(ini);
                            }
                            //intentosRetransmision++;

                        } else {
                            //System.out.println("Sale enviatrama2");
                            t = false;
                        }
                        if (intentosRetransmision > 3) {
                            //escribir("Numero de reenvios agotado");
                            //reiniciaComunicacion();
                            //System.out.println("Numero de reenvios agotado");
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {

                            boolean salir = true;
                            int intentosalir = 0;
                            while (salir) {
                                if (!enviando || intentosalir > ((5000 / 500)) * 2) {
                                    //System.out.println("Sale enviar");
                                    salir = false;
                                    if (!enviando) {
                                        t = false;
                                    }

                                } else {
                                    intentosalir++;
                                    //System.out.println("Esperando... enviatramack");
                                    sleep(500);
                                }
                            }
                            //sleep(tiemporetransmision);
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;

                        cerrarPuerto();
                        if (lST064Perfil & perfilincompleto) {
                            //guardamos lecturas hasta donde esten
                            avisoStr("Perfil Incompleto");
                            desglosaST076();
                            almacenaPerfilIncompleto();

                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                if (reinicio == 0) {
                                    reinicio = 1;
                                } else {
                                    reinicio = 0;
                                }
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            escribir("Numero de reintentos agotado");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
                           cerrarLog("Desconexion Numero de reintentos agotado", false);
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    //System.err.println(e.getMessage());
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
                output.close();
            }
            output = new BufferedOutputStream(serialPort.getOutputStream());
            output.write(bytes, 0, bytes.length);
            output.flush();
            output.close();
            rutinaCorrecta = false;
        } catch (Exception e) {
            //e.printStackTrace();
        }
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
                    escribir("=> " + tramasElster.encode(trama, trama.length));
                    //System.out.println(tramasElster.encode(trama, trama.length));
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
                                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                                    cerrarLog("Abortado", false);
                                    leer = false;
                                }
                            } else {
                                escribir("Numero de reintentos agostado..");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de intentos de conexiones agotado");
                                cerrarLog("Desconexion Numero de reintentos agotado", false);
                                escribir("Estado Lectura No Leido");
                                leer = false;

                            }
                        }
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        port3.start();

    }

    private void enviaTrama2(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final byte[] ini = {(byte) 0x06};
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
                            if (intentosRetransmision == 0) {
                                enviaTrama(ini);
                                //System.out.println("Envia trama 06=>");
                                escribir("=>06");
                            }
                            escribir(des);
                            //System.out.println(des);
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramasElster.encode(trama, trama.length));
                            //System.out.println(tramasElster.encode(trama, trama.length));
                            intentosRetransmision++;
                            enviaTrama(trama);

                        } else {
                            t = false;
                        }
                        if (intentosRetransmision > 8) {
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
                        if (lST064Perfil & perfilincompleto) {
                            //guardamos lecturas hasta donde esten
                            avisoStr("Perfil Incompleto");
                            desglosaST076();
                            almacenaPerfilIncompleto();

                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //terminahilos();
                            escribir("Numero de reintentos agotado");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            escribir("Estado Lectura No Leido");
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        port.start();

    }

    private void enviaTrama1(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final byte[] ini = {(byte) 0x06};
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
                            escribir(des);
                            //System.out.println(des);
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramasElster.encode(trama, trama.length));
                            //System.out.println(tramasElster.encode(trama, trama.length));
                            enviaTrama(trama);
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
                            intentosRetransmision++;
                            Thread.sleep(tiemporetransmision);
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;
                        cerrarPuerto();
                        if (lST064Perfil & perfilincompleto) {
                            //guardamos lecturas hasta donde esten
                            avisoStr("Perfil Incompleto");
                            desglosaST076();
                            almacenaPerfilIncompleto();

                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //terminahilos();
                            escribir("Numero de reintentos agotado");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            escribir("Estado Lectura No Leido");
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        port.start();

    }

    private void enviaTramaUltima(byte[] bytes, String descripcion) {
        actualUltimatrama++;

        if (actualUltimatrama > reintentosUltimatrama) {
            cerrarPuerto();
            actualUltimatrama = 0;
            if (numeroReintentos >= actualReintento) {
                if (!objabortar.labortar) {
                    abrePuerto();
                    iniciacomunicacion();
                } else {
                    escribir("Proceso Abortado");
                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {
                escribir("Numero de reintentos agotado");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
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
                                        rutinaCorrecta = true;
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
                            seEspera06 = true;
                            enviaTrama3(trama, des);
                        }
                    } catch (Exception e) {
                        //System.err.println(e.getMessage());
                    }
                }
            };
            port3.start();
        }
    }

    private void enviaTrama3(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final byte[] ini = {(byte) 0x06};
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
                            if (intentosRetransmision == 0) {
//                                escribir("Time out intento de reenvio ultima trama");
//                                //enviaTrama(ini);
////                                System.out.println("Envia trama 06=>");
//                                escribir("=>06");
                            }
                            escribir(des);
                            //System.out.println(des);
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramasElster.encode(trama, trama.length));
                            //System.out.println(tramasElster.encode(trama, trama.length));
                            intentosRetransmision++;
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

                        cerrarPuerto();
                        if (lST064Perfil & perfilincompleto) {
                            //guardamos lecturas hasta donde esten
                            avisoStr("Perfil Incompleto");
                            desglosaST076();
                            almacenaPerfilIncompleto();

                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            escribir("Numero de reintentos agotado");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            escribir("Estado Lectura No Leido");
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    //System.err.println(e.getMessage());
                }
            }
        };
        port.start();
    }

    private void reiniciaComuniacion() {
        cerrarPuerto();
        if (lST064Perfil & perfilincompleto) {
            //guardamos lecturas hasta donde esten
            avisoStr("Perfil Incompleto");
            desglosaST076();
            almacenaPerfilIncompleto();

        }
        if (numeroReintentos >= actualReintento) {
            if (!objabortar.labortar) {
                abrePuerto();
                iniciacomunicacion();
            } else {
                escribir("Proceso Abortado");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion proceso abortado");
                cerrarLog("Abortado", false);
                leer = false;
            }
        } else {
            //terminahilos();
            escribir("Numero de reintentos agotado");
            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion numero de reintentos agotado");
            cerrarLog("Desconexion Numero de reintentos agotado", false);
            escribir("Estado Lectura No Leido");
            leer = false;
        }
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

    private byte[] calcularnuevocrc(byte[] siguientetrama) {
        byte[] data = new byte[siguientetrama.length - 2];
        System.arraycopy(siguientetrama, 0, data, 0, data.length);
//        for (int i = 0; i < data.length; i++) {
//            data[i] = siguientetrama[i];
//        }
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

    public void avisoStr(String str) {
        try {
            if (aviso) {
                //lm.jtablemedidores.setValueAt(str, indx, 3);
                //lm.mdc.fireTableDataChanged();
            }
        } catch (Exception e) {
        }

    }

    private void desglosaPerfil() {
        try {
            //System.out.println("Datos perfil");
            desglosePerfil = new ArrayList<>();
            String tramadesglose = "";
            String tramatotaldata = "";
            for (int p = 0; p < profileData.size(); p++) {
                tramadesglose = "";
                for (int l = 0; l < profileData.get(p).length; l++) {
                    tramadesglose += profileData.get(p)[l];

                }

                if (tramadesglose.substring(0, 2).equals("06")) {//es primera parte del bloque
                    if (!tramadesglose.substring(8, 10).equals("00")) { //es retransmision del primero
                        tramadesglose = tramadesglose.substring(14, tramadesglose.length() - 4);
                    } else {
                        tramadesglose = tramadesglose.substring(14, tramadesglose.length() - 6);
                    }
                } else {// es segunda parte del bloque o retransmision del primero

                    if (!tramadesglose.substring(6, 8).equals("00")) { //es retransmision del primero
                        tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 4);
                    } else {//es la 2da parte del bloque
                        tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 6);
                    }
                }
                //System.out.println(tramadesglose.trim());
                tramatotaldata = tramatotaldata + tramadesglose.trim();
            }
            String valorcorte = "";
            int i = 1;
            while (tramatotaldata.length() > 0) {
                valorcorte = tramatotaldata.substring(0, 6);
                ////System.out.println("Tamaño bloque " + valorcorte);
                desglosePerfil.add(tramatotaldata.substring(0, (Integer.parseInt(valorcorte, 16) * 2) + 6));
                ////System.out.println("bloque" + i + " " + tramatotaldata.substring(0, (Integer.parseInt(valorcorte, 16) * 2) + 6));
                tramatotaldata = tramatotaldata.substring((Integer.parseInt(valorcorte, 16) * 2) + 6, tramatotaldata.length());
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void desglosaMT017() {
        vtotalMT017 = new Vector<String>();
        //System.out.println("MT017\n");
        String tramaMT017 = "";
        String totalMT017 = "";
        for (int p = 0; p < vMT017.size(); p++) {
            tramaMT017 = "";
            for (int j = 0; j < vMT017.get(p).length; j++) {
                tramaMT017 += vMT017.get(p)[j];
            }
            ////System.out.println(tramaMT017);
            if (tramaMT017.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                tramaMT017 = tramaMT017.substring(20, tramaMT017.length() - 4);
            } else {//es continuacion de bloques o retransmision de la primera trama
                if (p == 0) {//es la primera trama
                    tramaMT017 = tramaMT017.substring(18, tramaMT017.length() - 4);
                } else {//es continuacion de bloques
                    if (tramaMT017.substring(6, 8).equals("00")) {//es el ultimo bloque
                        tramaMT017 = tramaMT017.substring(18, tramaMT017.length() - 6);
                    } else {//es bloques intermedios
                        tramaMT017 = tramaMT017.substring(12, tramaMT017.length() - 4);
                    }
                }
            }
            ////System.out.println("trama cortada " + tramaMT017);
            vtotalMT017.add(tramaMT017);
        }
        for (int v = 0; v < vtotalMT017.size(); v++) {
            totalMT017 += vtotalMT017.get(v);
        }
        ////System.out.println("trama total " + totalMT017);
        regMt017 = new String[totalMT017.length() / 2];
        int i = 0;
        for (int v = 0; v < totalMT017.length(); v += 2) {

            regMt017[i] = totalMT017.substring(v, v + 2);
            System.out.print(regMt017[i] + " ");

            if ((i + 1) % 7 == 0 && i != 0) {
                System.out.print("\n");
            }
            i++;
        }
    }

    private void desglosaST062() {
        try {
            //System.out.println("ST062\n");
            //System.out.println("");
            for (int i = 0; i < vSt062.length; i++) {
                //System.out.print(vSt062[i]);
            }
            if (vSt062[0].equals("06")) {//es con 06
                regST062 = new String[vSt062.length - 13];
                for (int i = 0; i < regST062.length; i++) {
                    regST062[i] = vSt062[i + 10];
                }
            } else {//es con EE
                regST062 = new String[vSt062.length - 12];
                for (int i = 0; i < regST062.length; i++) {
                    regST062[i] = vSt062[i + 9];
                }
            }
            ////System.out.println("\nST062 mejorada");
            //System.out.println("");
            for (int i = 0; i < regST062.length; i++) {
                System.out.print(regST062[i] + " ");
                if (i % 16 == 0) {
                    System.out.print("\n");
                }
            }
            LP_CTRL_INT_FMT_CDE1 = regST062[(numCanales * 3) + 1];
            System.out.print("\n");
        } catch (Exception e) {
        }
    }

    private void desglosarST015() {
        try {
            vtotalST015 = new Vector<String>();
            //System.out.println("ST015");
            String tramaST015 = "";
            String totalST015 = "";
            for (int p = 0; p < vST015.size(); p++) {
                tramaST015 = "";
                for (int j = 0; j < vST015.get(p).length; j++) {
                    tramaST015 += vST015.get(p)[j];
                }
                //System.out.println(tramaST015);
                if (tramaST015.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                    tramaST015 = tramaST015.substring(20, tramaST015.length() - 4);
                } else {//es continuacion de bloques o retransmision de la primera trama
                    if (p == 0) {//es la primera trama
                        tramaST015 = tramaST015.substring(18, tramaST015.length() - 4);
                    } else {//es continuacion de bloques
                        if (tramaST015.substring(6, 8).equals("00")) {//es el ultimo bloque
                            tramaST015 = tramaST015.substring(18, tramaST015.length() - 6);
                        } else {//es bloques intermedios
                            tramaST015 = tramaST015.substring(12, tramaST015.length() - 4);
                        }
                    }
                }
                ////System.out.println("trama cortada " + tramaST015);
                vtotalST015.add(tramaST015);
            }
            for (int v = 0; v < vtotalST015.size(); v++) {
                totalST015 += vtotalST015.get(v);
            }
            ////System.out.println("trama total " + totalST015);
            regSt015 = new String[totalST015.length() / 2];
            int i = 0;
            for (int v = 0; v < totalST015.length() - 2; v += 2) {

                regSt015[i] = totalST015.substring(v, v + 2);
                System.out.print(regSt015[i] + " ");
                if (v % 16 == 0) {
                    System.out.print("\n");
                }
                i++;
            }
        } catch (Exception e) {
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
            desgloseEventos = new Vector<String>();
            String tramadesglose = "";
            String tramatotaldata = "";
            for (int p = 0; p < EventData.size(); p++) {
                tramadesglose = "";
                for (int l = 0; l < EventData.get(p).length; l++) {
                    tramadesglose += EventData.get(p)[l];
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
            e.printStackTrace();
        }
    }

    private void desglosaST022() {
        try {
            String tramatotaldata = "";
            for (int p = 0; p < vST022.size(); p++) {
                for (int l = 0; l < vST022.get(p).length; l++) {
                    tramatotaldata += vST022.get(p)[l];
                }
            }
            if (tramatotaldata.substring(0, 2).equals("06")) {
                tramatotaldata = tramatotaldata.substring(2, tramatotaldata.length());
            }
            vSumationSelect = new String[NBR_SUMMATIONS];
            vDemandSelect = new String[NBR_DEMANDS];
            if (NBR_COIN_VALUES > 0) {
                vCoincidentSelect = new String[NBR_COIN_VALUES];
                vDemandAssoc = new String[NBR_COIN_VALUES];
            }

            for (int i = 0; i < vSumationSelect.length; i++) {
                //System.out.println("Sumation select " + tramatotaldata.substring((i * 2) + (9 * 2), (i * 2) + (9 * 2) + 2));
                vSumationSelect[i] = tramatotaldata.substring((i * 2) + (9 * 2), (i * 2) + (9 * 2) + 2);
            }
            for (int i = 0; i < vDemandSelect.length; i++) {
                vDemandSelect[i] = tramatotaldata.substring((i * 2) + (9 * 2) + (NBR_SUMMATIONS * 2), (i * 2) + (NBR_SUMMATIONS * 2) + (9 * 2) + 2);
            }
            if (NBR_COIN_VALUES > 0) {
                for (int i = 0; i < vCoincidentSelect.length; i++) {
                    vCoincidentSelect[i] = tramatotaldata.substring((i * 2) + (9 * 2) + (NBR_SUMMATIONS * 2) + (NBR_DEMANDS * 2), (i * 2) + (NBR_SUMMATIONS * 2) + (NBR_DEMANDS * 2) + (9 * 2) + 2);
                }
                for (int i = 0; i < vDemandAssoc.length; i++) {
                    vDemandAssoc[i] = tramatotaldata.substring((i * 2) + (9 * 2) + (NBR_SUMMATIONS * 2) + (NBR_DEMANDS * 2) + (NBR_COIN_VALUES * 2), (i * 2) + (NBR_SUMMATIONS * 2) + (NBR_DEMANDS * 2) + (NBR_COIN_VALUES * 2) + (9 * 2) + 2);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void desglosaST023() {
        String tramadesglose = "";
        vdesgloseSt023 = "";
        for (int p = 0; p < vST023.size(); p++) {
            tramadesglose = "";
            for (int l = 0; l < vST023.get(p).length; l++) {
                tramadesglose += vST023.get(p)[l];
            }
            if (tramadesglose.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                tramadesglose = tramadesglose.substring(22, tramadesglose.length() - 4);
            } else {//es continuacion de bloques o retransmision de la primera trama
                if (p == 0) {//es la primera trama retransmitida
                    tramadesglose = tramadesglose.substring(20, tramadesglose.length() - 4);
                } else {//es continuacion de bloques
                    if (tramadesglose.substring(6, 8).equals("00")) {//es el ultimo bloque
                        tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 6);
                    } else {//es bloques intermedios
                        tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 4);
                    }
                }
            }
            //System.out.println("trama " + p + " " + tramadesglose);
            vdesgloseSt023 += tramadesglose;
        }

    }
    //metodo que entrega el orden de las tasas para los registros

    private void organizaSumations() {
        OrdenSumations = new String[NBR_SUMMATIONS];
        try {
            String tipocanal = "";
            String valorunidad;
            for (int i = 0; i < NBR_SUMMATIONS; i++) {
                //System.out.println("indice " + i);
                valorunidad = "";
                valorunidad = regMt017[7 * Integer.parseInt(vSumationSelect[i], 16)];
                //System.out.println("valor unidad " + regMt017[7 * Integer.parseInt(vSumationSelect[i], 16)]);
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

    }

    private void desglosaST027() {
        try {
            String tramatotaldata = "";
            for (int p = 0; p < vST027.size(); p++) {
                for (int l = 0; l < vST027.get(p).length; l++) {
                    tramatotaldata += vST027.get(p)[l];
                }
            }
            if (tramatotaldata.substring(0, 2).equals("06")) {
                tramatotaldata = tramatotaldata.substring(2, tramatotaldata.length());
            }
            //// System.out.println("Tratam 027 => " + tramatotaldata);
            vPresentdemandSelect = new String[NBR_PRESENT_DEMANDS];
            //System.out.println("Present demands :");
            vPresentValues = new String[NBR_PRESENT_VALUES];
            for (int i = 0; i < vPresentdemandSelect.length; i++) {
                vPresentdemandSelect[i] = tramatotaldata.substring((i * 2) + (9 * 2), (i * 2) + (9 * 2) + 2);
                System.out.print(vPresentdemandSelect[i]);
            }
            //System.out.println("\nPresent values :");
            for (int i = 0; i < vPresentValues.length; i++) {
                vPresentValues[i] = tramatotaldata.substring((i * 2) + (9 * 2) + (NBR_PRESENT_DEMANDS * 2), (i * 2) + (NBR_PRESENT_DEMANDS * 2) + (9 * 2) + 2);
                System.out.print(vPresentValues[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void desglosaST028() {
        try {
            String tramadesglose = "";
            vdesgloseSt028 = "";
            String vpdemands = "";
            String vpvalues = "";
            for (int p = 0; p < vST028.size(); p++) {
                tramadesglose = "";
                for (int l = 0; l < vST028.get(p).length; l++) {
                    tramadesglose += vST028.get(p)[l];
                }
                if (tramadesglose.substring(0, 2).equals("06")) {//tiene 06 es la primera trama
                    if (tramadesglose.substring(8, 10).equals("00")) {//viene un solo bloque
                        tramadesglose = tramadesglose.substring(20, tramadesglose.length() - 6);
                    } else {
                        tramadesglose = tramadesglose.substring(20, tramadesglose.length() - 4);
                    }
                } else {//es continuacion de bloques o retransmision de la primera trama
                    if (p == 0) {//es la primera trama retransmitida
                        if (tramadesglose.substring(6, 8).equals("00")) {//es el ultimo bloque
                            tramadesglose = tramadesglose.substring(18, tramadesglose.length() - 6);
                        } else {
                            tramadesglose = tramadesglose.substring(18, tramadesglose.length() - 4);
                        }
                    } else {//es continuacion de bloques
                        if (tramadesglose.substring(6, 8).equals("00")) {//es el ultimo bloque
                            tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 6);
                        } else {//es bloques intermedios
                            tramadesglose = tramadesglose.substring(12, tramadesglose.length() - 4);
                        }
                    }
                }
                //System.out.println("Trama cortada " + tramadesglose);
                vdesgloseSt028 += tramadesglose;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int obtenerElementCount() {
        int velementcount = 0;
        int int_status = ((int) ((NBR_BLK_INTS_SET1 + 7) / 8));
        int extend_status = (int) (numCanales / 2);
        if (extend_status == 0) {
            extend_status = 1;
        }
        int tamCanales = 2;
        velementcount = (((numCanales * tamCanales) + extend_status + 1) * NBR_BLK_INTS_SET1) + 5 + int_status;
        return velementcount;
    }

    public int obtenerOffset() {
        int offsetresultado = 0;
        offsetresultado = (ndias * (24 / ((NBR_BLK_INTS_SET1 * intervalos) / 60))) * (ElementCount);
        return offsetresultado;
    }

    public String[] EliminarRuido(String vectorhex[]) {
        String nuevoVector[] = vectorhex;
        int indiceInicio = 0;
        if (vectorhex.length >= 3) {
            for (int i = 0; i < vectorhex.length - 2; i++) {
                if (vectorhex[i].equals("06") && vectorhex[i + 1].equals("EE") && vectorhex[i + 2].equals("01")) {
                    nuevoVector = new String[vectorhex.length - indiceInicio];
                    System.arraycopy(vectorhex, i, nuevoVector, 0, nuevoVector.length);
                    break;
                } else {
                    indiceInicio++;
                }
            }
        }
        return nuevoVector;
    }

    public String[] EliminarRuido2(String vectorhex[]) {
        String nuevoVector[] = vectorhex;
        int indiceInicio = 0;
        if (vectorhex.length >= 2) {
            for (int i = 0; i < vectorhex.length - 2; i++) {
                if (vectorhex[i].equals("EE") && vectorhex[i + 1].equals("01")) {
                    if (((((byte) Integer.parseInt(vectorhex[i + 2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {

                        nuevoVector = new String[vectorhex.length - indiceInicio];
                        System.arraycopy(vectorhex, i, nuevoVector, 0, nuevoVector.length);
                        break;
                    } else {
                        indiceInicio++;
                    }

                } else {
                    indiceInicio++;
                }


            }
        }
        return nuevoVector;
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

    private void revisarComando(String[] vectorhex, String cadenahex) {
        if (vectorhex.length >= 1) {
            llegotramaini = true;
            if (cadenahex.contains("50 53 45")) {
                inicia1 = false;
                //es un A1800
                lcomandoI = false;
                lidentidad = true;
                spoling = false;
                bitSpoling = 0x00;
                tiemporetransmision = 8000;
                escribir("ENVIA identidad");
                byte[] tramaidentidad = tramasElster.getIdentidad();
                seEspera06 = true;
                tramaidentidad[1] = (byte) 0x01;
                byte tramanueva[] = calcularnuevocrc(tramaidentidad);
                ultimatramaEnviada = tramanueva;
                enviaTrama1(tramanueva, "");
            } else {
                inicia1 = false;
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion Error de protocolo");
                cerrarPuerto();
                cerrarLog("Desconexion Error de protocolo", false);
                escribir("Estado Lectura No Leido");
                leer = false;
            }
        } else {
            reiniciaComuniacion();
        }
    }

    private void revisarIdentidad(String[] vectorhex) { //revisa tramas de identidad
        if (seEspera06) { //si se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) { // se verifica si llega el 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16) + 9))) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    actualUltimatrama = 0;
                                    //verificamos  el byte feature
                                    if (vectorhex[11].equals("02")) {
                                        //lleva ticket
                                        isticket = true;
                                    } else {
                                        //no lleva ticket
                                        isticket = false;
                                    }
                                    byte[] ticket = new byte[8];
                                    int i = 0;
                                    for (int j = 15; j < 23; j++) {
                                        ticket[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                                        i++;
                                    }
                                    if (isticket) {
                                        //System.out.println("ticket " + tramasElster.encode(ticket, ticket.length));
                                        escribir("ticket " + tramasElster.encode(ticket, ticket.length));
                                        //user = DES2.encrypt(ticket, tramasElster.getK());
                                        String vpass[] = password.trim().split("-");
                                        if (vpass.length == 2) {
                                            nivelacceso = Integer.parseInt(vpass[1]);
                                        }
                                        user = tramasElster.encrypt(tramasElster.encode(ticket, ticket.length), vpass[0]);
                                        ////System.out.println("ENC " + tramasElster.encode(user, user.length));
                                        escribir("ENC " + tramasElster.encode(user, user.length));
                                        ////System.out.println("Nivel de acceso " + nivelacceso);
                                        escribir("Nivel de acceso " + nivelacceso);
                                    }


                                    //enviamos el dato con el id selecionado
                                    escribir("Envia velocidad");
                                    try {
                                        avisoStr("Configuracion Velocidad");
                                    } catch (Exception e) {
                                    }
                                    byte trama[] = tramasElster.getVelocidad();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lidentidad = false;
                                    lvelocidad = true;
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //validamos ack del data
                                    reiniciaComuniacion();
                                }
                            } else {
                                //es la trama anterior
                                enviaTrama2(ultimatramaEnviada, "Solicitud de identidad");
                            }
                        } else {
                            //bad crc
                            escribir("BAD CRC");
                            enviaTrama1(badcrc, "Solicitud de identidad");
                            //enviaTramaUltima(ultimatramaEnviada, "Solicitud de identidad");
                        }

                    } else {
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de identidad");
                    }
                } else {
                    //no tiene cabecera
                    if (vectorhex[0].equals("06")) {//llegoel 06 solo
                        reiniciaComuniacion();
                    }
                }
            } else {
                //lego algo diferente cuando se esperaba un 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud de identidad");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de identidad");
                }
            }
        } else {
            //no se espera un 06 se espera si es un complemento o retransmision
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) { //esperamos una retransmision o un complemento
                //validamos si es un complemento o retransmision
                if (siguienteTrama) {//es complemento de una trama
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos crc para
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado

                                //verificamos  el byte feature
                                if (vectorhex[10].equals("02")) {
                                    //lleva ticket
                                    isticket = true;
                                } else {
                                    //no lleva ticket
                                    isticket = false;
                                }
                                byte[] ticket = new byte[8];
                                int i = 0;
                                for (int j = 14; j < 22; j++) {
                                    ticket[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                                    i++;
                                }
                                if (isticket) {
                                    escribir("ticket " + tramasElster.encode(ticket, ticket.length));
                                    //user = DES2.encrypt(ticket, tramasElster.getK());
                                    String vpass[] = password.trim().split("-");
                                    if (vpass.length == 2) {
                                        nivelacceso = Integer.parseInt(vpass[1]);
                                    }
                                    user = tramasElster.encrypt(tramasElster.encode(ticket, ticket.length), vpass[0]);
                                    escribir("ENC " + tramasElster.encode(user, user.length));
                                    escribir("Nivel de acceso " + nivelacceso);
                                }

                                //enviamos el dato con el id selecionado
                                escribir("Envia velocidad");
                                try {
                                    avisoStr("Configuracion Velocidad");
                                } catch (Exception e) {
                                }
                                byte trama[] = tramasElster.getVelocidad();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lidentidad = false;
                                lvelocidad = true;
                                seEspera06 = true;
                                ultimatramaEnviada = nuevatrama;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                //es la trama anterior
                                enviaTrama2(ultimatramaEnviada, "Solicitud de identidad");
                            }
                        } else {
                            //bad crc
                            escribir("BAD CRC");
                            enviaTrama1(badcrc, "Solicitud de identidad");
                        }
                    } else {
                        //llego incompleta se guarda la trama para juntarla con la anterior
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de identidad");
                    }
                } else {// es una retransmision
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos crc para
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                //  verificamos el byte feature
                                if (vectorhex[10].equals("02")) {
                                    //lleva ticket
                                    isticket = true;
                                } else {
                                    //no lleva ticket
                                    isticket = false;
                                }
                                byte[] ticket = new byte[8];
                                int i = 0;
                                for (int j = 14; j < 22; j++) {
                                    ticket[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                                    i++;
                                }
                                if (isticket) {
                                    escribir("ticket " + tramasElster.encode(ticket, ticket.length));
                                    String vpass[] = password.trim().split("-");
                                    if (vpass.length == 2) {
                                        nivelacceso = Integer.parseInt(vpass[1]);
                                    }
                                    user = tramasElster.encrypt(tramasElster.encode(ticket, ticket.length), vpass[0]);
                                    escribir("ENC " + tramasElster.encode(user, user.length));
                                    escribir("Nivel de acceso " + nivelacceso);
                                }
                                //enviamos el dato con el id selecionado
                                escribir("Envia velocidad");
                                try {
                                    avisoStr("Configuracion Velocidad");
                                } catch (Exception e) {
                                }
                                byte trama[] = tramasElster.getVelocidad();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lidentidad = false;
                                lvelocidad = true;
                                seEspera06 = true;
                                ultimatramaEnviada = nuevatrama;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                //es la trama anterior
                                escribir("Bad bitspoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud de identidad");
                            }
                        } else {
                            //bad crc
                            escribir("badcrc");
                            enviaTrama1(badcrc, "Solicitud de identidad");
                        }
                    } else {
                        //llego incompleta se guarda la trama para juntarla con la anterior
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de identidad");
                    }
                }
            } else { // Esperamos un complemento
                if (complemento) { //verificamos si llega un complemento
                    complemento = false;
                    //verificamos si es un complemento de una trama 06 o EE
                    if (siguienteTrama) { //complemento de una trama EE
                        siguienteTrama = false;
                        tramaIncompleta = tramaIncompleta + " " + cadenahex;//completamos la trama con el resto de esta
                        String[] vectorhextemp = tramaIncompleta.trim().split(" ");  //convertimos las tramas pegadas en vector
                        tramaIncompleta = "";
                        //validamos el tamaño
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                siguienteTrama = false;
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos crc para
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        //enviamos la siguiente trama
                                        //verificamos el byte feature
                                        if (vectorhex[10].equals("02")) {
                                            //lleva ticket
                                            isticket = true;
                                        } else {
                                            //no lleva ticket
                                            isticket = false;
                                        }
                                        byte[] ticket = new byte[8];
                                        int i = 0;
                                        for (int j = 14; j < 22; j++) {
                                            ticket[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                                            i++;
                                        }
                                        if (isticket) {
                                            escribir("ticket " + tramasElster.encode(ticket, ticket.length));
                                            //user = DES2.encrypt(ticket, tramasElster.getK());
                                            String vpass[] = password.trim().split("-");
                                            if (vpass.length == 2) {
                                                nivelacceso = Integer.parseInt(vpass[1]);
                                            }
                                            user = tramasElster.encrypt(tramasElster.encode(ticket, ticket.length), vpass[0]);
                                            escribir("ENC " + tramasElster.encode(user, user.length));
                                            escribir("Nivel de acceso " + nivelacceso);
                                        }
                                        //enviamos el dato con el id selecionado
                                        try {
                                            avisoStr("Configuracion Velocidad");
                                        } catch (Exception e) {
                                        }
                                        byte trama[] = tramasElster.getVelocidad();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lidentidad = false;
                                        lvelocidad = true;
                                        seEspera06 = true;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud de identidad");
                                    }
                                } else {
                                    //badcrc
                                    escribir("badcrc");
                                    enviaTrama1(badcrc, "Solicitud de identidad");
                                }
                            } else {
                                //se corrompe la trama
                                reiniciaComuniacion();
                            }
                        } else {
                            reiniciaComuniacion();
                        }

                    } else { //complemento de uan trama 06
                        tramaIncompleta = tramaIncompleta + " " + cadenahex;//completamos la trama con el resto de esta
                        String[] vectorhextemp = tramaIncompleta.trim().split(" ");
                        tramaIncompleta = "";
                        //validamos el tamaño
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos crc para
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        //  verificamos el byte feature
                                        if (vectorhex[11].equals("02")) {
                                            //lleva ticket
                                            isticket = true;
                                        } else {
                                            //no lleva ticket
                                            isticket = false;
                                        }
                                        byte[] ticket = new byte[8];
                                        int i = 0;
                                        for (int j = 15; j < 23; j++) {
                                            ticket[i] = (byte) (Integer.parseInt(vectorhex[j], 16) & 0xFF);
                                            i++;
                                        }
                                        if (isticket) {
                                            //System.out.println("ticket " + tramasElster.encode(ticket, ticket.length));
                                            escribir("ticket " + tramasElster.encode(ticket, ticket.length));
                                            //user = DES2.encrypt(ticket, tramasElster.getK());
                                            String vpass[] = password.trim().split("-");
                                            if (vpass.length == 2) {
                                                nivelacceso = Integer.parseInt(vpass[1]);
                                            }
                                            user = tramasElster.encrypt(tramasElster.encode(ticket, ticket.length), vpass[0]);
                                            escribir("ENC " + tramasElster.encode(user, user.length));
                                            escribir("Nivel de acceso " + nivelacceso);
                                        }

                                        //enviamos el dato con el id selecionado
                                        escribir("Envia velocidad");
                                        try {
                                            avisoStr("Configuracion Velocidad");
                                        } catch (Exception e) {
                                        }
                                        byte trama[] = tramasElster.getVelocidad();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lidentidad = false;
                                        lvelocidad = true;
                                        seEspera06 = true;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud de identidad");
                                    }
                                } else {
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud de identidad");
                                }
                            } else {
                                //se corrompe la trama
                                reiniciaComuniacion();
                            }
                        } else {
                            reiniciaComuniacion();
                        }
                    }
                } else {//no se esperaba un complemento
                    //es trama basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud de identidad");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de identidad");
                    }
                }
            }
        }
    }

    private void revisarVelocidad(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego un 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) {
                                    actualReintento = 0;
                                    byte trama[] = tramasElster.getLogon();
                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lvelocidad = false;
                                    llogon = true;
                                    tiempo = 1000;
                                    escribir("Envia logon");
                                    try {
                                        avisoStr("Logon");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }

                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud Velocidad");
                            }

                        } else {//badcrc
                            enviaTrama1(badcrc, "Solicitud Velocidad");
                        }
                    } else {//trama incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Velocidad");
                    }

                } else { //No es una trama ya que no tiene cabecera pero puede venir una trama de desconexion
                    reiniciaComuniacion();
                }
            } else {//llego algo diferente al 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud Velocidad");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Velocidad");
                }
            }
        } else {// es EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {// es el bloque siguiente o retransmision
                //es  un bloque continuo
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                actualReintento = 0;
                                byte trama[] = tramasElster.getLogon();
                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lvelocidad = false;
                                llogon = true;
                                tiempo = 1000;
                                escribir("Envia logon");
                                try {
                                    avisoStr("Logon");
                                } catch (Exception e) {
                                }
                                seEspera06 = true;
                                siguienteTrama = false;
                                ultimatramaEnviada = nuevatrama;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud Velocidad");
                            }

                        } else {//bad CRC
                            siguienteTrama = false;
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud Velocidad");
                        }
                    } else {//llego incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Velocidad");
                    }
                } else {//no llego trama
                    reiniciaComuniacion();
                }

            } else { //es un complemento o basura
                if (complemento) {
                    //validamos si es complemento de 06 o EE
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (siguienteTrama) {// es complemento de EE
                        siguienteTrama = false;
                        if (vectorhex.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        actualReintento = 0;
                                        byte trama[] = tramasElster.getLogon();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lvelocidad = false;
                                        llogon = true;
                                        tiempo = 1000;
                                        escribir("Envia logon");
                                        try {
                                            avisoStr("Logon");
                                        } catch (Exception e) {
                                        }
                                        seEspera06 = true;
                                        siguienteTrama = false;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud Velocidad");
                                    }

                                } else {
                                    //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud Velocidad");
                                }
                            } else {
                                //trama corrupta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud Velocidad");
                            }
                        } else {
                            //no llego trama
                            reiniciaComuniacion();
                        }
                    } else {//es complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        actualReintento = 0;
                                        byte trama[] = tramasElster.getLogon();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lvelocidad = false;
                                        llogon = true;
                                        tiempo = 1000;
                                        escribir("Envia logon");
                                        try {
                                            avisoStr("Logon");
                                        } catch (Exception e) {
                                        }
                                        seEspera06 = true;
                                        siguienteTrama = false;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud Velocidad");
                                    }

                                } else {
                                    //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud Velocidad");
                                }
                            } else {
                                //trama corrupta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud Velocidad");
                            }
                        } else {
                            //no llego trama
                            reiniciaComuniacion();
                        }
                    }
                } else {//es basura
                    siguienteTrama = false;
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud Velocidad");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Velocidad");
                    }
                }
            }
        }
    }

    private void revisarLogon(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {// ack
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16) + 9))) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                actualReintento = 0;
                                if (isticket) {
                                    //hay autenticacion
                                    byte trama[] = tramasElster.getAutenticacion();

                                    trama[2] = getSpoling(bitSpoling);
                                    int i = 0;
                                    trama[8] = (byte) (Integer.parseInt(Integer.toHexString(nivelacceso), 16) & 0xFF);
                                    try {
                                        for (int j = 9; j < 17; j++) {
                                            trama[j] = user[i];
                                            i++;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    llogon = false;
                                    lautenticacion = true;
                                    tiempo = 1000;
                                    escribir("Envia autenticacion");
                                    try {
                                        avisoStr("Autenticacion");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //se salta la trama de autenticacion

                                    byte trama[] = tramasElster.getTiming();

                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    llogon = false;
                                    ltiming = true;
                                    seEspera06 = true;
                                    try {
                                        avisoStr("Timing");
                                    } catch (Exception e) {
                                    }
                                    escribir("Envia Timing");
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                            }

                        } else {// bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud Logon");
                        }
                    } else {// trama incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                } else {//no posee cabecera
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                }
            } else {//llego algo diferente cuando se esperaba el 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud Logon");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                }
            }
        } else {//es una retransmision o un complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {// Es una retransmision o siguiente bloque
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16) + 8))) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                actualReintento = 0;
                                if (isticket) {
                                    //hay autenticacion
                                    byte trama[] = tramasElster.getAutenticacion();

                                    trama[2] = getSpoling(bitSpoling);
                                    int i = 0;
                                    trama[8] = (byte) (Integer.parseInt(Integer.toHexString(nivelacceso), 16) & 0xFF);
                                    try {
                                        for (int j = 9; j < 17; j++) {
                                            trama[j] = user[i];
                                            i++;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    llogon = false;
                                    lautenticacion = true;
                                    tiempo = 1000;
                                    seEspera06 = true;
                                    try {
                                        avisoStr("Autenticacion");
                                    } catch (Exception e) {
                                    }

                                    escribir("Envia autenticacion");
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //se salta la trama de autenticacion

                                    byte trama[] = tramasElster.getTiming();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    llogon = false;
                                    ltiming = true;
                                    seEspera06 = true;
                                    try {
                                        avisoStr("Timing");
                                    } catch (Exception e) {
                                    }
                                    escribir("Envia Timing");
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud Logon");
                        }
                    } else {//llego incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                } else { //no llego nada
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                }
            } else {// es un complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("06")) { // complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        actualReintento = 0;
                                        if (isticket) {
                                            //hay autenticacion
                                            byte trama[] = tramasElster.getAutenticacion();

                                            trama[2] = getSpoling(bitSpoling);
                                            int i = 0;
                                            trama[8] = (byte) (Integer.parseInt(Integer.toHexString(nivelacceso), 16) & 0xFF);
                                            try {
                                                for (int j = 9; j < 17; j++) {
                                                    trama[j] = user[i];
                                                    i++;
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            llogon = false;
                                            lautenticacion = true;
                                            tiempo = 1000;
                                            seEspera06 = true;

                                            escribir("Envia autenticacion");
                                            try {
                                                avisoStr("Autenticacion");
                                            } catch (Exception e) {
                                            }
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //se salta la trama de autenticacion

                                            byte trama[] = tramasElster.getTiming();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            llogon = false;
                                            ltiming = true;
                                            seEspera06 = true;
                                            escribir("Envia Timing");
                                            try {
                                                avisoStr("Timing");
                                            } catch (Exception e) {
                                            }
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                                    }
                                } else { //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud Logon");
                                }
                            } else { //corrupta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                            }
                        } else {
                            reiniciaComuniacion();
                        }
                    } else if (vectorhextemp[0].equals("EE")) { //complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        actualReintento = 0;
                                        if (isticket) {
                                            //hay autenticacion
                                            byte trama[] = tramasElster.getAutenticacion();

                                            trama[2] = getSpoling(bitSpoling);
                                            int i = 0;
                                            trama[8] = (byte) (Integer.parseInt(Integer.toHexString(nivelacceso), 16) & 0xFF);
                                            try {
                                                for (int j = 9; j < 17; j++) {
                                                    trama[j] = user[i];
                                                    i++;
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            llogon = false;
                                            lautenticacion = true;
                                            tiempo = 1000;
                                            seEspera06 = true;
                                            try {
                                                avisoStr("Autenticacion");
                                            } catch (Exception e) {
                                            }
                                            escribir("Envia autenticacion");
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //se salta la trama de autenticacion
                                            byte trama[] = tramasElster.getTiming();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            llogon = false;
                                            ltiming = true;
                                            seEspera06 = true;
                                            try {
                                                avisoStr("Timing");
                                            } catch (Exception e) {
                                            }
                                            escribir("Envia Timing");
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                                    }
                                } else { //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud Logon");
                                }
                            } else { //corrupta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                            }
                        } else {
                            reiniciaComuniacion();
                        }
                    } else {
                        reiniciaComuniacion();
                    }

                } else { //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud Logon");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                }
            }
        }
    }

    private void revisarAutenticacion(String[] vectorhex) {
        if (seEspera06) {//esperamos un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {// llego un 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    byte trama[] = tramasElster.getTiming();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lautenticacion = false;
                                    ltiming = true;
                                    tiempo = 1000;

                                    escribir("ENVIA ltiming");
                                    try {
                                        avisoStr("Timing");
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");

                                } else {
                                    //valida ack del data
                                    escribir("Desconexion - Error de autenticacion");
                                    escribir("Revisar nivel de acceso y contraseña");
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                            }

                        } else {
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud Logon");
                        }
                    } else {//llego incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                } else {//llego sin cabecera
                    if (vectorhex.length == 1) {
                        reiniciaComuniacion();
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                }
            } else {//llego algo cuando no se esperaba
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud Logon");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                }
            }
        } else { // esperemos complemento o retransmision
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {// es retransmision
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[6].equals("00")) { //ack del data
                                    byte trama[] = tramasElster.getTiming();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lautenticacion = false;
                                    ltiming = true;
                                    tiempo = 1000;
                                    escribir("ENVIA ltiming");
                                    try {
                                        avisoStr("Timing");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");

                                } else {
                                    //valida ack del data
                                    escribir("Desconexion - Error de autenticacion");
                                    escribir("Revisar nivel de acceso y contraseña");
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud Logon");
                        }
                    } else {//trama incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                } else { //llego incompleta
                    if (vectorhex.length == 1) {
                        reiniciaComuniacion();
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                }
            } else {//es comeplemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            byte trama[] = tramasElster.getTiming();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lautenticacion = false;
                                            ltiming = true;
                                            tiempo = 1000;
                                            escribir("ENVIA ltiming");
                                            try {
                                                avisoStr("Timing");
                                            } catch (Exception e) {
                                            }
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //valida ack del data
                                            escribir("Desconexion - Error de autenticacion");
                                            escribir("Revisar nivel de acceso y contraseña");
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                                    }

                                } else {
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud Logon");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                            }
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                        }
                    } else if (vectorhextemp[0].equals("EE")) {//complemento EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[6].equals("00")) { //ack del data
                                            byte trama[] = tramasElster.getTiming();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lautenticacion = false;
                                            ltiming = true;
                                            tiempo = 1000;
                                            escribir("ENVIA ltiming");
                                            try {
                                                avisoStr("Timing");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //valida ack del data
                                            escribir("Desconexion - Error de autenticacion");
                                            escribir("Revisar nivel de acceso y contraseña");
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud Logon");
                                    }

                                } else {
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud Logon");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                            }
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                        }
                    } else {
                        reiniciaComuniacion();
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud Logon");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logon");
                    }
                }
            }
        }
    }

    private void revisarTiming(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {// llego un 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00") || vectorhex[7].equals("0A")) { //ack del data
                                    byte trama[] = tramasElster.getST06();
                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    ltiming = false;
                                    lST06 = true;
                                    seEspera06 = true;
                                    tiempo = 4000;
                                    escribir("Envia LST06");
                                    try {
                                        avisoStr("Serie medidor");
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud timing");
                            }

                        } else {// badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud timing");
                        }
                    } else {//trama incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                    }
                } else {//llego sin cabecera
                    if (vectorhex.length == 1) {
                        reiniciaComuniacion();
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                    }
                }
            } else {//se espera algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud timing");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                }
            }
        } else { //lego algo diferente cuando se esperaba un 06
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {//llego un EE
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[6].equals("00") || vectorhex[6].equals("0A")) { //ack del data
                                    byte trama[] = tramasElster.getST06();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    ltiming = false;
                                    lST06 = true;
                                    seEspera06 = true;
                                    tiempo = 4000;
                                    //System.out.println("Envia LST06");
                                    escribir("Envia LST06");
                                    try {
                                        avisoStr("Serie medidor");
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud timing");
                            }
                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud timing");
                        }
                    } else {//llego incompleta
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                    }
                } else { //no llego cabecera
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                }
            } else {//es un complemento
                if (complemento) { // hay cun complemento
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    //System.out.println("trama completa " + tramaIncompleta);
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//llego un 06
                        if (vectorhextemp.length > 7) {//tiene cabecera
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00") || vectorhextemp[7].equals("0A")) { //ack del data
                                            byte trama[] = tramasElster.getST06();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            ltiming = false;
                                            lST06 = true;
                                            seEspera06 = true;
                                            tiempo = 4000;
                                            escribir("Envia LST06");
                                            try {
                                                avisoStr("Serie medidor");
                                            } catch (Exception e) {
                                            }
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud timing");
                                    }
                                } else {// badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud timing");
                                }
                            } else {// trama incompleta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                            }
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                        }
                    } else {
                        if (vectorhextemp[0].equals("EE")) {
                            if (vectorhextemp.length > 6) {//pose cabecera?
                                if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                                    if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                        if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                            if (vectorhextemp[6].equals("00") || vectorhextemp[6].equals("0A")) { //ack del data
                                                byte trama[] = tramasElster.getST06();

                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                ltiming = false;
                                                lST06 = true;
                                                seEspera06 = true;
                                                tiempo = 4000;
                                                escribir("LST06");
                                                try {
                                                    avisoStr("Serie medidor");
                                                } catch (Exception e) {
                                                }
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        } else {
                                            escribir("bad bit spoling");
                                            enviaTrama2(ultimatramaEnviada, "Solicitud timing");
                                        }
                                    } else {//bad crc
                                        escribir("bad crc");
                                        enviaTrama1(badcrc, "Solicitud timing");
                                    }
                                } else {//llego incompleta
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                                }
                            } else { //no llego cabecera
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                            }
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                        }
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud timing");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud timing");
                    }
                }
            }
        }

    }

    private void revisarST06(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego un 06
                if (vectorhex.length > 7) {//tiene cabecera
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validacion crc
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    if (!vectorhex[4].equals("00")) {
                                        siguienteTrama = true;
                                        getSpoling(bitSpoling);
                                        enviaTramack("");
                                    } else {
                                        byte trama[] = tramasElster.getST05();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST06 = false;
                                        lST05 = true;
                                        tiempo = 1000;
                                        escribir("Envia ST005");
                                        try {
                                            avisoStr("Validando medidor");
                                        } catch (Exception e) {
                                        }
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    }
                                } else {//nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud de ST06");
                            }

                        } else {//Bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud de ST06");
                        }
                    } else {// llego incompleta la trama se guarda el tramo y se espera a la segunda parte
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de ST06");
                    }
                } else {// reviamos si el tamaño es 1 y es 06
//                            if (vectorhex.length == 1) {//llego el 06 solo
//                                reiniciaComuniacion();
//                            } else {//llego mocha
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
////                            }
                }
            } else { //llego algo diferente cuando se esperaba un 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST06");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
                }

            }

        } else {//llega una trama EE o un complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) { //es la siguiente trama de un bloque
                if (vectorhex.length > 6) {//tiene cabecera
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (!vectorhex[3].equals("00")) { //Validamos si es la ultima trama
                                    siguienteTrama = true;
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                } else {//es la ultima trama  se envia la proxima peticion
                                    byte trama[] = tramasElster.getST05();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST06 = false;
                                    lST05 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST005");
                                    try {
                                        avisoStr("Validando medidor");
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST06");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST06");
                        }
                    } else {// llego incompleta se guarda la trama para juntarla con otra
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de ST06");
                    }
                } else {
                    if (vectorhex.length == 1) {//llego el 06 solo
                        reiniciaComuniacion();
                    } else {//llego mocha
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
                    }
                }
            } else { //es n complemento de una trama mocha
                if (complemento) {// es un complemento de 06 0 de EE
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("EE")) {//es un complemento de EE
                        siguienteTrama = false;
                        if (vectorhextemp.length > 6) {//tiene cabecera
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {
                                    if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[3].equals("00")) {
                                            byte trama[] = tramasElster.getST05();
                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST06 = false;
                                            lST05 = true;
                                            tiempo = 1000;
                                            escribir("Envia ST005");
                                            try {
                                                avisoStr("Validando medidor");
                                            } catch (Exception e) {
                                            }
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            siguienteTrama = true;
                                            getSpoling(bitSpoling);
                                            enviaTramack("");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud de ST06");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud de ST06");
                                }
                            } else { //trama se corrompe
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
                            }
                        } else {
                            if (vectorhex.length == 1) {//llego el 06 solo
                                reiniciaComuniacion();
                            } else {//llego mocha
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
                            }
                        }
                    } else { //es un complemento de 06
                        if (vectorhextemp.length > 7) {//tiene cabecera
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        //validamos ack
                                        if (vectorhextemp[7].equals("00")) {
                                            if (vectorhextemp[4].equals("00")) {
                                                byte trama[] = tramasElster.getST05();
                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST06 = false;
                                                lST05 = true;
                                                tiempo = 1000;
                                                escribir("Envia ST005");
                                                try {
                                                    avisoStr("Validando medidor");
                                                } catch (Exception e) {
                                                }
                                                ultimatramaEnviada = nuevatrama;
                                                seEspera06 = true;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                siguienteTrama = true;
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        } else {
                                            //nack reiniciamos
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST06");
                                    }

                                } else {
                                    //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST06");
                                }
                            } else {
                                //llega incompleta se corrompe
                                reiniciaComuniacion();
                            }
                        } else { //no llego trama
                            if (vectorhex.length == 1) {//llego el 06 solo
                                reiniciaComuniacion();
                            } else {//llego mocha
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
                            }
                        }
                    }
                } else { // es trama basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST06");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST06");
                    }
                }
            }
        }
    }

    private void revisarST05(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    boolean continuar = false;
                                    try {
                                        String datoserial = vectorhex[22] + vectorhex[23] + vectorhex[24] + vectorhex[25] + vectorhex[26] + vectorhex[27] + vectorhex[28] + vectorhex[29];
                                        datoserial = Hex2ASCII(datoserial);
                                        if (seriemedidor.equals(datoserial)) {
                                            escribir("Numero de serial " + datoserial);
                                            continuar = true;
                                        } else {
                                            escribir("Numero de serial incorrecto");
                                        }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (continuar) {
                                        byte trama[] = tramasElster.getST55FechaAct();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST05 = false;
                                        lST055factual = true;
                                        tiempo = 1000;
                                        escribir("Envia ST055");
                                        try {
                                            avisoStr("Fecha actual");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        cerrarPuerto();
                                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion error en serial");
                                        cerrarLog("Error de serial", false);
                                        leer = false;
                                    }

                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST05");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST05");
                        }
                    } else {//trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                    }
                } else {
//                    if (vectorhex.length == 1) {
//                        reiniciaComuniacion();
//                    } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
//                    }

                }
            } else {//llego algo direferente cuando se esperaba un 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST05");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                }
            }
        } else {//es un complemento o continuacion de bloque o retransmision
            vectorhex = EliminarRuido2(vectorhex);
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[6].equals("00")) { //ack del data
                                    boolean continuar = false;
                                    try {
                                        String datoserial = vectorhex[21] + vectorhex[22] + vectorhex[23] + vectorhex[24] + vectorhex[25] + vectorhex[26] + vectorhex[27] + vectorhex[28];
                                        datoserial = Hex2ASCII(datoserial);
                                        if (seriemedidor.equals(datoserial)) {
                                            escribir("Numero de serial " + datoserial);
                                            continuar = true;
                                        } else {
                                            escribir("Numero de serial incorrecto");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (continuar) {
                                        byte trama[] = tramasElster.getST55FechaAct();
                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST05 = false;
                                        lST055factual = true;
                                        tiempo = 1000;
                                        escribir("Envia ST055");
                                        try {
                                            avisoStr("Fecha actual");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        cerrarPuerto();
                                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion error en serial");
                                        cerrarLog("Error de serial", false);
                                        leer = false;
                                    }

                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST05");
                            }

                        } else {
                            //badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST05");
                        }
                    } else { //incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                    }
                } else {//no tiene cabecera
                    if (vectorhex.length == 1) {
                        reiniciaComuniacion();
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                    }
                }
            } else { //complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            boolean continuar = false;
                                            try {
                                                String datoserial = vectorhextemp[22] + vectorhextemp[23] + vectorhextemp[24] + vectorhextemp[25] + vectorhextemp[26] + vectorhextemp[27] + vectorhextemp[28] + vectorhextemp[29];
                                                datoserial = Hex2ASCII(datoserial);
                                                if (seriemedidor.equals(datoserial)) {
                                                    escribir("Numero de serial " + datoserial);
                                                    continuar = true;
                                                } else {
                                                    escribir("Numero de serial incorrecto");
                                                }
////                                    System.out.println("Serial" + val);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (continuar) {
                                                byte trama[] = tramasElster.getST55FechaAct();
                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST05 = false;
                                                lST055factual = true;
                                                tiempo = 1000;
                                                //System.out.println("Envia ST055");
                                                escribir("Envia ST055");
                                                try {
                                                    avisoStr("Fecha actual");
                                                } catch (Exception e) {
                                                }
                                                intentoescuchar = 0;
                                                ultimatramaEnviada = nuevatrama;
                                                seEspera06 = true;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                cerrarPuerto();
                                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion error en serial");
                                                cerrarLog("Error de serial", false);
                                                leer = false;
                                            }

                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST05");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST05");
                                }
                            } else {//trama incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                            }
                        } else {
                            if (vectorhex.length == 1) {
                                reiniciaComuniacion();
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                            }
                        }
                    } else { //complemento EE
                        if (vectorhextemp[0].equals("EE")) {
                            if (vectorhextemp.length > 6) {//pose cabecera?
                                if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                    if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                        if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                            if (vectorhex[6].equals("00")) { //ack del data
                                                boolean continuar = false;
                                                try {
                                                    String datoserial = vectorhextemp[21] + vectorhextemp[22] + vectorhextemp[23] + vectorhextemp[24] + vectorhextemp[25] + vectorhextemp[26] + vectorhextemp[27] + vectorhextemp[28];
                                                    datoserial = Hex2ASCII(datoserial);
                                                    if (seriemedidor.equals(datoserial)) {
                                                        escribir("Numero de serial " + datoserial);
                                                        continuar = true;
                                                    } else {
                                                        escribir("Numero de serial incorrecto");
                                                    }
////                                    System.out.println("Serial" + val);

                                                } catch (Exception e) {
                                                    continuar = false;
                                                    e.printStackTrace();
                                                }
                                                if (continuar) {
                                                    byte trama[] = tramasElster.getST55FechaAct();
                                                    trama[2] = getSpoling(bitSpoling);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    lST05 = false;
                                                    lST055factual = true;
                                                    tiempo = 1000;
                                                    escribir("Envia ST055");
                                                    try {
                                                        avisoStr("Fecha actual");
                                                    } catch (Exception e) {
                                                    }
                                                    intentoescuchar = 0;
                                                    ultimatramaEnviada = nuevatrama;
                                                    seEspera06 = true;
                                                    enviaTrama2(nuevatrama, "");
                                                } else {
                                                    cerrarPuerto();
                                                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNElster", "Desconexion error en serial");
                                                    cerrarLog("Error de serial", false);
                                                    leer = false;
                                                }

                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        } else {
                                            escribir("bad bit spoling");
                                            enviaTrama2(ultimatramaEnviada, "Solicitud ST05");
                                        }
                                    } else {
                                        //badcrc
                                        escribir("bad crc");
                                        enviaTrama1(badcrc, "Solicitud ST05");
                                    }
                                } else { //incompleta
                                    tramaIncompleta = cadenahex;
                                    complemento = true;
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                                }
                            } else {//no tiene cabecera
                                if (vectorhex.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                                }
                            }
                        } else {//basura
                            if (vectorhex.length == 1) {
                                reiniciaComuniacion();
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                            }
                        }
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST05");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST05");
                    }
                }
            }

        }
    }

    private void revisarST055(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    //fecha actual
                                    String año = "" + Integer.parseInt(vectorhex[10], 16);
                                    String mes = "" + Integer.parseInt(vectorhex[11], 16);
                                    if (mes.length() == 1) {
                                        mes = "0" + mes;
                                    }
                                    String dia = "" + Integer.parseInt(vectorhex[12], 16);
                                    if (dia.length() == 1) {
                                        dia = "0" + dia;
                                    }
                                    String hora = "" + Integer.parseInt(vectorhex[13], 16);
                                    if (hora.length() == 1) {
                                        hora = "0" + hora;
                                    }
                                    String min = "" + Integer.parseInt(vectorhex[14], 16);
                                    if (min.length() == 1) {
                                        min = "0" + min;
                                    }
                                    String seg = "" + Integer.parseInt(vectorhex[15], 16);
                                    if (seg.length() == 1) {
                                        seg = "0" + seg;
                                    }
                                    String fechaactual1 = "" + año + mes + dia + hora + min + seg;
                                    try {
                                        escribir("Fecha actual Medidor " + sdf.parse(fechaactual1).toString());
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
                                        }
                                    } catch (Exception e) {
                                    }
                                    byte trama[] = tramasElster.getST61ConfiPerfil();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST055factual = false;
                                    lST061 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST061");
                                    try {
                                        avisoStr("Configuracion Perfil Carga");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {//nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                            }
                        } else {////bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST055");
                        }
                    } else {//tramaincompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                }
            } else {//llego algo inesperado
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST055");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                }
            }
        } else {//se espera EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                //fecha actual
                                String año = "" + Integer.parseInt(vectorhex[9], 16);
                                String mes = "" + Integer.parseInt(vectorhex[10], 16);
                                if (mes.length() == 1) {
                                    mes = "0" + mes;
                                }
                                String dia = "" + Integer.parseInt(vectorhex[11], 16);
                                if (dia.length() == 1) {
                                    dia = "0" + dia;
                                }
                                String hora = "" + Integer.parseInt(vectorhex[12], 16);
                                if (hora.length() == 1) {
                                    hora = "0" + hora;
                                }
                                String min = "" + Integer.parseInt(vectorhex[13], 16);
                                if (min.length() == 1) {
                                    min = "0" + min;
                                }
                                String seg = "" + Integer.parseInt(vectorhex[14], 16);
                                if (seg.length() == 1) {
                                    seg = "0" + seg;
                                }
                                String fechaactual1 = "" + año + mes + dia + hora + min + seg;
                                try {
                                    escribir("Fecha actual Medidor " + sdf.parse(fechaactual1).toString());
                                    escribir("Fecha actual PC " + Date.from(ZonedDateTime.now(zid).toInstant()).toString());
                                } catch (Exception e) {
                                }
                                byte trama[] = tramasElster.getST61ConfiPerfil();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lST055factual = false;
                                lST061 = true;
                                tiempo = 1000;
                                try {
                                    avisoStr("Configuracion Perfil Carga");
                                } catch (Exception e) {
                                }
                                seEspera06 = true;
                                ultimatramaEnviada = nuevatrama;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                            }

                        } else { //bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST055");
                        }
                    } else {//tramaincompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");

                    }
                } else {//no tiene cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                        }
                    } else {
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            //fecha actual
                                            String año = "" + Integer.parseInt(vectorhextemp[10], 16);
                                            String mes = "" + Integer.parseInt(vectorhextemp[11], 16);
                                            if (mes.length() == 1) {
                                                mes = "0" + mes;
                                            }
                                            String dia = "" + Integer.parseInt(vectorhextemp[12], 16);
                                            if (dia.length() == 1) {
                                                dia = "0" + dia;
                                            }
                                            String hora = "" + Integer.parseInt(vectorhextemp[13], 16);
                                            if (hora.length() == 1) {
                                                hora = "0" + hora;
                                            }
                                            String min = "" + Integer.parseInt(vectorhextemp[14], 16);
                                            if (min.length() == 1) {
                                                min = "0" + min;
                                            }
                                            String seg = "" + Integer.parseInt(vectorhex[15], 16);
                                            if (seg.length() == 1) {
                                                seg = "0" + seg;
                                            }
                                            String fechaactual1 = "" + año + mes + dia + hora + min + seg;
                                            try {
                                                escribir("Fecha actual Medidor " + sdf.parse(fechaactual1).toString());
                                                escribir("Fecha actual PC " + Date.from(ZonedDateTime.now(zid).toInstant()).toString());
                                            } catch (Exception e) {
                                            }
                                            byte trama[] = tramasElster.getST61ConfiPerfil();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST055factual = false;
                                            lST061 = true;
                                            tiempo = 1000;
                                            escribir("Envia ST061");
                                            try {
                                                avisoStr("Configuracion Perfil Carga");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {//nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                                    }
                                } else {////bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST055");
                                }
                            } else {//tramaincompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        }
                    } else {//complemento EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        //fecha actual
                                        String año = "" + Integer.parseInt(vectorhextemp[9], 16);
                                        String mes = "" + Integer.parseInt(vectorhextemp[10], 16);
                                        if (mes.length() == 1) {
                                            mes = "0" + mes;
                                        }
                                        String dia = "" + Integer.parseInt(vectorhextemp[11], 16);
                                        if (dia.length() == 1) {
                                            dia = "0" + dia;
                                        }
                                        String hora = "" + Integer.parseInt(vectorhextemp[12], 16);
                                        if (hora.length() == 1) {
                                            hora = "0" + hora;
                                        }
                                        String min = "" + Integer.parseInt(vectorhextemp[13], 16);
                                        if (min.length() == 1) {
                                            min = "0" + min;
                                        }
                                        String seg = "" + Integer.parseInt(vectorhextemp[14], 16);
                                        if (seg.length() == 1) {
                                            seg = "0" + seg;
                                        }
                                        String fechaactual1 = "" + año + mes + dia + hora + min + seg;
                                        try {
                                            escribir("Fecha actual Medidor " + sdf.parse(fechaactual1).toString());
                                            escribir("Fecha actual PC " + Date.from(ZonedDateTime.now(zid).toInstant()).toString());
                                        } catch (Exception e) {
                                        }
                                        byte trama[] = tramasElster.getST61ConfiPerfil();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST055factual = false;
                                        lST061 = true;
                                        tiempo = 1000;
                                        escribir("Envia ST061");
                                        try {
                                            avisoStr("Configuracion Perfil Carga");
                                        } catch (Exception e) {
                                        }
                                        seEspera06 = true;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                                    }
                                } else { //bad crc
                                    escribir("bad crc");
                                    seEspera06 = true;
                                    enviaTrama1(ultimatramaEnviada, "Solicitud ST055");
                                }
                            } else {//tramaincompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        } else {//no tiene cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        }
                    }
                } else {// es basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST055");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                }
            }
        }
    }

    private void revisarST061(String[] vectorhex) {
        if (seEspera06) {
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    numCanales = Integer.parseInt(vectorhex[21], 16);
                                    intervalos = Integer.parseInt(vectorhex[22], 16);
                                    NBR_BLK_INTS_SET1 = Integer.parseInt(vectorhex[20] + vectorhex[19], 16);
                                    escribir("Numero de canales " + numCanales);
                                    escribir("Intervalos de tiempo " + intervalos);
                                    byte trama[] = tramasElster.getST55FechaAct();
                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST061 = false;
                                    lST055factual2 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST055");
                                    try {
                                        avisoStr("Fecha y Hora");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST061");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST061");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                    }
                } else {//o cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                    }
                }
            } else {//llego algo que no es
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST061");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                }
            }
        } else { //complemento o EE
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[6].equals("00")) { //ack del data
                                    //parametros de perfil de carga numero de canales y intervalos de tiempo.
                                    numCanales = Integer.parseInt(vectorhex[20], 16);
                                    intervalos = Integer.parseInt(vectorhex[21], 16);
                                    NBR_BLK_INTS_SET1 = Integer.parseInt(vectorhex[19] + vectorhex[18], 16);
                                    escribir("Numero de canales " + numCanales);
                                    escribir("Intervalos de tiempo " + intervalos);
                                    byte trama[] = tramasElster.getST55FechaAct();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST061 = false;
                                    lST055factual2 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST055");
                                    try {
                                        avisoStr("Fecha y Hora");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST061");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST061");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                    }
                } else {//o cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                    }
                }
            } else { //es complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            numCanales = Integer.parseInt(vectorhextemp[21], 16);
                                            intervalos = Integer.parseInt(vectorhextemp[22], 16);
                                            NBR_BLK_INTS_SET1 = Integer.parseInt(vectorhextemp[20] + vectorhextemp[19], 16);
                                            escribir("Numero de canales " + numCanales);
                                            escribir("Intervalos de tiempo " + intervalos);
                                            byte trama[] = tramasElster.getST55FechaAct();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST061 = false;
                                            lST055factual2 = true;
                                            tiempo = 1000;
                                            //System.out.println("Envia ST055");
                                            escribir("Envia ST055");
                                            try {
                                                avisoStr("Fecha y Hora");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST061");
                                    }
                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST061");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                            }
                        } else {//o cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[6].equals("00")) { //ack del data
                                            //parametros de perfil de carga numero de canales y intervalos de tiempo.
                                            numCanales = Integer.parseInt(vectorhextemp[20], 16);
                                            intervalos = Integer.parseInt(vectorhextemp[21], 16);
                                            NBR_BLK_INTS_SET1 = Integer.parseInt(vectorhextemp[19] + vectorhextemp[18], 16);
                                            escribir("Numero de canales " + numCanales);
                                            escribir("Intervalos de tiempo " + intervalos);
                                            byte trama[] = tramasElster.getST55FechaAct();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST061 = false;
                                            lST055factual2 = true;
                                            tiempo = 1000;
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST061");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST061");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                            }
                        } else {//o cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                            }
                        }
                    }
                } else {
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST061");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST061");
                    }
                }
            }
        }
    }

    private void revisarST055_2(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    //fecha actual
                                    String año = "" + Integer.parseInt(vectorhex[10], 16);
                                    String mes = "" + Integer.parseInt(vectorhex[11], 16);
                                    if (mes.length() == 1) {
                                        mes = "0" + mes;
                                    }
                                    String dia = "" + Integer.parseInt(vectorhex[12], 16);
                                    if (dia.length() == 1) {
                                        dia = "0" + dia;
                                    }
                                    String hora = "" + Integer.parseInt(vectorhex[13], 16);
                                    if (hora.length() == 1) {
                                        hora = "0" + hora;
                                    }
                                    String min = "" + Integer.parseInt(vectorhex[14], 16);
                                    if (min.length() == 1) {
                                        min = "0" + min;
                                    }
                                    String seg = "" + Integer.parseInt(vectorhex[15], 16);
                                    if (seg.length() == 1) {
                                        seg = "0" + seg;
                                    }
                                    fechaactual = "" + año + mes + dia + hora + min + seg;
                                    try {
                                        escribir("Fecha actual Medidor " + sdf.parse(fechaactual).toString());
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
                                        }
                                    } catch (Exception e) {
                                    }
                                    byte trama[] = tramasElster.getST53TimeOffset();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST055factual2 = false;
                                    lST053 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST053");
                                    try {
                                        avisoStr("Zona horaria");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {//nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                            }
                        } else {////bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST055");
                        }
                    } else {//tramaincompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                }
            } else {//llego algo inesperado
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST055");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                }
            }
        } else {//se espera EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                //fecha actual
                                String año = "" + Integer.parseInt(vectorhex[9], 16);
                                String mes = "" + Integer.parseInt(vectorhex[10], 16);
                                if (mes.length() == 1) {
                                    mes = "0" + mes;
                                }
                                String dia = "" + Integer.parseInt(vectorhex[11], 16);
                                if (dia.length() == 1) {
                                    dia = "0" + dia;
                                }
                                String hora = "" + Integer.parseInt(vectorhex[12], 16);
                                if (hora.length() == 1) {
                                    hora = "0" + hora;
                                }
                                String min = "" + Integer.parseInt(vectorhex[13], 16);
                                if (min.length() == 1) {
                                    min = "0" + min;
                                }
                                String seg = "" + Integer.parseInt(vectorhex[14], 16);
                                if (seg.length() == 1) {
                                    seg = "0" + seg;
                                }
                                fechaactual = "" + año + mes + dia + hora + min + seg;
                                try {
                                    escribir("Fecha actual Medidor " + sdf.parse(fechaactual).toString());
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
                                    }
                                } catch (Exception e) {
                                }
                                byte trama[] = tramasElster.getST53TimeOffset();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lST055factual2 = false;
                                lST053 = true;
                                tiempo = 1000;
                                escribir("Envia ST053");
                                try {
                                    avisoStr("Zona horaria");
                                } catch (Exception e) {
                                }
                                seEspera06 = true;
                                ultimatramaEnviada = nuevatrama;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                            }

                        } else { //bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST055");
                        }
                    } else {//tramaincompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");

                    }
                } else {//no tiene cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            //fecha actual
                                            String año = "" + Integer.parseInt(vectorhextemp[10], 16);
                                            String mes = "" + Integer.parseInt(vectorhextemp[11], 16);
                                            if (mes.length() == 1) {
                                                mes = "0" + mes;
                                            }
                                            String dia = "" + Integer.parseInt(vectorhextemp[12], 16);
                                            if (dia.length() == 1) {
                                                dia = "0" + dia;
                                            }
                                            String hora = "" + Integer.parseInt(vectorhextemp[13], 16);
                                            if (hora.length() == 1) {
                                                hora = "0" + hora;
                                            }
                                            String min = "" + Integer.parseInt(vectorhextemp[14], 16);
                                            if (min.length() == 1) {
                                                min = "0" + min;
                                            }
                                            String seg = "" + Integer.parseInt(vectorhextemp[15], 16);
                                            if (seg.length() == 1) {
                                                seg = "0" + seg;
                                            }
                                            fechaactual = "" + año + mes + dia + hora + min + seg;
                                            try {
                                                escribir("Fecha actual Medidor " + sdf.parse(fechaactual).toString());
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
                                                }
                                            } catch (Exception e) {
                                            }
                                            byte trama[] = tramasElster.getST53TimeOffset();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST055factual2 = false;
                                            lST053 = true;
                                            tiempo = 1000;
                                            //System.out.println("Envia ST053");
                                            escribir("Envia ST053");
                                            try {
                                                avisoStr("Zona horaria");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {//nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                                    }

                                } else {////bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST055");
                                }
                            } else {//tramaincompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        }
                    } else {//complemento EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        //fecha actual
                                        String año = "" + Integer.parseInt(vectorhextemp[9], 16);
                                        String mes = "" + Integer.parseInt(vectorhextemp[10], 16);
                                        if (mes.length() == 1) {
                                            mes = "0" + mes;
                                        }
                                        String dia = "" + Integer.parseInt(vectorhextemp[11], 16);
                                        if (dia.length() == 1) {
                                            dia = "0" + dia;
                                        }
                                        String hora = "" + Integer.parseInt(vectorhextemp[12], 16);
                                        if (hora.length() == 1) {
                                            hora = "0" + hora;
                                        }
                                        String min = "" + Integer.parseInt(vectorhextemp[13], 16);
                                        if (min.length() == 1) {
                                            min = "0" + min;
                                        }
                                        String seg = "" + Integer.parseInt(vectorhextemp[14], 16);
                                        if (seg.length() == 1) {
                                            seg = "0" + seg;
                                        }
                                        fechaactual = "" + año + mes + dia + hora + min + seg;
                                        try {
                                            escribir("Fecha actual Medidor " + sdf.parse(fechaactual).toString());
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
                                            }
                                        } catch (Exception e) {
                                        }
                                        byte trama[] = tramasElster.getST53TimeOffset();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST055factual2 = false;
                                        lST053 = true;
                                        tiempo = 1000;
                                        escribir("Envia ST053");
                                        try {
                                            avisoStr("Zona horaria");
                                        } catch (Exception e) {
                                        }
                                        seEspera06 = true;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST055");
                                    }

                                } else { //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST055");
                                }
                            } else {//tramaincompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        } else {//no tiene cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                            }
                        }
                    }
                } else {// es basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST055");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST055");
                    }
                }
            }
        }
    }

    private void revisarST053(String[] vectorhex) {
        if (seEspera06) {
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    byte trama[] = tramasElster.getST62LpCtrl();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST053 = false;
                                    lST062 = true;
                                    tiempo = 3000;
                                    escribir("Envia ST062");
                                    try {
                                        avisoStr("Indices de tablas");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST053");
                            }

                        } else {
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST053");
                        }
                    } else {// incompleta
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                    }
                } else {
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                    }
                }
            } else {//llego algo dif. de 06d
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST053");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                }

            }
        } else {//EE - complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {//EE
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                byte trama[] = tramasElster.getST62LpCtrl();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lST053 = false;
                                lST062 = true;
                                tiempo = 3000;
                                escribir("Envia ST062");
                                try {
                                    avisoStr("Indices de tablas");
                                } catch (Exception e) {
                                }
                                intentoescuchar = 0;
                                seEspera06 = true;
                                ultimatramaEnviada = nuevatrama;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST053");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST053");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                    }
                } else {//no cebecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            byte trama[] = tramasElster.getST62LpCtrl();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST053 = false;
                                            lST062 = true;
                                            tiempo = 3000;
                                            escribir("Envia ST062");
                                            try {
                                                avisoStr("Indices de tablas");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST053");
                                    }

                                } else {
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST053");
                                }
                            } else {// incompleta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                            }
                        } else {
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                            }
                        }
                    } else {//complemento EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        byte trama[] = tramasElster.getST62LpCtrl();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST053 = false;
                                        lST062 = true;
                                        tiempo = 3000;
                                        //System.out.println("Envia ST062");
                                        escribir("Envia ST062");
                                        try {
                                            avisoStr("Indices de tablas");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        seEspera06 = true;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST053");
                                    }
                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST053");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                            }
                        } else {//no cebecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                            }
                        }
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST053");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST053");
                    }
                }
            }
        }
    }

    private void revisarST062(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {// ack
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    vSt062 = new String[(Integer.parseInt(vectorhex[5] + vectorhex[6], 16)) + 9];
                                    System.arraycopy(vectorhex, 0, vSt062, 0, (Integer.parseInt(vectorhex[5] + vectorhex[6], 16)) + 9);
                                    if (lregistros) {//permite registros pide la ST00
                                        byte trama[] = tramasElster.getST00();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST062 = false;
                                        lST00 = true;
                                        seEspera06 = true;
                                        tiempo = 1000;
                                        escribir("Envia ST00");
                                        try {
                                            avisoStr("Configuracion de Registros");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {//no requiere registros pide la MT017
                                        byte trama[] = tramasElster.getMT17();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST062 = false;
                                        lMT017 = true;
                                        tamañoacumuladolMT017 = 0;
                                        contadorMT017 = 14;
                                        primerbloque = true;

                                        vMT017 = new Vector<String[]>();
                                        seEspera06 = true;
                                        tiempo = 1000;

                                        escribir("Envia MT017");
                                        try {
                                            avisoStr("Configuracion de canales");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    }

                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST062");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST062");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                    }
                }
            } else {//llego inesperado
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST062");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                }
            }
        } else {//EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {//llego un EE
                if (vectorhex.length > 6) {//tiene cabecera
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validacion crc
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                vSt062 = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                System.arraycopy(vectorhex, 0, vSt062, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                if (lregistros) {//permite registros pide la ST00
                                    byte trama[] = tramasElster.getST00();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST062 = false;
                                    lST00 = true;
                                    seEspera06 = true;
                                    tiempo = 1000;

                                    escribir("Envia ST00");
                                    try {
                                        avisoStr("Configuracion de Registros");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {//no requiere registros pide la MT017
                                    byte trama[] = tramasElster.getMT17();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST062 = false;
                                    lMT017 = true;
                                    tamañoacumuladolMT017 = 0;
                                    contadorMT017 = 14;
                                    primerbloque = true;
                                    vMT017 = new Vector<String[]>();
                                    seEspera06 = true;
                                    tiempo = 1000;

                                    escribir("Envia MT017");
                                    try {
                                        avisoStr("Configuracion de canales");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST062");
                            }
                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST062");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                    }
                }
            } else { //complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            vSt062 = new String[(Integer.parseInt(vectorhextemp[6], 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, vSt062, 0, Integer.parseInt(vectorhextemp[6], 16) + 9);

                                            if (lregistros) {//permite registros pide la ST00
                                                byte trama[] = tramasElster.getST00();

                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST062 = false;
                                                lST00 = true;
                                                seEspera06 = true;
                                                tiempo = 1000;

                                                escribir("Envia ST00");
                                                try {
                                                    avisoStr("Configuracion de Registros");
                                                } catch (Exception e) {
                                                }
                                                intentoescuchar = 0;
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            } else {//no requiere registros pide la MT017
                                                byte trama[] = tramasElster.getMT17();

                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST062 = false;
                                                lMT017 = true;
                                                tamañoacumuladolMT017 = 0;
                                                contadorMT017 = 14;
                                                primerbloque = true;
                                                vMT017 = new Vector<String[]>();
                                                seEspera06 = true;
                                                tiempo = 1000;
                                                escribir("Envia MT017");
                                                try {
                                                    avisoStr("Configuracion de canales");
                                                } catch (Exception e) {
                                                }
                                                intentoescuchar = 0;
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            }
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST062");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST062");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                            }
                        }
                    } else {//complemento EE
                        if (vectorhextemp.length > 6) {//tiene cabecera
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validacion crc
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        vSt062 = new String[(Integer.parseInt(vectorhextemp[6], 16)) + 8];
                                        System.arraycopy(vectorhextemp, 0, vSt062, 0, Integer.parseInt(vectorhextemp[6], 16) + 8);
                                        if (lregistros) {//permite registros pide la ST00
                                            byte trama[] = tramasElster.getST00();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST062 = false;
                                            lST00 = true;
                                            seEspera06 = true;
                                            tiempo = 1000;
                                            escribir("Envia ST00");
                                            try {
                                                avisoStr("Configuracion de Registros");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {//no requiere registros pide la MT017
                                            byte trama[] = tramasElster.getMT17();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST062 = false;
                                            lMT017 = true;
                                            tamañoacumuladolMT017 = 0;
                                            contadorMT017 = 14;
                                            primerbloque = true;
                                            vMT017 = new Vector<String[]>();
                                            seEspera06 = true;
                                            tiempo = 1000;

                                            escribir("Envia MT017");
                                            try {
                                                avisoStr("Configuracion de canales");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST062");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST062");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                            }
                        }
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST062");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST062");
                    }
                }
            }
        }
    }

    private void revisarST00(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego un 06
                if (vectorhex.length > 7) {//tiene cabecera
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validacion crc
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    if (vectorhex[4].equals("00")) {
                                        byte trama[] = tramasElster.getMT17();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST00 = false;
                                        lMT017 = true;
                                        tamañoacumuladolMT017 = 0;
                                        contadorMT017 = 14;
                                        primerbloque = true;
                                        vMT017 = new Vector<String[]>();
                                        seEspera06 = true;
                                        tiempo = 1000;
                                        escribir("Envia MT017");
                                        try {
                                            avisoStr("Configuracion de canales");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        //aqui se captura el valor de formatcontrol
                                        FORMAT_CONTROL_1 = Integer.parseInt(vectorhex[10], 16);
                                        FORMAT_CONTROL_2 = Integer.parseInt(vectorhex[11], 16);
                                        getSpoling(bitSpoling);
                                        enviaTramack("");
                                    }
                                } else {//nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud de ST00");
                            }

                        } else {//Bad crc

                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud de ST00");
                        }
                    } else {// llego incompleta la trama se guarda el tramo y se espera a la segunda parte
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        siguienteTrama = false;
                        //System.out.println("Incompleta 06");
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de ST00");
                    }
                } else {// reviamos si el tamaño es 1 y es 06
                    if (vectorhex.length == 1) {//llego el 06 solo
                        reiniciaComuniacion();
                    } else {//llego mocha
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                    }
                }
            } else { //llego algo diferente cuando se esperaba un 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST00");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                }
            }
        } else {//llega una trama EE o un complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) { //es la siguiente trama de un bloque
                if (vectorhex.length > 6) {//tiene cabecera
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[3].equals("00")) {
                                    byte trama[] = tramasElster.getMT17();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST00 = false;
                                    lMT017 = true;
                                    tamañoacumuladolMT017 = 0;
                                    contadorMT017 = 14;
                                    primerbloque = true;
                                    vMT017 = new Vector<String[]>();
                                    seEspera06 = true;
                                    tiempo = 1000;

                                    escribir("Envia MT017");
                                    try {
                                        avisoStr("Configuracion de canales");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    FORMAT_CONTROL_1 = Integer.parseInt(vectorhex[9], 16);
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST00");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST00");
                        }
                    } else {// llego incompleta se guarda la trama para juntarla con otra
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de ST00");
                    }
                } else {
                    if (vectorhex.length == 1) {//llego el 06 solo
                        reiniciaComuniacion();
                    } else {//llego mocha
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                    }
                }
            } else {//es un complemento de una trama mocha
                if (complemento) {// es un complemento de 06 0 de EE
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("EE")) {//es un complemento de EE
                        if (vectorhextemp.length > 6) {//tiene cabecera
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[3].equals("00")) {
                                            byte trama[] = tramasElster.getMT17();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST00 = false;
                                            lMT017 = true;
                                            tamañoacumuladolMT017 = 0;
                                            contadorMT017 = 14;
                                            primerbloque = true;
                                            vMT017 = new Vector<String[]>();
                                            seEspera06 = true;
                                            tiempo = 1000;

                                            escribir("Envia MT017");
                                            try {
                                                avisoStr("Configuracion de canales");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            FORMAT_CONTROL_1 = Integer.parseInt(vectorhextemp[9], 16);
                                            FORMAT_CONTROL_2 = Integer.parseInt(vectorhextemp[10], 16);
                                            getSpoling(bitSpoling);
                                            enviaTramack("");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud de ST00");
                                    }
                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud de ST00");
                                }
                            } else { //trama se corrompe
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de ST00");
                            }
                        } else {
                            if (vectorhex.length == 1) {//llego el 06 solo
                                reiniciaComuniacion();
                            } else {//llego mocha
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                            }
                        }
                    } else { //es complemento 06
                        if (vectorhextemp.length > 7) {//tiene cabecera
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[4].equals("00")) {
                                            byte trama[] = tramasElster.getMT17();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST00 = false;
                                            lMT017 = true;
                                            tamañoacumuladolMT017 = 0;
                                            contadorMT017 = 14;
                                            primerbloque = true;
                                            vMT017 = new Vector<String[]>();
                                            seEspera06 = true;
                                            tiempo = 1000;

                                            escribir("Envia MT017");
                                            try {
                                                avisoStr("Configuracion de canales");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            FORMAT_CONTROL_1 = Integer.parseInt(vectorhextemp[10], 16);
                                            FORMAT_CONTROL_2 = Integer.parseInt(vectorhextemp[11], 16);
                                            getSpoling(bitSpoling);
                                            enviaTramack("");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST00");
                                    }

                                } else {
                                    //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST00");
                                }
                            } else {
                                //llega incompleta se corrompe
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                            }
                        } else { //no llego trama
                            if (vectorhex.length == 1) {//llego el 06 solo
                                reiniciaComuniacion();
                            } else {//llego mocha
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                            }
                        }
                    }
                } else { // es trama basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST00");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST00");
                    }
                }
            }
        }
    }

    private void revisarMT017(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego un 06
                if (vectorhex.length > 7) {//tiene cabecera
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validacion crc
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                contador15 = 0;
                                if (primerbloque) {
                                    contadorMT017 = Integer.parseInt(vectorhex[4], 16);
                                }
                                if (Integer.parseInt(vectorhex[4], 16) == contadorMT017) {//verificamos si el numero de secuencia es el esperado
                                    actualUltimatrama = 0;
                                    if (vectorhex[7].equals("00")) { //ack del data                                        
                                        if (primerbloque) {
                                            primerbloque = false;
                                            //guardamos el tamaño total de la tabla MT017
                                            tamañototalMT017 = Integer.parseInt((vectorhex[7] + vectorhex[8] + vectorhex[9]), 16);
                                            tamañoacumuladolMT017 += Integer.parseInt((vectorhex[5] + vectorhex[6]), 16);
                                        } else {
                                            tamañoacumuladolMT017 += Integer.parseInt((vectorhex[5] + vectorhex[6]), 16);
                                        }
                                        String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                        System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                        vMT017.add(tramatemp);
                                        if (!vectorhex[4].equals("00")) {
                                            siguienteTrama = true;
                                            getSpoling(bitSpoling);
                                            contadorMT017--;
                                            enviaTramack("");
                                        } else {
                                            if (tamañototalMT017 == tamañoacumuladolMT017 - 4) {
                                                byte trama[] = tramasElster.getST15Constantes();
                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lMT017 = false;
                                                lST015 = true;
                                                seEspera06 = true;
                                                tiempo = 600;
                                                vST015 = new Vector<String[]>();
                                                escribir("Envia ST015");
                                                try {
                                                    avisoStr("Configuracion Constantes");
                                                } catch (Exception e) {
                                                }
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                //se envia sin 06 no se guardo bien la trama
                                                vMT017 = new Vector<String[]>();
                                                tamañoacumuladolMT017 = 0;
                                                tamañototalMT017 = 0;
                                                primerbloque = true;
                                                seEspera06 = true;
                                                enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                                            }
                                        }
                                    } else {//nack
                                        //llego lo que no se esperaba
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                                    }
                                } else {
                                    //debemos revisar si se salto una trama o repitio la trama
                                    if (Integer.parseInt(vectorhex[4], 16) < contadorMT017) {// se salto una trama
                                        reiniciaComuniacion();
                                    } else { // la trama es repetida
                                        enviaTramack("");
                                    }
                                }
                            } else {
                                if (primerbloque) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                                } else {
                                    if (Integer.parseInt(vectorhex[4], 16) > contadorMT017) { //es por que llego repetida se envia ack para que envie la siguiente trama
                                        contador15++;
                                        if (contador15 > 3) {
                                            contador15 = 0;
                                            enviaTramack("");
                                        } else {
                                            enviaTrama1(badcrc, "");
                                        }
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                }
                            }
                        } else {//Bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud de MT017");
                        }
                    } else {// llego incompleta la trama se guarda el tramo y se espera a la segunda parte
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        siguienteTrama = false;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de MT017");
                    }
                } else {// reviamos si el tamaño es 1 y es 06
//                    if (vectorhex.length == 1) {//llego el 06 solo
//                        reiniciaComuniacion();
//                    } else {//llego mocha
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    siguienteTrama = false;
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
//                    }
                }
            } else { //llego algo diferente cuando se esperaba un 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");

                }
            }
        } else {//llega una trama EE o un complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) { //es la siguiente trama de un bloque
                if (vectorhex.length > 6) {//tiene cabecera
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                contador15 = 0;
                                if (primerbloque) {
                                    contadorMT017 = Integer.parseInt(vectorhex[3], 16);
                                }
                                if (Integer.parseInt(vectorhex[3], 16) == contadorMT017) {//verificamos si el numero de secuencia es el esperado
                                    actualUltimatrama = 0;
                                    if (primerbloque) {
                                        primerbloque = false;
                                        //guardamos el tamaño total de la tabla MT017
                                        tamañototalMT017 = Integer.parseInt((vectorhex[6] + vectorhex[7] + vectorhex[8]), 16);
                                        tamañoacumuladolMT017 += Integer.parseInt((vectorhex[4] + vectorhex[5]), 16);
                                    } else {
                                        tamañoacumuladolMT017 += Integer.parseInt((vectorhex[4] + vectorhex[5]), 16);
                                    }
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                    vMT017.add(tramatemp);
                                    if (!vectorhex[3].equals("00")) { //Validamos si es la ultima trama
                                        siguienteTrama = true;
                                        getSpoling(bitSpoling);
                                        contadorMT017--;
                                        enviaTramack("");
                                    } else {//es la ultima trama  se envia la proxima peticion
                                        if (tamañototalMT017 == tamañoacumuladolMT017 - 4) {
                                            byte trama[] = tramasElster.getST15Constantes();
                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lMT017 = false;
                                            lST015 = true;
                                            tiempo = 600;
                                            escribir("Envia ST015");
                                            vST015 = new Vector<String[]>();
                                            try {
                                                avisoStr("Configuracion Constantes");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            vMT017 = new Vector<String[]>();
                                            tamañoacumuladolMT017 = 0;
                                            tamañototalMT017 = 0;
                                            primerbloque = true;
                                            seEspera06 = true;
                                            enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                                        }
                                    }
                                } else {
                                    //debemos revisar si se salto una trama o repitio la trama
                                    if (Integer.parseInt(vectorhex[3], 16) < contadorMT017) {// se salto una trama
                                        reiniciaComuniacion();
                                    } else { // la trama es repetida
                                        enviaTramack("");
                                    }
                                }

                            } else {
                                if (primerbloque) {
                                    enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                                } else {
                                    if (Integer.parseInt(vectorhex[3], 16) > contadorMT017) {//trama repetida
                                        contador15++;
                                        if (contador15 > 3) {
                                            contador15 = 0;
                                            enviaTramack("");
                                        } else {
                                            enviaTrama1(badcrc, "");
                                        }
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                }
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud MT017");
                        }
                    } else {// llego incompleta se guarda la trama para juntarla con otra
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de MT017");
                    }
                } else {
                    if (vectorhex.length == 1) {//llego el 06 solo
                        if (vectorhex[0].equals("15")) {
                            seEspera06 = true;
                            enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                        } else {
                            reiniciaComuniacion();
                        }
                    } else {//llego mocha
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                    }
                }
            } else { //es un complemento de una trama mocha
                if (complemento) {// es un complemento de 06 0 de EE
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("EE")) {//es un complemento de EE
                        if (vectorhextemp.length > 6) {//tiene cabecera
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        contador15 = 0;
                                        if (primerbloque) {
                                            contadorMT017 = Integer.parseInt(vectorhextemp[3], 16);
                                        }
                                        if (Integer.parseInt(vectorhextemp[3], 16) == contadorMT017) {//verificamos si el numero de secuencia es el esperado
                                            actualUltimatrama = 0;
                                            if (primerbloque) {
                                                primerbloque = false;
                                                //guardamos el tamaño total de la tabla MT017
                                                tamañototalMT017 = Integer.parseInt((vectorhextemp[6] + vectorhextemp[7] + vectorhextemp[8]), 16);
                                                tamañoacumuladolMT017 += Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16);
                                            } else {
                                                tamañoacumuladolMT017 += Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16);
                                            }
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                            vMT017.add(tramatemp);
                                            if (vectorhextemp[3].equals("00")) {
                                                if (tamañototalMT017 == tamañoacumuladolMT017 - 4) {
                                                    byte trama[] = tramasElster.getST15Constantes();

                                                    trama[2] = getSpoling(bitSpoling);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    lMT017 = false;
                                                    lST015 = true;
                                                    seEspera06 = true;
                                                    vST015 = new Vector<String[]>();
                                                    tiempo = 600;
                                                    siguienteTrama = false;
                                                    escribir("Envia ST015");
                                                    try {
                                                        avisoStr("Configuracion Constantes");
                                                    } catch (Exception e) {
                                                    }
                                                    ultimatramaEnviada = nuevatrama;
                                                    enviaTrama2(nuevatrama, "");
                                                } else {
                                                    vMT017 = new Vector<String[]>();
                                                    tamañoacumuladolMT017 = 0;
                                                    tamañototalMT017 = 0;
                                                    primerbloque = true;
                                                    enviaTrama2(ultimatramaEnviada, "Solicitud de MT017");
                                                }

                                            } else {
                                                siguienteTrama = true;
                                                getSpoling(bitSpoling);
                                                contadorMT017--;
                                                enviaTramack("");
                                            }

                                        } else {
                                            if (Integer.parseInt(vectorhextemp[3], 16) < contadorMT017) {// se salto una trama
                                                reiniciaComuniacion();
                                            } else { // la trama es repetida
                                                enviaTramack("");
                                            }
                                        }

                                    } else {
                                        if (primerbloque) {
                                            enviaTrama1(ultimatramaEnviada, "Solicitud de MT017");
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[3], 16) < contadorMT017) {// se salto una trama
                                                reiniciaComuniacion();
                                            } else { // la trama es repetida
                                                contador15++;
                                                if (contador15 > 3) {
                                                    contador15 = 0;
                                                    enviaTramack("");
                                                } else {
                                                    enviaTrama1(badcrc, "");
                                                }
                                            }
                                        }
                                    }
                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud de MT017");
                                }
                            } else { //trama se corrompe
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de MT017");
                            }
                        } else {
                            if (vectorhex.length == 1) {//llego el 06 solo
                                reiniciaComuniacion();
                            } else {//llego mocha
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                            }
                        }
                    } else { //es un complemento de 06
                        if (vectorhextemp.length > 7) {//tiene cabecera
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) {//validamos el tamaño es mayor o = al campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        contador15 = 0;
                                        if (primerbloque) {
                                            contadorMT017 = Integer.parseInt(vectorhextemp[4], 16);
                                        }
                                        if (Integer.parseInt(vectorhextemp[4], 16) == contadorMT017) {//verificamos si el numero de secuencia es el esperado
                                            //validamos ack
                                            if (vectorhextemp[7].equals("00")) {
                                                actualUltimatrama = 0;
                                                if (primerbloque) {
                                                    primerbloque = false;
                                                    //guardamos el tamaño total de la tabla MT017
                                                    tamañototalMT017 = Integer.parseInt((vectorhextemp[7] + vectorhextemp[8] + vectorhextemp[9]), 16);
                                                    tamañoacumuladolMT017 += Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16);
                                                } else {
                                                    tamañoacumuladolMT017 += Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16);
                                                }
                                                String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                                System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                                vMT017.add(tramatemp);
                                                if (vectorhextemp[4].equals("00")) {
                                                    if (tamañototalMT017 == tamañoacumuladolMT017 - 4) {
                                                        byte trama[] = tramasElster.getST15Constantes();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        lMT017 = false;
                                                        lST015 = true;
                                                        siguienteTrama = false;
                                                        seEspera06 = true;
                                                        tiempo = 600;
                                                        vST015 = new Vector<String[]>();
                                                        escribir("Envia ST015");
                                                        try {
                                                            avisoStr("Configuracion Constantes");
                                                        } catch (Exception e) {
                                                        }
                                                        ultimatramaEnviada = nuevatrama;
                                                        enviaTrama2(nuevatrama, "");
                                                    } else {
                                                        vMT017 = new Vector<String[]>();
                                                        tamañoacumuladolMT017 = 0;
                                                        tamañototalMT017 = 0;
                                                        primerbloque = true;
                                                        seEspera06 = true;
                                                        enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                                                    }

                                                } else {
                                                    siguienteTrama = true;
                                                    getSpoling(bitSpoling);
                                                    contadorMT017--;
                                                    enviaTramack("");
                                                }
                                            } else {
                                                //nack reiniciamos
                                                reiniciaComuniacion();
                                            }
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[4], 16) < contadorMT017) {// se salto una trama
                                                reiniciaComuniacion();
                                            } else { // la trama es repetida
                                                enviaTramack("");
                                            }
                                        }

                                    } else {
                                        if (primerbloque) {
                                            enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[4], 16) < contadorMT017) {// se salto una trama
                                                reiniciaComuniacion();
                                            } else { // la trama es repetida
                                                contador15++;
                                                if (contador15 > 3) {
                                                    contador15 = 0;
                                                    enviaTramack("");
                                                } else {
                                                    enviaTrama1(badcrc, "");
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    //bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud MT017");
                                }
                            } else {
                                //llega incompleta se corrompe
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                            }
                        } else { //no llego trama
                            if (vectorhextemp.length == 1) {//llego el 06 solo
                                if (vectorhextemp[0].equals("15")) {
                                    seEspera06 = true;
                                    enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {//llego mocha
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                            }
                        }
                    }
                } else { // es trama basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud MT017");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT017");
                    }

                }
            }
        }
    }

    private void revisarST015(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    actualUltimatrama = 0;
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                    vST015.add(tramatemp);
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST015");
                            }
                        } else { //badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST015");
                        }
                    } else { //trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                    }
                } else { //no tiene cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                    }
                }
            } else {//llego lo que no se esperaba
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST015");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                }

            }
        } else {//es EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {//es un EE
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                actualUltimatrama = 0;
                                if (vectorhex[3].equals("00")) {
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                    vST015.add(tramatemp);
                                    byte trama[] = tramasElster.getMT015PrimMeteringInfo();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST015 = false;
                                    lMT015 = true;
                                    tiempo = 1000;
                                    escribir("Envia MT015");
                                    try {
                                        avisoStr("Configuracion Multiplos");
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    if (vectorhex[3].equals("01")) {
                                        String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                        System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                        vST015.add(tramatemp);
                                    }
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST015");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST015");
                        }
                    } else {//tramaincompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                    }
                } else {//no tiene cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                    }
                }
            } else { //complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        actualUltimatrama = 0;
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                            vST015.add(tramatemp);
                                            getSpoling(bitSpoling);
                                            enviaTramack("");
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST015");
                                    }

                                } else { //badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST015");
                                }
                            } else { //trama incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                            }
                        } else { //no tiene cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                            }
                        }
                    } else {
                        if (vectorhextemp[0].equals("EE")) {//complemento EE
                            if (vectorhextemp.length > 6) {//pose cabecera?
                                if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                    if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                        if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                            actualUltimatrama = 0;
                                            if (vectorhextemp[3].equals("00")) {
                                                String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                                System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                                vST015.add(tramatemp);
                                                byte trama[] = tramasElster.getMT015PrimMeteringInfo();
                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST015 = false;
                                                lMT015 = true;
                                                tiempo = 1000;
                                                escribir("Envia MT015");
                                                try {
                                                    avisoStr("Configuracion Multiplos");
                                                } catch (Exception e) {
                                                }
                                                ultimatramaEnviada = nuevatrama;
                                                seEspera06 = true;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                if (vectorhextemp[3].equals("01")) {
                                                    tiempo = 3000;
                                                    String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                                    System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                                    vST015.add(tramatemp);
                                                }
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        } else {

                                            escribir("bad bit spoling");
                                            enviaTrama2(ultimatramaEnviada, "Solicitud ST015");
                                        }

                                    } else {//bad crc
                                        escribir("bad crc");
                                        enviaTrama1(badcrc, "Solicitud ST015");
                                    }
                                } else {//tramaincompleta
                                    tramaIncompleta = cadenahex;
                                    complemento = true;
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                                }
                            } else {//no tiene cabecera
                                if (vectorhextemp.length > 0) {
                                    if (vectorhextemp.length == 1) {
                                        reiniciaComuniacion();
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                                    }
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                                }
                            }
                        } else {//basura
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                        }
                    }
                } else {
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST015");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST015");
                    }
                }
            }
        }
    }

    private void revisarMT015(String[] vectorhex) {
        if (seEspera06) {
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego 06
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    Ext_Mult_Scale_Factor = vectorhex[10];
                                    External_Multiplier = vectorhex[14] + vectorhex[13] + vectorhex[12] + vectorhex[11];
                                    byte trama[] = tramasElster.getMT16MeteringInfo();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lMT015 = false;
                                    lMT016 = true;
                                    tiempo = 1000;
                                    escribir("Envia MT016");
                                    try {
                                        avisoStr("Configuracion Escalares");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else { //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud MT015");
                            }
                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud MT015");
                        }
                    } else {// trama incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                    }
                } else {
                    //no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                    }
                }
            } else {//llego algo inesperado
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud MT015");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                }
            }
        } else { //es EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                boolean procede = false;
                                if (vectorhex.length - 8 != 1) {
                                    procede = true;
                                }
                                if (procede) {
                                    Ext_Mult_Scale_Factor = vectorhex[9];
                                    External_Multiplier = vectorhex[13] + vectorhex[12] + vectorhex[11] + vectorhex[10];
                                    byte trama[] = tramasElster.getMT16MeteringInfo();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lMT015 = false;
                                    lMT016 = true;
                                    tiempo = 1000;
                                    escribir("Envia MT016");
                                    try {
                                        avisoStr("Configuracion Escalares");
                                    } catch (Exception e) {
                                    }
                                    seEspera06 = true;
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud MT015");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud MT015");
                        }
                    } else {
                        //trama incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                    }
                    //enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//es complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        actualUltimatrama = 0;
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            Ext_Mult_Scale_Factor = vectorhextemp[10];
                                            External_Multiplier = vectorhextemp[14] + vectorhextemp[13] + vectorhextemp[12] + vectorhextemp[11];
                                            byte trama[] = tramasElster.getMT16MeteringInfo();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lMT015 = false;
                                            lMT016 = true;
                                            tiempo = 1000;
                                            escribir("Envia MT016");
                                            try {
                                                avisoStr("Configuracion Escalares");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else { //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud MT015");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud MT015");
                                }
                            } else {// trama incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                            }
                        } else {
                            //no cabecera
                            if (vectorhextemp.length > 0) {
//                                        if (vectorhextemp.length == 1) {
//                                            reiniciaComuniacion();
//                                        } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
//                                        }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                            }
                        }
                    } else {//es complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        boolean procede = false;
                                        if (vectorhextemp.length - 8 != 1) {
                                            procede = true;
                                        }
                                        if (procede) {
                                            Ext_Mult_Scale_Factor = vectorhextemp[9];
                                            External_Multiplier = vectorhextemp[13] + vectorhextemp[12] + vectorhextemp[11] + vectorhextemp[10];
                                            byte trama[] = tramasElster.getMT16MeteringInfo();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lMT015 = false;
                                            lMT016 = true;
                                            tiempo = 1000;
                                            try {
                                                avisoStr("Configuracion Escalares");
                                            } catch (Exception e) {
                                            }
                                            seEspera06 = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud MT015");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud MT015");
                                }
                            } else {
                                //trama incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");

                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                            }
                        }
                    }
                } else {
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud MT015");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT015");
                    }
                }
            }
        }
    }

    private void revisarMT016(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    Instrumentation_Scale_Factor = vectorhex[10];
                                    byte trama[] = tramasElster.getST63LpStatus();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lMT016 = false;
                                    lST063 = true;
                                    tiempo = 1000;
                                    //System.out.println("Envia ST063");
                                    escribir("Envia ST063");
                                    try {
                                        avisoStr("Estado medidor");
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud MT016");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud MT016");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
//                                if (vectorhex.length == 1) {
//                                    reiniciaComuniacion();
//                                } else {
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
//                                }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                    }
                }
            } else {//llego inseperado
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud MT016");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                }
            }
        } else {//se espera EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                Instrumentation_Scale_Factor = vectorhex[9];
                                byte trama[] = tramasElster.getST63LpStatus();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lMT016 = false;
                                lST063 = true;
                                tiempo = 1000;
                                escribir("Envia ST063");
                                try {
                                    avisoStr("Estado medidor");
                                } catch (Exception e) {
                                }
                                ultimatramaEnviada = nuevatrama;
                                seEspera06 = true;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud MT016");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud MT016");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                    }
                }
            } else {//es complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            Instrumentation_Scale_Factor = vectorhextemp[10];
                                            byte trama[] = tramasElster.getST63LpStatus();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lMT016 = false;
                                            lST063 = true;
                                            tiempo = 1000;
                                            escribir("Envia ST063");
                                            try {
                                                avisoStr("Estado medidor");
                                            } catch (Exception e) {
                                            }
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud MT016");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud MT016");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                            }
                        }
                    } else {//complemento EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        Instrumentation_Scale_Factor = vectorhextemp[9];
                                        byte trama[] = tramasElster.getST63LpStatus();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lMT016 = false;
                                        lST063 = true;
                                        tiempo = 1000;
                                        escribir("Envia ST063");
                                        try {
                                            avisoStr("Estado medidor");
                                        } catch (Exception e) {
                                        }
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud MT016");
                                    }
                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud MT016");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                complemento = true;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
//                                        if (vectorhextemp.length == 1) {
//                                            reiniciaComuniacion();
//                                        } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
//                                        }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                            }
                        }
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud MT016");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud MT016");
                    }
                }
            }
        }
    }

    private void revisarST063(String[] vectorhex) {
        if (seEspera06) {//se espera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (leventos) {
                                    byte trama[] = tramasElster.getST76Eventos();
                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST063 = false;
                                    lST076 = true;
                                    tiempo = 1000;
                                    EventData = new Vector<String[]>();
                                    escribir("Envia ST076");
                                    try {
                                        avisoStr("Solicitud de eventos");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else if (lregistros) {
                                    byte trama[] = tramasElster.getST021();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST063 = false;
                                    lST021 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST021");
                                    try {
                                        avisoStr("Solicitud de eventos");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else if (lperfil) {
                                    //validamos si llego un nack
                                    boolean procede = false;
                                    if ((vectorhex.length - 8) != 1) {
                                        procede = true;
                                    }

                                    if (procede) { //viene data
                                        try {

                                            ElementCount = obtenerElementCount();
                                            offset = obtenerOffset();
                                            //offset = ndias * (234 + ((numCanales - 1) * 288));
                                            //ElementCount = (39 + ((numCanales - 1) * 48));
                                            if (numCanales == 2) {
                                            }
                                            String codigo = Integer.toHexString(offset).toUpperCase();
                                            escribir("Offset" + codigo);
                                            while (codigo.length() < 6) {
                                                codigo = "0" + codigo;
                                            }
                                            String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                            escribir("Element count " + ecount);
                                            while (ecount.length() < 4) {
                                                ecount = "0" + ecount;
                                            }
                                            byte trama[] = tramasElster.getMT64Perfil();

                                            trama[2] = getSpoling(bitSpoling);
                                            //off set
                                            trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                            trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                            trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                            //element count
                                            trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                            trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            try {
                                                avisoStr("Solicitud Perfil Carga");
                                            } catch (Exception e) {
                                            }
                                            lST063 = false;
                                            lST064Perfil = true;

                                            profileData = new Vector<String[]>();
//                                            if (numCanales > 2) {
//                                                contadorST064 = 1;
//                                            } else {
//                                                contadorST064 = 0;
//                                            }
                                            tiempo = 1000;
                                            intentoescuchar = 0;
                                            seEspera06 = true;
                                            primerbloque = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                } else {
                                    //cerrar
                                    byte trama[] = tramasElster.getLogoff();

                                    trama[2] = getSpoling(bitSpoling);
                                    lST063 = false;
                                    tiempo = 1000;
                                    llogoff = true;
                                    try {
                                        avisoStr("Cerrando comunicacion");
                                    } catch (Exception e) {
                                    }
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST063");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST063");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                    }
                }
            } else {//llego algo no esperado
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST063");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                }
            }
        } else {//EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (leventos) {
                                    byte trama[] = tramasElster.getST76Eventos();
                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST063 = false;
                                    lST076 = true;
                                    tiempo = 1000;
                                    EventData = new Vector<String[]>();
                                    escribir("Envia ST076");
                                    try {
                                        avisoStr("Solicitud de eventos");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else if (lregistros) {
                                    byte trama[] = tramasElster.getST021();
                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST063 = false;
                                    lST021 = true;
                                    tiempo = 1000;
                                    escribir("Envia ST021");
                                    try {
                                        avisoStr("Solicitud de eventos");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else if (lperfil) {
                                    boolean procede = false;
                                    if ((vectorhex.length - 8) != 1) {
                                        procede = true;
                                    }

                                    if (procede) { //viene data
                                        try {
                                            ElementCount = obtenerElementCount();
                                            offset = obtenerOffset();
//                                                offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                ElementCount = (39 + ((numCanales - 1) * 48));

                                            String codigo = Integer.toHexString(offset).toUpperCase();

                                            escribir("Offset" + codigo);
                                            while (codigo.length() < 6) {
                                                codigo = "0" + codigo;
                                            }
                                            String ecount = Integer.toHexString(ElementCount).toUpperCase();

                                            escribir("Element count " + ecount);
                                            while (ecount.length() < 4) {
                                                ecount = "0" + ecount;
                                            }
                                            byte trama[] = tramasElster.getMT64Perfil();

                                            trama[2] = getSpoling(bitSpoling);
                                            //off set
                                            trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                            trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                            trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                            //element count
                                            trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                            trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            try {
                                                avisoStr("Solicitud Perfil Carga");
                                            } catch (Exception e) {
                                            }
                                            lST063 = false;
                                            lST064Perfil = true;

                                            if (numCanales > 2) {
                                                contadorST064 = 1;
                                            } else {
                                                contadorST064 = 0;
                                            }

                                            profileData = new Vector<String[]>();
                                            tiempo = 1000;
                                            intentoescuchar = 0;
                                            seEspera06 = true;
                                            primerbloque = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                } else {
                                    //cerrar
                                    byte trama[] = tramasElster.getLogoff();

                                    trama[2] = getSpoling(bitSpoling);
                                    lST063 = false;
                                    tiempo = 1000;
                                    llogoff = true;
                                    try {
                                        avisoStr("Cerrando comunicacion");
                                    } catch (Exception e) {
                                    }
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    ultimatramaEnviada = nuevatrama;
                                    enviaTrama2(nuevatrama, "");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST063");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST063");
                        }
                    } else {//incompleta
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = cadenahex;
                            complemento = true;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//complemento06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (leventos) {
                                            byte trama[] = tramasElster.getST76Eventos();

                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST063 = false;
                                            lST076 = true;
                                            tiempo = 1000;
                                            EventData = new Vector<String[]>();
                                            escribir("Envia ST076");
                                            try {
                                                avisoStr("Solicitud de eventos");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else if (lregistros) {
                                            byte trama[] = tramasElster.getST021();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST063 = false;
                                            lST021 = true;
                                            tiempo = 1000;
                                            escribir("Envia ST021");
                                            try {
                                                avisoStr("Solicitud de eventos");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else if (lperfil) {
                                            boolean procede = false;
                                            if ((vectorhex.length - 8) != 1) {
                                                procede = true;
                                            }
                                            if (procede) { //viene data
                                                try {
                                                    ElementCount = obtenerElementCount();
                                                    offset = obtenerOffset();
//                                                        offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                        ElementCount = (39 + ((numCanales - 1) * 48));
//                                                        if (numCanales == 2) {
//                                                        }
                                                    String codigo = Integer.toHexString(offset).toUpperCase();
                                                    escribir("Offset" + codigo);
                                                    while (codigo.length() < 6) {
                                                        codigo = "0" + codigo;
                                                    }
                                                    String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                    escribir("Element count " + ecount);
                                                    while (ecount.length() < 4) {
                                                        ecount = "0" + ecount;
                                                    }
                                                    byte trama[] = tramasElster.getMT64Perfil();

                                                    //off set
                                                    trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                    trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                    trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                    //element count
                                                    trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                    trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    try {
                                                        avisoStr("Solicitud Perfil Carga");
                                                    } catch (Exception e) {
                                                    }
                                                    lST063 = false;
                                                    lST064Perfil = true;
                                                    if (numCanales > 2) {
                                                        contadorST064 = 1;
                                                    } else {
                                                        contadorST064 = 0;
                                                    }
                                                    profileData = new Vector<String[]>();
                                                    tiempo = 1000;
                                                    intentoescuchar = 0;
                                                    seEspera06 = true;
                                                    primerbloque = true;
                                                    ultimatramaEnviada = nuevatrama;
                                                    enviaTrama2(nuevatrama, "");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        } else {
                                            //cerrar
                                            byte trama[] = tramasElster.getLogoff();

                                            trama[2] = getSpoling(bitSpoling);
                                            lST063 = false;
                                            tiempo = 1000;
                                            llogoff = true;
                                            try {
                                                avisoStr("Cerrando comunicacion");
                                            } catch (Exception e) {
                                            }
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST063");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST063");
                                }
                            } else {//incompleta

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                            }
                        }
                    } else {//complemento EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (leventos) {
                                            byte trama[] = tramasElster.getST76Eventos();
                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST063 = false;
                                            lST076 = true;
                                            tiempo = 1000;
                                            EventData = new Vector<String[]>();
                                            escribir("Envia ST076");
                                            try {
                                                avisoStr("Solicitud de eventos");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else if (lregistros) {
                                            byte trama[] = tramasElster.getST021();

                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST063 = false;
                                            lST021 = true;
                                            tiempo = 1000;
                                            escribir("Envia ST021");
                                            try {
                                                avisoStr("Solicitud de eventos");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else if (lperfil) {
                                            boolean procede = false;
                                            if ((vectorhex.length - 8) != 1) {
                                                procede = true;
                                            }
                                            if (procede) { //viene data
                                                try {
                                                    ElementCount = obtenerElementCount();
                                                    offset = obtenerOffset();
//                                                        offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                        ElementCount = (39 + ((numCanales - 1) * 48));
//                                                        if (numCanales == 2) {
//                                                        }
                                                    String codigo = Integer.toHexString(offset).toUpperCase();
                                                    escribir("Offset" + codigo);
                                                    while (codigo.length() < 6) {
                                                        codigo = "0" + codigo;
                                                    }
                                                    String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                    escribir("Element count " + ecount);
                                                    while (ecount.length() < 4) {
                                                        ecount = "0" + ecount;
                                                    }
                                                    byte trama[] = tramasElster.getMT64Perfil();

                                                    trama[2] = getSpoling(bitSpoling);
                                                    //off set
                                                    trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                    trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                    trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                    //element count
                                                    trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                    trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    try {
                                                        avisoStr("Solicitud Perfil Carga");
                                                    } catch (Exception e) {
                                                    }
                                                    lST063 = false;
                                                    lST064Perfil = true;
                                                    if (numCanales > 2) {
                                                        contadorST064 = 1;
                                                    } else {
                                                        contadorST064 = 0;
                                                    }
                                                    profileData = new Vector<String[]>();
                                                    tiempo = 1000;
                                                    intentoescuchar = 0;
                                                    seEspera06 = true;
                                                    primerbloque = true;
                                                    ultimatramaEnviada = nuevatrama;
                                                    enviaTrama2(nuevatrama, "");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        } else {
                                            //cerrar
                                            //cerrar
                                            byte trama[] = tramasElster.getLogoff();

                                            trama[2] = getSpoling(bitSpoling);
                                            lST063 = false;
                                            tiempo = 1000;
                                            llogoff = true;
                                            try {
                                                avisoStr("Cerrando comunicacion");
                                            } catch (Exception e) {
                                            }
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST063");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST063");
                                }
                            } else {//incompleta

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                            }
                        }
                    }
                } else {//basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST063");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST063");
                    }
                }
            }
        }
    }

    private void revisarST076(String[] vectorhex) {
        if (seEspera06) {//se epsera un 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {//llego 06
                if (vectorhex.length > 7) {//cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                    EventData.add(tramatemp);
                                    if (vectorhex[4].equals("00")) {
                                        try {
                                            if (lregistros) {
                                                byte trama[] = tramasElster.getST021();

                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST063 = false;
                                                lST021 = true;
                                                tiempo = 1000;
                                                escribir("Envia ST021");
                                                try {
                                                    avisoStr("Solicitud de eventos");
                                                } catch (Exception e) {
                                                }
                                                intentoescuchar = 0;
                                                ultimatramaEnviada = nuevatrama;
                                                seEspera06 = true;
                                                enviaTrama2(nuevatrama, "");
                                            } else if (lperfil) {
                                                //validamos si llego un nack
                                                boolean procede = false;
                                                if ((vectorhex.length - 8) != 1) {
                                                    procede = true;
                                                }

                                                if (procede) { //viene data
                                                    try {
                                                        ElementCount = obtenerElementCount();
                                                        offset = obtenerOffset();
//                                                            offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                            ElementCount = (39 + ((numCanales - 1) * 48));
//                                                            if (numCanales == 2) {
//                                                            }
                                                        String codigo = Integer.toHexString(offset).toUpperCase();
                                                        escribir("Offset" + codigo);
                                                        while (codigo.length() < 6) {
                                                            codigo = "0" + codigo;
                                                        }
                                                        String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                        escribir("Element count " + ecount);
                                                        while (ecount.length() < 4) {
                                                            ecount = "0" + ecount;
                                                        }
                                                        byte trama[] = tramasElster.getMT64Perfil();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        //off set
                                                        trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                        trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                        trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                        //element count
                                                        trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                        trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        try {
                                                            avisoStr("Solicitud Perfil Carga");
                                                        } catch (Exception e) {
                                                        }
                                                        lST076 = false;
                                                        lST064Perfil = true;
                                                        if (numCanales > 2) {
                                                            contadorST064 = 1;
                                                        } else {
                                                            contadorST064 = 0;
                                                        }
                                                        profileData = new Vector<String[]>();
                                                        tiempo = 1000;
                                                        intentoescuchar = 0;
                                                        seEspera06 = true;
                                                        primerbloque = true;
                                                        ultimatramaEnviada = nuevatrama;
                                                        enviaTrama2(nuevatrama, "");
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    reiniciaComuniacion();
                                                }
                                            } else {
                                                byte trama[] = tramasElster.getLogoff();

                                                trama[2] = getSpoling(bitSpoling);
                                                lST076 = false;
                                                tiempo = 1000;
                                                llogoff = true;
                                                try {
                                                    avisoStr("Cerrando comunicacion");
                                                } catch (Exception e) {
                                                }
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        getSpoling(bitSpoling);
                                        enviaTramack("");
                                    }

                                } else {//nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST076");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST076");
                        }
                    } else {//trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                    }
                } else {
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                    }
                }
            } else {//llego algo diferente cuando se esperaba 06
                if (vectorhex.length > 0) {
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST076");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                    }
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                }
            }
        } else {//se espera un EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {// llega EE
                if (vectorhex.length > 6) { //cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                //System.out.println("Valido tamaño");
                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                EventData.add(tramatemp);
                                if (vectorhex[3].equals("00")) { //es la ultima trama?
                                    if (lregistros) {
                                        byte trama[] = tramasElster.getST021();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST063 = false;
                                        lST021 = true;
                                        tiempo = 1000;
                                        //System.out.println("Envia ST021");
                                        escribir("Envia ST021");
                                        try {
                                            avisoStr("Solicitud de eventos");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    } else if (lperfil) {
                                        //validamos si llego un nack
                                        boolean procede = false;
                                        if ((vectorhex.length - 8) != 1) {
                                            procede = true;
                                        }

                                        if (procede) { //viene data
                                            try {
                                                ElementCount = obtenerElementCount();
                                                offset = obtenerOffset();
//                                                    offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                    ElementCount = (39 + ((numCanales - 1) * 48));
//                                                    if (numCanales == 2) {
//                                                    }
                                                String codigo = Integer.toHexString(offset).toUpperCase();
                                                escribir("Offset" + codigo);
                                                while (codigo.length() < 6) {
                                                    codigo = "0" + codigo;
                                                }
                                                String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                escribir("Element count " + ecount);
                                                while (ecount.length() < 4) {
                                                    ecount = "0" + ecount;
                                                }
                                                byte trama[] = tramasElster.getMT64Perfil();

                                                trama[2] = getSpoling(bitSpoling);
                                                //off set
                                                trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                //element count
                                                trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                try {
                                                    avisoStr("Solicitud Perfil Carga");
                                                } catch (Exception e) {
                                                }
                                                lST076 = false;
                                                lST064Perfil = true;
                                                if (numCanales > 2) {
                                                    contadorST064 = 1;
                                                } else {
                                                    contadorST064 = 0;
                                                }
                                                profileData = new Vector<String[]>();
                                                tiempo = 1000;
                                                intentoescuchar = 0;
                                                seEspera06 = true;
                                                primerbloque = true;
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        byte trama[] = tramasElster.getLogoff();

                                        trama[2] = getSpoling(bitSpoling);
                                        lST076 = false;
                                        tiempo = 1000;
                                        llogoff = true;
                                        try {
                                            avisoStr("Cerrando comunicacion");
                                        } catch (Exception e) {
                                        }
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        ultimatramaEnviada = nuevatrama;
                                        enviaTrama2(nuevatrama, "");
                                    }

                                } else { //no es la ultima solicitamos el siguiente bloque
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST076");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST076");
                        }
                    } else {//trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                    }
                } else {// no tiene cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                    }
                }
            } else {// es complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    //System.out.println("Trama completa " + tramaIncompleta);
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) {//es complemento 06
                        if (vectorhextemp.length > 7) {//cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                            EventData.add(tramatemp);
                                            if (vectorhextemp[4].equals("00")) {
                                                try {
                                                    if (lregistros) {
                                                        byte trama[] = tramasElster.getST021();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        lST063 = false;
                                                        lST021 = true;
                                                        tiempo = 1000;
                                                        //System.out.println("Envia ST021");
                                                        escribir("Envia ST021");
                                                        try {
                                                            avisoStr("Solicitud de eventos");
                                                        } catch (Exception e) {
                                                        }
                                                        intentoescuchar = 0;
                                                        ultimatramaEnviada = nuevatrama;
                                                        seEspera06 = true;
                                                        enviaTrama2(nuevatrama, "");
                                                    } else if (lperfil) {
                                                        //validamos si llego un nack
                                                        boolean procede = false;
                                                        if ((vectorhex.length - 8) != 1) {
                                                            procede = true;
                                                        }
                                                        if (procede) { //viene data
                                                            try {
                                                                ElementCount = obtenerElementCount();
                                                                offset = obtenerOffset();
//                                                                    offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                                    ElementCount = (39 + ((numCanales - 1) * 48));
//                                                                    if (numCanales == 2) {
//                                                                    }
                                                                String codigo = Integer.toHexString(offset).toUpperCase();
                                                                escribir("Offset" + codigo);
                                                                while (codigo.length() < 6) {
                                                                    codigo = "0" + codigo;
                                                                }
                                                                String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                                escribir("Element count " + ecount);
                                                                while (ecount.length() < 4) {
                                                                    ecount = "0" + ecount;
                                                                }
                                                                byte trama[] = tramasElster.getMT64Perfil();

                                                                trama[2] = getSpoling(bitSpoling);
                                                                //off set
                                                                trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                                trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                                trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                                //element count
                                                                trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                                trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                                byte nuevatrama[] = calcularnuevocrc(trama);

                                                                try {
                                                                    avisoStr("Solicitud Perfil Carga");
                                                                } catch (Exception e) {
                                                                }
                                                                lST076 = false;
                                                                lST064Perfil = true;
                                                                if (numCanales > 2) {
                                                                    contadorST064 = 1;
                                                                } else {
                                                                    contadorST064 = 0;
                                                                }
                                                                profileData = new Vector<String[]>();
                                                                tiempo = 1000;
                                                                intentoescuchar = 0;
                                                                seEspera06 = true;
                                                                primerbloque = true;
                                                                ultimatramaEnviada = nuevatrama;
                                                                enviaTrama2(nuevatrama, "");
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            reiniciaComuniacion();
                                                        }
                                                    } else {
                                                        byte trama[] = tramasElster.getLogoff();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        lST076 = false;
                                                        tiempo = 1000;
                                                        llogoff = true;
                                                        try {
                                                            avisoStr("Cerrando comunicacion");
                                                        } catch (Exception e) {
                                                        }
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        ultimatramaEnviada = nuevatrama;
                                                        enviaTrama2(nuevatrama, "");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        } else {//nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST076");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST076");
                                }
                            } else {//trama incompleta

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                            }
                        } else {
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                            }
                        }
                    } else {
                        if (vectorhextemp[0].equals("EE")) { //es complemento EE
                            if (vectorhextemp.length > 6) { //cabecera?
                                if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                    if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                        if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                            EventData.add(tramatemp);
                                            if (vectorhextemp[3].equals("00")) { //es la ultima trama?
                                                try {
                                                    if (lregistros) {
                                                        byte trama[] = tramasElster.getST021();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        lST063 = false;
                                                        lST021 = true;
                                                        tiempo = 1000;
                                                        //System.out.println("Envia ST021");
                                                        escribir("Envia ST021");
                                                        try {
                                                            avisoStr("Solicitud de eventos");
                                                        } catch (Exception e) {
                                                        }
                                                        intentoescuchar = 0;
                                                        ultimatramaEnviada = nuevatrama;
                                                        seEspera06 = true;
                                                        enviaTrama2(nuevatrama, "");
                                                    } else if (lperfil) {
                                                        ElementCount = obtenerElementCount();
                                                        offset = obtenerOffset();
//                                                            offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                            ElementCount = (39 + ((numCanales - 1) * 48));
//                                                            if (numCanales == 2) {
//                                                            }
                                                        String codigo = Integer.toHexString(offset).toUpperCase();
                                                        escribir("Offset" + codigo);
                                                        while (codigo.length() < 6) {
                                                            codigo = "0" + codigo;
                                                        }
                                                        String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                        escribir("Element count " + ecount);
                                                        while (ecount.length() < 4) {
                                                            ecount = "0" + ecount;
                                                        }
                                                        byte trama[] = tramasElster.getMT64Perfil();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        //off set
                                                        trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                        trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                        trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                        //element count
                                                        trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                        trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        try {
                                                            avisoStr("Solicitud Perfil Carga");
                                                        } catch (Exception e) {
                                                        }
                                                        lST076 = false;
                                                        lST064Perfil = true;
                                                        if (numCanales > 2) {
                                                            contadorST064 = 1;
                                                        } else {
                                                            contadorST064 = 0;
                                                        }
                                                        profileData = new Vector<String[]>();
                                                        primerbloque = true;
                                                        tiempo = 1000;
                                                        intentoescuchar = 0;
                                                        ultimatramaEnviada = nuevatrama;
                                                        enviaTrama2(nuevatrama, "");
                                                    }

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else { //no es la ultima solicitamos el siguiente bloque
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        } else {
                                            escribir("bad bit spoling");
                                            enviaTrama2(ultimatramaEnviada, "Solicitud ST076");
                                        }

                                    } else {//bad crc
                                        escribir("bad crc");
                                        enviaTrama1(badcrc, "Solicitud ST076");
                                    }
                                } else {//trama incompleta

                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                                }
                            } else {// no tiene cabecera
                                if (vectorhextemp.length > 0) {
                                    if (vectorhextemp.length == 1) {
                                        reiniciaComuniacion();
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                                    }
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                                }
                            }
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                        }
                    }
                } else {
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST076");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST076");
                    }
                }
            }
        }
    }

    private void revisarST021(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    NBR_SELF_READS = Integer.parseInt(vectorhex[12], 16);
                                    NBR_SUMMATIONS = Integer.parseInt(vectorhex[13], 16);
                                    NBR_DEMANDS = Integer.parseInt(vectorhex[14], 16);
                                    NBR_COIN_VALUES = Integer.parseInt(vectorhex[15], 16);
                                    NBR_PRESENT_DEMANDS = Integer.parseInt(vectorhex[18], 16);
                                    NBR_PRESENT_VALUES = Integer.parseInt(vectorhex[19], 16);
                                    NBR_OCCUR = Integer.parseInt(vectorhex[16], 16);
                                    REG_FUNC1_FLAG = Integer.parseInt(vectorhex[10], 16);
                                    byte trama[] = tramasElster.getST022();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST021 = false;
                                    lST022 = true;
                                    tiempo = 1000;
                                    vST022 = new Vector<String[]>();

                                    escribir("Envia ST022");
                                    try {
                                        avisoStr("Selecion de registros");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST021");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST021");
                        }
                    } else { //incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                    }
                } else { //sin cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                    }
                }
            } else {//llego algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST021");
                } else {
                    reiniciaComuniacion();
                }

            }
        } else { // llego EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                NBR_SELF_READS = Integer.parseInt(vectorhex[11], 16);
                                NBR_SUMMATIONS = Integer.parseInt(vectorhex[12], 16);
                                NBR_DEMANDS = Integer.parseInt(vectorhex[13], 16);
                                NBR_COIN_VALUES = Integer.parseInt(vectorhex[14], 16);
                                NBR_PRESENT_DEMANDS = Integer.parseInt(vectorhex[17], 16);
                                NBR_PRESENT_VALUES = Integer.parseInt(vectorhex[18], 16);
                                NBR_OCCUR = Integer.parseInt(vectorhex[15], 16);
                                REG_FUNC1_FLAG = Integer.parseInt(vectorhex[9], 16);
                                byte trama[] = tramasElster.getST022();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lST021 = false;
                                lST022 = true;
                                tiempo = 1000;
                                vST022 = new Vector<String[]>();
                                escribir("Envia ST022");
                                try {
                                    avisoStr("Selecion de registros");
                                } catch (Exception e) {
                                }
                                intentoescuchar = 0;
                                ultimatramaEnviada = nuevatrama;
                                seEspera06 = true;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST021");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST021");
                        }
                    } else {//incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            NBR_SELF_READS = Integer.parseInt(vectorhextemp[12], 16);
                                            NBR_SUMMATIONS = Integer.parseInt(vectorhextemp[13], 16);
                                            NBR_DEMANDS = Integer.parseInt(vectorhextemp[14], 16);
                                            NBR_COIN_VALUES = Integer.parseInt(vectorhextemp[15], 16);
                                            NBR_PRESENT_DEMANDS = Integer.parseInt(vectorhextemp[18], 16);
                                            NBR_PRESENT_VALUES = Integer.parseInt(vectorhextemp[19], 16);
                                            NBR_OCCUR = Integer.parseInt(vectorhextemp[16], 16);
                                            REG_FUNC1_FLAG = Integer.parseInt(vectorhextemp[10], 16);
                                            byte trama[] = tramasElster.getST022();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST021 = false;
                                            lST022 = true;
                                            tiempo = 1000;
                                            vST022 = new Vector<String[]>();
                                            escribir("Envia ST022");
                                            try {
                                                avisoStr("Selecion de registros");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST021");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST021");
                                }
                            } else { //incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                            }
                        } else { //sin cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp.length - 8 == 1) {//viene un nack
                                            reiniciaComuniacion();
                                        } else {
                                            NBR_SELF_READS = Integer.parseInt(vectorhextemp[11], 16);
                                            NBR_SUMMATIONS = Integer.parseInt(vectorhextemp[12], 16);
                                            NBR_DEMANDS = Integer.parseInt(vectorhextemp[13], 16);
                                            NBR_COIN_VALUES = Integer.parseInt(vectorhextemp[14], 16);
                                            NBR_PRESENT_DEMANDS = Integer.parseInt(vectorhextemp[17], 16);
                                            NBR_PRESENT_VALUES = Integer.parseInt(vectorhextemp[18], 16);
                                            NBR_OCCUR = Integer.parseInt(vectorhex[15], 16);
                                            REG_FUNC1_FLAG = Integer.parseInt(vectorhextemp[9], 16);
                                            byte trama[] = tramasElster.getST022();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST021 = false;
                                            lST022 = true;
                                            tiempo = 1000;
                                            vST022 = new Vector<String[]>();
                                            escribir("Envia ST022");
                                            try {
                                                avisoStr("Selecion de registros");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST021");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST021");
                                }
                            } else {//incompleta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                            }
                        }
                    }
                } else {
                    //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST021");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST021");
                    }

                }
            }
        }
    }

    private void revisarST022(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                    vST022.add(tramatemp);
                                    byte trama[] = tramasElster.getST023();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST022 = false;
                                    lST023 = true;
                                    contadorMT023 = 1;
                                    vST023 = new Vector<String[]>();
                                    tiempo = 1000;
                                    //System.out.println("Envia ST023");
                                    escribir("Envia ST023");
                                    try {
                                        avisoStr("Solicitando registros");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST022");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST022");
                        }
                    } else { //incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                    }
                } else { //sin cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                    }
                }
            } else {//llego algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST022");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                }
            }
        } else { // llego EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                vST022.add(tramatemp);
                                byte trama[] = tramasElster.getST023();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lST022 = false;
                                lST023 = true;
                                contadorMT023 = 1;
                                tiempo = 1000;
                                vST023 = new Vector<String[]>();
                                //System.out.println("Envia ST023");
                                escribir("Envia ST023");
                                try {
                                    avisoStr("Fecha actual");
                                } catch (Exception e) {
                                }
                                intentoescuchar = 0;
                                ultimatramaEnviada = nuevatrama;
                                seEspera06 = true;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                escribir("bad bit spoling");
                                enviaTrama2(ultimatramaEnviada, "Solicitud ST022");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST022");
                        }
                    } else {//incompleta
                        //System.out.println("incompleta");
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                            vST022.add(tramatemp);
                                            byte trama[] = tramasElster.getST023();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST022 = false;
                                            lST023 = true;
                                            contadorMT023 = 1;
                                            tiempo = 1000;
                                            vST023 = new Vector<String[]>();
                                            escribir("Envia ST023");
                                            try {
                                                avisoStr("Solicitando registros");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST022");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST022");
                                }
                            } else { //incompleta
                                //System.out.println("incompleta c");

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                            }
                        } else { //sin cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp.length - 8 == 1) {//viene un nack
                                            reiniciaComuniacion();
                                        } else {
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                            vST022.add(tramatemp);
                                            byte trama[] = tramasElster.getST023();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST022 = false;
                                            lST023 = true;
                                            contadorMT023 = 1;
                                            tiempo = 1000;
                                            vST023 = new Vector<String[]>();
                                            escribir("Envia ST022");
                                            try {
                                                avisoStr("Solicitando registros");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        escribir("bad bit spoling");
                                        enviaTrama2(ultimatramaEnviada, "Solicitud ST022");
                                    }
                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST022");
                                }
                            } else {//incompleta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                            }
                        }
                    }
                } else {
                    //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST022");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                    }

                }
            }
        }
    }

    private void revisarST023(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            //System.out.println("llego 06");
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                    vST023.add(tramatemp);
                                    if (vectorhex[4].equals("00")) {
                                        byte trama[] = tramasElster.getST027();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST023 = false;
                                        lST027 = true;
                                        tiempo = 1000;
                                        vST027 = new Vector<String[]>();
                                        //System.out.println("Envia ST027");
                                        escribir("Envia ST027");
                                        try {
                                            avisoStr("Conf. Registros Instrumentacion");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    } else {
                                        getSpoling(bitSpoling);
                                        enviaTramack("");
                                    }

                                } else {
                                    //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                if (Integer.parseInt(vectorhex[4], 16) < contadorMT023) {// se salto una trama
                                    reiniciaComuniacion();
                                } else { // la trama es repetida
                                    enviaTrama2(ultimatramaEnviada, "");
                                }
                                //enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST023");
                        }
                    } else { //incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                } else { //sin cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                }
            } else {//llego algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST023");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                }

            }
        } else { // llego EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                vST023.add(tramatemp);
                                if (vectorhex[3].equals("00")) {
                                    byte trama[] = tramasElster.getST027();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST023 = false;
                                    lST027 = true;
                                    tiempo = 1000;
                                    vST027 = new Vector<String[]>();
                                    //System.out.println("Envia ST027");
                                    escribir("Envia ST027");
                                    try {
                                        avisoStr("Conf. Registros Instrumentacion");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                }
                            } else {
                                if (Integer.parseInt(vectorhex[3], 16) < contadorMT023) {// se salto una trama
                                    reiniciaComuniacion();
                                } else { // la trama es repetida
                                    enviaTramack("");
                                }
                                //enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST023");
                        }
                    } else {//incompleta
                        //System.out.println("incompleta");
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                            vST023.add(tramatemp);
                                            if (vectorhextemp[4].equals("00")) {
                                                byte trama[] = tramasElster.getST027();

                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST023 = false;
                                                lST027 = true;
                                                tiempo = 1000;
                                                vST027 = new Vector<String[]>();
                                                //System.out.println("Envia ST027");
                                                escribir("Envia ST027");
                                                try {
                                                    avisoStr("Conf. Registros Instrumentacion");
                                                } catch (Exception e) {
                                                }
                                                intentoescuchar = 0;
                                                ultimatramaEnviada = nuevatrama;
                                                seEspera06 = true;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        } else {
                                            //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        if (Integer.parseInt(vectorhextemp[4], 16) < contadorMT023) {// se salto una trama
                                            reiniciaComuniacion();
                                        } else { // la trama es repetida
                                            enviaTramack("");
                                        }
                                        //enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST023");
                                }
                            } else { //incompleta
                                //System.out.println("incompleta c");

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        } else { //sin cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp.length - 8 == 1) {//viene un nack
                                            reiniciaComuniacion();
                                        } else {
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                            vST023.add(tramatemp);
                                            if (vectorhextemp[3].equals("00")) {
                                                byte trama[] = tramasElster.getST027();

                                                trama[2] = getSpoling(bitSpoling);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                lST023 = false;
                                                lST027 = true;
                                                tiempo = 1000;
                                                vST027 = new Vector<String[]>();
                                                //System.out.println("Envia ST027");
                                                escribir("Envia ST027");
                                                try {
                                                    avisoStr("Conf. Registros Instrumentacion");
                                                } catch (Exception e) {
                                                }
                                                intentoescuchar = 0;
                                                ultimatramaEnviada = nuevatrama;
                                                seEspera06 = true;
                                                enviaTrama2(nuevatrama, "");
                                            } else {
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        }
                                    } else {
                                        if (Integer.parseInt(vectorhextemp[3], 16) < contadorMT023) {// se salto una trama
                                            reiniciaComuniacion();
                                        } else { // la trama es repetida
                                            enviaTramack("");
                                        }
                                        //enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST023");
                                }
                            } else {//incompleta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        }
                    }
                } else {
                    //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST023");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }

                }
            }
        }
    }

    private void revisarST027(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                    vST027.add(tramatemp);
                                    byte trama[] = tramasElster.getST028();

                                    trama[2] = getSpoling(bitSpoling);
                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                    lST027 = false;
                                    lST028 = true;
                                    vST028 = new Vector<String[]>();
                                    tiempo = 1000;
                                    //System.out.println("Envia ST028");
                                    escribir("Envia ST028");
                                    try {
                                        avisoStr("Solicitando registros instumentacion");
                                    } catch (Exception e) {
                                    }
                                    intentoescuchar = 0;
                                    ultimatramaEnviada = nuevatrama;
                                    seEspera06 = true;
                                    enviaTrama2(nuevatrama, "");
                                } else {
                                    //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST027");
                        }
                    } else { //incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                    }
                } else { //sin cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                    }
                }
            } else {//llego algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST027");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                }

            }
        } else { // llego EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                vST027.add(tramatemp);
                                byte trama[] = tramasElster.getST028();

                                trama[2] = getSpoling(bitSpoling);
                                byte nuevatrama[] = calcularnuevocrc(trama);
                                lST027 = false;
                                lST028 = true;
                                tiempo = 1000;
                                vST028 = new Vector<String[]>();
                                //System.out.println("Envia ST028");
                                escribir("Envia ST028");
                                try {
                                    avisoStr("Solicitando registros instumentacion");
                                } catch (Exception e) {
                                }
                                intentoescuchar = 0;
                                ultimatramaEnviada = nuevatrama;
                                seEspera06 = true;
                                enviaTrama2(nuevatrama, "");
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST027");
                        }
                    } else {//incompleta
                        //System.out.println("incompleta");
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST022");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                            vST027.add(tramatemp);
                                            byte trama[] = tramasElster.getST028();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST027 = false;
                                            lST028 = true;
                                            vST028 = new Vector<String[]>();
                                            tiempo = 1000;
                                            //System.out.println("Envia ST028");
                                            escribir("Envia ST028");
                                            try {
                                                avisoStr("Solicitando registros instumentacion");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        } else {
                                            //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST027");
                                }
                            } else { //incompleta
                                //System.out.println("incompleta c");
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                            }
                        } else { //sin cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp.length - 8 == 1) {//viene un nack
                                            reiniciaComuniacion();
                                        } else {
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                            vST027.add(tramatemp);
                                            byte trama[] = tramasElster.getST028();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST027 = false;
                                            lST028 = true;
                                            tiempo = 1000;
                                            vST028 = new Vector<String[]>();
                                            //System.out.println("Envia ST028");
                                            escribir("Envia ST028");
                                            try {
                                                avisoStr("Solicitando registros instumentacion");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST027");
                                }
                            } else {//incompleta

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                            }
                        }
                    }
                } else {
                    //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST027");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST027");
                    }
                }
            }
        }
    }

    private void revisarST028(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                if (vectorhex[7].equals("00")) { //ack del data
                                    String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                    System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                    vST028.add(tramatemp);
                                    if (vectorhex[4].equals("00")) {
                                        if (lperfil) {
                                            try {
                                                ElementCount = obtenerElementCount();
                                                offset = obtenerOffset();
//                                                    offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                    ElementCount = (39 + ((numCanales - 1) * 48));
//                                                    if (numCanales == 2) {
//                                                    }
                                                String codigo = Integer.toHexString(offset).toUpperCase();
                                                //System.out.println("codigo offset" + codigo);
                                                escribir("Offset" + codigo);
                                                while (codigo.length() < 6) {
                                                    codigo = "0" + codigo;
                                                }
                                                String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                //System.out.println("Element count " + ecount);
                                                escribir("Element count " + ecount);
                                                while (ecount.length() < 4) {
                                                    ecount = "0" + ecount;
                                                }
                                                byte trama[] = tramasElster.getMT64Perfil();

                                                trama[2] = getSpoling(bitSpoling);
                                                //off set
                                                trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                //element count
                                                trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                byte nuevatrama[] = calcularnuevocrc(trama);
                                                //System.out.println("Envia P");
                                                try {
                                                    avisoStr("Solicitud Perfil Carga");
                                                } catch (Exception e) {
                                                }
                                                lST028 = false;
                                                lST064Perfil = true;
                                                if (numCanales > 2) {
                                                    contadorST064 = 1;
                                                } else {
                                                    contadorST064 = 0;
                                                }
                                                profileData = new Vector<String[]>();
                                                tiempo = 1000;
                                                intentoescuchar = 0;
                                                seEspera06 = true;
                                                primerbloque = true;
                                                ultimatramaEnviada = nuevatrama;
                                                enviaTrama2(nuevatrama, "");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            byte trama[] = tramasElster.getLogoff();

                                            trama[2] = getSpoling(bitSpoling);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            lST028 = false;
                                            llogoff = true;
                                            tiempo = 1000;
                                            //System.out.println("Envia logoff");
                                            escribir("Envia logoff");
                                            try {
                                                avisoStr("Cerrando conexion");
                                            } catch (Exception e) {
                                            }
                                            intentoescuchar = 0;
                                            ultimatramaEnviada = nuevatrama;
                                            seEspera06 = true;
                                            enviaTrama2(nuevatrama, "");
                                        }
                                    } else {
                                        getSpoling(bitSpoling);
                                        enviaTramack("");
                                    }
                                } else {
                                    //nack
                                    reiniciaComuniacion();
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST023");

                        }
                    } else { //incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                } else { //sin cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                }
            } else {//llego algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST028");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                }

            }
        } else { // llego EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                vST028.add(tramatemp);
                                if (vectorhex[3].equals("00")) {
                                    if (lperfil) {
                                        try {
                                            ElementCount = obtenerElementCount();
                                            offset = obtenerOffset();
//                                                offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                ElementCount = (39 + ((numCanales - 1) * 48));
//                                                if (numCanales == 2) {
//                                                }
                                            String codigo = Integer.toHexString(offset).toUpperCase();
                                            //System.out.println("codigo offset" + codigo);
                                            escribir("Offset" + codigo);
                                            while (codigo.length() < 6) {
                                                codigo = "0" + codigo;
                                            }
                                            String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                            //System.out.println("Element count " + ecount);
                                            escribir("Element count " + ecount);
                                            while (ecount.length() < 4) {
                                                ecount = "0" + ecount;
                                            }
                                            byte trama[] = tramasElster.getMT64Perfil();

                                            trama[2] = getSpoling(bitSpoling);
                                            //off set
                                            trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                            trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                            trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                            //element count
                                            trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                            trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                            //System.out.println("Envia P");
                                            try {
                                                avisoStr("Solicitud Perfil Carga");
                                            } catch (Exception e) {
                                            }
                                            lST028 = false;
                                            lST064Perfil = true;
                                            if (numCanales > 2) {
                                                contadorST064 = 1;
                                            } else {
                                                contadorST064 = 0;
                                            }
                                            profileData = new Vector<String[]>();
                                            tiempo = 1000;
                                            intentoescuchar = 0;
                                            seEspera06 = true;
                                            primerbloque = true;
                                            ultimatramaEnviada = nuevatrama;
                                            enviaTrama2(nuevatrama, "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        byte trama[] = tramasElster.getLogoff();

                                        trama[2] = getSpoling(bitSpoling);
                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                        lST028 = false;
                                        llogoff = true;
                                        tiempo = 1000;
                                        //System.out.println("Envia logoff");
                                        escribir("Envia logoff");
                                        try {
                                            avisoStr("Cerrando conexion");
                                        } catch (Exception e) {
                                        }
                                        intentoescuchar = 0;
                                        ultimatramaEnviada = nuevatrama;
                                        seEspera06 = true;
                                        enviaTrama2(nuevatrama, "");
                                    }
                                } else {
                                    getSpoling(bitSpoling);
                                    enviaTramack("");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }

                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST023");
                        }
                    } else {//incompleta
                        //System.out.println("incompleta");
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp[7].equals("00")) { //ack del data
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                            vST028.add(tramatemp);
                                            if (vectorhextemp[4].equals("00")) {
                                                if (lperfil) {
                                                    try {
                                                        ElementCount = obtenerElementCount();
                                                        offset = obtenerOffset();
//                                                            offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                            ElementCount = (39 + ((numCanales - 1) * 48));
//                                                            if (numCanales == 2) {
//                                                            }
                                                        String codigo = Integer.toHexString(offset).toUpperCase();
                                                        //System.out.println("codigo offset" + codigo);
                                                        escribir("Offset" + codigo);
                                                        while (codigo.length() < 6) {
                                                            codigo = "0" + codigo;
                                                        }
                                                        String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                        //System.out.println("Element count " + ecount);
                                                        escribir("Element count " + ecount);
                                                        while (ecount.length() < 4) {
                                                            ecount = "0" + ecount;
                                                        }
                                                        byte trama[] = tramasElster.getMT64Perfil();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        //off set
                                                        trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                        trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                        trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                        //element count
                                                        trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                        trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        //System.out.println("Envia P");
                                                        try {
                                                            avisoStr("Solicitud Perfil Carga");
                                                        } catch (Exception e) {
                                                        }
                                                        lST028 = false;
                                                        lST064Perfil = true;
                                                        if (numCanales > 2) {
                                                            contadorST064 = 1;
                                                        } else {
                                                            contadorST064 = 0;
                                                        }
                                                        profileData = new Vector<String[]>();
                                                        tiempo = 1000;
                                                        intentoescuchar = 0;
                                                        seEspera06 = true;
                                                        primerbloque = true;
                                                        ultimatramaEnviada = nuevatrama;
                                                        enviaTrama2(nuevatrama, "");
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    byte trama[] = tramasElster.getLogoff();

                                                    trama[2] = getSpoling(bitSpoling);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    lST028 = false;
                                                    llogoff = true;
                                                    tiempo = 1000;
                                                    //System.out.println("Envia logoff");
                                                    escribir("Envia logoff");
                                                    try {
                                                        avisoStr("Cerrando conexion");
                                                    } catch (Exception e) {
                                                    }
                                                    intentoescuchar = 0;
                                                    ultimatramaEnviada = nuevatrama;
                                                    seEspera06 = true;
                                                    enviaTrama2(nuevatrama, "");
                                                }
                                            } else {
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        } else {
                                            //nack
                                            reiniciaComuniacion();
                                        }
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST023");
                                }
                            } else { //incompleta

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        } else { //sin cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        if (vectorhextemp.length - 8 == 1) {//viene un nack
                                            reiniciaComuniacion();
                                        } else {
                                            String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                            System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                            vST028.add(tramatemp);
                                            if (vectorhextemp[3].equals("00")) {
                                                if (lperfil) {
                                                    try {
                                                        ElementCount = obtenerElementCount();
                                                        offset = obtenerOffset();
//                                                            offset = ndias * (234 + ((numCanales - 1) * 288));
//                                                            ElementCount = (39 + ((numCanales - 1) * 48));
//                                                            if (numCanales == 2) {
//                                                            }
                                                        String codigo = Integer.toHexString(offset).toUpperCase();
                                                        //System.out.println("codigo offset" + codigo);
                                                        escribir("Offset" + codigo);
                                                        while (codigo.length() < 6) {
                                                            codigo = "0" + codigo;
                                                        }
                                                        String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                        //System.out.println("Element count " + ecount);
                                                        escribir("Element count " + ecount);
                                                        while (ecount.length() < 4) {
                                                            ecount = "0" + ecount;
                                                        }
                                                        byte trama[] = tramasElster.getMT64Perfil();

                                                        trama[2] = getSpoling(bitSpoling);
                                                        //off set
                                                        trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                        trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                        trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                        //element count
                                                        trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                        trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                        byte nuevatrama[] = calcularnuevocrc(trama);
                                                        //System.out.println("Envia P");
                                                        try {
                                                            avisoStr("Solicitud Perfil Carga");
                                                        } catch (Exception e) {
                                                        }
                                                        lST028 = false;
                                                        lST064Perfil = true;
                                                        if (numCanales > 2) {
                                                            contadorST064 = 1;
                                                        } else {
                                                            contadorST064 = 0;
                                                        }
                                                        profileData = new Vector<String[]>();
                                                        tiempo = 1000;
                                                        intentoescuchar = 0;
                                                        seEspera06 = true;
                                                        primerbloque = true;
                                                        ultimatramaEnviada = nuevatrama;
                                                        enviaTrama2(nuevatrama, "");
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    byte trama[] = tramasElster.getLogoff();

                                                    trama[2] = getSpoling(bitSpoling);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    lST028 = false;
                                                    llogoff = true;
                                                    tiempo = 1000;
                                                    //System.out.println("Envia logoff");
                                                    escribir("Envia logoff");
                                                    try {
                                                        avisoStr("Cerrando conexion");
                                                    } catch (Exception e) {
                                                    }
                                                    intentoescuchar = 0;
                                                    ultimatramaEnviada = nuevatrama;
                                                    seEspera06 = true;
                                                    enviaTrama2(nuevatrama, "");
                                                }
                                            } else {
                                                getSpoling(bitSpoling);
                                                enviaTramack("");
                                            }
                                        }
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST023");
                                }
                            } else {//incompleta
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                            }
                        }
                    }
                } else {
                    //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST028");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST023");
                    }
                }
            }
        }
    }

    private void revisarST064(String[] vectorhex) {
        if (seEspera06) {//se espera 06
            seEspera06 = false;
            vectorhex = EliminarRuido(vectorhex);
            if (vectorhex[0].equals("06")) {
                if (vectorhex.length > 7) {//pose cabecera?
                    if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                        if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                perfilincompleto = true;
                                contador15 = 0;
                                if (primerbloque) {
                                    contadorST064 = Integer.parseInt(vectorhex[4], 16);
                                }
                                if (Integer.parseInt(vectorhex[4], 16) == contadorST064) {
                                    if (vectorhex[7].equals("00")) { //ack del data
                                        actualUltimatrama = 0;
                                        if (primerbloque) {
                                            primerbloque = false;
                                            tamañototalbloque = Integer.parseInt(vectorhex[7] + vectorhex[8] + vectorhex[9], 16);
                                            tamañobloque = Integer.parseInt((vectorhex[5] + vectorhex[6]), 16);

                                        } else {
                                            tamañobloque = +Integer.parseInt((vectorhex[5] + vectorhex[6]), 16);
                                        }
                                        if (vectorhex[4].equals("00")) {//viene un solo bloque
                                            if ((tamañobloque - 4) == tamañototalbloque) {
                                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                                profileData.add(tramatemp);
                                                //validamos el checksum por hacer
                                                offset = offset - ElementCount;
                                                tamañobloque = 0;
                                                tamañototalbloque = 0;
                                                if (offset < 0) {//es la ultima trama del perfil de carga
                                                    byte trama[] = tramasElster.getLogoff();

                                                    trama[2] = getSpoling(bitSpoling);
                                                    lST064Perfil = false;
                                                    tiempo = 1000;
                                                    llogoff = true;
                                                    try {
                                                        avisoStr("Cerrando comunicacion");
                                                    } catch (Exception e) {
                                                    }
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    ultimatramaEnviada = nuevatrama;
                                                    perfilincompleto = false;
                                                    enviaTrama2(nuevatrama, "");
                                                } else {
                                                    //se calcula el siguiente bloque
                                                    byte trama[] = tramasElster.getMT64Perfil();
//                                                    if (numCanales > 2) {
//                                                        contadorST064 = 1;
//                                                    } else {
//                                                        contadorST064 = 0;
//                                                    }
                                                    trama[2] = getSpoling(bitSpoling);
                                                    String codigo = Integer.toHexString(offset).toUpperCase();
                                                    while (codigo.length() < 6) {
                                                        codigo = "0" + codigo;
                                                    }
                                                    //System.out.println("Offset" + codigo);
                                                    escribir("Offset" + codigo);
                                                    String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                    while (ecount.length() < 4) {
                                                        ecount = "0" + ecount;
                                                    }
                                                    //Offset
                                                    trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                    trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                    trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                    //Element count
                                                    trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                    trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    tiempo = 4000;
                                                    seEspera06 = true;
                                                    primerbloque = true;
                                                    enviaTrama2(nuevatrama, "");
                                                }
                                            } else {
                                                //bloque esta completo
                                                //System.out.println("tamaño bloque incompleto tamaño total " + tamañototalbloque + " tamaño bloque " + tamañobloque);
                                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                            }
                                        } else {//se solicita el proximo bloque
                                            profileDataTemp = new Vector<String[]>();
                                            String[] trama = new String[(Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9];
                                            System.arraycopy(vectorhex, 0, trama, 0, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9);
                                            profileDataTemp.add(trama);
                                            getSpoling(bitSpoling);
                                            contadorST064--;
                                            enviaTramack("");
                                        }
                                    } else {
                                        //nack
                                        reiniciaComuniacion();
                                    }
                                } else {//no llego la secuencia esperada
                                    if (Integer.parseInt(vectorhex[4], 16) > contadorST064) {
                                        enviaTramack("");
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                }
                            } else {
                                escribir("bad bit spoling");
                                if (primerbloque) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                } else {
                                    if (Integer.parseInt(vectorhex[4], 16) > contadorST064) { //es por que llego repetida se envia ack para que envie la siguiente trama
                                        contador15++;
                                        if (contador15 > 3) {
                                            contador15 = 0;
                                            enviaTramack("");
                                        } else {
                                            enviaTrama1(badcrc, "");
                                        }
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                }
                            }

                        } else {//badcrc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST064");
                        }
                    } else { //incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                    }
                } else { //sin cabecera
                    if (vectorhex.length > 0) {
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                    }
                }
            } else {//llego algo diferente de 06
                if (vectorhex[0].equals("15")) {
                    seEspera06 = true;
                    enviaTrama1(ultimatramaEnviada, "Solicitud ST064");
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                }
            }
        } else { // llego EE o complemento
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex[0].equals("EE")) {
                //System.out.println("llego EE");
                if (vectorhex.length > 6) {//pose cabecera?
                    if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                        if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                            if (((((byte) Integer.parseInt(vectorhex[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                perfilincompleto = true;
                                contador15 = 0;
                                if (primerbloque) {
                                    contadorST064 = Integer.parseInt(vectorhex[3], 16);
                                }
                                if (Integer.parseInt(vectorhex[3], 16) == contadorST064) {
                                    if (vectorhex.length - 8 == 1) {//viene un nack
                                        reiniciaComuniacion();
                                    } else {
                                        actualUltimatrama = 0;
                                        if (vectorhex[3].equals("00")) {//es la ultima trama del bloque se solicita el siguiente bloque
                                            if (primerbloque) { //es un solo bloque
                                                primerbloque = false;
                                                profileDataTemp = new Vector<String[]>();
                                                tamañototalbloque = Integer.parseInt(vectorhex[6] + vectorhex[7] + vectorhex[8], 16);
                                                tamañobloque = (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16));
                                            } else { //es el ultimo bloque
                                                tamañobloque += (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16));
                                            }
                                            if ((tamañobloque - 4) == tamañototalbloque) {
                                                String[] tramatemp = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                                System.arraycopy(vectorhex, 0, tramatemp, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                                profileDataTemp.add(tramatemp);
                                                for (int j = 0; j < profileDataTemp.size(); j++) {
                                                    profileData.add(profileDataTemp.get(j));
                                                }
                                                //validamos el checksum por hacer
                                                offset = offset - ElementCount;
                                                tamañobloque = 0;
                                                tamañototalbloque = 0;
                                                if (offset < 0) {//es la ultima trama del perfil de carga
                                                    byte trama[] = tramasElster.getLogoff();

                                                    trama[2] = getSpoling(bitSpoling);
                                                    lST064Perfil = false;
                                                    tiempo = 1000;
                                                    llogoff = true;
                                                    try {
                                                        avisoStr("Cerrando comunicacion");
                                                    } catch (Exception e) {
                                                    }
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    ultimatramaEnviada = nuevatrama;
                                                    perfilincompleto = false;
                                                    enviaTrama2(nuevatrama, "");
                                                } else {
                                                    //se calcula el siguiente bloque
                                                    byte trama[] = tramasElster.getMT64Perfil();
//                                                    if (numCanales > 2) {
//                                                        contadorST064 = 1;
//                                                    } else {
//                                                        contadorST064 = 0;
//                                                    }
                                                    trama[2] = getSpoling(bitSpoling);
                                                    String codigo = Integer.toHexString(offset).toUpperCase();
                                                    while (codigo.length() < 6) {
                                                        codigo = "0" + codigo;
                                                    }
                                                    //System.out.println("Offset" + codigo);
                                                    escribir("Offset" + codigo);
                                                    String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                    while (ecount.length() < 4) {
                                                        ecount = "0" + ecount;
                                                    }
                                                    //Offset
                                                    trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                    trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                    trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                    //Element count
                                                    trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                    trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                    byte nuevatrama[] = calcularnuevocrc(trama);
                                                    tiempo = 4000;
                                                    seEspera06 = true;
                                                    primerbloque = true;
                                                    enviaTrama2(nuevatrama, "");
                                                }
                                            } else {
                                                //bloque esta completo
                                                //System.out.println("tamaño bloque incompleto tamaño total " + tamañototalbloque + " tamaño bloque " + tamañobloque);
                                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                            }

                                        } else {
                                            if (primerbloque) { //es el primer bloque de 3 o 2
                                                primerbloque = false;
                                                tamañototalbloque = Integer.parseInt(vectorhex[6] + vectorhex[7] + vectorhex[8], 16);
                                                tamañobloque = (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16));
                                                profileDataTemp = new Vector<String[]>();
                                            } else { //es el es un bloque intermedio
                                                tamañobloque += (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16));
                                            }
                                            String[] trama = new String[(Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8];
                                            System.arraycopy(vectorhex, 0, trama, 0, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8);
                                            profileDataTemp.add(trama);
                                            getSpoling(bitSpoling);
                                            contadorST064--;
                                            enviaTramack("");
                                        }
                                    }
                                } else {
                                    if (Integer.parseInt(vectorhex[3], 16) > contadorST064) {
                                        enviaTramack("");
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                }
                            } else {
                                if (primerbloque) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                } else {
                                    if (Integer.parseInt(vectorhex[3], 16) > contadorST064) {
                                        contador15++;
                                        if (contador15 > 3) {
                                            contador15 = 0;
                                            enviaTramack("");
                                        } else {
                                            enviaTrama1(badcrc, "");
                                        }
                                    } else {
                                        reiniciaComuniacion();
                                    }
                                }
                            }
                        } else {//bad crc
                            escribir("bad crc");
                            enviaTrama1(badcrc, "Solicitud ST064");
                        }
                    } else {//incompleta
                        //System.out.println("incompleta");
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                    }
                } else {//no cabecera
                    if (vectorhex.length > 0) {
                        if (vectorhex.length == 1) {
                            reiniciaComuniacion();
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                        }
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                    }
                }
            } else {//complemento
                if (complemento) {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.trim().split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("06")) { //complemento de 06
                        if (vectorhextemp.length > 7) {//pose cabecera?
                            if ((vectorhextemp.length - 9) >= (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16))) { //validamos campo len
                                if (validacionCRCFCS(vectorhextemp, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        perfilincompleto = true;
                                        contador15 = 0;
                                        if (primerbloque) {
                                            contadorST064 = Integer.parseInt(vectorhextemp[4], 16);
                                        }
                                        if (Integer.parseInt(vectorhextemp[4], 16) == contadorST064) {
                                            if (vectorhextemp[7].equals("00")) { //ack del data
                                                actualUltimatrama = 0;
                                                if (primerbloque) { //es un solo bloque
                                                    primerbloque = false;
                                                    tamañototalbloque = Integer.parseInt(vectorhextemp[7] + vectorhextemp[8] + vectorhextemp[9], 16);
                                                    tamañobloque = Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16);
                                                } else {
                                                    tamañobloque = +Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16);
                                                }
                                                if (vectorhextemp[4].equals("00")) {//viene un solo bloque
                                                    if ((tamañobloque - 4) == tamañototalbloque) {
                                                        String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                                        System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                                        profileData.add(tramatemp);
                                                        //validamos el checksum por hacer
                                                        offset = offset - ElementCount;
                                                        tamañobloque = 0;
                                                        tamañototalbloque = 0;
                                                        if (offset < 0) {//es la ultima trama del perfil de carga
                                                            byte trama[] = tramasElster.getLogoff();

                                                            trama[2] = getSpoling(bitSpoling);
                                                            lST064Perfil = false;
                                                            tiempo = 1000;
                                                            llogoff = true;
                                                            try {
                                                                avisoStr("Cerrando comunicacion");
                                                            } catch (Exception e) {
                                                            }
                                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                                            ultimatramaEnviada = nuevatrama;
                                                            perfilincompleto = false;
                                                            enviaTrama2(nuevatrama, "");
                                                        } else {
                                                            //se calcula el siguiente bloque
                                                            byte trama[] = tramasElster.getMT64Perfil();
//                                                            if (numCanales > 2) {
//                                                                contadorST064 = 1;
//                                                            } else {
//                                                                contadorST064 = 0;
//                                                            }
                                                            trama[2] = getSpoling(bitSpoling);
                                                            String codigo = Integer.toHexString(offset).toUpperCase();
                                                            while (codigo.length() < 6) {
                                                                codigo = "0" + codigo;
                                                            }
                                                            //System.out.println("Offset" + codigo);
                                                            escribir("Offset" + codigo);
                                                            String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                            while (ecount.length() < 4) {
                                                                ecount = "0" + ecount;
                                                            }
                                                            //Offset
                                                            trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                            trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                            trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                            //Element count
                                                            trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                            trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                                            tiempo = 4000;
                                                            seEspera06 = true;
                                                            primerbloque = true;
                                                            enviaTrama2(nuevatrama, "");
                                                        }
                                                    } else {
                                                        //bloque esta completo
                                                        //System.out.println("tamaño bloque incompleto tamaño total " + tamañototalbloque + " tamaño bloque " + tamañobloque);
                                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                                    }

                                                } else {//se solicita el proximo bloque
                                                    profileDataTemp = new Vector<String[]>();
                                                    String[] trama = new String[(Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9];
                                                    System.arraycopy(vectorhextemp, 0, trama, 0, (Integer.parseInt((vectorhextemp[5] + vectorhextemp[6]), 16)) + 9);
                                                    profileDataTemp.add(trama);
                                                    getSpoling(bitSpoling);
                                                    contadorST064--;
                                                    enviaTramack("");
                                                }
                                            } else {
                                                //nack
                                                reiniciaComuniacion();
                                            }
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[4], 16) > contadorST064) {
                                                enviaTramack("");
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        }

                                    } else {
                                        if (primerbloque) {
                                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[4], 16) > contadorST064) {
                                                contador15++;
                                                if (contador15 > 3) {
                                                    contador15 = 0;
                                                    enviaTramack("");
                                                } else {
                                                    enviaTrama1(badcrc, "");
                                                }
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        }
                                    }

                                } else {//badcrc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST064");
                                }
                            } else { //incompleta
                                //System.out.println("incompleta c");

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                            }
                        } else { //sin cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                            }
                        }
                    } else {//complemento de EE
                        if (vectorhextemp.length > 6) {//pose cabecera?
                            if ((vectorhextemp.length - 8) >= (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16))) { //validamos campo len
                                if (validacionCRCFCS2(vectorhextemp, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8)) {//validamos CRC
                                    if (((((byte) Integer.parseInt(vectorhextemp[2], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                                        perfilincompleto = true;
                                        contador15 = 0;
                                        if (primerbloque) {
                                            contadorST064 = Integer.parseInt(vectorhextemp[3], 16);
                                        }
                                        if (Integer.parseInt(vectorhextemp[3], 16) == contadorST064) {
                                            if (vectorhextemp.length - 8 == 1) {//viene un nack
                                                reiniciaComuniacion();
                                            } else {
                                                actualUltimatrama = 0;
                                                if (vectorhextemp[3].equals("00")) {//es la ultima trama del bloque se solicita el siguiente bloque
                                                    if (primerbloque) { //es un solo bloque
                                                        primerbloque = false;
                                                        profileDataTemp = new Vector<String[]>();
                                                        tamañototalbloque = Integer.parseInt(vectorhextemp[6] + vectorhextemp[7] + vectorhextemp[8], 16);
                                                        tamañobloque = (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16));
                                                    } else { //es el ultimo bloque
                                                        tamañobloque += (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16));
                                                    }
                                                    if ((tamañobloque - 4) == tamañototalbloque) {
                                                        String[] tramatemp = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                                        System.arraycopy(vectorhextemp, 0, tramatemp, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                                        profileDataTemp.add(tramatemp);
                                                        for (int j = 0; j < profileDataTemp.size(); j++) {
                                                            profileData.add(profileDataTemp.get(j));
                                                        }
                                                        offset = offset - ElementCount;
                                                        tamañobloque = 0;
                                                        tamañototalbloque = 0;
                                                        if (offset < 0) {//es la ultima trama del perfil de carga
                                                            byte trama[] = tramasElster.getLogoff();

                                                            trama[2] = getSpoling(bitSpoling);
                                                            lST064Perfil = false;
                                                            tiempo = 1000;
                                                            llogoff = true;
                                                            try {
                                                                avisoStr("Cerrando comunicacion");
                                                            } catch (Exception e) {
                                                            }
                                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                                            ultimatramaEnviada = nuevatrama;
                                                            perfilincompleto = false;
                                                            enviaTrama2(nuevatrama, "");
                                                        } else {
                                                            //se calcula el siguiente bloque
                                                            byte trama[] = tramasElster.getMT64Perfil();
//                                                            if (numCanales > 2) {
//                                                                contadorST064 = 1;
//                                                            } else {
//                                                                contadorST064 = 0;
//                                                            }
                                                            trama[2] = getSpoling(bitSpoling);
                                                            String codigo = Integer.toHexString(offset).toUpperCase();
                                                            while (codigo.length() < 6) {
                                                                codigo = "0" + codigo;
                                                            }
                                                            //System.out.println("Offset" + codigo);
                                                            escribir("Offset" + codigo);
                                                            String ecount = Integer.toHexString(ElementCount).toUpperCase();
                                                            while (ecount.length() < 4) {
                                                                ecount = "0" + ecount;
                                                            }
                                                            //Offset
                                                            trama[9] = (byte) (Integer.parseInt(codigo.substring(0, 2), 16) & 0xFF);
                                                            trama[10] = (byte) (Integer.parseInt(codigo.substring(2, 4), 16) & 0xFF);
                                                            trama[11] = (byte) (Integer.parseInt(codigo.substring(4, 6), 16) & 0xFF);
                                                            //Element count
                                                            trama[12] = (byte) (Integer.parseInt(ecount.substring(0, 2), 16) & 0xFF);
                                                            trama[13] = (byte) (Integer.parseInt(ecount.substring(2, 4), 16) & 0xFF);
                                                            byte nuevatrama[] = calcularnuevocrc(trama);
                                                            tiempo = 4000;
                                                            seEspera06 = true;
                                                            primerbloque = true;
                                                            enviaTrama2(nuevatrama, "");
                                                        }
                                                    } else {
                                                        //bloque esta completo
                                                        //System.out.println("tamaño bloque incompleto tamaño total " + tamañototalbloque + " tamaño bloque " + tamañobloque);
                                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                                    }

                                                } else {
                                                    if (primerbloque) { //es el primer bloque de 3 o 2
                                                        primerbloque = false;
                                                        tamañototalbloque = Integer.parseInt(vectorhextemp[6] + vectorhextemp[7] + vectorhextemp[8], 16);
                                                        tamañobloque = (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16));
                                                        profileDataTemp = new Vector<String[]>();
                                                    } else { //es un bloque intermedio
                                                        tamañobloque += (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16));
                                                    }
                                                    String[] trama = new String[(Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8];
                                                    System.arraycopy(vectorhextemp, 0, trama, 0, (Integer.parseInt((vectorhextemp[4] + vectorhextemp[5]), 16)) + 8);
                                                    profileDataTemp.add(trama);
                                                    getSpoling(bitSpoling);
                                                    contadorST064--;
                                                    enviaTramack("");
                                                }
                                            }
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[3], 16) > contadorST064) {
                                                enviaTramack("");
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        }

                                    } else {
                                        if (primerbloque) {
                                            enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                        } else {
                                            if (Integer.parseInt(vectorhextemp[3], 16) > contadorST064) {
                                                contador15++;
                                                if (contador15 > 3) {
                                                    contador15 = 0;
                                                    enviaTramack("");
                                                } else {
                                                    enviaTrama1(badcrc, "");
                                                }
                                            } else {
                                                reiniciaComuniacion();
                                            }
                                        }
                                    }

                                } else {//bad crc
                                    escribir("bad crc");
                                    enviaTrama1(badcrc, "Solicitud ST064");
                                }
                            } else {//incompleta
                                //System.out.println("incompleta c i");

                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                            }
                        } else {//no cabecera
                            if (vectorhextemp.length > 0) {
                                if (vectorhextemp.length == 1) {
                                    reiniciaComuniacion();
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                                }
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                            }
                        }
                    }
                } else {
                    //basura
                    if (vectorhex[0].equals("15")) {
                        seEspera06 = true;
                        enviaTrama1(ultimatramaEnviada, "Solicitud ST064");
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud ST064");
                    }
                }
            }
        }
    }

    private void revisarLogoff(String[] vectorhex) {
        if (vectorhex[0].equals("06")) {// se espera 06
            if (vectorhex.length > 7) {//pose cabecera?
                if ((vectorhex.length - 9) >= (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16))) { //validamos campo len
                    if (validacionCRCFCS(vectorhex, (Integer.parseInt((vectorhex[5] + vectorhex[6]), 16)) + 9)) {//validamos CRC
                        if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            cerrarPuerto();
                            //System.out.println("Logout");
                            escribir("Logout");
                            try {
                                if (aviso) {
                                    //lm.jtablemedidores.setValueAt("Leido", indx, 3);
                                    //lm.mdc.fireTableDataChanged();
                                }
                            } catch (Exception e) {
                            }

                            if (leventos) {
                                desglosaST076();
                            }
                            if (lregistros || lperfil) {
                                desglosaMT017();
                            }
                            if (lregistros) {
                                desglosaST022();
                                organizaSumations();
                                organizaDemands();
                                desglosaST023();
                                cp.almacenaST023(med.getnSerie(), med.getMarcaMedidor().getCodigo(), fechaactual, vdesgloseSt023, NBR_SUMMATIONS, NBR_DEMANDS, OrdenSumations, vSumationSelect, regMt017, OrdenDemands, vDemandSelect, NBR_OCCUR, REG_FUNC1_FLAG);
                                desglosaST027();
                                organizaPresentDemands();
                                organizaPresentValues();
                                desglosaST028();
                                cp.almacenaST028(med.getnSerie(), med.getMarcaMedidor().getCodigo(), fechaactual, vdesgloseSt028, NBR_PRESENT_DEMANDS, NBR_PRESENT_VALUES, OrdenPresentDemands, vPresentdemandSelect, regMt017, OrdenPresentValues, vPresentValues, OrdenFasesPresentValues);
                            }
                            if (lperfil) {
                                desglosaST062();
                                desglosarST015();
                                organizarCanales();
                                desglosaPerfil();
                                cp.AlmacenaPerfilCargaElster(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), fechaactual, NBR_BLK_INTS_SET1, numCanales, LP_CTRL_INT_FMT_CDE1, desglosePerfil, intervalos, regST062, regSt015, regMt017, External_Multiplier, Ext_Mult_Scale_Factor, Instrumentation_Scale_Factor, OrdenCanales, file);                    
                                }
                            med.MedLeido = true;
                            cerrarLog("Leido", true);
                            leer = false;
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                        }

                    } else {//badcrc
                        escribir("bad crc");
                        enviaTrama1(badcrc, "Solicitud Logoff");
                    }
                } else {//incompleta
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                }
            } else {
                if (vectorhex.length > 0) {
                    if (vectorhex.length == 1) {
                        reiniciaComuniacion();
                    } else {
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                    }
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                }
            }
        } else {//se espera EE
            if (!complemento) {
                vectorhex = EliminarRuido2(vectorhex);
            }
            if (vectorhex.length > 6) {//pose cabecera?
                if ((vectorhex.length - 8) >= (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16))) { //validamos campo len
                    if (validacionCRCFCS2(vectorhex, (Integer.parseInt((vectorhex[4] + vectorhex[5]), 16)) + 8)) {//validamos CRC
                        if (((((byte) Integer.parseInt(vectorhex[3], 16) & 0xFF) & 0x20) >> 5) == (bitSpoling >> 5)) {// bit spoling esperado
                            cerrarPuerto();
                            //System.out.println("Logout");
                            escribir("Logout");
                            try {
                                if (aviso) {
                                    //lm.jtablemedidores.setValueAt("Leido", indx, 3);
                                    //lm.mdc.fireTableDataChanged();
                                }
                            } catch (Exception e) {
                            }
                            if (leventos) {
                                desglosaST076();
                            }
                            if (lregistros || lperfil) {
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
                            }
                            if (lperfil) {
                                desglosaST062();
                                desglosarST015();
                                organizarCanales();
                                desglosaPerfil();
                                cp.AlmacenaPerfilCargaElster(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), fechaactual, NBR_BLK_INTS_SET1, numCanales, LP_CTRL_INT_FMT_CDE1, desglosePerfil, intervalos, regST062, regSt015, regMt017, External_Multiplier, Ext_Mult_Scale_Factor, Instrumentation_Scale_Factor, OrdenCanales, file);                    
                                }
                            med.MedLeido = true;
                            cerrarLog("Leido", true);
                            leer = false;
                        } else {
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                        }

                    } else {//badcrc
                        escribir("bad crc");
                        enviaTrama1(badcrc, "Solicitud Logoff");
                    }
                } else {//incompleta
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                }
            } else {
                if (vectorhex.length > 0) {
                    if (vectorhex.length == 1) {
                        reiniciaComuniacion();
                    } else {
                        tramaIncompleta = cadenahex;
                        complemento = true;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                    }
                } else {
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud Logoff");
                }
            }
        }
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

    private void almacenaPerfilIncompleto() {
        desglosaMT017();
        desglosaST062();
        desglosarST015();
        organizarCanales();
        desglosaPerfil();
        cp.AlmacenaPerfilCargaElster(med.getnSerie(), med.getMarcaMedidor().getCodigo(), med.getFecha(), fechaactual, NBR_BLK_INTS_SET1, numCanales, LP_CTRL_INT_FMT_CDE1, desglosePerfil, intervalos, regST062, regSt015, regMt017, External_Multiplier, Ext_Mult_Scale_Factor, Instrumentation_Scale_Factor, OrdenCanales, file);                    
        }

    public void terminaHilos() {
        try {
            port.interrupt();
        } catch (Exception e) {
        }
        try {
            port2.interrupt();
        } catch (Exception e) {
        }
        try {
            port3.interrupt();
        } catch (Exception e) {
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
