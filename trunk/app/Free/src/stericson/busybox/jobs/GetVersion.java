package stericson.busybox.jobs;

import stericson.busybox.App;
import stericson.busybox.R;
import stericson.busybox.Activity.MainActivity;
import stericson.busybox.domain.Result;
import stericson.busybox.interfaces.CallBack;

import com.stericson.RootTools.RootTools;

public class GetVersion extends AsyncJob<Result>
{
	private CallBack cb;
	
	public GetVersion(MainActivity activity, CallBack cb)
	{
		super(activity, R.string.initialChecks, false, false);
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
		
		result.setMessage(RootTools.getBusyBoxVersion());
				
		App.getInstance().setCurrentVersion(result.getMessage());
		
	    return result; 
    }

	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);

    }
    
	@Override
    void callback(Result result)
    {
		cb.jobCallBack(result, 2);
    }
}
