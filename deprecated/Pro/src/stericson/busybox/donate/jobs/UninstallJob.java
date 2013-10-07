package stericson.busybox.donate.jobs;

import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.UninstallTask;
import android.app.Activity;

public class UninstallJob extends AsyncJob {
    public static final int UNINSTALL_JOB = 5492;

    private boolean smart = true;
    private JobCallback cb;

    public UninstallJob(Activity activity, JobCallback cb, boolean smart) {
        super(activity, R.string.uninstalling, true, false);
        this.smart = smart;
        this.cb = cb;
    }

    @Override
    JobResult handle() {
        return new UninstallTask().execute(this, smart);
    }

    public void publishCurrentProgress(Object... values) {
        this.publishProgress(values);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        cb.jobProgress(values[0], UNINSTALL_JOB);
    }

    @Override
    void callback(JobResult result) {
        cb.jobFinished(result, UNINSTALL_JOB);
    }
}
