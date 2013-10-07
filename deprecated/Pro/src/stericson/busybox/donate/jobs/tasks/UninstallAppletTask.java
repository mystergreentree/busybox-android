package stericson.busybox.donate.jobs.tasks;

import android.content.Context;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.JobResult;

/**
 * Created by Stephen Erickson on 7/9/13.
 */
public class UninstallAppletTask extends BaseTask {

    public JobResult execute(AsyncJob j, String applet, String path) {
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
            if (RootTools.remount("/system", "rw")) {
                j.publishCurrentProgress("Uninstalling " + applet + "...");

                command = new ShellCommand(this, 0,
                        "toolbox rm " + path + "/" + applet,
                        "rm " + path + "/" + applet);
                Shell.startRootShell().add(command);
                command.pause();

                if (!RootTools.exists(path + "/" + applet)) {
                    result.setSuccess(true);
                }
            }
        } catch (Exception e) {}

        return result;
    }
}
