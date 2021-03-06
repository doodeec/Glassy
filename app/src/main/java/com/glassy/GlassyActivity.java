package com.glassy;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import com.glassy.model.Place;

/**
 * This activity manages the options menu that appears when the user taps on the compass's live
 * card.
 */
public class GlassyActivity extends Activity {

    private GlassyService.GlassyBinder mCompassService;
    private boolean mResumed;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof GlassyService.GlassyBinder) {
                mCompassService = (GlassyService.GlassyBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, GlassyService.class), mConnection, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        openOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void openOptionsMenu() {
        if (mResumed && mCompassService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mCompassService.readHeadingAloud();
        getMenuInflater().inflate(R.menu.glassy, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.read_aloud:
//                return true;
            case R.id.bar:
                mCompassService.changeType("bar");
                return true;
            case R.id.restaurant:
                mCompassService.changeType("restaurant");
                return true;
            case R.id.shop:
                mCompassService.changeType("shop");
                return true;
            case R.id.stop:
                stopService(new Intent(this, GlassyService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

        unbindService(mConnection);

        // We must call finish() from this method to ensure that the activity ends either when an
        // item is selected from the menu or when the menu is dismissed by swiping down.
        finish();
    }


}
