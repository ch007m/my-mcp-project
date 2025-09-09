package dev.snowdrop.service;

import java.util.Map;
import java.util.stream.Collectors;

import dev.snowdrop.weather.model.Alerts;
import dev.snowdrop.weather.model.Forecast;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkus.qute.Qute;

public class WeatherTools {

    @RestClient
    WeatherClient weatherClient;

    @Tool(description = "Get weather alerts for a US state.")
    String getAlerts(@ToolArg(description = "Two-letter US state code (e.g. CA, NY)") String state) {
        return formatAlerts(weatherClient.getAlerts(state));
    }

    @Tool(description = "Get weather forecast for a location.")
    String getForecast(@ToolArg(description = "Latitude of the location") double latitude,
                       @ToolArg(description = "Longitude of the location") double longitude) {
        var points = weatherClient.getPoints(latitude, longitude);
        var url = Qute.fmt("{p.properties.forecast}", Map.of("p", points));

        return formatForecast(weatherClient.getForecast(url));
    }

    String formatForecast(Forecast forecast) {
        return forecast.properties().periods().stream().map(period -> {

            return Qute.fmt(
                """
                        Temperature: {p.temperature}°{p.temperatureUnit}
                        Wind: {p.windSpeed} {p.windDirection}
                        Forecast: {p.detailedForecast}
                        """,
                Map.of("p", period));
        }).collect(Collectors.joining("\n---\n"));
    }

    String formatAlerts(Alerts alerts) {
        return alerts.features().stream().map(feature -> {
            return Qute.fmt(
                """
                        Event: {p.event}
                        Area: {p.areaDesc}
                        Severity: {p.severity}
                        Description: {p.description}
                        Instructions: {p.instruction}
                        """,
                Map.of("p", feature.properties()));
        }).collect(Collectors.joining("\n---\n"));
    }


}
