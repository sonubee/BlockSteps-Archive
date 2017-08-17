package gllc.tech.blocksteps.Sensor;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import gllc.tech.blocksteps.MyTestService;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

/**
 * Created by bhangoo on 8/11/2017.
 */

public class BootBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
        Log.i("--All", "Boot Wakeful");
        Toast.makeText(context, "Boot Wakeful", Toast.LENGTH_LONG).show();
        Intent startServiceIntent = new Intent(context, MyTestService.class);
        startWakefulService(context, startServiceIntent);

    }
}