package stericson.busybox.donate.jobs;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import android.widget.TextView;

public class PrepareBinaryJob extends AsyncJob<Result>
{
	private MainActivity activity;
	private String binaryPath;
	public final static int PREPARE_BINARY = 5;

	
	public PrepareBinaryJob(MainActivity activity, String binaryPath)
	{
		super(activity, R.string.preparing, true, false);
		this.activity = activity;
		this.binaryPath = binaryPath;
	}

	@Override
    Result handle()
    {		
		return new PrepareBinary().prepareBinary(activity, binaryPath);
    }

	public void publishCurrentProgress(Object... values)
	{
		this.publishProgress(values);
	}
	
	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
		header.setText(this.activity.getString(R.string.installing) + " " + values[0]);
    }
    
	@Override
    void callback(Result result)
    {
	    activity.jobCallBack(result, PREPARE_BINARY);
    }
}