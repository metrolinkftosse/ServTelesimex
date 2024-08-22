/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

import java.util.HashMap;

/**
 *
 * @author Lenovo
 */
public class TramasDLMS {
    
    private HashMap<String, byte[]> tramasSNRM = new HashMap();
        
    byte[] snrm = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x03, (byte) 0x03, (byte) 0x93, (byte) 0x03, (byte) 0xA2, (byte) 0x7E}; //común
    byte[] snrm2 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x03, (byte) 0x03, (byte) 0x93, (byte) 0x03, (byte) 0xA2,
        (byte) 0x81, (byte) 0x80, (byte) 0x14, //con parámetros del HDLC
        (byte) 0x05, (byte) 0x02, (byte) 0x07, (byte) 0xD0,//2000
        (byte) 0x06, (byte) 0x02, (byte) 0x07, (byte) 0xD0,
        (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
        (byte) 0x08, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
        (byte) 0x03, (byte) 0xA2, (byte) 0x7E}; 
    byte [] snrmHoneywellHS3400 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x21, (byte) 0x03, (byte) 0x21, (byte) 0x93, (byte) 0x48,
        (byte) 0x55, (byte) 0x81, (byte) 0x80, (byte) 0x14, (byte) 0x05, (byte) 0x02, (byte) 0x07, (byte) 0xEE, (byte) 0x06, (byte) 0x02, (byte) 0x07,
        (byte) 0xEE, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x04, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xB5, (byte) 0xD4, (byte) 0x7E};
    
    
    byte[] snrmKaifa = {(byte) 0x7E, (byte) 0xA0, (byte) 0x21, (byte) 0x23, (byte) 0x21, (byte) 0x93, (byte) 0x48, (byte) 0x55,
                        (byte) 0x81, (byte) 0x80, (byte) 0x14, (byte) 0x05, (byte) 0x02, (byte) 0x07, (byte) 0xEE, (byte) 0x06, (byte) 0x02,
                        (byte) 0x07, (byte) 0xEE, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08,
                        (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xB5, (byte) 0xD4, (byte) 0x7E};
    
    byte[] snrmWASIONAMeter300 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x23, (byte) 0x21, (byte) 0x21, (byte) 0x93,
                                  (byte) 0x1A, (byte) 0xE6, (byte) 0x81, (byte) 0x80, (byte) 0x14, (byte) 0x05, (byte) 0x02, (byte) 0x05, (byte) 0x00,
                                  (byte) 0x06, (byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                  (byte) 0x01, (byte) 0x08, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x7F, (byte) 0x65,
                                  (byte) 0x7E};
    
    //pass ocho ceros
    byte[] aarq = {(byte) 0x7E, (byte) 0xA0, (byte) 0x45, //2
        (byte) 0x03, //3
        (byte) 0x03, (byte) 0x10, (byte) 0xBB, (byte) 0xC1, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x60, //11
        (byte) 0x46, //12
        (byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, //16
        (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01,//27
//        (byte) 0xA6, (byte) 0x0A, (byte) 0x04, (byte) 0x08, (byte) 0x48, (byte) 0x45, (byte) 0x43, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x01,//39
        (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, //43 31
        (byte) 0x8B, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01, //52 40
        (byte) 0xAC, (byte) 0x0A, (byte) 0x80, (byte) 0x08, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,//64 52
        (byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x1F, (byte) 0x04, (byte) 0x00, //77
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, //80
        (byte) 0xFF, (byte) 0xFF, //82
        (byte) 0x2C, (byte) 0xF7, (byte) 0x7E};//85

    //----------------REESTRUCTURACIÓN CAMILO--------------------------------------------------------------------------------------------------------    
    byte[] aARQIni = {(byte) 0x7E, (byte) 0xA0, (byte) 0x45, //Flag + length 2
        (byte) 0x03, (byte) 0x03, //Dir 1 Byte de dirección, Cliente 1
        (byte) 0x10, //CTRL
        (byte) 0xBB, (byte) 0xC1, //HCS
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, //REQ LLC 10
        (byte) 0x60, //AARQ PDU INICIA CAMPOS AARQ, 11
        (byte) 0x46}; //LENGTH PDU 12
        //(byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, //16
    byte[] aARQACSEProtocolV = {(byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80};
    byte[] aARQContextName = {(byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01};//23
    byte[] aARQCalledAPTitle = {(byte) 0xA2, (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x00};
    byte[] aARQCalledAEQualifier = {(byte) 0xA3, (byte) 0x05, (byte) 0xA1, (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x00};
    byte[] aARQAcse = {(byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80}; //27
    byte[] aARQApTitle = {(byte) 0xA6, (byte) 0x0A, (byte) 0x04, (byte) 0x08, (byte) 0x4B, (byte) 0x46, (byte) 0x4D, (byte) 0x66, (byte) 0x70, 
        (byte) 0x00, (byte) 0x00, (byte) 0x0C};
    byte[] aARQMechName = {(byte) 0x8B, (byte) 0x07, //MECHANISM NAME 29
        (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01}; //36
    byte[] aARQAuthentication = {(byte) 0xAC, (byte) 0x0A, (byte) 0x80, (byte) 0x08}; //40
    byte[] aARQPassword = {(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30}; //48
    byte[] aARQUsrInfo = {(byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E}; //USR INFO TAG 52
//        (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, //USR INFO VAL 56
//        (byte) 0x06, //VERS DLMS 57
//        (byte) 0x5F, (byte) 0x1F, (byte) 0x04, (byte) 0x00}; //61
    byte[] aARQConformance = {(byte) 0x00, (byte) 0x7E, (byte) 0x1F}; //64
    byte[] aARQMaxPDU = {(byte) 0xFF, (byte) 0xFF}; //66
    byte[] aARQFin = {(byte) 0x2C, (byte) 0xF7, (byte) 0x7E}; //69  
    //------------------------------------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------
    byte[] cosemDescriptor = {(byte) 0xC0, (byte) 0x01, (byte) 0xC1, (byte) 0x00,
                              (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
                              (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x03,
                              (byte) 0x00};
    byte[] getRequestNormal = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A, (byte) 0x03, (byte) 0x03, (byte) 0x32, 
                               (byte) 0x32, (byte) 0x9D, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, 
                               (byte) 0x01, (byte) 0xC2, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                               (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, // 00 00 00 00 00 FF
                               (byte) 0x02, (byte) 0x00, (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    
    byte[] getRequestNormalAPDU = {(byte) 0xC0, (byte) 0x01, (byte) 0xC1, (byte) 0x00, 
                                   (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, 
                                   (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x03, 
                                   (byte) 0x00};
    
    byte[] getReqNextBlock = {(byte) 0xC0, (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07};
    
    byte[] clockAccessSelection = {(byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09,
                                   (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02,
                                   (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF, (byte) 0x04, (byte) 0x14,
                                   (byte) 0xFF, (byte) 0x0F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x09,
                                   (byte) 0x0C, (byte) 0x07, (byte) 0xDF, (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x17, (byte) 0x00, (byte) 0x00,
                                   (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00};
    
    byte[] clockAccessSelectionDT = {(byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09,
                                     (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, 
                                     (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x07, (byte) 0xE6, (byte) 0x01, (byte) 0x14, (byte) 0x04, 
                                     (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xD4, (byte) 0x00, (byte) 0x19, (byte) 0x07,
                                     (byte) 0xE6, (byte) 0x01, (byte) 0x14, (byte) 0x04, (byte) 0x03, (byte) 0x2D, (byte) 0x00, (byte) 0x00, (byte) 0xFE,
                                     (byte) 0xD4, (byte) 0x00, (byte) 0x01, (byte) 0x00};
    
    byte[] gloGetRequest = {(byte) 0x7E, (byte) 0xA0, (byte) 0x02, (byte) 0x23, (byte) 0x03, (byte) 0x54, (byte) 0x48, (byte) 0x80, (byte)
            0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC8, (byte) 0x1E, 
            (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x2F,
            (byte) 0xF1, (byte) 0xAF, (byte) 0x0E, (byte) 0x7C, (byte) 0xE3, (byte) 0xB6, (byte) 0x54, (byte) 0x26, (byte) 0xC3, (byte) 0x45, (byte) 0xA4,
            (byte) 0xB9, (byte) 0x27, (byte) 0xC0, (byte) 0x93, (byte) 0x72, (byte) 0xB8, (byte) 0x68, (byte) 0x74, (byte) 0x1F, (byte) 0x20, (byte) 0xF2,
            (byte) 0x6B, (byte) 0x06, (byte) 0x5F, (byte) 0xA4, (byte) 0x10, (byte) 0x7E};
    
    byte[] actionRequestAPDU = {(byte) 0xC3, (byte) 0x01, (byte) 0xC1, (byte) 0x00,
        (byte) 0x0F, (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x01, (byte) 0x09, (byte) 0x11, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x2D, 
        (byte) 0xC9, (byte) 0xF1, (byte) 0x07, (byte) 0xA7, (byte) 0xA4, (byte) 0x9A, (byte) 0x0B, (byte) 0x0E, (byte) 0x6D, (byte) 0x3F, (byte) 0xA3, (byte) 0x59};
    
    byte[] gloActionRequest = {(byte) 0x7E, (byte) 0xA0, (byte) 0x40, (byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0xBF, (byte) 0x5B,
                               (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xCB, (byte) 0x31, 
                               (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x2E, 
                               (byte) 0xD2, (byte) 0xB1, (byte) 0xB0, (byte) 0x77, (byte) 0x62, (byte) 0x0A, (byte) 0x56, (byte) 0x13, (byte) 0x94, (byte) 0xFE, 
                               (byte) 0xB0, (byte) 0x1B, (byte) 0x9E, (byte) 0x3E, (byte) 0xEF, (byte) 0x03, (byte) 0x7E, (byte) 0x62, (byte) 0xE0, (byte) 0xBC,
                               (byte) 0xE9, (byte) 0x2A, (byte) 0x18, (byte) 0x32, (byte) 0x07, (byte) 0x8C, (byte) 0x8A, (byte) 0xBA, (byte) 0x2E, (byte) 0xD6,
                               (byte) 0x2D, (byte) 0x88, (byte) 0xAA, (byte) 0x25, (byte) 0xD0, (byte) 0xE1, (byte) 0x9E, (byte) 0x49, (byte) 0xB0, (byte) 0x81,
                               (byte) 0x86, (byte) 0x51, (byte) 0x8E, (byte) 0xA9, (byte) 0xFF, (byte) 0xCF, (byte) 0x7E};
    
    byte[] setRequestAPDUClock = {(byte) 0xC1, (byte) 0x01, (byte) 0x41, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, 
                             (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE6, (byte) 0x02, (byte) 0x0F, (byte) 0x02, 
                             (byte) 0x10, (byte) 0x0A, (byte) 0x13, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00};
    
    byte[] setRequestAPDUClockDT = {(byte) 0xC1, (byte) 0x01, (byte) 0x41, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, 
                             (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x19, (byte) 0x07, (byte) 0xE6, (byte) 0x02, (byte) 0x0F, (byte) 0x02, 
                             (byte) 0x10, (byte) 0x0A, (byte) 0x13, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00};
    
    byte[] gloSetRequest = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x03, (byte) 0x03, (byte) 0x54, (byte) 0xC2, (byte) 0x77,
                            (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC9, (byte) 0x2C, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x52, (byte) 0xE0,
                            (byte) 0xB7, (byte) 0x13, (byte) 0x26, (byte) 0x73, (byte) 0xC8, (byte) 0x8E, (byte) 0xE1, (byte) 0x7C, (byte) 0x70, (byte) 0x7D, (byte) 0x9E,
                            (byte) 0x2F, (byte) 0xE2, (byte) 0x4B, (byte) 0xEE, (byte) 0x77, (byte) 0x4D, (byte) 0xB9, (byte) 0x81, (byte) 0x23, (byte) 0xEA, (byte) 0xC4,
                            (byte) 0x72, (byte) 0xE7, (byte) 0xFA, (byte) 0x92, (byte) 0x3B, (byte) 0xC6, (byte) 0x38, (byte) 0x45, (byte) 0x97, (byte) 0x0D, (byte) 0x86,
                            (byte) 0xB3, (byte) 0x08, (byte) 0xD1, (byte) 0x15, (byte) 0x32, (byte) 0x49, (byte) 0x19, (byte) 0x7E};
    
    byte[] serial = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0x32, (byte) 0x9D, (byte) 0xE6, (byte) 0xE6,
        (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0xC2, (byte) 0x00, (byte) 0x01, 
        (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x01, (byte) 0x00, (byte) 0xFF, // 00 00 60 01 00 FF
        (byte) 0x02, (byte) 0x00,
        (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    byte[] fechaactual = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A, (byte) 0x03, (byte) 0x03, (byte) 0x54, (byte) 0x02, (byte) 0x9B, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x08, 
        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, // 00 00 01 00 00 FF
        (byte) 0x02, (byte) 0x00, (byte) 0x65, (byte) 0xD7, (byte) 0x7E};
    byte[] periodoint = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x03, (byte) 0x03, (byte) 0x76, (byte) 0x12, (byte) 0x99, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, 
        (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, // 01 00 63 01 00 FF
        (byte) 0x04, (byte) 0x00, (byte) 0x3A, (byte) 0x0C, (byte) 0x7E};
//    byte[] entradasperfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x02, (byte) 0x23, (byte) 0x61, (byte) 0x76, (byte) 0x12, (byte) 0x99, (byte) 0xE6,
//        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63,
//        (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x07, (byte) 0x00, (byte) 0x3A, (byte) 0x0C, (byte) 0x7E};
    byte[] confperfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A, (byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0x01, (byte) 0xBE, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, 
        (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, // 01 00 63 01 00 FF
        (byte) 0x03, (byte) 0x00, (byte) 0x32, (byte) 0x41, (byte) 0x7E};
    byte[] constant = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1A,(byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0x32, (byte) 0x9D, (byte) 0xE6, 
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x03, 
        (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0xFF, // 0100010800FF +A - 0208 -A - 0308 +R - 0408 -R e instantáneos con 07 en vez de 08
        (byte) 0x03, (byte) 0x00, (byte) 0x58, (byte) 0x57, (byte) 0x7E};
    byte[] perfilcarga = {(byte) 0x7E, (byte) 0xA0, (byte) 0x4D, (byte) 0x03, (byte) 0x03, (byte) 0x54, (byte) 0x70, (byte) 0x03, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, 
        (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, // 01 00 63 01 00 FF
        (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04,
        (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
        (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF,
        (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x0F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF,
        (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF, (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x17, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0xCC, (byte) 0x55, (byte) 0x7E};
    //registros
    byte[] registros = {(byte) 0x7E, (byte) 0xA0, (byte) 0x4D, (byte) 0x03, (byte) 0x03, (byte) 0x54, (byte) 0x70, (byte) 0x03, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, 
        (byte) 0x01, (byte) 0x00, (byte) 0x62,(byte) 0x01, (byte) 0x03, (byte) 0xFF, // diarios 01 00 63 02 00 FF - mensuales 00 00 62 01 00 FF
        (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04,
        (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, 
        (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x02, (byte) 0x00, (byte) 0xFF, 
        (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF,
        (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x0F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF,
        (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDF, (byte) 0x04, (byte) 0x14, (byte) 0xFF, (byte) 0x17, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0xCC, (byte) 0x55, (byte) 0x7E};
    byte[] powerQuality = {(byte) 0x7E, (byte) 0xA0, (byte) 0x2D, (byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0xAC, (byte) 0xBC, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x07, 
        (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x62, (byte) 0x04, (byte) 0xFF, // 00 00 63 62 04 FF
        (byte) 0x02, (byte) 0x00, (byte) 0x32, (byte) 0x41, (byte) 0x7E};//sin acceso selectivo
//        (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x04, (byte) 0x06, (byte) 0x00,
//        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12, (byte) 0x00,
//        (byte) 0x01, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7E};
    byte[] REQ_NEXT = {(byte) 0x7E, (byte) 0xA0, (byte) 0x14,(byte) 0x03, (byte) 0x03, (byte) 0x54, (byte) 0xAC, (byte) 0x47,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x02, (byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x01, (byte) 0x73, (byte) 0x7F, (byte) 0x7E};    
    byte[] RR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08,(byte) 0x03, (byte) 0x03, (byte) 0x71, (byte) 0x7D, (byte) 0xA3, (byte) 0x7E};
    byte[] prelogout = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x03, (byte) 0x03, (byte) 0x53, (byte) 0x6D, (byte) 0xA1,
        (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x62, (byte) 0x15, (byte) 0x80, (byte) 0x01, (byte) 0x00,
        (byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x1F, (byte) 0x04, (byte) 0x00, 
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 
        (byte) 0xFF, (byte) 0xFF, 
        (byte) 0x2C, (byte) 0xF7, (byte) 0x7E};
    byte[] logout = {(byte) 0x7E, (byte) 0xA0, (byte) 0x08, (byte) 0x03, (byte) 0x03, (byte) 0x53, (byte) 0x6D, (byte) 0xA1, (byte) 0x7E};
    byte[] confhora = {(byte) 0x7E, (byte) 0xA0, (byte) 0x28,(byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0x82, (byte) 0x7F, (byte) 0xE6,
        (byte) 0xE6, (byte) 0x00, (byte) 0xC1, (byte) 0x01, (byte) 0x81, (byte) 0x00, (byte) 0x08, 
        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, // 00 00 01 00 00 FF
        (byte) 0x02, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xE0, (byte) 0x05,
        (byte) 0x13, (byte) 0x04, (byte) 0x0E, (byte) 0x0D, (byte) 0x1E, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xBE, (byte) 0x7F, (byte) 0x7E};
    
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};
    
    public TramasDLMS(){
        inicializarHashMaps();
    }
    
    public final void inicializarHashMaps(){
        this.tramasSNRM.put("Honeywell_HS3400", snrmHoneywellHS3400 );
        this.tramasSNRM.put("Kaifa", snrmKaifa );
        this.tramasSNRM.put("Wasion_aMeter300", snrmWASIONAMeter300);
        this.tramasSNRM.put("Default_1", snrm);
        this.tramasSNRM.put("Default_2", snrm2);
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

    public HashMap<String, byte[]> getTramasSNRM() {
        return tramasSNRM;
    }

    public void setTramasSNRM(HashMap<String, byte[]> tramasSNRM) {
        this.tramasSNRM = tramasSNRM;
    }
    
    public byte[] getSnrm() {
        return snrm;
    }

    public void setSnrm(byte[] snrm) {
        this.snrm = snrm;
    }

    public byte[] getAarq() {
        return aarq;
    }

    public void setAarq(byte[] aarq) {
        this.aarq = aarq;
    }

    public byte[] getAARQIni() {
        return aARQIni;
    }

    public void setAARQIni(byte[] aARQIni) {
        this.aARQIni = aARQIni;
    }

    public byte[] getAARQACSEProtocolV(){
        return this.aARQACSEProtocolV;
    }
    public byte[] getAARQContextName() {
        return aARQContextName;
    }

    public byte[] getAARQAcse() {
        return aARQAcse;
    }

    public void setAARQAcse(byte[] aARQAcse) {
        this.aARQAcse = aARQAcse;
    }

    public void setAARQContextName(byte[] aARQContextName) {
        this.aARQContextName = aARQContextName;
    }

    public byte[] getAARQMechName() {
        return aARQMechName;
    }

    public void setAARQMechName(byte[] aARQMechName) {
        this.aARQMechName = aARQMechName;
    }

    public byte[] getaARQCalledAPTitle() {
        return aARQCalledAPTitle;
    }

    public void setaARQCalledAPTitle(byte[] aARQCalledAPTitle) {
        this.aARQCalledAPTitle = aARQCalledAPTitle;
    }

    public void setAARQApTitle(byte[] aARQAppTitle){
        this.aARQApTitle = aARQAppTitle;
    }
    
    public byte[] getAARQApTitle(){
        return this.aARQApTitle;
    }
    
    public byte[] getAARQAuthentication() {
        return aARQAuthentication;
    }

    public void setAARQAuthentication(byte[] aARQAuthentication) {
        this.aARQAuthentication = aARQAuthentication;
    }

    public byte[] getAARQUsrInfo() {
        return aARQUsrInfo;
    }

    public void setAARQUsrInfo(byte[] aARQUsrInfo) {
        this.aARQUsrInfo = aARQUsrInfo;
    }

    public byte[] getAARQConformance() {
        return aARQConformance;
    }

    public void setAARQConformance(byte[] aARQConformance) {
        this.aARQConformance = aARQConformance;
    }

    public byte[] getAARQMaxPDU() {
        return aARQMaxPDU;
    }

    public void setAARQMaxPDU(byte[] aARQMaxPDU) {
        this.aARQMaxPDU = aARQMaxPDU;
    }

    public byte[] getAARQFin() {
        return aARQFin;
    }

    public void setAARQFin(byte[] aARQFin) {
        this.aARQFin = aARQFin;
    }

    public byte[] getCosemDescriptor() {
        return cosemDescriptor;
    }

    public void setCosemDescriptor(byte[] cosemDescriptor) {
        this.cosemDescriptor = cosemDescriptor;
    }
        

    public byte[] getGetRequestNormal() {
        return getRequestNormal;
    }

    public void setGetRequestNormal(byte[] getRequestNormal) {
        this.getRequestNormal = getRequestNormal;
    }

    public byte[] getGetRequestAPDU() {
        return getRequestNormalAPDU;
    }

    public void setGetRequestAPDU(byte[] getRequestNormalAPDU) {
        this.getRequestNormalAPDU = getRequestNormalAPDU;
    }

    public byte[] getGetReqNextBlock() {
        return getReqNextBlock;
    }

    public void setGetReqNextBlock(byte[] getReqNextBlock) {
        this.getReqNextBlock = getReqNextBlock;
    }

    public byte[] getClockAccessSelectionDT() {
        return clockAccessSelectionDT;
    }

    public void setClockAccessSelectionDT(byte[] clockAccessSelectionDT) {
        this.clockAccessSelectionDT = clockAccessSelectionDT;
    }            
        
    public byte[] getClockAccessSelection() {
        return this.clockAccessSelection;
    }

    public void setClockAccessSelection(byte[] clockAccessSelection) {
        this.clockAccessSelection = clockAccessSelection;
    }
    
    
    public byte[] getActionRequestAPDU() {
        return actionRequestAPDU;
    }

    public void setActionRequestAPDU(byte[] actionRequestAPDU) {
        this.actionRequestAPDU = actionRequestAPDU;
    }
    
    public byte[] getSetRequestAPDUClock() {
        return setRequestAPDUClock;
    }

    public void setSetRequestAPDUClock(byte[] setRequestAPDUClock) {
        this.setRequestAPDUClock = setRequestAPDUClock;
    }
    
    public byte[] getSetRequestAPDUClockDT() {
        return setRequestAPDUClockDT;
    }

    public void setSetRequestAPDUClockDT(byte[] setRequestAPDUClockDT) {
        this.setRequestAPDUClockDT = setRequestAPDUClockDT;
    }  
    
    public byte[] getGloGetRequest() {
        return this.gloGetRequest;
    }

    public void setGloGetRequest(byte[] gloGetRequest) {
        this.gloGetRequest = gloGetRequest;
    }
    
    public byte[] getGloActionRequest() {
        return this.gloActionRequest;
    }

    public void setGloActionRequest(byte[] gloActionRequest) {
        this.gloActionRequest = gloActionRequest;
    }
    
    public byte[] getGloSetRequest() {
        return this.gloSetRequest;
    }

    public void setGloSetRequest(byte[] gloSetRequest) {
        this.gloSetRequest = gloSetRequest;
    }

    public byte[] getSerial() {
        return serial;
    }

    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

    public byte[] getFechaactual() {
        return fechaactual;
    }

    public void setFechaactual(byte[] fechaactual) {
        this.fechaactual = fechaactual;
    }

    public byte[] getPeriodoint() {
        return periodoint;
    }

    public void setPeriodoint(byte[] periodoint) {
        this.periodoint = periodoint;
    }

//    public byte[] getEntradasperfil() {
//        return entradasperfil;
//    }
//
//    public void setEntradasperfil(byte[] entradasperfil) {
//        this.entradasperfil = entradasperfil;
//    }

    public byte[] getConfperfil() {
        return confperfil;
    }

    public void setConfperfil(byte[] confperfil) {
        this.confperfil = confperfil;
    }

    public byte[] getPerfilcarga() {
        return perfilcarga;
    }

    public void setPerfilcarga(byte[] perfilcarga) {
        this.perfilcarga = perfilcarga;
    }

    public byte[] getRegistros() {
        return registros;
    }

    public void setRegistros(byte[] registros) {
        this.registros = registros;
    }

    public byte[] getPowerQuality() {
        return powerQuality;
    }

    public void setPowerQuality(byte[] powerQuality) {
        this.powerQuality = powerQuality;
    }

    public byte[] getREQ_NEXT() {
        return REQ_NEXT;
    }

    public void setREQ_NEXT(byte[] REQ_NEXT) {
        this.REQ_NEXT = REQ_NEXT;
    }

    public byte[] getRR() {
        return RR;
    }

    public void setRR(byte[] RR) {
        this.RR = RR;
    }

    public byte[] getLogout() {
        return logout;
    }

    public void setLogout(byte[] logout) {
        this.logout = logout;
    }

    public byte[] getPrelogout() {
        return prelogout;
    }

    public void setPrelogout(byte[] prelogout) {
        this.prelogout = prelogout;
    }
    
    public byte[] getConfhora() {
        return confhora;
    }

    public void setConfhora(byte[] confhora) {
        this.confhora = confhora;
    }

    public byte[] getConstant() {
        return constant;
    }

    public void setConstant(byte[] constant) {
        this.constant = constant;
    }
}
