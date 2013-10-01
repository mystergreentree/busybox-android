package stericson.busybox.donate.jobs.tasks;

import android.content.Context;

import com.stericson.RootTools.RootTools;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.JobResult;

/**
 * Created by Stephen Erickson on 7/9/13.
 */
public class FindFreeSpaceTask {

    public static JobResult execute(AsyncJob j, String location) {
        Context context = j.getContext();
        JobResult result = new JobResult();

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        App.getInstance().setSpace((float) (RootTools.getSpace(location) / 1000));

        result.setSuccess(true);
        return result;

    }
}
