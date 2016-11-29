package cs656.com.firebasemessengerapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.Message;
import cs656.com.firebasemessengerapp.model.User;
import cs656.com.firebasemessengerapp.utils.Constants;

public class ChatMessagesActivity extends AppCompatActivity {

    private String messageId;
    private TextView mMessageField;
    private Button mSendButton;
    private String chatName;
    private ListView mMessageList;
    private Toolbar mToolBar;
    private String currentUserEmail;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    private FirebaseListAdapter<Message> mMessageListAdapter;
    private FirebaseAuth mFirebaseAuth;

    private ImageButton mphotoPickerButton;
    private static final int GALLERY_INTENT=2;
    private StorageReference mStorage;
    private ProgressDialog mProgress;

    private ImageButton mrecordVoiceButton;
    private TextView mRecordLable;

    private MediaRecorder mRecorder;
    private String mFileName = null;

    private static final String LOG_TAG = "Record_log";
    private ValueEventListener mValueEventListener;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_activity);


        Intent intent = this.getIntent();
        //MessageID is the location of the messages for this specific chat
        messageId = intent.getStringExtra(Constants.MESSAGE_ID);
        chatName = intent.getStringExtra(Constants.CHAT_NAME);

        if(messageId == null){
            finish(); // replace this.. nav user back to home
            return;
        }



        initializeScreen();
        mToolBar.setTitle(chatName);
        showMessages();
        addListeners();
        openImageSelector();
        openVoiceRecorder();

    }

    //Add listener for on completion of image selection
    public void openImageSelector(){
        mphotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mProgress = new ProgressDialog(this);
        mphotoPickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data){

        mStorage = FirebaseStorage.getInstance().getReference(); //make global
        super.onActivityResult(requestCode, requestCode, data);

        if(requestCode ==GALLERY_INTENT && resultCode == RESULT_OK){

            mProgress.setMessage("Uploading...");
            mProgress.show();

            Uri uri = data.getData();
            //Keep all images for a specific chat grouped together
            final String imageLocation = "Photos" + "/" + messageId;
            final String imageLocationId = imageLocation + "/" + uri.getLastPathSegment();
            final String uniqueId = UUID.randomUUID().toString();
            final StorageReference filepath = mStorage.child(imageLocation).child(uniqueId + "/image_message");
            final String downloadURl = filepath.getPath();
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //create a new message containing this image
                    addImageToMessages(downloadURl);
                    mProgress.dismiss();
                }
            });
        }

    }

    public void openVoiceRecorder(){
        //Implement voice selection
        mrecordVoiceButton =(ImageButton) findViewById(R.id.recordVoiceButton);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/recorded_audio.3gp";

        mrecordVoiceButton.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    startRecording();

            //        mRecordLable.setText("Recording started...");
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP){

                    stopRecording();

             //       mRecordLable.setText("Recording stopped...");
                }
                return false;
            }
        });

        //on complete: sendVoice()
    }

    private void startRecording() {

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        uploadAudio();
    }

    private void uploadAudio() {

        mStorage = FirebaseStorage.getInstance().getReference();

        mProgress.setMessage("Uploading Audio...");
        mProgress.show();

        Uri uri = Uri.fromFile(new File(mFileName));
        //Keep all voice for a specific chat grouped together
        final String voiceLocation = "Voice" + "/" + messageId;
        final String voiceLocationId = voiceLocation + "/" + uri.getLastPathSegment();
        final String uniqueId = UUID.randomUUID().toString();
        final StorageReference filepath = mStorage.child(voiceLocation).child(uniqueId + "/audio_message.3gp");
        final String downloadURl = filepath.getPath();

        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                addVoiceToMessages(downloadURl);
                mProgress.dismiss();
            }
        });
    }

    public void addListeners(){
        mMessageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //If voice message add them to Firebase.Storage
    public void addVoiceToMessages(String voiceLocation){
        final DatabaseReference pushRef = mMessageDatabaseReference.push();
        final String pushKey = pushRef.getKey();

        //Create message object with text/voice etc
        Message message =
                new Message(encodeEmail(mFirebaseAuth.getCurrentUser().getEmail()),
                        "Message: Voice Sent", "VOICE", voiceLocation);
        //Create HashMap for Pushing
        HashMap<String, Object> messageItemMap = new HashMap<String, Object>();
        HashMap<String,Object> messageObj = (HashMap<String, Object>) new ObjectMapper()
                .convertValue(message, Map.class);
        messageItemMap.put("/" + pushKey, messageObj);
        mMessageDatabaseReference.updateChildren(messageItemMap)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mMessageField.setText("");
                    }
                });
    }


    //Send image messages from here
    public void addImageToMessages(String imageLocation){
        final DatabaseReference pushRef = mMessageDatabaseReference.push();
        final String pushKey = pushRef.getKey();

        //Create message object with text/voice etc
        Message message =
                new Message(encodeEmail(mFirebaseAuth.getCurrentUser().getEmail()),
                        "Message: Image Sent", "IMAGE", imageLocation);
        //Create HashMap for Pushing
        HashMap<String, Object> messageItemMap = new HashMap<String, Object>();
        HashMap<String,Object> messageObj = (HashMap<String, Object>) new ObjectMapper()
                .convertValue(message, Map.class);
        messageItemMap.put("/" + pushKey, messageObj);
        mMessageDatabaseReference.updateChildren(messageItemMap)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mMessageField.setText("");
                    }
                });
    }



    public void sendMessage(View view){
        //final DatabaseReference messageRef = mFirebaseDatabase.getReference(Constants.MESSAGE_LOCATION);
        final DatabaseReference pushRef = mMessageDatabaseReference.push();
        final String pushKey = pushRef.getKey();

        String messageString = mMessageField.getText().toString();
        //Create message object with text/voice etc
        Message message = new Message(encodeEmail(mFirebaseAuth.getCurrentUser().getEmail()), messageString);
        //Create HashMap for Pushing
        HashMap<String, Object> messageItemMap = new HashMap<String, Object>();
        HashMap<String,Object> messageObj = (HashMap<String, Object>) new ObjectMapper()
                .convertValue(message, Map.class);
        messageItemMap.put("/" + pushKey, messageObj);
        mMessageDatabaseReference.updateChildren(messageItemMap)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mMessageField.setText("");
                    }
                });
    }

    private void showMessages() {
        mMessageListAdapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.message_item, mMessageDatabaseReference) {
            @Override
            protected void populateView(View view, Message message, final int position) {
                LinearLayout messageLine = (LinearLayout) view.findViewById(R.id.messageLine);
                TextView messgaeText = (TextView) view.findViewById(R.id.messageTextView);
                TextView senderText = (TextView) view.findViewById(R.id.senderTextView);

                messgaeText.setText(message.getMessage());
                senderText.setText(message.getSender());
                //If you sent this message, right align
                String mSender = message.getSender();

                if(mSender.equals(currentUserEmail)){
                    messgaeText.setGravity(Gravity.RIGHT);
                    senderText.setGravity(Gravity.RIGHT);
                    messageLine.setGravity(Gravity.RIGHT);
                    //messgaeText.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    //       R.color.colorAccent, null));
                }else{
                    messgaeText.setGravity(Gravity.LEFT);
                    senderText.setGravity(Gravity.LEFT);
                    messageLine.setGravity(Gravity.LEFT);
                    //messgaeText.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    //       R.color.colorPrimary, null));
                }

                //If this is multimedia display it
                final ImageView imageView = (ImageView) view.findViewById(R.id.imageMessage);
                if(message.getMultimedia()){
                    if(message.getContentType().equals("IMAGE")) {
                        StorageReference storageRef = FirebaseStorage.getInstance()
                                .getReference().child(message.getContentLocation());
                        imageView.setVisibility(View.VISIBLE);
                        //storageRef.getDownloadUrl().addOnCompleteListener(new O)
                        Glide.with(view.getContext())
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(imageView);
                    }else{
                        imageView.setVisibility(View.GONE);
                        imageView.setImageDrawable(null);
                    }
                }else{
                    imageView.setVisibility(View.GONE);
                    imageView.setImageDrawable(null);
                }
            }
        };
        mMessageList.setAdapter(mMessageListAdapter);
    }

    private void initializeScreen() {
        mMessageList = (ListView) findViewById(R.id.messageListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mMessageField = (TextView)findViewById(R.id.messageToSend);
        mSendButton = (Button)findViewById(R.id.sendButton);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserEmail = encodeEmail(mFirebaseAuth.getCurrentUser().getEmail());
        mMessageDatabaseReference = mFirebaseDatabase.getReference().child(Constants.MESSAGE_LOCATION
                + "/" + messageId);

        mToolBar.setTitle(chatName);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //TODO: Used in multiple places, should probably move to its own class
    public String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

}