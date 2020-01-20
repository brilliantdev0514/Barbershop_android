package com.Ivan.fashionhair.ListView;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.Ivan.fashionhair.Modal.Client;
import com.Ivan.fashionhair.R;
import com.Ivan.fashionhair.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ClientAdapter extends ArrayAdapter<Client> {

    DatabaseReference mRef;
    SharedPreferences pref;
    String sharePreferenceKEY = "MyPref";
    String sharedPrefKey = "MY_ROLE";
    String storedPreferenceString = "";
    int colorID = Color.YELLOW;
    ArrayList<Client> users;
    Context context;

    public ClientAdapter(Context context, ArrayList<Client> users) {
        super(context, 0, users);
        pref = context.getSharedPreferences(sharePreferenceKEY, 0); // 0 - for private mode

        storedPreferenceString = pref.getString(sharedPrefKey, "null");// it can be ADMIN or CLIENT

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference().child("Clients");
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Client user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_row, parent, false);
        }
        // Lookup view for data population


        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.list_image);
        TextView tvName = (TextView) convertView.findViewById(R.id.name);
        TextView tvTime = (TextView) convertView.findViewById(R.id.request_time);
        final TextView tvState = (TextView) convertView.findViewById(R.id.state);
        TextView phone = (TextView) convertView.findViewById(R.id.phone);
        TextView uid = convertView.findViewById(R.id.uid);
        // Populate the data into the template view using the data object
        tvName.setText(user.userName);
        tvTime.setText(user.requestTime);
        tvState.setText(user.state);
        phone.setText(user.phoneNumber);
        uid.setText(user.uid);

        tvState.setTextColor(setColor(user.state));

        if (storedPreferenceString.equals(SplashActivity.adminPwd))
        {
            tvState.setEnabled(true);
            tvState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvState.setText(changedString(tvState.getText().toString(), user.uid, position));
                }
            });
        } else
        {
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUID = fUser.getUid();
            if (user.uid.equals(currentUID))
            {
                convertView.setBackgroundColor(Color.DKGRAY);
            } else
            {
                convertView.setBackground(ContextCompat.getDrawable(context, R.drawable.list_selector));
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }

    public int setColor(String state)
    {
        int defaultColorID = Color.YELLOW;
        switch (state)
        {
            case "REQUESTED":
                defaultColorID = Color.YELLOW;
                break;
            case "STARTED":
                defaultColorID = Color.RED;
                break;
            case "COMPLETED":
                defaultColorID = Color.GREEN;
                break;
        }

        return defaultColorID;
    }

    public String changedString(String currentState, String uid, int position)
    {
        String changedValue = "COMPLETED";
        switch (currentState)
        {
            case "REQUESTED":
                changedValue = "STARTED";
                // update db: state - started
                changeStateAndSendNotificationToNext("STARTED", uid, position);
                colorID = Color.RED;
                break;
            case "STARTED":
                changedValue = "COMPLETED";
                changeStateTo("COMPLETED", uid);
                colorID = Color.GREEN;
                break;
        }

        return changedValue;
    }

    public void changeStateAndSendNotificationToNext(String targetString, String uid, int position)
    {
        try {
            mRef.child(uid).child("state").setValue(targetString);
            if (position < this.users.size() - 1)
            {
                // send notification to next user to inform Ready Cutting Hair
                // uid of next user
                String nextUID = this.users.get(position+1).uid;
                // set "ready" -> "true" for this user
                mRef.child(nextUID).child("ready").setValue(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeStateTo(String targetString, String uid)
    {


        try {
            mRef.child(uid).child("state").setValue(targetString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
