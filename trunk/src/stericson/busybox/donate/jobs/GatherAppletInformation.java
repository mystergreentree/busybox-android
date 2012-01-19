package stericson.busybox.donate.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.TuneActivity;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import android.widget.TextView;

import com.stericson.RootTools.Permissions;
import com.stericson.RootTools.RootTools;

public class GatherAppletInformation extends AsyncJob<Result>
{
	private TuneActivity activity;
	
	public GatherAppletInformation(TuneActivity activity)
	{
		super(activity, R.string.gathering, true, false);
		this.activity = activity;
	}

	@Override
    Result handle()
    {
		
		List<Item> itemList = new ArrayList<Item>();
		
		for (String applet : Constants.appletsString)
		{
			this.publishProgress(applet);
			Item item = new Item();
			
			item.setApplet(applet);
			
			if (RootTools.findBinary(applet))
			{
				item.setFound(true);
				item.setAppletPath(RootTools.lastFoundBinaryPaths.get(0));
				
				item.setSymlinkedTo(RootTools.getSymlink(new File(item.getAppletPath() + "/" + applet)));
				Permissions permissions = RootTools.getFilePermissions(item.getAppletPath() + "/" + applet);
				
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
			}
			else
			{
				item.setSymlinkedTo("");
				item.setRecommend(true);
			}

			itemList.add(item);
			
		}
		
		
		Result result = new Result();
		result.setItemList(itemList);
		result.setSuccess(true);
	    return result; 
    }

	@Override
    protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		TextView header = (TextView) App.getInstance().getPopupView().findViewById(R.id.header);
		header.setText(this.activity.getString(R.string.gatheringAbout) + " " + values[0]);
    }
    
	@Override
    void callback(Result result)
    {
	    activity.jobCallBack(result);
    }
}
