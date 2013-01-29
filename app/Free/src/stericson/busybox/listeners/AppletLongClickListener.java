package stericson.busybox.listeners;

import java.util.ArrayList;
import java.util.List;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;
import stericson.busybox.R;
import stericson.busybox.Activity.BaseActivity;
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
				final List<String> result = new ArrayList<String>();
				
				Command command = new Command(0, "busybox " + applet.getText().toString() + " --help")
				{
					@Override
					public void commandFinished(int arg0) {}

					@Override
					public void output(int arg0, String arg1)
					{
						result.add(arg1);
					}
				};
				Shell.startRootShell().add(command).waitForFinish();
				
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
