/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import protocolos.LeerRemotoHoneywellHS3400;
import protocolos.LeerRemotoTCPHexingStruct;
import protocolos.LeerRemotoTCPMicrostar;
import protocolos.LeerRemotoTCPMnC;
import protocolos.LeerRemotoTCPWasionAMeter300;
import protocolos.LeerRemotoTCP_ADDAXAD13A8;
import protocolos.LeerRemotoTCP_INHEMETERDTZ1513;
import protocolos.LeerRemotoTCP_ISKRAMT880;
import protocolos.LeerRemotoTCP_Kaifa;
import protocolos.LeerRemotoTCP_Metcom;
import protocolos.LeerRemotoTCP_STARDTS27;
import protocolos.LeerRemotoTCP_NANSEN_NSX112i;

/**
 *
 * @author Lenovo
 * Entidad para la recolección de los parámetros ajustables de los protocolos en dependencia con la marca de los medidores
 */
public class EParamProtocolos {
    public String type = "DLMS";
    public int PhyAddClass = 17;
    public int DLMS_numBytesDir = 1; //número de bytes para la dirección del medidor
    public int DLMS_InvokeIDandParity = 193; //0xC1 := 193
    public boolean DLMS_sumaInvokeIDnP = false;
    public boolean keepInvokeIDnP = false;
    public boolean DLMS_useDateTimeFormat = false;
    public boolean increaseCounter_HLS = true;
    public int DLMS_indxLength = 2;
    public ArrayList<String> DLMS_PrincipalOBIS = new ArrayList<>(); //Pasar OBIS de perfil, eventos y registros -- por ahora sin clase, porque se asumen todos clase 7
    public ArrayList<String> DLMS_PrincipalEvents = new ArrayList<>(); //Pasar códigos de eventos de reconexión - down and up
    public int users[] = {1};
    public double resolucion = 1000.0;
    public int rangoEnIntervalos = 80; //cantidad de intervalos por rango cuando se divide la petición de perfil
    public boolean usarRangoEnIntervalos = false; //Cuando se debe dividir la petición de perfil en varios rangos
    public boolean usarPowerOffConEstampas = false;
    public boolean usarDiasALeerEnEventos = true; //toma los dias de la bd
    public LeerRemotoTCPHexingStruct Hexing;
    public LeerRemotoTCPMnC MnC;
    public LeerRemotoTCPMicrostar Microstar;
    public LeerRemotoHoneywellHS3400 Honeywell;
    public LeerRemotoTCPWasionAMeter300 Wasion;
    public LeerRemotoTCP_ADDAXAD13A8 ADDAXAD13A8;
    public LeerRemotoTCP_INHEMETERDTZ1513 INHEMETERDTZ1513;
    public LeerRemotoTCP_ISKRAMT880 ISKRAMT880;
    public LeerRemotoTCP_Kaifa Kaifa;
    public LeerRemotoTCP_STARDTS27 STARDTS27;
    public LeerRemotoTCP_NANSEN_NSX112i NANSEN;
    public LeerRemotoTCP_Metcom METCOM;
    
    private LinkedHashMap<String, byte[]> camposAARQ = new LinkedHashMap<>(); //camilo 21.07.12 
    private LinkedHashMap<Integer, String> parametrosAvanzadosPorMedidor = new LinkedHashMap<>();
    private LinkedHashMap<Integer, byte[]> parametrosAvanzadosPorMedidor_Bytes = new LinkedHashMap<>();
    
    public byte[] DLMS_EK_HLS = {(byte) 0x7C, (byte) 0x71, (byte) 0x35, (byte) 0x7C, (byte) 0x72, (byte) 0x56, (byte) 0x79, (byte) 0x3F, (byte) 0x76, (byte) 0x77, (byte) 0x30, 
                    (byte) 0x7C, (byte) 0x4B, (byte) 0x51, (byte) 0x50, (byte) 0x32};
    public byte[] DLMS_AK_HLS = {(byte) 0x5D, (byte) 0x41, (byte) 0x46, (byte) 0x78, (byte) 0x6A, (byte) 0x5A, (byte) 0x63, (byte) 0x35, (byte) 0x6A, (byte) 0x39, (byte) 0x64, 
                    (byte) 0x3D, (byte) 0x32, (byte) 0x55, (byte) 0x5A, (byte) 0x25};
    public byte[] DLMS_SecurityControl_HLS = {(byte) 0x10};
    public byte[] DLMS_ClientS1_HLS = {(byte) 0x16};
    
