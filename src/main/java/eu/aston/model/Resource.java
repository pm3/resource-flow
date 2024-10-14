package eu.aston.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.micronaut.core.annotation.Introspected;

@Introspected
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = JobResource.class, name = "JobResource"),
    @JsonSubTypes.Type(value = SingleResource.class, name = "SingleResource"),
    @JsonSubTypes.Type(value = MultiResource.class, name = "MultiResource")
})
public abstract class Resource {
    private String name;
    private String kind;
    private String auth;
    private List<ResourceParam> params;
    private List<ResourceFile> files;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public List<ResourceParam> getParams() {
        return params;
    }

    public void setParams(List<ResourceParam> params) {
        this.params = params;
    }

    public List<ResourceFile> getFiles() {
        return files;
    }

    public void setFiles(List<ResourceFile> files) {
        this.files = files;
    }

}
