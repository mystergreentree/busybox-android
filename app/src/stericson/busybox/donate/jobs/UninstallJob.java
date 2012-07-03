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
import android.os.Environment;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Symlink;

public class UninstallJob extends AsyncJob<Result>
{
	private MainActivity activity;
	private String toolbox = "/data/local/toolbox";
	
	public UninstallJob(MainActivity activity)
	{
		super(activity, R.string.uninstalling, true, false);
		this.activity = activity;
	}

	@Override
    Result handle()
    {
		
		Result result = new Result();
		result.setSuccess(true);

		this.publishProgress("Checking System...");
		
		try 
		{
			if (!RootTools.fixUtils(new String[] {"ls", "rm", "ln", "dd", "chmod", "mount"}))
			{
				result.setError(activity.getString(R.string.utilProblem));
				result.setSuccess(false);
			    return result; 
			}
		}
		catch(Exception e) {
			result.setError(activity.getString(R.string.utilProblem));
			result.setSuccess(false);
		    return result; 
		}

		if (RootTools.remount("/system", "rw"))
		{
			this.publishProgress("preparing system...");
			
			String symlink = RootTools.getSymlink(new File("/system/bin/sh"));
			
			if (symlink.toLowerCase().contains("busybox"))
			{
				result.setError("Busybox could not be uninstalled because /system/bin/sh is symlinked to it! \n\n Removing Busybox WILL bootloop your device!");
				result.setSuccess(false);
				return result;
			}
			else if (symlink.toLowerCase().contains("bash"))
			{
				symlink = RootTools.getSymlink(new File("/system/bin/bash"));

				if (symlink.toLowerCase().contains("busybox"))
				{
					result.setError("Busybox could not be uninstalled because /system/bin/bash is symlinked to it! \n\n Removing Busybox WILL bootloop your device!");
					result.setSuccess(false);
					return result;
				}
			}
			
			extractResources(activity, "toolbox", Environment.getExternalStorageDirectory() + "/toolbox-stericson");
			
			String[] commands = {
					"dd if=" + Environment.getExternalStorageDirectory() + "/toolbox-stericson of=" + toolbox,
					"chmod 0755 /data/local/toolbox"
					};
			try 
			{
				RootTools.sendShell(commands, 0, -1);
			} 
			catch (Exception e) {}

			if (RootTools.checkUtil("toolbox"))
			{
				this.publishProgress("Creating required symlinks...");

				//Good to Link the files
				String[] commands_1 = {
						toolbox + " rm /system/bin/chmod",
						toolbox + " rm /system/xbin/chmod",
						toolbox + " ln -s " + toolbox + " /system/bin/chmod",
						toolbox + " rm /system/bin/ls",
						toolbox + " rm /system/xbin/ls",
						toolbox + " ln -s " + toolbox + " /system/bin/ls",
						toolbox + " rm /system/bin/cat",
						toolbox + " rm /system/xbin/cat",
						toolbox + " ln -s " + toolbox + " /system/bin/cat",
						toolbox + " rm /system/bin/ln",
						toolbox + " rm /system/xbin/ln",
						toolbox + " ln -s " + toolbox + " /system/bin/ln",
						toolbox + " rm /system/bin/df",
						toolbox + " rm /system/xbin/df",
						toolbox + " ln -s " + toolbox + " /system/bin/df",
						toolbox + " rm /system/bin/mount",
						toolbox + " rm /system/xbin/mount",
						toolbox + " ln -s " + toolbox + " /system/bin/mount",
						toolbox + " rm /system/bin/rm",
						toolbox + " rm /system/xbin/rm",
						toolbox + " ln -s " + toolbox + " /system/bin/rm",
						toolbox + " rm /system/bin/ps",
						toolbox + " rm /system/xbin/ps",
						toolbox + " ln -s " + toolbox + " /system/bin/ps",
						toolbox + " rm /system/bin/mkdir",
						toolbox + " rm /system/xbin/mkdir",
						toolbox + " ln -s " + toolbox + " /system/bin/mkdir",
						toolbox + " rm /system/bin/kill",
						toolbox + " rm /system/xbin/kill",
						toolbox + " ln -s " + toolbox + " /system/bin/kill",
						toolbox + " rm /system/bin/id",
						toolbox + " rm /system/xbin/id",
						toolbox + " ln -s " + toolbox + " /system/bin/id",
						toolbox + " rm /system/bin/dd",
						toolbox + " rm /system/xbin/dd",
						toolbox + " ln -s " + toolbox + " /system/bin/dd",
						toolbox + " rm /system/bin/insmod",
						toolbox + " rm /system/xbin/insmod",
						toolbox + " ln -s " + toolbox + " /system/bin/insmod"
						};
				try 
				{
					RootTools.sendShell(commands_1, 0, -1);
				} 
				catch (Exception e) {
					RootTools.log(e.toString());
				}
				
				try
				{
					if (RootTools.fixUtils(new String[] {"ls", "rm", "ln", "dd", "chmod", "mount"}))
					{		
						try
						{	
							for (String path : RootTools.getPath())
							{
								this.publishProgress("checking for symlinks in " + path);
		
								RootTools.remount(path, "rw");
								
								for (Symlink link : RootTools.getSymlinks(path))
								{
									this.publishProgress("inspecting " + link.getFile().toString() + " symlinked to " + link.getSymlinkPath());
		
									if (link.getSymlinkPath().toString().trim().toLowerCase().endsWith("busybox"))
									{
										if (link.getFile().toString().equals("sh") || link.getFile().toString().equals("bash"))
										{
											result.setError("Busybox could not be uninstalled because removing Busybox WILL bootloop your device!");
											result.setSuccess(false);
											return result;
										}
										
										this.publishProgress("removing symlink for " + link.getFile().toString() + " in " + path);
		
										RootTools.sendShell(toolbox + " rm " + path + "/" + link.getFile().toString(), -1);
									}
								}
								
								RootTools.remount(path, "ro");
							}
							
							this.publishProgress("Removing BusyBox!");
		
							RootTools.findBinary("busybox");

							for (String binaryPath : RootTools.lastFoundBinaryPaths)
							{
								RootTools.remount(binaryPath, "rw");
								RootTools.sendShell(toolbox + " rm " + binaryPath + "/busybox", -1);
								RootTools.remount(binaryPath, "ro");
							}
						}
						catch (Exception e) {
							RootTools.log(e.toString());
						};
					}
				}
				catch (Exception e)
				{
					//Do nothing
				}
			}
			
			if (!new File("/system/bin/reboot").exists())
			{
				extractResources(this.activity, "reboot", Environment.getExternalStorageDirectory() + "/reboot-stericson");

				String[] commands_0 = {
						toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
						toolbox + " chmod 0755 /system/bin/reboot"
						};
				try 
				{
					RootTools.sendShell(commands_0, 0, -1);
				} 
				catch (Exception e) {}
			}
		}
		
	    return result; 
    }

	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
		header.setText(this.activity.getString(R.string.uninstalling) + " " + values[0]);
    }
    
	@Override
    void callback(Result result)
    {
	    activity.uninstallDone(result);
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
