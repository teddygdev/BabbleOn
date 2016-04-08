#ifndef SPCHATACK_H_INCLUDED
#define SPCHATACK_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"

using namespace std;

class SPChatACK: public SPChatMessage {
public:
    /** Type of SPChatMessage */
	static const unsigned char SPCHATACK_TYPE = 3;
    /** Types of ACK senders */
    enum senderType{SERVER = 0, CLIENT = 1};
    /** Types of ACK errors */
    enum errorType{SUCCESS = 0, INVALID_MSG_TYPE = 1,
        INVALID_USERNAME_PASSWORD = 2,FORMATTING_ERROR = 3,
        DEFAULT_ERROR = 4, PASSWORD_AUTH = 5};


	virtual ~SPChatACK();

    /**
    *   Construct a SPChatACK with an user type and sender type
    *   @param error type of error
    *   @param sender type of sender
    */
	SPChatACK(errorType error,senderType sender);

	/**
    *   Construct a SPChatACK through deserialization
    *   @param buf buffer containing encoded message
    *   @param len buffer length
    */
	SPChatACK(unsigned char* buf, int len);

	/**
    *   Returns the error type
    */
	int getError();

	/**
    *   Sets the error type
    *   @param error new error type
    */
	void setError(int error);

	/**
    *   Returns the sender type
    */
	int getSender();

	/**
    *   Sets the sender type
    *   @param sender new sender type
    */
	void setSender(int sender);
private:
    senderType sender;  // Type of sender
	errorType error;    // Type of error
protected:
	void virtual encodeMiddle(vector<unsigned char>* buf);
	virtual long getPayloadLength();

};

#endif // SPCHATACK_H_INCLUDED
