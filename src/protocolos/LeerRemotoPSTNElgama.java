/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaPSTNElgama;
import Entidades.Abortar;
import Entidades.EConfModem;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.Electura;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
public class LeerRemotoPSTNElgama implements Runnable, SerialPortEventListener {

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
    int numcanales;
    int ndias;
    int numeroReintentos = 4;
    int nreintentos = 0;
    SerialPort serialPort;
    Enumeration portList;
    private CommPortIdentifier portId;
    boolean portFound = false;
    boolean portconect = false;
    TramasRemotaPSTNElgama tramaspstn = new TramasRemotaPSTNElgama();
    String cadenahex = "";
    long timeout;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    boolean conexionModem = false;
    int indiceconexion = 0;
    ControlProcesos cp;
    Vector<EConfModem> confModem;
    private boolean perfilincompleto = false;
    EMedidor med;
    boolean enviando = false;
    byte[] ultimatramaEnviada = null;
    int reintentosbadCRC = 0;
    public boolean leer = true;
    boolean aviso = false;
    int indx = 0;
    String fechaActual = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    int intervalo = 0;
    int factorIntervalo = 0;
    long tiemporetransmision = 6000;
    int actualReintento = 0;
    boolean inicia1 = false;
    boolean desbloqueo = false;
    Thread port = null;
    Thread port2;
    Thread port3;
    boolean cierrapuerto = false;
    boolean esperandoconexion = true;
    boolean lconfiguracion = false;
    boolean inicio = false;
    boolean inicio2 = false;
    boolean inicio3 = false;
    boolean lregistroEventos1 = false;
    boolean lregistroEventos2 = false;
    boolean lperfilCarga = false;
    boolean lregistrodias = false;
    boolean lregistromes = false;
    int version = 0; //version 0 es old, version 1 new
    Vector<String[]> powerfails = null;
    Vector<String[]> phasefails = null;
    Vector<String[]> loadProfile1;//canal 1
    Vector<String[]> loadProfile2;//canal 2
    Vector<String[]> loadProfile3;//canal 3
    Vector<String[]> loadProfile4;//canal 4
    Vector<String[]> regdias;//registro acumulados diarios
    Vector<String[]> regmeses;//registro acumulado mensual
    boolean complemento = false;
    public String tramaIncompleta = "";
    int adp = 1;
    boolean reenviando = false;
    int ndia = 3;
    int ndiaReg = 5; //numero de dias default para traer los acumulados por dias
    int ndiasRegSin = 0;
    int nmesReg = 3; //numero de meses default para traer los acumulados por mes
    int nmesRegSin = 0;
    int ncanal = 1;
    int parte = 0;
    double ktru = 0;
    double ktri = 0;
    int exponent = 0;
    int reintentosUltimatrama = 5;
    int actualUltimatrama = 0;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    String usuario = "admin";
    ZoneId zid;

