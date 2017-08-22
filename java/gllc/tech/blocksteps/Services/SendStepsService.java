package gllc.tech.blocksteps.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import gllc.tech.blocksteps.Auomation.DateFormatter;
import gllc.tech.blocksteps.BuildConfig;
import gllc.tech.blocksteps.MainActivity;
import gllc.tech.blocksteps.Objects.SentSteps;
import gllc.tech.blocksteps.Auomation.SetAlarm;
import io.fabric.sdk.android.Fabric;

/**
 * Created by bhangoo on 7/28/2017.
 */

public class SendStepsService extends IntentService {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Must create a default constructor
    public SendStepsService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        FirebaseApp.initializeApp(this);
        Fabric.with(this, new Crashlytics());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseReference myRef2 = database.getReference(sharedPref.getString("uniqueId","NA"));
        myRef2.child("Alarm-SendStepsService").push().setValue(DateFormatter.getHourlyTimeStamp() + " - Version " + BuildConfig.VERSION_NAME);

        int lastDate = sharedPref.getInt("lastDate",0);
        int currentDate = Integer.parseInt(DateFormatter.GetConCatDate(0));

        int steps = sharedPref.getInt("steps",0);
        int lastSteps = sharedPref.getInt("lastSteps",0);

        if (steps != lastSteps) {

            try {
                sendSteps(steps, lastDate);
            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "Error In SendStepsService onHandleIntent: " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "Error In SendStepsService onHandleIntent: " + e.getMessage());
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "NullPointerException In SendStepsService onHandleIntent: " + e.getMessage());
            }catch (Exception e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "General Exception In SendStepsService onHandleIntent: " + e.getMessage());
            }
        }

        if (currentDate > lastDate) {
            Log.i("--All", "Resetting");
            StepService.numSteps =0;
            editor.putInt("steps", 0).commit();
            editor.putInt("lastDate",currentDate).commit();

            //Redundant?
            SetAlarm.resetAlarm(getApplicationContext());
        }
    }

    public void sendSteps(int steps, int date) throws ExecutionException, InterruptedException {
        Log.i("--All", "Invoking Method Save Steps");
        Future<TransactionReceipt> transactionReceiptFuture = MainActivity.contract.saveMySteps(new Uint256(steps),new Utf8String(Integer.toString(date)));
        Log.i("--All", "Hash: "+transactionReceiptFuture.get().getTransactionHash());

        editor.putInt("lastSteps",steps).commit();

        Log.i("--All", "Sending to Firebase");
        SentSteps sentSteps = new SentSteps(DateFormatter.getHourlyTimeStamp(), steps, sharedPref.getString("uniqueId","NA"));
        DatabaseReference myRef = database.getReference(sharedPref.getString("uniqueId","NA"));
        myRef.child("SentSteps").push().setValue(sentSteps);
    }
}

