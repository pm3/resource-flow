package eu.aston.flow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        List<Resource> resources = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(configDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yaml"))
                    .forEach(path -> {
                        try {
                            Resource resource = loadResource(path);
                            resources.add(resource);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
        return resources;
    }

    private Resource loadResource(Path path) throws IOException {
        String content = Files.readString(path);
        Resource resource = mapper.readValue(content, Resource.class);
        // validate resource
        if (resource.getName() == null || resource.getName().isEmpty()) {
            throw new IllegalArgumentException("Názov zdroja nemôže byť prázdny");
        }
        if (resource.getParams() == null) {
            resource.setParams(new ArrayList<>());
        }
        validParams(resource.getParams());
        if (resource instanceof JobResource jobResource) {
            if (jobResource.getStart() == null || jobResource.getStart().isEmpty()) {
                throw new IllegalArgumentException("Skript start musí byť definovaný pre zdroj typu JOB");
            }
            if (jobResource.getMaxConcurrentOperations() <= 0) {
                jobResource.setMaxConcurrentOperations(1);
            }
        } else if (resource instanceof SingleResource singleResource) {
            validSingle(singleResource);
        } else if (resource instanceof MultiResource multiResource) {
            if (multiResource.getItems() == null || multiResource.getItems().size() < 2) {
                throw new IllegalArgumentException("V zdroji typu MULTI musí byť definovaných aspoň 2 položky");
            }
            for (MultiItem item : multiResource.getItems()) {
                if (item.getName() == null || item.getName().isEmpty()) {
                    throw new IllegalArgumentException("Názov položky v zdroji typu MULTI nesmie byť prázdny");
                }
                if (item.getParams() == null) {
                    item.setParams(new ArrayList<>());
                }
                validParams(item.getParams());
            }
        } else {
            throw new IllegalArgumentException("Neznámy typ zdroja: " + resource.getKind());
        }
        resources.put(resource.getName(), resource);
        return resource;
    }

    private void validSingle(SingleResource singleResource) {
        if (singleResource.getStart() == null || singleResource.getStart().isEmpty()) {
            throw new IllegalArgumentException("Skript start musí byť definovaný pre zdroj typu SINGLE");
        }
        if (singleResource.getStop() == null || singleResource.getStop().isEmpty()) {
            throw new IllegalArgumentException("Skript stop musí byť definovaný pre zdroj typu SINGLE");
        }
        if (singleResource.getFiles() == null) {
            singleResource.setFiles(new ArrayList<>());
        }
        for (ResourceFile file : singleResource.getFiles()) {
            if (file.name() == null || file.name().isEmpty()) {
                throw new IllegalArgumentException("Názov súboru musí byť definovaný");
            }
            if (file.content() == null || file.content().isEmpty()) {
                throw new IllegalArgumentException("Obsah súboru musí byť definovaný");
            }
        }
    }

    private void validParams(List<ResourceParam> params) {
        for (ResourceParam param : params) {
            if (param.name() == null || param.name().isEmpty()) {
                throw new IllegalArgumentException("Názov parametru nesmie byť prázdny");
            }
            if (param.value() == null && param.secret()==null && param.configMap()==null) {
                throw new IllegalArgumentException("Hodnota parametru nesmie byť prázdna");
            }
        }
    }
}
