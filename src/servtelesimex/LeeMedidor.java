/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servtelesimex;

import Control.ControlProcesos;
import Entidades.Abortar;
import Entidades.EConfModem;
import Entidades.EMedidor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Vector;
import protocolos.*;


/**
 *
 * @author dperez
 */
public class LeeMedidor {

    boolean p;
    boolean fp;
    boolean e;
    boolean r;
    boolean a;
    boolean ch; 
    boolean disc;
    boolean rec;
    EMedidor medidor;
    Vector<EConfModem> confmodem;
    int indx;
    //AppletLecturaMedidores lcm;
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Date actual;
    public boolean continua = true;
    public boolean termino = false;
    ControlProcesos cp;
    Abortar objabortar;
    ZoneId zid;
    long ndesfase;
    boolean last;

    public LeeMedidor(EMedidor med, Vector<EConfModem> confmodem, boolean perfil,  boolean factorPotencia, boolean eventos, boolean registros, boolean acumulados, boolean confhora, boolean disconnect, boolean reconnect, ControlProcesos cp, Abortar objabortar, ZoneId zid, long ndesfase, boolean last) {
        p = perfil;
        fp = factorPotencia;
        e = eventos;
        r = registros;
        a = acumulados;
        this.ch = confhora; 
        this.disc = disconnect;
        this.rec = reconnect;
        medidor = med;
        this.confmodem = confmodem;
        this.cp = cp;
        this.objabortar = objabortar;
        this.zid = zid;
        this.ndesfase = ndesfase;
        this.last = last;
    }

