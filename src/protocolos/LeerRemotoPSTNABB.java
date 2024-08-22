/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaPSTNABB;
import Datos.encrypt_password;
import Entidades.Abortar;
import Entidades.EConfModem;
import Entidades.ELogCall;
import Entidades.EMedidor;
//import Rutinas.AppletLecturaMedidores;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
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
public class LeerRemotoPSTNABB implements Runnable, SerialPortEventListener {

    int reintentosUtilizados;// reintentos utlizados
    Timestamp tiempoinicial; //inicio de comunicacion
    Timestamp tiempofinal;//fin de comunicacion
    String usuario = "admin";
    Date d = new Date();
    Abortar objabortar;
    long runvar = 0;
    boolean rutinaCorrecta = false;
    boolean inicio = false;
    boolean lhandshaking = false;
    boolean contraseña = false;
    String seriemedidor = "";
    int totalIntentosPC;
    int totalintentosactual;
    CountingInputStream input;
    BufferedOutputStream output;
    long tiempo = 500;
    String numeroPuerto = "COM4";
    int velocidadPuerto = 1200;
    String password;
    int numcanales;
    int ndias;
    //numero de reintentos de reconexion
    int numeroReintentos = 4;
    int nreintentos = 0;
    SerialPort serialPort;
    Enumeration portList;
    private CommPortIdentifier portId;
    boolean portFound = false;
    boolean portconect = false;
    TramasRemotaPSTNABB tramaspstn = new TramasRemotaPSTNABB();
    String cadenahex = "";
    long timeout;
    boolean lperfil;
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
    boolean lfirmware = false;
    boolean meteringFunction = false;
    boolean medPrimaria = false;
    boolean fecActual = false;
    String valorconstanteke = "0.0";
    String fechaActual = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    File file;
    RandomAccessFile fr;
    boolean existearchivo = false;
    String ultimoRegEvento = "";
    int intervalo = 0;
    int factorIntervalo = 0;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    ZoneId zid;
   

