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

    public void addResource(Resource resource) {
        if (resource instanceof JobResource jobResource) {
            states.put(resource.getName(), new JobState(jobResource, executor, flowRunner, paramsBuilder, baseDir, configStore));
        } else if (resource instanceof SingleResource singleResource) {
            states.put(resource.getName(), new SingleState(singleResource, flowRunner, paramsBuilder, baseDir, configStore));
        } else if (resource instanceof MultiResource multiResource) {
            states.put(resource.getName(), new MultiState(multiResource, flowRunner, paramsBuilder, baseDir, configStore));
        }
    }
}
