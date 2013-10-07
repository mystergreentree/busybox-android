package stericson.busybox.donate.jobs;

import android.app.Activity;

import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.InstallTask;

public class InstallJob extends AsyncJob {
    public static final int INSTALL_JOB = 1253;

    private String path;
    private JobCallback cb;
    private boolean clean;

    public InstallJob(Activity activity, JobCallback cb, String path, boolean clean) {
        super(activity, R.string.installing, true, false);
        this.path = path;
        this.cb = cb;
        this.clean = clean;
    }

    @Override
    JobResult handle() {
        return new InstallTask().execute(this, context, path, false, clean, false);
    }

    public void publishCurrentProgress(Object... values) {
        this.publishProgress(values);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        cb.jobProgress(values[0], INSTALL_JOB);
    }

    @Override
    void callback(JobResult result) {

        cb.jobFinished(result, INSTALL_JOB);
    }
}