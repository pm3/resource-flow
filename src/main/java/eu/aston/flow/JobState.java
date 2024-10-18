package eu.aston.flow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.aston.model.JobResource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;

public class JobState extends BaseState {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobState.class);

    private final JobResource jobResource;
    private final Executor executor;
    private final Semaphore semaphore;

    public JobState(JobResource jobResource, Executor executor, FlowRunner flowRunner, ParamsBuilder paramsBuilder, File baseDir, ConfigStore configStore) {
        super(jobResource, flowRunner, paramsBuilder, new File(baseDir, jobResource.getName()), configStore);
        this.jobResource = jobResource;
        this.executor = executor;
        this.semaphore = new Semaphore(jobResource.getMaxConcurrentOperations());
    }

    public void run(Map<String, String> runParameters) {
        if(jobResource.isPendingIfNoCapacity() && semaphore.availablePermits()==0){
            LOGGER.info("pending job {}", jobResource.getName());
            return;
        }
        if(semaphore.tryAcquire()) {
            executor.execute(() -> {
                try {
                    Map<String, String> parameters = new HashMap<>(getParams());
                    if(runParameters!=null) parameters.putAll(runParameters);
                    runNow(jobResource.getStart(), parameters);
                } finally {
                    semaphore.release();
                }
            });
        }
    }

    @Override
    public void checkState() {
    }

    @Override
    public int getRunning() {
        return semaphore.availablePermits();
    }

    public static JobState factory(JobResource jobResource, StateStore stateStore) {
        return new JobState(jobResource, stateStore.getExecutor(), stateStore.getFlowRunner(), stateStore.getParamsBuilder(), stateStore.getBaseDir(), stateStore.getConfigStore());
    }   
}
