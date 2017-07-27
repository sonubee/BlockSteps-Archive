package gllc.tech.blocksteps;

import android.app.Application;

/**
 * Created by bhangoo on 7/27/2017.
 */

public class MyApplication extends Application {

    public static final String contractAddress = "0xe253ded8ccc15b8b2cae509f6381841fe33de03c";
    public static final String mainEtherAddress = "0x4d5bcceba61400e52809a9e29eaccce328b4b43f";
    public static String ethAddress = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
