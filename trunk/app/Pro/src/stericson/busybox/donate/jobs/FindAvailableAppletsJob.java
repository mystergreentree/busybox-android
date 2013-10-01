package stericson.busybox.donate.jobs;

import android.app.Activity;
import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.FindAvailableAppletsTask;

public class FindAvailableAppletsJob extends AsyncJob {
    public final static int AVAIL_APPLETS = 76551;

    private JobCallback cb;

    public FindAvailableAppletsJob(Activity activity, JobCallback cb) {
        super(activity, R.string.initialChecks, false, false);
        this.cb = cb;
    }

    @Override
    JobResult handle() {
        return FindAvailableAppletsTask.execute(this);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    void callback(JobResult result) {
        cb.jobFinished(result, AVAIL_APPLETS);
    }
}
