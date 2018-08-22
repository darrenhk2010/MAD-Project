package com.mad.project.database;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.project.R;

import java.util.List;

/**
 * Created by Darren on 27/05/2018.
 */

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.MyViewHolder>{
    private List<Records> recordsList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView transaction_name, transaction_date, transaction_time, transaction_amount, transaction_type, transaction_location;

        public MyViewHolder(View view){
            super(view);
            transaction_name = (TextView) view.findViewById(R.id.transaction_nameTV);
            transaction_type = (TextView) view.findViewById(R.id.transaction_typeTV);
            transaction_date = (TextView)view.findViewById(R.id.transaction_dateTV);
            transaction_time = (TextView) view.findViewById(R.id.transaction_timeTV);
            transaction_amount = (TextView) view.findViewById(R.id.transaction_amountTV);
            transaction_location = (TextView) view.findViewById(R.id.transaction_locationTV);
        }
    }

    public RecordsAdapter(List<Records> recordsList){
        this.recordsList = recordsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.records, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Records records = recordsList.get(position);
        holder.transaction_name.setText(records.name);
        holder.transaction_type.setText(records.type);
        holder.transaction_date.setText(records.date);
        holder.transaction_time.setText(records.time);
        holder.transaction_amount.setText(String.valueOf(records.amount));
        holder.transaction_location.setText(records.location);
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }
}
