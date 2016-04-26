package protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Brandy
 */
public class BabbleOnMessage {
    //The header for the SPRT protocol
    protected static final String HEADER = "SPChat/1.0";
    //carriage return value
    protected static final String CR = "\r";
    //line feed value
    protected static final String LF = "\n";
    //crlf value for comparison
    protected static final String CRLF = "\r\n";
    //space value for insertion into strings and comparison
    protected static final String SPACE = " ";
    //character set of choice
    public static final String CHSET = "ASCII";
    //ensure characters are alphanumeric
    protected static final String REGEX = "^[!-~]$";
    //Maximum length of username
    protected static final int MAX_UNAME_LEN = 64;

    //Which type the message is
    protected byte messageType = (byte)MessageType.DefaultMessage.ordinal();
    
    //Length of the encoded message
    long messageLen;

    /**
     * Constructs a SpRTMessage with deserialization
     * @param type for the type of the given message
     * @param messageLen of the encoded message
     * @throws BabbleException if given value gives an error
     * @throws NullPointerException if parameter is null
     */
    public BabbleOnMessage(byte type, long messageLen)
        throws BabbleException{
            messageType = type;
            
            if(messageType >= MessageType.DefaultMessage.ordinal() ){
                throw new BabbleException("Invalid Message Type");
            }
            
            if(messageLen > 0){
                this.messageLen = messageLen;
            }
    }

    /**
     * Construct SpRTMessage with deserialization
     * @param in input stream to deserialize from 
     * @throws BabbleException if decoding or I/O problems
     *         or validation problems occur
     * @throws NullPointerException if in is null
     */
    public BabbleOnMessage(InputStream in, boolean sup)
        throws BabbleException{
            if(null == in){
                throw new NullPointerException();
            }
            if(!sup){
                DataInputStream ds = new DataInputStream(in);
                try {
                    String strg = readStr(ds, HEADER.length());
                    System.out.println(strg);
                    if(strg.isEmpty()){
                        throw new NullPointerException("No header");
                    }
                    else if(!(HEADER.equals(strg))){
                            throw new BabbleException("Decoding failure: broke protocol");
                    }

                    messageType = ds.readByte();
                    
                    messageLen = ds.readLong();
                    
                    if(0 > messageLen){
                        throw new BabbleException("Message length cannot be negative");
                    }

                } catch (BabbleException | IOException e) {
                    throw new BabbleException("BabbleException: " + e.getMessage());
                } 
            }
    }


    /**
     * Encodes a Message to the given output stream. Abstract method 
     * implemented by subclasses
     * @param out serialization output destination
     * @throws BabbleException if I/O problems
     * @throws NullPointerException if out is null
     */
    public  void encode(OutputStream out)
        throws BabbleException{};

    /**
     * Returns the type of the message
     * @return Byte type of message
     */
    public byte getMessageType(){
            return messageType;
    }

    /**
     * Set type
     * @param type new messageType for the Message
     * @throws BabbleException if invalid command
     * @throws NullPointerException if function is null
     */
    public void setMessageType(byte type)
        throws BabbleException{
            messageType = type;
    }
    
    public MessageType checkMessageType(){
        return MessageType.getMessageType(messageType);
    }
    
    /**
     * 
     * @param readMsg scanner with the input stream to read from
     * @param maxValue of the integer to be read
     * @return the integer read from the scanner
     * @throws BabbleException 
     */
    protected final int readInt(DataInputStream readMsg, int maxValue) throws BabbleException{
        int tempLen = -1;
        try {
            tempLen = readMsg.readInt();
             if(0 > tempLen || maxValue < tempLen){
               throw new BabbleException("Integer invalid");
           }
        } catch (IOException ex) {
            throw new BabbleException("Integer could not be read");
        }

        return tempLen;
    }
    
    /**
     * Reads a string from the given scanner
     * @param readMsg scanner input stream to read from
     * @param len expected length of the message
     * @return the string read from the scanner
     * @throws BabbleException if the message length is unexpected or read error occurs
     */
    protected final String readStr(DataInputStream readMsg, int len) throws BabbleException{
        byte [] b = new byte[len];
        try {
            readMsg.readFully(b);
        } catch (IOException ex) {
            throw new BabbleException("Could not read full string");
        }

        String s = "";
        try{
            s = new String(b,CHSET);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BabbleOnMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return s;
    }
    
    /**
     * Reads a long value from the input scanner
     * No bounds checking guaranteed
     * @param readMsg the scanner to read from
     * @return long value read from input stream
     * @throws BabbleException if problem reading long from scanner
     */
    protected final long readLong(DataInputStream ds) throws BabbleException{
        long tempLen;
        try{
           tempLen = ds.readLong();
        }catch(IOException e){
            throw new BabbleException("Bad message formatting");
        }
        return tempLen;
    }
    
    /**
     * Ensures the instance message matches passed message type
     * @param m passed message type
     * @throws BabbleException if types do not match
     */
    protected final boolean checkType(MessageType m) throws BabbleException{
        boolean r = true;
        if(messageType != (byte)m.ordinal()){
            messageType = (byte)m.ordinal();
            r = false;
        }
        return r;
    }
}
