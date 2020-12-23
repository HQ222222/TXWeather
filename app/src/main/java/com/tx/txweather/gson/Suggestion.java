package com.tx.txweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    public String name;
    public String info;
    public String category;

    @Override
    public String toString() {
        return "Suggestion{" +
                "name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
