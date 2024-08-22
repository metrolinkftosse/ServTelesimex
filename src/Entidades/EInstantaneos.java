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
public class EInstantaneos {
    public String seriemedidor;
    public String fpOK;
    public double v1;
    public double i1;
    public double fp1;
    public double v2;
    public double i2;
    public double fp2;
    public double v3;
    public double i3;
    public double fp3;
      
    
    public EInstantaneos(String seriemedidor, String fpOK, double v1, double i1, double fp1, double v2, double i2, double fp2, double v3, double i3, double fp3){
        this.seriemedidor = seriemedidor;
        this.fpOK = fpOK;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.i1 = i1;
        this.i2 = i2;
        this.i3 = i3;
        this.fp1 = fp1;
        this.fp2 = fp2;
        this.fp3 = fp3;
    }

    public String getSeriemedidor() {
        return seriemedidor;
    }

    public void setSeriemedidor(String seriemedidor) {
        this.seriemedidor = seriemedidor;
    }

    public String getFpOK() {
        return fpOK;
    }

    public void setFpOK(String fpOK) {
        this.fpOK = fpOK;
    }

    
    public double getV1() {
        return v1;
    }

    public void setV1(double v1) {
        this.v1 = v1;
    }

    public double getV2() {
        return v2;
    }

    public void setV2(double v2) {
        this.v2 = v2;
    }

    public double getV3() {
        return v3;
    }

    public void setV3(double v3) {
        this.v3 = v3;
    }

    public double getI1() {
        return i1;
    }

    public void setI1(double i1) {
        this.i1 = i1;
    }

    public double getI2() {
        return i2;
    }

    public void setI2(double i2) {
        this.i2 = i2;
    }

    public double getI3() {
        return i3;
    }

    public void setI3(double i3) {
        this.i3 = i3;
    }

    public double getFp1() {
        return fp1;
    }

    public void setFp1(double fp1) {
        this.fp1 = fp1;
    }

    public double getFp2() {
        return fp2;
    }

    public void setFp2(double fp2) {
        this.fp2 = fp2;
    }

    public double getFp3() {
        return fp3;
    }

    public void setFp3(double fp3) {
        this.fp3 = fp3;
    }
    
    
}