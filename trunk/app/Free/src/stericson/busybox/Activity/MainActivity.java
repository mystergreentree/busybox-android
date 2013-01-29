package stericson.busybox.Activity;

import java.util.Calendar;

import stericson.busybox.App;
import stericson.busybox.R;
import stericson.busybox.adapter.PageAdapter;
import stericson.busybox.domain.Result;
import stericson.busybox.interfaces.CallBack;
import stericson.busybox.interfaces.Choice;
import stericson.busybox.jobs.GatherAppletInformation;
import stericson.busybox.jobs.InitialChecks;
import stericson.busybox.jobs.InstallJob;
import stericson.busybox.jobs.UninstallJob;
import stericson.busybox.listeners.PageChange;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends BaseActivity implements CallBack, Choice {

	//Constants
	private static final int UNINSTALL = 0;
	private static final int INSTALL = 1;
	private static final int RESTORE = 2;

	private TextView header;
	private ViewPager pager;
	private PageAdapter adapter;
	private TitlePageIndicator indicator;

	private Button install;
	private Button uninstall;
	private Button restore;
	
	public TextView view1;
	public TextView view2;
	
	//maintains current page position
	public int position;
	
	private String custom = "";
	private TextView freespace;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate( savedInstanceState );
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.main);
	    	    
	    App.getInstance().setItemList(null);
	    
		randomAnimation();

		install = (Button) findViewById(R.id.install);
		uninstall = (Button) findViewById(R.id.uninstall);
		restore = (Button) findViewById(R.id.restore);
		
	    header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);
				
		new InitialChecks(this).execute();		
		
		final CheckBox autoupdate = (CheckBox) findViewById(R.id.autoupdate);
		autoupdate.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				MainActivity.this.initiatePopupWindow(getString(R.string.autoupdate), false, MainActivity.this);
				autoupdate.setChecked(false);
			}
			
		});
	}
    
    public void initiatePager()
    {
    	if (pager == null)
    	{
		    pager = (ViewPager)findViewById(R.id.viewpager);
		    indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		    
		    adapter = new PageAdapter(MainActivity.this);
		    indicator.setOnPageChangeListener(new PageChange(MainActivity.this, pager));
		    pager.setAdapter(adapter);
		    indicator.setViewPager(pager);
		    pager.setCurrentItem(1);
		    this.position = 1;
    	}
    }

	public void install(View v)
	{
		if (App.getInstance().getItemList() == null)
		{			
			if (RootTools.hasEnoughSpaceOnSdCard(1600)) 
			{			
				if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null)
				{				
					install.setEnabled(false);
					new InstallJob(this, App.getInstance().getVersion(), App.getInstance().getPath()).execute();
				}
				else
				{
					initiatePopupWindow(getString(R.string.unexpectederror), false, this);				
				}
			}
			else 
			{
				initiatePopupWindow(this.getString(R.string.sdcard), true, this);
			}	
		}
		else
		{
			this.makeChoice(this, INSTALL, R.string.install_type, R.string.install_type_content, R.string.smart_install, R.string.normal_install);			
		}
	}
	
	public void uninstall(View v)
	{
		this.makeChoice(this, UNINSTALL, R.string.careful, R.string.beforeUninstall, R.string.uninstall, R.string.cancel);
	}
	
	public void restore(View v)
	{
		this.initiatePopupWindow(this.getString(R.string.proonly_backup), false, this);		
	}
	
    public void uninstallDone(Result result)
    {    
    	if (result.isSuccess())
    	{
	    	if (pager != null)
	    		pager.setCurrentItem(2);
	    	
	        RootTools.remount("/system", "ro");
	        
	        if (!App.getInstance().isInstalled())
	        {
	        	initiatePopupWindow(this.getString(R.string.uninstallsuccess), false, this);                     
	        }
	        else
	        {
	        	initiatePopupWindow(this.getString(R.string.uninstallfailed), false, this);
	        }  
    	}
    	else
    	{
        	initiatePopupWindow(result.getError(), false, this);
    	}
    };

	public void installDone(Result install_result)
	{
		String currentVersion = App.getInstance().getCurrentVersion();
			    
		if (install_result.isSuccess())
		{
			install.setEnabled(true);
			
			pager.setCurrentItem(2);
			
			RootTools.remount("/system", "ro");
			
			boolean result = RootTools.checkUtil("busybox");
			
			if (result)
			{
				uninstall.setEnabled(true);
				
				String thisVersion = RootTools.getBusyBoxVersion();
				
				if (thisVersion == null)
				{
					thisVersion = "";
				}
	
				if (currentVersion == null)
				{
					currentVersion = "-1";
				}
				
				if (thisVersion.contains(App.getInstance().getVersion().toLowerCase().replace("busybox", "").trim()))
				{
					if (currentVersion.equals(thisVersion))
					{
						initiatePopupWindow(this.getString(R.string.installedsame), false, this);
					}
					else
					{
						initiatePopupWindow(this.getString(R.string.installedunique), false, this);
					}
				} 
				else
				{
					if (currentVersion.equals(thisVersion))
					{
						initiatePopupWindow(this.getString(R.string.installedsame), false, this);
					}
					else
					{
						initiatePopupWindow(this.getString(R.string.installedsomethingelse), false, this);
					}
				}
			} 
			else
			{
				initiatePopupWindow(this.getString(R.string.failed), true, this);
			}
		}
		else
		{
			initiatePopupWindow(install_result.getError(), true, this);
		}
	};

	@Override
	public void choiceMade(boolean choice, int id)
	{
		if (id == UNINSTALL)
		{
			if (choice)
			{
	    		uninstall.setEnabled(false);
	    			    		
    			new UninstallJob(MainActivity.this).execute();				
			}
		}
		else if (id == INSTALL)
		{
			App.getInstance().setSmartInstall(choice);
			
			if (RootTools.hasEnoughSpaceOnSdCard(1600)) 
			{			
				if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null)
				{				
					install.setEnabled(false);
					new InstallJob(this, App.getInstance().getVersion(), App.getInstance().getPath()).execute();
				}
				else
				{
					initiatePopupWindow(getString(R.string.unexpectederror), false, this);				
				}
			}
			else 
			{
				initiatePopupWindow(this.getString(R.string.sdcard), false, this);
			}
		}
	}
	
	public void jobCallBack(Result result, int id)
    {
		if (id == InitialChecks.Checks)
		{
			if (result.getError() != null && !result.getError().equals(""))
				this.initiatePopupWindow(result.getError(), true, this);
			else {
		    	new GatherAppletInformation(this, false).execute();
	
				initiatePager();
				
				//if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 28) {
				//	this.initiatePopupWindow(getString(R.string.welcome_sale), false, this);
				//} else {
				//	this.initiatePopupWindow(getString(R.string.welcome), false, this);
				//}

                this.initiatePopupWindow(getString(R.string.welcome), false, this);
				
			    restore.setEnabled(true);
			    install.setEnabled(true);
			    
				if (App.getInstance().isInstalled())
				{
					uninstall.setEnabled(true);
				}
			}
		}
		if (id == GatherAppletInformation.APPLET_INFO)
		{
			App.getInstance().setItemList(result.getItemList());
			pager.setAdapter(adapter);
		    pager.setCurrentItem(position);
		    
			this.initiatePopupWindow(getString(R.string.smartinstall), false, this);

		}
	}
	
	public void toggle_smart(View v)
	{
		try
		{
			App.getInstance().setToggled(!App.getInstance().isToggled());
			updateList();
			
    		if (App.getInstance().isToggled())
    			this.initiatePopupWindow(getString(R.string.smartinstall), false, this);

    		ImageButton toggle = (ImageButton) pager.findViewById(R.id.toggle_smart);
    		toggle.setImageDrawable(getResources().getDrawable(App.getInstance().isToggled() ? R.drawable.arrow_up_float : R.drawable.arrow_down_float));
		}
		catch (Exception ignore) {}
	}
	
	public void updateList()
	{
		try
		{
			App.getInstance().getAppletadapter().update();
		}
		catch(Exception ignore) {}

		try
		{
			App.getInstance().getTuneadapter().update();
		}
		catch(Exception ignore) {}
	}
	
	public void close(View v)
	{
		super.close(v);
		
		if (!endApplication)
		{

		}
	}
	
	public String getCustomPath()
	{
		return this.custom;
	}
	
	public TextView getFreeSpace()
	{
		return this.freespace;
	}
	
	public void setCustomPath(String path)
	{
		this.custom = path;
	}
	
	public void setFreeSpace(TextView space)
	{
		this.freespace = space;
	}
	
	public ListView getListView()
	{
		return listView;
	}

	public void setListView(ListView listView)
	{
		this.listView = listView;
	}
}
