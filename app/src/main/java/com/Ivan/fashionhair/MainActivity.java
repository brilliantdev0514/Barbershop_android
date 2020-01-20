package com.Ivan.fashionhair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private EditText editTextMobile, editTextName;
    private Button photoEdit;
    private final int PICK_IMAGE_REQUEST = 10;
    String mobile, name;
    private Uri filePath;
    private ImageView profilePhoto;
    Bitmap bmp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try
//        {
//            this.getSupportActionBar().hide();
//        }
//        catch (NullPointerException e){}

        // check auth

        setContentView(R.layout.activity_main);

        checkAuth();
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextName = findViewById(R.id.userNameInfo);
        photoEdit = findViewById(R.id.profilePhotoEditButton);
        profilePhoto = findViewById(R.id.imageView);
        editTextMobile.setHint("(+1)2127601011");
        photoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobile = editTextMobile.getText().toString().trim();
                name = editTextName.getText().toString().trim();
                if(name.isEmpty()){
                    editTextName.setError("Enter your Name");
                    editTextName.requestFocus();
                }
                if(mobile.isEmpty() || mobile.length() < 10){
                    editTextMobile.setError("Enter a valid mobile");
                    editTextMobile.requestFocus();
                    return;
                }

//                if (bmp == null)
//                {
//                    Toast.makeText(MainActivity.this, "Please upload your selfie.", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                profilePhoto.setDrawingCacheEnabled(true);
                profilePhoto.buildDrawingCache();

//                uploadImage();
                gotoNext();

            }
        });
    }

    private void checkAuth() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent intent = new Intent(this, ClientActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // User is signed out
            Log.d("AAA", "onAuthStateChanged:signed_out");
        }
    }

    public void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                profilePhoto.setImageBitmap(bmp);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        } else
        {
            bmp = null;
        }
    }

    public void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();;
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            StorageReference ref = storageRef.child("images/" + UUID.randomUUID().toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            gotoNext();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });


        }


    }
/////////////////////////////////////////////////////

//////////////////////////////////
    private void gotoNext() {
        Intent intent = new Intent(MainActivity.this, VerifyPhoneActivity.class);
        intent.putExtra("mobile", mobile);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }

}
