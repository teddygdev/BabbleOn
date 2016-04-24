cmake_minimum_required(VERSION 3.3)
project(mockServer)
find_package(OpenSSL REQUIRED)


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread -fpermissive -lmysqlcppconn")
#-I/usr/local/ssl/include -I/usr/local/ssl/include/openssl -lcrypto
#INCLUDE_DIRECTORIES(openssl/)
#include_directories(/usr/local/ssl/include)

#Ref from https://cmake.org/pipermail/cmake/2010-May/037159.html
####################################################################################
# - Try to find Mysql-Connector-C++
# Once done, this will define
#
#  MYSQLCONNECTORCPP_FOUND - system has Mysql-Connector-C++ installed
#  MYSQLCONNECTORCPP_INCLUDE_DIRS - the Mysql-Connector-C++ include directories
#  MYSQLCONNECTORCPP_LIBRARIES - link these to use Mysql-Connector-C++
#
# The user may wish to set, in the CMake GUI or otherwise, this variable:
#  MYSQLCONNECTORCPP_ROOT_DIR - path to start searching for the module

set(MYSQLCONNECTORCPP_ROOT_DIR
        "${MYSQLCONNECTORCPP_ROOT_DIR}"
        CACHE
        PATH
        "Where to start looking for this component.")

if(WIN32)
    find_path(MYSQLCONNECTORCPP_INCLUDE_DIR
            NAMES
            mysql_connection.h
            PATHS
            "C:\\Program Files"
            HINTS
            ${MYSQLCONNECTORCPP_ROOT_DIR}
            PATH_SUFFIXES
            include)

    find_library(MYSQLCONNECTORCPP_LIBRARY
            NAMES
            mysqlcppconn
            mysqlcppconn-static
            HINTS
            ${MYSQLCONNECTORCPP_ROOT_DIR}
            PATH_SUFFIXES
            lib)

else()
    find_path(MYSQLCONNECTORCPP_INCLUDE_DIR
            mysql_connection.h
            HINTS
            ${MYSQLCONNECTORCPP_ROOT_DIR}
            PATH_SUFFIXES
            include)

    find_library(MYSQLCONNECTORCPP_LIBRARY
            NAMES
            mysqlcppconn
            mysqlcppconn-static
            HINTS
            ${MYSQLCONNECTORCPP_ROOT_DIR}
            PATH_SUFFIXES
            lib64
            lib)
endif()

mark_as_advanced(MYSQLCONNECTORCPP_INCLUDE_DIR MYSQLCONNECTORCPP_LIBRARY)

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(MysqlConnectorCpp
        DEFAULT_MSG
        MYSQLCONNECTORCPP_INCLUDE_DIR
        MYSQLCONNECTORCPP_LIBRARY)

if(MYSQLCONNECTORCPP_FOUND)
    set(MYSQLCONNECTORCPP_INCLUDE_DIRS "${MYSQLCONNECTORCPP_INCLUDE_DIR}") # Add any dependencies here
    set(MYSQLCONNECTORCPP_LIBRARIES "${MYSQLCONNECTORCPP_LIBRARY}") # Add any dependencies here
    mark_as_advanced(MYSQLCONNECTORCPP_ROOT_DIR)
endif()
#########################################################################################

# Module for locating the Crypto++ encryption library.
#
# Customizable variables:
#   CRYPTOPP_ROOT_DIR
#     This variable points to the CryptoPP root directory. On Windows the
#     library location typically will have to be provided explicitly using the
#     -D command-line option. The directory should include the include/cryptopp,
#     lib and/or bin sub-directories.
#
# Read-only variables:
#   CRYPTOPP_FOUND
#     Indicates whether the library has been found.
#
#   CRYPTOPP_INCLUDE_DIRS
#     Points to the CryptoPP include directory.
#
#   CRYPTOPP_LIBRARIES
#     Points to the CryptoPP libraries that should be passed to
#     target_link_libararies.
#
#
# Copyright (c) 2012 Sergiu Dotenco
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

INCLUDE (FindPackageHandleStandardArgs)

FIND_PATH (CRYPTOPP_ROOT_DIR
        NAMES cryptopp/cryptlib.h include/cryptopp/cryptlib.h
        PATHS ENV CRYPTOPPROOT
        DOC "CryptoPP root directory")

# Re-use the previous path:
FIND_PATH (CRYPTOPP_INCLUDE_DIR
        NAMES cryptopp/cryptlib.h
        HINTS ${CRYPTOPP_ROOT_DIR}
        PATH_SUFFIXES include
        DOC "CryptoPP include directory")

FIND_LIBRARY (CRYPTOPP_LIBRARY_DEBUG
        NAMES cryptlibd cryptoppd
        HINTS ${CRYPTOPP_ROOT_DIR}
        PATH_SUFFIXES lib
        DOC "CryptoPP debug library")

