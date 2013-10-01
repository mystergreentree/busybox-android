package stericson.busybox.donate.activities;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import stericson.busybox.donate.App;
import stericson.busybox.donate.Support.Common;
import stericson.busybox.donate.Constants;
import stericson.busybox.donate.R;
import stericson.busybox.donate.Support.ShellCommand;
import stericson.busybox.donate.adapter.PageAdapter;
import stericson.busybox.donate.custom.DropDownAnim;
import stericson.busybox.donate.custom.FontableTextView;
import stericson.busybox.donate.interfaces.ChoiceCallback;
import stericson.busybox.donate.interfaces.JobCallback;
import stericson.busybox.donate.jobs.FindAppletInformationJob;
import stericson.busybox.donate.jobs.FindAvailableAppletsJob;
import stericson.busybox.donate.jobs.InitialChecksJob;
import stericson.busybox.donate.jobs.InstallAppletJob;
import stericson.busybox.donate.jobs.UnInstallAppletJob;
import stericson.busybox.donate.jobs.containers.Item;
import stericson.busybox.donate.fileexplorer.FileList;
import stericson.busybox.donate.jobs.InstallJob;
import stericson.busybox.donate.jobs.containers.JobResult;
import stericson.busybox.donate.jobs.PrepareBinaryJob;
import stericson.busybox.donate.jobs.RestoreBackupJob;
import stericson.busybox.donate.jobs.UninstallJob;
import stericson.busybox.donate.listeners.PageChange;
import stericson.busybox.donate.services.AppletService;
import stericson.busybox.donate.services.DBService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.viewpagerindicator.TitlePageIndicator;
import stericson.busybox.donate.services.PreferenceService;

public class MainActivity extends BaseActivity implements JobCallback, ChoiceCallback {

    private TextView header;
    private ViewPager pager;
    private PageAdapter adapter;
    private TitlePageIndicator indicator;

    private Button install;
    private Button uninstall;
    private Button restore;

    //maintains current page position
    public int position;

    private String custom = "";
    private boolean clean = false;
    private ListView listView;

    private RelativeLayout pb_container;
    private ProgressBar pb;
    private FontableTextView pb_msg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        App.getInstance().setItemList(null);

        randomAnimation();

        stopGatherInformation();

        install = (Button) findViewById(R.id.install);
        uninstall = (Button) findViewById(R.id.uninstall);
        restore = (Button) findViewById(R.id.restore);
        pb_container = (RelativeLayout) findViewById(R.id.progression);
        pb = (ProgressBar) findViewById(R.id.progression_bar);
        pb_msg = (FontableTextView) findViewById(R.id.progression_msg);

        header = (TextView) findViewById(R.id.header_main);
        header.setTypeface(tf);

        new InitialChecksJob(this, this).execute();

        final CheckBox autoupdate = (CheckBox) findViewById(R.id.autoupdate);
        final SharedPreferences sp = getSharedPreferences("BusyBox", 0);
        autoupdate.setChecked(sp.getBoolean("auto-update", false));

        autoupdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                sp.edit().putBoolean("auto-update", isChecked).commit();

