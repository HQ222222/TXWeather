package com.tx.txweather.gson;


public class Basic {
    public String countyName;
    public String weatherId;
    public String provinceName;
    public String cityName;

    @Override
    public String toString() {
        return "Basic{" +
                "countyName='" + countyName + '\'' +
                ", weatherId='" + weatherId + '\'' +
                ", provinceName='" + provinceName + '\'' +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}