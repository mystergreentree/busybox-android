package stericson.busybox.donate.jobs;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.tasks.RestoreBackupTask;
import android.app.Activity;
import android.widget.TextView;

public class RestoreBackupJob extends AsyncJob {
    public static final int RESTORE_BACKUP_JOB = 2364;

    private JobCallback cb;

    public RestoreBackupJob(Activity activity, JobCallback cb) {
        super(activity, R.string.restoring, true, false);

        this.cb = cb;
    }

    @Override
    JobResult handle() {
        return new RestoreBackupTask().execute(this);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        cb.jobProgress(values[0], RESTORE_BACKUP_JOB);
    }

    @Override
    void callback(JobResult result) {
        cb.jobFinished(result, RESTORE_BACKUP_JOB);
    }
}
