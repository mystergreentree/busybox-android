package stericson.busybox.jobs;

import java.util.ArrayList;
import java.util.List;

import stericson.busybox.Constants;
import stericson.busybox.R;
import stericson.busybox.Activity.MainActivity;
import stericson.busybox.domain.Item;
import stericson.busybox.domain.Result;
import android.widget.TextView;

import com.stericson.RootTools.Permissions;
import com.stericson.RootTools.RootTools;

public class GatherAppletInformation extends AsyncJob<Result>
{
	private MainActivity activity;
	protected TextView view;
	
	public GatherAppletInformation(MainActivity activity)
	{
		super(activity, R.string.gathering, false, false);
		this.activity = activity;
	}

	@Override
    Result handle()
    {
		List<Item> itemList = new ArrayList<Item>();
		
		for (String applet : Constants.appletsString)
		{
			try {
				this.publishProgress(applet);
				Item item = new Item();
				
				item.setApplet(applet);
				
				if (RootTools.findBinary(applet))
				{
					item.setFound(true);
					item.setAppletPath(RootTools.lastFoundBinaryPaths.get(0));
					
					Permissions permissions = RootTools.getFilePermissionsSymlinks(item.getAppletPath() + "/" + applet);
					item.setSymlinkedTo(permissions.getSymlink());
					
					if ((!item.getSymlinkedTo().equals("") && RootTools.findBinary(item.getSymlinkedTo().substring(item.getSymlinkedTo().lastIndexOf("/") + 1, item.getSymlinkedTo().length()))))
					{
						if (item.getSymlinkedTo().trim().toLowerCase().endsWith("busybox"))						
							item.setRecommend(true);
						else
						{
							item.setRecommend(false);
						}		
					}
					else if (permissions != null && !permissions.getType().equals("l"))
						item.setRecommend(false);
					else
						item.setRecommend(true);
					
					item.setOverwrite(item.getRecommend());
					
					try
					{
						List<String> result = RootTools.sendShell("busybox " + applet + " --help", -1);
						String appletInfo = "";
						
						for (String info : result)
						{
							if (!info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals(""))
							{
								appletInfo += info + "\n";
							}
						}
						
						item.setDescription(appletInfo);
					}
					catch (Exception e)
					{
						item.setDescription(context.getString(R.string.noInfo));
					}
				}
				else
				{
					item.setSymlinkedTo("No Symlink");
					item.setRecommend(true);
					item.setDescription(context.getString(R.string.noInfo));
				}
	
				itemList.add(item);
			}
			catch (Exception e) {}
		}
		
		
		Result result = new Result();
		result.setItemList(itemList);
		result.setSuccess(true);
	    return result; 
    }

	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		try
		{
			//TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
			activity.view1.setText(this.activity.getString(R.string.gatheringAbout) + " " + values[0]);
		} catch (Exception e) {}
		
		try
		{
			//TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
			activity.view2.setText(this.activity.getString(R.string.gatheringAbout) + " " + values[0]);
		} catch (Exception e) {}
    }
    
	@Override
    void callback(Result result)
    {
		activity.jobCallBack(result, 0);
    }
}
