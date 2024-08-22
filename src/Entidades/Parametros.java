/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class Parametros {
    String param;
    String Descrippcion;
    String host;
    String usuario;
    String password;
    String smb;

    public Parametros(String param, String Descrippcion, String host, String usuario, String password, String smb) {
        this.param = param;
        this.Descrippcion = Descrippcion;
        this.host = host;
        this.usuario = usuario;
        this.password = password;
        this.smb = smb;
    }

    public String getDescrippcion() {
        return Descrippcion;
    }

    public void setDescrippcion(String Descrippcion) {
        this.Descrippcion = Descrippcion;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSmb() {
        return smb;
    }

    public void setSmb(String smb) {
        this.smb = smb;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    

}
