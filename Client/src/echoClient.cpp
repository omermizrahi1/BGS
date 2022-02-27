#include <stdlib.h>
#include "../include/connectionHandler.h"
#include "../include/MessageEncoderDecoder.h"
#include "../include/KeyboardReader.h"
#include "../include/SocketReader.h"

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

using std::cout;

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
	if (!connectionHandler.connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	cout << "connected to server"<< std::endl;

	KeyboardReader keyboard_reader (&connectionHandler);
	SocketReader socket_reader(&connectionHandler);
	std::thread th1(&KeyboardReader::run,&keyboard_reader);
	std::thread th2(&SocketReader::run, &socket_reader);
	th1.join();
	th2.join();
    return 0;
}
