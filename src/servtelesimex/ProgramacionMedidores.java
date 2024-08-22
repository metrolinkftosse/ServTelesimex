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
import Entidades.EMedidorProgramado;
import Entidades.Ecaller;
import Entidades.Eprogramacion;
import Entidades.ProgramacionesActivas;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dperez
 */
public class ProgramacionMedidores extends Thread {

    //ControlProcesos cp;
    ControlProcesos cpprogramacion;
    private static ZoneId zid;
    private static int numConTCP;
    private static int numDiasBorrado;
    private static String host;
    private static String mac;
    private static List<String> macList;
    private static long ndesfase;

    private boolean lenprogramacion;
    private boolean progleida;
    private TimerTask tarea;
    private Timer task;
    private long timer = 0;
    private long timerfin = 0;
    private ArrayList<Ecaller> caller;
    private ArrayList<Ecaller> callerAux;
    private ArrayList<String> allMeds;
    private List<Integer> allBrandsModemsFrame;
    private ArrayList<ArrayList<Ecaller>> colasTCP;
    private ArrayList<ArrayList<Ecaller>> colasPSTN;

    public Abortar objabortar = new Abortar();
    private int medidoresleidos = 0;
    private int totalMedidoresProg = 0;
    private final SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    private File filep;
    Date inicioIntento;
    Date finIntento;
    public long duracionIntento = 0;
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");
    private String fechaProgramacion;
    private Timestamp currentDate;
    private final ProgramacionesActivas aProg;
    private Eprogramacion prog;
    private final String version_IDrev;
    private static final AtomicInteger simProgs = new AtomicInteger(0);

    public ProgramacionMedidores(String version_IDrev, ProgramacionesActivas aProg, ControlProcesos cpprogramacion) {
        this.version_IDrev = version_IDrev;
        this.aProg = aProg;
        this.cpprogramacion = cpprogramacion;
        this.lenprogramacion = true;
    }

    public static ZoneId getZid() {
        return ProgramacionMedidores.zid;
    }

    public static void setZid(ZoneId zid) {
        ProgramacionMedidores.zid = zid;
    }

    public static int getNumConTCP() {
        return ProgramacionMedidores.numConTCP;
    }

    public static void setNumConTCP(int numConTCP) {
        ProgramacionMedidores.numConTCP = numConTCP;
    }

    public static int getNumDiasBorrado() {
        return ProgramacionMedidores.numDiasBorrado;
    }

    public static void setNumDiasBorrado(int numDiasBorrado) {
        ProgramacionMedidores.numDiasBorrado = numDiasBorrado;
    }

    public static String getHost() {
        return ProgramacionMedidores.host;
    }

    public static void setHost(String host) {
        ProgramacionMedidores.host = host;
    }
        

    public static String getMac() {
        return ProgramacionMedidores.mac;
    }

    public static void setMac(String mac) {
        ProgramacionMedidores.mac = mac;
    }

    public static List<String> getMacList() {
        return macList;
    }

    public static void setMacList(List<String> macList) {
        ProgramacionMedidores.macList = macList;
    }        

    public static long getNDesfase() {
        return ProgramacionMedidores.ndesfase;
    }

    public static void setNDesfase(long ndesfase) {
        ProgramacionMedidores.ndesfase = ndesfase;
    }

    public static int getProgsSimultaneas() {
        return ProgramacionMedidores.simProgs.get();
    }

    public static void setProgsSimultaneas(int simProgs) {
        ProgramacionMedidores.simProgs.set(simProgs);
    }

    public static void incrementaProgsSimultanes(){
        while(true){
            int currentValue = getProgsSimultaneas();
            int newValue = currentValue + 1;
            if (simProgs.compareAndSet(currentValue,newValue)){
                return;
            }
        }   
    }
    
    public static void decrementaProgsSimultaneas(){
        while(true){
            int currentValue = getProgsSimultaneas();
            int newValue = currentValue - 1;
            if (simProgs.compareAndSet(currentValue,newValue)){
                return;
            }
        }
    }

