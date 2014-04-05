package com.glassy;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.glassy.model.Place;
import com.glassy.utils.PlacesParser;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Trieda na asynchronne ziskavanie places
 */
public class GetPlacesTask extends AsyncTask<Bundle, ArrayList<Place>, ArrayList<Place>> {

	private static final String MY_TAG = "GetPlacesTask";
	private String mType;
	private WeakReference<GlassyRenderer> wr;

	public GetPlacesTask(GlassyRenderer gr) {
		wr = new WeakReference<GlassyRenderer>(gr);
	}

    public static final  String TAG = "GetPlacesTask";
    public static final  String SERVER_URL = "http://glassapi.doodeec.com/";

	@Override
	protected ArrayList<Place> doInBackground(Bundle... bundle) {

		mType = bundle[0].getString("type");
		String url = SERVER_URL + mType;

        ArrayList<Place> places = null;
		HttpClient httpclient = new DefaultHttpClient();

		HttpResponse response = null;

        try {
            HttpGet get = new HttpGet(url);
            response = httpclient.execute(get);

            if (response.getStatusLine().getStatusCode() != 200) {
                // TODO osetrenie chybovych volani
                return null;
            }

            // overenie, ci dostanem JSON alebo stream
            Header hContentType = response.getFirstHeader("Content-Type");
            if (hContentType == null) {
                // TODO chyba - content type musi byt specifikovany v odpovedi
                return null;
            }

            /**
             * Data naparsujeme uz tu, v onPostEx len notifikujeme o postupe
             */
            JSONObject jResult = null;
            if (hContentType.getValue().contains("application/json")) {
                String r = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                jResult = new JSONObject(r);
            }
            else
            {
                // TODO chyba - content type musi byt specifikovany v odpovedi
                return null;
            }

            places = PlacesParser.GetPlaces(jResult);

        } catch (Exception e) {
            Log.v(MY_TAG, e.toString());
            return null;
        }
        return places;
	}

	@Override
	protected void onPostExecute(ArrayList<Place> places) {
		Log.d(MY_TAG, "GetPictureTask onPostExecute");

        wr.get().publishResults(places, mType);
    }
}
