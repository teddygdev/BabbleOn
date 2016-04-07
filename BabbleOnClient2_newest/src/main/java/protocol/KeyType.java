/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

/**
 * Possible key types for the login message
 * Each key type is paired with an expected key length
 * Login messages with a keyType of NONE or DEFAULT expect no key
 * @author Brandy
 */
public enum KeyType {
    NONE(0),ASYMMETRIC(271),SYMMETRIC(128), DEFAULT(0);
    
    private int keyLen;
    
    KeyType(int keyLen){
        this.keyLen = keyLen;
    }
    
    public static int getKeyLen(byte k){
        int retLen = 0;
        for(KeyType kt : KeyType.values()){
            if(k == (byte)kt.ordinal()){
                retLen = kt.keyLen;
            }
        }
        return retLen;
    }
    
    public int getKeyLen(){
        return keyLen;
    }
}
