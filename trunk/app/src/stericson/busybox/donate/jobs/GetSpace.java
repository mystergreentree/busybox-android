package stericson.busybox.donate.jobs;

import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;

import com.stericson.RootTools.RootTools;

public class GetSpace extends AsyncJob<Result>
{
	private MainActivity activity;
	private String location;
	private CallBack cb;
	
	public GetSpace(MainActivity activity, String location, CallBack cb)
	{
		super(activity, R.string.initialChecks, false, false);
		this.activity = activity;
		this.location = location;
		this.cb = cb;
	}

	@Override
    Result handle()
    {				
		Result result = new Result();

		result.setSpace((float) (RootTools.getSpace(location) / 1000));
				
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
		cb.jobCallBack(result, 3);
    }
}
