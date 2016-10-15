package com.sam_chordas.android.stockhawk.service;

import com.sam_chordas.android.stockhawk.Model.Stock;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Samir Thebti  on 15/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

public class HistoricalStockIntentService {
    public static final String TAG = HistoricalStockIntentService.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();
    private String url;
    private Response response;
    private ArrayList<Stock> stocks;


    }


