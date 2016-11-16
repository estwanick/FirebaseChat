package cs656.com.firebasemessengerapp.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import cs656.com.firebasemessengerapp.R;
import cs656.com.firebasemessengerapp.model.User;

//Temporarily display the list of all users in the DB
public class ConversationAdapter extends ArrayAdapter<User> {

    public ConversationAdapter(Context context, int resource, List<User> objects){
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.conversation_item, parent, false);

            TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            User message = getItem(position);
            messageTextView.setText(message.getUsername());
        }
        return convertView;
    }
}
