package com.tx.txweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    public String temperature;

    public String info;

    public String windDir;

    public String windScale;

    public String windSpeed;

    public String humidity;

    public String precipitation;

    public String pressure;

    public String visibility;

    public String cloud;

    @Override
    public String toString() {
        return "Now{" +
                "temperature='" + temperature + '\'' +
                ", info='" + info + '\'' +
                ", windDir='" + windDir + '\'' +
                ", windScale='" + windScale + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                ", humidity='" + humidity + '\'' +
                ", precipitation='" + precipitation + '\'' +
                ", pressure='" + pressure + '\'' +
                ", visibility='" + visibility + '\'' +
                ", cloud='" + cloud + '\'' +
                '}';
    }
}