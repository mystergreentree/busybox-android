package stericson.busybox.donate.jobs;

import stericson.busybox.donate.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;

import com.stericson.RootTools.RootTools;

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
