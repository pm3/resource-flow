package eu.aston.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class JobResource extends Resource {
    private String start;
    private int maxConcurrentOperations;
    private boolean pendingIfNoCapacity;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public int getMaxConcurrentOperations() {
        return maxConcurrentOperations;
    }

    public void setMaxConcurrentOperations(int maxConcurrentOperations) {
        this.maxConcurrentOperations = maxConcurrentOperations;
    }

    public boolean isPendingIfNoCapacity() {
        return pendingIfNoCapacity;
    }

    public void setPendingIfNoCapacity(boolean pendingIfNoCapacity) {
        this.pendingIfNoCapacity = pendingIfNoCapacity;
    }
}
