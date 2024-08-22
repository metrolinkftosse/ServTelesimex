/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author dperez
 */
public class TramasRemotaPSTNABB_1 {

    String data;
    String resp;
    byte [] inicial = {(byte) 0x49};

    //Quien es ud?
    byte[] local0 = {(byte) 0x02, (byte) 0x18, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x99, (byte) 0x9F};

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
    //Trama configuracion de perfil de carga
    byte[] local18 = {(byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x00,(byte) 0x2A,(byte) 0x00,(byte) 0x00,(byte) 0x0E,(byte) 0x48,(byte) 0x2A};
     //configuracion del firmware
    byte[] local19 = {(byte) 0x02,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x40,(byte) 0x00,(byte) 0x00,(byte) 0x08,(byte) 0x19,(byte) 0x95};
    //clase 07: Metering Function Block #2
    byte [] local20 = {(byte) 0x02,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x07,(byte) 0x86,(byte) 0xE6};
    //constantes de medicion primarias
    byte [] local21 = {(byte) 0x02,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0xF6,(byte) 0x01};
    //fecha actual del medidor
    byte [] local22 = {(byte) 0x02,(byte) 0x05,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x09,(byte) 0x67,(byte) 0x28};
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public TramasRemotaPSTNABB_1() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
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

    public    byte[] StringToHexMarcado(String s) {

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

    public byte[] getInicial() {
        return inicial;
    }

    public void setInicial(byte[] inicial) {
        this.inicial = inicial;
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

    public byte[] getLocal18() {
        return local18;
    }

    public void setLocal18(byte[] local18) {
        this.local18 = local18;
    }

    public byte[] getLocal19() {
        return local19;
    }

    public void setLocal19(byte[] local19) {
        this.local19 = local19;
    }

    public byte[] getLocal20() {
        return local20;
    }

    public void setLocal20(byte[] local20) {
        this.local20 = local20;
    }

    public byte[] getLocal21() {
        return local21;
    }

    public void setLocal21(byte[] local21) {
        this.local21 = local21;
    }

    public byte[] getLocal22() {
        return local22;
    }

    public void setLocal22(byte[] local22) {
        this.local22 = local22;
    }


}
