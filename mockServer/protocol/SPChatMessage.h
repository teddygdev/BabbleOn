#ifndef SPCHATMESSAGE_H_
#define SPCHATMESSAGE_H_

/**
 * Author:  Kelly Ward
 */

class TCPsocket;

#include <string>
#include <vector>
#include "SPChatException.h"
#include "../PracticalSocket.h"

using namespace std;

class SPChatMessage {
public:
    /** Number of bytes in the header */
    static const int TOTAL_HEADER_LENGTH;

	virtual ~SPChatMessage();

	/**
	*   Encodes the header and type specific payload into buffer
	*   @param buf buffer to contain the serialized bytes
	*/
	void encode(vector<unsigned char>* buf);

    /**
	*   Factory method that deserializes and returns a specific SPChatMessage
	*   @param sock socket where bytes will be read from
	*/
	static SPChatMessage* decode(TCPSocket* sock);

	/**
	*   Returns the type of SPChatMessage
	*/
	unsigned char getType();
	/**
	*   Returns length of the payload (based on message type)
	*/
	long getMessageLength();

	/**
	*   Returns length of entire message
	*/
	long getTotalMsgLength();

	/**
	*   Returns true if machine uses little endian
	*/
	static bool isLittleEndian();

private:
    /** Header found at the beginning of all SPChatMessages */
	static const string HEADER;
	/** Length of header */
	static const int HEADER_LENGTH;
	/** Position of type byte in serialization */
	static const int TYPE_POSITION;
	/** Position of payload length in serialization */
	static const int LENGTH_POSITION;

    unsigned char type; // Type of SPChatMessage


	/**
	*   Serializes and stores header in buf
	*   @param buf buffer to contain the serialized bytes
	*/
	void encodeHeader(vector<unsigned char>* buf);

	/**
    *   Decodes and checks the header, returning the payload length
    *   @param buf buffer containing encoded message
    *   @param len buffer length
    *   @param t reference to store SPChatMessage type
    */
	static long decodeHeader(unsigned char* buf, int len, unsigned char* t);

protected:
    /** Starting byte of the payload */
	static const int PAYLOAD_POSITION;
	/** Byte length of data types */
	static const int LONG_SIZE;
	static const int INT_SIZE;
	static const int BYTE_SIZE;

	/**
    *   Construct a SPChatMessage with inputted type
    *   @param type type of SPChatMessage
    */
	SPChatMessage(unsigned char type);

	/**
    *   Returns true if str is a valid username
    *   @param str username being checked
    */
	bool validUsername(string str);

	/**
    *   Returns true if num is a positive number
    *   @param num number being checked
    */
	bool validPositiveNum(int num);
	bool validPositiveNum(long num);

	/**
	*   Serializes and stores type specific payload in buf
	*   @param buf buffer to contain the serialized bytes
	*/
	virtual void encodeMiddle(vector<unsigned char>* buf);

	/**
	*   Returns type specific payload length
	*/
	virtual long getPayloadLength();

	/**
	*   Serializes and stores long in buf
	*   @param buf buffer to contain the serialized bytes
	*   @param num long being encoded
	*/
	void encodeLong(vector<unsigned char>* buf, long num);

	/**
	*   Decodes and returns long from buffer
	*   @param buf buffer containing encoded message
    *   @param len buffer length
    *   @param pos current position in buffer
    */
	static long decodeLong(unsigned char* buf, int len, int* pos);

	/**
	*   Serializes and stores int in buf
	*   @param buf buffer to contain the serialized bytes
	*   @param num int being encoded
	*/
	void encodeInt(vector<unsigned char>* buf, int num);

	/**
	*   Decodes and returns int from buffer
	*   @param buf buffer containing encoded message
    *   @param len buffer length
    *   @param pos current position in buffer
    */
	static int decodeInt(unsigned char* buf, int len, int* pos);

	/**
	*   Decodes and returns int from buffer, checking that it's positive
	*   @param buf buffer containing encoded message
    *   @param len buffer length
    *   @param pos current position in buffer
    */
	static int decodePositiveInt(unsigned char* buf, int len, int* pos);

	/**
	*   Serializes and stores str and its length in buf
	*   @param buf buffer to contain the serialized bytes
	*   @param str string being encoded
	*/
	void encodeString(vector<unsigned char>* buf, string str);

	/**
	*   Decodes and returns string from buffer
	*   @param buf buffer containing encoded message
    *   @param len buffer length
    *   @param pos current position in buffer
    */
	static string decodeString(unsigned char* buf, int len, int* pos);
};

#endif // SPCHATMESSAGE_H_INCLUDED
