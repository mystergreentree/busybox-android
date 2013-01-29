package stericson.busybox.donate.jobs;

import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import android.widget.AdapterView;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class UnInstallAppletJob extends AsyncJob<Result>
{
	private MainActivity activity;
	private String applet;
	private String path;
	private int position;
	private AdapterView<?> adapter;
	
	public UnInstallAppletJob(MainActivity activity, String applet, String path, int position, AdapterView<?> adapter)
	{
		super(activity, R.string.installing, true, false);
		this.activity = activity;
		this.applet = applet;
		this.path = path;
		this.adapter = adapter;
		this.position = position;
	}

	@Override
    Result handle()
    {		
		Result result = new Result();
		result.setSuccess(false);
		
		try
		{
			RootTools.getShell(true);
		}
		catch (Exception e)
		{
			result.setSuccess(false);
			result.setError(activity.getString(R.string.shell_error));
		    return result; 
		}
		
		this.publishProgress("Preparing System...");

		try
		{
			if (RootTools.remount("/system", "rw"))
			{
				this.publishProgress("Uninstalling " + applet + "...");
	
				CommandCapture command = new CommandCapture(0,
						"toolbox rm " + path + "/" + applet,
						"rm " + path + "/" + applet);
				Shell.startRootShell().add(command).waitForFinish();
								
				if (!RootTools.exists(path + "/" + applet)) {
					result.setSuccess(true);
				}
			}
		} 
		catch (Exception e) {}

	    return result; 
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
	    activity.uninstallAppletDone(result, position, adapter, applet);
    }
}
