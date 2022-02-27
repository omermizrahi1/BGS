package bgu.spl.net.srv;

public class Post extends Message {

    public Post(User sender, String post) {
        super(sender, post);
    }

    public String getPost() {
        return post;
    }



}
