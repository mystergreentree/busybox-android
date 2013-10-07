package stericson.busybox.donate.jobs;

import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.PrepareBinaryTask;
import android.app.Activity;

public class PrepareBinaryJob extends AsyncJob {
    public final static int PREPARE_BINARY_JOB = 752216;

    private String binaryPath;
    private JobCallback cb;

    public PrepareBinaryJob(Activity activity, JobCallback cb, String binaryPath) {
        super(activity, R.string.preparing, true, false);
        this.cb = cb;
        this.binaryPath = binaryPath;
    }

    @Override
    JobResult handle() {
        return PrepareBinaryTask.execute(this, binaryPath);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    void callback(JobResult result) {
        cb.jobFinished(result, PREPARE_BINARY_JOB);
    }
}