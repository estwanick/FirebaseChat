package cs656.com.firebasemessengerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import cs656.com.firebasemessengerapp.model.User;
import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.ui.AddConversationDialogFragment;
import cs656.com.firebasemessengerapp.ui.ConversationActivity;
import cs656.com.firebasemessengerapp.ui.ConversationAdapter;
import cs656.com.firebasemessengerapp.ui.FriendsListActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConversationDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ListView mConversationListView;
    private ConversationAdapter mConversationAdapter;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mConversationDatabaseReference = mFirebaseDatabase.getReference().child("users");

        //Initialize screen variables
        mConversationListView = (ListView) findViewById(R.id.conversationListView);

        //Create & Set firebase UI list adapter
        List<User> userList = new ArrayList<>();
        mConversationAdapter = new ConversationAdapter(this, R.layout.conversation_item, userList);
        mConversationListView.setAdapter(mConversationAdapter);


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                    createUser(user);
                } else {
                    // User is signed out
                    //onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }

    public void createNewConversation(View view){
        Intent intent = new Intent(this, ConversationActivity.class);
        startActivity(intent);
    }

    //Not being used
    public void showAddConversationActicity(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddConversationDialogFragment.newInstance();
        dialog.show(MainActivity.this.getFragmentManager(), "AddConversationDialogFragment");
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //Sending users for testing
                    User singleUser = dataSnapshot.getValue(User.class); //Eventually switch to conversation class
                    mConversationAdapter.add(singleUser); //Eventually switch to conversation class
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mConversationDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        detachDatabaseReadListener();
    }


    private void detachDatabaseReadListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (resultCode == RESULT_OK) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sign_out) {
            AuthUI.getInstance()
                    .signOut(this);
        }
        if (id == R.id.listFriends) {
            //Open up activity where a user can add and view friends
            ViewFriendsList();
        }

        return true;
    }

    private void ViewFriendsList(){
        Intent intent = new Intent(this, FriendsListActivity.class);
        startActivity(intent);
    }

    private void createUser(FirebaseUser user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference usersRef = database.getReference("users");
        final String encodedEmail = encodeEmail(user.getEmail());
        final DatabaseReference userRef = usersRef.child(encodedEmail);
        final String username = user.getDisplayName();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    User newUser = new User(username, encodeEmail(encodedEmail));
                    userRef.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    //TODO: Used in multiple places, should probably move to its own class
    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

}
