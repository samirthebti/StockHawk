package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.InputCallback;
import com.facebook.stetho.Stetho;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.QuoteProvider.Quotes;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.widget.StockWidgetProvider;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String INTENT_SYMBOLE_EXTRA = "symbole";
    public static final int SYMBOL_LENGHT = 6;


    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    private CoordinatorLayout coordinatorLayout;
    private int symbole;
    private RecyclerView recyclerView;
    private TextView emptyTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_my_stocks);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        mServiceIntent = new Intent(this, StockIntentService.class);
        mServiceIntent.putExtra("tag", "init");
        if (savedInstanceState == null) {
            if (Utils.isNetworkAvailable(mContext)) {
                startService(mServiceIntent);
            } else {
                Utils.setNetworkStatus(this, StockTaskService.STATUS_NO_NETWORK);
            }
            Stetho.initializeWithDefaults(this);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {

                        if (mCursor.moveToPosition(position)) {
                            symbole = mCursor.getColumnIndex(QuoteColumns.SYMBOL);
                            String currentSymbol = mCursor.getString(symbole);
                            Intent intent = new Intent(MyStocksActivity.this, StockItemActivity.class);
                            intent.putExtra(INTENT_SYMBOLE_EXTRA, currentSymbol);
                            startActivity(intent);
                        }

                    }
                }));

        recyclerView.setAdapter(mCursorAdapter);
        setEmptyView();


        final android.support.design.widget.FloatingActionButton fab = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(mContext)) {
                    new Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    String inputString = input.toString();

                                    // check  Stock input format
                                    if (inputString.length() <= SYMBOL_LENGHT && Utils.inputFormatterChecker(inputString)) {

                                        Cursor c = getContentResolver().query(Quotes.CONTENT_URI,
                                                new String[] {QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                                new String[] {inputString.toLowerCase()}, null);

                                        if (c.getCount() != 0) {

                                            Snackbar.make(coordinatorLayout, R.string.stock_already_saved,
                                                    Snackbar.LENGTH_LONG).show();
//
                                            return;
                                        } else {
                                            mServiceIntent.putExtra("tag", getString(R.string.add));
                                            mServiceIntent.putExtra("symbol", inputString);
                                            startService(mServiceIntent);
                                            Snackbar.make(coordinatorLayout, inputString + getString(R.string.stock_added),
                                                    Snackbar.LENGTH_LONG).
                                                    show();
                                        }
                                    } else {
                                        Snackbar.make(coordinatorLayout, R.string.stock_size_msg,
                                                Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .show();
                } else {
                    fab.setActivated(false);

                    Snackbar.make(coordinatorLayout, getString(R.string.string_status_no_network), Snackbar.LENGTH_LONG).show();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (Utils.isNetworkAvailable(mContext)) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = getString(R.string.periodic);

            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();

            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        setEmptyView();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {

            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[] {QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[] {"1"},
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        setEmptyView();
        updateStocksWidget();
    }


    @Override
    protected void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
        super.onDestroy();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
        setEmptyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(this.getString(R.string.status_shared_pref))) {
            setEmptyView();
        }
    }

    private void setEmptyView() {
        if (mCursorAdapter.getItemCount() <= 0) {
            emptyTextView = (TextView) findViewById(R.id.emptydisplay);
            String message = getString(R.string.empty_stock_list);
            @StockTaskService.StockStatuses
            int status = Utils.getNetworkStatus(this);
            switch (status) {
                case StockTaskService.STATUS_OK:
                    message += getString(R.string.string_status_ok);
                    break;

                case StockTaskService.STATUS_NO_NETWORK:
                    message += getString(R.string.string_status_no_network);
                    break;

                case StockTaskService.STATUS_ERROR_JSON:
                    message += getString(R.string.string_error_json);
                    break;

                case StockTaskService.STATUS_SERVER_DOWN:
                    message += getString(R.string.string_server_down);
                    break;

                case StockTaskService.STATUS_SERVER_ERROR:
                    message += getString(R.string.string_error_server);
                    break;

                case StockTaskService.STATUS_UNKNOWN:
                    message += getString(R.string.string_status_unknown);
                    break;
                default:
                    break;

            }
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(message);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            symbole = mCursor.getColumnIndex(QuoteColumns.SYMBOL);

        }

    }

    private void updateStocksWidget() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext.getApplicationContext());
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));
        if (ids.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget);
        }
    }


}

