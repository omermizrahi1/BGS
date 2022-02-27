CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: BGSClient
	g++ -o bin/BGSClient bin/connectionHandler.o bin/echoClient.o bin/MessageEncoderDecoder.o bin/KeyboardReader.o bin/SocketReader.o $(LDFLAGS) 

BGSClient: bin/connectionHandler.o bin/echoClient.o bin/MessageEncoderDecoder.o bin/KeyboardReader.o bin/SocketReader.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp
	
bin/MessageEncoderDecoder.o: src/MessageEncoderDecoder.cpp
	g++ $(CFLAGS) -o bin/MessageEncoderDecoder.o src/MessageEncoderDecoder.cpp

bin/KeyboardReader.o: src/KeyboardReader.cpp
	g++ $(CFLAGS) -o bin/KeyboardReader.o src/KeyboardReader.cpp
	
bin/SocketReader.o: src/SocketReader.cpp
	g++ $(CFLAGS) -o bin/SocketReader.o src/SocketReader.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
