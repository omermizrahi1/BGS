package bgu.spl.net.srv;

import java.time.LocalDateTime;

public class Message {

    protected User sender;
    protected String post;
    protected LocalDateTime timeStamp;


    public Message(User sender, String post) {
        this.post = post;
        this.timeStamp = LocalDateTime.now();
        this.sender = sender;
    }

    public String getMessage() {
        return post;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public User getSender(){
        return sender;
    }

}
