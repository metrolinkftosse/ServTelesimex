/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author dperez
 */
public class TramasLandysZMD {
    //handshake 1

    //Primer trama saludo por optico (TRAMA FIJA)
    byte[] A = {(byte) 0x2F, (byte) 0x3F, (byte) 0x21, (byte) 0x0D, (byte) 0x0A,};
    //Trama C (TRAMA FIJA)
    byte[] C = {(byte) 0x06, (byte) 0x32, (byte) 0x35, (byte) 0x32, (byte) 0x0D, (byte) 0x0A,};
    //Trama SNRM/UA (TRAMA FIJA) CAMBIA PARA LECTURA DE EVENTOS
    byte[] T1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x07, (byte) 0x41, (byte) 0x03, (byte) 0x93, (byte) 0x0B, (byte) 0x14, (byte) 0x7E};
    //Trama Application Association Request (AARQ)(puede cambiar contrasena medidor, FCS, en vatia la contrasena es de A hasta H)
    byte[] T3 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x47, (byte) 0x03, (byte) 0x41, (byte) 0x10, (byte) 0x7E, (byte) 0xC4, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0x60, (byte) 0x39, (byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x02, (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80,
        (byte) 0x8B, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0xAC, (byte) 0x0A, (byte) 0x80, (byte) 0x08, (byte) 0x30, (byte) 0x30, (byte) 0x30,
        (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x04,
        (byte) 0x00, (byte) 0x18, (byte) 0x02, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0xA7, (byte) 0x7E};
    //trama peticion de tablas de contenido
    byte[] TableContent = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x32, (byte) 0xE2, (byte) 0x58, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0xFA, (byte) 0x08,
        (byte) 0x18, (byte) 0x9E, (byte) 0x7E};
    //Trama RR (cambia el byte 9 o byte de control aqui se deja en 00, tambien cambia el HCS, aqui se deja en cero)
    byte[] RR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x07, (byte) 0x03, (byte) 0x41, (byte) 0x11, (byte) 0x40, (byte) 0xC3, (byte) 0x7E};
    //Trama peticion de firmware (cambia: control, HCS, FCS)
    byte[] Tfirmware = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x32, (byte) 0xE2, (byte) 0x58, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0xFF, (byte) 0x08,
        (byte) 0xA0, (byte) 0xE0, (byte) 0x7E};
    //Trama peticion numero de serie medidor (cambia: control, HCS, FCS)
    byte[] Tserie = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x54, (byte) 0xD2, (byte) 0x5E, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x40, (byte) 0xE0,
        (byte) 0x40, (byte) 0x32, (byte) 0x7E};
    //Trama peticion hora y fecha actual
    byte[] TfechaHora = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x10, (byte) 0xF2, (byte) 0x5A, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x2B, (byte) 0xC8,
        (byte) 0xF7, (byte) 0x1E, (byte) 0x7E};
    //Trama parametros perfil de carga1 ((cambia: control, HCS, FCS))
    byte[] Tparametrosperfil1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x41, (byte) 0x03, (byte) 0x74, (byte) 0x9F, (byte) 0xD7, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x62, (byte) 0x88,
        (byte) 0xAE, (byte) 0x6B, (byte) 0x7E};
    //Trama informacion del perfil de carga
    byte[] TinfoPerfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x76, (byte) 0xC2, (byte) 0x5C, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x62, (byte) 0x80,
        (byte) 0xC5, (byte) 0x41, (byte) 0x7E};
    byte[] Tconstants = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14, (byte) 0x03, (byte) 0x41, (byte) 0xB8, (byte) 0xE7, (byte) 0x1D, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x02, (byte) 0x02, (byte) 0x74, (byte) 0x58,
        (byte) 0x02, (byte) 0x74, (byte) 0x60, (byte) 0x59, (byte) 0xD1, (byte) 0x7E};
    //Trama Fecha y hora actual CASO 2 despues del perfil, depende del firmware ((cambia: control, HCS, FCS))
    byte[] TfechaHora2 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x03, (byte) 0x41, (byte) 0xFC, (byte) 0xFF, (byte) 0x77, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xD5, (byte) 0xE4, (byte) 0x7E};
    //Trama peticion perfil de carga1 (cambia: control, HCS, FCS)
    byte[] Tperfil1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x44, (byte) 0x03, (byte) 0x41, (byte) 0x10, (byte) 0xB3, (byte) 0xE1, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x04, (byte) 0x62, (byte) 0x78, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE0, (byte) 0x04, (byte) 0x05, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE0, (byte) 0x05, (byte) 0x05, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x07, (byte) 0x9D, (byte) 0x7E};
    //Trama siguiente bloque perfil de carga (cambia: control, numero de bloque, HCS, FCS)
    byte[] TbloquePerfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x16, (byte) 0x03, (byte) 0x41, (byte) 0xB0, (byte) 0xC0, (byte) 0x16, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x02,
        (byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x73, (byte) 0x7F, (byte) 0x7E};
    byte[] Tfinaliza = {(byte) 0x7E, (byte) 0xA0, (byte) 0x07, (byte) 0x03, (byte) 0x41, (byte) 0x53, (byte) 0x06, (byte) 0xC7, (byte) 0x7E};
     byte[] PowerFailureElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x11, (byte) 0x03, (byte) 0x41, (byte) 0x32, (byte) 0xE2, (byte) 0x58, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x05, (byte) 0x01,
        (byte) 0x02, (byte) 0x60, (byte) 0xE8, (byte) 0x3B, (byte) 0x9D, (byte) 0x7E};
     
     byte [] confhora= {(byte)0x7E, (byte) 0xA0, (byte) 0x20, (byte) 0x03, (byte) 0x61, (byte) 0x98, (byte) 0xC8, (byte) 0x21, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x06, (byte) 0x01, (byte) 0x02, (byte) 0x2B, (byte) 0xC8, (byte) 0x01, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE0, (byte) 0x07, (byte) 0x0E, (byte) 0x04, (byte) 0x08, (byte) 0x37, (byte) 0x38, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x87, (byte) 0x85, (byte) 0x7E};
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
//        if (dmls == 0) {
//            return C_LN;
//        }
        return C;
    }

    public void setC(byte[] C) {
        this.C = C;
    }

    public byte[] getRR() {

        return RR;
    }

    public void setRR(byte[] RR) {
        this.RR = RR;
    }

    public byte[] getT1() {
        return T1;
    }

    public void setT1(byte[] T1) {
        this.T1 = T1;
    }

    public byte[] getT3() {
        return T3;
    }

    public byte[] getTableContent() {
        return TableContent;
    }

    public void setTableContent(byte[] TableContent) {
        this.TableContent = TableContent;
    }

    public void setT3(byte[] T3) {
        this.T3 = T3;
    }

    public byte[] getTbloquePerfil() {
        return TbloquePerfil;
    }

    public void setTbloquePerfil(byte[] TbloquePerfil) {
        this.TbloquePerfil = TbloquePerfil;
    }

    public byte[] getTfechaHora() {
        return TfechaHora;
    }

    public void setTfechaHora(byte[] TfechaHora) {
        this.TfechaHora = TfechaHora;
    }

    public byte[] getTfechaHora2() {
        return TfechaHora2;
    }

    public void setTfechaHora2(byte[] TfechaHora2) {
        this.TfechaHora2 = TfechaHora2;
    }

    public byte[] getTfirmware() {
        return Tfirmware;
    }

    public void setTfirmware(byte[] Tfirmware) {
        this.Tfirmware = Tfirmware;
    }

    public byte[] getTinfoPerfil() {

        return TinfoPerfil;
    }

    public void setTinfoPerfil(byte[] TinfoPerfil) {
        this.TinfoPerfil = TinfoPerfil;
    }

    public byte[] getTparametrosperfil1() {

        return Tparametrosperfil1;
    }

    public void setTparametrosperfil1(byte[] Tparametrosperfil1) {
        this.Tparametrosperfil1 = Tparametrosperfil1;
    }

    public byte[] getTconstants() {
        return Tconstants;
    }

    public void setTconstants(byte[] Tconstants) {
        this.Tconstants = Tconstants;
    }

    public byte[] getTperfil1() {

        return Tperfil1;
    }

    public void setTperfil1(byte[] Tperfil1) {
        this.Tperfil1 = Tperfil1;
    }

    public byte[] getTserie() {

        return Tserie;
    }

    public void setTserie(byte[] Tserie) {
        this.Tserie = Tserie;
    }

    public byte[] getTfinaliza() {
        return Tfinaliza;
    }

    public void setTfinaliza(byte[] Tfinaliza) {
        this.Tfinaliza = Tfinaliza;
    }

    public byte[] getPowerFailureElements() {
        return PowerFailureElements;
    }

    public void setPowerFailureElements(byte[] PowerFailureElements) {
        this.PowerFailureElements = PowerFailureElements;
    }

    public byte[] getConfhora() {
        return confhora;
    }

    public void setConfhora(byte[] confhora) {
        this.confhora = confhora;
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
