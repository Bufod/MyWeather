package com.example.myweather;

import java.io.Serializable;

//стандартный класс погоды с основными параметрами, геттерами и сеттерами
class WeatherDay implements Serializable, Identified {
    private String
            date, //дата (24 дек 2019)
            dayOfTheWeek, // день недели (Вт)
            tymesOfDay, // время дня (утро, день, вечер, ночь)
            description, // описание (облачно, проливные дожди, снегопад утром и т.д)
            tempMax, // максимальная температура
            tempMin, // минимальная температура
            precip, // вероятность осадков
            wind, // ветер
            humidity, // влажность
            feelsLike, // ощущается как
            pressure, // давление
            icon; // иконка
    private long Id, idServer;

    public long getIdServer() {
        return idServer;
    }

    public void setIdServer(long idServer) {
        this.idServer = idServer;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public WeatherDay() {
    }

    public WeatherDay(String description, String tempMax,
                      String tempMin, String wind, String humidity) {
        this.description = description;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.wind = wind;
        this.humidity = humidity;
    }

    public long getId() {
        return Id;
    }

    public void setId(long Id) {
        this.Id = Id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getPrecip() {
        return precip;
    }

    public void setPrecip(String precip) {
        this.precip = precip;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTymesOfDay() {
        return tymesOfDay;
    }

    public void setTymesOfDay(String tymesOfDay) {
        this.tymesOfDay = tymesOfDay;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return this.date +
                "\n\t" + this.dayOfTheWeek
                + "\n\t" + this.description
                + "\n\t" + this.tempMax + "/" + this.tempMin
                + "\n\t" + this.precip
                + "\n\t" + this.wind
                + "\n\t" + this.humidity + "\n";
    }
}