package com.mad.project.model;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mad.project.R;
import com.mad.project.contract.MainContract;
import com.mad.project.database.Trips;
import com.mad.project.database.TripsAdapter;

import java.util.ArrayList;

import static com.google.android.gms.common.internal.Preconditions.checkNotNull;

/**
 * Created by Darren on 4/06/2018.
 */

public class MainFragment extends Fragment implements MainContract.View{
    private MainContract.Presenter mPresenter;

    private TripsAdapter mTripsAdapter;

    private View mNoTripView;

    public MainFragment(){

    }

    public static MainFragment newInstant(){
        return MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTripsAdapter = new TripsAdapter(new ArrayList<Trips>(0),mItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull MainContract.Presenter presenter){
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data){
        mPresenter.result(requestCode,resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container),Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.content_main){

            //set up task view
            L
        }
        }
}