    private final  String[] baseOBIS = {
        "0000600100FF", 
        "0000010000FF", 
        "0100630100FF", 
        "0000160000FF", 
        "00002B0101FF", 
        "0000280000FF", 
        "0100636200FF", 
        "0100630200FF", 
        "0000620100FF", 
        "0000000000FF", 
        "000060030AFF",//10, corte
        "0100000200FF",//11,firmware
        "0101010800FF"//12 Energía Total acumulada
    };
    
//    public LinkedHashMap<String, byte[]> camposAARQ = new LinkedHashMap<>(); //camilo 21.07.12 
    public void inicializarAARQ(){
        camposAARQ.put("acseProtocolVersion",null);
        camposAARQ.put("appContextName", null);
        camposAARQ.put("calledAPTitle", null);
        camposAARQ.put("calledAEQualifier", null);
        camposAARQ.put("calledAPInvID", null);
        camposAARQ.put("calledAEInvID",null);
        camposAARQ.put("callingAPTitle", null);
        camposAARQ.put("callingdAEQualifier", null);
        camposAARQ.put("callingAPInvID", null);
        camposAARQ.put("callingAEInvID",null);
        camposAARQ.put("acseReq", null);
        camposAARQ.put("mechName", null);
        camposAARQ.put("authenticationValue", null);
        camposAARQ.put("implementationInfo", null);
        // Campos internos de userInfo
        camposAARQ.put("userInfo", null);  
        camposAARQ.put("dedicatedKey",null);
        camposAARQ.put("responseAllowed",null);
        camposAARQ.put("proposedQoS",null);
        camposAARQ.put("proposedDLMSVersion",null);    
        camposAARQ.put("proposedConformance",null);  
        camposAARQ.put("clientMaxRxPDUSize",null);
    }
    
    public void inicializarOBIS() {
        //DLMS_PrincipalOBIS.set(0, "0000600100FF");//Device Id #1 Man. Number. - Serial
        //DLMS_PrincipalOBIS.set(1, "0000010000FF");//Fecha Actual - Conf. Hora.
        //DLMS_PrincipalOBIS.set(2, "0100630100FF");//Periodo de Integración - Conf Perfil - Perfil de carga
        //DLMS_PrincipalOBIS.set(3, "0000160000FF");//IEC HDLC Setup. - Phy Address.
        //DLMS_PrincipalOBIS.set(4, "00002B0101FF");// Invocation Counter Security.
        //DLMS_PrincipalOBIS.set(5, "0000280000FF");// Current Association.
        //DLMS_PrincipalOBIS.set(6, "0100636200FF");//Eventos
        //DLMS_PrincipalOBIS.set(7, "0100630200FF");// Registros Diarios. Hexing
        //DLMS_PrincipalOBIS.set(8, "0000620100FF");// Registro mensuales.  
        //DLMS_PrincipalOBIS.set(9, "0000000000FF");// Constantes. 
        //DLMS_PrincipalOBIS.set(10, "000060030AFF");// DISCONNECT CONTROL "000060030AFF"
        //DLMS_PrincipalOBIS.set(11, "0100000200FF");// Firmware
        for (String OBIS : baseOBIS) {
            this.DLMS_PrincipalOBIS.add(OBIS);
            System.out.println("Añadir OBIS: " + OBIS);
        }        
    }
            
    
    public EParamProtocolos(LeerRemotoTCPHexingStruct Hexing) {
        this.Hexing = Hexing;
    }
    
    public EParamProtocolos(LeerRemotoTCPMnC MnC){
        this.MnC = MnC;
    }
    
    public EParamProtocolos(LeerRemotoTCPMicrostar Microstar){
        this.Microstar = Microstar;
    }
    
    public EParamProtocolos(LeerRemotoHoneywellHS3400 Honeywell){
        this.Honeywell = Honeywell; 
    }
    
     public EParamProtocolos(LeerRemotoTCPWasionAMeter300 Wasion){
        this.Wasion = Wasion; 
    }    
    
    public EParamProtocolos(LeerRemotoTCP_ADDAXAD13A8 ADDAXAD13A8){
        this.ADDAXAD13A8 = ADDAXAD13A8;
    }
    
