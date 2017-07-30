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

    TextView todayStepsBig;
    TextView day0Steps, day1Steps,day2Steps,day3Steps, day4Steps, day5Steps, day6Steps;
    TextView day0Title, day1Title, day2Title, day3Title, day4Title, day5Title, day6Title;
    TextView peopleCount0, peopleCount1, peopleCount2, peopleCount3, peopleCount4, peopleCount5, peopleCount6;

    String uniqueID = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        todayStepsBig = (TextView)findViewById(R.id.todayStepsLarge);

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

    public void getPeopleCount(int day) {
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

        String data = MyApplication.countAllPeopleDate+first64+second64+third64;

        List<Object> getStepsList = new ArrayList<>();
        Map getStepsMap = new HashMap();
        getStepsMap.put("from", MyApplication.ethAddress);
        getStepsMap.put("to",MyApplication.contractAddress);
        getStepsMap.put("data",data);
        getStepsList.add(getStepsMap);
        getStepsList.add("latest");
        //Log.i("--All", getStepsList.toString());

        new ContactBlockchain().execute("eth_call",getStepsList,99, sharedPref,"getPeople",day);
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

        String data = MyApplication.recallMySteps+first64+second64+third64;

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

            if (extra.equals("getSteps") || extra.equals("getPeople")) {
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
                    for (int i=0; i>=-6; i--) {
                        getSteps(i);
                        getPeopleCount(i);
                    }
                }

                if (method.equals("eth_sendTransaction") && extra.equals("sentSteps")) {
                    Log.i("--All", "Successfully Sent Steps");
                }

                if (method.equals("eth_call") && extra.equals("getSteps")) {

                    int i = Integer.parseInt(response.getResult().toString().substring(2),16);
                    //Log.i("--All", "Day: " + day + " - Steps = " + i);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, day);
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd");
                    String formattedDate = format.format(calendar.getTime());

                    if (day == 0) {
                        todayStepsBig.setText(i+"");
                        day0Steps.setText(i+"");
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

                    if (day == 0 ) peopleCount0.setText(i+"");
                    if (day == -1 ) peopleCount1.setText(i+"");
                    if (day == -2 ) peopleCount2.setText(i+"");
                    if (day == -3 ) peopleCount3.setText(i+"");
                    if (day == -4 ) peopleCount4.setText(i+"");
                    if (day == -5 ) peopleCount5.setText(i+"");
                    if (day == -6 ) peopleCount6.setText(i+"");
                }

            }
            else
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
        }
    }

}
