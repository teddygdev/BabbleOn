#ifndef SPCHATLOGIN_H_INCLUDED
#define SPCHATLOGIN_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"

using namespace std;

class SPChatLogin: public SPChatMessage {
public:
	SPChatLogin(string username, const unsigned char keyType, unsigned char *key, int keyLen);

	/** Type of SPChatMessage */
	static const unsigned char SPCHATLOGIN_TYPE = 0;
	/** Type of keys */
	static const unsigned char NO_KEY;
	static const unsigned char ASYMMETRIC_KEY;
	static const unsigned char ENCRYPTED_SYM_KEY;
	/** Asymmetric key information */
    static const int ASYMMETRIC_LENGTH;
    static const string ASYMMETRIC_HEADER;
    static const string ASYMMETRIC_FOOTER;
    static const char ENDLINE;
    static const int ASYMMETRIC_ENDLINE_POS[];
    /** Symmetric key information */
    static const int ENCRYPTED_SYM_LENGTH;


    virtual ~SPChatLogin();

	/**
    *   Construct a SPChatLogin with inputted username and key,
    *   with key type and length
    *   @param username name of user
    *   @param keyType type of key
    *   @param key vector containing key bytes
    *   @param keyLen length of inputted key
    */
	SPChatLogin(string username, const unsigned char keyType,
             string key, int keyLen);

    /**
    *   Construct a SPChatLogin through deserialization
    *   @param buf buffer containing encoded message
    *   @param len buffer length
    */
	SPChatLogin(unsigned char* buf, int len);

	/**
    *   Returns the username
    */
	string getUsername();

	/**
	*   Sets the username
	*/
	void setUsername(string username);

	/**
    *   Returns the key type
    */
	unsigned char getKeyType();

	/**
    *   Sets the key and key type
    *   @param keyType type of key
    *   @param newKey vector containing key bytes
    *   @param keyLen length of inputted key
    */
//	void setKey(const unsigned char keyType,
//             string newKey, int keyLen);

	void setKey(const unsigned char keyType, unsigned char *newKey, int keyLen);
	void setKey(const unsigned char keyType, unsigned char* newKey, int keyLen, int* pos);


	/**
    *   Returns vector containing key bytes
    */
	string getKey();
    
    /**
    *   Returns vector containing key bytes
    */
	vector<unsigned char> getEncSymKey();


private:
	string username;        // Name of user
	unsigned char keyType;  // Type of key
	string key;             // Key string
    vector<unsigned char> encSymKey; //

	/**
    *   Sets the key and key type using deserialization
    *   @param buf buffer containing encoded message
    *   @param len buffer length
    *   @param pos current position in buffer
    */
	void decodeKey(unsigned char* buf, int len, int* pos);

	/**
    *   Copies a string from a buffer
    *   @param buf buffer containing string
    *   @param bufLen buffer length
    *   @param pos current position in buffer
    *   @param cpyLen length of string wished to copy
    */
	string bufferToString(unsigned char* buf, int bufLen, int pos, int cpyLen);

	/**
    *   Sets key to the new asymmetric key
    *   @param newKey new asymmetric key
    */
	void setAsymmetricKey(string newKey);


protected:
	void virtual encodeMiddle(vector<unsigned char>* buf);
	virtual long getPayloadLength();


};

#endif // SPCHATLOGIN_H_INCLUDED
