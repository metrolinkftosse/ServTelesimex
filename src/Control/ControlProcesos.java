/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import Entidades.*;
import Entidades.ProgramacionesActivas;
import Util.CifradoAES128;
import Util.ConnectionManager;
import Util.DireccionMac;
import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Vector;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;
import protocolos.LeerRemotoPSTNActarisSL7000;

/**
 *
 * @author dperez
 */
public class ControlProcesos {

    RandomAccessFile fr;
    Date d = new Date();//fecha para incluir en el traifile
    public String rutalogs = "C:/Metrolink/Telesimex/logs/";
    private String version;
    private String windowId;
    ConnectionManager manager;
    public volatile Connection connection;
    public AtomicBoolean beingUpdated = new AtomicBoolean(false);
    public boolean lecturacorrecta = true;
    public String db;
    String url = "";
    String user = "";
    String pass = "";
    String dblinksimex = "";
    String Equipo = "";
    private String rutaserver;
    Parametros parasmb;
    LeerRemotoPSTNActarisSL7000 PSTNActarisSL7000;
    public String fecVerificaVolcado;
    public int intervVerificaVolcado;
    public boolean reinicioPorVerificaVolcado = false;
    public boolean noLeidoPorVerificaVolcado = false;
    SimpleDateFormat sdfarchivo = new SimpleDateFormat("yyyy-MM-dd");
    
    public ControlProcesos() {
        //TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
        try {
            File logs = new File(rutalogs);
            if (!logs.exists()) {
                logs.mkdirs();
            }
        } catch (Exception e) {
        }
    }
    
    public ControlProcesos(String url, String user, String pass, String dblink, String version, String wId) {
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.dblinksimex = dblink;
        this.version = version;
        this.windowId = wId;
        try {
            File logs = new File(rutalogs);
            if (!logs.exists()) {
                logs.mkdirs();
            }
        } catch (Exception e) {
        }
    }

    public void cerrarConexion() {
        try {
            connection.close();
        } catch (Exception e) {
        }
    }
    
