package eu.aston;

import java.io.File;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("flow")
public record FlowConfig(File configDir,
                         File baseDir){}
