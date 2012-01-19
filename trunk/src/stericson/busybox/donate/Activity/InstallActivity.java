package stericson.busybox.donate.Activity;

import java.io.File;

import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.jobs.InstallJob;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stericson.RootTools.Mount;
import com.stericson.RootTools.RootTools;

public class InstallActivity extends BaseActivity
{
	RelativeLayout container;

	private Button install;

	private TextView	informationView,
						header,
						versionNumber;
	
	private static String	version = "",
							currentVersion = "",
							path;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.install);
		
		container = (RelativeLayout) this.findViewById(R.id.container);
		
		header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);

		versionNumber = (TextView) findViewById(R.id.versionInformation);
		versionNumber.setTypeface(tf);

		informationView = (TextView) findViewById(R.id.information);
		
		version = this.getIntent().getExtras().getString(Constants.EXTRA_BUSYBOX_VERSION);
		currentVersion = RootTools.getBusyBoxVersion();
	
		path = this.getIntent().getExtras().getString(Constants.EXTRA_INSTALL_PATH);
		
		install = (Button) findViewById(R.id.install);
	
		buildInformation();

		initiatePopupWindow(this.getString(R.string.beforeInstall), false, this);		
	}
	
	public void buildInformation()
	{		
		String information = "";
		
		String pathtmp = "";
		String pathtmp2;
		if (path.endsWith("/"))
		{
			pathtmp2 = path.substring(0, (path.length() - 1));
		}
		else
		{
			pathtmp2 = path;
		}

		try {
			while (pathtmp2 != null)
			{
				for (Mount mounts : RootTools.getMounts())
				{
					if (pathtmp2.equals(mounts.getMountPoint().toString()))
					{
						pathtmp = mounts.getMountPoint().getPath();
						break;
					}
				}
				if (pathtmp2.equals(pathtmp))
				{
					break;
				}
				pathtmp2 = new File(pathtmp2).getParent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (RootTools.getSpace(pathtmp) != -1)
		{
			information += this.getString(R.string.amount) + " " + pathtmp + ": " + RootTools.getSpace(pathtmp) + " kb \n\n";
		}
		
		if (RootTools.remount(path, "rw"))
		{
			information += path + " " + this.getString(R.string.mounted);
		}
		else
		{
			information += path + " " + this.getString(R.string.failedmount);
		}
		
		informationView.setText(information);
	}
	
	public void install(View v)
	{
		install.setEnabled(false);

		if (RootTools.hasEnoughSpaceOnSdCard(1600)) 
		{
			container.setVisibility(View.GONE);
			
			new InstallJob(this, version, path).execute();
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
		
		boolean result = RootTools.checkUtil("busybox");
		
		if (result)
		{
			String thisVersion = RootTools.getBusyBoxVersion();
			
			if (thisVersion == null)
			{
				thisVersion = "";
			}

			if (currentVersion == null)
			{
				InstallActivity.currentVersion = "-1";
			}
			
			if (thisVersion.contains(version.toLowerCase().replace("busybox", "").trim()))
			{
				if (RootTools.lastFoundBinaryPaths.size() == 1)
				{
					if (InstallActivity.currentVersion.equals(thisVersion))
					{
						initiatePopupWindow(this.getString(R.string.installedsame), true, this);
					}
					else
					{
						initiatePopupWindow(this.getString(R.string.installedunique), true, this);
					}
				} 
				else
				{
					initiatePopupWindow(this.getString(R.string.installedmultiple), true, this);
				}
			} 
			else
			{
				if (RootTools.lastFoundBinaryPaths.size() == 1)
				{
					if (InstallActivity.currentVersion.equals(thisVersion))
					{
						initiatePopupWindow(this.getString(R.string.installedsame), true, this);
					}
					else
					{
						initiatePopupWindow(this.getString(R.string.installedsomethingelse), true, this);
					}
				} 
				else
				{
					initiatePopupWindow(this.getString(R.string.installedmultiple), true, this);
				}
			}
		} 
		else
		{
			initiatePopupWindow(this.getString(R.string.failed), true, this);
		}
	};

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
				Intent i = new Intent(this, PathActivity.class);
				i.putExtra(Constants.EXTRA_BUSYBOX_VERSION, version);
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
