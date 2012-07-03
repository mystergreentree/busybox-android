package stericson.busybox.donate.listeners;

import stericson.busybox.donate.App;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Version implements OnItemSelectedListener
{

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3)
	{
		App.getInstance().setVersion(arg0.getSelectedItem().toString());
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}

}
