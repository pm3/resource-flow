package eu.aston.model;

import java.util.List;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class MultiItem {
    private String name;
    private List<ResourceParam> params;
    private String start;
    private String stop;
    private String check;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResourceParam> getParams() {
        return params;
    }

    public void setParams(List<ResourceParam> params) {
        this.params = params;
    }

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
}
