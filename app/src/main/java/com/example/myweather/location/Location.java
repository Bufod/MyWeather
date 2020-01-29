package com.example.myweather.location;

import com.example.myweather.Identified;

import java.io.Serializable;

public class Location implements Serializable, Identified {
    private String name;
    private long id;
    private boolean favorite;

    public Location(long id, String name, boolean favorite) {
        this.name = name;
        this.id = id;
        this.favorite = favorite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;
        else {
            Location tmpLocation = (Location) obj;
            return name.equals(tmpLocation.name) &&
                    id == tmpLocation.id &&
                    favorite == tmpLocation.favorite;
        }
    }
}
