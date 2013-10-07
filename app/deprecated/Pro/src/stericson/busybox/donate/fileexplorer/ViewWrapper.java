package stericson.busybox.donate.fileexplorer;

import android.view.View;
import android.widget.CheckBox;

public class ViewWrapper {
    View base;
    CheckBox cb = null;

    ViewWrapper(View base) {
        this.base = base;
    }
}
