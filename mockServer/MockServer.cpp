#include <iostream>
#include <fstream>
#include "PracticalSocket.h"
#include "SurveyCommon.h"
#include "Sha256Lib.h"
#include "protocol/SPChatMessage.h"
#include "protocol/SPChatChallenge.h"
#include "protocol/SPChatLogin.h"
#include "protocol/SPChatResponse.h"
#include "protocol/SPChatACK.h"
#include <openssl/rand.h>
#include "protocol/SPChatException.h"
#include "protocol/SPChatPoll.h"
#include "db/include/cppconn/exception.h"
#include "db/include/cppconn/driver.h"
#include "db/include/cppconn/resultset.h"

#include "db/include/cppconn/driver.h"
#include "db/include/cppconn/exception.h"
#include "db/include/cppconn/resultset.h"
#include "db/include/cppconn/statement.h"
#include "db/include/cppconn/prepared_statement.h"
#include "protocol/SPChatMsgMessage.h"
#include "protocol/SPChatPollResponse.h"

#include <cryptopp/integer.h>
#include <cryptopp/osrng.h>
#include <cryptopp/rsa.h>
#include <cryptopp/pem.h>
#include <cryptopp/files.h>
#include <cryptopp/hex.h>
#include <cryptopp/modes.h>
#include <sstream>



//using Donahoo's TCP C book example code

using namespace std;
using namespace CryptoPP;
const string DBUSER = "root";
const string DBPASS = "root";
const string DBIP = "tcp://127.0.0.1:3306";
const in_port_t serverPort = 54321;

string keyString = "";
string ivString = "QtIkUCmeW0XTCybi";
int keysize=16;

void aesEncrypt(string plain, string inputKey, string inputIv, string &cipher);
void aesDecrypt(string cipher, string inputKey, string inputIv, string &plain);
static long UnixTimeFromMysqlString(string s);



/** Thread main function to administer a chat over the given socket */
static void *clientConnectStart(void *arg);


int main(int argc, char *argv[]) {

    try {
        // Make a socket to listen for SurveyClient connections.
        TCPServerSocket servSock(serverPort);

        //can we connect to the database
        try {
            sql::Driver *driver;
            sql::Connection *con;
            sql::Statement *stmt;
            sql::ResultSet *res;

            /* Create a connection */
            driver = get_driver_instance();
            con = driver->connect(DBIP, DBUSER, DBPASS);
            /* Connect to the MySQL test database */
            //con->setSchema("chatserver");

            stmt = con->createStatement();
            stmt->execute("USE chatserver");
            //res = stmt->executeQuery("SELECT * FROM login WHERE email = 'jonhand93@gmail.com' AND password = 'derp'");
            res = stmt->executeQuery("SELECT password FROM login WHERE email = 'jonhand93@gmail.com'");
            //res = stmt->executeQuery("SELECT id, label FROM test ORDER BY id ASC");
            while (res->next()) {
                //set flag to true
                //cout << res->getString(1) << endl;


            }
            delete res;
            delete stmt;
            delete con;

        } catch (sql::SQLException &e) {
            cout << "No connection to database";
            cout << "# ERR: SQLException in " << __FILE__;
            cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
            cout << "# ERR: " << e.what();
            cout << " (MySQL error code: " << e.getErrorCode();
            cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        }




        for (;;) {    // Repeatedly accept connections for the chat server
            TCPSocket *sock = servSock.accept();



            pthread_t newThread;              // Give chat in a separate thread
            if (pthread_create(&newThread, NULL, clientConnectStart, sock) != 0) {
                cerr << "Can't create new thread" << endl;
                delete sock;
            }
        }
    } catch (SocketException &e) {
        cerr << e.what() << endl;           // Report errors to the console.
    }

    return 0;
}

