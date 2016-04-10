package securityjavaclient;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import static java.lang.System.currentTimeMillis;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import protocol.*;

/**
 *
 * @author Brandy
 * 
 * Code modified from "TCP/IP Sockets in JavaTM: Practical Guide for Programmers, 
 * Second Edition" by Kenneth L. Calvert and Michael J. Donahoo
 */
public class BabbleOnClient extends JFrame{

    //Algorithm for asymmetric key encryption
    private static final String ALGORITHM = "RSA";
    private static final String ALGORITHM_CRYPT = "RSA/ECB/PKCS1Padding";
    //Algorithm for symmetric encryption
    private static final String SYM_ALGORITHM = "AES";
    private static final String SYM_ALGORITHM_CRYPT = "AES/CBC/PKCS5Padding";
    //Expected username for server
    private static final String SERVER_NAME = "";
    //Secure hash algorithm
    private static final String HASH_ALGORITHM = "SHA-256";
    //New line for displaying messages
    private static final String NEWLINE = System.getProperty("line.separator");
    //Maximum length of a single message
    private static final int MAX_MSG_LEN = 15;
    //Maximum length of message list
    private static final int MAX_MSGS = 500;
    //Maximum length for the username
    private static final int UNAME_LEN = 32;
    //symmetric key size
    private static final int SYM_KEY_SIZE = 128;
    //symmetric iv
    private static final String SYM_IV_STRING = "QtIkUCmeW0XTCybi";
    //Scrub username
    private static final String REGEX = "^[a-zA-Z0-9.@_-]*$";
    

    //has the user been authenticated
    private boolean authenticated = false;
    //Client's username
    private String username;
    //Username for a message/message set
    private String recipientUsername;
    //Last time polling server for new messages
    private long lastPollTime = 0;
    //Input store
    private byte[] input;
    
    //Public key received from the server for symm key encryption
    private static PublicKey serverPubKey;
    //Public key generated by client for symm key encryption
    private static PublicKey localPublicKey;
    //Private key generated by client
    private static PrivateKey localPrivateKey;
    //Symmetric key shared between server and client for message encryption
    private static SecretKey symmetricKey;
    // Client socket
    private Socket socket;
    // Socket input stream
    private DataInputStream in; 
    // Socket output stream
    private OutputStream out; 
    
    //Ciphers for encryption and decryption 
    private Cipher cipherSymmetricOut = null;
    private Cipher cipherSymmetricIn = null;
    
    // Message text areas
    private final JTextArea incomingMessageBox = new JTextArea();
    private final JTextArea outgoingMessageBox = new JTextArea();
    //Create "to" space
    private JTextArea usernameText = new JTextArea();
    //Create and set up the content pane.
    private final PasswordPane loginPane = new PasswordPane(this);
    
    private JPanel frameContent;
    
    private JButton receiverButton = new JButton();
    private JButton outgoingMessageButton = new JButton();
    
    //Polling thread
    private PollThread poller;
    private boolean hasExchangedKeys = false;
    
    
    public static void main(String[] args) {
        if ((args.length < 2) || (args.length > 3)) {
            throw new IllegalArgumentException("Parameter(s): <keyFileName> <Server> [<Port>]");
        }
        String server = args[1];
        int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;
        String filename = args[0];
        BabbleOnClient frame = new BabbleOnClient(server, servPort, filename);
        
        frame.setVisible(true);
        ReaderThread reader = new ReaderThread();
        reader.setFrame(frame);
        reader.start();
  }

