package com.example.myweather;

import androidx.annotation.NonNull;

//перечисление всех месяцев
public enum Month{
    JANUARY("янв", "январь", "января", 1),
    February("фев", "февраль", "февраля", 2),
    March("мар", "март", "марта", 3),
    April("апр", "апрель", "апреля", 4),
    May("май", "май", "мая", 5),
    June("июн", "июнь", "июня", 6),
    July("июл", "июль", "июля", 7),
    August("авг", "август", "августа", 8),
    September("сен", "сентябрь", "сентября", 9),
    October("окт", "октябрь", "октября", 10),
    November("ноя", "ноябрь", "ноября", 11),
    December("дек", "декабрь", "декабря", 12);

    private String name3, nameFull, nameFullPrepositional;
    private int num;

    Month(String name3, String nameFull, String nameFullPrepositional, int num) {
        this.name3 = name3;
        this.nameFull = nameFull;
        this.nameFullPrepositional = nameFullPrepositional;
        this.num = num;
    }

    public static Month getMonthByName(String name){
        for (Month cur: values()) {
            if (cur.name3.equalsIgnoreCase(name) ||
                    cur.nameFull.equalsIgnoreCase(name) ||
                    cur.nameFullPrepositional.equalsIgnoreCase(name)){
                return cur;
            }
        }
        return null;
    }

    public String getNameFullPrepositional(){
        return nameFullPrepositional;
    }

    @NonNull
    @Override
    public String toString() {
        return nameFull;
    }
}