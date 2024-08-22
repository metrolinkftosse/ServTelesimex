/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servtelesimex;

import Control.ControlProcesos;
import Entidades.Abortar;
import Entidades.ProgramacionesActivas;
import Util.ConnectionManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author Metrolink 0
 */
public class ServTelesimex {

    /**
     *
     * @author Metrolink 0
     */
    private static boolean restart = false;
    private static boolean stop = false;
    private static final Object monitor = new Object();

    public static String url;
    public static String user;
    public static String pass;
    public static String db;
    public static String dblinksimex;
    public static String database;
    private static String conexionURL;
    public static String driver;

    private static ZoneId zid;
    private static String zona;    
    private static Timestamp todayColombia;
    private static ControlProcesos cpprogramacion;
    public static Connection conn;
    public static ConnectionManager manager;
    public static String host = "";
    public static String mac = "";
    public static List<String> macList =  new ArrayList<>();    
    private static int lastMinutes;
    private static int numConTCP;
    private static int nDiasBorrado;
    private static long ndesfase;
    public static Abortar objabortar = new Abortar();
    private static final String ID_Revision = "20240201-1150";
    private static final String version = "Programador v3.4";
    private static final String version_IDrev = version + " : " + ID_Revision;
    private static final SimpleDateFormat sdfW = new SimpleDateFormat("yyMMdd.HHmmss");
    private static final String windowID = sdfW.format(new Date());

    public static void start(String[] args) throws InterruptedException {
        synchronized (monitor) {                      
            objabortar.labortar = false;
            System.out.println("Start");
            setIdentificationParameters();
            do {
                restart = false;
                setConnParameters();
                //Zona Horaria
                if (!stop) {
                    try {
                        zona = cpprogramacion.buscarParametrosString(7);
                        zid = ZoneId.of(zona);//"America/Bogota"
                        ProgramacionMedidores.setZid(zid);
                    } catch (Exception e) {
                        System.out.println("Error obteniendo el parámetro de zona horaria.");
                        zona = "America/Bogota";
                        zid = ZoneId.of(zona);
                        ProgramacionMedidores.setZid(zid);
                    }
                } else {
                    return;
                }
                // Tasa de búsqueda de Servicio
                if (!stop) {
                    try {
                        lastMinutes = cpprogramacion.buscarParametros(13);
                    } catch (Exception e) {
                        System.out.println("Error obteniendo el parámetro de Tasa de búsqueda de Servicio Telesimex.");
                        lastMinutes = 5;// 5 minutos por defecto.                        
                    }
                } else {
                    return;
                }
                // Conexiones Simultanéas 
                if (!stop) {
                    try {
                        numConTCP = cpprogramacion.buscarParametros(1);
                        ProgramacionMedidores.setNumConTCP(numConTCP);
                    } catch (Exception e) {
                        System.out.println("Error obteniendo el parámetro Número de Conexiones Simultáneas.");
                        numConTCP = 80;// 80 conexiones por defecto.    
                        ProgramacionMedidores.setNumConTCP(numConTCP);
                    }
                } else {
                    return;
                }
                //Nímero de días de borrado de carpetas                
                if (!stop) {
                    try {
                        nDiasBorrado = cpprogramacion.buscarParametros(2);//numero de dias hacia atras para eliminar carpetas 
                        ProgramacionMedidores.setNumDiasBorrado(nDiasBorrado);
                    } catch (Exception e) {
                        System.out.println("Error obteniendo el parámetro Número de Días de Borrado de logs.");
                        nDiasBorrado = 15; // 15 días por defecto.
                        ProgramacionMedidores.setNumDiasBorrado(nDiasBorrado);
                    }
                } else {
                    return;
                }    
                //Segundos de Desfase Permitidos
                if (!stop) {
                    try {
                        ndesfase = cpprogramacion.buscarParametros(5);//Segundos de Desfase Permitidos 
                        ProgramacionMedidores.setNDesfase(ndesfase);
                    } catch (Exception e) {
                        System.out.println("Error obteniendo el parámetro Segundos de Desfase Permitidos.");
                        ndesfase = 7200; // 15 días por defecto.
                        ProgramacionMedidores.setNDesfase(ndesfase);
                    }
                } else {
                    return;
                }     
                
                while (!stop) {
                    System.out.println("Running");
                    System.out.println("Programaciones simultaneas: " + ProgramacionMedidores.getProgsSimultaneas());
                    try {
                        ArrayList<ProgramacionesActivas> programaciones;
                        resetOldProgs(lastMinutes);
                        if (validaLicencia()) {
                            programaciones = buscarProgramacionesActivas(lastMinutes);
                            if (programaciones.size() > 0) {
                                ProgramacionMedidores progThread;
                                for (ProgramacionesActivas prog : programaciones) {
                                    progThread = new ProgramacionMedidores(version_IDrev, prog, new ControlProcesos(url, user, pass, dblinksimex, version_IDrev, windowID));
                                    progThread.start();
                                    ProgramacionMedidores.incrementaProgsSimultanes();
                                }
                            } else {
                                System.out.println("No hay programaciones activas para los últimos " + lastMinutes + " minutos");
                            }
                        } else {
                            System.out.println("Sin licencia activa para realizar la operación, debido a vencimiento de la misma o porque no se encuentra una conexión.");
                            restart = true;
                            break;
                        }
                        monitor.wait(lastMinutes * 60000); //x minutos
                    } catch (InterruptedException e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                        restart = true;
                        break;
                    }
                }
            } while (restart);

        }
    }
    
