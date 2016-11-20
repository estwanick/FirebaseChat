package cs656.com.firebasemessengerapp.model;

/**
 * Created by michaelestwanick on 11/20/16.
 */

public class Message {

    private String sender;
    private String message;

    public Message(){

    }

    public Message(String sender, String message){
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
