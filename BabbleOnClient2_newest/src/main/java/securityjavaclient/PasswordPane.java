/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securityjavaclient;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import protocol.BabbleException;
import protocol.KeyType;
import protocol.LoginMessage;

/**
 *
 * @author Brandy
 */
public class PasswordPane extends JPanel
                          implements ActionListener {
    //OK button command
    private static String OK = "ok";
    //Length of the username input box
    private static final int UNAME_LEN = 20;
    //Length of the password input box
    private static final int PASSWD_LEN = 20;
    //Scrub username
    private static final String REGEX = "^[a-zA-Z0-9.@_-]*$";
    

    //Frame for dialogs to stem from
    private JFrame controllingFrame; 
    //Password field for pw
    private JPasswordField passwordField;
    //Username text entry field
    private JTextField usernameField;
    
    //Frame output stream for use in pw pane
    private static OutputStream out;
    //Frame input stream for use in pw pane
    private static InputStream in;
    //Username of <as entered in text field>
    private static String username;
    //Client public key
    private static PublicKey publicKey;
    //Spawning client
    private BabbleOnClient client;
    
    /**
     * Creates a password pane for user to input username/password
     * @param f controlling frame
     */
    public PasswordPane(JFrame f) {
        //Use the default FlowLayout.
        controllingFrame = f;
 
        usernameField = new JTextField(UNAME_LEN);
        usernameField.setActionCommand(OK);
        usernameField.addActionListener(this);
        
        JLabel labelUsername = new JLabel("Enter username: ");
        labelUsername.setLabelFor(usernameField);
        
        //Create password
        passwordField = new JPasswordField(PASSWD_LEN);
        passwordField.setActionCommand(OK);
        passwordField.addActionListener(this);
 
        JLabel labelPassword = new JLabel("Enter password: ");
        labelUsername.setLabelFor(passwordField);
 
        JComponent buttonPane = createButtonPanel();
 
        //Lay out everything.
        JPanel textPane1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        textPane1.add(labelUsername);
        textPane1.add(usernameField);
        JPanel textPane2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        textPane2.add(labelPassword);
        textPane2.add(passwordField);
 
        Box alignPane = new Box(BoxLayout.Y_AXIS);
        
        alignPane.add(textPane1);
        alignPane.add(textPane2);
        alignPane.add(buttonPane);
        
        add(alignPane);
    }
 
    /**
     * Creates OK button on panel
     * @return the Button panel
     */
    protected JComponent createButtonPanel() {
        JPanel p = new JPanel(new GridLayout(0,1));
        JButton okButton = new JButton("OK");
 
        okButton.setActionCommand(OK);
        okButton.addActionListener(this);
 
        p.add(okButton);
 
        return p;
    }
    
    /**
     * Deals with 'ok' button push storing username and password for eventual checking
     * on the server side
     * @param e button event (or other event; unevaluated)
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
 
        if (OK.equals(cmd)) { 
            username = usernameField.getText();
            checkPassword();
            
            if(!isValidUsername()){
                JOptionPane.showMessageDialog(controllingFrame,
                "Invalid username. Try again.",
                "Error Message",
                JOptionPane.ERROR_MESSAGE);
            }
            
            //Clear Text Fields
            usernameField.setText("");
            passwordField.setText("");
        } 
    }
 
    /**
     * Sets the username and pw to the client 
     */
    public void checkPassword(){
        //Add username and password to babbleOnClient if the fields are not empty
        if(null != client && passwordField.getPassword().length > 0 
                && !usernameField.getText().isEmpty()){
            client.setInput(passwordField.getPassword());
            client.setUsername(usernameField.getText());
        }

        passwordField.setText("");
        resetFocus();
    }
   
 
    //Must be called from the event dispatch thread.
    protected void resetFocus() {
        passwordField.requestFocusInWindow();
    }

    /**
     * Sends username in login attempt to server
     * @return success or failure of login message
     */
    private boolean isValidUsername(){        
        boolean ret = true;
        LoginMessage loginAttempt = null;
        
        if(null != out){
            try {
                if(username.length() > 0 && username.matches(REGEX)){
                    
                    loginAttempt = new LoginMessage(username.length(), username, 
                           KeyType.NONE,null);

                    loginAttempt.encode(out);
                }
                else{
                    ret = false;
                }
            } catch (BabbleException ex) {
                ret = false;
                Logger.getLogger(PasswordPane.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        else{
            ret = false;
        }
        
        return ret;
    }
    
    /**
     * Set the local output stream
     * @param out password panes new OS
     */
    public void setOutputStream(OutputStream out){
        PasswordPane.out = out;
    }
 
    /**
     * Getter for OS
     * @return OS
     */
    public OutputStream getOutputStream(){
        return out;
    }
    
    /**
     * 
     * @param in 
     */
    public void setInputStream(InputStream in){
        PasswordPane.in = in;
    }
    
    public InputStream getInputStream(){
        return in;
    }
    
    public void setPubKey(PublicKey p){
        publicKey = p;
    }
    
    public PublicKey getPubKey(){
        return publicKey;
    }
    
    public void setClient(BabbleOnClient c){
        client = c;
    }
    
}

