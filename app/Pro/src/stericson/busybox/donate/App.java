package stericson.busybox.donate;

import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.adapter.AppletAdapter;
import stericson.busybox.donate.adapter.TuneAdapter;
import stericson.busybox.donate.domain.Item;
import android.view.View;

public class App
{
	private static App instance = null;
	
	private List<Item> itemList;
	private List<String> availableApplets;
	
	private View popupView;

	private boolean toggled = false;
	private boolean smartInstallLoaded = false;
	private boolean smartInstall = false;
	private boolean isInstalled = false;
	private boolean choose;
	private boolean installCustom = false;
	private boolean stericson = false;
	
	private String path = "";
	private String currentVersion = "";
	private String version = Constants.versions[0];
	private String found = "";
	private String status = "";
	
	private float space = 0;
	private float progress = 0;
		
	private AppletAdapter appletadapter;
	private TuneAdapter tuneadapter;

	private App() {}
	
	public static App getInstance()
	{
		if (instance == null)
			instance = new App();
		return instance;
	}
	
	public List<String> getAvailableApplets()
	{
		return availableApplets;
	}

	public void setAvailableApplets(List<String> availableApplets)
	{
		this.availableApplets = availableApplets;
	}

	public float getProgress()
	{
		return progress;
	}

	public void setProgress(float progress)
	{
		this.progress = progress;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public float getSpace()
	{
		return space;
	}

	public void setSpace(float space)
	{
		this.space = space;
	}

	public boolean isSmartInstallLoaded()
	{
		return smartInstallLoaded;
	}

	public void setSmartInstallLoaded(boolean smartInstallLoaded)
	{
		this.smartInstallLoaded = smartInstallLoaded;
	}

	public boolean isInstallCustom()
	{
		return installCustom;
	}

	public void setInstallCustom(boolean installCustom)
	{
		this.installCustom = installCustom;
	}

	public boolean isStericson()
	{
		return stericson;
	}

	public void setStericson(boolean stericson)
	{
		this.stericson = stericson;
	}

	public boolean isChoose()
	{
		return choose;
	}

	public void setChoose(boolean choose_custom)
	{
		this.choose = choose_custom;
	}

	public boolean isInstalled()
	{
		return isInstalled;
	}

	public void setInstalled(boolean isInstalled)
	{
		this.isInstalled = isInstalled;
	}

	public AppletAdapter getAppletadapter()
	{
		return appletadapter;
	}

	public void setAppletadapter(AppletAdapter appletadapter)
	{
		this.appletadapter = appletadapter;
	}

	public TuneAdapter getTuneadapter()
	{
		return tuneadapter;
	}

	public void setTuneadapter(TuneAdapter tuneadapter)
	{
		this.tuneadapter = tuneadapter;
	}

	public String getFound()
	{
		return found;
	}

	public void setFound(String found)
	{
		this.found = found;
	}

	public String getCurrentVersion()
	{
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion)
	{
		this.currentVersion = currentVersion;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}
	
	public boolean isSmartInstall()
	{
		return smartInstall;
	}

	public void setSmartInstall(boolean smartInstall)
	{
		this.smartInstall = smartInstall;
	}

	public boolean isToggled()
	{
		return toggled;
	}

	public void setToggled(boolean toggled)
	{
		this.toggled = toggled;
	}
		
	public void setPopupView(View view)
	{
		this.popupView = view;
	}
	
	public void setItemList(List<Item> itemList)
	{
		if (itemList != null)
		{
			this.itemList = new ArrayList<Item>();
			this.itemList.addAll(itemList);
		}
		else
		{
			this.itemList = null;
		}
	}
	
	public List<Item> getItemList()
	{
		return this.itemList;
	}
	
	public View getPopupView()
	{
		return this.popupView;
	}
	
	
	public void updateVersion(int index)
	{
		try {
			App.getInstance().getTuneadapter().setVersion_index(index);
		} catch (Exception e) {}
	}
	
	public void updatePath(int index) {
		try {
			App.getInstance().getTuneadapter().setPath_index(index);
		} catch (Exception e) {}
	}
}
