/**
 * Author:  Kelly Ward
 */

#include "SPChatException.h"
using namespace std;

SPChatException::SPChatException(const string &message) throw() :
        runtime_error(message) {
}
