/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securityjavaclient;

import static java.lang.System.currentTimeMillis;
import java.util.logging.Level;
import java.util.logging.Logger;
import protocol.BabbleException;

/**
 *
 * @author Brandy
 */
class PollThread extends Thread{
    public static final int WAIT_TIME = 5000;
    
    private BabbleOnClient frame;
    private long lastPollTime = currentTimeMillis();
    private boolean serverConnected = true;
    
    @Override
    public void run(){
        while(serverConnected){
            if(currentTimeMillis() > lastPollTime+WAIT_TIME){
                try {
                    frame.pollServer(lastPollTime);
                    lastPollTime = currentTimeMillis();
                    Thread.sleep(WAIT_TIME);
                } catch (BabbleException | InterruptedException ex) {
                    Logger.getLogger(PollThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void setFrame(BabbleOnClient f){
        frame = f;
    }
    
    public void setLastPollTime(long time){
        lastPollTime = time;
    }
    
    public void setServerConnected(boolean conn){
        serverConnected = conn;
    }
}