    public static void setIdentificationParameters() throws InterruptedException {
        synchronized(monitor) {
            Boolean keep;
            do {
                monitor.wait(5000);
                try {
                    //MAC
                    host = getHost();
                    mac = obtenerMac();
                    macList = getValidMacs();                                       
                } catch (Exception e) {
                    System.err.println("Fecha: " + new Date());
                    e.printStackTrace();
                }
                keep = host.isEmpty() || mac.isEmpty(); 
            } while (keep && !stop);
            ProgramacionMedidores.setHost(host);
            ProgramacionMedidores.setMac(mac);
            ProgramacionMedidores.setMacList(macList);
        }
    }
    
    public static void setConnParameters() throws InterruptedException {
        synchronized (monitor) {
            Boolean keep;
            do {
                monitor.wait(5000);
                try {
                    FileReader fr = new FileReader("C:\\metrolink\\ServTelesimex\\telesimex.ini");
                    BufferedReader br = new BufferedReader(fr);
                    String linea = null;

                    if (br.ready()) {
                        linea = br.readLine();
                    }

                    if (linea != null) {
                        try {
                            if (linea.contains("oracle")) {
                                String[] desglose = linea.split(",");
                                url = desglose[0];
                                user = desglose[1];
                                pass = desglose[2];
                                dblinksimex = desglose[3];

                            } else if (linea.contains("sqlserver")) {
                                conexionURL = linea;
                                String[] desglose = linea.split(";");

                                db = "SQLServer";
                                url = desglose[0] + ";" + desglose[1];
                                database = desglose[1];
                                String[] desglose2 = desglose[2].split("=");
                                user = desglose2[1];
                                String[] desglose3 = desglose[3].split("=");
                                pass = desglose3[1];
                                dblinksimex = " ";
                                driver = "oracle.jdbc.driver.OracleDriver";
                            }

                        } catch (Exception ex) {
                            System.err.println("Fecha: " + new Date() );
                            ex.printStackTrace();
                        }
                    }
                    if (pass == null) {
                        pass = "";
                    }
                } catch (Exception e) {
                    System.err.println("Fecha: " + new Date() );
                    e.printStackTrace();
                }
                // cp = new ControlProcesos();
                cpprogramacion = new ControlProcesos();
                //cp.inicio(url, user, pass, dblinksimex);
                keep = !cpprogramacion.inicio(url, user, pass, dblinksimex, version_IDrev, windowID);
            } while (keep && !stop);
        }
    }

    public static void resetOldProgs(int lastMinutes) {
        //String mac = obtenerMac();
        ArrayList<Integer> dateParams = getParametrosFechaHora();
        System.out.println("Reestableciendo programaciones anteriores a la fecha: " + todayColombia + " para el equipo: " + host);
        //System.out.println("Reestableciendo programaciones anteriores a la fecha: " + todayColombia + " para las siguientes interfaces de Red: " + macList.toString());
        cpprogramacion.resetOldProgs(host, dateParams, lastMinutes);
    }

    public static ArrayList<ProgramacionesActivas> buscarProgramacionesActivas(int lastMinutes) {
        //String mac = obtenerMac();
        System.out.println("Buscando programaciones activas para el equipo: " + host);
        ArrayList<Integer> dateParams = getParametrosFechaHora();
        ArrayList<ProgramacionesActivas> data = cpprogramacion.obtenerProgramacionesEquipo(host, macList, dateParams, lastMinutes);
        return data;
    }

