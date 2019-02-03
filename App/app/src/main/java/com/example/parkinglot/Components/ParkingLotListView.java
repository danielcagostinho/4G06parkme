package com.example.parkinglot.Components;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.parkinglot.MapsActivity;
import com.example.parkinglot.ParkingSpotActivity;
import com.example.parkinglot.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

        drawHourlyAnalyticsGraph(viewHolder.graph, parkingLot.analytics);

        return convertView;
    }

    // draws analytics graphics
    private void drawHourlyAnalyticsGraph(GraphView graph, JSONObject analytics) {
        try {
            JSONObject hourly  = analytics.getJSONObject("hourly");
            Iterator<String> keys = hourly.keys();

            int total = analytics.getInt("total");

            List<DataPoint> points = new ArrayList<>();

            while(keys.hasNext()) {
                String key = keys.next();
                int hour = Integer.parseInt(key);
                int val = hourly.getInt(key);

                // peak times for parking lot
                if (hour < 8 || hour > 18)
                    continue;
                double percentVal = (val*1.0)/total;
                points.add( new DataPoint(hour, percentVal));
            }
            graph.getViewport().setMinX(8);
            graph.getViewport().setMaxX(18);

            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(points.toArray(new DataPoint[0]));
            graph.addSeries(series);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.getGridLabelRenderer().setTextSize(25f);
            graph.getViewport().setXAxisBoundsManual(true);

            graph.setTitle("Hourly Statistics");

            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {

                        //  show normal x values
                        String post ="";
                        if (value == 18) {
                            post = "PM";
                        }
                        if (value == 8) {
                            post = "AM";
                        }
                        if (value > 12) {
                            value -=12;
                        }
                        return super.formatLabel(value, isValueX) + post;
                    } else {
                        // show currency for y values
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            // toggle between views
            graph.setOnClickListener((View w) -> {
                Log.d(MapsActivity.TAG, "Click on dat mat ");
                graph.removeAllSeries();
                drawDailyAnalyticsGraph(graph, analytics);
            });

        } catch (JSONException e){
            // draw no points
            graph.addSeries(new BarGraphSeries());

        }
    }

    private void drawDailyAnalyticsGraph(GraphView graph, JSONObject analytics) {
        try {
            JSONObject daily  = analytics.getJSONObject("daily");
            Iterator<String> keys = daily.keys();

            int total = analytics.getInt("total");

            List<DataPoint> points = new ArrayList<>();

            while(keys.hasNext()) {
                String key = keys.next();
                int day = Integer.parseInt(key);
                int val = daily.getInt(key);

                double percentVal = (val*1.0)/total;
                points.add( new DataPoint(day, percentVal));
            }
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(6);
            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(points.toArray(new DataPoint[0]));
            graph.addSeries(series);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getGridLabelRenderer().setTextSize(25f);

            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setHorizontalLabels(new String[]{ "Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"});
            graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

            graph.setTitle("Daily Statistics");

            // toggle between views
            graph.setOnClickListener((View w) -> {
                Log.d(MapsActivity.TAG, "Click on dat mat ");
                graph.removeAllSeries();
                drawHourlyAnalyticsGraph(graph, analytics);
            });

        } catch (JSONException e){
            // draw no points
            graph.addSeries(new BarGraphSeries());
        }
    }

    // navigates to parking lot map
    private void goToParkingLot(String id) {
        Log.d(MapsActivity.TAG, "goToParkingLot: " + id);
        Intent intent = new Intent(mContext, ParkingSpotActivity.class);
        mContext.startActivity(intent);
    }
}