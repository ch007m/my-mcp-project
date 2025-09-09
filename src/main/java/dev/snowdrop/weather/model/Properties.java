package dev.snowdrop.weather.model;

public record Properties(
    String id,
    String areaDesc,
    String event,
    String severity,
    String description,
    String instruction) {
}
