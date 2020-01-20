package com.Ivan.fashionhair;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.Ivan.fashionhair.ListView.ClientAdapter;
import com.Ivan.fashionhair.Modal.Client;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ClientActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ArrayList<Client> arrayOfUsers;
    ListView listView;
    RelativeLayout listbackgroundchange;
    Button bookBtn, cancelBtn;
    Date currentTime;
    TextView displayDate;
    TextView displayTime;
    DatabaseReference mRef;
    DatabaseReference mNotificationRef;
    long unixTime;
    public static boolean isMe = false;

    @Override
    protected void onStart() {
        super.onStart();
//       mRef.removeValue();
       mRef.addValueEventListener(eventListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_user);
        displayDate = (TextView) findViewById(R.id.ClientDate);
        displayTime = (TextView) findViewById(R.id.ClientTime);
        bookBtn = findViewById(R.id.bookNow);
        cancelBtn = findViewById(R.id.cancelNow);
        mAuth = FirebaseAuth.getInstance();

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                tvClock.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
                currentTime = Calendar.getInstance().getTime();
                unixTime = System.currentTimeMillis() / 1000L;
                String Timeonly = (String) DateFormat.format("HH:mm:ss a", currentTime); // Thursday
                String dayOfTheWeek = (String) DateFormat.format("EEEE", currentTime); // Thursday
                String day          = (String) DateFormat.format("dd",   currentTime); // 20
                String monthString  = (String) DateFormat.format("MMM",  currentTime); // Jun
                String monthNumber  = (String) DateFormat.format("MM",   currentTime); // 06
                String year         = (String) DateFormat.format("yyyy", currentTime); // 2013
                displayDate.setText(year + "." + monthNumber + "." + day + " (" + dayOfTheWeek + ")");
                displayTime.setText(Timeonly+"");
                someHandler.postDelayed(this, 1000);
            }
        }, 10);





        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference().child("Clients");
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fUser.getUid();
        mNotificationRef = database.getReference().child("Clients").child(uid);

        listenNotification();

        listView = (ListView) findViewById(R.id.list);
        listbackgroundchange = (RelativeLayout) findViewById(R.id.listbackgroundchange);
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ClientActivity.this);
                alertDialogBuilder.setMessage("Are you sure cancellation?");
                        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String userId = user.getUid();
                                        final DatabaseReference mRef = database.getReference().child("Clients").child(userId);
                                        mRef.removeValue();
                                    }
                                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

    }

    private void listenNotification() {
        mNotificationRef.child("ready").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists())
                {
                    return;
                }
                boolean mImNext = (boolean)dataSnapshot.getValue();
                if (mImNext)
                {
                    sendNotification();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showAlert()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Are you sure you want to order?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        sendRequest();

                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot == null)
            {

                return;
            }


            arrayOfUsers = new ArrayList<Client>();


            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                if (!ds.hasChild("state"))
                {
                    continue;
                }

                Client user = ds.getValue(Client.class);

                    if (user == null)
                        continue;

                    if (user.uid == null)
                    {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(ClientActivity.this, MainActivity.class));
                    }

                    if (user.state.equals("null") || user.orderNo==0)
                        continue;

//                    if (ds.getKey().equals(uid))
//                        isMe = true;

                    arrayOfUsers.add(user);


            }

//            ArrayList<String> aaa = new ArrayList<String>;
//            Collections.sort(aaa);
//            Collections.reverse(arrayOfUsers);
            Collections.sort(arrayOfUsers);
            // Create the adapter to convert the array to views
            ClientAdapter adapter = new ClientAdapter(ClientActivity.this, arrayOfUsers);

            listView.setAdapter(adapter);
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    public void sendRequest()
    {
        //listbackgroundchange.setBackgroundColor(0xFF000000);

        // check DB if it is till available
        DatabaseReference myRef;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("isEnabled");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot == null)
//                    return;

                String isEnabled = (String)dataSnapshot.getValue();
                if (isEnabled == null || isEnabled.equals("1"))
                {
                    final String onlyTime = displayTime.getText().toString();
                    final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = fUser.getUid();

                    try {
                        String sharePreferenceKEY = "MyPref";
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(sharePreferenceKEY, 0);
                        String myName = pref.getString("myname", null);
                        if (myName != null)
                        {
                            Client mClient = new Client(uid, fUser.getPhoneNumber(), myName, onlyTime, "REQUESTED", unixTime, false);
                            mRef.child(uid).setValue(mClient);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(ClientActivity.this, "Barber is not available from now on", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void sendNotification()
    {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "notify_001");
        Intent ii = new Intent(this, ClientActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
//            bigText.bigText(verseurl);
        bigText.setBigContentTitle("Ivan says to you!");
        bigText.setSummaryText("You are next. Come to our BarberShop!");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Ivan says to you!");
        mBuilder.setContentText("You are next. Come to our BarberShop!");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setSound(Uri.parse("Notification.DEFAULT_SOUND"));
        mBuilder.setColor(Color.GREEN);
        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "notify_001";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRef.removeEventListener(eventListener);
    }
}
