package gllc.tech.blocksteps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by bhangoo on 7/28/2017.
 */

public class MyAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION_ALARM_RECEIVER = "gllc.tech.blocksteps.MyAlarmReceiver";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("--All", "Alarm Triggered");
        //Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show();
        Intent i = new Intent(context, SendStepsService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
