package stericson.busybox.donate.Activity;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.adapter.TuneAdapter;
import stericson.busybox.donate.domain.JobResult;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;
import stericson.busybox.donate.jobs.GatherAppletInformation;
import stericson.busybox.donate.listeners.AppletLongClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TuneActivity extends BaseActivity implements CallBack
{

	private String version;
	
	private TextView	header,
						versionNumber;
	
	private ListView list;
	private ListAdapter listAdapter;
	private AsyncTask<Void, Object, JobResult<Result>> appletJob;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tune);
					
		header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);

		versionNumber = (TextView) findViewById(R.id.versionInformation);
		versionNumber.setTypeface(tf);
		
		version = this.getIntent().getExtras().getString(Constants.EXTRA_BUSYBOX_VERSION);
		
		list = (ListView) this.findViewById(R.id.list);
		list.setFastScrollEnabled(true);
		
		appletJob = new GatherAppletInformation(this).execute();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		appletJob.cancel(true);
		
	}
	public String buildInformation()
	{
		
		String information = "";
				
		return information;
	}

	public void proceed(View v)
	{
		Intent intent = new Intent(this, PathActivity.class);
		intent.putExtra(Constants.EXTRA_BUSYBOX_VERSION, version);
		startActivity(intent);
		randomAnimation();
		finish();
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

	public void jobCallBack(Result result) 
	{
		RelativeLayout container = (RelativeLayout) this.findViewById(R.id.container);
		container.setVisibility(View.VISIBLE);
		
		App.getInstance().setItemList(result.getItemList());
		listAdapter = new TuneAdapter(this, result.getItemList());
		list.setAdapter(listAdapter);
		
		Toast.makeText(this, R.string.longPress, Toast.LENGTH_LONG).show();
		
		initiatePopupWindow(this.getString(R.string.customtunewarn), false, this);

	}
}
