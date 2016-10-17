package com.sam_chordas.android.stockhawk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Samir Thebti  on 17/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

public class NoStockService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "No Symbole Found", Toast.LENGTH_LONG).show();
    }
}
