/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

/**
 *
 * @author dperez
 */
import Control.ControlProcesos;
import Datos.DES2;
import Datos.TramasRemotaTCPElgama;
import Entidades.Abortar;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.Electura;
import Util.SynHoraNTP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class LeerRemotoTCPElgama extends Thread {
    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    int reinicio = 0;
    boolean rutinaCorrecta = false;
    long tiempo = 500;
    String seriemedidor = "";
    boolean lhandshaking = false;
    InputStream input;
    OutputStream output;
    String cadenahex = "";
    TramasRemotaTCPElgama tramasElgama = new TramasRemotaTCPElgama();
    // public Logger loger;
    byte[] user;
    boolean aviso = false;
    int indx = 0;
    String password = "";
    String password2 = "";
    EMedidor med;
    ControlProcesos cp;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lconfHora;
    public boolean leer = true;
    String numeroPuerto;
    int numeroReintentos = 4;
    int nreintentos = 0;
    int velocidadPuerto;
    long timeout;
    int ndias;
    int nDiasI = 0;
    String fechaActual;
    String vecSerieMedidor[] = new String[6];
    boolean portconect = false;
    long tiemporetransmision = 0;
    int actualReintento = 0;
    int reintentoadp = 0;
    Thread port = null;
    Thread port2 = null;
    Thread port3 = null;
    int reintentosbadCRC = 0;
    boolean inicia1 = false;
    public boolean cierrapuerto = false;
    Socket socket;
    boolean escucha = true;
    Thread tLectura;
    private int reintentoconexion = 0;
    boolean llegotramaini = false;
    //Estados
    boolean inicio = false;
    boolean inicio2 = false;
    boolean inicio3 = false;
    boolean lregistroEventos1 = false;
    boolean lregistroEventos2 = false;
    boolean lperfilCarga = false;
    boolean lregistrodias = false;
    boolean lregistromes = false;
    boolean lP1 = false;
    boolean lP2 = false;
    boolean firstLP_Req = false;
    int version = 0; //version 0 es old, version 1 new
    Vector<String[]> powerfails = null;
    Vector<String[]> phasefails = null;
    ArrayList<String[]> loadProfile1;//canal 1
    ArrayList<String[]> loadProfile2;//canal 2
    ArrayList<String[]> loadProfile3;//canal 3
    ArrayList<String[]> loadProfile4;//canal 4
    Vector<String[]> regdias;//registro acumulados diarios
    Vector<String[]> regmeses;//registro acumulado mensual
    boolean enviando = false;
    boolean reenviando = false;
    byte[] ultimatramaEnviada = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfacceso = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    int numcanales = 2;
    int intervalo = 0;
    int factorIntervalo = 0;
    int adp = 1;
    int ndia = 3;
    int ndiaReg = 5; //numero de dias default para traer los acumulados por dias
    int ndiasRegSin = 0;
    int nmesReg = 3; //numero de meses default para traer los acumulados por mes
    int nmesRegSin = 0;
    int ncanal = 1;
    int parte = 0;
    int parteI = 0;
    double ktru = 0;
    double ktri = 0;
    int exponent = 0;
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    public String tramaIncompleta = "";
    public boolean complemento = false;
    public boolean tramabloqueo = true;
    //FileHandler fh;
    private boolean perfilincompleto = false;
    Abortar objabortar;
    String usuario = "admin";
    //campos para actualizacion de hora
    Timestamp time = null; //tiempo de NTP
    Timestamp todayNoonGlobal = null;
    Timestamp tsfechaactual;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    long ndesfasepermitido = 0;
    boolean solicitar; //variable de control de la sync
    long npeticionesConfHora = 0;
    long desfase;
    int valorDesfase = 50;
    ZoneId zid;
    private final String label =  "LeerTCPElGama";

    public LeerRemotoTCPElgama(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, ZoneId zid, long ndesfase) {
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
        if (lperfil) {
            lregistros = true;
        }
        lconfHora = false;
        this.aviso = aviso;
        this.objabortar = objabortar;        
        this.indx = indx;
        jinit();
    }

    private void jinit() {
        try {
            tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            timeout = (long) med.getTimeout() * 1000;
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            String serie = seriemedidor;
            while (serie.length() < 12) {
                serie = "0" + serie;
            }
            int p = 0;
            for (int j = 5; j >= 0; j--) {
                vecSerieMedidor[j] = serie.substring(p, p + 2);
                p += 2;
            }
            abrePuerto();
            tiempo = 2000;
        } catch (Exception e) {
            leer = false;
            escribir(getErrorString(e.getStackTrace(), 3));
        }
    }

        private void abrePuerto() {
        tiempoinicial = new Timestamp(Calendar.getInstance().getTimeInMillis());
        try {
            //verifica que no se ha abortado la comunicacion
            if (!objabortar.labortar) {
                if (reintentoconexion < numeroReintentos) { //3) { //parametrizable con reintentos de conf_medidor
                    //abre socket                
                    socket = new Socket(med.getDireccionip(), Integer.parseInt(med.getPuertoip()));                    
                    //establece timeout para conexion de 35 seg                    
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
                    //metodo para escuchar el puerto
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
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion por numero de reintentos");
                    escribir("Número de reintentos agotado");          
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    leer = false;
                }
            } else {
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), seriemedidor, label, "Desconexion Cancelacion de operacion");
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
            escucha = true;
            rutinaCorrecta = false;
            while (escucha) {
                if (!rutinaCorrecta) {
                    Thread.sleep(tiempo);
                    if (!rutinaCorrecta) {
                        procesaCadena();
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void iniciacomunicacion() throws Exception {
        tiemporetransmision = 15000 + tiempo;
        perfilincompleto = false;
        lperfilCarga = false;
        if (portconect) {
            if (med.isLconfigurado()) {
                try {
                    //se imprimen los dato de configuracion en el archivo traifile
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
                adp = 1;
                inicio = true;
                byte[] tramaelgama = tramasElgama.getLocal0();
                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                //adp
                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                int crcCalculado = CRCElgama(tramaelgama);
                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                while (calculo.length() < 4) {
                    calculo = "0" + calculo;
                }
                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                ultimatramaEnviada = tramaelgama;
                enviaTrama2_2(tramaelgama, "=> Solicitud de Configuracion");
            } else {
                interrumpirHilo(tLectura);
                escribir("Medidor no configurado");
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), label, "Medidor no configurado");
                cerrarPuerto();                
                cerrarLog("Medidor no configurado", false);
                leer = false;
            }
        }
    }

    private void procesaCadena() {
        //tamaño buffer
        byte[] readBuffer = new byte[256000];
        try {
            int numbytes = 0;
            //si el puerto tiene datos llenamos el buffer con lo que se encuentra en el puerto.
            while (input.available() > 0) {
                numbytes = input.read(readBuffer);
            }
            //codificamos las tramas que vienen en hexa para indetificar su contenido
            cadenahex = tramasElgama.encode(readBuffer, numbytes);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
            //luego de tener la trama desglosada byte x byte continuamos a interpretarla
            if (cadenahex.length() > 0) {
                //System.out.println("LLega dato");
                interpretaCadena(cadenahex);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
                            escribir("=> " + tramasElgama.encode(trama, trama.length));
                            //System.out.println(tramasElgama.encode(trama, trama.length));
                            try {
                                enviaTrama(trama);
                            } catch (Exception e) {
                            }
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
                            //System.out.println("Esperando... enviatrama2");
                            while (salir) {
                                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                                    //System.out.println("Sale enviar");
                                    salir = false;
                                    if (!enviando) {
                                        t = false;
                                    }
                                } else {
                                    intentosalir++;
                                    //System.out.print(".");
                                    sleep(500);
                                }
                            }
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;
                        cerrarPuerto();
                        if (lperfilCarga & perfilincompleto) {
                            AlmacenarRegistrosIncompletos();
                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion timeout");
                                reinicio = 0;
                                abrePuerto();
                            } else {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion Aborto de operacion");
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            try {                                
                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No leido");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion Numero de envios agotado");
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

    private void enviaTrama2_2(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;
        enviando = true;
        if (port2 != null) {
            if (port2.isAlive()) {
                port2.stop();
                port2 = null;
            }
        }
        port2 = new Thread() {
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
                            escribir("=> " + tramasElgama.encode(trama, trama.length));
                            //System.out.println(tramasElgama.encode(trama, trama.length));
                            try {
                                enviaTrama(trama);
                            } catch (Exception e) {
                            }
                        } else {
                            //System.out.println("Sale enviatrama2_2");
                            t = false;
                        }
                        if (intentosRetransmision > 4) {
                            escribir("Numero de reenvios agotado");
                            //System.out.println("Numero de reenvios agotado");
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {
                            intentosRetransmision++;
                            boolean salir = true;
                            int intentosalir = 0;
                            //System.out.println("Esperando... enviatrama2_2");
                            while (salir) {
                                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                                    //System.out.println("Sale enviar2_2");
                                    salir = false;
                                    if (!enviando) {
                                        t = false;
                                    }
                                } else {
                                    intentosalir++;
                                    //System.out.print(".");
                                    sleep(500);
                                }
                            }
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;
                        cerrarPuerto();
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion timeout");
                                reinicio = 1;
                                abrePuerto();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion Aborto de operacion");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            try {                                
                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No leido");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de envios agotado");
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    //System.err.println(e.getMessage());
                }
            }
        };
        port2.start();
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
                            escribir("=> " + tramasElgama.encode(trama, trama.length));
                            //System.out.println(tramasElgama.encode(trama, trama.length));
                            try {
                                enviaTrama(trama);
                            } catch (Exception e) {
                            }
                        } else {
                            //System.out.println("Sale enviatrama3");
                            t = false;
                        }
                        if (intentosRetransmision > 4) {
                            escribir("Numero de reenvios agotado");
                            //reiniciaComunicacion();
                            //System.out.println("Numero de reenvios agotado");
                            escribir(des);
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {
                            intentosRetransmision++;
                            boolean salir = true;
                            int intentosalir = 0;
                            //System.out.println("Esperando... enviatrama3");
                            while (salir) {
                                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                                    //System.out.println("Sale enviar3");
                                    salir = false;
                                    if (!enviando) {
                                        t = false;
                                    }

                                } else {
                                    intentosalir++;
                                    //System.out.println(".");
                                    sleep(500);
                                }
                            }
                            //sleep(tiemporetransmision);
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;

                        cerrarPuerto();
                        if (lperfilCarga & perfilincompleto) {
                            AlmacenarRegistrosIncompletos();
                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion timeout");
                                reinicio = 0;

                                abrePuerto();
                            } else {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion Proceso abortado");
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //escribir("Numero de reintentos agotado");
                            try {                                
                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No leido");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de envios agotado");
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

    private void enviaTrama1(byte[] bytes, String descripcion) {
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
                            escribir(des);
                            //System.out.println(des);
                            //System.out.println("Envia trama =>");
                            escribir("=> " + tramasElgama.encode(trama, trama.length));
                            //System.out.println(tramasElgama.encode(trama, trama.length));
                            enviaTrama(trama);
                            intentosRetransmision++;
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
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion timeout");
                                reinicio = 0;

                                abrePuerto();
                                //iniciacomunicacion();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion proceso abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //escribir("Numero de reintentos agotado");
                            try {                                

                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No leido");
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de envios agotado");
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

    private void enviaTramaUltima(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
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
                    while (t) {
                        if (reenviando) {
                            boolean salir = true;
                            int intentosalir = 0;
                            //System.out.println("Esperando... enviarTramaultima");
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
                                    //System.out.print(".");
                                    sleep(500);
                                }
                            }

                        } else {
                            //System.out.println("Sale enviatramaUltima");
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
                    //System.err.println(e.getMessage());
                }
            }
        };
        port3.start();
    }

    private void enviaTrama(byte[] bytes) {
        try {
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal           
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e.getMessage());
        }
        rutinaCorrecta = false;
        //System.out.println("Termina Envio");
        //System.out.println("\n");
    }

    public void reiniciaComuniacion() {
        try {
            enviando = false;
            Thread.sleep(2000);
            cerrarPuerto();
            if (lperfilCarga & perfilincompleto) {
                AlmacenarRegistrosIncompletos();
            }
            complemento = false;
            tramaIncompleta = "";
            if (numeroReintentos >= actualReintento) {
                if (!objabortar.labortar) {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion reinicio de comunicacion");
                    reintentosbadCRC = 0;
                    if (reinicio == 0) {
                        reinicio = 1;
                    } else {
                        reinicio = 0;
                    }
                    abrePuerto();
                } else {
                    cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion Proceso Abortado");
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {                
                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de reintentos agotado");
                cerrarLog("Desconexion Numero de reintentos agotado", false);
                leer = false;
            }
        } catch (Exception eex) {
            eex.printStackTrace();
        }
    }

    public void cerrarPuerto() {
        try {
            if (output != null) {
                //System.out.println("Cierra salida");
                output.flush();
                output.close();
            }
            if (input != null) {
                //System.out.println("Cierra entrada");
                input.close();
            }
        } catch (Exception p2) {
            System.err.println(p2.getMessage());
        }
        try {
            escucha = false;
            Thread.sleep(2000);
            if (socket != null) {
                //System.out.println("Cierra puerto");
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
            portconect = false;
            try {                
            } catch (Exception e) {
            }
            Thread.sleep((long) 10000);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void interpretaCadena(String cadenahex) {
        rutinaCorrecta = true;
        enviando = false;
        reenviando = false;
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        //System.out.println("\n");
        //System.out.println("Tramma llegada ");
        //System.out.println(cadenahex);
        //System.out.println("Fin Tramma llegada ");
        escribir("Recibe <= " + cadenahex);
        String[] vectorhex = cadenahex.split(" ");
        if (cadenahex.contains("0D 0A 4F 4B")) {
            byte[] trama = ultimatramaEnviada;
            //System.out.println(tramasElgama.encode(trama, trama.length));
            enviaTrama2(trama, "Solicitud de reenvio");
        } else if (cadenahex.contains("0D 0A 43 4F 4E 4E 45 43 54")) {
            byte[] trama = ultimatramaEnviada;
            //System.out.println(tramasElgama.encode(trama, trama.length));
            enviaTrama2(trama, "Solicitud de reenvio");
        } else if (inicio) {//la primera trama de configuracion
            if (!complemento) {//no es complemento
                if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                    if (Integer.parseInt(vectorhex[8], 16) == 1) {
                        //System.out.println("llega pregunta");
                        try {
                            if (vectorhex.length > 13) {
                                String vectorhexComplemento[] = new String[vectorhex.length - 13];
                                String cadenahexcomplemento = "";
                                for (int j = 0; j < vectorhexComplemento.length; j++) {
                                    if (j == 0) {
                                        cadenahexcomplemento = vectorhex[j + 13] + " ";
                                    } else {
                                        cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                    }
                                    vectorhexComplemento[j] = vectorhex[j + 13];
                                }
                                //System.out.println("trama cortada " + cadenahexcomplemento);
                                cadenahex = cadenahexcomplemento.trim();
                                vectorhex = vectorhexComplemento;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                            reintentoadp = 0;
                            if (Integer.parseInt(vectorhex[0], 16) > 19) { //es version nueva
                                if (Integer.parseInt(vectorhex[17], 16) < 5) {
                                    version = 0;
                                    //System.out.println("OLD");
                                    escribir("OLD version");
                                } else {
                                    escribir("NEW Version");
                                    //System.out.println("NEW");
                                    ktru = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhex[19], 16)) + "." + String.valueOf(Integer.parseInt(vectorhex[18], 16)));
                                    ktri = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhex[21], 16)) + "." + String.valueOf(Integer.parseInt(vectorhex[20], 16)));
                                    exponent = Integer.parseInt(vectorhex[22], 16);
                                    escribir("KTRU: " + ktru);
                                    escribir("KTRI: " + ktri);
                                    escribir("EXP: " + exponent);
                                    //System.out.println("KTRU: " + ktru);
                                    //System.out.println("KTRI: " + ktri);
                                    //System.out.println("EXP: " + exponent);
                                    version = 1;
                                }
                            } else {//es version vieja
                                version = 0;
                                //System.out.println("OLD");
                                escribir("OLD version");
                            }
                            solicitar = true;
                            deltatimesync2 = new Timestamp(Calendar.getInstance().getTimeInMillis());
                            intervalo = 60 / (Integer.parseInt(vectorhex[15], 16));
                            //System.out.println("Intervalo " + intervalo);
                            byte[] fecha = {(byte) (Integer.parseInt(vectorhex[12], 16) & 0xFF),
                                (byte) (Integer.parseInt(vectorhex[11], 16) & 0xFF),
                                (byte) (Integer.parseInt(vectorhex[10], 16) & 0xFF),
                                (byte) (Integer.parseInt(vectorhex[9], 16) & 0xFF)};
                            int year = (int) ((fecha[0] & 0xFE) >> 1);
                            int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                            int dia = (int) ((fecha[1] & 0x1F));
                            int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                            int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                            int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                            //System.out.println("Fecha actual: " + year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg);
                            fechaActual = year + "/" + completarCeros(month, 2) + "/" + completarCeros(dia, 2) + " " + completarCeros(hora, 2) + ":" + completarCeros(min, 2) + ":" + completarCeros(seg, 2);

                            //VALIDAMOS HORA ACTUAL DEL MEDIDOR CON SINCRONIZACION A LA SIC
                            try {
                                tsfechaactual = new Timestamp(sdf2.parse(fechaActual).getTime());
                                //System.out.println("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2) / 1000));
                                escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                                //System.out.println("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                                escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                                desfase = (time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000;
                                if (Math.abs(desfase) > ndesfasepermitido) {
                                    solicitar = false;
                                    escribir("No se solicitara el perfil de carga");
                                    //System.out.println("No se solicitara el perfil de carga");
                                }
                                cp.actualizaDesfase(((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000), med.getnSerie());
                                //System.out.println("Desfase Actualizado");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                escribir("Fecha Actual Estacion de trabajo " + new Timestamp(new Date().getTime()));
                                escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaActual).getTime()));
                                if (med.getFecha() != null) {
                                    Timestamp lastReadOffset = new Timestamp(med.getFecha().getTime() + (desfase * 1000));
                                    escribir("Fecha Ultima Lectura corregida con Offset: " + lastReadOffset);
                                    Date todayDate = new Date();
                                    String justDate = sdf5.format(todayDate) + " 00:00:00";
                                    Timestamp todayWithoutHours = new Timestamp(sdfacceso.parse(justDate).getTime());
                                    escribir("Last Read: " + lastReadOffset);
                                    escribir("Fecha hoy: " + todayWithoutHours);
                                    if (lastReadOffset.before(todayWithoutHours)) {// Si lastRead no es del día de hoy
                                        escribir("Last read que no es el del día de hoy.");
                                        //Como no es el del día de hoy, debemos calcular los días hacía atrás y la parte del día.
                                        Long millisegDiff = todayWithoutHours.getTime() - lastReadOffset.getTime();
                                        float nDiasDiff = (float) millisegDiff / 86400000f;
                                        escribir("Días de diferencia: " + nDiasDiff);
                                        ndias = (int) Math.ceil(nDiasDiff);
                                        parte = intervalo != 15 ? 0 : (nDiasDiff % 1) <= 0.5 ? 1 : 0;
                                    } else {
                                        escribir("Last read del día de hoy.");
                                        ndias = 0;
                                        Timestamp todayNoon = new Timestamp(todayWithoutHours.getTime() + (long) 43200000);
                                        todayNoonGlobal = todayNoon;
                                        escribir("Fecha Medio día del día actual: " + todayNoon);
                                        parte = intervalo != 15 ? 0 : (lastReadOffset.before(todayNoon) ? 0 : 1);
                                    }

                                    if (ndias > 0) {
                                        if (ndias > 35) {
                                            ndias = 35;
                                        }
                                    }
                                    
                                    ncanal = 1;
                                    nDiasI = ndias;
                                    parteI = parte;
                                    escribir("Días necesarios a solicitar: " + ndias);
                                    escribir("Parte inicial a pedir: " + parte);
                                    escribir("Canal inicial a pedir: " + ncanal);
                                    firstLP_Req = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            adp += 1;
                            adp = adp % 256;

                            byte tramaelgama[] = tramasElgama.getLocal1();
                            tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                            //adp
                            tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                            int crcCalculado = CRCElgama(tramaelgama);
                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                            while (calculo.length() < 4) {
                                calculo = "0" + calculo;
                            }
                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                            inicio = false;
                            inicio2 = true;
                            ultimatramaEnviada = tramaelgama;
                            try {
                            } catch (Exception e) {
                            }
                            enviaTrama2(tramaelgama, "=> Solicitud de Configuracion 2");

                        } else {//bad crc
                            //System.out.println("CRC");
                            escribir("BAD CRC");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                        }
                    } else {//no es el adp correcto                        
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            

                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }

                    }
                } else {
                    //trama incompleta
                    complemento = true;
                    tramaIncompleta = cadenahex;
                    rutinaCorrecta = false;
                    //enviamos ultimatrama
                    escribir("Trama incompleta " + vectorhex.length + " bytes");
                    escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");

                }
            } else {//es complemento
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                    if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                            reintentoadp = 0;
                            if (Integer.parseInt(vectorhextemp[0], 16) > 19) { //es version nueva
                                if (Integer.parseInt(vectorhextemp[17], 16) < 5) {
                                    version = 0;
                                    //System.out.println("OLD");
                                    escribir("OLD version");
                                } else {
                                    escribir("NEW Version");
                                    //System.out.println("NEW");
                                    ktru = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhextemp[19], 16)) + "." + String.valueOf(Integer.parseInt(vectorhextemp[18], 16)));
                                    ktri = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhextemp[21], 16)) + "." + String.valueOf(Integer.parseInt(vectorhextemp[20], 16)));
                                    exponent = Integer.parseInt(vectorhex[22], 16);
                                    escribir("KTRU: " + ktru);
                                    escribir("KTRI: " + ktri);
                                    escribir("EXP: " + exponent);
                                    //System.out.println("KTRU: " + ktru);
                                    //System.out.println("KTRI: " + ktri);
                                    //System.out.println("EXP: " + exponent);
                                    version = 1;
                                }
                            } else {//es version vieja
                                version = 0;
                                //System.out.println("OLD");
                                escribir("OLD version");
                            }
                            solicitar = true;
                            deltatimesync2 = new Timestamp(Calendar.getInstance().getTimeInMillis());
                            intervalo = 60 / (Integer.parseInt(vectorhextemp[15], 16));
                            //System.out.println("Intervalo " + intervalo);
                            byte[] fecha = {(byte) (Integer.parseInt(vectorhextemp[12], 16) & 0xFF),
                                (byte) (Integer.parseInt(vectorhextemp[11], 16) & 0xFF),
                                (byte) (Integer.parseInt(vectorhextemp[10], 16) & 0xFF),
                                (byte) (Integer.parseInt(vectorhextemp[9], 16) & 0xFF)};
                            int year = (int) ((fecha[0] & 0xFE) >> 1);
                            int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                            int dia = (int) ((fecha[1] & 0x1F));
                            int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                            int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                            int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                            //System.out.println("Fecha actual: " + year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg);
                            fechaActual = year + "/" + completarCeros(month, 2) + "/" + completarCeros(dia, 2) + " " + completarCeros(hora, 2) + ":" + completarCeros(min, 2) + ":" + completarCeros(seg, 2);

                            // validamos el desfase y definimos si solicitamos el perfil de carga
                            try {
                                tsfechaactual = new Timestamp(sdf2.parse(fechaActual).getTime());
                                //System.out.println("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2) / 1000));
                                escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                                //System.out.println("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                                escribir("Diferencia SEG NTP " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                                desfase = (time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000;
                                if (Math.abs(desfase) > ndesfasepermitido) {
                                    solicitar = false;
                                    escribir("No se solicitara el perfil de carga");
                                    //System.out.println("No se solicitara el perfil de carga");
                                }
                                cp.actualizaDesfase(((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000), med.getnSerie());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                escribir("Fecha Actual Estacion de trabajo " + new Timestamp(new Date().getTime()));
                                escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaActual).getTime()));
                                if (med.getFecha() != null) {
                                    Timestamp lastReadOffset = new Timestamp(med.getFecha().getTime() + (desfase * 1000));
                                    escribir("Fecha Ultima Lectura corregida con Offset: " + lastReadOffset);
                                    Date todayDate = new Date();
                                    String justDate = sdf5.format(todayDate) + " 00:00:00";
                                    Timestamp todayWithoutHours = new Timestamp(sdfacceso.parse(justDate).getTime());
                                    escribir("Last Read: " + lastReadOffset);
                                    escribir("Fecha hoy: " + todayWithoutHours);
                                    if (lastReadOffset.before(todayWithoutHours)) {// Si lastRead no es del día de hoy
                                        escribir("Last read que no es el del día de hoy.");
                                        //Como no es el del día de hoy, debemos calcular los días hacía atrás y la parte del día.
                                        Long millisegDiff = todayWithoutHours.getTime() - lastReadOffset.getTime();
                                        float nDiasDiff = (float) millisegDiff / 86400000f;
                                        escribir("Días de diferencia: " + nDiasDiff);
                                        ndias = (int) Math.ceil(nDiasDiff);
                                        parte = intervalo > 15 ? 0 : (nDiasDiff % 1) <= 0.5 ? 1 : 0;
                                    } else {
                                        escribir("Last read del día de hoy.");
                                        ndias = 0;
                                        Timestamp todayNoon = new Timestamp(todayWithoutHours.getTime() + (long) 43200000);
                                        todayNoonGlobal = todayNoon;
                                        escribir("Fecha Medio día del día actual: " + todayNoon);
                                        parte = intervalo > 15 ? 0 : (lastReadOffset.before(todayNoon) ? 0 : 1);
                                    }

                                    if (ndias > 0) {
                                        if (ndias > 35) {
                                            ndias = 35;
                                        }
                                    }
                                    ncanal = 1;
                                    nDiasI = ndias;
                                    parteI = parte;
                                    escribir("Días necesarios a solicitar: " + ndias);
                                    escribir("Parte inicial a pedir: " + parte);
                                    escribir("Canal inicial a pedir: " + ncanal);
                                    firstLP_Req = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ndias = ndias + 1;
                            adp += 1;
                            adp = adp % 256;
                            byte tramaelgama[] = tramasElgama.getLocal1();
                            tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                            //adp
                            tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                            int crcCalculado = CRCElgama(tramaelgama);
                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                            while (calculo.length() < 4) {
                                calculo = "0" + calculo;
                            }
                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                            inicio = false;
                            inicio2 = true;
                            ultimatramaEnviada = tramaelgama;
                            try {                                
                            } catch (Exception e) {
                            }
                            enviaTrama2(tramaelgama, "=> Solicitud de Configuracion 2");

                        } else {//bad crc
                            //System.out.println("CRC");
                            escribir("BAD CRC");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                        }
                    } else {//no es el adp correcto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");                            
                            leer = false;

                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }
                    }
                } else {
                    if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                        reiniciaComuniacion();
                    } else {
                        //trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        rutinaCorrecta = false;
                        //enviamos ultimatrama
                        escribir("Trama incompleta " + vectorhex.length + " bytes");
                        escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                    }

                }
            }
        } else if (inicio2) {//segunda trama de configuracion
            if (!complemento) {//no es complemento
                if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                    if (Integer.parseInt(vectorhex[8], 16) == 1) {
                        //System.out.println("llega pregunta");
                        try {
                            String vectorhexComplemento[] = new String[vectorhex.length - 13];
                            String cadenahexcomplemento = "";
                            for (int j = 0; j < vectorhexComplemento.length; j++) {
                                if (j == 0) {
                                    cadenahexcomplemento = vectorhex[j + 13] + " ";
                                } else {
                                    cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                }
                                vectorhexComplemento[j] = vectorhex[j + 13];
                            }
                            //System.out.println("trama cortada " + cadenahexcomplemento);
                            cadenahex = cadenahexcomplemento.trim();
                            vectorhex = vectorhexComplemento;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (vectorhex.length > 0 && adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                            reintentoadp = 0;
                            adp += 1;
                            adp = adp % 256;
                            byte tramaelgama[] = tramasElgama.getLocal1();
                            String mensaje = "";
                            inicio2 = false;
                            if (leventos) {
                                tramaelgama = tramasElgama.getLocal4();
                                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                //adp
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                lregistroEventos1 = true;
                                powerfails = new Vector<String[]>();
                                try {                                    

                                } catch (Exception e) {
                                }
                                mensaje = "=> Solicitud de Registro de eventos PowerFails";
                                int crcCalculado = CRCElgama(tramaelgama);
                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                while (calculo.length() < 4) {
                                    calculo = "0" + calculo;
                                }
                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                ultimatramaEnviada = tramaelgama;
                                enviaTrama2(tramaelgama, mensaje);
                            } else if (lregistros) {
                                //System.out.println("Registros diarios");
                                //datos para registros
                                inicio3 = true;
                                tramaelgama = tramasElgama.getLocal1();
                                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);

                                //adp
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                int crcCalculado = CRCElgama(tramaelgama);
                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                while (calculo.length() < 4) {
                                    calculo = "0" + calculo;
                                }
                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                ultimatramaEnviada = tramaelgama;
                                try {                                    

                                } catch (Exception e) {
                                }
                                enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                            } else if (lperfil) {
                                tramaelgama = tramasElgama.getLocal2();
                                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                //adp
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                ndia = ndias;                                
                                lperfilCarga = true;
                                loadProfile1 = new ArrayList<>();
                                loadProfile2 = new ArrayList<>();
                                loadProfile3 = new ArrayList<>();
                                loadProfile4 = new ArrayList<>();
                                try {                                    
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                enviaPeticionPerfil(tramaelgama);
                            } else {
                                cerrarPuerto();
                                escribir("Estado lectura leido");                                
                                med.MedLeido = true;
                                cerrarLog("Leido", true);
                                leer = false;
                            }

                        } else {//bad crc
                            //System.out.println("CRC");
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                        }
                    } else {//no es el adp correcto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");                            
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;

                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                        }
                    }
                } else {
                    //trama incompleta
                    complemento = true;
                    tramaIncompleta = cadenahex;
                    rutinaCorrecta = false;
                    //enviamos ultimatrama
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                }
            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                    if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                            reintentoadp = 0;
                            adp += 1;
                            adp = adp % 256;
                            byte tramaelgama[] = tramasElgama.getLocal1();
                            String mensaje = "";
                            inicio2 = false;
                            if (leventos) {
                                tramaelgama = tramasElgama.getLocal4();
                                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                //adp
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                lregistroEventos1 = true;
                                powerfails = new Vector<String[]>();
                                try {                                    

                                } catch (Exception e) {
                                }
                                mensaje = "=> Solicitud de Registro de eventos PowerFails";
                                int crcCalculado = CRCElgama(tramaelgama);
                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                while (calculo.length() < 4) {
                                    calculo = "0" + calculo;
                                }
                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                ultimatramaEnviada = tramaelgama;
                                enviaTrama2(tramaelgama, mensaje);
                            } else {
                                if (lregistros) {
                                    //System.out.println("Registros diarios");
                                    //datos para registros
                                    inicio3 = true;
                                    tramaelgama = tramasElgama.getLocal1();
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                                } else if (lperfil) {
                                    tramaelgama = tramasElgama.getLocal2();
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    ndia = ndias;
                                    lperfilCarga = true;
                                    loadProfile1 = new ArrayList<>();
                                    loadProfile2 = new ArrayList<>();
                                    loadProfile3 = new ArrayList<>();
                                    loadProfile4 = new ArrayList<>();
                                    try {                                        
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    enviaPeticionPerfil(tramaelgama);
                                } else {
                                    cerrarPuerto();
                                    escribir("Estado lectura leido");
                                    //AlmacenarRegistros();
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    med.MedLeido = true;
                                    cerrarLog("Leido", true);
                                    leer = false;
                                }
                            }
                        } else {//bad crc
                            //System.out.println("CRC");
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                        }
                    } else {//no es el adp correcto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                        }
                    }
                } else {
                    if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                        reiniciaComuniacion();
                    } else {
                        //trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        rutinaCorrecta = false;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                    }
                }
            }
        } else if (lregistroEventos1) {
            if (!complemento) {
                if (vectorhex.length > 8) {
                    if (Integer.parseInt(vectorhex[8], 16) == 1) {
                        //System.out.println("llega pregunta");
                        try {
                            String vectorhexComplemento[] = new String[vectorhex.length - 13];
                            String cadenahexcomplemento = "";
                            for (int j = 0; j < vectorhexComplemento.length; j++) {
                                if (j == 0) {
                                    cadenahexcomplemento = vectorhex[j + 13] + " ";
                                } else {
                                    cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                }
                                vectorhexComplemento[j] = vectorhex[j + 13];
                            }
                            //System.out.println("trama cortada " + cadenahexcomplemento);
                            cadenahex = cadenahexcomplemento.trim();
                            vectorhex = vectorhexComplemento;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (vectorhex.length > 0 && vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                    if (adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                            reintentoadp = 0;
                            String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                            System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                            powerfails.add(trama);
                            lregistroEventos1 = false;
                            lregistroEventos2 = true;
                            byte tramaelgama[] = tramasElgama.getLocal5();
                            tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                            adp += 1;
                            adp = adp % 256;
                            tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                            int crcCalculado = CRCElgama(tramaelgama);
                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                            while (calculo.length() < 4) {
                                calculo = "0" + calculo;
                            }
                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                            ultimatramaEnviada = tramaelgama;
                            phasefails = new Vector<String[]>();
                            try {                                
                            } catch (Exception e) {
                            }
                            ultimatramaEnviada = tramaelgama;
                            enviaTrama2(tramaelgama, "=>Solicitud de eventos de falla en fases");
                        } else {//bad crc
                            //System.out.println("BCRC");
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    } else {//adp incorrecto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    }
                } else {//incompleta
                    complemento = true;
                    tramaIncompleta = cadenahex;
                    rutinaCorrecta = false;
                    //enviamos ultimatrama
                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                }
            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String cadenahextemp = tramaIncompleta;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                tramaIncompleta = "";
                if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                    if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                            reintentoadp = 0;
                            String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                            System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                            powerfails.add(trama);
                            lregistroEventos1 = false;
                            lregistroEventos2 = true;
                            byte tramaelgama[] = tramasElgama.getLocal5();
                            tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                            adp += 1;
                            adp = adp % 256;
                            tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                            int crcCalculado = CRCElgama(tramaelgama);
                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                            while (calculo.length() < 4) {
                                calculo = "0" + calculo;
                            }
                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                            ultimatramaEnviada = tramaelgama;
                            phasefails = new Vector<String[]>();
                            try {                                

                            } catch (Exception e) {
                            }
                            ultimatramaEnviada = tramaelgama;
                            enviaTrama2(tramaelgama, "=>Solicitud de eventos de falla en fases");
                        } else {//bad crc
                            //System.out.println("BCRC");
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla de energia");
                        }
                    } else {//adp incorrecto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla de energia");
                        }
                    }
                } else {//incompleta
                    if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                        reiniciaComuniacion();
                    } else {
                        complemento = true;
                        tramaIncompleta = "";
                        rutinaCorrecta = false;
                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla de energia");
                    }

                }
            }
        } else if (lregistroEventos2) {
            if (!complemento) {
                if (vectorhex.length > 8) {
                    if (Integer.parseInt(vectorhex[8], 16) == 1) {
                        //System.out.println("llega pregunta");
                        try {
                            String vectorhexComplemento[] = new String[vectorhex.length - 13];
                            String cadenahexcomplemento = "";
                            for (int j = 0; j < vectorhexComplemento.length; j++) {
                                if (j == 0) {
                                    cadenahexcomplemento = vectorhex[j + 13] + " ";
                                } else {
                                    cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                }
                                vectorhexComplemento[j] = vectorhex[j + 13];
                            }
                            //System.out.println("trama cortada " + cadenahexcomplemento);
                            cadenahex = cadenahexcomplemento.trim();
                            vectorhex = vectorhexComplemento;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (vectorhex.length > 0 && vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                    if (adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                            reintentoadp = 0;
                            String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                            System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                            phasefails.add(trama);
                            lregistroEventos2 = false;
                            inicio3 = true;
                            byte tramaelgama[] = tramasElgama.getLocal1();
                            tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                            adp += 1;
                            adp = adp % 256;
                            tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                            int crcCalculado = CRCElgama(tramaelgama);
                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                            while (calculo.length() < 4) {
                                calculo = "0" + calculo;
                            }
                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                            ultimatramaEnviada = tramaelgama;
                            try {                                
                            } catch (Exception e) {
                            }
                            enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                        } else {//bad crc
                            //System.out.println("BCRC");
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    } else {//adp incorrecto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    }
                } else {//incompleta
                    tramaIncompleta = cadenahex;
                    complemento = true;
                    rutinaCorrecta = false;
                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                }
            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String cadenahextemp = tramaIncompleta;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                tramaIncompleta = "";
                if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                    if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                        if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                            reintentoadp = 0;
                            String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                            System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                            phasefails.add(trama);
                            lregistroEventos2 = false;
                            inicio3 = true;
                            byte tramaelgama[] = tramasElgama.getLocal1();
                            tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                            adp += 1;
                            adp = adp % 256;
                            tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                            int crcCalculado = CRCElgama(tramaelgama);
                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                            while (calculo.length() < 4) {
                                calculo = "0" + calculo;
                            }
                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                            ultimatramaEnviada = tramaelgama;
                            try {                                
                            } catch (Exception e) {
                            }
                            enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                        } else {//bad crc
                            //System.out.println("BCRC");
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    } else {//adp incorrecto
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            escribir("ADP Incorrecto");
                            escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    }
                } else {//incompleta
                    if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                        reiniciaComuniacion();
                    } else {
                        tramaIncompleta = "";
                        rutinaCorrecta = false;
                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                    }
                }
            }
        } else if (lregistrodias) {
            if (!complemento) {
                if (vectorhex.length > 1) {//tiene cabecera
                    if (vectorhex.length > 8) {
                        if (Integer.parseInt(vectorhex[8], 16) == 1) {
                            //System.out.println("llega pregunta");
                            try {
                                String vectorhexComplemento[] = new String[vectorhex.length - 13];
                                String cadenahexcomplemento = "";
                                for (int j = 0; j < vectorhexComplemento.length; j++) {
                                    if (j == 0) {
                                        cadenahexcomplemento = vectorhex[j + 13] + " ";
                                    } else {
                                        cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                    }
                                    vectorhexComplemento[j] = vectorhex[j + 13];
                                }
                                //System.out.println("trama cortada " + cadenahexcomplemento);
                                cadenahex = cadenahexcomplemento.trim();
                                vectorhex = vectorhexComplemento;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (vectorhex.length > 0 && vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhex[7], 16) && (Integer.parseInt(vectorhex[8], 16) == 2 || Integer.parseInt(vectorhex[8], 16) == 11)) {//es el adp correcto
                            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                reintentoadp = 0;
                                if (vectorhex.length > 11) {
                                    String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                    System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                    regdias.add(trama);
                                } else {
                                    ndiasRegSin++;
                                }
                                ndiaReg--;
                                if (ndiaReg >= 0) {//continuamos solicitando
                                    byte tramaelgama[] = tramasElgama.getLocal6();
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    //resta de dia
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                    //System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                } else {
                                    //solicitamos el regitro acumulado de meses
                                    nmesReg = med.getNmesreg() - 1;//numero de meses atras
                                    nmesRegSin = 0;
                                    lregistrodias = false;
                                    lregistromes = true;
                                    regmeses = new Vector<String[]>();
                                    byte tramaelgama[] = tramasElgama.getLocal7();
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    //resta de dia
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                    //System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                }
                            } else {//bad crc
                                //System.out.println("BCRC");
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                escribir("Estado lectura No leido");
                                try {                                    
                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;
                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                            }
                        }
                    } else {
                        //trama incompleta
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                        }
                    }
                } else {
                    //no cabecera
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                }
            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                if (vectorhextemp.length > 1) {//tiene cabecera
                    if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                            if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                reintentoadp = 0;
                                if (vectorhextemp.length > 11) {
                                    String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                    System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                    regdias.add(trama);
                                } else {
                                    ndiasRegSin++;
                                }
                                ndiaReg--;
                                if (ndiaReg >= 0) {//continuamos solicitando
                                    byte tramaelgama[] = tramasElgama.getLocal6();
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    //resta de dia
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                    //System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                } else {
                                    //solicitamos el regitro acumulado de meses
                                    nmesReg = med.getNmesreg() - 1;//numero de meses atras
                                    nmesRegSin = 0;
                                    byte tramaelgama[] = tramasElgama.getLocal7();
                                    regmeses = new Vector<String[]>();
                                    lregistrodias = false;
                                    lregistromes = true;
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    //resta de dia
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                    //System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                }
                            } else {//bad crc
                                //System.out.println("CRC");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                escribir("Estado lectura No leido");
                                try {                                    
                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;
                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        }
                    } else {
                        if (reintentoadp > 4) {
                            cerrarPuerto();
                            escribir("Estado lectura No leido");
                            try {                                
                            } catch (Exception e) {
                            }
                            cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                            cerrarLog("Numero de adp erroneos",false);	
                            leer = false;
                        } else {
                            reintentoadp++;
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    }
                } else {//no cabcera
                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                }
            }
        } else if (lregistromes) {
            if (!complemento) {
                if (vectorhex.length > 1) {//tiene cabecera
                    if (vectorhex.length > 8) {
                        if (Integer.parseInt(vectorhex[8], 16) == 1) {
                            //System.out.println("llega pregunta");
                            try {
                                String vectorhexComplemento[] = new String[vectorhex.length - 13];
                                String cadenahexcomplemento = "";
                                for (int j = 0; j < vectorhexComplemento.length; j++) {
                                    if (j == 0) {
                                        cadenahexcomplemento = vectorhex[j + 13] + " ";
                                    } else {
                                        cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                    }
                                    vectorhexComplemento[j] = vectorhex[j + 13];
                                }
                                //System.out.println("trama cortada " + cadenahexcomplemento);
                                cadenahex = cadenahexcomplemento.trim();
                                vectorhex = vectorhexComplemento;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (vectorhex.length > 0 && vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhex[7], 16) && (Integer.parseInt(vectorhex[8], 16) == 2 || Integer.parseInt(vectorhex[8], 16) == 11)) {//es el adp correcto
                            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                reintentoadp = 0;
                                if (vectorhex.length > 11) {
                                    String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                    System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                    regmeses.add(trama);
                                } else {
                                    nmesRegSin++;
                                }
                                nmesReg--;
                                if (nmesReg >= 0) {
                                    byte tramaelgama[] = tramasElgama.getLocal7();
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    //resta de dia
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                    //System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                } else {// revisamos si solicitamos perfil o no
                                    lregistromes = false;
                                    if (lperfil) {
                                        adp += 1;
                                        adp = adp % 256;
                                        ndia = ndias;
                                        byte tramaelgama[] = tramasElgama.getLocal2();
                                        String mensaje = "";
                                        lperfilCarga = true;
                                        loadProfile1 = new ArrayList<>();
                                        loadProfile2 = new ArrayList<>();
                                        loadProfile3 = new ArrayList<>();
                                        loadProfile4 = new ArrayList<>();
                                        tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        try {                                            
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        enviaPeticionPerfil(tramaelgama);
                                    } else {
                                        cerrarPuerto();
                                        escribir("Estado lectura leido");
                                        AlmacenarRegistros();
                                        try {                                            
                                        } catch (Exception e) {
                                        }
                                        med.MedLeido = true;
                                        cerrarLog("Leido", true);
                                        leer = false;
                                    }
                                }
                            } else {//bad crc
                                //System.out.println("BCRC");
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del mes " + nmesReg);
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                escribir("Estado lectura No leido");
                                try {                                    
                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;
                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramasElgama.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del mes " + nmesReg);
                            }
                        }
                    } else {
                        //trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        rutinaCorrecta = false;
                        //enviamos ultimatrama
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del  mes " + nmesReg);
                    }
                } else {
                    //no cabecera
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del  mes " + nmesReg);
                }

            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                if (vectorhextemp.length > 1) {//tiene cabecera
                    if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                            if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                reintentoadp = 0;
                                if (vectorhex.length > 11) {
                                    String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                    System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                    regmeses.add(trama);
                                } else {
                                    nmesRegSin++;
                                }
                                nmesReg--;
                                if (nmesReg >= 0) {
                                    byte tramaelgama[] = tramasElgama.getLocal7();
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    //resta de dia
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                    //System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                    try {                                        

                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                } else {
                                    lregistromes = false;
                                    if (lperfil) {
                                        adp += 1;
                                        adp = adp % 256;
                                        ndia = ndias;
                                        byte tramaelgama[] = tramasElgama.getLocal2();
                                        String mensaje = "";
                                        inicio3 = false;
                                        lperfilCarga = true;
                                        loadProfile1 = new ArrayList<>();
                                        loadProfile2 = new ArrayList<>();
                                        loadProfile3 = new ArrayList<>();
                                        loadProfile4 = new ArrayList<>();
                                        tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        try {                                            

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        enviaPeticionPerfil(tramaelgama);
                                    } else {
                                        cerrarPuerto();
                                        escribir("Estado lectura leido");

                                        AlmacenarRegistros();
                                        try {                                            
                                        } catch (Exception e) {
                                        }
                                        //fh.close();
                                        med.MedLeido = true;
                                        cerrarLog("Leido", true);
                                        leer = false;
                                    }
                                }

                            } else {//bad crc
                                //System.out.println("CRC");
                                enviaTramaUltima(ultimatramaEnviada, "=>envia Solicitud registro acumulado mensual del mes " + nmesReg);
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                escribir("Estado lectura No leido");
                                //AlmacenarRegistros();
                                try {                                    

                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;

                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=>envia Solicitud registro acumulado mensual del mes " + nmesReg);
                            }
                        }
                    } else {
                        //trama incompleta
                        if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                            reiniciaComuniacion();
                        } else {
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>envia Solicitud registro acumulado mensual del mes " + nmesReg);
                        }

                    }
                } else {//no cabcera
                    enviaTramaUltima(ultimatramaEnviada, "=>envia Solicitud registro acumulado mensual del mes " + nmesReg);
                }
            }
        } else if (inicio3) {
            if (!complemento) {
                if (vectorhex.length > 1) {//tiene cabecera
                    if (vectorhex.length > 8) {
                        if (Integer.parseInt(vectorhex[8], 16) == 1) {
                            //System.out.println("llega pregunta");
                            try {
                                String vectorhexComplemento[] = new String[vectorhex.length - 13];
                                String cadenahexcomplemento = "";
                                for (int j = 0; j < vectorhexComplemento.length; j++) {
                                    if (j == 0) {
                                        cadenahexcomplemento = vectorhex[j + 13] + " ";
                                    } else {
                                        cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                    }
                                    vectorhexComplemento[j] = vectorhex[j + 13];
                                }
                                //System.out.println("trama cortada " + cadenahexcomplemento);
                                cadenahex = cadenahexcomplemento.trim();
                                vectorhex = vectorhexComplemento;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (vectorhex.length > 0 && vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
                            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                reintentoadp = 0;
                                inicio3 = false;
                                if (lregistros) {
                                    adp += 1;
                                    adp = adp % 256;
                                    ndiaReg = med.getNdiasreg() - 1;
                                    ndiasRegSin = 0;
                                    byte tramaelgama[] = tramasElgama.getLocal6();
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                    lregistrodias = true;
                                    regdias = new Vector<String[]>();
                                    String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                    //System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    try {                                        
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                } else if (lperfil) {
                                    adp += 1;
                                    adp = adp % 256;
                                    ndia = ndias;
                                    byte tramaelgama[] = tramasElgama.getLocal2();
                                    String mensaje = "";
                                    lperfilCarga = true;
                                    loadProfile1 = new ArrayList<>();
                                    loadProfile2 = new ArrayList<>();
                                    loadProfile3 = new ArrayList<>();
                                    loadProfile4 = new ArrayList<>();
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);                                    
                                    try {                                        
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    enviaPeticionPerfil(tramaelgama);
                                } else {
                                    cerrarPuerto();
                                    escribir("Estado lectura leido");

                                    AlmacenarRegistros();
                                    try {                                        

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //fh.close();
                                    med.MedLeido = true;
                                    cerrarLog("Leido", true);
                                    leer = false;
                                }
                            } else {//bad crc
                                //System.out.println("BCRC");
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                escribir("Estado lectura No leido");
                                //AlmacenarRegistros();
                                try {                                    
                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;

                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        }
                    } else {
                        //trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        rutinaCorrecta = false;
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        //enviamos ultimatrama
                    }
                } else {
                    //no cabecera
                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                }

            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                String vectorhextemp[] = tramaIncompleta.split(" ");
                if (vectorhextemp.length > 1) {//tiene cabecera
                    if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                            if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                reintentoadp = 0;
                                inicio3 = false;
                                if (lregistros) {
                                    adp += 1;
                                    adp = adp % 256;
                                    ndiaReg = med.getNdiasreg() - 1;
                                    ndiasRegSin = 0;
                                    byte tramaelgama[] = tramasElgama.getLocal6();
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    tramaelgama[10] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                    lregistrodias = true;
                                    regdias = new Vector<String[]>();
                                    String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                    //System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;
                                    try {
                                        

                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, mensaje);
                                } else if (lperfil) {
                                    adp += 1;
                                    adp = adp % 256;
                                    ndia = ndias;
                                    byte tramaelgama[] = tramasElgama.getLocal2();
                                    String mensaje = "";
                                    lperfilCarga = true;
                                    loadProfile1 = new ArrayList<>();
                                    loadProfile2 = new ArrayList<>();
                                    loadProfile3 = new ArrayList<>();
                                    loadProfile4 = new ArrayList<>();
                                    tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    try {                                        
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    enviaPeticionPerfil(tramaelgama);
                                } else {
                                    cerrarPuerto();
                                    escribir("Estado lectura leido");

                                    AlmacenarRegistros();
                                    try {                                        

                                    } catch (Exception e) {
                                    }
                                    //fh.close();
                                    med.MedLeido = true;
                                    cerrarLog("Leido", true);
                                    leer = false;
                                }
                            } else {//bad crc
                                //System.out.println("CRC");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                escribir("Estado lectura No leido");
                                //AlmacenarRegistros();
                                try {                                    

                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;

                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        }
                    } else {
                        if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                            reiniciaComuniacion();
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }

                    }
                } else {//no cabcera
                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                }
            }
        } else if (lperfilCarga) {
            //RevisarPerfil BEGIN
            if (!complemento) {
                if (vectorhex.length > 7) {//tiene cabecera
                    if (vectorhex.length > 8) {
                        if (Integer.parseInt(vectorhex[8], 16) == 1) {
                            //System.out.println("llega pregunta");
                            try {
                                String vectorhexComplemento[] = new String[vectorhex.length - 13];
                                String cadenahexcomplemento = "";
                                for (int j = 0; j < vectorhexComplemento.length; j++) {
                                    if (j == 0) {
                                        cadenahexcomplemento = vectorhex[j + 13] + " ";
                                    } else {
                                        cadenahexcomplemento = cadenahexcomplemento + "" + vectorhex[j + 13] + " ";
                                    }
                                    vectorhexComplemento[j] = vectorhex[j + 13];
                                }
                                //System.out.println("trama cortada " + cadenahexcomplemento);
                                cadenahex = cadenahexcomplemento.trim();
                                vectorhex = vectorhexComplemento;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (vectorhex.length > 0 && vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhex[7], 16) && Integer.parseInt(vectorhex[8], 16) == 2) {//es el adp correcto
                            if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                perfilincompleto = true;
                                reintentoadp = 0;
                                byte tramaelgama[] = tramasElgama.getLocal2();
                                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                adp += 1;
                                adp = adp % 256;
                                String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                
                                switch (ncanal) {
                                    case 1:
                                        loadProfile1.add(trama);
                                        break;
                                    case 2:
                                        loadProfile2.add(trama);
                                        break;
                                    case 3:
                                        loadProfile3.add(trama);
                                        break;
                                    case 4:
                                        loadProfile4.add(trama);
                                        break;
                                    default:
                                        break;
                                }
                                boolean finaliza = false;
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                if (intervalo == 15) {
                                    if (parte == 0) {
                                        if (firstLP_Req) {
                                            firstLP_Req = false;
                                        }
                                        if (ndia <= 0) {
                                            Timestamp currentTime = new Timestamp(new Date().getTime());
                                            Timestamp noonPlusOneInterval = new Timestamp(todayNoonGlobal.getTime() + (intervalo * 60000));
                                            if (currentTime.after(noonPlusOneInterval)) { // Ya es más de medio día + un intervalo entonces se debe pedir la parte 1 (2da parte).
                                                parte = 1;
                                            } else {
                                                ncanal++;
                                                if (ncanal >= 5) {
                                                    ndia--; //cambiamos de dia
                                                    finaliza = true;
                                                }
                                            }
                                        } else {
                                            parte = 1;
                                        }
                                    } else {
                                        if (!firstLP_Req) {
                                            parte = 0;
                                        }
                                        ncanal++;
                                        if (ncanal >= 5) {
                                            ndia--; //cambiamos de dia
                                            if (ndia >= 0) {
                                                ncanal = 0;
                                                parte = 0;
                                            } else {
                                                finaliza = true;
                                            }
                                        }
                                    }
                                } else {
                                    ncanal++;
                                    if (ncanal >= 5) {
                                        ndia--; //cambiamos de dia
                                        if (ndia >= 0) {
                                            ncanal = 0;
                                        } else {
                                            finaliza = true;
                                        }
                                    }
                                }
                                if (!finaliza) {
                                    enviaPeticionPerfil(tramaelgama);
                                } else {
                                    finalizaPerfil();
                                }                                
                            } else {//bad crc
                                //System.out.println("CRC");
                                enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                            }
                        } else {//no es el adp correcto
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                if (lperfilCarga & perfilincompleto) {
                                    AlmacenarRegistrosIncompletos();
                                }
                                escribir("Estado lectura No leido");
                                //AlmacenarRegistros();
                                try {                                    
                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos");
                                cerrarLog("Numero de adp erroneos",false);	
                                leer = false;

                            } else {
                                reintentoadp++;
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                            }
                        }
                    } else {
                        //trama incompleta
                        complemento = true;
                        tramaIncompleta = cadenahex;
                        rutinaCorrecta = false;
                        enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                        //enviamos ultimatrama
                    }
                } else {
                    //no cabecera
                    complemento = true;
                    tramaIncompleta = cadenahex;
                    rutinaCorrecta = false;
                    enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                }

            } else {
                complemento = false;
                tramaIncompleta = tramaIncompleta + " " + cadenahex;
                //System.out.println("Trama completa " + tramaIncompleta);
                String vectorhextemp[] = tramaIncompleta.split(" ");
                if (vectorhextemp.length > 6) {//tiene cabecera
                    if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                        if (adp == Integer.parseInt(vectorhextemp[7], 16) && Integer.parseInt(vectorhextemp[8], 16) == 2) {//es el adp correcto
                            if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                perfilincompleto = true;
                                reintentoadp = 0;
                                String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                /*
                                boolean almacena = false;
                                if (intervalo != 15 && parte == 0) {
                                    almacena = true;
                                } else {
                                    if (intervalo == 15) {
                                        almacena = true;
                                    }
                                }
                                */

                                switch (ncanal) {
                                    case 1:
                                        loadProfile1.add(trama);
                                        break;
                                    case 2:
                                        loadProfile2.add(trama);
                                        break;
                                    case 3:
                                        loadProfile3.add(trama);
                                        break;
                                    case 4:
                                        loadProfile4.add(trama);
                                        break;
                                    default:
                                        break;
                                }

                                byte tramaelgama[] = tramasElgama.getLocal2();
                                tramaelgama[1] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                adp += 1;
                                adp = adp % 256;
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                boolean finaliza = false;
                                tramaelgama[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                if (intervalo == 15) {
                                    if (parte == 0) {
                                        if (firstLP_Req) {
                                            firstLP_Req = false;
                                        }
                                        if (ndia <= 0) {
                                            Timestamp currentTime = new Timestamp(new Date().getTime());
                                            Timestamp noonPlusOneInterval = new Timestamp(todayNoonGlobal.getTime() + (intervalo * 60000));
                                            if (currentTime.after(noonPlusOneInterval)) { // Ya es más de medio día + un intervalo entonces se debe pedir la parte 1 (2da parte).
                                                parte = 1;
                                            } else {
                                                ncanal++;
                                                if (ncanal >= 5) {
                                                    ndia--; //cambiamos de dia
                                                    finaliza = true;
                                                }
                                            }
                                        } else {
                                            parte = 1;
                                        }
                                    } else {
                                        if (!firstLP_Req) {
                                            parte = 0;
                                        }
                                        ncanal++;
                                        if (ncanal >= 5) {
                                            ndia--; //cambiamos de dia
                                            if (ndia >= 0) {
                                                ncanal = 0;
                                                parte = 0;
                                            } else {
                                                finaliza = true;
                                            }
                                        }
                                    }
                                } else {
                                    ncanal++;
                                    if (ncanal >= 5) {
                                        ndia--; //cambiamos de dia
                                        if (ndia >= 0) {
                                            ncanal = 0;
                                        } else {
                                            finaliza = true;
                                        }
                                    }
                                }
                                if (!finaliza) {
                                    enviaPeticionPerfil(tramaelgama);
                                } else {
                                    finalizaPerfil();
                                }       
                            } else {//bad crc
                                //System.out.println("Badcrc");
                                if (parte == 0) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                }
                            }
                        } else {//no es el adp correcto
                            escribir("ADP Incorrecto");
                            if (reintentoadp > 4) {
                                cerrarPuerto();
                                if (lperfilCarga & perfilincompleto) {
                                    AlmacenarRegistrosIncompletos();
                                }
                                escribir("Estado lectura No leido");
                                //AlmacenarRegistros();
                                try {                                    
                                } catch (Exception e) {
                                }
                                cp.escribirLog(usuario, new Timestamp(new Date().getTime()), med.getnSerie(), "LeerTCPELgama", "Desconexion numero de adp erroneos ");
                                cerrarLog("Numero de adp erroneos",false);	
                                
                                leer = false;

                            } else {
                                reintentoadp++;
                                if (parte == 0) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                }
                            }

                        }
                    } else {
                        if (vectorhextemp[0].equals("F0") || vectorhextemp[0].equals("FC") || vectorhextemp[0].equals("00")) {
                            reiniciaComuniacion();
                        } else {
                            //trama incompleta
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            //enviamos ultimatrama
                            if (parte == 0) {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                            }
                        }

                    }
                } else {
                    //no cabecera
                    if (parte == 0) {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                    } else {
                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de Perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                    }
                }


            }
            // RevisaPerfil END
        }
        vectorhex = null;
    }

    public void escribir(String dato) {
        try {

//            asd.info((dato + "\r"));
//            asd.info("\r");
            if (file != null) {
                d = Calendar.getInstance().getTime();
                fr = new RandomAccessFile(file, "rw");
                fr.seek(fr.length());
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write(dato.getBytes(), 0, dato.getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.close();
            }
        } catch (Exception e) {
        }
    }

    private int CRCElgama(byte[] tramaelgama) {
        int[] table = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,};
        byte[] bytes = new byte[tramaelgama.length - 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = tramaelgama[i];
        }
        int crc = 0x0000;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        int crcAux;
        ////System.out.println("CRC2 " + Integer.toHexString(crc2));
        crcAux = (((crc) << 8) | ((crc) >>> 8)) & 0x0000ffff;
        return crcAux;
    }

    private boolean validaCRCElgama(String[] vectorhex, int len) {
        boolean ok = false;
        try {
            byte[] bytes = new byte[len];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (Integer.parseInt(vectorhex[i], 16) & 0xFF);
            }
            int crccalculado = CRCElgama(bytes);
            String crcc = Integer.toHexString(crccalculado);
            while (crcc.length() < 4) {
                crcc = "0" + crcc;
            }

            if (crcc.toUpperCase().equals(vectorhex[len - 2].toUpperCase() + "" + vectorhex[len - 1].toUpperCase())) {
                ok = true;
            } else {
                ok = false;
            }
        } catch (Exception e) {
            ok = false;
        }
        return ok;
    }

    private String obtenercanal(int ncanal) {
        String ca = "";
        switch (ncanal) {
            case 1:
                ca = "02";
                break;
            case 2:
                ca = "04";
                break;
            case 3:
                ca = "03";
                break;
            case 4:
                ca = "05";
                break;
            default:
                ca = "02";
                break;
        }
        return ca;
    }

    private void AlmacenarRegistros() {
        escribir("Procesando Resgitros..");
        if (lperfil) {
            //almacena perfil
            try {                

            } catch (Exception e) {
                e.printStackTrace();
            }
            escribir("Almacenando Perfil de Carga");
            almacenaPerfilCargaNCanales();
        }
        if (lregistros) {
            //almacena registros diarios
            escribir("Almacenando Registro diarios");
            try {                
            } catch (Exception e) {
            }
            almacenaRegistrosDiarios(regdias);
            //almacena registros mensuales
            escribir("Almacenando Registro mensuales");
            try {                
            } catch (Exception e) {
            }
            almacenaRegistrosMes(regmeses);
        }
        if (leventos) {
            //guarda el registro de eventos
            escribir("Almacenando Registro de eventos");
            try {                
            } catch (Exception e) {
            }
            almacenaRegistroEventosPower(powerfails);

        }
    }

    private void almacenaPerfilCargaNCanales() {
        Vector<Electura> vlec = new Vector<>();
        String fechaActualizada = "";
        Connection conn = cp.getConn();
        int valor = 720;
        if (intervalo != 15) {
            valor = 1440;
        }
        int duracionDias = valor / intervalo;
        int desglosa = ((2 * duracionDias) + 1) * 2;
        try {
            ArrayList<String> data1 = obtenerDataCanal(loadProfile1, desglosa);
            ArrayList<String> data2 = obtenerDataCanal(loadProfile2, desglosa);
            ArrayList<String> data3 = obtenerDataCanal(loadProfile3, desglosa);
            ArrayList<String> data4 = obtenerDataCanal(loadProfile4, desglosa);
            
            fechaActualizada = cp.lecturaELGAMANCanales(data1, seriemedidor, med.getMarcaMedidor().getCodigo(), 1, med.getFecha(), fechaActual, fechaActualizada, intervalo, nDiasI, parteI, version, ktru, ktri, exponent, numcanales, vlec, file);
            if (numcanales >= 2) {
                fechaActualizada = cp.lecturaELGAMANCanales(data2, seriemedidor, med.getMarcaMedidor().getCodigo(), 2, med.getFecha(), fechaActual, fechaActualizada, intervalo, nDiasI, parteI, version, ktru, ktri, exponent, numcanales, vlec, file);
            }
            if (numcanales >= 3) {
                fechaActualizada = cp.lecturaELGAMANCanales(data3, seriemedidor, med.getMarcaMedidor().getCodigo(), 3, med.getFecha(), fechaActual, fechaActualizada, intervalo, nDiasI, parteI, version, ktru, ktri, exponent, numcanales, vlec, file);
            }
            if (numcanales >= 4) {
                fechaActualizada = cp.lecturaELGAMANCanales(data4, seriemedidor, med.getMarcaMedidor().getCodigo(), 4, med.getFecha(), fechaActual, fechaActualizada, intervalo, nDiasI, parteI, version, ktru, ktri, exponent, numcanales, vlec, file);
            }
            ////System.out.println("Inicio almacenamiento " + seriemedidor + " " + new Date());            
            cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
            if (fechaActualizada.length() > 0) {
                SimpleDateFormat fechaAactualizarSDF = new SimpleDateFormat("yyMMddHHmmss");
                String fechaAactualizar = fechaActualizada;
                Date fechaAactualizarDate = new SimpleDateFormat("yy/MM/dd HH:mm:ss").parse(fechaAactualizar);
                fechaAactualizar = fechaAactualizarSDF.format(new Date(fechaAactualizarDate.getTime()));
                cp.actualizaFechaLectura(seriemedidor, fechaAactualizar);
            }
            //System.out.println("Fin almacenamiento    " + seriemedidor + " " + new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void almacenaRegistrosDiarios(Vector<String[]> regdias) {
        try {
            Vector<String> data = new Vector<String>();
            String dataRegDiarios = "";
            for (int pf = 0; pf < regdias.size(); pf++) {
                for (int j = 9; j < regdias.get(pf).length - 2; j++) {
                    dataRegDiarios += "" + regdias.get(pf)[j];
                }
            }
            while (dataRegDiarios.length() > 0) {
                if (dataRegDiarios.length() >= 98) {
                    data.add(dataRegDiarios.substring(0, 98));
                    dataRegDiarios = dataRegDiarios.substring(98, dataRegDiarios.length());
                } else {
                    data.add(dataRegDiarios.substring(0, dataRegDiarios.length()));
                    dataRegDiarios = "";
                }
            }
            cp.almacenaregistroDiario(data, seriemedidor, med.getMarcaMedidor().getCodigo(), fechaActual, med.getNdiasreg() - ndiasRegSin, numcanales);
        } catch (Exception e) {
        }
    }

    private void almacenaRegistrosMes(Vector<String[]> regmeses) {
        try {
            Vector<String> data = new Vector<String>();
            String dataRegMes = "";
            for (int pf = 0; pf < regmeses.size(); pf++) {
                for (int j = 9; j < regmeses.get(pf).length - 2; j++) {
                    dataRegMes += "" + regmeses.get(pf)[j];
                }
            }
            while (dataRegMes.length() > 0) {
                if (dataRegMes.length() >= 130) {
                    data.add(dataRegMes.substring(0, 130));
                    dataRegMes = dataRegMes.substring(130, dataRegMes.length());
                } else {
                    data.add(dataRegMes.substring(0, dataRegMes.length()));
                    dataRegMes = "";
                }
            }
            cp.almacenaregistroMensual(data, seriemedidor, med.getMarcaMedidor().getCodigo(), fechaActual, med.getNmesreg() - nmesRegSin, numcanales);
        } catch (Exception e) {
        }
    }

    private void almacenaRegistroEventosPower(Vector<String[]> powerfails) {
        try {
            Vector<String> data = new Vector<String>();
            String dataRegeventos = "";
            for (int pf = 0; pf < powerfails.size(); pf++) {
                for (int j = 9; j < powerfails.get(pf).length - 2; j++) {
                    dataRegeventos += "" + powerfails.get(pf)[j];
                }
            }
            while (dataRegeventos.length() > 0) {
                if (dataRegeventos.length() >= 16) {
                    data.add(dataRegeventos.substring(0, 16));
                    dataRegeventos = dataRegeventos.substring(16, dataRegeventos.length());
                } else {
                    data.add(dataRegeventos.substring(0, dataRegeventos.length()));
                    dataRegeventos = "";
                }
            }
            cp.eventosElgama(data, seriemedidor);
        } catch (Exception e) {
        }
    }

    private void enviaPeticionPerfil( byte[] tramaBase) {
        //System.out.println("=> Solicitud de Perfil de carga parte " + (parte+1) + " del canal " + ncanal + " del dia  " + ndia);
        String ca = obtenercanal(ncanal);
        tramaBase[9] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
        tramaBase[10] = (byte) ( ( ( Integer.parseInt(Integer.toHexString(ndia), 16 ) & 0xFF ) & 0x7F ) | ( parte == 0 ? 0x00:0x80) );
                                                
        int crcCalculado = CRCElgama(tramaBase);
        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
        while (calculo.length() < 4) {
            calculo = "0" + calculo;
        }
        tramaBase[tramaBase.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
        tramaBase[tramaBase.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
        ultimatramaEnviada = tramaBase;
        enviaTrama2(tramaBase, "=> Solicitud de Perfil de carga parte " + (parte+1) + " del canal " + ncanal + " del dia  " + ndia);
    }

    private void finalizaPerfil() {
        perfilincompleto = false;
        cerrarPuerto();
        escribir("Estado lectura leido");

        AlmacenarRegistros();
        try {            
        } catch (Exception e) {
        }
        //fh.close();
        med.MedLeido = true;
        cerrarLog("Leido", true);
        leer = false;
    }
    
    private ArrayList<String> obtenerDataCanal(ArrayList<String[]> loadProfile, int desglosa) { 
        String datapf = "";
        for (int pf = 0; pf < loadProfile.size(); pf++) {
                for (int j = 9; j < loadProfile.get(pf).length - 2; j++) {
                    datapf += "" + loadProfile.get(pf)[j];
                }
            }
            ArrayList<String> data = new ArrayList<>();
            while (datapf.length() > 0) {
                if (datapf.length() >= desglosa) {
                    //System.out.println("Data: " + datapf.substring(0, desglosa));
                    data.add(datapf.substring(0, desglosa));
                    datapf = datapf.substring(desglosa, datapf.length());
                } else {
                    data.add(datapf.substring(0, datapf.length()));
                    datapf = "";
                }
            }            
            return data;        
    }
    
    private void AlmacenarRegistrosIncompletos() {
        escribir("Procesando Resgitros..");
        if (lperfil) {
            //almacena perfil
            try {                

            } catch (Exception e) {
            }
            escribir("Almacenando Perfil de Carga Incompleto");
            almacenaPerfilCargaNCanales();
        }
        if (lregistros) {
            //almacena registros diarios
            escribir("Almacenando Registro diarios");
            try {                
            } catch (Exception e) {
            }
            almacenaRegistrosDiarios(regdias);
            //almacena registros mensuales
            escribir("Almacenando Registro mensuales");
            try {                
            } catch (Exception e) {
            }
            almacenaRegistrosMes(regmeses);
        }
        if (leventos) {
            //guarda el registro de eventos
            escribir("Almacenando Registro de eventos");
            try {                
            } catch (Exception e) {
            }
            almacenaRegistroEventosPower(powerfails);

        }
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
    
    public String[] cortarTrama(String vectorhex[]) {
        String nuevoVector[] = vectorhex;
        if (vectorhex.length > 0) {
            int tam = Integer.parseInt(vectorhex[0], 16);
            if (vectorhex.length >= tam) {
                //es igual o supera el tamaño
                nuevoVector = new String[tam];
                System.arraycopy(vectorhex, 0, nuevoVector, 0, nuevoVector.length);
                //System.out.println("Trama cortada " + Arrays.toString(vectorhex));
            }
        }
        return nuevoVector;
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

    public byte[] contruirTramaEPQS(byte[] trama, String[] serial, int adp) {

        byte[] data = trama;
        data[1] = (byte) (Integer.parseInt(serial[0], 16) & 0xFF);
        data[2] = (byte) (Integer.parseInt(serial[1], 16) & 0xFF);
        data[3] = (byte) (Integer.parseInt(serial[2], 16) & 0xFF);
        data[4] = (byte) (Integer.parseInt(serial[3], 16) & 0xFF);
        data[5] = (byte) (Integer.parseInt(serial[4], 16) & 0xFF);
        data[6] = (byte) (Integer.parseInt(serial[5], 16) & 0xFF);
        data[7] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
        int crcCalculado = CRCElgama(data);
        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
        while (calculo.length() < 4) {
            calculo = "0" + calculo;
        }
        data[data.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
        data[data.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
        return data;
    }

    public String completarCeros(int valor, int numceros) {
        String data = "" + valor;
        while (data.length() < numceros) {
            data = "0" + data;
        }
        return data;
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
    
    public Timestamp obtenerFecha_Hora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
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
