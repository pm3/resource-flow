package eu.aston.controller;

import java.util.Map;

import eu.aston.flow.BaseState;
import eu.aston.flow.JobState;
import eu.aston.flow.MultiState;
import eu.aston.flow.SingleState;
import eu.aston.flow.StateStore;
import eu.aston.model.ResourceStateData;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;

@Controller("/resource-flow")
public class ResourceController {

    private final StateStore stateStore;

    public ResourceController(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @Get("/state/{name}")
    public ResourceStateData fetchResource(@PathVariable String name) {
        BaseState state = stateStore.getState(name);       
        if(state != null) {
            return new ResourceStateData(name, state.getType(), state.getRunning());
        }
        throw new HttpStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Post("/start/{name}")
    public void startJob(@PathVariable String name, @Body Map<String, String> body) {
        BaseState state = stateStore.getState(name);
        if(state instanceof JobState jobState) {
            jobState.run(body);
        } else {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Resource is not a job");
        }
    }

    @Post("/run/{name}")
    public void run(@PathVariable String name, @QueryValue("count") int count, @QueryValue("up") @Nullable Boolean up) {
        BaseState state = stateStore.getState(name);   
        if(state instanceof SingleState singleState) {
            singleState.setRunning(count > 0);
        } else if(state instanceof MultiState multiState) {
            multiState.setCurrentInstances(count, up != null ? up : false);
        } else {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Resource is not a single or multi");
        }
    }

    @Post("/watchdog/{name}")
    public void watchdog(@PathVariable String name) {
        BaseState state = stateStore.getState(name);
        if(state instanceof SingleState singleState) {
            singleState.setWatchdog();
        }
    }
}