    public boolean inicio() {
        boolean ans = false;
        try {
            if (url.contains("sqlserver")) {
                db = "SQLServer";
            } else {
                db = "ORACLE";
            }
            manager = new ConnectionManager(url, user, pass, dblinksimex, null);

            connection = (manager.getConnection());
            ans = true;

            if (connection != null) {
                parasmb = ObtenerDireccionServer(connection);
                if (parasmb != null) {
                    rutaserver = parasmb.getSmb();
                } else {
                    rutaserver = "";
                }
                if (rutaserver.length() > 0) {
                    jcifs.Config.setProperty("jcifs.netbios.wins", parasmb.getHost());
                    if (rutaserver.toUpperCase().contains("SMB")) {
                        //System.out.println("Carpeta Protegida");

                        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                        SmbFile logs2 = new SmbFile(rutaserver + "lecturas\\", auth);
                        if (!logs2.exists()) {
                            logs2.mkdirs();
                        }
                        SmbFile logs3 = new SmbFile(rutaserver + "eventos\\", auth);
                        if (!logs3.exists()) {
                            logs3.mkdirs();
                        }
                        SmbFile logs4 = new SmbFile(rutaserver + "registros\\", auth);
                        if (!logs4.exists()) {
                            logs4.mkdirs();
                        }
                    } else {
                        File logs2 = new File(rutaserver + "lecturas\\");
                        if (!logs2.exists()) {
                            logs2.mkdirs();
                        }
                        File logs3 = new File(rutaserver + "eventos\\");
                        if (!logs3.exists()) {
                            logs3.mkdirs();
                        }
                        File logs4 = new File(rutaserver + "registros\\");
                        if (!logs4.exists()) {
                            logs4.mkdirs();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return ans;
    }

    public boolean inicio(String url, String user, String pass, String dblink, String version, String wId) {
        boolean ans = false;
        try {
            this.url = url;
            this.user = user;
            this.pass = pass;
            this.dblinksimex = dblink;
            this.version = version;
            this.windowId = wId;
            if (url.contains("sqlserver")) {
                db = "SQLServer";
            } else {
                db = "ORACLE";
            }
            manager = new ConnectionManager(url, user, pass, dblinksimex, null);

            connection = manager.getConnection();
            ans = true;

            if (connection != null) {
                parasmb = ObtenerDireccionServer(connection);
                if (parasmb != null) {
                    rutaserver = parasmb.getSmb();
                } else {
                    rutaserver = "";
                }
                if (rutaserver.length() > 0) {
                    jcifs.Config.setProperty("jcifs.netbios.wins", parasmb.getHost());
                    if (rutaserver.toUpperCase().contains("SMB")) {
                        //System.out.println("Carpeta Protegida");

                        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                        SmbFile logs2 = new SmbFile(rutaserver + "lecturas\\", auth);
                        if (!logs2.exists()) {
                            logs2.mkdirs();
                        }
                        SmbFile logs3 = new SmbFile(rutaserver + "eventos\\", auth);
                        if (!logs3.exists()) {
                            logs3.mkdirs();
                        }
                        SmbFile logs4 = new SmbFile(rutaserver + "registros\\", auth);
                        if (!logs4.exists()) {
                            logs4.mkdirs();
                        }
                    } else {
                        File logs2 = new File(rutaserver + "lecturas\\");
                        if (!logs2.exists()) {
                            logs2.mkdirs();
                        }
                        File logs3 = new File(rutaserver + "eventos\\");
                        if (!logs3.exists()) {
                            logs3.mkdirs();
                        }
                        File logs4 = new File(rutaserver + "registros\\");
                        if (!logs4.exists()) {
                            logs4.mkdirs();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return ans;
    }

    public void validateConn() {
        try {
            if (connection == null && !beingUpdated.get() ) {
                if (beingUpdated.compareAndSet(beingUpdated.get(), true)){
                    updateConn();
                }                
                return;
            }

            if (connection.isClosed() && !beingUpdated.get() ) {
                if (beingUpdated.compareAndSet(beingUpdated.get(), true)){
                    updateConn();
                }                
                return;
            }

            if (!connection.isValid(1) && !beingUpdated.get() ) {
                if (beingUpdated.compareAndSet(beingUpdated.get(), true)){
                    updateConn();
                }                                                
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public void updateConn() {
        try {
            connection = manager.getConnection();
            beingUpdated.compareAndSet(beingUpdated.get(), false);
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }
    
    public Connection getConn() {
        try {
            if (connection != null) {
                if (connection.isClosed()) {
                    connection = null;
                    manager = new ConnectionManager(url, user, pass, dblinksimex, null);
                    connection = manager.getConnection();
                }
            } else {
                manager = new ConnectionManager(url, user, pass, dblinksimex, null);
                connection = manager.getConnection();
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return connection;
    }

    public boolean isLecturacorrecta() {
        return lecturacorrecta;
    }

    public void restartConn() {

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
            connection = null;
        }
        try {
            manager = new ConnectionManager(url, user, pass, dblinksimex, null);
            connection = manager.getConnection();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public Parametros buscaParametros() {
        return null;
    }

    public String obtenerMac() {
        String smac = "";

        try {
            InetAddress ip = InetAddress.getLocalHost();

            ////System.out.println("The mac Address of this machine is :" + ip.getHostAddress());
            Enumeration<NetworkInterface> listnetwork = NetworkInterface.getNetworkInterfaces();
            //NetworkInterface networkInterface = listnetwork.nextElement();
            NetworkInterface network = null;
            while (listnetwork.hasMoreElements()) {
                network = listnetwork.nextElement();
                //if (network.getHardwareAddress() != null && !network.isVirtual() && network.isUp()){
                if (network.getHardwareAddress() != null && !network.isVirtual() && network.isUp()) {
                    byte[] mac = network.getHardwareAddress();
                    if (mac.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        //System.out.print("The mac address is : " + sb.toString());
                        smac = (sb.toString());
                        break;
                    }
                }
            }

            //System.out.println("MAC " + smac);
        } catch (UnknownHostException e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return smac;
    }

    public ArrayList<EMedidor> buscarAllMedidores(EMedidor med) {
        //TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        DireccionMac drmac = new DireccionMac();
        String mac = "";

        try {
            mac = obtenerMac();
            if (med.getnSerie() == null) {
                med.setnSerie("'%%'");
            } else {
                med.setnSerie("'%" + med.getnSerie() + "%'");
            }
            if (med.getGrupo() == null) {
                med.setGrupo(new EGrupo("tcm.vcgrupo", ""));
            } else {
                med.setGrupo(new EGrupo("'" + med.getGrupo().getCodigo() + "'", ""));
            }
            if (med.getMarcaModem() == null) {
                med.setMarcaModem(new EMarcaModem("tcm.vcmarcamodem", ""));
            } else {
                med.setMarcaModem(new EMarcaModem("'" + med.getMarcaModem().getCodigo() + "'", ""));
            }
            if (med.getMarcaMedidor() == null) {
                med.setMarcaMedidor(new EMarcaMedidor("im.vcmarca", null, null));
            } else {
                med.setMarcaMedidor(new EMarcaMedidor("'" + med.getMarcaMedidor().getCodigo() + "'", null, null));
            }
            if (med.getPuertocomm() == null) {
                med.setPuertocomm("gc.puertocomm");
            } else {
                med.setPuertocomm("'" + med.getPuertocomm() + "'");
            }
            if (med.getSic() == null) {
                med.setSic("im.vccodsic");
            } else {
                med.setSic("'" + med.getSic() + "'");
            }
            if (med.getIdmedidor() == null) {
                med.setIdmedidor("im.vccodinterno");
            } else {
                med.setIdmedidor("'" + med.getIdmedidor() + "'");
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

        ArrayList<EMedidor> listamedidores = new ArrayList<EMedidor>();
        String sql = "SELECT tcm.*,im.vcmarca,ma.vcnommarca,im.vcnomcliente, im.vccodsic, im.vccodinterno, "
                + "tm.descripcion AS vcnommodem,tc.vcnomgrupo, gc.vccom as grupoportcom, mu.vcnommuni,opr.vcnomoperador_red "
                + "FROM tele01_medidores im "
                + "INNER JOIN tele01_marca ma ON im.vcmarca=ma.vccodmarca "
                + "INNER JOIN tele01_conf_medidor tcm ON tcm.vcserie=im.vcserie "
                + "INNER JOIN tele01_grupo tc ON tc.vccodgrupo=tcm.vcgrupo "
                + "INNER JOIN tele01_modems tm ON tm.codigo=tcm.vcmarcamodem "
                + "INNER JOIN tele01_medidorxgrupocom mxg ON mxg.vcmedidor=im.vcserie "
                + "INNER JOIN tele01_gruposcom gc ON gc.vccodgrupocom=mxg.vccodgrupocom "
                + "LEFT JOIN municipios" + dblinksimex + " mu ON mu.vccodmuni=tcm.vcmunicipio "
                + "LEFT JOIN m_operadores_red" + dblinksimex + " opr ON opr.vccodoperador_red=tcm.vcoperador "
                + "WHERE tcm.vcserie LIKE " + med.getnSerie() + " "
                + "AND tcm.vcgrupo=" + med.getGrupo().getCodigo() + " "
                + "AND tcm.vcmarcamodem=" + med.getMarcaModem().getCodigo() + " "
                + "AND im.vcmarca=" + med.getMarcaMedidor().getCodigo() + " "
                + "AND gc.vcequipomac ='" + mac + "' "
                + "AND im.vccodinterno= " + med.getIdmedidor() + " "
                + "AND im.vccodsic=" + med.getSic() + " ";

        if (med.getOperador() != null) {
            sql += "AND tcm.vcoperador='" + med.getOperador() + "' ";
        }
        if (med.getMunicipio() != null) {
            sql += "AND tcm.vcmunicipio='" + med.getMunicipio() + "' ";
        }

        if (med.getDireccionip() != null) {
            sql += "AND tcm.ip ='" + med.getDireccionip() + "' ";

        }
        if (med.getPuertoip() != null) {
            sql += "AND tcm.puertoip ='" + med.getPuertoip() + "' ";
        }
        if (med.getNumtelefonico() != null) {
            sql += "AND tcm.numtelefono='" + med.getNumtelefonico() + "' ";
        }
        if (med.isLfaltantes()) {
            sql += "AND (tcm.DUFEC_LEC <= to_timestamp('" + sdf2.format(new Date(Calendar.getInstance().getTimeInMillis())) + "','yyyy-mm-dd') OR  tcm.DUFEC_LEC IS NULL) ";
        }

        if (med.isLdesfasados()) {
            int desfasepermitido = getdesfasePermitido(null);
            sql += " AND tcm.ndif>'" + desfasepermitido + "' ";
        }
        sql += "ORDER BY tcm.DUFEC_LEC NULLS FIRST,tcm.DUFEC_LEC ASC";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            EMedidor em = null;
            validateConn();

            //stmt = connection.prepareStatement(sql);
            stmt = genPrepStatement(sql);           
            rs = stmt.executeQuery();
            while (rs.next()) {
                em = new EMedidor();
                em.setnSerie(rs.getString("vcserie"));
                em.setCliente(new ECliente("", rs.getString("vcnomcliente")));
                em.setTipoconexion(rs.getInt("vctipoconexion"));
                em.setGrupo(new EGrupo(rs.getString("vcgrupo"), rs.getString("vcnomgrupo")));
                em.setMarcaMedidor(new EMarcaMedidor(rs.getString("vcmarca"), rs.getString("vcnommarca"), null));
                em.setMarcaModem(new EMarcaModem(rs.getString("vcmarcamodem"), rs.getString("vcnommodem")));
                em.setNumtelefonico(rs.getString("numtelefono"));
                em.setDireccionip(rs.getString("ip").trim());
                em.setPuertoip(rs.getString("puertoip").trim());
                em.setPuertocomm(rs.getString("grupoportcom"));
                if (rs.getDate("dufec_lec") == null) {
                    em.setFecha(null);
                } else {
                    em.setFecha(rs.getTimestamp("dufec_lec"));
                }
                em.setNdias(rs.getInt("ndias"));
                em.setNdiaseventos(rs.getInt("ndiaseventos"));
                em.setTimeout(rs.getLong("ntimeout"));
                em.setReintentos(rs.getInt("nreintentos"));
                em.setMunicipio(rs.getString("vcmunicipio") == null ? "" : rs.getString("vcnommuni"));
                em.setIdmedidor(rs.getString("vccodinterno"));
                em.setOperador(rs.getString("vcoperador") == null ? "" : rs.getString("vcnomoperador_red"));
                em.setSic(rs.getString("vccodsic"));

                listamedidores.add(em);
            }

            //connection.close();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (stmt != null) {
                   stmt.close(); 
                }                
            } catch (Exception e) {
            }

        }

        return listamedidores;
    }

    public EMedidor buscarMedidor(String serie, String host) {
        //TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
        EMedidor medidor = null;
        String sql = "SELECT imm.*,ma.vcnommarca FROM tele01_medidores imm "
                + " INNER JOIN tele01_marca ma ON (imm.vcmarca=ma.vccodmarca)"
                + " WHERE vcserie='" + serie + "'";
        ////System.out.println(sql);
        //String mac = "";
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        try {
            validateConn();
            int contInt = 0;            
            //stmt1 = connection.prepareStatement(sql);
            stmt1 = genPrepStatement(sql);
            stmt2 = null;
            rs = stmt1.executeQuery();
            if (rs.next()) {
                medidor = new EMedidor(serie, new EMarcaMedidor(rs.getString("vcmarca"), rs.getString("vcnommarca"), null));
                medidor.setnSerie(serie);
                medidor.setIdmedidor(rs.getString("vccodinterno") == null ? rs.getString("vccodinterno") : "");
                medidor.setSic(rs.getString("vccodsic") == null ? rs.getString("vccodsic") : "");
                medidor.setCliente(new ECliente("", rs.getString("vcnomcliente")));
                if (rs.getInt("lconfigurado") == 0) {
                    medidor.setLconfigurado(false);
                } else {
                    medidor.setLconfigurado(true);
                }
                sql = "SELECT tcm.*,tm.descripcion AS vcnommodems,v.vcnomvelocidad,c.vcnomgrupo , gc.vccom AS grupoportcom FROM tele01_conf_medidor tcm "
                        + "INNER JOIN tele01_modems tm ON tm.codigo=tcm.VCMARCAMODEM "
                        + "INNER JOIN tele01_velocidades v ON v.vccodvelocidad=tcm.vcvelocidad "
                        + "INNER JOIN tele01_grupo c ON c.vccodgrupo=tcm.vcgrupo "
                        + "INNER JOIN tele01_medidorxgrupocom mxg ON mxg.vcmedidor=tcm.vcserie "
                        + "INNER JOIN tele01_gruposcom gc ON gc.vccodgrupocom=mxg.vccodgrupocom "
                        + "WHERE tcm.vcserie='" + serie + "' AND gc.vcequipomac IN ( " 
                        + "SELECT VCMACEQUIPO FROM TELE01_EQUIPOS WHERE UPPER(VCNOMEQUIPO) = UPPER('" + host + "') )";
                try {
                    //stmt2 = connection.prepareStatement(sql);
                    stmt2 = genPrepStatement(sql);
                    rs2 = stmt2.executeQuery();
                    ResultSet rs3 = null;
                    if (rs2.next()) {
                        medidor.setMarcaModem(new EMarcaModem(rs2.getString("VCMARCAMODEM"), rs2.getString("vcnommodems")));
                        //si el tipo de conexion es 0 es GPRS y si es 1 es PSTN
                        medidor.setTipoconexion(rs2.getInt("VCTIPOCONEXION"));
                        if (rs2.getInt("VCTIPOCONEXION") == 0 || rs2.getInt("VCTIPOCONEXION") == 2 || rs2.getInt("VCTIPOCONEXION") == 3) {
                            medidor.setDireccionip(rs2.getString("IP").trim());
                            medidor.setPuertoip(rs2.getString("puertoIP").trim());
                            medidor.setNumtelefonico("");
                        } else {
                            medidor.setDireccionip("");
                            medidor.setPuertoip("");
                            medidor.setNumtelefonico(rs2.getString("numtelefono"));
                        }
                        medidor.setPuertocomm(rs2.getString("grupoportcom"));
                        medidor.setVelocidadpuerto(new EVelocidad(rs2.getString("vcvelocidad"), rs2.getString("vcnomvelocidad")));
                        medidor.setGrupo(new EGrupo(rs2.getString("vcgrupo"), rs2.getString("vcnomgrupo")));
                        medidor.setReintentos(rs2.getInt("nreintentos"));
                        medidor.setTimeout(rs2.getLong("ntimeout"));
                        medidor.setNdias(rs2.getInt("ndias"));
                        medidor.setPassword(rs2.getString("vcpassword"));
                        medidor.setPassword2(rs2.getString("vcpassword2") == null ? "" : rs2.getString("vcpassword2"));
                        medidor.setFecha(rs2.getTimestamp("DUFEC_LEC"));
                        medidor.setNcanales(rs2.getInt("ncanales"));
                        medidor.setNdiasreg(rs2.getInt("ndiasreg"));
                        medidor.setNdiaseventos(rs2.getInt("ndiaseventos"));
                        medidor.setNmesreg(rs2.getInt("nmesreg"));
                        medidor.setDireccionLogica(rs2.getString("ndireccionlogica") == null ? "0" : rs2.getString("ndireccionlogica").trim());
                        medidor.setDireccionFisica(rs2.getString("ndireccionfisica") == null ? "0" : rs2.getString("ndireccionfisica").trim());
                        medidor.setDireccionCliente(rs2.getString("vcdireccioncliente") == null ? "1" : rs2.getString("vcdireccioncliente").trim());
                        medidor.setBytesdireccion(rs2.getString("vcbytesdireccion") == null ? "4" : rs2.getString("vcbytesdireccion").trim());
                        medidor.setSeguridad(rs2.getBoolean("lsecurity"));
                        medidor.setMedLeido(false);
                        if (medidor.getMarcaMedidor().getCodigo().equals("23")) {
                            sql = "SELECT * FROM tele01_codigos_dlms WHERE vcserie = '" + serie + "' ORDER BY NCODIGO_PETICION ASC";
                            try {
                                //stmt3 = connection.prepareStatement(sql);
                                stmt3 = genPrepStatement(sql);
                                rs3 = stmt3.executeQuery();
                                while (rs3.next()) {
                                    medidor.addCodigoDLMS(rs3.getInt("ncodigo_peticion"), rs3.getString("vcvalor"));
                                }
                            } catch (Exception e) {
                                System.err.println("Fecha: " + new Date() );
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (rs3 != null) {
                                        rs3.close();
                                    }
                                    if (stmt3 != null) {
                                        stmt3.close();
                                    }
                                    stmt3 = null;
                                    rs3 = null;
                                } catch (Exception e2) {
                                    System.err.println("Fecha: " + new Date() );
                                    e2.printStackTrace();
                                }
                            }
                        }
                        medidor=agregarParametrosAvanzadosDelMedidor(medidor);
                    } else {
                        medidor.setLconfigurado(false);
                    }
                } catch (Exception e) {
                }
                try {
                    if (rs2 != null) {
                        rs2.close();
                    }
                    rs2 = null;
                } catch (Exception e2) {
                }
                try {
                    if (stmt2 != null) {
                        stmt2.close();
                    }
                    stmt2 = null;
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
        }
        return medidor;

    }    

    public EMedidor buscarMedidorequipo(String serie, String mac) {
        //TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
        EMedidor medidor = null;
        String sql = "SELECT imm.*,ma.vcnommarca FROM tele01_medidores imm "
                + " INNER JOIN tele01_marca ma ON (imm.vcmarca=ma.vccodmarca)"
                + " WHERE vcserie='" + serie + "'";
        try {
            validateConn();
            //try (PreparedStatement stmt1 = connection.prepareStatement(sql); ResultSet rs = stmt1.executeQuery(sql)) {
            try (PreparedStatement stmt1 = genPrepStatement(sql); ResultSet rs = stmt1.executeQuery()) {
                if (rs.next()) {
                    medidor = new EMedidor(serie, new EMarcaMedidor(rs.getString("vcmarca"), rs.getString("vcnommarca"), null));
                    medidor.setnSerie(serie);
                    medidor.setIdmedidor(rs.getString("vccodinterno") == null ? rs.getString("vccodinterno") : "");
                    medidor.setSic(rs.getString("vccodsic") == null ? rs.getString("vccodsic") : "");
                    medidor.setCliente(new ECliente("", rs.getString("vcnomcliente")));
                    if (rs.getInt("lconfigurado") == 0) {
                        medidor.setLconfigurado(false);
                    } else {
                        medidor.setLconfigurado(true);
                    }
                    sql = "SELECT tcm.*,tm.descripcion as vcnommodems,v.vcnomvelocidad,c.vcnomgrupo,gc.vccom AS commport FROM tele01_conf_medidor tcm "
                            + "INNER JOIN tele01_modems tm ON tm.codigo=tcm.VCMARCAMODEM "
                            + "INNER JOIN tele01_velocidades v ON v.vccodvelocidad=tcm.vcvelocidad "
                            + "INNER JOIN tele01_medidorxgrupocom mxg ON tcm.vcserie=mxg.vcmedidor "
                            + "INNER JOIN tele01_gruposcomm gc ON mxg.vccodgrupocom=gc.vccodgrupocom "
                            + "INNER JOIN tele01_grupo c ON c.vccodgrupo=tcm.vcgrupo WHERE tcm.vcserie='" + serie + "' AND gc.vcmacequipo='" + mac + "'";
                    //try (PreparedStatement stmt2 = connection.prepareStatement(sql); ResultSet rs2 = stmt2.executeQuery(sql)) {
                    try (PreparedStatement stmt2 = genPrepStatement(sql); ResultSet rs2 = stmt2.executeQuery()) {
                        if (rs2.next()) {
                            medidor.setMarcaModem(new EMarcaModem(rs2.getString("VCMARCAMODEM"), rs2.getString("vcnommodems")));
                            //si el tipo de conexion es 0 es GPRS y si es 1 es PSTN
                            medidor.setTipoconexion(rs2.getInt("VCTIPOCONEXION"));
                            if (rs2.getInt("VCTIPOCONEXION") == 0 || rs2.getInt("VCTIPOCONEXION") == 2 || rs2.getInt("VCTIPOCONEXION") == 3) {
                                medidor.setDireccionip(rs2.getString("IP").trim());
                                medidor.setPuertoip(rs2.getString("puertoIP").trim());
                                medidor.setNumtelefonico("");
                            } else {
                                medidor.setDireccionip("");
                                medidor.setPuertoip("");
                                medidor.setNumtelefonico(rs2.getString("numtelefono"));
                            }
                            medidor.setPuertocomm(rs2.getString("commport"));
                            medidor.setVelocidadpuerto(new EVelocidad(rs2.getString("vcvelocidad"), rs2.getString("vcnomvelocidad")));
                            medidor.setGrupo(new EGrupo(rs2.getString("vcgrupo"), rs2.getString("vcnomgrupo")));
                            medidor.setReintentos(rs2.getInt("nreintentos"));
                            medidor.setTimeout(rs2.getLong("ntimeout"));
                            medidor.setNdias(rs2.getInt("ndias"));
                            medidor.setPassword(rs2.getString("vcpassword"));
                            medidor.setPassword2(rs2.getString("vcpassword2") == null ? "" : rs2.getString("vcpassword2"));
                            medidor.setFecha(rs2.getTimestamp("DUFEC_LEC"));
                            medidor.setNcanales(rs2.getInt("ncanales"));
                            medidor.setNdiasreg(rs2.getInt("ndiasreg"));
                            medidor.setNmesreg(rs2.getInt("nmesreg"));
                            medidor.setNdiaseventos(rs2.getInt("ndiaseventos"));
                        }
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return medidor;
    }

    public void lecturaABBNCanales(Vector<String> data, String seriemedidor, String marca, double conske, int ncanales, String fechaactual, int intervalo, int factor, File fileLog, Timestamp ufeclec) {
        try {

            validateConn();
            for (int idat = 0; idat < data.size(); idat++) {
                desglosarTramaABBNCanales(data.get(idat), seriemedidor, marca, conske, ncanales, fechaactual, intervalo, factor, connection, fileLog, ufeclec);
            }
            //actualizamos la fecha del medidor
            actualizaFechaLectura(seriemedidor, fechaactual);
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            System.err.println("");
        }

    }

    private void desglosarTramaABBNCanales(String trama, String seriemedidor, String marca, double conske, int ncanales, String actual, int intervalo, int factor, Connection conn,  File fileLog, Timestamp ufeclec) {

        try {
            SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
            SimpleDateFormat spf = new SimpleDateFormat("yy-MM-dd HH:mm");
            EConstanteKE econske = null;
            Vector<Electura> vlec = new Vector<Electura>();
            ArrayList<EConstanteKE> vconske = null;
            ArrayList<EtipoCanal> vtipocanal = null;
            vconske = obtenerConstantesKe(seriemedidor);
            vtipocanal = obtenerTipoCanales(marca);
            String fec = trama.substring(0, 2) + "-" + trama.substring(2, 4) + "-" + trama.substring(4, 6);
            String energia = "";
            String hora = "";
            String min = "";
            Timestamp time = null;
            String canal = "";
            Timestamp feactual = new Timestamp(sgflect.parse(actual).getTime() - 1800000);
            //int n = 1, tiempo = 15;
            int n = 0, tiempo = intervalo;
            int aumento = 4 * ncanales;
            int vccanal = 0;
            for (int i = 12; i < trama.length(); i += aumento) {
                int tiempolec = tiempo * n;
                vccanal = 1;
                for (int j = 0; j < aumento; j += 4) {
                    energia = trama.substring(i + j, i + j + 4);
                    if (energia.equals("7FFF") || energia.equals("7FFE")) {
                        energia = "0";
                    }
                    energia = "" + ((Integer.parseInt((energia), 16)) & 0x7FFF);

                    hora = "" + ((int) (tiempolec / 60));
                    min = "" + (int) (tiempolec % 60);//
                    String ts = fec + " " + hora + ":" + min;
                    time = new Timestamp(spf.parse(ts).getTime());
                    if (time.before(feactual)) { //validamos si la fecha es despues de la fecha de la ultima lectura
                        econske = buscarConskeOptimizado(seriemedidor, vccanal, vconske);
                        if (econske != null) {
                            if (econske.isConsInterna()) {
                                econske.setPesopulso(conske);
                            }
                            canal = "";
                            for (EtipoCanal et : vtipocanal) {
                                if (Integer.parseInt(et.getCanal()) == vccanal) {
                                    canal = et.getUnidad();
                                }
                            }
                            vlec.add(new Electura(time, seriemedidor, vccanal, trasnformarEnergia(Double.parseDouble(energia) * factor, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(energia), intervalo, canal));
                        }
                    }
                    vccanal++;
                }
                n++;
            }
            actualizaLectura(vlec, fileLog, new Timestamp(ufeclec.getTime() - (long) 86400000) );
        } catch (Exception e) {

            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public double trasnformarEnergia(double lectura, double conske, double multiplo, double divisor) {
        double energia = 0;
        try {
            energia = ((lectura * conske) * multiplo) / divisor;
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            energia = -1;
        }
        return energia;
    }
    
    public void actualizaAcumulados(ArrayList<ELecturaAcumulada> lista_Acumulados, File file) throws SQLException {        
        String sql = "";
        switch (db) {
            case "SQLServer":
                break;
            case "ORACLE":
                sql += "INSERT INTO TELE01_LEC_ACUM (DFECHA, VCSERIE, NCANAL, NTARIFA, NPULSO, IDVAR) VALUES (?,?,?,?,?,?)";
                break;
        }
        validateConn();
        PreparedStatement pstm = genPrepStatement(sql);
        for (ELecturaAcumulada acumulada : lista_Acumulados) {
            if (acumulada.getFecha() != null) {
                switch (db) {
                    case "ORACLE":
                        pstm.setTimestamp(1, acumulada.getFecha());
                        pstm.setString(2, acumulada.getSerie());
                        // pstm1.setInt(3, electura.getCanal());
                        pstm.setLong(3, acumulada.getCanal());
                        pstm.setInt(4, acumulada.getTarifa());
                        //update
                        pstm.setDouble(5, acumulada.getPulso());
                        pstm.setString(6, acumulada.getUnit());
                        pstm.addBatch();
                        break;
                    case "SQLServer":
                        break;
                }
            }
        }
        try {
            escribir("Inicio Batch acumulados " + new Date(), file);
            pstm.executeBatch();
            escribir("Fin Batch acumulados " + new Date(), file);
        } catch (Exception e) {
            lecturacorrecta = false;
            e.printStackTrace();
        }
    }

    public void actualizaLectura(Vector<Electura> velectura, File fileLog, Timestamp ufeclec) throws Exception {
        SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sd2 = new SimpleDateFormat("HH");
        SimpleDateFormat sd3 = new SimpleDateFormat("yyyy-MM-dd");
        String sql4 = "";
        String sql5 = "";
        switch (db) {
            case "SQLServer":
                sql4 = "EXECUTE [MATRICES_OPT] \n"
                        + "   ?\n"
                        + "  ,?\n";
                sql5 = "MERGE INTO tele01_lecturas a USING \n"
                        + "(  SELECT * FROM (\n"
                        + "select*,ROW_NUMBER() OVER(ORDER BY (dfecha) desc) as 'Row_Number'\n"
                        + "from(\n"
                        + "SELECT dfecha,vcserie,ncanal \n"
                        + "FROM tele01_lecturas WHERE vcserie=? AND dfecha= convert(smalldatetime,?,105) AND ncanal=? \n"
                        + "UNION SELECT NULL dfecha,NULL vcserie,0 ncanal )c) b where  b.Row_Number = 1 ) b \n"
                        + "ON (a.dfecha=b.dfecha AND a.vcserie=b.vcserie AND a.ncanal=b.ncanal) \n"
                        + "WHEN MATCHED THEN UPDATE SET a.nlec=?, a.npulso=? "
                        + "WHEN NOT MATCHED THEN "
                        + "INSERT (nlec,vcserie,dfecha,ncanal,npulso,nintervalo,idvar,fechad,hourd) "
                        + "VALUES (?,?,convert(smalldatetime,?,105),?,?,?,?,cast(convert(smalldatetime,?,105) as date), datepart(HOUR,convert(smalldatetime,?,105)));";

                break;

            case "ORACLE":                
                sql4 = "{call MATRICES_OPT(?,?)}";
                sql5 = "MERGE INTO tele01_lecturas a USING (SELECT * FROM (SELECT dfecha,vcserie,ncanal FROM tele01_lecturas "
                        + "WHERE vcserie=? AND dfecha=? AND ncanal=? UNION SELECT NULL dfecha,NULL vcserie,0 ncanal  FROM DUAL "
                        + ") WHERE ROWNUM = 1 ) b ON (a.dfecha=b.dfecha AND a.vcserie=b.vcserie AND a.ncanal=b.ncanal) "
                        + "WHEN MATCHED THEN "
                        //                + "UPDATE SET a.nlec=(a.nlec+0) "
                        + "UPDATE SET a.nlec=?, a.npulso=? "
                        + "WHEN NOT MATCHED THEN "
                        + "INSERT (nlec,vcserie,dfecha,ncanal,npulso,nintervalo,idvar,fechad,hourd,lecaux) VALUES (?,?,?,?,?,?,?,?,?,?)"; //vic octubre
                break;

        }
        validateConn();

        //PreparedStatement pstm1 = connection.prepareStatement(sql5);
        PreparedStatement pstm1 = genPrepStatement(sql5);
        //CallableStatement callableStatement2 = connection.prepareCall(sql4);
        CallableStatement callableStatement2 = genCallStatement(sql4);
        try {
            escribir("Cantidad de Registros a Almacenar/Actualizar: " + velectura.size(), fileLog);
            for (Electura electura : velectura) {
                switch (db) {
                    case "ORACLE":
                        pstm1.setString(1, electura.getSerie());
                        pstm1.setTimestamp(2, electura.getFecha());
                        // pstm1.setInt(3, electura.getCanal());
                        pstm1.setLong(3, electura.getCanal());
                        //update
                        pstm1.setDouble(4, electura.getLec());
                        pstm1.setDouble(5, electura.getPulso());
                        //insert
                        pstm1.setDouble(6, electura.getLec());
                        pstm1.setString(7, electura.getSerie());
                        pstm1.setTimestamp(8, electura.getFecha());
//                pstm1.setInt(7, electura.getCanal());
                        pstm1.setLong(9, electura.getCanal());
                        pstm1.setDouble(10, electura.getPulso());
                        pstm1.setInt(11, electura.getIntervalo());
                        pstm1.setString(12, electura.getVccanal());
                        pstm1.setString(13, sd1.format(new Date(electura.getFecha().getTime())));
                        pstm1.setString(14, sd2.format(new Date(electura.getFecha().getTime())));
                        pstm1.setDouble(15, electura.getLecaux()); //vic octubre
                        pstm1.addBatch();
                        break;
                    case "SQLServer":
                        pstm1.setString(1, electura.getSerie());
                        pstm1.setTimestamp(2, electura.getFecha());
                        pstm1.setLong(3, electura.getCanal());
                        pstm1.setDouble(4, electura.getLec());
                        pstm1.setDouble(5, electura.getPulso());
                        pstm1.setDouble(6, electura.getLec());
                        pstm1.setString(7, electura.getSerie());
                        pstm1.setTimestamp(8, electura.getFecha());
                        pstm1.setLong(9, electura.getCanal());
                        pstm1.setDouble(10, electura.getPulso());
                        pstm1.setInt(11, electura.getIntervalo());
                        pstm1.setString(12, electura.getVccanal());
                        pstm1.setTimestamp(13, electura.getFecha());
                        pstm1.setTimestamp(14, electura.getFecha());
                        pstm1.addBatch();
                        break;
                }
            }
            try {
                switch (db) {
                    case "ORACLE":
                        callableStatement2.setTimestamp(1, ufeclec);
                        callableStatement2.setString(2, velectura.get(0).getSerie());
                    case "SQLServer":
                        callableStatement2.setTimestamp(1, ufeclec);
                        callableStatement2.setString(2, velectura.get(0).getSerie());
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
            try {
                ////System.out.println("Inicio Batch lecturas " + new Date());
                escribir("Inicio Batch lecturas " + new Date(), fileLog);
                pstm1.executeBatch();
                escribir("Fin Batch lecturas " + new Date(), fileLog);
                ////System.out.println("Fin Batch lecturas " + new Date());
                ////System.out.println("Inicio Batch Matrices " + new Date());
                escribir("Inicio Batch Matrices " + new Date(), fileLog);
                callableStatement2.execute();
                escribir("Fin Batch Matrices " + new Date(), fileLog);
                ////System.out.println("Fin Batch Matrices " + new Date());
            } catch (Exception e) {
                lecturacorrecta = false;
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        } catch (Exception e) {
            lecturacorrecta = false;
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm1 != null) {
                pstm1.close();
            }
            if (callableStatement2 != null) {
                callableStatement2.close();
            }
        }
    }

    //(NEW: Se retrasan campos de fecha y hora para los ITRON a solicitud de ENERTOTAL)
    public void actualizaLecturaDesfase(Vector<Electura> velectura, File fileLog, Timestamp ufeclec) throws Exception {
        SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sd2 = new SimpleDateFormat("HH");
        validateConn();
        
        // String sql3 = "{call SAVE_MATRIZ(?,?,?)}";
        //String sql4 = "{call SAVE_MATRIZCANALES(?,?,?,?,?,?)}";
        String sql4 = "{call MATRICES_OPT(?,?)}";
        String sql5 = "MERGE INTO tele01_lecturas a USING (SELECT * FROM (SELECT dfecha,vcserie,ncanal FROM tele01_lecturas "
                + "WHERE vcserie=? AND dfecha=? AND ncanal=? UNION SELECT NULL dfecha,NULL vcserie,0 ncanal  FROM DUAL "
                + ") WHERE ROWNUM = 1 ) b ON (a.dfecha=b.dfecha AND a.vcserie=b.vcserie AND a.ncanal=b.ncanal) "
                + "WHEN MATCHED THEN "
                //                + "UPDATE SET a.nlec=(a.nlec+0) "
                + "UPDATE SET a.nlec=?, a.npulso=? "
                + "WHEN NOT MATCHED THEN "
                + "INSERT (nlec,vcserie,dfecha,ncanal,npulso,nintervalo,idvar,fechad,hourd,lecaux) VALUES (?,?,?,?,?,?,?,?,?,?)"; //vic octubre

        //PreparedStatement pstm1 = connection.prepareStatement(sql5);
        PreparedStatement pstm1 = genPrepStatement(sql5);
        //CallableStatement callableStatement2 = connection.prepareCall(sql4);
        CallableStatement callableStatement2 = genCallStatement(sql4);
        try {
            escribir("Cantidad de Registros a Almacenar/Actualizar: " + velectura.size(), fileLog);
            for (Electura electura : velectura) {
                pstm1.setString(1, electura.getSerie());
                pstm1.setTimestamp(2, new Timestamp(electura.getFecha().getTime() - 60000L * (long) electura.getIntervalo()));
                pstm1.setLong(3, electura.getCanal());
                //update
                pstm1.setDouble(4, electura.getLec());
                pstm1.setDouble(5, electura.getPulso());
                //insert
                pstm1.setDouble(6, electura.getLec());
                pstm1.setString(7, electura.getSerie());
                pstm1.setTimestamp(8, new Timestamp(electura.getFecha().getTime() - 60000L * (long) electura.getIntervalo()));
                pstm1.setLong(9, electura.getCanal());
                pstm1.setDouble(10, electura.getPulso());
                pstm1.setInt(11, electura.getIntervalo());
                pstm1.setString(12, electura.getVccanal());
                pstm1.setString(13, sd1.format(new Date(electura.getFecha().getTime() - 60000L * (long) electura.getIntervalo())));
                pstm1.setString(14, sd2.format(new Date(electura.getFecha().getTime() - 60000L * (long) electura.getIntervalo())));
                pstm1.setDouble(15, electura.getLecaux()); //vic octubre
                pstm1.addBatch();                
            }
            try {
                callableStatement2.setTimestamp(1, new Timestamp(ufeclec.getTime() - 60000L * (long) velectura.get(0).getIntervalo()));
                callableStatement2.setString(2, velectura.get(0).getSerie());
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
            try {
                escribir("Inicio Batch lecturas " + new Date(), fileLog);
                pstm1.executeBatch();
                escribir("Fin Batch lecturas " + new Date(), fileLog);
                escribir("Inicio Batch Matrices " + new Date(), fileLog);
                callableStatement2.execute();
                escribir("Fin Batch Matrices " + new Date(), fileLog);
            } catch (Exception e) {
                lecturacorrecta = false;
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        } catch (Exception e) {
            lecturacorrecta = false;
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm1 != null) {
                pstm1.close();
            }
            if (callableStatement2 != null) {
                callableStatement2.close();
            }
        }
    }

    public void actualizaRegistro(ArrayList<ERegistro> vreg, String fechaactual) throws Exception {
        //System.out.println("*** Actualizando registros ***");
        //String seriemedidor, int tiporegistro, int year, int month, int day, Timestamp actual, double pulsos, String ncanal, String idvar, double energia
        SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
        Date fecha = sgflect.parse(fechaactual);
        validateConn();
        String sql = "SELECT * FROM TELE01_REGISTROS_LEC WHERE vcserie=? AND vctiporegistro=? AND vcano=? AND vcmes=? AND vccanal=? AND vcdia=?";

        String sql2 = "";
        PreparedStatement pstm1 = null;
        ResultSet rs1 = null;
        try {
            for (ERegistro eReg : vreg) {
                //pstm1 = connection.prepareStatement(sql);
                pstm1 = genPrepStatement(sql);
                pstm1.setString(1, eReg.getSeriemedidor());
                pstm1.setString(2, "" + eReg.getTiporegistro());
                pstm1.setString(3, "" + eReg.getYear());
                pstm1.setString(4, "" + eReg.getMonth());
                pstm1.setString(5, Long.toString(eReg.getNcanal()));
                pstm1.setString(6, "" + eReg.getDay());
                PreparedStatement pstm2 = null;
                rs1 = pstm1.executeQuery();
                if (rs1.next()) {
                    sql2 = "UPDATE TELE01_REGISTROS_LEC SET npulsos=?, nlec=?, dfec_ult_lec=? WHERE vcserie=? AND vctiporegistro=? AND vcano=? AND vcmes=? AND vccanal=? AND vcdia=?";
                    try {
                        //pstm2 = connection.prepareStatement(sql2);
                        pstm2 = genPrepStatement(sql2);
                        pstm2.setDouble(1, eReg.getPulsos());
                        pstm2.setDouble(2, eReg.getEnergia());
                        pstm2.setTimestamp(3, new Timestamp(fecha.getTime()));
                        pstm2.setString(4, eReg.getSeriemedidor());
                        pstm2.setString(5, "" + eReg.getTiporegistro());
                        pstm2.setString(6, "" + eReg.getYear());
                        pstm2.setString(7, "" + eReg.getMonth());
                        pstm2.setString(8, Long.toString(eReg.getNcanal()));
                        pstm2.setString(9, "" + eReg.getDay());
                        pstm2.executeUpdate();
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    } finally {
                        try {
                            if (pstm2 != null) {
                                pstm2.close();
                            }
                        } catch (Exception e) {
                            System.err.println("Fecha: " + new Date() );
                            e.printStackTrace();
                        }
                    }
                } else {
                    sql2 = "INSERT INTO TELE01_REGISTROS_LEC (npulsos,nlec,dfec_ult_lec,vcserie,vctiporegistro,vcano,vcmes,vccanal,vcdia,idvar)VALUES(?,?,?,?,?,?,?,?,?,?)";
                    try {
                        //pstm2 = connection.prepareStatement(sql2);
                        pstm2 = genPrepStatement(sql2);
                        pstm2.setDouble(1, eReg.getPulsos());
                        pstm2.setDouble(2, eReg.getEnergia());
                        pstm2.setTimestamp(3, new Timestamp(fecha.getTime()));
                        pstm2.setString(4, eReg.getSeriemedidor());
                        pstm2.setString(5, "" + eReg.getTiporegistro());
                        pstm2.setString(6, "" + eReg.getYear());
                        pstm2.setString(7, "" + eReg.getMonth());
                        pstm2.setString(8, Long.toString(eReg.getNcanal()));
                        pstm2.setString(9, "" + eReg.getDay());
                        pstm2.setString(10, eReg.getIdvar());
                        pstm2.executeUpdate();
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    } finally {
                        try {
                            if (pstm2 != null) {
                                pstm2.close();
                            }
                        } catch (Exception e) {
                            System.err.println("Fecha: " + new Date() );
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs1 != null) {
                rs1.close();
            }
            if (pstm1 != null) {
                pstm1.close();
            }
        }
    }

    public void eventosElgama(Vector<String> data, String seriemedidor) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            validateConn();
            SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
            String archivoContingencia = "" + seriemedidor + "_" + (fechacontingencia.format(new Date().getTime())) + ".txt";
            RandomAccessFile fr;
            SmbRandomAccessFile sfr;
            boolean guardar = false;
            String tramainicio = "";
            String tramafin = "";
            String fechai = "";
            String fechaf = "";
            for (int idat = 0; idat < data.size(); idat++) {

                tramainicio = data.get(idat).substring(8, 16).trim();
                tramafin = data.get(idat).substring(0, 8).trim();

                byte[] fechainicio = {(byte) (Integer.parseInt(tramainicio.substring(6, 8), 16) & 0xFF),
                    (byte) (Integer.parseInt(tramainicio.substring(4, 6), 16) & 0xFF),
                    (byte) (Integer.parseInt(tramainicio.substring(2, 4), 16) & 0xFF),
                    (byte) (Integer.parseInt(tramainicio.substring(0, 2), 16) & 0xFF)};
                byte[] fechafin = {(byte) (Integer.parseInt(tramafin.substring(6, 8), 16) & 0xFF),
                    (byte) (Integer.parseInt(tramafin.substring(4, 6), 16) & 0xFF),
                    (byte) (Integer.parseInt(tramafin.substring(2, 4), 16) & 0xFF),
                    (byte) (Integer.parseInt(tramafin.substring(0, 2), 16) & 0xFF)};
                int year = (int) ((fechainicio[0] & 0xFE) >> 1);
                int month = (int) (((fechainicio[0] & 0x01) << 3) | ((fechainicio[1] & 0xE0) >> 5));
                int dia = (int) ((fechainicio[1] & 0x1F));
                int hora = (int) ((fechainicio[2] & 0x00000F8) >> 3);
                int min = (int) (((fechainicio[2] & 0x07) << 3) | ((fechainicio[3] & 0x00000E0) >> 5));
                int seg = (int) ((fechainicio[3] & 0x0000001F) * 2);
                fechai = year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg;
                year = (int) ((fechafin[0] & 0xFE) >> 1);
                month = (int) (((fechafin[0] & 0x01) << 3) | ((fechafin[1] & 0xE0) >> 5));
                dia = (int) ((fechafin[1] & 0x1F));
                hora = (int) ((fechafin[2] & 0x00000F8) >> 3);
                min = (int) (((fechafin[2] & 0x07) << 3) | ((fechafin[3] & 0x00000E0) >> 5));
                seg = (int) ((fechafin[3] & 0x0000001F) * 2);
                fechaf = year + "/" + month + "/" + dia + " " + hora + ":" + min + ":" + seg;
                try {
                    ActualizaFechaELGAMApower(new Timestamp(sdf.parse(fechai).getTime()), new Timestamp(sdf.parse(fechaf).getTime()), seriemedidor, connection);
                } catch (Exception e) {
                    //contingencia Eventos
                    try {
                        if (rutaserver.length() > 0) {
                            if (!rutaserver.toUpperCase().contains("SMB")) {
                                File file = new File(rutaserver + "eventos\\" + archivoContingencia);
                                if (file != null) {
                                    fr = new RandomAccessFile(file, "rw");
                                    fr.seek(fr.length());
                                    StringBuilder cadena = new StringBuilder();
                                    cadena.append((seriemedidor.trim() + ";"));
                                    cadena.append((fechacontingencia.format(new Date(sdf.parse(fechai).getTime())) + ";"));
                                    cadena.append((fechacontingencia.format(new Date(sdf.parse(fechaf).getTime())) + "\r"));
                                    fr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                    fr.close();
                                }
                            } else {

                                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                                SmbFile file = new SmbFile(rutaserver + "eventos\\" + archivoContingencia, auth);
                                if (file != null) {
                                    sfr = new SmbRandomAccessFile(file, "rw");
                                    sfr.seek(sfr.length());
                                    StringBuilder cadena = new StringBuilder();
                                    cadena.append((seriemedidor.trim() + ";"));
                                    cadena.append((fechacontingencia.format(new Date(sdf.parse(fechai).getTime())) + ";"));
                                    cadena.append((fechacontingencia.format(new Date(sdf.parse(fechaf).getTime())) + "\r"));
                                    sfr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                    sfr.close();
                                }
                            }
                        }
                    } catch (Exception eex) {
                        System.err.println("Fecha: " + new Date() );
                        eex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public void eventosABB(Vector<String> data, String seriemedidor) {
        try {
            validateConn();
            boolean guardar = false;
            String tramaconexion = "";
            String tramadesconexion = "";
            for (int idat = 0; idat < data.size(); idat++) {
                if (data.get(idat).substring(0, 2).equals("00")) {
                    tramadesconexion = data.get(idat);
                } else if (data.get(idat).substring(0, 2).equals("01")) {
                    tramaconexion = data.get(idat);
                }
                if (tramaconexion.length() > 0 && tramadesconexion.length() > 0) {
                    guardar = true;
                }
                if (guardar) {
                    desglosarEventosABB(tramaconexion, tramadesconexion, seriemedidor, connection);
                    tramaconexion = "";
                    tramadesconexion = "";
                    guardar = false;
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            System.err.println("");
        }
    }

    //VIC 03-08-19
    public void almacenaEvento(ERegistroEvento evento, Connection conn) throws Exception {
        //VIC here //ojo error ORA-00001: unique constraint (TELESIMEXWEB.TELE01_EVENTOS_PK) violated
        //System.out.println("***Almacena evento***");
        validateConn();        
        String sql = "INSERT INTO tele01_eventos (vcserie,vcevento,dfechacorte,dfechareconexion,tiempocorte)"
                + " VALUES "
                + "(?,?,?,?,?)";
        //try (PreparedStatement pstm = connection.prepareStatement(sql)) {
        try (PreparedStatement pstm = genPrepStatement(sql)) {
            //System.out.println("evento.getVcserie()" + evento.getVcserie());
            pstm.setString(1, evento.getVcserie());
            //System.out.println("evento.getVctipo()" + evento.getVctipo());
            pstm.setString(2, evento.getVctipo());
            //System.out.println("evento.getVcfechacorte()" + evento.getVcfechacorte());
            pstm.setTimestamp(3, evento.getVcfechacorte());
            //System.out.println("evento.getVcfechareconexion()" + evento.getVcfechareconexion());
            pstm.setTimestamp(4, evento.getVcfechareconexion());
            //System.out.println("evento.getTiempocorte()" + evento.getTiempocorte());
            pstm.setInt(5, evento.getTiempocorte());
            pstm.execute();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }

    public void actualizaEvento(ERegistroEvento evento, Connection conn) throws Exception {
        if (conn == null || connection.isClosed()) {
            validateConn();
        }
        String sql = "SELECT * FROM tele01_eventos WHERE vcserie='" + evento.getVcserie() + "' "
                + "AND vcevento='" + evento.getVctipo() + "' "
                + "AND dfechacorte=? "
                + "AND dfechareconexion=?";
        PreparedStatement pstm2 = null;
        PreparedStatement pstm = null;
        ResultSet rs1 = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setTimestamp(1, evento.getVcfechacorte());
            pstm.setTimestamp(2, evento.getVcfechareconexion());
            rs1 = pstm.executeQuery();

            if (!rs1.next()) {
                //ingresa el registro nuevo   
                try {
                    rs1.close();
                    pstm.close();
                } catch (Exception e) {
                    System.err.println("Fecha: " + new Date() );
                    e.printStackTrace();
                }
                sql = "INSERT INTO tele01_eventos (vcserie,vcevento,dfechacorte,dfechareconexion)"
                        + " VALUES "
                        + "(?,?,?,?)";
                try {                    
                    //pstm2 = connection.prepareStatement(sql);
                    pstm2 = genPrepStatement(sql);
                    pstm2.setString(1, evento.getVcserie());
                    pstm2.setString(2, evento.getVctipo());
                    pstm2.setTimestamp(3, evento.getVcfechacorte());
                    pstm2.setTimestamp(4, evento.getVcfechareconexion());
                    pstm2.execute();
                } catch (Exception e) {
                    lecturacorrecta = false;
                    System.err.println("Fecha: " + new Date() );
                    e.printStackTrace();
                } finally {
                    try {
                        if (pstm2 != null) {
                            pstm2.close();
                        }
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                    pstm2 = null;
                }
            }
        } catch (Exception e) {
            lecturacorrecta = false;
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs1 != null) {
                    rs1.close();
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }
    }

    public List<Integer> getAllBrands_X_ModemsFrame() {
        List<Integer> brands = new ArrayList();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            validateConn();
            String sql = "SELECT DISTINCT(vcmarcamedidor) FROM tele01_conf_modem ORDER BY vcmarcamedidor ASC";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                brands.add(Integer.parseInt(rs.getString("vcmarcamedidor")));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }
        return brands;
    }

    public Vector<EConfModem> obtenerTramas(EMedidor med) {
        Vector<EConfModem> vconfmodem = new Vector<>();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            validateConn();

            String sql = "SELECT * FROM tele01_conf_modem WHERE vcmarcamedidor=? AND vcmodem=? AND vctipo=?";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, med.getMarcaMedidor().getCodigo());
            pstm.setString(2, med.getMarcaModem().getCodigo());
            pstm.setInt(3, med.getTipoconexion());
            rs = pstm.executeQuery();
            if (rs.next()) {
                EConfModem ecm = new EConfModem();
                ecm.setPeticion0(rs.getString("at0") == null ? "" : convertStringToHex(rs.getString("at0")));
                ////System.out.println(ecm.getPeticion0());
                ecm.setPeticion1(rs.getString("at1") == null ? "" : convertStringToHex(rs.getString("at1")));
                ecm.setPeticion2(rs.getString("at2") == null ? "" : convertStringToHex(rs.getString("at2")));
                ecm.setPeticion3(rs.getString("at3") == null ? "" : convertStringToHex(rs.getString("at3")));
                ecm.setPeticion4(rs.getString("at4") == null ? "" : convertStringToHex(rs.getString("at4")));
                ecm.setPeticion5(rs.getString("at5") == null ? "" : convertStringToHex(rs.getString("at5")));
                ecm.setPeticion6(rs.getString("at6") == null ? "" : convertStringToHex(rs.getString("at6")));
                ecm.setPeticion7(rs.getString("at7") == null ? "" : convertStringToHex(rs.getString("at7")));
                ecm.setPeticion8(rs.getString("at8") == null ? "" : convertStringToHex(rs.getString("at8")));
                ecm.setPeticion9(rs.getString("at9") == null ? "" : convertStringToHex(rs.getString("at9")));
                vconfmodem.add(ecm);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }

        }
        return vconfmodem;
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

    public byte[] ConvertASCIItoHex(String str) {
        String[] vstr = str.split(" ");
        byte[] vhex = new byte[vstr.length];
        for (int i = 0; i < vstr.length; i++) {
            vhex[i] = (byte) (Integer.parseInt(vstr[i], 16) & 0xFF);
        }
        return vhex;
    }

    private void desglosarEventosABB(String tramaconexion, String tramadesconexion, String seriemedidor, Connection conn) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        try {
            ERegistroEvento ereg = new ERegistroEvento();
            ereg.setVcserie(seriemedidor);
            ereg.setVctipo("0001");
            ereg.setVcfechacorte(new Timestamp(sdf.parse(tramadesconexion.substring(2, tramadesconexion.length())).getTime()));
            ereg.setVcfechareconexion(new Timestamp(sdf.parse(tramaconexion.substring(2, tramaconexion.length())).getTime()));
            actualizaEvento(ereg, connection);
        } catch (Exception e) {
        }

    }

    public void actualizaFechaLectura(String seriemedidor, String fechaactual) {
        //System.out.println("*** Validando Last Read:" + fechaactual + " ***");        
        String sql = "UPDATE TELE01_CONF_MEDIDOR SET DUFEC_LEC=?, VCOBSERVACIONES='AUTO' WHERE VCSERIE=?";
        SimpleDateFormat sgflect = new SimpleDateFormat("yyMMddHHmmss");
        validateConn();
        PreparedStatement pstm = null;
        try {
            Date fecha = sgflect.parse(fechaactual);
            pstm = genPrepStatement(sql);
            pstm.setTimestamp(1, new Timestamp(fecha.getTime()));
            pstm.setString(2, seriemedidor);
            pstm.executeUpdate();
            pstm.close();
        } catch (Exception e) {
            e.printStackTrace();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "ActualizaLastRead_Telesimex", "Error actualizando last read: " + fechaactual);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // vic 10-10-19 - no se usa en programada - has no sense yet
    public void actualizaPassword(String seriemedidor, String newpass, Connection conn) {
        //System.out.println("*** Actualizando password del medidor***");
        String sql = "UPDATE tele01_conf_medidor SET vcpassword=?, vcpassword2=? WHERE vcserie=?";
        if (conn == null) {
            validateConn();
        }
        PreparedStatement pstm = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, newpass);
            pstm.setString(2, newpass);
            pstm.setString(3, seriemedidor);
            pstm.executeUpdate();
            pstm.close();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }

    }
    
    /*
    public void actualizaFechaLecturaElgama(String seriemedidor, String fechaactual, Connection conn) {
        if (conn == null) {
            validateConn();
        }
        PreparedStatement pstm = null;
        String sql = "UPDATE tele01_conf_medidor SET DUFEC_LEC=? WHERE vcserie=?";
        SimpleDateFormat dfelgama = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        try {
            Date fecha = dfelgama.parse(fechaactual);
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setTimestamp(1, new Timestamp(fecha.getTime() - 1800000));
            pstm.setString(2, seriemedidor);
            pstm.executeUpdate();
            pstm.close();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }
    }
    */

    private EConstanteKE buscarConske(String seriemedidor, int vccanal, Connection conn) {
        EConstanteKE conske = null;
        ResultSet rs = null;
        PreparedStatement pstm = null;
        try {
            String sql = "SELECT * FROM tele01_constante WHERE vcserie=? AND ncanal=?";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, seriemedidor);
            pstm.setInt(2, vccanal);
            rs = pstm.executeQuery();
            if (rs.next()) {
                conske = new EConstanteKE();
                conske.setCanal(vccanal);
                conske.setSeriemedidor(seriemedidor);
                conske.setPesopulso(rs.getDouble("nconstanteke"));
                conske.setDivisor(rs.getDouble("ndivisor"));
                conske.setMultiplo(rs.getDouble("nmultiplo"));
                conske.setConsInterna(rs.getInt("lkeinterna") == 1 ? true : false);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if ( rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstm != null) {
                    pstm.close();
                }                
            } catch (Exception e) {
            }
        }
        return conske;
    }
    

    private EConstanteKE buscarConskeOptimizado(String seriemedidor, int vccanal, ArrayList<EConstanteKE> data) {
        EConstanteKE ke = null;
        for (EConstanteKE conske : data) {
            if (conske.getSeriemedidor().equals(seriemedidor) && conske.getCanal() == vccanal) {
                ke = conske;
                break;
            }
        }
        return ke;
    }

    public boolean isInteger(String text) {
        boolean resultado = false;
        try {
            Integer.parseInt(text);
            resultado = true;
        } catch (Exception e) {
            resultado = false;
        }
        return resultado;
    }

    public boolean isIntegerHex(String text) {
        boolean resultado = false;
        try {
            Integer.parseInt(text, 16);
            resultado = true;
        } catch (Exception e) {
            resultado = false;
        }
        return resultado;
    }

    public boolean isDouble(String text) {
        boolean resultado = false;
        try {
            Double.parseDouble(text);
            resultado = true;
        } catch (Exception e) {
            resultado = false;
        }
        return resultado;
    }

    public boolean isTimestamp(String fecha, SimpleDateFormat sdf) {
        boolean able = false;
        try {
            sdf.parse(fecha);
            able = true;
        } catch (Exception e) {

            able = false;
        }
        return able;
    }

    private void ActualizaFechaELGAMApower(Timestamp ini, Timestamp fin, String serie, Connection conn) throws Exception {
        ERegistroEvento ereg = new ERegistroEvento();
        ereg.setVcserie(serie);
        ereg.setVctipo("0001");
        ereg.setVcfechacorte(ini);
        ereg.setVcfechareconexion(fin);
        actualizaEvento(ereg, connection);

    }

    public String lecturaELGAMANCanales(ArrayList<String> data, String seriemedidor, String marca, int numcanal, Timestamp uFecLec, String fechaActual, String fechaActualizada, int intervalo, int ndias, int parteI, int firmware, double ktru, double ktri, int exp, int totalcanales, Vector<Electura> vlec, File fileLog) {
        try {
            Connection conn = getConn();
            Vector<EConstanteKE> lconske = buscarConstantesKe(seriemedidor);
            SimpleDateFormat sdfactual = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yy-MM-dd");
            Timestamp tsactual = new Timestamp(sdfactual.parse(fechaActual).getTime());
            long fechalec = 0;
            String fechalectura = "";
            int parteL = parteI;
            int diasleer = ndias;
            int nIntervals;
            for (int idat = 0; idat < data.size(); idat++) {
                if (idat == 0) {
                    fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                    fechalectura = sdf2.format(new Date(fechalec));
                } else {
                    if (intervalo == 15) {
                        parteL = parteL == 0 ? 1 : 0;
                        if (parteL == 0) {
                            diasleer--;
                            fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                            fechalectura = sdf2.format(new Date(fechalec));
                        }
                    } else {
                        diasleer--;
                        fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                        fechalectura = sdf2.format(new Date(fechalec));
                    }
                }
                
                nIntervals = findNIntervals( parteL, intervalo);
                fechaActualizada = desglosarTramaELGAMANCanales(data.get(idat), seriemedidor, marca, numcanal, uFecLec, fechaActual, fechaActualizada, intervalo, nIntervals, firmware, fechalectura, ktru, ktri, exp, totalcanales, lconske, conn, vlec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Fecha Actualizada: " + fechaActualizada);
        return fechaActualizada;
    }

    private String desglosarTramaELGAMANCanales(String trama, String seriemedidor, String marca, int numcanal, Timestamp ultimafechalec, String fechaActual, String fechaActualizada, int intervalo, int nIntervals, int firmware, String fechalectura, double ktru, double ktri, int exp, int totalcanales, Vector<EConstanteKE> lconske, Connection conn, Vector<Electura> vlec) {
        try {
            DecimalFormat decimalf = new DecimalFormat("######.#####");
            SimpleDateFormat dfABB = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            SimpleDateFormat spf = new SimpleDateFormat("yy-MM-dd HH:mm");
            EConstanteKE econske = null;
            //Vector<Electura> vlec = new Vector<Electura>();
            RandomAccessFile fr;
            SmbRandomAccessFile sfr;
            ArrayList<EtipoCanal> vtipocanal = null;
            vtipocanal = obtenerTipoCanales(marca);
            String energia = "";
            String hora = "";
            String min = "";
            SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
            String archivoContingencia = "" + seriemedidor + "_" + (fechacontingencia.format(new Date().getTime())) + "_" + numcanal + ".txt";
            int tiempo = intervalo;
            int aumento = 4;
            int factor = Integer.parseInt(trama.substring(0, 2), 16);
            Timestamp time = null;
            Timestamp feactual = new Timestamp(dfABB.parse(fechaActual).getTime() - 1800000);
            //System.out.println("Fecha Actual: " + feactual);
            
            Electura lec = null;
            String valorv = "";
            //Timestamp ultimafechalec = findUltimafechaLec(seriemedidor);            
            int mantisa = 0;
            int expo = 0;
            String canal = "";
            for (int i = 2; i < trama.length(); i += aumento) {
                byte[] valor = {(byte) (Integer.parseInt(trama.substring(i + 2, i + 4), 16) & 0xFF), (byte) (Integer.parseInt(trama.substring(i, i + 2), 16) & 0xFF)};
                int tiempolec = tiempo * nIntervals;
                hora = "" + ((int) (tiempolec / 60));
                min = "" + (int) (tiempolec % 60);
                String ts = fechalectura + " " + hora + ":" + min;
                time = new Timestamp(spf.parse(ts).getTime());
                //System.out.println("Time: " + time);
                if (firmware == 0) {//es old version
                    mantisa = ((valor[0] & 0X0000000F) << 8) | (valor[1] & 0X000000FF);
                    expo = (valor[0] & 0X000000F0) >> 4;
                    energia = "" + (decimalf.format((Math.pow(10, factor) * mantisa * (Math.pow(2, expo))) / 1000.0));
                } else {//new version
                    valorv = trama.substring(i + 2, i + 4) + "" + (trama.substring(i, i + 2));
                    energia = "" + (decimalf.format(((factor) * (Integer.parseInt(valorv, 16) / 10.0) * ktru * ktri * (Math.pow(10, exp))) / 1000.0));
                }
                energia = energia.replace(",", ".");
                if (time.before(feactual)) { //validamos si la fecha es despues de la fecha de la ultima lectura
                    try {

                        econske = buscarConske(lconske, numcanal);
                        for (EtipoCanal et : vtipocanal) {
                            if (Integer.parseInt(et.getCanal()) == numcanal) {
                                canal = et.getUnidad();
                                break;
                            }
                        }

                        if (econske != null) {
//                            throw new Exception("prueba");
                            lec = new Electura(time, seriemedidor, numcanal, trasnformarEnergia(Double.parseDouble(energia), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), Double.parseDouble(energia), intervalo, canal);
                            vlec.add(lec);
                            if (numcanal == totalcanales) {
                                if (ultimafechalec != null) {
                                    if (time.after(ultimafechalec)) {
                                        fechaActualizada = dfABB.format(new Date(time.getTime()));
                                        //actualiza = true;
                                    }
                                } else {
                                    //actualiza = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        //CONTROLAMOS EN EL PLAN DE CONTINGENCIA SE GUARDAN SIN UNIDAD POR SI SE CAE LA BASE DE DATOS TRAJENDO LAS UNIDADES
                        //EN EL SERVICIO SE TRAE LA UNIDAD PARA ALMACENAR
                        try {
                            if (rutaserver.length() > 0) {
                                if (!rutaserver.toUpperCase().contains("SMB")) {
                                    File file = new File(rutaserver + "lecturas\\" + archivoContingencia);
                                    if (file != null) {
                                        fr = new RandomAccessFile(file, "rw");
                                        fr.seek(fr.length());
                                        StringBuilder cadena = new StringBuilder();
                                        cadena.append((fechacontingencia.format(new Date(time.getTime())) + ";"));
                                        cadena.append((seriemedidor.trim() + ";"));
                                        cadena.append((("" + numcanal).trim() + ";"));
                                        cadena.append((("" + energia).trim() + ";"));
                                        cadena.append((("" + intervalo).trim() + "\r"));
                                        fr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                        fr.close();
                                    }
                                } else {
                                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                                    SmbFile file = new SmbFile(rutaserver + "lecturas\\" + archivoContingencia, auth);
                                    if (file != null) {
                                        sfr = new SmbRandomAccessFile(file, "rw");
                                        sfr.seek(sfr.length());
                                        StringBuilder cadena = new StringBuilder();
                                        cadena.append((fechacontingencia.format(new Date(time.getTime())) + ";"));
                                        cadena.append((seriemedidor.trim() + ";"));
                                        cadena.append((("" + numcanal).trim() + ";"));
                                        cadena.append((("" + energia).trim() + ";"));
                                        cadena.append((("" + intervalo).trim() + "\r"));
                                        sfr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                        sfr.close();
                                    }
                                }
                            }

                        } catch (Exception eex) {
                            eex.printStackTrace();
                        }

                    }

                }
                nIntervals++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Fecha Actualizada Desglose ElGama: " + fechaActualizada);
        return fechaActualizada;
    }
    
    private int findNIntervals(int parteL, int intervalo) {
        int n;
        if (parteL == 1) {
            n = ((60 / intervalo) * 12);
        } else {
            n = 0;
        }
        return n;
    }

    public void almacenaregistroDiario(Vector<String> data, String seriemedidor, String marca, String fechaActual, int dias, int numcanales) {
        try {
            validateConn();
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yy-MM-dd");
            Timestamp tsactual = new Timestamp(sdf.parse(fechaActual).getTime());
            long fechalec = 0;
            String fechalectura = "";
            int diasleer = dias - 1;
            for (int idat = 0; idat < data.size(); idat++) {
                fechalec = tsactual.getTime() - ((long) (86400000) * diasleer);
                fechalectura = sdf2.format(new Date(fechalec));
                diasleer--;
                desglosaRegistroDiarios(data.get(idat), seriemedidor, marca, numcanales, fechaActual, fechalectura, connection);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    private void desglosaRegistroDiarios(String trama, String seriemedidor, String marca, int numcanales, String fechaActual, String fechalectura, Connection conn) {
        try {
            SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
            String archivoContingencia = "" + seriemedidor + "_" + (fechacontingencia.format(new Date().getTime()) + ".txt");
            RandomAccessFile fr;
            SmbRandomAccessFile sfr;
            SimpleDateFormat spf = new SimpleDateFormat("yy-MM-dd HH:mm");
            int aumento = 24;
            int factor = Integer.parseInt(trama.substring(0, 2), 16);
            EConstanteKE econske = null;
            Timestamp time = null;
            String ts = fechalectura + " 00:00";
            int ncanal = 1;
            String tarifa1 = "";
            String tarifa2 = "";
            String tarifa3 = "";
            String tarifa4 = "";
            double energia = 0.0;
            time = new Timestamp(spf.parse(ts).getTime());
            PreparedStatement pstmcanal = null;
            ResultSet rscanal = null;
            String sqlcanal = "";
            String canal = "";
            int nc = 0;
            for (int i = 2; i < trama.length(); i += aumento) {
                tarifa1 = trama.substring(i + 4, i + 6) + trama.substring(i + 2, i + 4) + trama.substring(i, i + 2);
                tarifa2 = trama.substring(i + 10, i + 12) + trama.substring(i + 8, i + 10) + trama.substring(i + 6, i + 8);
                tarifa3 = trama.substring(i + 16, i + 18) + trama.substring(i + 14, i + 16) + trama.substring(i + 12, i + 14);
                tarifa4 = trama.substring(i + 22, i + 24) + trama.substring(i + 20, i + 22) + trama.substring(i + 18, i + 20);
                energia = (Integer.parseInt(tarifa1, 16) * (Math.pow(10, factor))) + (Integer.parseInt(tarifa2, 16) * (Math.pow(10, factor))) + (Integer.parseInt(tarifa3, 16) * (Math.pow(10, factor))) + (Integer.parseInt(tarifa4, 16) * (Math.pow(10, factor)));
                energia = energia / 1000.0;
                try {
                    sqlcanal = "SELECT * FROM tele01_tipo_registros WHERE vcmarca=? AND  vccanal=? AND vctiporegistro=0";
                    //pstmcanal = connection.prepareStatement(sqlcanal);
                    pstmcanal = genPrepStatement(sqlcanal);
                    canal = "";
                    try {
                        pstmcanal.setString(1, marca);
                        pstmcanal.setString(2, "" + ncanal);
                        rscanal = pstmcanal.executeQuery();
                        if (rscanal.next()) {
                            canal = rscanal.getString("vcnunidades");
                        }
                    } catch (Exception ex) {
                        try {
                            if (rscanal != null) {
                                rscanal.close();
                            }
                        } catch (Exception e) {
                        }
                        try {
                            if (pstmcanal != null) {
                                pstmcanal.close();
                            }
                        } catch (Exception e) {
                        }
                    }

                    nc = consultaCanal(ncanal);
                    econske = buscarConske(seriemedidor, nc, connection);
                    if (econske != null) {
                        ////System.out.println("año " + (time.getYear() + 1900) + " Mes " + (time.getMonth() + 1) + " dia " + time.getDate() + "pulsos " + energia + " energia " + (energia * econske.getMultiplo()));
                        //actualizamos registros diarios
                        actualizaRegistro(seriemedidor, 0, (time.getYear() + 1900), (time.getMonth() + 1), time.getDate(), new Timestamp(new Date().getTime()), energia, String.valueOf(ncanal), canal, energia * econske.getMultiplo(), connection);
                    }
                } catch (Exception e) {
                    //contingencia
                    try {
                        if (rutaserver.length() > 0) {
                            if (!rutaserver.toUpperCase().contains("SMB")) {
                                File file = new File(rutaserver + "registros\\" + archivoContingencia);
                                if (file != null) {
                                    fr = new RandomAccessFile(file, "rw");
                                    fr.seek(fr.length());
                                    StringBuilder cadena = new StringBuilder();
                                    cadena.append((seriemedidor.trim() + ";"));
                                    cadena.append(("0".trim() + ";"));
                                    cadena.append((("" + (time.getYear() + 1900)).trim() + ";"));
                                    cadena.append((("" + (time.getMonth() + 1)).trim() + ";"));
                                    cadena.append((("" + time.getDate()).trim() + ";"));
                                    cadena.append((fechacontingencia.format(new Date()) + ";"));
                                    cadena.append((("" + energia).trim() + ";"));
                                    cadena.append((String.valueOf(ncanal).trim() + "\r"));
                                    fr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                    fr.close();
                                }
                            } else {
                                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                                SmbFile file = new SmbFile(rutaserver + "registros\\" + archivoContingencia, auth);
                                if (file != null) {
                                    sfr = new SmbRandomAccessFile(file, "rw");
                                    sfr.seek(sfr.length());
                                    StringBuilder cadena = new StringBuilder();
                                    cadena.append((seriemedidor.trim() + ";"));
                                    cadena.append(("0".trim() + ";"));
                                    cadena.append((("" + (time.getYear() + 1900)).trim() + ";"));
                                    cadena.append((("" + (time.getMonth() + 1)).trim() + ";"));
                                    cadena.append((("" + time.getDate()).trim() + ";"));
                                    cadena.append((fechacontingencia.format(new Date()) + ";"));
                                    cadena.append((("" + energia).trim() + ";"));
                                    cadena.append((String.valueOf(ncanal).trim() + "\r"));
                                    sfr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                    sfr.close();
                                }
                            }
                        }
                    } catch (Exception eex) {
                        System.err.println("Fecha: " + new Date() );
                        eex.printStackTrace();
                    }
                } finally {
                    if (rscanal != null) {
                        rscanal.close();
                    }
                    if (pstmcanal != null) {
                        pstmcanal.close();
                    }
                }

                ncanal++;
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public ArrayList<EtipoCanal> obtenerTipoRegistros(String marca, String tiporegistro, Connection conn) {
        ArrayList<EtipoCanal> data = new ArrayList<EtipoCanal>();
        String sql = "SELECT * FROM tele01_tipo_registros WHERE vcmarca=? and VCTIPOREGISTRO=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        EtipoCanal tcanal = null;
        try {
            if (conn == null) {
                validateConn();
            }
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, marca);
            pstm.setString(2, tiporegistro);
            rs = pstm.executeQuery();
            while (rs.next()) {
                tcanal = new EtipoCanal();
                tcanal.setMarca(marca);
                tcanal.setDescripcion(rs.getString("VCDESCRIPCION"));
                tcanal.setCanal(rs.getString("VCCANAL"));
                tcanal.setUnidad(rs.getString("VCNUNIDADES"));
                tcanal.setTipo(rs.getString("VCTIPOREGISTRO"));
                data.add(tcanal);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {                   
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
        return data;
    }

    public int obtienePeriodoInt(String seriemedidor, Timestamp lastRead) throws SQLException {
        int periodoIntegracion = 0;
        SimpleDateFormat sgflect2 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat sd3 = new SimpleDateFormat("yyyy-MM-dd");
        PreparedStatement psObtienePeriodo = null;
        ResultSet rsObtienePeriodo = null;
        validateConn();
        String sql = " ";
        switch (db) {
            case "ORACLE":
                sql = "SELECT * FROM ("
                        + "SELECT MLEC.NINTERVALO FROM TELE01_MATRIZLEC MLEC "
                        + "WHERE MLEC.VCSERIE='" + seriemedidor + "' "
                        + "AND MLEC.DFECHA<=TO_DATE('" + sgflect2.format(new Date(lastRead.getTime())) + "','dd-MM-yyyy') "
                        + "order by MLEC.DFECHA desc) where rownum = 1";
                break;
            case "SQLServer":
                sql = "SELECT * FROM (SELECT MLEC.NINTERVALO, ROW_NUMBER() OVER(\n"
                        + "       ORDER BY MLEC.DFECHA desc) AS RowNum FROM TELE01_MATRIZLEC MLEC \n"
                        + "                WHERE MLEC.VCSERIE= '" + seriemedidor + "' "
                        + "                AND MLEC.DFECHA<= '" + sd3.format(new Date(lastRead.getTime())) + "' "
                        + "                 )a where a.RowNum = 1";
                break;

        }

        try {
            //psObtienePeriodo = connection.prepareStatement(sql);
            psObtienePeriodo = genPrepStatement(sql);
            rsObtienePeriodo = psObtienePeriodo.executeQuery();
            if (rsObtienePeriodo.next()) {
                periodoIntegracion = rsObtienePeriodo.getInt("NINTERVALO");
                //System.out.println("Obteniendo periodo del Medidor: " + seriemedidor);
                //System.out.println("con LastRead: " + sgflect2.format(new Date(lastRead.getTime())));
                //System.out.println("PeriodoIntegracion: " + periodoIntegracion);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            periodoIntegracion = 15;
            escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "ConsultaPeriodoDeIntegracion_ServTelesimex", "Error consultando periodo de integracion con el dia: " + sgflect2.format(new Date(lastRead.getTime())) + " - Asigna 15 por defecto");
        } finally {
            if (rsObtienePeriodo != null) {
                rsObtienePeriodo.close();
            }
            if (psObtienePeriodo != null) {
                psObtienePeriodo.close();
            }
        }
        return periodoIntegracion;
    }

    public int consultaMedidorNuevo(String seriemedidor) throws SQLException {
        int lmednuevo = 0;
        PreparedStatement psMedNuevo = null;
        ResultSet rsMedNuevo = null;
        try {
            String sql = "Select vcmednuevo from tele01_medidores where vcserie='" + seriemedidor + "'";
            validateConn();
            //psMedNuevo = connection.prepareStatement(sql);
            psMedNuevo = genPrepStatement(sql);
            rsMedNuevo = psMedNuevo.executeQuery();
            if (rsMedNuevo.next()) {
                lmednuevo = (rsMedNuevo.getString("vcmednuevo") == null ? 0 : Integer.parseInt(rsMedNuevo.getString("vcmednuevo")));
                //System.out.println("Medidor Nuevo?: " + (lmednuevo == 0 ? "No" : "Si"));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "consultaMedidorNuevoVolcado_ServTelesimex", "Error en consulta de medidor nuevo de Volcado");
        } finally {
            if (rsMedNuevo != null) {
                rsMedNuevo.close();
            }
            if (psMedNuevo != null) {
                psMedNuevo.close();
            }
        }
        return lmednuevo;
    }

    public void notMedidorNuevo(String seriemedidor) throws SQLException {
        Connection conn = null;
        PreparedStatement psMedNuevo = null;
        try {
            String sql = "Update tele01_medidores SET vcmednuevo=0 WHERE vcserie=?";
            validateConn();
            //psMedNuevo = connection.prepareStatement(sql);
            psMedNuevo = genPrepStatement(sql);
            psMedNuevo.setString(1, seriemedidor);
            psMedNuevo.executeQuery();
            psMedNuevo.close();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "ActualizaMedidorNuevo_ServTelesimex", "Estado Medidor Nuevo: " + 0);
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "ActualizaMedidorNuevo_ServTelesimex", "Error actualizando estado de medidor a no nuevo");
        } finally {
            if (psMedNuevo != null) {
                psMedNuevo.close();
            }
        }
    }

    public int historicosVolcado(String seriemedidor, Timestamp LastRead) throws SQLException {
        int historicos = 0;
        //System.out.println("*** Obtiene historicos para medidor: " + seriemedidor + " ***");
        String fechaParaRevision = "";
        SimpleDateFormat sgflect2 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat sd3 = new SimpleDateFormat("yyyy-MM-dd");
        Connection conn = null;
        PreparedStatement psHistoricoVolcado = null;
        ResultSet rshistoricoVolcado = null;
        String sql = "";
        try {
            switch (db) {
                case "ORACLE":
                    fechaParaRevision = sgflect2.format(new Date(LastRead.getTime()));
                    sql = "SELECT count(*) as historicos "
                            + "FROM TELE01_MATRIZLEC tml  "
                            + "WHERE "
                            + "tml.vcserie='" + seriemedidor + "'"
                            + " and tml.dfecha<TO_DATE('" + fechaParaRevision + "','dd-MM-yyyy') "
                            + "AND UPPER(tml.idvar) IN (UPPER('kWhD')) ";
                    break;
                case "SQLServer":
                    fechaParaRevision = sd3.format(new Date(LastRead.getTime()));
                    sql = "SELECT count(*) as historicos "
                            + "FROM TELE01_MATRIZLEC tml  "
                            + "WHERE "
                            + "tml.vcserie='" + seriemedidor + "'"
                            + " and tml.dfecha<'" + fechaParaRevision + "' "
                            + "AND UPPER(tml.idvar) IN (UPPER('kWhD')) ";
                    break;

            }
            //Costo 86, Cardinalidad 196
            validateConn();
            //psHistoricoVolcado = connection.prepareStatement(sql);
            psHistoricoVolcado = genPrepStatement(sql);
////                System.out.println("sql :"+sql);
            rshistoricoVolcado = psHistoricoVolcado.executeQuery();
            if (rshistoricoVolcado.next()) {
                historicos = rshistoricoVolcado.getInt("historicos");
                //System.out.println("historicos: " + historicos);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "consultaHistoricosVolcado_ServTelesimex", "Error en consulta de historicos de volcado desde: " + fechaParaRevision);
        } finally {
            if (rshistoricoVolcado != null) {
                rshistoricoVolcado.close();
            }
            if (psHistoricoVolcado != null) {
                psHistoricoVolcado.close();
            }
        }
        return historicos;
    }

    public String verificaAlmacenamiento(String seriemedidor, int periodoIntegracion, Timestamp lastRead, String newFechaUltLec) throws Exception {
        //System.out.println("*** Verifica almacenamiento para medidor: " + seriemedidor + " ***");
        reinicioPorVerificaVolcado = false;
        noLeidoPorVerificaVolcado = false;
        boolean medidorNuevo = (historicosVolcado(seriemedidor, lastRead) <= 1);
        boolean medidorReutilizado = (consultaMedidorNuevo(seriemedidor) == 1);//Si es 1 es nuevo
        int CantIntervalos = 0;
        int i = 0;
        SimpleDateFormat sDFTimestamp = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sDFTimestampC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sgflect = new SimpleDateFormat("yyMMdd");
        SimpleDateFormat sgflect2 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat sgflect3 = new SimpleDateFormat("yyMMddHHmmss");
        String actualLastRead = sgflect3.format(new Date(lastRead.getTime()));
        String fechaParaRevision = sgflect2.format(new Date(lastRead.getTime()));
        String lastReadS = sDFTimestamp.format(new Date(lastRead.getTime() + (long) (86400000)));
        String fechaSistema = sgflect2.format(new Date(new Date().getTime()));//new Timestamp(new Date().getTime());
        Date newFechaUltLecD = new SimpleDateFormat("yyMMddHHmmss").parse(newFechaUltLec);
        Timestamp newFechaUltLecT = null;
        Timestamp ultFechaVolcado = null;
        PreparedStatement psverifica = null;
        ResultSet rsverifica = null;
        String sql = "";
        //System.out.println("Clock Sistema: " + fechaSistema);
        //System.out.println("Fecha para revision: " + fechaParaRevision);
        if (fechaParaRevision.equals(fechaSistema)) {
            try {
                if (new Timestamp(newFechaUltLecD.getTime()).after(lastRead)) {
                    actualLastRead = sgflect3.format(new Date(newFechaUltLecD.getTime()));
                } else {
                    escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Inconsistencia en last read obtenido en sesion: " + newFechaUltLec + " - tipo1");
                    actualLastRead = sgflect3.format(new Date(lastRead.getTime()));
                }
            } catch (Exception e) {
                actualLastRead = sgflect3.format(new Date(new Date().getTime() - 1800000));
                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Error asignando last read obtenido en sesion: " + newFechaUltLec);
            }
        } else {
            if (medidorNuevo || medidorReutilizado) {
                //ignora la fecha del last read para la revision
                i++;//se pasa la fecha de revision al dia siguiente al last read
                //System.out.println("Medidor nuevo, fecha para revision: " + sgflect2.format(new Date(lastRead.getTime() + (long) (86400000) * (i))));
                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Medidor nuevo. Cambia fecha para revision a: " + sgflect2.format(new Date(lastRead.getTime() + (long) (86400000) * (i))));
                //fechaParaRevision = sgflect2.format(new Date(lastRead.getTime()+(long) (86400000)));
                lastReadS = sDFTimestamp.format(new Date(lastRead.getTime() + (long) (86400000)));
                newFechaUltLecT = Timestamp.valueOf(sDFTimestampC.format(new Date(newFechaUltLecD.getTime())));
                if (newFechaUltLecT.after(Timestamp.valueOf(lastReadS + " 00:00:00"))) {//(lastRead.getTime()+ (long) (86400000)))){
                    actualLastRead = sgflect3.format(new Date(newFechaUltLecD.getTime()));
                    //System.out.println("Last read obtenido en sesion: " + sgflect3.format(new Date(newFechaUltLecD.getTime())));
                } else {
                    //System.out.println("Inconsistencia en last read obtenido en sesion: " + sgflect3.format(new Date(newFechaUltLecD.getTime())));
                    escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Inconsistencia en last read obtenido en sesion: " + newFechaUltLec + " - tipo2");
                    actualLastRead = sgflect.format(lastRead.getTime()) + "000000";
                }
            }
        }
        while (!fechaParaRevision.equals(fechaSistema)) {
            try {
                fechaParaRevision = sgflect2.format(new Date(lastRead.getTime() + (long) (86400000) * (i)));
                switch (db) {
                    case "ORACLE":
                        sql = "SELECT * FROM "
                                + "(SELECT tml.dfecha,"
                                + "(tml.idx0+tml.idx1+tml.idx2+tml.idx3+tml.idx4+tml.idx5+tml.idx6+tml.idx7+"
                                + "tml.idx8+tml.idx9+tml.idx10+tml.idx11+tml.idx12+tml.idx13+tml.idx14+tml.idx15+"
                                + "tml.idx16+tml.idx17+tml.idx18+tml.idx19+tml.idx20+tml.idx21+tml.idx22+tml.idx23) AS CantIntervalos "
                                + "FROM TELE01_MATRIZLEC tml  "
                                + "WHERE "
                                + "tml.vcserie='" + seriemedidor + "'"
                                + " and tml.dfecha<=TO_DATE('" + fechaParaRevision + "','dd-MM-yyyy') "
                                +//TO_TIMESTAMP(TO_CHAR('"+fechaSistema+"','dd-MM-yyyy'),'dd-MM-yyyy') " + //Trae el ultimo dia anterior 
                                //"AND UPPER(tml.idvar) IN (UPPER('kWhD')) " +
                                "ORDER BY tml.dfecha DESC,tml.vcserie,tml.idvar "
                                + ") where rownum = 1"; //Costo 86, Cardinalidad 196

                        break;
                    case "SQLServer":
                        sql = "select * from (\n"
                                + "SELECT *, ROW_NUMBER() OVER (ORDER BY a.dfecha DESC) AS 'RowNumber' FROM \n"
                                + "(SELECT tml.dfecha,(tml.idx0+tml.idx1+tml.idx2+tml.idx3+tml.idx4+tml.idx5+tml.idx6+tml.idx7+\n"
                                + "tml.idx8+tml.idx9+tml.idx10+tml.idx11+tml.idx12+tml.idx13+tml.idx14+tml.idx15+\n"
                                + "tml.idx16+tml.idx17+tml.idx18+tml.idx19+tml.idx20+tml.idx21+tml.idx22+tml.idx23) AS CantIntervalos \n"
                                + "FROM TELE01_MATRIZLEC tml  \n"
                                + "WHERE \n"
                                + "tml.vcserie='" + seriemedidor + "'"
                                + "and tml.dfecha<=   CONVERT(date,'" + fechaParaRevision + "',105)   ) a) a  where a.RowNumber = 1 ";
                        break;
                }
                validateConn();
                //psverifica = connection.prepareStatement(sql);
                psverifica = genPrepStatement(sql);
////                System.out.println("sql :"+sql);
                rsverifica = psverifica.executeQuery();
                if (rsverifica.next()) {
                    CantIntervalos = rsverifica.getInt("CantIntervalos");
                    ultFechaVolcado = rsverifica.getTimestamp("dfecha");
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
                CantIntervalos = 0;
                ultFechaVolcado = new Timestamp(lastRead.getTime() + (long) (86400000) * (i));
                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Error en consulta de volcado desde: " + fechaParaRevision);
            } finally {
                if (rsverifica != null) {
                    rsverifica.close();
                }
                if (psverifica != null) {
                    psverifica.close();
                }
            }
            if (ultFechaVolcado == null) {
                fecVerificaVolcado = null;
                intervVerificaVolcado = 0;
                //System.out.println("Actualiza Last Read al siguiente dia por fecha de volcado antigua no encontrada: "
                        //+ sgflect.format(lastRead.getTime() + (long) (86400000)) + "000000");
                actualLastRead = sgflect.format(lastRead.getTime() + (long) (86400000)) + "000000";
                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Error obteniendo fecha de volcado mas proxima a: " + fechaParaRevision);
                reinicioPorVerificaVolcado = false;
                noLeidoPorVerificaVolcado = true;
                return actualLastRead;
            } else {
                fecVerificaVolcado = sgflect2.format(ultFechaVolcado);
                intervVerificaVolcado = CantIntervalos;
                //System.out.println("Ultimo registro almacenado: " + sgflect2.format(ultFechaVolcado));
                //System.out.println("Fecha a comparar: " + sgflect2.format(new Date(lastRead.getTime() + (long) (86400000) * (i))));
                if (sgflect2.format(ultFechaVolcado).equals(sgflect2.format(new Date(lastRead.getTime() + (long) (86400000) * (i)))) && (periodoIntegracion != 0)) {//verifica que el registro obtenido sea el de ayer
                    //System.out.println("Registro correcto");
                    //System.out.println("Numero de intervalos: " + CantIntervalos);
                    if (CantIntervalos == (1440 / periodoIntegracion)) { //verifica que los intervalos del dia anterior al last read esten completos
                        //System.out.println("Intervalos correctos");
                        if (fechaParaRevision.equals(fechaSistema) && sgflect2.format(newFechaUltLecD).equals(fechaSistema)) {
                            //si la fecha para revision es la de hoy actualiza con el last read que viene del procesamiento del perfil
                            //System.out.println("Last Read a actualizar obtenido en sesion: "
                                    //+ sgflect3.format(newFechaUltLecD.getTime()));
                            actualLastRead = sgflect3.format(newFechaUltLecD.getTime());
                        } else {
                            //si no, actualiza con la fecha siguiente a la revisada del volcado a las 00 horas
                            if (fechaParaRevision.equals(fechaSistema)) {
                                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Inconsistencia en last read obtenido en sesion: " + sgflect3.format(newFechaUltLecD.getTime()) + " - Tipo3");
                            }
                            //System.out.println("Last Read obtenida en verificacion: "
                                    //+ sgflect.format(ultFechaVolcado.getTime() + (long) (86400000)) + "000000");
                            actualLastRead = sgflect.format(ultFechaVolcado.getTime() + (long) (86400000)) + "000000";
                        }
                    } else { // si la cantidad no es correcta actualiza el last read con la fecha del volcado en la hora cero (u el obtenido si es hoy) y sale de la funcion
                        if (fechaParaRevision.equals(fechaSistema) && sgflect2.format(newFechaUltLecD).equals(fechaSistema)) {//si la fecha es hoy se actualiza con el last read obtenido en sesion si es consistente
                            //System.out.println("Last Read a actualizar obtenido en sesion: "
                                    //+ sgflect3.format(newFechaUltLecD.getTime()));
                            actualLastRead = sgflect3.format(newFechaUltLecD.getTime());
                        } else {
                            if (fechaParaRevision.equals(fechaSistema)) {//si la fecha es hoy y el last read de sesion no es consistente
                                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Inconsistencia en last read obtenido en sesion: " + sgflect3.format(newFechaUltLecD.getTime()) + " - Tipo4");
                            } else {
                                escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Inconsistencia en intervalos: " + CantIntervalos + " para el dia: " + sgflect2.format(ultFechaVolcado));
                                reinicioPorVerificaVolcado = true;
                            }
                            //System.out.println("Last Read a actualizar por inconsistencia en verificacion: "
                                    //+ sgflect.format(ultFechaVolcado.getTime()) + "000000");
                            actualLastRead = sgflect.format(ultFechaVolcado.getTime()) + "000000";
                        }
                        return actualLastRead;
                    }
                } else {
                    //si el volcado tiene una fecha más anterior al día de ayer del last read se actualiza con la fecha siguiente si los intervalos son correctos
                    // o con la fecha obtenida si los intervalos estan incompletos
                    if ((periodoIntegracion != 0) && CantIntervalos == (1440 / periodoIntegracion)) {
                        escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Fecha de volcado anterior con: " + CantIntervalos + " para el dia: " + sgflect2.format(ultFechaVolcado));
                        //System.out.println("Last Read a actualizar por fecha de volcado antigua en verificacion: "
                                //+ sgflect.format(ultFechaVolcado.getTime() + (long) (86400000)) + "000000");
                        actualLastRead = sgflect.format(ultFechaVolcado.getTime() + (long) (86400000)) + "000000";
                    } else {
                        escribirLog(Equipo, new Timestamp(new Date().getTime()), seriemedidor, "VerificaVolcado_ServTelesimex", "Inconsistencia en fecha de volcado anterior con: " + CantIntervalos + " para el dia: " + sgflect2.format(ultFechaVolcado));
                        //System.out.println("Last Read a actualizar por fecha de volcado antigua en verificacion con inconsistencias: "
                                //+ sgflect.format(ultFechaVolcado.getTime()) + "000000");
                        actualLastRead = sgflect.format(ultFechaVolcado.getTime()) + "000000";
                    }
                    reinicioPorVerificaVolcado = true;
                    return actualLastRead;
                }
            }
            i++; //siguiente fecha
        }
        return actualLastRead;
    }

    public void actualizaRegistro(String seriemedidor, int tiporegistro, int year, int month, int day, Timestamp actual, double pulsos, String ncanal, String idvar, double energia, Connection conn) {
        validateConn();        
        String sql = "SELECT * FROM tele01_registros_lec WHERE vcserie=? AND vctiporegistro=? AND vcano=? AND vcmes=? AND vccanal=?";
        sql = sql + " AND vcdia=?";

        String sql2 = "";
        PreparedStatement pstm1 = null;
        PreparedStatement pstm2 = null;
        ResultSet rs1 = null;
        try {
            //pstm1 = connection.prepareStatement(sql);
            pstm1 = genPrepStatement(sql);
            pstm1.setString(1, seriemedidor);
            pstm1.setString(2, "" + tiporegistro);
            pstm1.setString(3, "" + year);
            pstm1.setString(4, "" + month);
            pstm1.setString(5, "" + ncanal);
            pstm1.setString(6, "" + day);
            rs1 = pstm1.executeQuery();
            if (rs1.next()) {
                sql2 = "UPDATE tele01_registros_lec SET npulsos=?, nlec=?, dfec_ult_lec=? WHERE vcserie=? AND vctiporegistro=? AND vcano=? AND vcmes=? AND vccanal=?";
                sql2 = sql2 + " and vcdia=?";
                try {
                    //pstm2 = connection.prepareStatement(sql2);
                    pstm2 = genPrepStatement(sql2);
                    pstm2.setDouble(1, pulsos);
                    pstm2.setDouble(2, energia);
                    pstm2.setTimestamp(3, actual);
                    pstm2.setString(4, seriemedidor);
                    pstm2.setString(5, "" + tiporegistro);
                    pstm2.setString(6, "" + year);
                    pstm2.setString(7, "" + month);
                    pstm2.setString(8, ncanal);
                    pstm2.setString(9, "" + day);
                    pstm2.executeUpdate();
                } catch (Exception e) {
                } finally {
                    try {
                        if (pstm2 != null) {
                            pstm2.close();
                        }
                    } catch (Exception e) {
                    }
                }
            } else {
                sql2 = "INSERT INTO tele01_registros_lec (npulsos,nlec,dfec_ult_lec,vcserie,vctiporegistro,vcano,vcmes,vccanal,vcdia,idvar)VALUES( ?,?,?,?,?,?,?,?,?,?)";
                try {
                    //pstm2 = connection.prepareStatement(sql2);
                    pstm2 = genPrepStatement(sql2);
                    pstm2.setDouble(1, pulsos);
                    pstm2.setDouble(2, energia);
                    pstm2.setTimestamp(3, actual);
                    pstm2.setString(4, seriemedidor);
                    pstm2.setString(5, "" + tiporegistro);
                    pstm2.setString(6, "" + year);
                    pstm2.setString(7, "" + month);
                    pstm2.setString(8, ncanal);
                    pstm2.setString(9, "" + day);
                    pstm2.setString(10, idvar);
                    pstm2.executeUpdate();
                } catch (Exception e) {
                } finally {
                    try {
                        if (pstm2 != null) {
                            pstm2.close();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs1 != null) {
                    rs1.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstm1 != null) {
                    pstm1.close();
                }
            } catch (Exception e) {
            }
        }

    }

    private void actualizaRegistroTasas(String seriemedidor, int tiporegistro, int year, int month, int day, Timestamp actual, double pulsos, String ncanal, String idvar, double energia, String tasa, String fase, Connection conn) {
        String sql = "SELECT * FROM tele01_registros_lec WHERE vcserie=? AND vctiporegistro=? AND vcano=? AND vcmes=? AND vccanal=?";
        sql = sql + " AND vcdia=? AND vctipotasa=? AND vcfase=?";
        ////System.out.println("valor Tasa " + tasa);
        ////System.out.println("valor lectura " + energia);
        String sql2 = "";
        try {
            //try (PreparedStatement pstm1 = connection.prepareStatement(sql)) {
            try (PreparedStatement pstm1 = genPrepStatement(sql)) {
                pstm1.setString(1, seriemedidor);
                pstm1.setString(2, "" + tiporegistro);
                pstm1.setString(3, "" + year);
                pstm1.setString(4, "" + month);
                pstm1.setString(5, "" + ncanal);
                pstm1.setString(6, "" + day);
                pstm1.setString(7, tasa);
                pstm1.setString(8, fase);
                PreparedStatement pstm2 = null;
                try (ResultSet rs1 = pstm1.executeQuery()) {
                    if (rs1.next()) {
                        sql2 = "UPDATE tele01_registros_lec SET npulsos=?, nlec=?, dfec_ult_lec=? WHERE vcserie=? AND vctiporegistro=? AND vcano=? AND vcmes=? AND vccanal=?";
                        sql2 = sql2 + " AND vcdia=? AND vctipotasa=? AND vcfase=?";
                        //pstm2 = connection.prepareStatement(sql2);
                        pstm2 = genPrepStatement(sql2);
                        pstm2.setDouble(1, pulsos);
                        pstm2.setDouble(2, energia);
                        pstm2.setTimestamp(3, actual);
                        pstm2.setString(4, seriemedidor);
                        pstm2.setString(5, "" + tiporegistro);
                        pstm2.setString(6, "" + year);
                        pstm2.setString(7, "" + month);
                        pstm2.setString(8, ncanal);
                        pstm2.setString(9, "" + day);
                        pstm2.setString(10, tasa);
                        pstm2.setString(11, fase);
                        pstm2.executeUpdate();
                        pstm2.close();
                    } else {
                        sql2 = "INSERT INTO tele01_registros_lec (npulsos,nlec,dfec_ult_lec,vcserie,vctiporegistro,vcano,vcmes,vccanal,vcdia,idvar,vctipotasa,vcfase)VALUES( ?,?,?,?,?,?,?,?,?,?,?,?)";
                        //pstm2 = connection.prepareStatement(sql2);
                        pstm2 = genPrepStatement(sql2);
                        pstm2.setDouble(1, pulsos);
                        pstm2.setDouble(2, energia);
                        pstm2.setTimestamp(3, actual);
                        pstm2.setString(4, seriemedidor);
                        pstm2.setString(5, "" + tiporegistro);
                        pstm2.setString(6, "" + year);
                        pstm2.setString(7, "" + month);
                        pstm2.setString(8, ncanal);
                        pstm2.setString(9, "" + day);
                        pstm2.setString(10, idvar);
                        pstm2.setString(11, tasa);
                        pstm2.setString(12, fase);
                        
                        pstm2.executeUpdate();
                        pstm2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }

    public void almacenaregistroMensual(Vector<String> data, String seriemedidor, String marca, String fechaActual, int nmesreg, int numcanales) {
        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            validateConn();
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yy-MM-dd");
            Timestamp tsactual = new Timestamp(sdf.parse(fechaActual).getTime());
            Calendar c = Calendar.getInstance();
            Date dfechalec = null;
            long fechalec = 0;
            String fechalectura = "";
            int mesleer = nmesreg - 1;
            for (int idat = 0; idat < data.size(); idat++) {
                c.set(tsactual.getYear() + 1900, tsactual.getMonth() + 1, 1);
                c.add(Calendar.MONTH, ((mesleer + 1) * (-1)));
                dfechalec = c.getTime();
                fechalectura = sdf2.format(dfechalec);
////                System.out.println("Fecha lectura " + fechalectura);
////                System.out.println("Datos " + data.get(idat));
                desglosaRegistroMensual(data.get(idat), seriemedidor, marca, numcanales, fechaActual, fechalectura, mesleer, connection);
                mesleer--;
            }
            //connection.close();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    private void desglosaRegistroMensual(String trama, String seriemedidor, String marca, int numcanales, String fechaActual, String fechalectura, int mesleer, Connection conn) {
        try {
            int aumento = 32;
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
            SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
            int factor = Integer.parseInt(trama.substring(0, 2), 16);
            String archivoContingencia = "" + seriemedidor + "_" + (fechacontingencia.format(new Date().getTime()) + ".txt");
            RandomAccessFile fr;
            SmbRandomAccessFile sfr;
            EConstanteKE econske = null;
            Date time = null;
            String ts = fechalectura + " 00:00";
            int ncanal = 1;
            String tarifa1 = "";
            String tarifa2 = "";
            String tarifa3 = "";
            String tarifa4 = "";
            double energia = 0.0;

            time = sdf.parse(fechalectura);
            ////System.out.println("Registro mensual mes " + (mesleer));
            for (int i = 2; i < trama.length(); i += aumento) {
                tarifa1 = trama.substring(i + 6, i + 8) + trama.substring(i + 4, i + 6) + trama.substring(i + 2, i + 4) + trama.substring(i, i + 2);
                tarifa2 = trama.substring(i + 14, i + 16) + trama.substring(i + 12, i + 14) + trama.substring(i + 10, i + 12) + trama.substring(i + 8, i + 10);
                tarifa3 = trama.substring(i + 22, i + 24) + trama.substring(i + 20, i + 22) + trama.substring(i + 18, i + 20) + trama.substring(i + 16, i + 18);
                tarifa4 = trama.substring(i + 30, i + 32) + trama.substring(i + 28, i + 30) + trama.substring(i + 26, i + 28) + trama.substring(i + 24, i + 26);

                energia = Integer.parseInt(tarifa1) + Integer.parseInt(tarifa2) + Integer.parseInt(tarifa3) + Integer.parseInt(tarifa4);
                energia = (energia * Math.pow(10.0, factor)) / 1000.0;
                try {
                    String sqlcanal = "SELECT * FROM tele01_tipo_registros WHERE vcmarca=? AND  vccanal=? AND vctiporegistro=1";
                    PreparedStatement pstmcanal = null;
                    ResultSet rscanal = null;
                    String canal = "";
                    try {
                        //pstmcanal = connection.prepareStatement(sqlcanal);
                        pstmcanal = genPrepStatement(sqlcanal);
                        pstmcanal.setString(1, marca);
                        pstmcanal.setString(2, "" + ncanal);
                        rscanal = pstmcanal.executeQuery();
                        if (rscanal.next()) {
                            canal = rscanal.getString("vcnunidades");
                        }
                    } catch (Exception ex) {
                        try {
                            if (rscanal != null) {
                                rscanal.close();
                            }
                        } catch (Exception e) {
                        }
                        try {
                            if (pstmcanal != null) {
                                pstmcanal.close();
                            }                           
                        } catch (Exception e) {
                        }
                    }
                    int nc = consultaCanal(ncanal);
                    econske = buscarConske(seriemedidor, nc, connection);
                    if (econske != null) {
                        //System.out.println("Serie " + seriemedidor + " año " + (time.getYear() + 1900) + " Mes " + (time.getMonth() + 1) + " dia " + time.getDate() + "pulsos " + energia + " energia " + (energia * econske.getMultiplo()));
                        actualizaRegistro(seriemedidor, 1, (time.getYear() + 1900), (time.getMonth() + 1), time.getDate(), new Timestamp(new Date().getTime()), energia, String.valueOf(ncanal), canal, energia * econske.getMultiplo(), connection);
                    }
                } catch (Exception e) {
                    //contingencia
                    try {
                        if (rutaserver.length() > 0) {
                            if (!rutaserver.toUpperCase().contains("SMB")) {
                                File file = new File(rutaserver + "registros\\" + archivoContingencia);
                                if (file != null) {
                                    fr = new RandomAccessFile(file, "rw");
                                    fr.seek(fr.length());
                                    StringBuilder cadena = new StringBuilder();
                                    cadena.append((seriemedidor.trim() + ";"));
                                    cadena.append(("1".trim() + ";"));
                                    cadena.append((("" + (time.getYear() + 1900)).trim() + ";"));
                                    cadena.append((("" + (time.getMonth() + 1)).trim() + ";"));
                                    cadena.append((("" + time.getDate()).trim() + ";"));
                                    cadena.append((fechacontingencia.format(new Date()) + ";"));
                                    cadena.append((("" + energia).trim() + ";"));
                                    cadena.append((String.valueOf(ncanal).trim() + "\r"));
                                    fr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                    fr.close();
                                }
                            } else {
                                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                                SmbFile file = new SmbFile(rutaserver + "registros\\" + archivoContingencia, auth);
                                if (file != null) {
                                    sfr = new SmbRandomAccessFile(file, "rw");
                                    sfr.seek(sfr.length());
                                    StringBuilder cadena = new StringBuilder();
                                    cadena.append((seriemedidor.trim() + ";"));
                                    cadena.append(("1".trim() + ";"));
                                    cadena.append((("" + (time.getYear() + 1900)).trim() + ";"));
                                    cadena.append((("" + (time.getMonth() + 1)).trim() + ";"));
                                    cadena.append((("" + time.getDate()).trim() + ";"));
                                    cadena.append((fechacontingencia.format(new Date()) + ";"));
                                    cadena.append((("" + energia).trim() + ";"));
                                    cadena.append((String.valueOf(ncanal).trim() + "\r"));
                                    sfr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                    sfr.close();
                                }
                            }
                        }
                    } catch (Exception eex) {
                        System.err.println("Fecha: " + new Date() );
                        eex.printStackTrace();
                    }
                }
                ncanal++;
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    private int consultaCanal(int ncanal) {
        int ncanaltemp = 0;
        if (ncanal == 1) {
            ncanaltemp = 1;
        } else if (ncanal == 2) {
            ncanaltemp = 3;
        } else if (ncanal == 3) {
            ncanaltemp = 2;
        } else if (ncanal == 4) {
            ncanaltemp = 4;
        }
        return ncanaltemp;
    }

    public String buscarParametrosString(int i) {
        String server = "";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            validateConn();
            String sql = "SELECT * FROM tele01_parametros WHERE vccodparam=?";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setInt(1, i);
            rs = pstm.executeQuery();
            if (rs.next()) {
                server = rs.getString("vcvalor");
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }
        return server;

    }
    
    public EMedidor agregarParametrosAvanzadosDelMedidor(EMedidor medidor) {
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            getConn();
            String sql = "SELECT * FROM TELE01_PARAM_ADV_MED WHERE vcserie=?";
            pstm = connection.prepareStatement(sql);
            pstm.setString(1, medidor.getnSerie());
            rs = pstm.executeQuery();
            while (rs.next()) {
                medidor.addParametrosAvanzadosPorMedidor(rs.getInt("NCOD"), rs.getString("VCVALUE"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return medidor;
    }

    public int buscarParametros(int i) {
        int valor = 5;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            validateConn();
            String sql = "SELECT * FROM tele01_parametros WHERE vccodparam=?";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setInt(1, i);
            rs = pstm.executeQuery();
            if (rs.next()) {
                valor = rs.getInt("vcvalor");
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }
        return valor;
    }

    public void AlmacenaPerfilCargaElster(String serie, String marca, Timestamp uFecLec, String fechaactual, int NBR_BLK_INTS_SET1, int numCanales, String LP_CTRL_INT_FMT_CDE1,
            ArrayList<String> desglosePerfil, int intervalo, String regST062[], String[] regSt015, String[] regMt017,
            String External_Multiplier, String Ext_Mult_Scale_Factor, String Instrumentation_Scale_Factor, String OrdenCanales[], File fileLog) {

        ArrayList<EConstanteKE> vconske = null;
        ArrayList<EtipoCanal> vtipocanal = null;
        Vector<Electura> vlec = new Vector<>();
        LinkedHashMap<String, Double> fechasLec = new LinkedHashMap<>();
        RandomAccessFile fr;
        SmbRandomAccessFile sfr;
        //Simple Date Format's
        SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyMMddHHmmss");
        //Strings STATUS
        String commStatus = "", status = ""; 
        
        try {           
            Connection conn = getConn();
            vconske = obtenerConstantesKe(serie);
            vtipocanal = obtenerTipoCanales(marca);

            Electura lectura = null;
            EConstanteKE econske = null;

            //Timestamp ufec = findUltimafechaLec(serie);
            //Timestamp actualizafec = null;
            String tramaPerfil = "";
            ArrayList<Integer> possibleDate = new ArrayList<>();
            //Timestamp fecActual = new Timestamp(sdf2.parse(fechaactual).getTime() - (60000 * intervalos * 2));
            Timestamp fecActual = new Timestamp(sdf2.parse(fechaactual).getTime());
            int tamCanales = 0;
            int int_status = ((int) ((NBR_BLK_INTS_SET1 + 7) / 8)) * 2;
            ////System.out.println("Intervalos vacíos: "+NBR_BLK_INTS_SET1 );
            ////System.out.println("Int Status: "+int_status);
            int extend_status = (int) (numCanales / 2);
            if (extend_status == 0) {
                extend_status = 1;
            }
            //System.out.print("LP_CTRL_INT_FMT_CDE1: "+LP_CTRL_INT_FMT_CDE1);
            escribir("LP_CTRL_INT_FMT_CDE1: " + Integer.parseInt(LP_CTRL_INT_FMT_CDE1, 16), fileLog);
            switch ((Integer.parseInt(LP_CTRL_INT_FMT_CDE1, 16) & 0xFF)) {
                case 16:
                    tamCanales = 2;
                    break;
                case 64:
                    tamCanales = 6;
                    break;
                case 128:
                    tamCanales = 5;
                    break;
                default:
                    tamCanales = 2;
                    break;
            }
            String canales = "";
            int commonStatusInterval = 0;
            int channelStatus[] = new int[numCanales];
            String valorcanal = "";
            Timestamp fechaIntervalo = null, startDateStatusEvent = null, endDateStatusEvent = null;
            int tamIntervalo = (1 + (extend_status) + (tamCanales * numCanales)) * 2;
            escribir("Tamaño Intervalo: " + tamIntervalo, fileLog);
            String str_int_status = "";
            Byte[] byteStatusArr;
            boolean[] tempByte2BooleanArr = new boolean[8];
            boolean[] validIntervalsArr;
            boolean lastBlock = false, search = false, ignoreLastInterval = false, possibleEvent = false;
            Object[] container = new Object[2];           
            String archivoContingencia = "" + serie + "_" + (fechacontingencia.format(new Date().getTime()) + ".txt");
            
            for (int i = 0; i < desglosePerfil.size(); i++) {
                lastBlock = !((i + 1) < desglosePerfil.size());
                tramaPerfil = desglosePerfil.get(i);
                escribir("Bloque No." + i, fileLog);
                escribir(tramaPerfil, fileLog);
                //buscamos el int_status
                if (!(tramaPerfil.substring(0, 2) + tramaPerfil.substring(2, 4) + tramaPerfil.substring(4, 6)).equals("FFFFFF")) {
                    str_int_status = tramaPerfil.substring(10, 10 + (int_status));
                    escribir("Interval Status: " + str_int_status, fileLog);
                    byteStatusArr = new Byte[str_int_status.length() / 2];
                    validIntervalsArr = new boolean[byteStatusArr.length * 8];
                    for (int j = 0; j < byteStatusArr.length; j++) {
                        //obtenemos el int_status bytes marcados en negro
                        byteStatusArr[j] = (byte) (Integer.parseInt(str_int_status.substring(j * 2, 2 * (j + 1)), 16) & 0xFF);
                        tempByte2BooleanArr = byte2BooleanArr(byteStatusArr[j]);
                        ////System.out.println(Arrays.toString( tempByte2BooleanArr ));
                        System.arraycopy(tempByte2BooleanArr, 0, validIntervalsArr, j * 8, 8);
                    }
                    escribir("Vector de Intervalos validos", fileLog);
                    escribir(Arrays.toString(validIntervalsArr), fileLog);
                    canales = "";
                    valorcanal = "";
                    //fechaIntervalo = null;
                    int n = tramaPerfil.length() - (10 + int_status);
                    int interval2WhichDateBelongs = 15;
                    possibleDate = new ArrayList<>();
                    for (int j = 0; j < tramaPerfil.substring(0, 10).length(); j += 2) {
                        possibleDate.add(Integer.parseInt(tramaPerfil.substring(j, j + 2), 16));
                    }
                    // Buscamos el primer 1 recorriendo el array de atrás para calcular la fecha inicial del Bloque
                    for (int idx = validIntervalsArr.length - 1; idx >= 0; idx -= 1) {
                        if (validIntervalsArr[idx]) {
                            interval2WhichDateBelongs = idx;
                            break;
                        }
                    }
                    //Rutina para calcular la fecha inicial del bloque                    
                    container = findBlockInitialDate(possibleDate, interval2WhichDateBelongs, intervalo, lastBlock, fechaIntervalo, sdf1);
                    fechaIntervalo = (Timestamp) container[0];
                    ignoreLastInterval = (boolean) container[1];
                    //empezamos a desglosar los intervalos

                    float numIntervalos = n / tamIntervalo;
                    //System.out.println("Número de Intervalos por bloque: " + numIntervalos + " Long Trama Perfil: " + tramaPerfil.length());
                    for (int t = (10 + int_status), nInt = 1; t < tramaPerfil.length(); t += tamIntervalo, nInt++) {
                        canales = tramaPerfil.substring(t, t + tamIntervalo);//separamos los canales de los demas datos del intervalo
                        escribir("Intervalo " + nInt + "  " + canales, fileLog);
                        commonStatusInterval = (Integer.parseInt(canales.substring(0, 1), 16) & 0x0F); //Bits 0,1,2,3     
                        //System.out.println("Common Status Interval: " + commonStatusInterval);
                        for (int ch = 1; ch <= numCanales; ch++) {
                            //Llenar los status de cada canal
                            channelStatus[ch - 1] = Integer.parseInt(canales.substring(ch, ch + 1), 16) & 0x0F;
                            if (channelStatus[ch - 1] > 0) {
                                search = true;
                                status = statusChannel(channelStatus[ch - 1]);
                            }
                        }
                        boolean realizaIntervalo = validIntervalsArr[nInt - 1];
                        if (lastBlock) {
                            //System.out.println("Último Bloque");
                            if (nInt - 1 == interval2WhichDateBelongs) {
                                //System.out.println("Último intervalo válido");
                                if (ignoreLastInterval) {
                                    realizaIntervalo = false;
                                }
                            }
                        }

                        if (search) {
                            search = false;
                            if (!possibleEvent) {
                                possibleEvent = true;
                            }
                            commStatus =  commonStatus(commonStatusInterval);
                        }

                        if (nInt > 1) {//si no es el ´primer Intervalo
                            long intPeriod = 60000 * intervalo;
                            fechaIntervalo = new Timestamp(fechaIntervalo.getTime() + intPeriod);
                        }
                        if (realizaIntervalo) {//si el valor de boolean es 1 se almacena el intervalo o si se detecta que hay un evento en el status y se activa la bandera de ceros válidos.
                            if (startDateStatusEvent != null) {
                                endDateStatusEvent = new Timestamp(fechaIntervalo.getTime());
                                escribirLog(Equipo,  new Timestamp( new Date().getTime() ), serie, "Elster --> Evento encontrado",  "Status: " + status + ", Common Status: " + commStatus + ", Fecha inicial: " + startDateStatusEvent + ", Fecha final: " + endDateStatusEvent );
                                ERegistroEvento ereg = new ERegistroEvento();
                                ereg.setVcserie(serie);
                                ereg.setVctipo("0001");
                                ereg.setVcfechacorte( startDateStatusEvent );
                                ereg.setVcfechareconexion( endDateStatusEvent );
                                actualizaEvento(ereg, conn);
                                //System.out.println("Guardar Evento, Fecha inicial: " + startDateStatusEvent + ", Fecha final: " + endDateStatusEvent);
                                startDateStatusEvent = null;
                                endDateStatusEvent = null;
                            }
                            //System.out.println("Intervalo Valido");
                            //validacion fecha actual pendiente
                            //System.out.println("" + fechaIntervalo);
                            if (fechaIntervalo.before(fecActual)) {
                                //System.out.println("Fecha intervalo " + sdf1.format(new Date(fechaIntervalo.getTime())));
                                int ncanal = 1; //enumeramos los canales en el orden que vienen de izq a derecha el orden se tiene en el vector de canales
                                boolean firstChannel = true;
                                for (int q = (1 + extend_status) * 2; q < canales.length(); q = q + (tamCanales * 2)) {
                                    //separamos los canales dependiendo del valor de LP_CTRL_INT_FMT_CDE1 que define el numero de bytes por canal
                                    valorcanal = "";
                                    double dato = 0;
                                    double datopulsos = 0;
                                    for (int m = canales.substring(q, q + (tamCanales * 2)).length(); m > 0; m -= 2) {
                                        valorcanal += (canales.substring(q, q + (tamCanales * 2)).substring(m - 2, m));
                                    }
                                    if (tamCanales == 2) {//vienen en pulsos
                                        datopulsos = Integer.parseInt(valorcanal, 16);
                                        dato = datopulsos * Integer.parseInt(regST062[(((numCanales) * 3) + 1 + ((numCanales) * 2)) + ((ncanal - 1) * 2) + 1] + regST062[(((numCanales) * 3) + 1 + ((numCanales) * 2)) + ((ncanal - 1) * 2)], 16);//cambiar el 1 por el divisor chanel                                    
                                        dato = dato * (Integer.parseInt(regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 5]
                                                + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 4]
                                                + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 3]
                                                + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 2]
                                                + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 1]
                                                + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25)], 16)) * (Math.pow(10.0, COMPLEMENT2(true, (byte) ((Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 4], 16)) & 0xFF))));
                                        if (regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 12].equals("01")) {
                                            //se hace el paso 3
                                            dato = dato * (Integer.parseInt(regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 18]
                                                    + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 17]
                                                    + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 16]
                                                    + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 15]
                                                    + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 14]
                                                    + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 13], 16)) * Math.pow(10, COMPLEMENT2(false, (byte) (Integer.parseInt(Instrumentation_Scale_Factor, 16) & 0xFF)))
                                                    * (Integer.parseInt(regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 24) * 7) + 6], 16) * 25) + 18]
                                                            + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 23]
                                                            + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 22]
                                                            + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 21]
                                                            + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 20]
                                                            + regSt015[(Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 6], 16) * 25) + 19], 16)) * Math.pow(10, COMPLEMENT2(false, (byte) (Integer.parseInt(Instrumentation_Scale_Factor, 16) & 0xFF)));
                                        } else {
                                            //se pasa al paso 4                                       
                                        }
                                        //falta condicion si se usa ese mulltiplo o el ingresado por hoja de vida //pendiente
                                        dato = dato * Integer.parseInt(External_Multiplier, 16) * Math.pow(10, COMPLEMENT2(false, (byte) (Integer.parseInt(Ext_Mult_Scale_Factor, 16) & 0xFF)));
                                        dato = dato / 1000;
                                    } else {//no vienen en pulsos
                                        //preguntar a melisa
                                        dato = Integer.parseInt(valorcanal, 16) * (Math.pow(10.0, COMPLEMENT2(true, (byte) ((Integer.parseInt(regMt017[(Integer.parseInt(regST062[((ncanal - 1) * 3) + 1], 16) * 7) + 4], 16)) & 0xFF))));
                                        dato = dato / 1000;
                                    }
                                    //System.out.println("Dato Pulso: " + dato);
                                    try {
                                        int canal = 0;
                                        for (EtipoCanal ecanal : vtipocanal) {
                                            if (OrdenCanales[ncanal - 1].toUpperCase().equals(ecanal.getUnidad().toUpperCase())) {
                                                canal = Integer.parseInt(ecanal.getCanal());
                                                break;
                                            }
                                        }
                                        //System.out.println("Canal identificado: " + canal);
                                        if (canal != 0) {
                                            econske = buscarConskeOptimizado(serie, canal, vconske);
                                            if (econske != null) {
                                                if (firstChannel) {
                                                    //System.out.println("Llave fecha almacenada: " + sdf.format(new Date(fechaIntervalo.getTime())));
                                                    fechasLec.put(sdf.format(new Date(fechaIntervalo.getTime())), dato);
                                                    firstChannel = false;
                                                }
                                                //System.out.println("Canal: " + OrdenCanales[ncanal - 1] + ", Valor lectura: " + dato);
                                                lectura = new Electura(fechaIntervalo, serie, canal, trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), datopulsos, intervalo, OrdenCanales[ncanal - 1]);
                                                vlec.add(lectura);
                                            }
                                        }
                                    } catch (Exception e) {
                                        //System.out.println("Plan de Contingencia para Lecturas Elste A1800");
                                        try {
                                            if (rutaserver.length() > 0) {
                                                if (!rutaserver.toUpperCase().contains("SMB")) {
                                                    File file = new File(rutaserver + "lecturas\\" + archivoContingencia);
                                                    if (file != null) {
                                                        fr = new RandomAccessFile(file, "rw");
                                                        fr.seek(fr.length());
                                                        StringBuilder cadena = new StringBuilder();
                                                        cadena.append(fechacontingencia.format(new Date(fechaIntervalo.getTime()))).append(";");
                                                        cadena.append(serie.trim()).append(";");
                                                        cadena.append(("" + OrdenCanales[ncanal - 1]).trim()).append(";");
                                                        cadena.append(("" + dato).trim()).append(";");
                                                        cadena.append(("" + intervalo).trim()).append("\r");
                                                        fr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                                        fr.close();
                                                    }
                                                } else {
                                                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                                                    SmbFile file = new SmbFile(rutaserver + "lecturas\\" + archivoContingencia, auth);
                                                    if (file != null) {
                                                        sfr = new SmbRandomAccessFile(file, "rw");
                                                        sfr.seek(sfr.length());
                                                        StringBuilder cadena = new StringBuilder();
                                                        cadena.append((fechacontingencia.format(new Date(fechaIntervalo.getTime())) + ";"));
                                                        cadena.append((serie.trim() + ";"));
                                                        cadena.append((("" + OrdenCanales[ncanal - 1]).trim() + ";"));
                                                        cadena.append((("" + dato).trim() + ";"));
                                                        cadena.append((("" + intervalo).trim() + "\r"));
                                                        sfr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                                        sfr.close();
                                                    }
                                                }
                                            }
                                        } catch (Exception eex) {
                                            eex.printStackTrace();
                                        }
                                    }
                                    ncanal++;
                                }
                            }
                        } else if (possibleEvent) {
                            possibleEvent = false;
                            startDateStatusEvent = new Timestamp(fechaIntervalo.getTime());
                        }
                    }
                }
            }
            String ultimaFechaBloque = "20" + possibleDate.get(0) + "-" + (possibleDate.get(1) < 10 ? "0" + possibleDate.get(1) : possibleDate.get(1)) + "-" + (possibleDate.get(2) < 10 ? "0" + possibleDate.get(2) : possibleDate.get(2)) + " " + (possibleDate.get(3) < 10 ? "0" + possibleDate.get(3) : possibleDate.get(3)) + ":" + (possibleDate.get(4) < 10 ? "0" + possibleDate.get(4) : possibleDate.get(4));            
            escribir("Última Fecha capturada en Bloque: " + ultimaFechaBloque, fileLog);
            //revisamos los vacios generados por los eventos integridad de los datos desde la fecha de ultimalectura o 12 dias hacia atras e caso de la fecha sea null
            SimpleDateFormat sdfulec = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Timestamp ulec = uFecLec; //fecha de ultima lectura         
            Timestamp uact = new Timestamp(sdfulec.parse(ultimaFechaBloque).getTime());

            //quitamos los segundos para redondear por debajo y obtener una fecha equitativa al intervalo del medidor
            String fechaulec = sdfulec.format(new Date(ulec.getTime()));
            escribir("Fecha última lectura: " + fechaulec, fileLog);
            //String fecactlec = sdfulec.format(new Date(uact.getTime()));
            ulec = new Timestamp(sdfulec.parse(fechaulec).getTime());
            //uact = new Timestamp(sdfulec.parse(fecactlec).getTime());
            //redondeamos por debajo las fechas
            int min = ulec.getMinutes();
            int restaintervalo = 0;
            if (min % intervalo != 0) {
                min = min - (min % intervalo);
            } else {
                restaintervalo = (60000 * intervalo);
            }
            ulec.setMinutes(min);
            ulec = new Timestamp(ulec.getTime() - restaintervalo); //fecha de ultima lectura redondeada por debajo de 30 dias hacia atras
            //calculamos el numero de intervalos que hay entre la fecha de ultima lectura y la fecha actual menos 2 intervalos
            int numintervalos = (int) (((uact.getTime() - ulec.getTime()) / 60000) / 15);
            escribir("Número de Intervalos: " + numintervalos, fileLog);
            Timestamp fechaIntervaloRevision;
            Timestamp fechaIntervaloComp = null;
            String fechaIntervaloCompS;
            //revisamos intervalo por intervalo
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(ulec.getTime());
            for (int num = 0; num < numintervalos; num++) {
                if (num > 0) {
                    cal.add(Calendar.MINUTE, intervalo);
                }                
                fechaIntervaloRevision = new Timestamp(cal.getTimeInMillis());               
                //buscamos si el intervalo se encuentra dentro de un evento de corte
                fechaIntervaloComp = new Timestamp(fechaIntervaloRevision.getTime() + TimeUnit.MINUTES.toMillis(15));
                escribir("Fecha Intervalo Real de Comparación Medidor: " + fechaIntervaloComp, fileLog);
                fechaIntervaloCompS = sdf.format(new Date(fechaIntervaloRevision.getTime()));
                escribir("Fecha Intervalo de Revisión Simex: " + fechaIntervaloCompS, fileLog);
                //escribir(""+registers.get(fechaIntervaloRevisionS), fileLog);
                if (fechasLec.get(fechaIntervaloCompS) != null) {
                    continue;
                }
                if (buscarEventoIntervalo(serie, fechaIntervaloComp, "0001")) {
                    //buscamos unidades y en caso de no encontrar dato ponemos 0 en ese intervalo
                    escribir("Evento Encontrado", fileLog);
                    int ncanal = 1;
                    for (int l = 0; l < numCanales; l++) {
                        //buscamos la unidad
                        int canal = 0;
                        for (EtipoCanal ecanal : vtipocanal) {
                            if (OrdenCanales[ncanal - 1].toUpperCase().equals(ecanal.getUnidad().toUpperCase())) {
                                canal = Integer.parseInt(ecanal.getCanal());
                                break;
                            }
                        }
                        if (canal != 0) {
                            econske = buscarConskeOptimizado(serie, canal, vconske);
                            if (econske != null) {
                                ////System.out.println("Almacena cero: " + fechaintervalorevision);
                                escribir("Almacena cero --> Canal: " + canal + ", Fecha: " + fechaIntervaloRevision, fileLog);
                                lectura = new Electura(fechaIntervaloRevision, serie, canal, 0, 0, intervalo, OrdenCanales[ncanal - 1]);
                                vlec.add(lectura);
                            }
                        }
                        ncanal++;
                    }
                }
            }
            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Inicia carga Batchs", "Medidor: " + serie);
            escribir("Inicia procesamiento de base de datos", fileLog);
            actualizaLectura(vlec, fileLog, new Timestamp(uFecLec.getTime() - (long) 86400000));
            escribir("Finaliza procesamiento de base de datos", fileLog);
            //20-05-2021 se activa la actualizacion de fecha luego de almacenar
            if (uFecLec.before(uact)) {
                actualizaFechaLectura(serie, sdf2.format(new Date(uact.getTime() - TimeUnit.MINUTES.toMillis(intervalo))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void almacenaST023_Acum(String serie, String fechaactual, String tramatotaldata, int NBR_SUMMATIONS, int NBR_DEMANDS, int NBR_OCCUR, int REG_FUNC1_FLAG, String[] OrdenSumations, String[] vSumationSelect, String[] regMt017, File fileLog) {
        try {
            int num_fecha = 5;
            int aplicafecha = 0;
            if ((((byte) (REG_FUNC1_FLAG & 0xFF)) & 0x02) == 0x02) {
                aplicafecha = 1;
            }
            int separacion = ((6 * NBR_SUMMATIONS) + ((((num_fecha * NBR_OCCUR) * aplicafecha) + (6) + (5 * NBR_OCCUR)) * NBR_DEMANDS)) * 2;
            SimpleDateFormat sgf023 = new SimpleDateFormat("yyMMddHHmmss");
            String sumations;
            Timestamp fechaSumations = new Timestamp(sgf023.parse(fechaactual).getTime());
            String[] channels = {"01", "02", "03", "04"};
            double[][] cummValues = new double[5][4];
            for (int i = 0, nTarifa = 0; i < (tramatotaldata.length()/2) - 1; i += separacion, nTarifa++) {
                for (int j = 0; j < NBR_SUMMATIONS; j++) {
                    sumations = tramatotaldata.substring(i + (6 * j * 2), i + (6 * j * 2) + 12);                    
                    sumations = sumations.substring(10, 12) + sumations.substring(8, 10) + sumations.substring(6, 8) + sumations.substring(4, 6) + sumations.substring(2, 4);
                    if (!isIntegerHex(OrdenSumations[j])) {//es energia
                        if (!OrdenSumations[j].equals("99")) {
                            cummValues[nTarifa][j] = (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0;
                        }
                    }
                }
            }            
            almacenarAcumulados(cummValues, fechaSumations, channels, OrdenSumations, serie, fileLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static Object[] findBlockInitialDate(ArrayList<Integer> pDate, int int2WDateBelongs, int intervalo, boolean last, Timestamp lastInterval, SimpleDateFormat sdf) throws ParseException {
        Object[] out = new Object[2];
        out[1] = false;
        int hora = pDate.get(3);
        int min = pDate.get(4);
        long restaIntervalo = 0;
        long intervaloMillis = 60000L * intervalo;
        Timestamp fechaIntervalo;
        
        if (min % intervalo != 0) {
            min = min - (min % intervalo);
            restaIntervalo = (int2WDateBelongs * intervaloMillis);
            if (last) {//Es la última iteración                                        
                pDate.set(4, min);
                //System.out.println("Intervalo incompleto de bloque Final, por lo tanto no será almacenado");                
                out[1] = true;
            }
        } else {
            restaIntervalo = intervaloMillis + (int2WDateBelongs * intervaloMillis);
        }

        String fechabloque = "" + pDate.get(0) + "/" + pDate.get(1)
                + "/" + pDate.get(2) + " " + hora + ":"
                + min;
        fechaIntervalo = new Timestamp(sdf.parse(fechabloque).getTime() - restaIntervalo);
        if (lastInterval != null) {
            //System.out.println("Si fecha Intervalo actual calculada: " + fechaIntervalo + " es mayor a fecha del último intervalo: " + lastInterval);
            if (fechaIntervalo.after(lastInterval)) {
                out[0] = fechaIntervalo;
            } else {
                out[0] = new Timestamp(fechaIntervalo.getTime() + intervaloMillis);
            }
        } else {
            out[0] = fechaIntervalo;
        }
        
        return out;
    }

    private static boolean[] byte2BooleanArr(byte B) {
        int tempValue;
        boolean[] out = new boolean[8];
        for (int bit = 0; bit < 8; bit++) {
            tempValue = (B >> bit) & 0x01;
            out[bit] = tempValue == 1;
        }
        return out;
    }

    private static String statusChannel(int code) {
        switch (code) {
            case 1:
                return "Desbordamiento numérico";
            case 2:
                return "Intervalo parcial";
            case 3:
                return "Intervalo largo";
            case 4:
                return "Intervalo omitido";
            case 5:
                return "El intervalo contiene datos del modo de prueba";
            case 6:
                return "La configuración cambio durante este intervalo";
            case 7:
                return "Grabación del Perfil de Carga detenida";
            default:
                return "Desconocido";
        }
    }

    private static String commonStatus(int code) {
        switch (code) {
            case 1:
                return "Horario de verano vigente";
            case 2:
                return "Fallo de alimentación";
            case 4:
                return "Reloj adelantado";
            case 8:
                return "Reloj atrásado";
            default:
                return "Desconocido";
        }
    }

    public void AlmacenaPerfilCargaLandisRXRS4(String serie, String marca, Timestamp uFecLec, Timestamp fechaactual, int bytesXChannel, int numCanales, int intervalo, float kFactor, ArrayList<Object[]> profileElements, String[] loadProfileArray, File fileLog) {
        //Declaración de Variables primitivas y Objetos
        int flag, datoPulso, canal, channelCounter, mes, dia, hora, min, seg, miliseg, tamCanales;
        float minutes;
        double dato;
        boolean esCorte, esReconexion, primerIntervalo, searching;
        Timestamp fechaPrimerIntervalo, fechaIntervalo, fechaCorte, fechaReconexion;
        String unit, pulsoHex, data1, data2;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyMMddHHmmss");
        Calendar cal = Calendar.getInstance();
        Calendar calEvents = Calendar.getInstance();
        ERegistroEvento ereg;
        Electura lectura;
        Vector<Electura> vlec = new Vector<>();
        LinkedHashMap<String, Integer> fechasLec = new LinkedHashMap<>();
        ArrayList<EConstanteKE> vconske;
        try {
            tamCanales = bytesXChannel;
            //Validación de casos con formatos desconocidos
            if (tamCanales != 2) {
                escribir("Formato Perfil de carga desconocido", fileLog);
                return;
            }
            //Inicialización de Primitivas y Objetos
            cal.setTimeInMillis(uFecLec.getTime());
            mes = 0;
            dia = 0;
            channelCounter = 0;
            esCorte = false;
            esReconexion = false;
            primerIntervalo = true;
            searching = true;
            validateConn();
            vconske = obtenerConstantesKe(serie);
            fechaPrimerIntervalo = new Timestamp(cal.getTimeInMillis());
            fechaIntervalo = new Timestamp(cal.getTimeInMillis());
            fechaCorte = null;
            for (EConstanteKE econske : vconske) {
                for (Object[] data : profileElements) {
                    if ((int) data[0] == econske.getCanal()) {
                        data[2] = true;
                    }
                }
            }
            //Algoritmo de Procesamiento
            try {
                for (int i = 0; i < loadProfileArray.length - 1; i += tamCanales) {
                    flag = (Integer.parseInt(loadProfileArray[i + 1], 16) & 0xC0) & 0xFF;
                    data2 = "" + Integer.toHexString(Integer.parseInt(loadProfileArray[i + 1], 16) & 0x3F);
                    if (data2.length() == 1) {
                        data2 = "0" + data2;
                    }
                    data1 = "" + Integer.toHexString(Integer.parseInt(loadProfileArray[i], 16) & 0xFF);
                    if (data1.length() == 1) {
                        data1 = "0" + data1;
                    }
                    escribir("Data capturada: " + data2 + data1, fileLog);
                    if (flag == 128) { //Cambio de día
                        if (mes == Integer.parseInt(data2) && dia == Integer.parseInt(data1)) {//No es cambio de día si no evento
                            continue;
                        }
                        escribir("Cambio de día", fileLog);
                        mes = Integer.parseInt(data2);
                        dia = Integer.parseInt(data1);
                        escribir("Fecha (Mes/Día) : " + mes + "/" + dia, fileLog);
                        cal.set(Calendar.MONTH, mes - 1);
                        cal.set(Calendar.DATE, dia);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 15);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        searching = false;

                    } else if (flag == 192) { //Eventos de Corte y Reconexion
                        if (esCorte) { //Si no es el primer evento detectado es reconexion
                            esReconexion = true;
                        } else {
                            esCorte = true;
                        }
                        minutes = (float) (Integer.parseInt(data2 + data1, 16) / 10.0);
                        hora = (int) (minutes / 60);
                        min = (int) (minutes % 60);
                        seg = (int) (60 * (minutes - Math.floor(minutes)));
                        miliseg = (int) (1000 * ((60 * (minutes - Math.floor(minutes))) - (float) seg));
                        escribir("Hora: " + hora + ":" + min + ":" + seg, fileLog);
                        escribir("Fecha Cal Pre: " + cal.getTime(), fileLog);
                        calEvents.setTimeInMillis(cal.getTimeInMillis());
                        calEvents.set(Calendar.HOUR_OF_DAY, hora);
                        calEvents.set(Calendar.MINUTE, min);
                        calEvents.set(Calendar.SECOND, seg);
                        calEvents.set(Calendar.MILLISECOND, miliseg);
                        if (esCorte && !esReconexion) {
                            fechaCorte = new Timestamp(calEvents.getTimeInMillis());
                            escribir("Fecha Corte: " + fechaCorte, fileLog);
                        } else if (esCorte && esReconexion) {
                            fechaReconexion = new Timestamp(calEvents.getTimeInMillis());
                            escribir("Fecha Reconexión: " + fechaReconexion, fileLog);
                            //Actualiza evento
                            cal.set(Calendar.HOUR_OF_DAY, hora);
                            if (min % intervalo != 0 || (min % intervalo == 0 && (seg > 0 || miliseg > 0))) {
                                min = (min - (min % intervalo)) + intervalo;
                            }
                            if (min == 60) {
                                cal.add(Calendar.HOUR_OF_DAY, 1);
                                min = 0;
                            }
                            cal.set(Calendar.MINUTE, min);
                            escribir("Fecha Cal Post: " + cal.getTime(), fileLog);
                            ereg = new ERegistroEvento();
                            ereg.setVcserie(serie);
                            ereg.setVctipo("0001");
                            ereg.setVcfechacorte(fechaCorte);
                            ereg.setVcfechareconexion(fechaReconexion);
                            try {
                                actualizaEvento(ereg, connection);
                            } catch (Exception e) {
                                System.err.println("Fecha: " + new Date() );
                                e.printStackTrace();
                            } finally {
                                esCorte = false;
                                esReconexion = false;
                            }
                        }
                    } else { // Casos 64 y 0 para Lecturas  
                        if (!searching) {
                            // Dado a que no se cuenta con documentación solo se procesan formatos con tamaño de canal de 2 bytes  
                            escribir("" + channelCounter, fileLog);
                            escribir("Canal " + (int) profileElements.get(channelCounter)[0] + " asociado: " + (boolean) profileElements.get(channelCounter)[2], fileLog);
                            if ((boolean) profileElements.get(channelCounter)[2]) {
                                canal = (int) profileElements.get(channelCounter)[0];
                                unit = (String) profileElements.get(channelCounter)[1];
                                escribir("Canal: " + canal + " " + unit, fileLog);
                                if (primerIntervalo) {
                                    fechaPrimerIntervalo = new Timestamp(cal.getTimeInMillis());
                                    primerIntervalo = false;
                                }
                                fechaIntervalo = new Timestamp(cal.getTimeInMillis());
                                pulsoHex = data2 + data1;
                                datoPulso = Integer.parseInt(pulsoHex, 16) & 0x3FFF;//14 bits para Lectura                           
                                fechasLec.put(sdf.format(fechaIntervalo), datoPulso);
                                escribir("Valor Pulso Obtenido para el Intervalo " + fechaIntervalo + " : " + pulsoHex + " (16) -> " + datoPulso + " (10)", fileLog);
                                dato = (float) datoPulso * (float) kFactor / 1000.0;
                                lectura = new Electura(fechaIntervalo, serie, canal, dato, datoPulso, intervalo, unit);
                                vlec.add(lectura);
                            }
                            channelCounter = channelCounter < numCanales - 1 ? (channelCounter + 1) : 0;
                            if (channelCounter == 0) {
                                cal.add(Calendar.MINUTE, intervalo);
                            }
                        }
                    }
                }
            } catch ( Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
                if (channelCounter > 0  && vlec.size() >= channelCounter) {
                    for (int i = 0; i < channelCounter; i ++) {
                        vlec.remove(vlec.size() - 1);
                    }
                }
            }
            
            int numintervalos = (int) (((fechaIntervalo.getTime() - fechaPrimerIntervalo.getTime()) / 60000) / 15);
            escribir("Número de Intervalos: " + numintervalos, fileLog);
            Timestamp fechaIntervaloRevision;
            String fechaIntervaloRevisionS;
            Calendar calR = Calendar.getInstance();
            calR.setTimeInMillis(fechaPrimerIntervalo.getTime());
            //Revisamos intervalo por intervalo
            for (int num = 0; num < numintervalos; num++) {
                if ( num > 0) {
                    calR.add(Calendar.MINUTE, intervalo);
                }                
                fechaIntervaloRevision = new Timestamp(calR.getTimeInMillis());
                //buscamos si el intervalo se encuentra dentro de un evento de corte
                fechaIntervaloRevisionS = sdf.format(fechaIntervaloRevision);
                escribir("Fecha Intervalo de Revisión Simex: " + fechaIntervaloRevisionS, fileLog);
                //escribir(""+registers.get(fechaIntervaloRevisionS), fileLog);
                if (fechasLec.get(fechaIntervaloRevisionS) != null) {
                    continue;
                }
                if (buscarEventoIntervalo(serie, fechaIntervaloRevision, "0001")) {
                    //buscamos unidades y en caso de no encontrar dato ponemos 0 en ese intervalo
                    escribir("Evento Encontrado", fileLog);
                    for (int l = 0; l < numCanales; l++) {
                        if ((boolean) profileElements.get(l)[2]) {
                            canal = (int) profileElements.get(l)[0];
                            unit = (String) profileElements.get(l)[1];
                            escribir("Canal: " + canal + " " + unit, fileLog);
                            escribir("Almacena cero --> Canal: " + canal + ", Fecha: " + fechaIntervaloRevision, fileLog);
                            lectura = new Electura(fechaIntervaloRevision, serie, canal, 0, 0, intervalo, unit);
                            vlec.add(lectura);
                        }
                    }
                }
            }

            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Inicia carga Batchs", "Medidor: " + serie);
            escribir("Inicia procesamiento de base de datos", fileLog);
            actualizaLecturaDesfase(vlec, fileLog, new Timestamp(uFecLec.getTime() - (long) 86400000));
            escribir("Finaliza procesamiento de base de datos", fileLog);
            if (uFecLec.before(fechaIntervalo)) {
                actualizaFechaLectura(serie, sdf2.format(new Date(fechaIntervalo.getTime() - TimeUnit.MINUTES.toMillis(intervalo))));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public int COMPLEMENT2(boolean magnitud, byte b) {
        int c = 0;
        if (magnitud) {
            if ((b & 0x10) == 0x10) {
                b = (byte) (0xE0 | b);
            } else {
            }
            c = b;
        } else {
            c = b;
        }

        return c;

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

    public int complemento2Int(int i) {
        byte b;
        if (i < 0) {
            b = (byte) ((-1 * i) & 0xFF);
            b = (byte) (~b + 1);
        } else {
            b = (byte) (i & 0xFF);
        }
        return b;
    }

    public void almancenaEventosElster(String tramatotaldata, String serie) {

        try {
            SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            validateConn();
            ERegistroEvento ereg = null;
            boolean guardar = false;
            boolean reset = false;
            int desglose = 24;
            SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
            String archivoContingencia = "" + serie + "_" + (fechacontingencia.format(new Date().getTime())) + ".txt";
            RandomAccessFile fr;
            SmbRandomAccessFile sfr;
            String fechadesconexion = "";
            String fechaconexion = "";
            String fecharestet = "";
            for (int i = 0; i < tramatotaldata.length(); i += desglose) {
                if (Integer.parseInt(tramatotaldata.substring(i + 22, i + 24) + tramatotaldata.substring(i + 20, i + 22), 16) == 1) {//es corte
                    fechadesconexion = Integer.parseInt(tramatotaldata.substring(i, i + 2), 16) + "/" + Integer.parseInt(tramatotaldata.substring(i + 2, i + 4), 16) + "/" + Integer.parseInt(tramatotaldata.substring(i + 4, i + 6), 16) + " " + Integer.parseInt(tramatotaldata.substring(i + 6, i + 8), 16) + ":" + Integer.parseInt(tramatotaldata.substring(i + 8, i + 10), 16) + ":" + Integer.parseInt(tramatotaldata.substring(i + 10, i + 12), 16);
                } else if (Integer.parseInt(tramatotaldata.substring(i + 22, i + 24) + tramatotaldata.substring(i + 20, i + 22), 16) == 2) {//es conexion
                    fechaconexion = Integer.parseInt(tramatotaldata.substring(i, i + 2), 16) + "/" + Integer.parseInt(tramatotaldata.substring(i + 2, i + 4), 16) + "/" + Integer.parseInt(tramatotaldata.substring(i + 4, i + 6), 16) + " " + Integer.parseInt(tramatotaldata.substring(i + 6, i + 8), 16) + ":" + Integer.parseInt(tramatotaldata.substring(i + 8, i + 10), 16) + ":" + Integer.parseInt(tramatotaldata.substring(i + 10, i + 12), 16);
                } else if (Integer.parseInt(tramatotaldata.substring(i + 22, i + 24) + tramatotaldata.substring(i + 20, i + 22), 16) == 20) {//es reset                    
                    fecharestet = Integer.parseInt(tramatotaldata.substring(i, i + 2), 16) + "/" + Integer.parseInt(tramatotaldata.substring(i + 2, i + 4), 16) + "/" + Integer.parseInt(tramatotaldata.substring(i + 4, i + 6), 16) + " " + Integer.parseInt(tramatotaldata.substring(i + 6, i + 8), 16) + ":" + Integer.parseInt(tramatotaldata.substring(i + 8, i + 10), 16) + ":" + Integer.parseInt(tramatotaldata.substring(i + 10, i + 12), 16);
                }
                if (fechadesconexion.length() > 0 && fechaconexion.length() > 0) {
                    guardar = true;
                } else if (fecharestet.length() > 0) {
                    reset = true;
                }
                if (guardar) {
                    ereg = new ERegistroEvento();
                    ereg.setVcserie(serie);
                    ereg.setVctipo("0001");
                    ereg.setVcfechacorte(new Timestamp(df.parse(fechadesconexion).getTime()));
                    ereg.setVcfechareconexion(new Timestamp(df.parse(fechaconexion).getTime()));
                    try {
                        actualizaEvento(ereg, connection);
                    } catch (Exception e) {
                        //contigencia
                        try {
                            if (rutaserver.length() > 0) {
                                if (!rutaserver.toUpperCase().contains("SMB")) {
                                    File file = new File(rutaserver + "eventos\\" + archivoContingencia);
                                    if (file != null) {
                                        fr = new RandomAccessFile(file, "rw");
                                        fr.seek(fr.length());
                                        StringBuilder cadena = new StringBuilder();
                                        cadena.append(serie.trim()).append(";");
                                        cadena.append(fechacontingencia.format(new Date(df.parse(fechadesconexion).getTime()))).append(";");
                                        cadena.append(fechacontingencia.format(new Date(df.parse(fechaconexion).getTime()))).append("\r");
                                        fr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                        fr.close();
                                    }
                                } else {
                                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, parasmb.getUsuario(), parasmb.getPassword());
                                    SmbFile file = new SmbFile(rutaserver + "eventos\\" + archivoContingencia, auth);
                                    if (file != null) {
                                        sfr = new SmbRandomAccessFile(file, "rw");
                                        sfr.seek(sfr.length());
                                        StringBuilder cadena = new StringBuilder();
                                        cadena.append(serie.trim()).append(";");
                                        cadena.append(fechacontingencia.format(new Date(df.parse(fechadesconexion).getTime()))).append(";");
                                        cadena.append(fechacontingencia.format(new Date(df.parse(fechaconexion).getTime()))).append("\r");
                                        sfr.write(cadena.toString().getBytes(), 0, cadena.toString().getBytes().length);
                                        sfr.close();
                                    }
                                }

                            }

                        } catch (Exception eex) {
                            System.err.println("Fecha: " + new Date() );
                            eex.printStackTrace();
                        }
                    }

                    guardar = false;
                    fechaconexion = "";
                    fechadesconexion = "";
                }
                if (reset) {
                    ereg = new ERegistroEvento();
                    ereg.setVcserie(serie);
                    ereg.setVctipo("0002");
                    ereg.setVcfechacorte(new Timestamp(df.parse(fecharestet).getTime()));
                    ereg.setVcfechareconexion(new Timestamp(df.parse(fecharestet).getTime()));
                    actualizaEvento(ereg, connection);
                    reset = false;
                    fecharestet = "";
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }
    
    public void AlmacenaEventos_SEL735(List<ERegistroEvento> eventList) {
        try {
            validateConn();
            for (ERegistroEvento event : eventList) {
                actualizaEvento(event, connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void AlmacenaEventos_ION(List<ERegistroEvento> eventList) {
        try {
            validateConn();
            for (ERegistroEvento event: eventList) {
                actualizaEvento(event, connection);
            }                        
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public void almacenaST023(String serie, String marca, String fechaactual, String tramatotaldata, int NBR_SUMMATIONS, int NBR_DEMANDS, String[] OrdenSumations, String[] vSumationSelect, String[] regMt017, String[] OrdenDemands, String[] vDemandSelect, int NBR_OCCUR, int REG_FUNC1_FLAG) {
        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            SimpleDateFormat df2 = new SimpleDateFormat("yy/MM/dd HH:mm");
            validateConn();
            int num_fecha = 5;
            int aplicafecha = 0;
            if ((((byte) (REG_FUNC1_FLAG & 0xFF)) & 0x02) == 0x02) {
                aplicafecha = 1;
            }
            SimpleDateFormat sgf023 = new SimpleDateFormat("yyMMddHHmmss");
            int separacion = ((6 * NBR_SUMMATIONS) + ((((num_fecha * NBR_OCCUR) * aplicafecha) + (6) + (5 * NBR_OCCUR)) * NBR_DEMANDS)) * 2;
            int separacionsumations = (6 * NBR_SUMMATIONS) * 2;
            String sumations = "";
            String demandas = "";
            String fechademandas = "";
            String dmax = "";
            Timestamp fechaSumations = new Timestamp(sgf023.parse(fechaactual).getTime());
            Timestamp fechaDemandas;
            String dacum = "";
            int idxsumations = 0;
            int idxdemands = 0;
            String valor = "";
            for (int i = 0; i < tramatotaldata.length(); i += separacion) {
                for (int j = 0; j < NBR_SUMMATIONS; j++) {
                    sumations = tramatotaldata.substring(i + (6 * j * 2), i + (6 * j * 2) + 12);
                    sumations = sumations.substring(10, 12) + sumations.substring(8, 10) + sumations.substring(6, 8) + sumations.substring(4, 6) + sumations.substring(2, 4) + sumations.substring(0, 2);
////                    System.out.println("Dato " + j + " " + sumations);
                    if (!isIntegerHex(OrdenSumations[j])) {//es energia
                        valor = "" + obtenerCanalTasa(marca, OrdenSumations[j], connection);
////                        System.out.println("Valor " + valor);

                        if (!valor.equals("99")) {
                            if (OrdenSumations[j].toUpperCase().equals("KWHD") || OrdenSumations[j].toUpperCase().equals("KWHR")) {
                                actualizaRegistroTasas(serie, 0, (fechaSumations.getYear() + 1900), (fechaSumations.getMonth() + 1), (fechaSumations.getDate()), fechaSumations, (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenSumations[j], (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))), Calcular_Tasas(idxsumations + 1), "3", connection);
                            } else {
                                actualizaRegistroTasas(serie, 1, (fechaSumations.getYear() + 1900), (fechaSumations.getMonth() + 1), (fechaSumations.getDate()), fechaSumations, (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenSumations[j], (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))), Calcular_Tasas(idxsumations + 1), "3", connection);
                            }
                        }
                    } else {
                        //son datos de instrumentacion
                        valor = obtenerUnidadTasa(marca, "" + Integer.parseInt(OrdenSumations[j], 16), connection);
                        if (valor.length() > 0) {
                            actualizaRegistroTasas(serie, Integer.parseInt(OrdenSumations[j]), (fechaSumations.getYear() + 1900), (fechaSumations.getMonth() + 1), (fechaSumations.getDate()), fechaSumations, (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", valor, (Long.parseLong(sumations, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vSumationSelect[j], 16)) + 4], 16) & 0xFF)))))), Calcular_Tasas(idxsumations + 1), "3", connection);
                        }
                    }

                }
                idxsumations++;
                if (idxsumations >= 5) {
                    idxsumations = 0;
                }
                for (int j = 0; j < NBR_DEMANDS; j++) {
                    ////System.out.println("Demanda " + j + " " + sumations);
                    if (aplicafecha == 1) {
                        demandas = tramatotaldata.substring(i + separacionsumations + (32 * j), i + separacionsumations + (32 * j) + 32);
                        fechaDemandas = new Timestamp(df2.parse("" + Integer.parseInt(demandas.substring(0, 2), 16) + "/" + Integer.parseInt(demandas.substring(2, 4), 16) + "/" + Integer.parseInt(demandas.substring(4, 6), 16) + " " + Integer.parseInt(demandas.substring(6, 8), 16) + ":" + Integer.parseInt(demandas.substring(8, 10), 16)).getTime());
//                        System.err.println("Fecha: " + Integer.parseInt(demandas.substring(0, 2), 16) + "/" + Integer.parseInt(demandas.substring(2, 4), 16) + "/" + Integer.parseInt(demandas.substring(4, 6), 16) + " " + Integer.parseInt(demandas.substring(6, 8), 16) + ":" + Integer.parseInt(demandas.substring(8, 10), 16));
                        ////System.out.println("Acumulada " + demandas.substring(20, 22) + demandas.substring(18, 20) + demandas.substring(16, 18) + demandas.substring(14, 16) + demandas.substring(12, 14) + " " + OrdenDemands[j]);
                        if ((demandas.substring(0, 2) + "/" + demandas.substring(2, 4) + "/" + demandas.substring(4, 6)).equals("00/00/00")) {
                            fechaDemandas = fechaSumations;
                        }
                        dacum = demandas.substring(20, 22) + demandas.substring(18, 20) + demandas.substring(16, 18) + demandas.substring(14, 16) + demandas.substring(12, 14) + demandas.substring(10, 12);
                        ////System.out.println("Maxima " + demandas.substring(30, 32) + demandas.substring(28, 30) + demandas.substring(26, 28) + demandas.substring(24, 26) + demandas.substring(22, 24) + " " + OrdenDemands[j]);
                        dmax = demandas.substring(30, 32) + demandas.substring(28, 30) + demandas.substring(26, 28) + demandas.substring(24, 26) + demandas.substring(22, 24);
                    } else {
                        demandas = tramatotaldata.substring(i + separacionsumations + (22 * j), i + separacionsumations + (22 * j) + 22);
                        fechaDemandas = fechaSumations;
                        dacum = demandas.substring(10, 12) + demandas.substring(8, 10) + demandas.substring(6, 8) + demandas.substring(4, 6) + demandas.substring(2, 4) + demandas.substring(0, 2);
                        ////System.out.println("Maxima " + demandas.substring(30, 32) + demandas.substring(28, 30) + demandas.substring(26, 28) + demandas.substring(24, 26) + demandas.substring(22, 24) + " " + OrdenDemands[j]);
                        dmax = demandas.substring(20, 22) + demandas.substring(18, 20) + demandas.substring(16, 18) + demandas.substring(14, 16) + demandas.substring(12, 14);
                    }
                    if (!isIntegerHex(OrdenSumations[j])) {//es energia
                        valor = "" + obtenerCanalTasa(marca, OrdenDemands[j], connection);
////                        System.out.println("Valor Demanda" + demandas);
////                        System.out.println("Acum" + dacum);
////                        System.out.println("Max" + dmax);
////                        System.out.println("Valor Tasa Demanda" + valor);
////                        System.out.println("Valor Tasa " + Calcular_Tasas(idxdemands + 1));
////                        System.out.println("Año " + (fechaDemandas.getYear() + 1900));
                        if (!valor.equals("99")) {
                            if (OrdenDemands[j].toUpperCase().equals("KWD") || OrdenDemands[j].toUpperCase().equals("KWR")) {
                                actualizaRegistroTasas(serie, 82, (fechaDemandas.getYear() + 1900), (fechaDemandas.getMonth() + 1), (fechaDemandas.getDate()), fechaSumations, (Long.parseLong(dacum, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenDemands[j], (Long.parseLong(dacum, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, Calcular_Tasas(idxdemands + 1), "3", connection);
                                actualizaRegistroTasas(serie, 80, (fechaDemandas.getYear() + 1900), (fechaDemandas.getMonth() + 1), (fechaDemandas.getDate()), fechaSumations, (Long.parseLong(dmax, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenDemands[j], (Long.parseLong(dmax, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, Calcular_Tasas(idxdemands + 1), "3", connection);
                            } else {
                                actualizaRegistroTasas(serie, 83, (fechaDemandas.getYear() + 1900), (fechaDemandas.getMonth() + 1), (fechaDemandas.getDate()), fechaSumations, (Long.parseLong(dacum, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenDemands[j], (Long.parseLong(dacum, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, Calcular_Tasas(idxdemands + 1), "3", connection);
                                actualizaRegistroTasas(serie, 81, (fechaDemandas.getYear() + 1900), (fechaDemandas.getMonth() + 1), (fechaDemandas.getDate()), fechaSumations, (Long.parseLong(dmax, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenDemands[j], (Long.parseLong(dmax, 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vDemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, Calcular_Tasas(idxdemands + 1), "3", connection);
                            }
                        }
                    }
                }
                idxdemands++;
                if (idxdemands >= 5) {
                    idxdemands = 0;
                }
            }
            //connection.close();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }

    public double Redondear(double numero) {
        int cifras = (int) Math.pow(10, 10);
        return Math.rint(numero * cifras) / cifras;
    }

    public String Calcular_Tasas(int rate) { //metodo para los elster que calcula las tasas
        String value = "Total";
        if (rate == 2) {
            value = "A";
        } else if (rate == 3) {
            value = "B";
        } else if (rate == 4) {
            value = "C";
        } else if (rate == 5) {
            value = "D";
        }
        return value;
    }

    private String obtenerUnidadTasa(String marca, String tiporegistro, Connection conn) {
        String unidad = "";
        try {

            String sql = "SELECT * FROM tele01_tipo_registros WHERE vcmarca=? AND vctiporegistro=?";
            //PreparedStatement pstm = connection.prepareStatement(sql);
            PreparedStatement pstm = genPrepStatement(sql);
            pstm.setString(1, marca);
            pstm.setString(2, tiporegistro);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                unidad = rs.getString("vcnunidades");
            }
            rs.close();
            pstm.close();

        } catch (Exception e) {
        }
        return unidad;
    }

    private int obtenerCanalTasa(String marca, String unidad, Connection conn) {
        int canal = 99;
        try {
            String sqlcanal = "SELECT * FROM tele01_tipo_registros WHERE vcmarca=? AND  UPPER(vcnunidades)=?";
            //try (PreparedStatement pstmcanal = connection.prepareStatement(sqlcanal)) {
            try (PreparedStatement pstmcanal = genPrepStatement(sqlcanal)) {
                pstmcanal.setString(1, marca);
                pstmcanal.setString(2, unidad.toUpperCase());
                try (ResultSet rscanal = pstmcanal.executeQuery()) {
                    if (rscanal.next()) {
                        canal = rscanal.getInt("vccanal");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return canal;
    }

    public void almacenaST028(String serie, String marca, String fechaactual, String vdesgloseSt028, int NBR_PRESENT_DEMANDS, int NBR_PRESENT_VALUES, String[] OrdenPresentDemands, String[] vPresentdemandSelect, String[] regMt017, String[] OrdenPresentValues, String[] vPresentvaluesSelect, int[] OrdenFasesPresentValues) {

        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            validateConn();
            SimpleDateFormat sgf028 = new SimpleDateFormat("yyMMddHHmmss");
            SimpleDateFormat sgd028 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            int separacion = ((8 * NBR_PRESENT_DEMANDS) + (6 * NBR_PRESENT_VALUES)) * 2;
            int separacionPresentDemands = (8 * NBR_PRESENT_DEMANDS) * 2;
            String vpvalues = "";
            String vpdemands = "";
            Timestamp fechaPDemands;
            String valor = "";
            Timestamp fechaActual = new Timestamp(sgf028.parse(fechaactual).getTime());
            for (int j = 0; j < NBR_PRESENT_DEMANDS; j++) {
                vpdemands = vdesgloseSt028.substring((8 * j * 2), (8 * j * 2) + 16);
////                System.out.println(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8));
                fechaPDemands = new Timestamp(sgd028.parse("" + (fechaActual.getYear() + 1900) + "/" + (fechaActual.getMonth() + 1) + "/" + (fechaActual.getDate()) + " " + Integer.parseInt(vpdemands.substring(0, 2), 16) + ":" + Integer.parseInt(vpdemands.substring(2, 4), 16) + ":" + Integer.parseInt(vpdemands.substring(4, 6), 16)).getTime());
                if (!isIntegerHex(OrdenPresentDemands[j])) {//es energia
                    valor = "" + obtenerCanalTasa(marca, OrdenPresentDemands[j], connection);
                    if (!valor.equals("99")) {
                        if (OrdenPresentDemands[j].toUpperCase().equals("KWD") || OrdenPresentDemands[j].toUpperCase().equals("KWR")) {
                            actualizaRegistroTasas(serie, 84, (fechaPDemands.getYear() + 1900), (fechaPDemands.getMonth() + 1), (fechaPDemands.getDate()), fechaActual, (Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenPresentDemands[j], (Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", "3", connection);
                        } else {
                            actualizaRegistroTasas(serie, 85, (fechaPDemands.getYear() + 1900), (fechaPDemands.getMonth() + 1), (fechaPDemands.getDate()), fechaActual, (Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenPresentDemands[j], (Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", "3", connection);
                        }
                    }
                } else {//instrumentacion
                    valor = obtenerUnidadTasa(marca, "" + Integer.parseInt(OrdenPresentDemands[j], 16), connection);
                    if (valor.length() > 0) {
                        actualizaRegistroTasas(serie, Integer.parseInt(OrdenPresentDemands[j], 16), (fechaPDemands.getYear() + 1900), (fechaPDemands.getMonth() + 1), (fechaPDemands.getDate()), fechaActual, (Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", valor, (Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentdemandSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", "3", connection);
                    }
                }
////                System.out.println("Fecha " + Integer.parseInt(vpdemands.substring(0, 2), 16) + ":" + Integer.parseInt(vpdemands.substring(2, 4), 16) + ":" + Integer.parseInt(vpdemands.substring(4, 6), 16));
////                System.out.println("HExa demands " + vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8));
////                System.out.println("Valor demands " + Long.parseLong(vpdemands.substring(14, 16) + vpdemands.substring(12, 14) + vpdemands.substring(10, 12) + vpdemands.substring(8, 10) + vpdemands.substring(6, 8), 16));
            }
////            System.out.println("NBR_PRESENT_VALUES " + NBR_PRESENT_VALUES);
////            System.out.println("vPresentvaluesSelect " + vPresentvaluesSelect.length);
            for (int j = 0; j < NBR_PRESENT_VALUES; j++) {
////                System.out.println("Tipo values " + OrdenPresentValues[j]);
                vpvalues = vdesgloseSt028.substring(separacionPresentDemands + (6 * j * 2), separacionPresentDemands + (6 * j * 2) + 12);
                //instrumentacion
////                System.out.println("Hexa present " + vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2));
////                System.out.println("Valor present " + Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16));
                if (!isIntegerHex(OrdenPresentValues[j])) {//es energia
                    ////System.out.println("Tipo values " + OrdenPresentValues[j] + "Es energia");
                    valor = "" + obtenerCanalTasa(marca, OrdenPresentValues[j], connection);
////                        System.out.println("Valor " + valor);
                    if (!valor.equals("99")) {
                        if (OrdenPresentValues[j].toUpperCase().equals("KWD") || OrdenPresentValues[j].toUpperCase().equals("KWR")) {
                            actualizaRegistroTasas(serie, 86, (fechaActual.getYear() + 1900), (fechaActual.getMonth() + 1), (fechaActual.getDate()), fechaActual, (Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenPresentValues[j], (Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, "0", String.valueOf(OrdenFasesPresentValues[j]), connection);
                        } else {
                            actualizaRegistroTasas(serie, 87, (fechaActual.getYear() + 1900), (fechaActual.getMonth() + 1), (fechaActual.getDate()), fechaActual, (Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, valor, OrdenPresentValues[j], (Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4], 16) & 0xFF)))))) / 1000.0, "0", String.valueOf(OrdenFasesPresentValues[j]), connection);
                        }
                    }
                } else {
                    //son datos de instrumentacion
                    ////System.out.println("Tipo values " + OrdenPresentValues[j] + "Es Instrumentacion");
                    valor = obtenerUnidadTasa(marca, "" + Integer.parseInt(OrdenPresentValues[j], 16), connection);
                    if (valor.length() > 0) {
                        ////System.out.println("valor escala " + regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4]);
                        actualizaRegistroTasas(serie, Integer.parseInt(OrdenPresentValues[j], 16), (fechaActual.getYear() + 1900), (fechaActual.getMonth() + 1), (fechaActual.getDate()), fechaActual, (Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", valor, (Long.parseLong(vpvalues.substring(10, 12) + vpvalues.substring(8, 10) + vpvalues.substring(6, 8) + vpvalues.substring(4, 6) + vpvalues.substring(2, 4) + vpvalues.substring(0, 2), 16) * Math.pow(10.0, (COMPLEMENT2(true, ((byte) (Integer.parseInt(regMt017[(7 * Integer.parseInt(vPresentvaluesSelect[j], 16)) + 4], 16) & 0xFF)))))), "0", String.valueOf(OrdenFasesPresentValues[j]), connection);
                    }
                }

            }
            //connection.close();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }

    public Vector<String> buscarEquipos(boolean imprimeequipo) {
        Vector<String> equipos = new Vector<String>();
        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            validateConn();
            String sql = "SELECT * FROM tele01_equipos";
            //try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            try (PreparedStatement pstm = genPrepStatement(sql)) {
                ResultSet rs = pstm.executeQuery();
                while (rs.next()) {
                    if (imprimeequipo) {
                        equipos.add(rs.getString("vcmacequipo") + ":" + rs.getString("vcnomequipo"));
                    } else {
                        equipos.add(rs.getString("vcmacequipo"));
                    }
                    
                }   rs.close();
                //connection.close();
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

        return equipos;
    }

    public void actualizarequipo(String macequipo, String host) {
        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            validateConn();
            String sql = "select * from tele01_equipos where vcmacequipo=?";
            //try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            try (PreparedStatement pstm = genPrepStatement(sql)) {
                pstm.setString(1, macequipo);
                try (ResultSet rs = pstm.executeQuery()) {
                    if (!rs.next()) {
                        String sql2 = "insert into tele01_equipos (vcmacequipo,vcnomequipo) values(?,?)";
                        //try (PreparedStatement pstm2 = connection.prepareStatement(sql2)) {
                        try (PreparedStatement pstm2 = genPrepStatement(sql2)) {
                            pstm2.setString(1, macequipo);
                            pstm2.setString(2, host);
                            pstm2.execute();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }    

    
    public void almacenarAcumulados(double[][] values, Timestamp[][] fechas, String[] obis, String[] units, String serial, File fileLog) {
        ArrayList<ELecturaAcumulada> lecAcum_List = new ArrayList<>();
        for (int i = 0; i < values.length; i++) { // I -> Tarifa
            for (int j = 0; j < values[0].length; j++) { // J -> Tipo Energía
                lecAcum_List.add(new ELecturaAcumulada(fechas[i][j], serial, i, Long.parseLong(obis[j], 16), values[i][j], units[j]));
            }
        }
        try {
            actualizaAcumulados(lecAcum_List, fileLog);
        } catch (SQLException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            escribir(sw.toString(), fileLog);
        }
    }
    
    public void almacenarAcumulados(double[][] values, Timestamp fecha, String[] obis, String[] units, String serial, File fileLog) {
        ArrayList<ELecturaAcumulada> lecAcum_List = new ArrayList<>();
 
        for (int i = 0; i < values.length; i++) { // I -> Tarifa
            for (int j = 0; j < values[0].length; j++) {// J -> Tipo Energía
                lecAcum_List.add(new ELecturaAcumulada(fecha, serial, i, Long.parseLong(obis[j], 16), values[i][j], units[j]));
            }
        }
        try {
            actualizaAcumulados(lecAcum_List, fileLog);
        } catch (SQLException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            escribir(sw.toString(), fileLog);
        }
    }
    
    public void AlmacenaPerfilActaris(String seriemedidor, Vector<String> vPerfilCarga, String[] infoPerfil, String[] conskePerfil, String[] unidades, int intervalos, int periodoIntegracion, String strfechaactual, File fileLog, Timestamp ultimafec) {
        try {
//            escribir("INICIO Procesamiento de perfil de carga " + new Date());
            validateConn();
            Vector<EConstanteKE> constantes = buscarConstantesKe(seriemedidor);
            int constantesmatch = 0;
            for (int i = 0; i < infoPerfil.length; i++) {
                if (buscarConske(constantes, Integer.parseInt(infoPerfil[i], 16)) != null) {
                    constantesmatch++;
                }
            }
            //System.out.println("Número de constantes encontradas en configuración de perfil: " + constantesmatch);
//            escribir("Número de constantes encontradas en configuración de perfil: "+constantesmatch);

            Vector<EtipoCanal> ltiposcanal = buscarTipoCanalesActaris("2");
            Vector<Electura> vlec = new Vector<Electura>();

            SimpleDateFormat sgfactaris = new SimpleDateFormat("yyMMddHHmmss");
            SimpleDateFormat df3 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Timestamp red1 = null;
            Timestamp red2 = null;

            int ncanales = infoPerfil.length;

            Timestamp fechaactual = null;
            Timestamp fechatempmilis = null;
            //Timestamp ufeclec = findUltimafechaLec(seriemedidor);
            Timestamp ufeclec = ultimafec;
            Electura lec = null;
            Timestamp fechaintervalo = null;
            Timestamp fultimointervalo = null;
            EConstanteKE econske = null;

            Vector<String> dataPerfilCarga = vPerfilCarga;
            int pos = 0;
            int hora = 0;
            int min = 0;
            String fecha = Integer.parseInt((dataPerfilCarga.get(6) + dataPerfilCarga.get(7)), 16) + "/" + Integer.parseInt(dataPerfilCarga.get(8), 16) + "/" + Integer.parseInt(dataPerfilCarga.get(9), 16);
            ////System.out.println("Fecha inicio perfil de carga "+fecha);
            if (!dataPerfilCarga.get(11).equals("FF")) {
                //capturamos la hora y el minuto
                hora = Integer.parseInt(dataPerfilCarga.get(11), 16);
                min = Integer.parseInt(dataPerfilCarga.get(12), 16);
                if (Integer.parseInt(dataPerfilCarga.get(12), 16) % periodoIntegracion != 0) {
                    min = Integer.parseInt(dataPerfilCarga.get(12), 16) - (Integer.parseInt(dataPerfilCarga.get(12), 16) % periodoIntegracion);//                   
                }
                fecha = fecha + " " + hora + ":" + min;
            } else {
                if (periodoIntegracion == 60) {
                    fecha = fecha + " 01:00"; // el rango del día va de 1:00 am a 12:00 am (pasando por las 12 del medio día)
                } else {
                    fecha = fecha + " 00:" + periodoIntegracion; //00:00 es en realidad el último intervalo del día, se inicia desde las 00:pi am hasta 12:(60-pi) am
                }
            }
            boolean ingresa = false;
            int nintervalo = 0;
            String fechadia = null;
            String fechadiahoras = fecha;
            Timestamp fechadiaT = new Timestamp(df3.parse(fechadiahoras).getTime());
            Timestamp comparafecha = fechadiaT; //para comparar las cuatro fechas de cada registro
            fultimointervalo = fechadiaT;
            Timestamp fechaCero = null;
            //System.out.println("Procesa primer dia: " + fechadiaT);
//            escribir("Procesa primer dia: "+fechadiaT);
            int intervalodia = 0;
            String fechatemp = null;
            int indxlec = 0;
            int minutos = 0;
            boolean fechanula = false;
            boolean primerintervalo = true;
            //**** Modificacion lecturas incompletas del perfil
            //en caso de error guarda lo que tenga en el vector de lecturas
            try {
                for (int j = 0; j < intervalos; j++) {
                    int eltos = Integer.parseInt(dataPerfilCarga.get(pos + 1), 16);
                    int cuenta = 0;
                    pos = pos + 2;
                    boolean lecturavalida = true;
                    fechanula = true;
                    //Procesa fecha
                    while (cuenta < eltos - ncanales) {//elementos del registro menos el número de canales me dará el número de elementos adicionales (ceros o fechas)
                        if (dataPerfilCarga.get(pos).equals("00")) {
                            cuenta++;
                            pos = pos + 1;
//                            fechanula = true;
                        } else if (dataPerfilCarga.get(pos).equals("02") && dataPerfilCarga.get(pos + 1).equals("02")) {//cambio de dia o evento                            
                            fechanula = false;//dici
                            cuenta++;
                            if (!dataPerfilCarga.get(pos + 4).equals("FF"))//cambio de dia
                            {
                                fechadia = "" + Integer.parseInt(dataPerfilCarga.get(pos + 4) + dataPerfilCarga.get(pos + 5), 16);//ano
                                fechadia = fechadia + "/" + Integer.parseInt(dataPerfilCarga.get(pos + 6), 16);//mes
                                fechadia = fechadia + "/" + Integer.parseInt(dataPerfilCarga.get(pos + 7), 16);//dia
                                if (!dataPerfilCarga.get(pos + 9).equals("FF")) {// con hora
                                    //capturamos la hora y el minuto
                                    hora = Integer.parseInt(dataPerfilCarga.get(pos + 9), 16);
                                    if (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion != 0) {
                                        minutos = periodoIntegracion + (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) - (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion));//                   
                                    } else {
                                        minutos = Integer.parseInt(dataPerfilCarga.get(pos + 10), 16);
                                    }
                                    minutos = (minutos == 60 ? 0 : minutos);
                                    fechadiahoras = fechadia + " " + hora + ":" + minutos;
                                } else {//sin hora
                                    if (periodoIntegracion == 60) {
                                        fechadiahoras = fechadia + " 01:00"; // el rango del día va de 1:00 am a 12:00 am (pasando por las 12 del medio día)
                                    } else {
                                        fechadiahoras = fechadia + " 00:" + periodoIntegracion; //00:00 es en realidad el último intervalo del día, se inicia desde las 00:pI am hasta 12:(60-pI) am
                                    }
                                }
                                Timestamp fechadiaActual = new Timestamp(df3.parse(fechadiahoras).getTime());
                                if (fechadiaT.getYear() != fechadiaActual.getYear() || fechadiaT.getMonth() != fechadiaActual.getMonth() || fechadiaT.getDate() != fechadiaActual.getDate()) {
                                    fechadiaT = new Timestamp(df3.parse(fechadiahoras).getTime()); //cambio de día
                                    //System.out.println("Procesa dia: " + fechadiaT);
//                                    escribir("Procesa dia: "+fechadiaT);
                                    intervalodia = 0;
                                }

                                if (dataPerfilCarga.get(pos + 18).equals("40") || dataPerfilCarga.get(pos + 18).equals("80")) {//modificacion eventos dentro del perfil PRUEBA                                    
                                    //System.out.println("Evento tipo " + dataPerfilCarga.get(pos + 18) + " - En la fecha y hora: " + fechadiahoras);
//                                    escribir("Evento tipo "+dataPerfilCarga.get(pos + 18)+" - En la fecha y hora: "+fechadiahoras);
                                } else {
                                    //System.out.println("Sin evento");
                                }
                                comparafecha = (fechadiaActual.getTime() > comparafecha.getTime() ? fechadiaActual : comparafecha);
                            } else//evento
                            {
                                fechatemp = fechadia + " " + Integer.parseInt(dataPerfilCarga.get(pos + 9), 16);//hora
                                if (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion != 0) {
                                    minutos = periodoIntegracion + (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) - (Integer.parseInt(dataPerfilCarga.get(pos + 10), 16) % periodoIntegracion));//                   
                                } else {
                                    minutos = Integer.parseInt(dataPerfilCarga.get(pos + 10), 16);
                                }
                                if (minutos == 60) {
                                    minutos = 0;
                                    fechatemp = fechadia + " " + (Integer.parseInt(dataPerfilCarga.get(pos + 9), 16) + 1);//hora
                                }
                                // los minutos se desplazan hacia la derecha
                                fechatemp = fechatemp + ":" + minutos;
                                //System.out.println("Evento tipo " + dataPerfilCarga.get(pos + 18) + " - En la fecha y hora: " + fechatemp);
//                                escribir("Evento tipo "+dataPerfilCarga.get(pos + 18)+" - En la fecha y hora: "+fechatemp);
                                fechatempmilis = new Timestamp(df3.parse(fechatemp).getTime());
                                comparafecha = (fechatempmilis.getTime() > comparafecha.getTime() ? fechatempmilis : comparafecha);
                            }
                            pos += 19; //siguiente elemento después de procesar una fecha
                        } else {
                            fechanula = false;//dici
                            lecturavalida = false; //fecha no procesada
                        }
                    }
                    //Fecha del intervalo
                    if (fechanula) {
                        //fultimointervalo nunca es nulo inicialmente, ya que siempre llegará una fecha en el primer registro y fechanula es false
                        fechaintervalo = new Timestamp(fultimointervalo.getTime() + (60000L * (long) periodoIntegracion));
                    } else {
                        fechaintervalo = comparafecha;
                    }
                    //Nulos
                    red1 = fultimointervalo; //new Timestamp(df3.parse(fechaini).getTime());
                    red2 = fechaintervalo; //new Timestamp(df3.parse(fechafin).getTime());
                    //System.out.println("Revisa nulos entre: " + red1 + " y " + red2);
                    if (red1.getMinutes() % periodoIntegracion != 0) {
                        red1.setMinutes((red1.getMinutes() - (red1.getMinutes() % periodoIntegracion)));
                    }
                    if (red2.getMinutes() % periodoIntegracion != 0) {
                        red2.setMinutes((red2.getMinutes() - (red2.getMinutes() % periodoIntegracion)));
                    }
                    //System.out.println("Revisa nulos entre las fechas aproximadas: " + red1 + " y " + red2);
                    int aumento = 0;//Math.abs((redondeo(fechafin)-redondeo(fechaini))) /periodoIntegracion-1;
//                                aumento = (int) Math.abs((((red2.getTime() - red1.getTime()) / 60000) / periodoIntegracion)) - 1;
                    aumento = (int) (((red2.getTime() - red1.getTime()) / 60000) / periodoIntegracion) - 1;
                    if (aumento > 0) {
                        for (int i = 0; i < aumento; i++) {
//                                        fechaintervalo = new Timestamp(fechalec.getTime() + ((60000 * periodoIntegracion) * (intervalodia + i)));
                            fechaCero = new Timestamp(fultimointervalo.getTime() + (60000L * (long) periodoIntegracion) * (i + 1));
                            //System.out.println("Fecha Lectura nulo: " + fechaCero);
//                                escribir("Fecha Lectura nulo: "+fechaCero);
//                                        canalesmedidor = 0;
                            for (int k = 0; k < ncanales; k++) {
                                ingresa = false;
                                lec = new Electura(fechaCero, seriemedidor, Integer.parseInt(infoPerfil[k], 16),
                                        0, 0, periodoIntegracion, obtenerUnidadActaris(ltiposcanal, String.valueOf(Integer.parseInt(infoPerfil[k], 16))));
                                ingresa = true;
                                if (ingresa) {
                                    //almacenamos la lectura
                                    //validamos si la fecha es despues de la fecha de la ultima lectura                                                
                                    econske = buscarConske(constantes, Integer.parseInt(infoPerfil[k], 16));
                                    if (econske != null) {
                                        lec.setLec(trasnformarEnergia(lec.getLec(), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                        if (lec.vccanal.toUpperCase().contains("K")) {
                                            lec.setLec(lec.getLec() / 1000);
                                        }
                                        //System.out.println("Registro nulo en: " + lec.getFecha());
                                        vlec.add(lec);
                                    }
                                }
                            }
                        }
                        intervalodia = intervalodia + aumento;
                        nintervalo = nintervalo + aumento;
                        fultimointervalo = fechaCero;
                    }//Termina nulos  
                    // Procesa canales y almacena lectura
//                    canalesmedidor = 0;
                    for (int i = 0; i < ncanales; i++) {//
                        ingresa = false;
                        if (dataPerfilCarga.get(pos).equals("10")) {
                            lec = new Electura(fechaintervalo, seriemedidor, Integer.parseInt(infoPerfil[i], 16),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2), 16) * Double.parseDouble(conskePerfil[i])),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2), 16) * Double.parseDouble(conskePerfil[i])),
                                    periodoIntegracion, obtenerUnidadActaris(ltiposcanal, String.valueOf(Integer.parseInt(infoPerfil[i], 16))));
                            pos = pos + 3;
                            ingresa = true;
                        } else if (dataPerfilCarga.get(pos).equals("05")) {
                            lec = new Electura(fechaintervalo, seriemedidor, Integer.parseInt(infoPerfil[i], 16),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2) + dataPerfilCarga.get(pos + 3) + dataPerfilCarga.get(pos + 4), 16) * Double.parseDouble(conskePerfil[i])),
                                    (Integer.parseInt(dataPerfilCarga.get(pos + 1) + dataPerfilCarga.get(pos + 2) + dataPerfilCarga.get(pos + 3) + dataPerfilCarga.get(pos + 4), 16) * Double.parseDouble(conskePerfil[i])),
                                    periodoIntegracion, obtenerUnidadActaris(ltiposcanal, String.valueOf(Integer.parseInt(infoPerfil[i], 16))));//                       
                            pos = pos + 5;
                            ingresa = true;
                        }
                        if (lecturavalida) {
                            if (ingresa) {
                                //almacenamos la lectura
                                //validamos si la fecha es despues de la fecha de la ultima lectura                                
                                econske = buscarConske(constantes, Integer.parseInt(infoPerfil[i], 16));
                                if (econske != null) {
                                    lec.setLec(trasnformarEnergia(lec.getLec(), econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()));
                                    if (lec.vccanal.toUpperCase().contains("K")) {
                                        lec.setLec(lec.getLec() / 1000);
                                    }
                                    //*** modificacion para actualizar la fecha d eultima lectura correctamente      
                                    fechaactual = lec.getFecha();
                                    //Caso no observable
                                    if (!primerintervalo && fechaintervalo.getTime() == fultimointervalo.getTime()) {//fecha actual igual a la fecha pasada 
                                        indxlec = vlec.size() - constantesmatch; //obtiene la posición del intervalo pasado
                                        lec.setLec(vlec.get(indxlec).getLec() + lec.getLec()); //toma la lectura del intervalo pasado y lo suma al actual
                                        lec.setPulso(vlec.get(indxlec).getPulso() + lec.getLec()); //toma el pulso del intervalo pasado y lo suma al actual
                                        vlec.remove(indxlec);// elimina el registro del intervalo pasado ya que ya fue sumado
                                        //System.out.println("(Almacena) Elimina y suma intervalo en: " + lec.getFecha());
//                                        escribir("(Almacena) Elimina y suma intervalo en: "+ lec.getFecha());
                                        vlec.add(lec);//almacena la suma
                                    } else {
                                        //System.out.println("Registro en: " + lec.getFecha());
                                        vlec.add(lec);
                                    }
                                }
                            }
                            lec = null;
                        }
                    }
                    if (lecturavalida) {
                        nintervalo++;
                        intervalodia++;
                    }
                    fultimointervalo = fechaintervalo;
                    primerintervalo = false;
                }
                //System.out.println(nintervalo + " de " + intervalos + " procesados");
//                escribir(nintervalo+" de "+intervalos+" procesados");
            } catch (Exception e) {
                //Modificacion en caso de error en desbordamiento por perfil incompleto se guarda lo que este en el vector de perfil
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
                //System.out.println("ERROR de desborde de perfil de carga incompleto serial " + seriemedidor);
                lecturacorrecta = false;
//                escribir("Error de desborde de perfil de carga incompleto");
//                escribir("Inicia contingencia");
                //System.out.println("Inicia contingencia");
                //System.out.println("INICIO Almacenamiento de perfil de carga incompleto" + new Date());
                actualizaLecturaDesfase(vlec, fileLog, new Timestamp(ultimafec.getTime() - (long) 86400000));
                lecturacorrecta = isLecturacorrecta();
                //System.out.println("FIN Almacenamiento de perfil de carga incompleto" + new Date());
//                escribir("INICIO Almacenamiento de perfil de carga incompleto - nulos " + new Date());
//                cp.actualizaLectura(vlecnulos, connection);
//                lecturacorrecta = cp.isLecturacorrecta();
//                escribir("FIN Almacenamiento de perfil de carga incompleto - nulos " + new Date());
                if (fechaactual != null) {
                    if (fechaactual.after(ufeclec)) {
                        //System.out.println("Actualiza fecha de ultima lectura con perfil incompleto" + sgfactaris.format(new Date(fechaactual.getTime())));
                        actualizaFechaLectura(seriemedidor, sgfactaris.format(new Date(fechaactual.getTime())));
                    }
                }
//                escribir("Prepara reinicio de contingencia por perfil incompleto");
                //System.out.println("Prepara reinicio de contingencia por perfil incompleto");
//                reiniciaComuniacion();
            }//catch
            //System.out.println("FIN Procesamiento de perfil de carga " + new Date());
            //System.out.println("INICIO Almacenamiento de perfil de carga " + new Date());
            actualizaLecturaDesfase(vlec, fileLog, new Timestamp(ultimafec.getTime() - (long) 86400000));
            //System.out.println("FIN Almacenamiento de perfil de carga " + new Date());
//            escribir("INICIO Almacenamiento de perfil de carga nulos " + new Date());
//            cp.actualizaLectura(vlecnulos, connection);
//            escribir("FIN Almacenamiento de perfil de carga nulos " + new Date());
            //*** cambio para actualizar correctamente la fecha de ultima lectura
            //System.out.println("Fecha de ultima lectura " + ufeclec);
            //System.out.println("Fecha de ultima lectura del perfil de carga " + fechaactual);
            if (fechaactual != null) {
                if (fechaactual.after(ufeclec)) {
                    //System.out.println("Actualiza fecha de ultima lectura " + sgfactaris.format(new Date(fechaactual.getTime())));
                    actualizaFechaLectura(seriemedidor, sgfactaris.format(new Date(fechaactual.getTime())));
                }

            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }
    //cierrra new

    public String obtenerUnidadActaris(String canal, Connection conn) throws Exception {
        if (conn == null) {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            conn = conmngr.getConnection();
            validateConn();
        }
        String unidad = "";
        String sql = "SELECT * FROM tele01_tipocanales WHERE vccanal=? AND vcmarca=2";
        //try (PreparedStatement pstm = connection.prepareStatement(sql)) {
        try (PreparedStatement pstm = genPrepStatement(sql)) {
            pstm.setString(1, canal);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    unidad = rs.getString("vcconversion");
                }
            }
        }
        return unidad;
    }

    public String obtenerDescripcionCanalActaris(String canal, Connection conn) throws Exception {
        if (conn == null) {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            conn = conmngr.getConnection();
            validateConn();
        }
        String descripcion = "";
        String sql = "SELECT * FROM tele01_tipocanales WHERE vccanal=? AND vcmarca=2";
        //try (PreparedStatement pstm = connection.prepareStatement(sql)) {
        try (PreparedStatement pstm = genPrepStatement(sql)) {
            pstm.setString(1, canal);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    descripcion = rs.getString("vcdescanal");
                }
            }
        }
        return descripcion;
    }

    public void AlmacenaEventosActaris(String seriemedidor, Vector<String> vEventos, int nEventos) {
        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            validateConn();
            SimpleDateFormat sgdeve = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            int pos = 0;
            long duracion = 0;
            double ke = 0;
            String fechaEvento;
            Timestamp fechaini = null;
            Timestamp fechafin = null;
            ERegistroEvento reg = null;
            for (int i = 0; i < nEventos; i++) {
                duracion = Long.parseLong((vEventos.get(pos + 3) + vEventos.get(pos + 4) + vEventos.get(pos + 5) + vEventos.get(pos + 6)), 16);//duracion del evento
                ke = Math.pow(10, ActarisComplemento2(((byte) (Integer.parseInt(vEventos.get(pos + 10), 16) & 0xFF))));
                fechaEvento = Integer.parseInt(vEventos.get(pos + 18) + vEventos.get(pos + 19), 16) + "/"
                        + "" + Integer.parseInt(vEventos.get(pos + 20), 16) + "/"
                        + "" + Integer.parseInt(vEventos.get(pos + 21), 16) + " "
                        + "" + Integer.parseInt(vEventos.get(pos + 23), 16) + ":"
                        + "" + Integer.parseInt(vEventos.get(pos + 24), 16) + ":"
                        + "" + Integer.parseInt(vEventos.get(pos + 25), 16);
                fechaini = new Timestamp(sgdeve.parse(fechaEvento).getTime());
                fechafin = new Timestamp(sgdeve.parse(fechaEvento).getTime() + ((long) (duracion * ke) * 1000));

////                System.out.println("*****************");
////                System.out.println("Evento " + (i + 1));
////                System.out.println("*****************");
////                System.out.println("duracion " + duracion);
////                System.out.println("ke " + ke);
////                System.out.println("fechaini " + fechaini);
////                System.out.println("fechaifin " + fechafin);
////                System.out.println("*****************");
////                System.out.println("\n");
                reg = new ERegistroEvento();
                reg.setVcserie(seriemedidor);
                reg.setVctipo("0001");
                reg.setVcfechacorte(fechaini);
                reg.setVcfechareconexion(fechafin);
                try {
                    actualizaEvento(reg, connection);
                } catch (Exception e) {
                    lecturacorrecta = false;
                    System.err.println("Fecha: " + new Date() );
                    e.printStackTrace();
                }

                pos = pos + 30;
            }
            //connection.close();
        } catch (Exception e) {
            lecturacorrecta = false;
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }
    
    public EAARQ ObtenerValoresAARQ(String marca) {
        EAARQ aarq = null;
        try {
//            ConnectionManager conmngr = new ConnectionManager(url, user, pass, dblinksimex, null);
//            Connection conn = conmngr.getConnection();
            validateConn();
            String sql = "SELECT * FROM tele01_aarq WHERE vcmarca=?";
            //try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            try (PreparedStatement pstm = genPrepStatement(sql)) {
                pstm.setString(1, marca);
                try (ResultSet rs = pstm.executeQuery()) {
                    if (rs.next()) {
                        aarq = new EAARQ();
                        aarq.setPROTOCOL_VERSION(rs.getString("PROTOCOL_VERSION"));
                        aarq.setCALLED_AP_TITLE(rs.getString("CALLED_AP_TITLE"));
                        aarq.setCALLED_AE_QUANTIFIER(rs.getString("CALLED_AE_QUANTIFIER"));
                        aarq.setCALLED_AP_INVOCATION_ID(rs.getString("CALLED_AP_INVOCATION_ID"));
                        aarq.setCALLED_AE_INVOCATION_ID(rs.getString("CALLED_AE_INVOCATION_ID"));
                        aarq.setCALLING_AP_TITLE(rs.getString("CALLING_AP_TITLE"));
                        aarq.setCALLING_AE_QUANTIFIER(rs.getString("CALLING_AE_QUANTIFIER"));
                        aarq.setCALLING_AP_INVOCATION_ID(rs.getString("CALLING_AP_INVOCATION_ID"));
                        aarq.setCALLING_AE_INVOCATION_ID(rs.getString("CALLING_AE_INVOCATION_ID"));
                        aarq.setSENDER_ACSE_REQUIREMENTS(rs.getString("SENDER_ACSE_REQUIREMENTS"));
                        aarq.setMECHANISM_NAME(rs.getString("MECHANISM_NAME"));
                        aarq.setCALLING_AUTHENTICATION_VALUE(rs.getString("CALLING_AUTHENTICATION_VALUE"));
                        aarq.setIMPLEMENTATION_INFORMATION(rs.getString("IMPLEMENTATION_INFORMATION"));
                        aarq.setUSER_INFORMATION(rs.getString("USER_INFORMATION"));
                        aarq.setVCMARCA(rs.getString("VCMARCA"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return aarq;
    }

    public Timestamp findUltimafechaLec(String nSerie) throws Exception {
        Timestamp fecha = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            validateConn();
            String sql = " SELECT dufec_lec FROM tele01_conf_medidor WHERE vcserie='" + nSerie + "'";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            if (rs.next()) {
                fecha = rs.getTimestamp("dufec_lec");
            }
        } catch (Exception e) {
            escribirLog(Equipo, new Timestamp(new Date().getTime()), nSerie, "ConsultaLastRead_ServTelesimex", "Error consultando last read");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }
        return fecha;
    }

    private boolean buscarEventoIntervalo(String serie, Timestamp fechaintervalo, String tipocanal) {
        boolean encontrado = false;
        try {

            SimpleDateFormat fechaevento = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sql = "SELECT * FROM tele01_eventos WHERE vcserie='" + serie + "' AND vcevento='" + tipocanal + "' AND (to_timestamp('" + fechaevento.format(new Date(fechaintervalo.getTime())) + "','yyyy-mm-dd HH24:mi:ss') BETWEEN dfechacorte AND dfechareconexion)   ORDER BY dfechacorte";
            //try (PreparedStatement pstm = connection.prepareStatement(sql); ResultSet rs = pstm.executeQuery()) {
            try (PreparedStatement pstm = genPrepStatement(sql); ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    encontrado = true;
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } 

        return encontrado;
    }

    private Parametros ObtenerDireccionServer(Connection connection) {
        Parametros ruta = null;
        try {
            String sql = "SELECT * FROM tele01_smb";
            //try (PreparedStatement pstm = connection.prepareStatement(sql); ResultSet rs = pstm.executeQuery()) {                
            try (PreparedStatement pstm = genPrepStatement(sql); ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    ruta = new Parametros(rs.getString("vcparam"), rs.getString("vcdescripcion"), rs.getString("vchost"), rs.getString("vcuser"), rs.getString("vcpassword"), rs.getString("vcsmb"));
                    //ruta = rs.getString("vcnomparam") == null ? "" : rs.getString("vcnomparam");
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
        return ruta;
    }

    public Vector<EConstanteKE> buscarConstantesKe(String serial) {
        Vector<EConstanteKE> constantes = new Vector<EConstanteKE>();
        validateConn();
        try {
            String sql = "SELECT * FROM tele01_constante WHERE vcserie='" + serial + "'";
            //try (PreparedStatement pstm = connection.prepareStatement(sql); ResultSet rs = pstm.executeQuery()) {
            try (PreparedStatement pstm = genPrepStatement(sql); ResultSet rs = pstm.executeQuery()) {
                EConstanteKE conske = null;
                while (rs.next()) {
                    conske = new EConstanteKE();
                    conske.setCanal(rs.getInt("ncanal"));
                    conske.setSeriemedidor(serial);
                    conske.setPesopulso(rs.getDouble("nconstanteke"));
                    conske.setDivisor(rs.getDouble("ndivisor"));
                    conske.setMultiplo(rs.getDouble("nmultiplo"));
                    conske.setConsInterna((rs.getInt("lkeinterna") == 1));
                    constantes.add(conske);
                }
            }
        } catch (Exception e) {
        }

        return constantes;
    }

    public Vector<EConstanteKE> buscarConstantesKeLong(String serial) {
        Vector<EConstanteKE> constantes = new Vector<EConstanteKE>();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            validateConn();
            String sql = "SELECT * FROM tele01_constante WHERE vcserie='" + serial + "'";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            EConstanteKE conske = null;
            rs = pstm.executeQuery();
            while (rs.next()) {
                conske = new EConstanteKE();
                conske.setCanal2(rs.getLong("ncanal"));
                conske.setSeriemedidor(serial);
                conske.setPesopulso(rs.getDouble("nconstanteke"));
                conske.setDivisor(rs.getDouble("ndivisor"));
                conske.setMultiplo(rs.getDouble("nmultiplo"));
                conske.setConsInterna((rs.getInt("lkeinterna") == 1));
                constantes.add(conske);
            }
        } catch (Exception e) {
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }
        return constantes;
    }

    public EConstanteKE buscarConske(Vector<EConstanteKE> constantes, int canal) {
        EConstanteKE res = null;
        for (EConstanteKE buscar : constantes) {
            if (buscar.getCanal() == canal) {
                res = buscar;
                break;
            }
        }
        return res;
    }

    public EConstanteKE buscarConskeLong(Vector<EConstanteKE> constantes, long canal) {
        EConstanteKE res = null;
        for (EConstanteKE buscar : constantes) {
            if (buscar.getCanal2() == canal) {
                res = buscar;
                break;
            }
        }
        return res;
    }

    public Vector<EtipoCanal> buscarTipoCanalesActaris(String vcmarca) {
        Vector<EtipoCanal> tcanales = new Vector<EtipoCanal>();
        try {
            String sql = "SELECT * FROM tele01_tipocanales WHERE vcmarca='" + vcmarca + "'";
            //try (PreparedStatement pstm = connection.prepareStatement(sql); ResultSet rs = pstm.executeQuery()) {
            try (PreparedStatement pstm = genPrepStatement(sql); ResultSet rs = pstm.executeQuery()) {
                EtipoCanal canal = null;
                while (rs.next()) {
                    canal = new EtipoCanal();
                    canal.setCanal(rs.getString("vccanal"));
                    canal.setMarca(rs.getString("vcmarca"));
                    canal.setDescripcion(rs.getString("vcdescanal"));
                    canal.setUnidad(rs.getString("vcconversion"));
                    tcanales.add(canal);
                }
            }
        } catch (Exception e) {
        }
        return tcanales;

    }

    private String obtenerUnidadActaris(Vector<EtipoCanal> ltiposcanal, String canal) {
        String res = null;
        for (EtipoCanal buscar : ltiposcanal) {
            if (buscar.getCanal().equals(canal)) {
                res = buscar.getUnidad();
                break;
            }
        }
        return res;
    }

    public void escribirLog(String usuario, Timestamp fecha, String serie, String opcion, String formato) {
        PreparedStatement pstm = null;
        try {
            String sql = "INSERT INTO tele01_logs (DFECHA,VCSERIE,VCOPCION,VCFORMATO,VCCODUSUARIO) VALUES (?,?,?,?,?)";
            validateConn();
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setTimestamp(1, fecha);
            pstm.setString(2, serie);
            pstm.setString(3, opcion);
            pstm.setString(4, formato);
            pstm.setString(5, usuario);
            pstm.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();

        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception ex) {
            }
            pstm = null;
        }
    }

    public void AlmacenaPerfilSentinel(String serie, String marca, Timestamp uFecLec, ArrayList<String> desglosePerfil, int intervalos, int intervalosUltimoBloque, int blocksExp, File file) {
        try {
            Connection conn = getConn();
            Electura lectura = null;
            Vector<Electura> vlec = new Vector<Electura>();
            LinkedHashMap<String, Double> fechasLec = new LinkedHashMap<>();
            Vector<EConstanteKE> constantes = buscarConstantesKe(serie);
            ArrayList<EtipoCanal> vtipocanal = obtenerTipoCanales(marca);
            EConstanteKE econske = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yy/MM/dd HH:mm");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyMMddHHmmss");
            SimpleDateFormat sdf_First = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Timestamp firstInterval = uFecLec;
            Timestamp actualizafec = null;
            Timestamp fechaIntervaloRevision = null;
            Timestamp fechaIntervalo = null;
            Timestamp ut = null;
            String strfechabloque = "";
            int bloque = 0;
            double dato = 0;
            int datopulso = 0;
            escribir("Número de Bloques: " + desglosePerfil.size(), file);
            for (String data : desglosePerfil) {
                escribir("Block " + bloque + " : " + data, file);
                String strdate = data.substring(6, 8) + data.substring(4, 6) + data.substring(2, 4) + data.substring(0, 2);
                long d = Long.parseLong(strdate, 16);
                if (d >= 2147483648L) {
                    d = complemento2Int((int) d);
                    d = d * -1;
                }
                long intdate = (d * 1000) + sdf2.parse("000101000000").getTime();
                strfechabloque = sdf1.format(new Date(new Timestamp(intdate).getTime()));
                actualizafec = new Timestamp(intdate - (60000 * intervalos));
                escribir("Fecha del bloque " + bloque + " => " + strfechabloque, file);
                int fintrama = data.length() - 12;
                int n = 0;
                int m = 129;
                boolean valida = false;
                for (int i = fintrama; i >= 40; i = i - 12) {
                    valida = false;
                    if ((bloque + 1) == blocksExp) {
                        if (m - 1 <= intervalosUltimoBloque) {
                            n++;
                            valida = true;
                        }
                    } else {
                        n++;
                        valida = true;
                    }
                    if (valida) {
                        fechaIntervalo = new Timestamp(intdate - (60000 * intervalos * (n)));
                        //System.out.println("Fecha intervalo: " + sdf.format(new Date(fechaIntervalo.getTime())));
                        escribir("Fecha intervalo " + sdf.format(new Date(fechaIntervalo.getTime())), file);
                        if (bloque == 0 && (i - 12) < 40) {//Final del Primer Bloque o bloque 0                                                    
                            firstInterval = fechaIntervalo;
                        }
                        datopulso = Integer.parseInt(data.substring(i + 6, i + 8) + data.substring(i + 4, i + 6), 16);
                        dato = ((double) datopulso) / 1000.0;
                        escribir("Dato canal 1 kWhD " + data.substring(i + 6, i + 8) + data.substring(i + 4, i + 6), file);
                        try {
                            int canal = 0;
                            for (EtipoCanal tc : vtipocanal) {
                                if (tc.getUnidad().toUpperCase().equals("kWhD".toUpperCase())) {
                                    canal = Integer.parseInt(tc.getCanal());
                                    break;
                                }
                            }
                            if (canal != 0) {
                                econske = buscarConske(constantes, canal);
                                if (econske != null) {
                                    fechasLec.put(sdf.format(new Date(fechaIntervalo.getTime())), dato);
                                    escribir("Dato Lec 1 kWhD " + trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), file);
                                    lectura = new Electura(fechaIntervalo, serie, canal, trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), datopulso, intervalos, "kWhD");
                                    if (ut == null) {
                                        ut = fechaIntervalo;
                                    } else {
                                        if (fechaIntervalo.after(ut)) {
                                            ut = fechaIntervalo;
                                        }
                                    }
                                    vlec.add(lectura);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Fecha: " + new Date() );
                            e.printStackTrace();
                        }
                        //canal 2
                        datopulso = Integer.parseInt(data.substring(i + 10, i + 12) + data.substring(i + 8, i + 10), 16);
                        dato = ((double) datopulso) / 1000.0;
                        escribir("Dato canal 2 kVarhD " + data.substring(i + 10, i + 12) + data.substring(i + 8, i + 10), file);
                        try {
                            int canal = 0;
                            for (EtipoCanal tc : vtipocanal) {
                                if (tc.getUnidad().toUpperCase().equals("kVarhD".toUpperCase())) {
                                    canal = Integer.parseInt(tc.getCanal());
                                    break;
                                }
                            }
                            if (canal != 0) {
                                econske = buscarConske(constantes, canal);
                                if (econske != null) {
                                    escribir("Dato Lec 2 kWhD " + trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), file);
                                    lectura = new Electura(fechaIntervalo, serie, canal, trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), datopulso, intervalos, "kVarhD");
                                    if (ut == null) {
                                        ut = fechaIntervalo;
                                    } else {
                                        if (fechaIntervalo.after(ut)) {
                                            ut = fechaIntervalo;
                                        }
                                    }
                                    vlec.add(lectura);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Fecha: " + new Date() );
                            e.printStackTrace();
                        }
                    }
                    m--;
                }
                bloque++;
            }
            int numintervalos = (int) (((actualizafec.getTime() - firstInterval.getTime()) / 60000) / 15);
            escribir("Número de Intervalos: " + numintervalos, file);
            String fechaIntervaloRevisionS;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(firstInterval.getTime());
            for (int num = 0; num < numintervalos; num++) {
                if ( num > 0) {
                    cal.add(Calendar.MINUTE, intervalos);
                }                
                fechaIntervaloRevision = new Timestamp(cal.getTimeInMillis());
                //buscamos si el intervalo se encuentra dentro de un evento de corte              
                escribir("Fecha Intervalo de Revisión Simex: " + fechaIntervaloRevision, file);
                fechaIntervaloRevisionS = sdf.format(new Date(fechaIntervaloRevision.getTime()));
                if (fechasLec.get(fechaIntervaloRevisionS) == null) {
                    //buscamos unidades y en caso de no encontrar dato ponemos 0 en ese intervalo
                    escribir("Registro No Encontrado", file);
                    try {
                        int canal = 0;
                        for (EtipoCanal tc : vtipocanal) {
                            if (tc.getUnidad().toUpperCase().equals("kWhD".toUpperCase())) {
                                canal = Integer.parseInt(tc.getCanal());
                                break;
                            }
                        }
                        if (canal != 0) {
                            econske = buscarConske(constantes, canal);
                            if (econske != null) {
                                escribir("Almacena cero --> Canal: " + canal + ", Fecha: " + fechaIntervaloRevision, file);
                                lectura = new Electura(fechaIntervaloRevision, serie, canal, 0, 0, intervalos, "kWhD");
                                vlec.add(lectura);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }

                    try {
                        int canal = 0;
                        for (EtipoCanal tc : vtipocanal) {
                            if (tc.getUnidad().toUpperCase().equals("kVarhD".toUpperCase())) {
                                canal = Integer.parseInt(tc.getCanal());
                                break;
                            }
                        }
                        if (canal != 0) {
                            econske = buscarConske(constantes, canal);
                            if (econske != null) {
                                escribir("Almacena cero --> Canal: " + canal + ", Fecha: " + fechaIntervaloRevision, file);
                                lectura = new Electura(fechaIntervaloRevision, serie, canal, 0, 0, intervalos, "kVarhD");
                                vlec.add(lectura);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                }
            }
            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Inicia carga Batchs", "Medidor: " + serie);
            escribir("Inicia procesamiento de base de datos", file);
            actualizaLectura(vlec, file, new Timestamp(uFecLec.getTime() - (long) 86400000));
            escribir("Finaliza procesamiento de base de datos", file);
            //20-05-2021 se activa la actualizacion de fecha luego de almacenar
            if (uFecLec.before(actualizafec)) {
                actualizaFechaLectura(serie, sdf2.format(new Date(actualizafec.getTime() - TimeUnit.MINUTES.toMillis(intervalos))));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

    public void almancenaEventosSentinel(String tramatotaldata, String serie) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            //System.out.println("Almacena Eventos");
            validateConn();
            ERegistroEvento ereg = null;
            boolean guardar = false;
            boolean reset = false;
            int desglose = 36;
            SimpleDateFormat fechacontingencia = new SimpleDateFormat("yyyyMMddHHmmss");
            String archivoContingencia = "" + serie + "_" + (fechacontingencia.format(new Date().getTime())) + ".txt";
            RandomAccessFile fr;
            SmbRandomAccessFile sfr;
            String fechadesconexion = "";
            String fechaconexion = "";
            String fecharestet = "";
            for (int i = 0; i < tramatotaldata.length() - desglose; i += desglose) {
                ////System.out.println("Dato => " + tramatotaldata.substring(i + 28, i + 30) + tramatotaldata.substring(i + 26, i + 28) + tramatotaldata.substring(i + 24, i + 26) + tramatotaldata.substring(i + 22, i + 24));
                if (Long.parseLong(tramatotaldata.substring(i + 28, i + 30) + tramatotaldata.substring(i + 26, i + 28) + tramatotaldata.substring(i + 24, i + 26) + tramatotaldata.substring(i + 22, i + 24), 16) == 1) {//es corte
                    long intdate = (Long.parseLong(tramatotaldata.substring(i + 6, i + 8) + tramatotaldata.substring(i + 4, i + 6) + tramatotaldata.substring(i + 2, i + 4) + tramatotaldata.substring(i, i + 2), 16) * 60000L) + (Long.parseLong(tramatotaldata.substring(i + 8, i + 10), 16) * 1000L);
                    fechadesconexion = df.format(new Date(new Timestamp(intdate).getTime() + (new Timestamp(intdate).getTimezoneOffset() * 60000)));
                    //System.out.println("Fecha corte " + fechadesconexion);
                    //System.out.println("Fecha corte HEX " + (tramatotaldata.substring(i + 6, i + 8) + tramatotaldata.substring(i + 4, i + 6) + tramatotaldata.substring(i + 2, i + 4) + tramatotaldata.substring(i, i + 2)));
                } else if (Long.parseLong(tramatotaldata.substring(i + 28, i + 30) + tramatotaldata.substring(i + 26, i + 28) + tramatotaldata.substring(i + 24, i + 26) + tramatotaldata.substring(i + 22, i + 24), 16) == 2) {//es conexion
                    long intdate = (Long.parseLong(tramatotaldata.substring(i + 6, i + 8) + tramatotaldata.substring(i + 4, i + 6) + tramatotaldata.substring(i + 2, i + 4) + tramatotaldata.substring(i, i + 2), 16) * 60000L) + (Long.parseLong(tramatotaldata.substring(i + 8, i + 10), 16) * 1000L);
                    fechaconexion = df.format(new Date(new Timestamp(intdate).getTime() + (new Timestamp(intdate).getTimezoneOffset() * 60000)));
                    //System.out.println("Fecha reconexion " + fechaconexion);
                    //System.out.println("Fecha reconexion HEX " + (tramatotaldata.substring(i + 6, i + 8) + tramatotaldata.substring(i + 4, i + 6) + tramatotaldata.substring(i + 2, i + 4) + tramatotaldata.substring(i, i + 2)));
                }
                if (fechadesconexion.length() > 0 && fechaconexion.length() > 0) {
                    guardar = true;
                }
                if (guardar) {
                    guardar = false;
                    try {
                        ereg = new ERegistroEvento();
                        ereg.setVcserie(serie);
                        ereg.setVctipo("0001");
                        ereg.setVcfechacorte(new Timestamp(df.parse(fechadesconexion).getTime()));
                        ereg.setVcfechareconexion(new Timestamp(df.parse(fechaconexion).getTime()));
                        actualizaEvento(ereg, connection);
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }
                    fechaconexion = "";
                    fechadesconexion = "";
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }

    }
    
    public void AlmacenaPerfil_SEL735(String serie, HashMap<Integer, EConstanteKE> consKE_Map, HashMap<String, EtipoCanal> tipoCanal_Map, HashMap<String, Integer> channelsCode, Timestamp uFecLec, LinkedHashMap<Timestamp, LinkedHashMap<String, Double>> dataLDP, int periodoInt, File file) {
        try {
            Electura lectura = null;
            Vector<Electura> vlec = new Vector<>();
            Timestamp actualizafec = uFecLec;
            EConstanteKE econske = null;
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyMMddHHmmss");
            for (Map.Entry<Timestamp, LinkedHashMap<String, Double>> entry : dataLDP.entrySet()) {
                System.out.println("Timestamp: " + entry.getKey());
                LinkedHashMap<String, Double> innerMap = entry.getValue();
                for (Map.Entry<String, Double> innerEntry : innerMap.entrySet()) {
                    econske = consKE_Map.get(channelsCode.get(innerEntry.getKey()));
                    int canal = econske.getCanal();
                    String unit = tipoCanal_Map.get("" + canal).getUnidad();
                    double dato = innerEntry.getValue();
                    actualizafec = entry.getKey();
                    lectura = new Electura(entry.getKey(), serie, canal, trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()) / 1000, dato, periodoInt, unit);
                    vlec.add(lectura);
                }
            }
            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Inicia carga Batchs", "Medidor: " + serie);
            escribir("Inicia procesamiento de base de datos", file);
            actualizaLecturaDesfase(vlec, file, new Timestamp(uFecLec.getTime() - (long) 86400000));
            escribir("Finaliza procesamiento de base de datos", file);
            //20-05-2021 se activa la actualizacion de fecha luego de almacenar
            if (uFecLec.before(actualizafec)) {
                actualizaFechaLectura(serie, sdf2.format(new Date(actualizafec.getTime() - TimeUnit.MINUTES.toMillis(periodoInt))));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }
    
    public void AlmacenaPerfil_ION(String serie, String marca, Timestamp uFecLec, String[] arrayPerfil, int periodoInt, int nCanales, ArrayList<Object[]> profileElements, File file) {
        try {
            Electura lectura = null;
            Vector<Electura> vlec = new Vector<>();
            LinkedHashMap<String, double[]> registers = new LinkedHashMap<>();
            Vector<EConstanteKE> constantes = buscarConstantesKe(serie);
            ArrayList<EtipoCanal> vtipocanal = obtenerTipoCanales(marca);
            EConstanteKE econske = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyMMddHHmmss");
            Timestamp actualizafec = null;
            Timestamp fechaIntervalo = null;
            double[] channelValues = new double[nCanales];
            int currReg = 0;
            double dato = 0;
            String unit = "";
            escribir("Bloque de datos de Lectura: " + Arrays.toString(arrayPerfil), file);
            int idxData = 0;
            int regCounter = 0;
            int blockCounter = 0;
            int channCounter = 0;
            List<Integer> codCanales = new ArrayList();
            List<Integer> posData = new ArrayList();
            for (Object[] objChann : profileElements) {
                int pos = (int) objChann[0];
                unit = (String) objChann[1];
                for (EtipoCanal ecanal : vtipocanal) {
                    if (unit.toUpperCase().equals(ecanal.getUnidad().toUpperCase())) {
                        codCanales.add(Integer.parseInt(ecanal.getCanal()));
                        posData.add(pos - 1);
                        break;
                    }
                }

            }
            while (true) {
                //System.out.println("Posición: " + idxData);
                try {
                    switch ((Integer.parseInt(arrayPerfil[idxData], 16) & 0xF0)) {
                        case 48://34
                            String valueStr = arrayPerfil[idxData + 1] + arrayPerfil[idxData + 2] + arrayPerfil[idxData + 3] + arrayPerfil[idxData + 4];
                            escribir("Value String: " + valueStr, file);
                            escribir("Value double: " + ieeeToFloat(valueStr), file);
                            channelValues[channCounter] = ieeeToFloat(valueStr);
                            if (channCounter < nCanales - 1) {
                                channCounter++;
                            } else {
                                registers.put(sdf.format(fechaIntervalo), channelValues);
                                channCounter = 0;
                                channelValues = new double[nCanales];
                            }
                            idxData += 5;
                            break;
                        case 80: //58
                            String hexDate = arrayPerfil[idxData + 1] + arrayPerfil[idxData + 2] + arrayPerfil[idxData + 3] + arrayPerfil[idxData + 4];
                            Long milliseg = Long.parseLong(hexDate, 16) * 2000L;
                            fechaIntervalo = new Timestamp(milliseg);
                            actualizafec = fechaIntervalo;
                            escribir("Fecha Intervalo: " + actualizafec, file);
                            idxData += 9;
                            break;
                        case 96: //62 o 63                              
                        {
                            regCounter++;
                            int trail = Integer.parseInt(arrayPerfil[idxData], 16) & 0x0F;
                            String valStr = "";
                            for (int i = 0; i < trail; i++) {
                                valStr += arrayPerfil[idxData + (i + 1)];
                                escribir("Hex Value: " + valStr, file);
                            }
                            currReg = Integer.parseInt(valStr, 16);
                            escribir("Registro :" + currReg, file);
                            idxData += trail + 1;
                        }
                        break;
                        case 112://77
                        {
                            int trail = Integer.parseInt(arrayPerfil[idxData], 16) & 0x0F;
                            boolean textFounded = false;
                            String[] valStr;
                            for (int i = 0; i < trail; i++) {
                                if (arrayPerfil[idxData + (i + 1)].equalsIgnoreCase("FA")) {//Viene texto en ASCII
                                    textFounded = true;
                                    int textLen = Integer.parseInt(arrayPerfil[idxData + trail], 16) & 0xFF;
                                    valStr = new String[textLen];
                                    System.arraycopy(arrayPerfil, idxData + trail + 1, valStr, 0, textLen);
                                    String strMsg = String.join(",", valStr);
                                    escribir(strMsg, file);
                                    escribir("Value double: 0.0", file);
                                    channelValues[channCounter] = 0.0;
                                    if (channCounter < nCanales - 1) {
                                        channCounter++;
                                    } else {
                                        registers.put(sdf.format(fechaIntervalo), channelValues);
                                        channCounter = 0;
                                        channelValues = new double[nCanales];
                                    }
                                    idxData += trail + textLen + 2;
                                    break;
                                }
                            }
                            if (!textFounded) {
                                escribir("Value double: 0.0", file);
                                channelValues[channCounter] = 0.0;
                                if (channCounter < nCanales - 1) {
                                    channCounter++;
                                } else {
                                    registers.put(sdf.format(fechaIntervalo), channelValues);
                                    channCounter = 0;
                                    channelValues = new double[nCanales];
                                }
                                idxData += trail + 2;
                            }
                        }
                        break;
                        default:
                            switch ((Integer.parseInt(arrayPerfil[idxData], 16) & 0xFF)) {
                                case 243: // F3
                                    escribir("Fin Intervalo: " + currReg, file);
                                    break;
                                case 249:
                                    escribir("Fin bloque: " + blockCounter, file);
                                    blockCounter++;
                                    break;
                            }
                            idxData++;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    escribir("Perfil de carga procesado", file);
                    break;
                } catch (Exception e) {
                    System.err.println("Fecha: " + new Date() );
                    e.printStackTrace();
                    return;
                }
            }

            for (String fechaInt : registers.keySet()) {
                escribir("Registro: " + fechaInt, file);
                double[] cVals = registers.get(fechaInt);
                int canal = 0;
                for (int i = 0; i < codCanales.size(); i++) {
                    canal = codCanales.get(i);
                    dato = cVals[posData.get(i)];
                    unit = (String) profileElements.get(i)[1];
                    if (canal != 0) {
                        econske = buscarConske(constantes, canal);
                        if (econske != null) {
                            lectura = new Electura(new Timestamp(sdf.parse(fechaInt).getTime()), serie, canal, trasnformarEnergia(dato, econske.getPesopulso(), econske.getMultiplo(), econske.getDivisor()), dato, periodoInt, unit);
                            vlec.add(lectura);
                        }
                    }
                }
            }
            /*
            int numintervalos = (int) (((actualizafec.getTime() - firstInterval.getTime()) / 60000) / 15);
            escribir("Número de Intervalos: " + numintervalos, file);
            String fechaIntervaloRevisionS;
            for (int num = 0; num < numintervalos; num++) {
                fechaIntervaloRevision = new Timestamp(firstInterval.getTime() + (num * intervalos * 60000));
                //buscamos si el intervalo se encuentra dentro de un evento de corte              
                escribir("Fecha Intervalo de Revisión Simex: " + fechaIntervaloRevision, file);
                fechaIntervaloRevisionS = sdf.format(new Date(fechaIntervaloRevision.getTime()));
                if (registers.get(fechaIntervaloRevisionS) == null) {
                    //buscamos unidades y en caso de no encontrar dato ponemos 0 en ese intervalo
                    escribir("Registro No Encontrado", file);
                    try {
                        int canal = 0;
                        for (EtipoCanal tc : vtipocanal) {
                            if (tc.getUnidad().toUpperCase().equals("kWhD".toUpperCase())) {
                                canal = Integer.parseInt(tc.getCanal());
                                break;
                            }
                        }
                        if (canal != 0) {
                            econske = buscarConske(constantes, canal);
                            if (econske != null) {
                                escribir("Almacena cero --> Canal: " + canal + ", Fecha: " + fechaIntervaloRevision, file);
                                lectura = new Electura(fechaIntervaloRevision, serie, canal, 0, 0, intervalos, "kWhD");
                                vlec.add(lectura);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Fecha: " + new Date() );
                        e.printStackTrace();
                    }

                    try {
                        int canal = 0;
                        for (EtipoCanal tc : vtipocanal) {
                            if (tc.getUnidad().toUpperCase().equals("kVarhD".toUpperCase())) {
                                canal = Integer.parseInt(tc.getCanal());
                                break;
                            }
                        }
                        if (canal != 0) {
                            econske = buscarConske(constantes, canal);
                            if (econske != null) {
                                escribir("Almacena cero --> Canal: " + canal + ", Fecha: " + fechaIntervaloRevision, file);
                                lectura = new Electura(fechaIntervaloRevision, serie, canal, 0, 0, intervalos, "kVarhD");
                                vlec.add(lectura);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
             */
            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Inicia carga Batchs", "Medidor: " + serie);
            escribir("Inicia procesamiento de base de datos", file);
            actualizaLecturaDesfase(vlec, file, new Timestamp(uFecLec.getTime() - (long) 86400000));
            escribir("Finaliza procesamiento de base de datos", file); 
            //20-05-2021 se activa la actualizacion de fecha luego de almacenar
            if (uFecLec.before(actualizafec)) {
                actualizaFechaLectura(serie, sdf2.format(new Date(actualizafec.getTime() - TimeUnit.MINUTES.toMillis(periodoInt))));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        }
    }

     private double ieeeToFloat(String hexString) throws Exception {
        hexString = hexString.replace(" ", "");
        /* 32-bit */
        if (hexString.length() == 8) {
            return Float.intBitsToFloat(Integer.parseUnsignedInt(hexString, 16));
        } /* 64-bit */ else if (hexString.length() == 16) {
            return Double.longBitsToDouble(Long.parseUnsignedLong(hexString, 16));
        } /* An exception thrown for mismatched strings */ else {
            throw new Exception("Does not represent internal bits of a floating-point number");
        }
    }
    
    private ArrayList<EConstanteKE> obtenerConstantesKe(String serie) {
        ArrayList<EConstanteKE> data = new ArrayList<EConstanteKE>();
        String sql = "SELECT * FROM TELE01_CONSTANTE WHERE vcserie=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        EConstanteKE conske = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, serie);
            rs = pstm.executeQuery();
            while (rs.next()) {
                conske = new EConstanteKE();
                conske.setCanal(rs.getInt("ncanal"));
                conske.setSeriemedidor(serie);
                conske.setPesopulso(rs.getDouble("nconstanteke"));
                conske.setDivisor(rs.getDouble("ndivisor"));
                conske.setMultiplo(rs.getDouble("nmultiplo"));
                conske.setConsInterna((rs.getInt("lkeinterna") == 1));
                data.add(conske);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
        return data;
    }

    public ArrayList<EtipoCanal> obtenerTipoCanales(String marca) {
        ArrayList<EtipoCanal> data = new ArrayList<>();
        String sql = "SELECT * FROM tele01_tipocanales WHERE vcmarca=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        EtipoCanal tcanal = null;
        try {
            validateConn();
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, marca);
            rs = pstm.executeQuery();
            while (rs.next()) {
                tcanal = new EtipoCanal();
                tcanal.setMarca(marca);
                tcanal.setDescripcion(rs.getString("vcmarca"));
                tcanal.setCanal(rs.getString("vccanal"));
                tcanal.setUnidad(rs.getString("vcconversion"));
                data.add(tcanal);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
        return data;
    }

    //FACTOR DE POTENCIA--------------------------------------------------------------
    public boolean getFactorEnPerfil(Connection conn, String vcserie) {
        boolean factorEnPerfil = false;
        if (conn == null) {
            validateConn();
        }
        String sql = "SELECT INSTANTANEOS FROM tele01_conf_medidor WHERE vcserie=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, vcserie);
            rs = pstm.executeQuery();
            if (rs.next()) {
                if (rs.getInt("instantaneos") == 1) {
                    factorEnPerfil = true;
                }
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
        return factorEnPerfil;
    }

    public double getAnguloDesfasePermitido(Connection conn) {
        if (conn == null) {
            validateConn();
        }
        Double desfase = 0.0;
        String strdesfase = "6";
        String sql = "SELECT vcvalor FROM tele01_parametros WHERE vccodparam=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, strdesfase);
            rs = pstm.executeQuery();
            if (rs.next()) {
                desfase = rs.getDouble("vcvalor");
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
        return desfase;
    }

    public void ingresaInstantaneos(EInstantaneos inst) throws Exception {
        //System.out.println("***Ingresando Valores Instantáneos***");
        //String vcserie, string fpOK, Timestamp dfec_lec, date vcfecha, double v1,v2,v3,i1, i2, i3, fp1, fp2, fp3.

        Date in = new Date();
        LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
        Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fecha2 = LocalDateTime.now().format(formatter2);
        validateConn();

        String sql2 = "";
        PreparedStatement pstm2 = null;
        try {
            sql2 = "INSERT INTO TELE01_INSTANTANEOS (VCSERIE,FPOK,FECLEC,FECHA,V1,I1,FP1,V2,I2,FP2,V3,I3,FP3)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            //pstm2 = connection.prepareStatement(sql2);
            pstm2 = genPrepStatement(sql2);
            pstm2.setString(1, inst.getSeriemedidor());
            pstm2.setString(2, inst.getFpOK());
            pstm2.setTimestamp(3, new Timestamp(out.getTime()));
            pstm2.setString(4, fecha2);
            pstm2.setDouble(5, inst.getV1());
            pstm2.setDouble(6, inst.getI1());
            pstm2.setDouble(7, inst.getFp1());
            pstm2.setDouble(8, inst.getV2());
            pstm2.setDouble(9, inst.getI2());
            pstm2.setDouble(10, inst.getFp2());
            pstm2.setDouble(11, inst.getV3());
            pstm2.setDouble(12, inst.getI3());
            pstm2.setDouble(13, inst.getFp3());
            pstm2.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (pstm2 != null) {
                    pstm2.close();
                }                
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }

    }

    public int getdesfasePermitido(Connection conn) {
        if (conn == null) {
            validateConn();
        }
        int desfase = 0;
        String strdesfase = "5";
        String sql = "SELECT vcvalor FROM tele01_parametros WHERE vccodparam=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, strdesfase);
            rs = pstm.executeQuery();
            if (rs.next()) {
                desfase = rs.getInt("vcvalor");
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
        return desfase;
    }

    public void actualizaDesfase(long dato, String serie, Connection conn) {
        if (conn == null) {
            validateConn();
        }
        String sql = "UPDATE tele01_conf_medidor set ndif=? WHERE vcserie=?";
        PreparedStatement pstm = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setLong(1, dato);
            pstm.setString(2, serie);
            pstm.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
// e.printStackTrace();
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public void actualizaDesfase(long dato, String serie) {
        ////System.out.println("Desfase: "+dato);
        ////System.out.println("Serial: "+serie);
        validateConn();
        String sql = "UPDATE tele01_conf_medidor set ndif=? WHERE vcserie=?";
        ////System.out.println("SQL: "+sql);
        PreparedStatement pstm = null;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setLong(1, dato);
            pstm.setString(2, serie);
            pstm.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /*
    public Connection validateConn(Connection conn) {
        try {
            if (conn != null) {
                if (!connection.isValid(1)) {
                    conn = null;
                    conn = new ConnectionManager(url, user, pass, dblinksimex, null).getConnection();
                }
            } else {
                conn = new ConnectionManager(url, user, pass, dblinksimex, null).getConnection();
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
    e.printStackTrace();
        }
        return conn;
    }
    */

    public ArrayList<ProgramacionesActivas> obtenerProgramacionesEquipo(String host, List<String> macList, ArrayList<Integer> dateParams, int minutos) {
        this.Equipo = "Programacion_Equipo_" + host;
        ArrayList<ProgramacionesActivas> ob = new ArrayList<>();
        PreparedStatement pstm = null;
        PreparedStatement pstm2 = null;
        PreparedStatement pstm3 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        String sql;
        String macs2Search = "";
        try {
            if ( !macList.isEmpty() ) {
                for(Iterator<String> it = macList.iterator(); it.hasNext();) {
                    macs2Search += "'"+ it.next() +"'";
                    if (it.hasNext()) {
                        macs2Search += ",";
                    }
                }
            } else {
                macs2Search += "''";
            }
            switch (db) {
                case "ORACLE":
                    sql = "SELECT * FROM (SELECT CASE TO_CHAR(" + dateParams.get(0) + ") WHEN '1' THEN DOMINGO WHEN '2' THEN LUNES WHEN '3' THEN MARTES WHEN '4' THEN MIERCOLES WHEN '5' THEN JUEVES WHEN '6' THEN VIERNES ELSE SABADO END validaDia, tprog.*"
                            + " FROM dual, ( SELECT * FROM TELE01_EQUIPOS te INNER JOIN TELE01_PROGRAMACIONES tp ON (te.VCMACEQUIPO = tp.VCEQUIPO)  WHERE UPPER(te.VCNOMEQUIPO) LIKE UPPER('%" + host + "%') OR te.VCMACEQUIPO IN (" + macs2Search + ") )tprog)"
                            + " WHERE validadia = 1"
                            + " AND enespera = 0"
                            + " AND to_number(vchoraini)*60 + to_number(vcminini)"
                            + " BETWEEN to_number(to_char(" + dateParams.get(1) + ")*60) + to_number(to_char(" + dateParams.get(2) + ")) AND to_number(to_char(" + dateParams.get(1) + "))*60 + to_number(to_char(" + dateParams.get(2) + ")) +" + minutos;
                    break;
                case "SQLServer":
                    sql = "select * from tele01_programaciones where vcequipo=? order by  CONVERT(VARCHAR(5),concat(concat(vchoraini,':'),vcminini),108)";
                    break;
                default:
                    sql = "";
            }
            System.out.println("Sentencia: " + sql);
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            ProgramacionesActivas prog;
            int cont = 0;
            while (rs.next()) {
                String sql2 = "UPDATE tele01_programaciones SET enespera = 1 WHERE vcprog = '" + rs.getString("VCPROG") + "'";
                //System.out.println("Sentencia 2: " + sql2);
                //pstm2 = connection.prepareStatement(sql2);
                pstm2 = genPrepStatement(sql2);
                pstm2.executeUpdate();
                pstm2.close();

                cont = 0;
                String sql3 = "select count(*) as contador from tele01_det_programacion where vcprogramacion='" + rs.getString("VCPROG") + "'";
                //pstm3 = connection.prepareStatement(sql3);
                pstm3 = genPrepStatement(sql3);
                rs2 = pstm3.executeQuery();
                if (rs2.next()) {
                    cont = rs2.getInt("contador");
                }
                rs2.close();
                pstm3.close();
                prog = new ProgramacionesActivas(rs.getString("VCPROG"), rs.getString("VCEQUIPO"), "Sin iniciar", "0/" + cont, rs.getString("VCHORAINI") + ":" + rs.getString("VCMININI"), rs.getString("VCHORAFIN") + ":" + rs.getString("VCMINFIN"));
                ob.add(prog);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {

            if (rs2 != null) {
                try {
                    rs2.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
            if (pstm3 != null) {
                try {
                    pstm3.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
            if (pstm2 != null) {
                try {
                    pstm2.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
        }
        return ob;
    }

    public void resetOldProgs(String host, ArrayList<Integer> dateParams, int minutos) {
        this.Equipo = "Programacion_Equipo_" + host;

        PreparedStatement pstm = null;
        String sql;
        try {
            switch (db) {
                case "ORACLE":
                    sql = "UPDATE tele01_programaciones SET enespera = 0 "
                            + "WHERE vcequipo IN ( SELECT VCMACEQUIPO FROM TELE01_EQUIPOS WHERE UPPER(VCNOMEQUIPO) LIKE '%" + host
                            + "%') AND enespera = 1"
                            + " AND to_number(vchorafin)*60 + to_number(vcminfin) + " + minutos + " < to_number(to_char(" + dateParams.get(1) + ")*60) + to_number(to_char(" + dateParams.get(2) + "))";
                    break;
                case "SQLServer":
                    sql = "";
                    break;
                default:
                    sql = "";
                    break;
            }
            //System.out.println("Sentencia: " + sql);
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
        }
    }     

    public void cambiarEstadoProg(String vccodprog) {
        PreparedStatement pstm = null;
        String sql;
        try {
            switch (db) {
                case "ORACLE":
                    sql = "UPDATE tele01_programaciones SET enespera = 0 WHERE vcprog = '" + vccodprog + "'";
                    break;
                case "SQLServer":
                    sql = "";
                    //sql = "select * from tele01_programaciones where vcequipo=? order by  CONVERT(VARCHAR(5),concat(concat(vchoraini,':'),vcminini),108)";
                    break;
                default:
                    sql = "";
            }
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                    System.err.println(e.getStackTrace()[0]);
                    System.err.println(e.getStackTrace()[1]);
                    System.err.println(e.getStackTrace()[2]);
                }
            }
        }
    }

    public Eprogramacion buscarProgramacion(String idprog) {
        validateConn();
        Eprogramacion p = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            String sql = "select tele01_programaciones.*,tele01_equipos.vcnomequipo from tele01_programaciones "
                    + "inner join tele01_equipos ON tele01_programaciones.vcequipo=tele01_equipos.VCMACEQUIPO where vcprog='" + idprog + "'";
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            if (rs.next()) {
                p = new Eprogramacion();
                p.setIdprog(idprog);
                p.setEquipo(rs.getString("vcequipo") + ":" + rs.getString("vcnomequipo"));
                p.setHoraini(rs.getString("vchoraini") + ":" + rs.getString("vcminini"));
                p.setHorafin(rs.getString("vchorafin") + ":" + rs.getString("vcminfin"));
                p.setLunes((rs.getInt("lunes") != 0));
                p.setMartes((rs.getInt("martes") != 0));
                p.setMiercoles((rs.getInt("miercoles") != 0));
                p.setJueves((rs.getInt("jueves") != 0));
                p.setViernes((rs.getInt("viernes") != 0));
                p.setSabado((rs.getInt("sabado") != 0));
                p.setDomingo((rs.getInt("domingo") != 0));
                p.setRepeticiones(rs.getInt("nrepeticiones") == 0 ? 1 : rs.getInt("nrepeticiones"));
                p.setEspera(rs.getInt("nespera"));
                p.setRecurrente(rs.getBoolean("esrecurrente"));
            }

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            rs = null;
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (Exception e) {
                }
            }
            pstm = null;
        }
        return p;
    }

    public ArrayList<EMedidorProgramado> buscarAllMedidoresProgamados(String idprog) {
        ArrayList<EMedidorProgramado> ldata = new ArrayList<EMedidorProgramado>();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            String sql = "select tdp.*,tm.vcnomcliente,tg.vcnomgrupo,tcm.vctipoconexion, tcm.ip,tcm.puertoip,tcm.numtelefono from tele01_det_programacion tdp "
                    + "inner join tele01_conf_medidor tcm ON tcm.vcserie=tdp.VCMEDIDOR "
                    + "inner join tele01_medidores tm ON tdp.VCMEDIDOR=tm.vcserie "
                    + "inner join tele01_grupo tg ON tg.vccodgrupo=tcm.vcgrupo "
                    + "where tdp.vcprogramacion='" + idprog + "' ";
            EMedidorProgramado emp = null;
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                emp = new EMedidorProgramado(idprog, rs.getString("vcmedidor"), (rs.getInt("lperfil") == 1), (rs.getInt("lregistros") == 1), (rs.getInt("leventos") == 1), (rs.getInt("lacumulados") == 1), (rs.getInt("lconfhora") == 1), (rs.getInt("ldisconnect") == 1), (rs.getInt("lreconnect") == 1));
                emp.setCliente(rs.getString("vcnomcliente"));
                emp.setGrupo(rs.getString("vcnomgrupo"));
                if (rs.getString("vctipoconexion").equals("1")) {
                    emp.setTel_ipcom(rs.getString("numtelefono"));
                } else {
                    emp.setTel_ipcom(rs.getString("ip") + "-" + rs.getString("puertoip"));
                }
                ldata.add(emp);
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }
        return ldata;
    }

    public String getipoRegistro(String marca, String canal, String tipo, Connection conn) {
        if (conn == null) {
            validateConn();
        }
        //System.out.println("" + marca);
        //System.out.println("" + canal);
        //System.out.println("" + tipo);
        String sqlcanal = "SELECT * FROM tele01_tipo_registros WHERE vcmarca=? AND  vccanal=? AND vctiporegistro=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String unidad = "";
        try {
            //ps = connection.prepareStatement(sqlcanal);
            ps = genPrepStatement(sqlcanal);
            ps.setString(1, marca);
            ps.setString(2, canal);
            ps.setString(3, tipo);
            rs = ps.executeQuery();
            if (rs.next()) {
                unidad = rs.getString("vcnunidades");
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
            }
        }
        return unidad;
    }

    public int findTipoLicencia(String codigo, Connection conn) throws Exception {
        String sql = "SELECT vccod_tipolic, vcdesc_tipolic, lverificar FROM m_tipo_licencia" + dblinksimex + " WHERE vccod_tipolic=?";
        PreparedStatement pstm = null;
        ResultSet rs = null;
        int tl = -1;
        try {
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setString(1, codigo);
            rs = pstm.executeQuery();
            if (rs.next()) {
                tl = rs.getInt("lverificar");
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (pstm != null) {
                pstm.close();
            }
        }
        return tl;
    }

    public boolean getLicencia(String fecha) {
        boolean ok = false;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        CifradoAES128 cifra = new CifradoAES128();
        validateConn();
        String sql;
        try {
            String empresa = "";
            try {
                sql = "SELECT vcnit_esp FROM parametros" + dblinksimex;
                //pstm = connection.prepareStatement(sql);
                pstm = genPrepStatement(sql);
                rs = pstm.executeQuery();
                if (rs.next()) {
                    empresa = rs.getString("vcnit_esp");
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (Exception e) {
                }
                try {
                    if (pstm != null) {
                        pstm.close();
                    }
                } catch (Exception e) {
                }
            }
            sql = "SELECT vcnit, vcmodulo, vccod_tipolic, vcnum_equipos, vcfecha_inicial, vcfecha_final, vcnombre_archivo FROM mov_licencias" + dblinksimex;
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            String fechaini = "";
            String fechafin = "";
            String nombreTemp;
            String nit = "";
            String modulo = "";
            String tipolicencia = "";
            while (rs.next()) {
                nombreTemp = rs.getString("vcnombre_archivo");
                nit = cifra.decrypt(nombreTemp.substring(0, 16), nombreTemp.substring(nombreTemp.length() - 16, nombreTemp.length()), rs.getString("vcnit"));
                modulo = cifra.decrypt(nombreTemp.substring(0, 16), nombreTemp.substring(nombreTemp.length() - 16, nombreTemp.length()), rs.getString("vcmodulo"));
                tipolicencia = cifra.decrypt(nombreTemp.substring(0, 16), nombreTemp.substring(nombreTemp.length() - 16, nombreTemp.length()), rs.getString("VCCOD_TIPOLIC"));
                fechaini = cifra.decrypt(nombreTemp.substring(0, 16), nombreTemp.substring(nombreTemp.length() - 16, nombreTemp.length()), rs.getString("VCFECHA_INICIAL"));
                fechafin = cifra.decrypt(nombreTemp.substring(0, 16), nombreTemp.substring(nombreTemp.length() - 16, nombreTemp.length()), rs.getString("VCFECHA_FINAL"));
                if (nit.equals(empresa) && modulo.equals("03")) {
                    boolean validar = false;
                    int tipo = findTipoLicencia(tipolicencia, connection);
                    if (tipo != -1 && tipo == 1) {
                        validar = true;
                    } else if (tipo != -1 && tipo == 0) {
                        ok = true;
                    }
                    if (validar) {
                        //la licencia es de la empresa
                        SimpleDateFormat encryptdate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Timestamp ini = new Timestamp(encryptdate.parse(fechaini + " 00:00:00").getTime());
                        Timestamp fin = new Timestamp(encryptdate.parse(fechafin + " 23:59:59").getTime());
                        //System.out.println("validacion de licencia temporal");
                        //System.out.println("Fecha ini " + ini);
                        //System.out.println("Fecha fin " + fin);
                        Timestamp today = new Timestamp(encryptdate.parse(fecha).getTime());
                        if (today.after(ini) && today.before(fin)) {
                            ok = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {

            }
        }
        return ok;
    }

    public void saveLogCall(ELogCall obj, Connection conn) {
        if (conn == null) {
            validateConn();
        }
        PreparedStatement pstm = null;
        String sql = "INSERT INTO TELE01_LOGS_LLAMADAS(TSFECHA,VCSERIE,VCSTATUS,TSFECHAINI,TSFECHAFIN,NDURACION,LPERFIL,LEVENTOS,LREGISTROS,VCCODUSER,NREINTENTOS,TIPOLOG) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            //stm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setTimestamp(1, obj.getDfecha());
            pstm.setString(2, obj.getSerie());
            pstm.setString(3, obj.getStatus());
            pstm.setTimestamp(4, obj.getFechaini());
            pstm.setTimestamp(5, obj.getFechafin());
            pstm.setInt(6, obj.getNduracion());
            pstm.setInt(7, obj.isLperfil() ? 1 : 0);
            pstm.setInt(8, obj.isLeventos() ? 1 : 0);
            pstm.setInt(9, obj.isLregistros() ? 1 : 0);
            pstm.setString(10, obj.getVccoduser());
            pstm.setInt(11, obj.getNreintentos());
            pstm.setString(12, obj.getTipoCall());
            pstm.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
            }
        }

    }

    public void actualizaDirFis(String serie, int dirfis) throws SQLException {
        PreparedStatement pstm = null;
        String sql = "UPDATE tele01_conf_medidor SET NDIRECCIONFISICA=? WHERE vcserie=?";
        try {
            validateConn();
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setInt(1, dirfis);
            pstm.setString(2, serie);
            pstm.executeUpdate();

        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm != null) {
                pstm.close();
            }
        }
    }
    
    public void actualizaFirmware(String serie, String firmware) throws SQLException {//actualizaFirmware
        PreparedStatement pstm = null;
        String sql = "UPDATE tele01_medidores SET VCFIRMWARE=? WHERE vcserie=?";
        try {
            Connection conn = getConn();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, firmware);
            pstm.setString(2, serie);
            pstm.executeUpdate();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Actualiza CODFirmware_ServTelesimex", "Actualiza Firmware desde Telesimex: " + firmware);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstm != null) {
                pstm.close();
            }
        }
    }
    
    public void actualizaDirCliente(String serie, int dirCliente) throws SQLException {
        PreparedStatement pstm = null;
        String sql = "UPDATE tele01_conf_medidor SET vcdireccioncliente = ? WHERE vcserie = ?";
        try {
            validateConn();
            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            pstm.setInt(1, dirCliente);
            pstm.setString(2, serie);
            pstm.executeUpdate();
            escribirLog(Equipo, new Timestamp(new Date().getTime()), serie, "Actualiza DirCliente_ServTelesimex", "Actualiza DirCliente desde Telesimex: " + dirCliente);
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            if (pstm != null) {
                pstm.close();
            }
        }
    }

    public void escribir(File file, String dato) {
        try {
            if (file != null) {
                d = Calendar.getInstance().getTime();
                fr = new RandomAccessFile(file, "rw");
                fr.seek(fr.length());
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write(dato.getBytes(), 0, dato.getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.close();
            }
        } catch (Exception e) {
        }
    }

    public void escribir(String dato, File file) {
        try {
            if (file != null) {
                Date d = Calendar.getInstance().getTime();
                RandomAccessFile fr = new RandomAccessFile(file, "rw");
                fr.seek(fr.length());
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write(dato.trim().getBytes(), 0, dato.trim().getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.write((String.format("%tD %1$tT", d) + " : ").getBytes(), 0, (String.format("%tD %1$tT", d) + " : ").getBytes().length);
                fr.write("\r\n\n".getBytes(), 0, "\r\n\n".getBytes().length);
                fr.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());//vic 13-08-19
        }
    }

    public void saveAcceso(String serie, String tipoAcceso, String datoAnt, String datoAct, String usuario, Connection conn) {
        validateConn();        
        PreparedStatement ps = null;
        String sql = "INSERT INTO tele01_accesos (dfecha,vcserie,vctipoacceso,vcdato_anterior,vcdato_actual,vccodusuario) VALUES (?,?,?,?,?,?)";
        try {
            //ps = connection.prepareStatement(sql);
            ps = genPrepStatement(sql);
            ps.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            ps.setString(2, serie);
            ps.setString(3, tipoAcceso);
            ps.setString(4, datoAnt);
            ps.setString(5, datoAct);
            ps.setString(6, usuario);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public Electura findUltimaLectura(String nSerie, Timestamp fecha) throws Exception {
        Electura lec = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String sql = null;
        SimpleDateFormat sgflect = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            validateConn();
            switch (db) {
                case "ORACLE":
                    sql = " SELECT * FROM tele01_lecturas WHERE vcserie='" + nSerie + "' "
                            + "AND TO_DATE(TO_CHAR(DFECHA,'YYYY-MM-DD HH24'),'YYYY-MM-DD HH24')= TO_DATE('" + sgflect.format(new Date(fecha.getTime())) + "', 'YYYY-MM-DD HH24') "
                            + "order by dfecha desc";
                    break;
                case "SQLServer":
                    sql = " SELECT * FROM tele01_lecturas WHERE vcserie='" + nSerie + "' "
                            + "AND DFECHA = '" + sgflect.format(new Date(fecha.getTime())) + "' "
                            + "order by dfecha desc";
                    break;
            }

            //pstm = connection.prepareStatement(sql);
            pstm = genPrepStatement(sql);
            rs = pstm.executeQuery();
            if (rs.next()) {
                lec = new Electura(rs.getTimestamp("DFECHA"), rs.getString("VCSERIE"), rs.getLong("NCANAL"), rs.getDouble("NLEC"), rs.getDouble("NPULSO"), rs.getInt("NINTERVALO"), rs.getString("IDVAR"));
                lec.setLecaux(rs.getDouble("LECAUX"));
            }
        } catch (Exception e) {
            System.err.println("Fecha: " + new Date() );
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (Exception e) {
                System.err.println("Fecha: " + new Date() );
                e.printStackTrace();
            }
        }
        return lec;
    }
    
    public PreparedStatement genPrepStatement(String sql) throws SQLException{
        int intentos = 0;
        while (intentos < 3) {
            try {
                PreparedStatement pstm = connection.prepareStatement(sql);
                return pstm;
            } catch (SQLException sqlE) {
                if (intentos < 2) {                  
                    validateConn();
                } else {
                    System.err.println("Fecha: " + new Date() );
                    sqlE.printStackTrace();
                }                
            } finally {
                intentos ++;
            }            
        }
        throw new SQLException("No se pudo obtener conexión con la base de datos.");        
    }
    
    public CallableStatement genCallStatement(String sql) throws SQLException{
        int intentos = 0;
        while (intentos < 3) {
            try {
                CallableStatement pCall = connection.prepareCall(sql);
                return pCall;
            } catch (SQLException sqlE) {
                if (intentos < 2) {                  
                    validateConn();
                } else {
                    System.err.println("Fecha: " + new Date() );
                    sqlE.printStackTrace();
                }                
            } finally {
                intentos ++;
            }            
        }
        throw new SQLException("No se pudo obtener conexión con la base de datos.");        
    }

    public File creaArchivoLog(EMedidor med) {

        File f = new File(rutalogs + sdfarchivo.format(new Date()) + "/" + med.getMarcaMedidor().getNombre() + "/");
        try {
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File f2 = new File(rutalogs + sdfarchivo.format(new Date()) + "/" + med.getMarcaMedidor().getNombre() + "/TCP_" + med.getnSerie() + ".txt");
        try {
            if (!f2.exists()) {
                f2.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f2;
    }

    public String getEquipo() {
        return Equipo;
    }

    public void setEquipo(String Equipo) {
        this.Equipo = Equipo;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWindowId() {
        return this.windowId;
    }

    public void setWindowId(String wId) {
        this.windowId = wId;
    }

}
