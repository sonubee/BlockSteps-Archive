package gllc.tech.blocksteps.Sensor;

import android.app.IntentService;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;
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

    public static final int SCREEN_OFF_RECEIVER_DELAY = 500;
    private PowerManager.WakeLock mWakeLock = null;

    public StepService() {
        super("StepService");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE5");
/*
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        PowerManager manager =
                (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "--All");
*/
        //registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    private void registerListener() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        numSteps = 0;
    }

    private void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        unregisterListener();
        mWakeLock.release();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE6");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE4");
        startForeground(Process.myPid(), new Notification());
        registerListener();
        mWakeLock.acquire();

        return START_STICKY;
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("--All", "onReceive("+intent+")");
            Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE2");

            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE1");
                    Log.i("--All", "Runnable executing.");
                    unregisterListener();
                    registerListener();
                }
            };

            new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE7");

        isIntentServiceRunning = true;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        numSteps = 0;

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
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
    }
}
