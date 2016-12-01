package cs656.com.firebasemessengerapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.Message;
import cs656.com.firebasemessengerapp.model.User;
import cs656.com.firebasemessengerapp.utils.Constants;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton mphotoPickerButton;
    private static final int GALLERY_INTENT=2;
    private ProgressDialog mProgress;
    private StorageReference mStorage;
    private FirebaseAuth mFirebaseAuth;
    private String currentUserEmail;
    private ImageView profileImage;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCurrentUserDatabaseReference;
    private Context mView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        initializeScreen();
        openImageSelector();
        initializeUserInfo();
        mView = ProfileActivity.this;
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
            final String imageLocation = "Photos/profile_picture/" + currentUserEmail;
            final String imageLocationId = imageLocation + "/" + uri.getLastPathSegment();
            final String uniqueId = UUID.randomUUID().toString();
            final StorageReference filepath = mStorage.child(imageLocation).child(uniqueId + "/profile_pic");
            final String downloadURl = filepath.getPath();
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //create a new message containing this image
                    addImageToProfile(downloadURl);
                    mProgress.dismiss();
                }
            });
        }

    }

    public void addImageToProfile(final String imageLocation){
        final ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
        mCurrentUserDatabaseReference
                .child("profilePicLocation").setValue(imageLocation).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        StorageReference storageRef = FirebaseStorage.getInstance()
                                .getReference().child(imageLocation);
                        Glide.with(mView)
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(imageView);
                    }
                }
        );

    }

    public void openImageSelector(){

        mphotoPickerButton = (ImageButton) findViewById(R.id.imageButton);
        mProgress = new ProgressDialog(this);
        mphotoPickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
                //mView = view;
            }
        });
    }

    private void initializeUserInfo(){
        final ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
        mCurrentUserDatabaseReference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        StorageReference storageRef = FirebaseStorage.getInstance()
                                .getReference().child(user.getProfilePicLocation());
                        Glide.with(mView)
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(imageView);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initializeScreen(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCurrentUserDatabaseReference = mFirebaseDatabase
                .getReference().child(Constants.USERS_LOCATION
                        + "/" +encodeEmail(mFirebaseAuth.getCurrentUser().getEmail()));
        currentUserEmail = encodeEmail(mFirebaseAuth.getCurrentUser().getEmail());
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitle("Profile");
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
