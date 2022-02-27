package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class BGSProtocol implements BidiMessagingProtocol<List<Object>> {

    private boolean shouldTerminate = false;
    private DataBase dataBase = DataBase.getInstance();
    private ConnectionsImpl connections;
    private int connectionId;

    public BGSProtocol(ConnectionsImpl connections) {
        this.connections = connections;
    }

    public void start(int connectionId, Connections connections){
        this.connectionId = connectionId;
    }

    public void process(List<Object> message) {
        Iterator<Object> iter = message.iterator();
        short opcode = (short)iter.next();
        switch (opcode) {
            case 1: //Register
                String[] userData = new String[3];
                int i = 0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        userData[i++] = (String) o;
                    }
                }
                String regUsername = userData[0]; String regPassword = userData[1]; String regDate = userData[2];
                Boolean registerUser = dataBase.registerUser(regUsername, regPassword, regDate);
                if(registerUser){
                    connections.send(connectionId, generalAck(opcode));
                }else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 2: //Login
                String[] userLoginData = new String[2];
                i = 0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        userLoginData[i++] = (String) o;
                    }
                }
                String loginUsername = userLoginData[0];
                String loginPassword = userLoginData[1];
                boolean captcha = (short) message.get(message.size() - 1) == 1;
                User newUser = dataBase.verifyUserDetails(loginUsername, loginPassword, captcha);
                if(newUser != null){
                    newUser.login();
                    connections.addUserForConnection(newUser, connectionId);
                    connections.send(connectionId, generalAck(opcode));
                    for (Message msg: dataBase.getPendingNotifications(newUser) ){
                        byte type = msg instanceof PM ? (byte) 0 : (byte) 1;
                        List<Object> ack = Arrays.asList(new Object[]{(short)9, type, msg.getSender().getUsername(), (byte)0, msg.getMessage(), (byte)0 });
                        connections.send(connectionId, ack);
                    }
                    dataBase.getPendingNotifications(newUser).clear();

                } else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 3: //Logout
                if(connections.checkIfUserLogIn(connectionId)){
                    connections.send(connectionId, generalAck(opcode));
                    User user = (User) connections.getConnectionsByUser().get(connectionId);
                    user.logout();
                    connections.disconnect(connectionId);
                    shouldTerminate = true;
                } else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 4: // Follow(0)/Unfollow(1)
                boolean follow = (short)iter.next() == 0;
                String userNameToFollow = (String) iter.next();
                User userToFollow = dataBase.getUserByName(userNameToFollow);
                User currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId) && userToFollow != null && !userToFollow.equals(currentUser)){
                    if (follow){
                        if (!dataBase.checkIfUserFollowerOfOtherUser(currentUser, userToFollow) && dataBase.canCommunicate(currentUser, userToFollow)){
                            dataBase.follow(currentUser, userToFollow);
                            connections.send(connectionId,ackFollowResponse(userNameToFollow));
                        }else {
                            connections.send(connectionId,errorResponse(opcode));
                        }
                    }else {
                        if (dataBase.checkIfUserFollowerOfOtherUser(currentUser, userToFollow)) {
                            dataBase.unfollow(currentUser, userToFollow);
                            connections.send(connectionId,ackFollowResponse(userNameToFollow));
                        }else {
                            connections.send(connectionId,errorResponse(opcode));
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 5: //Post
                String content = (String) iter.next();
                currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId)){
                    List<User> taggedUsers = getTaggedUsers(content);
                    dataBase.removeBlockedUsers(currentUser,taggedUsers);
                    List<User> followingUsers = dataBase.getFollowers(currentUser);
                    List<User> postUsers = checkIfTaggedUserIsFollower(taggedUsers, followingUsers);
                    postUsers.remove(currentUser);
                    List<User> notLoggedUsers = new LinkedList<>();
                    Post post = new Post(currentUser,content);
                    dataBase.addPost(currentUser, post);
                    for (User user : postUsers){
                        if(dataBase.canCommunicate(currentUser,user)){
                            if (user.isLogged()) {
                                int id = connections.getConnectionIdByUser(user);
                                connections.send(id, ackPostResponse(currentUser.getUsername(), content));
                            } else {
                                notLoggedUsers.add(user);
                            }
                        }
                    }
                    dataBase.addMessageToPendingNotifications(notLoggedUsers, post);
                    connections.send(connectionId, generalAck(opcode));
                }else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 6: //PM
                String[] PMData = new String[3];
                i = 0;
                while (iter.hasNext()) {
                    Object o = iter.next();
                    if (o instanceof String) {
                        PMData[i++] = (String) o;
                    }
                }
                String PMUsername = PMData[0];
                String PMContent = PMData[1];
                String PMDateTime = PMData[2];  //format DD-MM-YYYY HH:MM
                User sendTo = dataBase.getUserByName(PMUsername);
                String filteredMessage = dataBase.filter(PMContent);
                LocalDateTime dateTime = convertToDateTime(PMDateTime);
                currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId) && sendTo != null && dataBase.canCommunicate(currentUser,sendTo)
                                && !sendTo.equals(currentUser) && dataBase.checkIfUserFollowerOfOtherUser(currentUser,sendTo)){
                    PM pm = new PM(currentUser, filteredMessage);
                    dataBase.addPM(currentUser, pm);
                    if(sendTo.isLogged()){
                        int id = connections.getConnectionIdByUser(sendTo);
                        connections.send(id, ackPmResponse(currentUser.getUsername(), filteredMessage));
                    } else {
                        dataBase.addMessageToPendingNotifications(sendTo, pm);
                    }
                    connections.send(connectionId, generalAck(opcode));
                } else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 7: //LOGSTAT
                currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId)){
                    for (User user : dataBase.getRegisteredUserList()){
                        if(user.isLogged() && dataBase.canCommunicate(currentUser,user)) {
                            connections.send(connectionId, ackStatAndLogStat(7, user));
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 8: //STAT
                String usersString = (String)iter.next();
                String [] users = usersString.split("\\|");
                currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId)) {
                    boolean allUsersExisting = true;
                    for (int j = 0; j < users.length && allUsersExisting; j++) {
                        User user = dataBase.getUserByName(users[j]);
                        if (user == null) {
                            allUsersExisting = false;
                        }
                    }
                    if (allUsersExisting) {
                        for (String userName : users) {
                            User user = dataBase.getUserByName(userName);
                            if (dataBase.canCommunicate(currentUser, user)){
                                connections.send(connectionId, ackStatAndLogStat(8, user));
                            }
                        }
                    } else {
                        connections.send(connectionId, errorResponse(opcode));
                    }
                } else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 12: //BLOCK
                String userToBlock_s = (String)iter.next();
                currentUser = connections.getUserByConnectionId(connectionId);
                User userToBlock = dataBase.getUserByName(userToBlock_s);
                if(connections.checkIfUserLogIn(connectionId) && userToBlock != null &&
                    !userToBlock.equals(currentUser)) {
                    if(dataBase.block(currentUser,userToBlock)){
                        if (dataBase.checkIfUserFollowerOfOtherUser(currentUser, userToBlock)){
                            dataBase.unfollow(currentUser, userToBlock);
                        }
                        if(dataBase.checkIfUserFollowerOfOtherUser(userToBlock, currentUser)){
                            dataBase.unfollow(userToBlock, currentUser);
                        }
                        connections.send(connectionId, generalAck(opcode));
                    } else{
                        //sends an error beacuse user tried to block someone who's he already blocking.
                        connections.send(connectionId, errorResponse(opcode));
                    }
                }else {
                    //if blocking user isn't logged in or user that the logged user trying to block do not exists
                    connections.send(connectionId, errorResponse(opcode));
                }
                break;
        }
    }

    public boolean shouldTerminate(){
        return shouldTerminate;
    }

    private LocalDateTime convertToDateTime(String dateTime){
        String[] s = dateTime.split(" ");
        String[] date = s[0].split("-");
        int days = Integer.parseInt(date[0]);
        int months = Integer.parseInt(date[1]);
        int years = Integer.parseInt(date[2]);
        String[] time = s[1].split(":");
        int hours = Integer.parseInt(time[0]);
        int minuets = Integer.parseInt(time[1]);
        return LocalDateTime.of(years,months,days,hours,minuets);
    }

    private List<Object> generalAck(short opcode){
        return Arrays.asList(new Object[]{(short)10, opcode});
    }

    private List<Object> ackFollowResponse(String UserName){
        List<Object> output = new ArrayList<>();
        output.add(((short)10));
        output.add(((short)4));
        output.add(UserName);
        output.add((byte)0);
        return output;
    }

    private List<Object> ackStatAndLogStat(int op, User user){
        List<Object> output = new ArrayList<>();
        output.add(((short)10));
        output.add(((short)op));
        output.add(user.getAge()); //<Age>
        output.add(dataBase.getNumPosts(user)); //<NumPosts>
        output.add(dataBase.getNumFollowersForUser(user)); //<NumFollowers>
        output.add(dataBase.getFollowingNumForUser(user)); //<NumFollowing>
        return output;
    }

    private List<Object> ackPostResponse(String username, String content){
        List<Object> output = new ArrayList<>();
        output.add(((short)9));
        output.add(((byte)1));
        output.add(username);
        output.add((byte)0);
        output.add(content);
        output.add((byte)0);
        return output;
    }

    private List<Object> ackPmResponse(String username, String filteredMessage){
        List<Object> output = new ArrayList<>();
        output.add(((short)9));
        output.add(((byte)0));
        output.add(username);
        output.add((byte)0);
        output.add(filteredMessage);
        output.add((byte)0);
        return output;
    }

    private List<Object> errorResponse(short messageOpcode){
        List<Object> output = new ArrayList<>();
        output.add(((short)11));
        output.add(messageOpcode);
        return output;
    }

    List<User> getTaggedUsers (String postContent){
        List<User> taggedList = new LinkedList<>();
        String[] words = postContent.split(" ");
        for (String str: words) {
            if (str.charAt(0) == '@') {
                String username = str.substring(1);
                User user = dataBase.getUserByName(username);
                if (user != null ) {
                    taggedList.add(user);
                }
            }
        }
        return taggedList;
    }

    List<User> checkIfTaggedUserIsFollower(List<User> taggedUsers, List<User> followingUsers){
        List<User> postUsers = new LinkedList<>();
        postUsers.addAll(followingUsers);
        for(User user : taggedUsers){
            if(!followingUsers.contains(user)){
                postUsers.add(user);
            }
        }
        return postUsers;
    }
}
