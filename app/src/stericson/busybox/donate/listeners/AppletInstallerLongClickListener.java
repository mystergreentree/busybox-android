package stericson.busybox.donate.listeners;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.jobs.InstallAppletJob;
import stericson.busybox.donate.jobs.UnInstallAppletJob;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class AppletInstallerLongClickListener implements OnItemLongClickListener {

	private MainActivity activity;
	
	public AppletInstallerLongClickListener(MainActivity activity)
	{
		this.activity = activity;
	}

	public boolean onItemLongClick(final AdapterView<?> adapter, View v,final int position,
			long arg3) {

		final Item item = App.getInstance().getItemList().get(position);
		
		if (item.getFound())
		{
			new AlertDialog.Builder(activity)
		    .setTitle("Reinstall/Uninstall " + item.getApplet() + "?")
		    .setMessage(activity.getString(R.string.installapplet))
		    .setPositiveButton("Reinstall", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	
		        	activity.stopGatherInformation();
		        	
		    		new InstallAppletJob(activity, item.getApplet(), position, adapter).execute();		    		
		        }
		    }).setNegativeButton("Uninstall", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		    		new UnInstallAppletJob(activity, item.getApplet(), item.getAppletPath(), position, adapter).execute();
		        }}).show();
		}
		else
		{
			new AlertDialog.Builder(activity)
		    .setTitle("Install " + item.getApplet() + "?")
		    .setMessage(activity.getString(R.string.installapplet))
		    .setPositiveButton("Install", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	
		        	activity.stopGatherInformation();

		    		new InstallAppletJob(activity, item.getApplet(), position, adapter).execute();
		        }
		    }).show();
		}
		return false;
	}
}
