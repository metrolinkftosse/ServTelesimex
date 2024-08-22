/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author Metrolink
 */
public class TramasRemotaIskraMT174 {
    //identificacion

    private byte[] identificacion = {(byte) 0x2F, (byte) 0x3F, (byte) 0x21, (byte) 0x0D, (byte) 0x0A};
    private byte[] identificacion2 = {(byte) 0x2F, (byte) 0x3F, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
        (byte) 0x21, (byte) 0x0D, (byte) 0x0A};
    //comunicacion 
    private byte[] comunicacion = {(byte) 0x06, (byte) 0x30, (byte) 0x35, (byte) 0x31, (byte) 0x0D, (byte) 0x0A};
    private byte[] password = {(byte) 0x01, (byte) 0x50, (byte) 0x31, (byte) 0x02, (byte) 0x28, (byte) 0x30, (byte) 0x30,
        (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x29, (byte) 0x03, (byte) 0x61};
    private byte[] seriemedidor = {(byte) 0x01, (byte) 0x52, (byte) 0x31, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x30,
        (byte) 0x2E, (byte) 0x30, (byte) 0x28, (byte) 0x29, (byte) 0x03, (byte) 0x53};
    private byte[] fechaactual = {(byte) 0x01, (byte) 0x52, (byte) 0x31, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x39, (byte) 0x2E, (byte) 0x32, (byte) 0x28, (byte) 0x29, (byte) 0x03, (byte) 0x58};
    private byte[] fechatime = {(byte) 0x01, (byte) 0x52, (byte) 0x31, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x39, (byte) 0x2E, (byte) 0x31, (byte) 0x28, (byte) 0x29, (byte) 0x03, (byte) 0xDB};
//    private byte[] perfil = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x30, (byte) 0x31, (byte) 0x28, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] perfil = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x30, (byte) 0x31, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] perfilfinal = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x30, (byte) 0x31, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    //private byte[] eventos = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x39, (byte) 0x38, (byte) 0x28, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] eventos = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x39, (byte) 0x38, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] eventosfinal = {(byte) 0x01, (byte) 0x52, (byte) 0x35, (byte) 0x02, (byte) 0x50, (byte) 0x2E, (byte) 0x39, (byte) 0x38, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34, (byte) 0x31, (byte) 0x32, (byte) 0x31, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x3B, (byte) 0x29, (byte) 0x03, (byte) 0x23};
    private byte[] sincreloj = {(byte) 0x01, (byte) 0x57, (byte) 0x31, (byte) 0x02, (byte) 0x30, (byte) 0x2E, (byte) 0x39, (byte) 0x2E, (byte) 0x34, (byte) 0x28, (byte) 0x30, (byte) 0x31, (byte) 0x34,
        (byte) 0x31, (byte) 0x31, (byte) 0x32, (byte) 0x34, (byte) 0x31, (byte) 0x34, (byte) 0x35, (byte) 0x37, (byte) 0x35, (byte) 0x39, (byte) 0x29, (byte) 0x03, (byte) 0x63};
    private byte[] desconexion = {(byte) 0x01, (byte) 0x42, (byte) 0x30, (byte) 0x03, (byte) 0x71};
    
    //--------------------------Tramas Valores instantáneos Camilo------------------------------------------------
    //COrriente L1
    private byte[] IL1 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x33, (byte) 0x31, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //Voltaje L1
    private byte[] VL1 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x33, (byte) 0x32, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //FP L1
    private byte[] FPL1 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x33, (byte) 0x33, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //Corriente L2
    private byte[] IL2 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x35, (byte) 0x31, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //Voltaje L2
    private byte[] VL2 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x35, (byte) 0x32, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //FP L2
    private byte[] FPL2 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x35, (byte) 0x33, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //Corriente L3
    private byte[] IL3 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x37, (byte) 0x31, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //VOltaje L3
    private byte[] VL3 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x37, (byte) 0x32, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    //FPL3
    private byte[] FPL3 = {(byte) 0x01, (byte) 0x52, (byte) 0x31,(byte) 0x02, (byte) 0x35, (byte) 0x33, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x30, (byte) 0x28, 
        (byte) 0x29, (byte) 0x03, (byte) 0x58};
    
    //Métodos F.POT Camilo
    public byte[] getVL1() {
        return VL1;
    }

    public void setVL1(byte[] VL1) {
        this.VL1 = VL1;
    }

    public byte[] getIL1() {
        return IL1;
    }

    public void setIL1(byte[] IL1) {
        this.IL1 = IL1;
    }

    public byte[] getVL2() {
        return VL2;
    }

    public void setVL2(byte[] VL2) {
        this.VL2 = VL2;
    }

    public byte[] getIL2() {
        return IL2;
    }

    public void setIL2(byte[] IL2) {
        this.IL2 = IL2;
    }

    public byte[] getVL3() {
        return VL3;
    }

    public void setVL3(byte[] VL3) {
        this.VL3 = VL3;
    }
    
    public byte[] getIL3() {
        return IL3;
    }

    public void setIL3(byte[] IL3) {
        this.IL3 = IL3;
    }

    public byte[] getFPL1() {
        return FPL1;
    }

    public void setFPL13(byte[] FPL1) {
        this.FPL1 = FPL1;
    }

    public byte[] getFPL2() {
        return FPL2;
    }

    public void setFPL2(byte[] FPL2) {
        this.FPL2 = FPL2;
    }

    public byte[] getFPL3() {
        return FPL3;
    }

    public void setFPL3(byte[] FPL3) {
        this.FPL3 = FPL3;
    }
//----------------------------------------------------------------FIN FPOT
//codificacion
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public byte[] getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(byte[] identificacion) {
        this.identificacion = identificacion;
    }

    public byte[] getIdentificacion2() {
        return identificacion2;
    }

    public void setIdentificacion2(byte[] identificacion2) {
        this.identificacion2 = identificacion2;
    }
    

    public byte[] getComunicacion() {
        return comunicacion;
    }

    public void setComunicacion(byte[] comunicacion) {
        this.comunicacion = comunicacion;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public byte[] getSeriemedidor() {
        return seriemedidor;
    }

    public void setSeriemedidor(byte[] seriemedidor) {
        this.seriemedidor = seriemedidor;
    }

    public byte[] getFechaactual() {
        return fechaactual;
    }

    public void setFechaactual(byte[] fechaactual) {
        this.fechaactual = fechaactual;
    }

    public byte[] getFechatime() {
        return fechatime;
    }

    public void setFechatime(byte[] fechatime) {
        this.fechatime = fechatime;
    }

    public byte[] getPerfil() {
        return perfil;
    }

    public void setPerfil(byte[] perfil) {
        this.perfil = perfil;
    }

    public byte[] getPerfilfinal() {
        return perfilfinal;
    }

    public void setPerfilfinal(byte[] perfilfinal) {
        this.perfilfinal = perfilfinal;
    }

    public byte[] getEventos() {
        return eventos;
    }

    public void setEventos(byte[] eventos) {
        this.eventos = eventos;
    }

    public byte[] getEventosfinal() {
        return eventosfinal;
    }

    public void setEventosfinal(byte[] eventosfinal) {
        this.eventosfinal = eventosfinal;
    }

    public byte[] getSincreloj() {
        return sincreloj;
    }

    public void setSincreloj(byte[] sincreloj) {
        this.sincreloj = sincreloj;
    }

    public byte[] getDesconexion() {
        return desconexion;
    }

    public void setDesconexion(byte[] desconexion) {
        this.desconexion = desconexion;
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
