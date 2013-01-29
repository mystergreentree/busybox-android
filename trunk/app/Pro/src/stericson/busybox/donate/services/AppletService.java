package stericson.busybox.donate.services;

import stericson.busybox.donate.jobs.AppletInformation;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public class AppletService extends Service
{  
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private AppletInformation appletInformation;

	// Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {

	    	  Bundle data = msg.getData();
	    	  
	    	  appletInformation = new AppletInformation();
	    	  appletInformation.getAppletInformation(AppletService.this, true, null, data.getStringArray("applets"), new PreferenceService(AppletService.this).getMakeBackup());
	    	  
	    	  // Stop the service using the startId, so that we don't stop
	          // the service in the middle of handling another job
	          stopSelf(msg.arg1);
	      }
	  }

	  @Override
	  public void onCreate() {
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
	    HandlerThread thread = new HandlerThread("ServiceStartArguments",
	            android.os.Process.THREAD_PRIORITY_LOWEST);
	    thread.start();
	    
	    // Get the HandlerThread's Looper and use it for our Handler 
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {

	      // For each start request, send a message to start a job and deliver the
	      // start ID so we know which request we're stopping when we finish the job
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg1 = startId;
	      Bundle data = new Bundle();
	      data.putStringArray("applets", intent.getStringArrayExtra("applets"));
	      msg.setData(data);
	      
	      mServiceHandler.sendMessage(msg);
	      
	      // If we get killed, after returning from here, restart
	      return START_NOT_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	  }
	  
	  @Override
	  public void onDestroy() {
		  try
		  {
			  appletInformation.closeDb();
		  }
		  catch (Exception e) {}
	  }
}
