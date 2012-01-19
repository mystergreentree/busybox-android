package stericson.busybox.donate.domain;

import java.util.ArrayList;
import java.util.List;

public class Result
{
	private boolean success = false;
	private String error;
	private List<Item> itemList = new ArrayList<Item>();
	
	public void setItemList(List<Item> itemList)
	{
		this.itemList = itemList;
	}
	
	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public boolean isSuccess()
	{
		return success;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public String getError()
	{
		return error;
	}
	
	public List<Item> getItemList()
	{
		return this.itemList;
	}

}
