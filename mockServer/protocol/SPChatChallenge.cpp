/**
 * Author:  Kelly Ward
 */

#include "SPChatChallenge.h"

using namespace std;

const int SPChatChallenge::CHALLENGE_LENGTH = 8;

SPChatChallenge::SPChatChallenge(vector<unsigned char> challenge)
: SPChatMessage(SPCHATCHALLENGE_TYPE) {
	setChallenge(challenge);
}

SPChatChallenge::SPChatChallenge(unsigned char* buf, int len)
: SPChatMessage(SPCHATCHALLENGE_TYPE) {
    int pos = PAYLOAD_POSITION;

    if(len-pos < CHALLENGE_LENGTH){ throw SPChatException(BAD_CHALLENGE_EXCEPTION);}

    // Pushes each byte from buffer into challenge as is
    for(int i = 0; i < CHALLENGE_LENGTH; i++){
        challenge.push_back(buf[pos+i]);
    }
}

SPChatChallenge::~SPChatChallenge() {
	// TODO Auto-generated destructor stub
}

void SPChatChallenge::encodeMiddle(vector<unsigned char>* buf){
	buf->insert(buf->end(), challenge.begin(), challenge.end());
}

long SPChatChallenge::getPayloadLength(){
    return CHALLENGE_LENGTH;
}

vector<unsigned char> SPChatChallenge::getChallenge() {
	return challenge;
}

void SPChatChallenge::setChallenge(vector<unsigned char> challenge) {
	if(challenge.size() != CHALLENGE_LENGTH){
        throw SPChatException(BAD_CHALLENGE_EXCEPTION);
	}

	this->challenge.clear();
	this->challenge.insert(this->challenge.end(), challenge.begin(), challenge.end());
}
