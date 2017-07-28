package gllc.tech.blocksteps;


import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
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
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    SharedPreferences sharedPref;

    TextView day1Steps,day2Steps,day3Steps;
    EditText getDate, getSteps;
    Button sendSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new VerifyDataTask().execute();

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        day1Steps = (TextView)findViewById(R.id.day1Steps);
        day2Steps = (TextView)findViewById(R.id.day2Steps);
        day3Steps = (TextView)findViewById(R.id.day3Steps);
        getDate = (EditText)findViewById(R.id.enterDate);
        getSteps = (EditText)findViewById(R.id.enterSteps);
        sendSteps = (Button)findViewById(R.id.sendSteps);

        //String hex = Integer.toHexString(72417);
        //Log.i("--All", "Hex: " + hex);
        //Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE" + Integer.parseInt(hex,16));

        checkEthereumAddress();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        sendSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Sending Steps
                int date = Integer.parseInt(getDate.getText().toString());
                String hexDate = Integer.toHexString(date);
                hexDate = StringUtils.rightPad(hexDate,64,"0");
                Log.i("--All", "Date in Hex: " + hexDate);

                int steps = Integer.parseInt(getSteps.getText().toString());
                String hexSteps = Integer.toHexString(steps);
                hexSteps = StringUtils.leftPad(hexSteps,64,"0");
                Log.i("--All", "Steps in Hex: " + hexSteps);

                //Number of steps
                String first64 = hexSteps;
                //Position of Date in Array (64)
                String second64 = "0000000000000000000000000000000000000000000000000000000000000040";
                //Character length of date
                int dateLength = getDate.getText().toString().length();
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
                new ContactBlockchain().execute("eth_sendTransaction",sendStepsList,99, sharedPref,"sentSteps");



