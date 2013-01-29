package stericson.busybox.jobs;

import stericson.busybox.App;
import stericson.busybox.R;
import stericson.busybox.Activity.MainActivity;
import stericson.busybox.domain.Result;
import android.widget.TextView;

public class InstallJob extends AsyncJob<Result>
{
	private MainActivity activity;
	private String version;
	private String path;
	
	public InstallJob(MainActivity activity, String version, String path)
	{
		super(activity, R.string.installing, true, false);
		this.activity = activity;
		this.version = version;
		this.path = path;
		
	}

	@Override
    Result handle()
    {		
		return new Install().install(activity, this, path, version, false, false, false);
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
	    activity.installDone(result);
    }
}