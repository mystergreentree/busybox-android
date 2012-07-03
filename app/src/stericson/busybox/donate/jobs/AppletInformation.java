package stericson.busybox.donate.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.Common;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.services.DBService;
import android.content.Context;

import com.stericson.RootTools.RootTools;

public class AppletInformation
{

	private DBService dbService;
	private List<Item> itemList;
	private Item item;
	private String storagePath = "";
	private String toolbox = "/data/local/toolbox";

	
	public Result getAppletInformation(Context context, boolean updating, GatherAppletInformation gai, String [] applets)
	{

		storagePath = context.getFilesDir().toString();
		dbService = new DBService(context);
		itemList = new ArrayList<Item>();

		RootTools.useRoot = false;
					
		String path = Common.getSingleBusyBoxPath();
		File file = new File(path + "busybox");
		if (file.exists())
		{
			file = new File(storagePath + "/busybox");
			if (!file.exists())
			{
				makeBackup("busybox", path);
			}
		}
		
		for (String applet : applets)
		{
			if (gai != null && gai.isCancelled())
			{
				closeDb();
				return null;
			}
			
			if (!updating && gai != null)
				gai.publishCurrentProgress(applet);

			if (updating || !dbService.isReady())
			{
				item = dbService.checkForApplet(applet);
				
				if (item == null)
				{
					try {
						
						item = new Item();
						
						item.setApplet(applet);
						
						findApplet(applet);
					}
					catch (Exception e) {}
				}
				else
				{
					file = new File(item.getAppletPath() + "/" + applet);
					if (file.exists())
					{
						String symlink = RootTools.getSymlink(file);
						if (!symlink.equals(item.getSymlinkedTo()))
						{
							item.setSymlinkedTo(symlink);
							dbService.insertOrUpdateRow(item);
						}
					}
					else
					{
						findApplet(applet);
					}
				}
			}
		}
		
		itemList = dbService.getApplets();

		RootTools.useRoot = true;
		
		Result result = new Result();
		result.setItemList(itemList);
		result.setSuccess(true);
	    return result; 
	}
	
	private void findApplet(String applet)
	{
		if (RootTools.findBinary(applet))
		{
			
			item.setFound(true);
			
			for (String paths : RootTools.lastFoundBinaryPaths)
			{
				if (paths.contains("/system/bin"))
				{
					item.setAppletPath(paths);
				}
			}
			
			if (item.getAppletPath().equals(""))
			{
				item.setAppletPath(RootTools.lastFoundBinaryPaths.get(0));
			}
						
			String symlink = RootTools.getSymlink(new File(item.getAppletPath() + "/" + applet));
			
			item.setSymlinkedTo(symlink);
			
			if ((!item.getSymlinkedTo().equals("")))
			{
				if (item.getSymlinkedTo().trim().toLowerCase().endsWith("busybox"))						
					item.setRecommend(true);
				else
				{
					item.setRecommend(false);
				}		
			}
			else if (item.getSymlinkedTo().equals(""))
			{
				item.setBackupFilePath(item.getAppletPath());
				item.setRecommend(false);
				
				File file = new File(storagePath + "/" + applet);
				if (!file.exists())
				{
					makeBackup(item.getApplet(), item.getAppletPath());
				}
			}
			else
				item.setRecommend(true);
			
			item.setOverwrite(item.getRecommend());
			
			if (item.getDescription().equals(""))
			{
				try
				{
					List<String> result = RootTools.sendShell("busybox " + applet + " --help", -1);
					String appletInfo = "";
					
					for (String info : result)
					{
						if (!info.contains("not found") && !info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals(""))
						{
							appletInfo += info + "\n";
						}
					}
					
					item.setDescription(appletInfo);
				}
				catch (Exception e){}
			}
		}
		else
		{
			item.setFound(false);
			item.setSymlinkedTo("");
			item.setRecommend(true);
		}
					
		dbService.insertOrUpdateRow(item);
	}
	
	
	
	public void makeBackup(String applet, String path)
	{
		String mountType = "rw";
		try
		{
			mountType = RootTools.getMountedAs(path);

			RootTools.remount(path, "rw");
			String[] commands = new String[]
			        {
						toolbox + " rm " + storagePath + "/" + applet,
						"/system/bin/toolbox rm " + storagePath + "/" + applet,
						"rm " + storagePath + "/" + applet,
						"dd if=" + path + "/" + applet + " of=" + storagePath + "/" + applet,
						toolbox + " dd if=" + path + "/" + applet + " of=" + storagePath + "/" + applet,
						"/system/bin/toolbox dd if=" + path + "/" + applet + " of=" + storagePath + "/" + applet
					};
			
			RootTools.sendShell(commands, 0, -1);
			
			if (!new File(storagePath + "/" + applet).exists())
			{
				commands = new String[] {
						"cat " + path + "/" + applet + " > " + storagePath + "/" + applet,
						"cp " + path + "/" + applet + " " + storagePath + "/" + applet,
						toolbox + " cat " + path + "/" + applet + " > " + storagePath + "/" + applet,
						toolbox + " cp " + path + "/" + applet + " " + storagePath + "/" + applet,
						"/system/bin/toolbox cat " + path + "/" + applet + " > " + storagePath + "/" + applet,
						"/system/bin/toolbox cp " + path + "/" + applet + " " + storagePath + "/" + applet};
				
				RootTools.sendShell(commands, 0, -1);
			}
		}
		catch (Exception e) {}
		finally
		{
			RootTools.remount(path, mountType);
		}
		
		if (new File(storagePath + "/" + applet).exists())
		{
			RootTools.log("Backup Created!");
		}
		else
		{
			RootTools.log("Backup Creation Failed!");
		}
	}
	
	public void closeDb()
	{
		if (dbService != null)
		{
			try
			{
				dbService.close();
			}
			catch (Exception e) {}
		}
	}
}
