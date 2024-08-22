/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolos;

import Control.ControlProcesos;
import Entidades.Abortar;
import Entidades.EMedidor;
import Entidades.EParamProtocolos;
import Entidades.ERegistro;
import Entidades.Electura;
import Entidades.ElecturaAux;
import static java.lang.Thread.sleep;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author nicol
 */
public class LeerRemotoTCP_NANSEN_NSX112i {
    private final String name = "Default_1";
    private boolean hasSec;
    //variables constructor
    private final ZoneId zid;
    private final long ndesfase;
    public EMedidor med;
    ControlProcesos cp;
    EParamProtocolos epp;
    private String usuario = "admin";
    boolean lperfil; //       Booleanos 
    boolean leventos; //      checkbox 
    boolean lregistros; //       de
    boolean lconfhora; //    Telesimex
    boolean lacumulados;
    boolean ldisconnect;
    boolean lreconnect;
    boolean aviso = false;
    int indx = 0;
    int ndias;
    Abortar objabortar;
    
    //parámetros DLMS
    int PhyAddClass = 1;
    int numBytesDir = 4; //número de bytes para la dirección del medidor
    int InvokeIDandParity = 193; //0xC1 := 193
    int indxLength = 2;
    int users[] = {32,16}; //dirección origen - Broadcast para MnC es "16"// "32" Local Reading client 
    double resolucion = 1000.0;
    
    public ProcesosSesion ps;
    ProcesosDLMS DLMS;
    Thread escucha = null;
    Thread envia = null;
    
    public ArrayList<String> DLMS_PrincipalOBIS;
    public ArrayList<String> DLMS_PrincipalEvents;
    
    private final Object monitor = new Object();
    
    public LeerRemotoTCP_NANSEN_NSX112i(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, boolean acumulados,  boolean ldisconnect, boolean lreconnect, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) throws InterruptedException{
        this.med = med;
        this.cp = cp;
        this.usuario = usuario;
        this.lconfhora = lconfhora;
        this.lperfil = perfil;
        this.leventos = eventos;
        this.lregistros = registros;
        this.lacumulados = acumulados;
        this.ldisconnect = ldisconnect;
        this.lreconnect = lreconnect;
        this.aviso = aviso;
        this.objabortar = objabortar;
        this.indx = indx;
        this.zid = zid;
        this.ndesfase = ndesfase;
        init();
            
    }
    private void init() throws InterruptedException {
        users[0] = Integer.parseInt((med.getDireccionCliente()== null ? "32" : med.getDireccionCliente()));
        numBytesDir = med.getBytesdireccion().equals("5")? numBytesDir : Integer.parseInt( med.getBytesdireccion() );
        hasSec = med.getSeguridad();
        epp = new EParamProtocolos(this);
        epp.inicializarAARQ();
        epp.inicializarOBIS();
        epp.setType("DLMS");
        epp.setPhyAddClass(PhyAddClass);
        epp.setDLMS_numBytesDir(numBytesDir);
        epp.setDLMS_InvokeIDandParity(InvokeIDandParity);
        epp.setDLMS_indxLength(indxLength);
        epp.setUsers(users);
        epp.setDLMS_sumaInvokeIDnP(false);
        epp.setUsarRangoEnIntervalos(false);
        //OBIS
        epp.editDLMS_PrincipalOBIS(3, "0100000003FF"); //Phy Address
        epp.editDLMS_PrincipalOBIS(6, "0000636200FF"); //eventos - Log 1
        //epp.editDLMS_PrincipalOBIS(7, ""); //registros - Diarios - 
        //epp.editDLMS_PrincipalOBIS(8, "0000620100FF"); //registros - Mensuales - Billing profile              
        DLMS_PrincipalEvents = new ArrayList<>();
        DLMS_PrincipalEvents.add("01"); //down
        DLMS_PrincipalEvents.add("02"); //up
        epp.setDLMS_PrincipalEvents(DLMS_PrincipalEvents);
        epp.setResolucion(resolucion);

        ps = new ProcesosSesion(med, lperfil, leventos, lregistros, lconfhora, lacumulados, ldisconnect, lreconnect, indx, aviso, cp, objabortar, usuario, epp, zid, ndesfase, monitor);
        if (!ps.leer) {
            ps.escribir("Inicialización de Procesos de sesión fallida");
            return;
        }
        DLMS = new ProcesosDLMS(epp, med, ps, cp, usuario, name, hasSec);
        if (!ps.leer) {
            ps.escribir("Inicialización de Procesos DLMS fallida");
            return;
        }
        DLMS.setEK_HLS(null);
        DLMS.setAK_HLS(null);
        
        byte[] addField = {(byte) 0x01};
        epp.editCamposAARQ("appContextName", addField);
        epp.editCamposAARQ("acseReq", addField);
        epp.editCamposAARQ("mechName", addField);
        epp.editCamposAARQ("authenticationValue", addField);
        epp.editCamposAARQ("userInfo", addField);
        epp.editCamposAARQ("proposedDLMSVersion", addField);
        epp.editCamposAARQ("proposedConformance", addField);
        epp.editCamposAARQ("clientMaxRxPDUSize", addField);
        
        escuchainit();
        enviaSNRMinit();
        escucha.start();
        envia.start();        
    }
    
