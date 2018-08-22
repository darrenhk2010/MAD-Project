package com.mad.project.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.project.FireAlertDialog;
import com.mad.project.R;
import com.mad.project.RecyclerTouchListener;
import com.mad.project.database.Trips;
import com.mad.project.database.TripsAdapter;



import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , FireAlertDialog.NoticeDialogListener {

    private static final String sTAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private RecyclerView mTripsRV;
    private TripsAdapter mTripsAdapter;
    private List<Trips> mTripsList = new ArrayList<>();
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mFirebaseRef;
    private String mUserId;
    private String mCurrentTripID;
    public static final int REQUEST_CODE = 70;
    public static final String EXTRA_TRIP_ID = "tripIDStr";
    public static final String EXTRA_TRIP_NAME = "tripNameStr";
    public static final String EXTRA_TRIP_SDATE = "tripSDateStr";
    public static final String EXTRA_TRIP_EDATE = "tripEDateStr";
    public static final String EXTRA_TRIP_BUDGET = "tripBudgetStr";

    /**
     * Listing the trips
     * Call, when MainActivity onCreate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Gets the default FirebaseDatabase instance from Auth
        mAuth = FirebaseAuth.getInstance();

        //Check is the user exist
        if(mAuth.getCurrentUser()!=null){
            mUserId = mAuth.getUid();
        }

        //Gets the default FirebaseDatabase instance from Database
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseInstance.setPersistenceEnabled(true);


        // get reference to 'trips' node
        mFirebaseDatabase = mFirebaseInstance.getReference("trips");
        //keeping data fresh
        mFirebaseDatabase.keepSynced(true);

        //Setting the RecycleView
        mTripsRV = (RecyclerView) findViewById(R.id.tripsRV);
        mTripsAdapter = new TripsAdapter(mTripsList);
        mTripsRV.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        mTripsRV.setLayoutManager(layoutManager);
        mTripsRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mTripsRV.removeItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mTripsRV.setItemAnimator(new DefaultItemAnimator());
        mTripsRV.setAdapter(mTripsAdapter);



        // row click listener
        mTripsRV.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mTripsRV, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Trips trips = mTripsList.get(position);
                Intent tripIntent = new Intent(MainActivity.this, RecordsActivity.class);
                tripIntent.putExtra(EXTRA_TRIP_ID, trips.tripID);
                tripIntent.putExtra(EXTRA_TRIP_NAME, trips.name);
                tripIntent.putExtra(EXTRA_TRIP_SDATE, trips.startdate);
                tripIntent.putExtra(EXTRA_TRIP_EDATE, trips.enddate);
                tripIntent.putExtra(EXTRA_TRIP_BUDGET, String.valueOf(trips.budget));
                startActivityForResult(tripIntent, REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                Trips trips = mTripsList.get(position);
                mCurrentTripID = trips.tripID;
                showFireDialog();

            }

        }));

        //Call method to add trips from database
        addTripsChangeListener();


        //Go to AddTripActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddTripActivity.class));
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

        //When click right menu
        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                return true;
            }
            else if (id == R.id.action_person){
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        //When click navigation menu
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

    //Trip data change listener
    private void addTripsChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(mUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Trips trips = dataSnapshot.getValue(Trips.class);
                mTripsList.add(trips);
                // Check for null
                if (trips == null) {
                    Log.e(sTAG, getResources().getString(R.string.tripNull));
                    return;
                }

                Log.e(sTAG, getResources().getString(R.string.tripChange) + trips.name + ", " + trips.budget + ", " + trips.currency + ", " + trips.startdate + ", " + trips.enddate);

                mTripsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Trips trips = dataSnapshot.getValue(Trips.class);
                mTripsList.remove(trips);
                mTripsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(sTAG, getResources().getString(R.string.FailRead), error.toException());
            }
        });
    }
    //Show Dialog
    public void showFireDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new FireAlertDialog();
        dialog.show(getSupportFragmentManager(), getResources().getString(R.string.NoticeDialogFragment));
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        mFirebaseDatabase.child(mUserId).child(mCurrentTripID).removeValue();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
    //End Dialog

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        mFirebaseDatabase.keepSynced(false);
        super.onStop();
    }
}


