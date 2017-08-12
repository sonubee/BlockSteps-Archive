package gllc.tech.blocksteps.Sensor;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import gllc.tech.blocksteps.MainActivity;

/**
 * Created by bhangoo on 7/30/2017.
 */

public class StepService extends IntentService implements SensorEventListener, StepListener {

    //private TextView TvSteps;
    //private Button BtnStart, BtnStop;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    public static int numSteps;
    public static boolean isIntentServiceRunning = false;
    public static final String ACTION = "com.codepath.example.servicesdemo.SendStepsService";
    BroadcastReceiver receiver;

    public StepService() {
        super("StepService");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        Log.i("--All", "onCreate StepService");

    }

    public class PhoneUnlockedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                Log.d("--All", "Phone unlocked");
            }else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                Log.d("--All", "Phone locked");
            }
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("--All", "onHandle");
        registerListener();
        numSteps = 0;
    }

    public void registerListener(){
        isIntentServiceRunning = true;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("--All", "On Destroy");
        sensorManager.unregisterListener(this);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);

        receiver = new StepService.PhoneUnlockedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        Toast.makeText(getApplicationContext(), "Step: " + numSteps, Toast.LENGTH_SHORT).show();
        //TvSteps.setText(TEXT_NUM_STEPS + numSteps);

        Intent in = new Intent(ACTION);
        // Put extras into the intent as usual
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue", ""+numSteps);
        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }
}
