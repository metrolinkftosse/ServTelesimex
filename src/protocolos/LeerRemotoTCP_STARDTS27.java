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
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marav
 */
public class LeerRemotoTCP_STARDTS27 {
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
    boolean salir = false;
    int indx = 0;
    int ndias;
    Abortar objabortar;
    
    //parámetros DLMS
    int PhyAddClass = 1;
    int numBytesDir = 4; //número de bytes para la dirección del medidor
    int InvokeIDandParity = 193; //0xC1 := 193
    int indxLength = 2;
    int users[] = {2,1}; //dirección origen - "2" Local Reading client  
    double resolucion = 1000.0;
    int nsession = 1;
    
    //HLS
    int lengthRandomString = 8;
//    byte[] EK_HLS = {(byte) 0x7C, (byte) 0x71, (byte) 0x35, (byte) 0x7C, (byte) 0x72, (byte) 0x56, (byte) 0x79, (byte) 0x3F, (byte) 0x76, (byte) 0x77, (byte) 0x30, 
//                    (byte) 0x7C, (byte) 0x4B, (byte) 0x51, (byte) 0x50, (byte) 0x32};
//    byte[] AK_HLS = {(byte) 0x5D, (byte) 0x41, (byte) 0x46, (byte) 0x78, (byte) 0x6A, (byte) 0x5A, (byte) 0x63, (byte) 0x35, (byte) 0x6A, (byte) 0x39, (byte) 0x64, 
//                    (byte) 0x3D, (byte) 0x32, (byte) 0x55, (byte) 0x5A, (byte) 0x25};
    private LinkedHashMap<Integer, String> parametrosAvanzadosPorMedidor = new LinkedHashMap<>();
    public ProcesosSesion ps;
    ProcesosDLMS DLMS;
    Thread escucha = null;
    Thread envia = null;
    
    public ArrayList<String> DLMS_PrincipalOBIS;
    public ArrayList<String> DLMS_PrincipalEvents;
    private final Object monitor = new Object();
    
