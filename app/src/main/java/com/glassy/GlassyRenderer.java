package com.glassy;

import com.glassy.model.Landmarks;
import com.glassy.model.Place;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The surface callback that provides the rendering logic for the compass live card. This callback
 * also manages the lifetime of the sensor and location event listeners (through
 * {@link OrientationManager}) so that tracking only occurs when the card is visible.
 */
public class GlassyRenderer implements SurfaceHolder.Callback {

    private static final String TAG = GlassyRenderer.class.getSimpleName();

    /**
     * The (absolute) pitch angle beyond which the compass will display a message telling the user
     * that his or her head is at too steep an angle to be reliable.
     */
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;

    /** The refresh rate, in frames per second, of the compass. */
    private static final int REFRESH_RATE_FPS = 45;

    /** The duration, in milliseconds, of one frame. */
    private static final long FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;

    private SurfaceHolder mHolder;
    private boolean mTooSteep;
    private boolean mInterference;
    private RenderThread mRenderThread;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private final FrameLayout mLayout;
    private final GlassyView mCompassView;
    private final RelativeLayout mTipsContainer;
    private final TextView mTipsView;
    private final OrientationManager mOrientationManager;
    private final Landmarks mLandmarks;

    public void myLocationChanged(Location newLocation) {
        Bundle bun = new Bundle();
        bun.putString("type", "shop");
        bun.putDouble("lat", newLocation.getLatitude());
        bun.putDouble("lon", newLocation.getLongitude());
        new GetPlacesTask(this).execute(bun);
    }


    private final OrientationManager.OnChangedListener mCompassListener =
            new OrientationManager.OnChangedListener() {

        @Override
        public void onOrientationChanged(OrientationManager orientationManager) {
            mCompassView.setHeading(orientationManager.getHeading());

            boolean oldTooSteep = mTooSteep;
            mTooSteep = (Math.abs(orientationManager.getPitch()) > TOO_STEEP_PITCH_DEGREES);
            if (mTooSteep != oldTooSteep) {
                updateTipsView();
            }
        }

        @Override
        public void onLocationChanged(OrientationManager orientationManager) {
            Location location = orientationManager.getLocation();
            List<Place> places = mLandmarks.getNearbyLandmarks(
                    location.getLatitude(), location.getLongitude());
            mCompassView.setNearbyPlaces(places, Place.LAST_TYPE);

        }

        @Override
        public void onAccuracyChanged(OrientationManager orientationManager) {
            mInterference = orientationManager.hasInterference();
            updateTipsView();
        }
    };


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
        //((TextView)findViewById(R.id.txtResponse)).setText(sb.toString());
        Log.d("Landmarks", "Result: " + sb.toString());

        mCompassView.setNearbyPlaces(places, type);
    }

    /**
     * Creates a new instance of the {@code CompassRenderer} with the specified context,
     * orientation manager, and landmark collection.
     */
    public GlassyRenderer(Context context, OrientationManager orientationManager,
                          Landmarks landmarks) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mLayout = (FrameLayout) inflater.inflate(R.layout.glassy, null);
        mLayout.setWillNotDraw(false);

        mCompassView = (GlassyView) mLayout.findViewById(R.id.glassy_view);
        mTipsContainer = (RelativeLayout) mLayout.findViewById(R.id.tips_container);
        mTipsView = (TextView) mLayout.findViewById(R.id.tips_view);

        mOrientationManager = orientationManager;
        mLandmarks = landmarks;

        mCompassView.setOrientationManager(mOrientationManager);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        doLayout();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;

        mOrientationManager.addOnChangedListener(mCompassListener);
        mOrientationManager.start();

        if (mOrientationManager.hasLocation()) {
            Location location = mOrientationManager.getLocation();
            //myLocationChanged(location);
            List<Place> nearbyPlaces = mLandmarks.getNearbyLandmarks(
                    location.getLatitude(), location.getLongitude());
            mCompassView.setNearbyPlaces(nearbyPlaces, Place.LAST_TYPE);
        }

        mRenderThread = new RenderThread();
        mRenderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRenderThread.quit();

        mOrientationManager.removeOnChangedListener(mCompassListener);
        mOrientationManager.stop();
    }

    /**
     * Requests that the views redo their layout. This must be called manually every time the
     * tips view's text is updated because this layout doesn't exist in a GUI thread where those
     * requests will be enqueued automatically.
     */
    private void doLayout() {
        // Measure and update the layout so that it will take up the entire surface space
        // when it is drawn.
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(mSurfaceWidth,
                View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(mSurfaceHeight,
                View.MeasureSpec.EXACTLY);

        mLayout.measure(measuredWidth, measuredHeight);
        mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
    }

    /**
     * Repaints the compass.
     */
    private synchronized void repaint() {
        Canvas canvas = null;

        try {
            canvas = mHolder.lockCanvas();
        } catch (RuntimeException e) {
            Log.d(TAG, "lockCanvas failed", e);
        }

        if (canvas != null) {
            mLayout.draw(canvas);

            try {
                mHolder.unlockCanvasAndPost(canvas);
            } catch (RuntimeException e) {
                Log.d(TAG, "unlockCanvasAndPost failed", e);
            }
        }
    }

    /**
     * Shows or hides the tip view with an appropriate message based on the current accuracy of the
     * compass.
     */
    private void updateTipsView() {
        int stringId = 0;

        // Only one message (with magnetic interference being higher priority than pitch too steep)
        // will be displayed in the tip.
        if (mInterference) {
            stringId = R.string.magnetic_interference;
        } else if (mTooSteep) {
            stringId = R.string.pitch_too_steep;
        }

        boolean show = (stringId != 0);

        if (show) {
            mTipsView.setText(stringId);
            doLayout();
        }

        if (mTipsContainer.getAnimation() == null) {
            float newAlpha = (show ? 1.0f : 0.0f);
            mTipsContainer.animate().alpha(newAlpha).start();
        }
    }

    /**
     * Redraws the compass in the background.
     */
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                repaint();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }
}
