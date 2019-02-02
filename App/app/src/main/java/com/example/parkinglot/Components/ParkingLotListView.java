package com.example.parkinglot.Components;

import android.content.Context;
import android.graphics.Color;
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
        TextView percentage;
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
            viewHolder.percentage = convertView.findViewById(R.id.percentage);

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

        int percentage = (parkingLot.totalParkingSpaces != 0) ? (Math.round((parkingLot.availableParkingSpaces * 100f) / parkingLot.totalParkingSpaces)) : 100 ;
        percentage  = 100 - percentage; // find inverse
        viewHolder.percentage.setText(Integer.toString(percentage) + "%");

        int textColor = Color.GREEN;
        if (percentage == 0) {
            textColor = Color.RED;
            viewHolder.percentage.setText("FULL");
        } else if( percentage <= 25) {
            textColor = Color.argb(255,255,144,0);    // orange
        } else if (percentage <= 50) {
            textColor = Color.YELLOW;
            viewHolder.percentage.setShadowLayer(1, 0, 0, Color.BLACK);
        }

        viewHolder.percentage.setTextColor(textColor);
        return convertView;
    }
}