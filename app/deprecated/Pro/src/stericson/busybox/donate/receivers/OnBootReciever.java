package stericson.busybox.donate.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import stericson.busybox.donate.services.PreferenceService;

public class OnBootReciever extends BroadcastReceiver {

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(final Context context, Intent intent) {
        PreferenceService p = new PreferenceService(context);

        if (p.getClearSbin()) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        RootTools.debugMode = true;
                        RootTools.getShell(true);

                        if (RootTools.exists("/system/bin/busybox") || RootTools.exists("/system/xbin/busybox")) {

                            RootTools.remount("/", "rw");
                            CommandCapture cmd = new CommandCapture(0,
                                    "/system/bin/busybox rm /sbin/busybox",
                                    "/system/xbin/busybox rm /sbin/busybox",
                                    "rm /sbin/busybox");
                            RootTools.getShell(true).add(cmd);
                            Thread.sleep(2000);

                            RootTools.remount("/", "r0");

                        }

                    } catch (Exception e) {}
                }
            };

            t.start();
        }
    }
}
