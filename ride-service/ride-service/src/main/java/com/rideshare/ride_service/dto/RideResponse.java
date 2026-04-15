package com.rideshare.ride_service.dto;

import com.rideshare.ride_service.entity.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideResponse {
    private String id;
    private String riderId;
    private String driverId;
    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;

    //Ride status - tracks the lifecyle of the ride (REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED)
    private RideStatus status;

    private double estimatedFare;
    private double actualFare;

    //Timestamps for tracking the ride lifecycle
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
