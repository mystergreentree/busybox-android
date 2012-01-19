package stericson.busybox.donate.utils;

import stericson.busybox.donate.R;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Utils {

	private static String TAG = "MeatMorph Utility";
	protected Activity ourActivity;

	// =========================================
	// Custom messages
	// =========================================

	// This is Our Custom Toast for error notifications & message notifications.
	public void ourMessages(Activity activity, String errormessage,
			boolean recoverable, boolean longtoast) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
				(ViewGroup) activity.findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(errormessage);

		Toast toast = new Toast(activity.getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		if (longtoast) {
			toast.setDuration(Toast.LENGTH_LONG);
		} else {
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.setView(layout);
		toast.show();
		if (!recoverable) {
			activity.finish();
		}
	}
	
	public void log(String message) {
		Log.i("Stericson's BusyBox", message);
	}
}
