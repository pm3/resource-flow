package eu.aston.flow;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.aston.model.SingleResource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;

public class SingleState extends BaseState {
    private final static Logger LOGGER = LoggerFactory.getLogger(SingleState.class);

    private final SingleResource singleResource;
    private boolean expectedState = false;
    private boolean aktState = false;
    private long lastWatchdog = 0;
    private static final long WATCHDOG_EXPIRE = Duration.ofSeconds(60).toMillis();
    private long lastCheck = 0;
    private static final long CHECK_INTERVAL = Duration.ofSeconds(300).toMillis();

    public SingleState(SingleResource singleResource, FlowRunner flowRunner, ParamsBuilder paramsBuilder, File baseDir, ConfigStore configStore) {
        super(singleResource, flowRunner, paramsBuilder, new File(baseDir, singleResource.getName()), configStore);
        this.singleResource = singleResource;
    }

    public void setRunning(boolean running) {
        this.expectedState = running;
        checkState();
    }

    public void setWatchdog() {
        this.lastWatchdog = System.currentTimeMillis();
    }

    @Override
	public void checkState() {
        long now = System.currentTimeMillis();
        if(singleResource.getCheck() != null && lastWatchdog < now - WATCHDOG_EXPIRE && lastCheck < now - CHECK_INTERVAL) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getCheck(), parameters);
            this.lastCheck = now;
            this.aktState = checkStateVariable(parameters, aktState);
        }
        if(this.expectedState && !aktState) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getStart(), parameters);
            this.aktState = expectedState;
            this.lastCheck = now;
            this.lastWatchdog = now+WATCHDOG_EXPIRE*3;
        } else if(!this.expectedState && aktState) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getStop(), parameters);
            this.aktState = expectedState;
            this.lastCheck = now;
            this.lastWatchdog = now+WATCHDOG_EXPIRE*3;
        }
	}

    @Override
    public int getRunning() {
        return aktState ? 1 : 0;
    }

    public static boolean checkStateVariable(Map<String, String> parameters, boolean defaultState) {
        String strState = parameters.get("state");
        if(parameters.get("state") !=null) {
            String runningStates = parameters.getOrDefault("running_states", "running,starting");
            LOGGER.info("compare state {} with {}", strState.toLowerCase(), runningStates);
            defaultState = runningStates.contains(strState.toLowerCase());
        }
        return defaultState;
    }

}
