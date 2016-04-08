/**
 * Author:  Kelly Ward
 */

#include "SPChatResponse.h"

using namespace std;

SPChatResponse::SPChatResponse(string hash, string username, vector<unsigned char> nonce)
: SPChatMessage(SPCHATRESPONSE_TYPE) {
	setHash(hash);
	setUsername(username);
	setNonce(nonce);
}

SPChatResponse::SPChatResponse(unsigned char* buf, int len)
        : SPChatMessage(SPCHATRESPONSE_TYPE) {
    int pos = PAYLOAD_POSITION;

    setUsername(decodeString(buf,len,&pos));
    setHash(decodeString(buf,len,&pos)); //setHash(decodeHash(buf,len,&pos))
    setNonce(decodeNonce(buf,len,&pos));

}

SPChatResponse::~SPChatResponse() {
	// TODO Auto-generated destructor stub
}

void SPChatResponse::encodeMiddle(vector<unsigned char>* buf){
    encodeString(buf, username);
    /*encodeInt(buf, hash.size());
    buf->insert(buf->end(), hash.begin(), hash.end());*/
    encodeString(buf,hash);
    //encodeLong(buf, nonce);
    buf->insert(buf->end(), nonce.begin(), nonce.end());

}

vector<unsigned char> SPChatResponse::decodeNonce(unsigned char* buf, int len, int* pos){
    //int hashLen = decodeInt(buf,len,pos);
    vector<unsigned char> newNonce;
    if(len-*pos >= LONG_SIZE){
        for(int i = 0; i < LONG_SIZE; i++){
            newNonce.push_back(buf[*pos]);
            *pos += 1;
        }
    }

    return newNonce;
}

long SPChatResponse::getPayloadLength(){
    return INT_SIZE + username.length() + INT_SIZE + hash.length() + LONG_SIZE;
}

string SPChatResponse::getHash(){
    return hash;
}

void SPChatResponse::setHash(string hash){
    this->hash = hash;
}

string SPChatResponse::getUsername(){
    return username;
}

void SPChatResponse::setUsername(string username){
    if(!validUsername(username)) {
	    throw SPChatException(BAD_USERNAME_EXCEPTION);
    }

	this->username = username;
}

vector<unsigned char> SPChatResponse::getNonce(){
    return nonce;
}

void SPChatResponse::setNonce(vector<unsigned char> nonce){
    this->nonce = nonce;
}




/*vector<unsigned char> SPChatResponse::decodeHash(unsigned char* buf, int len, int* pos){
    int hashLen = decodeInt(buf,len,pos);
    vector<unsigned char> newHash;
    if(len-*pos >= hashLen){
        for(int i = 0; i < hashLen; i++){
            newHash.push_back(buf[*pos]);
            *pos += 1;
        }
	}

    return newHash;
}*/
