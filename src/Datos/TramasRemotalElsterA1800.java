/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author dperez
 */
public class TramasRemotalElsterA1800 {

    byte[] Icommand = {(byte) 0x49};
    //se debe cambiar el 2byte en caso de no recibir respuesta
    byte[] Identidad = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x20, (byte) 0x38, (byte) 0x14};
    byte[] velocidad = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x60, (byte) 0x00, (byte) 0x80, (byte) 0xFF, (byte) 0xF6, (byte) 0x71};
    byte[] logon = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D, (byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xB1, (byte) 0xEB};
    byte[] timing = {(byte) 0xEE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x71, (byte) 0x1E, (byte) 0x03, (byte) 0x08, (byte) 0x05, (byte) 0x00, (byte) 0xEE};
    byte[] Autenticacion = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x53, (byte) 0x09, (byte) 0x00, (byte) 0x87, (byte) 0x07, (byte) 0x2A, (byte) 0x53, (byte) 0xDA, (byte) 0x3B, (byte) 0x16,
        (byte) 0xE9, (byte) 0x5A, (byte) 0x70};
    byte[] ST06 = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x06, (byte) 0x55, (byte) 0xF8};
    byte[] ST05 = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x05, (byte) 0x4D, (byte) 0xA9};
    byte[] ST55FechaAct = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x37, (byte) 0x5F, (byte) 0xD8};
    byte[] ST61ConfiPerfil = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x3D, (byte) 0x86, (byte) 0x14};
    byte[] ST53TimeOffset = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x35, (byte) 0xCE, (byte) 0x98};
    byte[] ST62LpCtrl = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x3E, (byte) 0x9E, (byte) 0x45};
    byte[] MT17 = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x08, (byte) 0x11, (byte) 0x28, (byte) 0x31};
    byte[] ACK = {(byte) 0x06};
    byte[] ST15Constantes = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x0F, (byte) 0x94, (byte) 0x65};
    byte[] MT015PrimMeteringInfo = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x08, (byte) 0x0F, (byte) 0xD7, (byte) 0xC8};
    byte[] MT16MeteringInfo = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x08, (byte) 0x10, (byte) 0x22, (byte) 0x43};
    byte[] ST63LpStatus = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x3F, (byte) 0x94, (byte) 0x37};
    byte[] ST76Eventos = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x4C, (byte) 0x0B, (byte) 0x15};
    byte[] MT64Perfil = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x3F, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x03, (byte) 0x2A, (byte) 0x00, (byte) 0x87, (byte) 0x6D, (byte) 0x82};
    byte[] logoff = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x52, (byte) 0xAD, (byte) 0x44};
    //peticion ST_00
    byte[] ST00 ={ (byte) 0xEE,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x03,(byte) 0x30,(byte) 0x00,(byte) 0x00,(byte) 0xDC,(byte) 0x1C};
    //Peticion ST_021_ACT_REGS
    byte[] ST021 = {(byte) 0xEE, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x15, (byte) 0x73, (byte) 0x38,};
    //Peticion ST_022_DATA_SELECTION
    byte[] ST022 = {(byte) 0xEE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x16, (byte) 0x6B, (byte) 0x69,};
    //Peticion ST_023_CURRENT_REG_DATA
    byte[] ST023 = {(byte) 0xEE, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x17, (byte) 0x61, (byte) 0x1B,};
    //Peticion ST_027_PRESENT_REGISTER_SELECT
    byte[] ST027 = {(byte) 0xEE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x1B, (byte) 0x8E, (byte) 0xB2,};
    //Peticion ST_028_PRESENT_REGISTER_DATA
    byte[] ST028 = {(byte) 0xEE, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x1C, (byte) 0xB2, (byte) 0xA5,};
    //key para DES
    byte[] k = {(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30};
    
    byte[] ST007 = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x40,
        (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x0B, (byte) 0x0A, (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x0B, (byte) 0x1E, (byte) 0x26, (byte) 0x00, (byte) 0xA3, (byte) 0xCA, (byte) 0x78};
    byte[] ST008 ={(byte) 0xEE	, (byte) 0x00	, (byte) 0x00	, (byte) 0x00	, (byte) 0x00	, (byte) 0x03	, (byte) 0x30	, (byte) 0x00	, (byte) 0x08	, (byte) 0x94	, (byte) 0x90};
    
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};
    public TramasRemotalElsterA1800() {
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

    public byte[] getACK() {
        return ACK;
    }

    public void setACK(byte[] ACK) {
        this.ACK = ACK;
    }

    public byte[] getAutenticacion() {
        return Autenticacion;
    }

    public void setAutenticacion(byte[] Autenticacion) {
        this.Autenticacion = Autenticacion;
    }

    public byte[] getIcommand() {
        return Icommand;
    }

    public void setIcommand(byte[] Icommand) {
        this.Icommand = Icommand;
    }

    public byte[] getIdentidad() {
        return Identidad;
    }

    public void setIdentidad(byte[] Identidad) {
        this.Identidad = Identidad;
    }

    public byte[] getMT015PrimMeteringInfo() {
        return MT015PrimMeteringInfo;
    }

    public void setMT015PrimMeteringInfo(byte[] MT015PrimMeteringInfo) {
        this.MT015PrimMeteringInfo = MT015PrimMeteringInfo;
    }

    public byte[] getMT16MeteringInfo() {
        return MT16MeteringInfo;
    }

    public void setMT16MeteringInfo(byte[] MT16MeteringInfo) {
        this.MT16MeteringInfo = MT16MeteringInfo;
    }

    public byte[] getMT17() {
        return MT17;
    }

    public void setMT17(byte[] MT17) {
        this.MT17 = MT17;
    }

    public byte[] getMT64Perfil() {
        return MT64Perfil;
    }

    public void setMT64Perfil(byte[] MT64Perfil) {
        this.MT64Perfil = MT64Perfil;
    }

    public byte[] getST05() {
        return ST05;
    }

    public void setST05(byte[] ST05) {
        this.ST05 = ST05;
    }

    public byte[] getST06() {
        return ST06;
    }

    public void setST06(byte[] ST06) {
        this.ST06 = ST06;
    }

    public byte[] getST15Constantes() {
        return ST15Constantes;
    }

    public void setST15Constantes(byte[] ST15Constantes) {
        this.ST15Constantes = ST15Constantes;
    }

    public byte[] getST53TimeOffset() {
        return ST53TimeOffset;
    }

    public void setST53TimeOffset(byte[] ST53TimeOffset) {
        this.ST53TimeOffset = ST53TimeOffset;
    }

    public byte[] getST55FechaAct() {
        return ST55FechaAct;
    }

    public void setST55FechaAct(byte[] ST55FechaAct) {
        this.ST55FechaAct = ST55FechaAct;
    }

    public byte[] getST61ConfiPerfil() {
        return ST61ConfiPerfil;
    }

    public void setST61ConfiPerfil(byte[] ST61ConfiPerfil) {
        this.ST61ConfiPerfil = ST61ConfiPerfil;
    }

    public byte[] getST62LpCtrl() {
        return ST62LpCtrl;
    }

    public void setST62LpCtrl(byte[] ST62LpCtrl) {
        this.ST62LpCtrl = ST62LpCtrl;
    }

    public byte[] getST63LpStatus() {
        return ST63LpStatus;
    }

    public void setST63LpStatus(byte[] ST63LpStatus) {
        this.ST63LpStatus = ST63LpStatus;
    }

    public byte[] getST76Eventos() {
        return ST76Eventos;
    }

    public void setST76Eventos(byte[] ST76Eventos) {
        this.ST76Eventos = ST76Eventos;
    }

    public byte[] getLogoff() {
        return logoff;
    }

    public void setLogoff(byte[] logoff) {
        this.logoff = logoff;
    }

    public byte[] getLogon() {
        return logon;
    }

    public void setLogon(byte[] logon) {
        this.logon = logon;
    }

    public byte[] getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(byte[] velocidad) {
        this.velocidad = velocidad;
    }

    public byte[] getK() {
        return k;
    }

    public void setK(byte[] k) {
        this.k = k;
    }

    public byte[] getTiming() {
        return timing;
    }

    public void setTiming(byte[] timing) {
        this.timing = timing;
    }

    public byte[] getST00() {
        return ST00;
    }

    public void setST00(byte[] ST00) {
        this.ST00 = ST00;
    }
    
    public byte[] getST021() {
        return ST021;
    }

    public void setST021(byte[] ST021) {
        this.ST021 = ST021;
    }

    public byte[] getST022() {
        return ST022;
    }

    public void setST022(byte[] ST022) {
        this.ST022 = ST022;
    }

    public byte[] getST023() {
        return ST023;
    }

    public void setST023(byte[] ST023) {
        this.ST023 = ST023;
    }

    public byte[] getST027() {
        return ST027;
    }

    public void setST027(byte[] ST027) {
        this.ST027 = ST027;
    }

    public byte[] getST028() {
        return ST028;
    }

    public void setST028(byte[] ST028) {
        this.ST028 = ST028;
    }

    public byte[] getST007() {
        return ST007;
    }

    public void setST007(byte[] ST007) {
        this.ST007 = ST007;
    }
    
    public byte[] getST008() {
        return ST008;
    }

    public void setST008(byte[] ST008) {
        this.ST008 = ST008;
    }
    

//   public byte[] encrypt(String random, String Strkey) {
//        String copyStrkey = "";
//        if (Strkey != null) {
//            copyStrkey = Strkey.trim();
//        }
//        while (copyStrkey.length() < 8) {
//            copyStrkey += " ";
//        }
//        copyStrkey = convertStringToHex(copyStrkey);
//        // TODO code application logic here
//        //String random = "04 4B B3 06 48 68 12 A8";
//        String[] ran = random.split(" ");
//        String[] rankey = copyStrkey.split(" ");
//        String cadenacompleta = "";
//        String cadenakey = "";
//        for (int i = 0; i < ran.length; i++) {
//            int hex = Integer.parseInt(ran[i], 16);
//            String binary = Integer.toBinaryString(hex);
//            while (binary.length() != 8) {
//                binary = "0" + binary;
//            }
//            //binary.replaceAll(" ","");
//            // System.out.println(binary);
//            String[] strArray = binary.trim().split("");
//            //System.out.println(strArray.length);
//            int key[] = new int[8];
//            String cadenabin = "";
//            for (int j = 1; j < strArray.length; j++) {
//                key[j - 1] = Integer.parseInt(strArray[j]);
//                cadenabin += Integer.parseInt(strArray[j]) + " ";
//                //System.out.println(strArray[j]);
//            }
//            cadenacompleta += cadenabin;
//        }
//        for (int i = 0; i < rankey.length; i++) {
//            int hex = Integer.parseInt(rankey[i], 16);
//            String binary = Integer.toBinaryString(hex);
//            while (binary.length() != 8) {
//                binary = "0" + binary;
//            }
//            //binary.replaceAll(" ","");
//            // System.out.println(binary);
//            String[] strArray = binary.trim().split("");
//            //System.out.println(strArray.length);
//            int key[] = new int[8];
//            String cadenabin = "";
//            for (int j = 1; j < strArray.length; j++) {
//                key[j - 1] = Integer.parseInt(strArray[j]);
//                cadenabin += Integer.parseInt(strArray[j]) + " ";
//                //System.out.println(strArray[j]);
//            }
//            cadenakey += cadenabin;
//        }
//        //System.out.println("");
//        // System.out.println(cadenacompleta.trim());
//        String[] ini = cadenacompleta.split(" ");
//        String[] vkey = cadenakey.split(" ");
//        int[] iniint = new int[ini.length];
//        int[] keyint = new int[vkey.length];
//        for (int k = 0; k < iniint.length; k++) {
//            iniint[k] = Integer.parseInt(ini[k]);
//        }
//        for (int k = 0; k < keyint.length; k++) {
//            keyint[k] = Integer.parseInt(vkey[k]);
//        }
//
//        byte[] res = desCHINO(iniint, keyint);
//        //System.out.println("HEX " + encode(res,res.length));
//        return res;
//    }
//
//    public byte[] desCHINO(int min[], int key[]) {
////        int key[] = {0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,
////            0, 0, 1, 1, 0, 0, 0, 0,};
//        Des d = new Des();
//        d.GenSubKey(key);
//        d.encrypt(min);
//
//        // System.out.println("");
//        //System.out.println("encrypt result");
//        String de = "";
//        byte[] hex = new byte[8];
//        int j = 0;
//        for (int i = 0; i < 64; i++) {
//            if (i % 8 == 0) {
//                if (i != 0) {
//                    // System.out.print("-");
//                    //System.out.print(Integer.parseInt(de.trim(), 2));
//                    hex[j] = (byte) (Integer.parseInt(Integer.toHexString(Integer.parseInt(de.trim(), 2)), 16) & 0xFF);
//                    j++;
//                    //System.out.print(" ");
//                    de = "";
//                    de = de + d.result[i];
//                } else {
//                    de = de + d.result[i];
//                }
//            } else {
//                de = de + d.result[i];
//                //System.out.print(d.result[i]);
//            }
//        }
//        hex[j] = (byte) (Integer.parseInt(Integer.toHexString(Integer.parseInt(de.trim(), 2)), 16) & 0xFF);
//        return hex;
//
//    }
    
    public static byte[] encrypt(String random, String Strkey) {
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
//        System.out.println(random + ", random L: " + random.length());
        String[] ran = random.split(" ");
        String[] rankey = copyStrkey.split(" ");
        String cadenacompleta = "";
        String cadenakey = "";
//        System.out.println(ran + ", ran L: " + ran.length);
        
        for (int i = 0; i < ran.length; i++) {
            int hex = Integer.parseInt(ran[i], 16);
//            System.out.println(hex + ", valor en la pos " + i + " de ran");
            String binary = Integer.toBinaryString(hex);
            while (binary.length() != 8) {
                binary = "0" + binary;
            }
            //binary.replaceAll(" ","");
//             System.out.println(binary + ",hex en binario, binary L: " + binary.length());
            String[] strArray = binary.trim().split("");
            //System.out.println(strArray.length);
//            int key[] = new int[8];
            String cadenabin = "";
            for (int j = 0; j < strArray.length; j++) {
//                key[j - 1] = Integer.parseInt(strArray[j]);
                if(!strArray[j].equals("")){
                    cadenabin += Integer.parseInt(strArray[j]) + " ";
                }
                //System.out.println(strArray[j]);
            }
            
            cadenacompleta += cadenabin;
//            System.out.println(strArray + ",binary en string[], strArray L: " +strArray.length);
//            System.out.println(cadenabin + ",strArray en string, cadenabin L: " +cadenabin.length());
//            System.out.println(cadenacompleta + ",cadenabin concatenado, cadenacompleta L: " +cadenacompleta.length());
        }
        for (int i = 0; i < rankey.length; i++) {
            int hex = Integer.parseInt(rankey[i], 16);
            String binary = Integer.toBinaryString(hex);
            while (binary.length() != 8) {
                binary = "0" + binary;
            }
            //binary.replaceAll(" ","");
//             System.out.println(binary);
            String[] strArray = binary.trim().split("");
//            System.out.println(strArray.length);
//            int key[] = new int[8];
            String cadenabin = "";
            for (int j = 0; j < strArray.length; j++) {
//                key[j - 1] = Integer.parseInt(strArray[j]);
                if(!strArray[j].equals("")){
                    cadenabin += Integer.parseInt(strArray[j]) + " ";
                }
                //System.out.println(strArray[j]);
            }
            cadenakey += cadenabin;
        }
        //System.out.println("");
//         System.out.println("cadecom sin espacios L = ini L:" + cadenacompleta.split(" ").length);
        String[] ini = cadenacompleta.split(" ");
        String[] vkey = cadenakey.split(" ");
//        System.out.println("ini L:" + ini.length);
        int[] iniint = new int[ini.length];
        int[] keyint = new int[vkey.length];
        for (int k = 0; k < iniint.length; k++) {
            iniint[k] = Integer.parseInt(ini[k]);
        }
        for (int k = 0; k < keyint.length; k++) {
            keyint[k] = Integer.parseInt(vkey[k]);
        }
//        System.out.println("iniint L:" + iniint.length);
        byte[] res = desCHINO(iniint, keyint);
        //System.out.println("HEX " + encode(res,res.length));
        return res;
    }

    public static byte[] desCHINO(int min[], int key[]) {
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

    
    
    public static String convertStringToHex(String str) {
        //System.out.println("cadena a transformar " + str);
        char[] chars = str.toCharArray();
        String hex = "";
        for (char ch : str.toCharArray()) {
            hex += Integer.toHexString(ch) + " ";
        }


        return hex.trim().toUpperCase();
    }

   
}
