/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author jgarcia
 */
public class AES_GMAC {

    private static final byte[] hexArgs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final Cipher cipher;
    private byte[] EK = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A,
                       (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F};//Llave de encriptado por defecto, con un set se podría cambiar facilmente. 
    private byte[] AK = {(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, 
                       (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF};//Llave de autenticado por defecto, con un set se podría cambiar facilmente.
    private byte[] IV = new byte[12];
    private SecretKey OK;

    public AES_GMAC(byte[] EK, byte[] AK, byte[] APT, byte[] IC) throws NoSuchAlgorithmException, NoSuchPaddingException {
      this.cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
      if (EK != null){
        this.EK = EK;
      }
      if (AK != null){
        this.AK = AK;
      }      
      System.arraycopy(APT, 0, this.IV, 0, 8);
      System.arraycopy(IC, 0, this.IV, 8, 4);
      this.OK = new SecretKeySpec ( this.EK, "AES");
    }
    
    public byte[] getEK() {
        return this.EK;
    }

    public void setEK(byte[] EK) {
        this.EK = EK;
        this.OK = new SecretKeySpec(this.EK, "AES");
    }
    
    public byte[] getAK(){
       return this.AK;
   }
   
   public void setAK(byte[] AK){
       this.AK = AK;
   }
   
   public byte[] getIV(){
       return this.IV;
   }
   
   public void setIV(byte[] IV){
       this.IV = IV;
   }
   
   public byte[] getAPTitle(){
       return Arrays.copyOfRange(this.IV, 0, 8); 
   }
   
   public void setAPTitle(byte[] APTitle){
       System.arraycopy(APTitle, 0, this.IV, 0, 8);
   }
   
   public byte[] getIC(){
       return Arrays.copyOfRange(this.IV, 8, this.IV.length); 
   }
   
   public void setIC(byte[] IC){
       System.arraycopy(IC, 0, this.IV, 8, 4);
   } 
   
   public byte[] generateGmac( byte[] StoC, byte SC) throws Exception {        
        
        byte[] aadData = buildAadForAuthenticationOnly(StoC, this.AK, SC);
        System.out.println("AAD Data Text : " + encode(aadData, aadData.length));

        // Generate GMAC
        this.cipher.init(ENCRYPT_MODE, this.OK, new GCMParameterSpec(96, this.IV), new SecureRandom());
        this.cipher.updateAAD(aadData);
        byte[] gmac = cipher.doFinal();
        System.out.println("GMAC Text : " + encode(gmac, gmac.length));
        return gmac;
    }
   public byte[] verifyGmac( byte[] CtoS, byte SC) throws Exception {
       
       byte[] aadData = buildAadForAuthenticationOnly(CtoS, this.AK, SC);
       System.out.println("AAD Data Text : " + encode(aadData, aadData.length));
       
       // Verify GMAC
       this.cipher.init(DECRYPT_MODE, this.OK, new GCMParameterSpec(96, this.IV), new SecureRandom());
       this.cipher.updateAAD(aadData);
       this.cipher.update(CtoS);
       byte[] verifiedGmac = this.cipher.doFinal();
       System.out.println("Verified GMAC : " + encode(verifiedGmac, verifiedGmac.length));
       return verifiedGmac;
       
   }
   
    private static byte[] buildAadForAuthenticationOnly(byte[] message, byte[] authenticatedKey, byte SC) throws IOException {
        ByteArrayOutputStream aaDoutputStream = new ByteArrayOutputStream();
        aaDoutputStream.write(SC);
        aaDoutputStream.write(authenticatedKey);
        aaDoutputStream.write(message);
        return aaDoutputStream.toByteArray();
    }
    public static String encode(byte[] b, int ancho) {

    StringBuilder s = new StringBuilder(2 * b.length);

    for (int i = 0; i < ancho; i++) {

      int v = b[i] & 0xff;
      if (i != 0) {
        s.append(" ");
      }
      s.append((char) hexArgs[v >> 4]);
      s.append((char) hexArgs[v & 0xf]);
    }

    return s.toString().toUpperCase();
  }

}
