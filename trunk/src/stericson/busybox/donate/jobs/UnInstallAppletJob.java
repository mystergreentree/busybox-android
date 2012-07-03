package stericson.busybox.donate.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import android.app.Activity;
import android.widget.AdapterView;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class UnInstallAppletJob extends AsyncJob<Result>
{
	private MainActivity activity;
	private String applet;
	private String path;
	private String toolbox = "/data/local/toolbox";
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
		RootTools.useRoot = true;

		Result result = new Result();
		result.setSuccess(false);
		
		this.publishProgress("Preparing System...");

		try
		{
			if (RootTools.remount("/system", "rw"))
			{
				
				String[] commands = {};
	
				this.publishProgress("Uninstalling " + applet + "...");
	
				commands = new String[] { 
						"toolbox rm " + path + "/" + applet,
						"rm " + path + "/" + applet,
						};
				
				RootTools.sendShell(commands, 0, -1);
				
				File file = new File(path + "/" + applet);
				if (!file.exists()) {
					result.setSuccess(true);				
				}
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
	    activity.uninstallAppletDone(result, position, adapter, applet);
    }
	
	/**
	 * Used to extract certain assets we may need. Can be used by any class to
	 * extract something. Right now this is tailored only for the initial check
	 * sequence but can easily be edited to allow for more customization
	 */
	public void extractResources(Activity activity, String file, String outputPath) {
		String realFile = "";
		if (file.contains("toolbox"))
		{
			realFile = "toolbox.png";
		}
		else if (file.contains("reboot"))
		{
			realFile = "reboot.png";
		}
		else if (file.contains("1.19.4")) {
			realFile = "busybox19_4.png";
		} else if (file.contains("1.18.4")) {
			realFile = "busybox18.png";
		} else if (file.contains("1.18.5")) {
			realFile = "busybox18_5.png";
		} else if (file.contains("1.19.2")) {
			realFile = "busybox19_2.png";
		} else {
			realFile = "busybox19_3.png";
		}

		try {
			InputStream in = activity.getResources().getAssets().open(realFile);
			OutputStream out = new FileOutputStream(
					outputPath);
			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// we have to close these here
			out.flush();
			out.close();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
