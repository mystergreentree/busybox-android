package stericson.busybox.donate.jobs;

import android.app.Activity;

import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.FindAppletInformationTask;

public class FindAppletInformationJob extends AsyncJob {
    public final static int APPLET_INFO = 13245;

    private boolean silent;
    private boolean backup;
    private JobCallback cb;

    public FindAppletInformationJob(Activity activity, JobCallback cb, boolean silent, boolean backup) {
        super(activity, R.string.gathering, false, false);
        this.silent = silent;
        this.backup = backup;
        this.cb = cb;
    }

    @Override
    JobResult handle() {
        return new FindAppletInformationTask().execute(this, silent, Constants.appletsString, backup);
    }

    @Override
    protected synchronized void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        cb.jobProgress(values[0], APPLET_INFO);
    }

    @Override
    void callback(JobResult result) {
        if (!silent)
            cb.jobFinished(result, APPLET_INFO);
    }
}
