package cs656.com.firebasemessengerapp.model;

/**
 * Created by michaelestwanick on 11/20/16.
 */

public class Message {

    private String sender;
    private String message;
    private Boolean isMultimedia;
    private String contentType;

    public Message(){

    }

    public Message(String sender, String message, Boolean isMultimedia, String contentType){
        this.sender = sender;
        this.message = message;
        this.isMultimedia = isMultimedia;
        this.contentType = contentType;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getMultimedia() {
        return isMultimedia;
    }

    public String getContentType() {
        return contentType;
    }
}
