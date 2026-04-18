package com.rideshare.matching_service.service;

import com.rideshare.matching_service.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventConsumer {

    private final MatchingService matchingService;

    /**
     * Listens to ride-requested topic and triggers the matching process
     * Triggered every time Ride service published a new ride request
     */
    @KafkaListener(
            topics = "ride-requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event){
        try{
            matchingService.matchDriverForRide(event);
        }
        catch (Exception ex){
            log.error("Error processing RideRequestedEvent for rideId: {}, error: {}", event.getRideId(), ex.getMessage());
            //in Production: send to dead letter query for retry
        }
    }
}
