package com.sgv.runtracker.loader;

import android.content.Context;
import android.location.Location;

import com.sgv.runtracker.RunManager;

public class LastLocationLoader extends DataLoader<Location> {
    private long mRunId;
    
    public LastLocationLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }

    @Override
    public Location loadInBackground() {
        return RunManager.get(getContext()).getLastLocationForRun(mRunId);
    }
}