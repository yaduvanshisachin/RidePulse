package com.rideshare.matching_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event consumed from Kafka topic "ride-requested"
 * published by Ride Service when a new rider requests a ride.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestedEvent {
    private String rideId;
    private String riderId;
    private double pickupLatitude;
    private double pickupLongitude;
    private double pickupAddress;
    private double dropLatitude;
    private double dropLongitude;
    private double dropAddress;

}
