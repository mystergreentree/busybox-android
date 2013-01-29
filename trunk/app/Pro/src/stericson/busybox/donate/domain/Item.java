package stericson.busybox.donate.domain;

import java.io.Serializable;

public class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	private String applet = "";
	private String appletPath = "";
	private String symlinkedTo = "";
	private String appletDescription = "";
	private String backupHardlink = "";
	private String backupSymlink = "";
	private String backupFilePath = "";
	private String inode = "";
	private boolean overwrite = true;
	private boolean found = false;
	private boolean recommend = true;
	private boolean remove = false;
	private boolean overwriteAll = false;
	private boolean ishardlink = false;
	
	public String getInode()
	{
		return inode;
	}

	public void setInode(String inode)
	{
		this.inode = inode;
	}
	
	public String getBackupHardlink()
	{
		return backupHardlink;
	}

	public void setBackupHardlink(String backupHardlink)
	{
		this.backupHardlink = backupHardlink;
	}

	public boolean isIshardlink()
	{
		return ishardlink;
	}

	public void setIshardlink(boolean ishardlink)
	{
		this.ishardlink = ishardlink;
	}

	public String getBackupFilePath()
	{
		return backupFilePath;
	}

	public void setBackupFilePath(String backupFilePath)
	{
		this.backupFilePath = (backupFilePath != null) ? backupFilePath : "";;
	}

	public String getBackupSymlink()
	{
		return (backupSymlink != null) ? backupSymlink : "";
	}

	public void setBackupSymlink(String backupSymlink)
	{
		this.backupSymlink = backupSymlink;
	}

	public void setOverwriteAll(boolean overwriteAll)
	{
		this.overwriteAll = overwriteAll;
	}
	
	public void setApplet(String applet)
	{
		this.applet = applet;
	}

	public void setAppletPath(String appletPath)
	{
		this.appletPath = appletPath;
	}

	public void setSymlinkedTo(String symlinkedTo)
	{
		this.symlinkedTo = symlinkedTo;
	}

	public void setRecommend(boolean recommend)
	{
		this.recommend = recommend;
	}
	
	public void setFound(boolean found)
	{
		this.found = found;
	}
	
	public void setOverwrite(boolean overwrite)
	{
		this.overwrite = overwrite;
	}
	
	public void setRemove(boolean remove)
	{
		this.remove = remove;
	}
	
	public void setDescription(String description)
	{
		this.appletDescription = description;
	}
	
	public boolean getOverwriteall()
	{
		return this.overwriteAll;	
	}
	
	public String getApplet()
	{
		return (applet != null) ? applet : "";
	}

	public String getAppletPath()
	{
		return (appletPath != null) ? appletPath : "";
	}

	public String getSymlinkedTo()
	{
		return (symlinkedTo != null) ? symlinkedTo : "";
	}
	
	public boolean getRecommend()
	{
		return recommend;
	}
	
	public boolean getOverWrite()
	{
		return overwrite;
	}
	
	public boolean getFound()
	{
		return found;
	}
	
	public boolean getRemove()
	{
		return remove;
	}
	
	public String getDescription()
	{
		return (appletDescription != null) ? appletDescription : "";
	}
}
