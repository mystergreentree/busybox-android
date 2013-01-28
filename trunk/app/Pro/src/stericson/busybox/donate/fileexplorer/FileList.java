package stericson.busybox.donate.fileexplorer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import stericson.busybox.donate.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FileList extends ListActivity {

	public static final String SELECTED_INTENT_KEY = "selected";
	private int[] colors = new int[] { 0xff303030, 0xff404040 };

	private String currentPath = "/";
	private String basePath = "/";
	private TextView currentPathView;
	private ImageButton backButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.explorer_filelist);

		currentPathView = (TextView) findViewById(R.id.explorer_filelist_currentpath);
		backButton = (ImageButton) findViewById(R.id.back_button);

		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});
		registerForContextMenu(getListView());
		loadCurrentPathFiles();
	}

    @Override 
    public void onConfigurationChanged(Configuration newConfig) { 
    super.onConfigurationChanged(newConfig); 
    // We do nothing here. We're only handling this to keep orientation 
    // or keyboard hiding from causing the activity to restart. 
    }
    
	ArrayList<FileEntry> list = new ArrayList<FileEntry>();

	private void loadCurrentPathFiles() {
		currentPathView.setText(currentPath);
		list.clear();

		File current = new File(currentPath);
		String[] filesInPath = current.list(new FilenameFilter() {
			public boolean accept(File dir, String filename) {

//				if (new File(dir, filename).isDirectory())
//					return true;

				return true;
			}
		});
		if (filesInPath == null) {
			// we prevent a crash here if filesInPath returns null
			// because of an inaccessible directory.
		} else {
			for (int i = 0; i < filesInPath.length; i++) {
				// we add the files to our arraylist
				FileEntry fe = new FileEntry(current.getAbsolutePath() + "/"
						+ filesInPath[i]);

				list.add(fe);
			}
		}

		Collections.sort(list);

		setListAdapter(new FileEntryAdapter(this,
				R.layout.explorer_filelist_row, list));
	}

	protected void onActivityResult(int request, int result, Intent intent) {
		loadCurrentPathFiles();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		FileEntry selected = list.get(position);

		if (selected.file.isDirectory()) {
			
			currentPath = selected.file.getAbsolutePath();

			loadCurrentPathFiles();
		} else {
			Intent result = new Intent();
			result.putExtra(SELECTED_INTENT_KEY, selected.filePath);
			setResult(RESULT_OK, result);
			finish();
		}
	}

	private void goBack() {
		if (!(currentPath.equals(basePath) || currentPath.equals("/"))) {
			File current = new File(currentPath);
			currentPath = current.getParent();
			loadCurrentPathFiles();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class FileEntryAdapter extends ArrayAdapter<FileEntry> {

		private ArrayList<FileEntry> items;
		private Context context;
		int index = 0;

		public FileEntryAdapter(Context context, int textViewResourceId,
				ArrayList<FileEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.context = context;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = convertView;
			ViewWrapper wrapper;
			final FileEntry o = items.get(position);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.explorer_filelist_row, null);
				wrapper = new ViewWrapper(v);
				v.setTag(wrapper);
			} else {
				wrapper = (ViewWrapper) v.getTag();
			}
			if (o != null) {
				TextView titulo = (TextView) v.findViewById(R.id.itemtitle);
				ImageView icon = (ImageView) v.findViewById(R.id.itemicon);
				LinearLayout container = (LinearLayout) v.findViewById(R.id.container);

				String value = o.file.getName();

				if (titulo != null)
					titulo.setText(value);

				if (o.file.isDirectory())
					icon.setImageResource(R.drawable.folder);
				else
					icon.setImageResource(R.drawable.file);
				
				if(position % 2 != 0) {
					container.setBackgroundColor(colors[position % 2]);
				}
				else {
					container.setBackgroundColor(colors[position % 2]);
				}
			}
			return (v);
		}

	}
}
