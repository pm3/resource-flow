package eu.aston.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.aston.model.ResourceParam;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ParamsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParamsBuilder.class);

    private final ExecProcess execProcess;
    private final Map<String, String> kubeData = new HashMap<>();
    private final String defaultNamespace;
    private final ObjectMapper objectMapper;

    public ParamsBuilder(ObjectMapper objectMapper, ExecProcess execProcess) throws IOException{
        this.objectMapper = objectMapper;
        this.execProcess = execProcess;
        this.defaultNamespace = defaultNamespace();
    }

    public Map<String, String> build(List<ResourceParam> itemsParams) {
        Map<String, String> params = new HashMap<>();
        for (ResourceParam param : itemsParams) {
            String value = param.value();
            if(value==null && param.secret()!=null) {
                value = kubeData("secret", param.secret());
            }
            if(value==null && param.configMap()!=null) {
                value = kubeData("configMap", param.configMap());
            }
            if(value!=null) {
                params.put(param.name(), value);   
            }
        }
        return params;
    }

    public String kubeData(String type, String ref) {
        String[] parts = ref.split("/");
        if(parts.length<2 || parts.length>3) {
            throw new IllegalArgumentException("Invalid reference: " + type + " " + ref+" use format [namespace/]name/property");
        }
        String namespace = parts.length==3 ? parts[0] : defaultNamespace;
        String name = parts.length==3 ? parts[1] : parts[0];
        String property = parts.length==3 ? parts[2] : parts[1];

        if(kubeData.containsKey(namespace+"/"+name+"/"+property)) {
            loadKubeData(type,namespace,name);
        }
        return kubeData.get(namespace+"/"+name+"/"+property);
    }

    private String defaultNamespace() throws IOException {
        File file = new File("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        if(file.exists()) {
            return  Files.readString(file.toPath());
        }
        return "default";

    }

    private void loadKubeData(String type, String namespace, String secretName) {
        try {
            String json = execProcess.execBuilder(new File("."), 
            List.of("kubectl", "get", type, "-n", namespace, secretName, "-o", "json"));
            JsonNode jsonNode = objectMapper.readTree(json);
            JsonNode data = jsonNode.path("data");
            for(Iterator<Map.Entry<String,JsonNode>> it = data.fields(); it.hasNext(); ) {
                Map.Entry<String,JsonNode> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue().asText();
                String decodedValue = new String(Base64.getDecoder().decode(value));
                kubeData.put(namespace+"/"+secretName+"/"+key, decodedValue);
            }
        } catch (Exception e) {
            LOGGER.warn("Error parsing secret {} {} : {}", namespace, secretName, e.getMessage());
        }
    }
}