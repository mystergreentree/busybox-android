package stericson.busybox.donate.receivers;

import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import stericson.busybox.donate.Common;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.jobs.Install;
import stericson.busybox.donate.services.PreferenceService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class OnUpgradeReceiver extends BroadcastReceiver
{

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		if (intent.getDataString().contains("stericson.busybox.donate"))
		{
			boolean blowout = false;
			if (blowout)
			{
				new PreferenceService(context).setDeleteDatabase(true).commit();
				
				Thread t = new Thread()
				{
					public void run()
					{
						try
						{
							CommandCapture command = new CommandCapture(0, "rm " + context.getFilesDir().toString() + "/*");
							Shell.startRootShell().add(command).waitForFinish();
						}
						catch (Exception ignore) {}
					}
				};
				
				t.start();
			}
			
			SharedPreferences sp = context.getSharedPreferences("BusyBox", 0);
			
			if (Constants.updateType != 0 && intent.getDataString().contains("stericson.busybox.donate"))
			{
				
				String ticker = "";
				String title = "Update!";
	
				if (sp.getBoolean("auto-update", false))
				{
					String[] locations = Common.findBusyBoxLocations(false, true);
					String location = locations.length > 0 ? Common.findBusyBoxLocations(false, true)[0] : "";
					
					if (location == null || location.equals(""))
						location = "/system/bin";
					
					if (Common.setupBusybox(context, Constants.newest, false)) {
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
					else {
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
	}
}
