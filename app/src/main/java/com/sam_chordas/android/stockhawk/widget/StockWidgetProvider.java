package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockItemActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidgetProvider extends AppWidgetProvider {
    public static final String INTENT_ACTION = "com.sam_chordas.stockhawk.widget.StockWidgetProvider.INTENT_ACTION";
    public static final String EXTRA_SYMBOL = "com.sam_chordas.stockhawk.widget.StockWidgetProvider.EXTRA_SYMBOL";
    public static final String TAG = StockWidgetProvider.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(Utils.ACTION_DATA_UPDATED)) {
            Log.d(TAG, "onReceive: ");
            /**
             * Matches our own created intent, and thus helps in showing data over time.
             * Can add other methods later too in the same receiver.
             */

            String symbol = intent.getStringExtra(INTENT_ACTION);
            Intent showHistoricalData = new Intent(context, StockItemActivity.class);
            showHistoricalData.putExtra("symbol_name", symbol);
            showHistoricalData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showHistoricalData);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d(TAG, "onUpdate: ");
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);

            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.widget_list, intent);
            remoteViews.setEmptyView(R.id.widget, R.id.widget_empty);


            Intent openSymbol = new Intent(context, StockWidgetProvider.class);
            openSymbol.setAction(StockWidgetProvider.INTENT_ACTION);
            openSymbol.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, openSymbol,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);


        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, StockWidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views ,int appWidgetIds) {
        views.setRemoteAdapter(appWidgetIds, R.id.widget_list,
                new Intent(context, StockWidgetService.class));
    }
}
