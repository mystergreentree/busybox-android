package stericson.busybox.donate.jobs.tasks;

import android.content.Context;

import com.stericson.RootTools.RootTools;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.Common;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.services.PreferenceService;

/**
 * Created by Stephen Erickson on 7/9/13.
 */
public class InitialChecksTask {

    public static JobResult execute(AsyncJob j) {
        Context context = j.getContext();
        JobResult result = new JobResult();
        PreferenceService p = new PreferenceService(context);

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        if (!RootTools.isRootAvailable()) {
            result.setError(context.getString(R.string.noroot2));
        } else {
            try {
                if (!RootTools.isAccessGiven()) {
                    result.setError(context.getString(R.string.noAccess));
                }

                App.getInstance().setSpace((float) (RootTools.getSpace("/system") / 1000));

            } catch (Exception e) {
                result.setError(context.getString(R.string.accessUndetermined));
            }
        }

        App.getInstance().setInstalled(RootTools.isBusyboxAvailable());

        if (p.getAskSbin()) {
            if (RootTools.exists("/sbin/busybox")) {
                App.getInstance().setInstalledSbin(true);
            }
        }

        Common.setupBusybox(context, Constants.newest, false);

        result.setSuccess(true);
        return result;

    }
}
