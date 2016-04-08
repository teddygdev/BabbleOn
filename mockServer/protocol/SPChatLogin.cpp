/**
 * Author:  Kelly Ward
 */

#include "SPChatLogin.h"

using namespace std;

const unsigned char SPChatLogin::NO_KEY = 0;
const unsigned char SPChatLogin::ASYMMETRIC_KEY = 1;
const unsigned char SPChatLogin::ENCRYPTED_SYM_KEY = 2;
const int SPChatLogin::ASYMMETRIC_LENGTH = 271;
const int SPChatLogin::ENCRYPTED_SYM_LENGTH = 128;
const string SPChatLogin::ASYMMETRIC_HEADER = "-----BEGIN PUBLIC KEY-----\n";
const string SPChatLogin::ASYMMETRIC_FOOTER = "-----END PUBLIC KEY-----\n";
const char SPChatLogin::ENDLINE = '\n';
const int SPChatLogin::ASYMMETRIC_ENDLINE_POS[4] = {91,156,221,246};

/*SPChatLogin::SPChatLogin(string username, const unsigned char keyType, string key, int keyLen)
: SPChatMessage(SPCHATLOGIN_TYPE) {
	setUsername(username);
	cout<<"spchatLoginfunctionString"<<endl;
	//strcat(key, "\n");
	key+="\n";
	cout<<key.length()<<endl;
	cout<<key<<endl;
	setKey(keyType, key, keyLen);
}
*/
SPChatLogin::SPChatLogin(string username, const unsigned char keyType, unsigned char* key, int keyLen)
		: SPChatMessage(SPCHATLOGIN_TYPE) {
	setUsername(username);

	cout<<"spchatLoginfunction"<<endl;
	string testString = string((char*)key,keyLen);
	testString+="\n";
	cout<<testString.length()<<endl;
	cout<<testString<<endl;
	setKey(keyType, key, keyLen);
}

SPChatLogin::SPChatLogin(unsigned char* buf, int len)
: SPChatMessage(SPCHATLOGIN_TYPE) {
	int pos = PAYLOAD_POSITION;
	setUsername(decodeString(buf, len, &pos));
	decodeKey(buf, len, &pos);
}

SPChatLogin::~SPChatLogin() {
	// TODO Auto-generated destructor stub
}

void SPChatLogin::decodeKey(unsigned char* buf, int len, int* pos){
    if(len - *pos < BYTE_SIZE){
		cout<<"line51"<<endl;
		throw SPChatException(TOO_SHORT_EXCEPTION); }
	unsigned char keyType = buf[*pos];
	cout<<unsigned(keyType)<<endl;
	*pos += 1;
	cout<<len - *pos<<endl;

	if ((keyType == NO_KEY)){
        this->keyType = NO_KEY;
	} else if(keyType == ASYMMETRIC_KEY && (len - *pos >= ASYMMETRIC_LENGTH)){

        setAsymmetricKey(bufferToString(buf, len, *pos, ASYMMETRIC_LENGTH));
        //*pos += ASYMMETRIC_LENGTH;
	} else if(keyType == ENCRYPTED_SYM_KEY && (len - *pos >= ENCRYPTED_SYM_LENGTH)){
        setKey(keyType, buf, len, pos);
        *pos += ENCRYPTED_SYM_LENGTH;
	}else{
		cout<<"line 67"<<endl;
        throw SPChatException(BAD_KEY_EXCEPTION);
	}
}

void SPChatLogin::encodeMiddle(vector<unsigned char>* buf){
	encodeString(buf,username);

	buf->push_back(keyType);
	if(keyType == ASYMMETRIC_KEY || keyType == ENCRYPTED_SYM_KEY){
        buf->insert(buf->end(), key.begin(), key.end());
	}
}

