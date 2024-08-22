/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Vic
 */
public class ElecturaAux {
    public ArrayList<Double> lecAuxMed = new ArrayList<>();
    public int indxlec = 0;
    public Vector<Electura> vlec = new Vector<Electura>();
    public ArrayList<ERegistro> vreg = new ArrayList<ERegistro>();

    public ElecturaAux() {
    }

    public ArrayList<Double> getLecAuxMed() {
        return lecAuxMed;
    }

    public void setLecAuxMed(ArrayList<Double> lecAuxMed) {
        this.lecAuxMed = lecAuxMed;
    }

    public int getIndexlec() {
        return indxlec;
    }

    public void setIndexlec(int indexlec) {
        this.indxlec = indexlec;
    }

    public Vector<Electura> getVlec() {
        return vlec;
    }

    public void setVlec(Vector<Electura> vlec) {
        this.vlec = vlec;
    }

    public ArrayList<ERegistro> getVreg() {
        return vreg;
    }

    public void setVreg(ArrayList<ERegistro> vreg) {
        this.vreg = vreg;
    }
}
