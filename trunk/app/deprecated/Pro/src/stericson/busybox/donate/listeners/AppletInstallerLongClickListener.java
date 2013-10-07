package stericson.busybox.donate.listeners;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.activities.MainActivity;
import stericson.busybox.donate.jobs.containers.Item;
import stericson.busybox.donate.jobs.InstallAppletJob;
import stericson.busybox.donate.jobs.UnInstallAppletJob;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class AppletInstallerLongClickListener implements OnItemLongClickListener {

    private MainActivity activity;

    public AppletInstallerLongClickListener(MainActivity activity) {
        this.activity = activity;
    }

    public boolean onItemLongClick(final AdapterView<?> adapter, View v, final int position,
                                   long arg3) {

        final Item item = App.getInstance().getItemList().get(position);

        if (item.getFound()) {
            if (App.getInstance().getAvailableApplets().contains(item.getApplet())) {
                new AlertDialog.Builder(activity)
                        .setTitle("Reinstall/Uninstall " + item.getApplet() + "?")
                        .setMessage(activity.getString(R.string.installapplet))
                        .setPositiveButton("Reinstall", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                activity.stopGatherInformation();

                                new InstallAppletJob(activity, activity, item.getApplet(), position).execute();
                            }
                        }).setNegativeButton("Uninstall", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new UnInstallAppletJob(activity, activity, item.getApplet(), item.getAppletPath(), position).execute();
                    }
                }).show();
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("Uninstall " + item.getApplet() + "?")
                        .setMessage(activity.getString(R.string.uninstallapplet))
                        .setPositiveButton("Uninstall", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new UnInstallAppletJob(activity, activity, item.getApplet(), item.getAppletPath(), position).execute();
                            }
                        })
                        .show();
            }
        } else {
            if (App.getInstance().getAvailableApplets().contains(item.getApplet())) {
                new AlertDialog.Builder(activity)
                        .setTitle("Install " + item.getApplet() + "?")
                        .setMessage(activity.getString(R.string.installapplet))
                        .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                activity.stopGatherInformation();

                                new InstallAppletJob(activity, activity, item.getApplet(), position).execute();
                            }
                        }).show();
            } else {
                Toast.makeText(activity, "This applet is not available for install", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
}
