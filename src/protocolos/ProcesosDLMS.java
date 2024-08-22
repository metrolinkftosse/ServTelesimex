/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasDLMS;
import Entidades.EConstanteKE;
import Entidades.EMedidor;
import Entidades.EParamProtocolos;
import Entidades.ERegistro;
import Entidades.ElecturaAux;
import Entidades.ERegistroEvento;
import Entidades.Electura;
import Entidades.EtipoCanal;
import Util.AES_GCM;
import Util.AES_GMAC;
import Util.SynHoraNTP;
import java.io.ByteArrayOutputStream;
import static java.lang.Thread.sleep;
import java.security.NoSuchAlgorithmException;
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
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author Lenovo
 */
public class ProcesosDLMS {

    EParamProtocolos epp;
    EMedidor med;
    ProcesosSesion ps;
    ControlProcesos cp;

    String usuario;
    private final String name;
    private final boolean hasSec;
    private Integer IC;
    private byte[] InvCounter = new byte[4];
    private byte[] SInvCounter = new byte[4];
    private byte[] StoC;
    byte[] APTitleC;
    byte[] APTitleS;
    byte SC_HLS = (byte) 0x30;
    byte[] EK_HLS;
    byte[] AK_HLS;
    byte[] challengeRes;
    private volatile int nSession = 1;

    TramasDLMS tramas = new TramasDLMS();
    AES_GCM cipher;
    AES_GMAC cipherGmac;

    boolean lperfil; //       Booleanos 
    boolean leventos; //      checkbox 
    boolean lregistros; //       de
    boolean lconfhora; //    Telesimex
    boolean lacumulados;
    boolean ldisconnect;
    boolean lreconnect;

    //variables para obtención de parámetros del medidor en jinit
    private int indxuser = 0;
//    String numeroPuerto;
//    int numeroReintentos = 4;
    private int reintentoReenvio = 0;
    int ReintentoFRMR = 0; //  Manejo de reintentos en
    int ReinicioFRMR = 0; //   validatipotrama por FRMR
//    int velocidadPuerto;
//    long timeout;
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
    int InvokeIDandParity = 193; //0xC1 := 193
    boolean sumaInvokeIDnP = true;
    boolean increaseCounterHLS = true;
    boolean save = false;
    long ndesfasepermitido = 0;
    Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    ArrayList<EtipoCanal> vtipocanal;
    long tiempo = 500;
    String tiporegistros = "0";
    ArrayList<EtipoCanal> vtiporegistros;
    Vector<Electura> vlec;

    //variables para control de procesamiento de respuestas
    boolean Broadcast = false;
    boolean lfechasync = false;
    boolean ldisc_rec = false;
    boolean lSNRMUA = false;
    boolean lARRQ = false;
    boolean lsecurity = false;
    boolean lserialnumber = false;
    boolean lconsumosAcumulados = false;
    boolean lfirmware = false;
    boolean lphyaddress = false;
    boolean lfechaactual = false;
    boolean lfechaactual2 = false;
    boolean lperiodoIntegracion = false;
    //boolean lentradasenuso = false; 
    boolean linfoperfil = false;
    boolean lconstants = false;
    boolean lpowerLost = false;
    boolean lperfilcarga = false;
    boolean lregis = false; //registros
    boolean regisdia = false; //registros diarios
    boolean regismes = false; //registros mensuales
    boolean ldisconnstate = false;
    boolean lprecierre = false; //antes lterminar
    boolean lcierrapuertos = false;
    boolean lReset = false;
    boolean prereset = false;
    boolean resetWithoutLogout = false;

    //control revision de tramas
    public boolean tramaOK = false;
    private int PhyAddClass = 17;
    private int indxLength = 2;
    private int numBytesDir = 1;
    private int indxControl;
    private int indxhcs1;
    private int indxhcs2;
    private int indxSacarDatos;
    private int indxData;
    private int idxVPerfil = 0;

    //Dinámica del control byte
    int ns = 0;
    int nr = 0;
    int nrEsperado = 0;
    int nsEsperado = 0;

    //variables para fecha y hora 
    private static ZoneId zid;
    private static String zona;
    Timestamp time = null; //tiempo de NTP
    Timestamp tsfechaactual;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    Timestamp fechaAcumulados;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM/dd HH");
    SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
    
    
    //Atributos para acumulados
    private int nOBIS_acumulado = 1;
    private int nTarifa = 0;
    private final double[][] valAcumulados_Tarifas = new double[4][4];
    private final String [] obisEnergias_Acumuladas = {"0101010800FF", "0101020800FF", "0101030800FF", "0101040800FF"};
    private final String[] unidadesEnergias_Acumuladas = {"kWhD", "kWhR", "kVarhD", "kVarhR"};
    
    
    //variables para obtención de datos (perfil, registros, eventos)
    List<String> listEventos; //No se usa
    ArrayList<String> vRegistros = new ArrayList<>();
    ArrayList<String> vRegistrosdia = new ArrayList<>();
    ArrayList<String> vRegistrosmes = new ArrayList<>();
    ArrayList<String> vPerfilCarga = new ArrayList<>();
    ArrayList<ArrayList<String>> vPerfilCargaList = new ArrayList<>();
    String[] currCiphBlock;
    boolean newBlock, twoBytesLen;
    int blockLen, partialLen;
    ArrayList<ERegistro> vreg;
    ERegistro reg;
    boolean procesaIncompleto = false; //para casos de errores en procesamiento de las peticiones principales
    boolean procesaIncompletoPerfil = false;
    boolean procesaIncompletoEventos = false;
    boolean procesaIncompletoRegistros = false;
    int registrosprocesados, registrosprocesadosE = 0;
    int registrosAprocesar = 0;
    boolean clockSincronizado = false;
    //variables adicionales para control de envío de peticiones
    private long desfase;
    boolean solicitar;
    boolean completabloque = false;
    boolean concatena = false;

    //variables para interpretación y procesamiento de datos
    ArrayList<String> dataEnArreglo = new ArrayList<>();
    ArrayList<Boolean> dataEnArregloNulos = new ArrayList<>();
    public short pila[] = new short[10];
    public int i1, j1, l1;
    boolean datoNulo = false;
    int bytesRecortados = 0;
    int bytesRecortadosPerfil = 0;
    int bytesRecortadosRegistros = 0;
    int recortaFormato, recortaFormatoE, recortaFormatoP, recortaFormatoR = 0;
    String[] vectorDatarecibido = new String[0];
    String datoserial = "";
    String datofirmware = "";
    SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Date fechaActual;
    private int periodoIntegracion = 15;
    int intervalo = 0;
    Date fechaFin;
    int constantesmatch = 0;
    Vector<String> infoPerfil = new Vector<>();          // variables generales
    ArrayList<String> obis = new ArrayList<>();             //    para
    ArrayList<String> conske = new ArrayList<>();           // infoperfil
    ArrayList<String> clase = new ArrayList<>();            //     = 
    ArrayList<String> unidad = new ArrayList<>();           //  canales
    //Específicos
    ArrayList<String> obisPerfil = new ArrayList<>();       //    Variables
    ArrayList<String> conskePerfil = new ArrayList<>();     //      para
    ArrayList<String> clasePerfil = new ArrayList<>();      //   canales de
    ArrayList<String> unidadPerfil = new ArrayList<>();     //  Perfil de carga
    int intervaloRdia = 0;
    ArrayList<String> obisRdia = new ArrayList<>();         //    Variables
    ArrayList<String> conskeRdia = new ArrayList<>();       //      para
    ArrayList<String> claseRdia = new ArrayList<>();        //   canales de
    ArrayList<String> unidadRdia = new ArrayList<>();       //  Registros diarios
    int intervaloRmes = 0;
    ArrayList<String> obisRmes = new ArrayList<>();         //    Variables
    ArrayList<String> conskeRmes = new ArrayList<>();       //      para
    ArrayList<String> claseRmes = new ArrayList<>();        //   canales de
    ArrayList<String> unidadRmes = new ArrayList<>();       //  Registros mensuales
    int indexConstant = 0;
    private boolean primerbloque;
    public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
    ERegistroEvento regEvento;
    Date fechaEvento = null;
    List<ERegistroEvento> listRegEventos;
    public int indicecanal = 0;
    Timestamp fechaintervalo = null;
    Timestamp fechalectura = null;
    //Gama300, Microstar ->
    Timestamp fprimerintervalo = null;
    boolean primerintervalo = true;
    ArrayList<Double> lecInterval = new ArrayList<>();
    public int indxlec = 0;
    //Gama300, Microstar <-
    int nintervalo = 0;//intervalo para controlar la fecha
    Timestamp ultimoIntervalo = null;
    Timestamp fechaCero = null;
    EConstanteKE econske = null;
    String canal = "";
    Electura lec;
    ElecturaAux elecaux = null;
    //double lecCero = 0.0;
    ArrayList<Double> lecCero = new ArrayList<>();
    public int indiceCero = 1;
    boolean procesaCeros = false;
    boolean constanteOk = false;

    ArrayList<String> PrincipalOBIS;
    
    int tipoconexion; //identificar comunicación vía GPRS interno
    String wrapperGPRS = ""; //Wrapper GPRS

    public ProcesosDLMS(EParamProtocolos epp, EMedidor med, ProcesosSesion ps, ControlProcesos cp, String usuario, String nameMedidor, boolean hasSec) {
        ////System.out.println("Todo bien iniciando");
        this.epp = epp;
        this.med = med;
        this.ps = ps;
        this.cp = cp;
        this.usuario = usuario;
        this.name = nameMedidor;
        this.hasSec = hasSec;
        init();
    }

    public void setSC_HLS(byte SC) {
        this.SC_HLS = SC;
    }
    
    public void setEK_HLS(byte[] EK_HLS) {
        this.EK_HLS = EK_HLS;
    }
    
    public void setAK_HLS(byte[] AK_HLS) {
        this.AK_HLS = AK_HLS;
    }

    private void init() {
        try {
            zona = cp.buscarParametrosString(7);
            ps.escribir("Estableciendo Zona Horaria: " + zona);
            zid = ZoneId.of(zona);//"America/Bogota"
        } catch (Exception e) {
            ps.escribir("Error obteniendo el parámetro de zona horaria.");
            zona = "America/Bogota";
            ps.escribir("Estableciendo Zona Horaria por defecto: " + zona);
            zid = ZoneId.of(zona);
        }      
        try {
            tipoconexion = ps.med.getTipoconexion();
            sumaInvokeIDnP = epp.DLMS_sumaInvokeIDnP;
            increaseCounterHLS = epp.increaseCounter_HLS;
            InvokeIDandParity = epp.DLMS_InvokeIDandParity;
            PrincipalOBIS = epp.DLMS_PrincipalOBIS;
            PhyAddClass = epp.getPhyAddClass();
            indxLength = epp.DLMS_indxLength;
            numBytesDir = epp.DLMS_numBytesDir;
            indxControl = indxLength + numBytesDir + 2;
            indxhcs1 = indxControl + 1;
            indxhcs2 = indxhcs1 + 1;
            indxSacarDatos = indxhcs2 + 1; //Desde donde inicia el E6E600 
            indxData = indxSacarDatos + 3; // Después de LLC (E6 E6 00)
            // Si la conexión no es GPRS mantenemos sus valores, si no, ambas variables se establecen en 8
            indxSacarDatos = tipoconexion == 0 ? 8 : indxSacarDatos;
            indxData = (tipoconexion == 0 ? 8 : indxData); //Index Data para GPRS interno
            lperfil = ps.lperfil; //       Booleanos 
            leventos = ps.leventos; //      checkbox 
            lregistros = ps.lregistros; //       de
            lconfhora = ps.lconfhora; //    Telesimex
            lacumulados = ps.lacumulados;
            ldisconnect = ps.ldisconnect;
            lreconnect = ldisconnect ? false : ps.lreconnect;

            indxuser = ps.indxuser;
            password = lconfhora ? ps.password2 : ps.password;
            ndias = ps.ndias;
            ndiaReg = ps.ndiaReg;
            nmesReg = ps.nmesReg;
            ndiaseventos = ps.ndiaseventos;
            seriemedidor = ps.seriemedidor;
            numcanales = ps.numcanales;
            dirfis = ps.dirfis;
            dirlog = ps.dirlog;
            ndesfasepermitido = ps.ndesfasepermitido;
            lconske = ps.lconske;
            vtipocanal = ps.vtipocanal;
            String sdirfis = Integer.toHexString((dirfis) & 0xFF).toUpperCase();
            while (sdirfis.length() < 2) {
                sdirfis = "0" + sdirfis;
            }
            String susers = Integer.toHexString(epp.users[0] & 0xFF).toUpperCase();
            while (susers.length() < 2) {
                susers = "0" + susers;
            }
            wrapperGPRS = "000100" + susers + "00" + sdirfis;// 00 01 00 client 00 physical - Ex. 0001 0001 0017
            ps.setEndFrame( calcularnuevocrcRR( asignaDirecciones( tramas.getLogout().clone() ) ));  
        } catch (Exception e) {
            ps.leer = false;
            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
        }
    }

    public void enviaSNRM1() {
        ps.enviaPrimeraTrama = false;
        if (Broadcast && setdirfis) {
            Broadcast = false;
        } else if (lSNRMUA) {
            Broadcast = true;
        }
        lSNRMUA = true;
        numBytesDir = (Broadcast ? 1 : epp.DLMS_numBytesDir);
        indxControl = indxLength + numBytesDir + 2;
        indxhcs1 = indxControl + 1;
        indxhcs2 = indxhcs1 + 1;
        indxSacarDatos = indxhcs2 + 1; //Desde donde inicia el E6E600 
        byte[] trama = tramas.getTramasSNRM().get(this.name).clone();        
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcI(trama);
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> SNRM";
        ps.lenviaTrama2 = true;
    }

