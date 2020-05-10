package edu.msu.jungse12.arewethereyetjungse12;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListPlace {
    String title;
    double latitude;
    double longitude;
    int id;
    //MainActivity main;

    public ListPlace(String title, double latitude, double longitude, int id) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

}