    public EParamProtocolos(LeerRemotoTCP_INHEMETERDTZ1513 INHEMETERDTZ1513){
        this.INHEMETERDTZ1513 = INHEMETERDTZ1513;
    }
//    public EParamProtocolos(LeerRemotoTCPMnC MnC) {
//        this.MnC = MnC;
//    }
    
    public EParamProtocolos(LeerRemotoTCP_ISKRAMT880 ISKRAMT880){
        this.ISKRAMT880 = ISKRAMT880;
    }
    
    public EParamProtocolos(LeerRemotoTCP_STARDTS27 STARDTS27){
        this.STARDTS27 = STARDTS27;
    }
    
    public EParamProtocolos(LeerRemotoTCP_NANSEN_NSX112i NANSEN) {
        this.NANSEN = NANSEN;
    }

    public EParamProtocolos(LeerRemotoTCP_Metcom METCOM) {
        this.METCOM = METCOM;
    }        
    
    public EParamProtocolos(LeerRemotoTCP_Kaifa Kaifa){
        this.Kaifa = Kaifa;
    }
    
    public LinkedHashMap getCamposAARQ() { //camilo 21.07.12
        return camposAARQ;
    }

    public void setCamposAARQ(LinkedHashMap<String, byte[]> camposAARQ) { //camilo 21.07.12
        this.camposAARQ = camposAARQ;
    }
    
