/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

//import static Datos.TramasRemotaTCPElgama.Hexhars;

/**
 *
 * @author Metrolink
 */
public class TramasRemotaGPRSIskraMT880 {

    byte[] aarq = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x38, (byte) 0x60, (byte) 0x36, (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, //15
        (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0x8B, //25
        (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0xAC, (byte) 0x0A, //35
        (byte) 0x80, (byte) 0x08, (byte) 0x38, (byte) 0x37, (byte) 0x36, (byte) 0x35, (byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, //45
        (byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, //55
        (byte) 0x1F, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0xB8, (byte) 0x1D, (byte) 0xFF, (byte) 0xFF}; //63
    byte[] serial = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x60, 
        (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00};
    byte[] logicalDeviceName = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x2A, 
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00};
    byte[] fechaactual = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, 
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00};
    byte[] periodoint = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, 
        (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x04, (byte) 0x00};
    byte[] confperfil = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, 
        (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x03, (byte) 0x00};
    byte[] perfilcarga = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, //15
        (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04, //25
        (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, //35
        (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE3, //45
        (byte) 0x02, (byte) 0x19, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, //55
        (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE3, (byte) 0x02, (byte) 0x22, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00};
    byte[] powerLost = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x61, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00}; //no es necesario agregar descriptor si se quieren todos los registros
    byte[] standardEvent = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x63,
        (byte) 0x62, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00};
    byte[] REQ_NEXT = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0, (byte) 0x02, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
    byte[] confhora = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D,(byte) 0xC1, (byte) 0x01, (byte) 0x40, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, //15
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE0, (byte) 0x05, //25
        (byte) 0x13, (byte) 0x04, (byte) 0x0E, (byte) 0x0D, (byte) 0x1E, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00};
    byte[] constant = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, //5
        (byte) 0x00, (byte) 0x0D, (byte) 0xC0 , (byte) 0x01 , (byte) 0x40 , (byte) 0x00 , (byte) 0x03 , (byte) 0x01 , 
         (byte) 0x00 , (byte) 0x20 , (byte) 0x07 , (byte) 0x00 , (byte) 0xFF , (byte) 0x03 , (byte) 0x00};
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public byte[] getAarq() {
        return aarq;
    }

    public void setAarq(byte[] aarq) {
        this.aarq = aarq;
    }

    public byte[] getLogicalDeviceName() {
        return logicalDeviceName;
    }

    public void setLogicalDeviceName(byte[] logicalDeviceName) {
        this.logicalDeviceName = logicalDeviceName;
    }
    
    public byte[] getSerial() {
        return serial;
    }

    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

    public byte[] getFechaactual() {
        return fechaactual;
    }

    public void setFechaactual(byte[] fechaactual) {
        this.fechaactual = fechaactual;
    }

    public byte[] getPeriodoint() {
        return periodoint;
    }

    public void setPeriodoint(byte[] periodoint) {
        this.periodoint = periodoint;
    }

    public byte[] getConfperfil() {
        return confperfil;
    }

    public void setConfperfil(byte[] confperfil) {
        this.confperfil = confperfil;
    }

    public byte[] getPerfilcarga() {
        return perfilcarga;
    }

    public void setPerfilcarga(byte[] perfilcarga) {
        this.perfilcarga = perfilcarga;
    }

    public byte[] getPowerLost() {
        return powerLost;
    }

    public void setPowerLost(byte[] powerLost) {
        this.powerLost = powerLost;
    }
    
    
    public byte[] getStandardEvent() {
        return standardEvent;
    }

    public void setStandardEvent(byte[] standardEvent) {
        this.standardEvent = standardEvent;
    }

    public byte[] getREQ_NEXT() {
        return REQ_NEXT;
    }

    public void setREQ_NEXT(byte[] REQ_NEXT) {
        this.REQ_NEXT = REQ_NEXT;
    }

    public byte[] getConfhora() {
        return confhora;
    }

    public void setConfhora(byte[] confhora) {
        this.confhora = confhora;
    }

    public byte[] getConstant() {
        return constant;
    }

    public void setConstant(byte[] constant) {
        this.constant = constant;
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

    public String convertStringToHex(String str) {
        //System.out.println("cadena a transformar " + str);
//        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
    }
}
