package com.agrocloud.dto;

import java.time.LocalDate;

public class WeatherForecastDTO {
    private LocalDate date;
    private String dayOfWeek;
    private double maxTemperature;
    private double minTemperature;
    private double precipitation;
    private int weatherCode;
    private String weatherDescription;
    private String icon;
    private String agriculturalAdvice;
    
    // Constructores
    public WeatherForecastDTO() {}
    
    public WeatherForecastDTO(LocalDate date, String dayOfWeek, double maxTemperature, 
                            double minTemperature, double precipitation, int weatherCode, 
                            String weatherDescription, String icon, String agriculturalAdvice) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.precipitation = precipitation;
        this.weatherCode = weatherCode;
        this.weatherDescription = weatherDescription;
        this.icon = icon;
        this.agriculturalAdvice = agriculturalAdvice;
    }
    
    // Getters y Setters
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public double getMaxTemperature() {
        return maxTemperature;
    }
    
    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
    
    public double getMinTemperature() {
        return minTemperature;
    }
    
    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
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
    
    public String getAgriculturalAdvice() {
        return agriculturalAdvice;
    }
    
    public void setAgriculturalAdvice(String agriculturalAdvice) {
        this.agriculturalAdvice = agriculturalAdvice;
    }
}
