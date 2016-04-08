#ifndef SPCHATPOLLRESPONSE_H_INCLUDED
#define SPCHATPOLLRESPONSE_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"
#include "SPChatMsgMessage.h"

using namespace std;

class SPChatPollResponse: public SPChatMessage {

public:
	static const unsigned char SPCHATPOLLRESPONSE_TYPE = 6;


	SPChatPollResponse(string username,
                    vector<SPChatMsgMessage> msgList, int listNum,
                    int totalList);
	SPChatPollResponse(unsigned char* buf, int len);
	virtual ~SPChatPollResponse();
	string getUsername();
	void setUsername(string);
	int getListNum();
	void setListNum(int);
	int getTotalList();
	void setTotalList(int);
	vector<SPChatMsgMessage> getMsgList();
	void setMsgList(vector<SPChatMsgMessage>);
private:
	string username;                  // Username
	vector<SPChatMsgMessage> msgList; // List of messages
	int listNum;                      // List number
	int totalList;                    // Total number of lists
protected:
	void virtual encodeMiddle(vector<unsigned char>* buf);
	void encodeMsgList(vector<unsigned char>* buf);
	void decodeMsgList(unsigned char*, int,int*);
	virtual long getPayloadLength();

};


#endif // SPCHATPOLLRESPONSE_H_INCLUDED
