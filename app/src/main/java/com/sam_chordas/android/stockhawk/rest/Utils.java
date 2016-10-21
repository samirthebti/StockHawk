package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sam_chordas.android.stockhawk.Model.Stock;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.service.StockTaskService.StockStatuses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;


import static com.sam_chordas.android.stockhawk.service.StockTaskService.STATUS_OK;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();
    public static final String BASE_API_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    public static final String ACTION_DATA_UPDATED = "com.sam_chordas.Stockhawk.ACTION_DATA_UPDATED";

    public static final String FINALISER_API_URL = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
            + "org%2Falltableswithkeys&callback=";
    private static final String JSON_SERIES = "series";
    private static final String JSON_DATE = "Date";
    private static final String JSON_CLOSE = "close";
    private static final String JSON_COMP_NAME = "Company-Name";
    private static final String JSON_PREV_CLOSE = "Company-Name";
    public static final String NULL = "null";
    public static final String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
    public static final String END_URL = "/chartdata;type=quote;range=1y/json";

    public static int randomColors() {
        Random rand = new Random();
        int r = rand.nextInt(200);
        int g = rand.nextInt(200);
        int b = rand.nextInt(200);
        return Color.rgb(r, g, b);
    }

    public static String builHistoricalRequest(@NonNull String symbol) throws UnsupportedEncodingException {
        String stringBuilder = null;
        stringBuilder = BASE_URL + symbol + END_URL;
        return stringBuilder;
    }


    public static boolean showPercent = true;

    public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(String JSON) throws JSONException {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        ContentProviderOperation operation = null;

        jsonObject = new JSONObject(JSON);

        if (jsonObject != null && jsonObject.length() != 0) {
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1) {
                jsonObject = jsonObject.getJSONObject("results")
                        .getJSONObject("quote");
                operation = buildBatchOperation(jsonObject);
                if (operation != null) {
                    batchOperations.add(operation);
                }
            } else {
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        operation = buildBatchOperation(jsonObject);
                        if (operation != null) {
                            batchOperations.add(operation);
                        }
                    }
                }
            }
        }

        return batchOperations;
    }

    public static ArrayList<Stock> getHestoricalData(String json) {
        ArrayList<Stock> stocks = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            //we need to remove first and last lines of json response
            String jsonGlobalObject = json.substring(json.indexOf("(") + 1, json.lastIndexOf(")"));

            jsonObject = new JSONObject(jsonGlobalObject);
            resultsArray = jsonObject.getJSONArray(JSON_SERIES);

            if (resultsArray != null && resultsArray.length() != 0) {
                for (int i = 0; i < resultsArray.length(); i++) {
                    Stock stock = new Stock();
                    jsonObject = resultsArray.getJSONObject(i);
                    stock.setDate(jsonObject.getString(JSON_DATE));
                    stock.setClose(jsonObject.getString(JSON_CLOSE));
                    stocks.add(stock);
                }
            }
        } catch (
                JSONException e
                )

        {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }

        return stocks;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws JSONException {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);

        if (!(jsonObject.getString("Change").equals(NULL)) && !(jsonObject.getString("Bid").equals(NULL))) {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } else {
            return null;
        }
        return builder.build();
    }

    public static String convertDate(@NonNull String s) {
        StringBuilder outputFormattedDate = new StringBuilder();
        outputFormattedDate.append(s.substring(6))
                .append("/")
                .append(s.substring(4, 6))
                .append("/")
                .append(s.substring(2, 4));
        return outputFormattedDate.toString();
    }

    //check the network is available
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void setNetworkStatus(Context context, @StockStatuses int stockStatus) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.status_shared_pref), stockStatus);
        editor.apply();
    }

    @SuppressWarnings("ResourceType")
    static public
    @StockTaskService.StockStatuses
    int getNetworkStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(R.string.status_shared_pref), STATUS_OK);
    }

    public static boolean inputFormatterChecker(@NonNull String s) {
        return s.matches("[a-zA-Z]+");
    }
}
