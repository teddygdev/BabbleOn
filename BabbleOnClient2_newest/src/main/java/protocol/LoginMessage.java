package protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;

/**
 *
 * @author Brandy
 */
public class LoginMessage extends BabbleOnMessage{
    //Strings for pem key file generation
    private static final String KEY_FILE_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String KEY_FILE_FOOTER = "\n-----END PUBLIC KEY-----";
    //Algorithm for asymmetric key encryption
    public static final String ALGORITHM = "RSA";
    //Size of the asymmetric key encryption in hex bytes
    private static final int KEYSIZE = 220;
    //Length of the keyfile/keystring information
    private static final int KEY_LEN = KEY_FILE_HEADER.length()+KEYSIZE+KEY_FILE_FOOTER.length();
    
    //Length of the login message username
    private int usernameLength;
    //Username for login message
    private String username;
    //Type for login message - byte enumeration of KeyType enum
    private byte keyType;
    //Space for key if included in message type
    private String publicKeyStr;
    
    private Key publicKey;
    
    private byte[] encodedKey;
    
    public LoginMessage(int usernameLength, String username, KeyType keyType, Key publicKey) throws BabbleException {
        super((byte)MessageType.LoginMessage.ordinal(), 
                Integer.BYTES+usernameLength+Byte.BYTES+keyType.getKeyLen());
        checkType(MessageType.LoginMessage);
        
        this.usernameLength = usernameLength;
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.username = username;
        
        this.keyType = (byte)keyType.ordinal();
        
        if(KeyType.ASYMMETRIC == keyType && null == publicKey){
            throw new BabbleException("Invalid key");
        }
        else if(KeyType.ASYMMETRIC == keyType){
            this.publicKey = publicKey;
            this.publicKeyStr = calculateKey(publicKey);
        }
        
        this.keyType = (byte)keyType.ordinal();
        
    }    
    
    public LoginMessage(InputStream in) throws BabbleException {
        super(in, true);
        checkType(MessageType.LoginMessage);

        DataInputStream ds = new DataInputStream(in);
        
        try {
            // if(readMsg.hasNextInt()){
            usernameLength = ds.readInt();//readMsg.nextInt();
             if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
               throw new BabbleException("Username Length invalid");
           }
        } catch (IOException ex) {
            throw new BabbleException("Couldnt read len");
        }
        
        username = readStr(ds, usernameLength);
        
        if(usernameLength != username.length()){
            throw new BabbleException("Passed message len does not match actual message len");
        }
        
        try {
            //  if(readMsg.hasNextByte()){
            keyType = ds.readByte();
             if(keyType >= KeyType.DEFAULT.ordinal()){
                throw new BabbleException("Invalid Key Type");
            }
        } catch (IOException ex) {
            throw new BabbleException("Key Type not read");
        }
        
