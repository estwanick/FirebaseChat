package cs656.com.firebasemessengerapp.model;


//Conversation object
public class Conversation {
    private String conversationName;

    public Conversation(){

    }

    public Conversation(String conversationName){
        this.conversationName = conversationName;
    }

    public String getConversationName() {
        return conversationName;
    }
}
