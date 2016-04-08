/**
 * Author:  Kelly Ward
 */

#include "SPChatACK.h"


using namespace std;

SPChatACK::SPChatACK(errorType error, senderType sender)
: SPChatMessage(SPCHATACK_TYPE) {
	setError(error);
	setSender(sender);
}

SPChatACK::SPChatACK(unsigned char* buf, int len)
: SPChatMessage(SPCHATACK_TYPE) {
	int pos = PAYLOAD_POSITION;

    // Checks the low order bit for sender
    setSender(buf[pos]%2);
    // Right shift rest to get the error
    setError(buf[pos] >> 1);

}

SPChatACK::~SPChatACK() {
	// TODO Auto-generated destructor stub
}

void SPChatACK::encodeMiddle(vector<unsigned char>* buf){
    // Left shift error by 1 and sets the the low order
    // bit as the sender (0 or 1) to get encoded byte
    unsigned char err = (error << 1) | sender;
    buf->push_back(err);
}

long SPChatACK::getPayloadLength(){
    return BYTE_SIZE;
}

int SPChatACK::getError(){
    return error;
}

void SPChatACK::setError(int error){
    // Checks that error is an errorType
    switch(error){
        case SUCCESS:
            this->error = SUCCESS;
            break;
        case INVALID_MSG_TYPE:
            this->error = INVALID_MSG_TYPE;
            break;
        case INVALID_USERNAME_PASSWORD:
            this->error = INVALID_USERNAME_PASSWORD;
            break;
        case FORMATTING_ERROR:
            this->error = FORMATTING_ERROR;
            break;
        case DEFAULT_ERROR:
            this->error = DEFAULT_ERROR;
            break;
        case PASSWORD_AUTH:
            this->error = PASSWORD_AUTH;
            break;
        default: throw SPChatException(INVALID_ERROR_EXCEPTION);
    }

    /*if(error < SUCCESS || error > PASSWORD_AUTH){
        throw INVALID_ERROR_EXCEPTION;
    }
    this->error = error;*/
}

int SPChatACK::getSender(){
    return sender;
}

void SPChatACK::setSender(int sender){
    // Checks that sender is a senderType
    switch(sender){
        case SERVER: this->sender = SERVER;
            break;
        case CLIENT: this->sender = CLIENT;
            break;
        default: throw SPChatException(INVALID_SENDER_EXCEPTION);
    }

    /*if(error != SERVER || sender != CLIENT){
        throw INVALID_SENDER_EXCEPTION;
    }
    this->sender = sender;*/
}
