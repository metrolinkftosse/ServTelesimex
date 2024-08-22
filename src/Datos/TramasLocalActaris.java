/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author dperez
 */
public class TramasLocalActaris {
    //handshake 1

    //Primer trama saludo por optico (TRAMA FIJA)
    byte[] A = {(byte) 0x2F, (byte) 0x3F, (byte) 0x21, (byte) 0x0D, (byte) 0x0A,};
    //Trama C (TRAMA FIJA)
    byte[] C = {(byte) 0x06, (byte) 0x32, (byte) 0x35, (byte) 0x32, (byte) 0x0D, (byte) 0x0A,};
    
    byte[] FRMR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x05, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x97, (byte) 0xF9, (byte) 0x5C, (byte) 0x7E};
    
    
    //Trama SNRM/UA (TRAMA FIJA) CAMBIA PARA LECTURA DE EVENTOS
    byte[] T1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x21, (byte) 0x00, 
    (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x93, (byte) 0x0B,
    (byte) 0x14, (byte) 0x81, (byte) 0x80, (byte) 0x12, (byte) 0x05, (byte) 0x01,
    (byte) 0x80, (byte) 0x06, (byte) 0x01, (byte) 0x80, (byte) 0x07, (byte) 0x04,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x04,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x41, (byte) 0x18,
    (byte) 0x7E};
    byte[] T1_1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x21, (byte) 0x00,
    (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x93, (byte) 0x0B,
    (byte) 0x14, (byte) 0x81, (byte) 0x80, (byte) 0x12, (byte) 0x05, (byte) 0x01,
    (byte) 0x80, (byte) 0x06, (byte) 0x01, (byte) 0x80, (byte) 0x07, (byte) 0x04,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x04,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x53, (byte) 0x3B,
    (byte) 0x7E};
