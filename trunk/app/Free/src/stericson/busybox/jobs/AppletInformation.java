package stericson.busybox.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.R;
import stericson.busybox.domain.Item;
import stericson.busybox.domain.Result;
import android.content.Context;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;

public class AppletInformation
{

	private List<Item> itemList;
	private Item item;
	private String storagePath = "";

	
	public Result getAppletInformation(Context context, boolean updating, GatherAppletInformation gai, String [] applets)
	{	
		Result result = new Result();
		result.setSuccess(true);

		try
		{
			RootTools.getShell(true);
		}
		catch (Exception e)
		{
			result.setSuccess(false);
			result.setError(context.getString(R.string.shell_error));
		    return result; 
		}
		
		//storagePath = context.getFilesDir().toString();
		itemList = new ArrayList<Item>();
								
		for (String applet : applets)
		{
			if (gai != null && gai.isCancelled())
			{
				return null;
			}
			
			if (!updating && gai != null)
				gai.publishCurrentProgress(applet);

			if (true)
			{				
				try {
					
					item = new Item();
					
					item.setApplet(applet);
					
					findApplet(applet);
				}
				catch (Exception e) {}
			}
		}
		
		result.setItemList(itemList);
	    return result; 
	}
	
	private void findApplet(String applet)
	{
		if (RootTools.findBinary(applet))
		{
			
			item.setFound(true);
			
			List<String> paths = new ArrayList<String>();
			paths.addAll(RootTools.lastFoundBinaryPaths);
			
			for (String path : RootTools.lastFoundBinaryPaths)
			{
				if (path.contains("/system/bin"))
				{
					item.setAppletPath(path);
					break;
				}
			}
			
			if (item.getAppletPath().equals(""))
			{
				item.setAppletPath(paths.get(0));
			}
						
			String symlink = RootTools.getSymlink(item.getAppletPath() + "/" + applet);
			
			item.setSymlinkedTo(symlink);
			
			if ((!item.getSymlinkedTo().equals("")))
			{
				if (item.getSymlinkedTo().trim().toLowerCase().endsWith("busybox"))	
				{					
					item.setRecommend(true);
				}
				else
				{
					item.setRecommend(false);
				}		
			}
			else if (item.getSymlinkedTo().equals(""))
			{
				item.setRecommend(false);
			}
			else
				item.setRecommend(true);
			
			item.setOverwrite(item.getRecommend());
			
			if (item.getDescription().equals(""))
			{
				try
				{
					final List<String> result = new ArrayList<String>();
					Command command = new Command(0, "busybox " + applet + " --help")
					{

						@Override
						public void commandFinished(int arg0) {}

						@Override
						public void output(int arg0, String arg1)
						{
							result.add(arg1);
						}
						
					};
					RootTools.getShell(true).add(command).waitForFinish();
					
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
					
		itemList.add(item);
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
