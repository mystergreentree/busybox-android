package stericson.busybox.donate.jobs.tasks;

import android.content.Context;
import com.stericson.RootTools.RootTools;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.CommandResult;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.Item;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.services.DBService;

/**
 * Created by Stephen Erickson on 7/9/13.
 */
public class RestoreBackupTask extends BaseTask {

    private AsyncJob j;
    private List<String> restoredPaths = new ArrayList<String>();
    private String toolbox = "/data/local/toolbox";
    private String storagePath = "";

    public JobResult execute(AsyncJob j) {
        this.j = j;
        Context context = j.getContext();
        storagePath = context.getFilesDir().toString();

        JobResult result = new JobResult();
        String applet = "";
        String path = "";
        String symlink = "";
        String inode = "";
        String hardlink = "";

        result.setSuccess(true);

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        j.publishCurrentProgress("Preparing System...");

        try {
            if (!RootTools.fixUtils(new String[]{"ls", "rm", "ln", "dd", "chmod", "mount"})) {
                result.setError(context.getString(R.string.utilProblem));
                result.setSuccess(false);
                return result;
            }

            DBService db = new DBService(context);

            List<Item> items = db.getApplets();

            for (Item item : items) {
                applet = item.getApplet();
                path = item.getBackupFilePath();
                inode = item.getInode();
                hardlink = item.getBackupHardlink();

                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                symlink = item.getBackupSymlink();

                if (!symlink.equals("")) {
                    if (!applet.equals("") && !path.equals("") && !symlink.equals("")) {
                        j.publishCurrentProgress("Restoring " + item.getApplet() + "...");

                        if (!restoreSymlink(applet, path, symlink, inode)) {
                            result.setSuccess(false);
                        } else {
                            clean(applet, path);
                        }
                    }
                } else if (item.isIshardlink()) {
                    if (new File(storagePath + "/" + inode).exists()) {
                        j.publishCurrentProgress("Restoring " + item.getApplet() + "...");

                        if (!restoreHardlink(applet, path, hardlink, inode)) {
                            result.setSuccess(false);
                        } else {
                            clean(applet, path);
                        }
                    }
                } else if (!path.equals("")) {
                    if (!applet.equals("") && !path.equals("") && new File(storagePath + "/" + applet).exists()) {
                        j.publishCurrentProgress("Restoring " + item.getApplet() + "...");

                        if (!restoreApplet(applet, path, inode)) {
                            result.setSuccess(false);
                        } else {
                            clean(applet, path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            result.setSuccess(false);
        }

        RootTools.remount("/system", "ro");
        return result;
    }

    private void clean(String applet, String path) {
        try {
            j.publishCurrentProgress("Cleaning up...");

            RootTools.findBinary(applet);
            for (String applet_path : RootTools.lastFoundBinaryPaths) {
                if (!path.contains(applet_path)) {
                    String mountType = RootTools.getMountedAs(path);
                    if (RootTools.remount(path, "rw")) {
                        RootTools.log("Cleaning up: " + applet);

                        command = new ShellCommand(this, 0,
                                toolbox + " rm " + applet_path + "/" + applet,
                                "/system/bin/toolbox rm " + applet_path + "/" + applet,
                                "rm " + applet_path + "/" + applet);
                        RootTools.getShell(true).add(command);
                        command.pause();

                    }

                    RootTools.remount(path, mountType);
                } else {
                    RootTools.log("Skipping clean on " + applet + " located at " + applet_path);
                }
            }
        } catch (Exception ignore) {
        }
    }

    private boolean restoreHardlink(String applet, String path, String hardlink, String inode) {
        if (!restoredPaths.contains(hardlink.replace("/busybox", ""))) {
            if (restoreApplet("busybox", hardlink.replace("/busybox", ""), inode)) {
                restoredPaths.add(hardlink.replace("/busybox", ""));
            }
        }

        //restore the applet.
        String mountType = "rw";
        try {
            mountType = RootTools.getMountedAs(path);

            RootTools.remount(path, "rw");

            command = new ShellCommand(this, 0,
                    toolbox + " rm " + path + "/" + applet,
                    "/system/bin/toolbox rm " + path + "/" + applet,
                    "rm " + path + "/" + applet,
                    toolbox + " ln " + hardlink + " " + path + "/" + applet,
                    "/system/bin/toolbox ln " + hardlink + " " + path + "/" + applet,
                    "ln " + hardlink + " " + path + "/" + applet,
                    toolbox + " chmod 0755 " + path + "/" + applet,
                    "/system/bin/toolbox chmod 0755 " + path + "/" + applet,
                    "chmod 0755 " + path + "/" + applet);
            RootTools.getShell(true).add(command);
            command.pause();

            if (!new File(path + "/" + applet).exists()) {
                return false;
            }
        } catch (Exception ingore) {
            return false;
        } finally {
            RootTools.remount(path, mountType);
        }

        if (new File(path + "/" + applet).exists()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean restoreApplet(String applet, String path, String backup_name) {
        String mountType = "rw";
        try {
            mountType = RootTools.getMountedAs(path);

            RootTools.remount(path, "rw");

            command = new ShellCommand(this, 0,
                    toolbox + " rm " + path + "/" + applet,
                    "/system/bin/toolbox rm " + path + "/" + applet,
                    "rm " + path + "/" + applet,
                    "dd if=" + storagePath + "/" + backup_name + " of=" + path + "/" + applet,
                    toolbox + " dd if=" + storagePath + "/" + backup_name + " of=" + path + "/" + applet,
                    "/system/bin/toolbox dd if=" + storagePath + "/" + backup_name + " of=" + path + "/" + applet,
                    toolbox + " chmod 0755 " + path + "/" + applet,
                    "/system/bin/toolbox chmod 0755 " + path + "/" + applet,
                    "chmod 0755 " + path + "/" + applet);
            RootTools.getShell(true).add(command);
            command.pause();

            if (!new File(path + "/" + applet).exists()) {
                command = new ShellCommand(this, 0,
                        "cat " + storagePath + "/" + backup_name + " > " + path + "/" + applet,
                        "cp " + storagePath + "/" + backup_name + " " + path + "/" + applet,
                        toolbox + " cat " + storagePath + "/" + backup_name + " > " + path + "/" + applet,
                        toolbox + " cp " + storagePath + "/" + backup_name + " " + path + "/" + applet,
                        "/system/bin/toolbox cat " + storagePath + "/" + backup_name + " > " + path + "/" + applet,
                        "/system/bin/toolbox cp " + storagePath + "/" + backup_name + " " + path + "/" + applet,
                        toolbox + " chmod 0755 " + path + "/" + applet,
                        "/system/bin/toolbox chmod 0755 " + path + "/" + applet,
                        "chmod 0755 " + path + "/" + applet);
                RootTools.getShell(true).add(command);
                command.pause();

                if (!new File(path + "/" + applet).exists()) {
                    return false;
                }
            }
        } catch (Exception ingore) {
            return false;
        } finally {
            RootTools.remount(path, mountType);
        }

        if (new File(path + "/" + applet).exists()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean restoreSymlink(String applet, String path, String symlink, String inode) {
        if (symlink.contains("/busybox") && !restoredPaths.contains(symlink.replace("/busybox", ""))) {
            if (restoreApplet("busybox", symlink.replace("/busybox", ""), inode)) {
                restoredPaths.add(symlink.replace("/busybox", ""));
            }
        }

        try {
            command = new ShellCommand(this, 0,
                    toolbox + " rm " + path + "/" + applet,
                    "/system/bin/toolbox rm " + path + "/" + applet,
                    "rm " + path + "/" + applet,
                    toolbox + " ln -s " + symlink + " " + path + "/" + applet,
                    "/system/bin/toolbox ln -s " + symlink + " " + path + "/" + applet,
                    "ln -s " + symlink + " " + path + "/" + applet);
            RootTools.getShell(true).add(command);
            command.pause();

        } catch (Exception ignore) {
            return false;
        }

        if (!RootTools.getSymlink(path + "/" + applet).equals(symlink)) {
            return false;
        }

        return true;
    }
}