    private void enviaSNRMinit() {
        envia = new Thread() {
            @Override
            public void run() {
                synchronized (monitor) {
                    try {
                        boolean esperainicio = true;
                        while (esperainicio) {
                            if (!ps.leer) {
                                break;
                            }
                            if (ps.enviaPrimeraTrama) {
                                DLMS.enviaSNRM1();
                            }
                            if (ps.lenviaTrama2) {
                                ps.enviaTrama2(ps.ultimatrama, ps.descripcionTrama, DLMS);
                            }
                            if (objabortar.labortar || !ps.leer) {
                                esperainicio = false;
                            } else {
                                sleep(300);
                            }
                        }
                        while (escucha.isAlive() ) {
                            monitor.wait(100);
                            escucha.interrupt();
                        }
                        ps.escribir("Finalizado hilo de envío");
                    } catch (Exception e) {
                        ps.leer = false;                        
                        while (escucha.isAlive() ) {                            
                            try {
                                monitor.wait(100);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            escucha.interrupt();
                        }
                        ps.escribir("Finalizado hilo de envío");
                        ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
        };
    }

    private void escuchainit() {
        escucha = new Thread() {
            @Override
            public void run() {
                synchronized (monitor) {
                    try {
                        boolean esperaprocesa = true;
                        while (esperaprocesa) {
                            ps.barrera.compareAndSet(false, true);
                            monitor.wait();
                            ps.escucha();                            
                            if (ps.interpretaCadenaB) {
                                DLMS.interpretaCadena(ps.cadenahex);
                            } 
                            if (objabortar.labortar || !ps.leer) {
                                esperaprocesa = false;
                            } else {
                                sleep(300);
                            }
                            ps.barrera.compareAndSet(true, false);
                            monitor.wait();
                        }
                    } catch (Exception e) {
                        ps.escribir(ps.getErrorString(e.getStackTrace(), 3));
                    }
                }
            }
        };
    }
    
    public Vector<Electura> procesaLecCero(Timestamp fprimerintervalo, Timestamp fechaintervalo, Timestamp fechaCero, int intervalo, Electura lec, Vector<Electura> vlec){
        if (!fprimerintervalo.equals(fechaintervalo)){ 
            lec.fecha = new Timestamp(fechaCero.getTime()-(60000 * intervalo));
            // lec.lec = lec.lec-lecgama300.get(indxlec);
            vlec.add(lec);
            // lecgama300.set(indxlec,lec.lecaux);
        }else{
            // lecgama300.add(indxlec,lec.lecaux);
        }
        return vlec;
    }
    
    public ElecturaAux procesaLec(Timestamp fprimerintervalo, Timestamp fechaintervalo, Timestamp fechaCero, int intervalo, Electura lec, ElecturaAux elecaux){
        System.out.println("Procesa valor MnC");
        try{
        int indx = elecaux.getIndexlec();
        Vector<Electura> vlec = elecaux.getVlec();
        lec.setLecaux(lec.lec);//Conserva el valor actual como histórico en "lecaux"
        if (!fprimerintervalo.equals(fechaintervalo)){
            lec.fecha = new Timestamp(fechaintervalo.getTime()-(60000 * intervalo));
            lec.lec = Math.abs(elecaux.lecAuxMed.get(indx) - lec.lec);//Saca la diferencia entre el valor calculado anterior y el valor actual
            System.out.println("Valor re-calculado: "+ lec.lec);
            vlec.add(lec);
            elecaux.lecAuxMed.set(indx,lec.lec); //Guarda el valor calculado en la posición para el canal actual
        }else{
            elecaux.lecAuxMed.add(indx,lec.lecaux); //Guarda el primer valor que llega en la posición para el canal actual
        }
        indx++;
        elecaux.setIndexlec(indx);
        elecaux.setVlec(vlec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elecaux;
    }
    
    public ArrayList<ERegistro> procesaRegCero(Timestamp fprimerintervalo, Timestamp fechaintervalo, Timestamp fechaCero, int intervalo, ERegistro reg, ArrayList<ERegistro> vreg){
        if (!fprimerintervalo.equals(fechaintervalo)){ 
            Timestamp fecha = new Timestamp(fechaCero.getTime()-(60000 * intervalo));
            reg.setYear(fecha.getYear() + 1900);
            reg.setMonth(fecha.getMonth() + 1);
            reg.setDay(fecha.getDate());
            // lec.lec = lec.lec-lecgama300.get(indxlec);
            vreg.add(reg);
            // lecgama300.set(indxlec,lec.lecaux);
        }else{
            // lecgama300.add(indxlec,lec.lecaux);
        }
        return vreg;
    }
    
    public ElecturaAux procesaReg(Timestamp fprimerintervalo, Timestamp fechaintervalo, Timestamp fechaCero, int intervalo, ERegistro reg, ElecturaAux elecaux){
        System.out.println("Registros - Procesa valor MnC");
        try{
        int indx = elecaux.getIndexlec();
        ArrayList<ERegistro> vreg = elecaux.getVreg();
        reg.setLecaux(reg.energia);//Conserva el valor actual como histórico en "lecaux"
        if (!fprimerintervalo.equals(fechaintervalo)){
            Timestamp fecha = new Timestamp(fechaintervalo.getTime()-(60000 * intervalo));
            reg.setYear(fecha.getYear() + 1900);
            reg.setMonth(fecha.getMonth() + 1);
            reg.setDay(fecha.getDate());
            reg.energia = Math.abs(elecaux.lecAuxMed.get(indx) - reg.energia);//Saca la diferencia entre el valor calculado anterior y el valor actual
            System.out.println("Valor re-calculado: "+ reg.energia);
            vreg.add(reg);
            elecaux.lecAuxMed.set(indx,reg.energia); //Guarda el valor calculado en la posición para el canal actual
        }else{
            elecaux.lecAuxMed.add(indx,reg.lecaux); //Guarda el primer valor que llega en la posición para el canal actual
        }
        indx++;
        elecaux.setIndexlec(indx);
        elecaux.setVreg(vreg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elecaux;
    }
    
}
