package stericson.busybox.donate.listeners;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Support.Common;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.activities.MainActivity;
import stericson.busybox.donate.jobs.FindAvailableAppletsJob;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Version implements OnItemSelectedListener {
    MainActivity activity = null;

    public Version(MainActivity main) {
        this.activity = main;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapter, View view, int position,
                               long arg3) {
        if (position >= 2) {
            activity.makeChoice(activity, Constants.CUSTOM_VERSION, R.string.custom, R.string.version_custom, R.string.choose_custom, R.string.download_custom);
        } else {
            App.getInstance().setInstallCustom(false);
            Common.setupBusybox(activity, adapter.getSelectedItem().toString(), false);
            new FindAvailableAppletsJob(activity, activity).execute();
        }

        App.getInstance().setVersion(adapter.getSelectedItem().toString());
        App.getInstance().updateVersion(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
