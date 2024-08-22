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
import Entidades.ERegistroEvento;
import Entidades.Electura;
import Entidades.EtipoCanal;
import static java.lang.Thread.sleep;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Lenovo
 */
public class LeerRemotoTCPHexingStruct {
    
    private final String name = "Default_1"; 
    private boolean hasSec;
    
    private final ZoneId zid;
    private final long ndesfase;
    //variables constructor
    EMedidor med;
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
    Abortar objabortar;
    
    //parámetros DLMS
    int numBytesDir = 1; //número de bytes para la dirección del medidor
    int InvokeIDandParity = 193; //0xC1 := 193
    int indxLength = 2;
    int users[] = {2}; //dirección origen - administrador para Hexing es "1" (solo para HLS) - Reading "2" (solo para LLS)
    
    public ProcesosSesion ps;
    ProcesosDLMS DLMS;
    Thread escucha = null;
    Thread envia = null;
    
    public ArrayList<String> DLMS_PrincipalOBIS;
    private final Object monitor = new Object();

    public LeerRemotoTCPHexingStruct(EMedidor med, boolean perfil, boolean eventos, boolean registros, boolean lconfhora, boolean lacumulados,  boolean ldisconnect, boolean lreconnect, int indx, boolean aviso, ControlProcesos cp, Abortar objabortar, String usuario, ZoneId zid, long ndesfase) throws InterruptedException{
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
        
        users[0] = Integer.parseInt((med.getDireccionCliente()== null ? "1" : med.getDireccionCliente()));
        numBytesDir = med.getBytesdireccion().equals("5") ? numBytesDir : Integer.parseInt( med.getBytesdireccion() ); 
        hasSec = med.getSeguridad();
        epp = new EParamProtocolos(this);
        epp.inicializarAARQ();
        epp.inicializarOBIS();
        epp.setType("DLMS");
        epp.setDLMS_numBytesDir(numBytesDir);
        epp.setDLMS_InvokeIDandParity(InvokeIDandParity);
        epp.setDLMS_indxLength(indxLength);
        epp.setUsers(users);
        epp.DLMS_sumaInvokeIDnP = false;
        DLMS_PrincipalOBIS = new ArrayList<>();
        epp.editDLMS_PrincipalOBIS(2, "0100630100FF"); //perfil - 1
        epp.editDLMS_PrincipalOBIS(6, "0000636204FF"); //eventos - Power Quality Log
        epp.editDLMS_PrincipalOBIS(7, "0100630200FF"); //registros - Diarios - 
        epp.editDLMS_PrincipalOBIS(8, "0000620100FF"); //registros - Mensuales

        
        ps = new ProcesosSesion(med, lperfil, leventos, lregistros, lconfhora, lacumulados, ldisconnect, lreconnect, indx, aviso, cp, objabortar, usuario, epp, zid, ndesfase, monitor);
        if (!ps.leer) {
            ps.escribir("Inicialización de Procesos de sesión fallida");
            return;
        }
        DLMS = new ProcesosDLMS(epp,med,ps,cp,usuario, name, hasSec);
        if (!ps.leer) {
            ps.escribir("Inicialización de Procesos DLMS fallida");
            return;
        }
        byte[] addField = {(byte) 0x01};
        epp.editCamposAARQ("acseProtocolVersion", addField);
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
    
    public void procesaDataPerfil(String dato){
        
        if (DLMS.l1 == 0) {
            DLMS.indicecanal = 0;
            try {
                //System.out.println("dato original " + dato);
                dato = DLMS.sdf2.format(DLMS.fecha.parse(ps.Hex2Date(dato)));
                //System.out.println("Fecha intervalo " + dato);
                DLMS.fechaintervalo = new Timestamp(DLMS.sdf2.parse(dato).getTime());
                if (DLMS.fechaintervalo.getMinutes() % DLMS.intervalo != 0) {
                    dato = DLMS.sdf4.format(new Date(DLMS.fechaintervalo.getTime())) + ":" + (DLMS.fechaintervalo.getMinutes() - (DLMS.fechaintervalo.getMinutes() % DLMS.intervalo));
                    DLMS.fechaintervalo = new Timestamp(DLMS.sdf2.parse(dato).getTime());
                    //System.out.println("Fecha intervalo aproximado" + dato);
                }
                if (DLMS.ultimoIntervalo == null) {
                    DLMS.ultimoIntervalo = DLMS.fechaintervalo;
                }
                //manejo de huecos
                int aumento = 0;
                DLMS.fechaCero = null;
                try {
                    if (DLMS.ultimoIntervalo != null) {//si tiene una fecha inicial
                        aumento = (int) Math.abs(((DLMS.fechaintervalo.getTime() - DLMS.ultimoIntervalo.getTime()) / 60000) / DLMS.intervalo) - 1;//se calcula si el intervalo actual es superior e 1 intervalo de integracion 
                        if (aumento > 0) {//obtiene el numero de intervalos a mover
                            for (int i = 0; i < aumento; i++) {
                                DLMS.fechaCero = new Timestamp(DLMS.ultimoIntervalo.getTime() + (60000 * DLMS.intervalo) * (i + 1));//movemos la fecha por cada intervalo faltante
                                //System.out.println("fecha intervalo en 0 " + DLMS.fechaCero);
                                for (int k = 0; k < DLMS.obis.size(); k++) {//se recorren la cantidad de canales programados en el medidor
                                    try {
                                        DLMS.econske = cp.buscarConskeLong(DLMS.lconske, Long.parseLong(DLMS.obis.get(k), 16)); //buscamos el valor de la constante creada en telesimex
                                        if (DLMS.econske != null) {
                                            DLMS.canal = "";
                                            for (EtipoCanal et : DLMS.vtipocanal) {//buscamos la unidad del canal
                                                if (Long.parseLong(et.getCanal()) == Long.parseLong(DLMS.obis.get(k), 16)) {
                                                    DLMS.canal = et.getUnidad();
                                                    break;
                                                }
                                            }
                                            if (DLMS.canal.length() > 0) {
                                                DLMS.lec = new Electura(DLMS.fechaCero, med.getnSerie(), Long.parseLong(DLMS.obis.get(k), 16), 0.0, 0, DLMS.intervalo, DLMS.canal);//lectura del canal en 0                                                    
                                                DLMS.vlec.add(DLMS.lec);
                                            } else {
                                                //System.out.println("Constante");
                                            }

                                        }
                                    } catch (Exception e) {
                                        System.err.println(e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                DLMS.ultimoIntervalo = DLMS.fechaintervalo;

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            DLMS.indicecanal++;
            try {
                dato = String.valueOf(Long.parseLong(dato, 16));
                //System.out.println(dato);
                DLMS.econske = cp.buscarConskeLong(DLMS.lconske, Long.parseLong(DLMS.obis.get(DLMS.indicecanal), 16));
                if (DLMS.econske != null) {
                    DLMS.canal = "";
                    for (EtipoCanal et : DLMS.vtipocanal) {
                        if (Long.parseLong(et.getCanal()) == Long.parseLong(DLMS.obis.get(DLMS.indicecanal), 16)) {
                            DLMS.canal = et.getUnidad();
                            //System.out.println("Canal " + et.getCanal() + " Unidad " + DLMS.canal);
                            break;
                        }
                    }
                    if (DLMS.fechaintervalo != null && DLMS.canal.length() > 0) {
                        if (DLMS.constanteOk){
                            //System.out.println("valor " + cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(DLMS.conskePerfil.get(DLMS.indicecanal), 16) & 0xFF))), DLMS.econske.getPesopulso(), DLMS.econske.getMultiplo(), DLMS.econske.getDivisor()));
                            //System.out.println("Unidad " + DLMS.unidad.get(DLMS.indicecanal));
                            DLMS.lec = new Electura(DLMS.fechaintervalo, med.getnSerie(), Long.parseLong(DLMS.obis.get(DLMS.indicecanal), 16), cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(DLMS.conskePerfil.get(DLMS.indicecanal), 16) & 0xFF))), DLMS.econske.getPesopulso(), DLMS.econske.getMultiplo(), DLMS.econske.getDivisor()), Double.parseDouble(dato), DLMS.intervalo, DLMS.canal);
                            if (Integer.parseInt(DLMS.unidad.get(DLMS.indicecanal), 16) == 30 || Integer.parseInt(DLMS.unidad.get(DLMS.indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                DLMS.lec.lec = DLMS.lec.lec / 1000.0;
                            }
                            DLMS.vlec.add(DLMS.lec);
                        }else{ //
                            //System.out.println("Sin Scaler-unit");
                            //System.out.println("\nConversion desde base de datos: ");
                            //System.out.println("valor " +cp.trasnformarEnergia(Double.parseDouble(dato), DLMS.econske.getPesopulso(), DLMS.econske.getMultiplo(), DLMS.econske.getDivisor()));
                            //System.out.println("Unidad " + DLMS.canal);
                            DLMS.lec = new Electura(DLMS.fechaintervalo, med.getnSerie(), Long.parseLong(DLMS.obis.get(DLMS.indicecanal), 16), cp.trasnformarEnergia(Double.parseDouble(dato), DLMS.econske.getPesopulso(), DLMS.econske.getMultiplo(), DLMS.econske.getDivisor()), Double.parseDouble(dato), DLMS.intervalo, DLMS.canal);
                            if (DLMS.canal.contains("k")||DLMS.canal.contains("K")) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                DLMS.lec.lec = DLMS.lec.lec / 1000.0;
                            }
                            DLMS.vlec.add(DLMS.lec);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        }
    }
    
    public void procesaDataRegistros(String dato){ //En desarrollo
        if (DLMS.l1 == 0) {
            DLMS.indicecanal = 0;
            try {
                ////System.out.println(" indicecanal " + indicecanal);
                //System.out.println("dato original: " + dato);
                dato = DLMS.sdf2.format(DLMS.fecha.parse(ps.Hex2Date(dato)));
                //System.out.println("Fecha intervalo: " + dato);
                DLMS.fechaintervalo = new Timestamp(DLMS.sdf2.parse(dato).getTime());
                //revisar si aproximar o no el intervalo para registros
//                        if (fechaintervalo.getMinutes() % intervalo != 0) {
//                            dato = sdf4.format(new Date(fechaintervalo.getTime())) + ":" + (fechaintervalo.getMinutes() - (fechaintervalo.getMinutes() % intervalo));
//                            fechaintervalo = new Timestamp(sdf2.parse(dato).getTime());
////                            System.out.println("Fecha intervalo aproximado" + dato);
//                        }
                if (DLMS.ultimoIntervalo == null) {
                    DLMS.ultimoIntervalo = DLMS.fechaintervalo;
                }
                //manejo de huecos para registros
                int aumento = 0;
                DLMS.fechaCero = null;
                        try {
                            if (DLMS.ultimoIntervalo != null) {//si tiene una fecha inicial
                                aumento = (int) Math.abs(((DLMS.fechaintervalo.getTime() - DLMS.ultimoIntervalo.getTime()) / 60000) / DLMS.intervalo) - 1;//se calcula si el intervalo actual es superior e 1 intervalo de integracion 
                                if (aumento > 0) {//obtiene el numero de intervalos a mover
                                    for (int i = 0; i < aumento; i++) {
                                        DLMS.fechaCero = new Timestamp(DLMS.ultimoIntervalo.getTime() + (60000 * DLMS.intervalo) * (i + 1));//movemos la fecha por cada intervalo faltante
                                        //System.out.println("fecha intervalo en 0 " + DLMS.fechaCero);
                                        for (int k = 0; k < DLMS.obis.size(); k++) {//se recorren la cantidad de canales programados en el medidor
                                            try {
                                                DLMS.canal = "";
                                                for (EtipoCanal et : DLMS.vtiporegistros) {//buscamos la unidad del canal
                                                    if (Long.parseLong(et.getCanal()) == Long.parseLong(DLMS.obis.get(k), 16)) {
                                                        DLMS.canal = et.getUnidad();
                                                        break;
                                                    }
                                                }
                                                if (DLMS.canal.length() > 0) {
                                                    //lectura del canal en 0
                                                    DLMS.reg = new ERegistro( med.getnSerie(),  DLMS.tiporegistros,  DLMS.fechaCero.getYear() + 1900, DLMS.fechaCero.getMonth() + 1,  DLMS.fechaCero.getDate(),  0.0,  Long.parseLong(DLMS.obis.get(k), 16),  DLMS.canal,  0.0);
                                                    DLMS.vreg.add(DLMS.reg);
                                                } else {
                                                    //System.out.println("Constante");
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
                DLMS.ultimoIntervalo = DLMS.fechaintervalo;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            DLMS.indicecanal++;
            ////System.out.println(" indicecanal " + indicecanal);
            try {
                dato = String.valueOf(Long.parseLong(dato, 16));
                //System.out.println(dato);
                    DLMS.canal = "";
                    for (EtipoCanal et : DLMS.vtiporegistros) {
////                                System.out.println("Canal tcun " + et.getCanal());
////                                System.out.println("OBIS " + Long.parseLong(obis.get(indicecanal), 16));
                        if (Long.parseLong(et.getCanal()) == Long.parseLong(DLMS.obis.get(DLMS.indicecanal), 16)) {
                            DLMS.canal = et.getUnidad();
                            //System.out.println("Canal: " + et.getCanal() + " - Unidad: " + DLMS.canal);
                            break;
                        }
                    }
                    if (DLMS.fechaintervalo != null && DLMS.canal.length() > 0) {
                            //System.out.println("valor: " + cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(DLMS.conske.get(DLMS.indicecanal), 16) & 0xFF))), DLMS.econske.getPesopulso(), DLMS.econske.getMultiplo(), DLMS.econske.getDivisor()));
                            //System.out.println("Unidad: " + DLMS.unidad.get(DLMS.indicecanal));
                            DLMS.reg = new ERegistro( med.getnSerie(),  DLMS.tiporegistros,  DLMS.fechaintervalo.getYear() + 1900, DLMS.fechaintervalo.getMonth() + 1,  DLMS.fechaintervalo.getDate(),  Double.parseDouble(dato),  Long.parseLong(DLMS.obis.get(DLMS.indicecanal), 16),  DLMS.canal,  cp.trasnformarEnergia(Double.parseDouble(dato) * Math.pow(10, cp.ActarisComplemento2((byte) (Integer.parseInt(DLMS.conske.get(DLMS.indicecanal), 16) & 0xFF))), DLMS.econske.getPesopulso(), DLMS.econske.getMultiplo(), DLMS.econske.getDivisor()));
                            if (Integer.parseInt(DLMS.unidad.get(DLMS.indicecanal), 16) == 30 || Integer.parseInt(DLMS.unidad.get(DLMS.indicecanal), 16) == 32) {//es energia activa o reactiva la unidad se divide por 1000 para pasar a kilos
                                DLMS.reg.energia = DLMS.reg.energia / 1000.0;
                            }
                            DLMS.vreg.add(DLMS.reg);
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    
    public void procesaDataEventos(String dato){ //verificar los códigos de eventos - 25 down - 26 up
        if (DLMS.l1 == 0) {
            try {
                DLMS.fechaEvento = DLMS.fecha.parse(ps.Hex2Date(dato));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else if (DLMS.l1 == 1) {
            try {
                if (dato.equals("26")) {
                    //System.out.println("Power up: " + DLMS.fechaEvento);
                    DLMS.regEvento.vcfechareconexion = new java.sql.Timestamp(DLMS.fechaEvento.getTime());
                    if (DLMS.regEvento.vcfechacorte != null && DLMS.regEvento.vcfechareconexion != null) {
                        //System.out.println(DLMS.fecha.format(DLMS.regEvento.vcfechacorte) + "-" + DLMS.fecha.format(DLMS.regEvento.vcfechareconexion));
                        DLMS.regEvento.vcserie = DLMS.seriemedidor;
                        DLMS.regEvento.vctipo = "0001";
                        cp.actualizaEvento(DLMS.regEvento, null);
                        //System.out.println("termina actualiza");
                        DLMS.listRegEventos.add(DLMS.regEvento); //la lista en realidad no se usa, ya que se almacena en la base cada par corte-reconexión que se interpreta
                        DLMS.regEvento = new ERegistroEvento();
                        DLMS.regEvento.vcfechacorte = null;
                        DLMS.regEvento.vcfechareconexion = null;
                    }
                } else if (dato.equals("25")) {
                    //System.out.println("Power down: " + DLMS.fechaEvento);
                    DLMS.regEvento = new ERegistroEvento();
                    DLMS.regEvento.vcfechareconexion = null;
                    DLMS.regEvento.vcfechacorte = new java.sql.Timestamp(DLMS.fechaEvento.getTime());
                } else {// Códigos restantes para el perfil de eventos Power Quality
                    //Descomentar para guardar el resto de eventos //VIC 12-07-19
                           //System.out.println("codigo de evento "+dato); 
                   DLMS.regEvento.vcfechacorte = new java.sql.Timestamp(DLMS.fechaEvento.getTime());
                   DLMS.regEvento.vcfechareconexion = DLMS.regEvento.vcfechacorte;
                   DLMS.regEvento.vctipo = dato;
                    if (DLMS.regEvento.vcfechacorte != null && DLMS.regEvento.vctipo != null) {
                        //Descomentar para guardar el resto de eventos //VIC 12-07-19
////                                System.out.println(fecha.format(regEvento.vcfechacorte) + ", " + regEvento.vctipo);
                        DLMS.regEvento.vcserie = DLMS.seriemedidor;
                        //Descomentar para guardar el resto de eventos //VIC 12-07-19
//                                avisoStr("Almacenando Evento");
//                                cp.actualizaEvento(regEvento, null);
////                                System.out.println("termina actualiza");
                        DLMS.listRegEventos.add(DLMS.regEvento);
                        DLMS.regEvento = new ERegistroEvento();
                        DLMS.regEvento.vcfechacorte = null;
                        DLMS.regEvento.vcfechareconexion = null;
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        //
    }
 
}
