package stericson.busybox.donate.Activity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;
import stericson.busybox.donate.App;
import stericson.busybox.donate.Common;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.adapter.PageAdapter;
import stericson.busybox.donate.custom.DropDownAnim;
import stericson.busybox.donate.custom.FontableTextView;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.domain.Result;
import stericson.busybox.donate.fileexplorer.FileList;
import stericson.busybox.donate.interfaces.CallBack;
import stericson.busybox.donate.interfaces.Choice;
import stericson.busybox.donate.jobs.GatherAppletInformation;
import stericson.busybox.donate.jobs.GetAvailableAppletsJob;
import stericson.busybox.donate.jobs.InitialChecks;
import stericson.busybox.donate.jobs.InstallJob;
import stericson.busybox.donate.jobs.PrepareBinaryJob;
import stericson.busybox.donate.jobs.RestoreBackup;
import stericson.busybox.donate.jobs.UninstallJob;
import stericson.busybox.donate.listeners.PageChange;
import stericson.busybox.donate.services.AppletService;
import stericson.busybox.donate.services.DBService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.viewpagerindicator.TitlePageIndicator;
import stericson.busybox.donate.services.PreferenceService;

public class MainActivity extends BaseActivity implements CallBack, Choice {

	private TextView header;
	private ViewPager pager;
	private PageAdapter adapter;
	private TitlePageIndicator indicator;

	private Button install;
	private Button uninstall;
	private Button restore;
	
	//maintains current page position
	public int position;
	
	private String custom = "";
	private boolean installed = false;
	private boolean clean = false;
	private ListView listView;
	
	private RelativeLayout pb_container;
	private ProgressBar pb;
	private FontableTextView pb_msg;

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
		pb_container = (RelativeLayout) findViewById(R.id.progression);
		pb = (ProgressBar) findViewById(R.id.progression_bar);
		pb_msg = (FontableTextView) findViewById(R.id.progression_msg);
		
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

