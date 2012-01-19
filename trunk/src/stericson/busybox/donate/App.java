package stericson.busybox.donate;

import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.domain.Item;
import android.view.View;

public class App
{

	private static App instance = null;
	private List<Item> itemList;
	private View popupView;

	private App()
	{
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
	
	public static App getInstance()
	{
		if (instance == null)
			instance = new App();
		return instance;
	}


}