//    byte[] T1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x21, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x23,
//    (byte) 0x05, (byte) 0x93, (byte) 0x0B, (byte) 0x14, (byte) 0x81, (byte) 0x80, (byte) 0x12, (byte) 0x05,
//    (byte) 0x01, (byte) 0x80, (byte) 0x06, (byte) 0x01, (byte) 0x80, (byte) 0x07, (byte) 0x04, (byte) 0x00,
//    (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//    (byte) 0x03, (byte) 0x41, (byte) 0x18, (byte) 0x7E,};
    //Trama Application Association Request (AARQ)(puede cambiar contrasena medidor, FCS, en vatia la contrasena es de A hasta H)
    byte[] T3 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x47, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x10, (byte) 0xD0, (byte) 0x5E, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x60, (byte) 0x36,
        (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0x8B,
        (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0xAC, (byte) 0x0A, (byte) 0x80, (byte) 0x08, (byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44,
        (byte) 0x45, (byte) 0x46, (byte) 0x47, (byte) 0x48, (byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x1F, (byte) 0x04,
        (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x1D, (byte) 0x00, (byte) 0x00, (byte) 0x3B, (byte) 0xB9, (byte) 0x7E,};
    //Trama RR (cambia el byte 9 o byte de control aqui se deja en 00, tambien cambia el HCS, aqui se deja en cero)
    byte[] RR = {(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7E,};
    //Trama peticion de firmware (cambia: control, HCS, FCS)
    byte[] Tfirmware = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x32, (byte) 0x8D, (byte) 0x58, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x8E, (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x86, (byte) 0xCA, (byte) 0x7E};
    //Trama peticion numero de serie medidor (cambia: control, HCS, FCS)
    byte[] Tserie = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x54, (byte) 0xBD, (byte) 0x5E, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x5E, (byte) 0xA8, (byte) 0x7E};
    //Trama peticion hora y fecha actual
    byte[] TfechaHora = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x76, (byte) 0xAD, (byte) 0x5C, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x65, (byte) 0xD7, (byte) 0x7E};
     //Trama syncronizacion hora.
    byte[] TfechaSync = {(byte) 0x7E, (byte) 0xA0, (byte) 0x2A, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x76, (byte) 0xAD, (byte) 0x5C, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC1, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xDD, (byte) 0x08,
        (byte) 0x10, (byte) 0x05, (byte) 0x03, (byte) 0x1C, (byte) 0x1E, (byte) 0xFF, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x65, (byte) 0xD7, (byte) 0x7E
    };
    //Trama parametros perfil de carga1 ((cambia: control, HCS, FCS))
    byte[] Tparametrosperfil1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x98, (byte) 0xDD, (byte) 0x52, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x38, (byte) 0xD9, (byte) 0x7E};
    //Trama informacion del perfil de carga
    byte[] TinfoPerfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xBA, (byte) 0xCD, (byte) 0x50, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x80, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x44, (byte) 0x58, (byte) 0x7E};
    //Trama Fecha y hora actual CASO 2 despues del perfil, depende del firmware ((cambia: control, HCS, FCS))
    byte[] TfechaHora2 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xFF, (byte) 0x77, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xD5, (byte) 0xE4, (byte) 0x7E};
    //Trama peticion perfil de carga1 (cambia: control, HCS, FCS)
    byte[] Tperfil1 = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x1E, (byte) 0xB5, (byte) 0x4F, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x00, (byte) 0x09,
        (byte) 0x0C, (byte) 0x07, (byte) 0xDB, (byte) 0x06, (byte) 0x03, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x09, (byte) 0x0C, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x9F, (byte) 0x9E, (byte) 0x7E};
    

    byte[] getRequestAcumulados = {(byte) 0x7E, (byte) 0xA0, (byte) 0x3D, (byte) 0x00, (byte) 0x022, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFC, (byte) 0xBA, (byte) 0x1E,
                                (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01, (byte) 0x81, 
                                (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00,                                 
                                (byte) 0x8C, (byte) 0x6D, (byte) 0x7E};
    
    //Trama siguiente bloque perfil de carga (cambia: control, numero de bloque, HCS, FCS)        
    byte[] TbloquePerfil = {(byte) 0x7E, (byte) 0xA0, (byte) 0x16, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xB0, (byte) 0xC0, (byte) 0x16, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x02,
        (byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x73, (byte) 0x7F, (byte) 0x7E};
    byte [] Tfinaliza ={(byte) 0x7E, (byte) 0xA0, (byte) 0x0A, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x53, (byte) 0x06, (byte) 0xC7, (byte) 0x7E};

    byte[] PowerFailureElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x98, (byte) 0xDD, (byte) 0x52, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x82, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x77, (byte) 0x52, (byte) 0x7E,};

    byte[] SwellElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFA, (byte) 0xC9, (byte) 0x12, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x0A, (byte) 0x02, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0xCB, (byte) 0x3A, (byte) 0x7E,};

    byte[] SagElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x7C, (byte) 0xF7, (byte) 0xF3, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x0A, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x06, (byte) 0x1F, (byte) 0x7E,};

    byte[] CutElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xFE, (byte) 0xED, (byte) 0x54, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x0A, (byte) 0x03, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x70, (byte) 0x26, (byte) 0x7E,};

    byte[] BatteryUseTimeCounter = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x70, (byte) 0x9B, (byte) 0x39, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x06, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0xF5, (byte) 0x7E,};

    byte[] CoverOpeningElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x92, (byte) 0x87, (byte) 0xFD, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x82, (byte) 0x05, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x20, (byte) 0x3C, (byte) 0x7E,};

    byte[] CurrentReversalElements = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xF4, (byte) 0xB7, (byte) 0xFB, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x80, (byte) 0x22, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x65, (byte) 0x73, (byte) 0x7E,};

    byte[] SuccessfullProgrammingSummary = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x56, (byte) 0xAF, (byte) 0x7D, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x02, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xE8, (byte) 0x31, (byte) 0x7E,};

    byte[] ConfigurationSummary = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x78, (byte) 0xD3, (byte) 0xB5, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x02, (byte) 0x0A, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x46, (byte) 0xED, (byte) 0x7E,};

    byte[] SwellSummary = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x9A, (byte) 0xCF, (byte) 0x71, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x62, (byte) 0x81, (byte) 0x0A, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x81, (byte) 0x16, (byte) 0x7E,};

    byte[] SagSummary = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0xDC, (byte) 0xFD, (byte) 0x56, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x62, (byte) 0x81, (byte) 0x00, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x2F, (byte) 0xCA, (byte) 0x7E,};

    byte[] CutSummary = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x1E, (byte) 0xE3, (byte) 0xB3, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x62, (byte) 0x81, (byte) 0x14, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0x62, (byte) 0x7B, (byte) 0x7E,};

    byte[] PowerFailureSummary = {(byte) 0x7E, (byte) 0xA0, (byte) 0x1C, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x23, (byte) 0x03, (byte) 0x50, (byte) 0x99, (byte) 0x18, (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC0, (byte) 0x01,
        (byte) 0x81, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x62, (byte) 0x82, (byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0x00, (byte) 0xE7, (byte) 0x4A, (byte) 0x7E,};
    
    byte[] TariffSummary = { (byte) 0x7e,  (byte) 0xa0,  (byte) 0x1c,  (byte) 0x00,  (byte) 0x22,  (byte) 0x00,  (byte) 0x23,  (byte) 0x07,  (byte) 0x9c,  (byte) 0x99,  (byte) 0x73,  (byte) 0xe6,  (byte) 0xe6,  (byte) 0x00,  (byte) 0xc0, 
        (byte) 0x01,  (byte) 0x41,  (byte) 0x00,  (byte) 0x07,  (byte) 0x00,  (byte) 0x00,  (byte) 0x62,  (byte) 0x85,  (byte) 0x01,  (byte) 0xff,  (byte) 0x02,  (byte) 0x00,  (byte) 0x25,  (byte) 0x25,  (byte) 0x7e };

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

    public byte[] getA() {
        return A;
    }

    public void setA(byte[] A) {
        this.A = A;
    }

    public byte[] getC() {
        return C;
    }

    public void setC(byte[] C) {
        this.C = C;
    }

    public byte[] getFRMR() {
        return FRMR;
    }

    public void setFRMR(byte[] FRMR) {
        this.FRMR = FRMR;
    }   
    
    public byte[] getRR() {
        return RR;
    }

    public void setRR(byte[] RR) {
        this.RR = RR;
    }

    public byte[] getT1() {
        return T1;
    }

    public void setT1(byte[] T1) {
        this.T1 = T1;
    }

    public byte[] getT1_1() {
        return T1_1;
    }

    public void setT1_1(byte[] T1_1) {
        this.T1_1 = T1_1;
    }
    

    public byte[] getT3() {
        return T3;
    }

    public void setT3(byte[] T3) {
        this.T3 = T3;
    }

    public byte[] getTbloquePerfil() {
        return TbloquePerfil;
    }

    public void setTbloquePerfil(byte[] TbloquePerfil) {
        this.TbloquePerfil = TbloquePerfil;
    }

    public byte[] getTfechaHora() {
        return TfechaHora;
    }

    public void setTfechaHora(byte[] TfechaHora) {
        this.TfechaHora = TfechaHora;
    }

    public byte[] getTfechaHora2() {
        return TfechaHora2;
    }

    public void setTfechaHora2(byte[] TfechaHora2) {
        this.TfechaHora2 = TfechaHora2;
    }

    public byte[] getTfechaSync() {
        return TfechaSync;
    }

    public void setTfechaSync(byte[] TfechaSync) {
        this.TfechaSync = TfechaSync;
    }

    
    public byte[] getTfirmware() {
        return Tfirmware;
    }

    public void setTfirmware(byte[] Tfirmware) {
        this.Tfirmware = Tfirmware;
    }

    public byte[] getTinfoPerfil() {
        return TinfoPerfil;
    }

    public void setTinfoPerfil(byte[] TinfoPerfil) {
        this.TinfoPerfil = TinfoPerfil;
    }

    public byte[] getTparametrosperfil1() {
        return Tparametrosperfil1;
    }

    public void setTparametrosperfil1(byte[] Tparametrosperfil1) {
        this.Tparametrosperfil1 = Tparametrosperfil1;
    }

    public byte[] getTperfil1() {
        return Tperfil1;
    }

    public void setTperfil1(byte[] Tperfil1) {
        this.Tperfil1 = Tperfil1;
    }

    public byte[] getGetRequestAcumulados() {
        return getRequestAcumulados;
    }

    public void setGetRequestAcumulados(byte[] getRequestAcumulados) {
        this.getRequestAcumulados = getRequestAcumulados;
    }        

    public byte[] getTserie() {
        return Tserie;
    }

    public void setTserie(byte[] Tserie) {
        this.Tserie = Tserie;
    }

    public byte[] getTfinaliza() {
        return Tfinaliza;
    }

    public void setTfinaliza(byte[] Tfinaliza) {
        this.Tfinaliza = Tfinaliza;
    }

    public byte[] getBatteryUseTimeCounter() {
        return BatteryUseTimeCounter;
    }

    public void setBatteryUseTimeCounter(byte[] BatteryUseTimeCounter) {
        this.BatteryUseTimeCounter = BatteryUseTimeCounter;
    }

    public byte[] getConfigurationSummary() {
        return ConfigurationSummary;
    }

    public void setConfigurationSummary(byte[] ConfigurationSummary) {
        this.ConfigurationSummary = ConfigurationSummary;
    }

    public byte[] getCoverOpeningElements() {
        return CoverOpeningElements;
    }

    public void setCoverOpeningElements(byte[] CoverOpeningElements) {
        this.CoverOpeningElements = CoverOpeningElements;
    }

    public byte[] getCurrentReversalElements() {
        return CurrentReversalElements;
    }

    public void setCurrentReversalElements(byte[] CurrentReversalElements) {
        this.CurrentReversalElements = CurrentReversalElements;
    }

    public byte[] getCutElements() {
        return CutElements;
    }

    public void setCutElements(byte[] CutElements) {
        this.CutElements = CutElements;
    }

    public byte[] getCutSummary() {
        return CutSummary;
    }

    public void setCutSummary(byte[] CutSummary) {
        this.CutSummary = CutSummary;
    }

    public byte[] getPowerFailureElements() {
        return PowerFailureElements;
    }

    public void setPowerFailureElements(byte[] PowerFailureElements) {
        this.PowerFailureElements = PowerFailureElements;
    }

    public byte[] getPowerFailureSummary() {
        return PowerFailureSummary;
    }

    public void setPowerFailureSummary(byte[] PowerFailureSummary) {
        this.PowerFailureSummary = PowerFailureSummary;
    }

    public byte[] getSagElements() {
        return SagElements;
    }

    public void setSagElements(byte[] SagElements) {
        this.SagElements = SagElements;
    }

    public byte[] getSagSummary() {
        return SagSummary;
    }

    public void setSagSummary(byte[] SagSummary) {
        this.SagSummary = SagSummary;
    }

    public byte[] getSuccessfullProgrammingSummary() {
        return SuccessfullProgrammingSummary;
    }

    public void setSuccessfullProgrammingSummary(byte[] SuccessfullProgrammingSummary) {
        this.SuccessfullProgrammingSummary = SuccessfullProgrammingSummary;
    }

    public byte[] getSwellElements() {
        return SwellElements;
    }

    public void setSwellElements(byte[] SwellElements) {
        this.SwellElements = SwellElements;
    }

    public byte[] getSwellSummary() {
        return SwellSummary;
    }

    public void setSwellSummary(byte[] SwellSummary) {
        this.SwellSummary = SwellSummary;
    }

    public byte[] getTariffSummary() {
        return TariffSummary;
    }

    public void setTariffSummary(byte[] TariffSummary) {
        this.TariffSummary = TariffSummary;
    }        
    
    public    byte[] StringToHexMarcado(String s) {

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
                //System.out.print(Byte.toString(bytee[i]));
//                System.out.println(Integer.toHexString(bytee[i]));

            }
        }

        return bytee;
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
