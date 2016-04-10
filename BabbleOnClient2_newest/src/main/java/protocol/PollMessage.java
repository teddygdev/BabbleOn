package protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.currentTimeMillis;

/**
 *
 * @author Brandy
 */
public class PollMessage extends BabbleOnMessage{

    private int usernameLength;
    private String username;
    private long timestamp;
    
    public PollMessage(
    	int usernameLength,
	String username,
	long timestamp) throws BabbleException {
        super((byte)MessageType.PollMessage.ordinal(),
                Integer.BYTES+usernameLength+Long.BYTES);
        
        checkType(MessageType.PollMessage);
        
        this.usernameLength = usernameLength;
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.username = username;
        
        this.timestamp = timestamp;
        
        if(timestamp > currentTimeMillis()){
            throw new BabbleException("Cannot have future message time");
        }
        if(0 > timestamp){
            throw new BabbleException("Negative time not accepted");
        }
        
    }
    
    public PollMessage(InputStream in) throws BabbleException {
        super(in, true);
        checkType(MessageType.PollMessage);

        DataInputStream ds = new DataInputStream(in);
        
        this.usernameLength = readInt(ds, MAX_UNAME_LEN);
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.username = readStr(ds, usernameLength);
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.timestamp = readLong(ds);
        
        if(timestamp > currentTimeMillis()){
            throw new BabbleException("Cannot have future message time");
        }
        if(0 < timestamp){
            throw new BabbleException("Non-negative time not accepted");
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
            ds.writeLong(timestamp);
        }catch (IOException e) {
            throw new BabbleException("Mag Exception: " + e.getMessage(), e);
        }
    }
    
}
