package com.example.myweather;

import androidx.annotation.NonNull;

//перечисление времени суток
public enum TimesOfDay{
    MORNING("утро"),
    AFTERNOON("обед"),
    EVENING("вечер"),
    NIGHT("ночь");
    private String name;
    TimesOfDay(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}