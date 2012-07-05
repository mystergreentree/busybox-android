package stericson.busybox.donate.services;


import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.Constants;
import stericson.busybox.donate.domain.Item;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.stericson.RootTools.RootTools;

public class DBService
{
	private static final String DATABASE_NAME = "BusyBox_db";
	private static final int DATABASE_VERSION = 1;

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	private static final String TBL_APPLETS = "applets";
	private static final String KEY_APPLET_ROWID = "applet_id";
	private static final String KEY_APPLET_NAME = "name";
	private static final String KEY_APPLET_PATH = "path";
	private static final String KEY_APPLET_USAGE = "usage";
	private static final String KEY_APPLET_SYMLINK = "symlink";
	private static final String KEY_APPLET_OVERWRITE = "overwrite";
	private static final String KEY_APPLET_FOUND = "found";
	private static final String KEY_APPLET_RECOMMEND = "recommend";
	private static final String KEY_APPLET_REMOVE = "remove";
	private static final String KEY_APPLET_BACKUP_SYMLINK = "backup_symlink";
	private static final String KEY_APPLET_BACKUP_HARDLINK = "backup_hardlink";
	private static final String KEY_APPLET_ISHARDLINK = "ishardlink";
	private static final String KEY_APPLET_INODE = "inode";
	private static final String KEY_APPLET_BACKUP_FILE_PATH = "backup_file_path";

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table "
	        + TBL_APPLETS + " (" + KEY_APPLET_ROWID
	        + " integer primary key autoincrement, " + 
	        KEY_APPLET_NAME + " text not null unique, " +
	        KEY_APPLET_PATH + " text not null, " +
	        KEY_APPLET_USAGE + " text not null, " +
	        KEY_APPLET_SYMLINK + " text, " +
	        KEY_APPLET_OVERWRITE + " integer not null DEFAULT 1, " +
	        KEY_APPLET_FOUND + " integer not null DEFAULT 0, " +
	        KEY_APPLET_RECOMMEND + " integer not null DEFAULT 1, " +
	        KEY_APPLET_REMOVE + " integer not null DEFAULT 0, " +
	        KEY_APPLET_ISHARDLINK + " integer not null DEFAULT 0, " +
	        KEY_APPLET_INODE + " text," +
	        KEY_APPLET_BACKUP_HARDLINK + " text," +
	        KEY_APPLET_BACKUP_SYMLINK + " text," +
			KEY_APPLET_BACKUP_FILE_PATH + " text);";

	private final Context context;

