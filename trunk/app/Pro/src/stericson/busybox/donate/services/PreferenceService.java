package stericson.busybox.donate.services;

import stericson.busybox.donate.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceService
{
	//All encryption AND decryption happens here.
	
	private SharedPreferences sharedPreferences = null;
	private Editor editor = null;
	
	public PreferenceService(Context context)
	{
		sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, 0);
	}
	
	
//	public String getUsername()
//	{
//		if (sharedPreferences.getString(Constants.PREF_KEY_USERNAME, null) != null)				
//			return EncryptService.decrypt(Base64.decode(sharedPreferences.getString(Constants.PREF_KEY_USERNAME, null), Base64.DEFAULT));
//		else
//			return null;
//	}
	
    public boolean hasPreference(String name)
    {
        return sharedPreferences.contains(name);
    }

    public boolean getDeleteDatabase()
   	{
   		return sharedPreferences.getBoolean("deleteDatabase", false);
   	}

   	public PreferenceService setDeleteDatabase(boolean delete)
   	{
   		getEditor().putBoolean("deleteDatabase", delete).commit();
        return this;
   	}

    public boolean getDeleteBackups()
   	{
   		return sharedPreferences.getBoolean("deletebackups", false);
   	}

   	public PreferenceService setDeleteBackups(boolean delete)
   	{
   		getEditor().putBoolean("deletebackups", delete).commit();
        return this;
   	}

    public boolean getMakeBackup()
   	{
   		return sharedPreferences.getBoolean("makebackup", false);
   	}

   	public PreferenceService setMakeBackup(boolean backup)
   	{
   		getEditor().putBoolean("makebackup", backup).commit();
           return this;
   	}

	public void commit()
	{
		getEditor().commit();
	}

	private Editor getEditor()
	{
		if (editor == null)
			editor = sharedPreferences.edit();
		return editor;
	}

    public PreferenceService removePreference(String name) {
        getEditor().remove(name);
        return this;
    }
}
