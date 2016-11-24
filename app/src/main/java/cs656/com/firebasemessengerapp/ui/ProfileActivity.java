package cs656.com.firebasemessengerapp.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import cs656.com.firebasemessengerapp.R;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        initializeScreen();
    }

    private void initializeScreen(){
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

}
