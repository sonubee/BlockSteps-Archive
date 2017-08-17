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
        Log.i("--All", "Launched Test Service");
        Intent i = new Intent(this, StepService2.class);
        startService(i);

        scheduleAlarm();

        //launchTestService();
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, MyTestService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        //Log.i("--All", "Interval Alarm Set");
        //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_HOUR, pIntent);
        Log.i("--All", "Minute and Half Alarm Set");
        alarm.setRepeating(AlarmManager.RTC, firstMillis, 1500 * 60, pIntent);
    }
}
