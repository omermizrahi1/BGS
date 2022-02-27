//
// Created by omer5 on 01/01/2022.
//

#include "../include/MessageEncoderDecoder.h"
using namespace std;



int MessageEncoderDecoder:: encode(string msg, char* bytes){
	vector<string> words = split_input(msg);
	string msg_type=words[0];
	words.erase(words.begin());
	int i=0;
	int j=0;
	for (string str: words) {
		if (msg_type == "block"){
			if (i == 0) {
				shortToBytes(12,bytes);
				i = 2;
			}
			for (char c: str) {
				bytes[i++] = c;
			}
			bytes[i++] = '\0';

		}
		if (msg_type == "register") {
			if (i == 0) {
				shortToBytes(1,bytes);
				i = 2;
			}
			for (char c: str) {
				bytes[i++] = c;
			}
			bytes[i++] = '\0';
		}

		if (msg_type == "login") {
			if (str == "0"){
				bytes[i++] = '\0';
		} else if (str == "1") {
			bytes[i++] = '\1';

		} else {
			if (i == 0) {
				shortToBytes(2,bytes);
				i = 2;
			}
			for (char c: str) {
				bytes[i++] = c;
			}
			bytes[i++] = '\0';
		}
	}


		if (msg_type == "follow") {
			if (i == 0) {
					shortToBytes(4,bytes);
					i = 2;
				}
			if (str == "0") {
				bytes[i++] = '\0';
			} else if (str == "1") {
				bytes[i++] = '\1';
			} else {
				
				for (char c: str) {
					bytes[i++] = c;
				}
			}
		}
		if (msg_type=="post") {
			if (i == 0) {
				shortToBytes(5,bytes);
				i = 2;
			}
			for (char c: str) {
				bytes[i++] = c;
			}
			if (str!=words[words.size()-1]){
				bytes[i++]=' ';
			}
		}
		if (msg_type=="pm") {

			if (i == 0) {
				shortToBytes(6,bytes);
				i = 2;
			}
			for (char c: str) {
				bytes[i++] = c;
			}
				if (j==0) {
					bytes[i++] = '\0';
					j++;
				}
				else if (str!=words[words.size()-1]){
					bytes[i++]=' ';
				}

		}

		if (msg_type == "stat") {
			if (i == 0) {
				shortToBytes(8,bytes);
				i = 2;
			}
			for (char c: str) {
				bytes[i++] = c;
			}
			bytes[i++] = '\0';
		}
	}
	if (msg_type == "post"){
		bytes[i++]='\0';
	}
	if (msg_type=="pm"){
		bytes[i++] = '\0';
		string date_time=get_date_and_time();
		for (char c: date_time) {
			bytes[i++] = c;
		}
		bytes[i++] = '\0';
	}
	if (msg_type == "logout") {
		shortToBytes(3,bytes);
		i = 2;
	}
	if (msg_type == "logstat"){
		shortToBytes(7,bytes);
		i = 2;
	}

	return i;

}

string MessageEncoderDecoder:: decode (char bytes[], unsigned int bytesToRead){
	string message = "";
	char opcode_bytes[2];
	opcode_bytes[0]=bytes[0];
	opcode_bytes[1]=bytes[1];
	short opcode = bytesToShort(opcode_bytes);
	vector<string> v;
	switch (opcode) {

		case 9: {//notification

			char post_pm = bytes[2];
			string post_or_pm = post_pm == '\0' ? "PM" : "Public";
			string section = "";
			for (unsigned int i = 3; i < bytesToRead && bytes[i] != ';'; ++i) {

				if (bytes[i] == '\0') {
					v.push_back(section);
					section = "";
				}
				else{
					section += bytes[i];
				}
			}
			string posting_user = v[0];
			string content = v[1];
			message = "NOTIFICATION " + post_or_pm + " " + posting_user + " " + content;
		}
		break;

		case 10://ACK message
			message = decode_ack_message(bytes,2,bytesToRead);

			break;

		case 11: //error
			char err[2];
			err[0] = bytes[2] ;
			err[1] = bytes[3];
			string error = std::to_string(bytesToShort(err));

			message="ERROR " + error;
			break;

	}
	return message;


}

