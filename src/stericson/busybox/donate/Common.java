package stericson.busybox.donate;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.stericson.RootTools.Permissions;
import com.stericson.RootTools.RootTools;

public class Common
{

	public static String getSingleBusyBoxPath()
	{
		try
		{
			for (String path : RootTools.sendShell("busybox which busybox", -1))
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
				File file = new File(paths + "/busybox");
				if (file.exists()) {
					Permissions perms = RootTools.getFilePermissionsSymlinks(paths + "/busybox");
					
					if (includeSymlinks || !perms.getType().equals("l"))
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
