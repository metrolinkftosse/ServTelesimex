/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.Electura;
import Entidades.EtipoCanal;
import Entidades.EParamProtocolos;
import Util.SynHoraNTP;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lenovo
 */
public class ProcesosSesion {
    //constructor
    EMedidor med;
    ControlProcesos cp;
    EParamProtocolos epp;
    ProcesosSesion ps;
    private String usuario = "admin";
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    File file;
    boolean existearchivo = false;
    boolean aviso = false;
    int indx = 0;
    Abortar objabortar;
    public boolean lperfil; //       Booleanos 
    public boolean leventos; //      checkbox 
    public boolean lregistros; //       de
    public boolean lconfhora; //    Telesimex
    public boolean lacumulados;
    public boolean ldisconnect; 
    public boolean lreconnect;
    //protocolos
//    ProcesosDLMS DLMS;
    //variables para obtención de parámetros del medidor en jinit
    public int indxuser = 0;
    String numeroPuerto;
    int numeroReintentos = 4;
    int ReintentoFRMR = 0; //  Manejo de reintentos en
    int ReinicioFRMR = 0; //   validatipotrama por FRMR
    int velocidadPuerto;
    long timeout;
    int ndias;
    int diasaleer = 0;
    long fechalectura;
    int ndiaReg;
    int nmesReg;
    int ndiaseventos;
    String password = "";
    String password2 = "";
    String seriemedidor = "";
    int numcanales = 2;
    int dirfis = 1;
    int dirlog = 1;
    int InvokeIDandParity = 193; //0xC1 := 193
    long ndesfasepermitido = 7200;
    Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
    ArrayList<EtipoCanal> vtipocanal;
    long tiempo = 500;
    String tiporegistros = "0";
    ArrayList<EtipoCanal> vtiporegistros; 
    Vector<Electura> vlec;
    //Variables en abrepuerto
    int reintentosUtilizados;// reintentos utlizados
    private int reintentoconexion = 0;
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    public volatile Socket socket;
    boolean portconect = false;
    public volatile InputStream input;
    public volatile OutputStream output;
    public volatile boolean leer = true;
    public boolean desbloqueo = false;
     //variables en cp.escribir
    Date d = new Date();
    RandomAccessFile fr;
    //ReiniciaCom
    public volatile boolean enviando = false;
    public volatile boolean reenviando = false;
    //variables en procesacadena
    public volatile String cadenahex = "";
     //variables en iniciacom
    long tiemporetransmision = 0;
    int actualReintento = 0;
    public byte[] ultimatramaEnviada = null;
    public byte[] ultimatrama = null;
    public String descripcionTrama = "";
    //variables en enviatramas
    public volatile boolean enviaPrimeraTrama = false;
    public volatile boolean interpretaCadenaB = false;
    public volatile boolean lenviaTrama2 = false;
    public Thread tReinicio = null;
    public volatile boolean cierrapuerto = false;    
    public ZoneId zid;
    
    private final Object monitor;
    public AtomicBoolean barrera = new AtomicBoolean(false);
    private volatile byte[] endFrame;
    
    public ProcesosSesion() {
        this.monitor = new Object();
    }
    
    public ProcesosSesion(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, boolean lacumulados, boolean disconnect, boolean reconnect,  int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, EParamProtocolos epp, ZoneId zid, long ndesfase, Object monitorExt){
        this.med = med;
        this.cp = cp;
        this.epp = epp;
        this.usuario = usuario;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        file = cp.creaArchivoLog(this.med);      
        this.lperfil = perfil;
        this.leventos = eventos;
        this.lregistros = registros;
        this.lconfhora = lconfhora;
        this.lacumulados = lacumulados;
        this.ldisconnect = disconnect;
        this.lreconnect = reconnect;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        this.monitor = monitorExt;
        jinit();
    }
    
