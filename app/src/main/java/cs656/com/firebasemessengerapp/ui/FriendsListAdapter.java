package cs656.com.firebasemessengerapp.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.User;


public class FriendsListAdapter extends ArrayAdapter<User> {
    private User mUserList;

    public FriendsListAdapter(Context context, int resource){
        super(context, resource);
    }

}
