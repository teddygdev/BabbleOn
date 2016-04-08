#ifndef SPCHATEXCEPTION_H_INCLUDED
#define SPCHATEXCEPTION_H_INCLUDED

/**
 * Author:  Kelly Ward
 */

#include <stdexcept>

using namespace std;


const static string BAD_INPUT_EXCEPTION = "Invalid input";     // General bad input message
const static string TOO_SHORT_EXCEPTION = "Buffer length too short";     // Buffer length is too small
const static string NEGATIVE_NUM_EXCEPTION = "Negative number";  // Negative number when expecting positive
const static string BAD_USERNAME_EXCEPTION = "Invalid username";  // Invalid username
const static string BAD_HEADER_EXCEPTION = "Invalid header";    // Decoded header doesn't match
const static string NO_SUCH_TYPE_EXCEPTION = "Invalid message type";  // Message type doesn't exist
const static string BAD_KEY_EXCEPTION = "Invalid key";       // Key decoding failed
const static string BAD_CHALLENGE_EXCEPTION = "Invalid challenge"; // Challenge length is incorrect
const static string INVALID_ERROR_EXCEPTION = "Invaid ACK error code"; // Invalid error code
const static string INVALID_SENDER_EXCEPTION = "Invalid ACK sender code";// Invalid sender bit
const static string MSG_LENGTH_EXCEPTION = "Exceeded max message length";    // MsgMessage's message length exceeds max
const static string ASYMMETRIC_KEY_EXCEPTION = "Invalid asymmetric key";    // MsgMessage's message length exceeds max

/**
 *   Signals a problem with the execution of a socket call.
 */
class SPChatException : public std::runtime_error {
public:
  /**
   *   Construct a SPChatException with a user message followed by a
   *   system detail message.
   *   @param message explanatory message
   */
  SPChatException(const std::string &message) throw();

};



#endif // SPCHATEXCEPTION_H_INCLUDED
