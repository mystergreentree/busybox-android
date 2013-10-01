package stericson.busybox.donate.jobs;

import android.app.Activity;
import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.InitialChecksTask;

public class InitialChecksJob extends AsyncJob {
    public final static int Checks = 456214;

    private JobCallback cb;

    public InitialChecksJob(Activity activity, JobCallback cb) {
        super(activity, R.string.initialChecks, false, false);
        this.cb = cb;
    }

    @Override
    JobResult handle() {
        return InitialChecksTask.execute(this);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    void callback(JobResult result) {
        cb.jobFinished(result, Checks);
    }
}
