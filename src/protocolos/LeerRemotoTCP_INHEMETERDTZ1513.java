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
import static java.lang.Thread.sleep;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 *
 * @author marav
 */
public class LeerRemotoTCP_INHEMETERDTZ1513 {
    
    
    private final String name = "Default_1";
    private boolean hasSec;
    
    private final ZoneId zid;
    private final long ndesfase;
    //variables constructor
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
    int PhyAddClass = 17;
    int numBytesDir = 4; //número de bytes para la dirección del medidor
    int InvokeIDandParity = 193; //0x40 := 64 //va hasta 0x4F
    int indxLength = 2;
    int users[] = {18,16}; //dirección origen - 
    double resolucion = 1000.0;
    
    public ProcesosSesion ps;
    ProcesosDLMS DLMS;
    Thread escucha = null;
    Thread envia = null;
    
    public ArrayList<String> DLMS_PrincipalOBIS;
    public ArrayList<String> DLMS_PrincipalEvents;
    private final Object monitor = new Object();
        
    public LeerRemotoTCP_INHEMETERDTZ1513(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, boolean lacumulados,  boolean ldisconnect, boolean lreconnect, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) throws InterruptedException{
        this.med = med;
        this.cp = cp;
        this.usuario = usuario;
        this.lperfil = perfil;
        this.leventos = eventos;
        this.lregistros = registros;
        this.lconfhora = lconfhora;
        this.lacumulados = lacumulados;
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

        users[0] = Integer.parseInt((med.getDireccionCliente() == null ? "18" : med.getDireccionCliente()));
        numBytesDir = med.getBytesdireccion().equals("5") ? numBytesDir : Integer.parseInt(med.getBytesdireccion());
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
        epp.setUsarPowerOffConEstampas(true);
        DLMS_PrincipalOBIS = new ArrayList<>();
        epp.editDLMS_PrincipalOBIS(0, "0000600100FF"); //Serial
        epp.editDLMS_PrincipalOBIS(2, "0100630100FF"); //perfil - 1
        epp.editDLMS_PrincipalOBIS(3, "0001160000FF"); //Phy Address - class 0x17, attr 0x09
        epp.editDLMS_PrincipalOBIS(6, "0100636100FF"); //eventos - Power Off - trae dos estampas de tiempo
        //epp.editDLMS_PrincipalOBIS(7, ""); //registros - Diarios - 
        //epp.editDLMS_PrincipalOBIS(8, ""); //registros - Mensuales - Billing profile       
        DLMS_PrincipalEvents = new ArrayList<>();
        DLMS_PrincipalEvents.add("-"); //No usa códigos
        DLMS_PrincipalEvents.add("-"); //No usa códigos
        epp.setDLMS_PrincipalEvents(DLMS_PrincipalEvents);
        epp.setResolucion(resolucion);
        setAARQFields();
        ////System.out.println("Se instanciará el objeto de Sesión");
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
    
    public void setAARQFields(){
        //Campos AARQ
        byte[] addField = {(byte) 0x01};
        byte[] conformance = {(byte) 0x60, (byte) 0xFE, (byte) 0xDF};
        byte[] pduSize = {(byte) 0x02, (byte) 0x00}; 
        epp.editCamposAARQ("appContextName", addField);
        epp.editCamposAARQ("acseReq", addField);
        epp.editCamposAARQ("mechName", addField);
        epp.editCamposAARQ("authenticationValue", addField);
        epp.editCamposAARQ("userInfo", addField);
        epp.editCamposAARQ("proposedDLMSVersion", addField);
        epp.editCamposAARQ("proposedConformance", conformance);
        epp.editCamposAARQ("clientMaxRxPDUSize", pduSize);
    }
}
