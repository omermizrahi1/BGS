package bgu.spl.net.srv;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataBase {

    private List<User> registeredUserList = new Vector<>();
    private ConcurrentHashMap<User, List<User>> blockingUsers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<User>> followersList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Post>> userPostList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<PM>> userPMList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Message>> pendingNotifications  = new ConcurrentHashMap<>();
    private final List<String>  filteredWords = Arrays.asList(new String[]{"war","spl"});
    private static DataBase instance = null;


    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    /*General*/
    public User getUserByName(String name) {
        synchronized (registeredUserList){
            for (User user : registeredUserList) {
                if (user.getUsername().equals(name)) {
                    return user;
                }
            }
            return null;
        }
    }

    /*REGISTER*/
    public boolean registerUser (String username, String password, String date){
        if (checkIfUsernameAlreadyExists(username)){
            return false;
        }else {
            User user = new User(username, password, date);
            registeredUserList.add(user);
            blockingUsers.put(user,new LinkedList<>());
            followersList.put(user,new LinkedList<>());
            userPostList.put(user,new LinkedList<>());
            userPMList.put(user,new LinkedList<>());
            pendingNotifications.put(user,new LinkedList<>());
            return true;
        }
    }

    private boolean checkIfUsernameAlreadyExists(String username){
        synchronized (registeredUserList){
            for (User user : registeredUserList) {
                if (user.getUsername().equals(username)) {
                    return true;
                }
            }
            return false;
        }
    }

    private User checkIfUserRegistered(String username, String password){
        synchronized (registeredUserList){
            for (User user : registeredUserList){
                if(user.getUsername().equals(username)){
                    if(user.getPassword().equals(password)){
                        return user;
                    }
                }
            }
            return null;
        }
    }

    /*LOGIN*/
    public User verifyUserDetails(String username, String password , boolean captcha){
        if(captcha) {
            User user = checkIfUserRegistered(username, password);
            if (user != null) {
                if (!user.isLogged()) {
                    return user;
                }
            }
        }
        return null;
    }

    public List<Message> getPendingNotifications(User user){
        synchronized (pendingNotifications.get(user)){
            return pendingNotifications.get(user);
        } 
    }

    /*FOLLOW*/
    public boolean checkIfUserFollowerOfOtherUser(User user, User other){
        if(followersList.containsKey(other)){
            synchronized (followersList.get(other)){
                if (followersList.get(other).contains(user)){
                    return true;
                }
            }
        }
        return false;
    }

    public void follow(User user, User userToFollow){
        synchronized (followersList.get(userToFollow)){
            this.followersList.get(userToFollow).add(user);
        }
    }

    public void unfollow(User user, User userToUnfollow){
        synchronized (followersList.get(userToUnfollow)){
            if(this.followersList.get(userToUnfollow).contains(user)){
                this.followersList.get(userToUnfollow).remove(user);
            }
        }
    }

    /*LOGSTAT && STAT*/
    public List<User> getRegisteredUserList() {
        synchronized (registeredUserList){
            return registeredUserList;
        }
    }

    public short getNumPosts(User user) {
        synchronized (userPostList.get(user)) {
            return (short) userPostList.get(user).size();
        }
    }

    public short getNumFollowersForUser(User user){
        synchronized (followersList.get(user)){
            return (short)followersList.get(user).size();
        }
    }

    public short getFollowingNumForUser(User user){
        synchronized (followersList){
            final int[] output = {0};
            followersList.forEach((k, v) -> {
                for (User user1 : v) {
                    if(user.equals(user1)){
                        output[0] = output[0] + 1;
                    }
                }
            });
            return (short)output[0];
        }
    }

    public List<User> getFollowers (User user) {
        synchronized (followersList.get(user)){
            return followersList.get(user);
        }
    }

    /*Post*/
    public void addPost(User user, Post post){
        synchronized (userPostList.get(user)){
            this.userPostList.get(user).add(post);
        }
    }

    public void removeBlockedUsers(User user, List<User> taggedUsers){
        synchronized (blockingUsers.get(user)){
            for (User u : taggedUsers){
                if (blockingUsers.get(user).contains(u)){
                    taggedUsers.remove(u);
                }
            }
        }
    }

    public void addMessageToPendingNotifications (List<User> notLoggedUsers, Message message){
        synchronized (pendingNotifications){
            for (User user: notLoggedUsers){
                pendingNotifications.get(user).add(message);
            }
        }
    }

    public void addMessageToPendingNotifications (User notLoggedUser, Message message){
        synchronized (pendingNotifications.get(notLoggedUser)) {
            pendingNotifications.get(notLoggedUser).add(message);
        }
    }

    /*Pm*/
    public boolean canCommunicate(User sender, User sendTo){

        synchronized (blockingUsers.get(sender)) {
            if (blockingUsers.get(sender).contains(sendTo)) {
                return false;
            }
        }

        synchronized (blockingUsers.get(sendTo)) {
            if (blockingUsers.get(sendTo).contains(sender)) {
                return false;
            }
        }

        return true;
    }

    public String filter (String pm){
        String [] words = pm.split(" ");
        String filteredPM = "";
        for (String word : words){
            if (filteredWords.contains(word)){
                word = "<filtered>";
            }
            filteredPM += " " + word;
        }
        return filteredPM.substring(1);
    }

    public void addPM(User user, PM pm){
        synchronized (userPMList.get(user)){
            if (!this.userPMList.containsKey(user)){
                this.userPMList.put(user,new ArrayList<>());
            }
            this.userPMList.get(user).add(pm);
        }
    }

    /*Block*/
    public boolean block (User blockingUser, User blockedUser){
        synchronized (blockingUsers.get(blockingUser)){
            if (blockingUsers.get(blockingUser).contains(blockedUser)){
                return false;
            }
            blockingUsers.get(blockingUser).add(blockedUser);
            return true;
        }
    }

}