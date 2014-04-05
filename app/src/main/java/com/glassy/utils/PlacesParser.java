package com.glassy.utils;

import android.util.Log;

import com.glassy.model.Place;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by tomas on 4/5/2014.
 */
public class PlacesParser {

    private static final String MY_TAG = "PlacesParser";


    public static ArrayList<Place> GetPlaces(JSONObject obj)
    {
        ArrayList<Place> places = new ArrayList<Place>();
        AddPlaces(places, obj);
        return places;
    }

    public static void AddPlaces(ArrayList<Place> places, JSONObject obj)
    {
        try
        {
            JSONArray jPlaces = obj.getJSONArray("places");
            for (int i = 0; i < jPlaces.length(); i++) {
                Place p = new Place(jPlaces.getJSONObject(i));
                places.add(p);
            }
        }
        catch (Exception ex) {
            Log.d(MY_TAG, "Exception: " + ex.getMessage() + "\n");
        }
    }
}
