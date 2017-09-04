package cs656.com.firebasemessengerapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.Chat;
import cs656.com.firebasemessengerapp.model.Friend;
import cs656.com.firebasemessengerapp.model.Message;
import cs656.com.firebasemessengerapp.model.User;
import cs656.com.firebasemessengerapp.utils.Constants;
import cs656.com.firebasemessengerapp.utils.EmailEncoding;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/*
    This view will show a list of the users friends,
    the user can select the friends they want to start
    a new conversation with.
 */
public class ChatActivity extends AppCompatActivity {
    private String TAG = "New Conversation";

    private ListView mListView;
    private Toolbar mToolBar;

    private FirebaseListAdapter mFriendListAdapter;
    private ValueEventListener mValueEventListener;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFriendsLocationDatabaseReference;
    private DatabaseReference mCurrentUserDatabaseReference;
    private DatabaseReference mFriendDatabaseReference;
    private TextView mFriendsInChat;
    private EditText mChatName;

    //Objects for Chat
    private Chat mChat;
    private DatabaseReference mUserDatabaseRef;
    private ImageButton mCreateButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_chat);
        initializeScreen();
        showFriendsList();
        addListeners();
    }

    private void addListeners(){
        mChatName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mCreateButton.setEnabled(true);
                } else {
                    mCreateButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void showFriendsList() {
        //TODO: This list should not show your own userid..
        mFriendListAdapter = new FirebaseListAdapter<String>(this, String.class, R.layout.friend_item, mFriendsLocationDatabaseReference) {
            @Override
            protected void populateView(final View view, final String friend, final int position) {
                Log.e("TAG", friend);
                final Friend addFriend = new Friend(friend);
                ((TextView) view.findViewById(R.id.nameTextView)).setText(EmailEncoding.commaDecodePeriod(friend));

                mUserDatabaseRef.child(friend).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User fUser = dataSnapshot.getValue(User.class);
                        if(fUser != null){
                            ((TextView) view.findViewById(R.id.messageTextView))
                                    .setText(EmailEncoding.commaDecodePeriod(fUser.getUsername()));
                            if(fUser.getProfilePicLocation() != null && fUser.getProfilePicLocation().length() > 0){
                                try{
                                    StorageReference storageRef = FirebaseStorage.getInstance()
                                            .getReference().child(fUser.getProfilePicLocation());
                                    Glide.with(view.getContext())
                                            .using(new FirebaseImageLoader())
                                            .load(storageRef)
                                            .bitmapTransform(new CropCircleTransformation(view.getContext()))
                                            .into((ImageView)view.findViewById(R.id.photoImageView));
                                }catch(Exception e){
                                    Log.e("Err", e.toString());
                                }
                            }
                        }else{
                            ((TextView) view.findViewById(R.id.messageTextView))
                                    .setText("A girl has no name");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //Hide remove button by default, we have to do this because we reuse the view
                if(mChat.getFriends().isEmpty()){
                    view.findViewById(R.id.removeFriend).setVisibility(View.GONE);
                }
                //view.findViewById(R.id.removeFriend).setVisibility(View.GONE);
                (view.findViewById(R.id.addFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + friend);

                        //TODO: Complete the creating of Chat object, then add to firebase
                        //Add friend to chat
                        if(mChat.appendFriend(addFriend)){
                            String friendsString = "";
                            for(Friend f: mChat.getFriends()){
                                friendsString += EmailEncoding.commaDecodePeriod(f.getEmail()) + ", ";
                            }
                            friendsString = friendsString.substring(0, friendsString.length() - 2);
                            mFriendsInChat.setText("Users added to chat: " + friendsString);
                        }
                        view.findViewById(R.id.removeFriend).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.addFriend).setVisibility(View.GONE);
                        Log.e(TAG, "Adding to chat: " + friend);
                    }
                });
                (view.findViewById(R.id.removeFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + friend);
                        //TODO: Add remove methods
                        mChat.removeFriend(addFriend); //the name add Friend here is not appropriate
                        String friendsString = "";
                        for(Friend f: mChat.getFriends()){
                            friendsString += f.getEmail() + ", ";
                        }
                        if(friendsString.length()>1) {
                            friendsString = friendsString.substring(0, friendsString.length() - 2);

                            mFriendsInChat.setText("Users added to chat: " + EmailEncoding.commaDecodePeriod(friendsString));
                        }else{
                            mFriendsInChat.setText("Users added to chat: ");
                        }
                        view.findViewById(R.id.addFriend).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.removeFriend).setVisibility(View.GONE);
                        Log.e(TAG, "Removing from chat: " + friend);
                    }
                });
            }
        };
        mListView.setAdapter(mFriendListAdapter);

        mValueEventListener = mFriendsLocationDatabaseReference.addValueEventListener(new ValueEventListener() {
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

    //TODO: Add create new Chat function
    public void createChat(View view){
        //final String userLoggedIn = mFirebaseAuth.getCurrentUser().getEmail();
        //Log.e(TAG, "User logged in is: " + userLoggedIn);
        // final String newFriendEncodedEmail = EmailEncoding.commaEncodePeriod(newFriendEmail);
        final DatabaseReference chatRef = mFirebaseDatabase.getReference(Constants.CHAT_LOCATION);
        final DatabaseReference messageRef = mFirebaseDatabase.getReference(Constants.MESSAGE_LOCATION);
        final DatabaseReference pushRef = chatRef.push();
        final String pushKey = pushRef.getKey();
        mChat.setUid(pushKey);
        mChat.setChatName(mChatName.getText().toString());
        Log.e(TAG, "Push key is: " + pushKey);

        //Create HashMap for Pushing Conv
        HashMap<String, Object> chatItemMap = new HashMap<String, Object>();
        HashMap<String,Object> chatObj = (HashMap<String, Object>) new ObjectMapper()
                .convertValue(mChat, Map.class);
        chatItemMap.put("/" + pushKey, chatObj);
        chatRef.updateChildren(chatItemMap);

        //Create corresponding message location for this chat
        String initialMessage = mFriendsInChat.getText().toString();
        Message initialMessages =
                new Message("System", initialMessage, "");
        final DatabaseReference initMsgRef =
                mFirebaseDatabase.getReference(Constants.MESSAGE_LOCATION + "/" + pushKey);
        final DatabaseReference msgPush = initMsgRef.push();
        final String msgPushKey = msgPush.getKey();
        initMsgRef.child(msgPushKey).setValue(initialMessages);

        //Must add chat reference under every user object. Chat/User/Chats[chat1, chat2 ..]
        //Add to current users chat object
        //TODO: OPTIMIZATION!! decide how we will solve data replication issue, we could just send chat id
        // but this would require more complex queries on other pages
        chatItemMap = new HashMap<String, Object>();
        chatItemMap.put("/chats/" + pushKey, chatObj); //repushes chat obj -- Not space efficient
        mCurrentUserDatabaseReference.updateChildren(chatItemMap); //Adds Chatkey to users chats

        //Push chat to all friends
        for(Friend f: mChat.getFriends()){
            mFriendDatabaseReference = mFirebaseDatabase.getReference().child(Constants.USERS_LOCATION
                    + "/" + EmailEncoding.commaEncodePeriod(f.getEmail()));
            chatItemMap = new HashMap<String, Object>();
            chatItemMap.put("/chats/" + pushKey, chatObj);
            mFriendDatabaseReference.updateChildren(chatItemMap);
            mFriendDatabaseReference = null;
        }

        Intent intent = new Intent(view.getContext(), ChatMessagesActivity.class);
        String messageKey = pushKey;
        intent.putExtra(Constants.MESSAGE_ID, messageKey);
        intent.putExtra(Constants.CHAT_NAME, mChat.getChatName());
        startActivity(intent);
    }

    private void initializeScreen() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserDatabaseRef = mFirebaseDatabase.getReference().child(Constants.USERS_LOCATION);
        mCurrentUserDatabaseReference = mFirebaseDatabase.getReference().child(Constants.USERS_LOCATION
                + "/" + EmailEncoding.commaEncodePeriod(mFirebaseAuth.getCurrentUser().getEmail()));
        //Eventually this list will filter out users that are already your friend
        mFriendsLocationDatabaseReference = mFirebaseDatabase.getReference().child(Constants.FRIENDS_LOCATION
            + "/" + EmailEncoding.commaEncodePeriod(mFirebaseAuth.getCurrentUser().getEmail()));

        mListView = (ListView) findViewById(R.id.conversationListView);
        //mToolBar = (Toolbar) findViewById(R.id.toolbar);

        mListView = (ListView) findViewById(R.id.conversationListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitle("Create new chat");

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCreateButton = (ImageButton) findViewById(R.id.createButton);

        mFriendsInChat = (TextView) findViewById(R.id.friendsInChat);
        mChatName = (EditText) findViewById(R.id.chat_name);
        mChat = new Chat("","");
    }
}