package com.example.fibro;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    TextView text;
    ImageView icon;
    FloatingActionButton fab;
    public static SharedPreferences prefs;
    Calendar calendar = Calendar.getInstance();
    public static Context context;
    LineChart graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        prefs = getSharedPreferences("days", MODE_PRIVATE);
        //prefs.edit().clear().commit();
        Days.getInstance().fetchDaysFromPrefs();    //Get Days from SharedPreferences

        text = findViewById(R.id.weatherText);
        icon = findViewById(R.id.weatherIcon);
        fab = findViewById(R.id.fab);
        graph = findViewById(R.id.graph);

        //GPSTracker gps = new GPSTracker(this);
        refreshWeather();
        //ThreadService.enqueueWork(this, getIntent());


    }


    public void refreshWeather()
    {Weather weather = new Weather();
        weather.refresh();
        Log.d("PRESSURE AFTER THREAD", weather.getPressure());
        if(weather.isLowPressure() == true){
            text.setText("On matalapainetta!");
            icon.setImageResource(R.drawable.very_sad_emoticon);
        }else {
            text.setText("Ei matalapainetta");
            icon.setImageResource(R.drawable.awesome_emoticon);
        }
        Log.i("FIBRO",weather.getPressure());
        DayCreator.getInstance().setPressure(weather.pressure);
    }
    public void fabPressed(View view){
        boolean alreadyLoggedToday = false;
        for(Day d : Days.getInstance().getDays()){
            if(d.date.toString().equals(new java.sql.Date(System.currentTimeMillis()).toString())){
                alreadyLoggedToday = true;
            }
        } Log.d("AlreadyLogged", String.valueOf(alreadyLoggedToday));
        if(alreadyLoggedToday == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Haluatko korvata tiedot uusilla?")
                    .setTitle("Olet jo kirjannut tämän päivän tiedot");
            builder.setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(MainActivity.context, MoodSelector.class);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("En", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            Intent intent = new Intent(this, MoodSelector.class);
            startActivity(intent);
        }

    }
    public void graphPressed(View view) {
        Intent intent = new Intent(this, GraphDetailActivity.class);
        startActivity(intent);
    }

    public void clear(View v){
        prefs.edit().clear().commit();
    }

    @Override
    protected void onResume(){
        XAxis x = graph.getXAxis();
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                SimpleDateFormat sdp = new SimpleDateFormat("dd-MM");
                return sdp.format(new Date(new Float(value).longValue()));
            }
        });

       // x.setGranularity(1f);
        //x.setGranularityEnabled(true);
        x.setCenterAxisLabels(false);
        x.setXOffset(-10);
       // x.setLabelCount(Graph.getInstance().entries.size()+2,true);
        x.setDrawGridLines(false);
        //x.setAxisMaximum(Days.getInstance().getHighestX());
        //x.setAxisMinimum(Days.getInstance().getLowestX());
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis y = graph.getAxisLeft();
        y.setLabelCount(5);
        y.setDrawGridLinesBehindData(false);
        graph.getAxisRight().setEnabled(false);
        //Graph.getInstance().addDaysToSeries(); //Add DataPoints to series
        Graph.getInstance().init();
        graph.setDrawGridBackground(false);
        graph.setData(Graph.getInstance().lineData);
        graph.invalidate();

        super.onResume();
    }



}