                if (isChecked)
                    MainActivity.this.initiatePopupWindow("This feature will automatically update Busybox to the latest version when a new version is available and when updates are done to the newest version. \n\n To best take advantage of this feature, allow Busybox to automatically update from the market as the update is delivered from the Android market. \n\n No interaction is required on your part to keep Busybox updated when using this feature..Enjoy! ", false, MainActivity.this);
            }

        });
    }

    protected void onResume() {
        if (App.getInstance().isChoose()) {
            //The user has returned and we should show the file browser?
            App.getInstance().setChoose(false);
            Intent i = new Intent(this, FileList.class);
            startActivityForResult(i, Constants.CHOOSE);
        }

        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CHOOSE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String file_chosen = extras.getString("selected");
                new PrepareBinaryJob(this, this, file_chosen).execute();
            } else {
                App.getInstance().updateVersion(0);
                updateList();
            }
        }
    }

    public void ClearBackups() {
        try {
            CommandCapture command = new CommandCapture(0, "rm " + getFilesDir().toString() + "/*");
            RootTools.getShell(true).add(command);

            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
        } catch (Exception ignore) {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT).show();
        }
    }

    public void ClearDatabase() {
        try {
            new PreferenceService(this).setDeleteDatabase(true).commit();
            Toast.makeText(this, R.string.success_after, Toast.LENGTH_LONG).show();
        } catch (Exception ignore) {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void choiceCancelled(int id) {
        switch (id) {
            case Constants.CUSTOM_VERSION:
                App.getInstance().updateVersion(0);
                updateList();
                break;
        }
    }

    @Override
    public void choiceMade(boolean choice, int id) {
        switch (id) {
            case Constants.UNINSTALL:
                if (choice) {
                    this.makeChoice(this, Constants.UNINSTALL_CHOICE, R.string.uninstall_type, R.string.uninstall_choice, R.string.smart_uninstall, R.string.normal_uninstall);
                }
                break;

            case Constants.INSTALL:
                App.getInstance().setSmartInstall(!choice);
                stopGatherInformation();

                if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null) {
                    install.setEnabled(false);
                    new InstallJob(this, this, App.getInstance().getPath(), clean).execute();
                } else {
                    initiatePopupWindow("An unexpected error has occured, please take a screenshot of the application and send it to me at StericDroid@gmail.com", false, this);
                }
                break;

            case Constants.NORMAL_INSTALL:
                if (choice) {
                    App.getInstance().setSmartInstall(false);
                    stopGatherInformation();

                    if (App.getInstance().getVersion() != null && App.getInstance().getPath() != null) {
                        install.setEnabled(false);
                        new InstallJob(this, this, App.getInstance().getPath(), clean).execute();
                    } else {
                        initiatePopupWindow("An unexpected error has occured, please take a screenshot of the application and send it to me at StericDroid@gmail.com", false, this);
                    }
                }
                break;

            case Constants.RESTORE:
                if (choice) {
                    stopGatherInformation();
                    new RestoreBackupJob(this, this).execute();
                }
                break;

            case Constants.CUSTOM_VERSION:
                if (choice) {
                    App.getInstance().setChoose(false);
                    Intent i = new Intent(this, FileList.class);
                    startActivityForResult(i, Constants.CHOOSE);
                } else {
                    App.getInstance().setChoose(true);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://docs.google.com/folder/d/0B5Amguus3csDakJ5SXhaX045R0U/edit"));
                    startActivity(i);
                }
                break;
            case Constants.UNINSTALL_CHOICE:
                uninstall.setEnabled(false);

                stopGatherInformation();

                new UninstallJob(this, this, choice).execute();
                break;

            case Constants.BACKUP:

                showProgress();
                App.getInstance().setProgress(0);

                new PreferenceService(this).setMakeBackup(choice).commit();
                new FindAppletInformationJob(this, this, false, choice).execute();
                break;
            case Constants.HANDLESBIN:
                PreferenceService p = new PreferenceService(this);
                if (choice) {
                    p.setClearSbin(true);
                }

                p.askSbin(false);

                startAppletInformationGathering();
                break;

        }
    }

    public void gatherInformation(String[] applets) {
        Intent intent = new Intent(this, AppletService.class);
        intent.putExtra("applets", applets);

        //Try to stop an existing service..
        stopService(intent);

        //Try to start a new one.
        startService(intent);
    }

    public void hideProgress() {
        DropDownAnim anim = new DropDownAnim(pb_container, Common.getDIP(this, 20), false);
        anim.setDuration(500);
        pb_container.startAnimation(anim);

        //pb_container.setVisibility(View.GONE);
    }

    public void initiatePager() {
        if (pager == null) {
            pager = (ViewPager) findViewById(R.id.viewpager);
            indicator = (TitlePageIndicator) findViewById(R.id.indicator);

            adapter = new PageAdapter(MainActivity.this);
            indicator.setOnPageChangeListener(new PageChange(this));
            pager.setAdapter(adapter);
            indicator.setViewPager(pager);
            pager.setCurrentItem(1);
            this.position = 1;
        }
    }

    public void install(View v) {
        if (RootTools.hasEnoughSpaceOnSdCard(1200)) {
            if (App.getInstance().getItemList() == null) {
                stopGatherInformation();

                this.makeChoice(this, Constants.NORMAL_INSTALL, R.string.install_type, R.string.install_type_custom, R.string.normal_install, R.string.cancel);
            } else {
                this.makeChoice(this, Constants.INSTALL, R.string.install_type, R.string.install_type_content, R.string.normal_install, R.string.smart_install);
            }
        } else {
            initiatePopupWindow(this.getString(R.string.sdcard), true, this);
        }
    }

    public void installDone() {
        String currentVersion = App.getInstance().getCurrentVersion();

        gatherInformation(Constants.appletsString);

        install.setEnabled(true);

        if (pager != null) {
            pager.setCurrentItem(2);
        }

        RootTools.remount("/system", "ro");

        boolean result = RootTools.checkUtil("busybox");

        if (result) {
            uninstall.setEnabled(true);

            String thisVersion = RootTools.getBusyBoxVersion();

            if (thisVersion == null) {
                thisVersion = "";
            }

            if (currentVersion == null) {
                currentVersion = "-1";
            }

            if (thisVersion.contains(App.getInstance().getVersion().toLowerCase().replace("busybox", "").trim())) {
                if (currentVersion.equals(thisVersion)) {
                    initiatePopupWindow(this.getString(R.string.installedsame), false, this);
                } else {
                    initiatePopupWindow(this.getString(R.string.installedunique), false, this);
                }
            } else {
                if (currentVersion.equals(thisVersion)) {
                    initiatePopupWindow(this.getString(R.string.installedsame), false, this);
                } else {
                    initiatePopupWindow(this.getString(R.string.installedsomethingelse), false, this);
                }
            }
        } else {
            initiatePopupWindow(this.getString(R.string.failed), true, this);
        }
    }

    public void installAppletDone(int position) {

        List<Item> items = App.getInstance().getItemList();
        Item item = items.get(position);

        String applet = item.getApplet();

        gatherInformation(new String[]{applet});

        item.setAppletPath("/system/xbin");
        item.setFound(true);
        item.setOverwrite(false);
        item.setRecommend(false);
        item.setRemove(false);
        item.setSymlinkedTo("");

        try {
            final List<String> result1 = new ArrayList<String>();

            ShellCommand command = new ShellCommand(null, 0, "busybox " + applet + " --help") {

                @Override
                public void commandOutput(int i, String s) {
                    result1.add(s);
                }

                @Override
                public void commandTerminated(int i, String s) {

                }

                @Override
                public void commandCompleted(int i, int i2) {

                }

            };
            Shell.startRootShell().add(command);
            command.pause();

            String appletInfo = "";

            for (String info : result1) {
                if (!info.equals("1") && !info.contains("multi-call binary") && !info.trim().equals("")) {
                    appletInfo += info + "\n";
                }
            }

            item.setDescription(appletInfo);
        } catch (Exception e) {
            item.setDescription(this.getString(R.string.noInfo));
        }

        updateList();

        initiatePopupWindow("Congratulations! The applet was installed successfully!", false, this);
    }


    public void jobFinished(JobResult result, int id) {

        switch (id) {
            case InitialChecksJob.Checks:
                if (result.getError() != null && !result.getError().equals(""))
                    this.initiatePopupWindow(result.getError(), true, this);
                else {

                    PreferenceService p = new PreferenceService(this);

                    initiatePager();

                    if (p.getAskSbin() && App.getInstance().isInstalledSbin()) {
                        makeChoice(this, Constants.HANDLESBIN, R.string.handleSbin_title, R.string.handleSbin, R.string.yes, R.string.no);
                    }
                    else {
                        startAppletInformationGathering();
                    }
                }
                break;

            case FindAppletInformationJob.APPLET_INFO:
                hideProgress();

                App.getInstance().setItemList(result.getItemList());
                updateList();

                gatherInformation(Constants.appletsString);

                //if we made a backup?
                if (new PreferenceService(this).getMakeBackup()) {
                    restore.setEnabled(true);
                }

                if (App.getInstance().isInstalled()) {
                    uninstall.setEnabled(true);
                }
                break;

            case PrepareBinaryJob.PREPARE_BINARY_JOB:
                if (result.getError() != null && !result.getError().equals("")) {

                    App.getInstance().updateVersion(0);
                    App.getInstance().setInstallCustom(false);
                    updateList();
                    this.initiatePopupWindow(result.getError(), false, this);
                } else {
                    new FindAvailableAppletsJob(this, this).execute();
                    Toast.makeText(this, "BusyBox version " + App.getInstance().getVersion() + " will be installed", Toast.LENGTH_LONG).show();
                }
                break;

            case FindAvailableAppletsJob.AVAIL_APPLETS:
                updateList();
                break;

            case InstallAppletJob.INSTALL_APPLET_JOB:
                if (result.isSuccess()) {
                    installAppletDone(Integer.valueOf((String) result.getData()));
                } else {
                    initiatePopupWindow("Uh Oh! The applet was not installed successfully!", false, this);
                }
                break;

            case InstallJob.INSTALL_JOB:
                if (result.isSuccess()) {
                    installDone();
                } else {
                    initiatePopupWindow(result.getError(), true, this);
                }
                break;

            case RestoreBackupJob.RESTORE_BACKUP_JOB:
                if (result.isSuccess()) {
                    this.initiatePopupWindow(getString(R.string.restoreSuccess), false, this);
                } else {
                    this.initiatePopupWindow(getString(R.string.restoreErrors), false, this);
                }
                break;

            case UnInstallAppletJob.UNINSTALL_APPLET_JOB:
                if (result.isSuccess()) {
                    uninstallAppletDone(Integer.valueOf((String) result.getData()));
                } else {
                    initiatePopupWindow("Uh Oh! The applet was not uninstalled successfully!", false, this);
                }
                break;

            case UninstallJob.UNINSTALL_JOB:
                if (result.isSuccess()) {
                    uninstallDone();
                } else {
                    initiatePopupWindow(result.getError(), false, this);
                }
                break;
        }
    }

    @Override
    public void jobProgress(Object value, int id) {

        View popView = App.getInstance().getPopupView();

        TextView header = popView != null ? (TextView) popView.findViewById(R.id.header) : null;

        switch (id) {
            case FindAppletInformationJob.APPLET_INFO:
                updateProgress(Float.valueOf((String)value));
                break;

            case InstallAppletJob.INSTALL_APPLET_JOB:
            case InstallJob.INSTALL_JOB:
                if (header != null) {
                    header.setText(this.getString(R.string.installing) + " " + value);
                }
                break;

            case RestoreBackupJob.RESTORE_BACKUP_JOB:
                if (header != null) {
                    header.setText(this.getString(R.string.restoring) + " " + value);
                }
                break;

            case UnInstallAppletJob.UNINSTALL_APPLET_JOB:
            case UninstallJob.UNINSTALL_JOB:
                if (header != null) {
                    header.setText(this.getString(R.string.uninstalling) + " " + value);
                }
                break;
        }
    }

    public void restore(View v) {
        this.makeChoice(this, Constants.RESTORE, R.string.restore, R.string.beforeRestore, R.string.restore, R.string.cancel);
    }

    public void showProgress() {
        pb_container.setVisibility(View.VISIBLE);
        DropDownAnim anim = new DropDownAnim(pb_container, Common.getDIP(this, 20), true);
        anim.setDuration(500);
        pb_container.startAnimation(anim);

        pb_msg.setText(pb.getProgress() + this.getString(R.string.loaded));
    }

    public void startAppletInformationGathering() {
        PreferenceService p = new PreferenceService(this);

        if (!new DBService(this).isReady())
            initiatePopupWindow("Hello, welcome to BusyBox Pro. \n\n It looks like the initial setup has not be completed yet. The initial setup will take a bit longer than BusyBox free the first time it is run because we are making a backup of your system as well as doing some additional checks on your system. \n\n After this initial setup is complete, startup times should be very fast. \n\n Thanks for your patience.", false, this);

        if (p.hasPreference("makebackup")) {
            showProgress();
            App.getInstance().setProgress(0);

            new FindAppletInformationJob(this, this, false, p.getMakeBackup()).execute();
        } else {
            makeChoice(this, Constants.BACKUP, R.string.makeBackup_title, R.string.makeBackup, R.string.yes, R.string.no);
        }
    }

    public void stopGatherInformation() {
        Intent intent = new Intent(this, AppletService.class);

        //Try to stop an existing service..
        stopService(intent);

    }

    public void toggle_smart(View v) {
        try {
            App.getInstance().setToggled(!App.getInstance().isToggled());
            updateList();

            if (App.getInstance().isToggled())
                this.initiatePopupWindow(this.getString(R.string.before_tweak), false, this);

            ImageButton toggle = (ImageButton) pager.findViewById(R.id.toggle_smart);
            toggle.setImageDrawable(getResources().getDrawable(App.getInstance().isToggled() ? R.drawable.arrow_up_float : R.drawable.arrow_down_float));
        } catch (Exception ignore) {}
    }

    public void uninstallAppletDone(int position) {
        Item item = App.getInstance().getItemList().get(position);

        String applet = item.getApplet();

        gatherInformation(new String[]{applet});

        item.setAppletPath("");
        item.setFound(false);
        item.setOverwrite(false);
        item.setRecommend(true);
        item.setRemove(false);
        item.setDescription("");
        item.setSymlinkedTo("");
        updateList();

        initiatePopupWindow("Congratulations! The applet was uninstalled successfully!", false, this);
    }

    public void uninstall(View v) {
        this.makeChoice(this, Constants.UNINSTALL, R.string.careful, R.string.beforeUninstall, R.string.uninstall, R.string.cancel);
    }

    public void uninstallDone() {
        gatherInformation(Constants.appletsString);

        if (pager != null)
            pager.setCurrentItem(2);

        RootTools.remount("/system", "ro");

        if (!App.getInstance().isInstalled()) {
            initiatePopupWindow(this.getString(R.string.uninstallsuccess), false, this);
        } else {
            initiatePopupWindow(this.getString(R.string.uninstallfailed), false, this);
        }
    }

    public void updateList() {
        try {
            App.getInstance().getAppletadapter().update();
        } catch (Exception ignore) {
        }

        try {
            App.getInstance().getTuneadapter().update();
        } catch (Exception ignore) {
        }
    }

    public void updateProgress(Float progress) {
        App.getInstance().setProgress(App.getInstance().getProgress() + progress);
        pb.setProgress((int) Math.floor((double) App.getInstance().getProgress()));

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);

        pb_msg.setText(nf.format(App.getInstance().getProgress()) + this.getString(R.string.loaded));
    }

    //Getters and Setters
    public boolean getClean() {
        return this.clean;
    }

    public String getCustomPath() {
        return this.custom;
    }

    public void setCustomPath(String path) {
        this.custom = path;
    }

    public void Reset() {
        try {
            new PreferenceService(this).removePreference("makebackup").setDeleteDatabase(true).commit();

            CommandCapture command = new CommandCapture(0, "rm " + getFilesDir().toString() + "/*");
            RootTools.getShell(true).add(command);

            Toast.makeText(this, R.string.success_after, Toast.LENGTH_SHORT).show();
        } catch (Exception ignore) {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT).show();
        }
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }
}
