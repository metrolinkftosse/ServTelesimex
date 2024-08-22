/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.sql.Timestamp;

/**
 *
 * @author Diego
 */
public class ELogCall {
    Timestamp dfecha;
    String serie;
    String status;
    Timestamp fechaini;
    Timestamp fechafin;
    int nduracion;
    boolean lperfil;
    boolean leventos;
    boolean lregistros;
    String vccoduser;
    int nreintentos;
    boolean lexito;
    String tipoCall;

    public ELogCall() {
    }

    public Timestamp getDfecha() {
        return dfecha;
    }

    public void setDfecha(Timestamp dfecha) {
        this.dfecha = dfecha;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getFechaini() {
        return fechaini;
    }

    public void setFechaini(Timestamp fechaini) {
        this.fechaini = fechaini;
    }

    public Timestamp getFechafin() {
        return fechafin;
    }

    public void setFechafin(Timestamp fechafin) {
        this.fechafin = fechafin;
    }

    public int getNduracion() {
        return nduracion;
    }

    public void setNduracion(int nduracion) {
        this.nduracion = nduracion;
    }

    public boolean isLperfil() {
        return lperfil;
    }

    public void setLperfil(boolean lperfil) {
        this.lperfil = lperfil;
    }

    public boolean isLeventos() {
        return leventos;
    }

    public void setLeventos(boolean leventos) {
        this.leventos = leventos;
    }

    public boolean isLregistros() {
        return lregistros;
    }

    public void setLregistros(boolean lregistros) {
        this.lregistros = lregistros;
    }

    public String getVccoduser() {
        return vccoduser;
    }

    public void setVccoduser(String vccoduser) {
        this.vccoduser = vccoduser;
    }

    public int getNreintentos() {
        return nreintentos;
    }

    public void setNreintentos(int nreintentos) {
        this.nreintentos = nreintentos;
    }

    public boolean isLexito() {
        return lexito;
    }

    public void setLexito(boolean lexito) {
        this.lexito = lexito;
    }

    public String getTipoCall() {
        return tipoCall;
    }

    public void setTipoCall(String tipoCall) {
        this.tipoCall = tipoCall;
    }
    
}