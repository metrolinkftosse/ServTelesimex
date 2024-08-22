/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author mramirez
 */
public class TramasRemotaTCPElgama {
//peticion de configuracion, fecha y hora
    byte[] local0 = {(byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion de configuracion, fecha y hora contador aumentado
    byte[] local1 = {(byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion perfil de carga
    byte[] local2 = {(byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x03, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x22, (byte) 0x22,};
//peticion para completar perfil de carga de 1 dia
    byte[] local3 = {(byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x04, (byte) 0x01, (byte) 0x02, (byte) 0x81, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar eventos de falla de energia
    byte[] local4 = {(byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x05, (byte) 0x01, (byte) 0x0A, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar eventos de falla en las fases
    byte[] local5 = {(byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x06, (byte) 0x01, (byte) 0x0B, (byte) 0x00, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar registro acumulado dia 6, esta peticion se va decrementando hasta pedir el dia 00 que es el actual
    byte[] local6 = { (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x07, (byte) 0x01, (byte) 0x06, (byte) 0x05, (byte) 0x22, (byte) 0x22,};
//peticion para solicitar registro acumulado mes 6, esta peticion se va decrementando hasta pedir el mes 00 que es el actual
    byte[] local7 = { (byte) 0x0D, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x08, (byte) 0x01, (byte) 0x07, (byte) 0x05, (byte) 0x22, (byte) 0x22,};
    // peticion configuracion hora    
    byte[] p1 = {(byte) 0x1F, (byte) 0x47, (byte) 0x37, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x4D, (byte) 0x2F, (byte) 0x08, (byte) 0x22, (byte) 0x3C, (byte) 0x60,
        (byte) 0x4D, (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xFB, (byte) 0x5C, (byte) 0xF5};
// peticion configuracion hora 2
    byte[] p2 = {(byte) 0x1F, (byte) 0x47, (byte) 0x37, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x86, (byte) 0x04, (byte) 0x6F, (byte) 0xF5, (byte) 0x3E, (byte) 0xE9, (byte) 0x19, (byte) 0x3C, (byte) 0x3A, (byte) 0xFA};
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

    public byte[] getP1() {
        return p1;
    }

    public void setP1(byte[] p1) {
        this.p1 = p1;
    }

    public byte[] getP2() {
        return p2;
    }

    public void setP2(byte[] p2) {
        this.p2 = p2;
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
    
    public byte[] encrypt(String random, String Strkey) {
        String copyStrkey = "";
        if (Strkey != null) {
            copyStrkey = Strkey.trim();
        }
        while (copyStrkey.length() < 8) {
            copyStrkey += " ";
        }
        copyStrkey = convertStringToHex(copyStrkey);
        // TODO code application logic here
        //String random = "04 4B B3 06 48 68 12 A8";
        String[] ran = random.split(" ");
        String[] rankey = copyStrkey.split(" ");
        String cadenacompleta = "";
        String cadenakey = "";
        for (int i = 0; i < ran.length; i++) {
            int hex = Integer.parseInt(ran[i], 16);
            String binary = Integer.toBinaryString(hex);
            while (binary.length() != 8) {
                binary = "0" + binary;
            }
            //binary.replaceAll(" ","");
            // System.out.println(binary);
            String[] strArray = binary.trim().split("");
            //System.out.println(strArray.length);
            int key[] = new int[8];
            String cadenabin = "";
            for (int j = 1; j < strArray.length; j++) {
                key[j - 1] = Integer.parseInt(strArray[j]);
                cadenabin += Integer.parseInt(strArray[j]) + " ";
                //System.out.println(strArray[j]);
            }
            cadenacompleta += cadenabin;
        }
        for (int i = 0; i < rankey.length; i++) {
            int hex = Integer.parseInt(rankey[i], 16);
            String binary = Integer.toBinaryString(hex);
            while (binary.length() >= 8) {
                binary = "0" + binary;
            }
            //binary.replaceAll(" ","");
            // System.out.println(binary);
            String[] strArray = binary.trim().split("");
            //System.out.println(strArray.length);
            int key[] = new int[8];
            String cadenabin = "";
            for (int j = 1; j < strArray.length; j++) {
                key[j - 1] = Integer.parseInt(strArray[j]);
                cadenabin += Integer.parseInt(strArray[j]) + " ";
                //System.out.println(strArray[j]);
            }
            cadenakey += cadenabin;
        }
        //System.out.println("");
        // System.out.println(cadenacompleta.trim());
        String[] ini = cadenacompleta.split(" ");
        String[] vkey = cadenakey.split(" ");
        int[] iniint = new int[ini.length];
        int[] keyint = new int[vkey.length];
        for (int k = 0; k < iniint.length; k++) {
            iniint[k] = Integer.parseInt(ini[k]);
        }
        for (int k = 0; k < keyint.length; k++) {
            keyint[k] = Integer.parseInt(vkey[k]);
        }

        byte[] res = desCHINO(iniint, keyint);
        //System.out.println("HEX " + encode(res,res.length));
        return res;
    }
    public byte[] desCHINO(int min[], int key[]) {
//        int key[] = {0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,
//            0, 0, 1, 1, 0, 0, 0, 0,};
        Des d = new Des();
        d.GenSubKey(key);
        d.encrypt(min);

        // System.out.println("");
        //System.out.println("encrypt result");
        String de = "";
        byte[] hex = new byte[8];
        int j = 0;
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                if (i != 0) {
                    // System.out.print("-");
                    //System.out.print(Integer.parseInt(de.trim(), 2));
                    hex[j] = (byte) (Integer.parseInt(Integer.toHexString(Integer.parseInt(de.trim(), 2)), 16) & 0xFF);
                    j++;
                    //System.out.print(" ");
                    de = "";
                    de = de + d.result[i];
                } else {
                    de = de + d.result[i];
                }
            } else {
                de = de + d.result[i];
                //System.out.print(d.result[i]);
            }
        }
        hex[j] = (byte) (Integer.parseInt(Integer.toHexString(Integer.parseInt(de.trim(), 2)), 16) & 0xFF);
        return hex;
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
