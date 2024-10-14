package eu.aston;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.aston.flow.ConfigStore;
import eu.aston.flow.StateStore;
import eu.aston.model.Resource;
import eu.aston.utils.FlowRunner;
import eu.aston.utils.ParamsBuilder;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class AppFactory {

    @Singleton
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

    @Singleton
    public StateStore stateStore(FlowRunner flowRunner, ParamsBuilder paramsBuilder) throws IOException {
        File configDir = new File("config");
        File baseDir = new File("data");
        ConfigStore configStore = new ConfigStore();
        List<Resource> resources = configStore.loadResources(configDir.toPath());
        StateStore stateStore = new StateStore(flowRunner, paramsBuilder, configStore, baseDir);
        for(Resource resource : resources) {
            stateStore.addResource(resource);
        }   
        return stateStore;
    }
}
