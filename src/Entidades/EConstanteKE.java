/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

/**
 *
 * @author dperez
 */
public class EConstanteKE {

    String seriemedidor;
    int canal;
    long canal2;
    double pesopulso;
    double multiplo;
    double divisor;
    boolean consInterna;

    public EConstanteKE() {
        divisor = 1;
    }

    public int getCanal() {
        return canal;
    }

    public void setCanal(int canal) {
        this.canal = canal;
    }

    public boolean isConsInterna() {
        return consInterna;
    }

    public void setConsInterna(boolean consInterna) {
        this.consInterna = consInterna;
    }

    public double getDivisor() {
        return divisor;
    }

    public void setDivisor(double divisor) {
        this.divisor = divisor;
    }

    public double getMultiplo() {
        return multiplo;
    }

    public void setMultiplo(double multiplo) {
        this.multiplo = multiplo;
    }

    public double getPesopulso() {
        return pesopulso;
    }

    public void setPesopulso(double pesopulso) {
        this.pesopulso = pesopulso;
    }

    public String getSeriemedidor() {
        return seriemedidor;
    }

    public void setSeriemedidor(String seriemedidor) {
        this.seriemedidor = seriemedidor;
    }

    public long getCanal2() {
        return canal2;
    }

    public void setCanal2(long canal2) {
        this.canal2 = canal2;
    }
    
}
