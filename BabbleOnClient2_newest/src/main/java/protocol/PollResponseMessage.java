package protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;

/**
 *
 * @author Brandy
 */
public class PollResponseMessage extends BabbleOnMessage{
    private static final int MAX_LISTS = 100;

    private int usernameLength;
    private String username;
    private int messageListSize;
    private int listNumber;
    private int totalLists;
    private List<MsgMessage> newMessages = new ArrayList<>();
    
    public PollResponseMessage(
        int usernameLength,
	String username,
	int messageListSize,
	int listNumber,
	int totalLists,
	ArrayList<MsgMessage> newMessages) throws BabbleException {
        super((byte)MessageType.PollResponseMessage.ordinal(),
                Integer.BYTES*4+usernameLength+MsgMessage.getLen(newMessages));
        
        checkType(MessageType.PollResponseMessage);
        
        this.usernameLength = usernameLength;
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.username = username;
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.messageListSize = messageListSize;
        
        if(0 > messageListSize){
            throw new BabbleException("Message size cannot be negative");
        }
        
        this.listNumber = listNumber;
        
        this.totalLists = totalLists;
        
        if(listNumber > totalLists){
            throw new BabbleException("List number beyond total list limit");
        }
        
        if(0 >= listNumber || 0 >= totalLists){
            throw new BabbleException("List number/TotalLists must be positive");
        }
        
        if(null == newMessages || newMessages.isEmpty()){
            throw new BabbleException("No messages");
        }
        
        this.newMessages = newMessages;
    }

    public PollResponseMessage(InputStream in, Cipher decrypt) throws BabbleException {
        super(in, true);

        checkType(MessageType.PollResponseMessage);
        
        DataInputStream ds = new DataInputStream(in);
        
        this.usernameLength = readInt(ds, MAX_UNAME_LEN);
        
        if(0 > usernameLength || MAX_UNAME_LEN < usernameLength){
            throw new BabbleException("Invalid usernameLength");
        }
        
        this.username = readStr(ds, usernameLength);
        
        if(null == username || usernameLength != username.length()){
            throw new BabbleException("Invalid username");
        }
        
        this.listNumber = readInt(ds, MAX_LISTS);
        this.totalLists = readInt(ds, MAX_LISTS);
        
        if(0 >= listNumber || 0 >= totalLists){
            throw new BabbleException("Message count value invalid");
        }
        
        if(totalLists < listNumber){
            throw new BabbleException("Cannot have more than " + totalLists +" lists");
        }
        
        this.messageListSize = readInt(ds, Integer.MAX_VALUE);
        
        if(0 > messageListSize){
            throw new BabbleException("Invalid message size");
        }
        
        if(null == decrypt){
            throw new BabbleException("Cannot decrypt");
        }
        
        for(int i = 0; i < messageListSize; i++){
            try { 
                
                newMessages.add(new MsgMessage(in, decrypt));
            } catch (IOException ex) {
                throw new BabbleException("Could not decrypt messages: "+ex.getMessage());
            }
        }
    }
    
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
            ds.writeInt(listNumber);
            ds.writeInt(totalLists);
            ds.writeInt(messageListSize);
            for(MsgMessage msg: newMessages){
                msg.encode(out);
            }
        }catch (IOException e) {
            throw new BabbleException("MsgException: " + e.getMessage(), e);
        }
    }
    
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getUsername(){
        return username;
    }
    
    public int getMessageListSize(){
        return messageListSize;
    }
    
    public int getListNumber(){
        return listNumber;
    }
    
    public int getTotalLists(){
        return totalLists;
    }
    
    public List<MsgMessage> getNewMessages(){
        return newMessages;
    }
}
