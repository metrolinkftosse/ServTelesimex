/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class EMarcaMedidor {
    public String codigo;
    public String nombre;
    public EFabricante fabricante;

    public EMarcaMedidor(String codigo, String nombre, EFabricante fabricante) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.fabricante = fabricante;
    }

    public EMarcaMedidor() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public EFabricante getFabricante() {
        return fabricante;
    }

    public void setFabricante(EFabricante fabricante) {
        this.fabricante = fabricante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
}