    public LeerRemotoTCP_STARDTS27 (EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, boolean lacumulados, boolean ldisconnect, boolean lreconnect, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) throws InterruptedException{
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
        DLMS_PrincipalOBIS = new ArrayList<>();
        epp.editDLMS_PrincipalOBIS(0, "0000600100FF"); //Serial
        epp.editDLMS_PrincipalOBIS(2, "0100630100FF"); //perfil - 1
//        epp.editDLMS_PrincipalOBIS(3, ""); //Phy Address
        epp.editDLMS_PrincipalOBIS(4, "00002B0100FF"); 
        epp.editDLMS_PrincipalOBIS(6, "0000636200FF"); //eventos - Log 1
//        epp.editDLMS_PrincipalOBIS(7, ""); //registros - Diarios - 
//        epp.editDLMS_PrincipalOBIS(8, ""); //registros - Mensuales - Billing profile  
        epp.editDLMS_PrincipalOBIS(10, "000060030AFF"); //corte
        epp.editDLMS_PrincipalOBIS(11, "0100000200FF"); //Firmware
        DLMS_PrincipalEvents = new ArrayList<>();
        DLMS_PrincipalEvents.add("01"); //down
        DLMS_PrincipalEvents.add("02"); //up
        epp.setDLMS_PrincipalEvents(DLMS_PrincipalEvents);
        epp.setResolucion(resolucion);
        try{
            epp.setParametrosAvanzadosPorMedidor(med.getParametrosAvanzadosPorMedidor());
        } catch (Exception e) {
            salir=true;
        }
        if (!salir){
            epp.inicializarParametrosAvanzadosPorMedidor();

            ps = new ProcesosSesion(med, lperfil, leventos, lregistros, lconfhora, lacumulados, ldisconnect, lreconnect, indx, aviso, cp, objabortar, usuario, epp, zid, ndesfase, monitor);
            if (!ps.leer) {
                ps.escribir("Inicialización de Procesos de sesión fallida");
                return;
            }
            DLMS = new ProcesosDLMS(epp,med,ps,cp,usuario,name, hasSec);
            if (!ps.leer) {
                ps.escribir("Inicialización de Procesos DLMS fallida");
                return;
            }
            
            DLMS.setSC_HLS(epp.DLMS_SecurityControl_HLS[0]);//Security Control Byte
            DLMS.setEK_HLS(epp.DLMS_EK_HLS);
            DLMS.setAK_HLS(epp.DLMS_AK_HLS);
            setAARQFields(nsession);

            
            escuchainit();
            enviaSNRMinit();//Hilo
            escucha.start();
            envia.start();           
        } else {
            ps = new ProcesosSesion();
            ps.leer=false;
        }
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
                            if (hasSec & nsession == 2 & DLMS.getNSession() == 1) {
                                epp.inicializarAARQ();
                                nsession = 1;
                                setAARQFields(nsession);
                                DLMS.epp.setCamposAARQ(epp.getCamposAARQ());
                            }
                            if (ps.enviaPrimeraTrama) {
                                DLMS.enviaSNRM1();
                            }
                            if (ps.lenviaTrama2) {
                                if (DLMS.lReset && DLMS.getNSession() == 2) {
                                    nsession = 2;
                                    setAARQFields(nsession);
                                }
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

    public void setAARQFields(int nSession){
        byte[] addField = {(byte) 0x01};
        byte[] versionDLMS = {(byte) 0x06};
        byte[] conformance = {(byte) 0x00, (byte) 0xFE, (byte) 0x1F};
        byte[] pduSize = {(byte) 0x08, (byte) 0x00};     
        if (nSession == 1){
            if (hasSec) {
                users[0] = epp.DLMS_ClientS1_HLS[0];//16
                epp.setUsers(users);
                byte[] conformanceSec = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
                byte[] pduSizeSec = {(byte) 0xFF, (byte) 0xFF};     
                epp.editCamposAARQ("appContextName", addField);
                epp.editCamposAARQ("userInfo", addField);
                epp.editCamposAARQ("proposedDLMSVersion", versionDLMS);    
                epp.editCamposAARQ("proposedConformance", conformanceSec);
                epp.editCamposAARQ("clientMaxRxPDUSize", pduSizeSec);   
            } else {
                epp.editCamposAARQ("appContextName", addField);
                epp.editCamposAARQ("acseReq", addField);
                epp.editCamposAARQ("mechName", addField);
                epp.editCamposAARQ("authenticationValue", addField);
                epp.editCamposAARQ("userInfo", addField);
                epp.editCamposAARQ("proposedDLMSVersion", versionDLMS);
                epp.editCamposAARQ("proposedConformance", conformance);
                epp.editCamposAARQ("clientMaxRxPDUSize", pduSize);  
            }        
        } else if (nSession == 2){
            byte[] appCN = {(byte) 0x03};
            byte[] ApTitle = {(byte) 0x53, (byte) 0x54, (byte) 0x41, (byte) 0xAC, (byte) 0x10, (byte) 0x00, (byte) 0x0F, (byte) 0xA2};//Podría parametrizarse
            byte[] mechName = {(byte) 0x05};
            users[0] = Integer.parseInt((med.getDireccionCliente()== null ? "1" : med.getDireccionCliente()));//(byte) 0x01;
            epp.setUsers(users);
            String CtoS = generateRandomString(lengthRandomString);
            byte[] authValue = CtoS.getBytes();
            epp.editCamposAARQ("appContextName", appCN);
            epp.editCamposAARQ("callingAPTitle", ApTitle);
            epp.editCamposAARQ("acseReq", addField);
            epp.editCamposAARQ("mechName", mechName);
            epp.editCamposAARQ("authenticationValue", authValue);
            epp.editCamposAARQ("userInfo", addField);
            epp.editCamposAARQ("proposedDLMSVersion", versionDLMS);    
            epp.editCamposAARQ("proposedConformance", conformance);
            epp.editCamposAARQ("clientMaxRxPDUSize", pduSize);
        }           
    }
    
    public String generateRandomString(int length){
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = length;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++){
            int randomLimitedInt = leftLimit + (int)(random.nextFloat() *(rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
//        generatedString="E2KCD12I";
        //System.out.println("generatedString: "+generatedString);
        return generatedString;  
    }
}
