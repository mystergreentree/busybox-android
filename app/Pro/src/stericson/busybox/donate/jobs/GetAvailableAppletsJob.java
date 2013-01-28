package stericson.busybox.donate.jobs;

import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;

import com.stericson.RootTools.RootTools;

public class GetAvailableAppletsJob extends AsyncJob<Result>
{
	private MainActivity activity;
	public final static int AVAIL_APPLETS = 9;
	
	public GetAvailableAppletsJob(MainActivity activity)
	{
		super(activity, R.string.initialChecks, false, false);
		this.activity = activity;
	}

	@Override
    Result handle()
    {				
		Result result = new Result();

		String storagePath = activity.getFilesDir().toString() + "/bb";

		try
		{
			RootTools.getShell(true);
		}
		catch (Exception e)
		{
			result.setSuccess(false);
			result.setError(context.getString(R.string.shell_error));
		    return result; 
		}
		
		List<String> applets;
		
		try {
			applets = RootTools.getBusyBoxApplets(storagePath);
			
			if (applets != null)
				App.getInstance().setAvailableApplets(applets);
			else
				App.getInstance().setAvailableApplets(new ArrayList<String>());
				
		} catch (Exception e) {
			App.getInstance().setAvailableApplets(new ArrayList<String>());
		}
		
		result.setSuccess(true);
	    return result; 
    }

	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);

    }
    
	@Override
    void callback(Result result)
    {
		activity.jobCallBack(result, AVAIL_APPLETS);
    }
}
