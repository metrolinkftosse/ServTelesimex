/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class EMarcaModem {
    public String codigo;
    public String nombre;

    public EMarcaModem(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public EMarcaModem() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
}
