package com.example.blog.activities;

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

public class RegisterActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int REQUESTCODE = 1;
    private static int PReqCode = 1;
    ImageView user_image;
    Uri chosenImageUri;

    private EditText user_name, user_email, user_password, user_confirm_password;
    private ProgressBar loadingProgressBar;
    private Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loadingProgressBar = findViewById(R.id.progressBar);
        user_name = findViewById(R.id.txt_reg_name);
        user_email = findViewById(R.id.txt_reg_mail);
        user_password = findViewById(R.id.txt_reg_password);
        user_confirm_password = findViewById(R.id.txt_reg_password_2);
        user_image = findViewById(R.id.profile_image);
        btn_register = findViewById(R.id.btn_register);

        loadingProgressBar.setVisibility(View.INVISIBLE);



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

    private void openGallery() {
        //TODO: Open Gallery Intent and wait for user to pick an image

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