package stericson.busybox.donate.Activity;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.stericson.RootTools.RootTools;

import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PathActivity extends BaseActivity
{

	private Spinner path;
	private ArrayAdapter<String> adapterForSpinner;
	private String version;
	private static String customPath;
	
	private TextView	informationView,
						header,
						versionNumber;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.path);
				
		header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);

		versionNumber = (TextView) findViewById(R.id.versionInformation);
		versionNumber.setTypeface(tf);

		informationView = (TextView) findViewById(R.id.choose);
		informationView.setText(informationView.getText().toString() + "\n\n " + buildInformation());
		
		version = this.getIntent().getExtras().getString(Constants.EXTRA_BUSYBOX_VERSION);
		path = (Spinner) findViewById(R.id.path);
	
		path.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (arg2 == path.getCount() - 1) 
				{
					final EditText input = new EditText(PathActivity.this);
					new AlertDialog.Builder(PathActivity.this)
				    .setTitle("Custom Path")
				    .setMessage("Please enter the Path you want to install Busybox, Success is not gauranteed. \n\n This is an advanced option")
				    .setView(input)
				    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				        	File file = new File(input.getText().toString());
				        	if (file.exists())
				        	{
				        		customPath = input.getText().toString();
				        		Toast.makeText(PathActivity.this, "Custom install path set to " + customPath, Toast.LENGTH_LONG).show();
				        	}
				        	else
				        	{
				        		Toast.makeText(PathActivity.this, "That path does not exist or is not valid!", Toast.LENGTH_LONG).show();
				        		path.setSelection(0);
					            customPath = "";
				        	}
				        }
				    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            path.setSelection(0);
				            customPath = "";
				        }
				    }).show();
				}
				else
				{
					customPath = "";
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		adapterForSpinner = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, configureSpinner());
		adapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		path.setAdapter(adapterForSpinner);
	}

	public String buildInformation()
	{
		
		String information = "";
		
		int count = this.findBusyBoxLocations().length;
		if (count == 0)
		{
			information += this.getString(R.string.notfound) + "\n\n";
		}
		else
		{
			if (count > 1)
			{	
				information += this.getString(R.string.morethanone) + "\n\n";
			}
			else
			{
				information += this.getString(R.string.foundit) + "\n\n" + (this.findBusyBoxLocations())[0];				
			}
		}
		
		return information;
	}

	public void proceed(View v)
	{
		Intent intent = new Intent(this, InstallActivity.class);
		intent.putExtra(Constants.EXTRA_INSTALL_PATH, path.getSelectedItem().toString());
		intent.putExtra(Constants.EXTRA_BUSYBOX_VERSION, version);
		startActivity(intent);
		randomAnimation();
		finish();
	}

	// Configure our Spinner for available install paths
	private String[] configureSpinner() {
		
		String[] busyboxLocation;
		
		busyboxLocation = findBusyBoxLocations();
		
		Set<String> tmpSet = new HashSet<String>();
		if (busyboxLocation != null) {
			for (String locations : busyboxLocation) {
				tmpSet.add(locations);
			}
			tmpSet.add("/system/bin/");
			tmpSet.add("/system/xbin/");
			
			busyboxLocation = new String[tmpSet.size() + 1];
			util.log("TMPSET " + tmpSet.size());
			int count = 0;
			for (String locations : tmpSet) {
				util.log(locations);
				busyboxLocation[count] = locations;
				count++;
			}
			
			busyboxLocation[tmpSet.size()] = "Custom Path";

			util.log("Count " + busyboxLocation.length);

		} else {
			busyboxLocation = new String[3];
			busyboxLocation[0] = "/system/bin/";
			busyboxLocation[1] = "/system/xbin/";
			busyboxLocation[2] = "Custom Path";
		}
		
		return busyboxLocation;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

	    	if (pw != null && pw.isShowing()) {
				//do nothing
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
}
