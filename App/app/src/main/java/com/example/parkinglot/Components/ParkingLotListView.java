package com.example.parkinglot.Components;

import android.content.Context;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.parkinglot.MapsActivity;
import com.example.parkinglot.R;

import java.util.ArrayList;
import java.util.Map;

public class ParkingLotListView extends ArrayAdapter<ParkingLotItem> implements View.OnClickListener{

    public ArrayList<ParkingLotItem> parkingLots;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView available;
        TextView total;
        TextView distance;
    }

    public ParkingLotListView(ArrayList<ParkingLotItem> data, Context context) {
        super(context, R.layout.parking_lot_row_item, data);
        this.parkingLots = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

//        int position=(Integer) v.getTag();
//        Object object= getItem(position);
//        ParkingLotItem dataModel=(ParkingLotItem)object;

        // do something on click

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        ParkingLotItem parkingLot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.parking_lot_row_item, parent, false);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.available =  convertView.findViewById(R.id.available);
            viewHolder.total =  convertView.findViewById(R.id.total);
            viewHolder.distance =  convertView.findViewById(R.id.distance);

            result=convertView;

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.name.setText(parkingLot.name);
        viewHolder.available.setText(Integer.toString(parkingLot.availableParkingSpaces));
        viewHolder.total.setText(Integer.toString(parkingLot.totalParkingSpaces));
        viewHolder.distance.setText(parkingLot.distance);

        // Return the completed view to render on screen
        Log.d(MapsActivity.TAG, "getView...");
        return convertView;
    }
}