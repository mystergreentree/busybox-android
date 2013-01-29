package stericson.busybox.donate.jobs;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;

import com.stericson.RootTools.RootTools;

public class PrepareBinary
{
	String storagePath = "";

	public Result prepareBinary(MainActivity activity, String binaryPath)
	{
		Result result = new Result();
		result.setSuccess(true);
		storagePath = activity.getFilesDir().toString() + "/bb";
		
		try {
			RootTools.getShell(true);
		}
		catch (Exception e) {
			result.setSuccess(false);
			result.setError(activity.getString(R.string.shell_error));
			return result;
		}
		
		try 
		{
			if (!RootTools.fixUtils(new String[] {"ls", "ln", "dd", "chmod"}))
			{
				result.setError(activity.getString(R.string.utilProblem));
				result.setSuccess(false);
			    return result; 
			}
		}
		catch(Exception e) {
			result.setError(activity.getString(R.string.utilProblem));
			result.setSuccess(false);
		    return result; 
		}
		
		Common.setupBusybox(activity, binaryPath, true);
		
		String version = RootTools.getBusyBoxVersion(storagePath);
		
		if (version.equals(""))
		{
			result.setSuccess(false);
			result.setError(activity.getString(R.string.binary_verification_failed));
			return result;
		}
		
		App.getInstance().setStericson(version.contains("stericson"));
		App.getInstance().setInstallCustom(true);
		App.getInstance().setVersion(version);
		
		return result;
	}
}
