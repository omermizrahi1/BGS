package bgu.spl.net.srv;

public class PM extends Message {


    public PM(User sender, String post ) {
        super(sender, post);
    }

    public String getPM() {
        return post;
    }



}