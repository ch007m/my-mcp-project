package dev.snowdrop.weather.model;

public record Period(
    String name,
    int temperature,
    String temperatureUnit,
    String windSpeed,
    String windDirection,
    String detailedForecast) {
}
