package hotspotsensor.benchmark;

import hotspotsensor.HotspotSensor;

import java.util.concurrent.TimeUnit;

/**
 * @author lotus.jzx
 */
public class EmitTask implements Runnable {


    private int intervalMillis;

    private HotspotSensor<String> hotspotSensor;

    private StringEmitter emitter;

    private int totalTimes;

    public volatile boolean stop;

    public EmitTask(HotspotSensor<String> hotspotSensor, StringEmitter emitter, int intervalMillis, int totalTimes) {
        this.hotspotSensor = hotspotSensor;
        this.intervalMillis = intervalMillis;
        this.emitter = emitter;
        this.totalTimes = totalTimes;
        this.stop = false;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                String element = emitter.emit();
                hotspotSensor.increase(element);

                if (totalTimes == 0) {
                    break;
                } else if (totalTimes > 0) {
                    totalTimes--;
                }

                if (intervalMillis > 0) {
                    TimeUnit.MILLISECONDS.sleep(intervalMillis);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.stop = true;
    }
}
