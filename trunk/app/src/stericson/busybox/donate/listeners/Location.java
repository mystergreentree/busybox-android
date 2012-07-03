package stericson.busybox.donate.listeners;

import java.io.File;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;
import stericson.busybox.donate.jobs.GetSpace;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Toast;

public class Location implements OnItemSelectedListener, CallBack {

	private MainActivity activity;
	
	public Location(MainActivity activity)
	{
		this.activity = activity;
	}

	public void onItemSelected(final AdapterView<?> arg0, View arg1,
			int arg2, long arg3) {
		if (arg2 == 2) 
		{
			final EditText input = new EditText(activity);
			new AlertDialog.Builder(activity)
		    .setTitle("Custom Path")
		    .setMessage("Please enter the Path you want to install Busybox, Success is not gauranteed. \n\n This is an advanced option")
		    .setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	File file = new File(input.getText().toString());
		        	if (file.exists())
		        	{
		   			 activity.setCustomPath(input.getText().toString());
		        		Toast.makeText(activity, "Custom install path set to " + activity.getCustomPath(), Toast.LENGTH_LONG).show();
		        	}
		        	else
		        	{
		        		Toast.makeText(activity, "That path does not exist or is not valid!", Toast.LENGTH_LONG).show();
		        		arg0.setSelection(0);
		        		App.getInstance().setPath(arg0.getSelectedItem().toString());
		        		App.getInstance().setPathPosition(0);
		        		activity.setCustomPath("");
		        	}
		        	
				    new GetSpace(activity, activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath(), Location.this).execute();

		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	arg0.setSelection(0);
	        		App.getInstance().setPath(arg0.getSelectedItem().toString());
	        		App.getInstance().setPathPosition(0);
					activity.setCustomPath("");
					
				    new GetSpace(activity, activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath(), Location.this).execute();

		        }
		    }).show();
		}
		else
		{
			activity.setCustomPath("");
			App.getInstance().setPathPosition(0);
			App.getInstance().setPath(arg0.getSelectedItem().toString());
		    new GetSpace(activity, activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath(), Location.this).execute();
		}		
	}

	public void onNothingSelected(AdapterView<?> arg0) {}

	public void jobCallBack(Result result, int id) {
	    activity.getFreeSpace().setText(result.getSpace() != -1 ? activity.getString(R.string.amount) + " " + (activity.getCustomPath().equals("") ? "/system" : activity.getCustomPath()) + " " + result.getSpace() + "mb" : "");		
	}

}
