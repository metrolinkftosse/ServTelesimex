package Entidades;

import java.sql.Timestamp;

/**
 *
 * @author nicol
 */
public class ELecturaAcumulada {
    public Timestamp fecha;
    public String serie;
    public int tarifa;
    public long canal;
    public double pulso;
    public String unit;

    public ELecturaAcumulada(Timestamp fecha, String serie, int tarifa, long canal, double pulso, String unit) {
        this.fecha = fecha;
        this.serie = serie;
        this.tarifa = tarifa;
        this.canal = canal;
        this.pulso = pulso;
        this.unit = unit;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getTarifa() {
        return tarifa;
    }

    public void setTarifa(int tarifa) {
        this.tarifa = tarifa;
    }    
    
    public long getCanal() {
        return canal;
    }

    public void setCanal(long canal) {
        this.canal = canal;
    }

    public double getPulso() {
        return pulso;
    }

    public void setPulso(double pulso) {
        this.pulso = pulso;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    
}
