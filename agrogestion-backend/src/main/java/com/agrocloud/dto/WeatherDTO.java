package com.agrocloud.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WeatherDTO {
    private WeatherCurrentDTO current;
    private List<WeatherForecastDTO> forecast;
    private String location;
    private LocalDateTime lastUpdated;
    
    // Constructores
    public WeatherDTO() {}
    
    public WeatherDTO(WeatherCurrentDTO current, List<WeatherForecastDTO> forecast, String location) {
        this.current = current;
        this.forecast = forecast;
        this.location = location;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters y Setters
    public WeatherCurrentDTO getCurrent() {
        return current;
    }
    
    public void setCurrent(WeatherCurrentDTO current) {
        this.current = current;
    }
    
    public List<WeatherForecastDTO> getForecast() {
        return forecast;
    }
    
    public void setForecast(List<WeatherForecastDTO> forecast) {
        this.forecast = forecast;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
