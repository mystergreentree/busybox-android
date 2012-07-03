package stericson.busybox.donate.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.services.DBService;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class RestoreBackup extends AsyncJob<Result>
{
	private MainActivity activity;
	private List<String> restoredPaths = new ArrayList<String>();
	private String toolbox = "/data/local/toolbox";
	private String storagePath = "";
	private Result result = new Result();

	public RestoreBackup(MainActivity activity)
	{
		super(activity, R.string.restoring, true, false);
		this.activity = activity;
		storagePath = activity.getFilesDir().toString();
	}

	@Override
    Result handle()
    {		
		String applet = "";
		String path = "";
		String symlink = "";
		
		RootTools.useRoot = true;

		result.setSuccess(true);
		
		this.publishProgress("Preparing System...");
		
		try
		{				
			DBService db = new DBService(activity);
			
			List<Item> items = db.getApplets();
			
			for (Item item : items)
			{
				applet = item.getApplet();
				path = item.getBackupFilePath();
				
				if (path.endsWith("/"))
				{
					path.substring(0, path.length() - 1);
				}
				
				symlink = item.getBackupSymlink();
				
				if (!symlink.equals(""))
				{
					this.publishProgress("Restoring " + item.getApplet() + "...");

					if (!restoreSymlink(applet, path, symlink))
					{
						result.setSuccess(false);
					}
					else
					{
						clean(applet, path);
					}
				}
				else if (!path.equals(""))
				{
					this.publishProgress("Restoring " + item.getApplet() + "...");

					if (!restoreApplet(applet, path))
					{
						result.setSuccess(false);
					}
					else
					{
						clean(applet, path);
				
					}
				}
			}
		} 
		catch (Exception e) {
			result.setSuccess(false);
		}

		RootTools.useRoot = false;

		RootTools.remount("/system", "ro");
	    return result; 
    }

	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
		header.setText(this.activity.getString(R.string.restoring) + " " + values[0]);
    }
    
	@Override
    void callback(Result result)
    {
		activity.restoreDone(result);
    }
	
	public void clean(String applet, String path)
	{
		try {
			this.publishProgress("Cleaning up...");
			
			RootTools.findBinary(applet);
			for (String applet_path : RootTools.lastFoundBinaryPaths)
			{
				if (!path.contains(applet_path))
				{
					String mountType = RootTools.getMountedAs(path);
					if (RootTools.remount(path, "rw"))
					{
						RootTools.log("Cleaning up: " + applet);
						String[] commands = new String[] {
								toolbox + " rm " + applet_path + "/" + applet,
								 "/system/bin/toolbox rm " + applet_path + "/" + applet,
								"rm " + applet_path + "/" + applet
						};
						
						RootTools.sendShell(commands, 0, -1);
					}
					
					RootTools.remount(path, mountType);
				}
				else
				{
					RootTools.log("Skipping clean on " + applet + " located at " + applet_path);
				}
			}
		}
		catch (Exception ignore) {}
	}
	
	public boolean restoreApplet(String applet, String path)
	{
		if (!applet.equals("") && !path.equals("") && new File(storagePath + "/" + applet).exists())
		{
			String mountType = "rw";
			try
			{
				mountType = RootTools.getMountedAs(path);

				RootTools.remount(path, "rw");
				String[] commands = new String[]
				        {
							toolbox + " rm " + path + "/" + applet,
							"/system/bin/toolbox rm " + path + "/" + applet,
							"rm " + path + "/" + applet,
							"dd if=" + storagePath + "/" + applet + " of=" + path + "/" + applet,
							toolbox + " dd if=" + storagePath + "/" + applet + " of=" + path + "/" + applet,
							"/system/bin/toolbox dd if=" + storagePath + "/" + applet + " of=" + path + "/" + applet,
							toolbox + " chmod 0755 " + path + "/" + applet,
							"/system/bin/toolbox chmod 0755 " + path + "/" + applet,
							"chmod 0755 " + path + "/" + applet
						};
				
				RootTools.sendShell(commands, 0, -1);
				
				if (!new File(path + "/" + applet).exists())
				{
					commands = new String[] {
							"cat " + storagePath + "/" + applet + " > " + path + "/" + applet,
							"cp " + storagePath + "/" + applet + " " + path + "/" + applet,
							toolbox + " cat " + storagePath + "/" + applet + " > " + path + "/" + applet,
							toolbox + " cp " + storagePath + "/" + applet + " " + path + "/" + applet,
							toolbox + " chmod 0755 " + path + "/" + applet,
							"/system/bin/toolbox cat " + storagePath + "/" + applet + " > " + path + "/" + applet,
							"/system/bin/toolbox cp " + storagePath + "/" + applet + " " + path + "/" + applet,
							"/system/bin/toolbox chmod 0755 " + path + "/" + applet,
							"chmod 0755 " + path + "/" + applet};
					
					RootTools.sendShell(commands, 0, -1);
					
					if (!new File(path + "/" + applet).exists())
					{
						return false;
					}
				}
			}
			catch (Exception ingore)
			{
				return false;
			}
			finally
			{
				RootTools.remount(path, mountType);
			}
		
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean restoreSymlink(String applet, String path, String symlink)
	{
		if (!applet.equals("") && !path.equals("") && !symlink.equals(""))
		{
			if (symlink.contains("/busybox") && !restoredPaths.contains(symlink.replace("/busybox", "")))
			{
				if (restoreApplet("busybox", symlink.replace("/busybox", "")))
				{
					restoredPaths.add(symlink.replace("/busybox", ""));
				}
			}
			
			try
			{
				String[] commands = new String[]
				        {
							toolbox + " rm " + path + "/" + applet,
							"/system/bin/toolbox rm " + path + "/" + applet,
							"rm " + path + "/" + applet,
							toolbox + " ln -s " + symlink + " " + path + "/" + applet,
							"/system/bin/toolbox ln -s " + symlink + " " + path + "/" + applet,
							"ln -s " + symlink + " " + path + "/" + applet
						};
				
				RootTools.sendShell(commands, 0, -1);
			}
			catch (Exception ignore)
			{
				return false;
			}
			
			if (!RootTools.getSymlink(new File(path + "/" + applet)).equals(symlink))
			{
				return false;
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}

}
