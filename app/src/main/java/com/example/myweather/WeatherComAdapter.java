package com.example.myweather;

import android.os.Handler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//класс взаимодейтствие сайта Weather.com и программы
public class WeatherComAdapter extends WeatherAdapter<WeatherDay> {

    private String
            cssQuery = "#twc-scrollabe > table.twc-table > tbody > tr";
    private Document doc;

    public WeatherComAdapter(Handler handler) {
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
            createWeatherDays10();
        }
        if (handler != null && !weatherDays.isEmpty()) {
            handler.sendEmptyMessage(1);
        }
    }

    //Получение информации и создание дней
    @Override
    public WeatherAdapter createWeatherDays10() {
        if (doc != null) {
            Elements elements = doc.select(cssQuery);

            for (int i = 0; i < elements.size(); i++) {
                Element curEl = elements.get(i);
                WeatherDay curDay = new WeatherDay();
                Element temp = curEl.select("td[headers=day] span.date-time").first();
                curDay.setDayOfTheWeek(temp.text().equals("Мес.") ? "Пн" : temp.text());

                temp = curEl.select("td[headers=day] span.day-detail.clearfix").first();
                String dayText = temp.text(), num = "";
                Matcher matcher1 = Pattern.compile("[A-я]*").matcher(dayText);
                Month curMonth = null;
                if (matcher1.find()) {
                    curMonth = Month.getMonthByName(
                            dayText.substring(matcher1.start(), matcher1.end()));
                    num = dayText.substring(matcher1.end());
                }
                if (curMonth != null && !num.equals("")) {
                    curDay.setDate(num + " " + curMonth.getNameFullPrepositional());
                } else {
                    curDay.setDate("");
                }
                if (i == 0){
                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("d");
                    String formattedDate = df.format(c);
                    if (formattedDate.equals(num))
                        continue;
                }

                temp = curEl.select("td[headers=description] span").first();
                curDay.setDescription(temp.text());
                temp = curEl.select("td[headers=hi-lo] div > span:nth-child(1)").first();
                curDay.setTempMax(temp.text());
                temp = curEl.select("td[headers=hi-lo] div > span:nth-child(3)").first();
                curDay.setTempMin(temp.text());
                temp = curEl.select("td[headers=precip] div > span:nth-child(2) > span").first();
                curDay.setPrecip(temp.text());
                temp = curEl.select("td[headers=wind] span").first();
                curDay.setWind(temp.text());
                temp = curEl.select("td[headers=humidity] span > span").first();
                curDay.setHumidity(temp.text());
                temp = curEl.select("icon").first();
                curDay.setIcon(temp.toString());
                weatherDays.add(curDay);
            }
        }
        return this;
    }
}
