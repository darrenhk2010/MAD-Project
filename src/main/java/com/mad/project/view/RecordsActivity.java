package com.mad.project.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.project.R;
import com.mad.project.database.Records;
import com.mad.project.database.RecordsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * RecordsActivity shows a list of records in a trip
 */
public class RecordsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String sTAG = RecordsActivity.class.getSimpleName();
    private TextView records_name, records_date, records_remain_destination, records_remain_base, records_spend_destination, records_spend_base, records_budget_destination, records_budget_base;
    private ProgressBar mRecords_progress;
    private StorageReference mStorageRef;
    private FirebaseStorage mStorage;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;
    private String mUserId;
    private List<Records> mRecordsList = new ArrayList<>();
    private RecyclerView mRecordsRV;
    private RecordsAdapter mRecordsAdapter;
    private String mTripID, mTripName, mTripStartDate, mTripEndDate, mTripBudget;
    public static final int REQUEST_CODE = 70;
    private double mSumAmount, mSetTripBudget;
    private static final String EXTRA_TRIP_ID = "tripIDStr";
    private static final String EXTRA_TRIP_NAME = "tripNameStr";
    private static final String EXTRA_TRIP_SDATE = "tripSDateStr";
    private static final String EXTRA_TRIP_EDATE = "tripEDateStr";
    private static final String EXTRA_TRIP_BUDGET = "tripBudgetStr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent tripIntent = getIntent();
        mTripID = tripIntent.getStringExtra(EXTRA_TRIP_ID);
        mTripName = tripIntent.getStringExtra(EXTRA_TRIP_NAME);
        mTripStartDate = tripIntent.getStringExtra(EXTRA_TRIP_SDATE);
        mTripEndDate = tripIntent.getStringExtra(EXTRA_TRIP_EDATE);
        mTripBudget = tripIntent.getStringExtra(EXTRA_TRIP_BUDGET);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            mUserId = mAuth.getUid();
        }

        // Create a mStorage reference from our app
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        mFirebaseInstance = FirebaseDatabase.getInstance();
        //enable disk  persistence
       // mFirebaseInstance.setPersistenceEnabled(true); //It was null and get error

        // get reference to 'records' node
        mFirebaseDatabase = mFirebaseInstance.getReference("records");
        //keeping data fresh
        mFirebaseDatabase.keepSynced(true);

        records_name = (TextView) findViewById(R.id.records_name);
        records_date = (TextView) findViewById(R.id.records_dates);
        records_remain_destination = (TextView) findViewById(R.id.records_remain_destination);
        records_remain_base = (TextView) findViewById(R.id.records_remain_base);
        records_spend_destination = (TextView) findViewById(R.id.records_spend_destination);
        records_spend_base = (TextView) findViewById(R.id.records_spend_base);
        records_budget_destination = (TextView) findViewById(R.id.records_budget_destination);
        records_budget_base = (TextView) findViewById(R.id.records_budget_base);
        mRecords_progress = (ProgressBar) findViewById(R.id.records_progress);




        //Records Listener. Get data from firebase
        addRecordChangeListener();
        getValueListener();

        if (mTripName !=null) {
            records_name.setText(mTripName);
        }
        if(mTripStartDate == null){
            mTripStartDate ="";
        }
        else if (mTripEndDate == null){
            mTripEndDate ="";
        }
        records_date.setText(mTripStartDate + " - " + mTripEndDate);
        mSetTripBudget = Double.parseDouble(mTripBudget);
        if(mTripBudget == null){
            mSetTripBudget = 0;
        }
        records_budget_destination.setText("$" + String.valueOf(mTripBudget));

        //Setting the RecycleView
        mRecordsRV = (RecyclerView) findViewById(R.id.tripsRV);
        mRecordsAdapter = new RecordsAdapter(mRecordsList);
        mRecordsRV.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        mRecordsRV.setLayoutManager(layoutManager);
        mRecordsRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecordsRV.setItemAnimator(new DefaultItemAnimator());
        mRecordsRV.setAdapter(mRecordsAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recordIntent = new Intent(RecordsActivity.this, AddRecordActivity.class);
                recordIntent.putExtra(EXTRA_TRIP_ID, mTripID);
                startActivityForResult(recordIntent, REQUEST_CODE);
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
            startActivity(new Intent(this, AccountActivity.class));
        }
        else if (id == R.id.action_person){
            startActivity(new Intent(this,ProfileActivity.class));
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

    /**
     * Record data change listener
     */
    private void addRecordChangeListener() {
        mFirebaseDatabase.child(mUserId).child(mTripID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Records records = dataSnapshot.getValue(Records.class);
                mRecordsList.add(records);
                // Check for null
                if (records == null) {
                    Log.e(sTAG, getResources().getString(R.string.FailRead));
                    return;
                }

                Log.e(sTAG, getResources().getString(R.string.tripChange) + records.recordID + ", " + records.name + ", " + records.type + ", " + records.date + ", "+ records.time + ", " + records.amount + ", " +records.location);

                mRecordsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(sTAG, getResources().getString(R.string.FailRead), error.toException());
            }
        });
    }
    /**
     * Get value data change listener
     */
    private void getValueListener() {
        mFirebaseDatabase.child(mUserId).child(mTripID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mSumAmount =0;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String,Object>) ds.getValue();
                    Object amount = map.get("amount");
                    double amountValue = Double.parseDouble(String.valueOf(amount));
                    mSumAmount += amountValue;
                    records_spend_destination.setText("$" + String.valueOf(mSumAmount));
                    double remain_destination = calculateRemain(mSetTripBudget, mSumAmount);
                    records_remain_destination.setText("$" + String.valueOf(remain_destination));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(sTAG, getResources().getString(R.string.FailRead), error.toException());
            }
        });
    }


    private double calculateRemain(double budget_destination, double spend_destination){
        double remainDestination = budget_destination - spend_destination;
        double progress = (remainDestination/budget_destination)*100;
        int progressInt = (int) progress;
        mRecords_progress.setProgress(progressInt);
        return remainDestination;
    }




}
