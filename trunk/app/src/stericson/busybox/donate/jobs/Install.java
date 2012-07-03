package stericson.busybox.donate.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import android.content.Context;
import android.os.Environment;

import com.stericson.RootTools.RootTools;

public class Install
{
	private String toolbox = "/data/local/toolbox";

	public Result install(Context activity, InstallJob ij, String path, String version, boolean silent, boolean clean, boolean update_only)
	{
		RootTools.useRoot = true;

		Result result = new Result();
		result.setSuccess(true);

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
				extractResources(activity, "toolbox", Environment.getExternalStorageDirectory() + "/toolbox-stericson");
				
				String[] commands = {
						"dd if=" + Environment.getExternalStorageDirectory() + "/toolbox-stericson of=" + toolbox,
						"/system/bin/toolbox dd if=" + Environment.getExternalStorageDirectory() + "/toolbox-stericson of=" + toolbox,
						"chmod 0755 " + toolbox,
						"/system/bin/toolbox chmod 0755 " + toolbox
						};
				try 
				{
					RootTools.sendShell(commands, 0, -1);
				} 
				catch (Exception ignore) {}

				
				if (!new File("/system/bin/reboot").exists())
				{
					if (!silent)
						ij.publishCurrentProgress("Adding reboot...");

					extractResources(activity, "reboot", Environment.getExternalStorageDirectory() + "/reboot-stericson");
	
					commands = new String[] {
							toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							"/system/bin/toolbox dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							"dd if=" + Environment.getExternalStorageDirectory() + "/reboot-stericson of=/system/bin/reboot",
							toolbox + " chmod 0755 /system/bin/reboot",
							"/system/bin/toolbox chmod 0755 /system/bin/reboot",
							"chmod 0755 /system/bin/reboot"};
	
					RootTools.sendShell(commands, 0, -1);
				}
			}	
	
			extractResources(activity, version, Environment.getExternalStorageDirectory() + "/busybox-stericson");
			
			if (path == null || path.equals("")) 
			{
				path = "/system/bin/";
			}
	
			if (!path.endsWith("/")) 
			{
				path = path + "/";
			}
			
			String[] commands = {};

			if (!silent)
				ij.publishCurrentProgress("Installing Busybox...");

			commands = new String[] {
					toolbox + " rm " + path + "busybox",
					"/system/bin/toolbox rm " + path + "busybox",
					"rm " + path + "busybox",
					"dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
					toolbox + " dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
					"/system/bin/toolbox dd if=" + Environment.getExternalStorageDirectory() + "/busybox-stericson of=" + path + "busybox",
					toolbox + " chmod 0755 " + path + "busybox",
					"/system/bin/toolbox chmod 0755 " + path + "busybox",
					"chmod 0755 " + path + "busybox"};

			RootTools.sendShell(commands, 0, -1);

			if (!new File(path + "busybox").exists())
			{
				commands = new String[] {
						"cat " + Environment.getExternalStorageDirectory() + "/busybox-stericson > " + path + "busybox",
						"cp " + Environment.getExternalStorageDirectory() + "/busybox-stericson " + path + "busybox",
						toolbox + " cat " + Environment.getExternalStorageDirectory() + "/busybox-stericson > " + path + "busybox",
						toolbox + " cp " + Environment.getExternalStorageDirectory() + "/busybox-stericson " + path + "busybox",
						toolbox + " chmod 0755 " + path + "busybox",
						"/system/bin/toolbox cat " + Environment.getExternalStorageDirectory() + "/busybox-stericson > " + path + "busybox",
						"/system/bin/toolbox cp " + Environment.getExternalStorageDirectory() + "/busybox-stericson " + path + "busybox",
						"/system/bin/toolbox chmod 0755 " + path + "busybox",
						"chmod 0755 " + path + "busybox"};
				
				RootTools.sendShell(commands, 0, -1);

			}

