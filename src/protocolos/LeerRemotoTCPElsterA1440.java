/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasTCPElsterA1440;
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
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author Metrolink
 */
public class LeerRemotoTCPElsterA1440 extends Thread {

    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    Date d = new Date();
    String seriemedidor = "";
    String fechaactual = "";
    Timestamp tsfechaactual;
    Timestamp tspeticion;
    Timestamp tsrecepcion;
    Timestamp tspeticioneventos;
    Timestamp tsrecepcioneventos;
    Timestamp deltatimesync1;
    Timestamp deltatimesync2;
    Timestamp time = null; //tiempo deZID
    long ndesfasepermitido = 7200;
    long tiempo = 500;
    boolean rutinaCorrecta = false;
    InputStream input;
    OutputStream output;
    String cadenahex = "";
    TramasTCPElsterA1440 tramaselster = new TramasTCPElsterA1440();
    int indx = 0; //indice la tabla
    String password = ""; //password
    String password2 = "";
    String serial = "";
    EMedidor med;
    ControlProcesos cp;
    //opciones perfil, eventos, registros, configuracion de hora
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean lconfHora;
    //variable que indica si lee
    public boolean leer = true;
    //parametros de conexion puerto conexion,
    String numeroPuerto;
    int numeroReintentos = 4;
    int nreintentos = 0; //intento actual completo
    int velocidadPuerto; //velocidad del puerto
    long timeout;
    int ndias;
    int npeticiones = 0;//numero de peticiones que se realizaran al perfil de carga.
    int npeticioneseventos = 0;//numero de peticiones que se realizaran al perfil de carga.
    boolean portconect = false;
    long tiemporetransmision = 0;
    int actualReintento = 0; //reintento actual
    Thread port = null;
    Thread port2 = null;
    Thread port3 = null;
    Thread port4 = null;
    int reintentosbadBCC = 0;
    public boolean cierrapuerto = false;
    Socket socket;
    boolean escucha = true;//variable de control de escuchar el puerto
    Thread escuchar;
    private int reintentoconexion = 0;
    boolean aviso = false; //control  de cambios de estado
    //Estados 
    boolean presentacion = false;
    boolean comunicacion = false;
    boolean contraseña = false;
    boolean serialmedidor = false;
    boolean fechaactualdate = false;
    boolean fechaactualtime = false;
    boolean perfilcarga = false;
    boolean registroeventos = false;
    boolean sycnrelog = false;
    boolean logout = false;
    boolean perfilincompleto = false;
    //vectores de datos
    String[] vPerfil = null;
    ArrayList<String[]> arrayperfil = new ArrayList<String[]>();
    String[] vEventos = null;
    ArrayList<String[]> arrayeventos = new ArrayList<String[]>();
    //variables de control de envio
    boolean enviando = false;
    boolean reenviando = false;
    byte[] ultimatramaEnviada = null;
    //formatos de fechas
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    SimpleDateFormat sdf4 = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf4lec = new SimpleDateFormat("ddMMyyHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    int numcanales = 2; //numero de canales
    int intervalo = 0; //intervalo de datos.
    //variables para el log
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    Abortar objabortar;
    String usuario = "admin";
    int reinicio = 0;
    ZoneId zid;

    public LeerRemotoTCPElsterA1440(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean confHora, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, String usuario, ZoneId zid, long ndesfase) {
        this.med = med;
        this.cp = cp;
        this.zid = zid;
        this.ndesfasepermitido = ndesfase;
        File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
        if (!f.exists()) {
            f.mkdirs();
        }
        file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
        try {
            if (file.exists()) {
                existearchivo = true;
            } else {
                //file.mkdir();
            }
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        lconfHora = confHora;
        //System.out.println("conf hora " + lconfHora);
        this.aviso = aviso;
        this.objabortar = objabortar;

        this.indx = indx;
        jinit();
    }

    private void jinit() {
        tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        //configuracion de equipos 
        numeroPuerto = med.getPuertocomm();
        numeroReintentos = med.getReintentos();
        velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
        password = med.getPassword();
        password2 = med.getPassword2();
        timeout = med.getTimeout();
        ndias = med.getNdias() + 1;
        seriemedidor = med.getnSerie();
        numcanales = med.getNcanales();
        serial = seriemedidor;
        while (serial.length() < 16) {
            serial = "0" + serial;
        }
        try {
            abrePuerto();
            tiempo = 1000;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void abrePuerto() {
        reintentosUtilizados++;
        try {
            if (!objabortar.labortar) {
                if (reintentoconexion < 3) {
                    try {
                        //avisoStr("Conectando..");
                    } catch (Exception e) {
                    }
                    //System.out.println("Conectando.. " + med.getDireccionip() + ":" + med.getPuertoip());
                    socket = new Socket(med.getDireccionip(), Integer.valueOf(med.getPuertoip()).intValue());
                    try {
                        //avisoStr("Conectado");
                    } catch (Exception e) {
                    }
                    //System.out.println("Conectado " + med.getDireccionip() + ":" + med.getPuertoip());
                    socket.setSoTimeout(35000);
                    if (!socket.isClosed()) {
                        portconect = true;
                        reintentoconexion = 0;
                    } else {
                        portconect = false;
                    }
                    if (portconect) {
                        try {
                            //Se crea un objeto input de la clase CountingInputStream con lo que se obtiene del metodo                            
                            input = socket.getInputStream();
                            output = socket.getOutputStream();
                        } catch (IOException e) {
                            System.err.println("Error Input/output");
                        }
                    }
                    //metodo para escuchar lel puerto
                    try {
                        //escuchamos el puerto
                        if (portconect) {
                            //System.out.println("Inicia Escucha");
                            escuchar = new Thread() {
                                public void run() {
                                    escucha();
                                }
                            };
                            escuchar.start();
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    Thread.sleep(2000);
                    if (portconect) {
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Conexion Satisfactoria");
                        if (reinicio == 0) {
                            iniciacomunicacion();
                        } else {
                            iniciacomunicacion2();
                        }
                    } else {
                        if (reintentoconexion > 3) {
                            //fh.close();
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "numero de conexiones sin respuesta");
                            cerrarLog("No conectado", false);
                            leer = false;
                        } else {
                            reintentoconexion++;
                        }
                    }
                } else {
                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion por numero de reintentos");
                    cerrarLog("No conectado", false);
                    //System.out.println("Sale abre puerto");
                    leer = false;
                }
            } else {
                //System.out.println("Sale abre puerto");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion Cancelacion de operacion");
                cerrarLog("Abortado", false);
                leer = false;
            }
        } catch (Exception e) {
            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerTCPElsterA1440", "No conectado");
            escribir("ERROR de comunicacion " + e.getMessage());
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
            //System.out.println("Sale escucha");
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
            cadenahex = tramaselster.encode(readBuffer, numbytes);
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

    private void enviaTramaUltima() {

        reenviando = true;

        if (port4 != null) {
            if (port4.isAlive()) {
                port4.stop();
                port4 = null;
            }
        }
        port4 = new Thread() {
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
                                    ////System.out.println("Esperando... enviarTramaultima");
                                    sleep(500);
                                }
                            }
                        } else {
                            //System.out.println("Llego algo Sale enviatramaUltima");
                            t = false;
                        }
                    }
                    //validamos si se acabo el tiempo cierra el puerto
                    if (reenviar) {
                        reenviar = false;
                        cerrarPuerto();
                        escribir("Estado lectura no leido");
                    }
                } catch (Exception e) {
                    //System.err.println(e.getMessage());
                }
            }
        };
        port4.start();
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
        if (presentacion) {   //presentacion
            //identificacion de fin de trama           
            if (cadenahex.contains("2F 45 4C 53 35 5C")) {
                presentacion = false;
                comunicacion = true;
                rutinaCorrecta = false;
                enviaTramaUltima();
            } else {
                //no es la identificacion de iskra
                rutinaCorrecta = true;
                presentacion = false;
                cerrarPuerto();
                escribir("Estado lectura no leido");
                try {
                    //avisoStr("No Leido");
                } catch (Exception e) {
                }
                cerrarLog("Error de identificacion de protocolo", false);
                leer = false;
            }
        } else if (comunicacion) {//modo de comunicacion
            if (vectorhex.length > 1 && vectorhex[0].equals("01") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin       
                if (validarBCC(vectorhex)) {
                    byte data[] = tramaselster.getPassword();
                    if (lconfHora) {
                        data[5] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(0, 1)), 16) & 0xFF);
                        data[6] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(1, 2)), 16) & 0xFF);
                        data[7] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(2, 3)), 16) & 0xFF);
                        data[8] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(3, 4)), 16) & 0xFF);
                        data[9] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(4, 5)), 16) & 0xFF);
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(5, 6)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(6, 7)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(password2.substring(7, 8)), 16) & 0xFF);
                    } else {
                        data[5] = (byte) (Integer.parseInt(convertStringToHex(password.substring(0, 1)), 16) & 0xFF);
                        data[6] = (byte) (Integer.parseInt(convertStringToHex(password.substring(1, 2)), 16) & 0xFF);
                        data[7] = (byte) (Integer.parseInt(convertStringToHex(password.substring(2, 3)), 16) & 0xFF);
                        data[8] = (byte) (Integer.parseInt(convertStringToHex(password.substring(3, 4)), 16) & 0xFF);
                        data[9] = (byte) (Integer.parseInt(convertStringToHex(password.substring(4, 5)), 16) & 0xFF);
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(password.substring(5, 6)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(password.substring(6, 7)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(password.substring(7, 8)), 16) & 0xFF);
                    }
                    data[data.length - 1] = calcularBCC(data);
                    comunicacion = false;
                    contraseña = true;
                    ultimatramaEnviada = data;
                    //avisoStr("Password");
                    enviaTrama2(data, "=> Solicitud de password");
                } else {
                    try {
                        //System.out.println("BAD BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                    }
                    comunicacion = false;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion Error BCC", false);
                    leer = false;
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2(ultimatramaEnviada, "=> Reenvio Solicitud de modo comunicacion");
                } else {
                    reintentosbadBCC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion error de recepcion", false);
                    leer = false;
                }
            }

        } else if (contraseña) { //password para lectura.
            if (cadenahex.equals("06")) {
                contraseña = false;
                serialmedidor = true;
                //avisoStr("Serial Medidor");
                byte[] data = tramaselster.getSeriemedidor();
                enviaTrama2(data, "=> Solicitud serial de medidor");
            } else {
                try {
                    //System.out.println("Error password");
                    escribir("BAD PASSWORD");
                } catch (Exception e) {
                }
                contraseña = false;
                cerrarPuerto();
                escribir("Estado lectura no leido");
                try {
                    //avisoStr("No Leido");
                } catch (Exception e) {
                }
                cerrarLog("Error de password", false);
                leer = false;
            }
        } else if (serialmedidor) { //serial del medidor
            if (vectorhex[0].equals("02") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin                
                if (validarBCC(vectorhex)) {
                    String datoserial = "";
                    for (int i = 0; i < 8; i++) {
                        datoserial = datoserial + vectorhex[11 + i];
                    }
                    datoserial = Hex2ASCII(datoserial);
                    if (seriemedidor.equals(datoserial)) {
                        escribir("Numero de serial " + datoserial);
                        byte data[] = tramaselster.getFechaactual();
                        ultimatramaEnviada = data;
                        //avisoStr("Fecha Actual");
                        serialmedidor = false;
                        fechaactualdate = true;
                        enviaTrama2(data, "=> Solicitud fecha actual");
                    } else {
                        //avisoStr("Cerrando puerto");
                        escribir("Numero de serial incorrecto");
                        serialmedidor = false;
                        cerrarPuerto();
                        escribir("Estado lectura no leido");
                        try {
                            //avisoStr("No Leido");
                        } catch (Exception e) {
                        }
                        cerrarLog("Serial incorrecto", false);
                        leer = false;
                    }
                } else {
                    try {
                        //System.out.println("Error BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                    }
                    comunicacion = false;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    try {
                        //avisoStr("No Leido");
                    } catch (Exception e) {
                    }
                    cerrarLog("Desconexion Error BCC", false);
                    leer = false;
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2(ultimatramaEnviada, "=> Reenvio Solicitud de serie de medidor");
                } else {
                    reintentosbadBCC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion error de recepcion", false);
                    leer = false;
                }
            }
        } else if (fechaactualdate) {//fecha actual dia/mes/año
            if (vectorhex[0].equals("02") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin                
                if (validarBCC(vectorhex)) {
                    fechaactual = vectorhex[11] + vectorhex[12] + vectorhex[13] + vectorhex[14] + vectorhex[15] + vectorhex[16];
                    byte data[] = tramaselster.getFechatime();
                    ultimatramaEnviada = data;
                    //avisoStr("Hora Actual");
                    fechaactualdate = false;
                    fechaactualtime = true;
                    escribir("Desfase Permitido " + ndesfasepermitido);

                    time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                    //System.out.println("Sincronizando hora con zona horaria");
                    escribir("Sincronizando hora con zona horaria");
                    escribir("FechaZID" + time);
                    //System.out.println("FechaZID" + time);

                    deltatimesync1 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                    enviaTrama2(data, "=> Solicitud fecha actual hora");

                } else {
                    try {
                        //System.out.println("Error BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                    }
                    fechaactualdate = false;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    try {
                        //avisoStr("No Leido");
                    } catch (Exception e) {
                    }
                    cerrarLog("Desconexion Error BCC", false);
                    leer = false;
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2(ultimatramaEnviada, "=> Reenvio Solicitud de Fecha");
                } else {
                    reintentosbadBCC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion error de recepcion", false);
                    leer = false;
                }
            }
        } else if (fechaactualtime) { //fecha actual hora/min
            if (vectorhex[0].equals("02") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin                
                if (validarBCC(vectorhex)) {
                    fechaactual = Hex2ASCII(fechaactual + vectorhex[11] + vectorhex[12] + vectorhex[13] + vectorhex[14] + vectorhex[15] + vectorhex[16]);
                    //System.out.println("Valor Fecha " + fechaactual);
                    Timestamp fechasys = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                    //********Cambio para ajuste de reloj***********//
                    boolean solicitar = true;
                    try {
                        deltatimesync2 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                        tsfechaactual = new Timestamp(sdf4lec.parse(fechaactual).getTime());
                        //System.out.println("Fecha Medidor " + tsfechaactual);
                        escribir("Fecha Actual Medidor " + tsfechaactual);
                        escribir("Fecha Actual Estacion de trabajo " + fechasys);
                        //System.out.println("Fecha Actual Estacion de trabajo " + fechasys);
                        try {
                            //System.out.println("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                            escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                            //System.out.println("Diferencia SEGZID " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                            escribir("Diferencia SEGZID " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                            if (Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000) > ndesfasepermitido) {
                                solicitar = false;
                                escribir("No se solicitara el perfil de carga");
                            }
                            cp.actualizaDesfase(((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000), med.getnSerie(), null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ///***************************Fin ajuste sincronizacion *****************************//
                    fechaactualtime = false;
                    if (lperfil) {// selecion perfil de carga
                        if (solicitar) {
                            try {
                                if (med.getFecha() != null) {
                                    escribir("Fecha Ultima Lectura " + new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(med.getFecha().getTime()))).getTime()));
                                    //System.out.println("Fecha Ultima Lectura " + new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(med.getFecha().getTime()))).getTime()));
                                    Long actual = fechasys.getTime();
                                    Long ultimamed = sdfarchivo.parse(sdfarchivo.format(new Date(med.getFecha().getTime()))).getTime();
                                    Long calculo = actual - ultimamed;
                                    int diasaleer = (int) (calculo / 86400000);
                                    if (calculo % 86400000 > 0) {
                                        diasaleer = diasaleer + 1;
                                    }
                                    //System.out.println("dias calculados " + diasaleer);
                                    npeticiones = ((diasaleer) * 2) - 1;
                                    escribir("Numero de dias leer calculado " + (diasaleer));
                                    escribir("Numero de bloques a solicitar" + npeticiones);
                                    //System.out.println("Numero de bloques a solicitar" + npeticiones);
                                    try {
                                        tspeticion = new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(med.getFecha().getTime()))).getTime());
                                        escribir("Fecha peticion inicial" + tspeticion);
                                        //System.out.println("Fecha peticion inicial" + tspeticion);
                                        tsrecepcion = new Timestamp(tspeticion.getTime() + 43200000);
                                        escribir("Fecha peticion final" + tsrecepcion);
                                        //System.out.println("Fecha peticion final" + tsrecepcion);
                                    } catch (Exception e) {
                                    }
                                } else {
                                    //pedimos los dias por defecto
                                    ndias = med.getNdias();
                                    escribir("Numero de dias leer calculado por defecto" + (med.getNdias()));
                                    tspeticion = new Timestamp((sdfarchivo.parse(sdfarchivo.format(new Date(fechasys.getTime()))).getTime()) - ((long) (86400000) * ndias));
                                    escribir("Fecha peticion inicial" + tspeticion);
                                    //System.out.println("Fecha peticion inicial" + tspeticion);
                                    tsrecepcion = new Timestamp(tspeticion.getTime() + 43200000);
                                    escribir("Fecha peticion final" + tsrecepcion);
                                    //System.out.println("Fecha peticion final" + tsrecepcion);
                                    npeticiones = ((ndias) * 2) - 1;
                                    escribir("Numero de bloques a solicitar" + npeticiones);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //debemos organizar la fecha en que se pedira el perfil y se usaran tiempo completos es decir si son las 3 am se pide desde las 0:15 de ese dia 
                            String fechapeticion = sdf.format(new Date(tspeticion.getTime()));
                            byte[] data = tramaselster.getPerfil();
                            //System.out.println("Tamaño peticion perfil carga " + data.length);
                            if (npeticiones == 1) {
                                //se solicita el perfil sin fecha final
                                data = tramaselster.getPerfilfinal();
                                data[9] = (byte) 0x30;
                                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                tiempo = 10000;

                            } else {
                                //si es un perfil intermedio  se solicita en un rango de fechas
                                data[9] = (byte) 0x30;
                                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                String fechafin = sdf.format(new Date(tsrecepcion.getTime()));
                                data[21] = (byte) 0x30;
                                data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                                data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                                data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                                data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                                data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                                data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                                data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                                data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                                data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                                data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                                tiempo = 8000;
                            }
                            data[data.length - 1] = calcularBCC(data);
                            ultimatramaEnviada = data;
                            //avisoStr("Perfil de Carga");
                            perfilincompleto = true;
                            perfilcarga = true;
                            vPerfil = null;
                            arrayperfil = new ArrayList<String[]>();
                            ultimatramaEnviada = data;

                            enviaTrama2(data, "=> Solicitud Perfil de carga");
                        } else {
                            //avisoStr("Cerrando puerto");
                            cerrarPuerto();
                            escribir("Estado lectura No leido Reloj no sincronizado");
                            //avisoStr("No Leido");
                            med.MedLeido = true;
                            cerrarLog("No sincronizado", false);
                            leer = false;
                        }
                    } else if (leventos) {// solo seleciono registro de eventos
                        byte[] data = tramaselster.getEventos();
                        npeticioneseventos = (int) (med.getNdiaseventos() / 5);
                        if (npeticioneseventos == 0) {
                            npeticioneseventos = 1;
                        }
                        //System.out.println("Numero de bloques a solicitar eventos" + npeticioneseventos);
                        long ndiaseventos = (tsfechaactual.getTime() - (86400000L * (long) med.getNdiaseventos()));
                        String fechapeticion = "";
                        try {
                            tspeticioneventos = new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(ndiaseventos))).getTime());

                            //System.out.println("Fecha peticion inicial" + tspeticioneventos);

                            fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                            if (npeticioneseventos > 1) {
                                tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
                                //System.out.println("Fecha peticion final" + tsrecepcioneventos);
                                data[9] = (byte) 0x30;
                                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
                                data[21] = (byte) 0x30;
                                data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                                data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                                data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                                data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                                data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                                data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                                data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                                data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                                data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                                data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                                tiempo = 10000;
                            } else {
                                data = tramaselster.getEventosfinal();
                                data[9] = (byte) 0x30;
                                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                tiempo = 8000;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //avisoStr("Registro Eventos");
                        vEventos = null;
                        arrayeventos = new ArrayList<String[]>();
                        registroeventos = true;
                        data[data.length - 1] = calcularBCC(data);
                        enviaTrama2(data, "=> Solicitud de eventos " + fechapeticion);
                        ultimatramaEnviada = data;
                        enviaTrama2(data, "=> Solicitud Registro de Eventos");
                    } else if (lconfHora) {//sycronizacion de reloj
                        byte data[] = tramaselster.getSincreloj();
                        sycnrelog = true;
                        //avisoStr("Sync Reloj");

                        Timestamp time = null;
                        try {
                            time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                            //System.out.println("Sincronizando hora con zona horaria");
                            //System.out.println("Fecha " + time);
                        } catch (Exception e) {
                            //System.out.println("Error ZID Sincronizando hora con equipo local ");
                            e.printStackTrace();
                            time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                        }
//                            Timestamp time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                        String fecha = sdf4.format(new Date(time.getTime()));
                        data[9] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(0, 1)), 16) & 0xFF);
                        data[10] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(1, 2)), 16) & 0xFF);
                        data[11] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(2, 3)), 16) & 0xFF);
                        data[12] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(3, 4)), 16) & 0xFF);
                        data[13] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(4, 5)), 16) & 0xFF);
                        data[14] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(5, 6)), 16) & 0xFF);
                        data[15] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(6, 7)), 16) & 0xFF);
                        data[16] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(7, 8)), 16) & 0xFF);
                        data[17] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(8, 9)), 16) & 0xFF);
                        data[18] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(9, 10)), 16) & 0xFF);
                        data[19] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(10, 11)), 16) & 0xFF);
                        data[20] = (byte) (Integer.parseInt(convertStringToHex(fecha.substring(11, 12)), 16) & 0xFF);

                        data[data.length - 1] = calcularBCC(data);
                        ultimatramaEnviada = data;
                        enviaTrama2(data, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));

                    } else {// no seleciono nada por lo tanto termina comunicacion.
                        //avisoStr("Cerrando puerto");
                        cerrarPuerto();
                        escribir("Estado lectura Leido");
                        cerrarLog("Leido", true);
                        leer = false;
                    }
                } else {
                    try {
                        //System.out.println("Error BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                    }
                    fechaactualdate = false;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    try {
                        //avisoStr("No Leido");
                    } catch (Exception e) {
                    }
                    cerrarLog("Desconexion Error BCC", false);
                    leer = false;
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2(ultimatramaEnviada, "=> Reenvio Solicitud de Fecha");
                } else {
                    reintentosbadBCC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion error de recepcion", false);
                    leer = false;
                }
            }
        } else if (perfilcarga) { //perfil de carga.
            if (vectorhex[0].equals("02") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin
                if (validarBCC(vectorhex)) {
                    npeticiones--;
                    vPerfil = new String[vectorhex.length - 3];
                    System.arraycopy(vectorhex, 1, vPerfil, 0, vPerfil.length);
                    arrayperfil.add(vPerfil);

                    //numero de peticiones del perfil se detienen cuando llega a -1 eso quiere decir que se solicita la ultima trama de perfil
                    if (npeticiones >= 1) {
                        tspeticion = tsrecepcion;
                        String fechapeticion = sdf.format(new Date(tspeticion.getTime()));
                        escribir("Fecha peticion inicial" + tspeticion);
                        //System.out.println("Fecha peticion inicial" + tspeticion);
                        byte[] data = tramaselster.getPerfil();
                        if (npeticiones == 0) {
                            //se solicita el perfil sin fecha final
                            data = tramaselster.getPerfilfinal();
                            data[9] = (byte) 0x30;
                            data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                            data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                            data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                            data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                            data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                            data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                            data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                            data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                            data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                            data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                            tiempo = 15000;
                        } else {
                            //si es un perfil intermedio  se solicita en un rango de fechas
                            data[9] = (byte) 0x30;
                            data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                            data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                            data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                            data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                            data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                            data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                            data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                            data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                            data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                            data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);

                            tsrecepcion = new Timestamp(tspeticion.getTime() + 43200000);
                            escribir("Fecha peticion final" + tsrecepcion);
                            //System.out.println("Fecha peticion final" + tsrecepcion);
                            String fechafin = sdf.format(new Date(tsrecepcion.getTime()));
                            data[21] = (byte) 0x30;
                            data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                            data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                            data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                            data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                            data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                            data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                            data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                            data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                            data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                            data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                            tiempo = 8000;

                        }
                        data[data.length - 1] = calcularBCC(data);
                        ultimatramaEnviada = data;
                        enviaTrama2(data, "=> Solicitud Perfil de carga " + fechapeticion);
                    } else {
                        //cuando sea -1 solicitamos los demas valores o cerramos el puerto.
                        perfilincompleto = false;
                        tiempo = 500;
                        perfilcarga = false;
                        if (leventos) {
                            byte[] data = tramaselster.getEventos();
                            npeticioneseventos = (int) (med.getNdiaseventos() / 5);
                            if (npeticioneseventos == 0) {
                                npeticioneseventos = 1;
                            }
                            //System.out.println("Numero de bloques a solicitar eventos" + npeticioneseventos);
                            long ndiaseventos = (tsfechaactual.getTime() - (86400000L * (long) med.getNdiaseventos()));
                            String fechapeticion = "";
                            try {
                                tspeticioneventos = new Timestamp(sdfarchivo.parse(sdfarchivo.format(new Date(ndiaseventos))).getTime());
                                //System.out.println("Fecha peticion inicial" + tspeticioneventos);
                                fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                                if (npeticioneseventos > 1) {
                                    tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
                                    //System.out.println("Fecha peticion final" + tsrecepcioneventos);
                                    data[9] = (byte) 0x30;
                                    data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                    data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                    data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                    data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                    data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                    data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                    data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                    data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                    data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                    data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                    String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
                                    data[21] = (byte) 0x30;
                                    data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                                    data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                                    data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                                    data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                                    data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                                    data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                                    data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                                    data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                                    data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                                    data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                                    tiempo = 8000;
                                } else {
                                    data = tramaselster.getEventosfinal();
                                    data[9] = (byte) 0x30;
                                    data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                    data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                    data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                    data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                    data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                    data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                    data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                    data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                    data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                    data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                    tiempo = 15000;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //avisoStr("Registro Eventos");
                            vEventos = null;
                            arrayeventos = new ArrayList<String[]>();
                            registroeventos = true;
                            data[data.length - 1] = calcularBCC(data);
                            enviaTrama2(data, "=> Solicitud de eventos " + fechapeticion);
                            ultimatramaEnviada = data;
                            enviaTrama2(data, "=> Solicitud Registro de Eventos");
                        } else {
                            //avisoStr("Cerrando puerto");
                            fechaactualtime = false;
                            cerrarPuerto();
                            escribir("Estado lectura Leido");
                            //System.out.println("Almancenando Datos");
                            //avisoStr("Almacenando Pefil De Carga");
                            almacenarRegistros();
                            //avisoStr("Leido");
                            med.MedLeido = true;
                            cerrarLog("Leido", true);
                            leer = false;
                        }
                    }
                } else {
                    try {
                        //System.out.println("Error BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                    }
                    fechaactualtime = false;

                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    try {
                        //avisoStr("No Leido");
                    } catch (Exception e) {
                    }
                    cerrarLog("Desconexion Error BCC", false);
                    leer = false;
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2(ultimatramaEnviada, "=> Solicitud Perfil de carga");
                } else {
                    reintentosbadBCC = 0;
                    cerrarPuerto();
                    if (perfilincompleto) {
                        almacenarRegistros();
                        perfilincompleto = false;
                    }
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion error de recepcion", false);
                    leer = false;
                }
            }
        } else if (registroeventos) {// registro de eventos
            if (vectorhex[0].equals("02") && vectorhex[vectorhex.length - 2].equals("03")) { //verifica inicio de trama y fin
                if (validarBCC(vectorhex)) {
                    vEventos = new String[vectorhex.length - 3];
                    System.arraycopy(vectorhex, 1, vEventos, 0, vEventos.length);
                    npeticioneseventos--;
                    arrayeventos.add(vEventos);
                    if (npeticioneseventos > 0) {
                        tspeticioneventos = tsrecepcioneventos;
                        //System.out.println("Fecha peticion inicial" + tspeticioneventos);
                        byte[] data = tramaselster.getEventos();
                        String fechapeticion = "";
                        if (npeticioneseventos == 1) {
                            fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                            data = tramaselster.getEventosfinal();
                            data[9] = (byte) 0x30;
                            data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                            data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                            data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                            data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                            data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                            data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                            data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                            data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                            data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                            data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                            tiempo = 10000;
                        } else {
                            try {
                                tsrecepcioneventos = new Timestamp(tspeticioneventos.getTime() + (10 * 43200000));
                                //System.out.println("Fecha peticion final" + tsrecepcioneventos);
                                fechapeticion = sdf.format(new Date(tspeticioneventos.getTime()));
                                data[9] = (byte) 0x30;
                                data[10] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(0, 1)), 16) & 0xFF);
                                data[11] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(1, 2)), 16) & 0xFF);
                                data[12] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(2, 3)), 16) & 0xFF);
                                data[13] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(3, 4)), 16) & 0xFF);
                                data[14] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(4, 5)), 16) & 0xFF);
                                data[15] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(5, 6)), 16) & 0xFF);
                                data[16] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(6, 7)), 16) & 0xFF);
                                data[17] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(7, 8)), 16) & 0xFF);
                                data[18] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(8, 9)), 16) & 0xFF);
                                data[19] = (byte) (Integer.parseInt(convertStringToHex(fechapeticion.substring(9, 10)), 16) & 0xFF);
                                String fechafin = sdf.format(new Date(tsrecepcioneventos.getTime()));
                                data[21] = (byte) 0x30;
                                data[22] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(0, 1)), 16) & 0xFF);
                                data[23] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(1, 2)), 16) & 0xFF);
                                data[24] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(2, 3)), 16) & 0xFF);
                                data[25] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(3, 4)), 16) & 0xFF);
                                data[26] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(4, 5)), 16) & 0xFF);
                                data[27] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(5, 6)), 16) & 0xFF);
                                data[28] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(6, 7)), 16) & 0xFF);
                                data[29] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(7, 8)), 16) & 0xFF);
                                data[30] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(8, 9)), 16) & 0xFF);
                                data[31] = (byte) (Integer.parseInt(convertStringToHex(fechafin.substring(9, 10)), 16) & 0xFF);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        data[data.length - 1] = calcularBCC(data);
                        ultimatramaEnviada = data;
                        enviaTrama2(data, "=> Solicitud de eventos " + fechapeticion);
                    } else {
                        registroeventos = false;
                        cerrarPuerto();
                        //avisoStr("Almacenando registros");
                        almacenarRegistros();
                        try {
                            //avisoStr("Leido");
                        } catch (Exception e) {
                        }
                        med.MedLeido = true;
                        cerrarLog("Leido", true);
                        leer = false;
                    }
                } else {
                    try {
                        //System.out.println("Error BCC");
                        escribir("BAD BCC");
                    } catch (Exception e) {
                    }
                    fechaactualtime = false;
                    cerrarPuerto();
                    escribir("Estado lectura No Leido");
                    try {
                        //avisoStr("No Leido");
                    } catch (Exception e) {
                    }
                    cerrarLog("Desconexion Error BCC", false);
                    leer = false;
                }
            } else {
                reintentosbadBCC++;
                if (reintentosbadBCC < 4) {
                    enviaTrama2(ultimatramaEnviada, "=> Solicitud Registro de Eventos");
                } else {
                    reintentosbadBCC = 0;
                    cerrarPuerto();
                    escribir("Estado lectura no leido");
                    cerrarLog("Desconexion error de recepcion", false);
                    leer = false;
                }
            }
        } else if (sycnrelog) {//syncronizacion de reloj.
            if (cadenahex.equals("06")) {
                cerrarPuerto();
                escribir("Estado Syncronizacion confirmado");
                try {
                    //avisoStr("Leido");
                } catch (Exception e) {
                }
                med.MedLeido = true;
                cerrarLog("Medidor sincronizado", false);
                leer = false;
            } else {
                cerrarPuerto();
                escribir("Estado Syncronizacion No confirmado");
                try {
                    //avisoStr("No Leido");
                } catch (Exception e) {
                }
                cerrarLog("Medidor no sincronizado", false);
                leer = false;
            }
        }
    }

    public void cerrarPuerto() {
        try {

            enviaTrama(tramaselster.getDesconexion());
        } catch (Exception e) {
        }
        rutinaCorrecta = true;
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
            String mensaje = "FIN DE SESSION --" + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()) + "--";
            escribir(mensaje);
            escribir("\r\n\n");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            enviando = false;
            portconect = false;
            try {
                //avisoStr("Cerrando puerto..");
            } catch (Exception e) {
            }
            Thread.sleep((long) 5000);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        //System.out.println("Termina Lectura");
    }

    //metodo que envia tramas y reenvios de tramas
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
                            escribir("=> " + tramaselster.encode(trama, trama.length));
                            //System.out.println(tramaselster.encode(trama, trama.length));
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
                                    System.out.print(".");
                                    sleep(500);
                                }
                            }
                        }
                    }
                    if (cierrapuerto) {
                        cierrapuerto = false;
                        cerrarPuerto();
                        if (perfilincompleto) {
                            almacenarRegistros();
                            perfilincompleto = false;
                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                reinicio = 1;
                                abrePuerto();
                            } else {
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion Aborto de operacion");
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            try {
                                //avisoStr("No leido");
                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No Leido");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion Numero de envios agotado");
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
    //metodo de envio de tramas y reenvios para los casos de reconexion (segunda vuelta)

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
                            escribir("=> " + tramaselster.encode(trama, trama.length));
                            //System.out.println(tramaselster.encode(trama, trama.length));
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
                                    System.out.print(".");
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
                                reinicio = 1;
                                abrePuerto();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion Aborto de operacion");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            try {
                                //avisoStr("No leido");
                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No Leido");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion numero de envios agotado");
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

    private void enviaTrama2_3(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;

        enviando = true;
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
                            escribir("=> " + tramaselster.encode(trama, trama.length));
                            //System.out.println(tramaselster.encode(trama, trama.length));
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
                                    System.out.print(".");
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
                                reinicio = 0;
                                abrePuerto();
                            } else {
                                escribir("Proceso Abortado");
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion Aborto de operacion");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            try {
                                //avisoStr("No leido");
                            } catch (Exception e) {
                            }
                            escribir("Estado Lectura No Leido");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerElsterA1440", "Desconexion numero de envios agotado");
                            cerrarLog("Desconexion Numero de reintentos agotado", false);
                            leer = false;
                        }
                    }
                } catch (Exception e) {
                    //System.err.println(e.getMessage());
                }
            }
        };
        port3.start();
    }
    //metodo envio basico

    private void enviaTrama(byte[] bytes) {
        try {
            //si esta abierta la salida la cerramos y volvemos a abrir para limpiar el canal                        
            output.write(bytes, 0, bytes.length);
            output.flush();
        } catch (Exception e) {
            //e.printStackTrace();
            //System.out.println(e.getMessage());
        }
        rutinaCorrecta = false;
        //System.out.println("Termina Envio");
        //System.out.println("\n");

    }

    public void escribir(String dato) {
        try {
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

    private boolean validarBCC(String data[]) {
        boolean exito = false;
        byte[] vdata = new byte[data.length];
        for (int i = 0; i < vdata.length; i++) {
            vdata[i] = (byte) (Integer.parseInt(data[i], 16) & 0xFF);
        }
        byte valor = calcularBCC(vdata);
        if (valor == vdata[data.length - 1]) {
            exito = true;
        }
        return exito;
    }

    private byte calcularBCC(byte data[]) {
        byte valor = 0x00;
        //hacemos una copia de la peticion quitando el h01 y el bcc de la trama de copia
        byte databcc[] = new byte[data.length - 2];

        byte resultado = 0x00;
        //obtenemos realizamos la copia de los valores
        for (int i = 0; i < databcc.length; i++) {
            databcc[i] = data[i + 1];
        }
        //realizamos los XOR de cada uno de los datos
        for (int i = 0; i < databcc.length - 1; i++) {
            if (i == 0) {
                resultado = (byte) (databcc[i] ^ databcc[i + 1]);
            } else {
                resultado = (byte) (resultado ^ databcc[i + 1]);
            }
        }
        //sumamos a nivel de bits con XOR cada uno de los bits hasta el 7 el valor de resultado
        byte valorfinal = (byte) 0x00;
        for (int i = 0; i < 8; i++) {
            if (i == 0) {
                valorfinal = (byte) (resultado & ((i + 1) & 0xFF));
            } else {
                valorfinal = (byte) (valorfinal & ((i + 1) & 0xFF));
            }
        }
        if (valorfinal == 0x00) {
            valor = (byte) (resultado | 0x00);
        } else {
            valor = (byte) (resultado | 0x80);
        }
        return valor;
    }

    public String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
    }

    private void iniciacomunicacion() {
        tiemporetransmision = 15000 + tiempo;
        perfilcarga = false;
        if (portconect) {
            if (med.isLconfigurado()) {
                try {
                    String mensaje = "INICIO DE SESSION --" + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()) + "--";
                    escribir(mensaje);
                    mensaje = "Medidor: "+med.getMarcaMedidor().getNombre()+ ", Serie: "+med.getnSerie();
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
                    if (actualReintento != 0) {
                        Timestamp actufecha = cp.findUltimafechaLec(med.getnSerie());
                        if (actufecha != null) {
                            med.setFecha(actufecha);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                actualReintento++;
                try {
                    //avisoStr("iniciamos comunicacion");
                } catch (Exception e) {
                }
                presentacion = true;
                //System.out.println("Envia Solicitud identificacion");
                byte[] data = tramaselster.getIdentificacion2();
                data[2] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(0, 1)), 16) & 0xFF);
                data[3] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(1, 2)), 16) & 0xFF);
                data[4] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(2, 3)), 16) & 0xFF);
                data[5] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(3, 4)), 16) & 0xFF);
                data[6] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(4, 5)), 16) & 0xFF);
                data[7] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(5, 6)), 16) & 0xFF);
                data[8] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(6, 7)), 16) & 0xFF);
                data[9] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(7, 8)), 16) & 0xFF);
                data[10] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(8, 9)), 16) & 0xFF);
                data[11] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(9, 10)), 16) & 0xFF);
                data[12] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(10, 11)), 16) & 0xFF);
                data[13] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(11, 12)), 16) & 0xFF);
                data[14] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(12, 13)), 16) & 0xFF);
                data[15] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(13, 14)), 16) & 0xFF);
                data[16] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(14, 15)), 16) & 0xFF);
                data[17] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(15, 16)), 16) & 0xFF);
                //System.out.println("=> " + tramaselster.encode(data, data.length));
                escribir("=> " + tramaselster.encode(data, data.length));
                enviaTrama(data);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                data = tramaselster.getComunicacion();
                ultimatramaEnviada = data;
                enviaTrama2_2(data, "=> Solicitud de identificacion");
            }
        }
    }

    private void iniciacomunicacion2() {
        perfilcarga = false;
        tiemporetransmision = 15000 + tiempo;
        if (portconect) {
            if (med.isLconfigurado()) {
                try {
                    String mensaje = "INICIO DE SESSION tipo 2 --" + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()) + "--";
                    escribir(mensaje);
                    mensaje = "Medidor: "+med.getMarcaMedidor().getNombre()+ ", Serie: "+med.getnSerie();
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
                    if (actualReintento != 0) {
                        Timestamp actufecha = cp.findUltimafechaLec(med.getnSerie());
                        if (actufecha != null) {
                            med.setFecha(actufecha);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                actualReintento++;
                try {
                    //avisoStr("iniciamos comunicacion");
                } catch (Exception e) {
                }
                presentacion = true;
                //System.out.println("Envia Solicitud identificacion");
                escribir("Envia Solicitud identificacion");
                byte[] data = tramaselster.getIdentificacion2();
                data[2] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(0, 1)), 16) & 0xFF);
                data[3] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(1, 2)), 16) & 0xFF);
                data[4] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(2, 3)), 16) & 0xFF);
                data[5] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(3, 4)), 16) & 0xFF);
                data[6] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(4, 5)), 16) & 0xFF);
                data[7] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(5, 6)), 16) & 0xFF);
                data[8] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(6, 7)), 16) & 0xFF);
                data[9] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(7, 8)), 16) & 0xFF);
                data[10] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(8, 9)), 16) & 0xFF);
                data[11] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(9, 10)), 16) & 0xFF);
                data[12] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(10, 11)), 16) & 0xFF);
                data[13] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(11, 12)), 16) & 0xFF);
                data[14] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(12, 13)), 16) & 0xFF);
                data[15] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(13, 14)), 16) & 0xFF);
                data[16] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(14, 15)), 16) & 0xFF);
                data[17] = (byte) (Integer.parseInt(convertStringToHex(serial.substring(15, 16)), 16) & 0xFF);
                //System.out.println("=> " + tramaselster.encode(data, data.length));
                escribir("=> " + tramaselster.encode(data, data.length));
                enviaTrama(data);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                data = tramaselster.getComunicacion();
                ultimatramaEnviada = data;
                enviaTrama2_3(data, "=> Solicitud de modo Comunicacion");
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

    public void almacenarRegistros() {

        try {
            if (lperfil) {//Almacena PerfilCarga
                Electura lec = null;
                Vector<Electura> arraylec = new Vector<Electura>();
                Timestamp fechaintervalo = null;
                Timestamp timeufec = null;
                ArrayList<String> canales = new ArrayList<String>();
                Vector<EConstanteKE> lconske = cp.buscarConstantesKe(med.getnSerie());//se toman los valores de las constantes 
                ArrayList<EtipoCanal> vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo()); //se toman los valores de los tipos de canales configurados
                int ncontadorintervalos = 0; //variable que realiza el control de los intervalos para obtener la fecha del intervalo apartir de la fecha base
                EConstanteKE econske = null;
                Timestamp ufec = null;
                String[] vcanales = null;
                Timestamp ultimointervalo = null;
                try {
                    ufec = cp.findUltimafechaLec(med.getnSerie());
                    ultimointervalo = ufec;
                } catch (Exception e) {
                }
                if (ufec == null) {
                    ultimointervalo = tsfechaactual;
                }
                String idvar = "";//variable que controla la unidad de los canales                
                for (String vectorperfil[] : arrayperfil) {
                    int i = 0;
                    while (i < vectorperfil.length) {
                        if ((vectorperfil[i] + vectorperfil[i + 1] + vectorperfil[i + 2] + vectorperfil[i + 3]).equals("502E3031")) {//es inicio de bloque.
                            ncontadorintervalos = 0;//reiniciamos el contador de intervalos para solo tener en cuenta los intervalos siguientes apartir de la fecha encontrada                            
                            i += 5;
                            if ((vectorperfil[i] + vectorperfil[i + 1] + vectorperfil[i + 2] + vectorperfil[i + 3] + vectorperfil[i + 4]).equals("4552524F52")) {//no vienen datos error
                                i += 6;
                                ////System.out.println("Sin Informacion");
                            } else {
                                fechaintervalo = null;
                                //formato fecha yyyMMddHHmm
                                try {
                                    //se toma el valor de la fecha  en el formato 
                                    long data = sdf.parse(Hex2ASCII(vectorperfil[i + 1]) + Hex2ASCII(vectorperfil[i + 2]) + Hex2ASCII(vectorperfil[i + 3]) + Hex2ASCII(vectorperfil[i + 4]) + Hex2ASCII(vectorperfil[i + 5]) + Hex2ASCII(vectorperfil[i + 6])
                                            + Hex2ASCII(vectorperfil[i + 7]) + Hex2ASCII(vectorperfil[i + 8]) + Hex2ASCII(vectorperfil[i + 9]) + Hex2ASCII(vectorperfil[i + 10])).getTime();
                                    fechaintervalo = new Timestamp(data); //se convierte el formato en timestamp no se toman los segundos                                    
                                    //System.out.println("Fecha Intervalo " + fechaintervalo);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                i += 19;
                                intervalo = Integer.parseInt(Hex2ASCII(vectorperfil[i]) + Hex2ASCII(vectorperfil[i + 1])); //se obtiene el valor de los intervalos 15,30,60
                                //es un intervalo de lectura
                                i += 4;
                                numcanales = Integer.parseInt(Hex2ASCII(vectorperfil[i])); //del bloque se revisa el numero de canales
                                i += 2;
                                canales = new ArrayList<String>();
                                for (int n = 0; n < numcanales; n++) {
                                    //se hace un ciclo para idenfiticar los valores de los canales
                                    String canal = "";
                                    if (vectorperfil[i].equals("28")) {//inicia valor inicio de parentesis (
                                        ////System.out.println("Inicio Intervalo codigos");
                                        i++;
                                        while (!vectorperfil[i].equals("29")) { //concatena valores hasta encontrar un fin de dato parentesis )
                                            canal = canal + vectorperfil[i]; //concatena el valor

                                            i++;
                                        }
                                        //System.out.println("canal " + Hex2ASCII(canal));
                                        vcanales = Hex2ASCII(canal).split(":");
                                        canales.add(vcanales[1].replace(".", "0")); //el valor viene en hexa y se debe pasar a ascii y luego eliminar el punto y convertirlo en 0 para que se convierta en un canal telesimex
                                        i++;
                                    }
                                    //continua en el ciclo para obtener las unidades que se omiten por que solo se toman las de telesimex
                                    if (vectorperfil[i].equals("28")) {//inicia valor inicio parentesis (
                                        i++;
                                        while (!vectorperfil[i].equals("29")) { //si encontramos el valor final ) parentesis  nos saltamos estos valores
                                            i++;
                                        }
                                        i++;
                                    }
                                }
                                if ((vectorperfil[i] + vectorperfil[i + 1]).equals("0D0A")) {//buscamos el fin de linea
                                    i += 2;
                                    ////System.out.println("Fin Intervalo");
                                }
                                //verificamos si la fechaactual del intervalo supera a la ultima fecha de lectura en el doble si lo supera es por que existe un hueco en el perfil de carga que debe rellenarse
                                if (ultimointervalo != null && fechaintervalo != null) {
                                    //System.out.println("fecha intervalo " + fechaintervalo);
                                    //System.out.println("fecha ultimo intervalo " + ultimointervalo);
                                    //System.out.println("Valor diferencia entre intervalos " + (fechaintervalo.getTime() - ultimointervalo.getTime()));
                                    if (fechaintervalo.getTime() - ultimointervalo.getTime() > (intervalo * 60000)) {
                                        //System.out.println("existe hueco en el perfil");
                                        //calculamos el numero de intervalos que debemos rellenar                                        
                                        long intervaloscero = ((fechaintervalo.getTime() - ultimointervalo.getTime()) / (intervalo * 60000)) - 1;
                                        //System.out.println("numero de intervalor en cero " + intervaloscero);
                                        for (int k = 1; k <= intervaloscero; k++) {
                                            Timestamp tscero = new Timestamp(ultimointervalo.getTime() + (intervalo * 60000 * k));
                                            //System.out.println("intervalo en 0 " + tscero);
                                            for (int n = 0; n < numcanales; n++) {
                                                String nlec = "0";
                                                econske = cp.buscarConske(lconske, Integer.parseInt(canales.get(n))); //buscamos el valor de la constante creada en telesimex
                                                if (econske != null) {
                                                    idvar = "";
                                                    //buscamos el idvar del canal que es el valor de la unidad para cada uno de los canales
                                                    for (EtipoCanal et : vtipocanal) {
                                                        if (Integer.parseInt(et.getCanal()) == Integer.parseInt(canales.get(n))) {
                                                            idvar = et.getUnidad();

                                                        }
                                                    }
                                                    //si los 2 valores constante y idvar existen? se almacena la lecturas
                                                    if (idvar.length() > 0) {
                                                        lec = new Electura(new Timestamp(tscero.getTime()), med.getnSerie(), Integer.parseInt(canales.get(n)), trasnformarEnergia(Double.valueOf(nlec), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.valueOf(nlec), intervalo, idvar);
                                                        arraylec.add(lec);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ultimointervalo = fechaintervalo;
                            }

                        } else {
                            //vienen datos   
                            //System.out.println("Hay Datos de canales");
                            String nlec = "";
                            for (int n = 0; n < numcanales; n++) {
                                nlec = "";
                                if (vectorperfil[i].equals("28")) {//inicia valor lecturas
                                    ////System.out.println("Inicio Intervalo datos");
                                    i++;
                                    while (!vectorperfil[i].equals("29")) { //este ciclo se debe hacer hasta que encuentre un 29 o ) parentesis que es donde termina el dato                                       
                                        nlec = nlec + vectorperfil[i]; //concatena el valor hasta que termine
                                        i++;
                                    }
                                    nlec = Hex2ASCII(nlec); //el valor viene en hexa pero en realidad son ascii por lo que se hace la conversion
                                    i++;
                                    //System.out.println("Fecha " + new Timestamp((fechaintervalo.getTime() + (intervalo * 60000 * ncontadorintervalos))));
                                    //System.out.println("Lectura " + nlec + "   canal " + canales.get(n));
                                    econske = cp.buscarConske(lconske, Integer.parseInt(canales.get(n))); //buscamos el valor de la constante creada en telesimex
                                    if (econske != null) {
                                        idvar = "";
                                        //buscamos el idvar del canal que es el valor de la unidad para cada uno de los canales
                                        for (EtipoCanal et : vtipocanal) {
                                            if (Integer.parseInt(et.getCanal()) == Integer.parseInt(canales.get(n))) {
                                                idvar = et.getUnidad();
                                            }
                                        }
                                        //si los 2 valores constante y idvar existen? se almacena la lecturas
                                        if (idvar.length() > 0) {
                                            lec = new Electura(new Timestamp((fechaintervalo.getTime() + (intervalo * 60000 * ncontadorintervalos))), med.getnSerie(), Integer.parseInt(canales.get(n)), trasnformarEnergia(Double.valueOf(nlec), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.valueOf(nlec), intervalo, idvar);
                                            arraylec.add(lec);
                                            timeufec = new Timestamp((fechaintervalo.getTime() + (intervalo * 60000 * ncontadorintervalos)));
                                            ultimointervalo = lec.getFecha();
                                        }
                                    }
                                } else {
                                    ////System.out.println("Error en almacenamiento");                                    
                                }
                            }
                            if ((vectorperfil[i] + vectorperfil[i + 1]).equals("0D0A")) {//buscamos el fin de linea
                                i += 2;
                            }
                            ncontadorintervalos++;
                        }
                    }
                }
                try {
                    cp.actualizaLectura(arraylec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                    if (timeufec != null) {
                        if (ufec != null) {
                            if (timeufec.after(ufec)) {
                                cp.actualizaFechaLectura(med.getnSerie(), sdf4.format(new Date(timeufec.getTime())));
                            }
                        } else {
                            cp.actualizaFechaLectura(med.getnSerie(), sdf4.format(new Date(timeufec.getTime())));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (leventos) {//almacena Eventos
                String tramaconexion = "";
                String tramadesconexion = "";
                boolean guardar = false;
                String valortipoevento = "";
                int nadicionales = 0;
                ERegistroEvento eregeven = null;
                for (String vectoreventos[] : arrayeventos) {
                    int i = 0;
                    //System.out.println("Tamaño de informacion " + vectoreventos.length);
                    //System.out.println("Valor de bloque => " + Arrays.toString(vectoreventos));
                    while (i < vectoreventos.length) {
                        //obtenemos el valor de la fecha                                              
                        if ((i + 4) > vectoreventos.length || (vectoreventos[i] + vectoreventos[i + 1] + vectoreventos[i + 2] + vectoreventos[i + 3] + vectoreventos[i + 4]).equals("4552524F52")) {
                            //viene error en el bloque
                            //System.out.println("Error de bloque ");
                            i += 6;
                        } else if ((vectoreventos[i] + vectoreventos[i + 1] + vectoreventos[i + 2] + vectoreventos[i + 3]).equals("502E3938")) {//es inicio de bloque.
                            //System.out.println("Inicio de bloque ");
                            i += 5;
                        } else {
                            //obtenemos fecha del evento
                            String fecha = Hex2ASCII(vectoreventos[i + 1] + vectoreventos[i + 2] + vectoreventos[i + 3] + vectoreventos[i + 4] + vectoreventos[i + 5] + vectoreventos[i + 6] + vectoreventos[i + 7] + vectoreventos[i + 8] + vectoreventos[i + 9] + vectoreventos[i + 10] + vectoreventos[i + 11] + vectoreventos[i + 12]);
                            i += 14;
                            valortipoevento = "";
                            if (vectoreventos[i].equals("28")) {//inicia de evento
                                i++;
                                while (!vectoreventos[i].equals("29")) { //este ciclo se debe hacer hasta que encuentre un 29 o ) parentesis que es donde termina el dato                                       
                                    valortipoevento = valortipoevento + vectoreventos[i]; //concatena el valor hasta que termine
                                    i++;
                                }
                            }
                            i++;
                            String codigo = Hex2ASCII(valortipoevento);
                            //System.out.println("Fecha Evento " + fecha);
                            //System.out.println("Codigo evento " + codigo);
                            if (codigo.substring(codigo.length() - 2, codigo.length() - 1).equals("8")) {
                                tramadesconexion = fecha;
                                tramaconexion = "";
                                //System.out.println("Evento de corte");
                                //System.out.println("Fecha corte " + fecha);
                            }
                            if (codigo.substring(codigo.length() - 2, codigo.length() - 1).equals("4")) {
                                tramaconexion = fecha;
                                //System.out.println("Evento de reconexion");
                                //System.out.println("Fecha reconexion " + fecha);
                            }

                            while (!(vectoreventos[i] + vectoreventos[i + 1]).equals("0D0A")) {
                                i++;
                            }
                            i += 2;
                            if (tramaconexion.length() > 0 && tramadesconexion.length() > 0) {
                                guardar = true;
                            }
                            if (guardar) {
                                eregeven = new ERegistroEvento();
                                eregeven.setVcfechacorte(new Timestamp(sdf4.parse(tramadesconexion).getTime()));
                                eregeven.setVcfechareconexion(new Timestamp(sdf4.parse(tramaconexion).getTime()));
                                eregeven.setVcserie(med.getnSerie());
                                eregeven.setVctipo("0001");
                                cp.actualizaEvento(eregeven, null);
                                tramaconexion = "";
                                tramadesconexion = "";
                                guardar = false;
                            }
                        }
                    }
                    i = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
