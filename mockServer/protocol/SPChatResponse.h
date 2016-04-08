#ifndef SPCHATRESPONSE_H_INCLUDED
#define SPCHATRESPONSE_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include "SPChatMessage.h"

using namespace std;

class SPChatResponse: public SPChatMessage {

public:
	static const unsigned char SPCHATRESPONSE_TYPE = 2;


	SPChatResponse(string, string, vector<unsigned char>);
	SPChatResponse(unsigned char*, int);
	virtual ~SPChatResponse();
	string getHash();
	void setHash(string);
	string getUsername();
	void setUsername(string);
	vector<unsigned char> getNonce();
	void setNonce(vector<unsigned char>);

private:
	string hash;        // Hash
	string username;    // Username
	vector<unsigned char> nonce;         // Nonce

protected:
	void virtual encodeMiddle(vector<unsigned char>*);
	virtual long getPayloadLength();
	vector<unsigned char> decodeHash(unsigned char*, int, int*);

	vector<unsigned char> decodeNonce(unsigned char *buf, int len, int *pos);
};


#endif // SPCHATRESPONSE_H_INCLUDED