    public void editCamposAARQ(String key, byte[] value){
        this.camposAARQ.put(key, value);        
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPhyAddClass() {
        return PhyAddClass;
    }

    public void setPhyAddClass(int PhyAddClass) {
        this.PhyAddClass = PhyAddClass;
    }

    public int getDLMS_numBytesDir() {
        return DLMS_numBytesDir;
    }

    public void setDLMS_numBytesDir(int DLMS_numBytesDir) {
        this.DLMS_numBytesDir = DLMS_numBytesDir;
    }

    public int getDLMS_InvokeIDandParity() {
        return DLMS_InvokeIDandParity;
    }

    public void setDLMS_InvokeIDandParity(int DLMS_InvokeIDandParity) {
        this.DLMS_InvokeIDandParity = DLMS_InvokeIDandParity;
    }

    public int getDLMS_indxLength() {
        return DLMS_indxLength;
    }

    public void setDLMS_indxLength(int DLMS_indxLength) {
        this.DLMS_indxLength = DLMS_indxLength;
    }

    public int[] getUsers() {
        return users;
    }

    public void setUsers(int[] users) {
        this.users = users;
    }

    public double getResolucion() {
        return resolucion;
    }

    public void setResolucion(double resolucion) {
        this.resolucion = resolucion;
    }

    public int getRangoEnIntervalos() {
        return rangoEnIntervalos;
    }

    public void setRangoEnIntervalos(int rangoEnIntervalos) {
        this.rangoEnIntervalos = rangoEnIntervalos;
    }

    public boolean isUsarRangoEnIntervalos() {
        return usarRangoEnIntervalos;
    }

    public void setUsarRangoEnIntervalos(boolean usarRangoEnIntervalos) {
        this.usarRangoEnIntervalos = usarRangoEnIntervalos;
    }

    public boolean isUsarPowerOffConEstampas() {
        return usarPowerOffConEstampas;
    }

    public void setUsarPowerOffConEstampas(boolean usarPowerOffConEstampas) {
        this.usarPowerOffConEstampas = usarPowerOffConEstampas;
    }

    public boolean isUsarDiasALeerEnEventos() {
        return usarDiasALeerEnEventos;
    }

    public void setUsarDiasALeerEnEventos(boolean usarDiasALeerEnEventos) {
        this.usarDiasALeerEnEventos = usarDiasALeerEnEventos;
    }

    public boolean isDLMS_sumaInvokeIDnP() {
        return DLMS_sumaInvokeIDnP;
    }

    public void setDLMS_sumaInvokeIDnP(boolean DLMS_sumaInvokeIDnP) {
        this.DLMS_sumaInvokeIDnP = DLMS_sumaInvokeIDnP;
    }

    public boolean isKeepInvokeIDnP() {
        return keepInvokeIDnP;
    }

    public void setKeepInvokeIDnP(boolean keepInvokeIDnP) {
        this.keepInvokeIDnP = keepInvokeIDnP;
    }        
    
    public boolean isIncreaseCounter_HLS() {
        return this.increaseCounter_HLS;
    }

    public void setIncreaseCounter_HLS(boolean increaseCounter_HLS) {
        this.increaseCounter_HLS = increaseCounter_HLS;
    }       
    
    public boolean isDLMS_useDateTimeFormat() {
        return DLMS_useDateTimeFormat;
    }

    public void setDLMS_useDateTimeFormat(boolean DLMS_useDateTimeFormat) {
        this.DLMS_useDateTimeFormat = DLMS_useDateTimeFormat;
    }

    public ArrayList<String> getDLMS_PrincipalOBIS() {
        return DLMS_PrincipalOBIS;
    }
    
    public String getDLMS_PrincipalOBIS(int idx) {
        return DLMS_PrincipalOBIS.get(idx);
    }
    
    public void addDLMS_PrincipalOBIS(String OBIS) {
        this.DLMS_PrincipalOBIS.add(OBIS);
    }
    
    public  void editDLMS_PrincipalOBIS(int idx, String OBIS) {
        this.DLMS_PrincipalOBIS.set(idx, OBIS);
    }

    public ArrayList<String> getDLMS_PrincipalEvents() {
        return DLMS_PrincipalEvents;
    }

    public void setDLMS_PrincipalEvents(ArrayList<String> DLMS_PrincipalEvents) {
        this.DLMS_PrincipalEvents = DLMS_PrincipalEvents;
    }

    public LinkedHashMap<Integer, String> getParametrosAvanzadosPorMedidor() {
        return parametrosAvanzadosPorMedidor;
    }

    public void setParametrosAvanzadosPorMedidor(LinkedHashMap<Integer, String> parametrosAvanzadosPorMedidor) {
        this.parametrosAvanzadosPorMedidor = parametrosAvanzadosPorMedidor;
        Set<Integer> keys = parametrosAvanzadosPorMedidor.keySet();
        for (Integer key : keys) {
            String tempVal = parametrosAvanzadosPorMedidor.get(key);
            byte[] byteValue = new byte[tempVal.length() / 2];
            int idx = 0;
            for (int hexIdx = 0; hexIdx < tempVal.length(); hexIdx += 2) {
                byteValue[idx] = (byte) Integer.parseInt(tempVal.substring(hexIdx, hexIdx + 2), 16);
                idx++;
            }
            this.parametrosAvanzadosPorMedidor_Bytes.put(key, byteValue);
        }
    }

    public LinkedHashMap<Integer, byte[]> getParametrosAvanzadosPorMedidor_Bytes() {
        return parametrosAvanzadosPorMedidor_Bytes;
    }

    public void setParametrosAvanzadosPorMedidor_Bytes(LinkedHashMap<Integer, byte[]> parametrosAvanzadosPorMedidor_Bytes) {
        this.parametrosAvanzadosPorMedidor_Bytes = parametrosAvanzadosPorMedidor_Bytes;
    }
    
    public void inicializarParametrosAvanzadosPorMedidor() {
        this.DLMS_EK_HLS=parametrosAvanzadosPorMedidor_Bytes.get(1)==null?DLMS_EK_HLS:parametrosAvanzadosPorMedidor_Bytes.get(1);
        this.DLMS_AK_HLS=parametrosAvanzadosPorMedidor_Bytes.get(2)==null?DLMS_AK_HLS:parametrosAvanzadosPorMedidor_Bytes.get(2);
        this.DLMS_SecurityControl_HLS=parametrosAvanzadosPorMedidor_Bytes.get(3)==null?DLMS_SecurityControl_HLS:parametrosAvanzadosPorMedidor_Bytes.get(3);
        byte[] temp = parametrosAvanzadosPorMedidor_Bytes.get(4)==null?DLMS_ClientS1_HLS:parametrosAvanzadosPorMedidor_Bytes.get(4);
        this.DLMS_ClientS1_HLS[0]= (byte) (Integer.parseInt(Integer.toHexString(temp[0])) & 0xFF);
    }
    
    public byte[] stringToByteVector(String cadena) {
         byte[] bytevector = new byte[cadena.length()*2];
        for (int i = 0; i <= 3; i=i+2) {
            bytevector[i] = (byte) (Integer.parseInt(cadena.substring(i, i+2), 16) & 0xFF);
            i++;
        }
        return bytevector;
    }
}