    /**
     * Creates a JFrame for the client
     * @param server address for the client to attach to
     * @param servPort the server is hosted at
     * @param filename key file name (not currently used)
     */
  public BabbleOnClient(String server, int servPort, String filename) { 
    super("BabbleOn"); // Set the window title
    setSize(900, 900); // Set the window size
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    frameContent = new JPanel();
    frameContent.setLayout(new GridLayout());

    setContentPane(frameContent);
    
    usernameText.setName("Recipient");
    usernameText.setEditable(true);
    usernameText.setToolTipText("Recipient name");
    
    receiverButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {
               
            recipientUsername = usernameText.getText().trim();
            
            if(!recipientUsername.matches(REGEX)){
                recipientUsername = "";
                usernameText.setText("");
            }
        }
        
    });
    
    receiverButton.setText("Enter Recipient Name");
    receiverButton.setVisible(false);
    
    outgoingMessageButton.setText("Send");
    outgoingMessageButton.setVisible(false);

    incomingMessageBox.setEditable(false);
    incomingMessageBox.setVisible(false);   
    
    try {
        KeyPair keyPair = getKeyPair(filename);
        
        socket = new Socket(server, servPort);//SSLSocketFactory.getDefault().createSocket(server, servPort); 

        //Prepare in and output for server client communication
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        
        loginPane.setOpaque(true); //content panes must be opaque
        frameContent.add(loginPane, "1");
        
        loginPane.setInputStream(in);
        loginPane.setOutputStream(out);
        loginPane.setPubKey(keyPair.getPublic());
        loginPane.setClient(this);
        localPublicKey = keyPair.getPublic();
        localPrivateKey = keyPair.getPrivate();
        symmetricKey = getSymmKey();
        
        //Make sure the focus goes to the right component
        //whenever the frame is initially given the focus.
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                loginPane.resetFocus();
            }
        });

        //Display the window.
        pack();
        setVisible(true);
        
        //List of messages to send 
        ArrayList<MsgMessage> outMsgs = new ArrayList<>(); 
        
      outgoingMessageButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (outgoingMessageBox.getText().length() > 0 
                  && outgoingMessageBox.getText().length() < (MAX_MSG_LEN*MAX_MSGS)) {
            String message = outgoingMessageBox.getText();
            
            try {
                if(null == cipherSymmetricOut){
                    cipherSymmetricOut = Cipher.getInstance(SYM_ALGORITHM_CRYPT);
                    cipherSymmetricOut.init(Cipher.ENCRYPT_MODE, symmetricKey, new IvParameterSpec(SYM_IV_STRING.getBytes(BabbleOnMessage.CHSET)));
                }
                
                int messages = (int)Math.ceil(message.length()/(double)MAX_MSG_LEN);
                for(int i = 1; i <= messages; i++){
                    if(null == recipientUsername && !usernameText.getText().isEmpty()){
                        recipientUsername = usernameText.getText();
                    }
                    else if(null == recipientUsername){
                        JOptionPane.showMessageDialog(loginPane,
                            "Please enter a Recipient Username");
                    }
                    else{
                        int messageLen = message.length()-(i-1)*MAX_MSG_LEN < MAX_MSG_LEN ? message.length()%MAX_MSG_LEN : MAX_MSG_LEN;
                        int max = messageLen < MAX_MSG_LEN ? message.length() : MAX_MSG_LEN*i;
                        String messageBit = message.substring((i-1)*MAX_MSG_LEN, max);

                        outMsgs.add(new MsgMessage(username.length(), username, recipientUsername.length(),
                                recipientUsername, messageBit, messageLen,
                                i, messages, currentTimeMillis(), cipherSymmetricOut));
                        
                    }
                }
                
                if(!outMsgs.isEmpty()){
                    PollResponseMessage sendMsg = new PollResponseMessage(username.length(),
                            username, outMsgs.size(), 1, 1, outMsgs);
                    sendMsg.encode(out, cipherSymmetricOut);


                    outgoingMessageBox.setText("");
                    outMsgs.clear();
                }
            } catch (BabbleException ex) {
                  Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    out.close();
                } catch (IOException ex1) {
                    Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
              } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
                  Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, ex.getMessage());
              } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
          }
          else if(outgoingMessageBox.getText().length() < (MAX_MSG_LEN*MAX_MSGS)){
              JOptionPane.showMessageDialog(outgoingMessageBox,
                "Message was too long.");
          }
        }

      });

      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          try {
            socket.close();
          } catch (Exception exception) {
          }
          System.exit(0);
        }
      });
    } catch (IOException exception) {
      
    }   catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, ex.getMessage());
    }
  }

  /**
   * Generates an asymmetric key pair for the client
   * @param filename containing key pair
   * @return generated keypair
   * @throws NoSuchAlgorithmException 
   */
    private KeyPair getKeyPair(String filename) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        
        KeyPair k = keyGen.genKeyPair();
        
        return k;
    }
    
    /**
     * Generates symmetric key for encryption use
     * @return Secret key
     * @throws NoSuchAlgorithmException if algorithm does not exist
     */
    private SecretKey getSymmKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGen = KeyGenerator.getInstance(SYM_ALGORITHM);
        
        keyGen.init(SYM_KEY_SIZE);
        //SecretKey key = keyGen.generateKey();
        
        return keyGen.generateKey();
    }
    
    /**
     * Extracts server public key from login message
     * @param loginResponse message containing key 
     * @throws BabbleException if problem decoding login message or regenerating key
     */
    private void readServerPublic(LoginMessage loginResponse) throws BabbleException{
        if(null == loginResponse.getUsername() || !SERVER_NAME.equals(loginResponse.getUsername())){
            throw new BabbleException("Expected response from server");
        }

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(loginResponse.decodeKey(loginResponse.getPubKey()).getEncoded());

        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(ALGORITHM);
            serverPubKey = keyFactory.generatePublic(pubKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Sends the client's symmetric key encrypted with the local private key and the 
     * server public key for authentication and integrity
     */
    public void sendSymmetricKey(){
        Cipher cipherServerPublic = null;
        Cipher cipherLocalPrivate = null;

        try {
            if(null != localPrivateKey && null != serverPubKey && null != out && null != symmetricKey){
                cipherLocalPrivate = Cipher.getInstance(ALGORITHM_CRYPT);
                cipherLocalPrivate.init(Cipher.WRAP_MODE,localPrivateKey);
                
                cipherServerPublic = Cipher.getInstance(ALGORITHM_CRYPT);
                cipherServerPublic.init(Cipher.WRAP_MODE, serverPubKey);
                
                byte [] encrypted = (cipherServerPublic.wrap(symmetricKey));
                LoginMessage l = new LoginMessage(username.length(),username,KeyType.SYMMETRIC, null);
                l.setEncodedKey(encrypted);
                l.encode(out);


                hasExchangedKeys = true;
            }
            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BabbleException ex) {
            Logger.getLogger(PasswordPane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    /**
     * Handles ACK message determining possible error or success status
     * @return false to release hold on reader progress while processing
     * @throws BabbleException if problem decoding message
     */
    public boolean handleACK() throws BabbleException {
        ACKMessage ack = new ACKMessage(in);
        if(!authenticated && Errors.PASSWORD_AUTH.name().equals(ack.getErr())&& 
                ACKMessage.SERVER == ack.getSender()){
            authenticated = true;
            try{        
                setPanes();
            } catch (BabbleException ex) {
                Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null,ex);
                try{
                in.close();
                out.close();
                } catch(IOException e){
                    System.err.println("Issues closing connections " + e.getMessage());
                }
                System.exit(1);
            }
            
            LoginMessage loginAttempt = null;
        
            if(null != out){
                try {
                        loginAttempt = new LoginMessage(username.length(), username, 
                               KeyType.ASYMMETRIC, localPublicKey);

                        loginAttempt.encode(out);
                } catch (BabbleException ex) {
                    Logger.getLogger(PasswordPane.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        
        }else if(!Errors.SUCCESS.name().equals(ack.getErr()) && ACKMessage.SERVER == ack.getSender()){
            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, ack.getErr());
            
            if(Errors.INVALID_USERNAME_PASSWORD.name().equals(ack.getErr())){
                JOptionPane.showMessageDialog(loginPane,
                    "Incorrect Username/Password");
            }
        }
        else if(ACKMessage.CLIENT == ack.getSender()){
            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, "Unexpected client error message");
        }
        else{
            if(Errors.SUCCESS.name().equals(ack.getErr()) && hasExchangedKeys){
                poller = new PollThread();
                poller.setFrame(this);
                poller.start();
            }
            if(Errors.INVALID_RECIPIENT.name().equals(ack.getErr())){
                JOptionPane.showMessageDialog(loginPane,
                    "Please enter a valid recipient username");
            }
            //AUTHENTICATE MSGS RECEIVED
        }
        return false;
    }

    /**
     * Deals with a challenge msg and sends response
     * @return false to release hold on progress during processing
     * @throws BabbleException if cannot decode challenge
     */
    public boolean handleChallenge() throws BabbleException {
        ChallengeMessage c = new ChallengeMessage(in);
        
        try {
            if(input != null && input.length > 0){
                MessageDigest msgDigest = MessageDigest.getInstance(HASH_ALGORITHM);
                long nonce = ResponseMessage.generateNonce();
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                
                b.write(c.getChallenge());
                b.write(ByteBuffer.allocate(Long.BYTES).putLong(nonce).array());
                b.write(input);

                String hashed =  String.format("%064x", new java.math.BigInteger(1,msgDigest.digest(b.toByteArray())));

                ResponseMessage resp = new ResponseMessage(username.length(), username, 
                        hashed.length(), hashed, nonce);

                resp.encode(out);

                //Zero out the possible password, for security.
                Arrays.fill(input, (byte)'0');
                
            }
            else{
                JOptionPane.showMessageDialog(loginPane,
                    "Please enter Username and Password");
            }
            
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Handle login message if server sent public key
     * @throws BabbleException 
     */
    public boolean handleLogin() throws BabbleException {
        LoginMessage login = new LoginMessage(in);
        if(login.getKeyType() == (byte)KeyType.ASYMMETRIC.ordinal()){
            readServerPublic(login);
            sendSymmetricKey();
        }
        return false;
    }

    /**
     * Handle response of poll message with new messages
     * @throws BabbleException 
     */
    public boolean handlePollResponse() throws BabbleException {
        if(null == cipherSymmetricIn){
            try {
                cipherSymmetricIn = Cipher.getInstance(SYM_ALGORITHM_CRYPT);
                cipherSymmetricIn.init(Cipher.DECRYPT_MODE, symmetricKey, 
                        //new IvParameterSpec(cipherSymmetricOut.getIV()));
                        new IvParameterSpec(SYM_IV_STRING.getBytes(BabbleOnMessage.CHSET)));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException 
                    | InvalidKeyException | InvalidAlgorithmParameterException ex) {
                throw new BabbleException("Problems with decrypt cipher: ", ex);
            } catch (UnsupportedEncodingException ex) {
                throw new BabbleException("Problems with decrypt (encoding) cipher: ", ex);
            }
        }
        
        PollResponseMessage resp = new PollResponseMessage(in, cipherSymmetricIn);
        StringBuilder messages = new StringBuilder();
        messages.append("From: ").append(resp.getUsername()).append(NEWLINE);
        
        for(MsgMessage msg : resp.getNewMessages()){
            if(msg.getReceiverUsername().equals(username)
                    && msg.getSenderUsername().equals(resp.getUsername())){
                messages.append(msg.getMessage());
                if(msg.getMessageNumber() == resp.getMessageListSize()){
                    messages.append(NEWLINE).append(new Date(msg.getTimestamp())).append(NEWLINE);
                }
            }
        }
        incomingMessageBox.setText(incomingMessageBox.getText()+messages.toString());
        return false;
    }

    /**
     * Sends poll message to server
     * @param timestamp last poll time
     * @throws BabbleException 
     */
    public void pollServer(long timestamp) throws BabbleException {
        if(null != username){
            PollMessage poll = new PollMessage(username.length(), username, timestamp);
            poll.encode(out);
        }
    }

    /**
     * Gets the sockets input stream for reader
     * @return input stream
     */
    public InputStream getInputStream() {
        return in;
    }
    
    /**
     * Sets pw input 
     * @param in chars to store
     */
    public void setInput(char[] in){
        input = new byte[in.length];
        for(int i = 0; i < in.length; i++){
            input[i] = (byte)in[i];
        }
    }
    
    /**
     * Sets username before 
     * @param username 
     * 
     * Optionally, to use client with echo server bypassing login but only being
     * able to send messages to yourself, uncomment try-catch block.
     */
    public void setUsername(String username){
        this.username = username;
        
//        try{        
//            setPanes();
//        } catch (BabbleException ex) {
//            Logger.getLogger(BabbleOnClient.class.getName()).log(Level.SEVERE, null,ex);
//        }
    }
    
    private void setPanes() throws BabbleException{
        try{        
            JOptionPane.showMessageDialog(loginPane,
                    "Success! You typed the right password.");

           loginPane.setVisible(false);
           this.remove(loginPane);

           if(null == cipherSymmetricOut){
               cipherSymmetricOut = Cipher.getInstance(SYM_ALGORITHM_CRYPT);
               cipherSymmetricOut.init(Cipher.ENCRYPT_MODE, symmetricKey, new IvParameterSpec(SYM_IV_STRING.getBytes(BabbleOnMessage.CHSET)));
           }

           cipherSymmetricIn = Cipher.getInstance(SYM_ALGORITHM_CRYPT);
           cipherSymmetricIn.init(Cipher.DECRYPT_MODE, symmetricKey, new IvParameterSpec(SYM_IV_STRING.getBytes(BabbleOnMessage.CHSET)));
           incomingMessageBox.setVisible(true);
           JPanel content = new JPanel();
           content.setLayout(new GridLayout(4,1));
           outgoingMessageBox.setVisible(true);
           content.add(usernameText, "1");
           usernameText.setVisible(true);
           content.add(receiverButton, "2");
           receiverButton.setVisible(true);
           content.add(outgoingMessageBox, "3");
           JScrollPane scrollPane = new JScrollPane(incomingMessageBox);
           content.add(outgoingMessageButton, "4");
           outgoingMessageButton.setVisible(true);
           frameContent.add(scrollPane, "2");

           frameContent.add(content, "1");
       } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
           throw new BabbleException("Problem establising panes " + ex.getMessage());
       } catch (UnsupportedEncodingException ex) {
            throw new BabbleException("Problem establising panes (encoding) " + ex.getMessage());
        }
    }
    
}
