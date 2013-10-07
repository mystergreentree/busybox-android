package stericson.busybox.jobs;

import com.stericson.RootTools.RootTools;

import stericson.busybox.Common;
import stericson.busybox.R;
import stericson.busybox.Activity.MainActivity;
import stericson.busybox.domain.Result;
import stericson.busybox.interfaces.CallBack;

public class GetLocations extends AsyncJob<Result>
{
	private CallBack cb;
	private boolean single;
	
	public GetLocations(MainActivity activity, CallBack cb, boolean single)
	{
		super(activity, R.string.initialChecks, false, false);
		this.cb = cb;
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
			result.setError(context.getString(R.string.shell_error));
		    return result; 
		}
		
		result.setLocations(Common.findBusyBoxLocations(false, single));
		
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
		cb.jobCallBack(result, 1);
    }
}
