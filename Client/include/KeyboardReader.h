//
// Created by omer5 on 01/01/2022.
//
#include <iostream>
#include <mutex>
#include <thread>
#include "../include/connectionHandler.h"
#include "../include/MessageEncoderDecoder.h"
#ifndef SPL3_CLIENT_KEYBOARDREADER_H
#define SPL3_CLIENT_KEYBOARDREADER_H
using std::mutex;


class KeyboardReader {
private:
/*	int id;
	mutex &_mutex;*/
	ConnectionHandler* connectionHandler;

public:
	KeyboardReader(ConnectionHandler* connectionHandler);
	void run();

};



#endif //SPL3_CLIENT_KEYBOARDREADER_H
