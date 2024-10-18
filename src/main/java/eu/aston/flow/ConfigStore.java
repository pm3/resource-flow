package eu.aston.flow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.aston.model.JobResource;
import eu.aston.model.MultiItem;
import eu.aston.model.MultiResource;
import eu.aston.model.Resource;
import eu.aston.model.ResourceFile;
import eu.aston.model.ResourceParam;
import eu.aston.model.SingleResource;

public class ConfigStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStore.class);

    private final Map<String, Resource> resources = new HashMap<>();
    private final ObjectMapper mapper;

    public Resource getResource(String name) {
        return resources.get(name);
    }

    public ConfigStore() {
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.registerModule(new JavaTimeModule());
    }

    public List<Resource> loadResources(Path configDir) throws IOException {
        List<Resource> resourceList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(configDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yaml"))
                    .forEach(path -> {
                        try {
                            Resource resource = loadResource(path);
                            resources.put(resource.getName(), resource);
                            resourceList.add(resource);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
        return resourceList;
    }

    private Resource loadResource(Path path) throws IOException {
        String content = Files.readString(path);
        Resource resource = mapper.readValue(content, Resource.class);
        LOGGER.info("Loaded resource: {} -> {}", path, resource.getName());
        // validate resource
        if (resource.getName() == null || resource.getName().isEmpty()) {
            throw new IllegalArgumentException("Resource name cannot be empty");
        }
        if (!resource.getName().matches("^[a-zA-Z0-9_\\-]+$")) {
            throw new IllegalArgumentException("Resource name can only contain letters, numbers and underscores");
        }
        if (resource.getParams() == null) {
            resource.setParams(new ArrayList<>());
        }
        validParams(resource.getParams());
        if (resource instanceof JobResource jobResource) {
            if (jobResource.getStart() == null || jobResource.getStart().isEmpty()) {
                throw new IllegalArgumentException("Start script must be defined for JOB type resource");
            }
            if (jobResource.getMaxConcurrentOperations() <= 0) {
                jobResource.setMaxConcurrentOperations(1);
            }
        } else if (resource instanceof SingleResource singleResource) {
            if (singleResource.getStart() == null || singleResource.getStart().isEmpty()) {
                throw new IllegalArgumentException("Start script must be defined for SINGLE type resource");
            }
        
            if (singleResource.getStop() == null || singleResource.getStop().isEmpty()) {
                throw new IllegalArgumentException("Stop script must be defined for SINGLE type resource");
            }
            validFiles(singleResource);
        } else if (resource instanceof MultiResource multiResource) {
            if (multiResource.getItems() == null || multiResource.getItems().size() < 2) {
                throw new IllegalArgumentException("At least 2 items must be defined in MULTI type resource");
            }
            for (MultiItem item : multiResource.getItems()) {
                if (item.getName() == null || item.getName().isEmpty()) {
                    throw new IllegalArgumentException("Item name in MULTI type resource cannot be empty");
                }
                if(item.getStart()==null && multiResource.getStart()!=null) {
                    item.setStart(multiResource.getStart());
                }
                if(item.getStop()==null && multiResource.getStop()!=null) {
                    item.setStop(multiResource.getStop());
                }
                if(item.getCheck()==null && multiResource.getCheck()!=null) {
                    item.setCheck(multiResource.getCheck());
                }
                if (item.getStart() == null || item.getStart().isEmpty()) {
                    throw new IllegalArgumentException("Start script must be defined for MULTI["+item.getName()+"] type resource");
                }
            
                if (item.getStop() == null || item.getStop().isEmpty()) {
                    throw new IllegalArgumentException("Stop script must be defined for MULTI["+item.getName()+"] type resource");
                }

                if (item.getParams() == null) {
                    item.setParams(new ArrayList<>());
                }
                item.getParams().addAll(multiResource.getParams());
                validParams(item.getParams());
            }
            validFiles(multiResource);
        } else {
            throw new IllegalArgumentException("Unknown resource type: " + resource.getKind());
        }
        resources.put(resource.getName(), resource);
        return resource;
    }

    private void validFiles(Resource singleResource) {
        if (singleResource.getFiles() == null) {
            singleResource.setFiles(new ArrayList<>());
        }
        for (ResourceFile file : singleResource.getFiles()) {
            if (file.name() == null || file.name().isEmpty()) {
                throw new IllegalArgumentException("File name must be defined");
            }
            if (file.content() == null || file.content().isEmpty()) {
                throw new IllegalArgumentException("File content must be defined");
            }
        }
    }

    private void validParams(List<ResourceParam> params) {
        for (ResourceParam param : params) {
            if (param.name() == null || param.name().isEmpty()) {
                throw new IllegalArgumentException("Parameter name cannot be empty");
            }
            if (param.value() == null && param.secret()==null && param.configMap()==null) {
                throw new IllegalArgumentException("Parameter value cannot be empty");
            }
        }
    }
}