string MessageEncoderDecoder:: decode_ack_message(char bytes [], unsigned  int start,unsigned int bytesToRead){
	string message;
	char op[2];
	op[0] = bytes[start];
	op[1] = bytes[start+1];
	short ack_op = bytesToShort(op);
	switch (ack_op) {

		case 1: //register ack
			message = "ACK-10 REGISTER-1";
			break;

		case 2: // login ack
			message = "ACK-10 LOGIN-2";
			break;

		case 3: //logout ack
			message = "ACK-10 LOGOUT-3";
			break;

		case 4:{ // follow ack
			string username = "";
			for (unsigned int i = start + 2; i < bytesToRead && bytes[i] != '\0'; ++i) {
				username += bytes[i];
			}

			message = "ACK-10 FOLLOW-4 " + username;
		}
			break;
		case 5: // post ack
			message = "ACK-10 POST-5";
			break;
		case 6: //PM ack
			message = "ACK-10 PM-6";
			break;
		case 12: //block ack
			message = "ACK-10 BLOCK-12";
			break;

		case 7: {
			char age_bytes[2];
			age_bytes[0] = bytes[start + 2];
			age_bytes[1] = bytes[start + 3];
			string age = std::to_string(bytesToShort(age_bytes));
			char num_post[2];
			num_post[0] = bytes[start + 4];
			num_post[1] = bytes[start + 5];
			string posts = std::to_string(bytesToShort(num_post));
			char num_followers[2];
			num_followers[0] = bytes[start + 6];
			num_followers[1] = bytes[start + 7];
			string followers = std::to_string(bytesToShort(num_followers));
			char num_following[2];
			num_following[0] = bytes[start + 8];
			num_following[1] = bytes[start + 9];
			string following = std::to_string(bytesToShort(num_following));

			message = "ACK-10 LOGSTAT-7 Age:" + age + " Number of posts:" + posts + " Followers:"  + followers + " Following:" + following;
		}
			break;
		case 8:{ //stat ack
			char age_bytes[2];
			age_bytes[0] = bytes[start + 2];
			age_bytes[1] = bytes[start + 3];
			string age = std::to_string(bytesToShort(age_bytes));
			char num_post[2];
			num_post[0] = bytes[start + 4];
			num_post[1] = bytes[start + 5];
			string posts = std::to_string(bytesToShort(num_post));
			char num_followers[2];
			num_followers[0] = bytes[start + 6];
			num_followers[1] = bytes[start + 7];
			string followers = std::to_string(bytesToShort(num_followers));
			char num_following[2];
			num_following[0] = bytes[start + 8];
			num_following[1] = bytes[start + 9];
			string following = std::to_string(bytesToShort(num_following));
			message = "ACK-10 STAT-8 Age:" + age + " Number of posts:" + posts + " Followers:" + " " + followers + " Following:" + following;
		}

			break;

	}
	return message;
}

string MessageEncoderDecoder:: time_to_string(int x){
	string str;
	if (x<10){
		str="0" + std::to_string(x);
	}
	else{
		str=std::to_string(x);
	}
	return str;
}

string MessageEncoderDecoder:: get_date_and_time(){
	time_t now = time(0);
	tm *ltm = localtime(&now);
	string year = std::to_string(1900 + ltm->tm_year);
	string day = time_to_string(ltm->tm_mday);
	string month = time_to_string(ltm->tm_mon+1);
	string hour = time_to_string(ltm->tm_hour);
	string minute = time_to_string(ltm->tm_min);
	return day+"-"+month+"-"+year + " " + hour + ":" + minute;

}

vector<string> MessageEncoderDecoder:: split_input(string msg){
	string message = to_lower(msg);
	vector<string> v;
	string delimiter = " ";
	size_t pos = 0;
	while ((pos = message.find(delimiter)) != string::npos) {
		v.push_back(message.substr(0, pos));
		message.erase(0, pos + delimiter.length());
	}
	v.push_back(message);
	return v;
}

string MessageEncoderDecoder::to_lower(string str) {
	string s="";
	for (char c: str){
		s+= tolower(c);
	}
	return s;
}

short MessageEncoderDecoder:: bytesToShort(char* bytesArr){
	short result = (short)((bytesArr[0] & 0xff) << 8);
	result += (short)(bytesArr[1] & 0xff);
	return result;

}

void MessageEncoderDecoder::shortToBytes(short num, char* bytesArr){
	bytesArr[0] = ((num >> 8) & 0xFF);
	bytesArr[1] = (num & 0xFF);
}

