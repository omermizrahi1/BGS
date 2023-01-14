### HOW TO RUN THE CODE:

## SERVER:
Use this command:
mvn compile
in order to compile and build the project files.

# THREAD PER CLIENT SERVER:
* run the bash script: tcp_server.sh
or
use this command:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="<port>"

# REACTOR SERVER:
* run the bash script: reactor_server.sh
or
use this command:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="<port> <Num of threads>"

## CLIENT:
* run the bash script: client.sh
or
open the client folder with terminal. type "make" to compile the code, and then use the command "./bin/BGSClient <ip address> <port>"
after that the client application should be running and connect to the server.


### CLIENT'S INPUT:
# We asusme that the client's input is valid.
# Commands can be written either in capital letters or in lowercase letter.
# If the command that written in the client is not valid, nothing will be sent to the server.
# If the command is valid, we assume that the format of the message is valid.
# While registering, birthdays should be submitted in the following format DD-MM-YYYY.
# You should not use the following characters: '|', ';'.  the former is used as a separator in STAT command,
and the latter is used as a delimiter in messages that sent in client-to-server and server-to-client communication.
# STAT messages should be sent in the following format: STAT user1|user2|...userN|. a pipe ('|') should be in the end of each username.


### POSSIBLE COMMANDS AND THIER FORMATS:
# REGISTER: register <username> <password> <birthday in DD-MM-YYYY formant>
# LOGIN: login <username> <password> <captcha charchter 0/1> (captcha character shuold be 1)
# LOGOUT: logout
# FOLLOW/UNFOLLOW: follow <0 to follow/1 to unfollow> <username>
# POST: post <content>
# PERSONAL MESSAGE: pm <username> <content>
# LOGSTAT: logstat
# STAT: stat <username1>|<username2>|...<usernamen>|
# BLOCK: block <username>


### FILTERED WORDS:
Filtered words are stored as a field in the DataBase Class. (bgu.spl.net.srv.DataBase, field name is "filterdWords")
