package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider.Quotes;

/**
 * Created by Samir Thebti  on 17/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

public class StockWidgetService extends RemoteViewsService {
    public static final String TAG = StockWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "onGetViewFactory: ");
        return new WidgetDataProvider(this.getApplicationContext(), intent);

    }

    public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        private Cursor mCursor;
        private int appWidgetId;

        public WidgetDataProvider(Context context, Intent mIntent) {
            this.mContext = context;
            this.appWidgetId = mIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        }

        @Override
        public void onCreate() {
            Log.d(TAG, "onCreate: ");
            mCursor = getContentResolver().query(Quotes.CONTENT_URI,
                    new String[] {QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[] {"1"},
                    null);
        }

        @Override
        public void onDataSetChanged() {
            mCursor = getContentResolver().query(Quotes.CONTENT_URI,
                    new String[] {QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[] {"1"},
                    null);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

        }

        @Override
        public int getCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
            if (mCursor.moveToPosition(position)) {
                String symbol = mCursor.getString(1);
                remoteViews.setTextViewText(R.id.stock_symbol, symbol);
                remoteViews.setTextViewText(R.id.bid_price, mCursor.getString(2));
                remoteViews.setTextViewText(R.id.change, mCursor.getString(3));
                if (mCursor.getInt(4) == 1) {
                    remoteViews.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    remoteViews.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }
                Bundle bundle = new Bundle();
                Log.d(TAG, "---------------------------> " + symbol);
                bundle.putString(StockWidgetProvider.EXTRA_SYMBOL, symbol);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                remoteViews.setOnClickFillInIntent(R.id.list_item_quote, intent);


            }
            return remoteViews;

        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return mCursor.getInt(0);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
