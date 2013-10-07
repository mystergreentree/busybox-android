package stericson.busybox.donate.jobs;

import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.UninstallAppletTask;
import android.app.Activity;

public class UnInstallAppletJob extends AsyncJob {
    public static final int UNINSTALL_APPLET_JOB = 3684;

    private String applet;
    private String path;
    private int position;
    private JobCallback cb;

    public UnInstallAppletJob(Activity activity, JobCallback cb, String applet, String path, int position) {
        super(activity, R.string.installing, true, false);
        this.cb = cb;
        this.applet = applet;
        this.path = path;
        this.position = position;
    }

    @Override
    JobResult handle() {
        return new UninstallAppletTask().execute(this, applet, path);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        cb.jobProgress(values[0], UNINSTALL_APPLET_JOB);
    }

    @Override
    void callback(JobResult result) {
        result.setData(Integer.toString(position));
        cb.jobFinished(result, UNINSTALL_APPLET_JOB);
    }
}