    public LeerRemotoPSTNABB(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, ControlProcesos cp, ZoneId zid) {

        this.med = med;
        this.confModem = confmodem;
        this.cp = cp;
        this.zid = zid;
        File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
        if (!f.exists()) {
            f.mkdirs();
        }
//            file = new File(cp.rutalogs + "" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");
        file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");


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
        lregistros = eventos;
        jinit();
    }

//    public LeerRemotoPSTNABB(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, AppletLecturaMedidores lm, int indx, ControlProcesos cp,Abortar objabortar,boolean aviso) {
    public LeerRemotoPSTNABB(EMedidor med, Vector<EConfModem> confmodem, boolean perfil, boolean eventos, int indx, ControlProcesos cp, Abortar objabortar, boolean aviso, ZoneId zid) {
        try {
            this.med = med;
            this.confModem = confmodem;
            this.cp = cp;
            this.zid = zid;
            File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
            if (!f.exists()) {
                f.mkdirs();
            }
//             file = new File(cp.rutalogs + "" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");
            file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/PSTN_" + med.getnSerie() + ".txt");
        } catch (Exception e) {
            escribir(e.getMessage());
        }

        try {
            if (file.exists()) {
                existearchivo = true;
            } else {
                //System.out.println("No existe el archivo");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        lperfil = perfil;
        lregistros = eventos;
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
            e.printStackTrace();
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


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    boolean ltimeout = false;
    boolean tamaño = false;
    boolean perfilcarga = false;
    int reintentosPerfilCarga;
    Vector<String[]> loadProfile;
    Vector<String[]> RegEventos;
    boolean status = false;
    boolean timeout2 = false;
    boolean lcanales = false;
    boolean status2 = false;
    boolean registroevento = false;
    long tiemporetransmision = 6000;
    boolean bloqueperfil;

    private void interpretaCadena(String cadenahex) {

        rutinaCorrecta = true;
        enviando = false;
        //conectando con el modem
        if (conexionModem) {
            if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52") && !desbloqueo) {
                //no carrier para los casos de que no se llegan los datos
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Error al conectar con el modem");
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
                ////System.out.println("Respondio No carrier iniciamos la comunicacion" + tramainicial);
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
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
                ////System.out.println("Respondio No carrier iniciamos la comunicacion" + tramainicial);
                try {
                    if (aviso) {
                        //lm.jtablemedidores.setValueAt("iniciamos comunicacion", indx, 3);
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
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion sin tono");

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
                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion portadora ocupada");
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


            } else if (cadenahex.contains("4F 4B")) {//ok
                escribir("<= OK " + cadenahex);
                nreintentos = 0;
                String trama = confModem.get(0).getpeticion(indiceconexion);

                if (trama.equals("")) {
                    byte[] vtrama = null;
                    if (med.getTipoconexion() == 1) {
                        vtrama = tramaspstn.StringToHexMarcado(med.getNumtelefonico().trim());
                    } else {
                        vtrama = tramaspstn.StringToHexMarcado(med.getDireccionip().trim() + "/" + med.getPuertoip().trim());
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
                conexionModem = false;
                inicio = true;
                lhandshaking = true;
                //enviando = true;
                //System.out.println("enviamos trama 1");
                tiemporetransmision = 6000;
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("Comunicando..", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
                ultimatramaEnviada = tramaspstn.getLocal0();
                enviaTrama2(tramaspstn.getLocal0(), "=> Solicitud de ID de medidor");

                //realizamos retransmision


            } else if (cadenahex.contains("4E 4F 20 41 4E 53 57 45 52")) { //NO answer
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion sin respuesta");
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
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion proceso abortado");
                        cerrarLog("Abortado", false);
                        leer = false;
                    }
                } else {

                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion numero de reintentos agotado");
                    cerrarLog("Desconexion Numero de reintentos agotado", false);
                    escribir("Estado de lectura no leido");
                    leer = false;
                }
            }
        } else {
            boolean crcOk = false;
            String[] vectorhex = cadenahex.split(" ");

            if (vectorhex.length >= 3) {
                //ya fue conectado con el modem por lo tanto continuamos enviando tramas para
                //obtener el perfil de carga
                if (cadenahex.contains("4E 4F 20 43 41 52 52 49 45 52")) {//Se cayo el modem o se desconecto
                    cerrarPuerto();
                    if (numeroReintentos >= actualReintento) {
                        if (!objabortar.labortar) {
                            abrePuerto();
                            iniciacomunicacion();
                        } else {
                            escribir("Proceso Abortado");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion proceso abortado");
                            cerrarLog("Abortado", false);
                            leer = false;
                        }
                    } else {
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Desconexion numero de reintentos agotado");
                        cerrarLog("Desconexion Numero de reintentos agotado", false);
                        leer = false;
                    }

                } else if (inicio && lhandshaking) {
                    escribir("<= Recibe ID medidor " + cadenahex);
                    if (aviso) {
                        try {
                            //lm.jtablemedidores.setValueAt("Negociando...", indx, 3);
                            //lm.mdc.fireTableDataChanged();
                        } catch (Exception e) {
                        }

                    }
                    //validamos el crc para continuar
                    if (validacionCRC(vectorhex)) {
                        reintentosbadCRC = 0;
                        //si es correcto el crc calculamos la data con el password que se entrega en la trama.
                        contraseña = true;
                        inicio = false;
                        //enviamos la cadena con password y nuevo crc
                        //ultimatramaEnviada = calcularPSWD(vectorhex);
                        enviaTrama2(calcularPSWD(vectorhex), "=> Envio Data Contraseña");
                    } else {
                        reintentosbadCRC++;
                        //System.out.println("CRC INVALIDO aceptacion contraseña");
                        if (reintentosbadCRC < 3) {
                            enviaTrama2(tramaspstn.getLocal0(), "=> Solicitud de id del medidor");
                        } else {
                            cerrarPuerto();
                            reintentosbadCRC = 0;
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
                    }
                } else if ((vectorhex[1].equals("18")) && contraseña && lhandshaking) {
                    //aceptacion de contraseña con data
                    if (vectorhex[2].equals("00")) {
                        //envio un ack por lo tanto acepta la peticion
                        if (validacionCRC(vectorhex)) {
                            escribir("<= Recibe Contraseña Aceptada - " + cadenahex);
                            reintentosbadCRC = 0;
                            try {
                                if (aviso) {
                                    //lm.jtablemedidores.setValueAt("Cambio timeout", indx, 3);
                                    //lm.mdc.fireTableDataChanged();
                                }
                            } catch (Exception e) {
                            }

                            //correcion cambio para velocidad
                            if (timeout > 30) {
                                byte[] tout = calculaNuevocrcTimeout((int) timeout);
                                contraseña = false;
                                ltimeout = true;
                                //System.out.println("TIMEOUT >30");
                                //MELISSA
                                ultimatramaEnviada = tout;
                                enviaTrama2(tout, "=> Solicitud de cambio de timeout");
                                //ennvia trama para el cambio de timeout
//                    enviaTrama(tout);
                            } else {
                                //System.out.println("TIMEOUT MENOR 15");
                                contraseña = false;
                                ltimeout = true;
                                //MELISSA
                                ultimatramaEnviada = tramaspstn.getLocal7();
                                enviaTrama2(tramaspstn.getLocal7(), "=> Solicitud de cambio de timeout");
                            }

                        } else {
                            escribir("BAD CRC aceptacion contraseña");
                            reintentosbadCRC++;
                            //System.out.println("CRC INVALIDO aceptacion contraseña con handshaking");
                            if (reintentosbadCRC < 3) {
                                contraseña = false;
                                inicio = true;
                                enviaTrama2(tramaspstn.getLocal0(), "=> Envio Solicitud ID medidor");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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
                        }
                    } else {
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Envio Data Contraseña");
                    }
                } else if ((vectorhex[1].equals("18")) && ltimeout) {
                    if (vectorhex[2].equals("00")) {//acepto cambio de timeout
                        //solicitamos envio a de paquetes de 256 bytes
                        escribir("<= Recibe Cambio timeout aceptado - " + cadenahex);
                        ltimeout = false;
                        tamaño = true;
                        try {
                            if (aviso) {
                                //lm.jtablemedidores.setValueAt("Tamaño de paquetes", indx, 3);
                                //lm.mdc.fireTableDataChanged();
                            }
                        } catch (Exception e) {
                        }
                        //envia trama para solicitud cambio tamano del paquete a 256
                        ultimatramaEnviada = tramaspstn.getLocal8();
                        enviaTrama2(tramaspstn.getLocal8(), "=> Solicitud de cambio de tamaño de paquetes a 256 bytes");

                    } else {
                        //System.out.println("NACK cambio El timeout");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de cambio de timeout");
                    }
                } else if (vectorhex[1].equals("18") && tamaño) {
                    if (vectorhex[2].equals("00")) {//acepto cambio de tamaño
                        escribir("<= Recibe Cambio tamaño paquete aceptado - " + cadenahex);
                        tamaño = false;
                        //solicitamos el firmware
                        try {
                            if (aviso) {
                                //lm.jtablemedidores.setValueAt("Obteniendo bloques", indx, 3);
                                //lm.mdc.fireTableDataChanged();
                            }
                        } catch (Exception e) {
                        }
                        lfirmware = true;
                        ultimatramaEnviada = tramaspstn.getLocal19();
                        enviaTrama2(tramaspstn.getLocal19(), "=> Solicitud de configuracion de firware");

                    } else {
                        //System.out.println("NACK cambio El tamaño de trama");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de Cambio de tamaño de paquetes a 256 bytes");
                    }
                } else if (vectorhex[1].equals("05") && lfirmware) { //flag para indicar que vamos a buscar el ke interno
                    if (vectorhex[2].equals("00")) {
                        if (validacionCRC(vectorhex)) {
                            if (vectorhex.length != 71) {
                                //System.out.println("Tamaño mayor a 71 firmware");
                                escribir("Error trama recibida " + cadenahex);
                                escribir("Tamaño esperado 71");
                                escribir("Tamaño recibido " + vectorhex.length);
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    ultimatramaEnviada = tramaspstn.getLocal19();
                                    enviaTrama2(tramaspstn.getLocal19(), "=> Solicitud de configuracion de firmware");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            } else {
                                escribir("<= Recibe Configuracion de firmware - " + cadenahex);
                                reintentosbadCRC = 0;
                                String pprim = vectorhex[10];

                                pprim = "" + ((Integer.parseInt((pprim), 16)) & 0x80);
                                //validamos en que clase se encuentra el ke interno
                                lfirmware = false;
                                valorconstanteke = "0.0";
                                if (pprim.equals("128")) {

                                    //si devuelve 80 el ke interno esta en la clase 7 Metering Function Block #2
                                    meteringFunction = true;
                                    ultimatramaEnviada = tramaspstn.getLocal20();
                                    enviaTrama2(tramaspstn.getLocal20(), "=> Solicitud de clase 7 Metering function block #2");
                                } else {
                                    // si devuelve 00 el ke interno esta en la clase 0
                                    medPrimaria = true;
                                    ultimatramaEnviada = tramaspstn.getLocal21();
                                    enviaTrama2(tramaspstn.getLocal21(), "=> Solicitud de clase 0 Mediciones primarias");
                                }
                            }

                        } else {
                            //System.out.println("CRC INVALIDO firmware");
                            //retranmision de datos
                            reintentosbadCRC++;
                            if (reintentosbadCRC < 3) {
                                enviaTrama2(ultimatramaEnviada, "=> Solicitud de configuracion de firmware");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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
                        }
                    } else {
                        //System.out.println("NACK acepto firmware");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de configuracion de firmware");
                    }

                } else if (vectorhex[1].equals("05") && meteringFunction) { //validamos la clase 7 para el ke interno
                    if (vectorhex[2].equals("00")) {
                        if (validacionCRC(vectorhex)) {
                            if (vectorhex.length != 71) {
                                //System.out.println("Mayor a 71 metering function clase 7");
                                escribir("Error trama recibida " + cadenahex);
                                escribir("Tamaño esperado 71");
                                escribir("Tamaño recibido " + vectorhex.length);
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    ultimatramaEnviada = tramaspstn.getLocal20();
                                    enviaTrama2(tramaspstn.getLocal20(), "=> Solicitud de clase 7 Metering funtion block #2");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            } else {
                                escribir("<= Recibe Clase 07 Metering Function - " + cadenahex);
                                reintentosbadCRC = 0;
                                valorconstanteke = vectorhex[9] + "" + vectorhex[10] + "." + vectorhex[11] + "" + vectorhex[12] + "" + vectorhex[13];
                                escribir("Valor constanteKE " + valorconstanteke);
                                meteringFunction = false;
                                fecActual = true;
                                ultimatramaEnviada = tramaspstn.getLocal22();
                                enviaTrama2(tramaspstn.getLocal22(), "=> Solicitud de fecha actual de medidor");
                            }

                        } else {
                            //System.out.println("CRC INVALIDO meteringFunction");
                            //retranmision de datos
                            escribir("BAD RCRC metering function");
                            reintentosbadCRC++;
                            if (reintentosbadCRC < 3) {
                                enviaTrama2(ultimatramaEnviada, "=> Solicitud de clase 7 Metering funtion block #2");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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
                        }
                    } else {
                        //System.out.println("NACK meteringFunction");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de clase 7 Metering funtion block #2");
                    }
                } else if (vectorhex[1].equals("05") && medPrimaria) { //validamos la clase 00 para el ke interno
                    if (vectorhex[2].equals("00")) {
                        if (validacionCRC(vectorhex)) {
                            if (vectorhex.length != 47) {
                                //System.out.println("Tamaño mayo a 47 mediciones primarias");
                                escribir("Error trama recibida " + cadenahex);
                                escribir("Tamaño esperado 47");
                                escribir("Tamaño recibido " + vectorhex.length);
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    ultimatramaEnviada = tramaspstn.getLocal21();
                                    enviaTrama2(tramaspstn.getLocal21(), "=> Solicitud de clase 0 Mediciones primarias");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            } else {
                                escribir("<= Recibe Clase 0 Mediciones primarias - " + cadenahex);
                                reintentosbadCRC = 0;
                                valorconstanteke = vectorhex[9] + "" + vectorhex[10] + "." + vectorhex[11] + "" + vectorhex[12] + "" + vectorhex[13];
                                escribir("Valor constanteKE " + valorconstanteke);

                                medPrimaria = false;
                                fecActual = true;
                                ultimatramaEnviada = tramaspstn.getLocal22();
                                enviaTrama2(tramaspstn.getLocal22(), "=> Solicitud de fecha actual de medidor");
                            }
                        } else {
                            //System.out.println("CRC INVALIDO medPrimaria");
                            //retranmision de datos
                            escribir("BAD CRC Mediciones primarias");
                            reintentosbadCRC++;
                            if (reintentosbadCRC < 3) {
                                enviaTrama2(ultimatramaEnviada, "=> Solicitud de clase 0 Mediciones primarias");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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
                        }
                    } else {
                        //System.out.println("NACK medPrimaria");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de clase 0 Mediciones primarias");
                    }
                } else if (vectorhex[1].equals("05") && fecActual) { //evaluamos la fecha actual del medidor
                    if (vectorhex[2].equals("00")) {
                        if (validacionCRC(vectorhex)) {
                            if (vectorhex.length != 55) {
                                //System.out.println("Tamaño mayor a 55 fecha actual");
                                escribir("Error trama recibida " + cadenahex);
                                escribir("Tamaño esperado 55");
                                escribir("Tamaño recibido " + vectorhex.length);
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    ultimatramaEnviada = tramaspstn.getLocal22();
                                    enviaTrama2(tramaspstn.getLocal22(), "=> Solicitud de fecha actual de medidor");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            } else {
                                //obtenemos la fecha actual del medidor
                                escribir("<= Recibe Fecha actual - " + cadenahex);
                                reintentosbadCRC = 0;
                                fechaActual = vectorhex[32] + "" + vectorhex[33] + "" + vectorhex[34] + "" + vectorhex[35] + "" + vectorhex[36] + "" + vectorhex[37];
                                //System.out.println("fecha actual " + fechaActual);
                                try {
                                    if (med.getFecha() != null) {
                                        escribir("Fecha Actual Estacion de trabajo " + new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()));
                                        escribir("Fecha Actual Medidor " + new Timestamp(sdf.parse(fechaActual).getTime()));
                                        escribir("Fecha Ultima Lectura " + med.getFecha());
                                        Long actual = new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()).getTime();
                                        Long ultimamed = med.getFecha().getTime();
                                        Long calculo = actual - ultimamed;
                                        int diasaleer = (int) (calculo / 86400000);
                                        if (calculo % 86400000 > 0) {
                                            diasaleer = diasaleer + 1;
                                        }
                                        //System.out.println("dias calculados " + diasaleer);
                                        if (diasaleer > 0) {
                                            if (diasaleer > 30) {
                                                ndias = 30;
                                            } else {
                                                ndias = diasaleer;
                                            }
                                        }
                                    }
                                    escribir("Numero de dias leer calculado " + ndias);
                                    //System.out.println("Numero de dias a leer = " + ndias);
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                                fecActual = false;
                                //solicitamos el numeor de serie del medidor status area#2
                                status2 = true;
                                tiemporetransmision = 12000;
                                seriemedidor = "";
                                ultimatramaEnviada = tramaspstn.getLocal13();
                                enviaTrama2(tramaspstn.getLocal13(), "=> Solicitud de Status 2 (Serie del medidor)");
                            }

                        } else {
                            //System.out.println("CRC INVALIDO Fecha actual");
                            escribir("BAD CRC Fecha actual");
                            //retranmision de datos
                            reintentosbadCRC++;
                            if (reintentosbadCRC < 3) {
                                enviaTrama2(ultimatramaEnviada, "=> Solicitud de fecha actual de medidor");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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

                        }
                    } else {
                        //System.out.println("NACK Fecha actual");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de fecha actual de medidor");
                    }

                } else if (vectorhex[1].equals("05") && status2) {
                    if (vectorhex[2].equals("00")) {
                        if (validacionCRC(vectorhex)) {
                            if (vectorhex.length != 31) {
                                //System.out.println("Tamaño mayor a 31 status2");
                                escribir("Error trama recibida " + cadenahex);
                                escribir("Tamaño esperado 31");
                                escribir("Tamaño recibido " + vectorhex.length);
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    ultimatramaEnviada = tramaspstn.getLocal13();
                                    enviaTrama2(tramaspstn.getLocal13(), "=> Solicitud status 2 (Serie medidor)");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            } else {
                                escribir("<= Recibe Clase Status 2 - " + cadenahex);
                                reintentosbadCRC = 0;
                                //juntamos los bytes de la serie del medidor
                                if (vectorhex.length > 16) {
                                    for (int i = 10; i < 15; i++) {
                                        seriemedidor += vectorhex[i];
                                    }
                                }
                                seriemedidor = "" + Long.valueOf(seriemedidor);
                                escribir("Numero de serie " + seriemedidor);
                                //System.out.println("Serie medidor =" + seriemedidor);
                                //solicitamos la configuracion del perfil de carga (numero de canales)
                                if (seriemedidor.equals("" + Long.valueOf(med.getnSerie()))) {
                                    status2 = false;
                                    lcanales = true;
                                    seriemedidor = med.getnSerie();

                                    ultimatramaEnviada = tramaspstn.getLocal18();
                                    enviaTrama2(tramaspstn.getLocal18(), "=> Solicitud de numero de canales");
                                } else {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (Exception e) {
                                    }
                                    escribir("Serial Invalido ");
                                    //enviando = false;
                                    escribir("Cerrando puerto " + med.getPuertocomm());
                                    enviaTrama(tramaspstn.getLocal17());//enviamso trama para cerrar el puerto
                                    cerrarPuerto();
                                    cerrarLog("Serial invalido", false);
                                    leer = false;

                                }

                            }


                        } else {
                            //System.out.println("CRC INVALIDO id medidor");
                            escribir("BAD CRC STATUS 2");
                            //retranmision de datos
                            reintentosbadCRC++;
                            if (reintentosbadCRC < 3) {
                                enviaTrama2(ultimatramaEnviada, "=> Solicitud status 2 (Serie medidor)");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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
                        }
                    } else {
                        //System.out.println("NACK acepto status2");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de Status 2 (Serie del medidor)");
                    }


                } else if (vectorhex[1].equals("05") && lcanales) {
                    if (vectorhex[2].equals("00")) {
                        if (validacionCRC(vectorhex)) {
                            if (vectorhex.length > 49) {
                                //System.out.println("Tamaño mayor a 49");
                                escribir("Error trama recibida " + cadenahex);
                                escribir("Tamaño esperado 49");
                                escribir("Tamaño recibido " + vectorhex.length);
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    ultimatramaEnviada = tramaspstn.getLocal18();
                                    enviaTrama2(tramaspstn.getLocal18(), "=> Solicitud de numero de canales");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            } else {
                                //obtenemos el numero de de canales
                                escribir("<= Recibe Numero de canales - " + cadenahex);
                                reintentosbadCRC = 0;
                                numcanales = Integer.parseInt((vectorhex[13]), 16);
                                //System.out.println("numero de canales = " + numcanales);
                                escribir("Numero de canales activos " + numcanales);
                                intervalo = Integer.parseInt(vectorhex[9], 16);
                                factorIntervalo = Integer.parseInt(vectorhex[8], 16);
                                escribir("Periodo de integracion  " + intervalo);
                                escribir("factor periodo de integracion  " + factorIntervalo);
                                int duracion = 1440 / intervalo;
                                totalIntentosPC = (((ndias + 1) * (duracion * numcanales * 2) + (ndias + 1) * 6) / 256);
                                //validamos si se obtiene el registro de eventos o no?
                                if (lregistros) {
                                    //si se obtiene el registros de eventos realiza esto
                                    lcanales = false;
                                    registroevento = true;
                                    tiemporetransmision = 8000;
                                    RegEventos = new Vector<String[]>();
                                    ultimatramaEnviada = tramaspstn.getLocal14();
                                    //peticion registro de eventos
                                    try {
                                        if (aviso) {
                                            //lm.jtablemedidores.setValueAt("Reg.Eventos", indx, 3);
                                            //lm.mdc.fireTableDataChanged();
                                        }
                                    } catch (Exception e) {
                                    }

                                    enviaTrama2(tramaspstn.getLocal14(), "=> Solicitud de Registro de eventos");
                                } else {
                                    if (lperfil) {//verificamos si obtiene el perfil de carga
                                        //cambio para el perfil de carga
                                        byte[] perfil = calculaNuevocrcPerfilCarga(ndias);
                                        tiempo = 3000;
                                        reintentosPerfilCarga = 0;
                                        //variable que guardara el perfil de carga
                                        loadProfile = new Vector<String[]>();
                                        lcanales = false;
                                        perfilcarga = true;
                                        //System.out.println("envio perfil de carga");
                                        try {
                                            if (aviso) {
                                                //lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                //lm.mdc.fireTableDataChanged();
                                            }
                                        } catch (Exception e) {
                                        }
                                        ultimatramaEnviada = perfil;
                                        bloqueant = 0;
                                        enviaTrama2(perfil, "=> Solicitud de perfil de carga");
                                    } else {//si no se pide nada cierra comunicacion
                                        //terminahilos();
                                        tiempo = 3000;
                                        lcanales = false;
                                        rutinaCorrecta = false;
                                        enviando = false;
                                        try {
                                            Thread.sleep(7000);
                                        } catch (Exception e) {
                                        }
                                        enviando = false;
                                        enviaTrama(tramaspstn.getLocal17());//enviamso trama para cerrar el puerto
                                        cerrarPuerto();

                                        escribir("Estado de lectura (Leido)");
                                        med.MedLeido = true;
                                        cerrarLog("Leido", true);
                                        leer = false;
                                    }

                                }
                            }
                        } else {
                            //bad crc configuracion perfil de carga
                            escribir("BAD CRC Configuracion del perfil de carga");
                            //System.out.println("CRC INVALIDO conf perfil carga");
                            //retranmision de datos
                            reintentosbadCRC++;
                            if (reintentosbadCRC < 3) {
                                enviaTrama2(ultimatramaEnviada, "=> Solicitud de configuracion del perfil de carga");
                            } else {
                                cerrarPuerto();
                                reintentosbadCRC = 0;
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
                        }
                    } else {
                        //nack configuracion perfil de carga
                        //System.out.println("NACK Conf perfil carga");
                        //validacion de ack
                        validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de numero de canales");
                    }

                } else if (registroevento) {
                    try {
                        if (vectorhex[2].equals("00")) {
                            if (validacionCRC(vectorhex)) {

                                if (vectorhex.length == 71) {
                                    escribir("<= Recibe datos registro eventos  - " + cadenahex);
                                    RegEventos.add(vectorhex);
                                    reintentosbadCRC = 0;
                                    //es la ultima trama? si es la ultima trama que recibe.
                                    //enviamos la peticion de peticion de siguiente trama
                                    //solicitamos mas datos del egistro de eventos

                                    enviaTramaEventos(tramaspstn.getLocal10(), "=> Solicitud de siguientes eventos");
                                } else {
                                    if (vectorhex.length > 71) {
                                        //trama invalida
                                        //System.out.println("Error trama invalida registro de eventos");
                                        escribir("Error trama recibida " + cadenahex);
                                        escribir("Tamaño esperado 71");
                                        escribir("Tamaño recibido " + vectorhex.length);
                                        reintentosbadCRC++;
                                        if (reintentosbadCRC < 3) {
                                            RegEventos = new Vector<String[]>();
                                            ultimatramaEnviada = tramaspstn.getLocal14();
                                            enviaTrama2(tramaspstn.getLocal14(), "=> Solicitud de Registro de eventos");
                                        } else {
                                            cerrarPuerto();
                                            reintentosbadCRC = 0;
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
                                    } else {
                                        escribir("<= Recibe datos registro eventos - " + cadenahex);
                                        RegEventos.add(vectorhex);
                                        reintentosbadCRC = 0;
                                        //validamos si leemos el perfil de carga
                                        if (lperfil) {
                                            //cambio para el perfil de carga
                                            byte[] perfil = calculaNuevocrcPerfilCarga(ndias);
                                            tiempo = 1000;
                                            reintentosPerfilCarga = 0;
                                            //variable que guardara el perfil de carga
                                            loadProfile = new Vector<String[]>();
                                            registroevento = false;
                                            perfilcarga = true;
                                            //System.out.println("envio perfil de carga");
                                            try {
                                                if (aviso) {
                                                    //lm.jtablemedidores.setValueAt("Perfil Carga", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }

                                            ultimatramaEnviada = perfil;
                                            bloqueant = 0;
                                            enviaTrama2(perfil, "=> Solicitud de perfil de carga");
                                        } else {
                                            terminahilos();
                                            tiempo = 3000;
                                            registroevento = false;
                                            //rutinaCorrecta = false;
                                            enviando = false;
                                            Thread.sleep(7000);
                                            //enviando = false;
                                            escribir("Cerrando puerto " + med.getPuertocomm());
                                            enviaTrama(tramaspstn.getLocal17());//enviamso trama para cerrar el puerto
                                            cerrarPuerto();
                                            try {
                                                if (aviso) {
                                                    //lm.jtablemedidores.setValueAt("Almacenando datos", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            escribir("Validando Registros");
                                            validarRegistros(numcanales); //validamos los registros obtenidos del perfil de carga y el registro de eventos

                                            try {
                                                if (aviso) {
                                                    //lm.jtablemedidores.setValueAt("Leido", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            escribir("Estado de lectura (Leido)");
                                            med.MedLeido = true;
                                            cerrarLog("Leido", true);
                                            leer = false;
                                        }
                                    }


                                }
                            } else {
                                escribir("BAD CRC Registro de evento");
                                //System.out.println("Bad CRC registro de evento");
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    enviaTrama2(ultimatramaEnviada, "=> Solicitud de Registro de eventos");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                                        leer = false;
                                    }
                                }
                            }

                        } else {
                            //System.out.println("NACK registro de eventos");
                            //validacion de ack
                            validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de Registro de eventos");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (vectorhex[1].equals("05") && perfilcarga) {
                    try {
                        if (vectorhex[2].equals("00")) {
                            //validamos el crc del perfil
                            int tam = 0;
                            if (vectorhex.length >= 263) {
                                tam = 261;
                            } else {

                                tam = Integer.parseInt(vectorhex[4], 16) + 5;
                            }
                            String vectorhextem[] = new String[tam + 2];
                            System.arraycopy(vectorhex, 0, vectorhextem, 0, vectorhextem.length);
                            if (validacionCRC2(vectorhextem, tam)) {
                                boolean realizaperfil = false;
                                if (vectorhex[4].equals("00") && vectorhextem.length >= 263) {
                                    realizaperfil = true;
                                } else {
                                    if (reintentosPerfilCarga == (int) totalIntentosPC) {
                                        if (vectorhextem.length == ((Integer.parseInt(vectorhextem[4], 16)) + 7)) {
                                            realizaperfil = true;
                                        }
                                    }
                                }
                                if (realizaperfil) {
                                    reintentosbadCRC = 0;
                                    //Validamos el perfil de carga
                                    bloqueperfil = false;
                                    if (reintentosPerfilCarga != 0) {
                                        bloqueact = Integer.parseInt(Integer.toHexString(((Integer.parseInt((vectorhex[3]), 16)) & 0x70)));
                                        //System.out.println("Bloque actual=" + bloqueact + "    bloque anterior=" + bloqueant);
                                        if (validarBloquePerfilCarga(bloqueant, bloqueact)) {
                                            bloqueant = bloqueact;
                                            bloqueperfil = true;
                                        }
                                    } else {
                                        bloqueant = 0;
                                        bloqueperfil = true;
                                    }
                                    if (bloqueperfil) {
                                        escribir("<= Recibe datos perfil carga - " + cadenahex);
                                        loadProfile.add(vectorhextem);
                                        if (reintentosPerfilCarga < (int) totalIntentosPC) {
                                            //es la ultima trama? si no es la ultima trama que recibe.
                                            //enviamos la peticion de peticion de siguiente trama
                                            reintentosPerfilCarga++;
                                            //solicitamos mas datos para el perfil de carga


                                            enviaTramaPerfil(tramaspstn.getLocal10(), "=> Solicitud de siguientes intervalos del perfil de carga");

                                        } else {
                                            //ya tenemos el perfil de carga

                                            terminahilos();
                                            tiempo = 3000;
                                            //perfilcarga = false;
                                            enviando = false;
                                            rutinaCorrecta = false;
                                            Thread.sleep(7000);
                                            escribir("Cerrando puerto " + med.getPuertocomm());
                                            enviando = false;
                                            enviaTrama(tramaspstn.getLocal17());
                                            cerrarPuerto();
                                            escribir("Validando Registros");
                                            validarRegistros(numcanales);
                                            try {
                                                if (aviso) {
                                                    //lm.jtablemedidores.setValueAt("Leido", indx, 3);
                                                    //lm.mdc.fireTableDataChanged();
                                                }
                                            } catch (Exception e) {
                                            }
                                            escribir("Estado de lectura (Leido)");
                                            cerrarLog("Leido", true);
                                            med.MedLeido = true;
                                            leer = false;

                                        }
                                    } else {
                                        //si la trama es = no guardamos y solicitamos la siguiente
                                        escribir("<= Recibe datos perfil carga - " + cadenahex);
                                        escribir("Bloque anterior ya leido");
                                        enviaTramaPerfil(tramaspstn.getLocal10(), "=> Solicitud de siguientes intervalos del perfil de carga");
                                    }
                                } else {
                                    //System.out.println("Tamaño incorecto perfil de carga");
                                    escribir("Tamaño incorrecto perfil de carga ");
                                    reintentosbadCRC++;
                                    if (reintentosbadCRC < 3) {
                                        byte[] perfil = calculaNuevocrcPerfilCarga(ndias);
                                        loadProfile = new Vector<String[]>();
                                        reintentosPerfilCarga = 0;
                                        ultimatramaEnviada = perfil;
                                        bloqueant = 0;
                                        enviaTrama2(perfil, "=> Solicitud de perfil de carga");
                                    } else {
                                        cerrarPuerto();
                                        reintentosbadCRC = 0;
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
                                            escribir("Estado Lectura No Leido");
                                            leer = false;
                                        }
                                    }

                                }
                            } else {
                                //retransmision
                                //System.out.println("BAD CRC perfil de carga");
                                escribir("BAD CRC perfil de carga");
                                reintentosbadCRC++;
                                if (reintentosbadCRC < 3) {
                                    byte[] perfil = calculaNuevocrcPerfilCarga(ndias);
                                    loadProfile = new Vector<String[]>();
                                    reintentosPerfilCarga = 0;
                                    ultimatramaEnviada = perfil;
                                    bloqueant = 0;
                                    enviaTrama2(ultimatramaEnviada, "=> Solicitud de perfil de carga");
                                } else {
                                    cerrarPuerto();
                                    reintentosbadCRC = 0;
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
                            }
                        } else {
                            //nack perfil de carga
                            //System.out.println("NACK Envio perfil de carga");
                            //validacion de ack
                            validarnack(ultimatramaEnviada, vectorhex[2], "Solicitud de perfil de carga");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    escribir("<= Recibe Trama fuera del protocolo - " + cadenahex);
                    //aqui fue que llego algo que no es del protocolo por lo tanto reiniciamos comunicacion.
                    if (cierrapuerto) {
                        cerrarPuerto();
                        cerrarLog("Error de protocolo", false);
                        leer = false;
                    } else {
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
                }
            } else {
                // la trama es menor a 3 bytes
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

        }
    }
    Thread port = null;
    boolean cierrapuerto = false;
    Thread port2;

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
                            escribir("=> " + tramaspstn.encode(trama, trama.length));
                            //System.out.println(tramaspstn.encode(trama, trama.length));
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
//                            boolean salir = true;
//                            int intentosalir = 0;
//                            while (salir) {
//                                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
////                                    System.out.println("Sale enviar");
//                                    salir = false;
//                                    if (!enviando) {
//                                        t = false;
//                                    }
//
//                                } else {
//                                    intentosalir++;
////                                    System.out.println("Esperando... enviatrama2");
//                                    sleep(500);
//                                }
//                            }
                            Thread.sleep(tiemporetransmision);
                        }

                    }
                    if (cierrapuerto) {
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
                    e.printStackTrace();
                }
            }
        };
        port.start();

    }

    private void enviaTramaPerfil(byte[] bytes, String descripcion) {
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
            public void run() {
                try {
                    int intentosRetransmision = 0;
                    boolean t = true;
                    while (t) {

                        if (enviando) {
                            if (intentosRetransmision == 0) {
                                escribir(des);
                                //System.out.println(des);
                                //System.out.println("Envia trama =>");
                                escribir("=> " + tramaspstn.encode(trama, trama.length));
                                //System.out.println(tramaspstn.encode(trama, trama.length));
                                enviaTrama(trama);
                            } else {
                                escribir(" => Solicitud ultima trama enviada");
                                enviaTrama(tramaspstn.getLocal16());
                            }
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
                    e.printStackTrace();
                }
            }
        };
        port.start();

    }

    private void enviaTramaEventos(byte[] bytes, String descripcion) {
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
            public void run() {
                try {
                    int intentosRetransmision = 0;
                    boolean t = true;
                    while (t) {

                        if (enviando) {
                            if (intentosRetransmision == 0) {
                                escribir(des);
                                //System.out.println(des);
                                //System.out.println("Envia trama =>");
                                escribir("=> " + tramaspstn.encode(trama, trama.length));
                                //System.out.println(tramaspstn.encode(trama, trama.length));
                                enviaTrama(trama);
                            } else {
                                escribir("=> Solicitud Registro de eventos");
                                RegEventos = new Vector<String[]>();
                                ultimatramaEnviada = tramaspstn.getLocal14();
                                enviaTrama(tramaspstn.getLocal14());

                            }
                            intentosRetransmision++;
                        } else {
                            t = false;
                        }
                        if (intentosRetransmision > 8) {
                            //reiniciaComunicacion();
                            escribir("Numero de reenvios agotado");
                            enviando = false;
                            t = false;
                            cierrapuerto = true;
                        } else {
                            Thread.sleep(tiemporetransmision);
                        }

                    }
                    if (cierrapuerto) {
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
                    e.printStackTrace();
                }
            }
        };
        port.start();

    }
    boolean inicia1 = false;

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
                    escribir("=> " + tramaspstn.encode(trama, trama.length));
                    //System.out.println(tramaspstn.encode(trama, trama.length));
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
                    e.printStackTrace();
                }
            }
        };
        port2.start();

    }
    Thread port3 = null;
    boolean esperandoconexion = true;

    private void enviaTramaConexion(byte[] bytes, String descripcion) {
        final byte[] trama = bytes;
        final String des = descripcion;
        lhandshaking = false;
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
                    escribir("=> " + tramaspstn.encode(trama, trama.length));
                    //System.out.println(tramaspstn.encode(trama, trama.length));
                    enviaTrama(trama);
                    while (entro) {
                        if (lhandshaking) {
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
                                escribir("Numero de reintentos agotado..");
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

    public byte[] calculaNuevocrcPerfilCarga(int ndias) {
        try {
            byte[] bytes = tramaspstn.getLocal9();
            bytes[4] = (byte) (Integer.parseInt(Integer.toHexString((ndias + 1)), 16) & 0xFF);
            byte b[] = new byte[bytes.length - 2];
            for (int j = 0; j < b.length; j++) {
                b[j] = (byte) bytes[j];
            }
            int crcCalculado = CRCABB(b);
            String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();

            if (calculo.length() == 3) {
                calculo = "0" + calculo;
            } else if (calculo.length() == 2) {
                calculo = "00" + calculo;
            } else if (calculo.length() == 1) {
                calculo = "000" + calculo;
            }
            bytes[8] = (byte) (Integer.parseInt((calculo.substring(0, 2)), 16) & 0xFF);
            bytes[9] = (byte) (Integer.parseInt((calculo.substring(2, 4)), 16) & 0xFF);
            return bytes;
        } catch (Exception e) {
        }
        return null;
    }

    public byte[] calculaNuevocrcTimeout(int timeout) {
        byte[] tout = tramaspstn.getLocal7();
        tout[5] = (byte) (Integer.parseInt(Integer.toHexString(timeout), 16) & 0xFF);
        byte b[] = new byte[tout.length - 2];
        for (int j = 0; j < b.length; j++) {
            b[j] = (byte) tout[j];
        }
        int crcCalculado = CRCABB(b);
        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();

        if (calculo.length() == 3) {
            calculo = "0" + calculo;
        } else if (calculo.length() == 2) {
            calculo = "00" + calculo;
        } else if (calculo.length() == 1) {
            calculo = "000" + calculo;
        }
        tout[6] = (byte) (Integer.parseInt((calculo.substring(0, 2)), 16) & 0xFF);
        tout[7] = (byte) (Integer.parseInt((calculo.substring(2, 4)), 16) & 0xFF);

        return tout;
    }

    private byte[] calcularPSWD(String[] datapswd) {
        //recibimos la trama menos los 2 ultimos bytes
        //de esta trama sacamos los ultimos 4 bytes que son el password del medidor
        byte[] psw = new byte[4];
        psw[0] = (byte) (Integer.parseInt(datapswd[datapswd.length - 6], 16) & 0xFF);
        psw[1] = (byte) (Integer.parseInt(datapswd[datapswd.length - 5], 16) & 0xFF);
        psw[2] = (byte) (Integer.parseInt(datapswd[datapswd.length - 4], 16) & 0xFF);
        psw[3] = (byte) (Integer.parseInt(datapswd[datapswd.length - 3], 16) & 0xFF);
        String password = algPsw(psw, this.password);
        String[] data = password.split(" ");
        for (int j = 0; j < data.length; j++) {
            if (data[j].length() == 1) {
                data[j] = "0" + data[j];
            }
        }
        byte[] bytes = tramaspstn.getLocal2();
        bytes[5] = (byte) (Integer.parseInt(data[0], 16) & 0xFF);
        bytes[6] = (byte) (Integer.parseInt(data[1], 16) & 0xFF);
        bytes[7] = (byte) (Integer.parseInt(data[2], 16) & 0xFF);
        bytes[8] = (byte) (Integer.parseInt(data[3], 16) & 0xFF);

        int crcCalculado = CRCABB(bytes);
        String calculo = "" + Integer.toHexString(crcCalculado).toUpperCase();

        if (calculo.length() == 3) {
            calculo = "0" + calculo;
        } else if (calculo.length() == 2) {
            calculo = "00" + calculo;
        } else if (calculo.length() == 1) {
            calculo = "000" + calculo;
        }
        byte[] bytes2 = tramaspstn.getLocal3();
        bytes2[5] = (byte) (Integer.parseInt(data[0], 16) & 0xFF);
        bytes2[6] = (byte) (Integer.parseInt(data[1], 16) & 0xFF);
        bytes2[7] = (byte) (Integer.parseInt(data[2], 16) & 0xFF);
        bytes2[8] = (byte) (Integer.parseInt(data[3], 16) & 0xFF);
        bytes2[9] = (byte) (Integer.parseInt((calculo.substring(0, 2)), 16) & 0xFF);
        bytes2[10] = (byte) (Integer.parseInt((calculo.substring(2, 4)), 16) & 0xFF);
        return bytes2;
    }

    private String algPsw(byte[] psw, String pwdata) {
        byte[] KeyFinal = new byte[4];
        //byte[] KeyFinalm = new byte[4];
        //Contraseña para medidor leido por PSTN SALUDCOP
        //byte[] pwd = {(byte) 0x01, (byte) 0x10, (byte) 0x10, (byte) 0x00,};
        byte[] pwd = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        if (pwdata.length() == 8) {

            pwd[0] = (byte) (Integer.parseInt(pwdata.substring(2, 4), 16) & 0xFF);
            pwd[1] = (byte) (Integer.parseInt(pwdata.substring(0, 2), 16) & 0xFF);
            pwd[2] = (byte) (Integer.parseInt(pwdata.substring(6, 8), 16) & 0xFF);
            pwd[3] = (byte) (Integer.parseInt(pwdata.substring(4, 6), 16) & 0xFF);
        }

        encrypt_password obj = new encrypt_password();
        KeyFinal = obj.encrypt_password1(pwd, psw);
        String passwordstx = "";
        for (int i = 0; i <= 3; i++) {
            if (i != 0) {
                passwordstx += " ";
            }
            passwordstx += Long.toHexString(KeyFinal[i] & 0xFF).toUpperCase();
        }
        return passwordstx;
    }

    private boolean validacionCRC(String[] data) {
        boolean lcrc = false;
        if (data.length >= 4) {
            byte b[] = new byte[data.length - 2];
            for (int j = 0; j < b.length; j++) {
                b[j] = (byte) (Integer.parseInt(data[j], 16) & 0xFF);
            }
            int crc = CRCABB(b);
            String stx = data[data.length - 2] + "" + data[data.length - 1];
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
        }

        return lcrc;
    }

    private boolean validacionCRC2(String[] data, int n) {
        boolean lcrc = false;
        if (data.length >= 4) {
            byte b[] = new byte[n];
            for (int j = 0; j < b.length; j++) {
                b[j] = (byte) (Integer.parseInt(data[j], 16) & 0xFF);
            }
            int crc = CRCABB(b);
            String stx = data[data.length - 2] + "" + data[data.length - 1];
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
        }

        return lcrc;
    }

    private int CRCABB(byte[] stx) {
        int crc = 0x0000;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)
        for (byte b : stx) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xffff;
        return crc;
    }

    private void jinit() {
        //confModem = cp.obtenerTramas(med);
        if (confModem.size() > 0) {
            numeroPuerto = med.getPuertocomm();
            numeroReintentos = med.getReintentos();
            velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
            password = med.getPassword();
            portList = CommPortIdentifier.getPortIdentifiers();
            timeout = (long) ((255 * med.getTimeout()) / 127.5);
            ndias = med.getNdias();
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

            //verificamos si esta abierto el puerto
            //verificamos si esta abierto el puerto
            iniciacomunicacion();
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
    boolean desbloqueo = false;
    int actualReintento = 0;

    private void iniciacomunicacion() {
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
                    lhandshaking = false;
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
                    try {
                        if (aviso) {
                            //lm.jtablemedidores.setValueAt("No Leido", indx, 3);
                            //lm.mdc.fireTableDataChanged();
                        }
                    } catch (Exception e) {
                    }
                    escribir("Estado Lectura No Leido");
                    cerrarLog("Medidor no configurado", false);
                    leer = false;
                }
            } else {
                //si no esta configurado cerramos el puerto
                cerrarPuerto();
               cerrarLog("Medidor no configurado", false);
                escribir("Estado Lectura No Leido");
                leer = false;
            }
        } else {
            escribir("Estado Lectura No Leido");
            cerrarLog("No conectado", false);
            leer = false;
        }
    }

    private void validarnack(byte[] ultimatramaenviada, String nack, String des) {
        escribir("NACK " + nack + " " + des);
        if (nack.equals("01") || nack.equals("03") || nack.equals("04") || nack.equals("07")) {
            enviaTrama2(ultimatramaenviada, des);
        } else if (nack.equals("02")) {
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
        } else if (nack.equals("05")) {
            cerrarPuerto();
            cerrarLog("Error de recepcion", false);
            escribir("Estado Lectura No Leido");
            leer = false;
        } else if (nack.equals("06")) {
            cerrarPuerto();
            cerrarLog("Error de recepcion", false);
            escribir("Estado Lectura No Leido");
            leer = false;
        } else {
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
            lhandshaking = false;
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

    private void abrePuerto() {
        try {
            portconect = false;
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

        } catch (PortInUseException e) {
            try {
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), seriemedidor, "LeerPSTNABB", "Error al conectar con el modem");
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("Error comunicacion", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception ex) {
            }


        }
        if (portconect) {
            try {
                serialPort.setSerialPortParams(velocidadPuerto, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                e.printStackTrace();
            }

            try {
                //Se crea un objeto input de la clase CountingInputStream con lo que se obtiene del metodo
                //getInputStream() que retorna lo que hay en la entrada
                //
                input = new CountingInputStream(serialPort.getInputStream());
                output = new BufferedOutputStream(serialPort.getOutputStream());


            } catch (IOException e) {
                System.err.println("Error Input/output");

            }

            try {
                serialPort.addEventListener(this);
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }

            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnOutputEmpty(true);
        }

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
            e.printStackTrace();

        }
    }

    private void almacenaRegistroEventos(Vector<String[]> RegEventos) {
        try {
            Vector<String> data = new Vector<String>();
            String dataRegeventos = "";
            for (int pf = 0; pf < RegEventos.size(); pf++) {
                for (int j = 5; j < RegEventos.get(pf).length - 2; j++) {
                    dataRegeventos += "" + RegEventos.get(pf)[j];
                }
            }
            while (dataRegeventos.length() > 0) {
                if (dataRegeventos.length() >= 14) {
                    data.add(dataRegeventos.substring(0, 14));
                    dataRegeventos = dataRegeventos.substring(14, dataRegeventos.length());
                } else {
                    data.add(dataRegeventos.substring(0, dataRegeventos.length()));
                    dataRegeventos = "";
                }
            }
            ////System.out.println("Tamaño Eventp =" + data.size());
//            for (int i = 0; i < data.size(); i++) {
////                System.out.println("Evento " + i + " " + data.get(i));
//            }
            cp.eventosABB(data, seriemedidor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validarRegistros(int numcanales) {

        if (lperfil) {
            try {
                if (aviso) {
                    //lm.jtablemedidores.setValueAt("Separando datos", indx, 3);
                    //lm.mdc.fireTableDataChanged();
                }
            } catch (Exception e) {
            }

            //guarda el perfil de carga en la base de datos
            almacenaPerfilCargaNCanales(loadProfile, numcanales);
        }
        if (lregistros) {
            //guarda el registro de eventos
            almacenaRegistroEventos(RegEventos);
        }
    }

    private void almacenaPerfilCargaNCanales(Vector<String[]> loadProfile, int numeroCanales) {
        //revisamos las tramas del perfil de carga
        ////System.out.println("Separamos el data del perfil de carga");
        int duracionDias = 1440 / intervalo;
        String dataPerfilCarga = "";
        //int desglosa = ((numeroCanales * 2 * 96) + 6) * 2;
        int desglosa = ((numeroCanales * 2 * duracionDias) + 6) * 2;
        for (int pf = 0; pf < loadProfile.size(); pf++) {
            for (int j = 5; j < loadProfile.get(pf).length - 2; j++) {
                dataPerfilCarga += "" + loadProfile.get(pf)[j];
            }
        }
        ////System.out.println("separamos entramas de n bytes depende del numero de canales");
        try {
            Vector<String> data = new Vector<String>();
            while (dataPerfilCarga.length() > 0) {
                if (dataPerfilCarga.length() >= desglosa) {
                    data.add(dataPerfilCarga.substring(0, desglosa));
                    dataPerfilCarga = dataPerfilCarga.substring(desglosa, dataPerfilCarga.length());
                } else {
                    data.add(dataPerfilCarga.substring(0, dataPerfilCarga.length()));
                    dataPerfilCarga = "";
                }

            }

            cp.lecturaABBNCanales(data, seriemedidor, med.getMarcaMedidor().getCodigo(), Double.parseDouble(valorconstanteke), numeroCanales, fechaActual, intervalo, factorIntervalo, file, med.getFecha());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void terminahilos() {
        try {
            if (port != null) {
                if (port.isAlive()) {
                    port.stop();
                    port = null;
                }
            }
            if (port2 != null) {
                if (port2.isAlive()) {
                    port2.stop();
                    port2 = null;
                }
            }

        } catch (Exception eport) {
        }
    }
    int bloqueant = 0;
    int bloqueact = 0;

    public boolean validarBloquePerfilCarga(int bloqueanterior, int bloqueactual) {

        boolean lbloque = false;
        switch (bloqueanterior) {
            case 0: {
                if (bloqueactual == 10) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 10: {
                if (bloqueactual == 20) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 20: {
                if (bloqueactual == 30) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 30: {
                if (bloqueactual == 40) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 40: {
                if (bloqueactual == 50) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 50: {
                if (bloqueactual == 60) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 60: {
                if (bloqueactual == 70) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }
            case 70: {
                if (bloqueactual == 0) {
                    ////System.out.println("Bloque actual=" + bloqueactual + "    bloque anterior=" + bloqueanterior);
                    lbloque = true;
                }
                break;
            }

        }
        return lbloque;

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

    public void cerrarLog(String status, boolean lexito) {
        tiempofinal = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        ELogCall log = new ELogCall();
        log.setDfecha(Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime()));
        log.setSerie(med.getnSerie());
        log.setFechaini(tiempoinicial);
        log.setFechafin(tiempofinal);
        log.setStatus(status);
        log.setLperfil(lperfil);
        log.setLeventos(lregistros);
        log.setLregistros(false);
        log.setNduracion((int) (tiempofinal.getTime() - tiempoinicial.getTime()));
        log.setNreintentos(reintentosUtilizados);
        log.setVccoduser(usuario);
        log.setLexito(lexito);
        log.setTipoCall("2");// es programado
        cp.saveLogCall(log, null);
    }
}
