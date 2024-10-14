package eu.aston.flow;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.aston.model.MultiResource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;

public class MultiState extends BaseState {

    private final MultiResource multiResource;
    private int minInstances = 0;
    private int currentInstances = 0;
    private final boolean[] running;
    private int lastStarted = 0;
    private final List<Map<String, String>> itemsParams;

    public MultiState(MultiResource multiResource, FlowRunner flowRunner, ParamsBuilder paramsBuilder, File baseDir, ConfigStore configStore) {
        super(multiResource, flowRunner, paramsBuilder, new File(baseDir, multiResource.getName()), configStore);
        this.multiResource = multiResource;
        this.running = new boolean[multiResource.getItems().size()];
        this.itemsParams = multiResource.getItems().stream().map(item -> paramsBuilder.build(item.getParams())).toList();
    }   

    public void setMinInstances(int minInstances, boolean up) {
        if(minInstances>multiResource.getItems().size()) {
            throw new IllegalArgumentException("minInstances cannot be greater than maxInstances");
        }
        if(minInstances==this.minInstances) {
            return;
        }
        if (up && minInstances <= this.minInstances) {
            return;
        }
        if (currentInstances < minInstances) {
            this.minInstances = minInstances;
            this.currentInstances = minInstances;
            checkState();
        }
    }

    public void setCurrentInstances(int currentInstances, boolean up) {
        if(currentInstances>multiResource.getItems().size()) {
            throw new IllegalArgumentException("currentInstances cannot be greater than maxInstances");
        }
        if(currentInstances==this.currentInstances) {
            return;
        }
        if(currentInstances<=this.currentInstances && up) {
            return;
        }
        if(currentInstances<this.minInstances) {
            return;
        }
        this.currentInstances = currentInstances;
        this.checkState();
    }

	private void checkState() {
        int aktRunning = 0;
        for(int i=lastStarted; i<lastStarted+running.length; i++) {
            int index = i%running.length;
            Map<String, String> parameters = cmd(index, "check");
            if(SingleState.checkStateVariable(parameters, running[index])) {
                aktRunning++;
            }
        }
        if(aktRunning<minInstances) {
            int start = lastStarted;
            for(int i=start; i<start+running.length; i++) {
                int index = i%running.length;
                if(!running[index] && aktRunning<currentInstances) {
                    running[index] = true;
                    cmd(index, "start");
                    aktRunning++;
                    lastStarted = index;
                }
            }
        }
        if(aktRunning>minInstances) {
            int start = lastStarted+running.length-1;
            for(int i=start; i<start-running.length; i--) {
                int index = i%running.length;
                if(running[index] && aktRunning>currentInstances) {
                    running[index] = false;
                    cmd(index, "stop");
                    aktRunning--;
                }
            }
        }
    }

    private Map<String, String> cmd(int index, String script) {
        Map<String, String> parameters = new HashMap<>();
        parameters.putAll(getParams());
        parameters.putAll(itemsParams.get(index));
        runNow(script, parameters);
        return parameters;
    }

    @Override
    public int getRunning() {
        return currentInstances;
    }   
}
