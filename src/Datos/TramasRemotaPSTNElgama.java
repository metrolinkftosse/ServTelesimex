/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author mramirez
 */
public class TramasRemotaPSTNElgama {
//peticion de configuracion, fecha y hora
    byte[] local0 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion de configuracion, fecha y hora contador aumentado
    byte[] local1 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion perfil de carga
    byte[] local2 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x03, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x22, (byte) 0x22,};
//peticion para completar perfil de carga de 1 dia
    byte[] local3 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x04, (byte) 0x01, (byte) 0x02, (byte) 0x81, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar eventos de falla de energia
    byte[] local4 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x05, (byte) 0x01, (byte) 0x0A, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar eventos de falla en las fases
    byte[] local5 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x06, (byte) 0x01, (byte) 0x0B, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar registro acumulado dia 6, esta peticion se va decrementando hasta pedir el dia 00 que es el actual
    byte[] local6 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x07, (byte) 0x01, (byte) 0x06, (byte) 0x05, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar registro acumulado mes 6, esta peticion se va decrementando hasta pedir el mes 00 que es el actual
    byte[] local7 = {(byte) 0x2F, (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x08, (byte) 0x01, (byte) 0x07, (byte) 0x05, (byte) 0x22, (byte) 0x22,};
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

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
