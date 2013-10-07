package stericson.busybox.donate.jobs.tasks;

import stericson.busybox.donate.Support.CommandResult;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.interfaces.CommandCallback;

/**
 * Created by Stephen Erickson on 7/10/13.
 */
public class BaseTask implements CommandCallback {

    protected ShellCommand command = null;

    @Override
    public void commandCallback(CommandResult result) {

    }

    @Override
    public void commandOutput(int id, String line) {

    }
}
