package eu.aston.model;

import java.util.List;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class MultiResource extends Resource {
    private String start;
    private String stop;
    private String check;
    private List<MultiItem> items;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public List<MultiItem> getItems() {
        return items;
    }

    public void setItems(List<MultiItem> items) {
        this.items = items;
    }   
}