    @Override
    public void run() {
        cpprogramacion.inicio();
        File f = new File(cpprogramacion.rutalogs + "" + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/PROGRAMACIONES" + "/");
        if (!f.exists()) {
            f.mkdirs();
        }
        System.out.println("Programación recibida: " + aProg.getProgramacion());

        //FECHA DE LA PROGRAMACION        
        fechaProgramacion = sdf3.format( obtenerHora() ) + " 00:00";
        try {
            currentDate = new Timestamp( sdf2.parse(fechaProgramacion).getTime() );
        } catch (ParseException ex) {
            System.err.println("Fecha: " + new Date() );
            ex.printStackTrace();
        }
        String codProg = aProg.getProgramacion();
        System.out.println("Inicia " + codProg);
        if (lenprogramacion) {
            //cpprogramacion.restartConn();
            try {
                prog = cpprogramacion.buscarProgramacion(codProg);
            } catch (Exception e) {
                cpprogramacion.restartConn();
                prog = cpprogramacion.buscarProgramacion(codProg);
            }
            try {
                try {
                    String[] cmd = {"powercfg", "-change", "-standby", "-timeout", "-ac", "0"}; //Comando de apagado en windows
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException ioe) {
                    System.err.println("Fecha: " + new Date() );
                    ioe.printStackTrace();
                }
                if (prog != null) {
                    filep = new File(cpprogramacion.rutalogs + "" + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/PROGRAMACIONES" + "/" + codProg + ".txt");
                    filep.createNewFile();
                    try {
                        Thread.sleep(1000);
                        if (!filep.exists()) {
                            System.out.println("File not found");
                            filep = new File(cpprogramacion.rutalogs + "" + sdfarchivo.format(Date.from(ZonedDateTime.now(zid).toInstant())) + "/PROGRAMACIONES" + "/" + codProg + ".txt");
                            filep.createNewFile();
                        }
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                    Date dpc = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                    Date dprog = sdf2.parse(sdf3.format(Date.from(ZonedDateTime.now(zid).toInstant())) + " " + prog.getHoraini());
                    Date fin = sdf2.parse(sdf3.format(Date.from(ZonedDateTime.now(zid).toInstant())) + " " + prog.getHorafin());
                    cpprogramacion.escribir(filep, "++++++++++++++++++++++++++ REPORTE DE PROGRAMACION ++++++++++++++++++++++++++");
                    cpprogramacion.escribir(filep, version_IDrev);
                    cpprogramacion.escribir(filep, "No. de Repeticiones parametrizadas: " + prog.repeticiones);
                    cpprogramacion.escribir(filep, "Tiempo de espera parametrizado [min]: " + prog.espera);
                    System.out.println("++++++++++++++++++++++++++");
                    System.out.println("Hora actual " + dpc);
                    System.out.println("Hora inicio " + dprog);
                    cpprogramacion.escribir(filep, "Hora programada para inicio: " + dprog);
                    System.out.println("Hora fin " + fin);
                    cpprogramacion.escribir(filep, "Hora programada para fin: " + fin);
                    System.out.println("++++++++++++++++++++++++++");
                    timer = dprog.getTime() - dpc.getTime();
                    //timerfin = fin.getTime() - dprog.getTime();
                    final String codigoProgramacion = codProg;
                    progleida = true;
                    tarea = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                construircallerProg(codigoProgramacion, prog.getRepeticiones(), prog.getEspera(), fin);
                            } catch (ParseException ex) {
                                System.err.println("Fecha: " + new Date() );
                                ex.printStackTrace();
                            } catch (InterruptedException ex) {
                                System.err.println("Fecha: " + new Date() );
                                ex.printStackTrace();
                            }
                            System.out.println("Sale caller");
                            lenprogramacion = false;
                        }
                    };
                    task = new java.util.Timer();
                    task.schedule(tarea, timer);                   
                    while (progleida) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.err.println("Fecha: " + new Date() );
                            e.printStackTrace();
                        }
                        dpc = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                        if (dpc.after(fin) && !objabortar.labortar) {
                            objabortar.labortar = true;
                            try {
                                cpprogramacion.cambiarEstadoProg(codigoProgramacion);
                            } catch (Exception e) {
                                cpprogramacion.restartConn();
                                cpprogramacion.cambiarEstadoProg(codigoProgramacion);
                                System.err.println("Fecha: " + new Date() );
                                e.printStackTrace();
                            }
                        }
                        if (!lenprogramacion) {
                            objabortar.labortar = false;
                            progleida = false;
                            task.cancel();
                        }
                    }
                    ProgramacionMedidores.decrementaProgsSimultaneas();
                    System.out.println("Programación finalizada");
                    cpprogramacion.escribir(filep, "Programación finalizada");
                    LeeListaMedidoresProg.deleteConnCounter2Prog( codProg );
                    task = null;
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Programacion no encontrada");
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }
        System.out.println("Cerrar Conexion");
        cpprogramacion.cerrarConexion();
        this.pasarGarbageCollector();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public void pasarGarbageCollector() {

        Runtime garbage = Runtime.getRuntime();
        //System.out.println("Memoria libre antes de limpieza: " + garbage.freeMemory());
        garbage.gc();
        //System.out.println("Memoria libre tras la limpieza: " + garbage.freeMemory());
    }

    private void construircallerProg(String codigoProgramacion, int repeticiones, int espera, Date horafin) throws ParseException, InterruptedException {
        String idProg = aProg.getProgramacion();
        LeeListaMedidoresProg.assignConnCounter2Prog( idProg );
        medidoresleidos = 0;
        totalMedidoresProg = 0;
        EMedidor med = null;
        Vector<EConfModem> confmodem = new Vector();
        //**************************
        SimpleDateFormat sdffile = new SimpleDateFormat("yyyy-MM-dd");
        File file = new File(cpprogramacion.rutalogs);
        File[] files = file.listFiles();
        System.out.println("numero de directorios");
        Date fecfile = null;
        Date fecborrado = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));

