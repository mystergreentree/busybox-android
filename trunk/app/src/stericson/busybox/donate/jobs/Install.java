package stericson.busybox.donate.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import android.content.Context;
import android.os.Environment;

import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;

public class Install
{
	private String toolbox = "/data/local/toolbox";
	private String binaryLocation = "";

	public Result install(Context activity, InstallJob ij, String path, String version, boolean silent, boolean clean, boolean update_only)
	{
		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		binaryLocation = activity.getFilesDir().toString() + "/bb/busybox";
		
		Result result = new Result();
		result.setSuccess(true);

		if (!RootTools.exists(binaryLocation))
		{
			result.setSuccess(false);
			result.setError(activity.getString(R.string.fatal_error));
		    return result; 
		} else {
			//Check the integrity of the file
			String tmp_version = RootTools.getBusyBoxVersion(activity.getFilesDir().toString() + "/bb");
			
			if (tmp_version.equals(""))
			{
				result.setSuccess(false);
				result.setError(activity.getString(R.string.binary_verification_failed_install));
				return result;
			}
		}
		
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
		
		CommandCapture command;

		if (!silent)
			ij.publishCurrentProgress("Checking System...");
		
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
		
		if (!silent)
			ij.publishCurrentProgress("Preparing System...");

		try
		{
			if (RootTools.remount("/system", "rw"))
			{

				//ALWAYS run this, I don't care if it does exist...I want to always make sure it is there.
				Common.extractResources(activity, "toolbox", sdPath + "/toolbox-stericson");
				
				try 
				{
					command = new CommandCapture(0, 
							"dd if=" + sdPath + "/toolbox-stericson of=" + toolbox,
							"/system/bin/toolbox dd if=" + sdPath + "/toolbox-stericson of=" + toolbox,
							"chmod 0755 " + toolbox,
							"/system/bin/toolbox chmod 0755 " + toolbox);
					Shell.startRootShell().add(command).waitForFinish();
				} 
				catch (Exception ignore) {}
				
				if (!new File("/system/bin/reboot").exists())
				{
					if (!silent)
						ij.publishCurrentProgress("Adding reboot...");

					Common.extractResources(activity, "reboot", sdPath + "/reboot-stericson");
		
					command = new CommandCapture(0, 
							toolbox + " dd if=" + sdPath + "/reboot-stericson of=/system/bin/reboot",
							"/system/bin/toolbox dd if=" + sdPath + "/reboot-stericson of=/system/bin/reboot",
							"dd if=" + sdPath + "/reboot-stericson of=/system/bin/reboot",
							toolbox + " chmod 0755 /system/bin/reboot",
							"/system/bin/toolbox chmod 0755 /system/bin/reboot",
							"chmod 0755 /system/bin/reboot");
					Shell.startRootShell().add(command).waitForFinish();
				}
			}	
			
			if (path == null || path.equals("")) 
			{
				path = "/system/bin/";
			}
	
			if (!path.endsWith("/")) 
			{
				path = path + "/";
			}
			
			if (!silent)
				ij.publishCurrentProgress("Installing Busybox...");

			command = new CommandCapture(0, 
					toolbox + " rm " + path + "busybox",
					"/system/bin/toolbox rm " + path + "busybox",
					"rm " + path + "busybox",
					"dd if=" + binaryLocation + " of=" + path + "busybox",
					toolbox + " dd if=" + binaryLocation + " of=" + path + "busybox",
					"/system/bin/toolbox dd if=" + binaryLocation + " of=" + path + "busybox",
					toolbox + " chmod 0755 " + path + "busybox",
					"/system/bin/toolbox chmod 0755 " + path + "busybox",
					"chmod 0755 " + path + "busybox");
			Shell.startRootShell().add(command).waitForFinish();
			
			if (!new File(path + "busybox").exists())
			{
				command = new CommandCapture(0, 
						"cat " + binaryLocation + " > " + path + "busybox",
						"cp " + binaryLocation + " " + path + "busybox",
						toolbox + " cat " + binaryLocation + " > " + path + "busybox",
						toolbox + " cp " + binaryLocation + " " + path + "busybox",
						toolbox + " chmod 0755 " + path + "busybox",
						"/system/bin/toolbox cat " + binaryLocation + " > " + path + "busybox",
						"/system/bin/toolbox cp " + binaryLocation + " " + path + "busybox",
						"/system/bin/toolbox chmod 0755 " + path + "busybox",
						"chmod 0755 " + path + "busybox");
				Shell.startRootShell().add(command).waitForFinish();

			}

			if (!update_only)
			{
				if (App.getInstance().getItemList() == null || !App.getInstance().isSmartInstall())
				{
    				command = new CommandCapture(0, path + "busybox --install -s " + path);
    				Shell.startRootShell().add(command).waitForFinish();
				}
				else
				{
					for (Item item : App.getInstance().getItemList())
					{
						if (item.getOverwriteall() ? true : item.getOverWrite())
						{
							//Make sure the binary has the applet
							if (App.getInstance().getAvailableApplets().contains(item.getApplet())) {
								if (!silent)
									ij.publishCurrentProgress("Setting up " + item.getApplet());
		
			    				command = new CommandCapture(0, 
										toolbox + " rm " + path + item.getApplet(),
										"/system/bin/toolbox rm " + path + item.getApplet(),
										"rm " + path + item.getApplet(),
										toolbox + " ln -s " + path + "busybox " + path + item.getApplet(),
										"/system/bin/toolbox ln -s " + path + "busybox " + path + item.getApplet(),
										"ln -s " + path + "busybox " + path + item.getApplet());
			    				Shell.startRootShell().add(command).waitForFinish();
								
								//clean up
								if (clean)
								{
									try {
										if (!silent)
											ij.publishCurrentProgress("Cleaning up...");
										
										RootTools.findBinary(item.getApplet());
										List<String> paths = new ArrayList<String>();
										paths.addAll(RootTools.lastFoundBinaryPaths);
										for (String applet_path : paths)
										{
											if (!path.contains(applet_path))
											{
												RootTools.log("Cleaning up Applet: " + item.getApplet());
												
							    				command = new CommandCapture(0, 
														toolbox + " rm " + applet_path + "/" + item.getApplet(),
														 "/system/bin/toolbox rm " + applet_path + "/" + item.getApplet(),
														"rm " + applet_path + "/" + item.getApplet());
							    				Shell.startRootShell().add(command).waitForFinish();
											}
											else
											{
												RootTools.log("Skipping clean on " + item.getApplet() + " located at " + applet_path);
											}
										}
									}
									catch (Exception ignore) {}
								}
							}
						}
						else if (item.getRemove())
						{
							if (!silent)
								ij.publishCurrentProgress("Removing " + item.getApplet());

		    				command = new CommandCapture(0, 
		    						toolbox + " rm " + item.getAppletPath() + "/" + item.getApplet(),
									"/system/bin/toolbox rm " + item.getAppletPath() + "/" + item.getApplet(),
									"rm " + item.getAppletPath() + "/" + item.getApplet());		    				
		    				Shell.startRootShell().add(command).waitForFinish();
							
							//clean up
							if (clean)
							{
								try {
									if (!silent)
										ij.publishCurrentProgress("Cleaning up...");
									
									RootTools.findBinary(item.getApplet());
									for (String applet_path : RootTools.lastFoundBinaryPaths)
									{
										RootTools.log("Cleaning up Applet: " + item.getApplet());

										command = new CommandCapture(0, 
												toolbox + " rm " + applet_path + "/" + item.getApplet(),
												"/system/bin/toolbox rm " + applet_path + "/" + item.getApplet(),
												"rm " + applet_path + "/" + item.getApplet());		    				
					    				Shell.startRootShell().add(command).waitForFinish();
									}
								}
								catch (Exception ignore) {}
							}
						}
					}
				}
			}
			
			if (!silent)
				ij.publishCurrentProgress("Removing old copies...");

			if (RootTools.exists(path + "busybox")) {
				String[] locations = Common.findBusyBoxLocations(true, false);
				if (locations.length >= 1) {
					int i = 0;
					while (i < locations.length) {
						if (!locations[i].equals(path)) {
							//Removing old copies
							//TODO this may need to be moved, in the future this will become a lot larger.
							RootTools.remount(locations[i], "RW");

							command = new CommandCapture(0, 
									toolbox + " rm " + locations[i] + "busybox",
									"/system/bin/toolbox rm " + locations[i] + "busybox",
									"rm " + locations[i] + "busybox",
									toolbox + " ln -s " + path + "busybox " + locations[i],
									"/system/bin/toolbox ln -s " + path + "busybox " + locations[i],
									"ln -s " + path + "busybox " + locations[i]);
							
		    				Shell.startRootShell().add(command).waitForFinish();
		    				
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
		catch (Exception e) {}

		App.getInstance().setInstalled(RootTools.isBusyboxAvailable());

	    return result; 
	}
}
