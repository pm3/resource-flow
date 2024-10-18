package eu.aston.flow;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import eu.aston.model.JobResource;
import eu.aston.model.MultiResource;
import eu.aston.model.Resource;
import eu.aston.model.SingleResource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;

public class StateStore {

    private final FlowRunner flowRunner;
    private final ParamsBuilder paramsBuilder;
    private final ConfigStore configStore;
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();
    private final File baseDir;

    private final Map<String, BaseState> states = new ConcurrentHashMap<>();

    public StateStore(FlowRunner flowRunner, ParamsBuilder paramsBuilder, ConfigStore configStore, File baseDir) {
        this.flowRunner = flowRunner;
        this.paramsBuilder = paramsBuilder;
        this.configStore = configStore;
        this.baseDir = baseDir;
    }

    public BaseState getState(String name) {
        return states.get(name);
    }

public void checkState() {
        for(BaseState state : states.values()) {
            state.checkState();
        }
    }

    public FlowRunner getFlowRunner() {
        return flowRunner;
    }

    public ParamsBuilder getParamsBuilder() {
        return paramsBuilder;
    }

    public ConfigStore getConfigStore() {
        return configStore;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public BaseState addResource(String parent,Resource resource) {
        BaseState state = null;
        if (resource instanceof JobResource jobResource) {
            state = new JobState(jobResource, executor, flowRunner, paramsBuilder, baseDir, configStore);
        } else if (resource instanceof SingleResource singleResource) {
            state = new SingleState(singleResource, flowRunner, paramsBuilder, baseDir, configStore);
        } else if (resource instanceof MultiResource multiResource) {
            state = new MultiState(multiResource, this);
        } else {
            throw new IllegalArgumentException("Unsupported resource type: " + resource.getClass().getName());
        }
        if(parent!=null) {
            states.put(parent+":"+resource.getName(), state);

        } else {
            states.put(resource.getName(), state);
        }
        return state;
    }

    
}
