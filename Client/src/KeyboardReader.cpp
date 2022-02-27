//
// Created by omer5 on 01/01/2022.
//

#include "../include/KeyboardReader.h"
KeyboardReader :: KeyboardReader(ConnectionHandler* connectionHandler):connectionHandler(connectionHandler){}
void KeyboardReader :: run(){

	const vector<string> legal_commands = {"login","logout", "register",
									 "follow", "post", "pm",
									 "stat","logstat","block"};
	const short bufsize = 1024;

	while (1) {

		char buf[bufsize];
		std::cin.getline(buf, bufsize);
		std::string line(buf);
		std::string command = MessageEncoderDecoder::to_lower(line.substr(0, line.find(" ")));
		if (!connectionHandler-> isConnected()){
			break;
		}
		else if (count(legal_commands.begin(), legal_commands.end(), command)==0){
			std::cout << "Illegal command. Try Again" << std::endl;
		}
		else if (!connectionHandler->sendLine(line)) {
			std::cout << "Disconnected. Exiting... " << std::endl;
			break;
		}

	}


	};