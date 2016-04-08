/**
 * Author:  Kelly Ward
 */

#include "SPChatMsgMessage.h"

using namespace std;

const int SPChatMsgMessage::MAX_MESSAGE_LENGTH = 500;

SPChatMsgMessage::SPChatMsgMessage(string sender,string receiver,
                    string message, long decryptMsgLen,int msgNum,
                    int totalMsg,long time)
                    : SPChatMessage(12){
    setSender(sender);
    setReceiver(receiver);
    setMessage(message);
    setDecryptMsgLen(decryptMsgLen);
    setMsgNum(msgNum);
    setTotalMsg(totalMsg);
    setTime(time);
}

SPChatMsgMessage::SPChatMsgMessage(unsigned char* buf, int len, int* pos)
: SPChatMessage(12){
    setSender(decodeString(buf,len,pos));
    setReceiver(decodeString(buf,len,pos));
    setMessage(decodeString(buf,len,pos));
    setDecryptMsgLen(decodeLong(buf,len,pos));
    setMsgNum(decodeInt(buf,len,pos));
    setTotalMsg(decodeInt(buf,len,pos));
    setTime(decodeLong(buf,len,pos));
}


SPChatMsgMessage::~SPChatMsgMessage(){

}

void SPChatMsgMessage::encodeMessage(vector<unsigned char>* buf){
    encodeString(buf, sender);
    encodeString(buf, receiver);
    encodeString(buf, message);
    encodeLong(buf, decryptMsgLen);
    encodeInt(buf, msgNum);
    encodeInt(buf, totalMsg);
    encodeLong(buf, time);
}

string SPChatMsgMessage::getSender(){
    return sender;
}


void SPChatMsgMessage::setSender(string sender){
    if(!validUsername(sender)) {
	    throw SPChatException(BAD_USERNAME_EXCEPTION);
    }
    this->sender = sender;
}


string SPChatMsgMessage::getReceiver(){
    return receiver;
}


void SPChatMsgMessage::setReceiver(string receiver){
    if(!validUsername(receiver)) {
	    throw SPChatException(BAD_USERNAME_EXCEPTION);
    }
    this->receiver = receiver;
}


string SPChatMsgMessage::getMessage(){
    return message;
}

void SPChatMsgMessage::setMessage(string newMsg){
    message = newMsg;
}

long SPChatMsgMessage::getDecryptMsgLen(){
    return decryptMsgLen;
}

void SPChatMsgMessage::setDecryptMsgLen(long decryptMsgLen){
    if(decryptMsgLen > MAX_MESSAGE_LENGTH){
        throw SPChatException(MSG_LENGTH_EXCEPTION);
    }
    this->decryptMsgLen = decryptMsgLen;
}

int SPChatMsgMessage::getMsgNum(){
    return msgNum;
}

void SPChatMsgMessage::setMsgNum(int msgNum){
    if(!validPositiveNum(time)){
        throw SPChatException(NEGATIVE_NUM_EXCEPTION);
    }
    this->msgNum = msgNum;
}

int SPChatMsgMessage::getTotalMsg(){
    return totalMsg;
}

void SPChatMsgMessage::setTotalMsg(int totalMsg){
    if(!validPositiveNum(totalMsg)){
        throw SPChatException(NEGATIVE_NUM_EXCEPTION);
    }
    this->totalMsg = totalMsg;
}

long SPChatMsgMessage::getTime(){
    return time;
}


void SPChatMsgMessage::setTime(long time){
    if(!validPositiveNum(time)){
        throw SPChatException(NEGATIVE_NUM_EXCEPTION);
    }
    this->time = time;
}

long SPChatMsgMessage::getPayloadLength(){
    return INT_SIZE+sender.length()+INT_SIZE+receiver.length()+INT_SIZE+message.size()
        +LONG_SIZE+INT_SIZE+INT_SIZE+LONG_SIZE;
}
