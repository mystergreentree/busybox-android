package stericson.busybox.donate.jobs;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import android.widget.TextView;

public class GatherAppletInformation extends AsyncJob<Result>
{
	private MainActivity activity;
	protected TextView view;
	public final static int APPLET_INFO = 0;
	private boolean silent;
    private boolean backup;
	
	public GatherAppletInformation(MainActivity activity, boolean silent, boolean backup)
	{
		super(activity, R.string.gathering, false, false);
		this.activity = activity;
		this.silent = silent;
        this.backup = backup;
		
		activity.showProgress();
		App.getInstance().setProgress(0);
	}

	@Override
    Result handle()
    {		
		AppletInformation appletInformation = new AppletInformation();
		return appletInformation.getAppletInformation(activity, silent, this, Constants.appletsString, backup);
    }

	public void publishCurrentProgress(Object... values)
	{
		this.publishProgress(values);
	}
	
	@Override
    protected synchronized void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		
		activity.updateProgress(Float.parseFloat((String) values[0]));

	}
    
	@Override
    void callback(Result result)
    {
		activity.hideProgress();
		
		if (!silent)
			activity.jobCallBack(result, APPLET_INFO);
    }
}
