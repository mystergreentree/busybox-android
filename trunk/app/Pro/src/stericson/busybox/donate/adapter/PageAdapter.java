package stericson.busybox.donate.adapter;

import stericson.busybox.donate.R;
import stericson.busybox.donate.Activity.MainActivity;
import stericson.busybox.donate.custom.FontableTextView;
import stericson.busybox.donate.listeners.AppletInstallerLongClickListener;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

import com.viewpagerindicator.TitleProvider;
 
public class PageAdapter extends PagerAdapter implements TitleProvider
{
	
    private static String[] titles = new String[]
    {
    	"Applet Manager",
        "Install Busybox",
        "About BusyBox"
    };
    
    private final MainActivity context;
 
    public PageAdapter(MainActivity context)
    {
        this.context = context;  
    }
  
    @Override
    public int getCount()
    {
        return titles.length;
    }
 
    @Override
    public Object instantiateItem(final View pager, final int position)
    {
    	View view = null;
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 	    
		
	    switch (position)
	    {
	    	case 0:
			    view = inflater.inflate(R.layout.generic_list, null);
			    ((ViewPager) pager).addView(view);
			    context.setListView((ListView) view.findViewById(R.id.list));
				ListView listView = context.getListView();
	
			    listView.setAdapter(new AppletAdapter(context));
			    listView.setOnItemLongClickListener(new AppletInstallerLongClickListener(context));
			    listView.setCacheColorHint(0);
    		break;
	    	
	    	case 1:
			    view = inflater.inflate(R.layout.generic_list, null);
			    ((ViewPager) pager).addView(view);
			    context.setListView((ListView) view.findViewById(R.id.list));
				ListView listView2 = context.getListView();
	
			    listView2.setAdapter(new TuneAdapter(context));	
			    listView2.setCacheColorHint(0);
    		break;
	    		
	    	case 2:
			    view = new ScrollView(context);
			    FontableTextView tv = new FontableTextView(context);
			    ((ScrollView) view).addView(tv);
			    ((ViewPager) pager).addView(view);
			    
			    tv.setText(context.getString(R.string.about));
			    Linkify.addLinks(tv, Linkify.ALL);
	    		
    		break;
	    }
	    
	    return view;
    }
    
    @Override
    public void destroyItem( View pager, int position, Object view )
    {
        ((ViewPager) pager).removeView((View) view);
    }
 
    @Override
    public boolean isViewFromObject( View view, Object object )
    {
        return view.equals( object );
    }
 
    @Override
    public void finishUpdate( View view ) {}
 
    @Override
    public void restoreState( Parcelable p, ClassLoader c ) {}
 
    @Override
    public Parcelable saveState() {
        return null;
    }
 
    @Override
    public void startUpdate( View view ) {}

	public String getTitle(int position) {
		return titles[ position ];
	}
}
