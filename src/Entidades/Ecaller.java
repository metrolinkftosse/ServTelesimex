/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

import java.util.Vector;

/**
 *
 * @author dperez
 */
public class Ecaller {
    public EMedidor medidor;
    public boolean perfil;
    public boolean eventos;
    public boolean registros;   
    public boolean acumulados;
    public boolean confhora;
    public boolean disconnect;
    public boolean reconnect;
    public int index;
    public Vector<EConfModem> confmodem;

    public Ecaller(EMedidor medidor, boolean perfil, boolean eventos, int index) {
        this.medidor = medidor;
        this.perfil = perfil;
        this.eventos = eventos;
        this.index = index;
    }
    public Ecaller(EMedidor medidor,Vector<EConfModem> confmodem, boolean perfil, boolean eventos,boolean registros, boolean acumulados, boolean confhora, boolean disconnect, boolean reconnect, int index) {
        this.medidor = medidor;
        this.perfil = perfil;
        this.eventos = eventos;
        this.registros = registros;
        this.acumulados = acumulados;
        this.confhora=confhora;
        this.disconnect = disconnect;
        this.reconnect = reconnect;
        this.index = index;
        this.confmodem=confmodem;
    }
    public Ecaller(EMedidor medidor,Vector<EConfModem> confmodem, boolean perfil, boolean eventos,boolean registros, boolean acumulados, boolean confhora, boolean disconnect, boolean reconnect) {
        this.medidor = medidor;
        this.perfil = perfil;
        this.eventos = eventos;
        this.registros = registros;
        this.acumulados = acumulados;
        this.confhora=confhora;
        this.disconnect = disconnect;
        this.reconnect = reconnect;
        this.confmodem=confmodem;
    }

    public boolean isEventos() {
        return eventos;
    }

    public void setEventos(boolean eventos) {
        this.eventos = eventos;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public EMedidor getMedidor() {
        return medidor;
    }

    public void setMedidor(EMedidor medidor) {
        this.medidor = medidor;
    }

    public boolean isPerfil() {
        return perfil;
    }

    public void setPerfil(boolean perfil) {
        this.perfil = perfil;
    }

    public boolean isRegistros() {
        return registros;
    }

    public void setRegistros(boolean registros) {
        this.registros = registros;
    }

    public boolean isAcumulados() {
        return acumulados;
    }

    public void setAcumulados(boolean acumulados) {
        this.acumulados = acumulados;
    }       

    public Vector<EConfModem> getConfmodem() {
        return confmodem;
    }

    public void setConfmodem(Vector<EConfModem> confmodem) {
        this.confmodem = confmodem;
    }

    public boolean isConfhora() {
        return confhora;
    }

    public void setConfhora(boolean confhora) {
        this.confhora = confhora;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public void setDisconnect(boolean disconnect) {
        this.disconnect = disconnect;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }
        
}
