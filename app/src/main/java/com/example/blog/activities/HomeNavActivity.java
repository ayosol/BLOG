package com.example.blog.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.blog.R;
import com.example.blog.fragments.HomeFragment;
import com.example.blog.fragments.ProfileFragment;
import com.example.blog.fragments.SettingsFragment;
import com.example.blog.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class HomeNavActivity extends AppCompatActivity implements com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 2;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private static final int PReqCode = 2;
    Dialog popAddPost;
    ImageView popupUserImage, popupPostImage, popupAddBtn;
    TextView popupTitle, popupDescription;
    ProgressBar popupProgressBar;
    private Uri chosenImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Initialize popup on start to be ready for call later
        iniPopup();
        setupPopupImageClick();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();


        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
    }

    //Initialize popup here so you can show later
    private void iniPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        //Initialize popup widgets
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupPostImage = popAddPost.findViewById(R.id.popup_image);
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupProgressBar = popAddPost.findViewById(R.id.popup_progressBar);

        //Load current User profile Image
        Glide.with(HomeNavActivity.this).load(currentUser.getPhotoUrl()).into(popupUserImage);

        //Add Post click listener
        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate all input fields
                if (!popupTitle.getText().toString().isEmpty()
                        && !popupDescription.getText().toString().isEmpty()
                        && chosenImageUri != null) {

                    popupAddBtn.setVisibility(View.INVISIBLE);
                    popupProgressBar.setVisibility(View.VISIBLE);

                    //TODO: Create Post Object and add to Firebase database
                    //Upload post Image first to Firebase Storage
                    StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child("blog_images");
                    final StorageReference imageFilePath = mStorageReference.child(chosenImageUri.getLastPathSegment());
                    imageFilePath.putFile(chosenImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Get the URL from the storage since it was uploaded successfully
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Store URL in a String
                                    String imageDownloadUrl = uri.toString();
                                    //Create the Post Object here
                                    Post post = new Post(popupTitle.getText().toString(),
                                            popupDescription.getText().toString(), imageDownloadUrl,
                                            currentUser.getUid(), currentUser.getPhotoUrl().toString());

                                    //Add Post to Firebase
                                    addPost(post);
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showToastMessage(e.getMessage());
                                    popupAddBtn.setVisibility(View.INVISIBLE);
                                    popupProgressBar.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });


                }
                //Show Message if one or more fields are empty
                else {
                    showToastMessage("Please verify all inputs fields and choose an Image");
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void addPost(Post post) {

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = mDatabase.getReference("Posts").push();

        //Get post unique id and update post key
        String key = mDatabaseRef.getKey();
        post.setPostKey(key);

        //Add Post Data to Firebase FDatabase
        mDatabaseRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToastMessage("Post Added Successfully");
                popupAddBtn.setVisibility(View.VISIBLE);
                popupProgressBar.setVisibility(View.INVISIBLE);
                popAddPost.dismiss();

            }
        });
    }

    private void setupPopupImageClick() {
        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupPostImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT < 22) {
                            openGallery();
                        }
                        checkAndRequestForPermission();
                    }
                });
            }
        });
    }


    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(HomeNavActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeNavActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(HomeNavActivity.this, "Please accept the required permission request", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(HomeNavActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        } else {
            openGallery();
        }
    }

    //Open Gallery of phone for user to pick Image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/");
        startActivityForResult(galleryIntent, REQUEST_CODE);
    }


    //When User picks an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            //The user has successfully chosen an image
            //Then save reference to it's Uri variable
            chosenImageUri = data.getData();
            popupPostImage.setImageURI(chosenImageUri);
            /*Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            user_image.setImageBitmap(bitmap);*/
        }


    }


    //To Avoid Application closing on back button pressed
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            /*//moveTaskToBack(false);
            finishActivity(0);*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                getSupportActionBar().setTitle("Profile");
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).commit();
                break;
            case R.id.nav_settings:
                getSupportActionBar().setTitle("Settings");
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
                break;
            case R.id.nav_log_out:
                //Sign user out here
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeNavActivity.this, LoginActivity.class));
                finish();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserImage = headerView.findViewById(R.id.nav_user_image);


        navUserMail.setText(currentUser.getEmail());
        navUsername.setText(currentUser.getDisplayName());

        //Use Glide to load User Image
        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserImage);

    }

    //Custom Toast message Method
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}