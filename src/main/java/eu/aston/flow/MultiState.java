package eu.aston.flow;

import java.io.File;
import java.util.List;

import eu.aston.model.MultiResource;

public class MultiState extends BaseState {

    private final MultiResource multiResource;
    private final List<SingleState> items;
    private int currentInstances = 0;
    private int lastStartedIndex = 0;

    MultiState(MultiResource multiResource, StateStore stateStore) {
        super(multiResource, stateStore.getFlowRunner(), stateStore.getParamsBuilder(), new File(stateStore.getBaseDir(), multiResource.getName()), stateStore.getConfigStore());
        this.multiResource = multiResource;
        this.items = multiResource.getItems().stream().map(item -> (SingleState)stateStore.addResource(multiResource.getName(),item)).toList();
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
        this.currentInstances = currentInstances;
        this.checkState();
    }

    @Override
    public void checkState() {
        int aktRunning = 0;
        for(SingleState item: items) {
            item.checkState();
            aktRunning += item.getRunning();
        }

        for(int i=lastStartedIndex; i<lastStartedIndex+items.size(); i++) {
            if(aktRunning<currentInstances) {
                SingleState item = items.get(i%items.size());
                if(item.getRunning()==0) {
                    item.setRunning(true);
                    aktRunning++;
                    lastStartedIndex = i%items.size();
                }
            }
        }
        for(int i=lastStartedIndex+1; i<lastStartedIndex+1+items.size(); i++) {
            if(aktRunning>currentInstances) {
                SingleState item = items.get(i%items.size());
                if(item.getRunning()==1) {
                    item.setRunning(false);
                    aktRunning--;
                    lastStartedIndex = i%items.size();
                }
            }
        }
    }

    @Override
    public int getRunning() {
        return items.stream().mapToInt(SingleState::getRunning).sum();
    }   

}
