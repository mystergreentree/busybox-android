package stericson.busybox.donate.listeners;

import java.util.List;

import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.BaseActivity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

public class AppletLongClickListener implements OnLongClickListener {

	private BaseActivity activity;
	
	public AppletLongClickListener(BaseActivity activity)
	{
		this.activity = activity;
	}

	public boolean onLongClick(View v) {
		CheckBox applet = (CheckBox) v.findViewById(R.id.appletCheck);
		
		if (applet != null && RootTools.isAppletAvailable(applet.getText().toString()))
		{
			try
			{
				List<String> result = RootTools.sendShell("busybox " + applet.getText().toString() + " --help", -1);
				String appletInfo = "";
				
				for (String info : result)
				{
					if (!info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals(""))
					{
						appletInfo += info + "\n";
					}
				}
				
				activity.initiatePopupWindow(appletInfo, false, activity);
			}
			catch (Exception e)
			{
				Toast.makeText(activity, R.string.noInfo, Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Toast.makeText(activity, R.string.noInfo, Toast.LENGTH_LONG).show();			
		}
		return false;
	}

}
