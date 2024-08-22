/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author dperez
 */
public class Electura {
    public Timestamp fecha;
    public String serie;
    public long canal;
    public double lec;
    public double lecaux = 0.0; // vic octubre
    public double pulso;
    public int intervalo;
    public String vccanal;

    
  

    public Electura(Timestamp fecha, String serie,long canal, double lec,double pulso,int intervalo,String vccanal) {
        this.fecha = fecha;
        this.serie = serie;
        this.canal=canal;
        this.lec = lec;
        this.pulso=pulso;
        this.intervalo=intervalo;
        this.vccanal=vccanal;
    }

    
    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public long getCanal() {
        return canal;
    }

    public void setCanal(long canal) {
        this.canal = canal;
    }

    public double getLec() {
        return lec;
    }

    public void setLec(double lec) {
        this.lec = lec;
    }
    
    public double getLecaux() { // vic octubre
        return lecaux;
    }

    public void setLecaux(double lecaux) {
        this.lecaux = lecaux;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public double getPulso() {
        return pulso;
    }

    public void setPulso(double pulso) {
        this.pulso = pulso;
    }

    public int getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(int intervalo) {
        this.intervalo = intervalo;
    }

    public String getVccanal() {
        return vccanal;
    }

    public void setVccanal(String vccanal) {
        this.vccanal = vccanal;
    }
    
    
    
}