    public void interpretaCadena(String cadenahex) throws Exception {
        try {
            ps.interpretaCadenaB = false;
            ps.escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (lSNRMUA) { //SNRM
                revisarSNRM(vectorhex);
            } else if (lARRQ) {
                revisarAARQ(vectorhex);
            } else if (lsecurity & !lprecierre & !lReset) {
                revisarSecurity(vectorhex);
            } else if (lpowerLost) {
                revisarEventos(vectorhex);
            } else if (lserialnumber) {
                revisarSerial(vectorhex);
            } else if (lconsumosAcumulados) {
                revisarAcumulados(vectorhex);
            } else if (lfirmware) {
                revisarFirmware(vectorhex);
            } else if (lphyaddress) {
                revisarPhyAddress(vectorhex);
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
            } else if (lprecierre) { //pre cierre
                revisarPrelogout(vectorhex);
            } else if (lcierrapuertos) { //cerrar puertos
                revisarLogout(vectorhex);
            } else if (lReset) {//estado reset para los casos de contraseña mala y negacio
                revisarReset(vectorhex);
            } else if (lfechasync) {
                revisarConfHora(vectorhex);
            } else if (ldisc_rec) {
                revisarDiscRec(vectorhex);
            } else if (ldisconnstate) {
                revisarDisc_ConnState(vectorhex);
            } else {
                throw new Exception("Estado desconocido");
            }
        } catch (Exception e) {
            ps.cerrarPuerto(true);
            ps.escribir("Estado lectura No leido");
            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, "LeerTCPDLMS", "Error al interpretar la respuesta.");
            ps.cerrarLog("Error en la etapa de interpretación", false);
            ps.leer = false;
        }
    }

    //Método Gestor de Reenvíos
    private void handleForwarding(String peticion) {
        if (reintentoReenvio <= ps.numeroReintentos) {
            reintentoReenvio ++;
            ps.descripcionTrama = "=> Envia Solicitud " + peticion;
            ps.lenviaTrama2 = true;
        } else {
            ps.enviaPrimeraTrama = false;
            ps.lenviaTrama2 = false;
            reintentoReenvio = 0;
            byte trama[] = tramas.getPrelogout();
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            ps.escribir("Envia  PreLogout sin espera de respuesta");
            ps.escribir("=> " + tramas.encode(trama, trama.length));
            ps.enviaTrama(trama);
            ps.escribir("Intentos de reenvío agotados");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, "LeerTCPDLMS", "Intentos de reenvío agotados");
            ps.cerrarPuerto(true);
            ps.leer = false;
        }
    }
    
    public String[] revisarHeaderYcrc(String[] vectorhex, String peticion) {
        if (tipoconexion == 0) {
            if (vectorhex.length > 7) {//tiene cabecera
                String tamauxGPRS = vectorhex[6] + vectorhex[7]; //Para verificar lenght de GPRS
                if (vectorhex.length == (Integer.parseInt(tamauxGPRS, 16) + indxData)) {
                    tramaOK = true;
                    return vectorhex;
                } else {
                    //trama incompleta
                    ps.escribir("Error trama incompleta");
                    handleForwarding(peticion);
                }
            } else {//trama incompleta
                ps.escribir("Error trama Sin cabecera");
                handleForwarding(peticion);
            }
        } else {
            vectorhex = ps.cortarTrama(vectorhex, (Integer.parseInt((vectorhex[indxLength - 1] + vectorhex[indxLength]), 16) & 0x7FF));
            if (validacionCRCHCS(vectorhex)) {
                if (validacionCRCFCS(vectorhex)) {
                    tramaOK = true;
                    return vectorhex;
                } else {
                    //badFCS
                    ps.escribir("BAD FCS");
                    handleForwarding(peticion);
                }
            } else {
                //badHCS
                ps.escribir("BAD HCS");
                handleForwarding(peticion);
            }
        }
        return vectorhex;
    }

    public void revisarSNRM(String[] vectorhex) {
        vectorhex = revisarHeaderYcrc(vectorhex, "SNRM");
        if (tramaOK) {
            tramaOK = false;
            if (vectorhex[indxControl].equals("73") || vectorhex[indxControl].equals("63")) {
                ns = 0;
                nr = 0;
                ps.escribir("NsPc " + ns + " NrPc " + nr);
                nrEsperado = ns + 1;
                nsEsperado = 0;
                lSNRMUA = false;
                enviaAARQ();
            } else {
                ps.reiniciaComunicacion(true);
            }
        }
    }
    
    public void enviaAARQ(){
        lARRQ = true;
        byte trama[] = crearAARQ(); //AARQ 
        trama = asignaDirecciones(trama);
        trama = calcularnuevocrcI(trama);
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;
        ps.ultimatrama = ps.ultimatramaEnviada = trama;        
        ps.descripcionTrama = "=> AARQ";
        ps.enviaPrimeraTrama = false;
        ps.lenviaTrama2 = true;
    }

    public void revisarControlByte(String[] vectorhex, String peticion) {
        if (tipoconexion != 0){
            if (tramaOK) {
                tramaOK = false;
                if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x01) == 0x00) { //es informacion
                    //validamos secuencia
                    if (nrEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) && nsEsperado == ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1)) {
                        if (vectorhex[indxSacarDatos + 3].equals("D8")) {
                            ps.escribir("Exception Response");
                            //detección de exceptions en tramas de información E6 E7 00 D8
                            lprecierre = true;
                            resetWithoutLogout = true;
                            enviaPrelogout();
                        } else {
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
                            tramaOK = true;
                        }
                    } else {
                        //no son los ns y nr esperados
                        ps.escribir("No son los ns y nr esperados");
                        handleForwarding(peticion);
                    }
                } else {
                    if (vectorhex[indxControl].equals("73")) {
                        handleForwarding(peticion);
                    } else {
                        validarTipoTrama(vectorhex[indxControl]);
                    }
                }
            } 
        }
    }

    public void revisarAARQ(String[] vectorhex) throws NoSuchPaddingException, Exception {
        String peticion = "AARQ";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lARRQ = false;
            if (vectorhex[indxData].equals("61")) {
                if (nSession == 1) {
                    if (vectorhex[indxData + 17].equals("00") && vectorhex[indxData + 24].equals("00")) { //VIC 22-10-19                        
                        if (this.hasSec && nSession == 1) {
                            enviaInvCounter();
                        } else if (Broadcast) {
                            enviaPhyAddress();
                        } else {
                            enviaSerial();
                        }
                    } else {
                        //reiniciamos
                        if (vectorhex[indxData + 24].equals("0D")) {
                            indxuser++;                        
                            if (indxuser < epp.users.length) {
                                ps.escribir("Desconexion - Error de autenticacion");
                                lprecierre = true;
                                prereset = true;
                                enviaPrelogout();
                            } else {
                                ps.cerrarPuerto(true);                                
                                ps.escribir("Desconexion - Error de autenticacion");
                                ps.cerrarLog("Desconexion Error de autenticacion", false);
                                ps.leer = false;
                            }
                        } else {
                            ps.escribir("Error de autenticacion - fallo en interpretación de AARE");
                            indxuser++;
                            if (indxuser < epp.users.length) {
                                lprecierre = true;
                                prereset = true;
                                enviaPrelogout();
                            } else {
                                ps.cerrarPuerto(true);                                
                                ps.escribir("Estado Lectura No leido");
                                ps.cerrarLog("Desconexion Error de autenticacion", false);
                                ps.leer = false;
                            }
                        }
                    }
                } else if (hasSec && nSession == 2) {
                    boolean A4 = false, AA = false, BE = false;
                    int pos = 0;
                    for (String byteElement : vectorhex) {
                        //System.out.println("byteElement: " + byteElement);
                        if (byteElement.equalsIgnoreCase("A4") && !A4) {
                            APTitleS = new byte[Integer.parseInt(vectorhex[pos + 3], 16)];
                            for (int i = 0; i < APTitleS.length; i++) {
                                APTitleS[i] = (byte) (Integer.parseInt(vectorhex[pos + 4 + i], 16) & 0xFF);
                            }
                            //System.out.println("APTitle Server: " + Arrays.toString(APTitleS));
                            ps.escribir("APTitle Server: " + Arrays.toString(APTitleS));
                            A4 = true;
                        }
                        if (byteElement.equalsIgnoreCase("AA") && A4 && !AA) {
                            StoC = new byte[Integer.parseInt(vectorhex[pos + 3], 16)];
                            for (int i = 0; i < StoC.length; i++) {
                                StoC[i] = (byte) (Integer.parseInt(vectorhex[pos + 4 + i], 16) & 0xFF);
                            }
                            //System.out.println("Challenge Server to Client: " + Arrays.toString(StoC));
                            ps.escribir("Challenge Server to Client: " + Arrays.toString(StoC));
                            AA = true;
                        }
                        if (byteElement.equalsIgnoreCase("BE") && A4 && AA && !BE) {
                            for (int i = 0; i < SInvCounter.length; i++) {
                                SInvCounter[i] = (byte) (Integer.parseInt(vectorhex[pos + 7 + i], 16) & 0xFF);
                            }
                            ps.escribir("Server Invocation Counter: " + Arrays.toString(SInvCounter));
                            SInvCounter = cipher.increaseCounter(SInvCounter);
                            SInvCounter = cipher.increaseCounter(SInvCounter);
                            BE = true;
                            break;
                        }
                        pos += 1;
                    }
                    if(BE){
                        enviaCurrentAssociation();
                    } else {
                        ps.escribir("AARE recibido inconsistente");
                        lprecierre = true;
                        prereset = true;
                        enviaPrelogout();
                    }
                }
            } else {
                ps.escribir("AARE no recibido");
                indxuser++;
                if (indxuser < epp.users.length) {
                    lprecierre = true;
                    prereset = true;
                    enviaPrelogout();
                } else {
                    ps.cerrarPuerto(true);                    
                    ps.escribir("Estado Lectura No leido");
                    ps.cerrarLog("Desconexion Error de autenticacion", false);
                    ps.leer = false;
                }
            }
        }
    }
    
    private byte[] construirTramaGPRS(byte[] trTCP) {
        ArrayList<String> trama = new ArrayList<>();
        //wrapper TCP
        for (int i = 0; i < wrapperGPRS.length(); i=i+2) {
            trama.add(wrapperGPRS.substring(i, i + 2).toUpperCase());
        }
        //lenght
        trama.add("00");
        trama.add("00");
        //data
        for (int i = indxControl+6; i < trTCP.length-3; i++) {//(Después de E6 E6 00)
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

    public void enviaSerial() throws Exception {
        lserialnumber = true;
        byte[] trama;
        if (hasSec && nSession == 2) {
            byte[] APDU =  reconstruirGetRequestAPDU(1, 0, 2);//Clase 1, Objeto 0, MethodId 2 - Serial;
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte[] cAPDU = cipher.encrypt( APDU );
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequestNormal(1, 0, 2);//clase 1, Objeto 0, MethodId 2 - Serial  
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Numero de Serial";
        ps.lenviaTrama2 = true;
    }
    
    private void enviaAcumulado() throws Exception {
        lconsumosAcumulados = true;
        byte trama[];       
        if (hasSec && nSession == 2) {
            byte[] APDU = reconstruirGetRequestAPDU(3, 12, 2);//Clase 3, Objeto 12, MethodId 2 - Acumulados;
            APDU[7] = (byte) (nOBIS_acumulado & 0xFF);
            APDU[9] = (byte) (nTarifa & 0xFF);
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte[] cAPDU = cipher.encrypt( APDU );
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequestNormal(3, 12, 2);//clase 3, Objeto 12, MethodId 2 - Acumulados  
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Acumulados";
        ps.lenviaTrama2 = true;
    }
    
    public void enviaFirmware() throws Exception {
        lfirmware = true;
        byte[] trama;
        if (hasSec && nSession == 2) {
            byte[] APDU = reconstruirGetRequestAPDU(1, 11, 2);//Clase 1, Objeto 11, MethodId 2 - Firmware;
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte[] cAPDU = cipher.encrypt( APDU );
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequestNormal(1, 11, 2);//clase 1, Objeto 11, MethodId 2 - Firmware  
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Firmware";
        ps.lenviaTrama2 = true;
    }

    public void enviaInvCounter() {
        lsecurity = true;
        byte trama[] = reconstruirGetRequestNormal(1, 4, 2);//clase 1, Objeto 4, MethodId 2 - Invocation Counter
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Contador de Invocación";
        ps.lenviaTrama2 = true;
    }

    public void enviaPhyAddress() throws Exception {
        lphyaddress = true;
        byte[] trama;
        if (hasSec && nSession == 2) {
            byte[] APDU = reconstruirGetRequestAPDU(PhyAddClass, 3, 2);//Clase 17, Objeto 3, MethodId 2 - Dirección Física;
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte[] cAPDU = cipher.encrypt( APDU );
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequestNormal(PhyAddClass, 3, 2);//clase 17, Objeto 3, MethodId 2 - Dirección Física  
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Direccion Fisica";
        ps.lenviaTrama2 = true;
    }

    public void enviaCurrentAssociation() throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {
        byte[] gmac;
        lsecurity = true;
        if (increaseCounterHLS) {
            cipher.increaseCounter();
        }
        cipherGmac = new AES_GMAC(EK_HLS, AK_HLS, APTitleC, cipher.getIC());
        gmac = cipherGmac.generateGmac(StoC, SC_HLS);
        challengeRes = new byte[5 + gmac.length];
        challengeRes[0] = SC_HLS;
        ////System.out.println("Inicialization Vector: "+ps.encode(cipherGmac.getIV(), cipherGmac.getIV().length));
        System.arraycopy(cipherGmac.getIC(), 0, challengeRes, 1, 4);
        System.arraycopy(gmac, 0, challengeRes, 5, gmac.length);
        ps.escribir("IC|GMAC Data: " + ps.encode(challengeRes, challengeRes.length));
        byte dataType[] = {(byte) 0x09, (byte) challengeRes.length};
        byte APDU[] = concatFrames(reconstruirActionRequestAPDU(15, 5, 1, true), concatFrames(dataType, challengeRes));
        ps.escribir("APDU sin Cifrar: " + tramas.encode(APDU, APDU.length));
        byte cAPDU[] = cipher.encrypt(APDU);
        byte trama[] = reconstruirGloActionRequest(cAPDU);
        trama = tipoconexion == 0 ? construirTramaGPRS(trama) : trama;
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Ciphered Action Req";
        ps.lenviaTrama2 = true;
    }
    
    public byte[] insertReqNextBlock ( byte[] trama, byte[] blockNumber) {
        // Req Next Data Block
        System.arraycopy(blockNumber, 0, trama, 3, blockNumber.length);
        return trama;
    }      

    public byte[] addClockAccessSelection(byte[] APDU, byte[] parameters) {
        byte[] clockAccessSelector;
        APDU[APDU.length - 1] = (byte) 0x01;       
        if (epp.isDLMS_useDateTimeFormat()) {//cambia el formato del dateTime
            clockAccessSelector = tramas.getClockAccessSelectionDT().clone();
            System.arraycopy(parameters, 0, clockAccessSelector, 22, 12);
            System.arraycopy(parameters, 12, clockAccessSelector, 35, 12);
        } else {
            clockAccessSelector = tramas.getClockAccessSelection().clone();
            System.arraycopy(parameters, 0, clockAccessSelector, 23, 12);
            System.arraycopy(parameters, 12, clockAccessSelector, 37, 12);
        }
        return concatFrames(APDU, clockAccessSelector);
    }

    public byte[] reconstruirGetRequestAPDU(int clase, int OBISInternalCod, int methodId) {
        byte cosemDesc[] = tramas.getCosemDescriptor().clone();
        cosemDesc[0] = (byte) 0xC0;
        cosemDesc = asignaInvokeIDandParity(cosemDesc, true);
        cosemDesc = editCosemMethodDescriptor(cosemDesc, clase, PrincipalOBIS.get(OBISInternalCod), methodId);
        return cosemDesc;
    }

    public byte[] reconstruirGetRequest(byte[] APDU) {
        byte trama[] = tramas.getGetRequestNormal().clone();
        byte tramaInicio[];
        byte tramaFin[];
        trama = asignaDirecciones(trama);
        tramaInicio = Arrays.copyOfRange(trama, 0, indxControl + 6);
        tramaFin = Arrays.copyOfRange(trama, trama.length - 3, (trama.length - 1) + 1);
        trama = concatFrames(concatFrames(tramaInicio, APDU), tramaFin);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama);
        return trama;
    }

    public byte[] reconstruirGloGetRequest(byte[] cAPDU) {
        byte nTrama[] = new byte[(indxControl + 9) + cAPDU.length - 8 + 3];
        byte cTrama[] = tramas.getGloGetRequest().clone();
        cTrama = asignaDirecciones(cTrama);
        cTrama[indxControl] = I_CTRL(nr, ns);
        System.arraycopy(cTrama, 0, nTrama, 0, (indxControl + 9));
        System.arraycopy(cAPDU, 8, nTrama, indxControl + 9, cAPDU.length - 8);
        System.arraycopy(cTrama, cTrama.length - 3, nTrama, nTrama.length - 3, 3);
        nTrama[indxControl + 7] = (byte) (cAPDU.length - 8 + 1);
        nTrama = calcularnuevocrcI(nTrama);
        return nTrama;
    }

    public byte[] reconstruirSetRequestAPDU(int clase, int OBISInternalCod, int methodId) {
        byte cosemDesc[] = tramas.getCosemDescriptor().clone();
        cosemDesc[0] = (byte) 0xC1;
        cosemDesc = asignaInvokeIDandParity(cosemDesc, true);
        cosemDesc = editCosemMethodDescriptor(cosemDesc, clase, PrincipalOBIS.get(OBISInternalCod), methodId);
        return cosemDesc;        
    }

    public byte[] reconstruirGloSetRequest(byte[] cAPDU) {
        byte cTrama[] = tramas.getGloSetRequest().clone();
        cTrama = asignaDirecciones(cTrama);
        System.arraycopy(cAPDU, 8, cTrama, indxControl + 9, cAPDU.length - 8);
        cTrama[indxControl] = I_CTRL(nr, ns);
        cTrama = calcularnuevocrcI(cTrama);
        return cTrama;
    }

    public byte[] reconstruirActionRequestAPDU(int clase, int OBISInternalCod, int methodId, boolean methodInvParam ) {
        byte cosemDesc[] = tramas.getCosemDescriptor().clone();
        cosemDesc[0] = (byte) 0xC3;
        cosemDesc = asignaInvokeIDandParity(cosemDesc, true);
        cosemDesc = editCosemMethodDescriptor(cosemDesc, clase, PrincipalOBIS.get(OBISInternalCod), methodId);
        cosemDesc[cosemDesc.length - 1] = methodInvParam ? (byte) 0x01 : (byte) 0x00;
        return cosemDesc; 
    }

    public byte[] reconstruirGloActionRequest(byte[] cAPDU) {
        byte cTrama[] = tramas.getGloActionRequest().clone();        
        cTrama = asignaDirecciones(cTrama);
        System.arraycopy(cAPDU, 8, cTrama, indxControl + 9, cAPDU.length - 8);
        cTrama[indxControl] = I_CTRL(nr, ns);
        cTrama = calcularnuevocrcI(cTrama);
        return cTrama;
    }

    public byte[] reconstruirGetRequestNormal(int clase, int OBISInternalCod, int methodId) {
        byte trama[] = tramas.getGetRequestNormal().clone();
        trama = asignaDirecciones(trama);
        trama = asignaInvokeIDandParity(trama, false);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = cambiaClassnOBIS(trama, clase, PrincipalOBIS.get(OBISInternalCod), methodId);
        trama = calcularnuevocrcI(trama);
        return trama;
    }

    public byte[] decifrarCAPDU(String[] vectorhex) throws Exception {
        int idxCAPDU = vectorhex[0].equalsIgnoreCase("7E") ? indxSacarDatos + 5 : vectorhex[0].equalsIgnoreCase("CC") ? 4 : 0;
        String SInvCounterStr = ps.encode(SInvCounter, SInvCounter.length);
        ps.escribir("\nServer Invocation Counter: " + SInvCounterStr);
        String[] tempCounter = SInvCounterStr.split(" ");
        for (int i = idxCAPDU; i < vectorhex.length; i++) {
            if (vectorhex[i].equals("30")) {
                boolean condition = vectorhex[i + 1].equalsIgnoreCase(tempCounter[0]) && vectorhex[i + 2].equalsIgnoreCase(tempCounter[1]) && vectorhex[i + 3].equalsIgnoreCase(tempCounter[2]) && vectorhex[i + 4].equalsIgnoreCase(tempCounter[3]);
                if (condition) {
                    idxCAPDU = i + 1;
                    break;
                }
            }
        }
        byte[] cAPDU = new byte[APTitleS.length + vectorhex.length - idxCAPDU - (vectorhex[0].equalsIgnoreCase("7E") ? 3 : 0)];
        System.arraycopy(APTitleS, 0, cAPDU, 0, APTitleS.length);
        int j = APTitleS.length;
        for (int i = idxCAPDU; i < vectorhex.length - (vectorhex[0].equalsIgnoreCase("7E") ? 3 : 0); i++) {//Toma el Inv Counter y la APDU Cifrada
            cAPDU[j] = (byte) Integer.parseInt(vectorhex[i], 16);
            j++;
        }
//        //OJO
//        byte[] cAPDUaux ={ (byte) 0x53, (byte) 0x54, (byte) 0x41, (byte) 0xAC, (byte) 0x10, (byte) 0x00, (byte) 0x0F, (byte) 0xA2, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x87, (byte) 0x32, (byte) 0xBC, (byte) 0xF7, (byte) 0xCA, (byte) 0x2B, (byte) 0x10, (byte) 0x74, (byte) 0xC1, (byte) 0xFC, (byte) 0x06, (byte) 0xB0, (byte) 0xBE, (byte) 0x1A, (byte) 0x2E, (byte) 0x23, (byte) 0x66, (byte) 0xB7, (byte) 0x57, (byte) 0x34, (byte) 0x43, (byte) 0xD4, (byte) 0xB5, (byte) 0xC3, (byte) 0xF8, (byte) 0x76, (byte) 0xB0, (byte) 0x61};
//        cAPDU = cAPDUaux;
        ps.escribir("\nCiphered APDU (Hex): " + ps.encode(cAPDU, cAPDU.length));
        byte[] dAPDU = cipher.decrypt(cAPDU);
        ps.escribir("\nDeciphered APDU (Hex): " + ps.encode(dAPDU, dAPDU.length));
        return dAPDU;
    }

    public void revisarSecurity(String[] vectorhex) throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {
        if (nSession == 1) {
            String peticion = "Contador de Invocación";
            vectorhex = revisarHeaderYcrc(vectorhex, peticion);
            revisarControlByte(vectorhex, peticion);
            if (tramaOK) {
                tramaOK = false;
                if (tipoconexion == 0) {
                    this.IC = (((Integer.parseInt(vectorhex[vectorhex.length - 4], 16) << 24) & 0xFF000000) + ((Integer.parseInt(vectorhex[vectorhex.length - 3], 16) << 16) & 0x00FF0000) + (Integer.parseInt(vectorhex[vectorhex.length - 2], 16) << 8) & 0x0000FF00) + (Integer.parseInt(vectorhex[vectorhex.length - 1], 16) & 0x000000FF);
                } else {
                    this.IC = (((Integer.parseInt(vectorhex[vectorhex.length - 7], 16) << 24) & 0xFF000000) + ((Integer.parseInt(vectorhex[vectorhex.length - 6], 16) << 16) & 0x00FF0000) + (Integer.parseInt(vectorhex[vectorhex.length - 5], 16) << 8) & 0x0000FF00) + (Integer.parseInt(vectorhex[vectorhex.length - 4], 16) & 0x000000FF);
                }
                this.InvCounter[0] = (byte) ((this.IC >> 24) & 0xFF);
                this.InvCounter[1] = (byte) ((this.IC >> 16) & 0xFF);
                this.InvCounter[2] = (byte) ((this.IC >> 8) & 0xFF);
                this.InvCounter[3] = (byte) (this.IC & 0xFF);
//                    this.InvCounter[2] = (byte) (0x00);//OJO
//                    this.InvCounter[3] = (byte) (0x87);//OJO
                try {
                    ps.escribir("Invocation Counter Dec capturado: " + IC);
                    ps.escribir("Invocation Counter Hex capturado: " + Arrays.toString(InvCounter));
                } catch (Exception e) {
                    ps.escribir("Error imprimiendo el Invocation counter");
                }                
                this.nSession = 2;
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        } else if (hasSec && nSession == 2) {
            String peticion = "Current Association";
            vectorhex = revisarHeaderYcrc(vectorhex, peticion);
            revisarControlByte(vectorhex, peticion);
            if (tramaOK) {
                tramaOK = false;
                lsecurity = false;
                try {
                    //Método de revisión o validación del desafío envíado por el medidor.                               
                    byte[] APDU = decifrarCAPDU(vectorhex);
                    SInvCounter = cipher.increaseCounter(SInvCounter);
                    //PENDIENTE
                    //Validar GMAC enviada por el Servidor. 
                } catch (Exception e) {
                    ps.escribir("Error al extraer el contador de invocación");
                } finally {
                    enviaSerial();
                }
            }
        }
    }

    public void revisarSerial(String[] vectorhex) throws Exception {
        String peticion = "Serial";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lserialnumber = false;
            try {
                String[] vectorData = null;
                if (nSession == 1) {
                    vectorData = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    byte[] APDU = decifrarCAPDU(vectorhex);
                    SInvCounter = cipher.increaseCounter(SInvCounter);
                    String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                    vectorData = sAPDU.split(" ");
                }
                boolean continuar;
                interpretaDatos(vectorData, 2);
                datoserial = "" + Long.parseLong(datoserial);
                ps.escribir("Numero serial obtenido: " + datoserial);
                if (Long.parseLong(seriemedidor) == Long.parseLong(datoserial)) {
                    continuar = true;
                } else {
                    ps.escribir("Serial incorrecto");
                    continuar = false;
                }
                if (continuar) {
                    try {
                        if (lacumulados) {
                            enviaAcumulado();
                        } else {
                            enviaFirmware();
                        }
                    } catch (Exception e) {
                        ps.escribir("Error al tratar de enviar petición de " + (lacumulados? "Acumulados" : "Firmware") );
                        ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                        lacumulados = lfirmware = false;
                        lprecierre = true;
                        prereset = false;//Para que se envíe logout y no reinicie comunicación innecesariamente
                        enviaPrelogout();
                    }
                } else {
                    ps.escribir("Numero de serial no valido");
                    lprecierre = true;
                    prereset = false;//Para que se envíe logout y no reinicie comunicación innecesariamente
                    enviaPrelogout();
                }
            } catch (Exception e) {                
                ps.escribir("Error al validar el numero serial");
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                lprecierre = true;
                prereset = false;//Para que se envíe logout y no reinicie comunicación innecesariamente
                enviaPrelogout();
            }
        } 
    }
    
    public void revisarAcumulados(String[] vectorhex) throws Exception {
        String peticion = "Consumos Acumulados";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            try {
                String[] vectorData = null;
                if (nSession == 1) {
                    vectorData = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    byte[] APDU = decifrarCAPDU(vectorhex);
                    SInvCounter = cipher.increaseCounter(SInvCounter);
                    String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                    vectorData = sAPDU.split(" ");
                }
                if (vectorData[6].equals("00")) {
                    String valueStr = extractValue(vectorData);
                    valAcumulados_Tarifas[nTarifa][nOBIS_acumulado - 1] = Long.parseLong(valueStr, 16) / 10000.0;
                } else {
                    ps.escribir("Negación de Petición");
                    valAcumulados_Tarifas[nTarifa][nOBIS_acumulado - 1] = 0.0;
                }
                if (nOBIS_acumulado < 4) {
                    nOBIS_acumulado++;
                    enviaAcumulado();
                } else if (nTarifa < 3) {
                    nOBIS_acumulado = 1;
                    nTarifa++;
                    enviaAcumulado();
                } else {//desconectar
                    SimpleDateFormat sdfAcum = new SimpleDateFormat("yyMMddHHmmss");            
                    fechaAcumulados = new Timestamp(sdfAcum.parse( sdfAcum.format(getDCurrentDate())).getTime());
                    lconsumosAcumulados = false;
                    lprecierre = true; 
                    prereset = false;
                    enviaPrelogout();
                }
            } catch (Exception e) {                
                ps.escribir("Error al extraer el valor acumulado " + nOBIS_acumulado + " con tarifa " + nTarifa);
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                lprecierre = true;
                prereset = false;
                enviaPrelogout();
            }                           
        }             
    }
    
    public void revisarFirmware(String[] vectorhex) throws Exception {
        String peticion = "Firmware";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lfirmware = false;
            try {
                String[] vectorData = null;
                if (nSession == 1) {
                    vectorData =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    byte[] APDU = decifrarCAPDU(vectorhex);
                    SInvCounter = cipher.increaseCounter(SInvCounter);
                    String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                    vectorData = sAPDU.split(" ");
                }
                interpretaDatos(vectorData, 6);
                ps.escribir("Versión de Firmware Obtenido: " + datofirmware);
                try {
                    enviaFechaactual(1);
                } catch (Exception e) {
                    ps.escribir("Error Petición Fecha Actual 1.");
                    ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                    lfechaactual = false;
                    lprecierre = true;
                    prereset = false;
                    enviaPrelogout();
                }                
            } catch (Exception e) {
                ps.escribir("Error al validar la versión de firmware");
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                lprecierre = true;
                prereset = false;
                enviaPrelogout();
            }
        }
    }

    public void revisarPhyAddress(String[] vectorhex) {
        String peticion = "Direccion Fisica";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lphyaddress = false;
            try {
                String[] vectorData = null;
                if (nSession == 1) {
                    vectorData = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    byte[] APDU = decifrarCAPDU(vectorhex);
                    SInvCounter = cipher.increaseCounter(SInvCounter);
                    String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                    vectorData = sAPDU.split(" ");
                }
                interpretaDatos(vectorData, 7);
                ps.escribir(peticion + " obtenida: " + dirfis);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "Interpreta Trama " + peticion, peticion + " obtenida: " + dirfis);
                if (setdirfis) {
                    ps.actualReintento--;
                }                
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            } catch (Exception e) {                
                ps.escribir("Error en interpreta trama " + peticion);
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                lprecierre = true;
                prereset = false;
                enviaPrelogout();
            }
        }
    }

    public void enviaFechaactual(int nfecha) throws Exception {
        lfechaactual = lfechaactual2 = false;
        if (nfecha == 1) {
            lfechaactual = true;
        } else if (nfecha == 2) {
            lfechaactual2 = true;
        }
        byte[] trama;
        if (hasSec && nSession == 2) {
            byte[] APDU = reconstruirGetRequestAPDU(8, 1, 2);//Clase 8, Objeto 1, MethodId 2 - Fecha Actual;
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte[] cAPDU = cipher.encrypt( APDU );
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequestNormal(8, 1, 2);//clase 8, Objeto 1, MethodId - Fecha Actual  
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Solicitud de fecha actual";
        ps.lenviaTrama2 = true;
    }

    public void revisarFecAct(String[] vectorhex) {
        String peticion = "Fecha Actual";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lfechaactual = false;
            try {
                String[] data = null;
                if (nSession == 1) {
                    data =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    byte[] APDU = decifrarCAPDU(vectorhex);
                    SInvCounter = cipher.increaseCounter(SInvCounter);
                    String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                    data = sAPDU.split(" ");
                }
                if (interpretaDatos(data, 3)) {                    
                    if (lconfhora) {
                        try {
                            enviaConfHora();
                        } catch (Exception e) {                            
                            ps.escribir("Error Petición Configuración hora");
                            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                            lfechasync = false;  
                            lprecierre = true;
                            prereset = false;
                            enviaPrelogout();
                        }
                    } else if (ldisconnect || lreconnect) {
                        try {
                            enviaDisc_Conn();
                        } catch (Exception e) {                            
                            ps.escribir("Error Petición de " + (ldisconnect ? "Desconexión" : "Reconexión"));
                            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                            ldisc_rec = false;
                            lprecierre = true;
                            prereset = false;
                            enviaPrelogout();
                        }
                    } else if (lperfil || lregistros) {
                        if (!lperfil) {
                            regisdia = (PrincipalOBIS.get(7).equals("") ? regisdia : true);
                            regismes = (regisdia ? false : (PrincipalOBIS.get(8).equals("") ? regismes : true));
                            if (!regisdia & !regismes) {
                                if (leventos) {
                                    enviaEventos(epp.usarDiasALeerEnEventos);
                                } else {
                                    ps.escribir("No existen objetos OBIS definidos para solicitar registro de consumo diario o mensual");
                                    lprecierre = true;
                                    prereset = false;
                                    enviaPrelogout();
                                }
                            } else {
                                try {
                                    enviaPeriodoDeIntegracion();
                                } catch (Exception e) {
                                    ps.escribir("Error Petición de Periodo de Integración");
                                    ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                                    lperiodoIntegracion = false;
                                    lprecierre = true;
                                    prereset = false;
                                    enviaPrelogout();
                                }
                            }
                        } else {
                            try {
                                enviaPeriodoDeIntegracion();
                            } catch (Exception e) {
                                ps.escribir("Error Petición de Periodo de Integración");
                                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                                lperiodoIntegracion = false;
                                lprecierre = true;
                                prereset = false;
                                enviaPrelogout();
                            }
                        }
                    } else if (leventos) {
                        enviaEventos(epp.usarDiasALeerEnEventos);
                    } else {
                        ps.escribir("No se seleccionó ninguna petición/acción");
                        lprecierre = true;
                        prereset = false;
                        enviaPrelogout();
                    }
                } else {
                    ps.escribir("Error en obtencion de fecha");
                    lprecierre = true;
                    prereset = true;
                    enviaPrelogout();
                }
            } catch (Exception e) {               
                ps.escribir("Error en obtencion de fecha");
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                lprecierre = true;
                prereset = false;
                enviaPrelogout();
            }
        } 
    }
    
    public void enviaDisc_Conn() throws Exception {        
        byte trama[];
        byte dataType[] = { (byte) 0x0F };
        byte params[] = { (byte) 0x00};
        byte APDU[] = concatFrames( reconstruirActionRequestAPDU(70, 10, ldisconnect ? 1 : 2, true), concatFrames ( dataType,  params ) );
        ldisc_rec = true;
        if (hasSec && nSession == 2) {
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte cAPDU[] = cipher.encrypt(APDU);
            trama = reconstruirGloActionRequest(cAPDU);
            trama[indxControl + 7] = (byte) 0x20;
            trama[indxControl] = I_CTRL(nr, ns);
            trama = calcularnuevocrcI(trama);
        } else {
            trama = reconstruirGetRequest(APDU);
        }        
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama =  "=> " + ( ldisconnect ? "Desconexión" : "Reconexión") ;       
        ps.lenviaTrama2 = true;
    }

    
    public void revisarDiscRec( String[] vectorhex) throws Exception {
        String peticion = ldisconnect? "Desconexión" : "Reconexión";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        String word1 = ldisconnect ? "Desconectó" : "Reconectó";
        String word2 = ldisconnect ? "Desconectar" : "Reconectar";
        if (tramaOK) {
            tramaOK = false;
            ldisc_rec = false;
            if (nSession == 1) {
                if (Integer.parseInt(vectorhex[vectorhex.length - ( tipoconexion == 0 ? 1 : 4)], 16) == 0) {
                    ps.escribir("Se " + word1 + " el medidor correctamente");
                } else {
                    ps.escribir("No fue posible " + word2 + " el medidor");
                    prereset = true;
                }
            } else if (hasSec && nSession == 2) {
                byte[] APDU = decifrarCAPDU(vectorhex);
                SInvCounter = cipher.increaseCounter(SInvCounter);
                if ((APDU[APDU.length - 1] & 0xFF) == 0) {
                    ps.escribir("Se " + word1 + " el medidor correctamente");
                } else {
                    ps.escribir("No fue posible " + word2 + " el medidor");
                    prereset = true;
                }
            }
            enviaDisc_ConnState();
        }
    }
    public void enviaDisc_ConnState() throws Exception {        
        byte trama[];
        byte APDU[] = reconstruirGetRequestAPDU(70, 10, 2);
        ldisconnstate = true;
        if (hasSec && nSession == 2) {
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte cAPDU[] = cipher.encrypt(APDU);
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequest(APDU);
        }        
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama =  "=> " + ( "Relay State") ;       
        ps.lenviaTrama2 = true;
    }
    
    public void revisarDisc_ConnState( String[] vectorhex) throws Exception {
        String peticion = "Relay State";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            ldisconnstate = false;
            lprecierre = true;
            if (nSession == 1) {
                if (Integer.parseInt(vectorhex[vectorhex.length - ( tipoconexion == 0 ? 1 : 4)], 16) == 0) {
                    ps.escribir("Estado de relé: Desconectado");
                    prereset=!ldisconnect;
                } else {
                    ps.escribir("Estado de relé: Conectado");
                    prereset=ldisconnect;
                }
            } else if (hasSec && nSession == 2) {
                byte[] APDU = decifrarCAPDU(vectorhex);
                SInvCounter = cipher.increaseCounter(SInvCounter);
                if ((APDU[APDU.length - 1] & 0xFF) == 0) {
                    ps.escribir("Estado de relé: Desconectado");
                    prereset=!ldisconnect;
                } else {
                    ps.escribir("Estado de relé: Conectado");
                    prereset=ldisconnect;
                }                
            }   
            enviaPrelogout();
        }
    }

    public void enviaConfHora() throws ParseException, Exception {       
        byte trama[];
        byte dataType[] = new byte[ epp.isDLMS_useDateTimeFormat() ? 1:2 ];
        byte APDU[];
        byte[] dateParams = new byte[12];
        GregorianCalendar fechaCalendario = new GregorianCalendar();
        fechaCalendario.setTime(Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime()));
        int diaSemana = fechaCalendario.get(Calendar.DAY_OF_WEEK);
        
        //String fecha = sdf.format(new Date(time.getTime()));
        String updateFecha = this.getCurrentDate(sdf);
        ps.escribir("\nFecha a actualizar: " + updateFecha);
        String lfecha = Integer.toHexString(Integer.parseInt(updateFecha.substring(0, 4)));
        while (lfecha.length() < 4) {
            lfecha = "0" + lfecha;
        }
        //Bytes para ubicación de la fecha y hora
        //int indxhora = indxhcs2 + 19;
        dateParams[0] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
        dateParams[1] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
        dateParams[2] = (byte) (Integer.parseInt(updateFecha.substring(4, 6)) & 0xFF);// mes 
        dateParams[3] = (byte) (Integer.parseInt(updateFecha.substring(6, 8)) & 0xFF);//dia
        dateParams[4] = (byte) (((diaSemana - 1) == 0 ? 7 : (diaSemana - 1)) & 0xFF);// dia de la semana
        dateParams[5] = (byte) (Integer.parseInt(updateFecha.substring(8, 10)) & 0xFF); // Hora 
        dateParams[6] = (byte) (Integer.parseInt(updateFecha.substring(10, 12)) & 0xFF); // min
        dateParams[7] = (byte) (Integer.parseInt(updateFecha.substring(12, 14)) & 0xFF); // seg
        dateParams[8] = (byte) 0xFF; // centesimas
        dateParams[9] = (byte) 0x80;
        dateParams[10] = (byte) 0x00; // desviacion 2 bytes
        dateParams[11] = (byte) 0x00; // status
        if ( epp.isDLMS_useDateTimeFormat() ) {
            dataType[0] =  (byte) 0x19;
        } else {
            dataType[0] = (byte) 0x09;
            dataType[1] = (byte) dateParams.length;
        }
        APDU = concatFrames( reconstruirSetRequestAPDU(8, 1, 2), concatFrames ( dataType, dateParams ) );
        if (hasSec && nSession == 2) {
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte cAPDU[] = cipher.encrypt(APDU);
            trama = reconstruirGloSetRequest(cAPDU);
        } else {            
            trama = reconstruirGetRequest(APDU);
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;
        lfechasync = true;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Configuracion de hora " + this.getCurrentDate(sdf3);
        ps.lenviaTrama2 = true;
    }

    public void enviaPeriodoDeIntegracion() throws Exception {
        lperiodoIntegracion = true;
        byte trama[];
        byte APDU[];
        if (regisdia) {
            APDU = reconstruirGetRequestAPDU(7, 7, 4);
        } else if (regismes) {
            APDU = reconstruirGetRequestAPDU(7, 8, 4);
        } else {
            APDU = reconstruirGetRequestAPDU(7, 2, 4);
        }
        if (hasSec && nSession == 2) {
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte cAPDU[] = cipher.encrypt(APDU);
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequest(APDU);
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Solicitud de periodo de integracion";
        ps.lenviaTrama2 = true;
    }

    public void enviaEventos(boolean rango) {
        primerbloque = true;
        lpowerLost = true;
        newBlock = true;
        listEventos = new ArrayList<>();
        byte trama[] = new byte[]{};
        byte APDU[];
        byte[] dateParams = new byte[12];
        String iniDate;
        String finDate;
        String peticion = "Power Quality Request";
        byte cAPDU[];
        if (rango) {
            try {
                ndias = this.calculaDiasALeer(regisdia, regismes, lpowerLost); //calcula ndias
            } catch (Exception e){                
                ps.escribir("Fallo en el cálculo de días de " + peticion + ". Se tomará 30 días como valor por defecto.");
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                ndias = 30;
            }
            iniDate = getSpecificDate(sdf, false, ndias, "D");
            ps.escribir("Fecha inicio de " + peticion + ": " + iniDate);
            finDate = getSpecificDate(sdf, true, 1, "D");
            ps.escribir("Fecha final de " + peticion + ": " + finDate);
            APDU = reconstruirGetRequestAPDU(7, 6, 2);
            try {
                dateParams = this.adecuarFechasFiltro(iniDate, finDate);
                APDU = this.addClockAccessSelection(APDU, dateParams);
            } catch (Exception e){
                ps.escribir("Fallo en la construcción del filtro por rango de" + peticion + ".");
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                resetCheckerBooleans("");
                lprecierre = true;
                prereset = false;
                enviaPrelogout();
                return;
            }            
        } else {
            APDU = reconstruirGetRequestAPDU(7, 6, 2);
        }
        if (hasSec && nSession == 2) {
            try {
                ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
                cAPDU = cipher.encrypt(APDU);
                trama = reconstruirGloGetRequest(cAPDU);
            } catch (Exception e) {                
                ps.escribir("Fallo en encriptación o reconstrucción de Global Get Request de "+peticion);
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                resetCheckerBooleans("");
                lprecierre = true;
                prereset = false;
                enviaPrelogout();
                return;
            }
        } else {
            trama = reconstruirGetRequest(APDU);
        }
        trama = tipoconexion == 0 ? construirTramaGPRS(trama) : trama;
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Solicitud de eventos";
        ps.lenviaTrama2 = true;
    }

    public void revisarConfHora(String[] vectorhex) throws Exception {
        String peticion = "Sincronizacion de reloj";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lfechasync = false;
            lprecierre = true;
            if (nSession == 1) {
                if (Integer.parseInt(vectorhex[ vectorhex.length - ( tipoconexion == 0 ? 1 : 4)], 16) == 0) {//VIC es 15 no 14
                    ps.escribir("Se sincronizó la hora correctamente");
                } else {
                    ps.escribir("No fue posible sincronizar la hora");
                    prereset = true;
                }
            } else if (hasSec && nSession == 2) {
                byte[] APDU = decifrarCAPDU(vectorhex);
                SInvCounter = cipher.increaseCounter(SInvCounter);
                if ((APDU[APDU.length - 1] & 0xFF) == 0) {//VIC es 15 no 14
                    ps.escribir("Se sincronizó la hora correctamente");
                } else {
                    ps.escribir("No fue posible sincronizar la hora");
                    prereset = true;
                }
            }
            enviaPrelogout();
        }
    }

    public void revisarPeriodoInt(String[] vectorhex) throws Exception {

        String peticion = "Periodo de integración";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lperiodoIntegracion = false;
            String[] data = null;
            if (nSession == 1) {
                data =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
            } else if (hasSec && nSession == 2) {
                byte[] APDU = decifrarCAPDU(vectorhex);
                SInvCounter = cipher.increaseCounter(SInvCounter);
                String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                data = sAPDU.split(" ");
            }
            if (interpretaDatos(data, 4)) {
                enviaInfoPerfil();
            } else {
                ps.escribir("Negacion de peticion");
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        }
    }

    public void enviaInfoPerfil() throws Exception {
        linfoperfil = true;
        newBlock = true;
        obis = new ArrayList<>();
        conske = new ArrayList<>();
        unidad = new ArrayList<>();
        clase = new ArrayList<>();
        infoPerfil = new Vector<>();
        primerbloque = true;
        byte trama[];
        byte APDU[];
        if (regisdia) {
            APDU = reconstruirGetRequestAPDU(7, 7, 3);
        } else if (regismes) {
            APDU = reconstruirGetRequestAPDU(7, 8, 3);
        } else {
            APDU = reconstruirGetRequestAPDU(7, 2, 3);
        }
        if (hasSec && nSession == 2) {
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte cAPDU[] = cipher.encrypt(APDU);
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequest(APDU);
        }
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;        
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Solicitud de configuracion de " + (regisdia == true ? "regs dia" : (regismes == true ? "regs mes" : "perfil"));
        ps.lenviaTrama2 = true;
    }

    public void revisarInfoPerfil(String[] vectorhex) throws Exception {
        String peticion = "Canales";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            String vdata[] = null;
            if (primerbloque || completabloque) {
                if (vectorhex[indxSacarDatos].equals("B0") & vectorhex[indxSacarDatos + 1].equals("3F")) {
                    ps.escribir("Recorta formato desconocido: " + vectorhex[indxSacarDatos] + " - " + vectorhex[indxSacarDatos + 1]);
                    vdata = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex, 33) : ps.sacarDatos(vectorhex, indxSacarDatos + 36);
                    bytesRecortados = bytesRecortados + (indxData + 33) - 1;
                } else {
                    if (nSession == 1) {
                        vdata = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                        bytesRecortados = bytesRecortados + indxSacarDatos - 1;
                        primerbloque = false;
                        concatena = true;
                    } else if (hasSec && nSession == 2) {
                        int longi;
                        int recortar;
                        if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                            if (newBlock) {
                                twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                                    blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                if (twoBytesLen) {
                                    String slongi = "";
                                    for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                        slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                    }
                                    blockLen = Integer.parseInt(slongi, 16) + 4;
                                } else {
                                    blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                }
                                currCiphBlock = new String[blockLen];
                                partialLen = vectorhex.length - (indxSacarDatos + 6);
                                System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                                newBlock = false;
                            } else {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                partialLen += vectorhex.length - (indxSacarDatos + 3);
                            }
                            concatena = false;
                        } else {
                            if (!newBlock) {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                            } else {
                                currCiphBlock = vectorhex;
                            }
                            byte[] APDU = decifrarCAPDU(currCiphBlock);
                            SInvCounter = cipher.increaseCounter(SInvCounter);
                            String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                            String[] arraysAPDU = sAPDU.split(" ");
                            if (arraysAPDU[4].equalsIgnoreCase("02")) {
                                longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                                recortar = 12;
                                if (longi >= 128) {
                                    int spare = longi % 128;
                                    recortar = recortar + spare;
                                    longi = /*HERE*/ getPDULength(spare, arraysAPDU);
                                }
                                bytesRecortados = bytesRecortados + recortar;
                                String nuevoVector[] = new String[primerbloque ? longi + recortar + 1 : longi];
                                System.arraycopy(arraysAPDU, primerbloque ? 0 : recortar + 1, nuevoVector, 0, nuevoVector.length);
                                vdata = nuevoVector;
                                vectorDatarecibido = arraysAPDU;
                                save = true;
                            } else {
                                vdata = arraysAPDU;
                                vectorDatarecibido = vdata;
                                save = true;
                            }
                        }
                    }
                }
            } else {
                int longi;
                int recortar;
                if (nSession == 1) {
                    longi = Integer.parseInt(vectorhex[indxData + 9], 16);
                    recortar = indxData + 9;
                    if (longi >= 128) {
                        recortar = recortar + (longi % 128);
                    }
                    bytesRecortados = bytesRecortados + recortar;
                    vdata = ps.sacarDatos(vectorhex, recortar + 1);
                    vectorDatarecibido = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                        if (newBlock) {
                            twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                                blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                            if (twoBytesLen) {
                                String slongi = "";
                                for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                    slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                }
                                blockLen = Integer.parseInt(slongi, 16) + 4;
                            } else {
                                blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                            }
                            currCiphBlock = new String[blockLen];
                            partialLen = vectorhex.length - (indxSacarDatos + 6);
                            System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                            newBlock = false;
                        } else {
                            System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                            partialLen += vectorhex.length - (indxSacarDatos + 3);
                        }
                        concatena = false;
                    } else {
                        if (!newBlock) {
                            System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                        } else {
                            currCiphBlock = vectorhex;
                        }
                        byte[] APDU = decifrarCAPDU(currCiphBlock);
                        SInvCounter = cipher.increaseCounter(SInvCounter);
                        String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                        String[] arraysAPDU = sAPDU.split(" ");
                        longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                        recortar = 12;
                        if (longi >= 128) {
                            int spare = longi % 128;
                            recortar = recortar + spare;
                            longi = getPDULength(spare, arraysAPDU);
                        }
                        bytesRecortados = bytesRecortados + recortar;
                        String nuevoVector[] = new String[longi];
                        System.arraycopy(arraysAPDU, recortar + 1, nuevoVector, 0, nuevoVector.length);
                        vdata = nuevoVector;
                        vectorDatarecibido = arraysAPDU;
                        save = true;
                    }
                }
                concatena = false;
            }

            if (nSession == 1) {
                infoPerfil.addAll(Arrays.asList(vdata));
            } else if (nSession == 2 && hasSec) {
                if (save) {
                    infoPerfil.addAll(Arrays.asList(vdata));
                    primerbloque = false;
                    save = false;
                }
            }
            if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {//vectorhex[1].equals("A9")) {//bloque con segmentación
                ps.escribir("Bloque segmentado");
                // vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                if (concatena) {
                    String nuevoVector[] = new String[vectorDatarecibido.length + vdata.length];
                    System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                    System.arraycopy(vdata, 0, nuevoVector, vectorDatarecibido.length, vdata.length);
                    vectorDatarecibido = nuevoVector;
                }
                completabloque = true;
                enviaRR();
            } else if (completabloque) {
                ps.escribir("Completa bloque");
                completabloque = false;
                if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque una vez lo completa
                    procesaInfoPerfil();
                    if (obis.size() > 0) {
                        String obisaux = obis.get(0);
                        for (int j = 1; j < obis.size(); j++) {
                            obisaux = obisaux + ";" + obis.get(j);
                        }
                    }
                    linfoperfil = false;
                    indexConstant = 0;
                    while (!clase.get(indexConstant).equals("0003")) {
                        conske.add("0");
                        unidad.add("0");
                        indexConstant++;
                    }
                    ps.escribir("Cantidad de canales internos del medidor: " + obis.size());
                    ps.escribir("Vector de canales internos del medidor: " + obis.toString());
                    enviaConstant();
                }
            } else {
                if (concatena) {
                    vectorDatarecibido = vdata;//ojo -18-09-2020
                }
                if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque como se acaba de recibir
                    procesaInfoPerfil();
                    if (obis.size() > 0) {
                        String obisaux = obis.get(0);
                        for (int j = 1; j < obis.size(); j++) {
                            obisaux = obisaux + ";" + obis.get(j);
                        }
                    }
                    linfoperfil = false;
                    indexConstant = 0;
                    while (!clase.get(indexConstant).equals("0003")) {
                        conske.add("0");
                        unidad.add("0");
                        indexConstant++;
                    }
                    ps.escribir("Cantidad de canales internos del medidor: " + obis.size());
                    ps.escribir("Vector de canales internos del medidor: " + obis.toString());
                    enviaConstant();
                }
            }
        }
    }

    private void procesaInfoPerfil() {
        String[] data = infoPerfil.toArray(new String[infoPerfil.size()]);
        interpretaDatos(data, 5);
    }

    public void enviaConstant() throws Exception {
        lconstants = true;
        byte[] trama = null;
        trama = construirConstant(obis.get(indexConstant));
        trama = tipoconexion == 0 ? construirTramaGPRS(trama) : trama;
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Solicitud de constantes " + (indexConstant + 1);
        ps.lenviaTrama2 = true;
    }

    public void revisarConstants(String[] vectorhex) throws Exception {
        String peticion = "Constantes";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            String[] vdata = null;
            if (nSession == 1) {
                vdata =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
            } else if (hasSec && nSession == 2) {
                byte[] APDU = decifrarCAPDU(vectorhex);
                SInvCounter = cipher.increaseCounter(SInvCounter);
                String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                vdata = sAPDU.split(" ");
            }
            interpretaDatos(vdata, 20);
            indexConstant++;
            try {
                while (!clase.get(indexConstant).equals("0003")) {
                    conske.add("0");
                    unidad.add("0");
                    indexConstant++;
                }
            } catch (Exception e) {
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
            }
            if (indexConstant > (obis.size() - 1)) {
                try {
                    time = new Timestamp(getDCurrentDate().getTime()); //
                    deltatimesync1 = new Timestamp(getDCurrentDate().getTime());
                } catch (Exception e) {
                    time = new Timestamp(new Date().getTime()); //
                    deltatimesync1 = new Timestamp(new Date().getTime());
                    ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                }
                ps.escribir("Vector de constantes internas del medidor: " + conske.toString());
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
                try {
                    enviaFechaactual(2);
                } catch (Exception e) {
                    ps.escribir("Error Petición Fecha Actual 2.");
                    ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                    lfechaactual2 = false;
                    lprecierre = true;
                    prereset = false;
                    enviaPrelogout();
                }
            } else {
                //se solicita el siguiente OBIS
                enviaConstant(); //no se lleva indexConstant a cero
            }
        }
    }

    public void revisarFecAct2(String[] vectorhex) throws Exception {
    
        String name = "Fecha Actual";
        vectorhex = revisarHeaderYcrc(vectorhex, name);
        revisarControlByte(vectorhex, name);
        if (tramaOK) {
            tramaOK = false;
            lfechaactual2 = false;
            String[] dataTrama = null;
            String peticion = "";
            try {
                deltatimesync2 = new Timestamp(getDCurrentDate().getTime());
            } catch (Exception e) {
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                deltatimesync2 = new Timestamp(new Date().getTime());
            }            
            if (nSession == 1) {
                dataTrama =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
            } else if (hasSec && nSession == 2) {
                byte[] APDU = decifrarCAPDU(vectorhex);
                SInvCounter = cipher.increaseCounter(SInvCounter);
                String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                dataTrama = sAPDU.split(" ");
            }
            if (interpretaDatos(dataTrama, 3)) {
                ps.escribir("Fecha actual del medidor: " + fechaActual);                
                if (lperfil || lregistros) {//se solicita perfil de carga o registros
                    peticion = (regisdia == true ? "Registros diarios" : (regismes == true ? "Registros mensuales" : "Perfil de carga"));
                    ndias = calculaDiasALeer(regisdia, regismes, lpowerLost); //calcula ndias
                    solicitar = true;
                    try {
                        tsfechaactual = new Timestamp(fechaActual.getTime());
                        ps.escribir("Delta time: " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                        desfase = (time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000;
                        ps.escribir("Diferencia SEG NTP: " + desfase);
                        if (Math.abs(desfase) > ndesfasepermitido) {
                            solicitar = false;
                            ps.escribir("Desfase Permitido: " + ndesfasepermitido);
                            ps.escribir("No se solicitara " + peticion);
                        }
                    } catch (Exception e) {            
                        ps.escribir("Error al calcular desfase, por tanto no se continuará.");
                        ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                        solicitar = false;                        
                    }                    
                    if (solicitar) {
                        try {
                            cp.actualizaDesfase(desfase, med.getnSerie());
                        } catch (Exception e) {
                            ps.escribir("Error al actualizar desfase en DB.");
                            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                        }
                        Date fechaUltLec = new Date(med.getFecha().getTime());
                        String fechaIni = ((regisdia || regismes) ? this.getSpecificDate(sdf, false, ndias, "D") : sdf.format(this.getDSpecificDate(false, 1, "H", fechaUltLec)));
                        enviaPeticionDeBufferConFiltro(fechaIni, peticion);
                    } else {
                        lprecierre = true;
                        prereset = false;
                        ps.escribir("Desfase horario no se solicitara " + peticion);
                        enviaPrelogout();
                    }
                }
            } else {
                ps.escribir("Negacion de peticion");
                lprecierre = true;
                prereset = true;
                enviaPrelogout();
            }
        }
    }

    public void enviaPeticionDeBufferConFiltro(String fechaIni, String peticion) throws Exception {
        byte trama[];
        byte APDU[];
        byte[] dateParams;

        if (regisdia || regismes) {
            lregis = true;
            if (regisdia) {
                vRegistrosdia = new ArrayList<>();
                APDU = reconstruirGetRequestAPDU(7, 7, 2);
            } else {
                vRegistrosmes = new ArrayList<>();
                APDU = reconstruirGetRequestAPDU(7, 8, 2);
            }
        } else {
            vPerfilCarga = new ArrayList<>();
            lperfilcarga = true;
            APDU = reconstruirGetRequestAPDU(7, 2, 2);
        }
        try {
            dateParams = asignaFechasDeFiltroPorRango(fechaIni, peticion);
            APDU = this.addClockAccessSelection(APDU, dateParams);
            if (hasSec && nSession == 2) {
                ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
                byte cAPDU[] = cipher.encrypt(APDU);
                trama = reconstruirGloGetRequest(cAPDU);
            } else {
                trama = reconstruirGetRequest(APDU);
            }
            trama = tipoconexion==0?construirTramaGPRS(trama):trama;
            primerbloque = true;
            newBlock = true;            
            ps.ultimatrama = ps.ultimatramaEnviada = trama;
            ps.descripcionTrama = "Solicitud de " + peticion + " ";
            ps.lenviaTrama2 = true;
        } catch (Exception e) {
            ps.escribir("Error construyendo petición de buffer con filtro.");
            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
            resetCheckerBooleans("");
            lprecierre = true;
            prereset = false;
            enviaPrelogout();
        }

    }

    private byte[] asignaFechasDeFiltroPorRango(String fechaIni, String peticion) throws ParseException {
        String sfechaFin = "";
        ps.escribir("Fecha inicio de " + peticion + ": " + sdf3.format(sdf.parse(fechaIni)));
        if (epp.isUsarRangoEnIntervalos()) {

            Calendar fechaCalendario = new GregorianCalendar();
            fechaCalendario.setTime(new Timestamp(this.getDCurrentDate().getTime()));

            if (fechaCalendario.get(Calendar.MINUTE) % intervalo != 0) {
                fechaCalendario.set(Calendar.MINUTE, fechaCalendario.get(Calendar.MINUTE) - (fechaCalendario.get(Calendar.MINUTE) % intervalo));
            }
            fechaCalendario.set(Calendar.SECOND, 0);
            Long diferencia = ((fechaCalendario.getTimeInMillis() - sdf.parse(fechaIni).getTime()) / ((long) (intervalo) * (60000))) + 1;
            if ((diferencia < epp.rangoEnIntervalos)) {
                fechaFin = this.getDSpecificDate(true, 1, "D");//si la diferencia entre fechas es menor al rango
                sfechaFin = this.getSpecificDate(sdf, true, 1, "D");//si la diferencia entre fechas es menor al rango
            } else {
                //Si la diferencia entre fechas es mayor al rango, se piden los primero intervalos que completan un rango
                fechaFin = this.getDSpecificDate(true, intervalo * epp.rangoEnIntervalos, "M", sdf.parse(fechaIni));
                sfechaFin = this.getSpecificDate(sdf, true, intervalo * epp.rangoEnIntervalos, "M", fechaIni);
            }
        } else {
            if (ndias > 30) {
                fechaFin = this.getDSpecificDate(true, 30, "D", sdf.parse(fechaIni));
                sfechaFin = this.getSpecificDate(sdf, true, 30, "D", fechaIni);
            } else {
                fechaFin = this.getDCurrentDate();
                sfechaFin = sdf.format(this.getDCurrentDate());
            }            
        }
        ps.escribir("Fecha final de " + peticion + ": " + sfechaFin);
        return this.adecuarFechasFiltro(fechaIni, sfechaFin);
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

    public void revisarPerfil(String[] vectorhex) throws Exception {
        String peticion = "Perfil de carga";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlBytePerfil(vectorhex, peticion);
        //revisarTipoDeRespuesta(vectorhex, peticion);
        try {
            if (tramaOK) {
                tramaOK = false;
                lperfilcompleto = true;
                String vdata[] = null;
                if (primerbloque) {
                    vPerfilCargaList.add(new ArrayList<>());
                }
                if (primerbloque || completabloque) {
                    ps.escribir("Primer Bloque o Completa Bloque");
                    if (vectorhex[indxSacarDatos].equals("B0") & vectorhex[indxSacarDatos + 1].equals("3F")) {
                        ps.escribir("Recorta formato desconocido: " + vectorhex[indxSacarDatos] + " - " + vectorhex[indxSacarDatos + 1]);
                        vdata =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex, 33) : ps.sacarDatos(vectorhex, indxSacarDatos + 36);
                        bytesRecortados = bytesRecortados + ( indxData + 33 ) - 1;
                        recortaFormatoP++;
                    } else {
                        if (nSession == 1) {
                            vdata =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                            bytesRecortados = bytesRecortados + indxSacarDatos - 1;
                            primerbloque = false;
                            concatena = true;
                        } else if (hasSec && nSession == 2) {
                            int longi;
                            int recortar;
                            if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                                if (newBlock) {
                                    twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                                    blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                    if(twoBytesLen){
                                        String slongi="";
                                        for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                            slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                        }
                                        blockLen = Integer.parseInt(slongi, 16) + 4;
                                    } else {
                                        blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                    }
                                    currCiphBlock = new String[blockLen];
                                    partialLen = vectorhex.length - (indxSacarDatos + 6);
                                    System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                                    //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                    newBlock = false;
                                } else {
                                    System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                    //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                    partialLen += vectorhex.length - (indxSacarDatos + 3);
                                }
                                concatena = false;
                            } else {
                                if (!newBlock) {
                                    System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                    //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                } else {
                                    currCiphBlock = vectorhex;
                                }
                                byte[] APDU = decifrarCAPDU(currCiphBlock);                                
                                SInvCounter = cipher.increaseCounter(SInvCounter);
                                String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                                String[] arraysAPDU = sAPDU.split(" ");
                                boolean continuar = true;//revisarTipoRespuestaDecifrada(arraysAPDU, peticion);
                                if (continuar) {
                                    if (arraysAPDU[4].equalsIgnoreCase("02")) {
                                        longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                                        recortar = 12;
                                        if (longi >= 128) {
                                            int spare = longi % 128;
                                            recortar = recortar + spare;
                                            longi = getPDULength(spare, arraysAPDU);
                                            ps.escribir("Longitud data del bloque: " + longi);
                                        }
                                        bytesRecortados = bytesRecortados + recortar;
                                        String nuevoVector[] = new String[primerbloque ? longi + recortar + 1 : longi];
                                        System.arraycopy(arraysAPDU, primerbloque ? 0 : recortar + 1, nuevoVector, 0, nuevoVector.length);
                                        vdata = nuevoVector;
                                        vectorDatarecibido = arraysAPDU;
                                        save = true;
                                    } else {
                                        vdata = arraysAPDU;
                                        vectorDatarecibido = vdata;
                                        save = true;
                                    }
                                } else {
                                    terminaPerfil();
                                }
                            }
                        }
                    }
                } else {
                    int longi;
                    int recortar;
                    if (nSession == 1) {
                        longi = Integer.parseInt(vectorhex[indxData + 9], 16);
                        recortar = indxData + 9;
                        if (longi >= 128) {
                            recortar = recortar + (longi % 128);
                        }
                        bytesRecortados = bytesRecortados + recortar;
                        vdata = ps.sacarDatos(vectorhex, recortar + 1);
                        vectorDatarecibido = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);                       
                    } else if (hasSec && nSession == 2) {
                        if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                            if (newBlock) {
                                twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                                blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                if(twoBytesLen){
                                    String slongi="";
                                    for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                        slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                    }
                                    blockLen = Integer.parseInt(slongi, 16) + 4;
                                } else {
                                    blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                }
                                currCiphBlock = new String[blockLen];
                                partialLen = vectorhex.length - (indxSacarDatos + 6);
                                System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                                newBlock = false;
                            } else {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                partialLen += vectorhex.length - (indxSacarDatos + 3);
                            }
                            concatena = false;
                        } else {
                            if (!newBlock) {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                            } else {
                                currCiphBlock = vectorhex;
                            }
                            byte[] APDU = decifrarCAPDU(currCiphBlock);
                            SInvCounter = cipher.increaseCounter(SInvCounter);
                            String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                            String[] arraysAPDU = sAPDU.split(" ");
                            boolean continuar = true;//revisarTipoRespuestaDecifrada(arraysAPDU, peticion);
                            if (continuar) {
                                longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                                recortar = 12;
                                if (longi >= 128) {
                                    int spare = longi % 128;
                                    recortar = recortar + spare;
                                    longi = this.getPDULength(spare, arraysAPDU);
                                }
                                bytesRecortados = bytesRecortados + recortar;
                                String nuevoVector[] = new String[longi];
                                System.arraycopy(arraysAPDU, recortar + 1, nuevoVector, 0, nuevoVector.length);
                                vdata = nuevoVector;
                                vectorDatarecibido = arraysAPDU;
                                save = true;
                            } else {
                                terminaPerfil();
                            }
                        }
                    }
                    concatena = false;
                }
                if (nSession == 1) {
                    vPerfilCargaList.get(idxVPerfil).addAll(Arrays.asList(vdata));
                } else if (nSession == 2 && hasSec) {
                    if (save) {
                        vPerfilCargaList.get(idxVPerfil).addAll(Arrays.asList(vdata));
                        primerbloque = false;
                        save = false;
                    }
                }
                //vPerfilCarga.addAll(Arrays.asList(vdata));
                if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {//vectorhex[1].equals("A9")) {//bloque con segmentación
                    ps.escribir("Bloque segmentado");
                    // vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                    if (concatena) {
                        String nuevoVector[] = new String[vectorDatarecibido.length + vdata.length];
                        System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                        System.arraycopy(vdata, 0, nuevoVector, vectorDatarecibido.length, vdata.length);
                        vectorDatarecibido = nuevoVector;
                    }
                    completabloque = true;
                    enviaRR();
                } else if (completabloque) {
                    ps.escribir("Completa bloque");
                    completabloque = false;
                    if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque una vez lo completa
                        if (!enviaPerfilPorEntradas()) { //se desconoce si es util para este medidor                        
                            //vPerfilCargaList.add(vPerfilCarga);
                            if (epp.isUsarRangoEnIntervalos()) {
                                if (fechaFin.after(this.getDCurrentDate())) {//si la fecha final está en el futuro, ya se terminó el rango
                                    terminaPerfil();
                                } else {
                                    vPerfilCargaList.add(new ArrayList<String>());
                                    idxVPerfil++;
                                    enviaPeticionDeBufferConFiltro(sdf.format(fechaFin), peticion);
                                }
                            } else {
                                terminaPerfil();
                            }
                        }
                    }
                } else {
                    if (concatena) {
                        vectorDatarecibido = vdata;//ojo -18-09-2020
                    }
                    if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque como se acaba de recibir
                        if (!enviaPerfilPorEntradas()) { //se desconoce si es util para este medidor
                            vPerfilCargaList.add(vPerfilCarga);
                            if (epp.isUsarRangoEnIntervalos()) {
                                if (fechaFin.after(this.getDCurrentDate())) {//si la fecha final está en el futuro, ya se terminó el rango
                                    terminaPerfil();
                                } else {
                                    vPerfilCargaList.add(new ArrayList<String>());
                                    idxVPerfil++;
                                    enviaPeticionDeBufferConFiltro(sdf.format(fechaFin), peticion);
                                }
                            } else {
                                terminaPerfil();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ps.escribir("Error revisando Perfil de Carga.");
            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
            resetCheckerBooleans("");
            lperfilcarga = false;
            lprecierre = true;
            prereset = true;
            enviaPrelogout();
        }

    }

    public void terminaPerfil() throws Exception {
        lperfilcompleto = false;
        lperfilcarga = false;
        lregis = false;
        concatena = false;
        bytesRecortados = 0;
        if (lregistros) {
            regisdia = (PrincipalOBIS.get(7).equals("") ? regisdia : true);
            regismes = (regisdia ? false : (PrincipalOBIS.get(8).equals("") ? regismes : true));
            if (!regisdia & !regismes) {
                if (leventos) {
                    enviaEventos(epp.usarDiasALeerEnEventos);
                } else {
                    ps.escribir("No existen objetos OBIS definidos para solicitar registro de consumo diario o mensual");
                    lprecierre = true;
                    prereset = false;
                    enviaPrelogout();               
                }
            } else {
                enviaPeriodoDeIntegracion();
            }
        } else if (leventos) {
            enviaEventos(epp.usarDiasALeerEnEventos);
        } else {
            lprecierre = true;
            prereset = false;
            enviaPrelogout();
        }
    }

    public void revisarControlBytePerfil(String[] vectorhex, String peticion) throws Exception {
        if (tramaOK) {
            tramaOK = false;
            if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x01) == 0x00) { //es informacion
                ps.escribir("NrM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0xE0) >> 5) + "  NsM " + ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0E) >> 1));
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
                    ps.escribir("NsPc " + ns + " NrPc " + nr);
                    tramaOK = true;
                } else {
                    //no son los ns y nr esperados
                    ps.escribir("No son los ns y nr esperados");
                    handleForwarding(peticion);
                }
            } else if ((Integer.parseInt(vectorhex[indxControl], 16) & 0x0F) == 0x01) {//es rr
                lperfilcompleto = false;
                if (lperfilcarga) {
                    lperfilcarga = false;
                    if (lregistros) {
                        regisdia = (PrincipalOBIS.get(7).equals("") ? regisdia : true);
                        regismes = (regisdia ? false : (PrincipalOBIS.get(8).equals("") ? regismes : true));
                        if (!regisdia & !regismes) {
                            if (leventos) {
                                enviaEventos(epp.usarDiasALeerEnEventos);
                            } else {
                                ps.escribir("No existen objetos OBIS definidos para solicitar registro de consumo diario o mensual");
                                lprecierre = true;
                                prereset = false;
                                enviaPrelogout();
                            }
                        } else {
                            enviaPeriodoDeIntegracion();
                        }
                    } else if (leventos) {
                        enviaEventos(epp.usarDiasALeerEnEventos);
                    } else {
                        lprecierre = true;
                        prereset = false;
                        enviaPrelogout();
                    }
                } else if (lregis) {
                    lregis = false;
                    if (leventos) {
                        enviaEventos(epp.usarDiasALeerEnEventos);
                    } else {
                        lprecierre = true;
                        prereset = false;
                        enviaPrelogout();
                    }
                }
            } else {
                if (vectorhex[indxControl].equals("73")) {
                    handleForwarding(peticion);
                } else {
                    validarTipoTrama(vectorhex[indxControl]);
                }
            }
        }
    }

    /*
    public void revisarTipoDeRespuesta(String[] vectorhex, String peticion) throws Exception {
        if (tramaOK) {
            tramaOK = false;
            if ((Integer.parseInt(vectorhex[indxData], 16) & 0xFF) == 0xCC) {
                tramaOK = true;
            } else if ((Integer.parseInt(vectorhex[indxData], 16) & 0xFF) == 0xC4 || completabloque) { //es get response
                if (!completabloque | primerbloque) {
                    if ((Integer.parseInt(vectorhex[indxData + 1], 16) & 0xFF) == 0x01 & (Integer.parseInt(vectorhex[indxData + 3], 16) & 0xFF) == 0x01) { // es get response normal con data access result
                        //obtiene data access result y reenvia solicitud
                        revisaDataAccessResult(Integer.parseInt(vectorhex[indxData + 4], 16));
                        ps.descripcionTrama = "=> Envia Solicitud " + peticion;
                        ps.lenviaTrama2 = true;
                    } else {
                        tramaOK = true;
                    }
                } else {
                    tramaOK = true;
                }            
            } else {
                if ((Integer.parseInt(vectorhex[indxData], 16) & 0xFF) == 0xD8) {// ExceptionResponse
                    ps.escribir("Exception Response");
                    if(Integer.parseInt(vectorhex[indxData + 1], 16)==2){
                        ps.escribir("service-unknown");
                    } else {
                        revisaServiceError(Integer.parseInt(vectorhex[indxData + 2], 16) & 0xFF);    
                    }
                    
                } else {
                    ps.escribir("Respuesta no esperada: desconocida");
                }
                if (tipoconexion != 0) {
                    //System.out.println("\n validando trama");
                    if (vectorhex[indxControl].equals("73")) {
                        // ps.enviaTramaUltima(ps.ultimatramaEnviada, "=> Envia Solicitud "+peticion);
                        ps.descripcionTrama = "=> Envia Solicitud " + peticion;
                        ps.lenviaTrama2 = true;
                    } else {
                        validarTipoTrama(vectorhex[indxControl]);
                    }
                }
                //Que pasa si es Wrapper, debo reiniciar,  o reeenviar la trama, y cuantas veces?               
            }
        } else {
            //System.out.println("Trama incorrecta");
        }
    }
    
    private boolean revisarTipoRespuestaDecifrada(String[] vectorhex, String peticion) {
        if ((Integer.parseInt(vectorhex[3], 16) & 0xFF) == 0xC4 || completabloque) { //es get response
            if ((Integer.parseInt(vectorhex[4], 16) & 0xFF) == 0x01 & (Integer.parseInt(vectorhex[6], 16) & 0xFF) == 0x01) { // es get response normal con data access result
                //obtiene data access result y reenvia solicitud
                revisaDataAccessResult(Integer.parseInt(vectorhex[7], 16));
                return false;
            } else {
                return true;
            }
        } else {
            if ((Integer.parseInt(vectorhex[3], 16) & 0xFF) == 0xD8) {// ExceptionResponse
                ps.escribir("Exception Response");
                if (Integer.parseInt(vectorhex[4], 16) == 2) {
                    ps.escribir("service-unknown");
                } else {
                    revisaServiceError(Integer.parseInt(vectorhex[5], 16) & 0xFF);
                }
            } else {
                ps.escribir("Respuesta no esperada: desconocida");
            }
            return false;
        }
    }

    
    
     public void revisaServiceError(int data){
        switch (data) {                
            case 1: 
                ps.escribir("ServiceError: operation-not-possible");
                break;
                
            case 2: 
                ps.escribir("ServiceError: service-not-supported");
                break;
                
            case 3: 
                ps.escribir("ServiceError: other-reason");
                break;

            case 4: 
                ps.escribir("ServiceError: pdu-too-long");
                break;
            
            case 5: 
                ps.escribir("ServiceError: deciphering-error");
                break;
            
            case 6: 
                ps.escribir("ServiceError: invocation-counter-error");
                break;
            
            default: // 
                ps.escribir("ServiceError: NO DEFINIDO");
                break;
        }
    }
    
    public void revisaDataAccessResult(int data){
        switch (data) {
            case 0: 
                ps.escribir("Data Access Result: success");
                break;
                
            case 1: 
                ps.escribir("Data Access Result: hardware-fault");
                break;
                
            case 2: 
                ps.escribir("Data Access Result: temporary-failure");
                break;
                
            case 3: 
                ps.escribir("Data Access Result: read-write-denied");
                break;

            case 4: 
                ps.escribir("Data Access Result: object-undefined");
                break;
            
            case 9: 
                ps.escribir("Data Access Result: object-class-inconsistent");
                break;
            
            case 11: 
                ps.escribir("Data Access Result: object-unavailable");
                break;
            
            case 12: 
                ps.escribir("Data Access Result: type-unmatched");
                break;
            
            case 13: 
                ps.escribir("Data Access Result: scope-of-access-violated");
                break;
             
            case 14: 
                ps.escribir("Data Access Result: data-block-unavailable");
                break;
            
            case 15: 
                ps.escribir("Data Access Result: long-get-aborted");
                break;
            
            case 16: 
                ps.escribir("Data Access Result: no-long-get-in-progress");
                break;
            
            case 17: 
                ps.escribir("Data Access Result: long-set-aborted");
                break;
            
            case 18: 
                ps.escribir("Data Access Result: no-long-set-in-progress");
                break;
            
            case 19: 
                ps.escribir("Data Access Result: data-block-number-invalid");
                break;
            
            case 250: 
                ps.escribir("Data Access Result: other-reason");
                break;
               
            default: // 
                ps.escribir("Data Access Result: NO DEFINIDO");                
                break;
        }
    }        
    */
    //Se desconoce si es util para este medidor, aún
    public boolean enviaPerfilPorEntradas() {
        boolean enviaPerfilEntradas = true;
        // if(vectorhex[16].equals("01")&vectorhex[17].equals("00")&(0<entradasEnUso)){
        // byte trama[] = tramas.getPerfilporentradas().clone();
        // int numentradas = (ndias*24*60*60)/(periodoIntegracion*60);
        // int nentradasfin = entradasEnUso;
        // //entradasEnUso-numentradas es el limite inicial
        //// System.out.println("Entradas debido a rango: " + numentradas);
        // if(entradasEnUso < numentradas){
        //  numentradas = entradasEnUso-1;  
        //  nentradasfin = 0;
        ////  System.out.println("Entradas a pedir: "+entradasEnUso);
        // }else{
        //// System.out.println("Entradas a pedir: "+numentradas);
        // }
        // String nentradasini = Integer.toHexString(entradasEnUso - numentradas).toUpperCase();
        // while (nentradasini.length() < 8) {
        //       nentradasini = "0" + nentradasini;
        //   }
        // trama[29] = (byte) (Integer.parseInt(nentradasini.substring(0, 2), 16) & 0xFF);
        // trama[30] = (byte) (Integer.parseInt(nentradasini.substring(2, 4), 16) & 0xFF);
        // trama[31] = (byte) (Integer.parseInt(nentradasini.substring(4, 6), 16) & 0xFF);
        // trama[32] = (byte) (Integer.parseInt(nentradasini.substring(6, 8), 16) & 0xFF);

        // //entradasEnUso es el limite final
        // String nentradas = Integer.toHexString(nentradasfin).toUpperCase();
        // while (nentradas.length() < 8) {
        //       nentradas = "0" + nentradas;
        //   }
        // trama[34] = (byte) (Integer.parseInt(nentradas.substring(0, 2), 16) & 0xFF);
        // trama[35] = (byte) (Integer.parseInt(nentradas.substring(2, 4), 16) & 0xFF);
        // trama[36] = (byte) (Integer.parseInt(nentradas.substring(4, 6), 16) & 0xFF);
        // trama[37] = (byte) (Integer.parseInt(nentradas.substring(6, 8), 16) & 0xFF);
        // vPerfilCarga = new ArrayList<String>();
        // trama = asignaDireciones(trama);
        // primerbloque = true;
        // trama[indxControl] = I_CTRL(nr, ns);//VIC
        // trama = calcularnuevocrcI(trama);
        // ps.ultimatramaEnviada = trama;
       // 
        // ps.enviaTrama2(trama, "Solicitud de perfil de carga - filtro por entradas");
        // } else {
        enviaPerfilEntradas = false;
        // }
        return enviaPerfilEntradas;
    }

    public void enviaRR() {
        byte trama[] = tramas.getRR().clone();
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
        // ps.enviaTrama2(trama, "=> Envia Recieved Ready RR");
        ps.ultimatrama = ps.ultimatramaEnviada = trama;
        ps.descripcionTrama = "=> Envia Recieved Ready RR";
        ps.lenviaTrama2 = true;
    }

    public void revisarRegistros(String[] vectorhex) throws Exception {
        String peticion = (regisdia == true ? "Registros diarios" : "Registros mensuales");
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlBytePerfil(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lperfilcompleto = true;
            String vdata[] = null;
            if (primerbloque || completabloque) {
                if (vectorhex[indxSacarDatos].equals("B0") & vectorhex[indxSacarDatos + 1].equals("3F")) {
                    ps.escribir("Recorta formato desconocido: " + vectorhex[indxSacarDatos] + " - " + vectorhex[indxSacarDatos + 1]);
                    vdata = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex, 33) : ps.sacarDatos(vectorhex, indxSacarDatos + 36);
                    bytesRecortados = bytesRecortados + (indxData + 33) - 1;
                    recortaFormatoR++;
                } else {
                    if (nSession == 1) {
                        vdata =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                        bytesRecortados = bytesRecortados + indxSacarDatos - 1;
                        primerbloque = false;
                        concatena = true;
                    } else if (hasSec && nSession == 2) {
                        int longi;
                        int recortar;
                        if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                            if (newBlock) {
                                twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                                blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                if(twoBytesLen){
                                    String slongi="";
                                    for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                        slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                    }
                                    blockLen = Integer.parseInt(slongi, 16) + 4;
                                } else {
                                    blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                }
                                currCiphBlock = new String[blockLen];
                                partialLen = vectorhex.length - (indxSacarDatos + 6);
                                System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                                //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                newBlock = false;
                            } else {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                partialLen += vectorhex.length - (indxSacarDatos + 3);
                            }
                            concatena = false;
                        } else {
                            if (!newBlock) {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                            } else {
                                currCiphBlock = vectorhex;
                            }
                            byte[] APDU = decifrarCAPDU(currCiphBlock);
                            SInvCounter = cipher.increaseCounter(SInvCounter);
                            String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                            String[] arraysAPDU = sAPDU.split(" ");
                            longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                            recortar = 12;
                            if (longi >= 128) {
                                int spare = longi % 128;
                                recortar = recortar + spare;
                                longi = getPDULength(spare, arraysAPDU);
                                ps.escribir("Longitud data del bloque: " + longi);
                            }
                            bytesRecortados = bytesRecortados + recortar;
                            String nuevoVector[] = new String[primerbloque ? longi + recortar + 1 : longi];
                            System.arraycopy(arraysAPDU, primerbloque ? 0 : recortar + 1, nuevoVector, 0, nuevoVector.length);
                            vdata = nuevoVector;
                            vectorDatarecibido = arraysAPDU;
                            save = true;                            
                        }
                    }
                }
            } else {
                int longi;
                int recortar;
                if (nSession == 1) {
                    longi = Integer.parseInt(vectorhex[indxData + 9], 16);
                    recortar = indxData + 9;
                    if (longi >= 128) {
                        recortar = recortar + (longi % 128);
                    }
                    bytesRecortados = bytesRecortados + recortar;
                    vdata = ps.sacarDatos(vectorhex, recortar + 1);
                    vectorDatarecibido = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                        if (newBlock) {
                            twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                            blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                            if(twoBytesLen){
                                String slongi="";
                                for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                    slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                }
                                blockLen = Integer.parseInt(slongi, 16) + 4;
                            } else {
                                blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                            }
                            currCiphBlock = new String[blockLen];
                            partialLen = vectorhex.length - (indxSacarDatos + 6);
                            System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                            newBlock = false;
                        } else {
                            System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                            partialLen += vectorhex.length - (indxSacarDatos + 3);
                        }
                        concatena = false;
                    } else {
                        if (!newBlock) {
                            System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                        } else {
                            currCiphBlock = vectorhex;
                        }
                        byte[] APDU = decifrarCAPDU(currCiphBlock);
                        SInvCounter = cipher.increaseCounter(SInvCounter);
                        String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                        String[] arraysAPDU = sAPDU.split(" ");
                        longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                        recortar = 12;
                        if (longi >= 128) {
                            int spare = longi % 128;
                            recortar = recortar + spare;
                            longi = getPDULength(spare, arraysAPDU);
                        }
                        bytesRecortados = bytesRecortados + recortar;
                        String nuevoVector[] = new String[longi];
                        System.arraycopy(arraysAPDU, recortar + 1, nuevoVector, 0, nuevoVector.length);
                        vdata = nuevoVector;
                        vectorDatarecibido = arraysAPDU;
                        save = true;
                    }
                }
                concatena = false;
            }

            if (regisdia) {
                if (nSession == 1) {
                    vRegistrosdia.addAll(Arrays.asList(vdata));
                } else if (nSession == 2 && hasSec) {
                    if (save) {
                        vRegistrosdia.addAll(Arrays.asList(vdata));
                        primerbloque = false;
                        save = false;
                    }
                }
            } else if (regismes) {
                if (nSession == 1) {
                    vRegistrosmes.addAll(Arrays.asList(vdata));
                } else if (nSession == 2 && hasSec) {
                    if (save) {
                        vRegistrosmes.addAll(Arrays.asList(vdata));
                        primerbloque = false;
                        save = false;
                    }
                }
            }
            if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {//vectorhex[1].equals("A9")) {//bloque con segmentación
                ps.escribir("Bloque segmentado");
                // vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                if (concatena) {
                    String nuevoVector[] = new String[vectorDatarecibido.length + vdata.length];
                    System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                    System.arraycopy(vdata, 0, nuevoVector, vectorDatarecibido.length, vdata.length);
                    vectorDatarecibido = nuevoVector;
                }
                completabloque = true;
                enviaRR();
            } else if (completabloque) {
                ps.escribir("Completa bloque");
                completabloque = false;
                if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque una vez lo completa
                    lperfilcompleto = false;
                    lregis = false;
                    regisdia = false;
                    if (regismes) {
                        regismes = false;
                    } else {
                        regismes = (PrincipalOBIS.get(8).equals("") ? regismes : true);
                    }
                    concatena = false;
                    bytesRecortados = 0;
                    if (!regismes) {
                        if (leventos) {
                            enviaEventos(epp.usarDiasALeerEnEventos);
                        } else {
                            lprecierre = true;
                            prereset = false;
                            enviaPrelogout();
                        }
                    } else {
                        enviaPeriodoDeIntegracion(); //pide todos los datos para registros mensuales (regismes en true)
                    }
                }
            } else {
                if (concatena) {
                    vectorDatarecibido = vdata;//ojo -18-09-2020
                }
                if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque como se acaba de recibir
                    lperfilcompleto = false;
                    lregis = false;
                    regisdia = false;
                    if (regismes) {
                        regismes = false;
                    } else {
                        regismes = (PrincipalOBIS.get(8).equals("") ? regismes : true);
                    }
                    concatena = false;
                    bytesRecortados = 0;
                    if (!regismes) {
                        if (leventos) {
                            enviaEventos(epp.usarDiasALeerEnEventos);
                        } else {
                            lprecierre = true;
                            prereset = false;
                            enviaPrelogout();
                        }
                    } else {
                        enviaPeriodoDeIntegracion(); //pide todos los datos para registros mensuales (regismes en true)
                    }
                }
            }
        } 
    }

    public void revisarEventos(String[] vectorhex) throws ParseException, Exception {
        String peticion = "Eventos";
        vectorhex = revisarHeaderYcrc(vectorhex, peticion);
        revisarControlByte(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            String[] vectorData = null;
            if (primerbloque || completabloque) {//completabloque cuando se recibe un bloque con 
                if (vectorhex[indxSacarDatos].equals("B0") & vectorhex[indxSacarDatos + 1].equals("3F")) {
                    ps.escribir("Recorta formato desconocido: " + vectorhex[indxSacarDatos] + " - " + vectorhex[indxSacarDatos + 1]);
                    vectorData = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex, 33) : ps.sacarDatos(vectorhex, indxSacarDatos + 36);
                    bytesRecortados = bytesRecortados + (indxData + 33) - 1;
                    recortaFormatoE++;
                } else {
                    if (nSession == 1) {
                        vectorData =  tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);                            
                        bytesRecortados = bytesRecortados + indxSacarDatos - 1;
                        primerbloque = false;
                        concatena = true;
                    } else if (hasSec && nSession == 2) {
                        int longi;
                        int recortar;
                        if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                            if (newBlock) {
                                twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                                blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                if(twoBytesLen){
                                    String slongi="";
                                    for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                        slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                    }
                                    blockLen = Integer.parseInt(slongi, 16) + 4;
                                } else {
                                    blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                                }
                                currCiphBlock = new String[blockLen];
                                partialLen = vectorhex.length - (indxSacarDatos + 6);
                                System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                                //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                newBlock = false;
                            } else {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                                partialLen += vectorhex.length - (indxSacarDatos + 3);
                            }
                            concatena = false;
                        } else {
                            if (!newBlock) {
                                System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                                //System.out.println("Current Cipher Block: " + Arrays.toString(currCiphBlock));
                            } else {
                                currCiphBlock = vectorhex;
                            }
                            byte[] APDU = decifrarCAPDU(currCiphBlock);
                                SInvCounter = cipher.increaseCounter(SInvCounter);
                                String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                                String[] arraysAPDU = sAPDU.split(" ");
                                if (arraysAPDU[4].equalsIgnoreCase("02")){
                                    longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                                    recortar = 12;
                                    if (longi >= 128) {
                                        int spare = longi % 128;
                                        recortar = recortar + spare;
                                        longi = getPDULength(spare, arraysAPDU);
                                        //System.out.println("Longitud data del bloque: " + longi);
                                    }
                                    bytesRecortados = bytesRecortados + recortar;
                                    String nuevoVector[] = new String[primerbloque ? longi + recortar + 1 : longi];
                                    System.arraycopy(arraysAPDU, primerbloque ? 0 : recortar + 1, nuevoVector, 0, nuevoVector.length);
                                    vectorData = nuevoVector;
                                    vectorDatarecibido = arraysAPDU;
                                    save = true;
                                } else {
                                    vectorData = arraysAPDU;
                                    vectorDatarecibido = vectorData;
                                    save = true;
                                }
                        }
                    }
                }
            } else {
                int longi;
                int recortar;
                if (nSession == 1) {
                    longi = Integer.parseInt(vectorhex[indxData + 9], 16);
                    recortar = indxData + 9;
                    if (longi >= 128) {
                        recortar = recortar + (longi % 128);
                    }
                    bytesRecortados = bytesRecortados + recortar;
                    vectorData = ps.sacarDatos(vectorhex, recortar + 1);
                    vectorDatarecibido = tipoconexion == 0 ? ps.sacarDatosWrapper(vectorhex) : ps.sacarDatos(vectorhex, indxSacarDatos);
                } else if (hasSec && nSession == 2) {
                    if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {
                        if (newBlock) {
                            twoBytesLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128) != 0;
//                            blockLen = twoBytesLen ? (Integer.parseInt(vectorhex[indxSacarDatos + 5] + vectorhex[indxSacarDatos + 6], 16)) + 4 : (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                            if(twoBytesLen){
                                String slongi="";
                                for (int k = 1; k <= (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16) % 128); k++) {
                                    slongi = slongi + vectorhex[indxSacarDatos + 4 + k];
                                }
                                blockLen = Integer.parseInt(slongi, 16) + 4;
                            } else {
                                blockLen = (Integer.parseInt(vectorhex[indxSacarDatos + 4], 16)) + 2;
                            }
                            currCiphBlock = new String[blockLen];
                            partialLen = vectorhex.length - (indxSacarDatos + 6);
                            System.arraycopy(vectorhex, indxSacarDatos + 3, currCiphBlock, 0, partialLen);
                            newBlock = false;
                        } else {
                            System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                            partialLen += vectorhex.length - (indxSacarDatos + 3);
                        }
                        concatena = false;
                    } else {
                        if (!newBlock) {
                            System.arraycopy(vectorhex, indxSacarDatos, currCiphBlock, partialLen, vectorhex.length - (indxSacarDatos + 3));
                        } else {
                            currCiphBlock = vectorhex;
                        }
                        byte[] APDU = decifrarCAPDU(currCiphBlock);
                        SInvCounter = cipher.increaseCounter(SInvCounter);
                        String sAPDU = "E6 E7 00 " + ps.encode(APDU, APDU.length);
                        String[] arraysAPDU = sAPDU.split(" ");
                        longi = Integer.parseInt(arraysAPDU[12], 16) & 0xFF;
                        recortar = 12;
                        if (longi >= 128) {
                            int spare = longi % 128;
                            recortar = recortar + spare;
                            longi = getPDULength(spare, arraysAPDU);
                        }
                        bytesRecortados = bytesRecortados + recortar;
                        String nuevoVector[] = new String[longi];
                        System.arraycopy(arraysAPDU, recortar + 1, nuevoVector, 0, nuevoVector.length);
                        vectorData = nuevoVector;
                        vectorDatarecibido = arraysAPDU;
                        save = true;
                    }
                }
                concatena = false;
            }
            if (nSession == 1) {
                listEventos.addAll(Arrays.asList(vectorData));
            } else if (nSession == 2 && hasSec) {
                if (save) {
                    listEventos.addAll(Arrays.asList(vectorData));
                    primerbloque = false;
                    save = false;
                }
            }
            //listEventos.addAll(Arrays.asList(vectorData));
            if ((Integer.parseInt(vectorhex[1], 16) & 0x08) == 0x08) {//vectorhex[1].equals("A9")) {//bloque con segmentación
                ps.escribir("Bloque segmentado");
                // vectorDatarecibido = vectorDatarecibido + vectorData; //concatena las datas de las tramas (anterior + actual)
                if (concatena) {
                    String nuevoVector[] = new String[vectorDatarecibido.length + vectorData.length];
                    System.arraycopy(vectorDatarecibido, 0, nuevoVector, 0, vectorDatarecibido.length);
                    System.arraycopy(vectorData, 0, nuevoVector, vectorDatarecibido.length, vectorData.length);
                    vectorDatarecibido = nuevoVector;//Lo que hace es completar el bloque de capa de aplicación 
                }
                completabloque = true;
                enviaRR();
            } else if (completabloque) {
                ps.escribir("Completa bloque");
                completabloque = false;
                if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque una vez lo completa
                    lpowerLost = false;
                    lprecierre = true;
                    prereset = false;
                    concatena = false;
                    enviaPrelogout();
                }
            } else {
                if (concatena) {
                    vectorDatarecibido = vectorData;//ojo -18-09-2020
                }
                if (!enviaREQ_NEXT(vectorDatarecibido)) {//pide el bloque como se acaba de recibir
                    lpowerLost = false;
                    lprecierre = true;
                    prereset = false;
                    concatena = false;
                    enviaPrelogout();
                }
            }
        }
    }

    public void enviaPrelogout() {
        byte trama[] = tramas.getPrelogout().clone();        
        trama = asignaDirecciones(trama);
        trama[indxControl] = I_CTRL(nr, ns);
        trama = calcularnuevocrcI(trama);
        trama = tipoconexion==0?construirTramaGPRS(trama):trama;
        ps.ultimatrama = trama;
        ps.descripcionTrama = "preLogout";
        ps.lenviaTrama2 = true;
    }

    public void revisarPrelogout(String[] vectorhex) {
        String peticion = "preLogout";
        revisarHeaderYcrc(vectorhex, peticion);
        if (tramaOK) {
            lprecierre = false;
            tramaOK = false;
            if (resetWithoutLogout) {
                resetWithoutLogout = false;
                ps.escribir("Inicia procesamiento de contingencia en caso de datos a procesar");
                almacenaDatos();
                resetCheckerBooleans("");
                nSession=nSession==2?1:nSession;
                ps.reiniciaComunicacion(true);
            } else {
                ps.escribir("Preparado para cerrar sesion");
                if (prereset) {
                    prereset = false;
                    lReset = true;
                } else {
                    lcierrapuertos = true;
                }
                enviaLogout();
            }
        }
    }

    public void enviaLogout() {
        if(tipoconexion!=0){
            byte trama[] = tramas.getLogout().clone();            
            trama = asignaDirecciones(trama);
            trama = calcularnuevocrcRR(trama);
            ps.ultimatrama = trama;
            ps.descripcionTrama = "Logout";
            ps.lenviaTrama2 = true;
        } else {
            revisarLogout(new String[0]);
        }
    }

    public void revisarLogout(String[] vectorhex) {
        String peticion = "Logout";
        String aviso = "";
        tramaOK = false;
        lcierrapuertos = false;
        ps.cerrarPuerto(false);
        almacenaDatos();
        resetCheckerBooleans("");
        boolean l = false;
        if (!lperfil && !leventos && !lacumulados) {
            l = true;
        }
        if (lconfhora) {
            l = true;
        }
        if (l || (vlec != null) || (listEventos != null) || (fechaAcumulados != null )) {
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
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPdlms", "Verificación estado de lectura - " + aviso);
            ps.escribir("Estado Lectura " + aviso);
            ps.cerrarLog("Leido", true);
        } else {
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPdlms", "Verificación estado de lectura - No Leído");            
            ps.escribir("Estado Lectura No leido");
            ps.cerrarLog("No leido", false);
        }
        ps.leer = false;
        ps.desbloqueo = true;
    }

    public void revisarReset(String[] vectorhex) {
        String peticion = "Logout para reinicio";
        revisarHeaderYcrc(vectorhex, peticion);
        if (tramaOK) {
            tramaOK = false;
            lReset = false;
            if (hasSec) {
                nSession=nSession==2&!lsecurity?1:nSession;
                lsecurity = false;
                enviaSNRM1();
            } else {
                ps.escribir("Inicia procesamiento de contingencia en caso de datos a procesar");
                almacenaDatos();
                resetCheckerBooleans("");
                ps.reiniciaComunicacion(false);
            }
        }
    }

    //calculos para construcción o interpretación de tramas
    private byte[] calcularnuevocrcRR(byte[] siguientetrama) {
        siguientetrama[indxLength] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
        byte[] data = new byte[siguientetrama.length - 4];
        for (int i = 0; i < data.length; i++) {
            data[i] = siguientetrama[i + 1];
        }
        int crc = ProcesosSesion.calculoFCS(data);
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
        siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
        siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);

        return siguientetrama;
    }

    private byte[] asignaDirecciones(byte[] trama) { //dirfis, dirlog y numBytesDir es global

        ArrayList<String> tr = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tr.add(Integer.toHexString(trama[i] & 0xFF).toUpperCase()); //copia los primeros tres bytes
        }
        //agrega la dirección del medidor
        tr = seleccionaNumBytesDir(tr);
        //agrega la dirección del usuario
        indxuser = (Broadcast ? (epp.users.length > 1 ? 1 : 0) : 0);
        String userDir = Integer.toHexString(((epp.users[indxuser] * 2) + 1) & 0xFF).toUpperCase();
        while (userDir.length() < 2) {
            userDir = "0" + userDir;
        }
        tr.add(userDir);
        for (int i = 5; i < trama.length; i++) {
            tr.add(Integer.toHexString(trama[i] & 0xFF).toUpperCase()); //copia los siguientes bytes de la trama
        }
        //copia tr a arreglo de bytes
        byte[] trbyte = new byte[tr.size()];
        int i = 0;
        for (String t : tr) {
            trbyte[i] = (byte) (Integer.parseInt(t, 16) & 0xFF);
            i++;
        }
        return trbyte;
    }

    private ArrayList<String> seleccionaNumBytesDir(ArrayList<String> tr) {
        //casos número de bytes //se agregan los bytes de la dirección del medidor
        //System.out.println("numBytesDir: " + numBytesDir);
        switch (numBytesDir) {
            case 1: {
                //sólo se tiene dirección lógica y se desplaza con uno a la izquierda
                String sdirlog = Integer.toHexString(((dirlog * 2) + 1) & 0xFF).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                tr.add(sdirlog);
                break;
            }
            case 2: {
                //Se tiene dirección lógica en la parte alta y física en la baja
                //La lógica se desplaza con cero a la izquierda
                String sdirlog = Integer.toHexString((dirlog * 2) & 0xFF).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                tr.add(sdirlog);
                //la física se desplaza con uno a la izquierda
                String sdirfis = Integer.toHexString(((dirfis * 2) + 1) & 0xFF).toUpperCase();
                while (sdirfis.length() < 2) {
                    sdirfis = "0" + sdirfis;
                }
                tr.add(sdirfis);
                break;
            }
            case 4: {
                //Se tiene dirección lógica en la parte alta y física en la baja
                //La parte alta y baja de la lógica se desplaza con cero a la izquierda
                String sdirlog = Integer.toHexString(((dirlog * 2) & 0xFF00) >> 7).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                tr.add(sdirlog);
                sdirlog = Integer.toHexString(((dirlog * 2) & 0xFF)).toUpperCase();
                while (sdirlog.length() < 2) {
                    sdirlog = "0" + sdirlog;
                }
                tr.add(sdirlog);
                //la parte alta de la física se desplaza con cero a la izquierda y la baja con uno
                String sdirfis = Integer.toHexString(((dirfis * 2) & 0xFF00) >> 7).toUpperCase();
                while (sdirfis.length() < 2) {
                    sdirfis = "0" + sdirfis;
                }
                tr.add(sdirfis);
                sdirfis = Integer.toHexString(((dirfis * 2) + 1) & 0xFF).toUpperCase();
                while (sdirfis.length() < 2) {
                    sdirfis = "0" + sdirfis;
                }
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

    public byte[] calcularnuevocrcI(byte[] siguientetrama) {
        try {
            siguientetrama[indxLength] = (byte) (Integer.parseInt(Integer.toHexString(siguientetrama.length - 2), 16) & 0xFF);
            byte[] data = new byte[indxControl];
            for (int i = 0; i < data.length; i++) {
                data[i] = siguientetrama[i + 1];
            }
            int crc = ProcesosSesion.calculoFCS(data);
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
            //System.out.println("\nNuevo HCF: " + stxcrc);
            siguientetrama[indxhcs1] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF); //21-10-19
            siguientetrama[indxhcs2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
            data = new byte[siguientetrama.length - 4];
            for (int i = 0; i < data.length; i++) {
                data[i] = siguientetrama[i + 1];
            }
            crc = ProcesosSesion.calculoFCS(data);
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
            //System.out.println("\nNuevo FCS: " + stxcrc);
            siguientetrama[siguientetrama.length - 3] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
            siguientetrama[siguientetrama.length - 2] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return siguientetrama;
    }

    public boolean validacionCRCHCS(String[] data) {
        boolean lcrc = false;
        byte b[] = new byte[indxControl];
        for (int j = 0; j < b.length; j++) {
            b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
        }
        int crc = ProcesosSesion.calculoFCS(b);
        String stx = data[indxhcs1] + "" + data[indxhcs2];
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
        int crc = ProcesosSesion.calculoFCS(b);
        String stx = data[data.length - 3] + "" + data[data.length - 2];
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
        if (stx.equals(stxcrc)) {
            lcrc = true;
        }
        return lcrc;
    }

    private byte[] cambiaClassnOBIS(byte[] trama, int clase, String sOBIS, int methodId) { //0100010800FF
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
        //Method Id
        trama[indxControl + 17] = (byte) (Integer.parseInt(Integer.toHexString(methodId), 16) & 0xFF);

        return trama;
    }

    private byte[] editCosemMethodDescriptor(byte[] trama, int clase, String OBIS, int methodId) {

        //clase
        trama[3] = (byte) ((Integer.parseInt(Integer.toHexString(clase), 16) >> 8) & 0xFF);
        trama[4] = (byte) (Integer.parseInt(Integer.toHexString(clase), 16) & 0xFF);
        //A
        trama[5] = (byte) (Integer.parseInt(OBIS.substring(0, 2), 16) & 0xFF);
        //B
        trama[6] = (byte) (Integer.parseInt(OBIS.substring(2, 4), 16) & 0xFF);
        //C
        trama[7] = (byte) (Integer.parseInt(OBIS.substring(4, 6), 16) & 0xFF);
        //D
        trama[8] = (byte) (Integer.parseInt(OBIS.substring(6, 8), 16) & 0xFF);
        //E
        trama[9] = (byte) (Integer.parseInt(OBIS.substring(8, 10), 16) & 0xFF);
        //F
        trama[10] = (byte) (Integer.parseInt(OBIS.substring(10, 12), 16) & 0xFF);
        //Method Id
        trama[11] = (byte) (Integer.parseInt(Integer.toHexString(methodId), 16) & 0xFF);
        return trama;

    }

    private byte[] asignaInvokeIDandParity(byte[] trama, boolean inAPDU) {
        int posInvokeIDandParity;
        if (inAPDU) {
            posInvokeIDandParity = 2;
        } else {
            posInvokeIDandParity = indxControl + 8;
        }

        trama[posInvokeIDandParity] = (byte) (Integer.parseInt(Integer.toHexString(InvokeIDandParity), 16) & 0xFF);
        if (sumaInvokeIDnP) {
            //Para algunos medidores el invonke ID and Parity incrementa de 0x_n a 0x_F
            InvokeIDandParity++;
            if (InvokeIDandParity == (epp.DLMS_InvokeIDandParity & 0xF0) + 0x0F + 1) { //revisa si ya llegó a 0x_F+1
                InvokeIDandParity = epp.DLMS_InvokeIDandParity;//toma el inicial
            }
        }
        return trama;
    }

    public List[] buildAARQFrame(int nFrame, boolean condition, ArrayList<String> trama, ArrayList<Byte> data2Cipher, byte[] value) {
        byte flag;
        switch (nFrame) {
            case 1: // ACSE Protocol Version
                for (int i = 0; i <= 3; i++) {
                    trama.add(Integer.toHexString(tramas.getAARQACSEProtocolV()[i] & 0xFF).toUpperCase());
                }
                //System.out.println("ACSE Protocol Version: " + trama);
                break;
            case 2: //appContextName
                for (int i = 0; i <= (condition ? 10 : 9); i++) {
                    trama.add(Integer.toHexString(tramas.getAARQContextName()[i] & 0xFF).toUpperCase());
                }
                if (!condition) {
                    trama.add(Integer.toHexString(value[value.length - 1] & 0xFF).toUpperCase());
                }
                //System.out.println("App Context Name: " + trama);
                break;
            case 3: //calledAPTitle
                trama.add(Integer.toHexString(tramas.getaARQCalledAPTitle()[0] & 0xFF).toUpperCase());//A2
                //Cantidad + valor
                if (!condition) {
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                } else {
                    for (int i = 1; i <= 4; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                }
                //System.out.println("Called AP Title: " + trama);
                break;
            case 4: //calledAEQualifier
                for (int i = 0; i <= (condition ? 6 : 2); i++) {//A3 05 A1 + Cantidad + Valor
                    trama.add(Integer.toHexString(tramas.getAARQContextName()[i] & 0xFF).toUpperCase());
                }
                if (!condition) {
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                }
                //System.out.println("Called AE Qualifier: " + trama);
                break;
            case 5: //calledAPInvID
                break;
            case 6: //calledAEInvID
                break;
            case 7: //callingAPTitle
                int auxAPTitle;
                for (int i = 0; i <= (condition ? 11 : 3); i++) {
                    auxAPTitle = !condition && i == 1 ? (value.length + 2) & 0XFF : !condition && i == 3 ? (value.length & 0xFF) : (tramas.getAARQApTitle()[i] & 0XFF);
                    trama.add(Integer.toHexString(auxAPTitle).toUpperCase());
                }
                if (!condition) {
                    APTitleC = value;
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                }
                //System.out.println("Calling AP Title: " + trama);
                break;
            case 8: //callingdAEQualifier
                break;
            case 9: //callingAPInvID
                break;
            case 10: // callingAEInvID
                break;
            case 11: //ACSE Requirements
                if (!condition) {
                    for (int i = 0; i <= 3; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                } else {
                    for (int i = 0; i <= 3; i++) {
                        trama.add(Integer.toHexString(tramas.getAARQAcse()[i] & 0xFF).toUpperCase());
                    }
                }
                //System.out.println("ACSE Requirements: " + trama);
                break;
            case 12: //Mechanism Nam
                for (int i = 0; i <= (condition ? 8 : 7); i++) {
                    trama.add(Integer.toHexString(tramas.getAARQMechName()[i] & 0xFF).toUpperCase());
                }
                if (!condition) {
                    trama.add(Integer.toHexString(value[value.length - 1] & 0xFF).toUpperCase());
                }
                //System.out.println("Mechanism Name: " + trama);
                break;
            case 13: //AuthenticationValue
                boolean sizeAuth = (tramas.getAARQAuthentication()[3] & 0xFF) == (condition ? password.getBytes().length : value.length);
                for (int i = 0; i <= 3; i++) {
                    if (sizeAuth) {
                        trama.add(Integer.toHexString(tramas.getAARQAuthentication()[i] & 0XFF).toUpperCase());
                    } else {
                        switch (i) {
                            case 1:
                                trama.add(Integer.toHexString(((condition ? password.getBytes().length : value.length) + 2) & 0XFF).toUpperCase());
                                break;
                            case 3:
                                trama.add(Integer.toHexString((condition ? password.getBytes().length : value.length) & 0XFF).toUpperCase());
                                break;
                            default:
                                trama.add(Integer.toHexString(tramas.getAARQAuthentication()[i] & 0XFF).toUpperCase());
                                break;
                        }
                    }
                }
                if (!condition) {
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                } else {
                    byte[] pwd = password.getBytes();
                    for (int i = 0; i <= pwd.length - 1; i++) {
                        trama.add(Integer.toHexString(pwd[i] & 0xFF).toUpperCase());
                    }
                }
                //System.out.println("Authentication Value: " + trama);
                break;
            case 14: //implementationInfo
                break;
            case 15: //User Info                 
                if (!condition) {
                    for (int i = 0; i <= 3; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                    }
                } else {
                    for (int i = 0; i <= 3; i++) {
                        trama.add(Integer.toHexString(tramas.getAARQUsrInfo()[i] & 0xFF).toUpperCase());
                    }
                }
                trama.add(Integer.toHexString(0x01 & 0xFF).toUpperCase()); //XDLMS-APDU CHOICE Initiate Request
                data2Cipher.add((byte) (0x01 & 0xFF));
                //System.out.println("User Info: " + trama);
                break;
            case 16: // Dedicated Key
                flag = 0x00;
                if (value == null) {
                    trama.add(Integer.toHexString(flag & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                } else {
                    flag = 0x01;
                    trama.add(Integer.toHexString((flag) & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                    trama.add(Integer.toHexString(value.length & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (value.length & 0xFF));
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (value[i] & 0xFF));
                    }
                }
                //System.out.println("Dedicated Key: " + trama);
                break;
            case 17: // Response Allowed
                flag = 0x00;
                if (value == null) {
                    trama.add(Integer.toHexString(flag & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                } else {
                    flag = 0x01;
                    trama.add(Integer.toHexString((flag) & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                    trama.add(Integer.toHexString(value.length & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (value.length & 0xFF));
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (value[i] & 0xFF));
                    }
                }
                //System.out.println("Response Allowed: " + trama);
                break;
            case 18: //Proposed QoS
                flag = 0x00;
                if (value == null) {
                    trama.add(Integer.toHexString(flag & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                } else {
                    flag = 0x01;
                    trama.add(Integer.toHexString((flag) & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                    trama.add(Integer.toHexString(value.length & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (value.length & 0xFF));
                    for (int i = 0; i <= value.length - 1; i++) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (value[i] & 0xFF));
                    }
                }
                //System.out.println("Proposed QoS: " + trama);
                break;
            case 19: // Proposed DLMS Version
                flag = 0x00;
                if (value == null) {
                    trama.add(Integer.toHexString(flag & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (flag & 0xFF));
                } else {
                    if (!condition) {
                        trama.add(Integer.toHexString(value[0] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (value[0] & 0xFF));
                    } else {
                        trama.add(Integer.toHexString(6 & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (6 & 0xFF));
                    }
                }
                //System.out.println("Proposed DLMS: " + trama);
                break;
            case 20: //Proposed Conformance 
                byte[] tagASN1 = {(byte) 0x5F, (byte) 0x1F, (byte) 0x04, (byte) 0x00}; //ASN 1 Explicit Tag
                for (int i = 0; i <= 3; i++) {
                    trama.add(Integer.toHexString(tagASN1[i] & 0xFF).toUpperCase());
                    data2Cipher.add((byte) (tagASN1[i] & 0xFF));
                }
                for (int i = 0; i <= 2; i++) {
                    if (!condition) {
                        trama.add(Integer.toHexString(value[i] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (value[i] & 0xFF));
                    } else {
                        trama.add(Integer.toHexString(tramas.getAARQConformance()[i] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (tramas.getAARQConformance()[i] & 0xFF));
                    }
                }
                //System.out.println("Proposed Conformance: " + trama);
                break;
            case 21: //Client Max Rx Pdu Size
                for (int i = 0; i <= 1; i++) {
                    if (!condition) {
                        trama.add(Integer.toHexString(value[i] & 0xFF));
                        data2Cipher.add((byte) (value[i] & 0xFF));
                    } else {
                        trama.add(Integer.toHexString(tramas.getAARQMaxPDU()[i] & 0xFF).toUpperCase());
                        data2Cipher.add((byte) (tramas.getAARQMaxPDU()[i] & 0xFF));
                    }
                }
                //System.out.println("Client Max Rx Pdu Size: " + trama);
                break;
        }
        return new List[]{trama, data2Cipher};
    }

    private byte[] crearAARQ() {
        LinkedHashMap<String, byte[]> camposAARQ = epp.getCamposAARQ();
        byte[] tramabyte = null;
        int nField = 0;
        int posUserInfo = 0;
        try {
            //// System.out.println("Construye trama aarq");                 
            ArrayList<String> trama = new ArrayList<>();
            ArrayList<Byte> data2Cipher = new ArrayList<>();
            byte[] plainText;
            byte[] cipherText;
            List[] data;
            int posLenPDU;
            for (int i = 0; i <= 12; i++) { //inicio de trama hasta length
                trama.add(Integer.toHexString(tramas.getAARQIni()[i] & 0xFF).toUpperCase());
            }
            //System.out.println("\nnumByteDir: " + numBytesDir);
            posLenPDU = trama.size() - 1;
            //System.out.println("posLenPDU: " + posLenPDU);
            Set<String> keys = camposAARQ.keySet();
            for (String key : keys) {
                //System.out.println("Campo: " + key);
                nField += 1;
                //System.out.println("Número de Campo: " + nField);
                if (nField == 14) {
                    posUserInfo = trama.size() - 1;
                }
                byte[] value = camposAARQ.get(key);
                if (value != null) {
                    boolean condition = value[0] == (byte) 0x01 && value.length == 1;
                    if (!(nField == 13 && condition && password.length() == 0)) {
                        data = buildAARQFrame(nField, condition, trama, data2Cipher, value);
                        ////System.out.println(data[0]);
                        trama = (ArrayList<String>) data[0];
                        data2Cipher = (ArrayList<Byte>) data[1];
                    } else {
                        //System.out.println("No usa campo de Autenticacion");
                        ps.escribir("No usa campo de Autenticacion");
                    }
                } else if (value == null && nField >= 15 && nField <= 18) {
                    data = buildAARQFrame(nField, false, trama, data2Cipher, null);
                    trama = (ArrayList<String>) data[0];
                    data2Cipher = (ArrayList<Byte>) data[1];
                }
            }
            int totalSize = trama.size();
            int lengthUsrInfo = totalSize - (posUserInfo + 1) - 4;//Length of total user info
            //System.out.println("Total Size: " + totalSize);
            //System.out.println("Longitud información de usuario: " + lengthUsrInfo);
            //System.out.println("Trama hasta el momento: " + trama);
            if (this.nSession == 1) {
                trama.set(posUserInfo + 4, Integer.toHexString(lengthUsrInfo).toUpperCase());
                trama.set(posUserInfo + 2, Integer.toHexString(lengthUsrInfo + 2).toUpperCase());
                for (int i = 0; i <= 2; i++) { // Final de trama FCS + Flag(7E)
                    trama.add(Integer.toHexString(tramas.getAARQFin()[i] & 0xFF).toUpperCase());
                }
            } else if (nSession == 2 && hasSec) {
                //System.out.println("AARQ Sesión 2 - Seguridad");
                trama.set(posUserInfo + 4, Integer.toHexString(lengthUsrInfo + 19).toUpperCase());
                trama.set(posUserInfo + 2, Integer.toHexString(lengthUsrInfo + 21).toUpperCase());
                trama.set(posUserInfo + 5, Integer.toHexString(33).toUpperCase());
                trama.set(posUserInfo + 6, Integer.toHexString(lengthUsrInfo + 17).toUpperCase());
                trama.set(posUserInfo + 7, Integer.toHexString(48).toUpperCase());
                cipher = new AES_GCM(EK_HLS, AK_HLS, APTitleC, InvCounter);
                plainText = new byte[data2Cipher.size()];
                for (int i = 0; i < data2Cipher.size(); i++) {
                    plainText[i] = data2Cipher.get(i);
                }
             ////   System.out.println("plainText: " + Arrays.toString(plainText));
                cipherText = cipher.encrypt(plainText);
                byte[] cipherTextN = new byte[cipherText.length - 8];
                System.arraycopy(cipherText, 8, cipherTextN, 0, cipherTextN.length);
                //System.out.println("CipherText: " + Arrays.toString(cipherTextN));
                int startCD = posUserInfo + 8;
                int idx = startCD;
                for (byte datoC : cipherTextN) {
                    //System.out.println("Trama hasta el momento: " + trama);
                    //System.out.println("Idx: " + idx);
                    if (idx < +trama.size()) {
                        trama.set(idx, Integer.toHexString(datoC & 0xFF));
                        idx++;
                    } else {
                        trama.add(Integer.toHexString(datoC & 0xFF));
                        idx++;
                    }
                }
                for (int i = 0; i <= 2; i++) { // Final de trama FCS + Flag(7E)
                    trama.add(Integer.toHexString(tramas.getAARQFin()[i] & 0xFF).toUpperCase());
                }
            }
            int lenFrame = trama.size() - 4;
            trama.set(1, Integer.toHexString(160 + ((lenFrame >> 8) & 0x07)));
            trama.set(2, Integer.toHexString(lenFrame & 0xFF));
            trama.set(posLenPDU, Integer.toHexString(lenFrame - posLenPDU));
            tramabyte = new byte[trama.size()];
            int i = 0;
            for (String t : trama) {
                tramabyte[i] = (byte) (Integer.parseInt(t, 16) & 0xFF);
                i++;
            }
        } catch (Exception e) {
            System.err.println(e.getStackTrace()[0]);
            e.printStackTrace();
        }
        return tramabyte;
    }

    private byte[] construirConstant(String obis) throws Exception {
        //System.out.println("\nOBIS a solicitar: " + obis);
        epp.editDLMS_PrincipalOBIS(9, obis);
        byte trama[];
        byte APDU[] = reconstruirGetRequestAPDU(3, 9, 3);
        if (hasSec && nSession == 2) {
            ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
            byte cAPDU[] = cipher.encrypt(APDU);
            trama = reconstruirGloGetRequest(cAPDU);
        } else {
            trama = reconstruirGetRequest(APDU);
        }
        return trama;
    }

    public boolean enviaREQ_NEXT(String[] vectorhex) {
        boolean enviaREQ = true;
        try {
            vectorDatarecibido = new String[0];
            int indxAux = (tipoconexion == 0 ? 1 : 4);
            if (vectorhex[indxAux].equals("02") & vectorhex[indxAux + 2].equals("00")) {
                byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[indxAux + 3], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[indxAux + 4], 16) & 0xFF),
                    (byte) (Integer.parseInt(vectorhex[indxAux + 5], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[indxAux + 6], 16) & 0xFF)};
                byte[] trama;
                if (hasSec && nSession == 2) {
                    byte APDU[] = insertReqNextBlock(tramas.getGetReqNextBlock().clone(), bloqueRecibido);
                    APDU = asignaInvokeIDandParity(APDU, true);
                    ps.escribir( "APDU sin Cifrar: " + tramas.encode( APDU, APDU.length ) );
                    byte cAPDU[] = cipher.encrypt(APDU);
                    trama = reconstruirGloGetRequest(cAPDU);
                } else {
                    trama = crearREQ_NEXT(bloqueRecibido);
                }
                trama = (tipoconexion == 0 ? construirTramaGPRS(trama) : trama);
                ps.ultimatrama = ps.ultimatramaEnviada = trama;
                ps.descripcionTrama = "=> Envia Next Data Block Request";
                ps.lenviaTrama2 = true;
                newBlock = true;
            } else {
                enviaREQ = false;
            }
        } catch (Exception e) {
            ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
            enviaREQ = false;
        }
        return enviaREQ;
    }

    private byte[] crearREQ_NEXT(byte[] bloqueRecibido) {
        byte trama[] = tramas.getREQ_NEXT().clone();
        trama = asignaDirecciones(trama);
//        trama = asignaInvokeIDandParity(trama); //Asigna el Invoke ID and Parity con el que envió la solicitud que generó respuesta en bloques
        trama[indxControl + 8] = (byte) (Integer.parseInt(Integer.toHexString(InvokeIDandParity - 1), 16) & 0xFF);
        trama[indxControl] = I_CTRL(nr, ns);
        System.arraycopy(bloqueRecibido, 0, trama, indxControl + 9, bloqueRecibido.length);
        trama = calcularnuevocrcI(trama);
        return trama;
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
                ps.escribir("Frame Reject. Desconexion");  
                break;
            case "1F":
                ps.escribir("Estado Lectura No leido");                
                break;
            default:
                ps.escribir("Trama no reconocida");
                break;                                     
        }
        ps.reiniciaComunicacion(true);  
    }
    

    public void resetCheckerBooleans(String exceptChecker) {
        lSNRMUA = exceptChecker.equalsIgnoreCase("lSNRMUA");
        lARRQ = exceptChecker.equalsIgnoreCase("lAARQ");
        lsecurity = exceptChecker.equalsIgnoreCase("lsecurity");
        lpowerLost = exceptChecker.equalsIgnoreCase("lpowerLost");
        lserialnumber = exceptChecker.equalsIgnoreCase("lserialnumber");
        lfirmware = exceptChecker.equalsIgnoreCase("lfirmware");
        lphyaddress = exceptChecker.equalsIgnoreCase("lphyaddress");
        lfechaactual = exceptChecker.equalsIgnoreCase("lfechaactual");
        lperiodoIntegracion = exceptChecker.equalsIgnoreCase("lperiodoIntegracion");
        linfoperfil = exceptChecker.equalsIgnoreCase("linfoperfil");
        lconstants = exceptChecker.equalsIgnoreCase("lconstants");
        lfechaactual2 = exceptChecker.equalsIgnoreCase("lfechaactual2");
        lperfilcarga = exceptChecker.equalsIgnoreCase("lperfilcarga");
        lregis = exceptChecker.equalsIgnoreCase("lregis");
        lprecierre = exceptChecker.equalsIgnoreCase("lprecierre");
        lcierrapuertos = exceptChecker.equalsIgnoreCase("lcierrapuertos");
        lReset = exceptChecker.equalsIgnoreCase("lReset");
        lfechasync = exceptChecker.equalsIgnoreCase("lfechasync");
        ldisc_rec = exceptChecker.equalsIgnoreCase("ldisc_rec");
        ldisconnstate = exceptChecker.equalsIgnoreCase("ldisconnstate");
        lconsumosAcumulados = exceptChecker.equalsIgnoreCase("lconsumosAcumulados");
    }

    public boolean almacenaDatos() {
        boolean l = false;        
        if (lperfil) {
            for (ArrayList<String> vPerfilCargaAux : vPerfilCargaList) {
                vPerfilCarga = vPerfilCargaAux;
                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {                    
                    registrosAprocesar = registrosprocesados = 0;
                    recortaFormato = recortaFormatoP;
                    procesaPerfil();
                    ps.escribir("Registros: " + registrosAprocesar + " - Registros procesados: " + registrosprocesados + " - Recorte: " + recortaFormato);
                    if (registrosprocesados == registrosAprocesar - recortaFormato) {
                        procesaIncompletoPerfil = false;
                    } else {
                        procesaIncompletoPerfil = procesaIncompleto;
                    }
                    try {
                        if (vlec.size() > 0) {                            
                            cp.actualizaLectura(vlec, ps.getFile(), new Timestamp(med.getFecha().getTime() - (long) 86400000));
                            l = true;
                            ps.escribir("Fecha Intervalo: "+fechaintervalo);
                            if (fechaintervalo != null) {
                                if (fechaintervalo.after(med.getFecha())) {//med.getFecha())) {
                                    cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                                    ps.escribir("Actualiza fecha: " + sdf3.format(new Date(fechaintervalo.getTime())));
                                } else {
                                    ps.escribir("No actualiza fecha - " + sdf3.format(new Date(fechaintervalo.getTime())));
                                }
                            }                            
                            ps.escribir("Almacena perfil");
                        } else {                            
                            ps.escribir("Sin datos de perfil para almacenar");
                        }
                    } catch (Exception e) {
                        ps.escribir("Error procesando perfil de carga");
                        ps.escribir(ps.getErrorString(e.getStackTrace(), 4));
                    }
                } else {                    
                    ps.escribir("Sin datos de perfil para almacenar");
                }
            }//
        }
        if (lregistros) {
            if ((vRegistrosdia != null && vRegistrosdia.size() > 0) || (vRegistrosmes != null && vRegistrosmes.size() > 0)) {
                try {                    
                    registrosAprocesar = registrosprocesados = 0;
                    procesaRegistros();
                    procesaIncompletoRegistros = procesaIncompleto;
                    if (vreg.size() > 0) {
                        l = true;
                        Timestamp fecha = new Timestamp(Calendar.getInstance().getTimeInMillis());
                        cp.actualizaRegistro(vreg, sgflect.format(new Date(fecha.getTime())));                        
                        ps.escribir("Almacena registros");
                    } else {                        
                        ps.escribir("Sin datos de registros para almacenar");
                    }
                } catch (Exception e) {
                    ps.escribir("Error procesando registros");
                    ps.escribir(ps.getErrorString(e.getStackTrace(), 4));
                }
            } else {                
                ps.escribir("Sin datos de registros para almacenar");
            }
        }
        if (leventos) {
            if (listEventos != null && listEventos.size() > 0) {
                try {                    
                    registrosAprocesar = registrosprocesados = 0;
                    recortaFormato = recortaFormatoE;
                    procesaInfoEventos();
                    l = true;
                    ps.escribir("Registros de Eventos: " + registrosAprocesar + " - Registros procesados: " + registrosprocesados + " - Recorte: " + recortaFormatoE);
                    registrosprocesadosE = registrosprocesados;
                    if (registrosprocesados == registrosAprocesar - recortaFormato) {
                        procesaIncompletoEventos = false;
                    } else {
                        procesaIncompletoEventos = procesaIncompleto;
                    }                    
                    ps.escribir("Almacena eventos");
                } catch (Exception e) {
                    ps.escribir("Error procesando eventos");
                    ps.escribir(ps.getErrorString(e.getStackTrace(), 4));                  
                }
            } else {                
                ps.escribir("Sin datos de eventos para almacenar");
            }
        }
        if (lacumulados) {
            try {
                ps.escribir("Procesando Acumulados");
                procesaAcumulados();
            } catch (Exception e) {
                ps.escribir("Error procesando Consumos Acumulados");
                ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
            }
            
        }
        return l;
    }

    private void procesaPerfil() {
        constantesmatch = 0;
        for (int i = 0; i < obis.size(); i++) {
            if (cp.buscarConskeLong(lconske, Long.parseLong(obis.get(i), 16)) != null) {
                constantesmatch++;
            }
        }
        ps.escribir("Número de constantes encontradas en configuración de perfil: " + constantesmatch);
        String[] data = vPerfilCarga.toArray(new String[vPerfilCarga.size()]);
        //Toma los backups de la info de los canales y los pasa a los genéricos
        obis = obisPerfil;
        conske = conskePerfil;
        unidad = unidadPerfil;
        clase = clasePerfil;
        elecaux = new ElecturaAux();
        ultimoIntervalo = null;//limpia timestamp
        interpretaDatos(data, 22);
    }

    private void procesaRegistros() {
        constantesmatch = 0;
        for (int i = 0; i < obis.size(); i++) {
            if (cp.buscarConskeLong(lconske, Long.parseLong(obis.get(i), 16)) != null) {
                constantesmatch++;
            }
        }
        //System.out.println("Número de constantes encontradas en configuración de registros: " + constantesmatch);
        ps.escribir("Número de constantes encontradas en configuración de registros: " + constantesmatch);
        if (vRegistrosdia.size() > 0) {
            String[] datardia = vRegistrosdia.toArray(new String[vRegistrosdia.size()]);
            tiporegistros = "0"; //diarios es 0
            vtiporegistros = cp.obtenerTipoRegistros(med.getMarcaMedidor().getCodigo(), tiporegistros, null); //consulta los canales de los registros mensuales
            intervalo = intervaloRdia;
            //Toma los backups de la info de los canales y los pasa a los genéricos
            obis = obisRdia;
            conske = conskeRdia;
            unidad = unidadRdia;
            clase = claseRdia;
            elecaux = new ElecturaAux();
            interpretaDatos(datardia, 23);
        } else if (vRegistrosmes.size() > 0) {
            String[] datarmes = vRegistrosmes.toArray(new String[vRegistrosmes.size()]);
            tiporegistros = "1"; //mensual es 1
            vtiporegistros = cp.obtenerTipoRegistros(med.getMarcaMedidor().getCodigo(), tiporegistros, null); //consulta los canales de los registros mensuales
            intervalo = intervaloRmes;
            //Toma los backups de la info de los canales y los pasa a los genéricos
            obis = obisRmes;
            conske = conskeRmes;
            unidad = unidadRmes;
            clase = claseRmes;
            elecaux = new ElecturaAux();
            interpretaDatos(datarmes, 23);
        } else {
            ps.escribir("Sin registros para almacenar");
        }

    }

    public void procesaInfoEventos() throws Exception {
        String[] arrayEventos = listEventos.toArray(new String[listEventos.size()]);
        interpretaDatos(arrayEventos, 24);
    }

    private void procesaAcumulados() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                ps.escribir("Registro acumulado: " + (j + 1) + ", tarifa: " + i + "-> Fecha: " + fechaAcumulados + ", Valor: " + valAcumulados_Tarifas[i][j]);
            }
        }
        cp.almacenarAcumulados(valAcumulados_Tarifas, fechaAcumulados, obisEnergias_Acumuladas, unidadesEnergias_Acumuladas, seriemedidor, ps.getFile());
    }
    
    //procesamiento de la data 
    public boolean interpretaDatos(String[] trama, int tipoTrama) {
        vlec = new Vector<>();
        vreg = new ArrayList<>();
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
            if (clasificarTrama(trama)) { //Para una respuesta sin bloques i1 queda en el byte de inicio de data 0x00, para una respuesta con bloques queda en el inicio del arreglo 0x01.
                //También guarda 1 en la posición 0 de la pila y aumenta la posición de la pila a 1. 
                while (i1 < trama.length) {
                    while (pila[0] > 0) {
                        // if (clasificarTrama(trama)) {
                        almacenar = false;
                        if (j1 == 10) {
                            //System.out.println("Error en la Pila de datos");
                            //System.out.println("----------------------------");
                            correcto = false;
                            break;
                        }
                        if (revisarIndice(i1, trama.length)) { //verifica que el índice no esté desbordado. 
                            //Verifica que el byte corresponda a un arreglo, estructura o si su tamaño está codificado. 
                            revisar = !(Integer.parseInt(trama[i1], 16) == 1 || Integer.parseInt(trama[i1], 16) == 2);
                            try {
                                if (Integer.parseInt(trama[i1 + 1], 16) >= 128) {//Casos en que es un dato y no un tamaño
                                    short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
                                    short temp = devLong[Integer.parseInt(trama[i1], 16)];
                                    if (temp != 0) {//Si el byte es una variable que tiene un tamaño fijo quiere decir que ese valor mayo a 0x80 era en realidad parte de un dato y se debe revisar. 
                                        revisar = true;
                                    }
                                }
                            } catch (Exception e) {
                                if (!revisarIndice(i1 + 1, trama.length)) {//índice superior desbordado
                                    //System.out.println("\n(!) Ultimo byte\n");
                                    short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
                                    short temp = devLong[Integer.parseInt(trama[i1], 16)];
                                    if (temp != 0) {//Si el byte es una variable que tiene un tamaño fijo quiere decir que ese valor mayo a 0x80 era en realidad parte de un dato y se debe revisar. 
                                        revisar = true;
                                    }
                                } else {
                                    //System.out.println("\n(!) Error verificando byte del índice superior\n");
                                }
                            }
                            i1++; //Pasa el índice a después del tipo de variable si es por paquetes o a después del inicio de data
                            if (!aumentaPila(trama)) { //Ubica el índice en el byte en el que inicia el dato de la variable revisada, almacena su tamaño en la pila y aumenta la posición de la pila.
                                //Es false sólo cuando el índice se desborda.
                                correcto = false;
                                break;
                            }
                            if (revisar) { //Se revisa cuando ya es un dato
                                cadenaTemp = "";
                                while (pila[j1 - 1] > 0) {//el valor almacenado en la posición anterior de la pila da el tamaño del dato actual
                                    //inicia en el primer byte del dato
                                    if (revisarIndice(i1, trama.length)) {
                                        cadenaTemp = cadenaTemp + trama[i1];  //Almacena cada byte del dato y va reduciendo el tamaño en la pila hasta llegar a cero.
                                        i1++;
                                        //// System.out.println("i1 en revisar: "+i1);
                                        pila[j1 - 1]--;
                                    } else {
                                        correcto = false;
                                        break;
                                    }
                                }
                                if (!correcto) {
                                    break;
                                }
                                j1--; //Disminuye la posición de la pila, ya que el dato actual fue almacenado en la cadena temporal. 
                                cadenas[k] = cadenaTemp;
////                                    System.out.println("cadenas k "+cadenas[k]+" - trama il-1 "+trama[i1-1]+" - il-1"+(i1-1));
                                if (!procesaData(cadenas[k], tipoTrama)) { //Procesa el dato 
                                    correcto = false;
                                }
                                k = (++k) % 10;
                                pila[j1 - 1]--;//Disminuye en uno el tamaño anterior de 
                                l1++;
                            }
                            while (j1 != 1 && pila[j1 - 1] == 0) { //Verifica que la posición de la pila sea diferente de uno y que el tamaño almacenado en la posición anterior sea cero
                                //Lo anterior quiere decir que ya se terminaría de revisar toda la variable anterior, por lo tanto se baja a la posición anterior (en la que el tamño es cero) y se reduce en uno el tamaño de la anterior a esa. 
                                j1--;
                                pila[j1 - 1]--;
                                if (pila[j1 - 1] != 0 && revisar) {//Si el tamaño que se redujo es diferente de cero y ya hubo un dato revisado se debe almacenar
                                    revisar = false;
                                    almacenar = true;
                                }
                            }
                            //hasta aqui termina
                            if (almacenar) {//Indica que terminó con la cola de datos
                                //System.out.println("----------------------------");
                                if (detNumCanales) {
                                    l1 = 0;
                                }
                            }
                        } else {
                            correcto = false;
                            break;
                        }
                        // } else {
                        //    correcto = false;
                        //    break;
                        // }
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
        } catch (Exception e) {
            // System.err.println(e.getCause());
            e.printStackTrace();
            if (recortaFormato == 0) {
                //System.out.println("Proceso completado con errores\n");
                ps.escribir("Procesamiento completado con errores");
            }
            procesaIncompleto = true;
            return true;
        }
        return correcto;
    }

    public boolean aumentaPila(String[] trama) {
        datoNulo = false;
        //El índice inicia en el byte del tamaño de la variable. 
        short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
        short temp = devLong[Integer.parseInt(trama[i1 - 1], 16)];//Capta el número de bytes usados para el tamaño con el tipo de variable
        String tramaTemp = "";
        if (Integer.parseInt(trama[i1 - 1], 16) != 0) {//Variable diferente del tipo null
            int longi = Integer.parseInt(trama[i1], 16); //Byte que indica el tamaño de la variable
            if (temp == 0) { //Variables que no usan tamaño fijo
                if (revisarIndice(i1, trama.length)) {
                    if (longi < 128) { //Verifica que el tamaño no esté codificado
                        if (Integer.parseInt(trama[i1 - 1], 16) == 4) {
                            pila[j1] = (short) Math.ceil(longi / 8.0);
                        } else { //Guarda en la posición siguiente de la pila el tamaño de la variable 
                            pila[j1] = Short.parseShort(trama[i1], 16);
                        }
                    } else { //Si el tamaño de la variable está codificado lo calcula y lo guarda en la siguiente posición de la pila y aumenta el indice a la posición anterior al dato
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
                        i1 = i1 + longi;//el indice se posiciona en el byte anterior al inicio del dato. 
                    }
                } else {
                    return false;
                }
            } else { //Si la variable usa tamaño fijo no hay que tomarlo de la trama, se toma del vector de tamaños (devLong)
                pila[j1] = temp;
                i1--; //el índice debe ubicarse en el byte anterior al inicio del dato, como era una variable con tamaño fijo quiere decir que estaba en una posición sobre el dato.
            }
        } else { //Variable tipo null 
            datoNulo = true;
            pila[j1] = 1; //El tamaño debe ser uno para que se almacene a sí mismo en la cadena temporal y se procese como "cero".
            i1 = i1 - 2; //el índice debe ubicarse en el byte anterior al inicio del dato, como era una variable con tamaño fijo quiere decir que estaba en una posición sobre el dato.
        }
        i1++; //El índice termina en el byte en el que inicia el dato de la variable revisada. 
        j1++; //La posición de la pila aumenta porque el tamaño de la variable revisada ya fue almacenado.
        return true;
    }

    public boolean revisarIndice(int indice, int longTrama) {
        if (indice == (longTrama + bytesRecortados)) {
            //System.out.println("----------------------------\nTrama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados\n----------------------------");
            return false;
        } else {
            return true;
        }
    }

    public boolean clasificarTrama(String[] trama) {
        boolean continuar = false;
        if (tipoconexion != 0) {
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
        } else {
            continuar = true;
        }
        
        if (continuar){
            switch (Short.parseShort(trama[i1], 16)) {
                        case 12: { //0x0C
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
                        case 97: { //0x61
                            //System.out.println("AARE APDU (No esperado)\n----------------------------");
                            return false;
                        }
                        case 196: { //0xC4
                            switch (Short.parseShort(trama[i1 + 1], 16)) {
                                case 1: { // C4 01
                                    //System.out.println("Get Normal Response \n----------------------------");
                                    if (i1 + 3 < trama.length) {
                                        i1 = i1 + 3; //Pasa el índice al byte de inicio de la data, es decir al 0x00.
                                        String dato = "";
                                        try {
                                            if (Integer.parseInt(trama[i1 + 1], 16) == 1 || Integer.parseInt(trama[i1 + 1], 16) == 2 || Integer.parseInt(trama[i1 + 2], 16) >= 128) {
                                                if (Short.parseShort(trama[i1 + 2], 16) >= 128) {
                                                    for (int k = 1; k <= (Integer.parseInt(trama[i1 + 2], 16) % 128); k++) {
                                                        dato = dato + trama[i1 + 2 + k];
                                                    }
                                                } else {
                                                    dato = trama[i1 + 2];
                                                }
                                                //System.out.println("RegistrosAprocesar: " + Integer.parseInt(dato, 16));
                                                registrosAprocesar = Integer.parseInt(dato, 16);
                                            } else {
                                                //System.out.println("Interpreta Trama: Dato tipo " + trama[i1 + 1] + " - " + trama[i1 + 2]);
////                                                System.out.println("tipo dato i1: "+trama[i1+1]+" tipo dato i1+1: "+trama[i1+2]);
                                            }
                                        } catch (Exception e) {
                                            //System.out.println("Error identificando registros a procesasr");
                                        }
                                    } else {
                                        //System.out.println("----------------------------");
                                        //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                        //System.out.println("----------------------------");
                                        return false;
                                    }

                                    if (j1 == 0) {
                                        pila[j1] = 1; //Guarda 1 en la posición 0 de la pila.
                                        j1++; // Aumenta la posición de la pila.
                                    }

                                    return operaReadResponse(trama);
                                }
                                case 2: { // C4 02
                                    //System.out.println("Get Block Transfer Response \n----------------------------");
                                    if (i1 + 12 < trama.length) {
                                        int recortar = 0;//VIC
                                        String dato = "";
                                        try {
                                            //Verifica el byte siguiente al byte de inicio de data, es decir el byte con el tamaño del paquete
                                            //Si este byte es mayor a 0x80 quiere decir que trae el tamaño codificado en el número de bytes que indiquen
                                            //los bits 0111 1111 (Ej.: si es 0x81, es un byte. Si es 0x82 son dos bytes).
                                            if (Short.parseShort(trama[i1 + 9], 16) >= 128) {
                                                recortar = (Integer.parseInt(trama[i1 + 9], 16) % 128); //Calcula el número de bytes en los que se codifica el tamaño.
                                                i1 = i1 + 10 + recortar;// Lleva el indice al byte siguiente al tamaño del paquete, si es un arreglo sería al 0x01.
                                            } else { //Cuando no está codificado el tamaño del paquete en más de un byte, usa un byte por defecto.
                                                i1 = i1 + 10;//Lleva el índice al byte siguiente al tamaño del paquete, si es un arreglo sería al 0x01.
                                            }
                                            if (Integer.parseInt(trama[i1], 16) == 1 || Integer.parseInt(trama[i1], 16) == 2 || Integer.parseInt(trama[i1 + 1], 16) >= 128) {
                                                if (Short.parseShort(trama[i1 + 1], 16) >= 128) {
                                                    for (int k = 1; k <= (Integer.parseInt(trama[i1 + 1], 16) % 128); k++) {
                                                        dato = dato + trama[i1 + 1 + k];
                                                    }
                                                } else {
                                                    dato = trama[i1 + 1];
                                                }
                                                registrosAprocesar = Integer.parseInt(dato, 16);
                                            }
                                        } catch (Exception e) {
                                            //System.out.println("Error identificando registros a procesasr");
                                        }
                                    } else {
                                        //System.out.println("----------------------------");
                                        //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                                        //System.out.println("----------------------------");
                                        return false;
                                    }
                                    if (j1 == 0) {
                                        pila[j1] = 1; //Guarda 1 en la posición 0 de la pila.
                                        j1++; // Aumenta la posición de la pila.
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
                dato = ps.Hex2ASCII(dato);
                //System.out.println(dato);
                datoserial = dato;
                break;
            }
            case 3: { //clock
                try {
                    fechaActual = fecha.parse(ps.Hex2Date(dato));
                    dato = sdf3.format(fechaActual);

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                //System.out.println("Fecha actual " + dato);
                break;
            }
            case 4: { //periodo de integración
                try {
                    periodoIntegracion = Integer.parseInt(dato, 16) / 60;
                    //System.out.println("\nPeriodo de Integracion capturado: " + periodoIntegracion);
                    ps.escribir("\nPeriodo de Integracion capturado: " + periodoIntegracion);
                    if (regisdia) {
                        //System.out.println("\nPeriodo de Integracion estimado: " + 1440);
                        ps.escribir("\nPeriodo de Integracion estimado: " + 1440);
                        intervaloRdia = 1440;
                    } else if (regismes) {
                        //System.out.println("\nPeriodo de Integracion estimado: " + 44640);
                        ps.escribir("\nPeriodo de Integracion estimado: " + 44640);
                        intervaloRmes = 44640;
                    } else {
                        intervalo = periodoIntegracion;
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
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
            case 6: { //firmware
                dato = ps.Hex2ASCII(dato);
                //System.out.println(dato);
                datofirmware = dato;
            try {
                cp.actualizaFirmware(med.getnSerie(), datofirmware);
            } catch (Exception e) {
                //System.out.println("\nError en set Firmware: " + datofirmware);
                e.printStackTrace();
            }
                break;
            }
            case 7: {
                try {
                    setdirfis = true;
                    if (Integer.parseInt(dato.substring(0, 2), 16) >= 48) {
                        //System.out.println("\nDireccion Fisica: " + ps.Hex2ASCII(dato));
                        dirfis = Integer.parseInt(ps.Hex2ASCII(dato));
                    } else {
                        dirfis = Integer.parseInt(dato, 16);
                    }
                    cp.actualizaDirFis(med.getnSerie(), dirfis);
                    //System.out.println("\nDireccion Fisica: " + dirfis);
                } catch (Exception e) {
                    //System.out.println("\nError en set Direccion Fisica: " + dirfis);
                    setdirfis = false;
                    e.printStackTrace();
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
                if (l1 == 0) {
                    dataEnArreglo = new ArrayList<>();
                    dataEnArregloNulos = new ArrayList<>();
                }
                dataEnArreglo.add(dato);
                dataEnArregloNulos.add(datoNulo);
                if (dataEnArreglo.size() == obis.size()) {
                    procesaDataPerfil();
                }

                break;
            }
            case 23: { //Registros - OJO EN DESARROLLO
                procesaDataRegistros(dato);
                if (med.marcaMedidor.codigo.equals("19")) {
                    // epp.Hexing.procesaDataRegistros(dato); 
                } else if (med.marcaMedidor.codigo.equals("20")) {
                }
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

    public void procesaDataEventos(String dato) { //verificar los códigos de eventos - 25 down - 26 up
        if (epp.isUsarPowerOffConEstampas()) {
            try {
                fechaEvento = fecha.parse(ps.Hex2Date(dato));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (l1 == 0) {
                regEvento = new ERegistroEvento();
                regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
            } else if (l1 == 1) {
                registrosprocesados++;
                regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
                if (regEvento.vcfechacorte != null && regEvento.vcfechareconexion != null) {
                    //System.out.println(fecha.format(regEvento.vcfechacorte) + "-" + fecha.format(regEvento.vcfechareconexion));
                    regEvento.vcserie = seriemedidor;
                    regEvento.vctipo = "0001";                    
                    try {
                        cp.actualizaEvento(regEvento, null);
                        ps.escribir("Power Down: " + fecha.format(regEvento.vcfechacorte) + "-" + "Power Up: " + fecha.format(regEvento.vcfechareconexion));
                    } catch (Exception e) {
                        e.printStackTrace();
                        ps.escribir("Error almacenando evento: " + fecha.format(regEvento.vcfechacorte) + "-" + fecha.format(regEvento.vcfechareconexion));
                    }
                    //System.out.println("termina actualiza");
                    regEvento = new ERegistroEvento();
                    regEvento.vcfechacorte = null;
                    regEvento.vcfechareconexion = null;
                }
            }
        } else {
            if (l1 == 0) {
                try {
                    registrosprocesados++;
                    fechaEvento = fecha.parse(ps.Hex2Date(dato));
                    //System.out.println("Procesa registro del: " + fechaEvento);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else if (l1 == 1) {
                try {
                    if (dato.equals(epp.getDLMS_PrincipalEvents().get(1))) {
                        //System.out.println("Cod.: 0x" + dato + " - Power up: " + fechaEvento);
                        regEvento.vcfechareconexion = new java.sql.Timestamp(fechaEvento.getTime());
                        if (regEvento.vcfechacorte != null && regEvento.vcfechareconexion != null) {
                            //System.out.println(fecha.format(regEvento.vcfechacorte) + "-" + fecha.format(regEvento.vcfechareconexion));
                            regEvento.vcserie = seriemedidor;
                            regEvento.vctipo = "0001";                            
                            cp.actualizaEvento(regEvento, null);
                            //System.out.println("termina actualiza");
                            regEvento = new ERegistroEvento();
                            regEvento.vcfechacorte = null;
                            regEvento.vcfechareconexion = null;
                        }
                    } else if (dato.equals(epp.getDLMS_PrincipalEvents().get(0))) {
                        //System.out.println("Cod.: 0x" + dato + " - Power down: " + fechaEvento);
                        regEvento = new ERegistroEvento();
                        regEvento.vcfechareconexion = null;
                        regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                    } else {// Códigos restantes para el perfil de eventos Power Quality
                        //System.out.println("codigo de evento: " + dato);
                        regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                        regEvento.vcfechareconexion = regEvento.vcfechacorte;
                        regEvento.vctipo = dato;
                        if (regEvento.vcfechacorte != null && regEvento.vctipo != null) {
                            regEvento.vcserie = seriemedidor;
                            regEvento = new ERegistroEvento();
                            regEvento.vcfechacorte = null;
                            regEvento.vcfechareconexion = null;
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void procesaDataPerfil() {
        int indxclock = obis.indexOf("0000010000FF");
        String dato = dataEnArreglo.get(indxclock);
        try {
            indicecanal = 0;
            indxlec = 0;
            elecaux.setIndexlec(0);
            registrosprocesados++;
            //// System.out.println(" indicecanal " + indicecanal);
            //System.out.println("-> Dato original: " + dato);
            ps.escribir("-> Dato original: " + dato);
//                if(obis.get(indicecanal).equals("0000010000FF")){    
            if (dato.equals("00") || dato.equals("")) {//si el primer elemento es diferente de nulo viene el intervalo con fecha
                nintervalo++;
                fechaintervalo = new Timestamp(fechalectura.getTime() + (nintervalo * (60000 * intervalo)));//multiplica el número de intervalos desde la última fecha procesada                
                //System.out.println("Fecha intervalo estimado " + sdf2.format(new Date(fechaintervalo.getTime())));
                ps.escribir("Fecha intervalo estimado " + sdf2.format(new Date(fechaintervalo.getTime())));
            } else {
                dato = sdf2.format(fecha.parse(ps.Hex2Date(dato)));
                //System.out.println("Fecha intervalo " + dato);
                ps.escribir("Fecha intervalo " + dato);
                fechalectura = new Timestamp(sdf2.parse(dato).getTime()); //Fecha captada desde la trama
                fechaintervalo = fechalectura;
                nintervalo = 0;
            }

            if (fechaintervalo.getMinutes() % intervalo != 0) {
                dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
                fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
                //System.out.println("Fecha intervalo aproximado " + dato);
                ps.escribir("Fecha intervalo aproximado " + dato);
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
                    ps.escribir("Diferencia entre intervalo anterior y actual: "+aumento+ ". Procesado con: "+fechaintervalo.getTime()+", "+ ultimoIntervalo.getTime()+", "+intervalo);
                    if (aumento > 0) {//obtiene el numero de intervalos a mover
                        for (int i = 0; i < aumento; i++) {
                            fechaCero = new Timestamp(ultimoIntervalo.getTime() + (60000L * (long) intervalo) * (i + 1));//movemos la fecha por cada intervalo faltante
                            //System.out.println("fecha intervalo en 0 " + fechaCero);
                            ps.escribir("Fecha de intervalo no capturado: " + fechaCero);
                            ps.escribir ("Cantidad de canales: "+obis.size());
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
                                            if ((obis.get(k).substring(6, 8).equals("08"))) {// && (Integer.parseInt(med.getMarcaMedidor().getCodigo())==21)){//canal no es interval
                                                if (!fprimerintervalo.equals(fechaintervalo)) {
                                                    vlec.add(lec);
                                                }
                                            } else {
                                                vlec.add(lec);
                                            }
                                            procesaCeros = true;
                                            ps.escribir("Hueco procesado: "+ fechaCero+" - "+obis.get(k)+" - "+canal);
                                        } else {
                                            //System.out.println("Constante");
                                        }

                                    }
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    ps.escribir("Clock obtenido sin valor");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ps.escribir("Captura error en procesa huecos: "+e.getMessage());
            }
            ultimoIntervalo = fechaintervalo;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            ps.escribir("Captura error en procesa fecha: "+e.getMessage());
        }
        for (int j = 0; j < dataEnArreglo.size(); j++) {
            if (j != indxclock) {
                try {
                    dato = dataEnArreglo.get(j);
                    indicecanal = j;//OJOOOO
                    //System.out.println("indicecanal: " + indicecanal);
                    //System.out.println("-> Dato original: " + dato);
                    if (dato.equals("") || dato.equals("00")) {
                        dato = "00";
                        //System.out.println("valor nulo");
                    }
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
                                lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                                if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                    lec.lec = lec.lec / epp.getResolucion();
                                }
                                //System.out.println("valor " + lec.lec);
                                //System.out.println("Unidad " + unidad.get(indicecanal));
                                if ((obis.get(indicecanal).substring(6, 8).equals("08"))) {//&& (Integer.parseInt(med.getMarcaMedidor().getCodigo())==21)){//canal no es interval
                                    lec.setLecaux(lec.lec);
                                    if (!fprimerintervalo.equals(fechaintervalo)) {
                                        if (med.marcaMedidor.codigo.equals("20") & dataEnArregloNulos.get(j)) {//datoNulo){
                                            //System.out.println("MnC_08");
                                            lec.lec = 0;
                                        } else {
                                            lec.lec = lec.lec - lecInterval.get(indxlec);
                                            lecInterval.set(indxlec, lec.lecaux);
                                        }
                                        //System.out.println("valor re calculado " + lec.lec);
                                        vlec.add(lec);
                                    } else {
                                        //Guarda el primer valor independiente de la marca
                                        lecInterval.add(indxlec, lec.lecaux);
                                    }
                                    indxlec++;
                                } else if (med.marcaMedidor.codigo.equals("20") & dataEnArregloNulos.get(j)) {//datoNulo){
                                    //System.out.println("MnC");
                                    lec.setLecaux(lec.lec);//Conserva el valor actual como histórico en "lecaux"
                                    if (procesaCeros) {
                                        lec.setLec(lecCero.get(lecCero.size() - constantesmatch));
                                        if (indiceCero == constantesmatch) {
                                            procesaCeros = false;
                                            indiceCero = 0;
                                        }
                                        indiceCero++;
                                    } else {
                                        lec.setLec(vlec.get(vlec.size() - constantesmatch).getLec());//obtiene el registro con la posición del canal en el intervalo pasado
                                    }
                                    //System.out.println("valor re calculado " + lec.lec);
                                    lecCero.add(lec.lec); // guarda el lec recalculado para ser usado si a futuro hay ceros por desconexión.
                                    vlec.add(lec);
                                } else {
                                    vlec.add(lec);
                                }
                            } else { //
                                //System.out.println("Sin Scaler-unit");
                                //System.out.println("\nConversion desde base de datos: ");
                                lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), cp.trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
                                if (canal.contains("k") || canal.contains("K")) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                    lec.lec = lec.lec / epp.getResolucion();
                                }
                                //System.out.println("valor " + lec.lec);
                                //System.out.println("Unidad " + unidad.get(indicecanal));
                                if ((obis.get(indicecanal).substring(6, 8).equals("08"))) {//&& (Integer.parseInt(med.getMarcaMedidor().getCodigo())==21)){//canal no es interval
                                    lec.setLecaux(lec.lec);
                                    if (!fprimerintervalo.equals(fechaintervalo)) {
                                        //cuando viene un nulo por configuración de los MnC, quiere decir que no cambió el valor anterior, por lo tanto la resta siempre dará cero. 
                                        if (med.marcaMedidor.codigo.equals("20") & dataEnArregloNulos.get(j)) {//datoNulo){
                                            //System.out.println("MnC_08");
//                                            lecInterval.set(indxlec,lecInterval.get(lecInterval.size() - constantesmatch));
                                            lec.lec = 0;
                                        } else {
                                            lec.lec = lec.lec - lecInterval.get(indxlec);
                                            lecInterval.set(indxlec, lec.lecaux);
                                        }
                                        //System.out.println("valor re calculado " + lec.lec);
                                        vlec.add(lec);
                                    } else {
                                        //Guarda el primer valor independiente de la marca
                                        lecInterval.add(indxlec, lec.lecaux);
                                    }
                                    indxlec++;
                                } else if (med.marcaMedidor.codigo.equals("20") & dataEnArregloNulos.get(j)) {//datoNulo){
                                    //System.out.println("MnC");
                                    lec.setLecaux(lec.lec);//Conserva el valor actual como histórico en "lecaux"
                                    if (procesaCeros) {
                                        lec.setLec(lecCero.get(lecCero.size() - constantesmatch));
                                        if (indiceCero == constantesmatch) {
                                            procesaCeros = false;
                                            indiceCero = 0;
                                        }
                                        indiceCero++;
                                    } else {
                                        lec.setLec(vlec.get(vlec.size() - constantesmatch).getLec());//obtiene el registro con la posición del canal en el intervalo pasado
                                    }
                                    //System.out.println("valor re calculado " + lec.lec);
                                    lecCero.add(lec.lec); // guarda el lec recalculado para ser usado si a futuro hay ceros por desconexión.
                                    vlec.add(lec);
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
        }
    }

    public void procesaDataRegistros(String dato) {

        if (l1 == 0) {
            indicecanal = 0;
            elecaux.setIndexlec(0);
            try {
                registrosprocesados++;
                //// System.out.println(" indicecanal " + indicecanal);
                //System.out.println("-> Dato original: " + dato);
                if (dato.equals("00") || dato.equals("")) {//si el primer elemento es diferente de nulo viene el intervalo con fecha
                    nintervalo++;
                    fechaintervalo = new Timestamp(fechalectura.getTime() + (nintervalo * (60000 * intervalo)));//multiplica el número de intervalos desde la última fecha procesada
                    //System.out.println("Fecha intervalo estimado " + sdf2.format(new Date(fechaintervalo.getTime())));
                } else {
                    dato = sdf2.format(fecha.parse(ps.Hex2Date(dato)));
                    //System.out.println("Fecha intervalo " + dato);
                    fechalectura = new Timestamp(sdf2.parse(dato).getTime()); //Fecha captada desde la trama
                    fechaintervalo = fechalectura;
                    nintervalo = 0;
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
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            indicecanal++;
            try {
                //System.out.println("-> Dato original: " + dato);
                if (dato.equals("") || dato.equals("00")) {
                    dato = "00";
                    //System.out.println("valor nulo");
                }
                try {
                    dato = String.valueOf(Long.parseLong(dato, 16));
                } catch (Exception e) {
                    //System.out.println("Error obteniendo valor numérico");
                }
                //System.out.println(dato);
                econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(indicecanal), 16));
                canal = "";
                for (EtipoCanal et : vtiporegistros) {
                    if (Long.parseLong(et.getCanal()) == Long.parseLong(obis.get(indicecanal), 16)) {
                        canal = et.getUnidad();
                        //System.out.println("Canal " + et.getCanal() + " Unidad " + canal);
                        break;
                    }
                }
                if (fechaintervalo != null && canal.length() > 0) {
                    if (constanteOk) {
                        //// System.out.println("valor " + cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                        //// System.out.println("Unidad " + unidad.get(indicecanal));
                        reg = new ERegistro(med.getnSerie(), tiporegistros, (fechaintervalo.getYear() + 1900), (fechaintervalo.getMonth() + 1), (fechaintervalo.getDate()), Double.parseDouble(dato), Long.parseLong(obis.get(indicecanal), 16), canal, cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conske.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                        if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                            reg.energia = reg.energia / epp.getResolucion();
                        }
                        //System.out.println("valor " + reg.energia);
                        //System.out.println("Unidad " + unidad.get(indicecanal));
                        if (med.marcaMedidor.codigo.equals("20") & datoNulo) {
                            //System.out.println("MnC");
                            reg.setLecaux(reg.energia);//Conserva el valor actual como histórico en "lecaux"
                            reg.setEnergia(vreg.get(vreg.size() - constantesmatch).getEnergia());//obtiene el registro con la posición del canal en el intervalo pasado
                            //System.out.println("valor re calculado " + reg.energia);
                            vreg.add(reg); //almacena el reg con el valor del intervalo pasado
                            // elecaux.setVreg(vreg);
                            // elecaux = epp.MnC.procesaReg(fprimerintervalo, fechaintervalo, fechaCero, intervalo, reg, elecaux);
                            // vreg = elecaux.getVreg();
                        } else {
                            vreg.add(reg);
                        }
                    } else { //
                        //System.out.println("Sin Scaler-unit");
                        //System.out.println("\nConversion desde base de datos: ");
                        //// System.out.println("valor " +cp.trasnformarEnergia(Double.parseDouble(dato), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                        //// System.out.println("Unidad " + canal);
                        reg = new ERegistro(med.getnSerie(), tiporegistros, (fechaintervalo.getYear() + 1900), (fechaintervalo.getMonth() + 1), (fechaintervalo.getDate()), Double.parseDouble(dato), Long.parseLong(obis.get(indicecanal), 16), canal, cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(conske.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                        if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                            reg.energia = reg.energia / epp.getResolucion();
                        }
                        //System.out.println("valor " + reg.energia);
                        //System.out.println("Unidad " + unidad.get(indicecanal));
                        if (med.marcaMedidor.codigo.equals("20") & datoNulo) {
                            //System.out.println("MnC");
                            reg.setLecaux(reg.energia);//Conserva el valor actual como histórico en "lecaux"
                            reg.setEnergia(vreg.get(vreg.size() - constantesmatch).getEnergia());//obtiene el registro con la posición del canal en el intervalo pasado
                            //System.out.println("valor re calculado " + reg.energia);
                            vreg.add(reg); //almacena el reg con el valor del intervalo pasado
                            // elecaux.setVreg(vreg);
                            // elecaux = epp.MnC.procesaReg(fprimerintervalo, fechaintervalo, fechaCero, intervalo, reg, elecaux);
                            // vreg = elecaux.getVreg();
                        } else {
                            vreg.add(reg);
                        }
                    }
                } else {
                    if (fechaintervalo == null) {
                        //System.out.println("Fecha nula");
                    } else if (canal.length() <= 0) {
                        //System.out.println("Canal no configurado");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // System.err.println(e.getMessage());
            }

        }
    }

    public Date getDCurrentDate() throws ParseException {
        return Date.from(ZonedDateTime.now(zid).toInstant());
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

    public int calculaDiasALeer(boolean regisdia, boolean regismes, boolean lpowerLost) {
        if (med.getFecha() != null) {
            if (regisdia) {
                ndias = ndiaReg; //Número de días de registro diario
            } else if (regismes) {
                ndias = nmesReg * 30; //Número de días de registro mensual
            } else if (lpowerLost) {
                ndias = ndiaseventos;
            } else {
                try {
                    ps.escribir("Fecha actual Colombia: " + getDCurrentDate());
                    ps.escribir("Fecha Ultima Lectura " + med.getFecha());
                    long diffInMillies = Math.abs(getDSpecificDate(true, 1, "D").getTime() - med.getFecha().getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    ndias = (int) diff;
                } catch (Exception e) {
                    ps.escribir("Error calculando días de diferencia. Se tomará el máximo de días (30) permitido por el aplicativo por defecto.");
                    ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                    ndias = 30;
                }
            }
            return ndias;
        } else {
            return 10;
        }        
    }

    public byte[] concatFrames(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
    public int getPDULength( int spare, String[] PDU) {
        switch(spare) {
            case 1: 
                return (Integer.parseInt(PDU[13], 16) & 0xFF);
            case 2: 
                return (Integer.parseInt(PDU[13], 16) << 8 & 0xFF00) + (Integer.parseInt(PDU[14], 16) & 0xFF);                
            case 3:
                return (Integer.parseInt(PDU[13], 16) << 16 & 0xFF0000) + (Integer.parseInt(PDU[14], 16) << 8 & 0xFF00)  + (Integer.parseInt(PDU[15], 16) & 0xFF);
            default:
                return (Integer.parseInt(PDU[13], 16) << 8 & 0xFF00) + (Integer.parseInt(PDU[14], 16) & 0xFF);                   
        }
    }
    
    public String extractValue(String[] vectorhex) {
        int dataType = Integer.parseInt( vectorhex[7], 16 );
        switch (dataType) {
            case 5:
                return ""+vectorhex[8] + vectorhex[9] + vectorhex[10] + vectorhex[11];            
            case 6:
                return ""+vectorhex[8] + vectorhex[9] + vectorhex[10] + vectorhex[11];                        
            case 13:
                return ""+vectorhex[8];
            case 15: 
                return ""+vectorhex[8];
            case 16:
                return ""+vectorhex[8] + vectorhex[9];
            case 17:
                return ""+vectorhex[8];
            case 18:
                return ""+vectorhex[8] + vectorhex[9];
            case 20:
                return ""+vectorhex[8] + vectorhex[9] + vectorhex[10] + vectorhex[11] + vectorhex[12] + vectorhex[13] + vectorhex[14] + vectorhex[5];
            case 21:
                return ""+vectorhex[8] + vectorhex[9] + vectorhex[10] + vectorhex[11] + vectorhex[12] + vectorhex[13] + vectorhex[14] + vectorhex[5];            
            case 22:
                return ""+vectorhex[8];
            default:
                ps.escribir("Tipo de dato no soportado");
                return "00";
        }
    }

    public int getNSession() {
        return this.nSession;
    }

    public void setNSession(int nS) {
        this.nSession = nS;
    }
}
