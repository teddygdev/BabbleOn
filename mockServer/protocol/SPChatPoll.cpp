/**
 * Author:  Kelly Ward
 */

#include "SPChatPoll.h"

using namespace std;

SPChatPoll::SPChatPoll(string username, long time)
: SPChatMessage(SPCHATPOLL_TYPE){
    setUsername(username);
    setTime(time);
}

SPChatPoll::SPChatPoll(unsigned char* buf, int len)
: SPChatMessage(SPCHATPOLL_TYPE){
    int pos = PAYLOAD_POSITION;
    setUsername(decodeString(buf,len,&pos));
    setTime(decodeLong(buf,len,&pos));
}


SPChatPoll::~SPChatPoll(){

}

void SPChatPoll::encodeMiddle(vector<unsigned char>* buf){
    encodeString(buf, username);
    encodeLong(buf, time);
}

string SPChatPoll::getUsername(){
    return username;
}


void SPChatPoll::setUsername(string username){
    if(!validUsername(username)) {
	    throw SPChatException(BAD_USERNAME_EXCEPTION);
    }
    this->username = username;
}


long SPChatPoll::getTime(){
    if(!validPositiveNum(time)){
        throw SPChatException(NEGATIVE_NUM_EXCEPTION);
    }
    return time;
}


void SPChatPoll::setTime(long time){
    this->time = time;
}

long SPChatPoll::getPayloadLength(){
    return INT_SIZE+username.size()+LONG_SIZE;
}
