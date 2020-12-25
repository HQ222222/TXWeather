package com.tx.txweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.tx.txweather.R;
import com.tx.txweather.gson.Forecast;
import com.tx.txweather.gson.Now;
import com.tx.txweather.gson.Suggestion;
import com.tx.txweather.gson.Weather;
import com.tx.txweather.service.AutoUpdateService;
import com.tx.txweather.utils.HttpUtil;
import com.tx.txweather.utils.Utility;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;
    public static final String KEY = "3be5a1e2fa7c40d3aed9b8bfa17f0ab6";
    public static final String API_URL = "http://47.94.144.190:8085/v1/api/weather/";
    private ScrollView weatherLayout;
    private Button navButton;
    private TextView titleCity;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private LinearLayout suggestionLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private ImageView bingPicImg;
    private String mWeatherId;
    private TextView mTvWindSpeed;
    private TextView mTvHumidity;
    private TextView mTvVisibility;
    private TextView mTvCloud;
    private TextView mTvPrecipitation;
    private TextView mTvPressure;
    private TextView mTvWindDirection;
    private TextView mTvUpdateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        bingPicImg = findViewById(R.id.bing_pic_img);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        suggestionLayout = findViewById(R.id.suggestion_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        mTvWindSpeed = findViewById(R.id.tv_wind_speed);
        mTvHumidity = findViewById(R.id.tv_humidity);
        mTvVisibility = findViewById(R.id.tv_visibility);
        mTvCloud = findViewById(R.id.tv_cloud);
        mTvPrecipitation = findViewById(R.id.tv_precipitation);
        mTvPressure = findViewById(R.id.tv_pressure);
        mTvWindDirection = findViewById(R.id.tv_wind_direction);
        mTvUpdateTime = findViewById(R.id.tv_update_time);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(() -> requestWeather(mWeatherId));
        navButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdw = new SimpleDateFormat("MM月dd日");

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.countyName;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        Log.d("weather", "showWeatherInfo:-->"+weather);
        titleCity.setText(cityName);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        Now now = weather.now;
        mTvWindDirection.setText(now.windDir+now.windScale+"级");
        mTvWindSpeed.setText(now.windSpeed+"km/h");
        mTvHumidity.setText(now.humidity+"%");
        mTvVisibility.setText(now.visibility+"km");
        mTvCloud.setText(now.cloud+"%");
        mTvPrecipitation.setText(now.precipitation+"mm");
        mTvPressure.setText(now.pressure+"hPa");
        mTvUpdateTime.setText(now.updateTime);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            try {
                forecast.date = sdw.format(sd.parse(forecast.date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateText.setText(forecast.date);
            infoText.setText(forecast.info);
            maxText.setText(forecast.maxTmp + "℃");
            minText.setText(forecast.minTmp + "～");
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.aqi);
            pm25Text.setText(weather.aqi.pm25);
        }
        suggestionLayout.removeAllViews();
        for (Suggestion suggestion : weather.suggestionList) {
            View view = LayoutInflater.from(this).inflate(R.layout.suggestion_item, suggestionLayout, false);
            TextView mSuggestionName = view.findViewById(R.id.suggestion_name);
            TextView mSuggestionInfo = view.findViewById(R.id.suggestion_info);

            mSuggestionName.setText(suggestion.name);
            mSuggestionInfo.setText(suggestion.info);
            suggestionLayout.addView(view);
        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    public void requestWeather(final String weatherId) {
        String weatherNowUrl = API_URL+weatherId;
        HttpUtil.getSync(weatherNowUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }


    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.getSync(requestBingPic, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }


}
