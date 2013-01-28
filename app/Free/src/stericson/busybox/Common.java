package stericson.busybox;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;

public class Common
{

	/**
	 * Used to extract certain assets we may need. Can be used by any class to
	 * extract something. Right now this is tailored only for the initial check
	 * sequence but can easily be edited to allow for more customization
	 */
	public static void extractResources(Context activity, String file, String outputPath) {
		String realFile = "";
		if (file.contains("toolbox"))
		{
			realFile = "toolbox.png";
		}
		else if (file.contains("reboot"))
		{
			realFile = "reboot.png";
		}
		else if (file.contains("1.20.2")) {
			realFile = "busybox1.20.2.png";
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
	
	public static String getSingleBusyBoxPath()
	{
		final List<String> paths = new ArrayList<String>();

		try
		{
			Command command = new Command(0, "busybox which busybox")
			{
	
				@Override
				public void commandFinished(int arg0) {}
	
				@Override
				public void output(int arg0, String arg1)
				{
					paths.add(arg1);
				}
				
			};
			Shell.startRootShell().add(command).waitForFinish();
		
			for (String path : paths)
			{
				if (path.startsWith("/"))
				{
					return path.replace("busybox", "");
				}
				else
				{
					break;
				}
			}
			
			return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static String[] findBusyBoxLocations(boolean includeSymlinks, boolean single) {

		if (single)
		{
			String single_location = getSingleBusyBoxPath();
			
			if (single_location != null)
			{
				if (single_location.contains("system/bin"))
				{
					App.getInstance().setPathPosition(0);
				}
				else if (single_location.contains("system/xbin"))
				{
					App.getInstance().setPathPosition(1);
				}

				return new String[] { single_location };	
			}
		}
		
		Set<String> tmpSet = new HashSet<String>();

		try {
			for (String paths : RootTools.getPath()) {
				if (RootTools.exists(paths + "/busybox")) {
					String symlink = RootTools.getSymlink(paths + "/busybox");
					
					if (includeSymlinks || symlink.equals(""))
					{
						tmpSet.add(paths);						
					}
				}
			}
		} catch (Exception ignore) {
			// nothing found.
		}

		String locations[] = new String[tmpSet.size()];

		int i = 0;
		for (String paths : tmpSet) {
			locations[i] = paths + "/";
			i++;
		}

		if (locations.length > 0)
		{
			if (locations[0].contains("system/bin"))
			{
				App.getInstance().setPathPosition(0);
			}
			else if (locations[0].contains("system/xbin"))
			{
				App.getInstance().setPathPosition(1);
			}
		}

		return locations;
	}
}
