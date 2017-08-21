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


import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionTimeoutException;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.ParityFactory;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.tx.Transfer;

import org.web3j.utils.Convert;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import gllc.tech.blocksteps.Objects.SentSteps;
import gllc.tech.blocksteps.Sensor.StepService;
import gllc.tech.blocksteps.Sensor.StepService2;
import io.fabric.sdk.android.Fabric;

import static android.R.attr.value;
import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;
import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "--All";
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    //public static GoogleApiClient mClient = null;
    ArrayList<Integer> peopleCount = new ArrayList<>();
    String uniqueID;
    ProgressDialog dialog;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    TextView todayStepsBig;
    TextView day0Steps, day1Steps,day2Steps,day3Steps, day4Steps, day5Steps, day6Steps;
    TextView day0Title, day1Title, day2Title, day3Title, day4Title, day5Title, day6Title;
    TextView peopleCount0, peopleCount1, peopleCount2, peopleCount3, peopleCount4, peopleCount5, peopleCount6;
    TextView avgPeopleSteps0, avgPeopleSteps1 , avgPeopleSteps2, avgPeopleSteps3, avgPeopleSteps4, avgPeopleSteps5, avgPeopleSteps6;
    TextView versionName;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Credentials credentials = null;
    Web3j web3 = Web3jFactory.build(new HttpService("http://45.55.4.74:8545"));  // defaults to http://localhost:8545/
    _Users_Admin_Desktop_Steps_sol_Steps contract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fabric.with(this, new Crashlytics());
        uniqueID = getHardwareId(this);
        //FirebaseApp.initializeApp(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();
        editor.putString("uniqueId",uniqueID).commit();
        //editor.putString("ethAddress","none").commit();
        //editor.putString("ethAddress","0x14694471df6c9e2b2fe8e16ea98e12b421042bd6").commit();

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading From BlockChain");
        //dialog.show();

        //to clear eth address
        //sharedPref.edit().clear().commit();

        assignUI();
        //checkEthereumAddress();
        checkForWallet();

        Intent i = new Intent(this, StepService2.class);
        startService(i);

        Log.i("--All", "Checking Alarm From MainActivity");
        //if (!(SetAlarm.alarmUp(getApplicationContext()))) new SetAlarm(getApplicationContext());

        //Crashlytics.log("Test Log");

        Web3ClientVersion web3ClientVersion = null;
        try {
            web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();

        Log.i("--All", "Client Version: "+clientVersion);

        //Log.i("--All", "Address from SharedPref: " + sharedPref.getString("ethAddress","none"));
/*
        Parity parity = ParityFactory.build(new HttpService("http://45.55.4.74:8545"));  // defaults to http://localhost:8545/
        PersonalUnlockAccount personalUnlockAccount = null;
        try {
            personalUnlockAccount = parity.personalUnlockAccount(sharedPref.getString("ethAddress","none"), uniqueID).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (personalUnlockAccount.accountUnlocked()) {
            // send a transaction, or use parity.personalSignAndSendTransaction() to do it all in one
            Log.i("--All", "Account Unlocked with Parity");
        }
        */
    /*
        //Call function
        Function function = new Function(
                //"returnInt2",
                //"everyoneStepsDate",
                //"recallMySteps",
                "countAllPeopleDate",
                //"returnString",
                //"getMap",
                //Arrays.<Type>asList(new Utf8String(Integer.toString(SendStepsService.getFormattedDate()))),
                Arrays.<Type>asList(new Utf8String("82017")),
                //Arrays.<Type>asList(new Uint256(1)),
                //Arrays.<Type>asList(),
                //Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() { }));
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() { }));


        String encodedFunction = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.response.EthCall response = null;
        try {
            response = web3.ethCall(
                    Transaction.createEthCallTransaction(sharedPref.getString("ethAddress","none"), MyApplication.contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST)
                    .sendAsync().get();
        } catch (InterruptedException e) {
            Log.i("--All", "Error: " + e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.i("--All", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        List<Type> someTypes = FunctionReturnDecoder.decode(
                response.getValue(), function.getOutputParameters());

        Log.i("--All", "Response Hex: " + response.getValue());
        Toast.makeText(getApplicationContext(), "Response: " + someTypes.get(0).getValue(), Toast.LENGTH_LONG).show();
        Log.i("--All", "Response: " + someTypes.get(0).getValue());

//Create nonce
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3.ethGetTransactionCount(
                    //sharedPref.getString("ethAddress","none"), DefaultBlockParameterName.LATEST).sendAsync().get();
                    MyApplication.contractAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException e) {
            Log.i("--All", "Error: " + e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.i("--All", "Error: " + e.getMessage());
            e.printStackTrace();
        }
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        BigInteger nonce2 = new BigInteger("229");

        Log.i("--All", "Nonce: " + nonce);

//Create Transaction

        Function function2 = new Function(
                "saveMySteps",  // function we're calling
                Arrays.<Type>asList(new Uint256(25), new Utf8String("82117")),  // Parameters to pass as Solidity Types
                Arrays.<TypeReference<?>>asList());

        String encodedFunction2 = FunctionEncoder.encode(function2);
        Transaction transaction = Transaction.createFunctionCallTransaction(
                sharedPref.getString("ethAddress","none"), nonce, Transaction.DEFAULT_GAS, new BigInteger("2100000"), MyApplication.contractAddress, new BigInteger("1"), encodedFunction2);

        org.web3j.protocol.core.methods.response.EthSendTransaction transactionResponse = null;
        try {
            transactionResponse = web3.ethSendTransaction(transaction).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("--All", "Error: " + e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.i("--All", "Error: " + e.getMessage());
        }

        String transactionHash = transactionResponse.getTransactionHash();
        Log.i("--All", "Hash: "+transactionHash);

//Get Receipt

        EthGetTransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = web3.ethGetTransactionReceipt(transactionHash).sendAsync().get();
            //transactionReceipt = web3.ethGetTransactionReceipt("0xf97162cea7755217a0bf32f1a19375f5afdf9863cc6360cb1d9471512f334666").sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("--All", "Error: " + e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.i("--All", "Error: " + e.getMessage());
        }


        //Log.i("--All", "FIIIIIIIIIIIIIIIIIINDMEEEE"+transactionReceipt.getTransactionReceipt().toString());
*/
    }

    private void checkForWallet() {
        if (sharedPref.getString("walletFileName","none").equals("none")) new CreateWallet().execute();
        else new LoadCredentials().execute();
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
            MyApplication.ethAddress = sharedPref.getString("ethAddress","none");

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
            try {response.indicatesSuccess();
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

                        //Log.i("--All", "Everyone Steps = " + i);

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
                else{
                    Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());

                    DatabaseReference myRef = database.getReference("Error");
                    myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Version " + BuildConfig.VERSION_NAME + ": " + response.getError().toString() + " - In MainActivity");
                    Crashlytics.logException(response.getError());
                }

            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i("--All", "Error: " + e.getMessage());
                Crashlytics.logException(e);
            }

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

        versionName = (TextView)findViewById(R.id.versionName);
        versionName.setText("Version: " + BuildConfig.VERSION_NAME);
    }

    public static String getHardwareId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
