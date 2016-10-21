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
    private String company_name;
    private String previewClose;

    public Stock(String date, String close, String company_name, String previewClose) {
        this.date = date;
        this.close = close;
        this.company_name = company_name;
        this.previewClose = previewClose;
    }

    public Stock() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getPreviewClose() {
        return previewClose;
    }

    public void setPreviewClose(String previewClose) {
        this.previewClose = previewClose;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.date);
        dest.writeString(this.close);
        dest.writeString(this.company_name);
        dest.writeString(this.previewClose);
    }

    protected Stock(Parcel in) {
        this.date = in.readString();
        this.close = in.readString();
        this.company_name = in.readString();
        this.previewClose = in.readString();
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
