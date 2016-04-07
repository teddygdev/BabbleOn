package protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Brandy
 */
public class ChallengeMessage extends BabbleOnMessage{
    private static final int CHALLENGE_LEN = 8;
    
    private byte[] challenge;
    
    
    public ChallengeMessage(byte[] challenge) throws BabbleException {
        super((byte)MessageType.ChallengeMessage.ordinal(), CHALLENGE_LEN);
        checkType(MessageType.ChallengeMessage);
        
        if(CHALLENGE_LEN != challenge.length){
            throw new BabbleException("Invalid challenge");
        }
        this.challenge = challenge;
    }
    
    public ChallengeMessage(InputStream in) throws BabbleException {
        super(in, true);
        checkType(MessageType.ChallengeMessage);
        
        DataInputStream ds = new DataInputStream(in);
        
        challenge = new byte[CHALLENGE_LEN];
        for(int i = 0; i < CHALLENGE_LEN; i++){
            try{
                challenge[i] = ds.readByte();
            }catch(IOException e){
                throw new BabbleException("Invalid challenge length");
            }
        }
    }

    @Override
    public void encode(OutputStream out) throws BabbleException {
        throw new UnsupportedOperationException("Should not be sending challenges");
    }
    
    public byte[] getChallenge(){
        return challenge;
    }
    
    public void setChallenge(byte[] c){
        challenge = c;
    }
    
}
