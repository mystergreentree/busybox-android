package stericson.busybox.jobs;

import stericson.busybox.R;
import stericson.busybox.Activity.MainActivity;
import stericson.busybox.domain.Result;
import stericson.busybox.interfaces.CallBack;

import com.stericson.RootTools.RootTools;

public class GetSpace extends AsyncJob<Result>
{
	private String location;
	private CallBack cb;
	
	public GetSpace(MainActivity activity, String location, CallBack cb)
	{
		super(activity, R.string.initialChecks, false, false);
		this.location = location;
		this.cb = cb;
	}

	@Override
    Result handle()
    {				
		Result result = new Result();
		result.setSuccess(true);

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
		
		result.setSpace((float) (RootTools.getSpace(location) / 1000));
				
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
