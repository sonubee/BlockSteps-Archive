package gllc.tech.blocksteps;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

/**
 * Created by bhangoo on 7/28/2017.
 */

public class MyTestService extends IntentService {
    // Must create a default constructor
    public MyTestService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        sendSteps();

    }


    public void sendSteps() {
        // This describes what will happen when service is triggered

        //Sending Steps
        int steps = 124;
        steps++;
        Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE-- FROM BACKGROUND SERVICE - " + steps);
        String hexSteps = Integer.toHexString(steps);
        hexSteps = StringUtils.leftPad(hexSteps,64,"0");
        Log.i("--All", "Steps in Hex: " + hexSteps);

        int date = 72217;
        String hexDate = Integer.toHexString(date);
        hexDate = StringUtils.rightPad(hexDate,64,"0");
        Log.i("--All", "Date in Hex: " + hexDate);

        //Number of steps
        String first64 = hexSteps;
        //Position of Date in Array (64)
        String second64 = "0000000000000000000000000000000000000000000000000000000000000040";
        //Character length of date
        int dateLength = Integer.toString(date).length();
        String dateLengthHex = Integer.toHexString(dateLength);
        dateLengthHex = StringUtils.leftPad(dateLengthHex,64,"0");
        Log.i("--All", "DateLength in Hex: " + dateLengthHex);
        String third64 = dateLengthHex;
        //Date in Hex
        String fourth64 = hexDate;

        String data = "0xd4caa4db"+first64+second64+third64+fourth64;

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

        String method;
        String extra;
        SharedPreferences sharedPref;
        //TextView displaySteps;
        int day=1;

        public  ContactBlockchain (String method, Object params,int id, String extra) {

            // The JSON-RPC 2.0 server URL
            URL serverURL = null;
            try {serverURL = new URL("http://45.55.4.74:8545");}
            catch (MalformedURLException e) {
                Log.e("--All", "Error in creating URL: " + e.getMessage());}

            // Create new JSON-RPC 2.0 client session
            JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

            // Construct new request
            Log.i("--All", "Method: " + method + " - Extra: " + extra);

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
                Log.i("--All", "*******Successful Server Response: " + response.getResult() +"*******");

                if (method.equals("eth_sendTransaction") && extra.equals("sentSteps")) {
                    Log.i("--All", "Successfully Sent Steps");
                }

            }
            else
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
        }
    }
}

