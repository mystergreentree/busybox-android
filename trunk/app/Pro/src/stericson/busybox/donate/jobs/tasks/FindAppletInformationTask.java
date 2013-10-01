package stericson.busybox.donate.jobs.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import stericson.busybox.donate.Support.Common;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.jobs.AsyncJob;
import stericson.busybox.donate.jobs.containers.Item;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.services.DBService;
import android.content.Context;
import android.os.Environment;
import com.stericson.RootTools.RootTools;

public class FindAppletInformationTask extends BaseTask {

    private final int SETUP_LS = 1;
    private final int FIND_APPLET_INFORMATION = 2;

    private static DBService dbService = null;

    private Item item = null;
    private String storagePath = "";
    private Context context = null;
    private List<String> result = new ArrayList<String>();


    public JobResult execute(AsyncJob j, boolean updating, String[] applets, boolean backup) {
        context = j.getContext();
        JobResult result = new JobResult();
        result.setSuccess(true);
        List<Item> itemList;

        RootTools.default_Command_Timeout = 3000;

        storagePath = context.getFilesDir().toString();
        dbService = new DBService(context);
        itemList = new ArrayList<Item>();

        try {
            RootTools.getShell(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(context.getString(R.string.shell_error));
            return result;
        }

        setupLS();

        if (!dbService.isReady()) {
            //Backup current Busybox versions.
            for (String path : Common.findBusyBoxLocations(false, false)) {
                if (!new File(storagePath + "/" + RootTools.getInode(path + "busybox")).exists()) {
                    makeBackup("busybox", path, RootTools.getInode(path + "busybox"), backup);
                }
            }
        }

        for (String applet : applets) {
            if (j.isCancelled()) {
                closeDb();
                return null;
            }

            if (updating || !dbService.isReady()) {
                item = dbService.checkForApplet(applet);

                if (item == null) {
                    try {

                        item = new Item();

                        item.setApplet(applet);

                        findApplet(applet, backup);
                    } catch (Exception e) {}
                } else {
                    if (RootTools.exists(item.getAppletPath() + "/" + applet)) {
                        String symlink = RootTools.getSymlink(item.getAppletPath() + "/" + applet);
                        if (!symlink.equals(item.getSymlinkedTo())) {
                            item.setSymlinkedTo(symlink);
                            dbService.insertOrUpdateRow(item);
                        }
                    } else {
                        findApplet(applet, backup);
                    }
                }
            }

            if (!updating)
                j.publishCurrentProgress(String.valueOf(((float) 100 / applets.length)));
        }

        itemList = dbService.getApplets();

        RootTools.default_Command_Timeout = 50000;

        result.setItemList(itemList);
        return result;
    }


    //normal methods
    public static void closeDb() {
        if (dbService != null) {
            try {
                dbService.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Used to extract certain assets we may need. Can be used by any class to
     * extract something. Right now this is tailored only for the initial check
     * sequence but can easily be edited to allow for more customization
     */
    private void extractResources(Context activity, String outputPath) {
        String realFile = "ls.png";

        try {
            InputStream in = activity.getResources().getAssets().open(realFile);
            OutputStream out = new FileOutputStream(
                    outputPath);
            byte[] buf = new byte[2048];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // we have to close these here
            out.flush();
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findApplet(String applet, boolean backup) {
        result = new ArrayList<String>();

        if (RootTools.findBinary(applet)) {

            item.setFound(true);

            List<String> paths = new ArrayList<String>();
            paths.addAll(RootTools.lastFoundBinaryPaths);

            for (String path : RootTools.lastFoundBinaryPaths) {
                if (path.contains("/system/bin")) {
                    item.setAppletPath(path);
                    break;
                }
            }

            if (item.getAppletPath().equals("")) {
                item.setAppletPath(paths.get(0));
            }

            String symlink = RootTools.getSymlink(item.getAppletPath() + "/" + applet);

            item.setSymlinkedTo(symlink);

            if ((!item.getSymlinkedTo().equals(""))) {
                if (item.getSymlinkedTo().trim().toLowerCase().endsWith("busybox")) {
                    item.setInode(RootTools.getInode(symlink));

                    if (!new File(storagePath + "/" + item.getInode()).exists()) {
                        makeBackup("busybox", symlink.replace("/busybox", ""), item.getInode(), backup);
                    }

                    item.setRecommend(true);
                } else {
                    item.setRecommend(false);
                }
            } else if (item.getSymlinkedTo().equals("")) {
                String inode = RootTools.getInode(item.getAppletPath() + "/" + applet);
                for (String path : Common.findBusyBoxLocations(false, false)) {
                    if (inode.equals(RootTools.getInode(path + "busybox"))) {
                        item.setIshardlink(true);
                        item.setBackupHardlink(path + "busybox");
                        item.setInode(inode);

                        if (!new File(storagePath + "/" + inode).exists()) {
                            makeBackup("busybox", path, inode, backup);
                        }
                    }
                }

                item.setBackupFilePath(item.getAppletPath());
                item.setRecommend(false);

                if (!item.isIshardlink()) {
                    if (!RootTools.exists(storagePath + "/" + applet)) {
                        makeBackup(item.getApplet(), item.getAppletPath(), item.getApplet(), backup);
                    }
                }
            } else
                item.setRecommend(true);

            item.setOverwrite(item.getRecommend());

            if (item.getDescription().equals("")) {
                try {
                    command = new ShellCommand(this, FIND_APPLET_INFORMATION, 3000, "busybox " + applet + " --help");
                    RootTools.getShell(true).add(command);
                    command.pause();

                    String appletInfo = "";

                    for (String info : result) {
                        if (!info.contains("not found") && !info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals("")) {
                            appletInfo += info + "\n";
                        }
                    }

                    item.setDescription(appletInfo);
                } catch (Exception e) {}
            }
        } else {
            item.setFound(false);
            item.setSymlinkedTo("");
            item.setRecommend(true);
        }

        dbService.insertOrUpdateRow(item);
    }

    private void makeBackup(String applet, String path, String name, boolean backup) {
        if (backup) {
            InputStream is = null;
            OutputStream os = null;
            byte[] buffer = new byte[2048];
            int bytes_read = 0;

            try {
                is = new FileInputStream(new File(path + "/" + applet));
                os = new FileOutputStream(new File(storagePath + "/" + name));

                while ((bytes_read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytes_read);
                }

            } catch (Exception ignore) {
            } finally {
                try {
                    is.close();
                } catch (Exception ignore) {}

                try {
                    os.close();
                } catch (Exception ignore) {}
            }

            if (new File(storagePath + "/" + applet).exists()) {
                RootTools.log("Backup Created!");
            } else {
                RootTools.log("Backup Creation Failed!");
            }
        }
    }

    private void setupLS() {
        extractResources(context, Environment.getExternalStorageDirectory() + "/stericson-ls");

        try {
            RootTools.fixUtils(new String[]{"dd", "chmod"});
            command = new ShellCommand(this, SETUP_LS, "dd if=" + Environment.getExternalStorageDirectory() + "/stericson-ls of=/data/local/ls", "chmod 0777 /data/local", "chmod 0755 /data/local/ls");
            RootTools.getShell(true).add(command);
            command.pause();

        } catch (Exception ignore) {}
    }

    //callbacks
    @Override
    public void commandOutput(int id, String line) {
        if (id == FIND_APPLET_INFORMATION) {
            result.add(line);
        }
    }
}
