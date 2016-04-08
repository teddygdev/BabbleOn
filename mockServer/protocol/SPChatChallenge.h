#ifndef SPCHATCHALLENGE_H_INCLUDED
#define SPCHATCHALLENGE_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"

using namespace std;

class SPChatChallenge: public SPChatMessage {
public:
    /** Type of SPChatMessage */
	static const unsigned char SPCHATCHALLENGE_TYPE = 1;
	/** Length of challenge */
    static const int CHALLENGE_LENGTH;

    virtual ~SPChatChallenge();

    /**
    *   Construct a SPChatChallenge with a challenge
    *   @param challenge the challange wanted to be sent
    */
	SPChatChallenge(vector<unsigned char> challenge);

	/**
    *   Construct a SPChatACK through deserialization
    *   @param buf buffer containing encoded message
    *   @param len buffer length
    */
	SPChatChallenge(unsigned char* buf, int len);

	/**
    *   Returns the challenge
    *   @param challenge new challenge wanted to be sent
    */
	vector<unsigned char> getChallenge();

	/**
    *   Sets the challenge
    */
	void setChallenge(vector<unsigned char> challenge);
private:
	vector<unsigned char> challenge;    // Challenge to be sent as bytes
protected:
	void virtual encodeMiddle(vector<unsigned char>*);
	virtual long getPayloadLength();
};

#endif // SPCHATCHALLENGE_H_INCLUDED