static void *clientConnectStart(void *arg) {
    TCPSocket *sock = (TCPSocket *)arg;   // Argument is really a socket

    try {
        //get message
        SPChatMessage* msg = SPChatMessage::decode(sock);
        //is it login message?
        if(msg->getType() == SPChatLogin::SPCHATLOGIN_TYPE) {
            //generate challenge
            unsigned char challenge[8];
            vector<unsigned char> theChallenge;
            RAND_bytes(challenge, 8);
            for (int i=0; i<8; i++) {
                theChallenge.push_back(challenge[i]);
            }
            SPChatLogin *login = (SPChatLogin *) msg;
            string username = login->getUsername();
            string password = "";
            //send challenge
            SPChatChallenge spChatChallenge(theChallenge);
            vector<unsigned char> encoding;
            spChatChallenge.encode(&encoding);
            sock->send(encoding.data(), spChatChallenge.getMessageLength() + SPChatMessage::TOTAL_HEADER_LENGTH);
            //get second message
            SPChatMessage *msg2 = SPChatMessage::decode(sock);
            //is it chat response?
            //to add
            //good idea to loop for password
            //to add
            if (msg2->getType() == SPChatResponse::SPCHATRESPONSE_TYPE) {
                SPChatResponse *response = (SPChatResponse *) msg2;
                //get hash from client
                vector<unsigned char> nonceVector = response->getNonce();
                string recHash = response->getHash();
                //mysql
                try {
                    sql::Driver *driver;
                    sql::Connection *con;
                    sql::Statement *stmt;
                    sql::ResultSet *res;

                    /* Create a connection */
                    driver = get_driver_instance();
                    con = driver->connect(DBIP, DBUSER, DBPASS);
                    /* Connect to the MySQL test database */
                    //con->setSchema("chatserver");

                    stmt = con->createStatement();
                    stmt->execute("USE chatserver");
                    //res = stmt->executeQuery("SELECT * FROM login WHERE email = 'jonhand93@gmail.com' AND password = 'derp'");
                    string query = "SELECT password FROM login WHERE email = '";
                    query+=username;
                    query+="'";
                    //cout<<query<<endl;
                    res = stmt->executeQuery(query);
                    //res = stmt->executeQuery("SELECT password FROM login WHERE email = '"<<username<<"' AND password = 'derp'");
                    //res = stmt->executeQuery("SELECT id, label FROM test ORDER BY id ASC");
                    while (res->next()) {
                        //set flag to true
                        password= res->getString(1);
                        //cout<<password<<endl;
                    }
                    delete res;
                    delete stmt;
                    delete con;

                } catch (sql::SQLException &e) {
                    cout <<"problem getting username/password combo";
                    cout << "# ERR: SQLException in " << __FILE__;
                    cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
                    cout << "# ERR: " << e.what();
                    cout << " (MySQL error code: " << e.getErrorCode();
                    cout << ", SQLState: " << e.getSQLState() << " )" << endl;
                }
                //to add
                //no such user
                //to add
                if (password=="") {
                    delete sock;
                }
                //mysql
                //start genereating our side of hash
                char pass[password.size()];
                strcpy(pass, password.c_str());
                int challengeSize = sizeof(challenge)/ sizeof(challenge[0]);
                int passwordSize = password.size();
                int nonceSize = nonceVector.size();
                int sizeConc = challengeSize+passwordSize +nonceSize;
                unsigned char concatenateMe[sizeConc];
                for(int i=0; i<challengeSize; i++) {
                    concatenateMe[i]=challenge[i];
                }
                for(int i=challengeSize, j=0; i<challengeSize+nonceSize; i++, j++) {
                    concatenateMe[i]=nonceVector[j];

                }
                for(int i=challengeSize+nonceSize, j=0; i<sizeConc; i++, j++) {
                    concatenateMe[i]=password[j];
                }
                char buffer[65];
                sha256_varbytes(concatenateMe, buffer, sizeConc);
                //check if hashes are equal
                //send ack
                if (response->getHash()==buffer) {
                    SPChatACK spChatACK(SPChatACK::PASSWORD_AUTH, SPChatACK::SERVER);
                    vector<unsigned char> encodingACK;
                    spChatACK.encode(&encodingACK);
                    sock->send(encodingACK.data(), spChatACK.getTotalMsgLength());
                }
                //to add
                //no such user/pass combo
                //to add
                else {
                    delete sock;
                }
                //continue with second part of authentication
                //get next msg
                msg = SPChatMessage::decode(sock);
                //if type is login
                if(msg->getType() == SPChatLogin::SPCHATLOGIN_TYPE) {
                    SPChatLogin *login = (SPChatLogin *) msg;
                    /////
                    ///// RSA PART
                    /////
                    AutoSeededRandomPool rng;

                    InvertibleRSAFunction params;
                    params.GenerateRandomWithKeySize(rng, 1024);

                    CryptoPP::RSA::PrivateKey privateKey(params);
                    CryptoPP::RSA::PublicKey publicKey(params);

                    string saved;
                    StringSink ss(saved);
                    PEM_Save(ss, publicKey);

                    cout<<saved<<endl;
                    unsigned char *val=new unsigned char[saved.length()+1];
                    strcpy((char *)val,saved.c_str());
                    ////
                    //// END RSA PART
                    ////
                    //send server pub key to client
                    SPChatLogin server2Client("", SPChatLogin::ASYMMETRIC_KEY, val,
                                              SPChatLogin::ASYMMETRIC_LENGTH);
                    vector<unsigned char> encodingServer2Client;
                    server2Client.encode(&encodingServer2Client);
                    sock->send(encodingServer2Client.data(), server2Client.getTotalMsgLength());
                    //get next msg
                    SPChatMessage *msgNewLogin = SPChatMessage::decode(sock);
                    //if type login
                    if(msgNewLogin->getType() == SPChatLogin::SPCHATLOGIN_TYPE) {
                        //login with symm key
                        SPChatLogin *loginNew = (SPChatLogin *) msgNewLogin;
                        //actual decryption
                        string decryptedSymKey;
                        RSAES_PKCS1v15_Decryptor d(privateKey);
                        StringSource ss2(loginNew->getEncSymKey().data(), loginNew->getEncSymKey().size(),true,
                                         new PK_DecryptorFilter(rng, d,
                                                                new StringSink(decryptedSymKey)
                                         ) // PK_DecryptorFilter
                        ); // StringSource


                        //check if we got a symmetric key
                        //send second ack

                        string newkeyString = decryptedSymKey;
                        keyString = decryptedSymKey;
                        string ivString = "QtIkUCmeW0XTCybi";
                        char const *key = keyString.c_str();
                        int keysize=16;

                        if (keyString.length()==keysize) {
                            SPChatACK spChatACK2(SPChatACK::SUCCESS, SPChatACK::SERVER);
                            vector<unsigned char> encodingACK2;
                            spChatACK2.encode(&encodingACK2);
                            sock->send(encodingACK2.data(), spChatACK2.getTotalMsgLength());
                        }
                        //not proper symm key lenght
                        else {
                            delete sock;
                        }
                        //end of login operations


                    }
                    else {
                        delete sock;
                    }

                }
                else {
                    delete sock;
                }

            }
            else {
                delete sock;
            }
        }
        else {
            delete sock;
            //not following protocol
        }


        for (;;) {
            cout << "-----new msg----" << endl;
            SPChatMessage *msgs = SPChatMessage::decode(sock);
            cout << "Type: " << hex << unsigned(msgs->getType()) << dec << endl;
            //client sends chat poll (asking for msgs)
            if(msgs->getType() == SPChatPoll::SPCHATPOLL_TYPE) {
                SPChatPoll *newPoll = (SPChatPoll*) msgs;

                cout<<"username: "<<newPoll->getUsername()<<endl;
                cout<<"time: "<<newPoll->getTime()<<endl;

                string username = newPoll->getUsername();
                long time = newPoll->getTime();
                //real
                //mysql
                vector<string> timeDb;
                vector<string> senderDb;
                vector<string> messageDb;

                try {
                    sql::Driver *driver;
                    sql::Connection *con;
                    sql::Statement *stmt;
                    sql::ResultSet *res;

                    /* Create a connection */
                    driver = get_driver_instance();
                    con = driver->connect(DBIP, DBUSER, DBPASS);
                    /* Connect to the MySQL test database */
                    //con->setSchema("chatserver");

                    stmt = con->createStatement();
                    stmt->execute("USE chatserver");
                    //res = stmt->executeQuery("SELECT * FROM login WHERE email = 'jonhand93@gmail.com' AND password = 'derp'");
                //string query = "SELECT password FROM login WHERE email = '";
                    //string query = "SELECT * FROM messages INNER JOIN (SELECT message_id, reciever_id, sender_id, timestamp FROM transactions) as x ON messages.message_id = x.message_id INNER JOIN (SELECT user_id, email FROM login) as y ON x.reciever_id = y.user_id WHERE UNIX_TIMESTAMP(timestamp) > '";
                    string query = "SELECT text, y.email, z.email, timestamp FROM messages "
                            "INNER JOIN (SELECT message_id, reciever_id, sender_id, timestamp FROM transactions) "
                            "as x ON messages.message_id = x.message_id "
                            "INNER JOIN (SELECT user_id, email FROM login) "
                            "as y ON x.reciever_id = y.user_id "
                            "INNER JOIN (SELECT user_id, email FROM login)"
                            "as z ON x.sender_id = z.user_id "
                            "WHERE UNIX_TIMESTAMP(timestamp) > '";
                    query+="1428420682";
                    long stringTime=newPoll->getTime();
                    string finalStringTime;
                    stringstream tempStream;
                    tempStream << stringTime;
                    finalStringTime = tempStream.str();
                    //query+= finalStringTime;
                    query+="' AND y.email = '";
                    //query+="jonhand93@gmail.com";
                    query+=newPoll->getUsername();
                    query+="'";
                //query+=username;
                //query+="'";
                    //cout<<query<<endl;
                    res = stmt->executeQuery(query);
                    //res = stmt->executeQuery("SELECT password FROM login WHERE email = '"<<username<<"' AND password = 'derp'");
                    //res = stmt->executeQuery("SELECT id, label FROM test ORDER BY id ASC");
                    while (res->next()) {
                        //set flag to true
                    //password= res->getString(1);
                        //cout<<"while:"<<res->getString(1)<<res->getString(3)<<UnixTimeFromMysqlString(res->getString(4))<<endl;
                        messageDb.push_back(res->getString(1));
                        senderDb.push_back(res->getString(3));
                        timeDb.push_back(res->getString(4));
                    }
                    delete res;
                    delete stmt;
                    delete con;

                } catch (sql::SQLException &e) {
                    cout <<"problem getting username/password combo";
                    cout << "# ERR: SQLException in " << __FILE__;
                    cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
                    cout << "# ERR: " << e.what();
                    cout << " (MySQL error code: " << e.getErrorCode();
                    cout << ", SQLState: " << e.getSQLState() << " )" << endl;
                }





                //end of real

                //encode msg
                //char const *key = keyString.c_str();
                //cout<<"key length:"<<keyString.length()<<endl;
                //char const *ivToUse = ivString.c_str();
                string plain = "hi there brandy I really hope you are getting this cool!";
                string encodedWithSym;
                aesEncrypt(plain, keyString, ivString, encodedWithSym);

                //<encode msg>
                string sender = "Server";
                string rec = "test@test.com";
                vector<SPChatMsgMessage> msgList;
                msgList.push_back(SPChatMsgMessage(sender, rec, encodedWithSym, plain.length(),
                        1, 1, newPoll->getTime()));

                SPChatPollResponse pollRspns = SPChatPollResponse("Server", msgList, 1, 1);

                vector<unsigned char> encodingPR;
                pollRspns.encode(&encodingPR);
                sock->send(encodingPR.data(), pollRspns.getTotalMsgLength());

            }
            //client sends poll response (sending msgs)
            if(msgs->getType() == SPChatPollResponse::SPCHATPOLLRESPONSE_TYPE) {
                cout<<"WE ARE HERE>>>>>>>>>>"<<endl;
                SPChatPollResponse *pollRspns = (SPChatPollResponse*) msgs;
                for(int i = 0; i < pollRspns->getMsgList().size(); i++){
                    //each msg in the poll response
                    //pollRspns->getMsgList()[i].
                }
            }

        }


    } catch (SPChatException &e) {
        cout<<"exception:"<<e.what()<<endl;
    } catch (runtime_error e) {
        cerr << e.what() << endl;           // Report errors to the console.
    }catch( const CryptoPP::Exception& e )  {
        cerr << e.what() << endl;
    } catch (int e) {
        cout<<"error: "<<e<<endl;

    }

    delete sock;  // Free the socket object (and close the connection)
    return NULL;
}

