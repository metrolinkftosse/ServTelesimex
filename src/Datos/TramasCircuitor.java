/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author Metrolink
 */
public class TramasCircuitor {

    byte solicitud[] = {(byte) 0x10, (byte) 0x5B, (byte) 0x01, (byte) 0x00, (byte) 0x5C, (byte) 0x16};
    byte inicio[] = {(byte) 0x68, (byte) 0x0D, (byte) 0x0D, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0xB7, (byte) 0x01, (byte) 0x06, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x35, (byte) 0x16};
    byte serialnumber[] = {(byte) 0x68, (byte) 0x09, (byte) 0x09, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0x64, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xDE, (byte) 0x16};
    byte logout[] = {(byte) 0x68, (byte) 0x09, (byte) 0x09, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0xBB, (byte) 0x00, (byte) 0x06, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x36, (byte) 0x16};
    byte fecha[] = {(byte) 0x68, (byte) 0x09, (byte) 0x09, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0x67, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xE1, (byte) 0x16};
    byte synchora[] = {(byte) 0x68, (byte) 0x10, (byte) 0x10, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0xB5, (byte) 0x01, (byte) 0x06, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7C, (byte) 0x30, (byte) 0x0B, (byte) 0x4A, (byte) 0x0B, (byte) 0x0F, (byte) 0x4C, (byte) 0x16};
    byte confcanales[] = {(byte) 0x68, (byte) 0x0A, (byte) 0x0A, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0xF7, (byte) 0x01, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x37, (byte) 0x89, (byte) 0x16};
    byte confperfil[] = {(byte) 0x68, (byte) 0x09, (byte) 0x09, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x16};
    byte solicitudPerfil[] = {(byte) 0x68, (byte) 0x14, (byte) 0x14, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0xBE, (byte) 0x01, (byte) 0x06, (byte) 0x01, (byte) 0x00, (byte) 0x0B, (byte) 0x09, (byte) 0x0D, (byte) 0x0B, (byte) 0xE2, (byte) 0x01, (byte) 0x00, (byte) 0x0D, (byte) 0x0B, (byte) 0x85, (byte) 0x0B, (byte) 0x0F, (byte) 0xFF, (byte) 0x16};    
    byte datoseventos[] = {(byte) 0x68, (byte) 0x13, (byte) 0x13, (byte) 0x68, (byte) 0x53, (byte) 0x01, (byte) 0x00, (byte) 0x66, (byte) 0x00, (byte) 0x06, (byte) 0x01, (byte) 0x00, (byte) 0x34, (byte) 0x00, (byte) 0x0C, (byte) 0xC1, (byte) 0x01, (byte) 0x00, (byte) 0x2A, (byte) 0x0C, (byte) 0x29, (byte) 0x0B, (byte) 0x0F, (byte) 0x3C, (byte) 0x16};
    byte elementosPerfil[] = {(byte) 0x68, (byte) 0x0A, (byte) 0x0A, (byte) 0x68, (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0x9C, (byte) 0x01, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xC5, (byte) 0xDF, (byte) 0x16};
    
    public byte[] getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(byte[] solicitud) {
        this.solicitud = solicitud;
    }

    public byte[] getInicio() {
        return inicio;
    }

    public void setInicio(byte[] inicio) {
        this.inicio = inicio;
    }

    public byte[] getSerialnumber() {
        return serialnumber;
    }

    public void setSerialnumber(byte[] serialnumber) {
        this.serialnumber = serialnumber;
    }

    public byte[] getLogout() {
        return logout;
    }

    public void setLogout(byte[] logout) {
        this.logout = logout;
    }

    public byte[] getFecha() {
        return fecha;
    }

    public void setFecha(byte[] fecha) {
        this.fecha = fecha;
    }

    public byte[] getSynchora() {
        return synchora;
    }

    public void setSynchora(byte[] synchora) {
        this.synchora = synchora;
    }

    public byte[] getConfcanales() {
        return confcanales;
    }

    public void setConfcanales(byte[] confcanales) {
        this.confcanales = confcanales;
    }

    public byte[] getConfperfil() {
        return confperfil;
    }

    public void setConfperfil(byte[] confperfil) {
        this.confperfil = confperfil;
    }

    public byte[] getSolicitudPerfil() {
        return solicitudPerfil;
    }

    public void setSolicitudPerfil(byte[] solicitudPerfil) {
        this.solicitudPerfil = solicitudPerfil;
    }

    public byte[] getDatoseventos() {
        return datoseventos;
    }

    public byte[] getElementosPerfil() {
        return elementosPerfil;
    }

    public void setElementosPerfil(byte[] elementosPerfil) {
        this.elementosPerfil = elementosPerfil;
    }
    
    

    public void setDatoseventos(byte[] datoseventos) {
        this.datoseventos = datoseventos;
    }
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public String encode(byte[] b, int ancho) {

        StringBuilder s = new StringBuilder(2 * b.length);

        for (int i = 0; i < ancho; i++) {

            int v = b[i] & 0xff;
            if (i != 0) {
                s.append(" ");
            }
            s.append((char) Hexhars[v >> 4]);
            s.append((char) Hexhars[v & 0xf]);
        }

        return s.toString().toUpperCase();
    }
}
