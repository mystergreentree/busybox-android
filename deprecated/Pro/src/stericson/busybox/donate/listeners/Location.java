package stericson.busybox.donate.listeners;

import stericson.busybox.donate.App;
import stericson.busybox.donate.activities.MainActivity;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.FindFreeSpaceJob;
import stericson.busybox.donate.jobs.containers.JobResult;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

public class Location implements OnItemSelectedListener, JobCallback {

    private MainActivity activity;

    public Location(MainActivity activity) {
        this.activity = activity;
    }

    public void onItemSelected(final AdapterView<?> adapter, View arg1,
                               int position, long arg3) {
        if (position == 2) {
            final EditText input = new EditText(activity);
            new AlertDialog.Builder(activity)
                    .setTitle("Custom Path")
                    .setMessage("Please enter the Path you want to install Busybox, Success is not gauranteed. \n\n This is an advanced option")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (RootTools.exists(input.getText().toString())) {
                                activity.setCustomPath(input.getText().toString());
                                Toast.makeText(activity, "Custom install path set to " + activity.getCustomPath(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(activity, "That path does not exist or is not valid!", Toast.LENGTH_LONG).show();

                                activity.updateList();
                                App.getInstance().setPath(adapter.getSelectedItem().toString());
                                activity.setCustomPath("");
                            }

                            new FindFreeSpaceJob(activity, activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath(), Location.this).execute();

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            activity.updateList();
                            App.getInstance().setPath(adapter.getSelectedItem().toString());
                            activity.setCustomPath("");

                            new FindFreeSpaceJob(activity, activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath(), Location.this).execute();

                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            activity.updateList();
                            App.getInstance().setPath(adapter.getSelectedItem().toString());
                        }
                    }).show();
        } else {
            activity.setCustomPath("");

            App.getInstance().updatePath(adapter.getSelectedItemPosition());
            App.getInstance().setPath(adapter.getSelectedItem().toString());
            new FindFreeSpaceJob(activity, activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath(), Location.this).execute();
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}

    public void jobFinished(JobResult result, int id) {
        activity.updateList();
    }

    @Override
    public void jobProgress(Object value, int id) {}

}
