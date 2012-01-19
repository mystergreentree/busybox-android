package stericson.busybox.donate.Activity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import stericson.busybox.donate.R;
import stericson.busybox.donate.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.Mount;
import com.stericson.RootTools.Permissions;
import com.stericson.RootTools.RootTools;

public class BaseActivity extends Activity {

	public Utils util = new Utils();
	public PopupWindow pw;
	public boolean endApplication;
	private String utilPath;

	public Typeface tf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RootTools.debugMode = true;
		
		tf = Typeface.createFromAsset(getAssets(), "fonts/DJGROSS.ttf");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "About");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			initiatePopupWindow(this.getString(R.string.about), false,
					BaseActivity.this);
			break;
		}
		return false;
	}

	public void initiatePopupWindow(String text, boolean endApplication,
			Activity context) {
		this.endApplication = endApplication;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Inflate the view from a predefined XML layout
		View layout = inflater.inflate(R.layout.popupwindow, null);
		pw = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT);

		context.findViewById(R.id.pop).post(new Runnable() {
			public void run() {
				pw.showAtLocation(findViewById(R.id.pop), Gravity.CENTER, 0, 0);
			}
		});

		TextView header = (TextView) layout.findViewById(R.id.header_main);
		header.setTypeface(tf);

		TextView textView = (TextView) layout.findViewById(R.id.content);
		textView.setText(text);
	}

	public String[] findBusyBoxLocations() {
		util.log("Finding BusyBox");

		Set<String> tmpSet = new HashSet<String>();

		try {
			for (String paths : RootTools.getPath()) {
				File file = new File(paths + "/busybox");
				if (file.exists()) {
					tmpSet.add(paths);
				}
			}
		} catch (Exception e) {
			// nothing found.
		}

		String locations[] = new String[tmpSet.size()];

		int i = 0;
		for (String paths : tmpSet) {
			locations[i] = paths + "/";
			i++;
		}

		return locations;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (pw != null && pw.isShowing()) {
				pw.dismiss();
				if (endApplication) {
					finish();
					randomAnimation();
				}
			} else {
				finish();
				randomAnimation();
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void randomAnimation() {
		Random random = new Random();
		switch (random.nextInt(3)) {
		case 0:
			overridePendingTransition(this, R.anim.enter_scalein,
					R.anim.exit_slideout);
			break;
		case 1:
			overridePendingTransition(this, R.anim.enter_dropin,
					R.anim.exit_dropout);
			break;
		case 2:
			overridePendingTransition(this, R.anim.enter_slidein,
					R.anim.exit_slideout);
			break;
		}
	}

	public void close(View v) {
		pw.dismiss();
		if (endApplication) {
			finish();
			randomAnimation();
		}
	}

	private static Method overridePendingTransition;

	static {
		try {
			overridePendingTransition = Activity.class
					.getMethod(
							"overridePendingTransition", new Class[] { Integer.TYPE, Integer.TYPE }); //$NON-NLS-1$
		} catch (NoSuchMethodException e) {
			overridePendingTransition = null;
		}
	}

	/**
	 * Calls Activity.overridePendingTransition if the method is available
	 * (>=Android 2.0)
	 * 
	 * @param activity
	 *            the activity that launches another activity
	 * @param animEnter
	 *            the entering animation
	 * @param animExit
	 *            the exiting animation
	 */
	public static void overridePendingTransition(Activity activity,
			int animEnter, int animExit) {
		if (overridePendingTransition != null) {
			try {
				overridePendingTransition.invoke(activity, animEnter, animExit);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	//My handy dandy utility fixer!
	public boolean checkCoreUtils() throws Exception {
		String[] utils = new String[] {"ls", "rm", "ln", "dd", "chmod", "mount"};
		
		for (String util : utils)
		{
			if (!checkUtil(util))
			{
				if (checkUtil("busybox"))
				{
					fixUtil(util);
					if (!checkUtil(util))
					{
						return false;
					}
				}
				else if (checkUtil("toolbox"))
				{
					fixUtil(util);
					if (!checkUtil(util))
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean checkUtil(String util)
	{
		if (RootTools.findBinary(util))
		{
			for (String path : RootTools.lastFoundBinaryPaths)
			{
				Permissions permission = RootTools.getFilePermissions(path + "/" + util);
				
				int permissions = permission.getPermissions();
				
				if (permissions == 755 || permissions == 777 || permissions == 775)
				{
					utilPath = path + "/" + util;
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void fixUtil(String util)
	{
		try
		{
			RootTools.remount("/system", "rw");
			
			if (RootTools.findBinary(util))
			{
				for (String path : RootTools.lastFoundBinaryPaths)
					RootTools.sendShell(utilPath + " rm " + path + "/" + util, -1);
				
				RootTools.sendShell(new String[] {	utilPath + " ln -s " + utilPath + " /system/bin/" + util,
													utilPath + " chmod 0755 /system/bin/" + util}, 10, -1);
			}
			
			RootTools.remount("/system", "ro");
		}
		catch (Exception e) {}
	}
}
