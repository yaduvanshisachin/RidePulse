package com.rideshare.ride_service.entity;

public enum RideStatus {
    REQUESTED, // Ride has been requested by the rider but not yet accepted by any driver
    MATCHING,  // Ride is in the process of being matched with a driver
    ACCEPTED,
    DRIVER_ARRIVING, // Ride has been completed successfully
    RIDE_STARTED,
    COMPLETED, // Ride has been completed successfully
    CANCELLED   // Ride has been cancelled by either the rider or the driver
}
