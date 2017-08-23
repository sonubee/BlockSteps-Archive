package gllc.tech.blocksteps.Auomation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import gllc.tech.blocksteps.Services.MyAlarmReceiver;

/**
 * Created by bhangoo on 8/18/2017.
 */

public class SetAlarm {
    public SetAlarm(Context context) {

        scheduleAlarm(context);
        //dailyAlarm(context);
    }

    public static void scheduleAlarm(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.setAction(MyAlarmReceiver.ACTION_ALARM_RECEIVER);//my custom string action name
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        Log.i("--All", "Hourly Interval Alarm Set");
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_HOUR, pIntent);
        //Log.i("--All", "Minute Alarm Set - SetAlarm");
        //alarm.setRepeating(AlarmManager.RTC, firstMillis, 1000 * 60, pIntent);
    }

    public void dailyAlarm(Context context) {
        Log.i("--All", "Daily Alarm Set - SetAlarm");
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.setAction(MyAlarmReceiver.ACTION_ALARM_RECEIVER);//my custom string action name
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 50);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pIntent);
    }

    public static boolean alarmUp(Context context) {
        Intent intent = new Intent(context, MyAlarmReceiver.class);//the same as up
        intent.setAction(MyAlarmReceiver.ACTION_ALARM_RECEIVER);//the same as up
        boolean isWorking = (PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE) != null);//just changed the flag

        Log.i("--All", "alarm is " + (isWorking ? "" : "not") + " working... from SetAlarm");
        //Toast.makeText(context, "Alarm:" + isWorking, Toast.LENGTH_SHORT).show();
        return isWorking;
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.setAction(MyAlarmReceiver.ACTION_ALARM_RECEIVER);//my custom string action name
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmMgr.cancel(pIntent);

        Log.i("--All", "Alarm Canceled");
    }

    public static void resetAlarm(Context context) {
        cancelAlarm(context);
        scheduleAlarm(context);
    }
}
