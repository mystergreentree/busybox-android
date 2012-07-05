package stericson.busybox.donate.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.Common;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.services.DBService;
import android.content.Context;
import android.os.Environment;

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

		RootTools.debugMode = true;
				
		this.extractResources(context, Environment.getExternalStorageDirectory() + "/stericson-ls");
		try
		{
			RootTools.useRoot = true;

			RootTools.fixUtils(new String[] {"dd", "chmod"});
			RootTools.sendShell(new String[] {"dd if=" + Environment.getExternalStorageDirectory() + "/stericson-ls of=/data/local/ls", "chmod 0777 /data/local", "chmod 0755 /data/local/ls" }, 0, -1);
		}
		catch (Exception ignore) {}
		
		RootTools.useRoot = false;
					
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
					File file = new File(item.getAppletPath() + "/" + applet);
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
				{
					item.setInode(getInode(symlink));

					if (!new File(storagePath + "/" + item.getInode()).exists())
					{
						makeBackup("busybox", symlink.replace("/busybox", ""), item.getInode());
					}
					
					item.setRecommend(true);
				}
				else
				{
					item.setRecommend(false);
				}		
			}
			else if (item.getSymlinkedTo().equals(""))
			{
				String inode = getInode(item.getAppletPath() + "/" + applet);
				for (String path : Common.findBusyBoxLocations(false, false))
				{
					if (inode.equals(getInode(path + "busybox")))
					{
						item.setIshardlink(true);
						item.setBackupHardlink(path + "busybox");
						item.setInode(inode);

						if (!new File(storagePath + "/" + inode).exists())
						{
							makeBackup("busybox", path, inode);
						}
					}
				}
				
				item.setBackupFilePath(item.getAppletPath());
				item.setRecommend(false);

				if (!item.isIshardlink())
				{
					File file = new File(storagePath + "/" + applet);
					if (!file.exists())
					{
						makeBackup(item.getApplet(), item.getAppletPath(), item.getApplet());
					}
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
	
	
	
	public void makeBackup(String applet, String path, String name)
	{
		InputStream is = null;
		OutputStream os = null;
		byte[] buffer = new byte[2048];
		int bytes_read = 0;
		
		try
		{
			is = new FileInputStream(new File(path + "/" + applet));
			os = new FileOutputStream(new File(storagePath + "/" + name));
			
			while ((bytes_read = is.read(buffer)) != -1)
			{
				os.write(buffer, 0, bytes_read);
			}
			
		}
		catch (Exception ignore) {}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception ignore) {}
			
			try
			{
				os.close();
			}
			catch (Exception ignore) {}
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
	
    public String getInode(String file)
    {
    	try
    	{
	    	for (String line : RootTools.sendShell("/data/local/ls -i " + file, -1))
	    	{
	    		if (Character.isDigit((char) line.trim().substring(0, 1).toCharArray()[0]))
	    		{
	    			return line.trim().split(" ")[0].toString();
	    		}
	    	}
	    	return "";
    	}
    	catch (Exception ignore)
    	{
    		return "";
    	}
    }
    
	/**
	 * Used to extract certain assets we may need. Can be used by any class to
	 * extract something. Right now this is tailored only for the initial check
	 * sequence but can easily be edited to allow for more customization
	 */
	public void extractResources(Context activity, String outputPath) {
		String realFile = "ls.png";

		try {
			InputStream in = activity.getResources().getAssets().open(realFile);
			OutputStream out = new FileOutputStream(
					outputPath);
			byte[] buf = new byte[2048];
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
