package stericson.busybox.donate.jobs;

import java.io.File;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import android.os.Environment;
import android.widget.AdapterView;
import android.widget.TextView;

import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;

public class InstallAppletJob extends AsyncJob<Result>
{
	private MainActivity activity;
	private String applet;
	private String toolbox = "/data/local/toolbox";
	private int position;
	private AdapterView<?> adapter;
	
	public InstallAppletJob(MainActivity activity, String applet, int position, AdapterView<?> adapter)
	{
		super(activity, R.string.installing, true, false);
		this.activity = activity;
		this.applet = applet;
		this.position = position;
		this.adapter = adapter;
	}

	@Override
    Result handle()
    {		
		RootTools.useRoot = true;

		Result result = new Result();
		result.setSuccess(false);
		
		this.publishProgress("Preparing System...");

		CommandCapture command;
		
		try
		{
			if (RootTools.remount("/system", "rw"))
			{

				//ALWAYS run this, I don't care if it does exist...I want to always make sure it is there.
				Common.extractResources(activity, "toolbox", Environment.getExternalStorageDirectory() + "/toolbox-stericson");
								
				try 
				{
					command = new CommandCapture(0, 
							"dd if=" + Environment.getExternalStorageDirectory() + "/toolbox-stericson of=" + toolbox,
							"chmod 0755 " + toolbox);
					Shell.startRootShell().add(command).waitForFinish();
				} 
				catch (Exception e) {}

				
				if (!new File("/system/bin/reboot").exists())
				{
					this.publishProgress("Adding reboot...");

					Common.extractResources(activity, "reboot", Environment.getExternalStorageDirectory() + "/reboot-stericson");
	
					command = new CommandCapture(0, 
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							"dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							toolbox + " chmod 0755 /system/bin/reboot",
							"chmod 0755 /system/bin/reboot");
					Shell.startRootShell().add(command).waitForFinish();
				}
			}	
	
			Common.extractResources(activity, "1.19.4", Environment.getExternalStorageDirectory() + "/busybox-stericson");
				
			this.publishProgress("Installing " + applet + "...");

			if (!RootTools.isBusyboxAvailable())
			{
				command = new CommandCapture(0, 
						"toolbox rm /system/xbin/" + applet,
						"rm /system/xbin/" + applet,
						toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/data/local/busybox",
						"dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/data/local/busybox",
						toolbox + " chmod 0755 /data/local/busybox",
						"chmod 0755 /data/local/busybox",
						"toolbox ln /data/local/busybox /system/xbin/" + applet,
						"ln /data/local/busybox /system/xbin/" + applet,
						"toolbox chmod 0755 /system/xbin/" + applet,
						"chmod 0755 /system/xbin/" + applet,
						"toolbox rm /data/local/busybox",
						"rm /data/local/busybox");
			}
			else
			{
				String location = Common.findBusyBoxLocations(false, true)[0];

				command = new CommandCapture(0, 
						"toolbox rm /system/xbin/" + applet,
						"rm /system/xbin/" + applet,
						"toolbox ln " + location + "busybox /system/xbin/" + applet,
						"ln " + location + "busybox /system/xbin/" + applet,
						"toolbox chmod 0755 /system/xbin/" + applet,
						"chmod 0755 /system/xbin/" + applet);

			}
			
			Shell.startRootShell().add(command).waitForFinish();
			
			File file = new File("/system/xbin/" + applet);
			if (file.exists()) {
				result.setSuccess(true);				
			}
		} 
		catch (Exception e) {}

		RootTools.useRoot = false;

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
	    activity.installAppletDone(result, position, adapter, applet);
    }
}
