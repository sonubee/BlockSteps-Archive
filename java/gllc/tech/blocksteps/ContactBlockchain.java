package gllc.tech.blocksteps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by bhangoo on 7/26/2017.
 */

public class ContactBlockchain extends AsyncTask <Object, Void, JSONRPC2Response> {

    String method;
    String extra;
    SharedPreferences sharedPref;

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

                List<Object> params3 = new ArrayList<>();

                Map params = new HashMap();
                params.put("from", MyApplication.ethAddress);
                params.put("to",MyApplication.contractAddress);
                params.put("data","0x5c6718730000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000830372f32342f3137000000000000000000000000000000000000000000000000");

                params3.add(params);

                //Log.i("--All", params.toString());

                new ContactBlockchain().execute("eth_sendTransaction",params3,99, sharedPref,"sentSteps");
            }

            if (method.equals("eth_sendTransaction") && extra.equals("sentSteps")) {
                Log.i("--All", "Successfully Sent Steps");
            }

        }
        else
            Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
    }
}
