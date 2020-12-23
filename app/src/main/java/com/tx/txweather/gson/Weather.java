package com.tx.txweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public Basic basic;

    public AQI aqi;

    public Now now;

    public List<Suggestion> suggestionList;

    public List<Forecast> forecastList;

    @Override
    public String toString() {
        return "Weather{" +
                ", basic=" + basic +
                ", aqi=" + aqi +
                ", now=" + now +
                ", suggestionList=" + suggestionList +
                ", forecastList=" + forecastList +
                '}';
    }
}