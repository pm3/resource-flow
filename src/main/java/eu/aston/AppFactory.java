package eu.aston;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.aston.flow.ConfigStore;
import eu.aston.flow.StateStore;
import eu.aston.model.Resource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import jakarta.inject.Singleton;

@Factory
public class AppFactory {

    @Singleton
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

    @Singleton
    public StateStore stateStore(FlowRunner flowRunner, ParamsBuilder paramsBuilder, FlowConfig flowConfig, ScheduledExecutorTaskScheduler scheduledExecutorTaskScheduler) throws IOException {
        ConfigStore configStore = new ConfigStore();
        List<Resource> resources = configStore.loadResources(flowConfig.configDir().toPath());
        StateStore stateStore = new StateStore(flowRunner, paramsBuilder, configStore, flowConfig.baseDir());
        for(Resource resource : resources) {
            stateStore.addResource(null, resource);
        }
        //Duration duration = Duration.ofSeconds(30);
        //scheduledExecutorTaskScheduler.scheduleAtFixedRate(duration, duration, stateStore::checkState);
        return stateStore;
    }
}
