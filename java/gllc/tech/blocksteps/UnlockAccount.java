package gllc.tech.blocksteps;

import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.R.attr.id;

/**
 * Created by bhangoo on 8/19/2017.
 */
/*
public class UnlockAccount extends AsyncTask<Object, Void, JSONRPC2Response> {

    public UnlockAccount() {

    }

    @Override
    protected JSONRPC2Response doInBackground(Object... objects) {

        Object params = objects[0];

        URL serverURL = null;
        try {serverURL = new URL("http://45.55.4.74:8545");}
        catch (MalformedURLException e) {Log.e("--All", "Error in creating URL: " + e.getMessage());}

        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        if (params instanceof List<?>){
            List<Object> castedParams = (List<Object>)params;
            request = new JSONRPC2Request("personal_unlockAccount", castedParams, id);
        } else if (params instanceof Map<?, ?>) {
            Map<String, Object> castedParams = (Map<String, Object>) params;
            request = new JSONRPC2Request("personal_unlockAccount", castedParams, id);
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

            }

            else{
                Log.e("--All", "Error in PostExecute: " + response.getError().getMessage());

                String id = sharedPref.getString("uniqueId","NA");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference(id);
                myRef.child("Error").push().setValue(response.getError().toString() + " - In MainActivity");
                Crashlytics.logException(response.getError());
            }

        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.logException(e);
        }

}
*/