package com.glassy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.glassy.model.Place;
import com.glassy.utils.PlacesParser;

import java.util.ArrayList;

public class UrlActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.url, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getMyPlaces(String type) {
        Bundle bun = new Bundle();
        bun.putString("type", type);
        //new GetPlacesTask(this).execute(bun);
    }

    public void getBars(View v) {
        getMyPlaces("bar");
    }
    public void getRestaurants(View v) {
        getMyPlaces("restaurant");
    }
    public void getShops(View v) {
        getMyPlaces("shop");
    }


    public void publishResults(ArrayList<Place> places, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ");
        sb.append(type);
        sb.append("\n---------------------\n");

        if (places != null) {
            sb.append("Size: ");
            sb.append(places.size());
            sb.append("\n-----\n");
            for (Place p : places) {
                sb.append("Name: ");
                sb.append(p.getName());
                sb.append("\nRating: ");
                sb.append(p.getRating());
                sb.append("\n*******\n");
            }
        }
        else {
            sb.append("Places is NULL");
            sb.append("\n*******\n");
        }
        ((TextView)findViewById(R.id.txtResponse)).setText(sb.toString());

    }
}
