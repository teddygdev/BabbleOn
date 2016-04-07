/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

/**
 * Possible error return values enumerated 
 * Utilized in ACK message errSender byte as the high-order 7 bits
 * @author Brandy
 */
public enum Errors {
    SUCCESS(0),
    INVALID_MSG_TYPE(1),
    INVALID_USERNAME_PASSWORD(2),
    FORMATTING_ERROR(3),
    DEFAULT_ERROR(4),
    PASSWORD_AUTH(5),
    INVALID_RECIPIENT(6),
    INVALID_ERROR(99);

    int err;

    Errors(int e){
        err = e;
    }
    
    public int getErr(){
        return err;
    }
    
    public static String getValue(int e){
        for(Errors er : Errors.values()){
            if(er.getErr() == e){
                return er.name();
            }
        }
        return INVALID_ERROR.name();
    }
}