    public static ArrayList<Integer> getParametrosFechaHora() {

        todayColombia = Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
        System.out.println("Fecha Colombia: " + todayColombia);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String strDate = dateFormat.format(todayColombia);
        int diaSemana, hora, min, seg;
        System.out.println("Converted String: " + strDate);
        hora = Integer.parseInt(strDate.substring(0, 2));
        System.out.println("Hora: " + hora);
        min = Integer.parseInt(strDate.substring(3, 5));
        System.out.println("Minutos: " + min);
        seg = Integer.parseInt(strDate.substring(6, 8));
        System.out.println("Segundos: " + seg);
        GregorianCalendar fechaCalendario = new GregorianCalendar();
        fechaCalendario.setTime(todayColombia);
        diaSemana = fechaCalendario.get(Calendar.DAY_OF_WEEK);
        System.out.println("Día de la semana: " + diaSemana);
        ArrayList<Integer> dateParams = new ArrayList<>();
        dateParams.add(diaSemana);
        dateParams.add(hora);
        dateParams.add(min);
        dateParams.add(seg);
        return dateParams;
    }

    public static void stop(String[] args) {
        synchronized (monitor) {
            System.out.println("Stop");
            stop = true;
            objabortar.labortar = true;
            monitor.notifyAll();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        if ("Start".equals(args[0])) {
            start(args);
        } else if ("Stop".equals(args[0])) {
            stop(args);
        }
    }

    public static String obtenerMac() {
        String smac = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            Enumeration<NetworkInterface> listnetwork = NetworkInterface.getNetworkInterfaces();
            NetworkInterface network = null;
            while (listnetwork.hasMoreElements()) {
                network = listnetwork.nextElement();
                
                if (network.getHardwareAddress() != null && !network.isVirtual() && network.isUp()) {
                    byte[] iMac = network.getHardwareAddress();
                    if (iMac.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < iMac.length; i++) {
                            sb.append(String.format("%02X%s", iMac[i], (i < iMac.length - 1) ? "-" : ""));
                        }
                        System.out.print("The mac address is : " + sb.toString());
                        smac = (sb.toString());
                        break;
                    }
                }
            }
            System.out.println("MAC " + smac);
        } catch (UnknownHostException e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return smac;
    }
    
    public static List<String> getValidMacs() {
        List<String> iMacList = new ArrayList<>();
        try {
            InetAddress ip = InetAddress.getLocalHost();
            Enumeration<NetworkInterface> listnetwork = NetworkInterface.getNetworkInterfaces();
            NetworkInterface network = null;
            while (listnetwork.hasMoreElements()) {
                network = listnetwork.nextElement();
                
                if (network.getHardwareAddress() != null && !network.isVirtual() && network.isUp()) {
                    byte[] iMac = network.getHardwareAddress();
                    if (iMac.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < iMac.length; i++) {
                            sb.append(String.format("%02X%s", iMac[i], (i < iMac.length - 1) ? "-" : ""));
                        }
                        System.out.print("The mac address is : " + sb.toString());
                        iMacList.add(sb.toString());                        
                    }
                }
            }
            System.out.println("MAC List: " + iMacList.toString());
        } catch (UnknownHostException e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return iMacList;
    }
    
    public static String getHost() {
        String host = "";
        try {
            InetAddress inet = Inet4Address.getLocalHost();
            host = inet.getHostName();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return host;
    }

    public static Connection getConn(String url, String user, String pass, String dblinksimex) {
        try {
            if (conn != null) {
                if (conn.isClosed()) {
                    manager = new ConnectionManager(url, user, pass, dblinksimex, null);
                    conn = manager.getConnection();
                }
            } else {
                manager = new ConnectionManager(url, user, pass, dblinksimex, null);
                conn = manager.getConnection();
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return conn;
    }

    public static boolean validaLicencia() {
        boolean ok;
        SimpleDateFormat objsdfsync = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fechaZid = objsdfsync.format(Date.from(ZonedDateTime.now(zid).toInstant()));
        System.out.println("Sincronizando hora con Id de Zona " + zona);
        System.out.println("Fecha de la zona " + fechaZid);
        ok = cpprogramacion.getLicencia(fechaZid);

        return ok;
    }
}
