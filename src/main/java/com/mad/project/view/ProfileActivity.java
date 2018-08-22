package com.mad.project.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.project.R;
import com.mad.project.database.User;

import java.io.File;
import java.io.IOException;

/**
 * Profile screen to store user information
 */
public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String sTAG = ProfileActivity.class.getSimpleName();
    private TextView mTxtDetails;
    private EditText mInputName, mInputEmail;
    private Button mBtnSave, mUploadBtn, mBtnLogout;
    private ImageView mProfileImg;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String mUserId;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseStorage storage;
    private Uri mFilePath;
    public final int PICK_IMAGE_REQUEST = 71;
    private Bitmap mBmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        // Create a storage reference from our app
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();

        mProfileImg = (ImageView) findViewById(R.id.profileImg);
        mProfileImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                chooseImage();
            }
        });

        mUploadBtn = (Button) findViewById(R.id.uploadBtn);
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        //Call DownloadAsyncTask class
        new DownloadAsyncTask();



        //Navigation Section
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Displaying toolbar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        mTxtDetails = (TextView) findViewById(R.id.txt_user);
        mInputName = (EditText) findViewById(R.id.name);
        mInputEmail = (EditText) findViewById(R.id.email);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnLogout = (Button) findViewById(R.id.btn_logout);

        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        // store app title to 'app_title' node
        mFirebaseInstance.getReference("app_title").setValue("Realtime Database");

        // app_title change listener
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(sTAG, "App title updated");

                String appTitle = dataSnapshot.getValue(String.class);

                // update toolbar title
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(sTAG, getResources().getString(R.string.FailRead), error.toException());
            }
        });

        // Save / update the user
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mInputName.getText().toString();
                String email = mInputEmail.getText().toString();

                // Check for already existed mUserId
                if (TextUtils.isEmpty(mUserId)) {
                    createUser(name, email);
                } else {
                    updateUser(name, email);
                }
            }
        });

        // Logout
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                ActivityCompat.finishAffinity(ProfileActivity.this);
            }
        });

        toggleButton();
    }

    //Get iamge from device
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectPicture)), PICK_IMAGE_REQUEST);
    }

    //Upload the image to firebase
    private void uploadImage() {

        if(mFilePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.uploading));
            progressDialog.show();

            StorageReference ref = mStorageRef.child(mAuth.getUid()).child("images/profile");
            ref.putFile(mFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.uploaded), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.Failed), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage(getResources().getString(R.string.Failed)+(int)progress+getResources().getString(R.string.present));
                        }
                    });
        }
    }

    //Store the image in the screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            mFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePath);
                mProfileImg.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onBackPressed () {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected (MenuItem item){
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this,MainActivity.class));
        } else if (id == R.id.nav_record) {
            startActivity(new Intent(this, ProfileActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Changing button text
    private void toggleButton() {
        if (TextUtils.isEmpty(mUserId)) {
            mBtnSave.setText(getResources().getString(R.string.save));
        } else {
            mBtnSave.setText(getResources().getString(R.string.update));
        }
    }

    //Creating new user node under 'users'
    private void createUser(String name, String email) {
        // In real apps this mUserId should be fetched
        // by implementing firebase mAuth

        if (TextUtils.isEmpty(mUserId)) {
            mUserId = mAuth.getUid();
        }

        User user = new User(name, email);

        mFirebaseDatabase.child(mUserId).setValue(user);

        addUserChangeListener();
    }

    //User data change listener
    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Check for null
                if (user == null) {
                    Log.e(sTAG, "User data is null!");
                    return;
                }

                Log.e(sTAG, "User data is changed!" + user.name + ", " + user.email);

                // Display newly updated name and email
                mTxtDetails.setText(user.name + ", " + user.email);

                // clear edit text
                mInputEmail.setText("");
                mInputName.setText("");

                toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(sTAG, getResources().getString(R.string.FailRead), error.toException());
            }
        });
    }

    private void updateUser(String name, String email) {
        // updating the user via child nodes
        if (!TextUtils.isEmpty(name))
            mFirebaseDatabase.child(mUserId).child("name").setValue(name);

        if (!TextUtils.isEmpty(email))
            mFirebaseDatabase.child(mUserId).child("email").setValue(email);
    }

    /**
     * Download the profile image
     */
    class DownloadAsyncTask extends AsyncTask<Void, Void, Bitmap > {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Bitmap doInBackground(Void... params) {

            StorageReference imageRef = mStorageRef.child(mAuth.getUid()+"/images/profile.jpg");
            if (imageRef != null) {

                try {
                    final File localFile = File.createTempFile("images", "jpg");

                    imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            mBmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("TripsAdapter", "onFailure: ", exception);
                        }

                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(sTAG, getResources().getString(R.string.FailRead));
            }

            return mBmp;
        }



        @Override
        protected void onPostExecute (Bitmap bmp){
            mProfileImg.setImageBitmap(bmp);
            super.onPostExecute(bmp);
        }
    }
}
