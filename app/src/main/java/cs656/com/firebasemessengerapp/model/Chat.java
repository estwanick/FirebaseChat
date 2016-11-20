package cs656.com.firebasemessengerapp.model;

import java.util.List;

public class Chat {

    private String uid;
    private String chatName;
    private List<Message> messages;

    public Chat(){

    }

    public Chat(String uid, String chatName){
        this.uid = uid;
        this.chatName = chatName;
    }

    public String getUid() {
        return uid;
    }

    public String getChatName() {
        return chatName;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
