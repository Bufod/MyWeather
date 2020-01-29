package com.example.myweather;

import android.os.Handler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

//класс взаимодейтствие сайта Yandex.Погоды и программы
public class YandexWeatherAdapter extends WeatherAdapter<WeatherDay[]>{
    private String
            cssQueryDay = "dt[id~=forecast-day[0-9]*$]",
            cssQueryDayInfo = "dd[class~=.*day-info$]";
    private Document doc;
    Elements elements_day;
    Elements elements_day_info;

    public YandexWeatherAdapter(Handler handler) {
        super(handler);
    }

    // задача выполняемая в потоке
    @Override
    public void run() {
        if (url != null) {
            if (!weatherDays.isEmpty())
                weatherDays.clear();
            try {
                this.doc = Jsoup.connect(url)
                        .userAgent(agent)
                        .referrer(ref)
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.elements_day = doc.select(cssQueryDay);
            this.elements_day_info = doc.select(cssQueryDayInfo);
            createWeatherDays10();
        }
        if (handler != null && !weatherDays.isEmpty()){
            handler.sendEmptyMessage(1);
        }
    }
    //Получение информации и создание дней
    @Override
    public WeatherAdapter createWeatherDays10() {
        if (doc != null) {
            TimesOfDay[] timesOfDays = TimesOfDay.values();
            for (int i = 0; i < elements_day.size(); i++) {
                WeatherDay[] curDay = new WeatherDay[timesOfDays.length];
                Element element_day = elements_day.get(i),
                        element_day_info = elements_day_info.get(i);

                for (int j = 0; j < timesOfDays.length; j++) {
                    //выбор части дня загрузки данных
                    Elements curElTime = element_day_info.select("tr:nth-child(" + (j+1) + ")");

                    //работа с частью дня и выгрузка данных
                    WeatherDay dayPart = curDay[j] = new WeatherDay();
                    dayPart.setDate(element_day.select("[class~=.*day-number$]").text() + " " +
                            element_day.select("[class~=.*day-month$]").text());
                    dayPart.setDayOfTheWeek(element_day.select("[class~=.*day-name$]").text());
                    dayPart.setDescription(curElTime.select("td[class~=.*condition$]").text());

                    String temp = curElTime.select(".weather-table__temp .temp:nth-child(2) .temp__value").text();
                    dayPart.setTempMax(temp.equals("") ? "--" : temp);
                    temp = curElTime.select("div[class~=^temp.*] .temp__value").first().text();
                    dayPart.setTempMin(temp.equals("") ? "--" : temp);

                    dayPart.setWind(curElTime.select(".wind-speed").text());
                    dayPart.setHumidity(curElTime.select("td[class~=.*humidity$]").text());
                    dayPart.setPressure(curElTime.select("td[class~=.*pressure$]").text());
                    dayPart.setFeelsLike(curElTime.select("td[class~=.*feels-like$] .temp__value").text());
                    dayPart.setTymesOfDay(timesOfDays[j].toString());
                }
                weatherDays.add(curDay);
            }
        }
        return this;
    }
}
