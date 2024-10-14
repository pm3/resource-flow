package eu.aston.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.aston.model.ResourceFile;
import jakarta.inject.Singleton;

@Singleton
public class FlowRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(FlowRunner.class);

    private final ExecProcess execProcess;

    public FlowRunner(ExecProcess execProcess) {
        this.execProcess = execProcess;
    }

    public void run(String resourceName, String script, Map<String, String> parameters, File workDir, List<ResourceFile> files) {
        if(script==null) return;
        if(files!=null) {
            copyFiles(workDir, files, parameters);
        }
        String script2 = parse(parameters, script);
        StringBuilder bashScript = new StringBuilder();
        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            if(entry.getKey().startsWith("env.")) {
                bashScript.append("export ").append(entry.getKey().substring(4)).append("=").append(entry.getValue()).append("\n");
            }
        }
        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            if(entry.getKey().startsWith("var.")) {
                bashScript.append(entry.getKey().substring(4)).append("=").append(entry.getValue()).append("\n");
            }
        }
        bashScript.append(script2);
        String resp = execProcess.execBuilder(workDir, List.of("bash", "-ce", bashScript.toString()));
        LOGGER.info("{} => {}", resourceName, resp);
        String varName = null;
        StringBuilder cmdResponse = null;
        for(String line : resp.split("\n")) {
            if(line.startsWith("##start-param")) {
                varName = line.substring(13).trim();
                cmdResponse = new StringBuilder();
            } else if(line.startsWith("##end")) {
                if(varName!=null && cmdResponse!=null) {
                    parameters.put(varName, cmdResponse.toString().trim());
                }
                varName = null;
                cmdResponse = null;
            } else if(line.startsWith("##set-param ")) {
                String[] varExpr = line.substring(11).trim().split("=");
                if(varExpr.length==2) {
                    parameters.put(varExpr[0], varExpr[1]);
                } else {
                    LOGGER.warn("ignore line: {}", line);
                }
            } else if(cmdResponse!=null) {
                cmdResponse.append(line).append("\n");
            }
        }
    }

    private void copyFiles(File workDir, List<ResourceFile> files, Map<String, String> parameters) {
        workDir.mkdirs();
        for(ResourceFile file : files) {
            File dest = new File(workDir, file.name());
            String content = parse(parameters, file.content()); 
            try(FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOGGER.error("Error writing file {}", dest, e);
            }
        }
    }

    public String parse(Map<String, String> parameters, String command) {
        Pattern pattern = Pattern.compile("%\\{([^}]+)}");
        return pattern.matcher(command).replaceAll(res->parameters.getOrDefault(res.group(1), ""));
    }
}
