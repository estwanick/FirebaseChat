package cs656.com.firebasemessengerapp.model;

/**
 * Created by michaelestwanick on 11/20/16.
 */

public class Message {

    private String sender;
    private String message;
    private Boolean isMultimedia;
    private String contentType;
    private String contentLocation;

    public Message(){

    }

    //Constructor for plain text message
    public Message(String sender, String message){
        this.sender = sender;
        this.message = message;
        this.isMultimedia = false;
    }

    //Constructor for Multimedia message
    public Message(String sender, String message, String contentType, String contentLocation){
        this.sender = sender;
        this.message = message;
        this.isMultimedia = true;
        this.contentType = contentType;
        this.contentLocation = contentLocation;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public Boolean getMultimedia() {
        return isMultimedia;
    }

    public String getContentType() {
        return contentType;
    }
}
