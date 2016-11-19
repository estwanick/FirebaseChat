package cs656.com.firebasemessengerapp.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
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

    private ListView mListView;
    private Toolbar mToolBar;

    private FirebaseListAdapter mFriendListAdapter;
    private ValueEventListener mValueEventListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends_activity);
        initializeScreen();

        mToolBar.setTitle("Find new friends");

        //Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users");

        mFriendListAdapter = new FirebaseListAdapter<User>(this, User.class, R.layout.friend_item, mUserDatabaseReference) {
            @Override
            protected void populateView(View view, User user, int position) {
                //Log.e("TAG", user.toString());
                String username = user.getUsername();
                ((TextView)view.findViewById(R.id.messageTextView)).setText(username);
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

    private void showAllUsers(){

    }

    private void initializeScreen(){
        mListView = (ListView) findViewById(R.id.friendsListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
    }

}
