package gllc.tech.blocksteps.Sensor;

/**
 * Created by bhangoo on 7/30/2017.
 */

// Will listen to step alerts
public interface StepListener {

    public void step(long timeNs);

}
