package com.example.myweather;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myweather.server.Server;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

//фрагмент yandex.Погоды
public class YandexWeatherFragment extends Fragment implements WeatherFragment{

    YandexWeatherAdapter yandexWeatherAdapter;
    View fragmentView;
    private final Handler handler = new YandexWeatherHandler(this);
    private static final String SERV_PARAM = "server";
    private Server server;
    private View.OnClickListener addServer;

    //переписанный адаптер для ListView в связи с использованием времени суток
    class ListAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private ArrayList<WeatherDay[]> arrayMyData;

        public ListAdapter (){
            this.layoutInflater = null;
            this.arrayMyData = null;
        }

        public ListAdapter (Context ctx) {
            layoutInflater = LayoutInflater.from(ctx);
        }

        public ListAdapter setContext(Context ctx){
            layoutInflater = LayoutInflater.from(ctx);
            return this;
        }

        public LayoutInflater getLayoutInflater() throws NullPointerException {
            if (layoutInflater == null)
                throw new NullPointerException("ListAdapter<T> -> layoutInflater -> null");
            return layoutInflater;
        }

        public ArrayList<WeatherDay[]> getArrayMyData() throws NullPointerException {
            if (arrayMyData == null)
                throw new NullPointerException("ListAdapter<T> -> arrayMyData -> null");
            return arrayMyData;
        }

        public ListAdapter setArrayMyData(ArrayList<WeatherDay[]> arrayMyData) {
            this.arrayMyData = arrayMyData;
            return this;
        }

        public int getCount () {
            return arrayMyData.size();
        }

        public Object getItem (int position) {
            return getArrayMyData().get(position);
        }

        public long getItemId (int position) {
            WeatherDay[] ob = getArrayMyData().get(position);
            if (ob != null) {
                return position;
            }
            return -1;
        }

        private void setValueInView(View convertView, int dateRes, int dayOfTheWeekRes, int tempRes,
                                    int feelsLikeRes, int windRes, int pressureRes, int humidityRes,
                                    WeatherDay partDay){
            ((TextView) convertView.findViewById(dateRes)).setText(partDay.getDate());
            ((TextView) convertView.findViewById(dayOfTheWeekRes)).setText(partDay.getDayOfTheWeek());

            ((TextView) convertView.findViewById(tempRes)).setText(
                    String.format("%s/%s", partDay.getTempMin(), partDay.getTempMax()));
            ((TextView) convertView.findViewById(feelsLikeRes)).setText(partDay.getFeelsLike());
            ((TextView) convertView.findViewById(windRes)).setText(partDay.getWind());
            ((TextView) convertView.findViewById(pressureRes)).setText(partDay.getPressure());
            ((TextView) convertView.findViewById(humidityRes)).setText(partDay.getHumidity());
        }

        public View getView(int position, View convertView, ViewGroup parent){
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_yandex_weather, null);

            getArrayMyData();
            WeatherDay[] curDay = getArrayMyData().get(position);
            TimesOfDay[] timesOfDays = TimesOfDay.values();
            for (int i = 0; i < curDay.length; i++) {
                WeatherDay partDay = curDay[i];

                if (timesOfDays[i] == TimesOfDay.MORNING) {
                    setValueInView(convertView, R.id.twDate, R.id.twDayOfTheWeek, R.id.twMorningTemp,
                            R.id.twMorningFeelsLike, R.id.twMorningWind, R.id.twMorningPressure,
                            R.id.twMorningHumidity, partDay);
                } else if (timesOfDays[i] == TimesOfDay.AFTERNOON){
                    setValueInView(convertView, R.id.twDate, R.id.twDayOfTheWeek, R.id.twAfternoonTemp,
                            R.id.twAfternoonFeelsLike, R.id.twAfternoonWind, R.id.twAfternoonPressure,
                            R.id.twAfternoonHumidity, partDay);
                } else if (timesOfDays[i] == TimesOfDay.EVENING){
                    setValueInView(convertView, R.id.twDate, R.id.twDayOfTheWeek, R.id.twEveningTemp,
                            R.id.twEveningFeelsLike, R.id.twEveningWind, R.id.twEveningPressure,
                            R.id.twEveningHumidity, partDay);
                } else if (timesOfDays[i] == TimesOfDay.NIGHT) {
                    setValueInView(convertView, R.id.twDate, R.id.twDayOfTheWeek, R.id.twNightTemp,
                            R.id.twNightFeelsLike, R.id.twNightWind, R.id.twNightPressure,
                            R.id.twNightHumidity, partDay);
                }
            }

            Log.i("Element", "Добавлен элемент");
            convertView.setMinimumHeight(fragmentView.getHeight());
            return convertView;
        }
    }

    ListAdapter myAdapter = new ListAdapter();

    public YandexWeatherFragment() {
        yandexWeatherAdapter = new YandexWeatherAdapter(handler);
    }

    public static YandexWeatherFragment newInstance(Server server) {
        YandexWeatherFragment fragment = new YandexWeatherFragment();
        Bundle args = new Bundle();
        args.putSerializable(SERV_PARAM, server);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListnerBtAddServer(View.OnClickListener addServer) {
        this.addServer = addServer;
    }

    public boolean isNullableServer() {
        return server == null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("YandexFragment", "onCreateView");
        if (getArguments() != null) {
            server = (Server) getArguments().getSerializable(SERV_PARAM);
        }
        if (server == null) {
            fragmentView = inflater.inflate(R.layout.fragment_empty_weather, null);
            fragmentView.findViewById(R.id.resolveBt).setOnClickListener(addServer);
        } else if (!yandexWeatherAdapter.isRun() &&
                !server.getUrl().equals(yandexWeatherAdapter.getUrl())) {
            yandexWeatherAdapter.setUrl(server.getUrl());
            yandexWeatherAdapter.start();
            fragmentView = inflater.inflate(R.layout.fragment_yandex_weather, null);
        }
        return fragmentView;
    }

    public void setData() {
        ListView list = fragmentView.findViewById(R.id.listYandexWeather);
        if (list.getAdapter() == null) {
            list.setAdapter(
                    myAdapter
                            .setContext(fragmentView.getContext())
                            .setArrayMyData(yandexWeatherAdapter.getWeatherDays())
            );
        }
        else{
            myAdapter.setArrayMyData(yandexWeatherAdapter.getWeatherDays());
            myAdapter.notifyDataSetChanged();
        }
    }

    public Server getServer() {
        return server;
    }

    //обработчик с использованием слабой связности
    private static class YandexWeatherHandler extends Handler {
        private final WeakReference<YandexWeatherFragment> mFragment;

        YandexWeatherHandler(YandexWeatherFragment activity) {
            mFragment = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            YandexWeatherFragment fragment = mFragment.get();
            if (mFragment != null) {
                fragment.setData();
            }
            Log.i("Update", "Finish");
        }
    }

}
