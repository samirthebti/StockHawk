package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.StockItemActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidgetProvider extends AppWidgetProvider {
    public static final String INTENT_ACTION = "com.sam_chordas.stockhawk.widget.StockWidgetProvider.INTENT_ACTION";
    public static final String TAG = StockWidgetProvider.class.getSimpleName();
    public static final String EXTRA_SYMBOL = "com.sam_chordas.stockhawk.widget.StockWidgetProvider.EXTRA_SYMBOL";


    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(INTENT_ACTION)) {
            String symbol = intent.getStringExtra(EXTRA_SYMBOL);
            Intent showHistoricalData = new Intent(context, StockItemActivity.class);
            showHistoricalData.putExtra(MyStocksActivity.INTENT_SYMBOLE_EXTRA, symbol);
            showHistoricalData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showHistoricalData);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);

            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.widget_list, intent);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);
            Intent openSymbol = new Intent(context, StockWidgetProvider.class);
            openSymbol.setAction(StockWidgetProvider.INTENT_ACTION);

            openSymbol.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, openSymbol,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget_list, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }
}