        if(keyType == KeyType.ASYMMETRIC.ordinal()){
            publicKeyStr = readStr(ds,KEY_LEN);
            publicKey = decodeKey(publicKeyStr);
        }

    }
    
    @Override
    public void encode(OutputStream out) throws BabbleException {
        if(null == out){
            throw new NullPointerException();
        }
        try{
            DataOutputStream ds = new DataOutputStream(out);
            ds.write((HEADER).getBytes(CHSET));
            ds.writeByte(messageType);
            ds.writeLong(messageLen);
            ds.writeInt(usernameLength);
            ds.write(username.getBytes(CHSET));
            ds.writeByte(keyType);
            if(KeyType.ASYMMETRIC.ordinal() == keyType){
                ds.write(publicKeyStr.getBytes(CHSET));
            }
            else if(KeyType.SYMMETRIC.ordinal() == keyType){
                ds.write(encodedKey);
            }
        }catch (IOException e) {
            throw new BabbleException("MsgException: " + e.getMessage(), e);
        }
    }
    
    public void encode(OutputStream out, CipherOutputStream cOut) throws BabbleException {
        if(null == out){
            throw new NullPointerException();
        }
        try{
            DataOutputStream ds = new DataOutputStream(out);
            ds.write((HEADER).getBytes(CHSET));
            ds.writeByte(messageType);
            ds.writeLong(messageLen);
            ds.writeInt(usernameLength);
            ds.write(username.getBytes(CHSET));
            ds.writeByte(keyType);
            ds.flush();
            if(KeyType.SYMMETRIC.ordinal() == keyType){
                //PrintWriter pw = new PrintWriter(
		//				new OutputStreamWriter(cOut));
                //cOut.write(publicKeyStr.getBytes(CHSET));
                //cOut.flush();
                //pw.print(publicKeyStr);
                //pw.checkError();
            }
        }catch (IOException e) {
            throw new BabbleException("MsgException: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String toString(){
        return HEADER + messageType + 
                String.format("%064x", new java.math.BigInteger(1,String.valueOf(messageLen).getBytes()))
                + usernameLength + username+publicKeyStr;
    }
    
	
    /**
     * Get username length
     * @return int length of username
     */
    public int getUsernameLen(){
        return usernameLength;
    }

    /**
     * sets username length
     * @param unameLen length of the username
     * @throws BabbleException if command invalid
     * @throws NullPointerException if command null
     */
    public void setUsernameLen(int unameLen)
      throws BabbleException{
        if(0 > unameLen || MAX_UNAME_LEN < unameLen){
            throw new BabbleException("BabbleException: invalid username length");
        }
        usernameLength = unameLen;
    }

    /**
     * Gets username
     * @return String array parameter list
     */
    public String getUsername(){
        return username;
    }

    /**
     * Sets username
     * @param uname String user name
     * @throws BabbleException if uname invalid
     * @throws NullPointerException if uname is null
     */
    public void setUsername(String uname)
      throws BabbleException{
        if(null == uname){
                throw new NullPointerException();
        }

        username = uname;
    }
    
    public String getPubKey(){
        return publicKeyStr;
    }
    
    public void setPubKey(String key) throws BabbleException{
        keyType = (byte)KeyType.ASYMMETRIC.ordinal();
        if(null == key || KeyType.ASYMMETRIC.getKeyLen() != key.length()){
            throw new BabbleException("Key of incorrect length");
        }
        this.publicKeyStr = key;
    }
    
    public String getSymKey(){
        return publicKeyStr;
    }
    
    public void setSymKey(String key) throws BabbleException{
        keyType = (byte)KeyType.SYMMETRIC.ordinal();
        if(null == key || KeyType.SYMMETRIC.getKeyLen() != key.length()){
            throw new BabbleException("Key of incorrect length");
        }
        this.publicKeyStr = key;
    }
    
    public byte getKeyType(){
        return keyType;
    }
    
    public String calculateKey(Key key)throws BabbleException {
        try {
            String keyRet = null;
            Base64 encoder = new Base64();
            
            if(KeyType.ASYMMETRIC.ordinal() == keyType){
                StringBuilder pemCertPre = new StringBuilder();
                char [] bytes = new String((encoder.encode(((RSAPublicKey)key).getEncoded())),
                        CHSET).toCharArray();
                int i;
                for(i = 0; i< bytes.length; i++){
                    if(0 == i%64){
                        pemCertPre.append("\n");
                    }
                    pemCertPre.append(bytes[i]);
                }
                keyRet = KEY_FILE_HEADER + pemCertPre.toString() + KEY_FILE_FOOTER;
            }
            else if(KeyType.SYMMETRIC.ordinal() == keyType){
                keyRet = new String((encoder.encode(((SecretKey)key).getEncoded())),
                        CHSET);
            }
            
            return keyRet;

        } catch (UnsupportedEncodingException ex) {
            throw new BabbleException("Could not encode key " + ex);
        }
    }
    
    public Key decodeKey(String key) throws BabbleException {
        Key retKey = null;
        
        try {
            String justKey = key.substring(KEY_FILE_HEADER.length(), key.length()-KEY_FILE_FOOTER.length());

            ByteBuffer keyBytes = new BASE64Decoder().decodeBufferToByteBuffer(justKey);
            System.out.println("Key: " + key + "\nDecoded: " + Arrays.toString(keyBytes.array()));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes.array());
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            RSAPublicKey pk = (RSAPublicKey) kf.generatePublic(keySpec);

            retKey = pk;
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
            throw new BabbleException("Cannot decode key: "+ ex.getMessage());
        }

       return retKey;

    }
    
    public void setEncodedKey(byte[] key){
        encodedKey = key;
    }
}