long SPChatLogin::getPayloadLength(){
    long length = INT_SIZE + username.length() + BYTE_SIZE;

    if(keyType == ASYMMETRIC_KEY){
        length += ASYMMETRIC_LENGTH;
    }else if(keyType == ENCRYPTED_SYM_KEY){
        length += ENCRYPTED_SYM_LENGTH;
    }
    return length;
}

string SPChatLogin::getUsername() {
	return username;
}

void SPChatLogin::setUsername(string username) {
	if(!validUsername(username)) {
	    throw SPChatException(BAD_USERNAME_EXCEPTION);
    }

	this->username = username;
}

unsigned char SPChatLogin::getKeyType() {
	return keyType;
}

void SPChatLogin::setKey(const unsigned char keyType, unsigned char* newKey, int keyLen) {
    key.clear();
    encSymKey.clear();
	if(keyType == NO_KEY){
        this->keyType = NO_KEY;
    } else if(keyType == ASYMMETRIC_KEY && (keyLen == ASYMMETRIC_LENGTH)){
        setAsymmetricKey(string((char*)newKey,keyLen));
        this->keyType = keyType;
	} else if(keyType == ENCRYPTED_SYM_KEY && (keyLen == ENCRYPTED_SYM_LENGTH)){
        this->keyType = keyType;
        encSymKey.insert(encSymKey.end(), newKey, newKey+keyLen);
    } else {
		cout<<"120"<<endl;
        throw SPChatException(BAD_KEY_EXCEPTION);
    }
}

void SPChatLogin::setKey(const unsigned char keyType, unsigned char* newKey, int keyLen, int* pos) {
	key.clear();
	encSymKey.clear();
	if(keyType == ENCRYPTED_SYM_KEY && (keyLen - *pos == ENCRYPTED_SYM_LENGTH)){
		this->keyType = keyType;
		encSymKey.insert(encSymKey.end(), newKey+*pos, newKey+keyLen);
	} else {
		cout << "line 130"<<endl;
		throw SPChatException(BAD_KEY_EXCEPTION);
	}
}

string SPChatLogin::bufferToString(unsigned char* buf, int bufLen, int pos, int cpyLen){
    if(bufLen - pos < cpyLen){
		cout<<"line124"<<endl;
		throw TOO_SHORT_EXCEPTION; }

	string str = "";
	for(int i = 0; i < cpyLen; i++){
        str += buf[pos + i];
	}

	return str;
}

void SPChatLogin::setAsymmetricKey(string newKey){
	keyType = ASYMMETRIC_KEY;
	for(int i = 0; i < newKey.length(); i++){
		//cout << i << ":  " << newKey[i]<<endl;
	}

    // Check header
    for(int i = 0; i < ASYMMETRIC_HEADER.length(); i++){
		if(ASYMMETRIC_HEADER[i] != newKey[i]){
			cout << "header"<<endl;
		    throw SPChatException(ASYMMETRIC_KEY_EXCEPTION);
        }
	}
	//Check footer
	int footerPos = ASYMMETRIC_LENGTH - ASYMMETRIC_FOOTER.length() +1;
	//cout<<"foot start"<<endl;
	for(int i = 0; i < ASYMMETRIC_FOOTER.length()-1; i++){
		//cout <<footerPos+i<<"   "<<ASYMMETRIC_FOOTER[i] <<" | "<< newKey[footerPos + i]<<endl;
		if(ASYMMETRIC_FOOTER[i] != newKey[footerPos + i]){
			cout << "footer"<<endl;
		    throw SPChatException(ASYMMETRIC_KEY_EXCEPTION);
        }
	}

	// Check endline positions
	for(int i = 0; i < 4; i++){
        if(ENDLINE != newKey[ASYMMETRIC_ENDLINE_POS[i]]){
			cout <<"endline"<<endl;
		    throw SPChatException(ASYMMETRIC_KEY_EXCEPTION);
        }
	}

    key = newKey;
}

string SPChatLogin::getKey(){
    return key;
}

vector<unsigned char> SPChatLogin::getEncSymKey(){
    return encSymKey;
}
