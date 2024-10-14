package eu.aston.model;

import java.util.List;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class MultiItem {
    private String name;
    private List<ResourceParam> params;

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
}
