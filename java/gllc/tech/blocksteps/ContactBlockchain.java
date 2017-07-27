package gllc.tech.blocksteps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by bhangoo on 7/26/2017.
 */

public class ContactBlockchain extends AsyncTask <Object, Void, JSONRPC2Response> {

    String method;
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
            Log.i("--All", "Successful Server Response: " + response.getResult());

            if (method.equals("personal_newAccount")) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("ethAddress",response.getResult().toString());
                editor.commit();
            }
        }
        else
            Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());
    }
}
