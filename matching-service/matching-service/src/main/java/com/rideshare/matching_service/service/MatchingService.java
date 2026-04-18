package com.rideshare.matching_service.service;

import com.rideshare.matching_service.client.LocationServiceClient;
import com.rideshare.matching_service.dto.NearByDriverResponse;
import com.rideshare.matching_service.event.RideMatchedEvent;
import com.rideshare.matching_service.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride-matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     * Main matching logic:
     * Called when RideRequestedEvent is consumed from Kafka topic "ride-requested"
     * @param event
     * Step 1:
     * ask Location service for nearby drivers within a certain radius of pickup location
     *
     * step 2:
     * Score and rank the nearby drivers based on distance to pickup location, driver rating,
     */

    public void matchDriverForRide(RideRequestedEvent event) {

        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearbyDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );

        if(nearByDrivers.isEmpty()){
            log.warn("No driver found near for rideId: {}", event.getRideId());
            return;
        }

        // Step 2: For simplicity, we just pick the first driver from the list
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if(bestDriver.isEmpty()){
            log.warn("No suitable driver found for rideId: {}", event.getRideId());
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        //STEP3: Publish RideMatchedEvent to Kafka topic "ride-matched" with assigned driver details
        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.getRideId(), matchedEvent);
        log.info("Published RideMatchedEvent for rideId: {} with driverId: {}", event.getRideId(), assignedDriver.getDriverId());
    }

    /**
     * Driver Scoring and Ranking logic:
     *
     * Distance: 70% weight - closer drivers get higher score
     * Driver Rating: 30% weight - higher rated drivers get higher score
     *
     * score = (1/ distance) * distanceWeight + rating * ratingWeight
     */
    private Optional<NearByDriverResponse>  findBestDriver(
            List<NearByDriverResponse> drivers){

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    double distanceScore = 1.0 / (driver.getDistanceInKm() + 0.1); // add small value to avoid division by zero

                    //simulated rating between 3.0 and 5.0 for demo purposes
                    //in production: we would get real driver ratings Driver-Service
                    double simulatedRating = 3.0 + Math.random() * 2.0;

                    return distanceScore * distanceWeight + simulatedRating * ratingWeight;
                }));
    }

}
