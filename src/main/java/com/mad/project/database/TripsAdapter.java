package com.mad.project.database;

/**
 * Created by Darren on 21/05/2018.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.project.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.MyViewHolder> {
    private List<Trips> tripsList;
    private StorageReference mStorageRef;
    private FirebaseStorage mStorage;
    private Bitmap bmp;



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tripsName, tripsDates, tripsBudget, tripsCurrency;
        public ImageView trips_photo;


        public MyViewHolder(View view) {

            super(view);
            tripsName = (TextView) view.findViewById(R.id.trips_nameTV);
            tripsDates = (TextView) view.findViewById(R.id.trips_datesTV);
            tripsBudget = (TextView) view.findViewById(R.id.trips_budgetTV);
            tripsCurrency = (TextView) view.findViewById(R.id.trips_currencyTV);
            trips_photo = (ImageView) view.findViewById(R.id.trips_photo);
        }
    }

    public TripsAdapter(List<Trips> tripsList) {
        this.tripsList = tripsList;

        // Create a storage reference from our app
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        new DownloadAsyncTask().execute();

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trips, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Trips trips = tripsList.get(position);
        holder.tripsName.setText(trips.name);
        holder.tripsBudget.setText(String.valueOf(trips.budget));
        holder.tripsDates.setText(trips.startdate + " - " + trips.enddate);
        holder.tripsCurrency.setText(trips.currency);

    }

    @Override
    public int getItemCount() {
        return tripsList.size();
    }


    class DownloadAsyncTask extends AsyncTask<Void, Void, Bitmap > {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Bitmap doInBackground(Void... params) {

            StorageReference imageRef = mStorageRef.child("images/country/view.jpg");
            if (imageRef != null) {

                try {
                    final File localFile = File.createTempFile("images", "jpg");

                    imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
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
                Log.d("TripsAdapter", "downloadToLocalFile: ");
            }

            return bmp;
        }


        @Override
        protected void onPostExecute (Bitmap bmp){

            super.onPostExecute(bmp);
        }
    }

}