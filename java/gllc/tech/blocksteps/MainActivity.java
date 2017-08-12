package gllc.tech.blocksteps;


import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
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

import gllc.tech.blocksteps.Sensor.StepService;
import gllc.tech.blocksteps.Sensor.StepService2;
import io.fabric.sdk.android.Fabric;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "--All";
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public static GoogleApiClient mClient = null;
    ArrayList<Integer> peopleCount = new ArrayList<>();
    String uniqueID;
    ProgressDialog dialog;

    SharedPreferences sharedPref;

    TextView todayStepsBig;
    TextView day0Steps, day1Steps,day2Steps,day3Steps, day4Steps, day5Steps, day6Steps;
    TextView day0Title, day1Title, day2Title, day3Title, day4Title, day5Title, day6Title;
    TextView peopleCount0, peopleCount1, peopleCount2, peopleCount3, peopleCount4, peopleCount5, peopleCount6;
    TextView avgPeopleSteps0, avgPeopleSteps1 , avgPeopleSteps2, avgPeopleSteps3, avgPeopleSteps4, avgPeopleSteps5, avgPeopleSteps6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());
        uniqueID = getHardwareId(this);

        View view = null;
        //forceCrash(view);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading");
        dialog.show();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //sharedPref.edit().clear().commit();

        assignUI();
        checkEthereumAddress();

        Intent i = new Intent(this, StepService2.class);
        startService(i);
        //scheduleAlarm();
        dailyAlarm();

/*
        int steps = sharedPref.getInt("steps",0);
        Log.i("--All", "Main Activity Steps = " + steps);

        todayStepsBig.setText(Integer.toString(steps));
        day0Steps.setText(Integer.toString(steps));
*/
        //todayStepsBig.setText(StepService2.numSteps+"");
        //day0Steps.setText(StepService2.numSteps+"");


/*
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
*/



