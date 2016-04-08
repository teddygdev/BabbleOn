#ifndef SPCHATPOLL_H_INCLUDED
#define SPCHATPOLL_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"

using namespace std;

class SPChatPoll: public SPChatMessage {
public:
	static const unsigned char SPCHATPOLL_TYPE = 5;


	SPChatPoll(string username, long time);
	SPChatPoll(unsigned char* buf, int len);
	virtual ~SPChatPoll();
	string getUsername();
	void setUsername(string username);
	long getTime();
	void setTime(long time);
private:
	string username;    // Username
	long time;          // Time since last poll
protected:
	void virtual encodeMiddle(vector<unsigned char>* buf);
	virtual long getPayloadLength();

};


#endif // SPCHATPOLL_H_INCLUDED
