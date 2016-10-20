package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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
    private static final String STOCK_PARCEL = "liststock";
    public static final String SYMBOL_PARCEL = "symb";
    private LineChart chart;
    private ArrayList<Stock> stocks;
    private OkHttpClient client;
    private String url;
    private List<Entry> entries = new ArrayList<>();
    private List<String> entriesLabel = new ArrayList<>();
    private XAxis xAxis;
    private YAxis yAxis;

    private String mSymbole;
    private TextView symbolTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSymbole = getIntent().getStringExtra(MyStocksActivity.INTENT_SYMBOLE_EXTRA);

        setContentView(R.layout.activity_stock_item);
        symbolTextView = (TextView) findViewById(R.id.symbolename);
        getSupportActionBar().setTitle(mSymbole);
        symbolTextView.setText(mSymbole);
        symbolTextView.setContentDescription(mSymbole);
        chart = (LineChart) findViewById(R.id.chart);
        client = new OkHttpClient();
        xAxis = chart.getXAxis();
        yAxis = chart.getAxisLeft();
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
                Snackbar.make(findViewById(R.id.activity_stock_item), R.string.symbol_norfound, Snackbar.LENGTH_LONG).show();
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
        outState.putString(SYMBOL_PARCEL, mSymbole);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null || !(savedInstanceState.containsKey(STOCK_PARCEL))) {
            getData();
            mSymbole = getIntent().getStringExtra(MyStocksActivity.INTENT_SYMBOLE_EXTRA);
        } else {
            stocks = savedInstanceState.getParcelableArrayList(STOCK_PARCEL);
            mSymbole = savedInstanceState.getString(SYMBOL_PARCEL);
        }
    }

    private void plotData() {
        // we need to run this coe in other thread because we can't plot
        StockItemActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for (Stock data : stocks) {
                    entriesLabel.add(Utils.convertDate(data.getDate()));
                    float cls = Float.valueOf(data.getClose());
                    entries.add(new Entry(cls, i));
                    i++;
                }
                // Config the chart
                YAxis yAxisR = chart.getAxisRight();
                yAxisR.setDrawLabels(false);
                yAxis.setDrawGridLines(false);
                xAxis.setPosition(XAxisPosition.BOTTOM);
                xAxis.setTextSize(12f);
                YAxis left = chart.getAxisLeft();
                left.setEnabled(true);
                chart.getAxisRight().setEnabled(false);
                chart.getLegend().setTextSize(16f);
                LineDataSet dataSet = new LineDataSet(entries, mSymbole);
                LineData lineData = new LineData(entriesLabel, dataSet);
                chart.setBackgroundColor(Color.WHITE);
                dataSet.setDrawFilled(true);
                dataSet.setDrawValues(true);
                dataSet.setDrawCircles(false);
                chart.setClickable(true);
                dataSet.setFillColor(getResources().getColor(R.color.material_green_700));
                dataSet.setFillAlpha(1000);
                chart.animateX(3000);
                chart.setData(lineData);
            }
        });
    }
}
