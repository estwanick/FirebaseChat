package cs656.com.firebasemessengerapp.model;

import java.util.ArrayList;
import java.util.List;

public class Chat {

    private String uid;
    private String chatName;
    private List<Message> messages;
    private List<Friend> friends;

    public Chat(){

    }

    public Chat(String uid, String chatName){
        this.uid = uid;
        this.chatName = chatName;
        this.messages = new ArrayList<Message>();
        this.friends = new ArrayList<Friend>();
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

    public List<Friend> getFriends() {
        return friends;
    }

    public boolean appendFriend(Friend friend){
        Boolean contFriend = friends.contains(friend);
        if(!contFriend){
            friends.add(friend);
            return true;
        }
        return false;
    }

    public boolean removeFriend(Friend friend){
        friends.remove(friend);
        return true;
    }

}
