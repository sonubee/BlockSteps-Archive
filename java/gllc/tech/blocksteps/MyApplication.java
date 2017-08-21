package gllc.tech.blocksteps;

import android.app.Application;

import static gllc.tech.blocksteps.MainActivity.getHardwareId;

/**
 * Created by bhangoo on 7/27/2017.
 */

public class MyApplication extends Application {

    public static final String contractAddress = "0x481791ccfdcaa1dc0547fdfcd92b5cd288c8634e";
    public static final String contractAddress2 = "0xf668824598137732f30621c044fb0965ff10e16e";
    public static final String mainEtherAddress = "0x4d5bcceba61400e52809a9e29eaccce328b4b43f";

    public static final String recallMySteps = "0x8fd2f1cd";
    public static final String countAllPeopleDate = "0x8c88af22";
    public static final String saveMySteps = "0xdc5e5c0f";
    public static final String everyoneStepsDate = "0x7a725806";

    public static String ethAddress = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
