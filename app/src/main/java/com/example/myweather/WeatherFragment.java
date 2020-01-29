package com.example.myweather;

import android.view.View;

//интервейс для погодных фрагментов
public interface WeatherFragment {
    void setListnerBtAddServer(View.OnClickListener addServer);
    boolean isNullableServer();
    void setData();
}
