package com.rideshare.ride_service.Service;

import org.modelmapper.ModelMapper;
import com.rideshare.ride_service.dto.RideRequest;
import com.rideshare.ride_service.dto.RideResponse;
import com.rideshare.ride_service.entity.Ride;
import com.rideshare.ride_service.entity.RideStatus;
import com.rideshare.ride_service.event.RideRequestedEvent;
import com.rideshare.ride_service.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private final ModelMapper modelMapper;

    private static final String RIDE_REQUESTED_TOPIC = "ride-requested";


    /**
     * create ride in DB with REQUESTED status and publish RideRequestedEvent to Kafka topic for matching service to consume
     */
    public RideResponse requestRide(RideRequest request) {
        log.info("New ride request from rider: {}", request.getRiderId());

        //Step 1: save ride to DB
        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setRiderId(request.getRiderId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimatedFare(request));

        Ride savedRide = rideRepository.save(ride);

        //Step 2: publish RideRequestedEvent to Kafka topic
        //matching service will consume this and find nearest available drivers and send them notifications

        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to Kafka for rideId: {}", savedRide.getId());

        //Update status to Matching
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return modelMapper.map(savedRide, RideResponse.class);
    }

    public void updateRideStatus(String rideId, String driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    public RideResponse startRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if(ride.getStatus() != RideStatus.ACCEPTED){
            throw new RuntimeException("Ride cannot be started. Current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);

        return modelMapper.map(ride, RideResponse.class);
    }

    public RideResponse completeRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if(ride.getStatus() != RideStatus.RIDE_STARTED){
            throw new RuntimeException("Ride cannot be completed. Current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setStartedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return modelMapper.map(ride, RideResponse.class);
    }

    public RideResponse cancelRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return modelMapper.map(ride, RideResponse.class);
    }

    public RideResponse getRideById(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
        return modelMapper.map(ride, RideResponse.class);
    }

    public List<RideResponse> getRidesByRiderId(String riderId) {
        return rideRepository.findByRiderIdOrderByCreatedDesc(riderId)
                .stream()
                .map(ride -> modelMapper.map(ride, RideResponse.class))
                .collect(Collectors.toList());
    }

    private double calculateEstimatedFare(RideRequest request) {
        //Simplified Haversine formula to calculate distance between pickup and drop locations
        double lat1 = Math.toRadians(request.getPickupLatitude());
        double log1 = Math.toRadians(request.getPickupLongitude());

        double lat2 = Math.toRadians(request.getDropLatitude());
        double log2 = Math.toRadians(request.getDropLongitude());

        double dLat = lat2 - lat1;
        double dLog = log2 - log1;

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dLog / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceInKm = 6371 * c; // Earth radius in km

        //Base fare : 50Rs + 12Rs per km
        double fare = 50 + (distanceInKm * 12);

        return Math.round(fare + 100.0) / 100.0; // Round to 2 decimal places
    }
}