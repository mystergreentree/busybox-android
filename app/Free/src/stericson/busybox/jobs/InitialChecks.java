package stericson.busybox.jobs;

import stericson.busybox.App;
import stericson.busybox.Common;
import stericson.busybox.R;
import stericson.busybox.Activity.MainActivity;
import stericson.busybox.domain.Result;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class InitialChecks extends AsyncJob<Result>
{
	private MainActivity activity;
	protected TextView view;
	public static int Checks = 2;
	
	public InitialChecks(MainActivity activity)
	{
		super(activity, R.string.initialChecks, false, false);
		this.activity = activity;
	}

	@Override
    Result handle()
    {		
		Result result = new Result();
		
		try
		{
			RootTools.getShell(true);
		}
		catch (Exception e)
		{
			result.setSuccess(false);
			result.setError(activity.getString(R.string.shell_error));
		    return result; 
		}
		
		if (!RootTools.isRootAvailable()) {
			result.setError(activity.getString(R.string.noroot2));
		}
		else
		{
			try {
				if (!RootTools.isAccessGiven()) {
					result.setError(activity.getString(R.string.noAccess));
				}
			} catch (Exception e) {				
				result.setError(activity.getString(R.string.accessUndetermined));
			}
		}

		App.getInstance().setInstalled(RootTools.isBusyboxAvailable());
		
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
		activity.jobCallBack(result, Checks);
    }
}
