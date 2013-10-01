package stericson.busybox.donate.jobs.tasks;

import android.content.Context;

import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.JobResult;

/**
 * Created by Stephen Erickson on 7/9/13.
 */
public class FindAvailableAppletsTask {

    public static JobResult execute(AsyncJob j) {
        Context context = j.getContext();
        JobResult result = new JobResult();

        String storagePath = context.getFilesDir().toString() + "/bb";

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        List<String> applets;

        try {
            applets = RootTools.getBusyBoxApplets(storagePath);

            if (applets != null)
                App.getInstance().setAvailableApplets(applets);
            else
                App.getInstance().setAvailableApplets(new ArrayList<String>());

        } catch (Exception e) {
            App.getInstance().setAvailableApplets(new ArrayList<String>());
        }

        result.setSuccess(true);
        return result;
    }
}
