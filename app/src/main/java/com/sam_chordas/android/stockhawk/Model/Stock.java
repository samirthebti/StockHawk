package com.sam_chordas.android.stockhawk.Model;

/**
 * Created by Samir Thebti  on 15/10/16.
 * ----->> thebtisam@gmail.com <<-----
 */

import android.os.Parcel;

/**
 * we need the date and close information for ploting the hitorical shart
 */
public class Stock implements android.os.Parcelable {
    private String date;
    private String close;

    public Stock(String date, String close) {
        this.date = date;
        this.close = close;
    }

    public Stock() {
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "date='" + date + '\'' +
                ", close='" + close + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.date);
        dest.writeString(this.close);
    }

    protected Stock(Parcel in) {
        this.date = in.readString();
        this.close = in.readString();
    }

    public static final Creator<Stock> CREATOR = new Creator<Stock>() {
        @Override
        public Stock createFromParcel(Parcel source) {
            return new Stock(source);
        }

        @Override
        public Stock[] newArray(int size) {
            return new Stock[size];
        }
    };
}
