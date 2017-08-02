package gllc.tech.blocksteps;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import gllc.tech.blocksteps.Sensor.StepService;
import gllc.tech.blocksteps.Sensor.StepService2;

/**
 * Created by bhangoo on 7/28/2017.
 */

public class MyTestService extends IntentService {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    // Must create a default constructor
    public MyTestService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //int steps = (int)GetTotalSteps();
/*
        int steps = StepService.numSteps;
        sendSteps(steps);
        StepService.numSteps =0;
        */

        int steps = sharedPref.getInt("steps",0);
        sendSteps(steps);
        StepService2.numSteps =0;
        editor.putInt("steps", 0).commit();
    }


    public void sendSteps(int steps) {
        // This describes what will happen when service is triggered

        //Random r = new Random();
        //int i1 = r.nextInt();
        //int steps = i1;

        //Sending Steps
        Log.i("--All", "Send Steps from BG Service, Steps: " + steps);
        //Toast.makeText(getApplicationContext(), "Sending Steps from BG Service, Steps: " + steps, Toast.LENGTH_SHORT).show();
        String hexSteps = Integer.toHexString(steps);
        hexSteps = StringUtils.leftPad(hexSteps,64,"0");
        //Log.i("--All", "Steps in Hex: " + hexSteps);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        SimpleDateFormat format = new SimpleDateFormat("MMddyy");
        String formattedDate = format.format(calendar.getTime());
        if (formattedDate.length() == 6) {formattedDate = formattedDate.substring(1);}
        Log.i("--All", "Sending Today Date to Server: " + formattedDate);
        int date = Integer.parseInt(formattedDate);

        String hexDate = Integer.toHexString(date);
        hexDate = StringUtils.rightPad(hexDate,64,"0");
        //Log.i("--All", "Date in Hex: " + hexDate);

        //Number of steps
        String first64 = hexSteps;
        //Position of Date in Array (64)
        String second64 = "0000000000000000000000000000000000000000000000000000000000000040";
        //Character length of date
        int dateLength = Integer.toString(date).length();
        String dateLengthHex = Integer.toHexString(dateLength);
        dateLengthHex = StringUtils.leftPad(dateLengthHex,64,"0");
        //Log.i("--All", "DateLength in Hex: " + dateLengthHex);
        String third64 = dateLengthHex;
        //Date in Hex
        String fourth64 = hexDate;

        String data = MyApplication.saveMySteps+first64+second64+third64+fourth64;

        List<Object> sendStepsList = new ArrayList<>();
        Map sendStepsMap = new HashMap();
        sendStepsMap.put("from", MyApplication.ethAddress);
        sendStepsMap.put("to",MyApplication.contractAddress);
        sendStepsMap.put("data",data);
        sendStepsList.add(sendStepsMap);
        //Log.i("--All", sendStepsList.toString());
        new ContactBlockchain("eth_sendTransaction",sendStepsList,99, "sentSteps");
    }


    public class ContactBlockchain  {
        public  ContactBlockchain (String method, Object params,int id, String extra) {

            // The JSON-RPC 2.0 server URL
            URL serverURL = null;
            try {serverURL = new URL("http://45.55.4.74:8545");}
            catch (MalformedURLException e) {
                Log.e("--All", "Error in creating URL: " + e.getMessage());}

            // Create new JSON-RPC 2.0 client session
            JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

            // Construct new request
            //Log.i("--All", "Method: " + method + " - Extra: " + extra);

            JSONRPC2Request request = null;
            JSONRPC2Response response = null;

            if (params instanceof List<?>){
                List<Object> castedParams = (List<Object>)params;
                request = new JSONRPC2Request(method, castedParams, id);
            } else if (params instanceof Map<?, ?>) {
                Map<String, Object> castedParams = (Map<String, Object>) params;
                request = new JSONRPC2Request(method, castedParams, id);
            }

            try {response = mySession.send(request);}
            catch (JSONRPC2SessionException e) {Log.e("--All", "Error Sending Request: " + e.getMessage());}

            if (response.indicatesSuccess()) {
                Log.i("--All", "*******Successful Server Response (From Background): " + response.getResult() +"*******");

                if (method.equals("eth_sendTransaction") && extra.equals("sentSteps")) {
                    Log.i("--All", "Successfully Sent Steps");
                }

            }
            else
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
        }
    }

    public long GetTotalSteps(){

        Log.i("--All", "Getting Steps from BackGround");

        long total = 0;

        MainActivity.mClient.connect();
        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(MainActivity.mClient, DataType.TYPE_STEP_COUNT_DELTA);
        DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            total = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        }else {
            Log.w("--All", "There was a problem getting the step count in BackGround.");
        }

        Log.i("--All", "Total steps from BackGround is: " + total);

        return total;
    }

}