	private static class DatabaseHelper extends SQLiteOpenHelper
	{

		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			onCreate(db);
		}
	}

	public DBService(Context context)
	{
		this.context = context;
	}

	private DBService open() throws SQLException
	{
		if (new PreferenceService(context).getDeleteDatabase())
			deleteDatabase();
		
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		try
		{
			db.close();
			dbHelper.close();
		}
		catch (Exception ignore) {}
	}

	private void deleteDatabase()
	{
		context.deleteDatabase(DATABASE_NAME);
		new PreferenceService(context).setDeleteDatabase(false);
	}
	
	public boolean isEmpty()
	{
		try
		{
			open();
			Cursor cur = db.rawQuery("select count(*) from " + TBL_APPLETS, null);
			
			if (cur != null)
			{
				cur.moveToFirst();
				if (cur.getCount() > 0)
				{
					return true;
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		
		return false;
	}
	
	public boolean isReady()
	{
		try
		{
			open();
			
			Cursor cur = db.rawQuery("select count(*) from " + TBL_APPLETS, null);
			
			if (cur != null && cur.getCount() > 0)
			{
				cur.moveToFirst();

				if (cur.getInt(0) == Constants.appletsString.length)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		
		return false;
	}
	
	public Item checkForApplet(String applet)
	{
		try 
		{
			open();
			
			Cursor cur = db.rawQuery("select * from " + TBL_APPLETS + " where " + KEY_APPLET_NAME + "= ?", new String[] { applet });
			
			if (cur != null && cur.getCount() > 0)
			{
				cur.moveToFirst();
				Item item = new Item();
				item.setApplet(cur.getString(1));
				item.setAppletPath(cur.getString(2));
				item.setDescription(cur.getString(3));
				item.setSymlinkedTo(cur.getString(4));
				item.setOverwrite(cur.getInt(5) == 0 ? false : true);
				item.setFound(cur.getInt(6) == 0 ? false : true);
				item.setRecommend(cur.getInt(7) == 0 ? false : true);
				item.setRemove(cur.getInt(8) == 0 ? false : true);
				item.setIshardlink(cur.getInt(9) == 0 ? false : true);
				item.setInode(cur.getString(10));
				item.setBackupHardlink(cur.getString(11));				
				item.setBackupSymlink(cur.getString(12));
				item.setBackupFilePath(cur.getString(13));
				
				return item;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		
		return null;
	}

	public boolean insertOrUpdateRow(Item item)
	{
		try
		{
			open();
			
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_APPLET_FOUND, item.getFound());
			initialValues.put(KEY_APPLET_REMOVE, item.getRemove());
			initialValues.put(KEY_APPLET_RECOMMEND, item.getRecommend());
			initialValues.put(KEY_APPLET_PATH, item.getAppletPath());
			if (!item.getDescription().equals(""))
				initialValues.put(KEY_APPLET_USAGE, item.getDescription());
			initialValues.put(KEY_APPLET_SYMLINK, item.getSymlinkedTo());
			initialValues.put(KEY_APPLET_OVERWRITE, item.getOverWrite());
			String[] value = { item.getApplet().trim() };
			
			long lng;
			
			try
			{
				lng = db.update(TBL_APPLETS, initialValues, KEY_APPLET_NAME + "= ?", value);
			}
			catch (Exception e)
			{
				RootTools.log("Could not Update " + item.getApplet());
				return false;
			}
			
			if (lng > 0)
			{
				return true;
			}
			else
			{
	
				initialValues.put(KEY_APPLET_NAME, item.getApplet());
				initialValues.put(KEY_APPLET_USAGE, item.getDescription());
				
				if (item.getSymlinkedTo().equals(""))
				{
					if (item.isIshardlink())
					{
						initialValues.put(KEY_APPLET_ISHARDLINK, item.isIshardlink());
						initialValues.put(KEY_APPLET_INODE, item.getInode());
						initialValues.put(KEY_APPLET_BACKUP_HARDLINK, item.getBackupHardlink());
					}
					
					item.setBackupFilePath(item.getAppletPath());
				}
				else
				{
					item.setBackupFilePath(item.getAppletPath());
					item.setBackupSymlink(item.getSymlinkedTo());
					initialValues.put(KEY_APPLET_BACKUP_SYMLINK, item.getBackupSymlink());
				}

				initialValues.put(KEY_APPLET_BACKUP_FILE_PATH, item.getBackupFilePath());

				lng = db.insert(TBL_APPLETS, null, initialValues);
		
				if (lng != -1)		
				{
					return true;
				}
			
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}

		return false;

	}
	
	public List<Item> getApplets()
	{
		try
		{
			open();
			List<Item> list = new ArrayList<Item>();
			
			Cursor cur = db.rawQuery("select * from " + TBL_APPLETS, null);
			
			if (cur != null && cur.getCount() > 0)
			{
				while (cur.moveToNext())
				{
					try
					{
						Item item = new Item();
						item.setApplet(cur.getString(1));
						item.setAppletPath(cur.getString(2));
						item.setDescription(cur.getString(3));
						item.setSymlinkedTo(cur.getString(4));
						item.setOverwrite(cur.getInt(5) == 0 ? false : true);
						item.setFound(cur.getInt(6) == 0 ? false : true);
						item.setRecommend(cur.getInt(7) == 0 ? false : true);
						item.setRemove(cur.getInt(8) == 0 ? false : true);
						item.setIshardlink(cur.getInt(9) == 0 ? false : true);
						item.setInode(cur.getString(10));
						item.setBackupHardlink(cur.getString(11));				
						item.setBackupSymlink(cur.getString(12));
						item.setBackupFilePath(cur.getString(13));
						
						list.add(item);
					}
					catch (Exception ignore) {}
				}
				
				return list;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		
		return null;

	}
//	public boolean createDraft(String title, String json)
//	{
//
		// Delete the existing one!
//		deleteDraft(title);

//		ContentValues initialValues = new ContentValues();
//		initialValues.put(KEY_DRAFT_STORY_TITLE, title);
//		initialValues.put(KEY_DRAFT_STORY_JSON, json);

//		long lng = db.insert(TBL_DRAFT_STORY, null, initialValues);

//		if (lng != -1)
//		{
//			return true;
//		}

//		return false;
//	}

//	public boolean deleteDraft(String title)
//	{
//		Cursor mCursor = db.query(true, TBL_DRAFT_STORY,
//		        new String[] { KEY_DRAFT_STORY_ROWID }, KEY_DRAFT_STORY_TITLE + "='" + title + "'",
//		        null, null, null, null, null);

//		if (mCursor.moveToFirst())	
//		{
//			return db.delete(TBL_DRAFT_STORY,
//					KEY_DRAFT_STORY_ROWID + "=" + mCursor.getInt(0), null) > 0;
//		}
		
//		return false;
//	}

//	public Cursor fetchAllDrafts()
//	{
//
//		return db.query(TBL_DRAFT_STORY, new String[] { KEY_DRAFT_STORY_ROWID,
//		        KEY_DRAFT_STORY_TITLE, KEY_DRAFT_STORY_JSON }, null, null, null, null, null);
//	}

//	public DraftStory fetchDraft(String title) throws SQLException
//	{
//
//		Cursor mCursor = db.query(TBL_DRAFT_STORY, new String[] {
//		        KEY_DRAFT_STORY_ROWID, KEY_DRAFT_STORY_TITLE, KEY_DRAFT_STORY_JSON }, KEY_DRAFT_STORY_TITLE + "='" + title
//		        + "'", null, null, null, null, null);
//
//		if (mCursor.moveToFirst())
//		{
//			return formatDraftStory(mCursor);
//		}
//		return null;
//	}

//	public DraftStory fetchDraft(int row) throws SQLException
//	{
//
//		Cursor mCursor = db.query(TBL_DRAFT_STORY, new String[] {
//		        KEY_DRAFT_STORY_ROWID, KEY_DRAFT_STORY_TITLE, KEY_DRAFT_STORY_JSON }, KEY_DRAFT_STORY_ROWID + "='" + row + "'",
//		        null, null, null, null, null);
//
//		if (mCursor.moveToFirst())
//		{
//			return formatDraftStory(mCursor);
//		}
//		return null;
//	}

}
