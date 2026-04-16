package com.rideshare.matching_service.dto;

//Response received from Location service

public class NearByDriverResponse {
    private String driverId;
    private double latitude;
    private double longitude;
    private double distanceInKm;
}
