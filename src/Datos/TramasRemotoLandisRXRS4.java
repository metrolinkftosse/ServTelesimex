package Datos;

/**
 *
 * @author jgarcia
 */
public class TramasRemotoLandisRXRS4 {
    
    byte commAddress[] = {(byte) 0x2F, (byte) 0x3F, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x32,(byte) 0x21};
    byte[] directionability = {(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0xFF,(byte) 0x00};
    byte[] readSecurity = {(byte) 0x9B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x9B,(byte) 0x00};
    byte errorCodes[] = {(byte) 0x83, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x83,(byte) 0x00};
    byte firmware[] = {(byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x08,(byte) 0x00};
    byte touOptions[] = {(byte) 0x5A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x5A,(byte) 0x00};
    byte lpOptions[] = {(byte) 0x8F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x8F,(byte) 0x00};
    byte serial[] = {(byte) 0x6F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x6F,(byte) 0x00};
    byte fecha[] = {(byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x02,(byte) 0x00};
    byte hora[] = {(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x01,(byte) 0x00};
    byte fechaW[] = {(byte) 0x22, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x02,(byte) 0x00};
    byte horaW[] = {(byte) 0x21, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x01,(byte) 0x00};       
    byte loadProfileConfig[] = {(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x04,(byte) 0x00};
    byte profileElements[] = {(byte) 0xA6, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0xA6,(byte) 0x00};
    byte kFactor[] = {(byte) 0x1F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x1F,(byte) 0x00};
    byte transFactor[] = {(byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x72,(byte) 0x00};
    byte loadProfile[] = {(byte) 0x03, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x05,(byte) 0x00};
    byte logout[] = {(byte) 0x79, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00, (byte) 0x7D,(byte) 0x00};
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public byte[] getCommAddress() {
        return commAddress;
    }

    public void setCommAddress(byte[] commAddress) {
        this.commAddress = commAddress;
    }

    public byte[] getDirectionability() {
        return directionability;
    }

    public void setDirectionability(byte[] directionability) {
        this.directionability = directionability;
    }

    public byte[] getReadSecurity() {
        return readSecurity;
    }

    public void setReadSecurity(byte[] readSecurity) {
        this.readSecurity = readSecurity;
    }

    public byte[] getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(byte[] errorCodes) {
        this.errorCodes = errorCodes;
    }

    public byte[] getFirmware() {
        return firmware;
    }

    public void setFirmware(byte[] firmware) {
        this.firmware = firmware;
    }

    public byte[] getTouOptions() {
        return touOptions;
    }

    public void setTouOptions(byte[] touOptions) {
        this.touOptions = touOptions;
    }

    public byte[] getLpOptions() {
        return lpOptions;
    }

    public void setLpOptions(byte[] lpOptions) {
        this.lpOptions = lpOptions;
    }

    public byte[] getSerial() {
        return serial;
    }

    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

    public byte[] getFecha() {
        return fecha;
    }

    public void setFecha(byte[] fecha) {
        this.fecha = fecha;
    }

    public byte[] getHora() {
        return hora;
    }

    public void setHora(byte[] hora) {
        this.hora = hora;
    }

    public byte[] getFechaW() {
        return fechaW;
    }

    public void setFechaW(byte[] fechaW) {
        this.fechaW = fechaW;
    }

    public byte[] getHoraW() {
        return horaW;
    }

    public void setHoraW(byte[] horaW) {
        this.horaW = horaW;
    }
    
    public byte[] getLoadProfileConfig() {
        return loadProfileConfig;
    }

    public void setLoadProfileConfig(byte[] loadProfileConfig) {
        this.loadProfileConfig = loadProfileConfig;
    }

    public byte[] getProfileElements() {
        return profileElements;
    }

    public void setProfileElements(byte[] profileElements) {
        this.profileElements = profileElements;
    }

    public byte[] getkFactor() {
        return kFactor;
    }

    public void setkFactor(byte[] kFactor) {
        this.kFactor = kFactor;
    }

    public byte[] getTransFactor() {
        return transFactor;
    }

    public void setTransFactor(byte[] transFactor) {
        this.transFactor = transFactor;
    }

    public byte[] getLoadProfile() {
        return loadProfile;
    }

    public void setLoadProfile(byte[] loadProfile) {
        this.loadProfile = loadProfile;
    }

    public byte[] getLogout() {
        return logout;
    }

    public void setLogout(byte[] logout) {
        this.logout = logout;
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

}
