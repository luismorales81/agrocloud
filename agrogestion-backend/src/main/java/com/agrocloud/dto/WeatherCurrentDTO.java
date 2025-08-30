package com.agrocloud.dto;

public class WeatherCurrentDTO {
    private double temperature;
    private int humidity;
    private double windSpeed;
    private double precipitation;
    private int weatherCode;
    private String weatherDescription;
    private String icon;
    
    // Constructores
    public WeatherCurrentDTO() {}
    
    public WeatherCurrentDTO(double temperature, int humidity, double windSpeed, 
                           double precipitation, int weatherCode, String weatherDescription, String icon) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
        this.weatherCode = weatherCode;
        this.weatherDescription = weatherDescription;
        this.icon = icon;
    }
    
    // Getters y Setters
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public int getHumidity() {
        return humidity;
    }
    
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
    
    public double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public double getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
    
    public int getWeatherCode() {
        return weatherCode;
    }
    
    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }
    
    public String getWeatherDescription() {
        return weatherDescription;
    }
    
    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
