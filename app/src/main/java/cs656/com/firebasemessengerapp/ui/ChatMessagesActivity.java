package cs656.com.firebasemessengerapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.utils.Constants;

public class ChatMessagesActivity extends AppCompatActivity {

    private String messageId;
    private String chatName;
    private ListView mMessageList;
    private Toolbar mToolBar;

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
    }

    private void showMessages() {

    }

}
