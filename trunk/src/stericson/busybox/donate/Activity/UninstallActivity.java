package stericson.busybox.donate.Activity;

import stericson.busybox.donate.R;
import stericson.busybox.donate.StaticThings;
import stericson.busybox.donate.jobs.UninstallJob;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class UninstallActivity extends BaseActivity
{

	private Button uninstall;

	RelativeLayout container;

	private TextView	informationView,
						header,
						versionNumber;
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uninstall);
		
		container = (RelativeLayout) this.findViewById(R.id.container);
		
		header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);

		versionNumber = (TextView) findViewById(R.id.versionInformation);
		versionNumber.setTypeface(tf);

		informationView = (TextView) findViewById(R.id.information);
		informationView.setText("Cross your fingers, hit the magic button, and hold on for the ride!");
			
		uninstall = (Button) findViewById(R.id.uninstall);
	
		initiatePopupWindow(this.getString(R.string.beforeUninstall), false, this);		
	}
		
	public void uninstall(View v)
	{
		uninstall.setEnabled(false);
		
		if (RootTools.hasEnoughSpaceOnSdCard(1600)) 
		{
			container.setVisibility(View.GONE);

			new UninstallJob(this).execute();

		}
		else 
		{
			initiatePopupWindow(this.getString(R.string.sdcard), true, this);
		}
	}
	
	//Called after we have performed the install, this is responsible
	//for making sure everything was done right.
	public void done()
	{		
		RootTools.remount("/system", "ro");
		
		if (!RootTools.isBusyboxAvailable())
		{
			initiatePopupWindow(this.getString(R.string.uninstallsuccess), true, this);			
		}
		else
		{
			initiatePopupWindow(this.getString(R.string.uninstallfailed), true, this);
		}		

	};

	
	// how many did we find?
	//checks to see how many BusyBoxes we found, if any.
	private boolean check(String[] locations) {
		if (locations != null) 
		{ 
			return true;
		}
		return false;
	}
	
	//Handler for telling us when we are done with our threads.
	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				done();
				return;
			}
		}
	};
		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (pw != null && pw.isShowing()) {
			}
			else
			{
				Intent i = new Intent(this, BusyBoxActivity.class);
				this.startActivity(i);
				randomAnimation();
			}
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void close(View v)
	{
		pw.dismiss();
		if (endApplication)
		{
			finish();
			randomAnimation();
		}
	}
}
