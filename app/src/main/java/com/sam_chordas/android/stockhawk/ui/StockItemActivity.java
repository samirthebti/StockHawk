package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.HistoricalStockIntentService;

public class StockItemActivity extends AppCompatActivity  {
    private Intent mServiceIntent;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_item);
        chart = (LineChart) findViewById(R.id.chart);
        mServiceIntent = new Intent(this, HistoricalStockIntentService.class);
        startService(mServiceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
}
