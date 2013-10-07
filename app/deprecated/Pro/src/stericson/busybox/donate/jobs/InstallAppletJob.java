package stericson.busybox.donate.jobs;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.InstallAppletTask;

import android.app.Activity;
import android.widget.AdapterView;
import android.widget.TextView;

public class InstallAppletJob extends AsyncJob {
    public static final int INSTALL_APPLET_JOB = 1347;

    private JobCallback cb;
    private String applet;
    private int position;

    public InstallAppletJob(Activity activity, JobCallback cb, String applet, int position) {
        super(activity, R.string.installing, true, false);
        this.cb = cb;
        this.applet = applet;
        this.position = position;
    }

    @Override
    JobResult handle() {
        return new InstallAppletTask().execute(this, applet);
    }

    public void publishCurrentProgress(Object... values) {
        this.publishProgress(values);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        cb.jobProgress(values[0], INSTALL_APPLET_JOB);
    }

    @Override
    void callback(JobResult result) {
        result.setData(Integer.toString(position));
        cb.jobFinished(result, INSTALL_APPLET_JOB);
    }
}
