#ifndef SPCHATMSGMESSAGE_H_INCLUDED
#define SPCHATMSGMESSAGE_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"
#include "SPChatException.h"

using namespace std;

class SPChatMsgMessage: public SPChatMessage {
public:
	/*SPChatMsgMessage(string sender, string receiver,
            string message, long decryptMsgLen, int msgNum,
            int totalMsg, long time);*/
    SPChatMsgMessage(string sender, string receiver,
            string message, long decryptMsgLen, int msgNum,
            int totalMsg, long time);
	SPChatMsgMessage(unsigned char* buf, int len, int* pos);
	virtual ~SPChatMsgMessage();
	void  encodeMessage(vector <unsigned char>* buf);
	string getSender();
	void setSender(string);
	string getReceiver();
	void setReceiver(string);
	string getMessage();
	void setMessage(string);
	long getDecryptMsgLen();
	void setDecryptMsgLen(long);
	int getMsgNum();
	void setMsgNum(int);
	int getTotalMsg();
	void setTotalMsg(int);
	long getTime();
	void setTime(long);
	long getPayloadLength();
private:
    /** Max length of text */
    static const int MAX_MESSAGE_LENGTH;

	string sender;                  // Sender username
	string receiver;                // Receiver username
	string message; // Message text
	long decryptMsgLen;             // DecryptMsgLen
	int msgNum;                     // Message number
	int totalMsg;                   // Total number of messages
	long time;                      // Time sent
};

#endif // SPCHATMSGMESSAGE_H_INCLUDED
