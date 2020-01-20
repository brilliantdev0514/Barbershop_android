package com.Ivan.fashionhair;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Ivan.fashionhair.ListView.ClientAdapter;
import com.Ivan.fashionhair.Modal.Client;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.security.cert.CertPathValidatorException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class BarberActivity extends AppCompatActivity {
    ArrayList<Client> arrayOfUsers;
    ListView listView;
    Date currentTime;
    TextView displayDate;
    TextView displayTime;
    TextView userName;
    DatabaseReference mRef;
    Button requestEnable;
    Button clear;
    RelativeLayout relativeLayout;
    ImageView customAdding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_profile);
        displayDate = (TextView) findViewById(R.id.BarberDate);
        displayTime = (TextView) findViewById(R.id.BarberTime);
        requestEnable = findViewById(R.id.requestEnableBtn);
        userName = (TextView) findViewById(R.id.name);

        relativeLayout = (RelativeLayout) findViewById(R.id.listbackgroundchange);
        clear = findViewById(R.id.clearBtn);
        customAdding = findViewById(R.id.customAddBtn);
        //TODO; custom add click event here
        customAdding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BarberActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = requestEnable.getText().toString();
                if (status.equals("DISABLE")){
                    Toast.makeText(BarberActivity.this, "You can't clear list!", Toast.LENGTH_LONG).show();
                }else if (status.equals("ENABLE")){
                    mRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listView.setAdapter(null);
                        }
                    });
                }
            }
        });

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                tvClock.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
                currentTime = Calendar.getInstance().getTime();
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


        database.getReference().child("isEnabled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot == null) {
//                    return;
//                }

                String isEnable = dataSnapshot.getValue(String.class);
                if (isEnable == null  || isEnable.equals("1")) {
                    requestEnable.setSelected(false);
                    requestEnable.setText("DISABLE");
                } else {
                    requestEnable.setSelected(true);
                    requestEnable.setText("ENABLE");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listView = (ListView) findViewById(R.id.AdminList);

        requestEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestEnable.isSelected())
                {
                    requestEnable.setSelected(false);
                    requestEnable.setText("DISABLE");
                } else
                {
                    requestEnable.setSelected(true);
                    requestEnable.setText("ENABLE");
                }

                setEnableDisableRquest();
            }
        });
    }

    private void setEnableDisableRquest() {

        final DatabaseReference myRef;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        if (requestEnable.isSelected())
        {
            // can not get any request from client
            myRef.child("isEnabled").setValue("0");
            //todo; database clean

        } else
        {
            // can get request
            myRef.child("isEnabled").setValue("1");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRef.addValueEventListener(eventListener);
    }

    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {

            arrayOfUsers = new ArrayList<Client>();

            if (dataSnapshot.getValue() == null) {
                return;
            }



            for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                final String phone = ds.child("phoneNumber").getValue().toString();
                if (!ds.hasChild("state"))
                {
                    continue;
                }
                Client user = ds.getValue(Client.class);
                String key = ds.getKey();
                if (user.state.equals("null") || user.orderNo == 0)
                    continue;
                arrayOfUsers.add(user);
            }

            Collections.sort(arrayOfUsers);
//            Collections.reverse(arrayOfUsers);
//            Collections.reverse(arrayOfUsers);
            // Create the adapter to convert the array to views
            final ClientAdapter adapter = new ClientAdapter(BarberActivity.this, arrayOfUsers);
            listView.setAdapter(adapter);

            listView.setClickable(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                    TextView a = (TextView) view.findViewById(R.id.phone);
                    TextView b = view.findViewById(R.id.uid);
                    final String phone = a.getText().toString();
                    final String uid = b.getText().toString();
                    new AlertDialog.Builder(BarberActivity.this)
                            .setTitle("User Info")
                            .setPositiveButton("Dial", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri number = Uri.parse("tel:"+ phone);
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                    startActivity(callIntent);
                                }
                            })
                            .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Object item = adapter.getItem(position);
                                    adapter.remove((Client) item);
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference mRef = database.getReference().child("Clients").child(uid);
                                    mRef.removeValue();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

        }


        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mRef.removeEventListener(eventListener);
    }



}