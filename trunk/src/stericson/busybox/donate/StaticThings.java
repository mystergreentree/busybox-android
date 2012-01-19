package stericson.busybox.donate;

import java.util.Set;

import android.app.ProgressDialog;

public class StaticThings {
	
	public static boolean failed = false;
	public static String dialogTitle;
	public static String dialogMSG;
	public static Set<String> PATH;

	public static ProgressDialog pBarDialog;
	
	// Switch for hiding the patiencedialog
	public static boolean patienceShowing;
	public static ProgressDialog patience;
	public static String mymsg;

	public static void patience() {
		if (patienceShowing) {
			patience.dismiss();
			patienceShowing = false;
		}
	}
}
