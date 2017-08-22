package gllc.tech.blocksteps.Sensor;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import gllc.tech.blocksteps.SetAlarm;
import io.fabric.sdk.android.Fabric;

/**
 * Created by bhangoo on 7/30/2017.
 */

public class StepService2 extends Service implements SensorEventListener, StepListener {


    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    public static int numSteps;
    public static final String ACTION = "gllc.tech.blocksteps.SendStepsService";
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        FirebaseApp.initializeApp(this);
        Fabric.with(this, new Crashlytics());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();

        Log.i("--All", "StepService2 onCreate = " + sharedPref.getInt("steps",0));

        numSteps = sharedPref.getInt("steps",0);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", 10);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
            Log.i("--All", "StepService2 ServiceHandler");
        }
        @Override
        public void handleMessage(Message msg) {
            Log.i("--All", "StepService2 - Handle Message inside ServiceHandler");
            // Normally we would do some work here, like download a file.
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("--All", "StepService2 onStartCommand");
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        registerSensorListener();

        //to set main ui with steps
        Intent in = new Intent(ACTION);
        // Put extras into the intent as usual
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue", ""+numSteps);
        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);

        SetAlarm.resetAlarm(getApplicationContext());

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    public void registerSensorListener(){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("--All", "StepService2 On Destroy");
        sensorManager.unregisterListener(this);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("--All", "StepService2 onBind");
        return null;
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
        numSteps = sharedPref.getInt("steps",0);
        numSteps++;
        editor.putInt("steps",numSteps).commit();

        Intent in = new Intent(ACTION);
        // Put extras into the intent as usual
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue", ""+ numSteps);
        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);

        if (!(SetAlarm.alarmUp(getApplicationContext()))) new SetAlarm(getApplicationContext());
    }
}

//onCreate
//ServiceHandler
//onStartCommand
//Handle Message