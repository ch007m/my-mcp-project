package dev.snowdrop.service;

import dev.snowdrop.weather.model.Alerts;
import dev.snowdrop.weather.model.Forecast;
import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Map;

@RegisterRestClient(baseUri = "https://api.weather.gov")
public interface WeatherClient {
    @GET
    @Path("/alerts/active/area/{state}")
    Alerts getAlerts(@RestPath String state);

    @GET
    @Path("/points/{latitude},{longitude}")
    Map<String, Object> getPoints(@RestPath double latitude, @RestPath double longitude);

    @GET
    @Path("/")
    Forecast getForecast(@Url String url);
}