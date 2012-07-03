package stericson.busybox.donate.receivers;

import com.stericson.RootTools.RootTools;

import stericson.busybox.donate.Common;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;
import stericson.busybox.donate.jobs.Install;
import stericson.busybox.donate.services.PreferenceService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class OnUpgradeReceiver extends BroadcastReceiver implements CallBack
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		boolean blowout = false;
		if (blowout)
		{
			new PreferenceService(context).setDeleteDatabase(true);
			
			try
			{
				RootTools.sendShell("rm " + context.getFilesDir().toString() + "/*", -1);
			}
			catch (Exception ignore) {}
		}
		
		SharedPreferences sp = context.getSharedPreferences("BusyBox", 0);
		
		if (Constants.updateType != 0 && intent.getDataString().contains("stericson.busybox.donate"))
		{
			
			String ticker = "";
			String title = "Update!";

			if (sp.getBoolean("auto-update", false))
			{
				String location = Common.findBusyBoxLocations(false, true)[0];
				
				if (location == null || location.equals(""))
					location = "/system/bin";
				
				Result result = new Install().install(context, null, Constants.newest, location == null ? "/system/bin" : location , true, false, true);
				
				if (result.isSuccess())
				{
					title = "Success!";
					ticker = "Updated/Installed " + Constants.newest;					
				}
				else
				{
					title = "Failed";
					ticker = "Update/Install of " + Constants.newest;					
				}
			}
			else
			{			
				switch (Constants.updateType)
				{
					case 1:
						ticker = "Update available for BusyBox binary!";
						break;
					case 2:
						ticker = "New BusyBox binary available!";
						break;
					case 3:
						ticker = "New binary and updates available!";
						break;
				}
			}
			
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
	
			Notification notification = new Notification();
			notification.icon = R.drawable.icon;
			notification.when = System.currentTimeMillis();
			
			Intent notificationIntent = new Intent(context, MainActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	
			notification.contentIntent = contentIntent;
			
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
	
			notification.setLatestEventInfo(context, title,
					ticker, contentIntent);
			
			mNotificationManager.notify(1, notification);
		}			
	}

	public void jobCallBack(Result result, int id)
	{
		
	}

}
