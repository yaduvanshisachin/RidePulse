package com.rideshare.ride_service.controller;

import com.rideshare.ride_service.Service.RideService;
import com.rideshare.ride_service.dto.RideRequest;
import com.rideshare.ride_service.dto.RideResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@Slf4j
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;

    //Rider requests a ride
    @PostMapping("/request")
    public ResponseEntity<RideResponse> requestRide(
            @Valid @RequestBody RideRequest rideRequest) {
        log.info("Received ride request from rider: {}", rideRequest.getRiderId());
        return ResponseEntity.status(HttpStatus.CREATED).
                body(rideService.requestRide(rideRequest));
    }

    @GetMapping("{riderId}")
    public ResponseEntity<RideResponse> getRideById(
            @PathVariable String rideId) {
        return ResponseEntity.ok(rideService.getRideById(rideId));
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<RideResponse>> getRidesByRiderId(
            @PathVariable String riderId) {
        return ResponseEntity.ok(rideService.getRidesByRiderId(riderId));
    }

    //Driver starts the ride
    @PutMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(
            @PathVariable String rideId) {
        return ResponseEntity.ok(rideService.startRide(rideId));
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(
            @PathVariable String rideId) {
        return ResponseEntity.ok(rideService.completeRide(rideId));
    }

    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable String rideId) {
        return ResponseEntity.ok(rideService.cancelRide(rideId));
    }
}
