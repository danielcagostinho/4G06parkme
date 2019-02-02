package com.example.parkinglot.Components;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

public class ParkingLotListView extends ArrayAdapter<ParkingLotItem> implements View.OnClickListener{

    public ArrayList<ParkingLotItem> parkingLots;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView available;
        TextView distance;
        TextView time;
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
            viewHolder.distance =  convertView.findViewById(R.id.distance);
            viewHolder.percentage = convertView.findViewById(R.id.percentage);
            viewHolder.time = convertView.findViewById(R.id.time);
            result=convertView;

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.name.setText(parkingLot.name);
        viewHolder.available.setText(Integer.toString(parkingLot.availableParkingSpaces));
        viewHolder.distance.setText(parkingLot.getDistance());
        viewHolder.time.setText(parkingLot.getTime());

        int percentage = (int) (parkingLot.percentage * 100);
        viewHolder.percentage.setText(Integer.toString(percentage) + "%");



        // "full" text
        TextView  tv = convertView.findViewById(R.id.full);
        tv.setVisibility(View.VISIBLE);

        int textColor = Color.GREEN;
        if (percentage == 100) {
            textColor = Color.RED;
            tv.setVisibility(View.INVISIBLE);
        } else if (percentage >= 90 ){
            textColor = Color.RED;
            viewHolder.percentage.setText("FULL");
        } else if (percentage >= 50) {
            textColor = Color.argb(255,255,144,0);    // orange
        }

        viewHolder.percentage.setTextColor(textColor);
        return convertView;
    }
}