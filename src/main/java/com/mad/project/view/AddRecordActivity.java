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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.project.DatePickerFragment;
import com.mad.project.R;
import com.mad.project.database.Records;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Add a record to store how much user spent
 */
public class AddRecordActivity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener{
    private EditText mNameET, mDateET, mTimeET, mTypeET, mAmountET, mLocationET;
    private Button mSaveBtn;
    private String mTripID, mUserId;
    public static final String EXTRA_TRIP_ID = "tripIDStr";
    private FirebaseAuth mAuth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String mCurrentDateString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent tripIntent = getIntent();
        mTripID = tripIntent.getStringExtra(EXTRA_TRIP_ID);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() !=null){
            mUserId = mAuth.getUid();
        }

        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'trips' node
        mFirebaseDatabase = mFirebaseInstance.getReference("records").child(mUserId);

        mNameET = (EditText) findViewById(R.id.add_record_nameET);
        mDateET = (EditText) findViewById(R.id.add_record_dateET);
        mTimeET = (EditText) findViewById(R.id.add_record_timeET);
        mTypeET = (EditText) findViewById(R.id.add_record_typeET);
        mAmountET = (EditText) findViewById(R.id.add_record_amountET);
        mLocationET = (EditText) findViewById(R.id.add_record_locationET);

        mDateET.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
                }
            }
        });

        mSaveBtn = (Button) findViewById(R.id.add_recordBtn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String recordID = mFirebaseDatabase.push().getKey();
                String name = mNameET.getText().toString();
                String date = mDateET.getText().toString();
                String time = mTimeET.getText().toString();
                String type = mTypeET.getText().toString();
                String location = mLocationET.getText().toString();
                double amount = Double.parseDouble(mAmountET.getText().toString());

                createRecord(recordID, name, date, time, type, location, amount);
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
        mDateET.setText(mCurrentDateString);
    }

    private void createRecord(String recordID, String name, String date, String time, String type, String location ,double amount){

        Records records = new Records(recordID, name, date, time, type, amount, location);

        try {
            mFirebaseDatabase.child(mTripID).child(recordID).setValue(records);
        }catch (Exception ex){
            Log.e("EX", "createRecord: ", ex);
        }


    }

}
