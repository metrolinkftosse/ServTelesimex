/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class ProgramacionesActivas {

    public String programacion;
    public String equipo;
    public String estado;
    public String avance;
    public String fechaini;
    public String fechafin;

    public ProgramacionesActivas(String programacion, String equipo, String estado, String avance, String fechaini, String fechafin) {
        this.programacion = programacion;
        this.equipo = equipo;
        this.estado = estado;
        this.avance = avance;
        this.fechaini = fechaini;
        this.fechafin = fechafin;
    }

    public ProgramacionesActivas(String programacion, String equipo) {
        this.programacion = programacion;
        this.equipo = equipo;
    }
    

    public String getAvance() {
        return avance;
    }

    public void setAvance(String avance) {
        this.avance = avance;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechafin() {
        return fechafin;
    }

    public void setFechafin(String fechafin) {
        this.fechafin = fechafin;
    }

    public String getFechaini() {
        return fechaini;
    }

    public void setFechaini(String fechaini) {
        this.fechaini = fechaini;
    }

    public String getProgramacion() {
        return programacion;
    }

    public void setProgramacion(String programacion) {
        this.programacion = programacion;
    }
    

}
