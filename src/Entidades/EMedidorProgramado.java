/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class EMedidorProgramado {
    String prog;
    String serial;
    boolean lperfil;
    boolean lregistros;
    boolean leventos;
    boolean acumulados;
    public boolean confhora;
    public boolean disconnect;
    public boolean reconnect;
    String estado;
    String tel_ipcom;
    String cliente;
    String grupo;

    public EMedidorProgramado(String prog, String serial, boolean lperfil, boolean lregistros, boolean leventos, boolean acumulados, boolean confhora, boolean disconnect, boolean reconnect) {
        this.prog = prog;
        this.serial = serial;
        this.lperfil = lperfil;
        this.lregistros = lregistros;
        this.leventos = leventos;
        this.acumulados = acumulados;
        this.confhora=confhora;
        this.disconnect = disconnect;
        this.reconnect = reconnect;
    }

    public boolean isLeventos() {
        return leventos;
    }

    public void setLeventos(boolean leventos) {
        this.leventos = leventos;
    }

    public boolean isLperfil() {
        return lperfil;
    }

    public void setLperfil(boolean lperfil) {
        this.lperfil = lperfil;
    }

    public boolean isLregistros() {
        return lregistros;
    }

    public void setLregistros(boolean lregistros) {
        this.lregistros = lregistros;
    }

    public boolean isAcumulados() {
        return acumulados;
    }

    public void setAcumulados(boolean acumulados) {
        this.acumulados = acumulados;
    }
        
    public String getProg() {
        return prog;
    }

    public void setProg(String prog) {
        this.prog = prog;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getTel_ipcom() {
        return tel_ipcom;
    }

    public void setTel_ipcom(String tel_ipcom) {
        this.tel_ipcom = tel_ipcom;
    }

    public boolean isConfhora() {
        return confhora;
    }

    public void setConfhora(boolean confhora) {
        this.confhora = confhora;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public void setDisconnect(boolean disconnect) {
        this.disconnect = disconnect;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }
    

}
