/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author Metrolink
 */
public class TramasRemotaTecun {

    byte[] optico1 = {(byte) 0x2F, (byte) 0x3F, (byte) 0x21, (byte) 0x0D, (byte) 0x0A};
//    byte[] optico2 = {(byte) 0x06, (byte) 0x32, (byte) 0x35, (byte) 0x32, (byte) 0x0D, (byte) 0x0A};
    byte[] optico2 = {(byte) 0x06, (byte) 0x32, (byte) 0x33, (byte) 0x32, (byte) 0x0D, (byte) 0x0A};//VIC
    
    byte[] FRMR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x05, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x97, (byte) 0xF9, (byte) 0x5C, (byte) 0x7E};
    
    //byte[] snrm = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x02,(byte) 0x23, (byte) 0x61, (byte) 0x93, (byte) 0x52, (byte) 0x44, (byte) 0x7E};
    byte[] snrm = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x02, (byte) 0x53,(byte) 0x61, (byte) 0x93, (byte) 0x03, (byte) 0xA2, (byte) 0x7E}; //VIC
    byte[] aarq = {(byte) 0x7E, (byte) 0xA0, (byte) 0x45,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x10, (byte) 0xBB, (byte) 0xC1, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0x60, (byte) 0x36, (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85,
        (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0x8B,
        (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0xAC, (byte) 0x0A,
        (byte) 0x80, (byte) 0x08, (byte) 0x38, (byte) 0x37, (byte) 0x36, (byte) 0x35, (byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31,
        (byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F,
        (byte) 0x1F, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x1F, (byte) 0xFF, (byte) 0xFF, (byte) 0x2C, (byte) 0xF7, (byte) 0x7E};
    byte[] serial = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x32, (byte) 0x32, (byte) 0x9D, (byte) 0xE6, (byte) 0xE6,
        (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x01, (byte) 0x00,
        (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    byte[] fechaactual = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A, (byte) 0x02,(byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0x02, (byte) 0x9B, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
        (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x65, (byte) 0xD7, (byte) 0x7E};
    byte[] periodoint = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x76, (byte) 0x12, (byte) 0x99, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x04, (byte) 0x00, (byte) 0x3A, (byte) 0x0C, (byte) 0x7E};
    byte[] entradasperfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x76, (byte) 0x12, (byte) 0x99, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x07, (byte) 0x00, (byte) 0x3A, (byte) 0x0C, (byte) 0x7E};
    byte[] confperfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A, (byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x32, (byte) 0x01, (byte) 0xBE, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x03, (byte) 0x00, (byte) 0x32, (byte) 0x41, (byte) 0x7E};
    byte[] perfilcarga = {(byte) 0x7E, (byte) 0xA0, (byte) 0x4D, (byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0x70, (byte) 0x03, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04,
        (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
        (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF,
        (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x0F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF,
        (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF, (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x17, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0xCC, (byte) 0x55, (byte) 0x7E};
    byte[] perfilporentradas = {(byte) 0x7E, (byte) 0xA0, (byte) 0x4D, (byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0x70, (byte) 0x03, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x04, (byte) 0x06, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, //37
        (byte) 0x12, (byte) 0x00,
        (byte) 0x01, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7E};
    byte[] powerLost = {(byte) 0x7E, (byte) 0xA0, (byte) 0x2D, (byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x32, (byte) 0xAC, (byte) 0xBC, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
        (byte) 0x61, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x04, (byte) 0x06, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12, (byte) 0x00,
        (byte) 0x01, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7E};
    byte[] REQ_NEXT = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x02, (byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x01, (byte) 0x73, (byte) 0x7F, (byte) 0x7E};
    byte[] RR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x71, (byte) 0x7D, (byte) 0xA3, (byte) 0x7E};
    byte[] logout = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x53, (byte) 0x6D, (byte) 0xA1, (byte) 0x7E};
    byte[] confhora = {(byte) 0x7E, (byte) 0xA0, (byte) 0x28,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x32, (byte) 0x82, (byte) 0x7F, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC1, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE0, (byte) 0x05,
        (byte) 0x13, (byte) 0x04, (byte) 0x0E, (byte) 0x0D, (byte) 0x1E, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xBE, (byte) 0x7F, (byte) 0x7E};
    byte[] constant = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x32, (byte) 0x32, (byte) 0x9D, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0xFF, (byte) 0x03, (byte) 0x00, (byte) 0x58, (byte) 0x57, (byte) 0x7E};
    //vic 09-10-19
    byte[] disconnect = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC3, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x46, (byte) 0x00, (byte) 0x00, 
        (byte) 0x60, (byte) 0x03, (byte) 0x0A, (byte) 0xFF, (byte) 0x01, (byte) 0x0F, (byte) 0x00, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    byte[] connect = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC3, (byte) 0x01, (byte) 0x81,(byte) 0x00, (byte) 0x46, (byte) 0x00, (byte) 0x00, 
        (byte) 0x60, (byte) 0x03, (byte) 0x0A, (byte) 0xFF, (byte) 0x02,(byte) 0x0F, (byte) 0x00, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    byte[] outState = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x46, (byte) 0x00, (byte) 0x00, 
        (byte) 0x60, (byte) 0x03, (byte) 0x0A, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    // vic 10-10-19  - no se usa en programada - has no sense yet
    byte[] changePass = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC1, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x00, 
        (byte) 0x14, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x08, (byte) 0x00, (byte) 0x09, (byte) 0x08, (byte) 0x38, (byte) 0x37,
        (byte) 0x36, (byte) 0x35, (byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    // dirección física
    byte[] phyaddress = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x00, 
        (byte) 0x16, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x09, (byte) 0x00, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};    
    
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public byte[] getSnrm() {
        return snrm;
    }

    public void setSnrm(byte[] snrm) {
        this.snrm = snrm;
    }

    public byte[] getFRMR() {
        return FRMR;
    }

    public void setFRMR(byte[] FRMR) {
        this.FRMR = FRMR;
    }   
    
    public byte[] getAarq() {
        return aarq;
    }

    public void setAarq(byte[] aarq) {
        this.aarq = aarq;
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
    
    public byte[] getEntradasperfil() {
        return entradasperfil;
    }

    public void setEntradasperfil(byte[] entradasperfil) {
        this.entradasperfil = entradasperfil;
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
    
    public byte[] getPerfilporentradas() {
        return perfilporentradas;
    }

    public void setPerfilporentradas(byte[] perfilporentradas) {
        this.perfilporentradas = perfilporentradas;
    }

    public byte[] getPowerLost() {
        return powerLost;
    }

    public void setPowerLost(byte[] powerLost) {
        this.powerLost = powerLost;
    }

    public byte[] getREQ_NEXT() {
        return REQ_NEXT;
    }

    public void setREQ_NEXT(byte[] REQ_NEXT) {
        this.REQ_NEXT = REQ_NEXT;
    }

    public byte[] getRR() {
        return RR;
    }

    public void setRR(byte[] RR) {
        this.RR = RR;
    }

    public byte[] getLogout() {
        return logout;
    }

    public void setLogout(byte[] logout) {
        this.logout = logout;
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

    public byte[] getOptico1() {
        return optico1;
    }

    public void setOptico1(byte[] optico1) {
        this.optico1 = optico1;
    }

    public byte[] getOptico2() {
        return optico2;
    }

    public void setOptico2(byte[] optico2) {
        this.optico2 = optico2;
    }
    
    //vic 09-10-19
    public byte[] getDisconnect() {
        return disconnect;
    }

    public void setDisconnect(byte[] disconnect) {
        this.disconnect = disconnect;
    }
    
    public byte[] getConnect() {
        return connect;
    }

    public void setConnect(byte[] connect) {
        this.connect = connect;
    }
    
    public byte[] getOutState() {
        return outState;
    }

    public void setOutState(byte[] outState) {
        this.outState = outState;
    }
    
    //vic 10-10-19
    public byte[] getChangePass() {
        return changePass;
    }

    public void setChangePass(byte[] changePass) {
        this.changePass = changePass;
    }

    public byte[] getPhyaddress() {
        return phyaddress;
    }

    public void setPhyaddress(byte[] phyaddress) {
        this.phyaddress = phyaddress;
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
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }
        return hex.trim().toUpperCase();
    }
}
