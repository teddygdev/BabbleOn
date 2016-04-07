/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

/**
 *
 * @author Brandy
 */
public enum MessageType {
    LoginMessage((byte) 0),
    ChallengeMessage((byte) 1),
    ResponseMessage((byte) 2),
    ACKMessage((byte) 3),
    MsgMessage((byte) 4),
    PollMessage((byte) 5),
    PollResponseMessage((byte) 6),
    DefaultMessage((byte)7);

    private byte messageType;

    MessageType(byte b){
        messageType = b;
    }
    
    public static MessageType getMessageType(byte b){
        MessageType returnVal;
        switch(b){
            case 0:
                returnVal = LoginMessage;
                break;
            case 1:
                returnVal = ChallengeMessage;
                break;
            case 2:
                returnVal = ResponseMessage;
                break;
            case 3:
                returnVal = ACKMessage;
                break;
            case 4:
                returnVal = MsgMessage;
                break;
            case 5: 
                returnVal = PollMessage;
                break;
            case 6:
                returnVal = PollResponseMessage;
                break;
            default:
                returnVal = null;
        }
        
        return returnVal;
    }
}
