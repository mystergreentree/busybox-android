package stericson.busybox.donate.jobs;

import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import android.widget.TextView;

public class GatherAppletInformation extends AsyncJob<Result>
{
	private MainActivity activity;
	protected TextView view;
	public static int APPLET_INFO = 0;
	private boolean silent;
	
	public GatherAppletInformation(MainActivity activity, boolean silent)
	{
		super(activity, R.string.gathering, false, false);
		this.activity = activity;
		this.silent = silent;
	}

	@Override
    Result handle()
    {		
		AppletInformation appletInformation = new AppletInformation();
		return appletInformation.getAppletInformation(activity, silent, this, Constants.appletsString);
    }

	public void publishCurrentProgress(Object... values)
	{
		this.publishProgress(values);
	}
	
	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		try
		{
			//TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
			activity.view1.setText(this.activity.getString(R.string.gatheringAbout) + " " + values[0]);
		} catch (Exception e) {}
		
		try
		{
			//TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
			activity.view2.setText(this.activity.getString(R.string.gatheringAbout) + " " + values[0]);
		} catch (Exception e) {}
		
    }
    
	@Override
    void callback(Result result)
    {
		if (!silent)
			activity.jobCallBack(result, APPLET_INFO);
    }
}
