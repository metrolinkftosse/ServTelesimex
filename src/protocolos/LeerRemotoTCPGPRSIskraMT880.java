/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Datos.TramasRemotaGPRSIskraMT880;
import Entidades.Abortar;
import Entidades.EConstanteKE;
import Entidades.ELogCall;
import Entidades.EMedidor;
import Entidades.ERegistroEvento;
import Entidades.Electura;
import Entidades.EtipoCanal;
//import Rutinas.LecturaMedidoresFrame;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Dell
 */

public class LeerRemotoTCPGPRSIskraMT880 extends Thread{

  int reintentosUtilizados;// reintentos utlizados
  int bytesRecortados = 0;
  Timestamp tiempoinicial; //inicio de comunicacion
  Timestamp tiempofinal;//fin de comunicacion
  Date d = new Date();
  String seriemedidor = "";
  String datoserial = "";
  boolean rutinaCorrecta = false;
  InputStream input;
  OutputStream output;
  long tiempo = 500;
  String cadenahex = "";
  TramasRemotaGPRSIskraMT880 tramastcum = new TramasRemotaGPRSIskraMT880();
  boolean aviso = false;
//  LecturaMedidoresFrame lm = null;
  String password = "";
  String password2 = "";
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
  long ndias;
  Date fechaActual;
  //*** Conf Hora***///
  Timestamp time = null; //tiempo de ZID
  Timestamp tsfechaactual;
  Timestamp deltatimesync1;
  Timestamp deltatimesync2;
  long ndesfasepermitido = 0;
  boolean solicitar; //variable de control de la sync
  boolean portconect = false;
  long tiemporetransmision = 0;
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
  //****** Estados******//
  boolean lARRQ = false;
  boolean lserial = false;
  boolean lfechaactual = false;
  boolean lfechaactual2 = false;
  boolean lperiodoIntegracion = false;
  boolean linfoperfil = false;
  boolean lconstants = false;
  boolean lpowerLost = false;
  boolean lperfilEventos = false;
  boolean lperfilcarga = false;
  boolean lfechasync = false;
  boolean enviando = false;
  boolean reenviando = false;
  boolean rcom = false;
  byte[] ultimatramaEnviada = null;
  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
  SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
  SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM/dd HH");
  SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
  int numcanales = 2;
  int intervalo = 0;
  int factorIntervalo = 0;
  int ndia = 3;
  int dirfis = 0;
  int dirlog = 0;
  int users[] = {1}; //dirección origen - administrador para MT880 es "1" - Reading "16"
  String wrapperGPRS = ""; //Wrapper GPRS
  int listaeventos =0;
  File file;
  RandomAccessFile fr;
  boolean existearchivo = false;
  public String tramaIncompleta = "";
  public boolean complemento = false;
  public boolean lperfilcompleto = false;//variable que controla el perfil de carga incompleto.
  int reinicio = 0;
  boolean ultimbloquePerfil = false;
  Vector<String> vPerfilCarga;
  Vector<String> vEventos;
  Vector<String> infoPerfil;
  ArrayList<String> obis;
  ArrayList<String> conskePerfil;
  ArrayList<String> clase;
  ArrayList<String> unidad;
  String eventCode="";
  private int nintervalosperfil = 0;
  private int nintervaloseventos = 0;
  private int periodoIntegracion = 15;
  private boolean primerbloque;
  private boolean ultimobloqueEventos = false;
  private int indxuser = 0;
  private boolean ultimatramabloque = false;
  SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
  Abortar objabortar;
  private String usuario = "admin";
  List<String> listEventos;
  List<ERegistroEvento> listRegEventos;
  ERegistroEvento regEvento;
  public short pila[] = new short[10];
  public int i1, j1, l1;
  int indexConstant = 0;
  int eventObisChange = 0;
  ZoneId zid;

