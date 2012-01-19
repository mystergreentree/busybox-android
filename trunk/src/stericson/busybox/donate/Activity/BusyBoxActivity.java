package stericson.busybox.donate.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.listeners.AppletCheck;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class BusyBoxActivity extends BaseActivity {

	private Spinner appletCheck;
	private Spinner version;
	private ArrayAdapter<String> adapterForSpinner2;	
	private ArrayAdapter<String> adapterForSpinner3;	
	private String[] versions = { "BusyBox 1.19.3", "BusyBox 1.19.2", "BusyBox 1.18.5", "BusyBox 1.18.4", "BusyBox 1.17.1"};

	private Button install;
	private Button uninstall;

	private TextView	informationView,
						header,
						versionNumber;
	
	private CheckBox customTune;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		randomAnimation();
		
        App.getInstance().setItemList(null);
		
        customTune = (CheckBox) this.findViewById(R.id.customtune);
		
		header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);

		appletCheck = (Spinner) findViewById(R.id.appletcheck);
		List<String> list = new ArrayList<String>();
		list.add("Applet Checker");
		list.add("What is this?");
		for(String applet : Constants.appletsString)
		{
			list.add(applet);
		}
		adapterForSpinner3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		adapterForSpinner3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		appletCheck.setAdapter(adapterForSpinner3);
		appletCheck.setOnItemSelectedListener(new AppletCheck(this));
		
		versionNumber = (TextView) findViewById(R.id.versionInformation);
		versionNumber.setTypeface(tf);

		informationView = (TextView) findViewById(R.id.choose);
		informationView.setText(informationView.getText().toString() + buildInformation());
		
		version = (Spinner) findViewById(R.id.busyboxversiontobe);
		adapterForSpinner2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, versions);
		adapterForSpinner2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		version.setAdapter(adapterForSpinner2);

		install = (Button) findViewById(R.id.install);
		uninstall = (Button) findViewById(R.id.uninstall);
		
		if (!RootTools.isBusyboxAvailable())
		{
			uninstall.setVisibility(View.GONE);
			uninstall.setEnabled(false);
		}
		
		if (!RootTools.isRootAvailable()) {
			initiatePopupWindow(this.getString(R.string.noroot2), true, this);
		}
		else
		{
			try {
				if (!RootTools.isAccessGiven()) {
					initiatePopupWindow(this.getString(R.string.noAccess), true, this);
				}
				else
				{
					try 
					{
						if (!checkCoreUtils())
						{
							initiatePopupWindow(this.getString(R.string.utilProblem), false, this);
						}
					}
					catch(Exception e) {
						RootTools.log(e.toString());
					}
				}
			} catch (TimeoutException e) {				
				try 
				{
					if (!checkCoreUtils())
					{
						initiatePopupWindow(this.getString(R.string.accessUndetermined) + "\n\n" + this.getString(R.string.utilProblem), false, this);
					}
					else
					{
						initiatePopupWindow(this.getString(R.string.accessUndetermined), false, this);
					}
				}
				catch(Exception ex) {
					RootTools.log(e.toString());
				}
			}
		}
	}

	public String buildInformation()
	{

		String information = "";

		try {
			if (RootTools.isAccessGiven()) {
	
				int count = this.findBusyBoxLocations().length;
				if (count == 0)
				{
					information += this.getString(R.string.notfound) + "\n\n";
				}
				else
				{
					if (count > 1)
					{	
						information += this.getString(R.string.morethanone);
					}
					else
					{
						
						information += this.getString(R.string.busyboxversion) + " " + RootTools.getBusyBoxVersion();
					}
				}
				
			}
		} catch (TimeoutException e) {}
		
		return information;
	}

	public void proceed(View v)
	{
		Intent i;
		
		if (customTune.isChecked())
		{
			i = new Intent(this, TuneActivity.class);
		}
		else
		{
			i = new Intent(this, PathActivity.class);
			App.getInstance().setItemList(null);
		}
		
		i.putExtra(Constants.EXTRA_BUSYBOX_VERSION, version.getSelectedItem().toString());
		this.startActivity(i);
		randomAnimation();
		finish();
	}
	
	public void uninstall(View v)
	{
		Intent i = new Intent(this, UninstallActivity.class);
		this.startActivity(i);
		randomAnimation();
		finish();
	}
}