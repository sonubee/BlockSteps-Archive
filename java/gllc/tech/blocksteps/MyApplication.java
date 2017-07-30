package gllc.tech.blocksteps;

import android.app.Application;

/**
 * Created by bhangoo on 7/27/2017.
 */

public class MyApplication extends Application {

    public static final String contractAddress = "0xe919320e97e6d6a5874dbbae7a5e33cfd0c9fb09";
    public static final String mainEtherAddress = "0x4d5bcceba61400e52809a9e29eaccce328b4b43f";

    public static final String recallMySteps = "0x8fd2f1cd";
    public static final String countAllPeopleDate = "0x8c88af22";
    public static final String saveMySteps = "0xdc5e5c0f";

    public static String ethAddress = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
