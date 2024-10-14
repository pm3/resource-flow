package eu.aston.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ResourceParam(String name, String value, String secret, String configMap) {
}
