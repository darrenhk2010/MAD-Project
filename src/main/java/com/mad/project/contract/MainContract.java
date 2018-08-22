package com.mad.project.contract;

import android.support.annotation.NonNull;

import com.mad.project.BasePresenter;
import com.mad.project.BaseView;
import com.mad.project.database.Trips;

import java.util.List;

/**
 * Created by Darren on 4/06/2018.
 */

public interface MainContract {

    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showTrips(List<Trips> tripsList);

        void showAddTrip();

    }
    interface Presenter extends BasePresenter{
        void result(int requestCode, int resultCode);

        void loadTrips (boolean forceUpdate);

        void addNewTrip();

        void openTripDetails(@NonNull Trip request)
    }
}
