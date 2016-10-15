package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

/**
 * Created by Samir Thebti  on 15/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

public class HistoricalStockIntentService extends IntentService {
    public static final String TAG = HistoricalStockIntentService.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();
    private String url;
    private Response response;

    public HistoricalStockIntentService() {
        super("HistoricalStockIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {



    }
}