void aesEncrypt(string plain, string inputKey, string inputIv, string &cipher) {
    try
    {
        //cout << "plain text: " << plain << endl;
        char const *ivToUse = inputIv.c_str();
        char const *key = inputKey.c_str();
        CBC_Mode< AES >::Encryption e;
        e.SetKeyWithIV((const byte *) key, size_t(keysize), (const byte *) ivToUse);

        // The StreamTransformationFilter adds padding
        //  as required. ECB and CBC Mode must be padded
        //  to the block size of the cipher.
        StringSource ss( plain, true,
                         new StreamTransformationFilter( e,
                                                         new StringSink( cipher )
                         ) // StreamTransformationFilter
        ); // StringSource
    }
    catch( const CryptoPP::Exception& e )
    {
        cerr << e.what() << endl;
    }
}

void aesDecrypt(string cipher, string inputKey, string inputIv, string &plain){
    try
    {
        CBC_Mode< AES >::Decryption d;
        char const *ivToUse = inputIv.c_str();
        char const *key = inputKey.c_str();
        d.SetKeyWithIV((const byte *) key, size_t(keysize), (const byte *) ivToUse);

        // The StreamTransformationFilter removes
        //  padding as required.
        StringSource ss( cipher, true,
                         new StreamTransformationFilter( d,
                                                         new StringSink( plain )
                         ) // StreamTransformationFilter
        ); // StringSource

        //cout << "recovered text: " << recovered << endl;
    }
    catch( const CryptoPP::Exception& e )
    {
        cerr << e.what() << endl;
    }
}

//https://stackoverflow.com/questions/16969844/c-unix-time-from-mysql-datetime-string
static long UnixTimeFromMysqlString(string s)

{

    struct tm tmlol;
    strptime(s.c_str(), "%Y-%m-%d %H:%M:%S", &tmlol);

    time_t t = mktime(&tmlol);
    return t;

}

/* query
 SELECT * FROM messages INNER JOIN (SELECT message_id, reciever_id, timestamp FROM transactions) as x ON messages.message_id = x.message_id INNER JOIN (SELECT user_id, email FROM login) as y ON x.reciever_id = y.user_id WHERE timestamp > '2016-03-23 22:38:23' AND email = 'jonhand93@gmail.com'
 */

/*
 SELECT text, y.email, z.email, timestamp FROM messages
INNER JOIN (SELECT message_id, reciever_id, sender_id, timestamp FROM transactions)
	as x ON messages.message_id = x.message_id
INNER JOIN (SELECT user_id, email FROM login)
	as y ON x.reciever_id = y.user_id
INNER JOIN (SELECT user_id, email FROM login)
	as z ON x.sender_id = z.user_id
WHERE UNIX_TIMESTAMP(timestamp) > '11111111' AND y.email = 'test@test.com'
 */