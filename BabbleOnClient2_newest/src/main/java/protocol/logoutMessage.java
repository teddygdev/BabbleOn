package protocol;

import java.io.OutputStream;

/**
 *
 * @author Brandy
 */
public class logoutMessage extends BabbleOnMessage{

    public logoutMessage(byte type, long num) throws BabbleException {
        super(type, num);
    }

    @Override
    public void encode(OutputStream out) throws BabbleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
