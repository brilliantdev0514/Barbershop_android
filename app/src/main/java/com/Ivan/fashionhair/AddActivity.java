package com.Ivan.fashionhair;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.Ivan.fashionhair.Modal.Client;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {
    EditText username, userPhone;
    Button add;
    TextView displayTime;
    Date currentTime;
    long unixTime;
    TextView displayDate;
    DatabaseReference mRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        setContentView(R.layout.add);
        username = findViewById(R.id.username);
        userPhone = findViewById(R.id.userPhone);
        displayTime = (TextView) findViewById(R.id. timeDisp);
        displayDate = (TextView) findViewById(R.id.dateDisp);
        add = findViewById(R.id.addBtn);
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


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = username.getText().toString();
                final String phone = userPhone.getText().toString();
                if (name.equals("") || phone.equals("")){
                    Toast.makeText(AddActivity.this, "Please enter name & phone number!", Toast.LENGTH_SHORT).show();
                }else {
                    DatabaseReference myRef;
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    myRef = database.getReference().child("isEnabled");
                    mRef = database.getReference().child("Clients");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String isEnabled = (String)dataSnapshot.getValue();
                            if (isEnabled == null || isEnabled.equals("1"))
                            {
                                final String onlyTime = displayTime.getText().toString();
                                final String uid = UUID.randomUUID().toString();
                                Client mClient = new Client(uid, phone, name, onlyTime, "REQUESTED", unixTime, false);
                                mRef.child(uid).setValue(mClient).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(AddActivity.this, BarberActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {
                                Toast.makeText(AddActivity.this, "You are not available from now on", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }
}
