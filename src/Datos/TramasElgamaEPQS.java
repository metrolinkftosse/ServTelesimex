/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author Metrolink
 */
public class TramasElgamaEPQS {

    byte modelo[] = {(byte) 0x0E, (byte) 0x48, (byte) 0x59, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x80, (byte) 0x0A, (byte) 0x00, (byte) 0xCB, (byte) 0x0A};
    byte fechaactual[] = {(byte) 0x0E, (byte) 0x48, (byte) 0x59, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0xCD, (byte) 0x87};
    byte perfilCarga[] = {(byte) 0x0E, (byte) 0x48, (byte) 0x59, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x18, (byte) 0x05, (byte) 0x00, (byte) 0x4F, (byte) 0x15};
    byte registrosmes[] = {(byte) 0x0E, (byte) 0x48, (byte) 0x59, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x21, (byte) 0x00, (byte) 0x10, (byte) 0x43, (byte) 0xCD};
    byte registrosdia[] = {(byte) 0x0E, (byte) 0x48, (byte) 0x59, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x31, (byte) 0x00, (byte) 0x10, (byte) 0x1B, (byte) 0xCC};
    byte numeventos[] = {(byte) 0x0E, (byte) 0x59, (byte) 0x36, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x84, (byte) 0x41};
    byte eventos[] = {(byte) 0x0E, (byte) 0x59, (byte) 0x36, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x84, (byte) 0x41};
    byte confhora1[] = {(byte) 0x1B, (byte) 0x47, (byte) 0x37, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x4D, (byte) 0x2F, (byte) 0x08, (byte) 0x22, (byte) 0x3C, (byte) 0x60, (byte) 0x4D, (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xFB, (byte) 0x5C, (byte) 0xF5};
    byte confhora2[] = {(byte) 0x1B, (byte) 0x47, (byte) 0x37, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x86, (byte) 0x04, (byte) 0x6F, (byte) 0xF5, (byte) 0x3E, (byte) 0xE9, (byte) 0x19, (byte) 0x3C, (byte) 0x3A, (byte) 0xFA};
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

    public byte[] getModelo() {
        return modelo;
    }

    public void setModelo(byte[] modelo) {
        this.modelo = modelo;
    }

    public byte[] getFechaactual() {
        return fechaactual;
    }

    public void setFechaactual(byte[] fechaactual) {
        this.fechaactual = fechaactual;
    }

    public byte[] getPerfilCarga() {
        return perfilCarga;
    }

    public void setPerfilCarga(byte[] perfilCarga) {
        this.perfilCarga = perfilCarga;
    }

    public byte[] getRegistrosmes() {
        return registrosmes;
    }

    public void setRegistrosmes(byte[] registrosmes) {
        this.registrosmes = registrosmes;
    }

    public byte[] getRegistrosdia() {
        return registrosdia;
    }

    public void setRegistrosdia(byte[] registrosdia) {
        this.registrosdia = registrosdia;
    }

    public byte[] getEventos() {
        return eventos;
    }

    public void setEventos(byte[] eventos) {
        this.eventos = eventos;
    }

    public byte[] getConfhora1() {
        return confhora1;
    }

    public void setConfhora1(byte[] confhora1) {
        this.confhora1 = confhora1;
    }

    public byte[] getConfhora2() {
        return confhora2;
    }

    public void setConfhora2(byte[] confhora2) {
        this.confhora2 = confhora2;
    }

    public byte[] getNumeventos() {
        return numeventos;
    }

    public void setNumeventos(byte[] numeventos) {
        this.numeventos = numeventos;
    }
    public byte[] encrypt(String random, String [] Strkey) {
        //String copyStrkey = "";
        
        //copyStrkey = Strkey;
        // TODO code application logic here
        //String random = "04 4B B3 06 48 68 12 A8";
        String[] ran = random.split(" ");
        String[] rankey = Strkey;
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
}
