package stericson.busybox.donate.jobs.tasks;

import android.content.Context;
import android.os.Environment;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import java.io.File;

import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.CommandResult;
import stericson.busybox.donate.Support.Common;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.JobResult;

/**
 * Created by Stephen Erickson on 7/9/13.
 */
public class InstallAppletTask extends BaseTask {

    public JobResult execute(AsyncJob j, String applet) {
        String toolbox = Constants.toolbox;
        Context context = j.getContext();
        JobResult result = new JobResult();
        result.setSuccess(false);

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        j.publishCurrentProgress("Preparing System...");

        try {
            RootTools.remount("/", "rw");
            RootTools.remount("/system", "rw");

            //ALWAYS run this, I don't care if it does exist...I want to always make sure it is there.
            Common.extractResources(context, "toolbox", Environment.getExternalStorageDirectory() + "/toolbox-stericson");

            try {
                command = new ShellCommand(this, 0,
                        "dd if=" + Environment.getExternalStorageDirectory() + "/toolbox-stericson of=" + toolbox,
                        "chmod 0755 " + toolbox);
                Shell.startRootShell().add(command);
                command.pause();

            } catch (Exception e) {}


            if (!new File("/system/bin/reboot").exists()) {
                j.publishCurrentProgress("Adding reboot...");

                Common.extractResources(context, "reboot", Environment.getExternalStorageDirectory() + "/reboot-stericson");

                command = new ShellCommand(this, 0,
                        toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
                        "dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
                        toolbox + " chmod 0755 /system/bin/reboot",
                        "chmod 0755 /system/bin/reboot");
                Shell.startRootShell().add(command);
                command.pause();
            }


            Common.extractResources(context, Constants.newest, Environment.getExternalStorageDirectory() + "/busybox-stericson");

            j.publishCurrentProgress("Installing " + applet + "...");

            if (!RootTools.isBusyboxAvailable()) {
                command = new ShellCommand(this, 0,
                        "toolbox rm /system/xbin/" + applet,
                        "rm /system/xbin/" + applet,
                        toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/data/local/busybox",
                        "dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/data/local/busybox",
                        toolbox + " chmod 0755 /data/local/busybox",
                        "chmod 0755 /data/local/busybox",
                        "toolbox ln /data/local/busybox /system/xbin/" + applet,
                        "ln /data/local/busybox /system/xbin/" + applet,
                        "toolbox chmod 0755 /system/xbin/" + applet,
                        "chmod 0755 /system/xbin/" + applet,
                        "toolbox rm /data/local/busybox",
                        "rm /data/local/busybox");
            } else {
                String location = Common.findBusyBoxLocations(false, true)[0];

                command = new ShellCommand(this, 0,
                        "toolbox rm /system/xbin/" + applet,
                        "rm /system/xbin/" + applet,
                        "toolbox ln " + location + "busybox /system/xbin/" + applet,
                        "ln " + location + "busybox /system/xbin/" + applet,
                        "toolbox chmod 0755 /system/xbin/" + applet,
                        "chmod 0755 /system/xbin/" + applet);

            }

            Shell.startRootShell().add(command);
            command.pause();

            if (RootTools.exists("/system/xbin/" + applet)) {
                result.setSuccess(true);
            }
        } catch (Exception e) {}

        return result;
    }
}
