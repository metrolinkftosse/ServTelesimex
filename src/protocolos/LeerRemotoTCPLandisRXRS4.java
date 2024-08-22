/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotoLandisRXRS4;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LeerRemotoTCPLandisRXRS4 extends Thread {

    private Timestamp tiempoinicial; //inicio de comunicacion
    private Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    private boolean rutinaCorrecta = false;
    private InputStream input;
    private OutputStream output;
    private String cadenahex = "";
    private TramasRemotoLandisRXRS4 tramasLandisRXRS4 = new TramasRemotoLandisRXRS4();
    private boolean aviso = false;
    private int indx = 0;
    private int idxSimpleMsg = 0;
    private String password = "";
    EMedidor med;
    ControlProcesos cp;
    private final boolean lperfil;
    private final boolean leventos;
    private final boolean lregistros;
    private final boolean lconfhora;
    public boolean leer = true;
    private String numeroPuerto;
    private int numeroReintentos = 4;
    private int velocidadPuerto;
    private long timeout;
    private int nhoras;
    boolean portconect = false;
    private int actualReintento = 0;
    Thread tEscritura = null;
    Thread tReinicio = null;
    public boolean cierrapuerto = false;
    private Socket socket;
    private volatile boolean escucha = true;
    private Thread tLectura;
    private int reintentoconexion = 0;
    //Estados
    private boolean leido = true;
    private boolean commandI = false;
    private boolean commAddress = false;
    private boolean directionability = false;
    private boolean readSecurity = false;
    private boolean errorCodes = false;
    private boolean firmware = false;
    private boolean touOptions = false;
    private boolean lpOptions = false;
    private boolean serial = false;
    private boolean fecha = false;
    private boolean wFecha = false;    
    private boolean hora = false;
    private boolean wHora = false;
    private boolean loadProfileConfig = false;
    private boolean profileElements = false;
    private boolean kFactor = false;
    private boolean transFactor = false;
    private int kbToRequest = 2;
    private int expBytes = 16;
    private int nBytes = 0;
    private boolean loadProfile = false;
    private boolean logout = false;
    private boolean enviando = false;
    private boolean reenviando = false;
    private byte[] ultimatramaEnviada = null;
    private byte[] idASCII = {(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30};
    private ArrayList <Object[]>  profileElementsV = new ArrayList <>();     

    SimpleDateFormat sdfComplete = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
    SimpleDateFormat sdfJustDate = new SimpleDateFormat("yyyy-MM-dd");   
    private int numCanales = 2;
    private int intervalos = 15;
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    boolean seEsperaACK = false;
    boolean haveNoise = false;
    public boolean tramaOK = false;
    public boolean uncomplete = false;
    public boolean delayed = false;
    public boolean empty = false;
    private String[] fragment1;
    private String[] fragment2;
    private int[] errorCodesVI = new int[7];
    private float firmwareF;
    private String[] touOptionsVS = new String[3];
    private String LP_Options;
    private String serialObtenido;
    private String seg, min, horas, dia, mes, año;
    private String fechaOb, horaOb;
    private String logoutMsg = "";
    private int bitsXChannel, bytesXChannel = 2, ncanales, intervalo;
    private float kFactorF, transFactorF;
    private String[] loadProfileArray;
    private int reinicio = 0;       
    public String fechaactual;
    private Timestamp fechaULec_JustDate;
    private Timestamp fechaactualTS;    
    private long desfase;
    private String seriemedidor = "";
    private byte[] trama;
    private byte[] contacting_1 = {(byte) 0x55};
    private byte[] contacting_2 = {(byte) 0xAA};
    private byte[] ACK = {(byte) 0x33};
    private byte[] nextBlock = {(byte) 0x66};
    private byte[] NACK = {(byte) 0x99};
    private byte[] DENY = {(byte) 0xCC};
    private int intentosNAK = 0;
    private int intentosReenvio = 0;
    private int intentosContacting = 0;
    private SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    private boolean perfilincompleto = false;
    public Abortar objabortar;
    private String usuario = "admin";
    private Timestamp time1, time2 = null;
    private long ndesfasepermitido = 7200;
    private String dirfis = "0";
    boolean solicitar; //variable de control de la sync

    private ZoneId zid;

    private final Object monitor = new Object();
    private String label = "LeerTCPLandisRXRS4";

    public LeerRemotoTCPLandisRXRS4(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) {
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
        lregistros = false;
        this.lconfhora = false;
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
            password = lconfhora ? med.getPassword2() : med.getPassword();
            timeout = med.getTimeout() * 1000;
            seriemedidor = med.getnSerie();
            nhoras = med.getNdias();
            numCanales = med.getNcanales();
            dirfis = med.getDireccionFisica() == null ? "000000" : med.getDireccionFisica();
            dirfis = reverseStr(dirfis);
            while (dirfis.length() < 6) {
                dirfis = dirfis + "0";
            }
            int idxDir = 0;
            for (String ch : dirfis.split("")) {
                idASCII[idxDir] = (byte) (Integer.parseInt(ch) + 48);
                idxDir++;
            }
            abrePuerto();
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
    
    private void escucha() {
        try {
            synchronized (monitor) {
                escucha = true;
                while (escucha) {
                    monitor.wait();
                        if (commandI) {
                            procesaSimpleMsg();
                        } else {
                            procesaCadena();
                        }                    
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

    private void iniciacomunicacion() throws InterruptedException, Exception {
        reiniciarEstados();
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
                enviaContactingDevice();
            } else {
                interrumpirHilo(tLectura);
                escribir("Medidor no configurado");
                cerrarPuerto(false);
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion medidor no configurado");
                cerrarLog("Medidor no configurado", false);
                leer = false;
            }
        }
    }

    private void procesaSimpleMsg() {
        byte[] readBuffer = new byte[2047];
        int numbytes = 0;
        boolean expTime = true;
        try {
            synchronized (monitor) {
                monitor.wait(1000);
                long startT = System.currentTimeMillis();
                long endT = startT + timeout;
                while (!socket.isClosed() && System.currentTimeMillis() < endT) {
                    monitor.wait(200);
                    if (input.available() > 0) {
                        numbytes = input.read(readBuffer);
                        if (numbytes > 1) {
                            if (search(readBuffer, trama[0])) {
                                expTime = false;
                                break;
                            }
                        } else if (numbytes == 1) {
                            if ((trama[0] == contacting_1[0] && readBuffer[0] == contacting_1[0]) || (trama[0] == contacting_2[0] && readBuffer[0] == contacting_2[0]) || (readBuffer[0] == ACK[0]) || (readBuffer[0] == NACK[0]) || (readBuffer[0] == DENY[0]) ) {
                                expTime = false;
                                break;
                            }
                        }
                    }
                }
                if (!socket.isClosed() && expTime) {
                    if (commAddress && intentosContacting == 2) {
                        intentosContacting = 0;
                        enviando = false;
                        reenviando = false;
                        monitor.notifyAll();
                        monitor.wait(150);
                        enviaCommAddress();
                    } else {
                        rutinaCorrecta = true;
                        reenviando = true;
                        enviando = false;
                        monitor.notifyAll();
                    }      
                    return;
                } else {
                    enviando = false;
                    reenviando = false;
                    monitor.notifyAll();
                }
                cadenahex = tramasLandisRXRS4.encode(readBuffer, numbytes);                
                escribir("Recibe <= " + cadenahex);
                if (cadenahex.length() > 0) {
                    if (trama[0] == contacting_1[0]) {
                        monitor.wait(200);
                        if ( !(readBuffer[idxSimpleMsg] == ACK[0]) ){                           
                            trama = contacting_2;
                        }                                                
                        enviaTrama2(trama, "");
                    } else if (trama[0] == contacting_2[0]) {
                        commandI = false;
                        stateSelector();                        
                    } else if (directionability) {
                        commandI = false;
                        interpretaCadena(cadenahex);
                    } else if (wFecha || wHora) {
                        commandI = false;
                        interpretaCadena(cadenahex);
                    } else if (logout) {
                        commandI = false;
                        interpretaCadena(cadenahex);
                    }
                }
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

    private boolean search(byte[] arrayByte, byte base) {        
        for (byte toComp : arrayByte) {
            if (base == toComp) {
                return true;
            }
            idxSimpleMsg ++;
        }
        return false;
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
                        if (hora) {time2 = obtenerHora();}
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
                            if (tempBuffer[0] == ACK[0]) {
                                readBuffer[0] = tempBuffer[0];
                                numbytes = auxNumbytes;
                            } else if (tempBuffer[0] == NACK[0] || tempBuffer[0] == DENY[0]) {
                                expTime = false;
                                readBuffer[0] = tempBuffer[0];
                                numbytes = auxNumbytes;
                                break;
                            } else {
                                readBuffer[0] = tempBuffer[0];
                                numbytes = auxNumbytes;
                                break;
                            }
                        }
                    }
                }
                if (!socket.isClosed() && expTime) {
                    dataExtBuffer = esperaCadena();
                    boolean reenvio = (boolean) dataExtBuffer[2];
                    if (reenvio) {
                        rutinaCorrecta = true;
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
                cadenahex = tramasLandisRXRS4.encode(auxBuffer, tempNumBytes);
            } else {
                cadenahex = tramasLandisRXRS4.encode(readBuffer, numbytes);
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
                            if (tempBuffer[0] == ACK[0]) {
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
                                    if (loadProfile) {
                                        escribir("TimeOut Perfil de Carga, espera un periodo mas de Timeout");
                                    } else {
                                        escribir("TimeOut, Intento de reenvio..");  
                                    }                                    
                                }
                                escribir(des);
                                if (trama != null) {
                                    ultimatramaEnviada = trama;
                                    escribir("=> " + tramasLandisRXRS4.encode(trama, trama.length));
                                }
                                try {
                                    monitor.notifyAll();                                    
                                    enviaTrama(reenviando? loadProfile? NACK : trama : trama);
                                    monitor.wait();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    escribir("Error enviando trama tipo 2_2");
                                }
                            } else {
                                t = false;
                            }
                            if (reenviando && intentosRetransmision <= 2 ) {
                                intentosRetransmision++;
                                if (commandI && commAddress) {
                                    intentosContacting = intentosRetransmision;
                                }
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
                            if (loadProfile & perfilincompleto) {
                                //guardamos lecturas hasta donde esten                                
                                almacenaPerfilIncompleto();                                
                                med.MedLeido = true;
                                cerrarLog("Leido Incompleto", true);
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
            if (bytes != null) {
                output.write(bytes, 0, bytes.length);
                output.flush();
            } else {
                escribir("Es necesario sensar el buffer de escucha nuevamente para completar trama o para capturar tramas desfasadas");
            }

        } catch (Exception e) {
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

    public void reiniciaComunicacion(boolean sendBye) {
        try {
            escribir("Reinicia Comunicacion");
            cerrarPuerto(sendBye);
            Thread.sleep(1000);
            if (loadProfile & perfilincompleto) {
                //guardamos lecturas hasta donde esten                
                almacenaPerfilIncompleto();
            }
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
            byte trama[] = tramasLandisRXRS4.getLogout();
            escribir("Envia Logout sin espera de respuesta");
            escribir("=> " + tramasLandisRXRS4.encode(trama, trama.length));
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

    private void interpretaCadena(String cadenahex) throws ParseException {
        try {
            escribir("<= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (directionability) {
                revisarDireccionabilidad(vectorhex);
            } else if (readSecurity) {
                revisarReadSecurity(vectorhex);
            } else if (errorCodes) {
                revisarErrorCodes(vectorhex);
            } else if (firmware) {
                revisarFirmware(vectorhex);
            } else if (touOptions) {
                revisarTOU_Options(vectorhex);
            } else if (lpOptions) {
                revisarLP_Options(vectorhex);
            } else if (serial) {
                revisarSerial(vectorhex);
            } else if (fecha) {
                revisarFecha(vectorhex);
            } else if (hora) {
                revisarHora(vectorhex);
            } else if (wFecha) {
                revisarWFecha(vectorhex);
            } else if (wHora) {
                revisarWHora(vectorhex);
            } else if (loadProfileConfig) {
                revisarLoadProfileConfig(vectorhex);
            } else if (profileElements) {
                revisarProfileElements(vectorhex);
            } else if (kFactor) {
                revisarKFactor(vectorhex);
            } else if (transFactor) {
                revisarTransFactor(vectorhex);
            } else if (loadProfile) {
                revisarLoadProfile(vectorhex);
            } else if (logout) {
                revisarLogout(vectorhex);
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

    private void stateSelector() throws ParseException {
        intentosReenvio = 0;
        intentosNAK = 0;
        if (directionability) {
            enviaDireccionabilidad();
        } else if (readSecurity) {
            enviaReadSecurity();
        } else if (errorCodes) {
            enviaErrorCodes();
        } else if (firmware) {
            enviaFirmware();
        } else if (touOptions) {
            enviaTOU_Options();
        } else if (lpOptions) {
            enviaLP_Options();
        } else if (serial) {
            enviaSerial();
        } else if (fecha) {
            enviaFecha();
        } else if (hora) {
            enviaHora();
        } else if (wFecha) {
            enviaWFecha();
        } else if (wHora) {
            enviaWHora();
        } else if (loadProfileConfig) {
            enviaLoadProfileConfig();
        } else if (profileElements) { //fecha actual 2
            enviaProfileElements();
        } else if (kFactor) {
            enviaKFactor();
        } else if (transFactor) {
            enviaTransFactor();
        } else if (loadProfile) {
            enviaLoadProfile();
        } else if (logout) {
            enviaLogout();
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
            System.err.println(e.getMessage());//vic 13-08-19
            try {
                fr.close();
            } catch (Exception exc) {
                System.err.println(exc.getMessage());
            }
        }
    }
    
    private void revisaTrama(String[] vectorhex) {
        tramaOK = false;
        uncomplete = false;
        delayed = false;
        seEsperaACK = vectorhex[0].equals("33");
        int indxlen = (seEsperaACK ? 1 : 0);
        int indxSpare = (seEsperaACK ? 4 : 3);
        int expectedLen = indxlen + getExpectedLen();
        boolean validaCRC;

        if ((vectorhex.length - indxSpare) == (Integer.parseInt((vectorhex[indxlen]), 16))) { //validamos campo len
            validaCRC = validaCheckSum(vectorhex, indxlen);
            if (validaCRC) {//validamos CRC
                if (vectorhex.length == expectedLen) {
                    tramaOK = true;
                } else {
                    escribir("Respuesta no adecuada al estado actual");
                }
            } else {// CRC
                escribir("BAD CRC");
            }
        } else {// LEN    
            escribir("Error longitud de trama");
            // Indica si la trama que llego es más corta que el length definitido en la cabecera de la trama por tanto incompleta.
            boolean shorter = (vectorhex.length - indxSpare) < Integer.parseInt(vectorhex[indxlen], 16);
            if (shorter) {
                escribir("Trama incompleta");
                fragment1 = vectorhex;
                uncomplete = true;
            }  // No tenemos una bandera para hacer el split por tanto se solicita reenvío con un 99                                    
        }

    }

    private void enviaContactingDevice() {        
        String msg;        
        trama = contacting_1;
        commandI = true;
        commAddress = true;
        directionability = true;
        msg = "Comando I";
        enviaTrama2(trama, msg);
    }
    
    private void enviaCommAddress() throws InterruptedException {
        String msg;
        trama = tramasLandisRXRS4.getCommAddress();
        int idxTrama = 2;
        for (byte asciiCH : idASCII) {
            trama[idxTrama] = asciiCH;
            idxTrama++;
        }
        msg = "Communication Address";
        commAddress = false;
        synchronized(monitor) {
            escribir(msg);
            escribir("=> " + tramasLandisRXRS4.encode(trama, trama.length));
            enviaTrama(trama);
            monitor.wait(20);
        }
        trama = contacting_1;
        enviaTrama2(trama, msg);        
    }

    private void enviaDireccionabilidad() {
        String msg = "Direccionabilidad";
        trama = tramasLandisRXRS4.getDirectionability();
        commandI = true;
        try {            
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wFecha) {
            trama[4] = (byte) 0x09;
            trama[5] = (byte) 0x04;
            trama = generaChecksum(trama);
        }        
        enviaTrama2(trama, msg);
    }

    private void revisarDireccionabilidad(String[] vectorhexO) {
        if (Integer.parseInt(vectorhexO[0], 16) == ACK[0]) {
            directionability = false;
            commandI = true;
            readSecurity = !(wFecha || wHora);
            if (wFecha) {
                enviaWFecha();
            } else if (wHora) {
                enviaWHora();
            } else {
                trama = contacting_1;
                String msg = "Comando I";
                enviaTrama2(trama, msg);
            }                                                                        
        } else {
            reiniciaComunicacion(true);
        }
    }

    private void enviaReadSecurity() {
        String msg = "Read Security";
        trama = tramasLandisRXRS4.getReadSecurity();
        enviaTrama2(trama, msg);
    }

    private void revisarReadSecurity(String[] vectorhexO) {
        String peticion = "Read Security";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            //if (Integer.parseInt(vectorhexO[indxData]) < 2) {//EN EL LSB va la bandera de Red (Network) 0:Disabled, 1: Enabled, Sin embargo no implica nada hasta donde se ha visto
                escribir("Read Security Disabled");
                escribir("Network " + ((Integer.parseInt(vectorhexO[indxData]) & 0x01) == 1 ? "Enabled" : "Disabled"));
                trama = contacting_1;
                readSecurity = false;
                commandI = true;
                errorCodes = true;
                String msg = "Comando I";
                enviaTrama2(trama, msg);
            //} else { // Se cree que habilita la bandera ReadSecurity, no se encontraron ejemplos en donde se usara una petición donde se envié contraseña.
                //RUTINA Para enviar contraseña
            //    escribir("Read Security Enabled");
            //}
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaErrorCodes() {
        String msg = "Error Codes";
        trama = tramasLandisRXRS4.getErrorCodes();
        enviaTrama2(trama, msg);
    }

    private void revisarErrorCodes(String[] vectorhexO) {
        String peticion = "Error Codes";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            for (int i = 0; i < 7; i++) {
                errorCodesVI[i] = Integer.parseInt(vectorhexO[i + indxData], 16);
            }
            escribir("Códigos de Error: " + Arrays.toString(errorCodesVI));
            trama = contacting_1;
            errorCodes = false;
            commandI = true;
            firmware = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaFirmware() {
        String msg = "Firmware";
        trama = tramasLandisRXRS4.getFirmware();
        enviaTrama2(trama, msg);
    }

    private void revisarFirmware(String[] vectorhexO) {
        String peticion = "Firmware";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            firmwareF = Float.parseFloat(vectorhexO[indxData].substring(0, 1) + "." + vectorhexO[indxData].substring(1, 2) + vectorhexO[indxData + 1].substring(0, 1) + vectorhexO[indxData + 1].substring(1, 2));
            escribir("Firmware Version: " + firmwareF);
            trama = contacting_1;
            firmware = false;
            commandI = true;
            touOptions = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaTOU_Options() {
        String msg = "TOU Options";
        trama = tramasLandisRXRS4.getTouOptions();
        enviaTrama2(trama, msg);
    }

    private void revisarTOU_Options(String[] vectorhexO) {
        String peticion = "TOU Options";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            for (int i = 0; i < 3; i++) {
                touOptionsVS[i] = vectorhexO[i + indxData];
            }
            escribir("TOU Options: " + Arrays.toString(touOptionsVS));
            trama = contacting_1;
            touOptions = false;
            commandI = true;
            lpOptions = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaLP_Options() {
        String msg = "LP Options";
        trama = tramasLandisRXRS4.getLpOptions();
        enviaTrama2(trama, msg);
    }

    private void revisarLP_Options(String[] vectorhexO) {
        String peticion = "LP Options";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            LP_Options = vectorhexO[indxData];
            escribir("TOU Options: " + LP_Options);
            trama = contacting_1;
            lpOptions = false;
            commandI = true;
            serial = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaSerial() {
        String msg = "Serial";
        trama = tramasLandisRXRS4.getSerial();
        enviaTrama2(trama, msg);
    }

    private void revisarSerial(String[] vectorhexO) {
        String peticion = "Serial";
        serialObtenido = "";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            boolean continuar = false;
            for (int i = vectorhexO.length - 3; i > indxData - 1; i--) {
                if (!vectorhexO[i].equals("20")) {
                    serialObtenido += vectorhexO[i];
                } else {
                    break;
                }
            }
            serialObtenido = Hex2ASCII(serialObtenido);
            escribir("Serial obtenido: " + serialObtenido);
            if (Long.parseLong(seriemedidor) == Long.parseLong(serialObtenido)) {
                continuar = true;
            }
            if (continuar) {
                trama = contacting_1;
                serial = false;
                commandI = true;
                fecha = true;
                String msg = "Comando I";
                enviaTrama2(trama, msg);
            } else {                
                trama = contacting_1;
                logoutMsg = "Serial";
                leido = false;
                serial = false;
                commandI = true;
                logout = true;
                String msg = "Logout";
                enviaTrama2(trama, msg);
            }
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaFecha() {
        String msg = "Fecha";
        trama = tramasLandisRXRS4.getFecha();
        enviaTrama2(trama, msg);
    }

    private void revisarFecha(String[] vectorhexO) {
        String peticion = "Fecha";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            año = "20" + vectorhexO[indxData + 1];            
            mes = vectorhexO[indxData + 3];
            dia = vectorhexO[indxData + 2];
            fechaOb = año + "-" + mes + "-" + dia;
            escribir("Fecha Obtenida: " + fechaOb);
            trama = contacting_1;
            fecha = false;
            commandI = true;
            hora = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaHora() {
        String msg = "Hora";
        trama = tramasLandisRXRS4.getHora();
        time1 = obtenerHora();
        enviaTrama2(trama, msg);
    }

    private void revisarHora(String[] vectorhexO) throws ParseException {
        String peticion = "Hora";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            horas = vectorhexO[indxData + 2];
            min = vectorhexO[indxData + 1];
            seg = vectorhexO[indxData];
            horaOb = horas + ":" + min + ":" + seg;
            escribir("Hora obtenida: " + horaOb);
            fechaactual = fechaOb + " " +horaOb;
            solicitar = true;
            try {
                fechaactualTS = new Timestamp(sdfComplete.parse(fechaactual).getTime());
                desfase = Math.abs((time2.getTime() - (fechaactualTS.getTime() + (time2.getTime() - time1.getTime())))) / 1000;
                escribir("Fecha Medidor: " + fechaactual);
                escribir("Fecha Actual Colombia: " + time2);
                escribir("Diferencia SEG NTP " + desfase);
                if (desfase > this.ndesfasepermitido) {
                    solicitar = false;
                    escribir("No se solicitara el perfil de carga");
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
                if (med.getFecha() != null) {
                    escribir("Fecha Ultima Lectura: " + med.getFecha());
                    long diffInMillies = Math.abs(obtenerHora().getTime() - med.getFecha().getTime());
                    long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    nhoras = (int) diff + 1;
                    escribir("Numero de horas a leer calculadas: " + nhoras);
                }
                String msg = "Comando I";
                trama = contacting_1;
                hora = false;
                commandI = true;
                if (lconfhora) {
                    directionability = true;
                    wFecha = true;
                //} else if (leventos) { //Continua con Perfil
                    //RUTINA PARA ENCONTRAR LOS EVENTOS DEL MEDIDOR
                } else if (lperfil) {
                    loadProfileConfig = true;
                } else {
                    escribir("No se ha seleccionado ninguna opción");
                    logout = true;
                }
                enviaTrama2(trama, msg);
            } else {
                trama = contacting_1;
                logoutMsg = "Desfasado";
                leido = false;
                hora = false;
                commandI = true;
                logout = true;
                String msg = "Logout";
                enviaTrama2(trama, msg);
            }        
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }
    
    private void enviaWFecha() {
        String msg = "Write Fecha";
        trama = tramasLandisRXRS4.getFechaW();
        Timestamp currentTS = this.obtenerHora();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTS.getTime());
        String  year = ""+ (cal.get(Calendar.YEAR) & 0xFFFF); 
        trama[1] = (byte) (Integer.parseInt(""+ (cal.get(Calendar.DAY_OF_WEEK) & 0xFF), 16));
        trama[2] = (byte) (Integer.parseInt( year.substring(2, 4), 16));
        trama[3] = (byte) (Integer.parseInt(""+ (cal.get(Calendar.DAY_OF_MONTH) & 0xFF), 16));
        trama[4] = (byte) (Integer.parseInt(""+ ( (cal.get(Calendar.MONTH) + 1 ) & 0xFF), 16));
        trama = generaChecksum(trama);
        enviaTrama2(trama, msg);
    }
    
    private void revisarWFecha(String[] vectorhexO) {
        if (Integer.parseInt(vectorhexO[0], 16) == ACK[0]) {
            wFecha = false;
            directionability = true;
            wHora = true;
        } else {
            logoutMsg = "Escritura Fecha Denegada";
            leido = false;
            logout = true;
        }
        trama = contacting_1;
        wFecha = false;
        commandI = true;;
        String msg = "Comando I";
        enviaTrama2(trama, msg);               
    }
    
    private void enviaWHora() {
        String msg = "Write Hora";
        trama = tramasLandisRXRS4.getHoraW();
        Timestamp currentTS = this.obtenerHora();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTS.getTime());
        trama[1] = (byte) (Integer.parseInt(""+ (cal.get(Calendar.SECOND) & 0xFF), 16));
        trama[2] = (byte) (Integer.parseInt(""+ (cal.get(Calendar.MINUTE) & 0xFF), 16));
        trama[3] = (byte) (Integer.parseInt(""+ (cal.get(Calendar.HOUR_OF_DAY) & 0xFF), 16));
        trama = generaChecksum(trama);
        enviaTrama2(trama, msg);
    }

    private void revisarWHora(String[] vectorhexO) {
        if ( !( Integer.parseInt(vectorhexO[0], 16) == ACK[0] ) ) {
            logoutMsg = "Escritura Hora Denegada";
            leido = false;
        } 
        trama = contacting_1;
        wHora = false;
        commandI = true;
        logout = true;
        String msg = "Comando I";
        enviaTrama2(trama, msg);
    }
    
    private void enviaLoadProfileConfig() {
        String msg = "Load Profile Configuration";
        trama = tramasLandisRXRS4.getLoadProfileConfig();         
        enviaTrama2(trama, msg);
    }
    
    private void revisarLoadProfileConfig(String[] vectorhexO) {
        String peticion = "Load Profile Configuration";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {            
            ncanales = Integer.parseInt(vectorhexO[indxData + 1], 16);
            escribir("Número de canales: " + ncanales);
            intervalo = Integer.parseInt(vectorhexO[indxData + 2]);
            escribir("Duración de Intervalo: " + intervalo + " minutos.");
            bitsXChannel = Integer.bitCount( Integer.parseInt( reverseStr(vectorhexO[indxData + 3] + vectorhexO[ indxData + 4]), 16 ) & 0xFFFF );
            escribir("Número de bits por canal: " + bitsXChannel);
            escribir("Número de Bytes por canal: " + bytesXChannel);
            trama = contacting_1;
            loadProfileConfig = false;
            commandI = true;
            profileElements = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }
    
    private void enviaProfileElements() {
        String msg = "Profile Elements";
        trama = tramasLandisRXRS4.getProfileElements();
        enviaTrama2(trama, msg);
    }
    
    private void revisarProfileElements(String[] vectorhexO) {
        String peticion = "Profile Elements";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);        
        if (tramaOK && !uncomplete && !delayed) { 
            Object[] data;
            int codCanal;
            escribir("Elementos del Perfil de Carga");
            for (int i = indxData; i < (indxData + ncanales); i++) {
                codCanal = Integer.parseInt(vectorhexO[i], 16);
                escribir("Código canal: " + codCanal + ", Unidad: " + unitSelector(codCanal));
                data = new Object[3];
                data[0] = codCanal;
                data[1] = unitSelector(codCanal);
                data[2] = false;
                profileElementsV.add(data);
            }
            escribir("Elementos de Perfil de Carga: ");
            trama = contacting_1;
            profileElements = false;
            commandI = true;
            kFactor = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }            
    
    private void enviaKFactor() {
        String msg = "K Factor";
        trama = tramasLandisRXRS4.getkFactor();
        enviaTrama2(trama, msg);
    }
    
    private void revisarKFactor(String[] vectorhexO) {
        String peticion = "K Factor";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            kFactorF = Float.parseFloat(vectorhexO[indxData + 1]) / 10;
            escribir("Factor K: " + kFactorF);            
            trama = contacting_1;
            kFactor = false;
            commandI = true;
            transFactor = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }

    private void enviaTransFactor() {
        String msg = "Transformation Factor";
        trama = tramasLandisRXRS4.getTransFactor();
        enviaTrama2(trama, msg);
    }
    
    private void revisarTransFactor(String[] vectorhexO) {
        String peticion = "Transformation Factor";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) {
            transFactorF = Float.parseFloat(vectorhexO[indxData]);
            escribir("Factor de Transformación: " + transFactorF);            
            trama = contacting_1;
            transFactor = false;
            commandI = true;
            loadProfile = true;
            String msg = "Comando I";
            enviaTrama2(trama, msg);
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }      
    
    private void enviaLoadProfile() throws ParseException {
        String msg = "Perfil de Carga";
        trama = tramasLandisRXRS4.getLoadProfile();
        kbToRequest = calcularKB();
        nBytes = 0;
        expBytes = kbToRequest*1024;
        loadProfileArray = new String[expBytes];       
        escribir("Número de Bytes Esperados: " + expBytes);        
        trama[1] = (byte) (Integer.parseInt(""+ (kbToRequest & 0xFF), 16));
        trama = generaChecksum(trama);
        enviaTrama2(trama, msg);
    }

    private void revisarLoadProfile(String[] vectorhexO) {
        String peticion = "Perfil de Carga";
        if (uncomplete) {
            fragment2 = vectorhexO;
            vectorhexO = concatFrames(fragment1, fragment2);
        }
        revisaTrama(vectorhexO);
        int indxData = (seEsperaACK ? 2 : 1);
        if (tramaOK && !uncomplete && !delayed) { 
            perfilincompleto = true;
            int lengthData = (Integer.parseInt(vectorhexO[indxData - 1], 16)  & 0xFF);
            escribir("Número de Bytes de data: "+lengthData);
            System.arraycopy(vectorhexO, indxData, loadProfileArray, nBytes, lengthData);           
            nBytes += lengthData;
            escribir(nBytes+" bytes de: "+expBytes);
            if (nBytes < expBytes) {                
                trama = nextBlock;
                String msg = "Next Block";
                enviaTrama2(trama, msg);
            } else {
                escribir("Perfil de carga Completo: " + Arrays.toString(loadProfileArray));
                trama = contacting_1;
                loadProfile = false;
                commandI = true;
                logout = true;
                String msg = "Logout";
                enviaTrama2(trama, msg);
            }            
        } else {
            if (vectorhexO[0].equals("" + NACK[0])) {
                escribir("Recibe NAK");
                handleForwarding(peticion);
            } else {
                escribir("Trama incorrecta");
                handleReject();
            }
        }
    }
    
    private void enviaLogout() {
        String msg = "Logout";
        commandI = true;
        trama = tramasLandisRXRS4.getLogout();
        enviaTrama2(trama, msg);
    }

    private void revisarLogout(String[] vectorhex) {
        String peticion = "Logout";
        tramaOK = false;
        logout = false;
        cerrarPuerto(false);
        escribir("Logout");
        cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Lectura OK");
        if (leido) {
            if (leventos) {
                //Rutina para procesar eventos
            }
            if (lregistros) {
                //Rutina para procesar registros
            }
            if (lperfil) {
                try {                    
                    escribir("Almacena Perfil de Carga");
                    cp.AlmacenaPerfilCargaLandisRXRS4(med.getnSerie(), med.getMarcaMedidor().getCodigo(), fechaULec_JustDate, time2, bytesXChannel, ncanales, intervalo, kFactorF, profileElementsV, loadProfileArray, file);
                } catch (Exception e) {
                    e.printStackTrace();
                    escribir("Error procesando o almacenando perfil");
                    escribir("Ultimo registro de volcado en: " + cp.fecVerificaVolcado + " con " + cp.intervVerificaVolcado + " intervalos");
                    reiniciaComunicacion(false);
                }
            }
            cerrarLog("Leido", true);
            leer = false;
        } else {
            cerrarPuerto(false);
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion Error: " + logoutMsg);
            cerrarLog("Desconexion Error: " + logoutMsg, false);
            leer = false;            
        }
        
    }

    private void almacenaPerfilIncompleto() {
        escribir("Almacena Perfil de Carga");
        cp.AlmacenaPerfilCargaLandisRXRS4(med.getnSerie(), med.getMarcaMedidor().getCodigo(), fechaULec_JustDate, time2, bytesXChannel, ncanales, intervalo, kFactorF, profileElementsV, loadProfileArray, file);
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
            /*
            System.err.println(e.getMessage());
            System.err.println("" + e.getStackTrace()[0]);
            System.err.println("" + e.getStackTrace()[1]);
            System.err.println("" + e.getStackTrace()[2]);
            */
        }
        try {
            tReinicio.interrupt();
        } catch (Exception e) {
            /*
            System.err.println(e.getMessage());
            System.err.println("" + e.getStackTrace()[0]);
            System.err.println("" + e.getStackTrace()[1]);
            System.err.println("" + e.getStackTrace()[2]);
            */
        }              
        tEscritura = null;
        tReinicio = null;
    }
    
    private String unitSelector(int codCanal) {
        switch (codCanal) {
            case 0:
                return "kWhD";
            case 1:
                return "kWhR";
            case 6:
                return "kVArhD";
            case 7:
                return "kVArhR";            
            case 8:
                return "kW";
            case 9:
                return "kW";
            case 10:    
                return "kW";
            case 12:
                return "kW";
            case 13:
                return "kW";
            case 14:
                return "kW";
            default:  
                return "No Unit";
        }
    }

    private int getExpectedLen() {
        if (readSecurity) {
            return 4;
        } else if (errorCodes) {
            return 10;
        } else if (firmware) {
            return 5;
        } else if (touOptions) {
            return 6;
        } else if (lpOptions) {
            return 4;
        } else if (serial) {
            return 21;
        } else if (fecha) {
            return 7;
        } else if (hora) {
            return 6;
        } else if (loadProfileConfig) {
            return 8;
        } else if (profileElements) {
            return 18;
        } else if (kFactor) {
            return 6;
        } else if (transFactor) {
            return 7;
        } else if (loadProfile) {
            return 131;
        } else {
            return 1;
        }
    }

    private boolean validaCheckSum(String[] data, int inicio) {
        try {
            //verificamos si es de longitud fija
            int dato = 0;
            for (int i = inicio; i < data.length - 2; i++) {
                dato = dato + Integer.parseInt(data[i], 16);
            }
            dato = dato & 0xFFFF;
            return (dato == Integer.parseInt((data[data.length - 1] + data[data.length - 2]), 16));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private byte[] generaChecksum(byte[] trama) {
        int checkSum = 0;
        for (int i = 0; i < trama.length - 2; i++) {
            checkSum += (trama[i] & 0xFF);
        }
        checkSum = checkSum & 0xFFFF;
        trama[trama.length - 2] = (byte) (checkSum & 0xFF);
        trama[trama.length - 1] = (byte) ((checkSum >> 8) & 0xFF);
        return trama;
    }   

    private Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
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
    
    private String reverseStr(String str) {
        String nstr="";
        char ch;              
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i); //extracts each character
            nstr = ch + nstr; //adds each character in front of the existing string
        }
        return nstr;
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
    
    private int calcularKB() throws ParseException {
        Date dufecLec_Day = sdfComplete.parse(sdfJustDate.format( med.getFecha() )+ " 00:00:00" );
        fechaULec_JustDate = new Timestamp(dufecLec_Day.getTime());
        Date currentDate = sdfComplete.parse( fechaactual );
        long diffInMillies = Math.abs( currentDate.getTime() - dufecLec_Day.getTime() );
        long diff = TimeUnit.MINUTES.convert( diffInMillies, TimeUnit.MILLISECONDS );        
        int nIntervals = (int) diff/intervalo; 
        int nTimeStamps = nIntervals / ( 1440/intervalo );
        int nTotalBytes = (ncanales*bytesXChannel*nIntervals) + (nTimeStamps*2);
        boolean isFactor = nTotalBytes%1024 == 0;
        int kbApprox = (int) Math.ceil( (float) nTotalBytes/ (float) 1024 )  + ( isFactor? 1:0 );
        escribir("Minutos transcurridos: " + diff);
        escribir("Número de Intervalos Calculados: " + nIntervals);
        escribir("Número de Estampas de Tiempo Calculadas: " + nTimeStamps);
        escribir("Número total de Bytes Calculados: " + nTotalBytes);
        escribir("Número de KiloBytes Aproximados a solicitar: " +kbApprox);
        return kbApprox;
    }
    
    private void reiniciarEstados() {
        commandI = false;
        commAddress = false;
        directionability = false;
        readSecurity = false;
        errorCodes = false;
        firmware = false;
        touOptions = false;
        lpOptions = false;
        serial = false;
        fecha = false;
        hora = false;
        loadProfileConfig = false;
        profileElements = false;
        kFactor = false;
        transFactor = false;
        loadProfile = false;
        logout = false;
        enviando = false;
        reenviando = false;
    }
    
    
    private void handleForwarding(String peticion) {
        if (intentosReenvio < numeroReintentos) {
            intentosReenvio++;
            enviaTrama2(ultimatramaEnviada, "Solicitud de " + peticion);
        } else {
            intentosReenvio = 0;
            cerrarPuerto(true);
            escribir("Numero de reintentos agotado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado");
            escribir("Estado Lectura No leido");
            cerrarLog("Desconexion Numero de reintentos agotado", false);
            leer = false;
        }
    }

    private void handleReject() {
        seEsperaACK = false;
        if (intentosNAK < numeroReintentos) {
            intentosNAK++;
            seEsperaACK = false;
            if (uncomplete || delayed) {
                enviaTrama2(null, "Sensado Buffer");//No envía nada
            } else {
                enviaTrama2(NACK, "NACK");//Envia nak
            }
        } else {
            intentosNAK = 0;
            cerrarPuerto(true);
            escribir("Numero de reintentos agotado");
            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Desconexion numero de reintentos agotado");
            escribir("Estado Lectura No leido");
            cerrarLog("Desconexion Numero de reintentos agotado", false);
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