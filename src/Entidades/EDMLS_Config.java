/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

/**
 *
 * @author dperez
 */
public class EDMLS_Config {

    public int nbytesdir;
    public int nbyteControl;
    public int TipoTrama;
    public int AARE1;
    public int AARE2;
    public int tamDataAARE;
    public int tamPasswordAARE;
    public int tamPasswordPlus2AARE;
    public String users;

    public EDMLS_Config(int nbytesdir, int nbyteControl, int TipoTrama, int AARE1, int AARE2, int tamDataAARE, int tamPasswordAARE, int tamPasswordPlus2AARE) {
        this.nbytesdir = nbytesdir;
        this.nbyteControl = nbyteControl;
        this.TipoTrama = TipoTrama;
        this.AARE1 = AARE1;
        this.AARE2 = AARE2;
        this.tamDataAARE = tamDataAARE;
        this.tamPasswordAARE = tamPasswordAARE;
        this.tamPasswordPlus2AARE = tamPasswordPlus2AARE;
    }

    public int getAARE1() {
        return AARE1;
    }

    public void setAARE1(int AARE1) {
        this.AARE1 = AARE1;
    }

    public int getAARE2() {
        return AARE2;
    }

    public void setAARE2(int AARE2) {
        this.AARE2 = AARE2;
    }

    public int getTamDataAARE() {
        return tamDataAARE;
    }

    public void setTamDataAARE(int tamDataAARE) {
        this.tamDataAARE = tamDataAARE;
    }

    public int getTamPasswordAARE() {
        return tamPasswordAARE;
    }

    public void setTamPasswordAARE(int tamPasswordAARE) {
        this.tamPasswordAARE = tamPasswordAARE;
    }

    public int getTamPasswordPlus2AARE() {
        return tamPasswordPlus2AARE;
    }

    public void setTamPasswordPlus2AARE(int tamPasswordPlus2AARE) {
        this.tamPasswordPlus2AARE = tamPasswordPlus2AARE;
    }

    public EDMLS_Config() {
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public int getTipoTrama() {
        return TipoTrama;
    }

    public void setTipoTrama(int TipoTrama) {
        this.TipoTrama = TipoTrama;
    }

    public int getNbyteControl() {
        return nbyteControl;
    }

    public void setNbyteControl(int nbyteControl) {
        this.nbyteControl = nbyteControl;
    }

    public int getNbytesdir() {
        return nbytesdir;
    }

    public void setNbytesdir(int nbytesdir) {
        this.nbytesdir = nbytesdir;
    }
}
