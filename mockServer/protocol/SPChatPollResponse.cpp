/**
 * Author:  Kelly Ward
 */

#include "SPChatPollResponse.h"

using namespace std;

SPChatPollResponse::SPChatPollResponse(string username,
        vector<SPChatMsgMessage> msgList,int listNum,int totalList)
: SPChatMessage(SPCHATPOLLRESPONSE_TYPE){
    setUsername(username);
    setListNum(listNum);
    setTotalList(totalList);
    setMsgList(msgList);
}

SPChatPollResponse::SPChatPollResponse(unsigned char* buf, int len)
: SPChatMessage(SPCHATPOLLRESPONSE_TYPE){
    //cout<<"SPCHATRESPONSE<<<<<<<<<<<<"<<endl;
    int pos = PAYLOAD_POSITION;
    setUsername(decodeString(buf,len,&pos));
    setListNum(decodeInt(buf,len,&pos));
    //cout<<listNum<<endl;
    setTotalList(decodeInt(buf,len,&pos));
    //cout<<totalList<<endl;
    decodeMsgList(buf,len,&pos);
}


SPChatPollResponse::~SPChatPollResponse(){

}

void SPChatPollResponse::encodeMiddle(vector<unsigned char>* buf){
    encodeString(buf, username);
    encodeInt(buf, listNum);
    encodeInt(buf, totalList);
    encodeMsgList(buf);
}

void SPChatPollResponse::encodeMsgList(vector<unsigned char>* buf){
    encodeInt(buf, msgList.size());

    for(int i = 0; i < msgList.size(); i++){
        msgList[i].encodeMessage(buf);
    }
}

void SPChatPollResponse::decodeMsgList(unsigned char* buf, int len, int* pos){
    int msgNum = decodeInt(buf,len,pos);
    for(int i = 0; i<msgNum; i++){
        msgList.push_back(SPChatMsgMessage(buf,len,pos));
    }

}

string SPChatPollResponse::getUsername(){
    return username;
}

void SPChatPollResponse::setUsername(string username){
    if(!validUsername(username)) {
	    throw SPChatException(BAD_USERNAME_EXCEPTION);
    }
    this->username = username;
}

int SPChatPollResponse::getListNum(){
    return listNum;
}

void SPChatPollResponse::setListNum(int listNum){
    if(!validPositiveNum(listNum)){
        throw SPChatException(NEGATIVE_NUM_EXCEPTION);
    }
    this->listNum = listNum;
}

int SPChatPollResponse::getTotalList(){
    return totalList;
}

void SPChatPollResponse::setTotalList(int totalList){
    if(!validPositiveNum(totalList)){
        throw SPChatException(NEGATIVE_NUM_EXCEPTION);
    }
    this->totalList = totalList;
}

vector<SPChatMsgMessage> SPChatPollResponse::getMsgList(){
    return msgList;
}

void SPChatPollResponse::setMsgList(vector<SPChatMsgMessage> msgList){
    this->msgList.clear();
	this->msgList.insert(this->msgList.end(), msgList.begin(), msgList.end());
}

long SPChatPollResponse::getPayloadLength(){
    long length = INT_SIZE+username.size()+INT_SIZE+INT_SIZE+INT_SIZE;
    for(int i = 0; i < msgList.size(); i++){
        length += msgList[i].getPayloadLength();
    }
    return length;
}
