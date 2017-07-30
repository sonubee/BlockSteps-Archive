package gllc.tech.blocksteps;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "--All";
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public static GoogleApiClient mClient = null;

    SharedPreferences sharedPref;

    TextView day1Steps,day2Steps,day3Steps;

    String uniqueID = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        day1Steps = (TextView)findViewById(R.id.day1Steps);
        day2Steps = (TextView)findViewById(R.id.day2Steps);
        day3Steps = (TextView)findViewById(R.id.day3Steps);

        checkEthereumAddress();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();

        Log.i("--All", "Unique ID: "+uniqueID);
        Log.i("--All", "Device ID: "+getHardwareId(this));
    }

    public static String getHardwareId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void checkEthereumAddress() {

        MyApplication.ethAddress = sharedPref.getString("ethAddress","none");

        if (MyApplication.ethAddress.equals("none")) {
            Log.i("--All", "Requesting ethAddress");
            List<Object> params = new ArrayList<>();
            params.add("hellya");
            new ContactBlockchain().execute("personal_newAccount",params,99, sharedPref, "NA");
            Toast.makeText(getApplicationContext(),"Getting New Address",Toast.LENGTH_SHORT).show();
        }

        else {
            Log.i("--All", "Already Have ethAddress");

            List<Object> params = new ArrayList<>();
            params.add(MyApplication.ethAddress);
            params.add("hellya");
            params.add(0);

            new ContactBlockchain().execute("personal_unlockAccount",params,99, sharedPref,"personalUnlock");

            Toast.makeText(getApplicationContext(),"Already Have Address - Unlocking",Toast.LENGTH_SHORT).show();
        }
    }

    public class ContactBlockchain extends AsyncTask<Object, Void, JSONRPC2Response> {

        String method;
        String extra;
        SharedPreferences sharedPref;
        //TextView displaySteps;
        int day=1;

        @Override
        protected JSONRPC2Response doInBackground(Object... objects) {

            // The JSON-RPC 2.0 server URL
            URL serverURL = null;
            try {serverURL = new URL("http://45.55.4.74:8545");}
            catch (MalformedURLException e) {Log.e("--All", "Error in creating URL: " + e.getMessage());}

            // Create new JSON-RPC 2.0 client session
            JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

            // Construct new request
            method = (String)objects[0];
            Object params = objects[1];
            int id = (Integer)objects[2];
            sharedPref = (SharedPreferences)objects[3];
            extra = (String)objects[4];

            if (extra.equals("getSteps")) {
                day = (Integer)objects[5];
            }

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

            return response;

        }

        @Override
        protected void onPostExecute(JSONRPC2Response response) {

            // Print response result / error
            if (response.indicatesSuccess()) {
                Log.i("--All", "*******Successful Server Response: " + response.getResult() +"*******");

                //Storing new address in SharedPreferences
                if (method.equals("personal_newAccount")) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("ethAddress",response.getResult().toString());
                    editor.commit();
                    MyApplication.ethAddress = response.getResult().toString();

                    Log.i("--All", "Created Address: " + response.getResult().toString());

                    //Unlock Main Account to Send Ether to new Address
                    List<Object> params = new ArrayList<>();
                    params.add(MyApplication.mainEtherAddress);
                    params.add("hellya");
                    params.add(0);

                    Log.i("--All", "Unlocking Account to send Ether: " + MyApplication.mainEtherAddress);

                    new ContactBlockchain().execute("personal_unlockAccount",params,99, sharedPref,"NA");
                }

                if (method.equals("personal_unlockAccount") && extra.equals("mainUnlock")) {
                    //Send Ether to new Address
                    Log.i("--All", "Successfully Unlocked, now Sending Ether");

                    List<Object> params3 = new ArrayList<>();

                    Map params = new HashMap();
                    params.put("from", MyApplication.mainEtherAddress);
                    params.put("to",MyApplication.ethAddress);
                    params.put("value","0xDE0B6B3A7640000");

                    params3.add(params);

                    Log.i("--All", params.toString());

                    new ContactBlockchain().execute("eth_sendTransaction",params3,99, sharedPref,"mainUnlock");
                }

                if (method.equals("personal_unlockAccount") && extra.equals("personalUnlock")) {
                    for (int i=0; i>=-2; i--) {
                        getSteps(i);
                    }
                }

                if (method.equals("eth_sendTransaction") && extra.equals("sentSteps")) {
                    Log.i("--All", "Successfully Sent Steps");
                }

                if (method.equals("eth_call") && extra.equals("getSteps")) {

                    int i = Integer.parseInt(response.getResult().toString().substring(2),16);
                    //Log.i("--All", "Day: " + day + " - Steps = " + i);

                    if (day == 0) day1Steps.setText("Today Steps = " + i);
                    if (day == -1) day2Steps.setText("Yesterday Steps = " + i);
                    if (day == -2) day3Steps.setText("2 Days Ago Steps = " + i);
                }

            }
            else
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
        }
    }

    public void getSteps(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, day);
        SimpleDateFormat format = new SimpleDateFormat("MMddyy");
        String formattedDate = format.format(calendar.getTime());
        if (formattedDate.length() == 6) {formattedDate = formattedDate.substring(1);}
        //Log.i("--All", "Day: " + formattedDate);

        //Position of Date in bytes
        String first64 = "0000000000000000000000000000000000000000000000000000000000000020";
        //Length of Date
        int dateLength = formattedDate.length();
        //Log.i("--All", "DateLength: " + dateLength);
        String dateLengthHex = Integer.toHexString(dateLength);
        dateLengthHex = StringUtils.leftPad(dateLengthHex,64,"0");
        //Log.i("--All", "DateLength in Hex: " + dateLengthHex);
        String second64 = dateLengthHex;
        //Date in Hex
        String hexDate = Integer.toHexString(Integer.parseInt(formattedDate));
        hexDate = StringUtils.rightPad(hexDate,64,"0");
        //Log.i("--All", "Date in Hex: " + hexDate);
        String third64 = hexDate;

        String data = "0x5c671873"+first64+second64+third64;

        List<Object> getStepsList = new ArrayList<>();
        Map getStepsMap = new HashMap();
        getStepsMap.put("from", MyApplication.ethAddress);
        getStepsMap.put("to",MyApplication.contractAddress);
        getStepsMap.put("data",data);
        getStepsList.add(getStepsMap);
        getStepsList.add("latest");
        //Log.i("--All", getStepsList.toString());

        new ContactBlockchain().execute("eth_call",getStepsList,99, sharedPref,"getSteps",day);
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Look at some data!!
                                //new InsertAndVerifyDataTask().execute();
                                //new VerifyDataTask().execute();

                                //launchTestService();
                                //scheduleAlarm();
                                dailyAlarm();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                Log.i("--All", "Disconnected!!!");
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Google Play services connection failed. Cause: " +
                                result.toString());
                    }
                })
                .build();
    }

    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            // Begin by creating the query.
            DataReadRequest readRequest = queryFitnessData();

            // [START read_dataset]
            // Invoke the History API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            // [END read_dataset]

            // For the sake of the sample, we'll print the data so we can see what we just added.
            // In general, logging fitness information should be avoided for privacy reasons.
            printData(dataReadResult);



            return null;
        }
    }


    public static DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        final DataSource ds = new DataSource.Builder()
                .setAppPackageName("com.google.android.gms")
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .build();

        final DataReadRequest req = new DataReadRequest.Builder()
                .aggregate(ds, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        //return readRequest;
        return req;
    }
    /**
     * Log a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would
     * dump all the data. In this sample, logging also prints to the device screen, so we can see
     * what the query returns, but your app should not log fitness information as a privacy
     * consideration. A better option would be to dump the data you receive to a local data
     * directory to avoid exposing it to other applications.
     */
    public static void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();


        for (DataPoint dp : dataSet.getDataPoints()) {

            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                //textView.append(" Value: " + dp.getValue(field));
            }
        }
    }
    // [END parse_dataset]

    public class VerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            long total = 0;

            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
            } else {
                Log.w(TAG, "There was a problem getting the step count.");
            }

            Log.i(TAG, "Total steps1: " + total);

            final long finalTotal = total;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Your code to run in GUI thread here
                    Toast.makeText(getApplicationContext(), "Current Steps: " + finalTotal, Toast.LENGTH_SHORT).show();
                }//public void run() {
            });
            return null;
        }
    }

    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, MyTestService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
        alarm.setRepeating(AlarmManager.RTC, firstMillis, 1000 * 60, pIntent);
    }

    public void dailyAlarm() {
        Log.i("--All", "Daily Alarm");
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 50);

        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pIntent);
    }
}
