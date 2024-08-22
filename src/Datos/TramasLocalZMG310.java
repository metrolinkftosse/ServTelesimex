/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author nicolas
 */
public class TramasLocalZMG310 {

    //Primer trama saludo por optico (TRAMA FIJA)
    byte[] A = {(byte) 0xAF, (byte) 0x3F, (byte) 0x21, (byte) 0x8D, (byte) 0x0A,};
    //Trama C (TRAMA FIJA)
    byte[] C = {(byte) 0x06, (byte) 0xB2, (byte) 0x35, (byte) 0xB2, (byte) 0x8D, (byte) 0x0A,};
    
    byte[] FRMR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x05, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x97, (byte) 0xF9, (byte) 0x5C, (byte) 0x7E};
    //Trama SNRM/UA (TRAMA FIJA) 
    byte[] T_SNRM = {(byte) 0x7E, (byte) 0xA0, (byte) 0x23, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x93, (byte) 0xB8, (byte) 0x4E,
        (byte) 0x81, (byte) 0x80, (byte) 0x14, (byte) 0x05, (byte) 0x02, (byte) 0x05, (byte) 0xDC, (byte) 0x06, (byte) 0x02, (byte) 0x05, (byte) 0xDC, (byte) 0x07,
        (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xA9,
        (byte) 0x0D, (byte) 0x7E};
    //Trama Application Association Request (AARQ)
    byte[] T_AARQ = {(byte) 0x7E, (byte) 0xA0, (byte) 0x4A, (byte) 0x00, (byte) 0x02, (byte) 0x5E, (byte) 0x83, (byte) 0x41, (byte) 0x10, (byte) 0x5B, (byte) 0x11, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0x60, (byte) 0x39, (byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07,
        (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x02, (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0x8B,
        (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0xAC, (byte) 0x0A, (byte) 0x80, (byte) 0x08,
        (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D,
        (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x04, (byte) 0x00, (byte) 0x1C, (byte) 0x1B, (byte) 0x20, (byte) 0x00,
        (byte) 0x00, (byte) 0x42, (byte) 0xB0, (byte) 0x7E};
    byte[] T_AARQ2 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x42, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x61, (byte) 0x10, (byte) 0x48, (byte) 0xE0, (byte) 0xE6, (byte) 0xE6,
        (byte) 0x00, (byte) 0x60, (byte) 0x31, (byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74,
        (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x02, (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0x8B, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05,
        (byte) 0x08, (byte) 0x02, (byte) 0x02, (byte) 0xAC, (byte) 0x02, (byte) 0x80, (byte) 0x00, (byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D, (byte) 0x01, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x04, (byte) 0x00, (byte) 0x1C, (byte) 0x13, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0xE0, (byte) 0xAF, (byte) 0x7E};
    byte[] T_AARQP = {(byte) 0x7E, (byte) 0xA0, (byte) 0x31, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x21, (byte) 0x10, (byte) 0xAC, (byte) 0xC1,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x60, (byte) 0x20, (byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0xA1, (byte) 0x09,
        (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x02, (byte) 0xBE, (byte) 0x0F,
        (byte) 0x04, (byte) 0x0D, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x04, (byte) 0x00, (byte) 0x1C,
        (byte) 0x1B, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0xD0, (byte) 0xE1, (byte) 0x7E};
    byte[] T_negotiation = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1F, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x61, (byte) 0x32, (byte) 0xD8, (byte) 0xBE, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0x06, (byte) 0x01, (byte) 0x02, (byte) 0xFA, (byte) 0x58, (byte) 0x01, (byte) 0x09, (byte) 0x08, (byte) 0x42, (byte) 0x36, (byte) 0x39, (byte) 0x42,
        (byte) 0x35, (byte) 0x46, (byte) 0x38, (byte) 0x33, (byte) 0xC8, (byte) 0xD8, (byte) 0x7E};
    //Trama peticion de firmware 
    byte[] Tfirmware = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x32, (byte) 0xE2, (byte) 0x58, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02,
        (byte) 0xFF, (byte) 0x08, (byte) 0xA0, (byte) 0xE0, (byte) 0x7E};
    //Trama peticion numero de serie medidor 
    byte[] Tserie = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x32, (byte) 0x69, (byte) 0xEB, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05,
        (byte) 0x01, (byte) 0x02, (byte) 0x15, (byte) 0x00, (byte) 0x01, (byte) 0x78, (byte) 0x7E};
    //Trama peticion hora y fecha actual
    byte[] TfechaHora = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x54, (byte) 0x59, (byte) 0xED, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x2B, (byte) 0xC8, (byte) 0xF7, (byte) 0x1E, (byte) 0x7E};
    //Trama peticion periodo de integracion - periodo de captura
    byte[] TperiodoInt = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x76, (byte) 0x49, (byte) 0xEF, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x62, (byte) 0x88, (byte) 0x8D, (byte) 0xCD, (byte) 0x7E};
    //Trama objetos del perfil de carga 
    byte[] TconfPerfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x98, (byte) 0x39, (byte) 0xE1, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x62, (byte) 0x80, (byte) 0xC5, (byte) 0x41, (byte) 0x7E};
    //Trama RR 
    byte[] RR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0xB1, (byte) 0x12, (byte) 0x6E, (byte) 0x7E};
    //Trama Evento powerLost
    byte[] TpowerLost = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0xF2, (byte) 0x65, (byte) 0x2D, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x60, (byte) 0xE8, (byte) 0x3B, (byte) 0x9D, (byte) 0x7E};
    //Trama SCALER_UNIT de los objetos del perfil de carga
    byte[] Tscaler_unit = {(byte) 0x7E, (byte) 0xA0, (byte) 0x35, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0xDA, (byte) 0x79, (byte) 0x7C, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x05, (byte) 0x0C, (byte) 0x02, (byte) 0x6A, (byte) 0xF0, (byte) 0x02, (byte) 0x6B, (byte) 0x88, (byte) 0x02, (byte) 0x6C, (byte) 0x20, (byte) 0x02, (byte) 0x6D, (byte) 0x50, (byte) 0x02, (byte) 0x6D,
        (byte) 0xE8, (byte) 0x02, (byte) 0x6E, (byte) 0x80, (byte) 0x02, (byte) 0x1C, (byte) 0xD8, (byte) 0x02, (byte) 0x1D, (byte) 0x70, (byte) 0x02, (byte) 0x1E, (byte) 0x08, (byte) 0x02, (byte) 0x1E, (byte) 0xA0,
        (byte) 0x02, (byte) 0x01, (byte) 0xB8, (byte) 0x02, (byte) 0x7F, (byte) 0x20, (byte) 0x78, (byte) 0x47, (byte) 0x7E};
    //Trama perfil de carga buffer de datos
    byte[] TperfilCarga = {(byte) 0x7E, (byte) 0xA0, (byte) 0x47, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x04, (byte) 0x62, (byte) 0x78, (byte) 0x01, (byte) 0x02, (byte) 0x04,
        (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
        (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE5,
        (byte) 0x0B, (byte) 0x02, (byte) 0x02, (byte) 0x17, (byte) 0x2D, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x09,
        (byte) 0x0C, (byte) 0x07, (byte) 0xE5, (byte) 0x0B, (byte) 0x03, (byte) 0x03, (byte) 0x0B, (byte) 0x2D, (byte) 0x00, (byte) 0x00, (byte) 0x80,
        (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x8C, (byte) 0x7E, (byte) 0x7E};

    byte[] getRequestAcumulados = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x022, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC1,
        (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00,
        (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};

    byte[] getRequestSerial = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x022, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC1,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00,
        (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};

    byte[] getRequestFechaActual = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x022, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC1,
        (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00,
        (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};

    byte[] getRequestPeriodoInt = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x022, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC1,
        (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x04, (byte) 0x00,
        (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};

    byte[] getRequestConstant = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x022, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC1,
        (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x03, (byte) 0x00,
        (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};

    byte[] getRequestLoadProfile = {(byte) 0x7E, (byte) 0xA0, (byte) 0x4D, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x54, (byte) 0x70, (byte) 0x03,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC1, (byte) 0x00, (byte) 0x07,
        (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, // 01 00 63 01 00 FF
        (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09,
        (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02,
        (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x07, (byte) 0xE6, (byte) 0x01, (byte) 0x14, (byte) 0x04,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xD4, (byte) 0x00, (byte) 0x19, (byte) 0x07,
        (byte) 0xE6, (byte) 0x01, (byte) 0x14, (byte) 0x04, (byte) 0x03, (byte) 0x2D, (byte) 0x00, (byte) 0x00, (byte) 0xFE,
        (byte) 0xD4, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0xCC, (byte) 0x55, (byte) 0x7E};

    byte[] reqNextBlock = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x03, (byte) 0x54, (byte) 0x04, (byte) 0x2B, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x05, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0xA4, (byte) 0x0E, (byte) 0x7E};

    byte[] TconfHora = {(byte) 0x7E, (byte) 0xA0, (byte) 0x23, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x32, (byte) 0x3B, (byte) 0xFA, (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
        (byte) 0x06, (byte) 0x01, (byte) 0x02, (byte) 0x2B, (byte) 0xC8, (byte) 0x01, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE5, (byte) 0x0B, (byte) 0x04, (byte) 0x04, (byte) 0x10, (byte) 0x0F,
        (byte) 0x3B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x4B, (byte) 0x2F, (byte) 0x7E};

    byte[] T_DISC = {(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x00, (byte) 0x02, (byte) 0x1C, (byte) 0xBF, (byte) 0x41, (byte) 0x53, (byte) 0x0E, (byte) 0xAA, (byte) 0x7E};
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

    public byte[] getA() {
        return A;
    }

    public void setA(byte[] A) {
        this.A = A;
    }

    public byte[] getC() {
        return C;
    }

    public void setC(byte[] C) {
        this.C = C;
    }

    public byte[] getFRMR() {
        return FRMR;
    }

    public void setFRMR(byte[] FRMR) {
        this.FRMR = FRMR;
    }        

    public byte[] getRR() {
        return RR;
    }

    public void setRR(byte[] RR) {
        this.RR = RR;
    }

    public byte[] getT_SNRM() {
        return T_SNRM;
    }

    public void setT1(byte[] T_SNRM) {
        this.T_SNRM = T_SNRM;
    }

    public byte[] getT_AARQ() {
        return T_AARQ;
    }

    public byte[] getT_AARQ2() {
        return T_AARQ2;
    }

    public byte[] getT_AARQP() {
        return T_AARQP;
    }

    public byte[] getT_negotiation() {
        return T_negotiation;
    }

    public void setT3(byte[] T_AARQ) {
        this.T_AARQ = T_AARQ;
    }

    public byte[] getTfechaHora(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.TfechaHora[this.TfechaHora.length - 5] = SNCode[0];
            this.TfechaHora[this.TfechaHora.length - 4] = SNCode[1];
        }
        return TfechaHora;
    }

    public void setTfechaHora(byte[] TfechaHora) {
        this.TfechaHora = TfechaHora;
    }

    public byte[] getTfirmware() {
        return Tfirmware;
    }

    public void setTfirmware(byte[] Tfirmware) {
        this.Tfirmware = Tfirmware;
    }

    public byte[] getTserie(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.Tserie[this.Tserie.length - 5] = SNCode[0];
            this.Tserie[this.Tserie.length - 4] = SNCode[1];
        }
        return this.Tserie;
    }

    public void setTserie(byte[] Tserie) {
        this.Tserie = Tserie;
    }

    public byte[] getTperiodoInt(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.TperiodoInt[this.TperiodoInt.length - 5] = SNCode[0];
            this.TperiodoInt[this.TperiodoInt.length - 4] = SNCode[1];
        }
        return this.TperiodoInt;
    }

    public void setTperiodoInt(byte[] TperiodoInt) {
        this.TperiodoInt = TperiodoInt;
    }

    public byte[] getTconfPerfil(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.TconfPerfil[this.TconfPerfil.length - 5] = SNCode[0];
            this.TconfPerfil[this.TconfPerfil.length - 4] = SNCode[1];
        }
        return this.TconfPerfil;
    }

    public void setTconfPerfil(byte[] TconfPerfil) {
        this.TconfPerfil = TconfPerfil;
    }

    public byte[] getTscaler_unit(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.Tscaler_unit[this.Tscaler_unit.length - 5] = SNCode[0];
            this.Tscaler_unit[this.Tscaler_unit.length - 4] = SNCode[1];
        }
        return this.Tscaler_unit;
    }

    public void setTscaler_unit(byte[] Tscaler_unit) {
        this.Tscaler_unit = Tscaler_unit;
    }

    public byte[] getTperfilCarga(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.TperfilCarga[17] = SNCode[0];
            this.TperfilCarga[18] = SNCode[1];
        }
        return this.TperfilCarga;
    }

    public void setTreqNextBlock(byte[] TreqNextBlock) {
        this.reqNextBlock = TreqNextBlock;
    }

    public byte[] getTreqNextBlock(byte[] blockNumber) {
        this.reqNextBlock[this.reqNextBlock.length - 5] = blockNumber[0];
        this.reqNextBlock[this.reqNextBlock.length - 4] = blockNumber[1];
        return this.reqNextBlock;
    }

    public void setTperfilCarga(byte[] TperfilCarga) {
        this.TperfilCarga = TperfilCarga;
    }

    public byte[] getGetRequestAcumulados() {
        return getRequestAcumulados;
    }

    public void setGetRequestAcumulados(byte[] getRequestAcumulados) {
        this.getRequestAcumulados = getRequestAcumulados;
    }

    public byte[] getGetRequestSerial() {
        return getRequestSerial;
    }

    public void setGetRequestSerial(byte[] getRequestSerial) {
        this.getRequestSerial = getRequestSerial;
    }

    public byte[] getGetRequestFechaActual() {
        return getRequestFechaActual;
    }

    public void setGetRequestFechaActual(byte[] getRequestFechaActual) {
        this.getRequestFechaActual = getRequestFechaActual;
    }

    public byte[] getGetRequestPeriodoInt() {
        return getRequestPeriodoInt;
    }

    public void setGetRequestPeriodoInt(byte[] getRequestPeriodoInt) {
        this.getRequestPeriodoInt = getRequestPeriodoInt;
    }

    public byte[] getGetRequestConstant() {
        return getRequestConstant;
    }

    public void setGetRequestConstant(byte[] getRequestConstant) {
        this.getRequestConstant = getRequestConstant;
    }

    public byte[] getGetRequestLoadProfile() {
        return getRequestLoadProfile;
    }

    public void setGetRequestLoadProfile(byte[] getRequestLoadProfile) {
        this.getRequestLoadProfile = getRequestLoadProfile;
    }

    public byte[] getReqNextBlock() {
        return reqNextBlock;
    }

    public void setReqNextBlock(byte[] reqNextBlock) {
        this.reqNextBlock = reqNextBlock;
    }

    
    
    public byte[] getTconfHora() {
        return this.TconfHora;
    }

    public void setTconfHora(byte[] TconfHora) {
        this.TconfHora = TconfHora;
    }

    public byte[] getTpowerLost(byte[] SNCode) {
        if (SNCode.length > 1) {
            this.TpowerLost[this.TpowerLost.length - 5] = SNCode[0];
            this.TpowerLost[this.TpowerLost.length - 4] = SNCode[1];
        }
        return this.TpowerLost;
    }

    public void setTpowerLost(byte[] TpowerLost) {
        this.TpowerLost = TpowerLost;
    }

    public byte[] getT_DISC() {
        return this.T_DISC;
    }

    public void setT_DISC(byte[] T_DISC) {
        this.T_DISC = T_DISC;
    }

    public byte[] StringToHexMarcado(String s) {

        int[] intArray = new int[s.length()];

        String cabecera = "65 84 68 84";//en decimal para que en hex sea "41 54 44 50"
        for (int i = 0; i < s.length(); i++) {
            intArray[i] = Character.digit(s.charAt(i), 10);
            cabecera = cabecera + " " + intArray[i];
            if (i == s.length() - 1) {
                cabecera = cabecera + " " + "0";
            }
        }
//      contador de bytes para identificar donde poner los puntos y el / de la direccion IP
        int cnt = 0;

        String[] aux = cabecera.split(" ");
        byte[] bytee = new byte[aux.length];

        for (int i = 0; i < aux.length; i++) {
            if (i == aux.length - 1) {
                bytee[i] = 0x0D;
            } else {
                if (i == 0 || i == 1 || i == 2 || i == 3) {
                    bytee[i] = (byte) Byte.parseByte(aux[i]);
                } else {

                    bytee[i] = (byte) ((Byte.parseByte(aux[i])) + 0x30);
                }
                if (bytee[i] == 0x2F && cnt != 3) {
                    bytee[i] = 0x2C;
                }
                //System.out.print(Byte.toString(bytee[i]));
//                System.out.println(Integer.toHexString(bytee[i]));

            }
        }

        return bytee;
    }

    public byte[] StringToHexMarcadoPSTN(String s) {
        int[] intArray = new int[s.length()];
        String cabecera = "65 84 68 84";//en decimal para que en hex sea "41 54 44 50"
        for (int i = 0; i < s.length(); i++) {
            intArray[i] = Character.digit(s.charAt(i), 10);
            cabecera = cabecera + " " + intArray[i];
            if (i == s.length() - 1) {
                cabecera = cabecera + " " + "0";
            }
        }
//      contador de bytes para identificar donde poner los puntos y el / de la direccion IP
        int cnt = 0;
        String[] aux = cabecera.split(" ");
        byte[] bytee = new byte[aux.length];
        for (int i = 0; i < aux.length; i++) {
            if (i == aux.length - 1) {
                bytee[i] = 0x0D;
            } else {
                if (i == 0 || i == 1 || i == 2 || i == 3) {
                    bytee[i] = (byte) Byte.parseByte(aux[i]);
                } else {
                    bytee[i] = (byte) ((Byte.parseByte(aux[i])) + 0x30);
                }
                if (bytee[i] == 0x2F && cnt != 3) {
                    bytee[i] = 0x2C;
                }
            }
        }
        return bytee;
    }

    public byte[] StringToHexMarcadoGPRS(String s) {

        int[] intArray = new int[s.length()];

        String cabecera = "65 84 68 80";//en decimal para que en hex sea "41 54 44 50"
        for (int i = 0; i < s.length(); i++) {
            intArray[i] = Character.digit(s.charAt(i), 10);
            cabecera = cabecera + " " + intArray[i];
            if (i == s.length() - 1) {
                cabecera = cabecera + " " + "0";
            }

        }
//      contador de bytes para identificar donde poner los puntos y el / de la direccion IP
        int cnt = 0;

        String[] aux = cabecera.split(" ");
        byte[] bytee = new byte[aux.length];

        for (int i = 0; i < aux.length; i++) {
            if (i == aux.length - 1) {
                bytee[i] = 0x0D;
            } else {
                if (i == 0 || i == 1 || i == 2 || i == 3) {
                    bytee[i] = (byte) Byte.parseByte(aux[i]);
                } else {

                    bytee[i] = (byte) ((Byte.parseByte(aux[i])) + 0x30);
                }
                if (bytee[i] == 0x2F && cnt != 3) {
                    cnt++;
                    bytee[i] = 0x2E;
                } else if (bytee[i] == 29 && cnt == 3) {
                    cnt = 0;
                    bytee[i] = 0x2F;
                }
                //System.out.print(Byte.toString(bytee[i]));
//                System.out.println(Integer.toHexString(bytee[i]));

            }
        }

        return bytee;
    }
}
