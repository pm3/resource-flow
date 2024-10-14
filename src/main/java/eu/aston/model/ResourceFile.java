package eu.aston.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ResourceFile(String name, String content) {
}
