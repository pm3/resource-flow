package eu.aston.flow;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.aston.model.JobResource;
import eu.aston.model.MultiResource;
import eu.aston.model.Resource;
import eu.aston.model.SingleResource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;

public class StateStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateStore.class);

    private final FlowRunner flowRunner;
    private final ParamsBuilder paramsBuilder;
    private final ConfigStore configStore;
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();
    private final File baseDir;

    private final Map<String, BaseState> states = new ConcurrentHashMap<>();
    private final Map<Class<? extends Resource>, IStateFactory> factories = new ConcurrentHashMap<>() {{
        put(JobResource.class, (resource, stateStore) -> JobState.factory((JobResource) resource, stateStore));
        put(SingleResource.class, (resource, stateStore) -> SingleState.factory((SingleResource) resource, stateStore));
        put(MultiResource.class, (resource, stateStore) -> MultiState.factory((MultiResource) resource, stateStore));
    }};

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

    public Executor getExecutor() {
        return executor;
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
        IStateFactory factory = Optional.ofNullable(factories.get(resource.getClass()))
            .orElseThrow(() -> new IllegalArgumentException("Unsupported resource type: " + resource.getClass().getName()));
        BaseState state = factory.create(resource, this);
        if(parent!=null) {
            states.put(parent+":"+resource.getName(), state);
            LOGGER.info("Added state: {} -> {}", parent+":"+resource.getName(), state.getName());
        } else {
            states.put(resource.getName(), state);
            LOGGER.info("Added state: {} -> {}", resource.getName(), state.getName());
        }
        return state;
    }
}
