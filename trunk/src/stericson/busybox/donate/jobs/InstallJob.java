package stericson.busybox.donate.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.StaticThings;
import stericson.busybox.donate.Activity.InstallActivity;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Environment;
import android.widget.TextView;

import com.stericson.RootTools.Permissions;
import com.stericson.RootTools.RootTools;

public class InstallJob extends AsyncJob<Result>
{
	private InstallActivity activity;
	private String version;
	private String path;
	private String toolbox = "/data/local/toolbox";
	
	public InstallJob(InstallActivity activity, String version, String path)
	{
		super(activity, R.string.installing, true, false);
		this.activity = activity;
		this.version = version;
		this.path = path;
	}

	@Override
    Result handle()
    {		
		this.publishProgress("Preparing System...");

		try
		{
			if (RootTools.remount("/system", "rw"))
			{

				//ALWAYS run this, I don't care if it does exist...I want to always make sure it is there.
				extractResources(activity, "toolbox", Environment.getExternalStorageDirectory() + "/toolbox-stericson");
				
				String[] commands = {
						"dd if=" + Environment.getExternalStorageDirectory() + "/toolbox-stericson of=" + toolbox,
						"chmod 0755 " + toolbox
						};
				try 
				{
					RootTools.sendShell(commands, 0, -1);
				} 
				catch (Exception e) {}

				
				if (!new File("/system/bin/reboot").exists())
				{
					this.publishProgress("Adding reboot...");

					extractResources(activity, "reboot", Environment.getExternalStorageDirectory() + "/reboot-stericson");
	
					commands = new String[] {
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							"dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							toolbox + " chmod 0755 /system/bin/reboot",
							"chmod 0755 /system/bin/reboot"};
	
					RootTools.sendShell(commands, 0, -1);
				}
			}	
	
			extractResources(activity, version, Environment.getExternalStorageDirectory() + "/busybox-stericson");
			
			if (!path.endsWith("/")) 
			{
				path = path + "/";
			}
			
			if (path != null) 
			{
	
				String[] commands = {};
	
				if (App.getInstance().getItemList() != null)
				{
					this.publishProgress("Installing Busybox...");

					commands = new String[] {
							toolbox + " rm " + path + "busybox",
							"rm " + path + "busybox",
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
							"dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
							toolbox + " chmod 0755 " + path + "busybox",
							"chmod 0755 " + path + "busybox",
							path + "busybox --install -s " + path };

					RootTools.sendShell(commands, 0, -1);
	
					for (Item item : App.getInstance().getItemList())
					{

						if (item.getOverWrite())
						{
							this.publishProgress("Setting up " + item.getApplet());

							commands = new String[]{
									toolbox + " rm " + path + item.getApplet(),
									"rm " + path + item.getApplet(),
									toolbox + " ln -s " + path + "busybox " + path + item.getApplet(),
									"ln -s " + path + "busybox " + path + item.getApplet()};
							
							RootTools.sendShell(commands, 0, -1);
						}
						else if (item.getRemove())
						{
							this.publishProgress("Removing " + item.getApplet());

							RootTools.sendShell(new String[] {  toolbox + " rm " + item.getAppletPath() + "/" + item.getApplet(),
																"rm " + item.getAppletPath() + "/" + item.getApplet()}, 0, -1);
						}
					}
				}
				else
				{
					this.publishProgress("Installing Busybox...");

					commands = new String[] { 
							toolbox + " rm " + path + "busybox",
							"rm " + path + "busybox",
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
							"dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
							toolbox + " chmod 0755 " + path + "busybox",
							"chmod 0755 " + path + "busybox",
							path + "busybox --install -s " + path };
					
					RootTools.sendShell(commands, 0, -1);
				}
				
				this.publishProgress("Removing old copies...");

				File file = new File(path + "busybox");
				if (file.exists()) {
					String[] locations = activity.findBusyBoxLocations();
					if (locations.length >= 1) {
						int i = 0;
						while (i < locations.length) {
							if (!locations[i].equals(path)) {
								//Removing old copies
								//TODO this may need to be moved, in the future this will become a lot larger.
								RootTools.remount(locations[i], "RW");
								final String[] command = {
										toolbox + " rm " + locations[i] + "busybox",
										"rm " + locations[i] + "busybox",
										toolbox + " ln -s "
												+ path
												+ "busybox "
												+ locations[i],
										"ln -s " + path
												+ "busybox "
												+ locations[i]
								};
	
								RootTools.sendShell(command, 0, -1);
								
								if (new File(locations[i] + "busybox").exists()) {
									RootTools.log("BusyBox Installer", "The file was not removed: " + locations[i] + "busybox");
								}
								else {
									RootTools.log("BusyBox Installer", "The file was successfully removed: " + locations[i] + "busybox");
								}
							}
							i++;
						}
					}
				}
			}
			else
			{			
				if (App.getInstance().getItemList() != null)
				{
					this.publishProgress("Installing Busybox...");

					String[] commands = {
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/system/bin/busybox",
							"dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/system/bin/busybox",
							toolbox + " chmod 0755 /system/bin/busybox",
							"chmod 0755 /system/bin/busybox"};
						
					RootTools.sendShell(commands, 0, -1);
	
					for (Item item : App.getInstance().getItemList())
					{
						if (item.getOverWrite())
						{
							this.publishProgress("Setting up " + item.getApplet());

							commands = new String[]{
									toolbox + " rm /system/bin/" + item.getApplet(),
									"rm /system/bin/" + item.getApplet(),
									toolbox + " ln -s /system/bin/busybox /system/bin/" + item.getApplet(),
									"ln -s /system/bin/busybox /system/bin/" + item.getApplet()};
							
							RootTools.sendShell(commands, 0, -1);
						}
						else if (item.getRemove())
						{
							this.publishProgress("Removing " + item.getApplet());

							RootTools.sendShell(new String[] {	toolbox + " rm " + item.getAppletPath() + "/" + item.getApplet(),
																"rm " + item.getAppletPath() + "/" + item.getApplet()}, 0, -1);
						}
					}
				}
				else
				{
					this.publishProgress("Installing Busybox...");

					String[] commands = {
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/system/bin/busybox",
							"dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=/system/bin/busybox",
							toolbox + " chmod 0755 /system/bin/busybox",
							"chmod 0755 /system/bin/busybox",
							"/system/bin/busybox --install -s /system/bin" };
					
					RootTools.sendShell(commands, 0, -1);
				}
			}
		} 
		catch (Exception e) {}

		Result result = new Result();
		result.setSuccess(true);
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
	    activity.done();
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
		else if (file.contains("1.17.1")) {
			realFile = "busybox17.png";
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
