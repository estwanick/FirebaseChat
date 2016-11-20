package cs656.com.firebasemessengerapp.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.User;
import cs656.com.firebasemessengerapp.utils.Constants;

public class FriendsListActivity extends AppCompatActivity {

    private String TAG = "Friends List Activity";

    private ListView mListView;
    private Toolbar mToolBar;

    private FirebaseListAdapter mFriendListAdapter;
    private ValueEventListener mValueEventListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mCurrentUsersFriends;
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends_activity);
        initializeScreen();

        mToolBar.setTitle("Find new friends");

        showUserList();
    }

    private void showUserList(){
        mFriendListAdapter = new FirebaseListAdapter<User>(this, User.class, R.layout.friend_item, mUserDatabaseReference) {
            @Override
            protected void populateView(View view, User user, final int position) {
                //Log.e("TAG", user.toString());
                final String username = user.getUsername();
                final String email = user.getEmail();
                ((TextView)view.findViewById(R.id.messageTextView)).setText(username);
                ((Button)view.findViewById(R.id.addFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + username);
                        //Add this user to your friends list, by email
                        addNewFriend(email); //change to send email
                    }
                });
                ((Button)view.findViewById(R.id.removeFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + username);
                        //Add this user to your friends list, by email
                        removeFriend(email); //change to send email
                    }
                });
            }
        };
        mListView.setAdapter(mFriendListAdapter);

        mValueEventListener = mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user == null){
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

    private void removeFriend(String friendEmail){
        //Get current user logged in by email
        final String userLoggedIn = mFirebaseAuth.getCurrentUser().getEmail();
        Log.e(TAG, "User logged in is: " + userLoggedIn);
        final DatabaseReference friendsRef = mFirebaseDatabase.getReference(Constants.FRIENDS_LOCATION
                + "/" + encodeEmail(userLoggedIn));
        friendsRef.child(encodeEmail(friendEmail)).removeValue();
    }

    private void addNewFriend(String newFriendEmail){
        //Get current user logged in by email
        final String userLoggedIn = mFirebaseAuth.getCurrentUser().getEmail();
        Log.e(TAG, "User logged in is: " + userLoggedIn);
        //final String newFriendEncodedEmail = encodeEmail(newFriendEmail);
        final DatabaseReference friendsRef = mFirebaseDatabase.getReference(Constants.FRIENDS_LOCATION
                + "/" + encodeEmail(userLoggedIn));
        //Add friends to current users friends list
        friendsRef.child(encodeEmail(newFriendEmail)).setValue(newFriendEmail);
    }

    //TODO: Used in multiple places, should probably move to its own class
    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    private void initializeScreen(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Eventually this list will filter out users that are already your friend
        mUserDatabaseReference = mFirebaseDatabase.getReference().child(Constants.USERS_LOCATION);

        mListView = (ListView) findViewById(R.id.friendsListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
    }
}
