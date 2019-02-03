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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        GraphView graph;
    }

    public ParkingLotListView(ArrayList<ParkingLotItem> data, Context context) {
        super(context, R.layout.parking_lot_row_item, data);
        this.parkingLots = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {


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
            viewHolder.graph = convertView.findViewById(R.id.graph);

            convertView.findViewById(R.id.parkingLotBtn).setOnClickListener((View view) -> {
                goToParkingLot(parkingLot.id);
            });


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

        drawAnalyticsGraph(viewHolder.graph, parkingLot.analytics);

        return convertView;
    }

    // draws analytics graphics
    private void drawAnalyticsGraph(GraphView graph, JSONObject analytics) {
        try {
            JSONObject hourly  = analytics.getJSONObject("hourly");
            Iterator<String> keys = hourly.keys();

            int total = analytics.getInt("total");

            List<DataPoint> points = new ArrayList<>();

            while(keys.hasNext()) {
                String key = keys.next();
                int val = hourly.getInt(key);
                double percentVal = (val*1.0)/total;
                points.add( new DataPoint(Integer.parseInt(key), percentVal));
            }

            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(points.toArray(new DataPoint[0]));
            graph.addSeries(series);
        } catch (JSONException e){
            // draw no points
            graph.addSeries(new BarGraphSeries());
        }
    }

    // navigates to parking lot map
    private void goToParkingLot(String id) {
        Log.d(MapsActivity.TAG, "goToParkingLot: " + id);
        // KAT PUT CODE HERE
    }
}