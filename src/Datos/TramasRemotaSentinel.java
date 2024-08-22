/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author Metrolink
 */
public class TramasRemotaSentinel {
    //identidad

    byte[] Identidad = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x20, (byte) 0x13, (byte) 0x10};
    //negociacion
    byte[] Negotiation = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x65, (byte) 0x00, (byte) 0x80, (byte) 0xFE, (byte) 0x01,
        (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0xCE, (byte) 0x28};
    //login
    byte[] Session = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D, (byte) 0x50, (byte) 0x00, (byte) 0x02, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x74, (byte) 0xB8};
    //autenticacion
    byte[] autenticate = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x15, (byte) 0x51, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x27, (byte) 0xA2};
    //timeout
    byte[] WaitService = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x70, (byte) 0xFF, (byte) 0x99, (byte) 0xE1};
    //serial
    byte[] Serialnumber = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x05, (byte) 0xF2, (byte) 0x28};
    //escritura fecha
    byte[] Writefecha = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x40, (byte) 0x08, (byte) 0x01, (byte) 0x00,
        (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6C, (byte) 0x93, (byte) 0x20, (byte) 0xB8};
    //solicitud de fecha actual
    byte[] Readfecha = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x08, (byte) 0x02, (byte) 0x0E, (byte) 0xF1};
    //configuracion de eventos
    byte[] ConfEvents = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x47, (byte) 0xD8, (byte) 0xAB};
    //eventos
    byte[] Events = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x4A, (byte) 0xBE, (byte) 0x13};
    //escritura configuracion perfil carga
    byte[] WriteConfPerfil = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x40, (byte) 0x08, (byte) 0x01, (byte) 0x00, (byte) 0x16,
        (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x48, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x48, (byte) 0x14, (byte) 0x00,
        (byte) 0x00, (byte) 0x48, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x4C, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x4C, (byte) 0x70, (byte) 0x97, (byte) 0x33};
    //lectura configuracion perfil carga
    byte[] ReadConfPerfil = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x30, (byte) 0x08, (byte) 0x02, (byte) 0x8D, (byte) 0x92};
    //solicitud perfil carga
    byte[] ReadPerfil = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x3F, (byte) 0x08, (byte) 0x07, (byte) 0x00, (byte) 0x7E,
        (byte) 0x34, (byte) 0x03, (byte) 0x14, (byte) 0x0F, (byte) 0xA1};    
    byte[] ReadPerfilExt = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x3F, (byte) 0x08, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x7E,
        (byte) 0x34, (byte) 0x03, (byte) 0x14, (byte) 0x0F, (byte) 0xA1};
    //Solicitud de escritura de configuracion de hora.
    byte[] writeConfHora = {(byte) 0xEE, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x40, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x0A, (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x52, (byte) 0x43, (byte) 0x73, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0xE3, (byte) 0x05, (byte) 0x81};
    //solicitud lectura de configuracion de hora.
    byte[] readConfHora = {(byte) 0xEE	, (byte) 0x01	, (byte) 0x20	, (byte) 0x00	, (byte) 0x00	, (byte) 0x03	, (byte) 0x30	, (byte) 0x00	, (byte) 0x08	, (byte) 0xA8	, (byte) 0x72};
    //ack
    byte[] ACK = {(byte) 0x06};
    //logoof
    byte[] logoff = {(byte) 0xEE, (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x52, (byte) 0xAD, (byte) 0x44};
     protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};
    
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
     
    public byte[] getIdentidad() {
        return Identidad;
    }

    public void setIdentidad(byte[] Identidad) {
        this.Identidad = Identidad;
    }

    public byte[] getNegotiation() {
        return Negotiation;
    }

    public void setNegotiation(byte[] Negotiation) {
        this.Negotiation = Negotiation;
    }

    public byte[] getSession() {
        return Session;
    }

    public void setSession(byte[] Session) {
        this.Session = Session;
    }

    public byte[] getAutenticate() {
        return autenticate;
    }

    public void setAutenticate(byte[] autenticate) {
        this.autenticate = autenticate;
    }

    public byte[] getWaitService() {
        return WaitService;
    }

    public void setWaitService(byte[] WaitService) {
        this.WaitService = WaitService;
    }

    public byte[] getSerialnumber() {
        return Serialnumber;
    }

    public void setSerialnumber(byte[] Serialnumber) {
        this.Serialnumber = Serialnumber;
    }

    public byte[] getWritefecha() {
        return Writefecha;
    }

    public void setWritefecha(byte[] Writefecha) {
        this.Writefecha = Writefecha;
    }

    public byte[] getReadfecha() {
        return Readfecha;
    }

    public void setReadfecha(byte[] Readfecha) {
        this.Readfecha = Readfecha;
    }

    public byte[] getConfEvents() {
        return ConfEvents;
    }

    public void setConfEvents(byte[] ConfEvents) {
        this.ConfEvents = ConfEvents;
    }

    public byte[] getEvents() {
        return Events;
    }

    public void setEvents(byte[] Events) {
        this.Events = Events;
    }

    public byte[] getWriteConfPerfil() {
        return WriteConfPerfil;
    }

    public void setWriteConfPerfil(byte[] WriteConfPerfil) {
        this.WriteConfPerfil = WriteConfPerfil;
    }

    public byte[] getReadConfPerfil() {
        return ReadConfPerfil;
    }

    public void setReadConfPerfil(byte[] ReadConfPerfil) {
        this.ReadConfPerfil = ReadConfPerfil;
    }

    public byte[] getReadPerfil() {
        return ReadPerfil;
    }

    public void setReadPerfil(byte[] ReadPerfil) {
        this.ReadPerfil = ReadPerfil;
    }

    public byte[] getReadPerfilExt() {
        return ReadPerfilExt;
    }
    
    public void setReadPerfilExt(byte[] ReadPerfilExt) {
        this.ReadPerfilExt = ReadPerfilExt;
    }
    
    public byte[] getACK() {
        return ACK;
    }

    public void setACK(byte[] ACK) {
        this.ACK = ACK;
    }

    public static byte[] getHexhars() {
        return Hexhars;
    }

    public byte[] getWriteConfHora() {
        return writeConfHora;
    }

    public void setWriteConfHora(byte[] writeConfHora) {
        this.writeConfHora = writeConfHora;
    }

    public byte[] getReadConfHora() {
        return readConfHora;
    }

    public void setReadConfHora(byte[] readConfHora) {
        this.readConfHora = readConfHora;
    }

    public byte[] getLogoff() {
        return logoff;
    }

    public void setLogoff(byte[] logoff) {
        this.logoff = logoff;
    }
    

    public static void setHexhars(byte[] Hexhars) {
        TramasRemotaSentinel.Hexhars = Hexhars;
    }    
}
