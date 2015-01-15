
package com.sgv.runtracker.fragment;

import java.util.Date;

import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sgv.runtracker.LocationReceiver;
import com.sgv.runtracker.R;
import com.sgv.runtracker.RunManager;
import com.sgv.runtracker.database.RunDatabaseHelper.LocationCursor;
import com.sgv.runtracker.loader.LocationListCursorLoader;

public class RunMapFragment extends SupportMapFragment implements OnMapReadyCallback,
        LoaderCallbacks<Cursor> {
    private static final String ARG_RUN_ID = "RUN_ID";
    private static final int LOAD_LOCATIONS = 0;
    private static final String TAG = "com.sgv.runtracker.fragment.RunMapFragment";
    private long runId;
    private GoogleMap mGoogleMap;
    LocationCursor mLocationCursor;
    Marker endMarker;

    private LocationReceiver mLocationReceiver = new LocationReceiver() {
        // do live update to the map
        @Override
        protected void onLocationReceived(Context context, Location loc) {
            // insert the location
            mGoogleMap.clear();
            Log.d("DEBUG", "onLOCATIONRECEIVED for RunMapFragment");
            redrawMap();

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(RunManager.ACTION_LOCATION);
        filter.setPriority(0);
        getActivity().registerReceiver(mLocationReceiver, filter);
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    public static RunMapFragment newInstance(long runId) {
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, runId);
        RunMapFragment rf = new RunMapFragment();
        rf.setArguments(args);
        return rf;
    }

    protected void redrawMap() {
        Bundle args = getArguments();
        if (args != null) {
            long runId = args.getLong(ARG_RUN_ID, -1);
            if (runId != -1) {
                args.putLong(ARG_RUN_ID, runId);
                getLoaderManager().restartLoader(LOAD_LOCATIONS, args, this);
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // check for a Run ID as an argument, and find the run
        Bundle args = getArguments();
        if (args != null) {
            runId = args.getLong(ARG_RUN_ID, -1);
            if (runId != -1) {
                LoaderManager lm = getLoaderManager();
                lm.initLoader(LOAD_LOCATIONS, args, this);
            }
        }
    }

    private void updateUI() {
        // if (!(RunManager.get(getActivity()).isTrackingRun(runId))) {
        // Toast.makeText(getActivity(),
        // "This run is not being tracked, so there are no live updates",
        // Toast.LENGTH_SHORT);
        // }
        if (mGoogleMap == null || mLocationCursor == null) {
            return;
        } else {
            Log.d(TAG, "in updateUI &&&&& map + LC good");

        }
        float zIndex = 0.0f;
        // set up an overlay on the map for this run's locations
        // create a polyline with all of the points
        PolylineOptions line = new PolylineOptions();
        line.color(Color.BLUE);
        // also create a LatLngBounds so we can zoom to fit
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        // iterate over the locations
        mLocationCursor.moveToFirst();
        while (!mLocationCursor.isAfterLast()) {
            Location loc = mLocationCursor.getLocation();
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

            // if this is the first location, add a marker for it
            if (mLocationCursor.isFirst()) {
                String startDate = new Date(loc.getTime()).toString();
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(getResources().getString(R.string.run_start,
                                loc.getLatitude() + "," + loc.getLongitude()))
                        .snippet(
                                getResources().getString(R.string.run_started_at_format, startDate));
                mGoogleMap.addMarker(startMarkerOptions);
            } else if (mLocationCursor.isLast()) {

                // if this is the last location, and not also the first, add a
                // marker
                String endDate = new Date(loc.getTime()).toString();
                MarkerOptions finishMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(getResources().getString(R.string.run_finish,
                                loc.getLatitude() + "," + loc.getLongitude()))
                        .snippet(getResources().getString(R.string.run_finished_at_format, endDate));
                endMarker = mGoogleMap.addMarker(finishMarkerOptions);
            }

            line.add(latLng);
            line.zIndex(zIndex + 1);
            latLngBuilder.include(latLng);
            mLocationCursor.moveToNext();
        }
        // add the polyline to the map
        mGoogleMap.addPolyline(line);
        // make the map zoom to show the track, with some padding
        // use the size of the current display in pixels as a bounding box
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        // construct a movement instruction for the map camera
        CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(),
                display.getWidth(), display.getHeight(), 15);
        mGoogleMap.moveCamera(movement);
    }

    /*
     * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
     * container, Bundle savedInstanceState) { View v =
     * super.onCreateView(inflater, container, savedInstanceState); //wrong
     * place for getMap in lifecycle and getmap deprecated return v; }
     */

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated in RunMapFragment");
        super.onActivityCreated(savedInstanceState); // causes exception without
        getMapAsync(this); // guaranteed to get the map
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new LocationListCursorLoader(getActivity(), args.getLong(ARG_RUN_ID, -1));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mLocationCursor = (LocationCursor) cursor;
        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // stop using the data
        mLocationCursor.close();
        mLocationCursor = null;
    }

}
