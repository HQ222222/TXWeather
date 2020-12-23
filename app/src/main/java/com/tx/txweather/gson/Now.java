package com.tx.txweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    public String temperature;

    public String info;

    @Override
    public String toString() {
        return "Now{" +
                "temperature='" + temperature + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}