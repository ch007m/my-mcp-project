package dev.snowdrop.weather.tools;

import java.util.Map;
import java.util.stream.Collectors;

import dev.snowdrop.weather.service.WeatherApiRestClient;
import dev.snowdrop.weather.model.Alerts;
import dev.snowdrop.weather.model.Forecast;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkus.qute.Qute;
import org.jboss.logging.Logger;

public class WeatherMcpTools {

    Logger logger = Logger.getLogger(WeatherMcpTools.class);

    @RestClient
    WeatherApiRestClient weatherClient;

    @Tool(description = "Get weather alerts for a US state.")
    String getAlerts(@ToolArg(description = "Two-letter US state code (e.g. CA, NY)") String state) {
        return formatAlerts(weatherClient.getAlerts(state));
    }

    @Tool(description = "Get weather forecast for a location.")
    String getForecast(@ToolArg(description = "Latitude of the location") double latitude,
                       @ToolArg(description = "Longitude of the location") double longitude) {

        // Get from the coordinates to search the nearest forecast office
        var points = weatherClient.getPoints(latitude, longitude);
        //var url = Qute.fmt("{p.properties.forecast}", Map.of("p", points));

        // Get a forecast from the forecast office using its office ID (OKD, etc) and x,y coordinates
        var forecastOffice = Qute.fmt("{p.properties.gridId}", Map.of("p", points));
        Integer gridX = Integer.valueOf(Qute.fmt("{p.properties.gridX}", Map.of("p", points)));
        Integer gridY = Integer.valueOf(Qute.fmt("{p.properties.gridY}", Map.of("p", points)));
        return formatForecast(weatherClient.getForecast(forecastOffice, gridX, gridY));
    }

    public static double roundingCoordinate(double coordinate) {
        int places = 4;
        double scale = Math.pow(10, places);

        // Perform the rounding
        return Math.round(coordinate * scale) / scale;
        // return new BigDecimal(coordinate).setScale(4, RoundingMode.HALF_UP).doubleValue();
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
