/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class EtipoCanal {
    String marca;
    String canal;
    String descripcion;
    String unidad;
    String tipo;

    public EtipoCanal() {
    }

    public EtipoCanal(String marca, String canal, String descripcion) {
        this.marca = marca;
        this.canal = canal;
        this.descripcion = descripcion;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
    
}
