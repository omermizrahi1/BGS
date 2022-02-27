//
// Created by omer5 on 01/01/2022.
//
#include <iostream>
#include <vector>
#include <ctime>

#ifndef SPL3_CLIENT_MESSAGEENCODERDECODER_H
#define SPL3_CLIENT_MESSAGEENCODERDECODER_H

using std::string;
using std::vector;


class MessageEncoderDecoder {
	private:

		static vector<string> split_input(string msg);
		static string time_to_string(int x);
		static string get_date_and_time();
		static short bytesToShort(char* bytesArr);
		static string decode_ack_message(char bytes [], unsigned int start, unsigned int bytesToRead);
		static void shortToBytes(short num, char* bytesArr);


	public:
		MessageEncoderDecoder();
		static int encode(string msg, char bytes[]);
		static string decode (char bytes[], unsigned int bytesToRead);
		static string to_lower(string str);

};


#endif //SPL3_CLIENT_MESSAGEENCODERDECODER_H
