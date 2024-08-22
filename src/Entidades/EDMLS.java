/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class EDMLS {
    public String codigo;
    public String obis;
    public String atributo;
    public String classID;
    public String BaseName;
    public String firmware;

    public EDMLS(String codigo, String obis, String atributo, String classID, String BaseName) {
        this.codigo = codigo;
        this.obis = obis;
        this.atributo = atributo;
        this.classID = classID;
        this.BaseName = BaseName;
    }

    public EDMLS(String codigo, String obis, String atributo, String classID, String BaseName, String firmware) {
        this.codigo = codigo;
        this.obis = obis;
        this.atributo = atributo;
        this.classID = classID;
        this.BaseName = BaseName;
        this.firmware = firmware;
    }

    public EDMLS() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getBaseName() {
        return BaseName;
    }

    public void setBaseName(String BaseName) {
        this.BaseName = BaseName;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getObis() {
        return obis;
    }

    public void setObis(String obis) {
        this.obis = obis;
    }

    public String getAtributo() {
        return atributo;
    }

    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }
    
    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }
}
