package com.Ivan.fashionhair;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SplashActivity extends AppCompatActivity {
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    SharedPreferences pref;
    String sharePreferenceKEY = "MyPref";
    String sharedPrefKey = "MY_ROLE";
    String storedPreferenceString = "";
    public static String adminPwd = "2580";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                // check if admin or client

//                sendNotification();
                checkFistTimeAndRole();

            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    public void displayCustomDialogWithEditText() {


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(SplashActivity.this);
        edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edittext.setGravity(Gravity.CENTER);
        alert.setTitle("Are you an Admin?"); // enter admin key
        alert.setMessage("Insert Password of Admin");
        alert.setView(edittext);

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String enteredAdminKey = edittext.getText().toString();
                // we set it as default value 0123456789
                if (enteredAdminKey.equals(adminPwd)) {
                    // you are admin
                    storeSharedPreferenceValue(enteredAdminKey);
                    gotoBarber();
                } else {
                    // if you are not admin
                    Toast.makeText(SplashActivity.this, "You entered an invalid key. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                // if client///
                gotoClient();
            }
        });

        alert.show();
    }

    public void storeSharedPreferenceValue(String storingValue)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(sharedPrefKey, storingValue);
        editor.commit();


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
        mBuilder.setColor(Color.RED);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);

        int resID=getResources().getIdentifier("notification_music.mp3", "raw", getPackageName());

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        mBuilder.setSound(alarmSound);

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

        mNotificationManager.notify(11, mBuilder.build());
    }

    public void checkFistTimeAndRole() {


        pref = getApplicationContext().getSharedPreferences(sharePreferenceKEY, 0); // 0 - for private mode

        storedPreferenceString = pref.getString(sharedPrefKey, null);// it can be ADMIN or CLIENT
        if (storedPreferenceString == null) {
            // the app was installed first time
            // write the string from firsttimeandRole
            displayCustomDialogWithEditText();

        } else if (storedPreferenceString.equals(SplashActivity.adminPwd)){
            gotoBarber();
        } else
        {
            gotoClient();
        }
    }

    public void gotoBarber()
    {
        Intent mainIntent = new Intent(SplashActivity.this, BarberActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        finish();
    }

    public void gotoClient()
    {
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        finish();
    }
}
