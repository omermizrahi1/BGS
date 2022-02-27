//
// Created by omer5 on 01/01/2022.
//

#include "../include/SocketReader.h"

SocketReader::SocketReader(ConnectionHandler *connectionHandler) : connectionHandler(connectionHandler) {}

void SocketReader::run() {
	
	while (1) {
		std::string answer;
		// Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
		// We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
		if (!connectionHandler->getLine(answer)) {
			std::cout << "Disconnected. Exiting..." << std::endl;
			break;
		}

		std::cout << answer << std::endl;
		//if the server returns the logout ack message then the client should disconnect.
		if (answer == "ACK-10 LOGOUT-3") {
			connectionHandler->disconnect();
			std::cout << "Disconnected. Exiting...\npress ENTER to continue..." << std::endl;
			break;
		}
	}
}