FIND_LIBRARY (CRYPTOPP_LIBRARY_RELEASE
        NAMES cryptlib cryptopp
        HINTS ${CRYPTOPP_ROOT_DIR}
        PATH_SUFFIXES lib
        DOC "CryptoPP release library")

IF (CRYPTOPP_LIBRARY_DEBUG AND CRYPTOPP_LIBRARY_RELEASE)
    SET (CRYPTOPP_LIBRARY
            optimized ${CRYPTOPP_LIBRARY_RELEASE}
            debug ${CRYPTOPP_LIBRARY_DEBUG} CACHE DOC "CryptoPP library")
ELSEIF (CRYPTOPP_LIBRARY_RELEASE)
    SET (CRYPTOPP_LIBRARY ${CRYPTOPP_LIBRARY_RELEASE} CACHE DOC
            "CryptoPP library")
ENDIF (CRYPTOPP_LIBRARY_DEBUG AND CRYPTOPP_LIBRARY_RELEASE)

IF (CRYPTOPP_INCLUDE_DIR)
    SET (_CRYPTOPP_VERSION_HEADER ${CRYPTOPP_INCLUDE_DIR}/cryptopp/config.h)

    IF (EXISTS ${_CRYPTOPP_VERSION_HEADER})
        FILE (STRINGS ${_CRYPTOPP_VERSION_HEADER} _CRYPTOPP_VERSION_TMP REGEX
                "^#define CRYPTOPP_VERSION[ \t]+[0-9]+$")

        STRING (REGEX REPLACE
                "^#define CRYPTOPP_VERSION[ \t]+([0-9]+)" "\\1" _CRYPTOPP_VERSION_TMP
                ${_CRYPTOPP_VERSION_TMP})

        STRING (REGEX REPLACE "([0-9]+)[0-9][0-9]" "\\1" CRYPTOPP_VERSION_MAJOR
                ${_CRYPTOPP_VERSION_TMP})
        STRING (REGEX REPLACE "[0-9]([0-9])[0-9]" "\\1" CRYPTOPP_VERSION_MINOR
                ${_CRYPTOPP_VERSION_TMP})
        STRING (REGEX REPLACE "[0-9][0-9]([0-9])" "\\1" CRYPTOPP_VERSION_PATCH
                ${_CRYPTOPP_VERSION_TMP})

        SET (CRYPTOPP_VERSION_COUNT 3)
        SET (CRYPTOPP_VERSION
                ${CRYPTOPP_VERSION_MAJOR}.${CRYPTOPP_VERSION_MINOR}.${CRYPTOPP_VERSION_PATCH})
    ENDIF (EXISTS ${_CRYPTOPP_VERSION_HEADER})
ENDIF (CRYPTOPP_INCLUDE_DIR)

SET (CRYPTOPP_INCLUDE_DIRS ${CRYPTOPP_INCLUDE_DIR})
SET (CRYPTOPP_LIBRARIES ${CRYPTOPP_LIBRARY})

MARK_AS_ADVANCED (CRYPTOPP_INCLUDE_DIR CRYPTOPP_LIBRARY CRYPTOPP_LIBRARY_DEBUG
        CRYPTOPP_LIBRARY_RELEASE)

FIND_PACKAGE_HANDLE_STANDARD_ARGS (CryptoPP REQUIRED_VARS CRYPTOPP_ROOT_DIR
        CRYPTOPP_INCLUDE_DIR CRYPTOPP_LIBRARY VERSION_VAR CRYPTOPP_VERSION)
###################################################################################


set(SOURCE_FILES MockServer.cpp PracticalSocket.cpp PracticalSocket.h MockServer.cpp Sha256Lib.h
        protocol/SPChatMessage.h protocol/SPChatMessage.cpp protocol/SPChatLogin.cpp protocol/SPChatLogin.h protocol/SPChatChallenge.cpp
        protocol/SPChatChallenge.h protocol/SPChatResponse.cpp protocol/SPChatResponse.h protocol/SPChatACK.h protocol/SPChatACK.cpp
        protocol/SPChatException.h protocol/SPChatMsgMessage.cpp protocol/SPChatMsgMessage.h protocol/SPChatPoll.cpp protocol/SPChatPoll.h
        protocol/SPChatPollResponse.cpp protocol/SPChatPollResponse.h db/include/mysql_connection.h db/include/cppconn/driver.h db/include/cppconn/exception.h db/include/cppconn/resultset.h
        db/include/cppconn/statement.h db/include/cppconn/prepared_statement.h protocol/SPChatException.cpp db/include/mysql_driver.h db/include/mysql_error.h
        )
add_executable(mockServer ${SOURCE_FILES})
target_link_libraries( mockServer PRIVATE ${OPENSSL_LIBRARIES} ${MYSQLCONNECTORCPP_LIBRARY} ${CRYPTOPP_LIBRARIES})
