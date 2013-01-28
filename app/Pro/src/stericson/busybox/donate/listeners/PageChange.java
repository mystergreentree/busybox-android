package stericson.busybox.donate.listeners;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;
import stericson.busybox.donate.jobs.GetLocations;
import stericson.busybox.donate.jobs.GetVersion;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class PageChange implements OnPageChangeListener, CallBack {

	private MainActivity context;
	
	public PageChange(MainActivity context, ViewPager view)
	{
		this.context = context;		
	}

	public void onPageScrollStateChanged(int arg0) {}

	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

	public void onPageSelected(int position) {
		context.position = position;
		if (position == 0)
		{
			context.initiatePopupWindow("This feature allows you to install, uninstall, or reinstall the applets listed below on an individual basis. \n\n This feature will also tell you a little information about the applet and whether or not it is currently installed or symlinked. \n\n This feature is useful if you are having a problem with a specific applet not functioning correctly. \n\n To access this feature, please long press on an applet.", false, context);
		}
		else if (position == 1)
		{
			new GetVersion(context, this).execute();
		}
	}

	public void jobCallBack(Result result, int id) {
		if (id == 1)
		{
			if (result.getLocations().length > 0)
			{
				App.getInstance().setFound(result.getLocations().length > 1 ? context.getString(R.string.morethanone) : context.getString(R.string.busybox) + " " + App.getInstance().getCurrentVersion() + " " + context.getString(R.string.foundit) + "\n\n" + context.getString(R.string.installedto) + " " + result.getLocations()[0]);
			}
			else
			{
				App.getInstance().setFound("Location of Busybox could not be determined.");
			}
			
			context.updateList();
			
			try
			{
				App.getInstance().updateVersion(0);
				
				context.updateList();
			} catch (Exception ignore) {}
		}
		else if (id == 2)
		{
	    	new GetLocations(context, this, true).execute();
		}
		else if (id == 3)
		{
			context.updateList();
		}
	}
}
