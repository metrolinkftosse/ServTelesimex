/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author Meli
 */
public final class encrypt_password {
    short tmp=0;
    short tmp2=0;
    byte[] key = new byte[4];
    
    public encrypt_password(){
    }
    
    public byte[] encrypt_password1(byte[] pwd, byte[] e_key) {
        
        
        /* add the arbitrary constant 0xAB41 to a copy of the key */
        
        tmp = (short)((e_key[3] & 0xFF)+0x41);
        key[3] = (byte) (tmp & 0xFF);        
        
        tmp = (short)(((tmp >>> 8) & 0x01) + (0xab & 0xFF) + (e_key[2] & 0xFF));
        key[2] = (byte) (tmp & 0xFF);
                     
        tmp = (short)(((tmp >>> 8) & 0x01) + (e_key[1] & 0xFF));        
        key[1] = (byte) (tmp & 0xFF);
        
        tmp = (short)(((tmp >>> 8) & 0x01) + (e_key[0] & 0xFF));
        key[0] = (byte) (tmp & 0xFF);
        tmp=0;
        
       /* During each step, the key is rotated left through a 33-bit
        * register. Only the low 32 bits are used in each step.
        *
        * At each step, the password is xor'd with the current low
        * 32 bits of the key.
        */
        for (int i=((key[3]+key[2]+key[1]+key[0]) & 0xF); i>=0; i--){
            for (int j=3; j>=0; j--){
                
                tmp |= key[j] << 1;
                key[j] = (byte)(tmp & 0xFF);
                pwd[j] ^= key[j]; 
                tmp = (short)((tmp >>> 8) & 1);
            }
        }
        
        return pwd;
}
    
}
