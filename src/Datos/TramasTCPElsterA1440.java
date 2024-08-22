/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;



/**
 *
 * @author Metrolink
 */
public class TramasTCPElsterA1440 {
    private byte[] identificacion2 = {(byte) 0x2F, (byte) 0x3F, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
        (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
        (byte) 0x21, (byte) 0x0D, (byte) 0x0A};
    private byte[] comunicacion = {(byte) 0x06, (byte) 0x30, (byte) 0x35, (byte) 0x31, (byte) 0x0D, (byte) 0x0A};
    private byte[] password = {(byte) 0x01, (byte) 0x50, (byte) 0x31, (byte) 0x02, (byte) 0x28, (byte) 0x30, (byte) 0x30,
        (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x29, (byte) 0x03, (byte) 0x61};
    private byte[] seriemedidor = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x30,
        (byte) 0x2E, (byte) 0x30, (byte) 0x28, (byte) 0x29, (byte) 0x03, (byte) 0x57};
    private byte[] fechaactual = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x39, (byte) 0x2E, (byte) 0x32, (byte) 0x28, (byte) 0x29, (byte) 0x03, (byte) 0x5C};
    private byte[] fechatime = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x39, (byte) 0x2E, (byte) 0x31, (byte) 0x28, (byte) 0x29, (byte) 0x03, (byte) 0x5F};
    private byte[] perfil = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x30, (byte) 0x31, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] perfilfinal = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x30, (byte) 0x31, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    //private byte[] eventos = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x39, (byte) 0x38, (byte) 0x28, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] eventos = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x39, (byte) 0x38, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] eventosfinal = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x39, (byte) 0x38, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] sincreloj = {(byte) 0x01, (byte) 0x57, (byte) 0x32, (byte) 0x02, (byte) 0x43,(byte) 0x30,(byte) 0x30,(byte) 0x33, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34,
        (byte) 0x31, (byte) 0x31, (byte) 0x32, (byte) 0x34, (byte) 0x31, (byte) 0x34, (byte) 0x35, (byte) 0x37, (byte) 0x35, (byte) 0x39, (byte) 0x29, (byte) 0x03, (byte) 0x63};
    private byte[] desconexion = {(byte) 0x01, (byte) 0x42, (byte) 0x30, (byte) 0x03, (byte) 0x71};
       //codificacion
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public byte[] getIdentificacion2() {
        return identificacion2;
    }

    public void setIdentificacion2(byte[] identificacion2) {
        this.identificacion2 = identificacion2;
    }

    public byte[] getComunicacion() {
        return comunicacion;
    }

    public void setComunicacion(byte[] comunicacion) {
        this.comunicacion = comunicacion;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public byte[] getSeriemedidor() {
        return seriemedidor;
    }

    public void setSeriemedidor(byte[] seriemedidor) {
        this.seriemedidor = seriemedidor;
    }
        
    public byte[] getDesconexion() {
        return desconexion;
    }

    public byte[] getFechaactual() {
        return fechaactual;
    }

    public void setFechaactual(byte[] fechaactual) {
        this.fechaactual = fechaactual;
    }

    public byte[] getFechatime() {
        return fechatime;
    }

    public void setFechatime(byte[] fechatime) {
        this.fechatime = fechatime;
    }

    public byte[] getPerfil() {
        return perfil;
    }

    public void setPerfil(byte[] perfil) {
        this.perfil = perfil;
    }

    public byte[] getPerfilfinal() {
        return perfilfinal;
    }

    public void setPerfilfinal(byte[] perfilfinal) {
        this.perfilfinal = perfilfinal;
    }

    public byte[] getEventos() {
        return eventos;
    }

    public void setEventos(byte[] eventos) {
        this.eventos = eventos;
    }

    public byte[] getEventosfinal() {
        return eventosfinal;
    }

    public void setEventosfinal(byte[] eventosfinal) {
        this.eventosfinal = eventosfinal;
    }

    public byte[] getSincreloj() {
        return sincreloj;
    }

    public void setSincreloj(byte[] sincreloj) {
        this.sincreloj = sincreloj;
    }    

    public void setDesconexion(byte[] desconexion) {
        this.desconexion = desconexion;
    }    
    
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
