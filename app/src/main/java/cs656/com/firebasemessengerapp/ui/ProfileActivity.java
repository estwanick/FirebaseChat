package cs656.com.firebasemessengerapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.utils.Constants;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton mphotoPickerButton;
    private static final int GALLERY_INTENT=2;
    private ProgressDialog mProgress;
    private StorageReference mStorage;
    private FirebaseAuth mFirebaseAuth;
    private String currentUserEmail;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        initializeScreen();
        openImageSelector();
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
                    //addImageToMessages(downloadURl);
                    mProgress.dismiss();
                }
            });
        }

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
            }
        });
    }

    private void initializeScreen(){
        mFirebaseAuth = FirebaseAuth.getInstance();
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