/*
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
    }*/

    public class CreateWallet extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            //Creating wallet
            String fileName = "not set";

            Log.i("--All", "Creating Wallet");
            DatabaseReference myRef = database.getReference("First Load");
            myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Creating Wallet");

            try {
                fileName = WalletUtils.generateNewWalletFile(
                        uniqueID,
                        new File(getFilesDir(), "/"),false);
            } catch (CipherException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            }

            Log.i("--All", "Filename: " + fileName);
            editor.putString("walletFileName",fileName).commit();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.i("--All", "Loading Wallet (from Wallet Creation)");
            DatabaseReference myRef = database.getReference("First Load");
            myRef.child(sharedPref.getString("uniqueId","NA")).push().setValue("Loading Wallet (from Wallet Creation)");

            new LoadCredentials().execute();
        }
    }

    public class LoadCredentials extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            try {
                credentials = WalletUtils.loadCredentials(
                        uniqueID,
                        getFilesDir().getAbsolutePath() + "/" + sharedPref.getString("walletFileName","none"));
            } catch (IOException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            } catch (CipherException e) {
                Log.i("--All", "Error: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.i("--All", "Credentials Address: " + credentials.getAddress());

            editor.putString("ethAddress",credentials.getAddress()).commit();

            new CreateContract().execute();
        }
    }

    public class CreateContract extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("--All", "Creating Contract");
            contract = _Users_Admin_Desktop_Steps_sol_Steps.load(MyApplication.contractAddress, web3, credentials, GAS_PRICE, GAS_LIMIT);

            try {
                Log.i("--All", "Contract Valid: " + contract.isValid());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Future<Uint256> resultSteps = contract.everyoneStepsDate(new Utf8String("82117"));
            try {
                Log.i("--All", "Everyone Steps: " + resultSteps.get().getValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            int steps = 35;

            Log.i("--All", "Invoking Method");
            Future<TransactionReceipt> temp = contract.saveMySteps(new Uint256(steps),new Utf8String(Integer.toString(82117)));
            try {
                Log.i("--All", "Hash: "+temp.get().getTransactionHash());
                Log.i("--All", "Contract Address: "+temp.get().getContractAddress());
                Log.i("--All", "Block Number: "+temp.get().getBlockNumber());


            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            for (int i=0; i>=-6; i--) {
                loadBlockchainData(i);
            }

            //if (!(SetAlarm.alarmUp(getApplicationContext()))) new SetAlarm(getApplicationContext());


        }
    }

    public void loadBlockchainData(int day) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, day);
        SimpleDateFormat format = new SimpleDateFormat("MMddyy");
        String formattedDate = format.format(calendar.getTime());
        if (formattedDate.length() == 6) {formattedDate = formattedDate.substring(1);}

        loadDatesAndSteps(day, formattedDate);
        loadPeopleCount(day, formattedDate);
        loadEveryoneSteps(day, formattedDate);
    }

    public void loadEveryoneSteps(int day, String formattedDate) {
        Future<Uint256> everyoneSteps = contract.everyoneStepsDate(new Utf8String(formattedDate));

        int steps = 0;
        try {
            steps = everyoneSteps.get().getValue().intValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        int index = day * -1;

        int avgSteps;
        if (peopleCount.get(index) != 0) avgSteps = steps / peopleCount.get(index);
        else avgSteps = 0;

        //Log.i("--All", "Everyone Steps = " + i);

        if (day == 0 ) avgPeopleSteps0.setText(avgSteps+"");
        if (day == -1 ) avgPeopleSteps1.setText(avgSteps+"");
        if (day == -2 ) avgPeopleSteps2.setText(avgSteps+"");
        if (day == -3 ) avgPeopleSteps3.setText(avgSteps+"");
        if (day == -4 ) avgPeopleSteps4.setText(avgSteps+"");
        if (day == -5 ) avgPeopleSteps5.setText(avgSteps+"");
        if (day == -6 ) avgPeopleSteps6.setText(avgSteps+"");
    }

    public void loadPeopleCount(int day, String formattedDate) {
        Future<Uint256> peopleAllCount = contract.countAllPeopleDate(new Utf8String(formattedDate));

        try {
            if (day == 0 ) peopleCount0.setText(peopleAllCount.get().getValue()+"");
            if (day == -1 ) peopleCount1.setText(peopleAllCount.get().getValue()+"");
            if (day == -2 ) peopleCount2.setText(peopleAllCount.get().getValue()+"");
            if (day == -3 ) peopleCount3.setText(peopleAllCount.get().getValue()+"");
            if (day == -4 ) peopleCount4.setText(peopleAllCount.get().getValue()+"");
            if (day == -5 ) peopleCount5.setText(peopleAllCount.get().getValue()+"");
            if (day == -6 ) peopleCount6.setText(peopleAllCount.get().getValue()+"");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        int index = day * -1;

        try {
            peopleCount.add(index,peopleAllCount.get().getValue().intValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (day == -6){
            for (int j=0; j>=-6; j--) {
                //getSteps(i);
                //makeEthCall(j, "getEveryoneSteps", MyApplication.everyoneStepsDate);
            }
        }
    }

    public void loadDatesAndSteps(int day, String formattedDate) {

        //Load My Steps
        Future<Uint256> mySteps = contract.recallMySteps(new Utf8String(formattedDate));
        try {
            Log.i("--All", "My Steps: " + mySteps.get().getValue());

            Calendar calendar2 = Calendar.getInstance();
            calendar2.add(Calendar.DAY_OF_YEAR, day);
            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd");
            String formattedDate2 = format2.format(calendar2.getTime());

            if (day == 0) {
                //todayStepsBig.setText(i+"");
                day0Steps.setText(mySteps.get().getValue()+"");
                day0Title.setText(formattedDate2);
            }

            if (day == -1) {
                day1Steps.setText(mySteps.get().getValue()+"");
                day1Title.setText(formattedDate2);
            }
            if (day == -2) {
                day2Steps.setText(mySteps.get().getValue()+"");
                day2Title.setText(formattedDate2);
            }
            if (day == -3) {
                day3Steps.setText(mySteps.get().getValue()+"");
                day3Title.setText(formattedDate2);
            }
            if (day == -4) {
                day4Steps.setText(mySteps.get().getValue()+"");
                day4Title.setText(formattedDate2);
            }
            if (day == -5) {
                day5Steps.setText(mySteps.get().getValue()+"");
                day5Title.setText(formattedDate2);
            }
            if (day == -6) {
                day6Steps.setText(mySteps.get().getValue()+"");
                day6Title.setText(formattedDate2);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
