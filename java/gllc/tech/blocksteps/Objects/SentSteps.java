package gllc.tech.blocksteps.Objects;

/**
 * Created by bhangoo on 8/16/2017.
 */

public class SentSteps {

    public String timeStamp;
    public int stepCount;
    public String deviceIdentifier;

    public SentSteps() {}

    public SentSteps(String timeStamp, int stepCount, String deviceIdentifier) {
        this.timeStamp = timeStamp;
        this.stepCount = stepCount;
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }
}
