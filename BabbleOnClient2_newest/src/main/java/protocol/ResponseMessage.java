package protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Brandy
 */
public class ResponseMessage extends BabbleOnMessage{
    
    private int usernameLength;
    private String username;
    private int hashLen;
    private String hash; 
    private long nonce;
   

    public ResponseMessage(
        int usernameLength,
	String username,
        int fLen,
        String hashF,
        long nonce) throws BabbleException {
        
        super((byte)MessageType.ResponseMessage.ordinal(),
                Integer.BYTES*2+usernameLength+fLen+Long.BYTES);
        checkType(MessageType.ResponseMessage);
        
        if(fLen != hashF.length()){
            throw new BabbleException("Invalid hash length");
        }
        
        this.hashLen = fLen;
        
        this.hash = hashF;
        
        if(this.hashLen != this.hash.length()){
            throw new BabbleException("Hash lens do not match");
        }
        
        this.nonce = nonce;
        
        this.usernameLength = usernameLength;
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.username = username;
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
    }

    public ResponseMessage(InputStream in) throws BabbleException {
        super(in, true);

        checkType(MessageType.ResponseMessage);
        
        DataInputStream ds = new DataInputStream(in);
        
        this.usernameLength = readInt(ds, MAX_UNAME_LEN);
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.username = readStr(ds, usernameLength);
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.hashLen = readInt(ds, Integer.MAX_VALUE);
        
        if(0 > hashLen){
            throw new BabbleException("Invalid functionLength");
        }
        
        this.hash = readStr(ds, hashLen);
        
        if(this.hashLen != this.hash.length()){
            throw new BabbleException("Hash length mismatch");
        }
        
        this.nonce = readLong(ds);
        
        //Probably check nonce too
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
            ds.writeInt(hashLen);
            ds.write(hash.getBytes(CHSET));
            ds.writeLong(nonce);
        }catch (IOException e) {
            throw new BabbleException("Response Msg Exception: " + e.getMessage(), e);
        }
    }
    
    
    public String getUsername(){
        return username;
    }
    
    public long getNonce(){
        return nonce;
    }
    
    public void setNonce(long nonce){
        this.nonce = nonce;
    }
    
    public final static long generateNonce(){
        return (long)(Math.random() * Long.MAX_VALUE);
    }

    public String getHash(){
        return hash;
    }
    
    public void setHash(String hash){
        this.hash = hash;
    }
}
