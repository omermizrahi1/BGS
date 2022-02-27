//
// Created by omer5 on 01/01/2022.
//
#include "../include/connectionHandler.h"
#include "../include/MessageEncoderDecoder.h"

#ifndef SPL3_CLIENT_SOCKETREADER_H
#define SPL3_CLIENT_SOCKETREADER_H



class SocketReader {
private:
	ConnectionHandler *connectionHandler;

public:
	SocketReader(ConnectionHandler* connectionHandler);
	void run();

};


#endif //SPL3_CLIENT_SOCKETREADER_H