    public LeerRemotoPSTNElgama(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, boolean registros, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, ZoneId zid) {
        this.med = med;
        this.confModem = confmodem;
        this.cp = cp;
        this.zid = zid;
        File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
        if (!f.exists()) {
            f.mkdirs();
        }
        file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");
//        file = new File(cp.rutalogs + "" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");

        try {
            if (file.exists()) {
                existearchivo = true;
            } else {
                //////System.out.println();
            }
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }
        lperfil = perfil;
        leventos = eventos;
        lregistros = registros;
        if (lperfil) {
            lregistros = true;
        }
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

    private void jinit() {
        tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        if (confModem.size() > 0) {
            version = 0;
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            portList = CommPortIdentifier.getPortIdentifiers();
            timeout = (long) (med.getTimeout());
            ndias = med.getNdias() + 1;
            seriemedidor = med.getnSerie();
            numcanales = med.getNcanales();
            String serie = seriemedidor;
            while (serie.length() < 12) {
                serie = "0" + serie;
            }
            try {
                int p = 0;
                for (int j = 5; j >= 0; j--) {
                    vecSerieMedidor[j] = serie.substring(p, p + 2);
                    p += 2;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            while (portList.hasMoreElements()) {
                portId = (CommPortIdentifier) portList.nextElement();
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (portId.getName().equals(numeroPuerto)) {
                        portFound = true;
                        ////System.out.println("Puerto " + numeroPuerto);
                        break;
                    }
                }
            }
            try {
                ////System.out.println("puerto " + portFound);
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
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("No Leido", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Intento de conexion sin respuesta");
                cerrarLog("No conectado", false);
                leer = false;
            }

        } else {
            try {
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerPSTNELgama", "Intento de conexion sin configuracion de modem");
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
                    procesaCadena();
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
            cadenahex = tramaspstn.encode(readBuffer, numbytes);
            //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte                        
            interpretaCadena(cadenahex);

        } catch (ArrayIndexOutOfBoundsException aiob) {
            aiob.printStackTrace();
            cerrarPuerto();
            try {
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("No leido", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            cerrarLog("Error de protocolo", false);
            leer = false;
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
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion No se encuentra portadora");
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
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
                //////System.out.println("Respondio No carrier iniciamos la comunicacion" + tramainicial);
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

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
                //////System.out.println("Respondio No carrier iniciamos la comunicacion" + tramainicial);
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                byte[] vtramainicial = cp.ConvertASCIItoHex(tramainicial);
                indiceconexion++;
                desbloqueo = false;
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                enviaTrama(vtramainicial);

            } else if (cadenahex.contains("4E 4F 20 44 49 41 4C 54 4F 4E 45")) {//no dialtone
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion No se encuentra linea");
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("No Dialtone", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }
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
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion portadora ocupada");
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("Ocupado", indx, 3);
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
                        // lm.jtablemedidores.setValueAt("Conectado", indx, 3);
                        //lm.mdc.fireTableDataChanged();
                    }
                } catch (Exception e) {
                }

                //se conecto al modem
                lconfiguracion = true;
                inicio = true;
                adp = 1;
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                conexionModem = false;
                //enviando = true;
                ////System.out.println("enviamos trama 1");
                tiemporetransmision = timeout * 1000;
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("Comunicando..", indx, 3);
                    try {
                        //lm.mdc.fireTableDataChanged();
                    } catch (Exception e) {
                    }

                }
                byte[] tramaelgama = tramaspstn.getLocal0();
                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                //adp
                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                int crcCalculado = CRCElgama(tramaelgama);
                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                while (calculo.length() < 4) {
                    calculo = "0" + calculo;
                }
                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                ultimatramaEnviada = tramaelgama;
                enviaTrama2(tramaelgama, "=> Solicitud de Configuracion");
            } else if (cadenahex.contains("4F 4B")) {//ok
                escribir("<= OK " + cadenahex);
                nreintentos = 0;
                String trama = confModem.get(0).getpeticion(indiceconexion);

                if (trama.equals("")) {
                    byte[] vtrama = null;
                    if (med.getTipoconexion() == 1) {
                        vtrama = tramaspstn.StringToHexMarcadoPSTN(med.getNumtelefonico().trim());
                    } else {
                        vtrama = tramaspstn.StringToHexMarcadoGPRS(med.getDireccionip().trim() + "/" + med.getPuertoip().trim());
                    }
                    esperandoconexion = true;
                    enviaTramaConexion(vtrama, "=> Solicitud de conexion Numero telefono " + med.getNumtelefonico());

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    tiemporetransmision = 5000;
                    trama = trama.replace("2E", "0D");
                    byte[] vtrama = cp.ConvertASCIItoHex(trama);
                    indiceconexion++;
                    enviaTrama2(vtrama, "=> Envio Trama AT " + trama);
                }
            } else if (cadenahex.contains("4E 4F 20 41 4E 53 57 45 52")) { //NO answer
                escribir("<= Sin respuesta " + cadenahex);
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion no responde comunicacion");
                try {
                    if (aviso) {
                        // lm.jtablemedidores.setValueAt("No answer", indx, 3);
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
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion Error lectura de protocolo");
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
        } else {//conexion con medidor

            escribir("Recibe <= " + cadenahex);
            String[] vectorhex = cadenahex.split(" ");
            if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52")) {//Se cayo el modem o se desconecto      
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion Modem");
                cerrarPuerto();
                if (lperfilCarga & perfilincompleto) {
                    AlmacenarRegistrosIncompletos();
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
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    escribir("Estado lectura No leido");
                    leer = false;
                }

            } else if (inicio) {//la primera trama de configuracion               
                if (!complemento) {//no es complemento
                    if (vectorhex[0].equals("2F")) {
                        if (vectorhex.length > 8) {
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        if (Integer.parseInt(vectorhex[1], 16) + 1 > 20) { //es version nueva
                                            if (Integer.parseInt(vectorhex[18], 16) < 5) {
                                                version = 0;
                                                ////System.out.println("OLD");
                                                escribir("OLD version");
                                            } else {
                                                escribir("NEW Version");
                                                ////System.out.println("NEW");
                                                ktru = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhex[20], 16)) + "." + String.valueOf(Integer.parseInt(vectorhex[19], 16)));
                                                ktri = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhex[22], 16)) + "." + String.valueOf(Integer.parseInt(vectorhex[21], 16)));
                                                exponent = Integer.parseInt(vectorhex[23], 16);
                                                escribir("KTRU: " + ktru);
                                                escribir("KTRI: " + ktri);
                                                escribir("EXP: " + exponent);
                                                ////System.out.println("KTRU: " + ktru);
                                                ////System.out.println("KTRI: " + ktri);
                                                ////System.out.println("EXP: " + exponent);
                                                version = 1;
                                            }
                                        } else {//es version vieja
                                            version = 0;
                                            ////System.out.println("OLD");
                                            escribir("OLD version");
                                        }
                                        intervalo = 60 / (Integer.parseInt(vectorhex[16], 16));
                                        ////System.out.println("Intervalo " + intervalo);
                                        byte[] fecha = {(byte) (Integer.parseInt(vectorhex[13], 16) & 0xFF),
                                            (byte) (Integer.parseInt(vectorhex[12], 16) & 0xFF),
                                            (byte) (Integer.parseInt(vectorhex[11], 16) & 0xFF),
                                            (byte) (Integer.parseInt(vectorhex[10], 16) & 0xFF)};
                                        int year = (int) ((fecha[0] & 0xFE) >> 1);
                                        int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                                        int dia = (int) ((fecha[1] & 0x1F));
                                        int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                                        int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                                        int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                                        ////System.out.println("Fecha actual: " + year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg);
                                        fechaActual = year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg;
                                        try {
                                            escribir("Fecha Actual Estacion de trabajo " + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()));
                                            escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaActual).getTime()));
                                            if (med.getFecha() != null) {
                                                escribir("Fecha Ultima Lectura " + med.getFecha());
                                                Long actual = new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()).getTime();
                                                Long ultimamed = med.getFecha().getTime();
                                                Long calculo = actual - ultimamed;
                                                int diasaleer = (int) (calculo / 86400000);
                                                if (calculo % 86400000 > 0) {
                                                    diasaleer = diasaleer + 1;
                                                }
                                                ////System.out.println("dias calculados " + diasaleer);
                                                if (diasaleer > 0) {
                                                    if (diasaleer > 35) {
                                                        ndias = 35;
                                                    } else {
                                                        ndias = diasaleer;
                                                    }
                                                }
                                                escribir("Numero de dias leer calculado " + (ndias + 1));
                                            }
                                        } catch (Exception e) {
                                        }
                                        ndias = ndias + 1;
                                        adp += 1;
                                        adp = adp % 256;
                                        byte tramaelgama[] = tramaspstn.getLocal1();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud Configuracion", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, "=> Solicitud de Configuracion 2");
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        escribir("BAD CRC");
                                        escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                }

                            } else {
                                //trama incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                rutinaCorrecta = false;
                                //enviamos ultimatrama
                                escribir("Trama incompleta " + vectorhex.length + " bytes");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }

                    } else {//puede llegar sin 2F
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    actualUltimatrama = 0;
                                    if (Integer.parseInt(vectorhex[0], 16) > 19) { //es version nueva
                                        if (Integer.parseInt(vectorhex[17], 16) < 5) {
                                            version = 0;
                                            ////System.out.println("OLD");
                                            escribir("OLD version");
                                        } else {
                                            escribir("NEW Version");
                                            ////System.out.println("NEW");
                                            ktru = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhex[19], 16)) + "." + String.valueOf(Integer.parseInt(vectorhex[18], 16)));
                                            ktri = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhex[21], 16)) + "." + String.valueOf(Integer.parseInt(vectorhex[20], 16)));
                                            exponent = Integer.parseInt(vectorhex[23], 16);
                                            escribir("KTRU: " + ktru);
                                            escribir("KTRI: " + ktri);
                                            escribir("EXP: " + exponent);
                                            ////System.out.println("KTRU: " + ktru);
                                            ////System.out.println("KTRI: " + ktri);
                                            ////System.out.println("EXP: " + exponent);
                                            version = 1;
                                        }
                                    } else {//es version vieja
                                        version = 0;
                                        ////System.out.println("OLD");
                                        escribir("OLD version");
                                    }
                                    intervalo = 60 / (Integer.parseInt(vectorhex[15], 16));
                                    ////System.out.println("Intervalo " + intervalo);
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
                                    ////System.out.println("Fecha actual: " + year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg);
                                    fechaActual = year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg;
                                    try {
                                        escribir("Fecha Actual Estacion de trabajo " + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()));
                                        escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaActual).getTime()));
                                        if (med.getFecha() != null) {
                                            escribir("Fecha Ultima Lectura " + med.getFecha());
                                            Long actual = new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()).getTime();
                                            Long ultimamed = med.getFecha().getTime();
                                            Long calculo = actual - ultimamed;
                                            int diasaleer = (int) (calculo / 86400000);
                                            if (calculo % 86400000 > 0) {
                                                diasaleer = diasaleer + 1;
                                            }
                                            ////System.out.println("dias calculados " + diasaleer);
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
                                    ndias = ndias + 1;
                                    adp += 1;
                                    adp = adp % 256;
                                    byte tramaelgama[] = tramaspstn.getLocal1();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                        if (aviso) {
                                            // lm.jtablemedidores.setValueAt("Solicitud Configuracion", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, "=> Solicitud de Configuracion 2");
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    escribir("BAD CRC");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }


                    }
                } else {//es complemento
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("2F")) {
                        if (vectorhextemp.length > 2) {
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        if (Integer.parseInt(vectorhextemp[1], 16) + 1 > 20) { //es version nueva
                                            if (Integer.parseInt(vectorhextemp[18], 16) < 5) {
                                                version = 0;
                                                escribir("OLD Version");
                                            } else {
                                                version = 1;
                                                escribir("NEW Version");
                                                ktru = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhextemp[20], 16)) + "." + String.valueOf(Integer.parseInt(vectorhextemp[19], 16)));
                                                ktri = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhextemp[22], 16)) + "." + String.valueOf(Integer.parseInt(vectorhextemp[21], 16)));
                                                exponent = Integer.parseInt(vectorhextemp[23], 16);
                                                escribir("KTRU: " + ktru);
                                                escribir("KTRI: " + ktri);
                                                escribir("EXP: " + exponent);
                                                ////System.out.println("KTRU: " + ktru);
                                                ////System.out.println("KTRI: " + ktri);
                                                ////System.out.println("EXP: " + exponent);
                                            }

                                        } else {//es version vieja
                                            version = 0;
                                            escribir("OLD Version");
                                        }
                                        escribir("Numero de canales 4");
                                        intervalo = 60 / (Integer.parseInt(vectorhextemp[16], 16));
                                        ////System.out.println("Intervalo " + intervalo);
                                        escribir("Periodo de integracion " + intervalo);
                                        byte[] fecha = {(byte) (Integer.parseInt(vectorhextemp[13], 16) & 0xFF),
                                            (byte) (Integer.parseInt(vectorhextemp[12], 16) & 0xFF),
                                            (byte) (Integer.parseInt(vectorhextemp[11], 16) & 0xFF),
                                            (byte) (Integer.parseInt(vectorhextemp[10], 16) & 0xFF)};
                                        int year = (int) ((fecha[0] & 0xFE) >> 1);
                                        int month = (int) (((fecha[0] & 0x01) << 3) | ((fecha[1] & 0xE0) >> 5));
                                        int dia = (int) ((fecha[1] & 0x1F));
                                        int hora = (int) ((fecha[2] & 0x00000F8) >> 3);
                                        int min = (int) (((fecha[2] & 0x07) << 3) | ((fecha[3] & 0x00000E0) >> 5));
                                        int seg = (int) ((fecha[3] & 0x0000001F) * 2);
                                        ////System.out.println("Fecha actual: " + year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg);
                                        fechaActual = year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg;
                                        try {
                                            escribir("Fecha Actual Estacion de trabajo " + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()));
                                            escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaActual).getTime()));
                                            if (med.getFecha() != null) {
                                                escribir("Fecha Ultima Lectura " + med.getFecha());
                                                Long actual = new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()).getTime();
                                                Long ultimamed = med.getFecha().getTime();
                                                Long calculo = actual - ultimamed;
                                                int diasaleer = (int) (calculo / 86400000);
                                                if (calculo % 86400000 > 0) {
                                                    diasaleer = diasaleer + 1;
                                                }
                                                ////System.out.println("dias calculados " + diasaleer);
                                                if (diasaleer > 0) {
                                                    if (diasaleer > 30) {
                                                        ndias = 30;
                                                    } else {
                                                        ndias = diasaleer;

                                                    }
                                                }

                                                escribir("Numero de dias leer calculado " + (ndias + 1));
                                            }
                                            ndias = ndias + 1;
                                        } catch (Exception e) {
                                        }
                                        adp += 1;
                                        adp = adp % 256;
                                        byte tramaelgama[] = tramaspstn.getLocal1();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud de configuracion", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }

                                        enviaTrama2(tramaelgama, "=> Solicitud de Configuracion 2");
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                }

                            } else {
                                //trama incompleta
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }

                    } else {//puede llegar sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    if (Integer.parseInt(vectorhextemp[0], 16) > 19) { //es version nueva
                                        if (Integer.parseInt(vectorhextemp[17], 16) < 5) {
                                            version = 0;
                                            escribir("OLD Version");
                                        } else {
                                            version = 1;
                                            escribir("NEW Version");
                                            ktru = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhextemp[19], 16)) + "." + String.valueOf(Integer.parseInt(vectorhextemp[18], 16)));
                                            ktri = Double.parseDouble(String.valueOf(Integer.parseInt(vectorhextemp[21], 16)) + "." + String.valueOf(Integer.parseInt(vectorhextemp[20], 16)));
                                            exponent = Integer.parseInt(vectorhextemp[23], 16);
                                            escribir("KTRU: " + ktru);
                                            escribir("KTRI: " + ktri);
                                            escribir("EXP: " + exponent);
                                            ////System.out.println("KTRU: " + ktru);
                                            ////System.out.println("KTRI: " + ktri);
                                            ////System.out.println("EXP: " + exponent);
                                        }
                                    } else {//es version vieja
                                        version = 0;
                                        escribir("OLD Version");
                                    }
                                    escribir("Numero de canales 4");
                                    intervalo = 60 / (Integer.parseInt(vectorhextemp[15], 16));
                                    ////System.out.println("Intervalo " + intervalo);
                                    escribir("Periodo de integracion " + intervalo);
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
                                    ////System.out.println("Fecha actual: " + year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg);
                                    fechaActual = year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg;
                                    try {
                                        escribir("Fecha Actual Estacion de trabajo " + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()));
                                        escribir("Fecha Actual Medidor " + new Timestamp(sdf2.parse(fechaActual).getTime()));
                                        if (med.getFecha() != null) {
                                            escribir("Fecha Ultima Lectura " + med.getFecha());
                                            Long actual = new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()).getTime();
                                            Long ultimamed = med.getFecha().getTime();
                                            Long calculo = actual - ultimamed;
                                            int diasaleer = (int) (calculo / 86400000);
                                            if (calculo % 86400000 > 0) {
                                                diasaleer = diasaleer + 1;
                                            }
                                            ////System.out.println("dias calculados " + diasaleer);
                                            if (diasaleer > 0) {
                                                if (diasaleer > 30) {
                                                    ndias = 30;
                                                } else {
                                                    ndias = diasaleer;
                                                }
                                            }
                                            escribir("Numero de dias leer calculado " + (ndias + 1));
                                        }
                                        ndias = ndias + 1;
                                    } catch (Exception e) {
                                    }
                                    adp += 1;
                                    adp = adp % 256;
                                    byte tramaelgama[] = tramaspstn.getLocal1();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    //adp
                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                        if (aviso) {
                                            // lm.jtablemedidores.setValueAt("Solicitud de configuracion", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }

                                    enviaTrama2(tramaelgama, "=> Solicitud de Configuracion 2");
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }
                    }
                }
            } else if (inicio2) {//segunda trama de configuracion
                if (!complemento) {//no es complemento
                    if (vectorhex[0].equals("2F")) {
                        if (vectorhex.length > 2) {
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        adp += 1;
                                        adp = adp % 256;
                                        byte tramaelgama[] = tramaspstn.getLocal1();
                                        String mensaje = "";
                                        inicio2 = false;
                                        if (leventos) {
                                            tramaelgama = tramaspstn.getLocal4();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            lregistroEventos1 = true;
                                            powerfails = new Vector<String[]>();
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Registro Eventos Powerfail", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
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
                                                ////System.out.println("Registros diarios");
                                                //datos para registros
                                                inicio3 = true;
                                                tramaelgama = tramaspstn.getLocal1();
                                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);

                                                //adp
                                                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                try {
                                                    if (aviso) {
                                                        // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                                        //lm.mdc.fireTableDataChanged();
                                                    }
                                                } catch (Exception e) {
                                                }
                                                enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                                            } else if (lperfil) {
                                                ncanal = 1;
                                                tramaelgama = tramaspstn.getLocal2();
                                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                                //adp
                                                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                ndia = ndias - 1;
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;

                                                lperfilCarga = true;
                                                loadProfile1 = new Vector<String[]>();
                                                loadProfile2 = new Vector<String[]>();
                                                loadProfile3 = new Vector<String[]>();
                                                loadProfile4 = new Vector<String[]>();
                                                parte = 0;
                                                ncanal = 1;
                                                try {
                                                    if (aviso) {
                                                        // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                        //lm.mdc.fireTableDataChanged();
                                                    }
                                                } catch (Exception e) {
                                                }
                                                mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                                enviaTrama2(tramaelgama, mensaje);
                                            } else {
                                                cerrarPuerto();
                                                escribir("Estado lectura leido");

                                                AlmacenarRegistros();
                                                cerrarLog("Leido", true);
                                                med.MedLeido = true;
                                                leer = false;
                                            }
                                        }
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                                    }
                                } else {//no es el adp correcto

                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                }
                            } else {
                                //trama incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                rutinaCorrecta = false;
                                //enviamos ultimatrama
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            //enviamos ultimatrama

                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }
                    } else {//puede llegar sin 2F
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    actualUltimatrama = 0;
                                    adp += 1;
                                    adp = adp % 256;
                                    byte tramaelgama[] = tramaspstn.getLocal1();
                                    String mensaje = "";
                                    inicio2 = false;
                                    if (leventos) {
                                        tramaelgama = tramaspstn.getLocal4();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        lregistroEventos1 = true;
                                        powerfails = new Vector<String[]>();
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Registro Eventos Powerfail", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
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
                                            ////System.out.println("Registros diarios");
                                            //datos para registros
                                            inicio3 = true;
                                            tramaelgama = tramaspstn.getLocal1();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);

                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                                        } else if (lperfil) {
                                            ncanal = 1;
                                            tramaelgama = tramaspstn.getLocal2();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            ndia = ndias - 1;
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            lperfilCarga = true;
                                            loadProfile1 = new Vector<String[]>();
                                            loadProfile2 = new Vector<String[]>();
                                            loadProfile3 = new Vector<String[]>();
                                            loadProfile4 = new Vector<String[]>();
                                            ncanal = 1;
                                            parte = 0;
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            cerrarPuerto();
                                            escribir("Estado lectura leido");
                                            AlmacenarRegistros();

                                            med.MedLeido = true;
                                            cerrarLog("Leido", true);
                                            leer = false;
                                        }
                                    }
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion 2");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            //enviamos ultimatrama
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                        }
                    }
                } else {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("2F")) {
                        if (vectorhextemp.length > 2) {
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        adp += 1;
                                        adp = adp % 256;
                                        byte tramaelgama[] = tramaspstn.getLocal1();
                                        String mensaje = "";
                                        inicio2 = false;
                                        if (leventos) {
                                            tramaelgama = tramaspstn.getLocal4();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            lregistroEventos1 = true;
                                            powerfails = new Vector<String[]>();
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Registro Eventos Powerfail", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
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
                                            if (lperfil) {
                                                ncanal = 1;
                                                tramaelgama = tramaspstn.getLocal2();
                                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                                //adp
                                                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                ndia = ndias - 1;
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                enviaTrama2(tramaelgama, mensaje);
                                                lperfilCarga = true;
                                                loadProfile1 = new Vector<String[]>();
                                                loadProfile2 = new Vector<String[]>();
                                                loadProfile3 = new Vector<String[]>();
                                                loadProfile4 = new Vector<String[]>();
                                                parte = 0;
                                                ncanal = 1;
                                                try {
                                                    if (aviso) {
                                                        // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                        //lm.mdc.fireTableDataChanged();
                                                    }
                                                } catch (Exception e) {
                                                }
                                                mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                            } else if (lregistros) {
                                                ////System.out.println("Registros diarios");
                                                //datos para registros
                                                inicio3 = true;
                                                tramaelgama = tramaspstn.getLocal1();
                                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);

                                                //adp
                                                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                try {
                                                    if (aviso) {
                                                        // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                                        //lm.mdc.fireTableDataChanged();
                                                    }
                                                } catch (Exception e) {
                                                }
                                                enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");

                                            } else {
                                                cerrarPuerto();
                                                escribir("Estado lectura leido");

                                                AlmacenarRegistros();
                                                cerrarLog("Leido", true);
                                                med.MedLeido = true;
                                                leer = false;
                                            }
                                        }
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {
                                //trama incompleta
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    } else {//puede llegar sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    adp += 1;
                                    adp = adp % 256;
                                    byte tramaelgama[] = tramaspstn.getLocal1();
                                    String mensaje = "";
                                    inicio2 = false;
                                    if (leventos) {
                                        tramaelgama = tramaspstn.getLocal4();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        lregistroEventos1 = true;
                                        powerfails = new Vector<String[]>();
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Registro Eventos Powerfail", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
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
                                        if (lperfil) {
                                            ncanal = 1;
                                            tramaelgama = tramaspstn.getLocal2();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            ndia = ndias - 1;
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            enviaTrama2(tramaelgama, mensaje);
                                            lperfilCarga = true;
                                            loadProfile1 = new Vector<String[]>();
                                            loadProfile2 = new Vector<String[]>();
                                            loadProfile3 = new Vector<String[]>();
                                            loadProfile4 = new Vector<String[]>();
                                            parte = 0;
                                            ncanal = 1;
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                        } else if (lregistros) {
                                            ////System.out.println("Registros diarios");
                                            //datos para registros
                                            inicio3 = true;
                                            tramaelgama = tramaspstn.getLocal1();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);

                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");

                                        } else {
                                            cerrarPuerto();
                                            escribir("Estado lectura leido");

                                            AlmacenarRegistros();
                                            cerrarLog("Leido", true);
                                            med.MedLeido = true;
                                            leer = false;
                                        }
                                    }
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }


                    }
                }
            } else if (lregistroEventos1) {
                if (!complemento) {
                    if (vectorhex[0].equals("2F")) {//llego correctamente
                        if (vectorhex.length > 2) {
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        String[] trama = new String[(Integer.parseInt(vectorhex[1], 16) + 1)];
                                        System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[1], 16) + 1);
                                        powerfails.add(trama);
                                        lregistroEventos1 = false;
                                        lregistroEventos2 = true;
                                        byte tramaelgama[] = tramaspstn.getLocal5();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("=>Solicitud eventos phasefail", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        ultimatramaEnviada = tramaelgama;
                                        enviaTrama2(tramaelgama, "=>Solicitud de eventos de falla en fases");
                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                                    }
                                } else {//adp incorrecto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                            }
                        } else {//incompleta
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    } else {//puede llegar sin 2F
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    actualUltimatrama = 0;
                                    String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                    System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                    powerfails.add(trama);
                                    lregistroEventos1 = false;
                                    lregistroEventos2 = true;
                                    byte tramaelgama[] = tramaspstn.getLocal5();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                        if (aviso) {
                                            // lm.jtablemedidores.setValueAt("=>Solicitud eventos phasefail", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }
                                    ultimatramaEnviada = tramaelgama;
                                    enviaTrama2(tramaelgama, "=>Solicitud de eventos de falla en fases");
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                                }
                            } else {//adp incorrecto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                            }
                        } else {//incompleta
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    }
                } else {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String cadenahextemp = tramaIncompleta;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("2F")) {    //llego correctamente
                        if (vectorhextemp.length > 2) {
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8])) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        String[] trama = new String[(Integer.parseInt(vectorhextemp[1], 16) + 1)];
                                        System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[1], 16) + 1);
                                        powerfails.add(trama);
                                        lregistroEventos1 = false;
                                        lregistroEventos2 = true;
                                        byte tramaelgama[] = tramaspstn.getLocal5();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("=>Solicitud phasefail", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }

                                        enviaTrama2(tramaelgama, "=>Solicitud de eventos de falla en fases");

                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//adp incorrecto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                                }
                            } else {//incompleta
                                tramaIncompleta = "";
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                            }
                        } else {
                            tramaIncompleta = "";
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }
                    } else {//puede llegar sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7])) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                    System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                    powerfails.add(trama);
                                    lregistroEventos1 = false;
                                    lregistroEventos2 = true;
                                    byte tramaelgama[] = tramaspstn.getLocal5();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    adp += 1;
                                    adp = adp % 256;

                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
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
                                        if (aviso) {
                                            // lm.jtablemedidores.setValueAt("=>Solicitud phasefail", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }

                                    enviaTrama2(tramaelgama, "=>Solicitud de eventos de falla en fases");
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//adp incorrecto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                            }
                        } else {//incompleta
                            tramaIncompleta = "";
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de eventos de falla en fases");
                        }

                    }
                }
            } else if (lregistroEventos2) {
                if (!complemento) {
                    if (vectorhex[0].equals("2F")) {//llego correctamente
                        if (vectorhex.length > 2) {
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        String[] trama = new String[(Integer.parseInt(vectorhex[1], 16) + 1)];
                                        System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[1], 16) + 1);
                                        phasefails.add(trama);
                                        lregistroEventos2 = false;
                                        inicio3 = true;
                                        byte tramaelgama[] = tramaspstn.getLocal1();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;

                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//adp incorrecto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//incompleta
                                tramaIncompleta = cadenahex;
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    } else {//llego inesperado
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    actualUltimatrama = 0;
                                    String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                    System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                    phasefails.add(trama);
                                    lregistroEventos2 = false;
                                    inicio3 = true;
                                    byte tramaelgama[] = tramaspstn.getLocal1();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;

                                    try {
                                        if (aviso) {
                                            // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, "=>Solicitud de fecha y hora actual");
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//adp incorrecto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//incompleta
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    }
                } else {//complemento
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String cadenahextemp = tramaIncompleta;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    tramaIncompleta = "";
                    if (vectorhextemp[0].equals("2F")) {//llego correctamente
                        if (vectorhextemp.length > 2) {
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8])) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        String[] trama = new String[(Integer.parseInt(vectorhextemp[1], 16) + 1)];
                                        System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[1], 16) + 1);
                                        phasefails.add(trama);
                                        lregistroEventos2 = false;
                                        inicio3 = true;
                                        byte tramaelgama[] = tramaspstn.getLocal1();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;

                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, "Solicitud de fecha y hora actual");

                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//adp incorrecto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//incompleta
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//incompleta
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    } else {//sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7])) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                    System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                    phasefails.add(trama);
                                    lregistroEventos2 = false;
                                    inicio3 = true;
                                    byte tramaelgama[] = tramaspstn.getLocal1();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    int crcCalculado = CRCElgama(tramaelgama);
                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                    while (calculo.length() < 4) {
                                        calculo = "0" + calculo;
                                    }
                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                    ultimatramaEnviada = tramaelgama;

                                    try {
                                        if (aviso) {
                                            // lm.jtablemedidores.setValueAt("Fecha y hora", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }
                                    enviaTrama2(tramaelgama, "Solicitud de fecha y hora actual");
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//adp incorrecto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    }
                }

            } else if (lregistrodias) {
                if (!complemento) {
                    if (vectorhex[0].equals("2F")) {
                        if (vectorhex.length > 2) {//tiene cabecera
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        if (vectorhex.length > 12) {
                                            String[] trama = new String[(Integer.parseInt(vectorhex[1], 16) + 1)];
                                            System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[1], 16) + 1);
                                            regdias.add(trama);
                                        } else {
                                            ndiasRegSin++;
                                        }
                                        ndiaReg--;
                                        if (ndiaReg >= 0) {//continuamos solicitando
                                            byte tramaelgama[] = tramaspstn.getLocal6();
                                            adp += 1;
                                            adp = adp % 256;
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            //resta de dia
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                            ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud registros diarios", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
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
                                            byte tramaelgama[] = tramaspstn.getLocal7();
                                            adp += 1;
                                            adp = adp % 256;
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            //resta de dia
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                            ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        }

                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                                }
                            } else {
                                //trama incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                rutinaCorrecta = false;
                                //enviamos ultimatrama
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                            }
                        } else {
                            //no cabecera
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                        }
                    } else {//Sin 2F

                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    if (vectorhex.length > 11) {
                                        String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                        System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                        regdias.add(trama);
                                    } else {
                                        ndiasRegSin++;
                                    }
                                    ndiaReg--;
                                    if (ndiaReg >= 0) {//continuamos solicitando
                                        byte tramaelgama[] = tramaspstn.getLocal6();
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        //resta de dia
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                        ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud registros diarios", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
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
                                        byte tramaelgama[] = tramaspstn.getLocal7();
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        //resta de dia
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                        ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    }
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                            }
                        } else {
                            //trama incompleta
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            //enviamos ultimatrama
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                        }
                    }
                } else {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("2F")) {
                        if (vectorhextemp.length > 2) {//tiene cabecera
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        if (vectorhextemp.length > 12) {
                                            String[] trama = new String[(Integer.parseInt(vectorhextemp[1], 16) + 1)];
                                            System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[1], 16) + 1);
                                            regdias.add(trama);

                                        } else {
                                            ndiasRegSin++;
                                        }
                                        ndiaReg--;
                                        if (ndiaReg >= 0) {//continuamos solicitando
                                            byte tramaelgama[] = tramaspstn.getLocal6();
                                            adp += 1;
                                            adp = adp % 256;
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            //resta de dia
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                            ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud registros diarios", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            //solicitamos el regitro acumulado de meses
                                            nmesReg = med.getNmesreg() - 1;//numero de meses atras
                                            nmesRegSin = 0;
                                            byte tramaelgama[] = tramaspstn.getLocal7();
                                            regmeses = new Vector<String[]>();
                                            lregistrodias = false;
                                            lregistromes = true;
                                            adp += 1;
                                            adp = adp % 256;
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            //resta de dia
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                            ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        }
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {
                                //trama incompleta
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//no cabcera
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }

                    } else {//sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    if (vectorhextemp.length > 12) {
                                        String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                        System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                        regdias.add(trama);
                                    } else {
                                        ndiasRegSin++;
                                    }
                                    ndiaReg--;
                                    if (ndiaReg >= 0) {//continuamos solicitando
                                        byte tramaelgama[] = tramaspstn.getLocal6();
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        //resta de dia
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                        ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud registros diarios", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else {
                                        //solicitamos el regitro acumulado de meses
                                        nmesReg = med.getNmesreg() - 1;//numero de meses atras
                                        nmesRegSin = 0;
                                        byte tramaelgama[] = tramaspstn.getLocal7();
                                        regmeses = new Vector<String[]>();
                                        lregistrodias = false;
                                        lregistromes = true;
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        //resta de dia
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                        ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    }
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    }
                }
            } else if (lregistromes) {
                if (!complemento) {
                    if (vectorhex[0].equals("2F")) {
                        if (vectorhex.length > 2) {//tiene cabecera
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        if (vectorhex.length > 12) {
                                            String[] trama = new String[(Integer.parseInt(vectorhex[1], 16) + 1)];
                                            System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[1], 16) + 1);
                                            regmeses.add(trama);
                                        } else {
                                            nmesRegSin++;
                                        }

                                        nmesReg--;
                                        if (nmesReg >= 0) {
                                            byte tramaelgama[] = tramaspstn.getLocal7();
                                            adp += 1;
                                            adp = adp % 256;
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            //resta de dia
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                            ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {// revisamos si solicitamos perfil o no
                                            lregistromes = false;
                                            if (lperfil) {
                                                adp += 1;
                                                adp = adp % 256;
                                                ndia = ndias - 1;
                                                ncanal = 1;
                                                byte tramaelgama[] = tramaspstn.getLocal2();
                                                String mensaje = "";
                                                lperfilCarga = true;
                                                parte = 0;
                                                loadProfile1 = new Vector<String[]>();
                                                loadProfile2 = new Vector<String[]>();
                                                loadProfile3 = new Vector<String[]>();
                                                loadProfile4 = new Vector<String[]>();
                                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                                //adp
                                                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                                ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                try {
                                                    if (aviso) {
                                                        // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                        //lm.mdc.fireTableDataChanged();
                                                    }
                                                } catch (Exception e) {
                                                }
                                                enviaTrama2(tramaelgama, mensaje);
                                            } else {
                                                cerrarPuerto();
                                                escribir("Estado lectura leido");

                                                AlmacenarRegistros();
                                                cerrarLog("Leido", true);
                                                med.MedLeido = true;
                                                leer = false;
                                            }
                                        }
                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                                }
                            } else {
                                //trama incompleta
                                complemento = true;
                                tramaIncompleta = cadenahex;
                                rutinaCorrecta = false;
                                //enviamos ultimatrama
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                            }
                        } else {
                            //no cabecera
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                        }
                    } else {//sin 2F
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    actualUltimatrama = 0;
                                    if (vectorhex.length > 11) {
                                        String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                        System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                        regmeses.add(trama);
                                    } else {
                                        nmesRegSin++;
                                    }
                                    nmesReg--;
                                    if (nmesReg >= 0) {
                                        byte tramaelgama[] = tramaspstn.getLocal7();
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        //resta de dia
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                        ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else {// revisamos si solicitamos perfil o no
                                        lregistromes = false;
                                        if (lperfil) {
                                            adp += 1;
                                            adp = adp % 256;
                                            ndia = ndias - 1;
                                            ncanal = 1;
                                            byte tramaelgama[] = tramaspstn.getLocal2();
                                            String mensaje = "";
                                            lperfilCarga = true;
                                            parte = 0;
                                            loadProfile1 = new Vector<String[]>();
                                            loadProfile2 = new Vector<String[]>();
                                            loadProfile3 = new Vector<String[]>();
                                            loadProfile4 = new Vector<String[]>();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                            ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            cerrarPuerto();
                                            escribir("Estado lectura leido");

                                            AlmacenarRegistros();
                                            cerrarLog("Leido", true);
                                            med.MedLeido = true;
                                            leer = false;
                                        }
                                    }
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                escribir("=> Espera a envio de ultima trama " + tramaspstn.encode(ultimatramaEnviada, ultimatramaEnviada.length));
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                            }
                        } else {
                            //trama incompleta
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            //enviamos ultimatrama
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de registro acumulado del dia " + ndiaReg);
                        }
                    }
                } else {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("2F")) {
                        if (vectorhextemp.length > 2) {//tiene cabecera
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        if (vectorhex.length > 12) {
                                            String[] trama = new String[(Integer.parseInt(vectorhextemp[1], 16) + 1)];
                                            System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[1], 16) + 1);
                                            regmeses.add(trama);
                                        } else {
                                            nmesRegSin++;
                                        }
                                        nmesReg--;
                                        if (nmesReg >= 0) {
                                            byte tramaelgama[] = tramaspstn.getLocal7();
                                            adp += 1;
                                            adp = adp % 256;
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            //resta de dia
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                            ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            lregistromes = false;
                                            if (lperfil) {
                                                adp += 1;
                                                adp = adp % 256;
                                                ndia = ndias - 1;
                                                ncanal = 1;
                                                byte tramaelgama[] = tramaspstn.getLocal2();
                                                String mensaje = "";
                                                inicio3 = false;
                                                lperfilCarga = true;
                                                parte = 0;
                                                loadProfile1 = new Vector<String[]>();
                                                loadProfile2 = new Vector<String[]>();
                                                loadProfile3 = new Vector<String[]>();
                                                loadProfile4 = new Vector<String[]>();
                                                tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                                tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                                tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                                tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                                tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                                tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                                //adp
                                                tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                                ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                try {
                                                    if (aviso) {
                                                        // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                        //lm.mdc.fireTableDataChanged();
                                                    }
                                                } catch (Exception e) {
                                                }
                                                enviaTrama2(tramaelgama, mensaje);
                                            } else {
                                                cerrarPuerto();
                                                escribir("Estado lectura leido");

                                                AlmacenarRegistros();
                                                cerrarLog("Leido", true);
                                                med.MedLeido = true;
                                                leer = false;
                                            }
                                        }

                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {
                                //trama incompleta
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//no cabcera
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }

                    } else {//sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    if (vectorhex.length > 11) {
                                        String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                        System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                        regmeses.add(trama);
                                    } else {
                                        nmesRegSin++;
                                    }
                                    nmesReg--;
                                    if (nmesReg >= 0) {
                                        byte tramaelgama[] = tramaspstn.getLocal7();
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        //resta de dia
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(nmesReg), 16) & 0xFF);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        String mensaje = "=> Solicitud de registro acumulado mensual del mes " + nmesReg;
                                        ////System.out.println("envia Solicitud registro acumulado mensual del mes " + nmesReg);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud registros mensuales", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else {
                                        lregistromes = false;
                                        if (lperfil) {
                                            adp += 1;
                                            adp = adp % 256;
                                            ndia = ndias - 1;
                                            ncanal = 1;
                                            byte tramaelgama[] = tramaspstn.getLocal2();
                                            String mensaje = "";
                                            inicio3 = false;
                                            lperfilCarga = true;
                                            parte = 0;
                                            loadProfile1 = new Vector<String[]>();
                                            loadProfile2 = new Vector<String[]>();
                                            loadProfile3 = new Vector<String[]>();
                                            loadProfile4 = new Vector<String[]>();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                            ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            cerrarPuerto();
                                            escribir("Estado lectura leido");

                                            AlmacenarRegistros();
                                            cerrarLog("Leido", true);
                                            med.MedLeido = true;
                                            leer = false;
                                        }
                                    }
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    }
                }
            } else if (inicio3) {
                if (!complemento) {
                    if (vectorhex[0].equals("2F")) {
                        if (vectorhex.length > 2) {//tiene cabecera
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        inicio3 = false;
                                        if (lregistros) {
                                            adp += 1;
                                            adp = adp % 256;
                                            ndiaReg = med.getNdiasreg() - 1;
                                            ndiasRegSin = 0;
                                            byte tramaelgama[] = tramaspstn.getLocal6();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                            lregistrodias = true;
                                            regdias = new Vector<String[]>();
                                            String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                            ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud de registros acumulados diarios ", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else if (lperfil) {
                                            adp += 1;
                                            adp = adp % 256;
                                            ndia = ndias - 1;
                                            ncanal = 1;
                                            byte tramaelgama[] = tramaspstn.getLocal2();
                                            String mensaje = "";
                                            lperfilCarga = true;
                                            parte = 0;
                                            loadProfile1 = new Vector<String[]>();
                                            loadProfile2 = new Vector<String[]>();
                                            loadProfile3 = new Vector<String[]>();
                                            loadProfile4 = new Vector<String[]>();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                            ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            cerrarPuerto();
                                            escribir("Estado lectura leido");

                                            AlmacenarRegistros();
                                            cerrarLog("Leido", true);
                                            med.MedLeido = true;
                                            leer = false;
                                        }
                                    } else {//bad crc
                                        ////System.out.println("BCRC");
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
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

                    } else {//sin 2F
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    actualUltimatrama = 0;
                                    inicio3 = false;
                                    if (lregistros) {
                                        adp += 1;
                                        adp = adp % 256;
                                        ndiaReg = med.getNdiasreg() - 1;
                                        ndiasRegSin = 0;
                                        byte tramaelgama[] = tramaspstn.getLocal6();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                        lregistrodias = true;
                                        regdias = new Vector<String[]>();
                                        String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                        ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud de registros acumulados diarios ", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else if (lperfil) {
                                        adp += 1;
                                        adp = adp % 256;
                                        ndia = ndias - 1;
                                        ncanal = 1;
                                        byte tramaelgama[] = tramaspstn.getLocal2();
                                        String mensaje = "";
                                        lperfilCarga = true;
                                        parte = 0;
                                        loadProfile1 = new Vector<String[]>();
                                        loadProfile2 = new Vector<String[]>();
                                        loadProfile3 = new Vector<String[]>();
                                        loadProfile4 = new Vector<String[]>();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        String ca = obtenercanal(ncanal);
                                        tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                        tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                        ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else {
                                        cerrarPuerto();
                                        escribir("Estado lectura leido");

                                        AlmacenarRegistros();
                                        cerrarLog("Leido", true);
                                        med.MedLeido = true;
                                        leer = false;
                                    }
                                } else {//bad crc
                                    ////System.out.println("BCRC");
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "Solicitud de congiguracion");
                            //enviamos ultimatrama
                        }
                    }
                } else {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("2F")) {
                        if (vectorhextemp.length > 2) {//tiene cabecera
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        actualUltimatrama = 0;
                                        inicio3 = false;
                                        if (lregistros) {
                                            adp += 1;
                                            adp = adp % 256;
                                            ndiaReg = med.getNdiasreg() - 1;
                                            ndiasRegSin = 0;
                                            byte tramaelgama[] = tramaspstn.getLocal6();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                            lregistrodias = true;
                                            regdias = new Vector<String[]>();
                                            String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                            ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Solicitud de registros acumulados diarios ", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else if (lperfil) {
                                            adp += 1;
                                            adp = adp % 256;
                                            ndia = ndias - 1;
                                            ncanal = 1;
                                            byte tramaelgama[] = tramaspstn.getLocal2();
                                            String mensaje = "";
                                            lperfilCarga = true;
                                            parte = 0;
                                            loadProfile1 = new Vector<String[]>();
                                            loadProfile2 = new Vector<String[]>();
                                            loadProfile3 = new Vector<String[]>();
                                            loadProfile4 = new Vector<String[]>();
                                            tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                            tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                            tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                            tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                            tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                            tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                            //adp
                                            tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                            ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                            try {
                                                if (aviso) {
                                                    // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            enviaTrama2(tramaelgama, mensaje);
                                        } else {
                                            cerrarPuerto();
                                            escribir("Estado lectura leido");

                                            AlmacenarRegistros();
                                            cerrarLog("Leido", true);
                                            med.MedLeido = true;
                                            leer = false;
                                        }
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {
                                //trama incompleta
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {//no cabcera
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }

                    } else {//sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    actualUltimatrama = 0;
                                    inicio3 = false;
                                    if (lregistros) {
                                        adp += 1;
                                        adp = adp % 256;
                                        ndiaReg = med.getNdiasreg() - 1;
                                        ndiasRegSin = 0;
                                        byte tramaelgama[] = tramaspstn.getLocal6();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        tramaelgama[11] = (byte) (Integer.parseInt(Integer.toHexString(ndiaReg), 16) & 0xFF);
                                        lregistrodias = true;
                                        regdias = new Vector<String[]>();
                                        String mensaje = "=> Solicitud de registro acumulado del dia " + ndiaReg;
                                        ////System.out.println("envia Solicitud registro acumulado del dia " + ndiaReg);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Solicitud de registros acumulados diarios ", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else if (lperfil) {
                                        adp += 1;
                                        adp = adp % 256;
                                        ndia = ndias - 1;
                                        ncanal = 1;
                                        byte tramaelgama[] = tramaspstn.getLocal2();
                                        String mensaje = "";
                                        lperfilCarga = true;
                                        parte = 0;
                                        loadProfile1 = new Vector<String[]>();
                                        loadProfile2 = new Vector<String[]>();
                                        loadProfile3 = new Vector<String[]>();
                                        loadProfile4 = new Vector<String[]>();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        //adp
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        String ca = obtenercanal(ncanal);
                                        tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                        tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        mensaje = "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia;
                                        ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                        try {
                                            if (aviso) {
                                                // lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        enviaTrama2(tramaelgama, mensaje);
                                    } else {
                                        perfilincompleto = false;
                                        cerrarPuerto();
                                        escribir("Estado lectura leido");
                                        AlmacenarRegistros();
                                        cerrarLog("Leido", true);
                                        med.MedLeido = true;
                                        leer = false;
                                    }
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                            }
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=>Solicitud de congiguracion");
                        }
                    }
                }
            } else if (lperfilCarga) {
                if (!complemento) {
                    if (vectorhex[0].equals("2F")) {
                        if (vectorhex.length > 7) {//tiene cabecera
                            if (vectorhex.length - 1 >= Integer.parseInt(vectorhex[1], 16)) {
                                if (adp == Integer.parseInt(vectorhex[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhex, Integer.parseInt(vectorhex[1], 16) + 1)) {
                                        perfilincompleto = true;
                                        actualUltimatrama = 0;
                                        byte tramaelgama[] = tramaspstn.getLocal2();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        adp += 1;
                                        adp = adp % 256;
                                        String[] trama = new String[(Integer.parseInt(vectorhex[1], 16) + 1)];
                                        System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[1], 16) + 1);
                                        boolean almacena = false;
                                        if (intervalo != 15 && parte == 0) {
                                            almacena = true;
                                        } else {
                                            if (intervalo == 15) {
                                                almacena = true;
                                            }
                                        }
                                        if (almacena) {
                                            if (ncanal == 1) {
                                                loadProfile1.add(trama);
                                            } else if (ncanal == 2) {
                                                loadProfile2.add(trama);
                                            } else if (ncanal == 3) {
                                                loadProfile3.add(trama);
                                            } else if (ncanal == 4) {
                                                loadProfile4.add(trama);
                                            }
                                        }

                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        if (parte == 0) {//es primera parte
                                            ////System.out.println("envia parte 2 del canal " + ncanal + " del dia " + ndia);
                                            //mascara al dia
                                            parte = 1;
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) (((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F) | 0x80);
                                            //y enviar
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            enviaTrama2(tramaelgama, "=> Solicitud de Perfil de carga parte 2 del canal " + ncanal + " del dia  " + ndia);
                                        } else {//es segunda parte
                                            //validamos canal
                                            ncanal += 1;
                                            if (ncanal < 5) {
                                                ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                parte = 0;
                                                //envio canal
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                //y enviar
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                enviaTrama2(tramaelgama, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                                            } else {//cambio dia
                                                ndia -= 1;
                                                if (ndia >= 0) {
                                                    ncanal = 1;
                                                    parte = 0;
                                                    //envio
                                                    ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                    String ca = obtenercanal(ncanal);
                                                    tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                    tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                    //y enviar
                                                    int crcCalculado = CRCElgama(tramaelgama);
                                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                    while (calculo.length() < 4) {
                                                        calculo = "0" + calculo;
                                                    }
                                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                    ultimatramaEnviada = tramaelgama;
                                                    enviaTrama2(tramaelgama, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                                                } else {
                                                    //cierra puerto
                                                    perfilincompleto = false;
                                                    cerrarPuerto();
                                                    escribir("Estado lectura leido");

                                                    AlmacenarRegistros();
                                                    cerrarLog("Leido", true);
                                                    med.MedLeido = true;
                                                    leer = false;
                                                }
                                            }
                                        }
                                    } else {//bad crc
                                        ////System.out.println("CRC");
                                        enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
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
                    } else {//sin 2F
                        if (vectorhex.length >= Integer.parseInt(vectorhex[0], 16) && !vectorhex[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhex[7], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhex, Integer.parseInt(vectorhex[0], 16))) {
                                    perfilincompleto = true;
                                    actualUltimatrama = 0;
                                    byte tramaelgama[] = tramaspstn.getLocal2();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    adp += 1;
                                    adp = adp % 256;
                                    String[] trama = new String[(Integer.parseInt(vectorhex[0], 16))];
                                    System.arraycopy(vectorhex, 0, trama, 0, Integer.parseInt(vectorhex[0], 16));
                                    boolean almacena = false;
                                    if (intervalo != 15 && parte == 0) {
                                        almacena = true;
                                    } else {
                                        if (intervalo == 15) {
                                            almacena = true;
                                        }
                                    }
                                    if (almacena) {
                                        if (ncanal == 1) {
                                            loadProfile1.add(trama);
                                        } else if (ncanal == 2) {
                                            loadProfile2.add(trama);
                                        } else if (ncanal == 3) {
                                            loadProfile3.add(trama);
                                        } else if (ncanal == 4) {
                                            loadProfile4.add(trama);
                                        }
                                    }

                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    if (parte == 0) {//es primera parte
                                        ////System.out.println("envia parte 2 del canal " + ncanal + " del dia " + ndia);
                                        //mascara al dia
                                        parte = 1;
                                        String ca = obtenercanal(ncanal);
                                        tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                        tramaelgama[11] = (byte) (((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F) | 0x80);
                                        //y enviar
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        enviaTrama2(tramaelgama, "=> Solicitud de Perfil de carga parte 2 del canal " + ncanal + " del dia  " + ndia);
                                    } else {//es segunda parte
                                        //validamos canal
                                        ncanal += 1;
                                        if (ncanal < 5) {
                                            ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                            parte = 0;
                                            //envio canal
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            //y enviar
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            enviaTrama2(tramaelgama, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                                        } else {//cambio dia
                                            ndia -= 1;
                                            if (ndia >= 0) {
                                                ncanal = 1;
                                                parte = 0;
                                                //envio
                                                ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                //y enviar
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                enviaTrama2(tramaelgama, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                                            } else {
                                                //cierra puerto
                                                perfilincompleto = false;
                                                cerrarPuerto();
                                                escribir("Estado lectura leido");

                                                AlmacenarRegistros();
                                                cerrarLog("Leido", true);
                                                med.MedLeido = true;
                                                leer = false;
                                            }
                                        }
                                    }
                                } else {//bad crc
                                    ////System.out.println("CRC");
                                    enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                            }
                        } else {
                            //trama incompleta
                            complemento = true;
                            tramaIncompleta = cadenahex;
                            rutinaCorrecta = false;
                            enviaTramaUltima(ultimatramaEnviada, "=> Solicitud de Perfil de carga parte 1 del canal " + ncanal + " del dia  " + ndia);
                            //enviamos ultimatrama
                        }
                    }
                } else {
                    complemento = false;
                    tramaIncompleta = tramaIncompleta + " " + cadenahex;
                    String vectorhextemp[] = tramaIncompleta.split(" ");
                    if (vectorhextemp[0].equals("2F")) {
                        if (vectorhextemp.length > 7) {//tiene cabecera
                            if (vectorhextemp.length - 1 >= Integer.parseInt(vectorhextemp[1], 16)) {
                                if (adp == Integer.parseInt(vectorhextemp[8], 16)) {//es el adp correcto
                                    if (validaCRCElgama(vectorhextemp, Integer.parseInt(vectorhextemp[1], 16) + 1)) {
                                        perfilincompleto = true;
                                        actualUltimatrama = 0;
                                        String[] trama = new String[(Integer.parseInt(vectorhextemp[1], 16) + 1)];
                                        System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[1], 16) + 1);
                                        boolean almacena = false;
                                        if (intervalo != 15 && parte == 0) {
                                            almacena = true;
                                        } else {
                                            if (intervalo == 15) {
                                                almacena = true;
                                            }
                                        }
                                        if (almacena) {
                                            if (ncanal == 1) {
                                                loadProfile1.add(trama);
                                            } else if (ncanal == 2) {
                                                loadProfile2.add(trama);
                                            } else if (ncanal == 3) {
                                                loadProfile3.add(trama);
                                            } else if (ncanal == 4) {
                                                loadProfile4.add(trama);
                                            }
                                        }
                                        byte tramaelgama[] = tramaspstn.getLocal2();
                                        tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                        tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                        tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                        tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                        tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                        tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                        adp += 1;
                                        adp = adp % 256;
                                        tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                        if (parte == 0) {//es primera parte
                                            //mascara al dia
                                            parte = 1;
                                            ////System.out.println("envia parte 2 del canal " + ncanal + " del dia " + ndia);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) (((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F) | 0x80);
                                            //y enviar
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            enviaTrama2(tramaelgama, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                        } else {//es segunda parte
                                            //validamos canal
                                            ncanal += 1;
                                            if (ncanal < 5) {
                                                parte = 0;
                                                //envio canal
                                                ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                //y enviar
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                enviaTrama2(tramaelgama, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                            } else {//cambio dia
                                                ndia -= 1;
                                                if (ndia >= 0) {
                                                    ncanal = 1;
                                                    parte = 0;
                                                    //envio
                                                    ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                    String ca = obtenercanal(ncanal);
                                                    tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                    tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                    //y enviar
                                                    int crcCalculado = CRCElgama(tramaelgama);
                                                    String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                    while (calculo.length() < 4) {
                                                        calculo = "0" + calculo;
                                                    }
                                                    tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                    tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                    ultimatramaEnviada = tramaelgama;
                                                    enviaTrama2(tramaelgama, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                                } else {
                                                    //cierra puerto
                                                    cerrarPuerto();
                                                    perfilincompleto = false;
                                                    escribir("Estado lectura leido");

                                                    AlmacenarRegistros();
                                                    cerrarLog("Leido", true);
                                                    med.MedLeido = true;
                                                    leer = false;
                                                }
                                            }
                                        }
                                    } else {//bad crc
                                        ////System.out.println("Badcrc");
                                        if (parte == 0) {
                                            enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                        } else {
                                            enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                        }
                                    }
                                } else {//no es el adp correcto
                                    escribir("ADP Incorrecto");
                                    if (parte == 0) {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                    }
                                }
                            } else {
                                //trama incompleta                                
                                tramaIncompleta = "";
                                rutinaCorrecta = false;
                                //enviamos ultimatrama
                                if (parte == 0) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                }
                            }
                        } else {
                            //no cabecera
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            if (parte == 0) {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                            }
                        }

                    } else {//sin 2F
                        if (vectorhextemp.length >= Integer.parseInt(vectorhextemp[0], 16) && !vectorhextemp[0].equals("00")) {
                            if (adp == Integer.parseInt(vectorhextemp[0], 16)) {//es el adp correcto
                                if (validaCRCElgama2(vectorhextemp, Integer.parseInt(vectorhextemp[0], 16))) {
                                    perfilincompleto = true;
                                    actualUltimatrama = 0;
                                    String[] trama = new String[(Integer.parseInt(vectorhextemp[0], 16))];
                                    System.arraycopy(vectorhextemp, 0, trama, 0, Integer.parseInt(vectorhextemp[0], 16));
                                    boolean almacena = false;
                                    if (intervalo != 15 && parte == 0) {
                                        almacena = true;
                                    } else {
                                        if (intervalo == 15) {
                                            almacena = true;
                                        }
                                    }
                                    if (almacena) {
                                        if (ncanal == 1) {
                                            loadProfile1.add(trama);
                                        } else if (ncanal == 2) {
                                            loadProfile2.add(trama);
                                        } else if (ncanal == 3) {
                                            loadProfile3.add(trama);
                                        } else if (ncanal == 4) {
                                            loadProfile4.add(trama);
                                        }
                                    }
                                    byte tramaelgama[] = tramaspstn.getLocal2();
                                    tramaelgama[2] = (byte) (Integer.parseInt(vecSerieMedidor[0], 16) & 0xFF);
                                    tramaelgama[3] = (byte) (Integer.parseInt(vecSerieMedidor[1], 16) & 0xFF);
                                    tramaelgama[4] = (byte) (Integer.parseInt(vecSerieMedidor[2], 16) & 0xFF);
                                    tramaelgama[5] = (byte) (Integer.parseInt(vecSerieMedidor[3], 16) & 0xFF);
                                    tramaelgama[6] = (byte) (Integer.parseInt(vecSerieMedidor[4], 16) & 0xFF);
                                    tramaelgama[7] = (byte) (Integer.parseInt(vecSerieMedidor[5], 16) & 0xFF);
                                    adp += 1;
                                    adp = adp % 256;
                                    tramaelgama[8] = (byte) (Integer.parseInt(Integer.toHexString(adp), 16) & 0xFF);
                                    if (parte == 0) {//es primera parte
                                        //mascara al dia
                                        parte = 1;
                                        ////System.out.println("envia parte 2 del canal " + ncanal + " del dia " + ndia);
                                        String ca = obtenercanal(ncanal);
                                        tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                        tramaelgama[11] = (byte) (((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F) | 0x80);
                                        //y enviar
                                        int crcCalculado = CRCElgama(tramaelgama);
                                        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                        while (calculo.length() < 4) {
                                            calculo = "0" + calculo;
                                        }
                                        tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                        tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                        ultimatramaEnviada = tramaelgama;
                                        enviaTrama2(tramaelgama, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                    } else {//es segunda parte
                                        //validamos canal
                                        ncanal += 1;
                                        if (ncanal < 5) {
                                            parte = 0;
                                            //envio canal
                                            ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                            String ca = obtenercanal(ncanal);
                                            tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                            tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                            //y enviar
                                            int crcCalculado = CRCElgama(tramaelgama);
                                            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                            while (calculo.length() < 4) {
                                                calculo = "0" + calculo;
                                            }
                                            tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                            tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                            ultimatramaEnviada = tramaelgama;
                                            enviaTrama2(tramaelgama, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                        } else {//cambio dia
                                            ndia -= 1;
                                            if (ndia >= 0) {
                                                ncanal = 1;
                                                parte = 0;
                                                //envio
                                                ////System.out.println("envia parte 1 del canal " + ncanal + " del dia " + ndia);
                                                String ca = obtenercanal(ncanal);
                                                tramaelgama[10] = (byte) (Integer.parseInt(ca, 16) & 0xFF);
                                                tramaelgama[11] = (byte) ((Integer.parseInt(Integer.toHexString(ndia), 16) & 0xFF) & 0x7F);
                                                //y enviar
                                                int crcCalculado = CRCElgama(tramaelgama);
                                                String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();
                                                while (calculo.length() < 4) {
                                                    calculo = "0" + calculo;
                                                }
                                                tramaelgama[tramaelgama.length - 2] = (byte) (Integer.parseInt(calculo.substring(0, 2), 16) & 0xFF);
                                                tramaelgama[tramaelgama.length - 1] = (byte) (Integer.parseInt(calculo.substring(2, 4), 16) & 0xFF);
                                                ultimatramaEnviada = tramaelgama;
                                                enviaTrama2(tramaelgama, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                            } else {
                                                //cierra puerto
                                                perfilincompleto = false;
                                                cerrarPuerto();
                                                escribir("Estado lectura leido");

                                                AlmacenarRegistros();
                                                cerrarLog("Leido", true);
                                                med.MedLeido = true;
                                                leer = false;
                                            }
                                        }
                                    }
                                } else {//bad crc
                                    ////System.out.println("Badcrc");
                                    if (parte == 0) {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                    } else {
                                        enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                    }
                                }
                            } else {//no es el adp correcto
                                escribir("ADP Incorrecto");
                                if (parte == 0) {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                                } else {
                                    enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                                }
                            }
                        } else {
                            //trama incompleta
                            tramaIncompleta = "";
                            rutinaCorrecta = false;
                            //enviamos ultimatrama
                            if (parte == 0) {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 1 del canal " + ncanal + " del dia " + ndia);
                            } else {
                                enviaTramaUltima(ultimatramaEnviada, "Solicitud perfil de carga parte 2 del canal " + ncanal + " del dia " + ndia);
                            }
                        }
                    }
                }
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
        reintentosUtilizados++;
        portconect = false;
        actualUltimatrama = 0;
        try {
            //parametrizacion del puerto, lo abre a 1200 baud
            try {
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            CommPort puerto = portId.open("AbreSerial" + med.getnSerie(), velocidadPuerto);
            serialPort = (SerialPort) puerto;
            portconect = true;
            if (aviso) {
                // lm.jtablemedidores.setValueAt("Abre puerto..", indx, 3);
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
        } catch (Exception e) {
            escribir(e.getMessage());
            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion Error al abrir comunicacion con el modem");
            if (portconect) {
                cerrarPuerto();
            }
            //e.printStackTrace();
        }

    }

    public void cerrarPuerto() {
        try {
            if (output != null) {
                //////System.out.println("Cierra salida");
                output.flush();
                output.close();
            }
            if (input != null) {
                //////System.out.println("Cierra entrada");
                input.close();
            }

        } catch (Exception p) {
        } finally {
            try {
                if (serialPort != null) {
                    escribir("Cierra puerto");
                    ////System.out.println("Cierra puerto");
                    serialPort.close();
                }
            } catch (Exception e) {
            }
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
                    // lm.jtablemedidores.setValueAt("Cerrando puerto..", indx, 3);
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
        perfilincompleto = false;
        lperfilCarga = false;
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
            mensaje = "Numero Telefonico " + med.getNumtelefonico();
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
                        //////System.out.println("No respondio No carrier iniciamos la comunicacion" + tramainicial);
                        try {
                            if (aviso) {
                                // lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
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
                    cerrarLog("Medidor no configurado", false);
                    escribir("Estado Lectura No Leido");
                    leer = false;
                }
            } else {
                //si no esta configurado cerramos el puerto
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion Medidor sin configuracion");
                cerrarPuerto();
                cerrarLog("Medidor no configurado", false);
                escribir("Estado Lectura No Leido");
                leer = false;
            }
        } else {
            cerrarLog("Medidor no configurado", false);
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
                    ////System.out.println("Envia trama =>");
                    escribir("=> " + tramaspstn.encode(trama, trama.length));
                    ////System.out.println(tramaspstn.encode(trama, trama.length));
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
                    ////System.out.println(des);
                    ////System.out.println("Envia trama =>");
                    escribir("=> " + tramaspstn.encode(trama, trama.length));
                    ////System.out.println(tramaspstn.encode(trama, trama.length));
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
                                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion proceso abortado");
                                    cerrarLog("Abortado", false);
                                    leer = false;
                                }
                            } else {
                                escribir("Numero de reintentos agotado..");
                                //terminahilos();
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion numero de reintentos agotado");
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
                            ////System.out.println(des);
                            ////System.out.println("Envia trama =>");
                            escribir("=> " + tramaspstn.encode(trama, trama.length));
                            ////System.out.println(tramaspstn.encode(trama, trama.length));
                            enviaTrama(trama);
                            intentosRetransmision++;
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

                        cerrarPuerto();
                        if (lperfilCarga & perfilincompleto) {
                            AlmacenarRegistrosIncompletos();
                        }
                        if (numeroReintentos >= actualReintento) {
                            if (!objabortar.labortar) {
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion proceso abortado");
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //terminahilos();
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion Numero de reintentos agotado");
                            escribir("Numero de reintentos agotado");
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
        byte[] bytes = new byte[tramaelgama.length - 3];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = tramaelgama[i + 1];
        }
        int crc = 0x0000;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        int crcAux;
        //////System.out.println("CRC2 " + Integer.toHexString(crc2));
        crcAux = (((crc) << 8) | ((crc) >>> 8)) & 0x0000ffff;
        return crcAux;
    }

    private int CRCElgama2(byte[] tramaelgama) { //esto se ejecuta cuando no hay 2F
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
        //////System.out.println("CRC2 " + Integer.toHexString(crc2));
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

    private boolean validaCRCElgama2(String[] vectorhex, int len) {//metodo para cuando llega sin 2F
        boolean ok = false;
        try {
            byte[] bytes = new byte[len];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (Integer.parseInt(vectorhex[i], 16) & 0xFF);
            }
            int crccalculado = CRCElgama2(bytes);
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

    private void enviaTramaUltima(byte[] bytes, String descripcion) {
        actualUltimatrama++;
        if (actualUltimatrama > reintentosUltimatrama) {
            cerrarPuerto();
            if (lperfilCarga & perfilincompleto) {
                AlmacenarRegistrosIncompletos();
            }
            if (numeroReintentos >= actualReintento) {
                if (!objabortar.labortar) {
                    abrePuerto();
                    iniciacomunicacion();
                } else {
                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion proceso abortado");
                    escribir("Proceso Abortado");
                    cerrarLog("Abortado", false);
                    leer = false;
                }
            } else {
                //escribir("Numero de reintentos agotado");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion numero de reintentos agotado");
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
                                        ////System.out.println("Sale enviarTramaultima");
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
                                ////System.out.println("Llego algo Sale enviatramaUltima");
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
                            ////System.out.println(des);
                            ////System.out.println("Envia trama =>");
                            escribir("=> " + tramaspstn.encode(trama, trama.length));
                            ////System.out.println(tramaspstn.encode(trama, trama.length));
                            enviaTrama(trama);

                        } else {
                            ////System.out.println("Sale enviatrama2");
                            t = false;
                        }
                        if (intentosRetransmision > 4) {
                            escribir("Numero de reenvios agotado");
                            //reiniciaComunicacion();
                            ////System.out.println("Numero de reenvios agotado");
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {
                            intentosRetransmision++;
                            boolean salir = true;
                            int intentosalir = 0;
                            while (salir) {
                                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                                    ////System.out.println("Sale enviar");
                                    salir = false;
                                    if (!enviando) {
                                        t = false;
                                    }

                                } else {
                                    intentosalir++;
                                    ////System.out.println("Esperando... enviatrama2");
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
                                abrePuerto();
                                iniciacomunicacion();
                            } else {
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion proceso abortado");
                                escribir("Proceso Abortado");
                                cerrarLog("Abortado", false);
                                leer = false;
                            }
                        } else {
                            //escribir("Numero de reintentos agotado");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNELgama", "Desconexion numero de reintentos agotadoh");
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

    private String obtenercanal(int ncanal) {
        String ca = "";
        if (ncanal == 1) {
            ca = "02";
        } else if (ncanal == 2) {
            ca = "04";
        } else if (ncanal == 3) {
            ca = "03";
        } else if (ncanal == 4) {
            ca = "05";
        }
        return ca;
    }

    private void reiniciaComunicacion() {
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
            //terminahilos();
            escribir("Numero de reintentos agotado");
            
            escribir("Estado Lectura No Leido");
            leer = false;
        }
    }

    private void AlmacenarRegistros() {//metodo que solo almacena el numero de canales
        escribir("Procesando Resgitros..");
        if (lperfil) {
            //almacena perfil
            try {
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("Almacenando Perfil de Carga", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            escribir("Almacenando Perfil de Carga");
            almacenaPerfilCargaNCanales(loadProfile1, loadProfile2, loadProfile3, loadProfile4, numcanales);
        }
        if (lregistros) {
            //almacena registros diarios
            escribir("Almacenando Registro diarios");
            try {
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("Almacenando registros diarios", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            almacenaRegistrosDiarios(regdias);
            //almacena registros mensuales
            escribir("Almacenando Registro mensuales");
            try {
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("Almacenando registros mensuales", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            almacenaRegistrosMes(regmeses);
        }
        if (leventos) {
            //guarda el registro de eventos
            escribir("Almacenando Registro de eventos");
            try {
                if (aviso) {
                    // lm.jtablemedidores.setValueAt("Almacenando registros eventos", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }
            almacenaRegistroEventosPower(powerfails);

        }
    }

    private void AlmacenarRegistrosIncompletos() {
        escribir("Procesando Resgitros..");
        if (lperfil) {
            //almacena perfil
            escribir("Almacenando Perfil de Carga Incompleto");
            almacenaPerfilCargaNCanales(loadProfile1, loadProfile2, loadProfile3, loadProfile4, numcanales);
        }
        if (lregistros) {
            //almacena registros diarios
            escribir("Almacenando Registro diarios");
            almacenaRegistrosDiarios(regdias);
            //almacena registros mensuales
            escribir("Almacenando Registro mensuales");
            almacenaRegistrosMes(regmeses);
        }
        if (leventos) {
            //guarda el registro de eventos
            escribir("Almacenando Registro de eventos");
            almacenaRegistroEventosPower(powerfails);

        }
    }

    private void almacenaPerfilCargaNCanales(Vector<String[]> loadProfile1, Vector<String[]> loadProfile2, Vector<String[]> loadProfile3, Vector<String[]> loadProfile4, int numcanales) {
        Vector<Electura> vlec = new Vector<>();
        String fechaActualizada = "";
        int valor = 720;
        if (intervalo != 15) {
            valor = 1440;
        }
        int duracionDias = valor / intervalo;
        try {
            String datapf1 = "";
            String datapf2 = "";
            String datapf3 = "";
            String datapf4 = "";
            int desglosa = ((2 * duracionDias) + 1) * 2;
            //perfil de carga de canal1
            for (int pf = 0; pf < loadProfile1.size(); pf++) {
                if (loadProfile1.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < loadProfile1.get(pf).length - 2; j++) {
                        datapf1 += "" + loadProfile1.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < loadProfile1.get(pf).length - 2; j++) {
                        datapf1 += "" + loadProfile1.get(pf)[j];
                    }
                }
            }
            Vector<String> data1 = new Vector<String>();
            while (datapf1.length() > 0) {
                if (datapf1.length() >= desglosa) {
                    data1.add(datapf1.substring(0, desglosa));
                    datapf1 = datapf1.substring(desglosa, datapf1.length());
                } else {
                    data1.add(datapf1.substring(0, datapf1.length()));
                    datapf1 = "";
                }
            }
            //perfil de carga de canal2
            for (int pf = 0; pf < loadProfile2.size(); pf++) {
                if (loadProfile2.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < loadProfile2.get(pf).length - 2; j++) {
                        datapf2 += "" + loadProfile2.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < loadProfile2.get(pf).length - 2; j++) {
                        datapf2 += "" + loadProfile2.get(pf)[j];
                    }
                }
            }
            Vector<String> data2 = new Vector<String>();
            while (datapf2.length() > 0) {
                if (datapf2.length() >= desglosa) {
                    data2.add(datapf2.substring(0, desglosa));
                    datapf2 = datapf2.substring(desglosa, datapf2.length());
                } else {
                    data2.add(datapf2.substring(0, datapf2.length()));
                    datapf2 = "";
                }
            }
            //perfil de carga de canal3
            for (int pf = 0; pf < loadProfile3.size(); pf++) {
                if (loadProfile3.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < loadProfile3.get(pf).length - 2; j++) {
                        datapf3 += "" + loadProfile3.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < loadProfile3.get(pf).length - 2; j++) {
                        datapf3 += "" + loadProfile3.get(pf)[j];
                    }
                }
            }
            Vector<String> data3 = new Vector<String>();
            while (datapf3.length() > 0) {
                if (datapf3.length() >= desglosa) {
                    data3.add(datapf3.substring(0, desglosa));
                    datapf3 = datapf3.substring(desglosa, datapf3.length());
                } else {
                    data3.add(datapf3.substring(0, datapf3.length()));
                    datapf3 = "";
                }
            }
            //perfil de carga de canal4
            for (int pf = 0; pf < loadProfile4.size(); pf++) {
                if (loadProfile4.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < loadProfile4.get(pf).length - 2; j++) {
                        datapf4 += "" + loadProfile4.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < loadProfile4.get(pf).length - 2; j++) {
                        datapf4 += "" + loadProfile4.get(pf)[j];
                    }
                }
            }
            Vector<String> data4 = new Vector<String>();
            while (datapf4.length() > 0) {
                if (datapf4.length() >= desglosa) {
                    data4.add(datapf4.substring(0, desglosa));
                    datapf4 = datapf4.substring(desglosa, datapf4.length());
                } else {
                    data4.add(datapf4.substring(0, datapf4.length()));
                    datapf4 = "";
                }
            }
            /*
            fechaActualizada = cp.lecturaELGAMANCanales(data1, seriemedidor, med.getMarcaMedidor().getCodigo(), 1, med.getFecha(), fechaActual, fechaActualizada, intervalo, ndias, version, ktru, ktri, exponent, numcanales, vlec, file);
            if (numcanales >= 2) {
                fechaActualizada = cp.lecturaELGAMANCanales(data2, seriemedidor, med.getMarcaMedidor().getCodigo(), 2, med.getFecha(), fechaActual, fechaActualizada, intervalo, ndias, version, ktru, ktri, exponent, numcanales, vlec, file);
            }
            if (numcanales >= 3) {
                fechaActualizada = cp.lecturaELGAMANCanales(data3, seriemedidor, med.getMarcaMedidor().getCodigo(), 3, med.getFecha(), fechaActual, fechaActualizada, intervalo, ndias, version, ktru, ktri, exponent, numcanales, vlec, file);
            }
            if (numcanales >= 4) {
                fechaActualizada = cp.lecturaELGAMANCanales(data4, seriemedidor, med.getMarcaMedidor().getCodigo(), 4, med.getFecha(), fechaActual, fechaActualizada, intervalo, ndias, version, ktru, ktri, exponent, numcanales, vlec, file);
            }
            */
            ////System.out.println("Inicio almacenamiento " + seriemedidor + " " + new Date());            
            cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
            if (fechaActualizada.length() > 0) {
                SimpleDateFormat fechaAactualizarSDF = new SimpleDateFormat("yyMMddHHmmss");
                String fechaAactualizar = fechaActualizada;
                Date fechaAactualizarDate = new SimpleDateFormat("yy/MM/dd HH:mm:ss").parse(fechaAactualizar);
                fechaAactualizar = fechaAactualizarSDF.format(new Date(fechaAactualizarDate.getTime()));
                cp.actualizaFechaLectura(seriemedidor, fechaAactualizar);
            }
            ////System.out.println("Fin almacenamiento    " + seriemedidor + " " + new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void almacenaRegistroEventosPower(Vector<String[]> powerfails) {
        try {
            Vector<String> data = new Vector<String>();
            String dataRegeventos = "";
            for (int pf = 0; pf < powerfails.size(); pf++) {
                if (powerfails.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < powerfails.get(pf).length - 2; j++) {
                        dataRegeventos += "" + powerfails.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < powerfails.get(pf).length - 2; j++) {
                        dataRegeventos += "" + powerfails.get(pf)[j];
                    }
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

    private void almacenaRegistrosDiarios(Vector<String[]> regdias) {
        try {
            Vector<String> data = new Vector<String>();
            String dataRegDiarios = "";
            for (int pf = 0; pf < regdias.size(); pf++) {
                if (regdias.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < regdias.get(pf).length - 2; j++) {
                        dataRegDiarios += "" + regdias.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < regdias.get(pf).length - 2; j++) {
                        dataRegDiarios += "" + regdias.get(pf)[j];
                    }
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
                if (regmeses.get(pf)[0].equals("2F")) {
                    for (int j = 10; j < regmeses.get(pf).length - 2; j++) {
                        dataRegMes += "" + regmeses.get(pf)[j];
                    }
                } else {
                    for (int j = 9; j < regmeses.get(pf).length - 2; j++) {
                        dataRegMes += "" + regmeses.get(pf)[j];
                    }
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
