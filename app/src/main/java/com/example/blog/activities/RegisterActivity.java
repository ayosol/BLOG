package com.example.blog.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int REQUESTCODE = 1;
    private static int PReqCode = 1;
    ImageView user_image;
    Uri chosenImageUri;

    private EditText user_name, user_email, user_password, user_confirm_password;
    private ProgressBar loadingProgressBar;
    private Button btn_register;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        loadingProgressBar = findViewById(R.id.progressBar);
        user_name = findViewById(R.id.txt_reg_name);
        user_email = findViewById(R.id.txt_reg_mail);
        user_password = findViewById(R.id.txt_reg_password);
        user_confirm_password = findViewById(R.id.txt_reg_password_2);
        user_image = findViewById(R.id.profile_image);
        btn_register = findViewById(R.id.btn_register);

        loadingProgressBar.setVisibility(View.INVISIBLE);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_register.setVisibility(View.INVISIBLE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                String email = user_email.getText().toString().trim();
                String password = user_password.getText().toString().trim();
                String confirm_password = user_confirm_password.getText().toString().trim();
                String name = user_name.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password) || !password.equals(confirm_password)){
                    //Show Error message to user
                    showToastMessage("Please verify all fields correctly");
                    btn_register.setVisibility(View.VISIBLE);
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
                else {
                    //Fields are complete and okay
                    createUserAccount(email, name, password);
                }
            }
        });


        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 22){
                    openGallery();
                }
                checkAndRequestForPermission();
            }
        });
    }


    private void createUserAccount(String email, final String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showToastMessage("Account created successfully");
                            updateUserInfo(name, chosenImageUri, mAuth.getCurrentUser());

                        }
                        else {
                            showToastMessage("Account creation failed. Try Again! \n" + task.getException().getMessage());
                            loadingProgressBar.setVisibility(View.INVISIBLE);
                            btn_register.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }


    //Method to update User Info and store image
    private void updateUserInfo(final String name, Uri chosenImageUri, final FirebaseUser currentUser) {
        //Upload user Image to firebase storage first ad get the url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_images");
        final StorageReference imageFilePath = mStorage.child(chosenImageUri.getLastPathSegment());
        imageFilePath.putFile(chosenImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Image has been uploaded successfully, so we can now get Image url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Uri contains Image url
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //User info has been updated successfully
                                            showToastMessage("Registration complete");
                                            updateUI();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }


    private void updateUI() {
        Intent homeActivityIntent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(homeActivityIntent);
        finish();
    }


    //Custom Toast message Method
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext() , message, Toast.LENGTH_SHORT).show();
    }


    //Open Gallery of phone for user to pick Image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }


    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){

                Toast.makeText(RegisterActivity.this, "Please accept the required permission request", Toast.LENGTH_SHORT).show();
            }
            else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }

        else {
            openGallery();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null){
            //The user has successfully chosen an image
            //Then save reference to it's Uri variable
            chosenImageUri = data.getData();
            user_image.setImageURI(chosenImageUri);
        }

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                user_image.setImageBitmap(bitmap);
            }
        }
    }
}