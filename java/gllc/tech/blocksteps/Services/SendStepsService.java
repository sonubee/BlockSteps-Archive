package gllc.tech.blocksteps.Services;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import gllc.tech.blocksteps.Auomation.DateFormatter;
import gllc.tech.blocksteps.Auomation._Users_Admin_Desktop_Steps_sol_Steps;
import gllc.tech.blocksteps.BuildConfig;
import gllc.tech.blocksteps.MainActivity;
import gllc.tech.blocksteps.MyApplication;
import gllc.tech.blocksteps.Objects.SentSteps;
import gllc.tech.blocksteps.Auomation.SetAlarm;
import io.fabric.sdk.android.Fabric;

import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

/**
 * Created by bhangoo on 7/28/2017.
 */

public class SendStepsService extends IntentService {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Credentials credentials = null;
    Web3j web3;
    public static _Users_Admin_Desktop_Steps_sol_Steps contract;

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

        try {
            web3 = Web3jFactory.build(new HttpService("http://45.55.4.74:8545"));  // defaults to http://localhost:8545/
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("--All", "Exception Building WebFactory " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseReference myRef2 = database.getReference(sharedPref.getString("uniqueId","NA"));

        myRef2.child("Alarm-SendStepsService").push().setValue(DateFormatter.getHourlyTimeStamp() + " - Version " + BuildConfig.VERSION_NAME + " - Steps: " + sharedPref.getInt("steps",0));

        loadCredAndContract();
    }

    public void beforeSendSteps() {
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
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "Error In SendStepsService onHandleIntent: " +
                        e.getMessage() + " - " + DateFormatter.getHourlyTimeStamp());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "Error In SendStepsService onHandleIntent: " +
                        e.getMessage() + " - " + DateFormatter.getHourlyTimeStamp());
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "NullPointerException In SendStepsService onHandleIntent: " +
                        e.getMessage() + " - " + DateFormatter.getHourlyTimeStamp());
            }catch (Exception e) {
                e.printStackTrace();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                DatabaseReference myRef = database.getReference("Error");
                myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + "General Exception In SendStepsService onHandleIntent: " +
                        e.getMessage() + " - " + DateFormatter.getHourlyTimeStamp());
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

        //Future<TransactionReceipt> transactionReceiptFuture = MainActivity.contract.saveMySteps(new Uint256(steps),new Utf8String(Integer.toString(date)));
        //Log.i("--All", "Hash: "+MainActivity.contract.saveMySteps(new Uint256(steps),new Utf8String(Integer.toString(date))).get().getTransactionHash());
        Log.i("--All", "Hash: "+ contract.saveMySteps(new Uint256(steps),new Utf8String(Integer.toString(date))).get().getTransactionHash());

        editor.putInt("lastSteps",steps).commit();

        Log.i("--All", "Sending to Firebase");
        SentSteps sentSteps = new SentSteps(DateFormatter.getHourlyTimeStamp(), steps, sharedPref.getString("uniqueId","NA"));
        DatabaseReference myRef = database.getReference(sharedPref.getString("uniqueId","NA"));
        myRef.child("SentSteps").push().setValue(sentSteps);
    }

    public void loadCredAndContract() {
        try {
            credentials = WalletUtils.loadCredentials(
                    sharedPref.getString("uniqueId","NA"),
                    getFilesDir().getAbsolutePath() + "/" + sharedPref.getString("walletFileName","none"));

            Log.i("--All", "Credentials Address from SendStepsService: " + credentials.getAddress());

            Log.i("--All", "Creating Contract from SendStepsService");
            contract = _Users_Admin_Desktop_Steps_sol_Steps.load(MyApplication.contractAddress, web3, credentials, GAS_PRICE, GAS_LIMIT);

        } catch (IOException e) {
            Log.i("--All", "Error: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
        } catch (CipherException e) {
            Log.i("--All", "Error: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.i("--All", "Error: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        try {
            Log.i("--All", "Contract Valid from SendStepsService: " + contract.isValid());
        } catch (IOException e) {
            Log.i("--All", "Error: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        beforeSendSteps();
    }

}

