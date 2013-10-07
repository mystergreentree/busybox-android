package stericson.busybox.donate.listeners;

import java.util.ArrayList;
import java.util.List;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.CommandResult;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.activities.BaseActivity;
import stericson.busybox.donate.interfaces.CommandCallback;

import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

public class AppletLongClickListener implements OnLongClickListener, CommandCallback {

    private static final int APPLET_INFO = 3654;

    private BaseActivity activity;
    List<String> result = new ArrayList<String>();

    public AppletLongClickListener(BaseActivity activity) {
        this.activity = activity;
    }

    public boolean onLongClick(View v) {
        CheckBox applet_box = (CheckBox) v.findViewById(R.id.appletCheck);
        String applet = applet_box.getText().toString();

        if (RootTools.isAppletAvailable(applet)) {
            try {

                ShellCommand command = new ShellCommand(this, 0, "busybox " + applet + " --help");
                Shell.startRootShell().add(command);
                command.pause();

                String appletInfo = "";

                for (String info : result) {
                    if (!info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals("")) {
                        appletInfo += info + "\n";
                    }
                }

                activity.initiatePopupWindow(appletInfo, false, activity);
            } catch (Exception e) {
                Toast.makeText(activity, R.string.noInfo, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activity, R.string.noInfo, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void commandCallback(CommandResult result) {

    }

    @Override
    public void commandOutput(int id, String line) {
        if (id == APPLET_INFO) {
            result.add(line);
        }
    }
}
