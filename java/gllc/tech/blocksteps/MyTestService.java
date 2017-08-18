package gllc.tech.blocksteps;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import gllc.tech.blocksteps.Sensor.StepService2;

/**
 * Created by bhangoo on 8/11/2017.
 */

public class MyTestService extends IntentService {
    // Must create a default constructor
    public MyTestService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        Log.i("--All", "Launched MyTestService");
        Intent i = new Intent(this, StepService2.class);
        startService(i);

        Log.i("--All", "Checking Alarm From MyTestService");
        if (!(SetAlarm.alarmUp(getApplicationContext()))) new SetAlarm(getApplicationContext());

        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }
}
