package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 *
 *  @author Faiver Andres Bustos A. Metodos Particulares 2008
 *  @since since 3 de julio de 2008, 10:46 PM
 *
 *  Clase que proporciona una conexion a la base de datos.
 */
public class ConnectionManager {
    //El String que contiene la ubicacion del driver para conexion con la bd.

    private String driver = "oracle.jdbc.driver.OracleDriver";
    //El url para ubicar el host y la bd.
    private String url;
    //El login del usuario que va a realizar la conexion
    private String login;
    //El password del usuario
    private String password;
    private String Mensaje;
    private String host;
    private String db;
    private String user;
    private String dblink;
    public String ruta = "C:\\metrolink\\ProtocolosMedidores";

    /** Creates a new instance of ConnectionManager
     * @param url
     * @param login
     * @param password
     * @param dblink
     * @param trash
     * @throws java.lang.Exception */
    public ConnectionManager(String url, String login, String password,String dblink,String trash) throws Exception {
        this.url = url;
        this.login = login;
        this.password = password;
        this.dblink=dblink;
        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new Exception("La clase " + this.driver + " no pudo ser encontrada");
        }
    }

    public ConnectionManager(String driver, String url, String login, String password) throws Exception {
        this.driver = driver;
        this.url = url;
        this.login = login;
        this.password = password;

        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new Exception("La clase " + this.driver + " no pudo ser encontrada");
        }
    }

    /**
     *  Regresa una conexion del tipo java.sql.Connection
     *  @return la conexion
     *  @exception Exception en caso de que no se pueda establecer la conexion
     */
    public  Connection getConnection() throws Exception {
        try {
            return DriverManager.getConnection(url, login, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("No se pudo obtener la conexion con los siguiente parametros url:" + url + " login:" + login + " password:" + password);
        }
    }

    public String getMensaje() {
        return Mensaje;
    }

    public void setMensaje(String Mensaje) {
        this.Mensaje = Mensaje;
    }
}
