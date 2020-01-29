package com.example.myweather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.util.ArrayList;

//обобщенный класс адаптера работы со списками ListView
public abstract class ListAdapter<T extends Serializable & Identified> extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<T> arrayMyData;

    public ListAdapter (){
        this.layoutInflater = null;
        this.arrayMyData = null;
    }

    public ListAdapter (Context ctx) {
        layoutInflater = LayoutInflater.from(ctx);
    }

    public ListAdapter<T> setContext(Context ctx){
        layoutInflater = LayoutInflater.from(ctx);
        return this;
    }

    public LayoutInflater getLayoutInflater() throws NullPointerException {
        if (layoutInflater == null)
            throw new NullPointerException("ListAdapter<T> -> layoutInflater -> null");
        return layoutInflater;
    }

    public ArrayList<T> getArrayMyData() throws NullPointerException {
        if (arrayMyData == null)
            throw new NullPointerException("ListAdapter<T> -> arrayMyData -> null");
        return arrayMyData;
    }

    public ListAdapter<T> setArrayMyData(ArrayList<T> arrayMyData) {
        this.arrayMyData = arrayMyData;
        return this;
    }

    public int getCount () {
        return arrayMyData.size();
    }

    public Object getItem (int position) {
        return getArrayMyData().get(position);
    }

    public long getItemId (int position) {
        T ob = getArrayMyData().get(position);
        if (ob != null) {
            return ob.getId();
        }
        return -1;
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}
