/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securityjavaclient;

import java.util.logging.Level;
import java.util.logging.Logger;
import protocol.BabbleException;
import protocol.BabbleOnMessage;

/**
 *
 * @author Brandy
 */
class ReaderThread extends Thread{
    private BabbleOnClient frame;
    
    @Override
    public void run(){
        
        boolean working = false;
        while(true) {   
            if(!working){
                try {
                    working = true;
                    //Sort through server messages
                    BabbleOnMessage incoming = new BabbleOnMessage(frame.getInputStream(), false);                        
                    frame.getInputStream().mark(100);
                    
                    switch(incoming.checkMessageType()){
                        case ACKMessage:
                            System.out.println("ACK");
                            working = frame.handleACK();
                            break;
                        case ChallengeMessage:
                            System.out.println("Challenge");
                            working = frame.handleChallenge();
                            break;
                        case LoginMessage:
                            System.out.println("Login");
                            working = frame.handleLogin();
                            break;
                        case PollResponseMessage:
                            System.out.println("PollResponse");
                            working = frame.handlePollResponse();
                            break;
                        default:
                            working = false;
                            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.WARNING,
                                    null, incoming.getMessageType() + " not expected message type");
                        }
                        
                } catch (BabbleException ex) {
                    Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }
    
    public void setFrame(BabbleOnClient f){
        frame = f;
    }
}
