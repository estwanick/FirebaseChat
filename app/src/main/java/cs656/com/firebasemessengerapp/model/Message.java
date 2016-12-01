package cs656.com.firebasemessengerapp.model;

/**
 * Created by michaelestwanick on 11/20/16.
 */

public class Message {

    private String sender;
    private String message;
    private Boolean multimedia = false;
    private String contentType = "";
    private String contentLocation = "";
    private String timestamp = "";

    public Message(){

    }

    //Constructor for plain text message
    public Message(String sender, String message, String time){
        this.sender = sender;
        this.message = message;
        this.timestamp = time;
        this.multimedia = false;
    }

    //Constructor for Multimedia message
    public Message(String sender, String message, String contentType, String contentLocation, String time){
        this.sender = sender;
        this.message = message;
        this.multimedia = true;
        this.contentType = contentType;
        this.timestamp = time;
        this.contentLocation = contentLocation;
    }

    public String getSender() {
        return sender;
    }
    public String getTimestamp(){return timestamp;}
    public String getMessage() {
        return message;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public Boolean getMultimedia() {
        return multimedia;
    }

    public String getContentType() {
        return contentType;
    }
}
