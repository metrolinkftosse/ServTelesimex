/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

/**
 *
 * @author jgarcia
 */
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author jgarcia
 */
public class AES_GCM {
  
  private static final byte[] hexArgs = {'0', '1', '2', '3', '4', '5','6', '7', '8', '9', 'a', 'b','c', 'd', 'e', 'f'};
  private final Cipher cipher;
  private byte[] EK = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A,
                       (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F};//Llave de encriptado por defecto, con un set se podría cambiar facilmente. 
  private byte[] AK = {(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, 
                       (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF};//Llave de autenticado por defecto, con un set se podría cambiar facilmente.
  private byte[] IV = new byte[12];
  private SecretKey OK;  
  
  public AES_GCM(byte[] EK, byte[] AK, byte[] APT, byte[] IC) throws NoSuchAlgorithmException, NoSuchPaddingException{
      this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
      if (EK != null){
        this.EK = EK;
      }
      if (AK != null){
        this.AK = AK;
      }      
      System.arraycopy(APT, 0, this.IV, 0, 8);
      System.arraycopy(IC, 0, this.IV, 8, 4);
      this.OK = new SecretKeySpec ( this.EK, 0, this.EK.length, "AES");
    }
    public byte[] getEK() {
        return this.EK;
    }

    public void setEK(byte[] EK) {
        this.EK = EK;
        this.OK = new SecretKeySpec ( this.EK, 0, this.EK.length, "AES");
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
   
       
    public byte[] encrypt(byte[] plaintext) throws Exception {
        byte[] aad = new byte[this.AK.length+1];
        aad[0] = (byte) 0x30;
        System.arraycopy(this.AK, 0, aad, 1, this.AK.length);
        increaseCounter();
        GCMParameterSpec parameterSpec = new GCMParameterSpec(96, this.IV); //128 bit auth tag length
        cipher.init(Cipher.ENCRYPT_MODE, this.OK, parameterSpec);

        if (this.AK != null) {
            cipher.updateAAD(aad);
        }

        byte[] cipherText = cipher.doFinal(plaintext);

        ByteBuffer byteBuffer = ByteBuffer.allocate(this.IV.length + cipherText.length);
        byteBuffer.put(this.IV);
        byteBuffer.put(cipherText);
        return byteBuffer.array();
    }

    public byte[] decrypt(byte[] cipherMessage) throws Exception {
        //use first 12 bytes for iv
        byte[] aad = new byte[this.AK.length+1];
        aad[0] = (byte) 0x30;
        System.arraycopy(this.AK, 0, aad, 1, this.AK.length);
        AlgorithmParameterSpec gcmIv = new GCMParameterSpec(96, cipherMessage, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, this.OK, gcmIv);

        if (aad != null) {
            cipher.updateAAD(aad);
        }
        //use everything from 12 bytes on as ciphertext
        byte[] originalText = cipher.doFinal(cipherMessage, 12, cipherMessage.length - 12);

        return originalText;
    }
    
    public void increaseCounter(){
        int byteP = 1;
        if ((this.IV[this.IV.length - byteP] & 0xFF) == 255){
            this.IV[this.IV.length - byteP] = 0;
            byteP ++;            
            if ((this.IV[this.IV.length -  byteP] & 0xFF) == 255){
                this.IV[this.IV.length - byteP] = 0;
                byteP ++;
                if ((this.IV[this.IV.length -  byteP] & 0xFF) == 255){
                    this.IV[this.IV.length - byteP] = 0;
                    byteP ++;
                    this.IV[this.IV.length - byteP] += 1; 
                } else {
                    this.IV[this.IV.length - byteP] += 1; 
                }
            } else {
                this.IV[this.IV.length - byteP] += 1; 
            }
        } else {
            this.IV[this.IV.length - byteP] += 1; 
        }
    }
    
    public byte[] increaseCounter (byte[] extInvCounter){
        int byteP = 1;
        if ((extInvCounter[extInvCounter.length - byteP] & 0xFF) == 255){
            extInvCounter[extInvCounter.length - byteP] = 0;
            byteP ++;            
            if ((extInvCounter[extInvCounter.length - byteP] & 0xFF) == 255){
                extInvCounter[extInvCounter.length - byteP] = 0;
                byteP ++;
                if ((extInvCounter[extInvCounter.length - byteP] & 0xFF) == 255){
                    extInvCounter[extInvCounter.length - byteP] = 0;
                    byteP ++;
                    extInvCounter[extInvCounter.length - byteP] += 1; 
                } else {
                    extInvCounter[extInvCounter.length - byteP] += 1; 
                }
            } else {
                extInvCounter[extInvCounter.length - byteP] += 1; 
            }
        } else {
            extInvCounter[extInvCounter.length - byteP] += 1; 
        }
        return extInvCounter;
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