  public LeerRemotoTCPGPRSIskraMT880(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid) {
    this.med = med;
    this.cp = cp;
    this.usuario = usuario;
    this.zid = zid;
    File f = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/");
    if (!f.exists()) {
      f.mkdirs();
    }
    file = new File(cp.rutalogs + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/" + this.med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
//        file = new File(cp.rutalogs + "" + this.med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
    try {
      if (file.exists()) {
        existearchivo = true;
      } else {
        //System.out.println();
      }
    } catch (Exception e) {
      //System.err.println(e.getMessage());
    }
    this.lconfhora = lconfhora;
    lperfil = perfil;
    leventos = eventos;
    lregistros = registros;
    this.aviso = aviso;
    this.objabortar = objabortar;
//    this.lm = lm;
    this.indx = indx;
    jinit();
  }

  private void jinit() {
    indxuser = 0;
    numeroPuerto = med.getPuertocomm();
    numeroReintentos = med.getReintentos();
    velocidadPuerto = Integer.parseInt(med.getVelocidadpuerto().getDescripcion());
    password = med.getPassword();
    password2 = med.getPassword2();
    timeout = (long) ((255 * med.getTimeout()) / 127.5);
    ndias = med.getNdias() + 1;
    seriemedidor = med.getnSerie();
    numcanales = med.getNcanales();
    dirfis = Integer.parseInt((med.getDireccionFisica() == null ? "0" : med.getDireccionFisica()));
    dirlog = Integer.parseInt((med.getDireccionLogica() == null ? "0" : med.getDireccionLogica()));
    users[0] = Integer.parseInt((med.getDireccionCliente()== null ? "1" : med.getDireccionCliente()));
    String sdirfis = Integer.toHexString((dirfis) & 0xFF).toUpperCase();
            while (sdirfis.length() < 2) {
                sdirfis = "0" + sdirfis;
            }
    String susers = Integer.toHexString((users[0]) & 0xFF).toUpperCase();
            while (susers.length() < 2) {
                susers = "0" + susers;
            }
    wrapperGPRS = "000100"+susers+"00"+sdirfis;// 00 01 00 client 00 physical - Ex. 000100010017
    ndesfasepermitido = cp.getdesfasePermitido(null);
    lconske = cp.buscarConstantesKeLong(med.getnSerie());//se toman los valores de las constantes 
//    lconske = cp.buscarConstantesKe(med.getnSerie(), null);//se toman los valores de las constantes 
    vtipocanal = cp.obtenerTipoCanales(med.getMarcaMedidor().getCodigo());
    try {
      abrePuerto();
      tiempo = 1000; //Escucha
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  private void abrePuerto() {
    reintentosUtilizados++;
    tiempoinicial = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    try {
      if (!objabortar.labortar) {
        if (reintentoconexion < 3) {
          avisoStr("Conectando..");
          
          //System.out.println("Conectando.. " + med.getDireccionip() + ":" + med.getPuertoip());
          socket = new Socket(med.getDireccionip(), Integer.valueOf(med.getPuertoip()).intValue());
          med.setFecha(cp.findUltimafechaLec(med.getnSerie()));
          avisoStr("Conectado");

          //System.out.println("Conectado " + med.getDireccionip() + ":" + med.getPuertoip());
          socket.setSoTimeout(35000);
          portconect = true;
          if (!socket.isClosed()) {
            portconect = true;
            reintentoconexion = 0;
          } else {
            portconect = false;
          }
          if (portconect) {
            try {
              //Se crea un objeto input de la clase CountingInputStream con lo que se obtiene del metodo
              //getInputStream() que retorna lo que hay en la entrada
              input = socket.getInputStream();
              output = socket.getOutputStream();
            } catch (IOException e) {
              System.err.println("Error Input/output");
            }
          }
          try {
            //escuchamos el puerto
            if (portconect) {
              //System.out.println("Inicia Escucha!");
              escuchar = new Thread() {
                @Override
                public void run() {
                  escucha();
                }
              };
              escuchar.start();
            }
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
          //esperamos que se reinicie el escuchar
          Thread.sleep(2000);
          if (portconect) {
            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Conexion satisfactoria");
            if (reinicio == 0) {
              iniciacomunicacion();
            } else {
              iniciacomunicacion2();
            }
          } else {
            if (reintentoconexion > 3) {
              cerrarLog("No conectado", false);
              leer = false;
            } else {
              reintentoconexion++;
            }
          }
        } else {
          cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerTCUM", "Numero de intentos de conexion agotado");
          //System.out.println("Sale abre puerto");
          avisoStr("No Leido");
          cerrarLog("No conectado", false);
          leer = false;
        }

      } else {
        //System.out.println("Sale abre puerto");
        avisoStr("Abortado");
        cerrarLog("Abortado", false);
        leer = false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "No conectado");
      avisoStr("No conectado");

      // System.err.println(e.getMessage());
      escribir("ERROR de comunicacion " + e.getMessage());
      reintentoconexion++;
      abrePuerto();
    }
  }

  public void avisoStr(String str) {
    try {
      if (aviso) {
//        lm.jtablemedidores.setValueAt(str, indx, 3);
//        //lm.mdc.fireTableDataChanged();
//        lm.mdc.fireTableDataChanged();
      }
    } catch (Exception e) {
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
    }
  }

  private void escucha() {
    try {
      escucha = true;
      rutinaCorrecta = false;
      while (escucha) {
        if (!rutinaCorrecta) {
          Thread.sleep(tiempo);
          procesaCadena();
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
    byte[] readBuffer = new byte[256000];
    try {
      int numbytes = 0;
      //si el puerto tiene datos llenamos el buffer con lo que se encuentra en el puerto.
      while (!socket.isClosed() && input.available() > 0) {
        numbytes = input.read(readBuffer);
      }
      //codificamos las tramas que vienen en hexa para indetificar su contenido
      cadenahex = tramastcum.encode(readBuffer, numbytes);
      //creamos un vector para encasillar cada byte de la trama y asi identifcarlo byte x byte
      //luego de tener la trama desglosada byte x byte continuamos a interpretarla
      if (cadenahex.length() > 0) {
        //System.out.println("LLega dato");
        interpretaCadena(cadenahex);
      }

    } catch (Exception e) {

      e.printStackTrace();
      //System.err.println(e.getMessage());
    }
  }

  private void interpretaCadena(String cadenahex) throws Exception {
    rutinaCorrecta = true;
    enviando = false;
    reenviando = false;
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
    }

    escribir("<= " + cadenahex);
    //System.out.println("<= " + cadenahex);
    String[] vectorhex = cadenahex.split(" ");
//    if (rcom){
//        //lARRQ = true;
//        iniciacomunicacion();
//    }
    
    if (lARRQ) {
      revisarAARQ(vectorhex);
    } else if (lpowerLost) {
      revisarPowerLost(vectorhex);
    } else if (lperfilEventos) {
      revisarEventos(vectorhex);
    } else if (lserial) {
      revisarSerial(vectorhex);
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
    } else if (lfechasync) {
      revisarConfHora(vectorhex);
    }
  }

  public boolean interpretaDatos(String[] trama, int tipoTrama) {
    vlec = new Vector<Electura>();
    boolean revisar, almacenar, detNumCanales, correcto;
    correcto = true;
    int k, mod;
    detNumCanales = false;
    String cadenaTemp;
    String cadenas[] = new String[10];
    i1 = j1 = k = l1 = mod = 0;
    if (tipoTrama == 5 || tipoTrama == 22 || tipoTrama == 23 || tipoTrama == 24 || tipoTrama == 25) { 
      detNumCanales = true;
    } else {
      mod = 4;
    }
    //System.out.println("il antes de clasificar: "+i1);
    if (clasificarTrama(trama)) {
      while (i1 < trama.length) {
        while (pila[0] > 0) {
//          if (clasificarTrama(trama)) {
            almacenar = false;
            if (j1 == 10) {
              //System.out.println("Error en la Pila de datos");
              //System.out.println("----------------------------");
              correcto = false;
              break;
            }
            if (revisarIndice(i1, trama.length)) {
                
////                System.out.println(Short.parseShort(trama[i1], 16));
////                System.out.println(Short.parseShort(trama[i1+1], 16));
////                System.out.println(Integer.parseInt(trama[i1 + 1], 16));
                //si el canal es nulo muestra un byte 0
              if (Short.parseShort(trama[i1], 16) == 1 || Short.parseShort(trama[i1], 16) == 2) {// || Integer.parseInt(trama[i1 + 1], 16) >= 128) {
                revisar = false;
                //Integer.parseInt(trama[i1 + 1], 16) >= 128 lenght negativo
//                if (Short.parseShort(trama[i1], 16) == 0){
////                    revisar = true;
//                    
//                }
              } else {
                revisar = true;
              }
              i1++;
              
////              System.out.println(revisar);
////              System.out.println("il antes de aumentaPila: "+i1);
////              System.out.println("jl antes de aumentaPila: "+j1);
              
              if (!aumentaPila(trama)) {
                correcto = false;
                //System.out.println("correcto: "+correcto);
                break;
              }
              if (revisar) {
                cadenaTemp = "";
////                System.out.println("antes del while pila "+j1+"menos 1 es: "+pila[j1 - 1]);
                while (pila[j1 - 1] > 0) {
////                    System.out.println("while pila "+j1+" menos 1 es: "+pila[j1 - 1]);
                  if (revisarIndice(i1, trama.length)) {
                      //System.out.println("trama[il]: "+trama[i1]);
                    cadenaTemp = cadenaTemp + trama[i1];
                    i1++;
                    pila[j1 - 1]--;
                  } else {
                    correcto = false;
                    break;
                  }
                }
                if (!correcto) {
                  break;
                }
                j1--;
                if (cadenaTemp == ""){
                 cadenas[k] = "00"; //cuando el dato fue nulo
                }else{
                cadenas[k] = cadenaTemp;
                }
                if (!procesaDato(cadenas[k], tipoTrama)) {
                  correcto = false;
                }
                k = (++k) % 10;
////                System.out.println("k es");
////                System.out.println(k);
                pila[j1 - 1]--;
                l1++;//tefa
              }
              while (j1 != 1 && pila[j1 - 1] == 0) {
                j1--;
                pila[j1 - 1]--;
                if (pila[j1 - 1] != 0 && revisar) {
                  revisar = false;
                  almacenar = true;
                }
              }             
              if (almacenar) {
                //System.out.println("----------------------------");
                if (detNumCanales) {
                  l1 = 0;
                }
              }
            } else {
              correcto = false;
              break;
            }
//          } else {
//            correcto = false;
//            break;
//          }
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
    } else {
      //System.out.println("Proceso completado con errores\n");
    }
    return correcto;
  }

  public boolean aumentaPila(String[] trama) {
    short[] devLong = {0, 0, 0, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 2, 0, 8, 8, 1, 4, 8};
////    System.out.println("i1 en aumentaPila");
////    System.out.println(i1);
    
    short temp = devLong[Integer.parseInt(trama[i1 - 1], 16)];
////    System.out.println("temp en aumentaPila");
////    System.out.println(temp);
////    System.out.println("trama[i1 - 1] en aumentaPila");
////    System.out.println(Integer.parseInt(trama[i1 - 1], 16));
    int longi = Integer.parseInt(trama[i1], 16);
    String tramaTemp = "";
    if (temp == 0) {
        
      if (revisarIndice(i1, trama.length)) {
        if (Integer.parseInt(trama[i1 - 1], 16)==0){
////            System.out.println("el dato es nulo");
            pila[j1] = temp;
////            System.out.println("pila en ");
            //System.out.println(j1);
////            System.out.println("es ");
            //System.out.println(pila[j1]);
            i1--;
        } else if (longi < 128) {
          if (Integer.parseInt(trama[i1 - 1], 16) == 4) {
            pila[j1] = (short) Math.ceil(longi / 8.0);
          } else {
            pila[j1] = Short.parseShort(trama[i1], 16);
          }
        } else {
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
////          System.out.println("pila en ");
////          System.out.println(j1);
////          System.out.println("es ");
////          System.out.println(pila[j1]);
          i1 = i1 + longi;
        }
      } else {
        return false;
      }
    } else {
      pila[j1] = temp;
////      System.out.println("pila en ");
      //System.out.println(j1);
////      System.out.println("es ");
      //System.out.println(pila[j1]);
      i1--;
    }
    i1++;
    j1++;
////    System.out.println("il al salir de aumenta pila: "+i1);
////    System.out.println("jl al salir de aumenta pila: "+j1);
    return true;
  }

  public boolean clasificarTrama(String[] trama) {

        if (i1 + 3 < trama.length) { //tipo de trama e invoke_id and priority
//          i1 = i1 + 3;
////            System.out.println(i1);
////            System.out.println(Short.parseShort(trama[i1], 16));
          switch (Short.parseShort(trama[i1], 16)) {
            case 97: {//no es necesario
              //System.out.println("AARE APDU (No esta realizado aun)\n----------------------------");
              return false;
            }
//            break;
            case 196: {
              switch (Short.parseShort(trama[i1 + 1], 16)) {
                case 1: {
                  //System.out.println("Get Response Normal \n----------------------------");
                  if (i1 + 3 < trama.length) {
                    i1 = i1 + 3;
                    //System.out.println(i1);
                  } else {
                    //System.out.println("----------------------------");
                    //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                    //System.out.println("----------------------------");
                    return false;
                  }

                  if (j1 == 0) {
                    pila[j1] = 1;
                    j1++;
                  }

                  return operaReadResponse(trama);
                }
                
                case 2: {
                  //System.out.println("Get Response With Data Block \n----------------------------");
                  if (i1 + 12 < trama.length) {
                    i1 = i1 + 12;
                  } else {
                    //System.out.println("----------------------------");
                    //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                    //System.out.println("----------------------------");
                    return false;
                  }
                  if (j1 == 0) {
                    pila[j1] = 1;
                    j1++;
                  }
                  return true;
                }

                default: {
                  //System.out.println("Unknown Response Code \n----------------------------");
                }

              }

              return false;
            }
            case 197: {
              switch (Short.parseShort(trama[i1 + 1], 16)) {
                case 1: {
                  //System.out.println("Set Response Normal \n----------------------------");
                  if (i1 + 3 < trama.length) {
                    i1 = i1 + 3;
                    //System.out.println(i1);
                  } else {
                    //System.out.println("----------------------------");
                    //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                    //System.out.println("----------------------------");
                    return false;
                  }

                  if (j1 == 0) {
                    pila[j1] = 1;
                    j1++;
                  }

                  return operaReadResponse(trama);
                }
                default: {
                  //System.out.println("Unknown Response Code \n----------------------------");
                }

              }

              return false;
            }
                
            case 199: {
              switch (Short.parseShort(trama[i1 + 1], 16)) {
                case 1: {
                  //System.out.println("Action Response Normal \n----------------------------");
                  if (i1 + 3 < trama.length) {
                    i1 = i1 + 3;
                    //System.out.println(i1);
                  } else {
                    //System.out.println("----------------------------");
                    //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
                    //System.out.println("----------------------------");
                    return false;
                  }

                  if (j1 == 0) {
                    pila[j1] = 1;
                    j1++;
                  }

                  return operaReadResponse(trama);
                }
                default: {
                  //System.out.println("Unknown Response Code \n----------------------------");
                }

              }

              return false;
            }

                
//            break;
            default: {
              //System.out.println("Codigo xDLMS-APDU Erroneo \n----------------------------");
              return false;
            }
//            break;
          }

        } else {
          //System.out.println("----------------------------");
          //System.out.println("Trama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados");
          //System.out.println("----------------------------");
          return false;
        }

//    return true;
  }

  public boolean operaReadResponse(String trama[]) {
    if (j1 == 1 && revisarIndice(i1, trama.length)) {
      switch (Short.parseShort(trama[i1], 16)) {
        case 0: {
          i1++;
          //System.out.println("i1 en operaReadResponse");
          //System.out.println(i1);
          return true;
        }
        case 1: {
          //System.out.println("Error al Acceder a los Datos\n----------------------------");
          i1++;
          revisarErrorCode(trama);//verificar codigo de error
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
          revisarErrorCode(trama);//verificar codigo de error
          return false;
        }
      }
    } else {
      return false;
    }
  }
  
  public void revisarErrorCode(String trama[]) {
      switch (Short.parseShort(trama[i1], 16)) {
        case 0: {
          //System.out.println("Success error\n----------------------------");
          avisoStr("Success");
          escribir("Success error");
          break;
        }
        case 1: {
          //System.out.println("Hardware fault error\n----------------------------");
          avisoStr("Hardware fault");
          escribir("Hardware fault error");
          break;
        }
        case 2: {
          //System.out.println("temporary-failure error\n----------------------------");
          avisoStr("temporary-failure");
          escribir("temporary-failure error");
          break;
        }
        case 3: {
          //System.out.println("read-write-denied error\n----------------------------");
          avisoStr("read-write-denied");
          escribir("read-write-denied error");
          break;
        }
        case 4: {
          //System.out.println("object-undefined error\n----------------------------");
          avisoStr("object-undefined");
          escribir("object-undefined error");
          break;
        }
        case 9: {
          //System.out.println("object-class-inconsistent error\n----------------------------");
          avisoStr("object-class-inconsistent");
          escribir("object-class-inconsistent error");
          break;
        }
        case 11: {
          //System.out.println("object-unavailable error\n----------------------------");
          avisoStr("object-unavailable");
          escribir("object-unavailable error");
          break;
        }
        case 12: {
          //System.out.println("type-unmatched error\n----------------------------");
          avisoStr("type-unmatched");
          escribir("type-unmatched error");
          break;
        }
        case 13: {
          //System.out.println("scope-of-access-violated error\n----------------------------");
          avisoStr("scope-of-access-violated");
          escribir("scope-of-access-violated error");
          break;
        }
        case 14: {
          //System.out.println("data-block-unavailable error\n----------------------------");
          avisoStr("data-block-unavailable");
          escribir("data-block-unavailable error");
          break;
        }
        case 15: {
          //System.out.println("long-get-aborted error\n----------------------------");
          avisoStr("long-get-aborted");
          escribir("long-get-aborted error");
          break;
        }
        case 16: {
          //System.out.println("no-long-get-in-progress error\n----------------------------");
          avisoStr("no-long-get-in-progress");
          escribir("no-long-get-in-progress error");
          break;
        }
        case 17: {
          //System.out.println("long-set-aborted error\n----------------------------");
          avisoStr("long-set-aborted");
          escribir("long-set-aborted error");
          break;
        }
        case 18: {
          //System.out.println("no-long-set-in-progress error\n----------------------------");
          avisoStr("no-long-set-in-progress");
          escribir("no-long-set-in-progress error");
          break;
        }
        case 19: {
          //System.out.println("data-block-number-invalid error\n----------------------------");
          avisoStr("data-block-number-invalid");
          escribir("data-block-number-invalid error");
          break;
        }
        case 250: {
          //System.out.println("other-reason error\n----------------------------");
          avisoStr("other-reason");
          escribir("other-reason error");
          break;
        }
        default: {
          //System.out.println("Valor de error indefinido\n----------------------------");
          break;
        }
      }
      //reiniciar conexion y enviar tramas de nuevo
  }
  
  //datos para el perfil
  SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  Electura lec;
  public int indicecanal = 0;
  public int powerlosttime=0;
  Date fechaEvento = null;
  Timestamp fechaintervalo = null;
  Timestamp ultimoIntervalo = null;
  Timestamp fechaCero = null;
  EConstanteKE econske = null;
  String canal = "";
  Vector<Electura> vlec;
  Vector<EConstanteKE> lconske;//se toman los valores de las constantes 
  ArrayList<EtipoCanal> vtipocanal;

  public boolean procesaDato(String dato, int tipoTrama) {

    switch (tipoTrama) {
      case 2: {
        dato = Hex2ASCII(dato);
        //System.out.println(dato);
        datoserial = dato;
        break;
      }
      case 3: {
        try {
          fechaActual = fecha.parse(Hex2Date(dato));
          dato = sdf3.format(fechaActual);

        } catch (Exception e) {
        }
        //System.out.println("Fecha actual " + dato);
        break;
      }
      case 4: {
        try {
          periodoIntegracion = Integer.parseInt(dato, 16) / 60;
          //System.out.println("\nPeriodo de Integración " + periodoIntegracion);
          intervalo = periodoIntegracion;

        } catch (Exception e) {
        }
        break;
      }

      case 5: {
        if (l1 == 0) {
          clase.add(dato);
          //System.out.println("\nClase " + dato);
        }
        if (l1 == 1) {
          obis.add(dato);//.replaceAll("F", ""));
          //System.out.println("\nObis " + dato);
        }
        //l1++;
        break;
      }
      case 20: {
        if (l1 == 0) {
          conskePerfil.add(dato);
          //System.out.println("\nEscala " + dato);
        }
        if (l1 == 1) {
          unidad.add(dato);
          //System.out.println("\nUnidad " + dato);
        }
        break;
      }
      case 22: {
          
        if (l1 == 0) {
          indicecanal = 0;
          try {
              if (dato == "00"){
                 //dato es la fecha anterior mas el periodo de integracion
                  //System.out.println("ultimoIntervalo " + ultimoIntervalo);
                 long milisUI=ultimoIntervalo.getTime();
                 //System.out.println("ultimoIntervalo en milis " + milisUI);
                 fechaintervalo = new Timestamp(ultimoIntervalo.getTime()+(long) (periodoIntegracion*60*1000));
                  //System.out.println("fechaintervalo en nulo " + fechaintervalo);
              }else {
            ////System.out.println(" indicecanal " + indicecanal);
            //System.out.println("dato original " + dato);
            dato = sdf2.format(fecha.parse(Hex2Date(dato)));
            //System.out.println("Fecha intervalo " + dato);
            fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
              }
            if (fechaintervalo.getMinutes() % intervalo != 0) {
              dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
              fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
              //System.out.println("Fecha intervalo aproximado" + dato);
            }
            if (ultimoIntervalo == null) {
              ultimoIntervalo = fechaintervalo;
            }
            //manejo de huecos
            int aumento = 0;
            fechaCero = null;
            try {
              if (ultimoIntervalo != null) {//si tiene una fecha inicial
                aumento = (int) Math.abs(((fechaintervalo.getTime() - ultimoIntervalo.getTime()) / 60000) / intervalo) - 1;//se calcula si el intervalo actual es superior e 1 intervalo de integracion 
                if (aumento > 0) {//obtiene el numero de intervalos a mover
                  for (int i = 0; i < aumento; i++) {
                    fechaCero = new Timestamp(ultimoIntervalo.getTime() + (60000 * intervalo) * (i + 1));//movemos la fecha por cada intervalo faltante
                    //System.out.println("fecha intervalo en 0 " + fechaCero);
                    for (int k = 0; k < obis.size(); k++) {//se recorren la cantidad de canales programados en el medidor
                      try {
                        econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(k), 16)); //buscamos el valor de la constante creada en telesimex
//                        econske = cp.buscarConske(lconske, Integer.parseInt(obis.get(k),16)); //buscamos el valor de la constante creada en telesimex
                        if (econske != null) {
                          canal = "";
                          for (EtipoCanal et : vtipocanal) {//buscamos la unidad del canal
//                              if (Integer.parseInt(et.getCanal(),16) == Integer.parseInt(obis.get(k), 16)) { //
                            if (Long.valueOf(et.getCanal()) == Long.parseLong(obis.get(k), 16)) { //
                              canal = et.getUnidad();
                              break;
                            }
                          }
                          if (canal.length() > 0) {
                            lec = new Electura(fechaCero, med.getnSerie(), Long.parseLong(obis.get(k), 16), 0.0, 0, intervalo, canal);//lectura del canal en 0                                                    
                            vlec.add(lec);
                          } else {
                            //System.out.println("Constante");
                          }

                        }
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                    }
                  }
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            ultimoIntervalo = fechaintervalo;

          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {

          indicecanal++;
////          System.out.println(" indicecanal " + indicecanal);
          try {
            dato = String.valueOf(Integer.parseInt(dato, 16));
            //System.out.println(dato);
////            System.out.println("!!lconske: "+ lconske);
////            System.out.println("Canal " + Long.parseLong(obis.get(indicecanal), 16));
            econske = cp.buscarConskeLong(lconske, Long.parseLong(obis.get(indicecanal),16));
////            System.out.println("econske: "+ econske);
            if (econske != null) {
              //tiene la constante
              //// System.out.println("Constante encontrada " + econske.getCanal2());
              canal = "";
              for (EtipoCanal et : vtipocanal) {
////                System.out.println("Canal ISKRA " + et.getCanal());
////                System.out.println("OBIS " + Long.parseLong(obis.get(indicecanal), 16));
                if (Long.valueOf(et.getCanal()) == Long.parseLong(obis.get(indicecanal), 16)) {
                  canal = et.getUnidad();
                  //System.out.println("Canal " + et.getCanal() + " Unidad " + canal);
                  break;
                }
              }
              if (fechaintervalo != null && canal.length() > 0) {
                //System.out.println("valor " + trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                //System.out.println("Unidad " + unidad.get(indicecanal));
                ////System.out.println("Complemento a 2 ="+ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF)));
                lec = new Electura(fechaintervalo, med.getnSerie(), Long.parseLong(obis.get(indicecanal), 16), trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, ActarisComplemento2((byte) (Integer.parseInt(conskePerfil.get(indicecanal), 16) & 0xFF))), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(dato), intervalo, canal);
//                lec.lec = lec.lec;
//                if (Integer.parseInt(unidad.get(indicecanal), 16) == 30 || Integer.parseInt(unidad.get(indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
//                  lec.lec = lec.lec / 1000.0;
//                } //no se guarda en kilos
                vlec.add(lec);
              }
            }else{
                //System.out.println("econske null ");
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

        }

        break;
      }
      case 24: {
        
            if (l1 == 0) {
              try {
                if (regEvento == null) {
                  regEvento = new ERegistroEvento();
                  regEvento.vcserie = seriemedidor;
                  regEvento.vctipo = "0001";
                  regEvento.tiempocorte=0;

                }

                fechaEvento = fecha.parse(Hex2Date(dato));
                regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                regEvento.vcfechareconexion=regEvento.vcfechacorte;
              } catch (Exception e) {
              }
            } else if (l1 == 1) {
              try {
                  powerlosttime=Integer.parseInt(dato, 16);
                  regEvento.tiempocorte=powerlosttime;

                if (regEvento.vcfechacorte != null && regEvento.tiempocorte != 0) {

                  //System.out.println(fecha.format(regEvento.vcfechacorte) + ", " + regEvento.tiempocorte);
                  avisoStr("Almacenando Evento");
                  cp.almacenaEvento(regEvento, null);
//                  cp.actualizaEvento(regEvento, null);
                  listRegEventos.add(regEvento);
                  regEvento = new ERegistroEvento();
                  regEvento.vcserie = seriemedidor;
                  regEvento.vctipo = "0001";
                  regEvento.tiempocorte=0;

                }

              } catch (Exception e) {
              }
            }
            
            
        //l1++;
        break;
      }
      case 25: {
                   
            if (l1 == 0) {
              try {
                if (regEvento == null) {
                  regEvento = new ERegistroEvento();
                  regEvento.vcserie = seriemedidor;
                  regEvento.vctipo = "0005";
                  
                }
                regEvento.tiempocorte=0;
                fechaEvento = fecha.parse(Hex2Date(dato));
                regEvento.vcfechacorte = new java.sql.Timestamp(fechaEvento.getTime());
                regEvento.vcfechareconexion=regEvento.vcfechacorte;

              } catch (Exception e) {
              }
            } else if (l1 == 1) {
              if (dato.equals("0040")){
              regEvento.vctipo = "0001"; //el 0040 tambien se toma como un Power Lost"
              }else if (dato.equals("0001")){
              regEvento.vctipo = "0003"; //en el MT880 0001 es "Fatal Error"
              }
              try {
//                eventCode = regEvento.getEventcode(dato);// fecha.parse(Hex2Date(dato));
                regEvento.vctipo = dato; //guardar el eventcode en vctipo
//                regEvento.eventcode=eventCode;
                if (regEvento.vcfechacorte != null && regEvento.vctipo != null) {

                  //System.out.println(fecha.format(regEvento.vcfechacorte) + ", "+regEvento.vctipo);
                  avisoStr("Almacenando Evento");
                  cp.almacenaEvento(regEvento, null);
//                  cp.actualizaEvento(regEvento, null);
                  listRegEventos.add(regEvento);
                  regEvento = new ERegistroEvento();
                  regEvento.vcserie = seriemedidor;
                  regEvento.vctipo = "0005"; //evento desconocido
                  regEvento.tiempocorte=0;                  
                }
              } catch (Exception e) {
              }
            }
                        
        //l1++;
        break;
      }
      default: {
        //System.out.println("No es posible interpretar los datos");
        return false;
      }
    }
    return true;
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

  public boolean revisarIndice(int indice, int longTrama) {
    if (indice == (longTrama + bytesRecortados)) {
      //System.out.println("----------------------------\nTrama incompleta. Los datos recibidos que no hayan sido almacenados seran descartados\n----------------------------");
      return false;
    } else {
      return true;
    }
  }

  public int ActarisComplemento2(byte b) {
    int c = 0;
    if (b != 0) {
      if ((b & 0x80) != 0) {
        c = -1 * (~b + 1);
      } else {
        c = b;
      }
    }
    return c;
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

  public void revisarAARQ(String[] vectorhex) {
    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxAARQ = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxAARQ, 16)) {
        if ((Integer.parseInt(tamauxAARQ, 16) + 8) <= vectorhex.length) {

                  if (vectorhex[25].equals("00") && vectorhex[32].equals("00")) {
                      //System.out.println("SUCCESS");
                      lARRQ = false;
                      lserial = true;
                      byte trama[] = tramastcum.getSerial();

                      trama = ajustarWrapper(trama);
                      ultimatramaEnviada = trama;
                      avisoStr("Serial");
                      enviaTrama2(trama, "=> Serial");
                      
                  } else {
                    //reiniciamos
                    if (vectorhex[32].equals("0D")) {
                      //System.out.println("error de autenticacion");
                      indxuser++;
                      lARRQ = false;
                        procesarDatos();
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion Error en identificacion de usuario");
                        cerrarPuerto();
                        avisoStr("No Leido");
                        escribir("Desconexion - Error de autenticacion");
                        cerrarLog("Desconexion error de identificacion", false);
                        leer = false;
                    } else {
                        //System.out.println("Error de protocolo - Fallo en interpretacion de AARE");
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion - Fallo en interpretacion de AARE");
                        cerrarPuerto();
                        escribir("Desconexion - Fallo en interpretacion de AARE");
                        cerrarLog("Fallo en interpretacion de AARE", false);
                        leer = false;
                    }
                  }
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud ARRQ");
    }
  }

  public void revisarSerial(String[] vectorhex) {
    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxS = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera 0 1 0 1 0 1 length
      if (vectorhex.length >= Integer.parseInt(tamauxS, 16)) {
        if ((Integer.parseInt(tamauxS, 16) + 8) <= vectorhex.length) {

                    lserial = false;
                    try {
                      String[] vectorData = sacarDatos(vectorhex, 8); //seguido al length
                      boolean continuar;
                      interpretaDatos(vectorData, 2);
                      datoserial = "" + Long.parseLong(datoserial);
                      //System.out.println("Serial obtennido " + datoserial);
                      //System.out.println("Serial Medidor " + seriemedidor);
                      if (seriemedidor.equals(datoserial)) {
                        escribir("Numero serial " + datoserial);
                        continuar = true;
                      } else {
                        escribir("Numero serial incorrecto");
                        continuar = false;
                      }

                      if (continuar) {
                        lfechaactual = true;
                        byte trama[] = tramastcum.getFechaactual();
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        avisoStr("Fecha Actual");
                        enviaTrama2(trama, "=> Solicitud de fecha actual");
                      } else {
                        escribir("Numero de serial invalido");
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion serial incorrecto");
                        procesarDatos();
                      }

                    } catch (Exception e) {
                      escribir("Error al validar el numero serial");
                      cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion Error de serial");
                      reiniciaComuniacion();

                    }
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Serial number");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Serial number");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Serial number");
    }
  }

    public void revisarFecAct(String[] vectorhex) {
        if (complemento) {
            complemento = false;
            tramaIncompleta = tramaIncompleta + " " + cadenahex;
            vectorhex = tramaIncompleta.split(" ");
        }
        String tamauxPI = vectorhex[6] + vectorhex[7]; //para verificar bien el length
        if (vectorhex.length > 7) {//tiene cabecera
            if (vectorhex.length >= Integer.parseInt(tamauxPI, 16)) {
                if ((Integer.parseInt(tamauxPI, 16) + 2) <= vectorhex.length) {
                    try {
                        String[] data = sacarDatos(vectorhex, 8);
                        if (interpretaDatos(data, 3)) {
                            lfechaactual = false;
//                        if(!rcom){
                            if (lconfhora) {
                                //**** conf hora
                                try {
                                    byte trama[] = tramastcum.getConfhora();
                                    avisoStr("Sync Reloj");
                                    time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                                    //System.out.println("Sincronizando hora con Zona horaria ");
                                    //System.out.println("Fecha " + time);

                                    String fecha = sdf.format(new Date(time.getTime()));
                                    //System.out.println("Fecha a actualizar " + fecha);
                                    String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                                    int v = 0;
                                    while (lfecha.length() < 4) {
                                        lfecha = "0" + lfecha;
                                        //System.out.println("lfecha " + v + ": " + lfecha);
                                        v++;
                                    }
                                    //System.out.println("fecha: " + fecha);
                                    trama[23] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                                    trama[24] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);// año en 2 bytes
                                    trama[25] = (byte) (Integer.parseInt(fecha.substring(4, 6)) & 0xFF);// mes 
                                    trama[26] = (byte) (Integer.parseInt(fecha.substring(6, 8)) & 0xFF);//dia
                                    trama[27] = (byte) (((time.getDay()) == 0 ? 7 : (time.getDay())) & 0xFF);// dia de la semana
                                    //trama[33] = (byte) (((time.getDay()-1)==0? 7:(time.getDay()-1)) & 0xFF);// dia de la semana
                                    //System.out.println("A");
                                    trama[28] = (byte) (Integer.parseInt(fecha.substring(8, 10)) & 0xFF); // hora 
                                    trama[29] = (byte) (Integer.parseInt(fecha.substring(10, 12)) & 0xFF); // min
                                    trama[30] = (byte) (Integer.parseInt(fecha.substring(12, 14)) & 0xFF); // seg
                                    trama[31] = (byte) 0xFF; // centesimas
                                    trama[32] = (byte) 0x80;
                                    trama[33] = (byte) 0x00; // desviacion 2 bytes
                                    trama[34] = (byte) 0x00; // status

                                    //System.out.println("B");

                                    lfechaactual = false;
                                    lfechasync = true;
                                    //System.out.println("C");
                                    trama = ajustarWrapper(trama);

                                    ultimatramaEnviada = trama;
                                    avisoStr("Configuracion de hora");
                                    //System.out.println("D");

                                    tsfechaactual = new Timestamp(fechaActual.getTime());
                                    //System.out.println("D2" + sdf3.format(new Date(tsfechaactual.getTime()))); //cual debe ser

//                                  cp.saveAcceso(med.getnSerie(), "2", sdf3.format(new Date(tsfechaactual.getTime())), sdf3.format(new Date(time.getTime())), usuario, null);
                                    //System.out.println("E");
                                    enviaTrama2(trama, "=> Configuracion de hora " + sdf3.format(new Date(time.getTime())));
                                    //System.out.println("envia trama 2");
                                } catch (Exception e) {
                                    escribir("Error en obtencion de ZID");
                                    cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion error de ZID");
                                    reiniciaComuniacion();
                                }

                            } else if (lperfil) {
                                lperiodoIntegracion = true;
                                byte trama[] = tramastcum.getPeriodoint();
                                trama = ajustarWrapper(trama);
                                ultimatramaEnviada = trama;
                                avisoStr("Periodo Integraccion");
                                enviaTrama2(trama, "=> Solicitud de periodo de integracion");
                            } else if (leventos) {
                                lpowerLost = true;
                                primerbloque = true;
                                listEventos = new ArrayList<>();
                                byte trama[] = tramastcum.getPowerLost();
                                trama = ajustarWrapper(trama);
                                ultimatramaEnviada = trama;
                                avisoStr("Power Lost Event");
                                enviaTrama2(trama, "=> Solicitud de eventos");
                            } else {
                                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Leido sin opciones");
                                cerrarPuerto();
                                avisoStr("Leido");
                                escribir("Leido");
                                cerrarLog("Leido", true);
                                //System.out.println("leer = false; en revisarFecAct");
                                leer = false;
                            }
//                      } //if rcom
                        } else {
                            escribir("Error en obtencion de fecha");
                            cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion error de fecha");
                            reiniciaComuniacion();
                        }
                    } catch (Exception e) {
                        escribir("Error en obtencion de fecha");
                        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion error de fecha");
                        reiniciaComuniacion();
                    }
                } else {
                    //trama incompleta
                    //System.out.println("Incompleta");
                    escribir("Error trama incompleta");
                    complemento = true;
                    tramaIncompleta = cadenahex;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
                }
            } else {
                //trama incompleta
                //System.out.println("Incompleta");
                escribir("Error trama incompleta");
                complemento = true;
                tramaIncompleta = cadenahex;
                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
            }
        } else {//trama incompleta
            //System.out.println("Sin cabecera");
            escribir("Error trama Sin cabecera");
            complemento = true;
            tramaIncompleta = cadenahex;
            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
        }
    }

  public void revisarPeriodoInt(String[] vectorhex) {

    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxPI = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxPI, 16)) {
        if ((Integer.parseInt(tamauxPI, 16) + 2) <= vectorhex.length) {

                    lperiodoIntegracion = false;
                    String[] data = sacarDatos(vectorhex, 8);
                    if (interpretaDatos(data, 4)) {
                      linfoperfil = true;
                      obis = new ArrayList<String>();
                      conskePerfil = new ArrayList<String>();
                      unidad = new ArrayList<String>();
                      clase = new ArrayList<String>();
                      infoPerfil = new Vector<String>();
                      byte trama[] = tramastcum.getConfperfil();
//                      trama = asignaDireciones(trama, dirlog);
//                      trama[5] = I_CTRL(nr, ns);
//                      trama = calcularnuevocrcI(trama);
                      trama = ajustarWrapper(trama);
                      ultimatramaEnviada = trama;
                      avisoStr("Configuracion del perfil");
                      enviaTrama2(trama, "=> Solicitud de configuracion del perfil de carga");
                    } else {
                      escribir("Negacion de peticion");
                      cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion negacion de peticion");
                      reiniciaComuniacion();
                    }
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Periodo Integracion");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Periodo Integracion");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Periodo Integracion");
    }
  }

  public void revisarInfoPerfil(String[] vectorhex) {

    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxIP = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxIP, 16)) {
        if ((Integer.parseInt(tamauxIP, 16) + 2) <= vectorhex.length) {

                    String vdata[] = sacarDatos(vectorhex, 8);
                    infoPerfil.addAll(Arrays.asList(vdata));
                    if (vectorhex[9].equals("01")) { //sin bloque
                      procesaInfoPerfil();
                        escribir("Cantidad de canales internos del medidor: "+obis.size());
                        escribir("Vector de canales internos del medidor: " + obis.toString());
                      linfoperfil = false;
                      lconstants = true;
                      indexConstant = 0;
                      while (!clase.get(indexConstant).equals("0003")) {
                        conskePerfil.add("0");
                        unidad.add("0");
                        indexConstant++;
                      }
                      byte[] trama = construirConstant(obis.get(indexConstant));
                      trama = ajustarWrapper(trama);
                      ultimatramaEnviada = trama;
                      avisoStr("Constantes");
                      enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
                    } else {//con bloques
                      if (vectorhex[14].equals("00")) {//no es el ultimo bloque por lo tanto se solicitan los siguientes bloques.
                        byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[15], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[16], 16) & 0xFF),
                          (byte) (Integer.parseInt(vectorhex[17], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[18], 16) & 0xFF)};

                        byte trama[] = crearREQ_NEXT(bloqueRecibido);
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "=> Envia Next Data Block Request");
                      } else {
                        procesaInfoPerfil();
                        escribir("Cantidad de canales internos del medidor: "+obis.size());
                        escribir("Vector de canales internos del medidor: " + obis.toString());
                        linfoperfil = false;
                        lconstants = true;
                        indexConstant = 0;
                        while (!clase.get(indexConstant).equals("0003")) {
                          conskePerfil.add("0");
                          unidad.add("0");
                          indexConstant++;
                        }
                        byte[] trama = construirConstant(obis.get(indexConstant));
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        avisoStr("Constantes");
                        enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
                      }
                    }
                              
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
    }
  }

    public void revisarConstants(String[] vectorhex) {

        if (complemento) {
            complemento = false;
            tramaIncompleta = tramaIncompleta + " " + cadenahex;
            vectorhex = tramaIncompleta.split(" ");
        }
        String tamauxC = vectorhex[6] + vectorhex[7]; //para verificar bien el length
        if (vectorhex.length > 7) {//tiene cabecera
            if (vectorhex.length >= Integer.parseInt(tamauxC, 16)) {
                if ((Integer.parseInt(tamauxC, 16) + 2) <= vectorhex.length) {

                    String vdata[] = sacarDatos(vectorhex, 8);
                    interpretaDatos(vdata, 20);
                    indexConstant++;
                    if (indexConstant > (obis.size() - 1)) {

                        time = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                        //System.out.println("Sincronizando hora con servidor");
                        escribir("Sincronizando hora con zona horaria");
                        escribir("Fecha ZID" + time);
                        //System.out.println("max desfase permitido" + ndesfasepermitido);
                        //System.out.println("Fecha ZID" + time);

                        deltatimesync1 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                        //System.out.println(" Vector constantes " + conskePerfil.toString());
                        lconstants = false;
                        lfechaactual2 = true;
                        byte trama[] = tramastcum.getFechaactual();
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        avisoStr("Fecha Actual");
                        enviaTrama2(trama, "=> Solicitud de fecha actual");
                    } else {
                        //se solicita el siguiente obis
                        while (!clase.get(indexConstant).equals("0003")) {
                            conskePerfil.add("0");
                            unidad.add("0");
                            indexConstant++;
                        }
                        byte[] trama = construirConstant(obis.get(indexConstant));
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        enviaTrama2(trama, "=> Solicitud de constantes " + (indexConstant + 1));
                    }

                } else {
                    //trama incompleta
                    //System.out.println("Incompleta");
                    escribir("Error trama incompleta");
                    complemento = true;
                    tramaIncompleta = cadenahex;
                    enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
                }
            } else {
                //trama incompleta
                //System.out.println("Incompleta");
                escribir("Error trama incompleta");
                complemento = true;
                tramaIncompleta = cadenahex;
                enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
            }
        } else {//trama incompleta
            //System.out.println("Sin cabecera");
            escribir("Error trama Sin cabecera");
            complemento = true;
            tramaIncompleta = cadenahex;
            enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
        }
    }

  public void revisarFecAct2(String[] vectorhex) {
    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxFA = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxFA, 16)) {
        if ((Integer.parseInt(tamauxFA, 16) + 2) <= vectorhex.length) {

                    
                    deltatimesync2 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                    String[] dataTrama = sacarDatos(vectorhex, 8);
                    if (interpretaDatos(dataTrama, 3)) {
                      escribir("Fecha actual de medidor " + fechaActual);
                      //System.out.println("Fecha actual de medidor " + fechaActual);
                      deltatimesync2 = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
                      escribir("Fecha actual de medidor " + fechaActual);
                      //System.out.println("Fecha actual de medidor " + fechaActual);
                      lfechaactual2 = false;
                      if (lperfil) {//se solicita perfil de carga1
                        try {
                          escribir("Fecha actual Medidor " + fechaActual.toString());
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
                        }
                        solicitar = true;
                        try {
                          tsfechaactual = new Timestamp(fechaActual.getTime());
                          //System.out.println("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2) / 1000));
                          escribir("Delta time " + ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2)));
                          //System.out.println("Diferencia SEG ZID " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                          escribir("Diferencia SEG ZID " + Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000));
                          if (Math.abs((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000) > ndesfasepermitido) {
                            solicitar = false;
                            escribir("No se solicitara el perfil de carga");
                            //System.out.println("No se solicitara el perfil de carga");
                          }
                          cp.actualizaDesfase(((time.getTime() - tsfechaactual.getTime() - ((int) ((deltatimesync2.getTime() - deltatimesync1.getTime()) / 2))) / 1000), med.getnSerie(), null);
                        } catch (Exception e) {
                          e.printStackTrace();
                        }
                        if (solicitar) {
                          lperfilcarga = true;
                          tiempo = 1000 * timeout;
                          byte trama[] = tramastcum.getPerfilcarga();
                          //byte 47 fecha ini
//                          ndias=1; //VIC ojo
                          //System.out.println("Numero de dias " + ndias);
                          try {

                            String fecha = sdf.format(new Date((Calendar.getInstance().getTimeInMillis() - (86400000L * ndias))));
                            //System.out.println("Fecha inicio peticion pefil  " + sdf3.format(new Date(Date.from(ZonedDateTime.now(zid).toInstant()).getTime() - (86400000L * ndias))));
                            escribir("Fecha inicio peticion de perfil de carga " + fecha);
                            String lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                            while (lfecha.length() < 4) {
                              lfecha = "0" + lfecha;
                            }
                            //year
                            trama[44] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF); 
                            trama[45] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                            while (lfecha.length() < 2) {
                              lfecha = "0" + lfecha;
                            }
                            //month
                            trama[46] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                            while (lfecha.length() < 2) {
                              lfecha = "0" + lfecha;
                            }
                            //day
                            trama[47] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            //byte 61 fecha fin
                            //fecha final******************************************
                            fecha = sdf.format(new Date(Date.from(ZonedDateTime.now(zid).toInstant()).getTime() + (long) (86400000)  ));//+ (long) (86400000)));
                            //System.out.println("Fecha final peticion pefil  " + sdf3.format(new Date(Date.from(ZonedDateTime.now(zid).toInstant()).getTime() + (long) (86400000) ))  );// + (long) (86400000) )));
                            escribir("Fecha inicio final de perfil de carga " + fecha);
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(0, 4)));
                            while (lfecha.length() < 4) {
                              lfecha = "0" + lfecha;
                            }
                            //year
                            trama[58] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            trama[59] = (byte) (Integer.parseInt(lfecha.substring(2, 4), 16) & 0xFF);
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(4, 6)));
                            while (lfecha.length() < 2) {
                              lfecha = "0" + lfecha;
                            }
                            trama[60] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);
                            lfecha = Integer.toHexString(Integer.parseInt(fecha.substring(6, 8)));
                            while (lfecha.length() < 2) {
                              lfecha = "0" + lfecha;
                            }
                            trama[61] = (byte) (Integer.parseInt(lfecha.substring(0, 2), 16) & 0xFF);

                          } catch (Exception e) {
                            e.printStackTrace();
                          }
                          vPerfilCarga = new Vector<String>();
                          primerbloque = true;
                          trama = ajustarWrapper(trama);
                          ultimatramaEnviada = trama;
                          avisoStr("Perfil carga");
//                          long indexblock = 1;
                          enviaTrama2(trama, "Solicitud de pefil de carga");
                        } else {
                          escribir("Desfase horario no se solicitara el perfil de carga");
                          lfechaactual2 = false;
                          procesarDatos();

                        }
                      }
                    } else {
                      escribir("Negacion de peticion");
                      cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion negacion de peticion");
                      reiniciaComuniacion();
                    }

          
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud fecha actual");
    }
  }

  public void revisarPerfil(String[] vectorhex) {

    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxPC = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxPC, 16)) {
        if ((Integer.parseInt(tamauxPC, 16) + 2) <= vectorhex.length) {

                    String vdata[] = null;
                    if (primerbloque) {
                      vdata = sacarDatos(vectorhex, 8);
                      bytesRecortados = 7;
                      primerbloque = false;
                    } else {
                      int longi = Integer.parseInt(vectorhex[17], 16);
                      int recortar = 17; //era 20, pero en realidad son 17 bytes antes del ochentaypico
                      if (longi >= 128) {
                        recortar = recortar + (longi % 128);
                      }
                      bytesRecortados = bytesRecortados + recortar;
                      vdata = sacarDatos(vectorhex, recortar + 1);
                    }

                    vPerfilCarga.addAll(Arrays.asList(vdata));
                    if (vectorhex[9].equals("02")&vectorhex[11].equals("00")) { //verifica si la respuesta es por bloques y si es el ultimo
                      byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[12], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[13], 16) & 0xFF),
                        (byte) (Integer.parseInt(vectorhex[14], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[15], 16) & 0xFF)};

                      byte trama[] = crearREQ_NEXT(bloqueRecibido);
                      trama = ajustarWrapper(trama);
                      ultimatramaEnviada = trama;
                      enviaTrama2(trama, "=> Envia Next Data Block Request");
                    } else {
                        
                        lperfilcompleto = false;
                                                
                        if (leventos) {
                            try { //procesa antes de seguir con los eventos 
                            procesaDatos();
                            cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                            if (fechaintervalo != null) {
                              if (fechaintervalo.after(med.getFecha())) {
                                cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                              }
                            }

                          } catch (Exception e) {
                            e.printStackTrace();
                          }
                        lpowerLost = true;
                        listEventos = new ArrayList<>();
                        byte trama[] = tramastcum.getPowerLost();
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        avisoStr("Eventos");
                        enviaTrama2(trama, "=> Solicitud de eventos");
                      } else {
                        procesarDatos();
                      }
                        lperfilcarga = false;
                    }
                                     
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Periodo Integracion");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Periodo Integracion");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Periodo Integracion");
    }
  }

  public void revisarPowerLost(String[] vectorhex) throws ParseException {

    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxv = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxv, 16)) {
        if ((Integer.parseInt(tamauxv, 16) + 8) <= vectorhex.length) {

                    String vectorData[] = null;
                    
                    if (vectorhex[9].equals("02")&vectorhex[11].equals("00")) { //verifica tambien si la respuesta es por bloques
                        
                        if (primerbloque) {
                         vectorData = sacarDatos(vectorhex, 8);
                         bytesRecortados = 7;
                         primerbloque = false;
                       } else {
                         int longv = Integer.parseInt(vectorhex[17], 16);
                         int recortarv = 17; //era 20, pero en realidad son 17 bytes antes del ochentaypico
                         if (longv >= 128) {
                           recortarv = recortarv + (longv % 128);
                         }
                         bytesRecortados = bytesRecortados + recortarv;
                         vectorData = sacarDatos(vectorhex, recortarv + 1);
                       }
                        
                      listEventos.addAll(Arrays.asList(vectorData));
                        
                      byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[12], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[13], 16) & 0xFF),
                        (byte) (Integer.parseInt(vectorhex[14], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[15], 16) & 0xFF)};

                      byte trama[] = crearREQ_NEXT(bloqueRecibido);
                      trama = ajustarWrapper(trama);
                      ultimatramaEnviada = trama;
                      avisoStr("Power Lost Event");
                      enviaTrama2(trama, "=> Envia Next Data Block Request");
                    } else {
                        
                        vectorData = sacarDatos(vectorhex, 8);
                        listEventos.addAll(Arrays.asList(vectorData));
                        
                        
                        try {
                                procesaInfoEventos();
//                                cp.actualizaEvento(regEvento, null);
                            } catch (Exception e) {
                             }
                       
                        listEventos = new ArrayList<>();
                        lpowerLost = false;
                        lperfilEventos = true;
                        byte trama[] = tramastcum.getStandardEvent();
                        trama = ajustarWrapper(trama);
                        ultimatramaEnviada = trama;
                        avisoStr("Perfil de eventos 0");
                        enviaTrama2(trama, "=> Envia trama perfil de eventos 0");
                        
                    }

        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
    }
  }

  
  
  public void revisarEventos(String[] vectorhex) throws ParseException {
//System.out.println("entra a revisarEventos");
    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    String tamauxPE = vectorhex[6]+vectorhex[7]; //para verificar bien el length
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(tamauxPE, 16)) {
        if ((Integer.parseInt(tamauxPE, 16) + 2) <= vectorhex.length) {

                    String vectorDataPE[] = null;
                    
                    if (vectorhex[9].equals("02")&vectorhex[11].equals("00")) { //verifica tambien si la respuesta es por bloques
                        
                         if (primerbloque) {
                         vectorDataPE = sacarDatos(vectorhex, 8);
                         bytesRecortados = 7;
                         primerbloque = false;
                       } else {
                         int longv = Integer.parseInt(vectorhex[17], 16);
                         int recortarv = 17; //era 20, pero en realidad son 17 bytes antes del ochentaypico
                         if (longv >= 128) {
                           recortarv = recortarv + (longv % 128);
                         }
                         bytesRecortados = bytesRecortados + recortarv;
                         vectorDataPE = sacarDatos(vectorhex, recortarv + 1);
                       }
                        
                      listEventos.addAll(Arrays.asList(vectorDataPE));
                        
                      byte[] bloqueRecibido = {(byte) (Integer.parseInt(vectorhex[12], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[13], 16) & 0xFF),
                        (byte) (Integer.parseInt(vectorhex[14], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[15], 16) & 0xFF)};

                      byte trama[] = crearREQ_NEXT(bloqueRecibido);
                      trama = ajustarWrapper(trama);
                      ultimatramaEnviada = trama;
                      avisoStr("Perfil de eventos "+eventObisChange);
                      enviaTrama2(trama, "=> Envia Next Data Block Request");
                    } else {
                        vectorDataPE = sacarDatos(vectorhex, 8);
                        listEventos.addAll(Arrays.asList(vectorDataPE));
                        
                        
                        //System.out.println("sin bloque en eventos");
//                      lperfilEventos=true;
                        if (eventObisChange==8){
                            procesarDatos();
                            //System.out.println("no mas eventos");
                            lperfilEventos = false;
                            primerbloque = false;
//                            cerrarPuerto();
                        } else{ 
                            //System.out.println("otros eventos");
                            try {
                                procesaInfoEventos();
//                                cp.actualizaEvento(regEvento, null);
                            } catch (Exception e) {
                             }
                            listEventos = new ArrayList<>();
                            //System.out.println("siguiente evento");
                            listaeventos=listEventos.size();
                            byte trama[] = crearNext_Event();//)tramastcum.getStandardEvent();
                            trama = ajustarWrapper(trama);
                            ultimatramaEnviada = trama;
                            avisoStr("Perfil de eventos "+eventObisChange);
                            enviaTrama2(trama, "=> Envia trama perfil de eventos "+eventObisChange);    
                        }

                    }

        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          complemento = true;
          tramaIncompleta = cadenahex;
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Solicitud Informacon Perfil");
    }
  }

  
  private byte[] crearNext_Event(){
        eventObisChange++;
        if (eventObisChange==2){ //se salta el 2 y el 3
            eventObisChange=4;
        }
        
    byte trama[] = tramastcum.getStandardEvent();
    trama[17]= (byte)(eventObisChange);
    trama[7]=(byte)(trama.length-8);
    //System.out.println("eventObisChange: "+eventObisChange);
    return trama;
  }
    
  //VIC
    public void procesarDatos() { //nuevo  para reemplazar a logout                     
        cerrarPuerto();
        
        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Lectura OK");
        boolean l = false;
        if (lperfilcarga) {
//        if (lperfil) {
          avisoStr("Procesando PerfilCarga");

          if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
            try {
              procesaDatos();
              cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
              if (fechaintervalo != null) {
                if (fechaintervalo.after(med.getFecha())) {
                  cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                }
              }

            } catch (Exception e) {
              e.printStackTrace();
            }
                l = true;
          }
        }
        //System.out.println("leventos: "+leventos);
        if (lperfilEventos) {
//        if (leventos) {
//          avisoStr("Procesando Eventos");
          if (listEventos != null && listEventos.size() > 0) {
            try {
              procesaInfoEventos();
//              cp.actualizaEvento(regEvento, null);
            } catch (Exception e) {
            }
            
//            if (!lperfilEventos){
                l = true;
//            }
            
          }
        }
        if (!lperfil && !leventos) {
          l = true;
        }
        if (lfechasync) {
          l = true;
        }
        if (l) {
          avisoStr("Leido");
          escribir("Estado Lectura Leido");
          cerrarLog("Leido", true);
          med.MedLeido = true;
        } else {
          avisoStr("No Leido");
          escribir("Estado Lectura No Leido");
          cerrarLog("No Leido", false);
        }
        //System.out.println("leer = false; en procesarDatos");
        leer = false;   
  }
  //VIC

  public void revisarConfHora(String[] vectorhex) {
    if (complemento) {
      complemento = false;
      tramaIncompleta = tramaIncompleta + " " + cadenahex;
      vectorhex = tramaIncompleta.split(" ");
    }
    if (vectorhex.length > 7) {//tiene cabecera
      if (vectorhex.length >= Integer.parseInt(vectorhex[7], 16)) {
        if ((Integer.parseInt(vectorhex[7], 16) + 2) <= vectorhex.length) {
 
                    

                    if (Integer.parseInt(vectorhex[11], 16) == 0) {
                      //System.out.println("\nSe sincronizó la hora correctamente");
                      escribir("Se sincronizó la hora correctamente");
                      procesarDatos(); 
                    } else {
                      //System.out.println("No fue posible sincronizar la hora");
                      escribir("No fue posible sincronizar la hora");
                      reiniciaComuniacion();
                    }
                    lfechasync = false;
 
        } else {
          //trama incompleta
          //System.out.println("Incompleta");
          escribir("Error trama incompleta");
          enviaTramaUltima(ultimatramaEnviada, "=> Envia Sincronización de hora");
        }
      } else {
        //trama incompleta
        //System.out.println("Incompleta");
        escribir("Error trama incompleta");
        complemento = true;
        tramaIncompleta = cadenahex;
        enviaTramaUltima(ultimatramaEnviada, "=> Envia Sincronización de hora");
      }
    } else {//trama incompleta
      //System.out.println("Sin cabecera");
      escribir("Error trama Sin cabecera");
      complemento = true;
      tramaIncompleta = cadenahex;
      enviaTramaUltima(ultimatramaEnviada, "=> Envia Sincronización de hora");
    }
  }

  public void procesaInfoEventos() throws Exception {
    avisoStr("Procesando Eventos");
    String[] arrayEventos = listEventos.toArray(new String[listEventos.size()]);  
    int listaeventos=0;
    if (!lperfilEventos){
        interpretaDatos(arrayEventos, 24);
    }else{
     //System.out.println("Tipo de trama 25");
     interpretaDatos(arrayEventos, 25);      
    }
  }

  public void reiniciaComuniacion() {
    try {
//      rcom=true;
      enviando = false;
      rutinaCorrecta = true;
      Thread.sleep(2000);
      cerrarPuerto();
      complemento = false;
      tramaIncompleta = "";
      if (numeroReintentos >= actualReintento) {
        if (!objabortar.labortar) {
          if (reinicio == 0) {
            reinicio = 1;
          } else {
            reinicio = 0;
          }
          abrePuerto();
          //iniciacomunicacion();
        } else {
          cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion proceso abortado");
          cerrarLog("Abortado", false);
          leer = false;
        }
      } else {
        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion numero de reintentos agotado");
        avisoStr("No Leido");
        cerrarLog("Desconexion Numero de reintentos agotado", false);
        leer = false;

      }
    } catch (Exception eex) {
      eex.printStackTrace();
    }
  }

  private void iniciacomunicacion() {
    tiemporetransmision = 15000;
    lfechasync = false;
//    rcom = false;
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
        } catch (Exception e) {
          //e.printStackTrace();
        }
        ultimatramabloque = false;
        actualReintento++;
        try {
          avisoStr("Iniciando comunicacion");

        } catch (Exception e) {
        }
        
        //**

        lARRQ = true;
        byte trama[] = crearAARQ(tramastcum.getAarq(), (lconfhora ? password2 : password)); //AARQ
        trama = ajustarWrapper(trama);
        ultimatramaEnviada = trama;
        avisoStr("AARQ");
        //System.out.println("trama!!!!!!");
        //System.out.println(tramastcum.encode(trama, trama.length));
        enviaTrama2(trama, "");
        
        //**
      } else {
        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Medidor no configurado");
        cerrarPuerto();
        cerrarLog("Medidor no configurado", true);
        leer = false;
      }
    }
  }

  private void iniciacomunicacion2() {
    tiemporetransmision = 15000;
    lfechasync = false;
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
        } catch (Exception e) {
          //e.printStackTrace();
        }
        ultimatramabloque = false;
        actualReintento++;
        try {
          avisoStr("Iniciando comunicacion");
        } catch (Exception e) {
        }
        lARRQ = true; //VIC
        byte trama[] = crearAARQ(tramastcum.getAarq(), (lconfhora ? password2 : password)); //AARQ
        trama = ajustarWrapper(trama);
        ultimatramaEnviada = trama;
        avisoStr("AARQ");
        //System.out.println("trama AARQ!!!!!!");
        //System.out.println(tramastcum.encode(trama, trama.length));
        enviaTrama2(trama, "");
        
      } else {
        cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Medidor no configurado");
        cerrarLog("Medidor no configurado", true);
        cerrarPuerto();
        leer = false;
      }
    }
  }

  public void cerrarPuerto() {

    try {
      rutinaCorrecta = true;
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
      Thread.sleep(3000);
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
      reenviando = false;
      portconect = false;
      avisoStr("Cerrando puerto..");
      Thread.sleep((long) 5000);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  private void enviaTramaUltima(byte[] bytes, String descripcion) {
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
            reenviar = false;
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
                escribir("=> " + tramastcum.encode(trama, trama.length));
                //System.out.println(tramastcum.encode(trama, trama.length));
              enviaTrama(trama);
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
              while (salir) {
                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                  //System.out.println("Sale enviar");
                  salir = false;
                  if (!enviando) {
                    t = false;
                  }
                } else {
                  intentosalir++;
                  sleep(500);
                }
              }
            }
          }
          if (cierrapuerto) {
            cierrapuerto = false;
            if (lperfilcarga) {
              if (!lperfilcompleto) {
                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                  procesaInfoPerfil();
                  try {
                    procesaDatos();
                    cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                    if (fechaintervalo != null) {
                      if (fechaintervalo.after(med.getFecha())) {
                        cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                      }
                    }
                  } catch (Exception e) {
                  }

                }
              }
              lperfilcarga = false;
            }
            cerrarPuerto();
            if (numeroReintentos >= actualReintento) {
              if (!objabortar.labortar) {
                reinicio = 0;
                abrePuerto();
              } else {
                escribir("Proceso Abortado");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion proceso abortado");
                cerrarLog("Abortado", false);
                leer = false;
              }
            } else {
              escribir("Numero de reintentos agotado");
              cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion numero de reintentos agotado");
              try {
                avisoStr("No leido");
              } catch (Exception e) {
              }
              escribir("Estado Lectura No Leido");
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
              if (intentosRetransmision != 0) {
                escribir("TimeOut, Intento de reenvio..");
              }
                escribir(des);
                //System.out.println(des);
                //System.out.println("Envia trama =>");
                escribir("=> " + tramastcum.encode(trama, trama.length));
                //System.out.println(tramastcum.encode(trama, trama.length));
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
              while (salir) {
                if (!enviando || intentosalir > (tiemporetransmision / 500)) {
                  //System.out.println("Sale enviar");
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
            if (lperfilcarga) {
              if (!lperfilcompleto) {
                if (vPerfilCarga != null && vPerfilCarga.size() > 0) {
                  procesaInfoPerfil();
                  try {
                    procesaDatos();
                    cp.actualizaLectura(vlec, file, new Timestamp(med.getFecha().getTime() - (long) 86400000));
                    if (fechaintervalo != null) {
                      if (fechaintervalo.after(med.getFecha())) {
                        cp.actualizaFechaLectura(med.getnSerie(), sgflect.format(new Date(fechaintervalo.getTime())));
                      }
                    }
                  } catch (Exception e) {
                  }
                }
              }
              lperfilcarga = false;
            }

            if (numeroReintentos >= actualReintento) {
              if (!objabortar.labortar) {
                reinicio = 0;
                abrePuerto();
              } else {
                escribir("Proceso Abortado");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion proceso abortado");
                cerrarLog("Abortado", false);
                leer = false;
              }
            } else {
              escribir("Numero de reintentos agotado");
              cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion numero de reintentos agotado");
              avisoStr("No leido");
              escribir("Estado Lectura No Leido");
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
                escribir("=> " + tramastcum.encode(trama, trama.length));
                //System.out.println(tramastcum.encode(trama, trama.length));
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
                reinicio = 1;
                abrePuerto();
              } else {
                escribir("Proceso Abortado");
                cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion proceso abortado");
                cerrarLog("Abortado", false);
                leer = false;
              }
            } else {
              escribir("Numero de reintentos agotado");
              cp.escribirLog(usuario, new Timestamp(Date.from(ZonedDateTime.now(zid).toInstant()).getTime()), med.getnSerie(), "LeerISKRAMT880", "Desconexion numero de reintentos agotado");
              avisoStr("No leido");
              escribir("Estado Lectura No Leido");
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

  private void procesaInfoPerfil() {
    String[] data = infoPerfil.toArray(new String[infoPerfil.size()]);
    interpretaDatos(data, 5);
  }

  private void procesaDatos() {
    String[] data = vPerfilCarga.toArray(new String[vPerfilCarga.size()]);
    interpretaDatos(data, 22);
  }

  private String[] cortarTrama(String[] vectorhex, int tamaño) {
    ////System.out.println("Tamaño "+tamaño);
    String nuevoVector[] = new String[tamaño + 2];
    System.arraycopy(vectorhex, 0, nuevoVector, 0, tamaño + 2);
    ////System.out.println("Nueva trama");
    for (int i = 0; i < nuevoVector.length; i++) {
      System.out.print(" " + nuevoVector[i]);
    }
    return nuevoVector;
  }

  
  public static byte[] calcularnuevocrcI(byte[] siguientetrama) {
    try {
      byte[] data = new byte[5];
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
      siguientetrama[6] = (byte) (Integer.parseInt(stxcrc.substring(0, 2), 16) & 0xFF);
      siguientetrama[7] = (byte) (Integer.parseInt(stxcrc.substring(2, 4), 16) & 0xFF);
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

  public static boolean validacionCRCHCS(String[] data) {
    boolean lcrc = false;
    byte b[] = new byte[5];
    for (int j = 0; j
            < b.length; j++) {
      b[j] = (byte) (Integer.parseInt(data[j + 1], 16) & 0xFF);
    }
    int crc = calculoFCS(b);
    ////System.out.println("valor crc cal" + crc);
    String stx = data[6] + "" + data[7];
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

  private static boolean validacionCRCFCS(String[] data) {
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
    ////System.out.println("fcs trama " + stx);
    ////System.out.println("fcs calculado " + stxcrc);
    if (stx.equals(stxcrc)) {
      lcrc = true;
    }
    return lcrc;
  }
  protected static byte[] Hexhars = {
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f'};

  public static String encode(byte[] b, int ancho) {

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


  public String[] sacarDatos(String[] vectorhex, int inicio) {
    String tamaux = vectorhex[6]+vectorhex[7];
//    tamaux.add=
//      byte[] tamaux = {(byte) (Integer.parseInt(vectorhex[6], 16) & 0xFF), (byte) (Integer.parseInt(vectorhex[7], 16) & 0xFF)};
////    System.out.println("\nTamaux\n" + tamaux);
    int tam = Integer.parseInt(tamaux, 16);
////    System.out.println("\nTamaux a integer\n" + tam);
    String nuevoVector[] = new String[tam+8-inicio];// - inicio - 1]; //tam ya es el numero de bytes siguientes
////    System.out.println("\ninicio\n" + inicio);
////    System.out.println("\nnuevoVector.length\n" + nuevoVector.length);
////    System.out.println("\nvectorhex.length\n" + vectorhex.length);
    System.arraycopy(vectorhex, inicio, nuevoVector, 0, nuevoVector.length);
    ////System.out.println("\nDATA\n");
    //System.out.println("\nTrama Recortada\n" + Arrays.toString(nuevoVector));
    return nuevoVector;
  }
  
  private byte[] ajustarWrapper(byte[] trTCP) {
        ArrayList<String> trama = new ArrayList<>();
        //wrapper TCP
        for (int i = 0; i < wrapperGPRS.length(); i=i+2) {
            trama.add(wrapperGPRS.substring(i, i + 2).toUpperCase());
        }
        //bytes restantes
        for (int i = 6; i < trTCP.length; i++) {
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
  
  private byte[] crearAARQ(byte[] t3, String pass) {
    Vector<String> trama = new Vector<String>();
  
    //copiar unknow - bytes de 0 a 6
    for (int i = 0; i < 7; i++) {
      trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
    }
    
    // bytes number -byte 7 VIC
    trama.add(Integer.toHexString((48 + pass.length())).toUpperCase()); 

    // copiar data VIC bytes de 0 a 37
    for (int i = 8; i < 38; i++) {
      trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
    }
      
    //password bytes de 38 a 45
    for (int i = 0; i < pass.length(); i++) {
      trama.add(convertStringToHex(pass.substring(i, i + 1)).toUpperCase());
    }
    
    //datos 2da parte bytes del 46 al final 
    for (int i = 46; i < t3.length; i++) {
      trama.add(Integer.toHexString(t3[i] & 0xFF).toUpperCase());
    }
    
    //copia trama a arreglo de bytes
    byte[] tramabyte = new byte[trama.size()];
    int i = 0;
    for (String t : trama) {
      tramabyte[i] = (byte) (Integer.parseInt(t, 16) & 0xFF);
      i++;
    }
      
    //tamaño datos trama VIC
    tramabyte[7] = (byte) (Integer.parseInt(Integer.toHexString(tramabyte.length - 8), 16) & 0xFF);
       
    //tamaño data aarq VIC
    tramabyte[9] = (byte) (Integer.parseInt(Integer.toHexString((46 + pass.length())), 16) & 0xFF);
    
    //tamaño password VIC
    tramabyte[37] = (byte) (Integer.parseInt(Integer.toHexString(pass.length()), 16) & 0xFF);
    
    return tramabyte;
  }

  private byte[] crearREQ_NEXT(byte[] bloqueRecibido) {

    byte trama[] = tramastcum.getREQ_NEXT();
    System.arraycopy(bloqueRecibido, 0, trama, 11, bloqueRecibido.length);
    trama[7]=(byte)(trama.length-8);

    return trama;
  }

  private byte[] construirConstant(String obis) {
    //System.out.println("OBIS a solicitar " + obis);
    byte trama[] = tramastcum.getConstant();
//    trama = asignaDireciones(trama, dirlog);
//    trama[5] = I_CTRL(nr, ns);
    trama[13] = (byte) (Integer.parseInt(obis.substring(0, 2), 16) & 0xFF);
    trama[14] = (byte) (Integer.parseInt(obis.substring(2, 4), 16) & 0xFF);
    trama[15] = (byte) (Integer.parseInt(obis.substring(4, 6), 16) & 0xFF);
    trama[16] = (byte) (Integer.parseInt(obis.substring(6, 8), 16) & 0xFF);
    trama[17] = (byte) (Integer.parseInt(obis.substring(8, 10), 16) & 0xFF);
    trama[18] = (byte) (Integer.parseInt(obis.substring(10, 12), 16) & 0xFF);
//    trama = calcularnuevocrcI(trama);
    return trama;
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
    if (tipotrama.equals("97")) {
      enviaTramaUltima(ultimatramaEnviada, "Frame Reject enviando reenviando ultima trama");
    } else if (tipotrama.equals("1F")) {
      avisoStr("No leido");
      escribir("Estado Lectura No Leido");
      reiniciaComuniacion();
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
    log.setLeventos(leventos);
    log.setLregistros(lregistros);
    log.setNduracion((int) (tiempofinal.getTime() - tiempoinicial.getTime()));
    log.setNreintentos(reintentosUtilizados);
    log.setVccoduser(usuario);
    log.setLexito(lexito);
    cp.saveLogCall(log, null);
  }
}
