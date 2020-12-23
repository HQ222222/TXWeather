package com.tx.txweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;
    public String info;
    public String maxTmp;
    public String minTmp;

    @Override
    public String toString() {
        return "Forecast{" +
                "date='" + date + '\'' +
                ", info='" + info + '\'' +
                ", maxTmp='" + maxTmp + '\'' +
                ", minTmp='" + minTmp + '\'' +
                '}';
    }
}