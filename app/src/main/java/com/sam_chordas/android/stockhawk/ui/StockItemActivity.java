package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.Model.Stock;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockItemActivity extends AppCompatActivity {
    public static final String TAG = StockItemActivity.class.getSimpleName();
    private LineChart chart;
    private ArrayList<Stock> stocks;
    private OkHttpClient client;
    private String url;
    private List<Entry> entries = new ArrayList<>();
    private List<String> entriesLabel = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_item);
        chart = (LineChart) findViewById(R.id.chart);
        client = new OkHttpClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    private void getData() {

        try {
            url = Utils.builHistoricalRequest("FB", "2012-09-11", "2014-09-11");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
                stocks = Utils.getHestoricalData(response.body().string());
                plotData();
            }
        });
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
                    entriesLabel.add(data.getDate().substring(0, 3));
                    entries.add(new Entry(i, Float.valueOf(data.getClose())));
                    Log.d(TAG, "run: " + data.getClose().toString());
                    i++;
                }
                LineDataSet dataSet = new LineDataSet(entries, null);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(getResources().getColor(R.color.material_green_700));
                dataSet.setFillAlpha(1000);
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                XAxis xAxis = chart.getXAxis();
                YAxis yAxis = chart.getAxisLeft();
                YAxis yAxisR = chart.getAxisRight();
                yAxisR.setDrawLabels(false);
                yAxis.setDrawGridLines(false);
                xAxis.setPosition(XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                xAxis.setTextColor(Color.RED);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);
// set a custom value formatter
//                xAxis.setValueFormatter(entriesLabel);
                chart.invalidate();
            }
        });
    }
}
