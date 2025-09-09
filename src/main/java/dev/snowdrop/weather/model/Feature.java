package dev.snowdrop.weather.model;

public record Feature(
    String id,
    String type,
    Object geometry,
    Properties properties) {
}
