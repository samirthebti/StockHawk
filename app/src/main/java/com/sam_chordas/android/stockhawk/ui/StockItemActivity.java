package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.sam_chordas.android.stockhawk.Model.Stock;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.utils.HourAxisValueFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockItemActivity extends AppCompatActivity {
    public static final String TAG = StockItemActivity.class.getSimpleName();
    private static final String STOCK_PARCEL = "liststock";
    private LineChart chart;
    private ArrayList<Stock> stocks;
    private OkHttpClient client;
    private String url;
    private List<Entry> entries = new ArrayList<>();
    private List<String> entriesLabel = new ArrayList<>();
    private XAxis xAxis;
    private YAxis yAxis;
    private MarkerView mv;
    private String mSymbole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mSymbole = bundle.getString("symbole");
        Log.d(TAG, "onCreate: " + mSymbole);
        setContentView(R.layout.activity_stock_item);
        chart = (LineChart) findViewById(R.id.chart);
        client = new OkHttpClient();
        xAxis = chart.getXAxis();
        yAxis = chart.getAxisLeft();
        mv = new MarkerView(this, R.layout.layout_marker);
        getData();
    }


    private void getData() {

        try {
            url = Utils.builHistoricalRequest(mSymbole);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Snackbar.make(findViewById(R.id.activity_stock_item), "Erreur while fetch Date", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                stocks = Utils.getHestoricalData(response.body().string());
                plotData();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArrayList(STOCK_PARCEL, (ArrayList<? extends Parcelable>) stocks);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null || !(savedInstanceState.containsKey(STOCK_PARCEL))) {
            getData();
        } else if (savedInstanceState != null || savedInstanceState.containsKey(STOCK_PARCEL)) {
            stocks = savedInstanceState.getParcelableArrayList(STOCK_PARCEL);
        }
    }

    private void plotData() {

        // we need to run this coe in other thread becouase we can't plot
        StockItemActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float i = 0;
                for (Stock data : stocks) {
                    Log.d(TAG, "onCreate: " + data.toString());
                    //turn your data into Entry objects
                    entriesLabel.add(data.getDate());
                    entries.add(new Entry(i, Float.valueOf(data.getClose())));
                    Log.d(TAG, "run: " + data.getClose().toString());
                    i++;
                }
                // Config the chart
                LineDataSet dataSet = new LineDataSet(entries, null);
                dataSet.setHighlightEnabled(false);
                dataSet.setDrawHighlightIndicators(true);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(getResources().getColor(R.color.material_green_700));
                dataSet.setFillAlpha(1000);
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                YAxis yAxisR = chart.getAxisRight();
                yAxisR.setDrawLabels(false);
                yAxis.setDrawGridLines(false);
                xAxis.setPosition(XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                yAxisR.setTextColor(Color.RED);
                xAxis.setTextColor(Color.RED);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);
                AxisValueFormatter xAxisFormatter = null;
                try {
                    xAxisFormatter = new HourAxisValueFormatter(Utils.dateToTimestamp(stocks.get(0).getDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // Setup the marker

                chart.setMarkerView(mv);
                xAxis.setValueFormatter(xAxisFormatter);
                chart.animateX(2000);
                if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                    chart.setElevation(0.2f);
                }

                chart.invalidate();
            }
        });
    }
}
