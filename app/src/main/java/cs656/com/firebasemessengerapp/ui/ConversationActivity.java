package cs656.com.firebasemessengerapp.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.google.firebase.database.FirebaseDatabase;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.utils.Constants;

/*
    This view will show a list of the users friends,
    the user can select the friends they want to start
    a new conversation with.
 */
public class ConversationActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_conversation);
        initializeScreen();

        mToolBar.setTitle("New Conversation");
    }

    private void initializeScreen(){
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void showFriends(){

    }

}