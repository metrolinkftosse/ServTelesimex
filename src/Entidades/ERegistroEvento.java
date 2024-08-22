/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

import java.sql.Timestamp;





/**
 *
 * @author dperez
 */
public class ERegistroEvento {
    public String vcserie;
    public String vctipo;
    public Timestamp vcfechacorte;
    public Timestamp vcfechareconexion;
    public int tiempocorte;//vic 07-06-19

    public ERegistroEvento() {
    }

    public Timestamp getVcfechacorte() {
        return vcfechacorte;
    }

    public void setVcfechacorte(Timestamp vcfechacorte) {
        this.vcfechacorte = vcfechacorte;
    }

    public Timestamp getVcfechareconexion() {
        return vcfechareconexion;
    }

    public void setVcfechareconexion(Timestamp vcfechareconexion) {
        this.vcfechareconexion = vcfechareconexion;
    }

    public String getVcserie() {
        return vcserie;
    }

    public void setVcserie(String vcserie) {
        this.vcserie = vcserie;
    }

    public String getVctipo() {
        return vctipo;
    }

    public void setVctipo(String vctipo) {
        this.vctipo = vctipo;
    }
    
    //VIC 07-06-19
    public int getTiempocorte() {
        return tiempocorte;
    }

    public void setTiempocorte(int tiempocorte) {
        this.tiempocorte = tiempocorte;
    }
    

}
