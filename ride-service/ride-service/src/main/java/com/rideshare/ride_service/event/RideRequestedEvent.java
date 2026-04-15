package com.rideshare.ride_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * event published to Kafka topic new ride requested
 * Matching service will consume this event and find nearest available drivers and send them notifications
 * TOPIC: ride-requested
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestedEvent {
    private String Id;
    private String riderId;

    //Pickup
    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;

    //Drop
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
}