        try {
            fecborrado = sdffile.parse(sdffile.format(sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant()))).getTime() - ((long) (86400000) * numDiasBorrado)));
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        System.out.println("Fecha a comparar " + sdffile.format(fecborrado));
        for (File archivotofind : files) {
            try {
                System.out.println(archivotofind.getName());
                fecfile = sdffile.parse(archivotofind.getName());
                if (fecborrado.after(fecfile)) {
                    System.out.println("debe borrar " + archivotofind.getName());
                    if (archivotofind.isDirectory()) {
                        System.out.println("es directorio borra");
                        borrarArchivo(archivotofind);
                        archivotofind.delete();
                    }
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }
        /*
        filecaller = new File(cpprogramacion.rutalogs + sdffile.format(sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())))) + "/");
        if (!filecaller.exists()) {
            filecaller.mkdirs();
        }
         */
        //**************************
        ArrayList<EMedidorProgramado> lista;
        try {
            lista = cpprogramacion.buscarAllMedidoresProgamados(codigoProgramacion);
        } catch (Exception e) {
            cpprogramacion.restartConn();
            lista = cpprogramacion.buscarAllMedidoresProgamados(codigoProgramacion);
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        totalMedidoresProg = lista.size();
        caller = new ArrayList();
        callerAux = new ArrayList();
        allMeds = new ArrayList();
        allBrandsModemsFrame = cpprogramacion.getAllBrands_X_ModemsFrame();
        for (EMedidorProgramado medprog : lista) {
            try {                
                med = cpprogramacion.buscarMedidor(medprog.getSerial(), host);
                med.setIdProg(idProg);
            } catch (Exception e) {
                cpprogramacion.restartConn();
                med = cpprogramacion.buscarMedidor(medprog.getSerial(), host);
                med.setIdProg(idProg);
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }

            if (med != null) {
                //Construimos la lista de callers
                System.out.println("Addicionamos a programador " + medprog.getSerial());
                if (med.lconfigurado) {
                    if (binarySearch(allBrandsModemsFrame, Integer.parseInt(med.marcaMedidor.codigo))) {
                        try {
                            confmodem = cpprogramacion.obtenerTramas(med);
                        } catch (Exception e) {
                            cpprogramacion.restartConn();
                            confmodem = cpprogramacion.obtenerTramas(med);
                            System.err.println("Fecha: " + new Date() );
                            e.printStackTrace();
                        }
                    }                    
                    caller.add(new Ecaller(med, confmodem, (medprog.isLperfil()), (medprog.isLeventos()), (medprog.isLregistros()), (medprog.isAcumulados()), (medprog.isConfhora()), (medprog.isDisconnect()), (medprog.isReconnect())));
                    callerAux.add(new Ecaller(med, confmodem, (medprog.isLperfil()), (medprog.isLeventos()), (medprog.isLregistros()), (medprog.isAcumulados()), (medprog.isConfhora()), (medprog.isDisconnect()), (medprog.isReconnect())));
                    allMeds.add(med.getnSerie());
                    cpprogramacion.escribir(filep, medprog.getSerial() + " - Medidor programado para lectura de: " + (medprog.isLperfil() ? "Perfil de carga - " : "x - ") + (medprog.isLeventos() ? "Eventos - " : "x - ") + (medprog.isLregistros() ? "Registros " : "x ") + (medprog.isDisconnect() ? "Desconexión de Relé " : "") + (medprog.isReconnect() ? "Reconexión de Relé " : ""));
                }
            }
        }
        for (int repeticionesRestantes = repeticiones; repeticionesRestantes > 0; repeticionesRestantes--) {
            cpprogramacion.escribir(filep, "Cantidad total de medidores programados para lectura: " + caller.size());
            colasTCP = new ArrayList<>();
            colasTCP.add(new ArrayList<Ecaller>());//TCP Independientes
            colasPSTN = new ArrayList<>();
            boolean aggregatedPSTN;
            boolean aggregatedTCP;
            //revisamos todo el caller 1 x 1 para encontrar los puertos en comun.
            for (int j = 0; j < caller.size(); j++) {
                aggregatedPSTN = false;
                aggregatedTCP = false;
                //este vector va en incremento a medida que se vayan ubicando los medidores (cola de colas)(grafo)
                if (caller.get(j).getMedidor().getTipoconexion() == 0 || caller.get(j).getMedidor().getTipoconexion() == 2) { //Si es GPRS O TCP Independiente
                    colasTCP.get(0).add(caller.get(j));
                } else if (caller.get(j).getMedidor().getTipoconexion() == 1) { // Si es PSTN                
                    if (!colasPSTN.isEmpty()) { // Si no esta vacio el array de colas PSTN
                        for (ArrayList<Ecaller> cola : colasPSTN) { // Recorro el array de colasPSTN, cola a cola
                            //Verificamos si el elemento j de los callers programados tiene el mismo puerto que el primer elemento de la cola seleccionada en el momento.
                            if (caller.get(j).getMedidor().getPuertocomm().equals(cola.get(0).getMedidor().getPuertocomm())) { //Si tienen el mismo puerto
                                cola.add(caller.get(j)); // Añadimos ese caller
                                aggregatedPSTN = true;
                                break; // Y quebramos el for pues ya no necesitamos revisar las demás colas
                            }
                        }
                        if (!aggregatedPSTN) {
                            colasPSTN.add(new ArrayList<Ecaller>());
                            colasPSTN.get(colasPSTN.size() - 1).add(caller.get(j));
                        }
                    } else { // Si esta vacio el array de colas PSTN
                        colasPSTN.add(new ArrayList<Ecaller>()); // Crea la primera cola de  Callers PSTN con el puerto comm del primer Caller PSTN.
                        colasPSTN.get(0).add(caller.get(j));
                    }
                } else { // Si son TCP en cascada
                    if (!colasTCP.isEmpty()) { // Si no esta vacio el array de colas TCP  
                        boolean first = true;
                        for (ArrayList<Ecaller> cola : colasTCP) { // Recorro el array de colasTCP, cola a cola
                            if (first) { //Si es la primera cola, la que es predefinida para TCP independientes, no ejecuta ninguna acción y cambia el booleano
                                first = false;
                            } else {
                                //Verificamos si el elemento j de los callers programados tiene el mismo puerto que el primer elemento de la cola seleccionada en el momento.
                                boolean sameIp = caller.get(j).getMedidor().getDireccionip().equals(cola.get(0).getMedidor().getDireccionip());
                                boolean samePort = caller.get(j).getMedidor().getPuertoip().equals(cola.get(0).getMedidor().getPuertoip());
                                if (sameIp && samePort) { //Si tienen la misma ip y puerto 
                                    cola.add(caller.get(j)); // Añadimos ese caller
                                    aggregatedTCP = true;
                                    break; // Y quebramos el for pues ya no necesitamos revisar las demás colas
                                }
                            }
                        }
                        if (!aggregatedTCP) {
                            colasTCP.add(new ArrayList<Ecaller>());
                            colasTCP.get(colasTCP.size() - 1).add(caller.get(j));
                        }
                    } else { // Si esta vacio el array de colas TCP
                        colasTCP.add(new ArrayList<Ecaller>()); // Crea la primera cola de  Callers TCP con el puerto comm del primer Caller TCP.
                        colasTCP.get(1).add(caller.get(j));
                    }
                }
            }
            cpprogramacion.escribir(filep, "Inicia lanzamiento de lecturas.");
            inicioIntento = sdf2.parse(sdf2.format(new Date()));
            if (totalMedidoresProg != medidoresleidos) {
                int cantMaxIt = Math.max(Math.max(colasTCP.get(0).size(), colasTCP.size() - 1), colasPSTN.size());// La cantidad màxima de iteraciones que deberìa hacer el for siguiente.
                LeeListaMedidoresProg iTCP;
                LeeListaMedidoresProg redTCP;
                LeeListaMedidoresProg PSTN;
                for (int hilo = 0; hilo < cantMaxIt; hilo++) {
                    if (!objabortar.labortar) {
                        if (hilo < colasTCP.get(0).size()) {
                            while (true) {
                                if (LeeListaMedidoresProg.getConexSimultaneas( idProg ) < numConTCP) {
//                                    System.out.println("Lee LeeListaMedidoresProg: " + colasTCP.get(0).get(hilo).getMedidor().getnSerie()+(colasTCP.get(0).get(hilo).isDisconnect()?" - Desconexión de Relé": ""));
                                    iTCP = new LeeListaMedidoresProg(Arrays.asList(colasTCP.get(0).get(hilo)), this, cpprogramacion, filep, horafin);
                                    iTCP.start();
                                    LeeListaMedidoresProg.incrementaConexSimultaneas( idProg );
                                    break;
                                } else {
                                    Thread.sleep(500);
                                    if (objabortar.labortar) {
                                        break;
                                    }
                                }
                            }
                        }

                        if (hilo < colasTCP.size() - 1) {
                            while (true) {
                                if (LeeListaMedidoresProg.getConexSimultaneas( idProg ) < numConTCP) {
                                    redTCP = new LeeListaMedidoresProg(colasTCP.get(hilo + 1), this, cpprogramacion, filep, horafin);
                                    redTCP.start();
                                    LeeListaMedidoresProg.incrementaConexSimultaneas( idProg );
                                    break;
                                } else {
                                    Thread.sleep(500);
                                    if (objabortar.labortar) {
                                        break;
                                    }
                                }
                            }
                        }

                        if (hilo < colasPSTN.size()) {
                            while (true) {
                                if (LeeListaMedidoresProg.getConexSimultaneas( idProg ) < numConTCP) {
                                    PSTN = new LeeListaMedidoresProg(colasPSTN.get(hilo), this, cpprogramacion, filep, horafin);
                                    PSTN.start();
                                    LeeListaMedidoresProg.incrementaConexSimultaneas(idProg );
                                    break;
                                } else {
                                    Thread.sleep(500);
                                    if (objabortar.labortar) {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
                do {
                    System.out.println("Conexiones Simultaneas :" + LeeListaMedidoresProg.getConexSimultaneas( idProg ));
                    Thread.sleep(1000);
                } while (LeeListaMedidoresProg.getConexSimultaneas( idProg ) > 0 && !objabortar.labortar);
                System.out.println("Conexiones Simultaneas :" + LeeListaMedidoresProg.getConexSimultaneas( idProg ));
                caller = new ArrayList();
                for (EMedidorProgramado medprog : lista) {
                    try {
                        med = cpprogramacion.buscarMedidor(medprog.getSerial(), host);
                        if (med != null) {
                            //Adicionamos a una nueva lista los medidores que no fueron leìdos.
                            System.out.println("Adicionamos a programador " + medprog.getSerial());
                            //String lastReadMed = sdf3.format(med.fecha);
                            Timestamp lastReadMed = med.getFecha();
                            if (med.lconfigurado && currentDate.after(lastReadMed)) {
                                if (binarySearch(allBrandsModemsFrame, Integer.parseInt(med.marcaMedidor.codigo))) {
                                    confmodem = cpprogramacion.obtenerTramas(med);
                                }                                
                                caller.add(new Ecaller(med, confmodem, (medprog.isLperfil()), (medprog.isLeventos()), (medprog.isLregistros()), (medprog.isAcumulados()), (medprog.isConfhora()), (medprog.isDisconnect()), (medprog.isReconnect())));
                                cpprogramacion.escribir(filep, medprog.getSerial() + " - Medidor en estado de no leído programado para lectura de: " + (medprog.isLperfil() ? "Perfil de carga - " : "x - ") + (medprog.isLeventos() ? "Eventos - " : "x - ") + (medprog.isLregistros() ? "Registros " : "x ") + (medprog.isDisconnect() ? "Desconexión de Relé " : "") + (medprog.isReconnect() ? "Reconexión de Relé " : ""));
                            }
                        }
                    } catch (Exception e) {
                        cpprogramacion.restartConn();
                        int idxMed = allMeds.indexOf(medprog.getSerial());
                        if (idxMed != -1) {
                            caller.add(callerAux.get(idxMed));
                            cpprogramacion.escribir(filep, medprog.getSerial() + " - Medidor programado por estado incierto debido a fallo en conexiòn con la base de datos. ");
                        }
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                }
                medidoresleidos = totalMedidoresProg - caller.size();
                cpprogramacion.escribir(filep, medidoresleidos + " medidores leìdos de " + totalMedidoresProg + " programados.");
                long nespera = espera * 60;
                int n = 0;
                finIntento = sdf2.parse(sdf2.format(Date.from(ZonedDateTime.now(zid).toInstant())));
                cpprogramacion.escribir(filep, "Fin intento: " + finIntento);
                duracionIntento = finIntento.getTime() - inicioIntento.getTime();
                cpprogramacion.escribir(filep, "Duracion del intento [min]: " + (duracionIntento / 60000));
                cpprogramacion.escribir(filep, "Reintentos restantes: " + (repeticionesRestantes - 1));
                System.out.println("Inicia espera " + Date.from(ZonedDateTime.now(zid).toInstant()));
                cpprogramacion.escribir(filep, "Inicia espera: " + Date.from(ZonedDateTime.now(zid).toInstant()));
                if ((repeticionesRestantes - 1) > 0) {
                    while (true) {
                        if (!objabortar.labortar) {
                            if (n < nespera) {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    System.err.println("Fecha: " + new Date() );
                                    e.printStackTrace();
                                }
                                n++;
                            } else {
                                System.out.println("Termina espera " + Date.from(ZonedDateTime.now(zid).toInstant()));
                                cpprogramacion.escribir(filep, "Termina espera " + Date.from(ZonedDateTime.now(zid).toInstant()));
                                break;
                            }
                        } else {
                            System.out.println("Termina caller");
                            cpprogramacion.escribir(filep, "Finaliza programación por hora programada para fin ó por detención del servicio: " + horafin);
                            try {
                                cpprogramacion.cambiarEstadoProg(codigoProgramacion);
                            } catch (Exception e) {
                                cpprogramacion.restartConn();
                                cpprogramacion.cambiarEstadoProg(codigoProgramacion);
                                System.err.println("Fecha: " + new Date() );
                                e.printStackTrace();
                            }
                            ProgramacionMedidores.decrementaProgsSimultaneas();
                            repeticionesRestantes = 0;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public Timestamp obtenerHora() {
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }

    public boolean binarySearch(List<Integer> brands, int brand2Search) {
        int lo = 0;
        int hi = brands.size() - 1;
        int searchIdx;
        int currBrand;
        while (lo <= hi) {
            searchIdx = lo + (hi - lo) / 2;
            currBrand = brands.get(searchIdx);
            if (brand2Search == currBrand) {
                return true;
            }
            if (brand2Search > currBrand) {
                lo = searchIdx + 1;
            } else {
                hi = searchIdx - 1;
            }
        }
        return false;
    }

    private void borrarArchivo(File archivotofind) {
        File[] ficheros = archivotofind.listFiles();

        for (int x = 0; x < ficheros.length; x++) {
            if (ficheros[x].isDirectory()) {
                borrarArchivo(ficheros[x]);
            }
            ficheros[x].delete();
        }
    }
}
