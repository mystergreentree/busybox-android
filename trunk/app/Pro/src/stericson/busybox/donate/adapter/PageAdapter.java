package stericson.busybox.donate.adapter;

import android.view.ViewGroup;
import android.widget.Button;
import stericson.busybox.donate.R;
import stericson.busybox.donate.activities.MainActivity;
import stericson.busybox.donate.custom.FontableTextView;
import stericson.busybox.donate.listeners.AppletInstallerLongClickListener;
import stericson.busybox.donate.services.PreferenceService;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;

public class PageAdapter extends PagerAdapter {

    private static String[] titles = new String[]
            {
                    "Applet Manager",
                    "Install Busybox",
                    "About BusyBox",
                    "Settings"
            };

    private final MainActivity context;

    public PageAdapter(MainActivity context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object instantiateItem(final ViewGroup pager, final int position) {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (position) {
            case 0:
                view = inflater.inflate(R.layout.generic_list, null);
                pager.addView(view);
                context.setListView((ListView) view.findViewById(R.id.list));
                ListView listView = context.getListView();

                listView.setAdapter(new AppletAdapter(context));
                listView.setOnItemLongClickListener(new AppletInstallerLongClickListener(context));
                listView.setCacheColorHint(0);
                break;

            case 1:
                view = inflater.inflate(R.layout.generic_list, null);
                pager.addView(view);
                context.setListView((ListView) view.findViewById(R.id.list));
                ListView listView2 = context.getListView();

                listView2.setAdapter(new TuneAdapter(context));
                listView2.setCacheColorHint(0);
                break;

            case 2:
                view = new ScrollView(context);
                FontableTextView tv = new FontableTextView(context);
                ((ScrollView) view).addView(tv);

                pager.addView(view);

                tv.setText(context.getString(R.string.about));
                Linkify.addLinks(tv, Linkify.ALL);

                break;

            case 3:
                view = inflater.inflate(R.layout.settings, null);

                CheckBox clearSbin = (CheckBox) view.findViewById(R.id.removeSbin);
                clearSbin.setChecked(new PreferenceService(context).getClearSbin());
                clearSbin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        PreferenceService p = new PreferenceService(context);
                        p.setClearSbin(isChecked);
                        p.commit();
                    }
                });

                Button resetBackup = (Button) view.findViewById(R.id.clearBackupChoice);
                resetBackup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.Reset();
                    }
                });

                Button clearbackups = (Button) view.findViewById(R.id.clearBackup);
                clearbackups.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.ClearBackups();
                    }
                });

                Button cleardb = (Button) view.findViewById(R.id.clearDatabase);
                cleardb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.ClearDatabase();
                    }
                });
                pager.addView(view);

                break;
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup pager, int position, Object view) {
        pager.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    public String getPageTitle(int position) {
        return titles[position];
    }
}
