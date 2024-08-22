/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

import java.util.Vector;

/**
 *
 * @author dperez
 */
public class TramasLocalABB {

    Vector tramas;
    public String localinicio = "02 83 C7 89";
    //01 local peticion  quien es ud?
    byte[] local0 = {(byte) 0x02, (byte) 0x18, (byte) 0x06, (byte) 0x08, (byte) 0x01, (byte) 0x00, (byte) 0x30, (byte) 0x3E};
    //02 peticion local para quien es ud en medidores sin contraseña (caso especial)
    byte[] local1 = {(byte) 0x02, (byte) 0x83, (byte) 0x00, (byte) 0x00, (byte) 0x8F, (byte) 0x62};
    //03 envio de data para calculo de crc en encriptacion de password (NOTA:rescribir los ultimos 4 posiciones)
    byte[] local2 = {(byte) 0x02, (byte) 0x18, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    //04 envio de data para encriptacion con el nuevo crc calculado (NOTA:rescribir los ultimos 6 posiciones con data y el nuevo crc)
    byte[] local3 = {(byte) 0x02, (byte) 0x18, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    //05 Peticion cambio de velocidad si se requiere el cambio de velocidad final se debe modificar la trama
    byte[] local4 = {(byte) 0x02, (byte) 0x93, (byte) 0x00, (byte) 0x00, (byte) 0xCC, (byte) 0x01};
    //06 peticion envio de data para medidores sin contraseña
    byte[] local5 = {(byte) 0x02, (byte) 0x18, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xD4, (byte) 0x6E};
    //peticion de cambio de velocidad sin handshaking
    byte[] local6 = {(byte) 0x02, (byte) 0x93, (byte) 0x00, (byte) 0x00, (byte) 0xCC, (byte) 0x01};
    //peticion cambio de timeout se cambia el valor del timeout el 6to byte y los ultimos para el numero de dias
    byte[] local7 = {(byte) 0x02, (byte) 0x18, (byte) 0xF2, (byte) 0x00, (byte) 0x01, (byte) 0x1E, (byte) 0x3F, (byte) 0xDC};
    //peticion envio a 256 bytes
    byte[] local8 = {(byte) 0x02, (byte) 0x18, (byte) 0x09, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0x0D, (byte) 0xF5};
    //peticion envio de perfil de carga depende de dias se cambia el 5to byte por el numero de dias a leer y los 2 ultimos para el crc
    byte[] local9 = {(byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x00,
        (byte) 0x00};
    //peticion para que continue enviando datos
    byte[] local10 = {(byte) 0x02, (byte) 0x81, (byte) 0xE7, (byte) 0xCB};
    //peticion para el registro registro status#1
    byte[] local11 = {(byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x00,
        (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x4B, (byte) 0xC1};
    //cambio de timeout para registro de eventos.
    byte[] local12 = {(byte) 0x02, (byte) 0x18, (byte) 0xF2, (byte) 0x00, (byte) 0x01,
        (byte) 0x01, (byte) 0xDC, (byte) 0x02};
    //status area #2 id del medidor
    byte[] local13 = {(byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x18,
        (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0xC9, (byte) 0x2F};
    //solicitud de registro de eventos
    byte[] local14 = {(byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0xE4, (byte) 0x30};
    //negociacion a 9600 baud
    byte[] local15 = {(byte) 0x02, (byte) 0x08, (byte) 0x12, (byte) 0xD5, (byte) 0xBA};
    //trama retransmision de ultima trama
    byte[] local16 = {(byte) 0x02, (byte) 0x82, (byte) 0xD7, (byte) 0xA8};
    //trama cerrar puerto 
    byte[] local17 = {(byte) 0x02, (byte) 0x80, (byte) 0xF7, (byte) 0xEA};
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public Vector obtenerTrama(int codigo) {
        tramas = new Vector();
        switch (codigo) {
            case 0:
                tramas.add(getLocal0());
            case 1:
                tramas.add(getLocal1());
            case 2:
                tramas.add(getLocal2());
            case 3:
                tramas.add(getLocal3());
            case 4:
                tramas.add(getLocal4());
            case 5:
                tramas.add(getLocal5());
            case 6:
                tramas.add(getLocal6());
            case 7:
                tramas.add(getLocal7());
            case 8:
                tramas.add(getLocal8());
            case 9:
                tramas.add(getLocal9());
            case 10:
                tramas.add(getLocal10());
            case 11:
                tramas.add(getLocal11());
            case 12:
                tramas.add(getLocal12());
            case 13:
                tramas.add(getLocal13());
            case 14:
                tramas.add(getLocal14());
            case 15:
                tramas.add(getLocal15());
            case 16:
                tramas.add(getLocal16());
            case 17:
                tramas.add(getLocal17());

        }
        return tramas;
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

    public byte[] getLocal0() {
        return local0;
    }

    public void setLocal0(byte[] local0) {
        this.local0 = local0;
    }

    public byte[] getLocal1() {
        return local1;
    }

    public void setLocal1(byte[] local1) {
        this.local1 = local1;
    }

    public byte[] getLocal2() {
        return local2;
    }

    public void setLocal2(byte[] local2) {
        this.local2 = local2;
    }

    public byte[] getLocal3() {
        return local3;
    }

    public void setLocal3(byte[] local3) {
        this.local3 = local3;
    }

    public byte[] getLocal4() {
        return local4;
    }

    public void setLocal4(byte[] local4) {
        this.local4 = local4;
    }

    public byte[] getLocal5() {
        return local5;
    }

    public void setLocal5(byte[] local5) {
        this.local5 = local5;
    }

    public byte[] getLocal6() {
        return local6;
    }

    public void setLocal6(byte[] local6) {
        this.local6 = local6;
    }

    public byte[] getLocal7() {
        return local7;
    }

    public void setLocal7(byte[] local7) {
        this.local7 = local7;
    }

    public byte[] getLocal8() {
        return local8;
    }

    public void setLocal8(byte[] local8) {
        this.local8 = local8;
    }

    public byte[] getLocal9() {
        return local9;
    }

    public void setLocal9(byte[] local9) {
        this.local9 = local9;
    }

    public byte[] getLocal10() {
        return local10;
    }

    public void setLocal10(byte[] local10) {
        this.local10 = local10;
    }

    public byte[] getLocal11() {
        return local11;
    }

    public void setLocal11(byte[] local11) {
        this.local11 = local11;
    }

    public byte[] getLocal12() {
        return local12;
    }

    public void setLocal12(byte[] local12) {
        this.local12 = local12;
    }

    public byte[] getLocal13() {
        return local13;
    }

    public void setLocal13(byte[] local13) {
        this.local13 = local13;
    }

    public byte[] getLocal14() {
        return local14;
    }

    public void setLocal14(byte[] local14) {
        this.local14 = local14;
    }

    public byte[] getLocal15() {
        return local15;
    }

    public void setLocal15(byte[] local15) {
        this.local15 = local15;
    }

    public byte[] getLocal16() {
        return local16;
    }

    public void setLocal16(byte[] local16) {
        this.local16 = local16;
    }

    public byte[] getLocal17() {
        return local17;
    }

    public void setLocal17(byte[] local17) {
        this.local17 = local17;
    }
    
}
