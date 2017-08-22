package gllc.tech.blocksteps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by bhangoo on 7/28/2017.
 */

public class MyAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION_ALARM_RECEIVER = "gllc.tech.blocksteps.MyAlarmReceiver";
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    //SharedPreferences sharedPref;
    //SharedPreferences.Editor editor;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("--All", "Alarm Triggered");
        /*
        FirebaseApp.initializeApp(context);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPref.edit();

        DatabaseReference myRef = database.getReference(sharedPref.getString("uniqueId","NA"));
        myRef.child("Alarm").push().setValue(SendStepsService.getHourlyTimeStamp());
        //Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show();
        */
        Intent i = new Intent(context, SendStepsService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
