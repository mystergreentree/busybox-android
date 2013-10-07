package stericson.busybox.donate.interfaces;

import stericson.busybox.donate.jobs.containers.JobResult;

public interface JobCallback {

    public void jobFinished(JobResult result, int id);

    public void jobProgress(Object value, int id);
}
