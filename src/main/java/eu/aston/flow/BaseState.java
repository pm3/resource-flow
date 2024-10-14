package eu.aston.flow;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import eu.aston.model.JobResource;
import eu.aston.model.Resource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;

public abstract class BaseState {

    private static final String AUTH_ENV = "AUTH_ENV:";
    protected final Resource resource;
    protected final FlowRunner flowRunner;
    protected final File workDir;
    protected final ConfigStore configStore;
    protected final Map<String, String> params;

    public BaseState(Resource resource, FlowRunner flowRunner, ParamsBuilder paramsBuilder, File workDir, ConfigStore configStore) {
        this.resource = resource;
        this.flowRunner = flowRunner;
        this.workDir = workDir;
        this.configStore = configStore;
        this.params = paramsBuilder.build(resource.getParams());
    }

    public Map<String, String> getParams() {
        return params;
    }

    private long lastAuth = 0L;
    private Map<String, String> authEnv;

    public void runNow(String script, Map<String, String> parameters){
        if(resource.getAuth()!=null && lastAuth<System.currentTimeMillis()-Duration.ofHours(24).toMillis()){
            if(configStore.getResource(resource.getAuth()) instanceof JobResource jobResource){
                lastAuth = System.currentTimeMillis();
                runAuth(jobResource);
            }
        }
        flowRunner.run(resource.getName(), script, parameters, workDir, resource.getFiles());
    }

    private void runAuth(JobResource jobResource) {
        Map<String, String> authEnvParams = new HashMap<>(params);
        flowRunner.run(resource.getName(), jobResource.getStart(), authEnvParams, workDir, jobResource.getFiles());
        authEnv = new HashMap<>();
        for(Map.Entry<String, String> entry : authEnvParams.entrySet()){
            if(entry.getKey().startsWith(AUTH_ENV)){
                authEnv.put("env."+entry.getKey().substring(AUTH_ENV.length()), entry.getValue());
            }
        }
    }

    public abstract int getRunning();

    public String getName() {
        return resource.getName();
    }

    public String getType() {
        return resource.getClass().getSimpleName();
    }
}
