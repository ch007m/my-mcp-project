package dev.snowdrop.weather.model;

import java.util.List;

public record ForecastProperties(
    List<Period> periods) {
}