			if (!update_only)
			{
				if (App.getInstance().getItemList() == null || !App.getInstance().isSmartInstall())
				{
                    commands = new String[] {
                                    path + "busybox --install -s " + path };

					RootTools.sendShell(commands, 0, -1);
				}
				else
				{
					for (Item item : App.getInstance().getItemList())
					{
	
						if (item.getOverwriteall() ? true : item.getOverWrite())
						{
							if (!silent)
								ij.publishCurrentProgress("Setting up " + item.getApplet());
	
							commands = new String[]{
									toolbox + " rm " + path + item.getApplet(),
									"/system/bin/toolbox rm " + path + item.getApplet(),
									"rm " + path + item.getApplet(),
									toolbox + " ln -s " + path + "busybox " + path + item.getApplet(),
									"/system/bin/toolbox ln -s " + path + "busybox " + path + item.getApplet(),
									"ln -s " + path + "busybox " + path + item.getApplet()};
							
							RootTools.sendShell(commands, 0, -1);
							
							//clean up
							if (clean)
							{
								try {
									if (!silent)
										ij.publishCurrentProgress("Cleaning up...");
									
									RootTools.findBinary(item.getApplet());
									for (String applet_path : RootTools.lastFoundBinaryPaths)
									{
										if (!path.contains(applet_path))
										{
											RootTools.log("Cleaning up Applet: " + item.getApplet());
											commands = new String[] {
													toolbox + " rm " + applet_path + "/" + item.getApplet(),
													 "/system/bin/toolbox rm " + applet_path + "/" + item.getApplet(),
													"rm " + applet_path + "/" + item.getApplet()
											};
											
											RootTools.sendShell(commands, 0, -1);
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
						else if (item.getRemove())
						{
							if (!silent)
								ij.publishCurrentProgress("Removing " + item.getApplet());
	
							RootTools.sendShell(new String[] {  toolbox + " rm " + item.getAppletPath() + "/" + item.getApplet(),
																"/system/bin/toolbox rm " + item.getAppletPath() + "/" + item.getApplet(),
																"rm " + item.getAppletPath() + "/" + item.getApplet()}, 0, -1);
							
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
										commands = new String[] {
												toolbox + " rm " + applet_path + "/" + item.getApplet(),
												"/system/bin/toolbox rm " + applet_path + "/" + item.getApplet(),
												"rm " + applet_path + "/" + item.getApplet()
										};
											
										RootTools.sendShell(commands, 0, -1);
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

			File file = new File(path + "busybox");
			if (file.exists()) {
				String[] locations = Common.findBusyBoxLocations(true, false);
				if (locations.length >= 1) {
					int i = 0;
					while (i < locations.length) {
						if (!locations[i].equals(path)) {
							//Removing old copies
							//TODO this may need to be moved, in the future this will become a lot larger.
							RootTools.remount(locations[i], "RW");
							final String[] command = {
									toolbox + " rm " + locations[i] + "busybox",
									"/system/bin/toolbox rm " + locations[i] + "busybox",
									"rm " + locations[i] + "busybox",
									toolbox + " ln -s "
											+ path
											+ "busybox "
											+ locations[i],
									"/system/bin/toolbox ln -s "
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
		catch (Exception e) {}

		RootTools.useRoot = false;

	    return result; 
	}
	
	/**
	 * Used to extract certain assets we may need. Can be used by any class to
	 * extract something. Right now this is tailored only for the initial check
	 * sequence but can easily be edited to allow for more customization
	 */
	public void extractResources(Context activity, String file, String outputPath) {
		String realFile = "";
		if (file.contains("toolbox"))
		{
			realFile = "toolbox.png";
		}
		else if (file.contains("reboot"))
		{
			realFile = "reboot.png";
		}
		else if (file.contains("1.20.1")) {
			realFile = "busybox1.20.1.png";
		}
		else if (file.contains("1.20.0")) {
			realFile = "busybox20_0.png";
		}
		else if (file.contains("1.19.4")) {
			realFile = "busybox19_4.png";
		}
		else if (file.contains("1.19.2")) {
			realFile = "busybox19_2.png";
		}
		else {
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
