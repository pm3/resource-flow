package eu.aston.flow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import eu.aston.model.SingleResource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleState extends BaseState {
    private final static Logger LOGGER = LoggerFactory.getLogger(SingleState.class);

    private final SingleResource singleResource; 
    private boolean running = false;
    private boolean lastState = false;

    public SingleState(SingleResource singleResource, FlowRunner flowRunner, ParamsBuilder paramsBuilder, File baseDir, ConfigStore configStore) {
        super(singleResource, flowRunner, paramsBuilder, new File(baseDir, singleResource.getName()), configStore);
        this.singleResource = singleResource;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
        checkState();
    }

	public void checkState() {
        boolean actualState = lastState;
        if(singleResource.getCheck() != null) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getCheck(), parameters);
            actualState = checkStateVariable(parameters, actualState);
        }
        if(this.running && !actualState) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getStart(), parameters);
            this.lastState = true;
        } else if(!this.running && actualState) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getStop(), parameters);
            this.lastState = false;
        }
	}

    @Override
    public int getRunning() {
        return running ? 1 : 0;
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
