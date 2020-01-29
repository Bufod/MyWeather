package com.example.myweather;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myweather.server.Server;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

//фрагмент weather.com
public class WeatherComFragment extends Fragment implements WeatherFragment {
    WeatherComAdapter weatherComAdapter;
    View fragmentView;
    private final Handler handler = new WeatherComHandler(this);
    private static final String SERV_PARAM = "server";
    private Server server;
    private View.OnClickListener addServer;

    ListAdapter<WeatherDay> myAdapter = new ListAdapter<WeatherDay>() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_weather_com, null);
            WeatherDay curDay = getArrayMyData().get(position);
            ((TextView) convertView.findViewById(R.id.date)).setText(curDay.getDate());
            ((TextView) convertView.findViewById(R.id.dayOfTheWeek)).setText(curDay.getDayOfTheWeek());
            ((TextView) convertView.findViewById(R.id.temp)).setText(
                    String.format("%s/%s", curDay.getTempMin(), curDay.getTempMax()));
            ((TextView) convertView.findViewById(R.id.description)).setText(curDay.getDescription());
            ((TextView) convertView.findViewById(R.id.precip)).setText(curDay.getPrecip());
            ((TextView) convertView.findViewById(R.id.wind)).setText(curDay.getWind());
            ((TextView) convertView.findViewById(R.id.humidity)).setText(curDay.getHumidity());
            Log.i("Element", "Добавлен элемент");

            WebView webView = convertView.findViewById(R.id.icon);
            String ans = getString(R.string.htmlWeathearCom);
            ans = ans.replace(String.format("#%s:%s#", "icon", "String"),
                    curDay.getIcon());
            String encodedHtml = Base64.encodeToString(ans.getBytes(StandardCharsets.UTF_8), Base64.NO_PADDING);
            webView.loadData(encodedHtml , "text/html", "base64");
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
            convertView.setMinimumHeight(fragmentView.getHeight());
            return convertView;
        }
    };

    public WeatherComFragment() {
        weatherComAdapter = new WeatherComAdapter(handler);
    }

    public void setListnerBtAddServer(View.OnClickListener addServer) {
        this.addServer = addServer;
    }

    public Server getServer() {
        return server;
    }

    public boolean isNullableServer() {
        return server == null;
    }

    public static WeatherComFragment newInstance(Server server) {
        WeatherComFragment fragment = new WeatherComFragment();
        Bundle args = new Bundle();
        args.putSerializable(SERV_PARAM, server);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("WeatherFragment", "onCreateView");
        if (getArguments() != null) {
            server = (Server) getArguments().getSerializable(SERV_PARAM);
        }
        if (server == null) {
            fragmentView = inflater.inflate(R.layout.fragment_empty_weather, null);
            fragmentView.findViewById(R.id.resolveBt).setOnClickListener(addServer);
        } else if (!weatherComAdapter.isRun() &&
                !server.getUrl().equals(weatherComAdapter.getUrl())) {
            weatherComAdapter.setUrl(server.getUrl());
            weatherComAdapter.start();
            fragmentView = inflater.inflate(R.layout.fragment_weather_com, null);
        }
        return fragmentView;
    }

    public void setData() {
        ListView list = fragmentView.findViewById(R.id.listWeatherCom);

        if (list.getAdapter() == null) {
            list.setAdapter(
                    myAdapter
                            .setContext(fragmentView.getContext())
                            .setArrayMyData(weatherComAdapter.getWeatherDays())
            );
        }
        else{
            myAdapter.setArrayMyData(weatherComAdapter.getWeatherDays());
            myAdapter.notifyDataSetChanged();
        }

    }

    //обработчик с использованием слабой связности
    private static class WeatherComHandler extends Handler {
        private final WeakReference<WeatherComFragment> mFragment;

        WeatherComHandler(WeatherComFragment activity) {
            mFragment = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            WeatherComFragment fragment = mFragment.get();
            if (mFragment != null) {
                fragment.setData();
            }
            Log.i("Update", "Finish");
        }
    }

}
