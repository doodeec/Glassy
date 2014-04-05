package com.glassy.model;

import com.glassy.utils.MathUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a point of interest that has geographical coordinates (latitude and
 * longitude) and a name that is displayed to the user.
 */
public class Place {

    public static String LAST_TYPE = "bar";

    public static double GetMyLatitude() {
        return 49.211750;
    }
    public static double GetMyLongitude() {
        return 16.598490;
    }


    private final double mLatitude;
    private final double mLongitude;
    private final String mName;
    private final String mType;
    private final double mRating;


    public double getDistance() {
        return MathUtils.getDistance(mLatitude, mLongitude, Place.GetMyLatitude(), Place.GetMyLongitude());
    }
    public double getBearing() {
        return MathUtils.getBearing(mLatitude, mLongitude, Place.GetMyLatitude(), Place.GetMyLongitude());
    }

    public Place(JSONObject jObj) throws JSONException {

        mName = jObj.getString("name");
        mType = jObj.getString("locationType");
        mRating = jObj.getDouble("rating");

        JSONObject jLoc = jObj.getJSONObject("loc");
        mLatitude = jLoc.getDouble("lat");
        mLongitude =jLoc.getDouble("lon");
    }
    /**
     * Initializes a new place with the specified coordinates and name.
     *
     * @param latitude the latitude of the place
     * @param longitude the longitude of the place
     * @param name the name of the place
     */
    public Place(double latitude, double longitude, String name, double rating) {
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        //TODO
        mType = null;
        mRating = rating;
    }

    /**
     * Gets the latitude of the place.
     *
     * @return the latitude of the place
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Gets the longitude of the place.
     *
     * @return the longitude of the place
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Gets the name of the place.
     *
     * @return the name of the place
     */
    public String getName() {
        return mName;
    }

    /**
     * Get type
     * @return Type
     */
    public String getType() {
        return mType;
    }

    /**
     * Get rating
     * @return rating
     */
    public double getRating() {
        return mRating;
    }
}
