
package com.sgv.runtracker.activity;

import android.support.v4.app.Fragment;

import com.sgv.runtracker.fragment.RunMapFragment;

public class RunMapActivity extends SingleFragmentActivity {

    public static final String EXTRA_RUN_ID = "com.sgv.runtracker.run_id";

    @Override
    protected Fragment createFragment() {
        long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        if (runId != -1) {
            return RunMapFragment.newInstance(runId);

        } else {
            return new RunMapFragment();
        }
    }

}
