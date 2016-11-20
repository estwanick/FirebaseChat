package cs656.com.firebasemessengerapp.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.Chat;
import cs656.com.firebasemessengerapp.model.Conversation;
import cs656.com.firebasemessengerapp.model.Friend;
import cs656.com.firebasemessengerapp.model.User;
import cs656.com.firebasemessengerapp.utils.Constants;

/*
    This view will show a list of the users friends,
    the user can select the friends they want to start
    a new conversation with.
 */
public class ConversationActivity extends AppCompatActivity {
    private String TAG = "New Conversation";

    private ListView mListView;
    private Toolbar mToolBar;

    private FirebaseListAdapter mFriendListAdapter;
    private ValueEventListener mValueEventListener;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFriendDatabaseReference;
    private TextView mFriendsInChat;

    //Objects for Chat
    private Chat mChat;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_conversation);
        initializeScreen();
        mToolBar.setTitle("Select Participants");
        showFriendsList();
    }

    private void showFriendsList() {
        mFriendListAdapter = new FirebaseListAdapter<String>(this, String.class, R.layout.friend_item, mFriendDatabaseReference) {
            @Override
            protected void populateView(View view, final String friend, final int position) {
                Log.e("TAG", friend);
                final Friend addFriend = new Friend(friend);
                ((TextView) view.findViewById(R.id.messageTextView)).setText(friend);
                ((Button) view.findViewById(R.id.addFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + friend);

                        //TODO: Complete the creating of Chat object, then add to firebase
                        //Add friend to chat
                        if(mChat.appendFriend(addFriend)){
                            mFriendsInChat.setText( mFriendsInChat.getText() + ", " + friend);
                        }

                        Log.e(TAG, "Adding to chat: " + friend);
                    }
                });
                ((Button) view.findViewById(R.id.removeFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + friend);
                        //TODO: Add remove methods
                        mChat.removeFriend(addFriend); //the name add Friend here is not appropriate
                        Log.e(TAG, "Removing from chat: " + friend);
                    }
                });
            }
        };
        mListView.setAdapter(mFriendListAdapter);

        mValueEventListener = mFriendDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    finish();
                    return;
                }
                mFriendListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addToConversation(){

    }

    private void removeFromConversation(){

    }

    //TODO: Used in multiple places, should probably move to its own class
    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    private void initializeScreen() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Eventually this list will filter out users that are already your friend
        mFriendDatabaseReference = mFirebaseDatabase.getReference().child(Constants.FRIENDS_LOCATION
            + "/" + encodeEmail(mFirebaseAuth.getCurrentUser().getEmail()));

        mListView = (ListView) findViewById(R.id.conversationListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mFriendsInChat = (TextView) findViewById(R.id.friendsInChat);
        mChat = new Chat("chatuid","Chat Name");
    }
}