    private void jinit() {
        try {
            cp.escribir("Equipo llamador: " + cp.obtenerMac(), file);
            cp.escribir("Telesimex version: " + cp.getVersion(), file);
            cp.escribir("Window Id: " + cp.getWindowId(), file);
            indxuser = 0;
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
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
            dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "0" : med.getDireccionFisica()));
            dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "0" : med.getDireccionLogica()));
            lconske = cp.buscarConstantesKeLong(med.getnSerie());//se toman los valores de las constantes 
            vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
            abrePuerto();
            tiempo = 1000; //Escucha
        } catch (Exception e) {
            leer = false;
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }
    
    
    public void abrePuerto() {
        tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
        try {
            if (!objabortar.labortar) {
                if (reintentoconexion < numeroReintentos) { //3) { //parametrizable con reintentos de conf_medidor                    
                    socket = new Socket(med.getDireccionip(), Integer.parseInt(med.getPuertoip()));
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

                    //esperamos que se reinicie el escuchar
                    Thread.sleep(2000);
                    if (portconect) {
                        if (!objabortar.labortar) {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, "LeerTCPDLMS", "Conexion Satisfactoria");
                            iniciacomunicacion();
                        } else {
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        escribir("Socket cerrado");
                        throw new Exception("Socket cerrado");
                    }
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Numero de intentos de conexion agotado");
                    escribir("Número de reintentos agotado");
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                }

            } else {            
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Desconexion Cancelacion de operacion");
                escribir("Operación abortada");
                cerrarLog("Desconexion Cancelacion de operacion", false);
                leer = false;
            }
        } catch (Exception e) {
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPActaris", "No conectado");
            escribir("ERROR de comunicacion - " + e.getMessage());
            reintentoconexion++;
            abrePuerto();
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
        log.setNreintentos(reintentosUtilizados);
        log.setVccoduser(usuario);
        log.setLexito(lexito);
        cp.saveLogCall(log, null);
    }
    
    public void reiniciaComunicacion(boolean sendBye) {
        try {
            escribir("Reinicia Comunicacion");            
            Thread.sleep(1000);
            cerrarPuerto(sendBye);
            if (reintentoconexion <= numeroReintentos) {
                if (!objabortar.labortar) {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Desconexion reinicio de comunicacion");
                    tReinicio = new Thread() {
                        @Override
                        public void run() {                            
                            reintentoconexion++;
                            abrePuerto();
                        }
                    };
                    tReinicio.start();
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Desconexion proceso abortado");
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Desconexion numero de reintentos agotado");
                cerrarLog("Desconexion Numero de reintentos agotado", false);
                leer = false;

            }
        } catch (Exception e) {
            escribir("Error en hilo de reinicio");
            escribir(getErrorString(e.getStackTrace(), 3));
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, "LeerTCPDLMS", "Error al reiniciar la comunicación.");
            leer = false;
        }
    }        
    
    public void iniciacomunicacion() {
        tiemporetransmision = timeout * 1000;
        InvokeIDandParity = epp.getDLMS_InvokeIDandParity();
        if (med.isLconfigurado()) {
            try {
                String mensaje = "INICIO DE SESSION --" + new Timestamp(new Date().getTime()) + "--";
                cp.escribir(mensaje, file);
                mensaje = "Medidor: " + med.getMarcaMedidor().getNombre() + ", Serie: " + med.getnSerie();
                cp.escribir(mensaje, file);
                mensaje = "Puerto TCP " + med.getPuertoip();
                cp.escribir(mensaje, file);
                mensaje = "Direccion IP " + med.getDireccionip();
                cp.escribir(mensaje, file);
                mensaje = "Numero de reintentos " + med.getReintentos();
                cp.escribir(mensaje, file);
                mensaje = "Numero de dias default " + med.getNdias();
                cp.escribir(mensaje, file);
                mensaje = "Timeout " + med.getTimeout();
                cp.escribir(mensaje, file);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                escribir("Error Escribiendo Cabecera de Log de Comunicaciones");
            }
            enviaPrimeraTrama = true;
        } else {
            escribir("Medidor no configurado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPdlms", "Medidor no configurado");
            cerrarPuerto(false);
            cerrarLog("Medidor no configurado", true);
            leer = false;
        }
    }

    public void enviaTrama2(byte[] bytes, String descripcion, ProcesosDLMS DLMS ) {
        final byte[] trama = bytes;
        final String des = descripcion;
        enviando = true;
        synchronized (monitor) {
            try {
                lenviaTrama2 = false;
                int intentosRetransmision = 0;
                boolean t = true;
                while (t) {
                    if (enviando || reenviando) {
                        if (intentosRetransmision != 0) {
                            escribir("TimeOut, Intento de reenvio..");
                        }
                        escribir(des);
                        escribir("=> " + encode(trama, trama.length));
                        try {
                            while (!barrera.get()) {
                                monitor.wait(100);
                            }
                            monitor.notifyAll();
                            enviaTrama(trama);
                            monitor.wait();
                        } catch (Exception e) {
                            escribir(getErrorString(e.getStackTrace(), 3));
                        }
                    } else {
                        t = false;
                    }
                    if (reenviando && intentosRetransmision <= 2) {
                        intentosRetransmision++;
                        while (barrera.get()) {
                            monitor.wait(100);
                        }
                        monitor.notifyAll();
                    } else if (reenviando && intentosRetransmision > 2) {
                        escribir("Numero de reenvios agotado");
                        enviando = false;
                        t = false;
                        cierrapuerto = true;
                        while (barrera.get()) {
                            monitor.wait(100);
                        }
                        monitor.notifyAll();
                    } else {
                        while (barrera.get()) {
                            monitor.wait(100);
                        }
                        monitor.notifyAll();
                        return;
                    }
                }
                if (cierrapuerto) {
                    cierrapuerto = false;
                    cerrarPuerto(true);                    
                    if (des.equalsIgnoreCase("Perfil de carga")) {
                        DLMS.almacenaDatos();
                        DLMS.resetCheckerBooleans("");
                    }
                    if (reintentoconexion <= numeroReintentos) {
                        if (!objabortar.labortar) {
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCDLMS", "Desconexion timeout");
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
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Desconexion proceso abortado");
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        escribir("Numero de reintentos agotado");
                        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPDLMS", "Desconexion numero de reintentos agotado");
                        escribir("Estado Lectura No leido");
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }
                }
            } catch (Exception e) {
                cerrarPuerto(true);
                escribir("Error en hilo envia trama tipo");
                escribir(getErrorString(e.getStackTrace(), 3));
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, "LeerTCPDLMS", "Error en hilo de envio de data.");
                cerrarLog("Error en hilo envia trama", false);
                leer = false;
            }
        }
    }
    
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};
    
    public String encode(byte[] b, int ancho) {

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
    
    protected void enviaTrama(byte[] bytes) {
        try {
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal                      
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }
    
    public void cerrarPuerto(boolean sendBye) {
        if (sendBye) {
            escribir("Envia Logout sin espera de respuesta");
            escribir("=> " + encode(endFrame, endFrame.length));
            enviaTrama(endFrame);
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
            enviaPrimeraTrama = false;
            lenviaTrama2 = false;
            Thread.sleep((long) 5000);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void escucha() {
        try {
            procesaCadena();                            
        } catch (Exception e) {
            leer = false;
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
                                break;
                            }
                        }
                    } else if (idxFrame == 0) {
                        auxBuffer = readBuffer;
                        idxFrame = numbytes;
                        uncomplete = false;
                        enviando = false;
                        reenviando = false;
                    } else {
                        uncomplete = false;
                        enviando = false;
                        reenviando = false;
                    }
                }
                if (!socket.isClosed() && uncomplete) {
                    cp.escribir("Se venció el timeout de respuesta sin recibir nada.", file);
                    if (descripcionTrama.equalsIgnoreCase("Logout")) {
                        auxBuffer[0] = (byte) 0x00;
                        idxFrame = 1;
                        enviando = false;
                        reenviando = false;
                    } else {
                        //Se vencío el timeout de respuesta sin recibir nada                        
                        reenviando = true;
                        enviando = false;
                        monitor.notifyAll();
                        return;
                    }
                }
                //codificamos las tramas que vienen en hexa para indetificar su contenido
                cadenahex = encode(auxBuffer, idxFrame);
                //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
                //luego de tener la trama desglosada byte x byte continuamos a interpretarla
                if (cadenahex.length() > 0) {
                    interpretaCadenaB = true;
                }
                monitor.notifyAll();
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
    //calculos y procesos
    public String convertStringToHex(String str) {
        //////System.out.println("cadena a transformar " + str);
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
    
    public String[] cortarTrama(String[] vectorhex, int tamaño) {
        String nuevoVector[] = new String[tamaño + 2];
        System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
        for (int i = 0; i < nuevoVector.length; i++) {
            //System.out.print(" " + nuevoVector[i]);
        }
        return nuevoVector;
    }
    
    public String[] sacarDatos(String[] vectorhex, int inicio) {
        int tam = (Integer.parseInt((vectorhex[1] + vectorhex[2]), 16) & 0x7FF);
        String nuevoVector[] = new String[tam - inicio - 1];
        System.arraycopy(vectorhex, inicio, nuevoVector, 0, nuevoVector.length);
        return nuevoVector;
    }  
    
    public String[] sacarDatosWrapper(String[] vectorhex) {
        int tam = Integer.parseInt( ( vectorhex[6] + vectorhex[7] ), 16 ) & 0xFFFF;
        String nuevoVector[] = new String[tam];
        System.arraycopy(vectorhex, 8, nuevoVector, 0, tam);
        return nuevoVector;
    }    
    
    public String[] sacarDatosWrapper(String[] vectorhex, int inicio) {
        int tam = Integer.parseInt( ( vectorhex[6] + vectorhex[7] ), 16 ) & 0xFFFF;
        String nuevoVector[] = new String[tam - inicio];
        System.arraycopy(vectorhex, 8 + inicio, nuevoVector, 0, tam);
        return nuevoVector;
    } 
    
    public int calculaDiasALeer(boolean regisdia, boolean regismes, boolean lpowerLost, ZoneId zid){
        if (med.getFecha() != null){//med.getFecha() != null) {
            Long ultimamed = (long) 1;
            Long actual = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime()).getTime();
            if (regisdia){
                    diasaleer = ndiaReg; //Número de días de registro diario
            } else if (regismes){
                    diasaleer = nmesReg*30; //Número de días de registro mensual
            } else if (lpowerLost){
                    diasaleer = ndiaseventos;
            } else {
                cp.escribir("Fecha Ultima Lectura: " + med.getFecha(), file);
                ////System.out.println("Fecha Ultima Lectura: " + med.getFecha());
                ultimamed = med.getFecha().getTime();
                fechalectura = ultimamed;                
                Long calculo = actual - ultimamed;
                diasaleer = (int) (calculo / 86400000);
                if (calculo % 86400000 > 0) {
                    diasaleer = diasaleer + 1;
                }
            }
            cp.escribir("Numero de dias calculados hasta la fecha actual: " + diasaleer, file);
            ////System.out.println("Numero de dias calculados hasta la fecha actual: " + diasaleer);
            if (diasaleer > 0) {
                    if ((diasaleer > 30) && !regismes) { //se debe saber si para registros diarios también aplica
                        ndias = 30;
                        cp.escribir("Numero de dias a leer no debe superar los 30 dias ", file);
                        ////System.out.println("Numero de dias a leer no debe superar los 30 dias ");
                    } else {
                        ndias = diasaleer;
                    }
            } 
            cp.escribir("Numero de dias a leer: " + (ndias), file);
            ////System.out.println("Numero de dias a leer: " + (ndias));
        }
        return ndias;
    }

    public String getErrorString(StackTraceElement ste[], int depthOfError) {
        String error = "";
        int len = ste.length >= depthOfError ? depthOfError : ste.length;
        for (int i = 0; i < len; i++) {
            error += ste[i];
        }
        return error;
    }
    
    public void escribir(String msj) {
        cp.escribir(msj, file);
    }

    public long getFechalectura() {
        return fechalectura;
    }

    public void setFechalectura(long fechalectura) {
        this.fechalectura = fechalectura;
    }
  
     public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte[] getEndFrame() {
        return endFrame;
    }

    public void setEndFrame(byte[] endFrame) {
        this.endFrame = endFrame;
    }   
}
