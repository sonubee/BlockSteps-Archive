package gllc.tech.blocksteps;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;

/**
 * Created by bhangoo on 8/11/2017.
 */

public class BootStepService extends IntentService {

    public BootStepService() {
        super("test-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Extract the receiver passed into the service
        ResultReceiver rec = intent.getParcelableExtra("receiver");
        // Extract additional values from the bundle
        String val = intent.getStringExtra("foo");
        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString("resultValue", "My Result Value. Passed in: " + val);
        // Here we call send passing a resultCode and the bundle of extras
        rec.send(Activity.RESULT_OK, bundle);
    }
}