    public void medir(boolean aviso, Date fin) throws Exception {
        //LeeListaMedidoresProg.incrementaConexSimultaneas();
        int codigo = Integer.parseInt(medidor.getMarcaMedidor().getCodigo());
        if (codigo==21){codigo=13;} //Tecun series 50 := 21
        else if (codigo==22){codigo=19;} //Hexing HX34K := 22
        switch (codigo) {

            case 1: {//ABB ALPHA II
                if (medidor.getTipoconexion() == 1) {//ABB ALPHA II PSTN
                    try {
                        LeerRemotoPSTNABB leer = new LeerRemotoPSTNABB(medidor, confmodem, p, e, indx, cp, objabortar, aviso, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar ||  actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto();
                                leer.terminahilos();
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ABB ALPHA II " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }

                } else { //ABB ALPHA II GPRS
                    try {
                        LeerRemotoGPRSABB leer = new LeerRemotoGPRSABB(medidor, confmodem, p, e, false, indx, cp, objabortar, aviso, zid);//
                        try {
                            while (leer.leer) {
                                Thread.sleep(1000);
                                actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                                if(objabortar.labortar || actual.after(fin)) {
                                    leer.cerrarPuerto();
                                    leer.terminahilos();
                                    leer.leer = false;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Fecha: " + new Date() );
                            System.err.println(getErrorString(e.getStackTrace(), 3));
                        }
                        leer = null;
                        System.out.println("Sale lectura - ABB ALPHA II GPRS " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 2: { //ACTARIS
                if (medidor.getTipoconexion() == 1) { //pstn
                    try {
                        System.out.println("Lee actaris" + medidor.getnSerie());
                        LeerRemotoPSTNActarisSL7000 leer = new LeerRemotoPSTNActarisSL7000(medidor, confmodem, p, e, false, false, indx, aviso, cp, objabortar, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        System.out.println("Sale lectura - ITRON SL7000 PSTN " + medidor.getnSerie());
                        leer.terminaHilos();
                        leer = null;
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } else if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee actaris" + medidor.getnSerie());
                        LeerRemotoTCPActarisSL7000 leer = new LeerRemotoTCPActarisSL7000(medidor, p, e, r, a, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));                            
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto(false);
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - ITRON SL7000 TCP " + medidor.getnSerie());

                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }

            }
            break;
            case 3: {//ABB ALPHA I
//               
            }
            case 4: {//ELSTER A1800
                if (medidor.getTipoconexion() == 0) { //GPRS
                    try {
                        System.out.println("Lee Elster" + medidor.getnSerie());
                        LeerRemotoGPRSElsterA1800 leer = new LeerRemotoGPRSElsterA1800(medidor, confmodem, p, e, r, indx, cp, objabortar, aviso, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - ELSTER A1800 GPRS" + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } else if (medidor.getTipoconexion() == 1) { //PSTN
                    try {
                        System.out.println("Lee Elster" + medidor.getnSerie());
                        LeerRemotoPSTNElsterA1800 leer = new LeerRemotoPSTNElsterA1800(medidor, confmodem, p, e, r, indx, cp, objabortar, aviso, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - ELSTER A1800 PSTN " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } else if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) {//TCP
                    try {
                        System.out.println("Lee Elster" + medidor.getnSerie());
                        LeerRemotoTCPElsterA1800 leer = new LeerRemotoTCPElsterA1800(medidor, p, e, r, false, a, indx, cp, objabortar, aviso, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto(false);
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - ELSTER A1800 TCP " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 5: { //elgama ehm
                if (medidor.getTipoconexion() == 1) { //PSTN
                    try {
                        LeerRemotoPSTNElgama leer = new LeerRemotoPSTNElgama(medidor, confmodem, p, e, r, indx, cp, objabortar, aviso, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }

                } else if (medidor.getTipoconexion() == 0) {//GPRS
                    try {
                        LeerRemotoGPRSElgama leer = new LeerRemotoGPRSElgama(medidor, confmodem, p, e, r, indx, cp, objabortar, aviso, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } else if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) {//TCP
                    try {
                        LeerRemotoTCPElgama leer = new LeerRemotoTCPElgama(medidor, p, e, r, false, indx, cp, objabortar, aviso, zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 6:  //landys ZMD                           
            break;
            case 7: { //Actaris AC6000
                if (medidor.getTipoconexion() == 1) { //pstn
                    try {
                        System.out.println("Lee actaris" + medidor.getnSerie());
                        LeerRemotoPSTNActarisSL7000 leer = new LeerRemotoPSTNActarisSL7000(medidor, confmodem, p, e, r, false, indx, aviso, cp, objabortar, zid);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                try {
                                    leer.cerrarPuerto();
                                } catch (Exception e) {
                                    System.err.println("Fecha: " + new Date() );
                                    System.err.println(getErrorString(e.getStackTrace(), 3));
                                } finally {
                                    leer.leer = false;
                                }                                
                            }
                        }
                        System.out.println("Sale lectura - ITRON AC6000 PSTN " + medidor.getnSerie());
                        leer.terminaHilos();
                        leer = null;
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } else if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee actaris" + medidor.getnSerie());
                        LeerRemotoTCPActarisSL7000 leer = new LeerRemotoTCPActarisSL7000(medidor, p, e, r, a, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                try {
                                    leer.cerrarPuerto(false);
                                } catch (Exception e) {
                                    System.err.println("Fecha: " + new Date() );
                                    System.err.println(getErrorString(e.getStackTrace(), 3));
                                } finally {
                                    leer.leer = false;
                                }                                
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - ITRON AC6000 TCP " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 8: { //Sentinel
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee SENTNEL" + medidor.getnSerie());
                        LeerRemotoTCPSentinel leer = new LeerRemotoTCPSentinel(medidor, p, e, r, indx, aviso, cp, objabortar, zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                try {
                                    leer.cerrarPuerto(false);
                                } catch (Exception e) {
                                    System.err.println("Fecha: " + new Date() );
                                    System.err.println(getErrorString(e.getStackTrace(), 3));
                                } finally {
                                    leer.leer = false;
                                }                                
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - Sentinel TCP " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 9: { //iskra
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee ISKRA" + medidor.getnSerie());
                        LeerRemotoIskraMT174 leer = new LeerRemotoIskraMT174(medidor, p, fp, e, r, indx, cp, objabortar, aviso, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                try {
                                    leer.cerrarPuerto(true);
                                } catch (Exception e) {
                                    System.err.println("Fecha: " + new Date() );
                                    System.err.println(getErrorString(e.getStackTrace(), 3));
                                } finally {
                                    leer.leer = false;
                                }
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - ISKRA MT174 " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 10: { //Elster A1440
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee ELSTER A1440" + medidor.getnSerie());
                        LeerRemotoTCPElsterA1440 leer = new LeerRemotoTCPElsterA1440(medidor, p, e, r, false, indx, cp, objabortar, aviso,cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto();                                
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura");
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 11: { //CIRCUITOR
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee CIRCUITOR" + medidor.getnSerie());
                        LeerRemotoCircuttor leer = new LeerRemotoCircuttor(medidor, p, e, r, indx, cp, objabortar, aviso, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura - CIRCUITOR " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 12: { //ELGAMA EPQS
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee ELGAMA EPQS" + medidor.getnSerie());
                        LeerElgamaEPQS leer = new LeerElgamaEPQS(medidor, p, e, r, false, indx, cp, objabortar, aviso, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto                                
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ELGAMA EPQS " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 13: { //TECUN LY
                //Vic 07-06-19                
                 if (medidor.getTipoconexion() == 5) { // TCP con GPRS interno //Vic 03-08-19
                    try {
                        //para pruebas locales
                        System.out.println("Lee TECUN - para prueba con LYSM300 " + medidor.getnSerie()); 
                        LeerRemotoTCPGPRSTecunLYSM300 leer = new LeerRemotoTCPGPRSTecunLYSM300(medidor, p, e, r, false, indx, aviso, cp, objabortar, cp.getEquipo(), zid);
                        
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - TECUN GPRS interno " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));                        
                    }
                } 
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP //Vic 03-08-19
                    try {
                        System.out.println("Lee TECUN LY" + medidor.getnSerie());
                        LeerRemotoTCPTecunly leer = new LeerRemotoTCPTecunly(medidor, p, e, r, false, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto(false);
                                leer.leer = false;

                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - TECUN " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 14: { //Iskra MT880 vic 07-06-19
                // Vic 05-06-19
                
                if (medidor.getTipoconexion() == 5) { // TCP con GPRS interno
                    try {

                        System.out.println("Lee ISKRAMT880 " + medidor.getnSerie()); 
                        LeerRemotoTCPGPRSIskraMT880 leer = new LeerRemotoTCPGPRSIskraMT880(medidor, p, e, r, false, indx, aviso, cp, objabortar, cp.getEquipo(), zid);
                        
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ISKRA MT880 " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } 
                
                //
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // 2 GPRS 3 TCP //Vic 03-08-19
                    try {
                        System.out.println("Lee ISKRAMT880 " + medidor.getnSerie());
                        LeerRemotoTCP_ISKRAMT880 leer = new LeerRemotoTCP_ISKRAMT880(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ISKRA MT880 " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 15: { //Landis RXRS4
                if (medidor.getTipoconexion() == 1) { //PSTN
                } else if (medidor.getTipoconexion() == 0) { // GPRS
                } else if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // TCP
                    try {
                        System.out.println("Lee LandisRXRS4" + medidor.getnSerie());
                        LeerRemotoTCPLandisRXRS4 leer = new LeerRemotoTCPLandisRXRS4(medidor, p, e, r, false, indx, aviso, cp,  objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto(false);    
                                leer.leer = false;
                            }
                        }
                        leer.terminaHilos();
                        leer = null;
                        System.out.println("Sale lectura");
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            //vic 05-06-19        
            case 17: { //ElGama300
//                medidor.tipoconexion=4;
                if (medidor.getTipoconexion() == 2) { //GPRS interno - este medidor no presenta modem interno, por eso las clases son las mismas en todos los tipos
                    try {

                        System.out.println("Lee Elgama - Gama300 " + medidor.getnSerie()); 
                        LeerRemotoTCPElgama300 leer = new LeerRemotoTCPElgama300(medidor, p, e, r,false, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto(false);
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ELGAMA300 " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } 
                if (medidor.getTipoconexion() == 0 || medidor.getTipoconexion() == 3) { 
                    try {
                        
                        System.out.println("Lee Elgama - Gama300 " + medidor.getnSerie()); 
                        
                        LeerRemotoTCPElgama300 leer = new LeerRemotoTCPElgama300(medidor, p, e, r,false, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto(false);
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ELGAMA300 " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 18: { // DTS27
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485 //RED TCP
                    try {
//                        System.out.println("Lee STAR DTS27: " + medidor.getnSerie()+(disc?" - Desconexión de Relé": ""));
                        LeerRemotoTCP_STARDTS27 leer = new LeerRemotoTCP_STARDTS27(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);;
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);                                
                                leer.ps.leer = false;
                            }
                        }    
                        leer = null;
                        System.out.println("Sale lectura");
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 19: { // HEXING                
                if (medidor.getTipoconexion() == 0) {//GPRS interno - TCP - Wrapper
                    try {
                        System.out.println("Lee HEXING: " + medidor.getnSerie());
                        LeerRemotoTCPHexing leer = new LeerRemotoTCPHexing(medidor, p, e, r, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto(false);
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura");
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }                    
                } else if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { // 
                    try {
                        System.out.println("Lee HEXING Estructurado: " + medidor.getnSerie());
                        LeerRemotoTCPHexingStruct leer = new LeerRemotoTCPHexingStruct(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - HEXING " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                    }
                }            
            break;
            case 20: { // MeterAndControl
                //
                if ( medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //  TCP
                    try {
                        System.out.println("Lee MeterAndControl " + medidor.getnSerie());
                        LeerRemotoTCPMnC leer = new LeerRemotoTCPMnC(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - MNC " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;  
        case 23: { //Landis Gyr ZMG 310
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP
                    try {
                        System.out.println("Lee Landis Gyr - ZMG 310: " + medidor.getnSerie()); 
                        LeerRemotoTCP_ZMG310 read = new LeerRemotoTCP_ZMG310(medidor, p, e, r, false, a, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        
                        while (read.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                read.cerrarPuerto(false);
                                read.leer = false;
                            }
                        }
                        read = null;
                        System.out.println("Sale lectura");
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 24: { // Microstar
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee Microstar: " + medidor.getnSerie()); 
                        LeerRemotoTCPMicrostar leer = new LeerRemotoTCPMicrostar(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - Microstar: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 25: { // ADDAXAD13A8
                if (medidor.getTipoconexion() == 2) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee ADDAXAD13A8: " + medidor.getnSerie());
                        LeerRemotoTCP_ADDAXAD13A8 leer = new LeerRemotoTCP_ADDAXAD13A8(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ADDAX: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } 
            }
            break;
            case 26: { // INHEMETER DTZ1513
                if (medidor.getTipoconexion() == 2) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee INHEMETER DTZ1513: " + medidor.getnSerie());
                        LeerRemotoTCP_INHEMETERDTZ1513 leer = new LeerRemotoTCP_INHEMETERDTZ1513(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - INHEMETER: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                } 
            }
            break;
            case 27: { // WASION
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee Wasion: " + medidor.getnSerie());
                        LeerRemotoTCPWasionAMeter300 leer = new LeerRemotoTCPWasionAMeter300(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if(objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - Wasion: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            case 28: { // HoneyWell
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee HoneyWell: " + medidor.getnSerie());
                        LeerRemotoHoneywellHS3400 leer = new LeerRemotoHoneywellHS3400(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
//                            System.out.println(leer.med.nSerie);
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - HoneyWell: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            
            case 29: { // KAIFA
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee Kaifa: " + medidor.getnSerie());
                        LeerRemotoTCP_Kaifa leer = new LeerRemotoTCP_Kaifa(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
//                            System.out.println(leer.med.nSerie);
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - Kaifa: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            
            case 30: { // ION 8650
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee ION 8650: " + medidor.getnSerie());
                        LeerRemotoTCP_ION8650 leer = new LeerRemotoTCP_ION8650(medidor, p, e, r, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
//                            System.out.println(leer.med.nSerie);
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin) ) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - ION: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            
            case 31: { // SEL 735
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee SEL 735: " + medidor.getnSerie());
                        LeerRemotoTCP_SEL735 leer = new LeerRemotoTCP_SEL735(medidor, p, e, r, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.leer) {
//                            System.out.println(leer.med.nSerie);
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.cerrarPuerto();
                                leer.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - SEL: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            
            case 32: { // NANSEN_NSX112i
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee Nansen NSX 112i: " + medidor.getnSerie());
                        LeerRemotoTCP_NANSEN_NSX112i leer = new LeerRemotoTCP_NANSEN_NSX112i(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
//                            System.out.println(leer.med.nSerie);
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - Nansen: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
            
            case 33: { // METCOM
                if (medidor.getTipoconexion() == 2 || medidor.getTipoconexion() == 3) { //TCP - modem externo a traves de RS485
                    try {
                        System.out.println("Lee Metcom: " + medidor.getnSerie());
                        LeerRemotoTCP_Metcom leer = new LeerRemotoTCP_Metcom(medidor, p, e, r, ch, a, disc, rec, indx, aviso, cp, objabortar, cp.getEquipo(), zid, ndesfase);
                        while (leer.ps.leer) {
//                            System.out.println(leer.med.nSerie);
                            Thread.sleep(1000);
                            actual = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                            if (objabortar.labortar || actual.after(fin)) {
                                //se aborto la operacion por lo tanto
                                leer.ps.cerrarPuerto(false);
                                leer.ps.leer = false;
                            }
                        }
                        leer = null;
                        System.out.println("Sale lectura - Metcom: " + medidor.getnSerie());
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        System.err.println(getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
            break;
        }
        if (this.last) {
            LeeListaMedidoresProg.decrementaConexSimultaneas( medidor.getIdProg() );
        }        
        termino = true;
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