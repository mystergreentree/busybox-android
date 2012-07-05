package stericson.busybox.donate.Activity;

import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.adapter.PageAdapter;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.interfaces.CallBack;
import stericson.busybox.donate.interfaces.Choice;
import stericson.busybox.donate.jobs.GatherAppletInformation;
import stericson.busybox.donate.jobs.InitialChecks;
import stericson.busybox.donate.jobs.InstallJob;
import stericson.busybox.donate.jobs.RestoreBackup;
import stericson.busybox.donate.jobs.UninstallJob;
import stericson.busybox.donate.listeners.PageChange;
import stericson.busybox.donate.services.AppletService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
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
	private boolean installed = false;
	private boolean clean = false;
	private ListView listView;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
	    super.onCreate( savedInstanceState );
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.main);
	    	    
	    App.getInstance().setItemList(null);
	    
		randomAnimation();

		stopGatherInformation();
		
		install = (Button) findViewById(R.id.install);
		uninstall = (Button) findViewById(R.id.uninstall);
		restore = (Button) findViewById(R.id.restore);
		
	    header = (TextView) findViewById(R.id.header_main);
		header.setTypeface(tf);
		
		new InitialChecks(this).execute();
		
		final CheckBox autoupdate = (CheckBox) findViewById(R.id.autoupdate);
		final SharedPreferences sp = getSharedPreferences("BusyBox", 0);				
		autoupdate.setChecked(sp.getBoolean("auto-update", false));
		
		autoupdate.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				sp.edit().putBoolean("auto-update", isChecked).commit();
				
				if (isChecked)
					MainActivity.this.initiatePopupWindow("This feature will automatically update Busybox to the latest version when a new version is available and when updates are done to the newest version. \n\n To best take advantage of this feature, allow Busybox to automatically update from the market as the update is delivered from the Android market. \n\n No interaction is required on your part to keep Busybox updated when using this feature..Enjoy! ", false, MainActivity.this);
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
			stopGatherInformation();
			
			if (RootTools.hasEnoughSpaceOnSdCard(1600)) 
			{			
				if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null)
				{				
					install.setEnabled(false);
					new InstallJob(this, App.getInstance().getVersion(), App.getInstance().getPath()).execute();
				}
				else
				{
					initiatePopupWindow("An unexpected error has occured, please take a screenshot of the application and send it to me at StericDroid@gmail.com", false, this);				
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
		this.makeChoice(this, RESTORE, R.string.restore, R.string.beforeRestore, R.string.restore, R.string.cancel);		
	}
	
    public void uninstallDone(Result result)
    {    
	    gatherInformation(Constants.appletsString);

    	if (result.isSuccess())
    	{
	    	if (pager != null)
	    		pager.setCurrentItem(2);
	    	
	        RootTools.remount("/system", "ro");
	        
	        if (!RootTools.isBusyboxAvailable())
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

    public void restoreDone(Result result)
    {
    	if (result.isSuccess())
    	{
    		this.initiatePopupWindow(getString(R.string.restoreSuccess), false, this);
    	}
    	else
    	{
    		this.initiatePopupWindow(getString(R.string.restoreErrors), false, this);    		
    	}
    }
    
	public void installDone(Result install_result)
	{
		String currentVersion = App.getInstance().getCurrentVersion();
		
	    gatherInformation(Constants.appletsString);
	    
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

    public void installAppletDone(Result result, int position, AdapterView<?> adapter, String applet)
    {

	    gatherInformation(new String[] {applet});

    	if (result.isSuccess())
    	{
    		Item item = App.getInstance().getItemList().get(position);

    		item.setAppletPath("/system/xbin");
    		item.setFound(true);
    		item.setOverwrite(false);
    		item.setRecommend(false);
    		item.setRemove(false);
    		item.setSymlinkedTo("");
    		
    		try
			{
				List<String> result1 = RootTools.sendShell("busybox " + applet + " --help", -1);
				String appletInfo = "";
				
				for (String info : result1)
				{
					if (!info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals(""))
					{
						appletInfo += info + "\n";
					}
				}
				
				item.setDescription(appletInfo);
			}
			catch (Exception e)
			{
				item.setDescription(this.getString(R.string.noInfo));
			}
			
			updateList();    		
    		
			initiatePopupWindow("Congratulations! The applet was installed successfully!", false, this);
    	}
    	else
    	{
			initiatePopupWindow("Uh Oh! The applet was not installed successfully!", false, this);
    	}
    }
    
    public void uninstallAppletDone(Result result, int position, AdapterView<?> adapter, String applet)
    {
	    gatherInformation(new String[] {applet});

    	if (result.isSuccess())
    	{
    		Item item = App.getInstance().getItemList().get(position);
    		
    		item.setAppletPath("");
    		item.setFound(false);
    		item.setOverwrite(false);
    		item.setRecommend(true);
    		item.setRemove(false);
    		item.setDescription("");
    		item.setSymlinkedTo("");
			updateList();
			
			initiatePopupWindow("Congratulations! The applet was uninstalled successfully!", false, this);
    	}
    	else
    	{
			initiatePopupWindow("Uh Oh! The applet was not uninstalled successfully!", false, this);
    	}
    }

	public void jobCallBack(Result result, int id) {
		if (id == InitialChecks.Checks)
		{
			if (result.getMessage() != null && !result.getMessage().equals(""))
				this.initiatePopupWindow(result.getMessage(), false, this);

	    	new GatherAppletInformation(this, false).execute();

			initiatePager();
		}
		if (id == GatherAppletInformation.APPLET_INFO)
		{
			App.getInstance().setItemList(result.getItemList());
			pager.setAdapter(adapter);
		    pager.setCurrentItem(position);
		 
		    gatherInformation(Constants.appletsString);
		    
		    restore.setEnabled(true);
		    
			if (RootTools.isBusyboxAvailable())
			{
				uninstall.setEnabled(true);
			}
		}
	}
	
	@Override
	public void choiceMade(boolean choice, int id)
	{
		if (id == UNINSTALL)
		{
			if (choice)
			{
	    		uninstall.setEnabled(false);
	    		
	    		stopGatherInformation();
	    		
    			new UninstallJob(MainActivity.this).execute();				
			}
		}
		else if (id == INSTALL)
		{
			App.getInstance().setSmartInstall(choice);
			stopGatherInformation();
			
			if (RootTools.hasEnoughSpaceOnSdCard(1600)) 
			{			
				if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null)
				{				
					install.setEnabled(false);
					new InstallJob(this, App.getInstance().getVersion(), App.getInstance().getPath()).execute();
				}
				else
				{
					initiatePopupWindow("An unexpected error has occured, please take a screenshot of the application and send it to me at StericDroid@gmail.com", false, this);				
				}
			}
			else 
			{
				initiatePopupWindow(this.getString(R.string.sdcard), false, this);
			}
		}
		else if (id == RESTORE)
		{
			if (choice)
			{
				stopGatherInformation();
				new RestoreBackup(this).execute();
			}
		}

	}
	
	public void gatherInformation(String[] applets)
	{
		Intent intent = new Intent(this, AppletService.class);
		intent.putExtra("applets", applets);

		//Try to stop an existing service..
		stopService(intent);
		
		//Try to start a new one.
	    startService(intent);
	}
	
	public void stopGatherInformation()
	{
		Intent intent = new Intent(this, AppletService.class);
		
		//Try to stop an existing service..
		stopService(intent);
		
	}
	
	public void toggle_smart(View v)
	{
		try
		{
			App.getInstance().setToggled(!App.getInstance().isToggled());
			updateList();
			
    		if (App.getInstance().isToggled())
    			this.initiatePopupWindow("Before you begin tweaking things please be aware that changing the settings and selections for smart install is for advanced users only! \n\n Some binaries found on your system should NOT be replaced by Busybox and doing so can make your device perform in an undesireable manner. The only reason you should tweak the settings below is if you know exactly what you are doing or if you know how to reflash your rom to fix issues that may occur from modifying the selections below.", false, this);

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
	
	public boolean getClean()
	{
		return this.clean;
	}
	
	public String getCustomPath()
	{
		return this.custom;
	}
	
	public TextView getFreeSpace()
	{
		return this.freespace;
	}
	
	public boolean getInstalled()
	{
		return this.installed;
	}
	
	public void setCustomPath(String path)
	{
		this.custom = path;
	}
	
	public void setFreeSpace(TextView space)
	{
		this.freespace = space;
	}
	
	public void setInstalled(boolean installed)
	{
		this.installed = installed;
	}
	
	public void setClean(boolean clean)
	{
		this.clean = clean;
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
