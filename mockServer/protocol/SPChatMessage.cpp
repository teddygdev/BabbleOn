/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"

#include "SPChatMessage.h"
#include "SPChatLogin.h"
#include "SPChatChallenge.h"
#include "SPChatResponse.h"
#include "../PracticalSocket.h"
#include "SPChatACK.h"
#include "SPChatPoll.h"
#include "SPChatPollResponse.h"
#include "SPChatException.h"


using namespace std;

const string SPChatMessage::HEADER = "SPChat/1.0";
const int SPChatMessage::HEADER_LENGTH = 10;
const int SPChatMessage::TOTAL_HEADER_LENGTH = 19;
const int SPChatMessage::TYPE_POSITION = 10;
const int SPChatMessage::LENGTH_POSITION = 11;
const int SPChatMessage::PAYLOAD_POSITION = 0;
const int SPChatMessage::LONG_SIZE = 8;
const int SPChatMessage::INT_SIZE = 4;
const int SPChatMessage::BYTE_SIZE = 1;

SPChatMessage::SPChatMessage(unsigned char type) {
	this->type = type;
}

SPChatMessage::~SPChatMessage() {
	// TODO Auto-generated destructor stub
}

unsigned char SPChatMessage::getType(){
	return type;
}

void SPChatMessage::encode(vector<unsigned char>* buf){
	encodeHeader(buf);
	encodeMiddle(buf);
}

void SPChatMessage::encodeHeader(vector<unsigned char>* buf){
	buf->insert(buf->end(), HEADER.begin(), HEADER.end());
	buf->push_back(type);
	encodeLong(buf, getPayloadLength());
}

void SPChatMessage::encodeMiddle(vector<unsigned char>* buf){
    // Should never be called
}

long SPChatMessage::getPayloadLength(){
    return 0;
}

void SPChatMessage::encodeLong(vector<unsigned char>* buf, long num){
	if(isLittleEndian()){
        for (int i = 0; i < LONG_SIZE; i++){
            buf->push_back(num >> ((i-(LONG_SIZE-1)) * -8));
        }
	}else{
        for (int i = 0; i < LONG_SIZE; i++){
            buf->push_back(num >> (i * 8));
        }
	}
}

void SPChatMessage::encodeInt(vector<unsigned char>* buf, int num){
	if(isLittleEndian()){
        for (int i = 0; i < INT_SIZE; i++){
            buf->push_back(num >> ((i-(INT_SIZE-1)) * -8));
        }
	}else{
        for (int i = 0; i < INT_SIZE; i++){
            buf->push_back(num >> (i * 8));
        }
	}
}

void SPChatMessage::encodeString(vector<unsigned char>* buf, string str){
	encodeInt(buf, str.length());
	buf->insert(buf->end(), str.begin(), str.end());
}

SPChatMessage* SPChatMessage::decode(TCPSocket* sock){
	unsigned char headerArray[TOTAL_HEADER_LENGTH];
	sock->recvFully(headerArray, TOTAL_HEADER_LENGTH);
	//cout<<"read in header"<<endl;
	unsigned char t;
	long len = decodeHeader(headerArray, TOTAL_HEADER_LENGTH, &t);
	//cout<<"decoded header"<<endl;
	//cout<<"assumed msg len:"<<len;

	unsigned char buf[len];
	int actualLen = sock->recvFully(buf, len);
	//cout<<"actual msg len:"<<actualLen;
	//cout<<"header:"<<t;

	//cout<<endl;

	if(actualLen != len){
		cout<<"msgline96"<<endl;
		throw SPChatException(TOO_SHORT_EXCEPTION);
	}

	switch ( t ){
		case SPChatLogin::SPCHATLOGIN_TYPE:
			cout <<"login payload length:  "<< len <<endl;
			return new SPChatLogin(buf, len);
        case SPChatChallenge::SPCHATCHALLENGE_TYPE:
			return new SPChatChallenge(buf, len);
        case SPChatACK::SPCHATACK_TYPE:
			return new SPChatACK(buf, len);
        case SPChatResponse::SPCHATRESPONSE_TYPE:
			return new SPChatResponse(buf, len);
        case SPChatPoll::SPCHATPOLL_TYPE:
			return new SPChatPoll(buf, len);
        case SPChatPollResponse::SPCHATPOLLRESPONSE_TYPE:
			return new SPChatPollResponse(buf, len);
		default:
  			throw SPChatException(NO_SUCH_TYPE_EXCEPTION);
	}
}

long SPChatMessage::decodeHeader(unsigned char*  buf, int len, unsigned char* t){
	for(int i = 0; i < HEADER_LENGTH; i++){
		//cout <<hex << unsigned(HEADER[i]) << " ";
		//cout <<hex << unsigned(buf[i]) << " ";
		//cout<<endl;


		if(HEADER[i] != buf[i]){

			throw SPChatException(BAD_HEADER_EXCEPTION);
		}
	}

	for(int i = 0; i < TOTAL_HEADER_LENGTH; i++){
		//cout <<hex << unsigned(buf[i]) << " ";
	}
	cout << dec <<endl;

	*t = buf[TYPE_POSITION];

	int pos = LENGTH_POSITION;
	return decodeLong(buf,TOTAL_HEADER_LENGTH,&pos);
}