/*
                List<Object> sendStepsList = new ArrayList<>();
                Map sendStepsMap = new HashMap();
                sendStepsMap.put("from", MyApplication.ethAddress);
                sendStepsMap.put("to",MyApplication.contractAddress);
                sendStepsMap.put("data","0xd4caa4db00000000000000000000000000000000000000000000000000000000000000090000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000511AE100000000000000000000000000000000000000000000000000000000000");
                sendStepsList.add(sendStepsMap);
                //Log.i("--All", params.toString());
                new ContactBlockchain().execute("eth_sendTransaction",sendStepsList,99, sharedPref,"sentSteps");
                */
            }
        });


    }

    @Override
    public void onConnected(Bundle bundle) {
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE )
                .setDataSourceTypes( DataSource.TYPE_RAW )
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 3, TimeUnit.SECONDS )
                .build();

        Fitness.SensorsApi.add( mApiClient, request, this )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i( "--All", "SensorApi successfully added" );
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if( !authInProgress ) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult( MainActivity.this, REQUEST_OAUTH );
            } catch(IntentSender.SendIntentException e ) {

            }
        } else {
            Log.e( "--All", "authInProgress" );
        }

    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for( final Field field : dataPoint.getDataType().getFields() ) {
            final Value value = dataPoint.getValue( field );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();


                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_OAUTH ) {
            authInProgress = false;
            if( resultCode == RESULT_OK ) {
                if( !mApiClient.isConnecting() && !mApiClient.isConnected() ) {
                    mApiClient.connect();
                }
            } else if( resultCode == RESULT_CANCELED ) {
                Log.e( "--All", "RESULT_CANCELED" );
            }
        } else {
            Log.e("--All", "requestCode NOT request_oauth");
        }
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

                    //Getting Steps
                    /*
                    List<Object> getStepsList = new ArrayList<>();
                    Map getStepsMap = new HashMap();
                    getStepsMap.put("from", MyApplication.ethAddress);
                    getStepsMap.put("to",MyApplication.contractAddress);
                    getStepsMap.put("data","0x5c6718730000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000511AE100000000000000000000000000000000000000000000000000000000000");
                    getStepsList.add(getStepsMap);
                    getStepsList.add("latest");
                    Log.i("--All", getStepsList.toString());
                    new ContactBlockchain().execute("eth_call",getStepsList,99, sharedPref,"getSteps");
*/
                    /*
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, -3);
                    SimpleDateFormat format = new SimpleDateFormat("MMddyy");
                    String formattedDate = format.format(calendar.getTime());
                    if (formattedDate.length() == 6) {formattedDate = formattedDate.substring(1);}
                    Log.i("--All", "Day: " + formattedDate);

                    //Position of Date in bytes
                    String first64 = "0000000000000000000000000000000000000000000000000000000000000020";
                    //Length of Date
                    int dateLength = formattedDate.length();
                    Log.i("--All", "DateLength: " + dateLength);
                    String dateLengthHex = Integer.toHexString(dateLength);
                    dateLengthHex = StringUtils.leftPad(dateLengthHex,64,"0");
                    Log.i("--All", "DateLength in Hex: " + dateLengthHex);
                    String second64 = dateLengthHex;
                    //Date in Hex
                    String hexDate = Integer.toHexString(Integer.parseInt(formattedDate));
                    hexDate = StringUtils.rightPad(hexDate,64,"0");
                    Log.i("--All", "Date in Hex: " + hexDate);
                    String third64 = hexDate;

                    String data = "0x5c671873"+first64+second64+third64;

                    List<Object> getStepsList = new ArrayList<>();
                    Map getStepsMap = new HashMap();
                    getStepsMap.put("from", MyApplication.ethAddress);
                    getStepsMap.put("to",MyApplication.contractAddress);
                    getStepsMap.put("data",data);
                    getStepsList.add(getStepsMap);
                    getStepsList.add("latest");
                    Log.i("--All", getStepsList.toString());
                    new ContactBlockchain().execute("eth_call",getStepsList,99, sharedPref,"getSteps");
*/
                }

                if (method.equals("eth_sendTransaction") && extra.equals("sentSteps")) {
                    Log.i("--All", "Successfully Sent Steps");
                }

                if (method.equals("eth_call") && extra.equals("getSteps")) {

                    int i = Integer.parseInt(response.getResult().toString().substring(2),16);
                    Log.i("--All", "Day: " + day + " - Steps = " + i);

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
        Log.i("--All", "Day: " + formattedDate);

        //Position of Date in bytes
        String first64 = "0000000000000000000000000000000000000000000000000000000000000020";
        //Length of Date
        int dateLength = formattedDate.length();
        Log.i("--All", "DateLength: " + dateLength);
        String dateLengthHex = Integer.toHexString(dateLength);
        dateLengthHex = StringUtils.leftPad(dateLengthHex,64,"0");
        Log.i("--All", "DateLength in Hex: " + dateLengthHex);
        String second64 = dateLengthHex;
        //Date in Hex
        String hexDate = Integer.toHexString(Integer.parseInt(formattedDate));
        hexDate = StringUtils.rightPad(hexDate,64,"0");
        Log.i("--All", "Date in Hex: " + hexDate);
        String third64 = hexDate;

        String data = "0x5c671873"+first64+second64+third64;

        List<Object> getStepsList = new ArrayList<>();
        Map getStepsMap = new HashMap();
        getStepsMap.put("from", MyApplication.ethAddress);
        getStepsMap.put("to",MyApplication.contractAddress);
        getStepsMap.put("data",data);
        getStepsList.add(getStepsMap);
        getStepsList.add("latest");
        Log.i("--All", getStepsList.toString());

        new ContactBlockchain().execute("eth_call",getStepsList,99, sharedPref,"getSteps",day);
    }

    private class VerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE");
            long total = 0;

            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
            } else {
                Log.w("--All", "There was a problem getting the step count.");
            }

            Log.i("--All", "Total steps: " + total);

            return null;
        }
    }

}
