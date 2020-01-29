package com.example.myweather.server;

import com.example.myweather.Identified;

import java.io.Serializable;

public class Server implements Serializable, Identified {
    private String name, url, serverType;
    private long id, idLocation;
    public Server(long id, String serverType, String name, String url, long idLocation){
        this.id = id;
        this.serverType = serverType;
        this.name = name;
        this.url = url;
        this.idLocation = idLocation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(long idLocation) {
        this.idLocation = idLocation;
    }
}
