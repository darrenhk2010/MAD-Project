package com.mad.project.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.project.DatePickerFragment;
import com.mad.project.R;
import com.mad.project.database.Trips;

import java.text.DateFormat;
import java.util.Calendar;

public class AddTripActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , DatePickerDialog.OnDateSetListener{

    public static final String sTAG = AddTripActivity.class.getSimpleName();
    private EditText mNameET, mBudgetET, mStartdateET, mEnddateET;
    private Spinner mCurrencySpi;
    private Button mSaveBtn;
    private ImageView mAdd_trip_startdateBtn, mAdd_trip_enddateBtn;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String mUserId;
    private String mCurrentDateString;
    private Boolean mStartEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        // Create a mStorage reference from our app
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'trips' node
        mFirebaseDatabase = mFirebaseInstance.getReference("trips");

        mNameET = (EditText) findViewById(R.id.add_trip_nameET);
        mBudgetET = (EditText) findViewById(R.id.add_trip_budgetET);
        mCurrencySpi = (Spinner) findViewById(R.id.add_trip_currencySpi);
        mStartdateET = (EditText) findViewById(R.id.add_trip_startdateET);
        mEnddateET = (EditText) findViewById(R.id.add_trip_enddateET);
        mAdd_trip_startdateBtn = (ImageView) findViewById(R.id.add_trip_startdateBtn);
        mAdd_trip_enddateBtn = (ImageView) findViewById(R.id.add_trip_enddateBtn);

        mAdd_trip_startdateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), getResources().getString(R.string.datePicker));
                    mStartEndDate = true;
                }
        });
        mAdd_trip_enddateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), getResources().getString(R.string.datePicker));
                mStartEndDate = false;
            }
        });

        mSaveBtn = (Button) findViewById(R.id.add_tripBtn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String tripID = mFirebaseDatabase.push().getKey();
                    String name = mNameET.getText().toString();
                    double budget = 0;
                    budget = Double.parseDouble(mBudgetET.getText().toString());
                    String currency = mCurrencySpi.getSelectedItem().toString();
                    String startdate = mStartdateET.getText().toString();
                    String enddate = mEnddateET.getText().toString();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(AddTripActivity.this, getResources().getString(R.string.NameEmpty), Toast.LENGTH_SHORT).show();
                        return;
                    }


                if(budget >= 0){
                    createTrip(tripID, name, budget, currency, startdate, enddate);
                }else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.AmountEmpty), Toast.LENGTH_SHORT).show();
                    return;
                }

                }catch (RuntimeException ex){
                    Log.e(sTAG, getResources().getString(R.string.Exception), ex);
                }
                finish();
            }
        });

        //Navigation Section
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    //When click on Navigation menu
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        mCurrentDateString = DateFormat.getDateInstance().format(calendar.getTime());
        if(mStartEndDate == true){
            mStartdateET.setText(mCurrentDateString);
        }else if(mStartEndDate== false){
            mEnddateET.setText(mCurrentDateString);
        }
    }

    //Upload data to firebase
    private void createTrip(String tripID, String name, double budget, String currency, String startdate, String enddate){
        if (TextUtils.isEmpty(mUserId)) {
            mUserId = mAuth.getUid();
        }
        Trips trips = new Trips(tripID, name, budget, currency, startdate, enddate);

        mFirebaseInstance.getReference("trips").child(mUserId).child(tripID).setValue(trips);

    }



}
