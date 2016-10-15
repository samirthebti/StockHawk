package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;

import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
        try {
            url = Utils.builHistoricalRequest("FB", "2012-09-11", "2014-09-11");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            response = client.newCall(request).execute();
            Utils.getHestoricalData(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