/*
        if (!StepService.isIntentServiceRunning) {
            //Toast.makeText(getApplicationContext(), "Starting Service", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, StepService.class);
            //i.putExtra("foo", "bar");
            startService(i);
            dailyAlarm();
        }
*/
        //todayStepsBig.setText(StepService.numSteps+"");
        //day0Steps.setText(StepService.numSteps+"");

    }

    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }


    private void checkEthereumAddress() {

        MyApplication.ethAddress = sharedPref.getString("ethAddress","none");

        if (MyApplication.ethAddress.equals("none")) {
            Log.i("--All", "Requesting ethAddress");
            List<Object> params = new ArrayList<>();
            params.add(uniqueID);
            //Toast.makeText(getApplicationContext(),"One Moment",Toast.LENGTH_SHORT).show();
            new ContactBlockchain().execute("personal_newAccount",params,99, sharedPref, "NA");
            //Toast.makeText(getApplicationContext(),"Getting New Address",Toast.LENGTH_SHORT).show();
        }

        else {
            Log.i("--All", "Already Have ethAddress");

            List<Object> params = new ArrayList<>();
            params.add(MyApplication.ethAddress);
            params.add(uniqueID);
            params.add(0);

            new ContactBlockchain().execute("personal_unlockAccount",params,99, sharedPref,"personalUnlock");

            //Toast.makeText(getApplicationContext(),"Loading Your Data",Toast.LENGTH_SHORT).show();
        }
    }

    public void makeEthCall(int day, String call, String prefix) {
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

        String data = prefix+first64+second64+third64;

        List<Object> getStepsList = new ArrayList<>();
        Map getStepsMap = new HashMap();
        getStepsMap.put("from", MyApplication.ethAddress);
        getStepsMap.put("to",MyApplication.contractAddress);
        getStepsMap.put("data",data);
        getStepsList.add(getStepsMap);
        getStepsList.add("latest");
        //Log.i("--All", getStepsList.toString());

        new ContactBlockchain().execute("eth_call",getStepsList,99, sharedPref,call,day);
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

                                //scheduleAlarm();
                                //dailyAlarm();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(StepService2.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, filter);
        // or `registerReceiver(testReceiver, filter)` for a normal broadcast
        todayStepsBig.setText(StepService2.numSteps+"");
        day0Steps.setText(StepService2.numSteps+"");
    }

    // Define the callback for what to do when data is received
    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                String resultValue = intent.getStringExtra("resultValue");
                //Toast.makeText(MainActivity.this, "From Activity: " + resultValue, Toast.LENGTH_SHORT).show();
                todayStepsBig.setText(resultValue);
                day0Steps.setText(resultValue);
            }
        }
    };

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
        Log.i("--All", "Daily Alarm Set");
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

            if (extra.equals("getSteps") || extra.equals("getPeople") || extra.equals("getEveryoneSteps")) {
                day = (Integer)objects[5];
            }

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

            return response;
        }

        @Override
        protected void onPostExecute(JSONRPC2Response response) {

            // Print response result / error
            if (response.indicatesSuccess()) {
                //Log.i("--All", "*******Successful Server Response: " + response.getResult() +"*******");

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

                    new ContactBlockchain().execute("personal_unlockAccount",params,99, sharedPref,"mainUnlock");
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

                    //Log.i("--All", params.toString());

                    new ContactBlockchain().execute("eth_sendTransaction",params3,99, sharedPref,"mainUnlock");
                }

                if (method.equals("personal_unlockAccount") && extra.equals("personalUnlock")) {
                    loadData();
                }

                if (method.equals("eth_sendTransaction") && extra.equals("mainUnlock")) {
                    loadData();
                }

                if (method.equals("eth_call") && extra.equals("getSteps")) {

                    int i = Integer.parseInt(response.getResult().toString().substring(2),16);
                    //Log.i("--All", "Day: " + day + " - Steps = " + i);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, day);
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd");
                    String formattedDate = format.format(calendar.getTime());

                    if (day == 0) {
                        //todayStepsBig.setText(i+"");
                        //day0Steps.setText(i+"");
                        day0Title.setText(formattedDate);
                    }
                    if (day == -1) {
                        day1Steps.setText(i+"");
                        day1Title.setText(formattedDate);
                    }
                    if (day == -2) {
                        day2Steps.setText(i+"");
                        day2Title.setText(formattedDate);
                    }
                    if (day == -3) {
                        day3Steps.setText(i+"");
                        day3Title.setText(formattedDate);
                    }
                    if (day == -4) {
                        day4Steps.setText(i+"");
                        day4Title.setText(formattedDate);
                    }
                    if (day == -5) {
                        day5Steps.setText(i+"");
                        day5Title.setText(formattedDate);
                    }
                    if (day == -6) {
                        day6Steps.setText(i+"");
                        day6Title.setText(formattedDate);
                    }
                }

                if (method.equals("eth_call") && extra.equals("getPeople")) {

                    int i = Integer.parseInt(response.getResult().toString().substring(2),16);

                    if (day == 0 )  peopleCount0.setText(i+"");
                    if (day == -1 ) peopleCount1.setText(i+"");
                    if (day == -2 ) peopleCount2.setText(i+"");
                    if (day == -3 ) peopleCount3.setText(i+"");
                    if (day == -4 ) peopleCount4.setText(i+"");
                    if (day == -5 ) peopleCount5.setText(i+"");
                    if (day == -6 ) peopleCount6.setText(i+"");

                    int index = day * -1;
                    peopleCount.add(index,i);

                    if (day == -6){
                        for (int j=0; j>=-6; j--) {
                            //getSteps(i);
                            makeEthCall(j, "getEveryoneSteps", MyApplication.everyoneStepsDate);
                        }
                    }
                }

                if (method.equals("eth_call") && extra.equals("getEveryoneSteps")) {
                    int i = Integer.parseInt(response.getResult().toString().substring(2),16);
                    int index = day * -1;

                    int avgSteps;
                    if (peopleCount.get(index) != 0) avgSteps = i / peopleCount.get(index);
                    else avgSteps = 0;


                    if (day == 0 ) avgPeopleSteps0.setText(avgSteps+"");
                    if (day == -1 ) avgPeopleSteps1.setText(avgSteps+"");
                    if (day == -2 ) avgPeopleSteps2.setText(avgSteps+"");
                    if (day == -3 ) avgPeopleSteps3.setText(avgSteps+"");
                    if (day == -4 ) avgPeopleSteps4.setText(avgSteps+"");
                    if (day == -5 ) avgPeopleSteps5.setText(avgSteps+"");
                    if (day == -6 ) avgPeopleSteps6.setText(avgSteps+"");

                    dialog.dismiss();
                }

            }
            else
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
        }
    }

    public void loadData() {
        Log.i("--All", "Loading All Data from Blockchain");
        for (int i=0; i>=-6; i--) {
            //getSteps(i);
            makeEthCall(i,"getSteps", MyApplication.recallMySteps);
            makeEthCall(i,"getPeople", MyApplication.countAllPeopleDate);
            //makeEthCall(i, "getEveryoneSteps", MyApplication.everyoneStepsDate);
        }
    }

    public void assignUI() {
        todayStepsBig = (TextView)findViewById(R.id.todayStepsLarge);

        avgPeopleSteps0 = (TextView)findViewById(R.id.avgPeopleSteps0);
        avgPeopleSteps1 = (TextView)findViewById(R.id.avgPeopleSteps1);
        avgPeopleSteps2 = (TextView)findViewById(R.id.avgPeopleSteps2);
        avgPeopleSteps3 = (TextView)findViewById(R.id.avgPeopleSteps3);
        avgPeopleSteps4 = (TextView)findViewById(R.id.avgPeopleSteps4);
        avgPeopleSteps5 = (TextView)findViewById(R.id.avgPeopleSteps5);
        avgPeopleSteps6 = (TextView)findViewById(R.id.avgPeopleSteps6);

        day0Steps = (TextView)findViewById(R.id.weekSteps0);
        day1Steps = (TextView)findViewById(R.id.weekSteps1);
        day2Steps = (TextView)findViewById(R.id.weekSteps2);
        day3Steps = (TextView)findViewById(R.id.weekSteps3);
        day4Steps = (TextView)findViewById(R.id.weekSteps4);
        day5Steps = (TextView)findViewById(R.id.weekSteps5);
        day6Steps = (TextView)findViewById(R.id.weekSteps6);

        day0Title = (TextView)findViewById(R.id.weekDayTitle0);
        day1Title = (TextView)findViewById(R.id.weekDayTitle1);
        day2Title = (TextView)findViewById(R.id.weekDayTitle2);
        day3Title = (TextView)findViewById(R.id.weekDayTitle3);
        day4Title = (TextView)findViewById(R.id.weekDayTitle4);
        day5Title = (TextView)findViewById(R.id.weekDayTitle5);
        day6Title = (TextView)findViewById(R.id.weekDayTitle6);

        peopleCount0 = (TextView)findViewById(R.id.peopleCount0);
        peopleCount1 = (TextView)findViewById(R.id.peopleCount1);
        peopleCount2 = (TextView)findViewById(R.id.peopleCount2);
        peopleCount3 = (TextView)findViewById(R.id.peopleCount3);
        peopleCount4 = (TextView)findViewById(R.id.peopleCount4);
        peopleCount5 = (TextView)findViewById(R.id.peopleCount5);
        peopleCount6 = (TextView)findViewById(R.id.peopleCount6);
    }

    public static String getHardwareId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
