package com.sam_chordas.android.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

/**
 * Created by Samir Thebti  on 15/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

public class Label implements AxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return null;
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
