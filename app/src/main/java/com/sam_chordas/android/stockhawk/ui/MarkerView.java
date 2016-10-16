package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Samir Thebti  on 16/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

public class MarkerView extends com.github.mikephil.charting.components.MarkerView {

    private TextView stockTextView;
//    private TextView dateTextView;


    public MarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        // find your layout components
        stockTextView = (TextView) findViewById(R.id.stock);
//        stockTextView = (TextView) findViewById(R.id.date);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        stockTextView.setText("" + e.getY()); // set the entry-value as the display text
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }

}
