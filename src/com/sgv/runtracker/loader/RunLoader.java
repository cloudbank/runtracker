package com.sgv.runtracker.loader;

import android.content.Context;

import com.sgv.runtracker.RunManager;
import com.sgv.runtracker.model.Run;

public class RunLoader extends DataLoader<Run> {
    private long mRunId;
    
    public RunLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }
    
    @Override
    public Run loadInBackground() {
        return RunManager.get(getContext()).getRun(mRunId);
    }
}