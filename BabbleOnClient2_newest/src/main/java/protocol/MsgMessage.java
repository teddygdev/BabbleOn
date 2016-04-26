package protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author Brandy
 */
public class MsgMessage extends BabbleOnMessage{

    private static final int MAX_MSG_LEN = 15;
    private static final int MAX_MSGS = 500;
    private static final int HEADER_LEN = HEADER.length()+Byte.BYTES+Long.BYTES;
    
    private int senderUsernameLength;
    private String senderUsername;
    private int receiverUsernameLength;
    private String receiverUsername;
    private int messageLengthEncrypted;
    private String message;
    private long messageLenDecrypted;
    private int messageNumber;
    private int totalMessages;
    private long timestamp;
    private byte[] encrypted;
    
    public MsgMessage( 
        int senderUsernameLength,
	String senderUsername,
	int receiverUsernameLength,
	String receiverUsername,
	String message,
	long messageLength,
	int messageNumber,
	int totalMessages,
	long timestamp, 
        Cipher encrypt) throws BabbleException {
        
        super((byte)MessageType.MsgMessage.ordinal(),
                senderUsernameLength+receiverUsernameLength
                +messageLength+Long.BYTES*2+Integer.BYTES*5);
        
        checkType(MessageType.MsgMessage);
        
        this.senderUsernameLength = senderUsernameLength;
        
        if(0 > senderUsernameLength || MAX_UNAME_LEN < senderUsernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        if(null == senderUsername || senderUsernameLength != senderUsername.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.senderUsername = senderUsername;
        
        this.receiverUsernameLength = receiverUsernameLength;
        
        if(0 > receiverUsernameLength || MAX_UNAME_LEN < receiverUsernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        if(null == receiverUsername || receiverUsernameLength != receiverUsername.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.receiverUsername = receiverUsername;
        
        if(0 > messageLength || MAX_MSG_LEN < messageLength){
            throw new BabbleException("Invalid messageLength");
        }
        
        if(null == message || messageLength != message.length()){
            message = message == null ? "" : message ;
            throw new BabbleException("Invalid message length: " + messageLength + " v. " + message.length());
        }
        
        this.message = message;
        
        this.encrypted = doEncryption(encrypt, message);
        
        this.messageLenDecrypted = messageLength;
        
        if(messageLength != this.messageLenDecrypted){
            throw new BabbleException("Message Hashes do not match");
        }
        
        this.messageLengthEncrypted = encrypted.length;
        
        this.messageNumber = messageNumber;
        
        this.totalMessages = totalMessages;
        
        if(totalMessages < messageNumber){
            throw new BabbleException("Cannot have more than " + totalMessages +" messages");
        }
        if(0 >= totalMessages || 0 >= messageNumber){
            throw new BabbleException("Message count value invalid");
        }
        
        this.timestamp = timestamp;
        
        if(timestamp > currentTimeMillis()){
            throw new BabbleException("Cannot have future message time");
        }
//        if(0 < timestamp){
//            throw new BabbleException("Non-negative time not accepted");
//        }
    }
    
     public MsgMessage(InputStream in, Cipher decrypt) throws BabbleException, IOException {
        super(in, true);
        checkType(MessageType.MsgMessage);
        DataInputStream ds = new DataInputStream(in);
        
        this.senderUsernameLength = readInt(ds, MAX_UNAME_LEN);
        
        if(0 > senderUsernameLength || MAX_UNAME_LEN < senderUsernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.senderUsername = readStr(ds, senderUsernameLength);
        
        if(null == senderUsername || senderUsernameLength != senderUsername.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.receiverUsernameLength = readInt(ds, MAX_UNAME_LEN);
        
        if(0 > receiverUsernameLength || MAX_UNAME_LEN < receiverUsernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.receiverUsername = readStr(ds, receiverUsernameLength);
        
        if(null == receiverUsername || receiverUsernameLength != receiverUsername.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.messageLengthEncrypted = readInt(ds, Integer.MAX_VALUE);
        
        if(0 > messageLengthEncrypted){//|| MAX_MSG_LEN < messageLengthEncrypted){
            throw new BabbleException("Invalid usernameLength");
        }
        
        int read;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        
        byte[] chunk = new byte[decrypt.getBlockSize()];
        for(read = 0; read < messageLengthEncrypted-decrypt.getBlockSize(); read+=decrypt.getBlockSize()){
            ds.read(chunk);
            bs.write(decrypt.update(chunk));
        }
        
        if(read < messageLengthEncrypted){ 
            ds.read(chunk);
            
            try {
                bs.write(decrypt.doFinal(chunk));
            } catch (IllegalBlockSizeException | BadPaddingException ex) {
                throw new BabbleException("Bad stuffs happening in messages: ", ex);
            }
        }
        
        this.message = new String(bs.toByteArray(), CHSET);
        if(null == message){
            throw new BabbleException("Invalid username");
        }
        
        this.messageLenDecrypted = readLong(ds);
        if((long)message.length() != this.messageLenDecrypted || messageLenDecrypted > MAX_MSG_LEN*MAX_MSGS){
            throw new BabbleException("Message lengths do not match: " + message.length() + " : " + messageLenDecrypted);
        }
        
        this.messageNumber = readInt(ds, Integer.MAX_VALUE);
        
        this.totalMessages = readInt(ds, Integer.MAX_VALUE);
        
        if(totalMessages < messageNumber){
            throw new BabbleException("Cannot have more than " + totalMessages +" messages");
        }
        if(0 >= totalMessages || 0 >= messageNumber){
            throw new BabbleException("Message count value invalid");
        }
        
        this.timestamp = readLong(ds);
        
        if(timestamp > currentTimeMillis()){
            throw new BabbleException("Cannot have future message time");
        }
        if(0 > timestamp){
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

            ds.writeInt(senderUsernameLength);
            ds.write(senderUsername.getBytes(CHSET));
            ds.writeInt(receiverUsernameLength);
            ds.write(receiverUsername.getBytes(CHSET));
            ds.writeInt(encrypted.length);
            ds.write(encrypted);
            ds.writeLong(messageLenDecrypted);
            ds.writeInt(messageNumber);
            ds.writeInt(totalMessages);
            ds.writeLong(currentTimeMillis());
        }catch (IOException e) {
            throw new BabbleException("MsgException: " + e.getMessage(), e);
        }
    }

    public String getSenderUsername(){
        return senderUsername;
    }
    
    
    public String getReceiverUsername(){
        return receiverUsername;
    }
    
    public String getMessage(){
        return message;
    }
    
    
    public int getMessageNumber(){
        return messageNumber;
    }
    
    public int getTotalMessages(){
        return totalMessages;
    }
    
    public long getTimestamp(){
        return timestamp;
    }
    
    
    protected static long getLen(ArrayList<MsgMessage> newMessages) {
        long ret = 0;
        for(MsgMessage m : newMessages){
            ret = ret+m.senderUsernameLength+m.receiverUsernameLength
                +m.encrypted.length+Long.BYTES*2+Integer.BYTES*5; 
        }
        
        return ret;
    }
    
    private byte[] doEncryption(Cipher cipher, String message) throws BabbleException{
        byte[] b = new byte[0];
        try{
            int write;
            byte[] chunk = (message).getBytes(CHSET); 
            
            //loop though all chunks before final chunk
            for(write = 0; write < chunk.length-cipher.getBlockSize(); write+=cipher.getBlockSize()){  
                cipher.update(chunk, write, 
                        write+cipher.getBlockSize());
            }
            //get final chunk of encrypted data
            if(write < chunk.length){
                b = cipher.doFinal(chunk, write, chunk.length);
            }    
        } catch (IllegalBlockSizeException | BadPaddingException | IOException ex) {
                    throw new BabbleException("Bad stuffs happening in messages");        
        } 
        
        return b;
    }
}
