package cs656.com.firebasemessengerapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.Friend;
import cs656.com.firebasemessengerapp.model.Message;
import cs656.com.firebasemessengerapp.utils.Constants;

public class ChatMessagesActivity extends AppCompatActivity {

    private String messageId;
    private String chatName;
    private ListView mMessageList;
    private Toolbar mToolBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    private FirebaseListAdapter<Message> mMessageListAdapter;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_activity);
        Intent intent = this.getIntent();
        messageId = intent.getStringExtra(Constants.MESSAGE_ID);
        chatName = intent.getStringExtra(Constants.CHAT_NAME);

        if(messageId == null){
            finish();
            return;
        }

        initializeScreen();
        mToolBar.setTitle(chatName);
        showMessages();
    }

    private void initializeScreen() {
        mMessageList = (ListView) findViewById(R.id.messageListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mMessageDatabaseReference = mFirebaseDatabase.getReference().child(Constants.MESSAGE_LOCATION
                + "/" + messageId);
    }

    private void showMessages() {
        mMessageListAdapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.message_item, mMessageDatabaseReference) {
            @Override
            protected void populateView(View view, Message message, final int position) {
                ((TextView) view.findViewById(R.id.messageTextView)).setText(message.getMessage());
                ((TextView) view.findViewById(R.id.senderTextView)).setText(message.getSender());
            }
        };
        mMessageList.setAdapter(mMessageListAdapter);
    }

}
