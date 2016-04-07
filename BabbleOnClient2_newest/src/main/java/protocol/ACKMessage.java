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
public class ACKMessage extends BabbleOnMessage{
    //Used to manipulate the bits and to check value of low order bit
    private static final int BINARY = 2;
    //Number of bits in the error message signal
    private static final int UPPER = 7;
    //Max possible error specific value
    private static final int UPPER_BYTE_SIZE = (int)Math.pow(BINARY, UPPER);
    
    //Who sent the ACK message
    public static final int SERVER = 0;
    public static final int CLIENT = 1;
    
    //Byte encoded and decoded for ACK message
    byte errSender;
    
    //decoded error message
    String error;
    
    //Indication of message sender
    int sender;

    public ACKMessage(byte errSender) throws BabbleException {        
        super((byte)MessageType.ACKMessage.ordinal(), Byte.BYTES);
        checkType(MessageType.ACKMessage);
        
        this.errSender = errSender;
        
        error = getErr(findError());
        
        sender = errSender%BINARY;
    }

    public ACKMessage(InputStream is) throws BabbleException {        
        super(is, true);
        checkType(MessageType.ACKMessage);

        DataInputStream ds = new DataInputStream(is);
        
        try{
            this.errSender = ds.readByte();
        }catch(IOException e){
            throw new BabbleException("Impropper message format");
        }
        
        error = getErr(findError());
        
        sender = errSender%BINARY;
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
            ds.writeByte(errSender);
        }catch (IOException e) {
            throw new BabbleException("MsgException: " + e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @return 
     */
    private int findError(){
        int err = 0;
        err = errSender >>> (byte)1;
        return err;
    }
    
    private String getErr(int e){
        return Errors.getValue(e);
    }
    
    public void setErr(int e){
        if(UPPER_BYTE_SIZE > e){
            errSender = (byte)((errSender%2 + e) << 1);
        }
    }
    
    public String getErr(){
        return error;
    }
    
    public void setSender(int s){
        if(SERVER == s && errSender%BINARY != SERVER){
            errSender--;
        }
        else if(CLIENT == s && errSender%BINARY != CLIENT){
            errSender++;
        }
    }
    
    public int getSender(){
        return errSender%BINARY;
    }
}
