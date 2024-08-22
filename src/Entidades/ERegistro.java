/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

/**
 *
 * @author Lenovo
 */
public class ERegistro {
    public String seriemedidor;
    public String tiporegistro;
    public int year;
    public int month;
    public int day;
    public double pulsos;
    public long ncanal;
    public String idvar;
    public double energia;
    public double lecaux = 0.0; 
    public String tasa;
    public String fase;
    
    
    public ERegistro(String seriemedidor, String tiporegistro, int year, int month, int day, double pulsos, long ncanal, String idvar, double energia){
        this.seriemedidor = seriemedidor;
        this.tiporegistro = tiporegistro;
        this.year = year;
        this.month = month;
        this.day = day;
        this.pulsos = pulsos;
        this.ncanal = ncanal;
        this.idvar = idvar;
        this.energia = energia;
    }

    public String getSeriemedidor() {
        return seriemedidor;
    }

    public void setSeriemedidor(String seriemedidor) {
        this.seriemedidor = seriemedidor;
    }

    public String getTiporegistro() {
        return tiporegistro;
    }

    public void setTiporegistro(String tiporegistro) {
        this.tiporegistro = tiporegistro;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getPulsos() {
        return pulsos;
    }

    public void setPulsos(double pulsos) {
        this.pulsos = pulsos;
    }

    public long getNcanal() {
        return ncanal;
    }

    public void setNcanal(long ncanal) {
        this.ncanal = ncanal;
    }

    public String getIdvar() {
        return idvar;
    }

    public void setIdvar(String idvar) {
        this.idvar = idvar;
    }

    public double getEnergia() {
        return energia;
    }

    public void setEnergia(double energia) {
        this.energia = energia;
    }

    public double getLecaux() {
        return lecaux;
    }

    public void setLecaux(double lecaux) {
        this.lecaux = lecaux;
    }

    public String getTasa() {
        return tasa;
    }

    public void setTasa(String tasa) {
        this.tasa = tasa;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }
    
}