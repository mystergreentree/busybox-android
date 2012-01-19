package stericson.busybox.donate.adapter;

import java.util.List;

import stericson.busybox.donate.App;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.TuneActivity;
import stericson.busybox.donate.domain.Item;
import stericson.busybox.donate.listeners.AppletLongClickListener;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TuneAdapter extends BaseAdapter
{

	private TuneActivity activity;
	private static LayoutInflater inflater = null;
	private List<Item> itemList;
	
	public TuneAdapter(TuneActivity activity, List<Item> itemList)
	{
		this.activity = activity;
		this.itemList = itemList;
		
		inflater = (LayoutInflater) activity
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public Object getItem(int position)
	{
		return position;
	}

	public long getItemId(int position)
	{
		return position;
	}

	private class ViewHolder
	{
		private RelativeLayout container;
		private CheckBox appletCheck;
		private CheckBox appletDecision;
		private TextView appletState;
		private TextView appletStatus;
		private TextView appletSymlinkedTo;
		private TextView appletRecomendation;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View vi = convertView;
		final ViewHolder holder;
		final App app = App.getInstance();
		
		if (convertView == null)
		{
			vi = inflater.inflate(R.layout.list_item, null);
			holder = new ViewHolder();
			
			holder.container = (RelativeLayout) vi.findViewById(R.id.container);
			holder.container.setClickable(true);
			holder.container.setOnLongClickListener(new AppletLongClickListener(this.activity));

			holder.appletCheck = (CheckBox) vi.findViewById(R.id.appletCheck);

			holder.appletDecision = (CheckBox) vi.findViewById(R.id.appletDecision);
			
			holder.appletState = (TextView) vi.findViewById(R.id.appletState);

			holder.appletStatus = (TextView) vi.findViewById(R.id.appletStatus);
			
			holder.appletSymlinkedTo = (TextView) vi.findViewById(R.id.appletSymlinkedTo);

			holder.appletRecomendation = (TextView) vi.findViewById(R.id.appletRecommendation);

			vi.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) vi.getTag();
		}
		
		final Item item = itemList.get(position);
		
		holder.appletDecision.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) 
			{
				List<Item> list = app.getItemList();

				item.setRemove(!isChecked);
				
				list.get(position).setRemove(!isChecked);
				app.setItemList(list);
				
				if (isChecked)
				{
					holder.appletDecision.setText(R.string.leaveApplet);							
					holder.appletDecision.setTextColor(Color.GREEN);
				}
				else
				{
					holder.appletDecision.setText(R.string.removeApplet);							
					holder.appletDecision.setTextColor(Color.RED);
				}
			}
		});
		
		holder.appletCheck.setText(item.getApplet());
		holder.appletCheck.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) 
			{
				List<Item> list = app.getItemList();

				item.setOverwrite(isChecked);
				
				list.get(position).setOverwrite(isChecked);
				app.setItemList(list);

				if (isChecked)
				{
					holder.appletState.setText(R.string.willSymlink);
					holder.appletState.setTextColor(Color.GREEN);
					
					item.setRemove(false);
					list.get(position).setRemove(false);
					app.setItemList(list);
					holder.appletDecision.setVisibility(View.GONE);

				}
				else
				{
					holder.appletState.setText(R.string.willNotSymlink);
					holder.appletState.setTextColor(Color.RED);
					
					holder.appletDecision.setVisibility(View.VISIBLE);
					
					if (item.getSymlinkedTo().endsWith("busybox"))
					{
						item.setRemove(true);
						list.get(position).setRemove(true);
						app.setItemList(list);
						holder.appletDecision.setText(R.string.removeApplet);							
						holder.appletDecision.setTextColor(Color.RED);
						holder.appletDecision.setChecked(false);
					}
					else
					{
						item.setRemove(false);
						list.get(position).setRemove(false);
						app.setItemList(list);
						holder.appletDecision.setText(R.string.leaveApplet);							
						holder.appletDecision.setTextColor(Color.GREEN);
						holder.appletDecision.setChecked(true);
					}

				}
			}
			
		});

		holder.appletCheck.setChecked(item.getOverWrite());

		if (holder.appletCheck.isChecked())
		{
			holder.appletState.setText(R.string.willSymlink);
			holder.appletState.setTextColor(Color.GREEN);
			holder.appletDecision.setVisibility(View.GONE);
		}
		else
		{
			holder.appletState.setText(R.string.willNotSymlink);
			holder.appletState.setTextColor(Color.RED);
			holder.appletDecision.setVisibility(View.VISIBLE);
			
			if (item.getRemove())
			{
				holder.appletDecision.setText(R.string.removeApplet);
				holder.appletDecision.setTextColor(Color.RED);
				holder.appletDecision.setChecked(false);
			}
			else
			{
				holder.appletDecision.setText(R.string.leaveApplet);
				holder.appletDecision.setTextColor(Color.GREEN);
				holder.appletDecision.setChecked(true);
			}
		}
		
		if (item.getFound())
		{
			holder.appletStatus.setText(R.string.found);
			if (!item.getSymlinkedTo().equals(""))
			{
				holder.appletSymlinkedTo.setText(activity.getString(R.string.symlinkedTo) + " " + item.getSymlinkedTo());
			}
		}
		else
		{
			holder.appletStatus.setText(R.string.notFound);
		}
		
		if (item.getRecommend())
		{
			holder.appletRecomendation.setText(R.string.cautionDo);
		}
		else
		{
			holder.appletRecomendation.setText(R.string.cautionDoNot);
		}
		
		return vi;
	}
	

	public int getCount() 
	{
		return itemList.size();
	}
}
