package com.tx.txweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tx.txweather.R;
import com.tx.txweather.db.City;
import com.tx.txweather.db.County;
import com.tx.txweather.db.Province;
import com.tx.txweather.utils.HttpUtil;
import com.tx.txweather.utils.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ChooseAreaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressBar mProgressBar;

    private TextView mTitleText;
    private ImageButton mBackButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    private Province selectedProvice;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        // Inflate the layout for this fragment
        mTitleText = view.findViewById(R.id.title);
        mBackButton = view.findViewById(R.id.back);
        mListView = view.findViewById(R.id.list);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id){
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvice = mProvinceList.get(pos);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCityList.get(pos);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = mCountyList.get(pos).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        mTitleText.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = LitePal.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province province : mProvinceList) {
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        mTitleText.setText(selectedProvice.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCityList = LitePal.where("provinceId = ?", String.valueOf(selectedProvice.getId())).find(City.class);
        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city : mCityList) {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvice.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }

    }

    private void queryCounties() {
        mTitleText.setText(selectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = LitePal.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (mCountyList.size() > 0) {
            dataList.clear();
            for (County county : mCountyList) {
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvice.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }
    private void queryFromServer(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();

                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvice.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //closeProgressBar();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                //通过
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //closeProgressBar();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}