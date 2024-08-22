package Datos;

/**
 *
 * @author jgarcia
 */
public class TramasRemota_ION8650 {
    byte[] header = {(byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0xAC, (byte) 0x00, (byte) 0x22, (byte) 0xF5, (byte) 0x82, (byte) 0xFF, (byte) 0xFF, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x95, (byte) 0x64, (byte) 0x00, (byte) 0x01, (byte) 0x95, (byte) 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x0F, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x04};
    byte[] headerPwd = {(byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0xAC, (byte) 0x00, (byte) 0x2A, (byte) 0x76, (byte) 0xC2, (byte) 0xFF, (byte) 0xFF, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x23, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x95, (byte) 0x64, (byte) 0x00, (byte) 0x01, (byte) 0x95, (byte) 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x36, (byte) 0xDF, (byte) 0xD5, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x0F, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x04};
    byte[] contactingDevice = {(byte) 0xF6, (byte) 0x13, (byte) 0x00, (byte) 0x15};
    byte[] serial = {(byte) 0xF6, (byte) 0x13, (byte) 0x04, (byte) 0x15};
    byte[] realTime = {(byte) 0xF6, (byte) 0x5B, (byte) 0x7D, (byte) 0x15};
    byte[] timeZone = {(byte) 0xF6, (byte) 0x72, (byte) 0x2E, (byte) 0x15};
    byte[] listManager = {(byte) 0xF6, (byte) 0x00, (byte) 0x02};
    byte[] logs = {(byte) 0xFF, (byte) 0x03, (byte) 0xEC};
    byte[] logPositionCounter = {(byte) 0xFF, (byte) 0x03, (byte) 0xEB};
    byte description = (byte) 0x03;
    byte valueLogs = (byte) 0x15;
    byte valuePositionLogs = (byte) 0x28;
    byte idRecord[] = {(byte) 0xFF, (byte) 0x05, (byte) 0xDE};
    byte inputs[] = {(byte) 0xFF, (byte) 0x03, (byte) 0xE8};
    byte recordOfInput[] = {(byte) 0xFF, (byte) 0x00, (byte) 0x83}; 
    byte events[] = {(byte) 0x10, (byte) 0x00};
    byte loadProfile[] = {(byte) 0xF6, (byte) 0x0F, (byte) 0x80, (byte) 0x95, (byte) 0x74};
    byte nextOrLogout[] = {(byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0xAC, (byte) 0xE0, (byte) 0x07, (byte) 0x76, (byte) 0xC2, (byte) 0x64, (byte) 0x00, (byte) 0x41, (byte) 0x00};
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};
    
    
    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getHeaderPwd() {
        return headerPwd;
    }

    public void setHeaderPwd(byte[] headerPwd) {
        this.headerPwd = headerPwd;
    }

    public byte[] getContactingDevice() {
        return contactingDevice;
    }

    public void setContactingDevice(byte[] contactingDevice) {
        this.contactingDevice = contactingDevice;
    }

    public byte[] getSerial() {
        return serial;
    }

    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

    public byte[] getRealTime() {
        return realTime;
    }

    public void setRealTime(byte[] realTime) {
        this.realTime = realTime;
    }

    public byte[] getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(byte[] timeZone) {
        this.timeZone = timeZone;
    }

    public byte[] getListManager() {
        return listManager;
    }

    public void setListManager(byte[] listManager) {
        this.listManager = listManager;
    }
    
    public byte[] getLogs() {
        return logs;
    }

    public void setLogs(byte[] logs) {
        this.logs = logs;
    }

    public byte[] getLogPositionCounter() {
        return logPositionCounter;
    }

    public void setLogPositionCounter(byte[] logPositionCounter) {
        this.logPositionCounter = logPositionCounter;
    }

    public byte getDescription() {
        return description;
    }

    public void setDescription(byte description) {
        this.description = description;
    }

    public byte getValueLogs() {
        return valueLogs;
    }

    public void setValueLogs(byte valueLogs) {
        this.valueLogs = valueLogs;
    }

    public byte getValuePositionLogs() {
        return valuePositionLogs;
    }

    public void setValuePositionLogs(byte valuePositionLogs) {
        this.valuePositionLogs = valuePositionLogs;
    }

    public byte[] getIdRecord() {
        return idRecord;
    }

    public void setIdRecord(byte[] idRecord) {
        this.idRecord = idRecord;
    }

    public byte[] getInputs() {
        return inputs;
    }

    public void setInputs(byte[] inputs) {
        this.inputs = inputs;
    }

    public byte[] getRecordOfInput() {
        return recordOfInput;
    }

    public void setRecordOfInput(byte[] recordOfInput) {
        this.recordOfInput = recordOfInput;
    }

    public byte[] getEvents() {
        return events;
    }

    public void setEvents(byte[] events) {
        this.events = events;
    }        

    public byte[] getLoadProfile() {
        return loadProfile;
    }

    public void setLoadProfile(byte[] loadProfile) {
        this.loadProfile = loadProfile;
    }

    public byte[] getNextOrLogout() {
        return nextOrLogout;
    }

    public void setNextOrLogout(byte[] nextOrLogout) {
        this.nextOrLogout = nextOrLogout;
    }

    public static byte[] getHexhars() {
        return Hexhars;
    }

    public static void setHexhars(byte[] Hexhars) {
        TramasRemota_ION8650.Hexhars = Hexhars;
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