long SPChatMessage::decodeLong(unsigned char* buf, int len, int* pos){
	if(LONG_SIZE > len - *pos ){ throw SPChatException(TOO_SHORT_EXCEPTION); }

    long n = 0;
	if(!isLittleEndian()){
		//cout << "very bad test case --- " << *pos << endl;
        for(int i = LONG_SIZE-1; i >= 0; i--){
			//cout << *pos+i <<":  "<< hex << unsigned(buf[*pos+i])<<dec<<"   ";
            n = n | (static_cast<uint64_t>(buf[*pos+i]) << ((i-(LONG_SIZE-1)) * -8));
			//cout << hex << n <<dec<<endl;
        }
	} else{
        for(int i = 0; i < LONG_SIZE; i++){
			//cout << *pos+i <<":  "<< hex << unsigned(buf[*pos+i])<<dec<<"   ";
            n = n | (static_cast<uint64_t>(buf[*pos+i])
                     << -8*(i-(LONG_SIZE-1)));
			//cout<<hex<<n<<dec<<endl;
        }
	}

	*pos += LONG_SIZE;
	//cout << n << endl;
	return n;
}


int SPChatMessage::decodeInt(unsigned char* buf, int len, int* pos){
	if(INT_SIZE > len - *pos ){ throw SPChatException(TOO_SHORT_EXCEPTION); }

	int n = 0;
	if(!isLittleEndian()){
        for(int i = INT_SIZE-1; i >= 0; i--){
            n = n | (buf[*pos+i] << ((i-(INT_SIZE-1)) * -8));
         }
	} else{
        for(int i = 0; i < INT_SIZE; i++){
            n = n | (buf[*pos+i] << 8*((INT_SIZE-1)-i));
        }
	}
	*pos += INT_SIZE;

	//cout << "INT:    " << n << endl;

	return n;
}

int SPChatMessage::decodePositiveInt(unsigned char* buf, int len, int* pos){
	int i = decodeInt(buf, len, pos);
	if( i < 0){ throw SPChatException(NEGATIVE_NUM_EXCEPTION); }
	return i;
}

string SPChatMessage::decodeString(unsigned char* buf, int len, int* pos){
	int strLen = decodePositiveInt(buf, len, pos);
	if(len - *pos < strLen){ throw SPChatException(TOO_SHORT_EXCEPTION); }

	string str = "";
	for(int i = 0; i < strLen; i++){
		str += buf[*pos];
		*pos +=1;
	}

	return str;
}

long SPChatMessage::getMessageLength(){
    return getPayloadLength();
}

long SPChatMessage::getTotalMsgLength(){
	return getPayloadLength() + TOTAL_HEADER_LENGTH;
}
bool SPChatMessage::validUsername(string str){
    for( int i = 0; i < str.length(); i++) {
        if( !isalnum(str[i]) && str[i] != '@' && str[i] != '-'
           && str[i] != '.' && str[i] != '_'){
            return false;
        }
    }

    return true;
}

bool SPChatMessage::validPositiveNum(int num){
    return num >= 0;
}

bool SPChatMessage::validPositiveNum(long num){
    return num >= 0;
}

bool SPChatMessage::isLittleEndian(){
    short int number = 0x1;
    char *numPtr = (char*)&number;
    return (numPtr[0] == 1);
}




/*SPChatMessage* SPChatMessage::decode(unsigned char* buf, int len){
	unsigned char t;
	t = decodeHeader(buf);
	switch ( t ){
		case SPChatLogin::SPCHATLOGIN_TYPE:
			return new SPChatLogin(buf, len);
        case SPChatChallenge::SPCHATCHALLENGE_TYPE:
			return new SPChatChallenge(buf, len);
        case SPChatACK::SPCHATACK_TYPE:
			return new SPChatACK(buf, len);
        case SPChatResponse::SPCHATRESPONSE_TYPE:
			return new SPChatResponse(buf, len);
        case SPChatPoll::SPCHATPOLL_TYPE:
			return new SPChatPoll(buf, len);
        case SPChatPollResponse::SPCHATPOLLRESPONSE_TYPE:
			return new SPChatPollResponse(buf, len);
		default:
			throw NO_SUCH_TYPE_EXCEPTION;
	}
}*/
/*long SPChatMessage::decodeHeader(unsigned char*  buf){
	//if(HEADER_LENGTH <= len){ return NULL; }

	for(int i = 0; i < HEADER_LENGTH; i++){
		if(HEADER[i] != buf[i]){ throw BAD_HEADER_EXCEPTION; }
	}

	unsigned char t = buf[TYPE_POSITION];
	int pos = LENGTH_POSITION;

	return decodeLong(buf,TOTAL_HEADER_LENGTH,&pos);
}*/
