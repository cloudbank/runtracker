package  com.sgv.runtracker.activity;

import android.support.v4.app.Fragment;

import com.sgv.runtracker.fragment.RunListFragment;

public class RunListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }

}
