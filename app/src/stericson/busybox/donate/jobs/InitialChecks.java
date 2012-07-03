package stericson.busybox.donate.jobs;

import stericson.busybox.donate.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
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

		RootTools.useRoot = true;
		
		if (!RootTools.isRootAvailable()) {
			result.setMessage(activity.getString(R.string.noroot2));
		}
		else
		{
			try {
				if (!RootTools.isAccessGiven()) {
					result.setMessage(activity.getString(R.string.noAccess));
				}
			} catch (Exception e) {				
				result.setMessage(activity.getString(R.string.accessUndetermined));
			}
		}		
			
		Common.findBusyBoxLocations(false, false);
		

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