	protected void onResume() {
		if (App.getInstance().isChoose())
		{
			//The user has returned and we should show the file browser?
			App.getInstance().setChoose(false);
			Intent i = new Intent(this, FileList.class);
			startActivityForResult(i, Constants.CHOOSE);
		}
		
		super.onResume();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Constants.CHOOSE)
		{
			if (resultCode == RESULT_OK)
			{
				Bundle extras = data.getExtras();
				String file_chosen = extras.getString("selected");
				new PrepareBinaryJob(this, file_chosen).execute();
			}
			else
			{
				App.getInstance().updateVersion(0);
				updateList();
			}
		}
	}

    public void ClearBackups() {
        try
        {
            RootTools.sendShell("rm " + getFilesDir().toString() + "/*", -1);
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
        }
        catch (Exception ignore) {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT).show();
        }
    }

    public void ClearDatabase() {
        try
        {
            new PreferenceService(this).setDeleteDatabase(true).commit();
            Toast.makeText(this, R.string.success_after, Toast.LENGTH_LONG).show();
        }
        catch (Exception ignore) {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT).show();
        }
    }

	@Override
	public void choiceCancelled(int id)
	{
		switch (id) {
			case Constants.CUSTOM_VERSION:
				App.getInstance().updateVersion(0);
				updateList();
			break;
		}
	}
	
	@Override
	public void choiceMade(boolean choice, int id)
	{
		switch (id) {
			case Constants.UNINSTALL:
				if (choice)
				{
					this.makeChoice(this, Constants.UNINSTALL_CHOICE, R.string.uninstall_type, R.string.uninstall_choice, R.string.smart_uninstall, R.string.normal_uninstall);
				}	
			break;
			
			case Constants.INSTALL:
				App.getInstance().setSmartInstall(choice);
				stopGatherInformation();
				
				if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null)
				{				
					install.setEnabled(false);
					new InstallJob(this, App.getInstance().getVersion(), App.getInstance().getPath()).execute();
				}
				else
				{
					initiatePopupWindow("An unexpected error has occured, please take a screenshot of the application and send it to me at StericDroid@gmail.com", false, this);				
				}				
			break;
			
			case Constants.NORMAL_INSTALL:
				if (choice)
				{
					App.getInstance().setSmartInstall(false);
					stopGatherInformation();
					
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
			break;
			
			case Constants.RESTORE:
				if (choice)
				{
					stopGatherInformation();
					new RestoreBackup(this).execute();
				}
			break;
			
			case Constants.CUSTOM_VERSION:
				if (choice)
				{
					App.getInstance().setChoose(false);
					Intent i = new Intent(this, FileList.class);
					startActivityForResult(i, Constants.CHOOSE);
				}
				else
				{
					App.getInstance().setChoose(true);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("https://docs.google.com/folder/d/0B5Amguus3csDakJ5SXhaX045R0U/edit"));
					startActivity(i);
				}		
			break;
			case Constants.UNINSTALL_CHOICE:
	    		uninstall.setEnabled(false);
	    		
	    		stopGatherInformation();

    			new UninstallJob(MainActivity.this, choice).execute();

				
			break;

            case Constants.BACKUP:
                new PreferenceService(this).setMakeBackup(choice).commit();
                new GatherAppletInformation(this, false, choice).execute();
            break;
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
	
    public void hideProgress()
    {
    	DropDownAnim anim = new DropDownAnim(pb_container, Common.getDIP(this, 20), false);
    	anim.setDuration(500);
    	pb_container.startAnimation(anim);
    	
    	//pb_container.setVisibility(View.GONE);
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
		if (RootTools.hasEnoughSpaceOnSdCard(1200)) 
		{			
			if (App.getInstance().getItemList() == null)
			{
				stopGatherInformation();

				this.makeChoice(this, Constants.NORMAL_INSTALL, R.string.install_type, R.string.install_type_custom, R.string.normal_install, R.string.cancel);
			}
			else
			{
				this.makeChoice(this, Constants.INSTALL, R.string.install_type, R.string.install_type_content, R.string.smart_install, R.string.normal_install);			
			}
		}
		else 
		{
			initiatePopupWindow(this.getString(R.string.sdcard), true, this);
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
    			final List<String> result1 = new ArrayList<String>();
    			
    			Command command = new Command(0, "busybox " + applet + " --help")
    			{

					@Override
					public void commandFinished(int arg0){}

					@Override
					public void output(int arg0, String arg1)
					{
						result1.add(arg1);						
					}
    				
    			};
    			Shell.startRootShell().add(command).waitForFinish();
    			
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
    

	public void jobCallBack(Result result, int id) {
		
		switch (id) {
			case InitialChecks.Checks:
				if (result.getError() != null && !result.getError().equals(""))
					this.initiatePopupWindow(result.getError(), true, this);
				else {

					if (!new DBService(this).isReady())
						initiatePopupWindow("Hello, welcome to BusyBox Pro. \n\n It looks like the initial setup has not be completed yet. The initial setup will take a bit longer than BusyBox free the first time it is run because we are making a backup of your system as well as doing some additional checks on your system. \n\n After this initial setup is complete, startup times should be very fast. \n\n Thanks for your patience.", false, this);

                    PreferenceService p = new PreferenceService(this);

                    if (p.hasPreference("makebackup"))
                    {
                        new GatherAppletInformation(this, false, p.getMakeBackup()).execute();
                    }
                    else {
                        makeChoice(this, Constants.BACKUP, R.string.makeBackup_title, R.string.makeBackup, R.string.yes, R.string.no);
                    }

					initiatePager();
				}
				break;
			case GatherAppletInformation.APPLET_INFO:
				App.getInstance().setItemList(result.getItemList());
				updateList();
				
			    gatherInformation(Constants.appletsString);

                //if we made a backup?
                if (new PreferenceService(this).getMakeBackup()) {
			        restore.setEnabled(true);
                }
			    
		        if (App.getInstance().isInstalled())
				{
					uninstall.setEnabled(true);
				}
		        break;
			case PrepareBinaryJob.PREPARE_BINARY:
				if (result.getError() != null && !result.getError().equals("")) {
					
					App.getInstance().updateVersion(0);
					App.getInstance().setInstallCustom(false);
					updateList();
					this.initiatePopupWindow(result.getError(), false, this);
				}
				else {
					new GetAvailableAppletsJob(this).execute();
					Toast.makeText(this, "BusyBox version " + App.getInstance().getVersion() + " will be installed", Toast.LENGTH_LONG).show();
				}
				break;
				
			case GetAvailableAppletsJob.AVAIL_APPLETS:
					updateList();
				break;
		}
	}
	
	public void restore(View v)
	{
		this.makeChoice(this, Constants.RESTORE, R.string.restore, R.string.beforeRestore, R.string.restore, R.string.cancel);		
	}
	
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

    public void showProgress()
    {
    	pb_container.setVisibility(View.VISIBLE);
    	DropDownAnim anim = new DropDownAnim(pb_container, Common.getDIP(this, 20), true);
    	anim.setDuration(500);
    	pb_container.startAnimation(anim);
    	
		pb_msg.setText(pb.getProgress() + this.getString(R.string.loaded));
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
    			this.initiatePopupWindow(this.getString(R.string.before_tweak), false, this);

    		ImageButton toggle = (ImageButton) pager.findViewById(R.id.toggle_smart);
    		toggle.setImageDrawable(getResources().getDrawable(App.getInstance().isToggled() ? R.drawable.arrow_up_float : R.drawable.arrow_down_float));
		}
		catch (Exception ignore) {}
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
	
	public void uninstall(View v)
	{
		this.makeChoice(this, Constants.UNINSTALL, R.string.careful, R.string.beforeUninstall, R.string.uninstall, R.string.cancel);
	}
	
    public void uninstallDone(Result result)
    {    
	    gatherInformation(Constants.appletsString);

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
	
	public void updateProgress(Float progress)
	{
		App.getInstance().setProgress(App.getInstance().getProgress() + progress);
		pb.setProgress((int) Math.floor((double) App.getInstance().getProgress()));
		
		NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);            
        nf.setGroupingUsed(false); 
        
		pb_msg.setText(nf.format(App.getInstance().getProgress()) + this.getString(R.string.loaded));
	}
    
    //Getters and Setters
	public boolean getClean()
	{
		return this.clean;
	}
	
	public String getCustomPath()
	{
		return this.custom;
	}
	
	public boolean getInstalled()
	{
		return this.installed;
	}
	
	public void setCustomPath(String path)
	{
		this.custom = path;
	}

    public void Reset() {
        try
        {
            new PreferenceService(this).removePreference("makebackup").setDeleteDatabase(true).commit();
            RootTools.sendShell("rm " + getFilesDir().toString() + "/*", -1);

            Toast.makeText(this, R.string.success_after, Toast.LENGTH_SHORT).show();
        }
        catch (Exception ignore) {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT).show();
        }
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
