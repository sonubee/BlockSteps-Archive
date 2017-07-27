package gllc.tech.blocksteps;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
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

// The Client sessions package
// The Base package for representing JSON-RPC 2.0 messages
// The JSON Smart package for JSON encoding/decoding (optional)
// For creating URLs

public class SendPush {

    public SendPush(final Context context) {
        new AsyncCaller().execute();
    }

    private class AsyncCaller extends AsyncTask<Void, Void, JSONRPC2Response>
    {

        @Override
        protected JSONRPC2Response doInBackground(Void... nothing) {

            // The JSON-RPC 2.0 server URL
            URL serverURL = null;

            try {
                serverURL = new URL("http://45.55.4.74:8545");

            } catch (MalformedURLException e) {
                System.err.println(e.getMessage());
                // handle exception...
            }

// Create new JSON-RPC 2.0 client session
            JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

            // Construct new request
            //String method = "web3_clientVersion";
            //String method = "personal_newAccount";
            String method = "eth_call";
            int requestID = 0;

            Map params = new HashMap();
            params.put("from", "0xc76961cc78c3d55870d5f37e2ee4936cbc7bba8b");
            params.put("to","0xe253ded8ccc15b8b2cae509f6381841fe33de03c");
            params.put("data","0x5c6718730000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000830372f32342f3137000000000000000000000000000000000000000000000000");


            //List<Object> params = new ArrayList<>();
            //params.add("hellya");

            List<Object> params3 = new ArrayList<>();
            params3.add(params);
            params3.add("latest");

            JSONRPC2Request request = new JSONRPC2Request(method, params3, requestID);
            //JSONRPC2Request request = new JSONRPC2Request(method, requestID);


            // Send request
            JSONRPC2Response response = null;

            try {
                response = mySession.send(request);

            } catch (JSONRPC2SessionException e) {

                Log.e("--All", "Error Sending Request: " + e.getMessage());
                // handle exception...
            }

            return response;

        }

        protected void onPostExecute(JSONRPC2Response response) {

            // Print response result / error
            if (response.indicatesSuccess())
                Log.i("--All","Successful Server Response: " + response.getResult());

            else
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());

        }
    }

}
