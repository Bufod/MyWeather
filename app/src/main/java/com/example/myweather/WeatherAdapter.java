package com.example.myweather;

import android.os.Handler;

import java.util.ArrayList;

//обобщенный класс погодного адаптера при подключении к серверу
public abstract class WeatherAdapter<T> implements Runnable {
    protected String
            url,
            agent = "Chrome/4.0.249.0 Safari/532.5",
            ref = "http://www.google.com";
    protected ArrayList<T> weatherDays;
    protected int cursor;
    protected Thread t;
    protected Handler handler;

    public WeatherAdapter() {
        this.weatherDays = new ArrayList<>();
    }

    public WeatherAdapter(Handler handler) {
        this.weatherDays = new ArrayList<>();
        this.handler = handler;
        t = new Thread(this);
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    // запуск потока
    public void start() {
        t = new Thread(this);
        t.start();
    }

    // присоединение потока
    public void join(){
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRun(){
        return t.isAlive();
    }

    // задача выполняемая в потоке
    @Override
    public abstract void run();

    //Получение информации и создание дней
    public abstract WeatherAdapter createWeatherDays10();

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public ArrayList<T> getWeatherDays() {
        return weatherDays;
    }

    // выбор следующего дня
    public T nextDay() {
        if (cursor + 1 < weatherDays.size()) {
            return weatherDays.get(++cursor);
        }
        return null;
    }

    // выбор предыдущего дня
    public T prevDay(){
        if (cursor - 1 >= 0) {
            return weatherDays.get(--cursor);
        }
        return null;
    }

    public T getCurrentDay(){
        return weatherDays.get(cursor);
    }

    public T getDayByPos(int position){
        if (position >= 0 && position < weatherDays.size())
            return weatherDays.get(position);
        return null;
    }
}
