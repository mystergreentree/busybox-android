package stericson.busybox.donate.jobs.tasks;

import android.content.Context;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Support.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.JobResult;

import com.stericson.RootTools.RootTools;

public class PrepareBinaryTask {
    
    public static JobResult execute(AsyncJob j, String binaryPath) {
        Context context = j.getContext();
        JobResult result = new JobResult();
        result.setSuccess(true);
        
        String storagePath = context.getFilesDir().toString() + "/bb";

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        try {
            if (!RootTools.fixUtils(new String[]{"ls", "ln", "dd", "chmod"})) {
                result.setError(context.getString(R.string.utilProblem));
                result.setSuccess(false);
                return result;
            }
        } catch (Exception e) {
            result.setError(context.getString(R.string.utilProblem));
            result.setSuccess(false);
            return result;
        }

        Common.setupBusybox(context, binaryPath, true);

        String version = RootTools.getBusyBoxVersion(storagePath);

        if (version.equals("")) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.binary_verification_failed));
            return result;
        }

        App.getInstance().setStericson(version.contains("stericson"));
        App.getInstance().setInstallCustom(true);
        App.getInstance().setVersion(version);

        return result;
    }
}
