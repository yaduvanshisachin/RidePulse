package com.rideshare.location_service.controller;

import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.dto.NearbyDriverResponse;
import com.rideshare.location_service.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Endpoint for drivers to update their location
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(
            @RequestBody DriverLocationRequest driverLocationRequest) {

        locationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver's Location updated successfully");
    }

    // Matching service calls this when ride is requested to find nearby drivers
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam (defaultValue = "5.0") double radiusInKm) {

        List<NearbyDriverResponse> nearbyDrivers = locationService.findNearbyDrivers(latitude, longitude, radiusInKm);
        return ResponseEntity.ok(nearbyDrivers);
    }

    //when driver goes offline or logs out, we can remove their location from Redis
    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<String> removeDriver (@PathVariable String driverId) {
        locationService.removeDriver(driverId);
        return ResponseEntity.ok("Driver removed successfully");
    }
}
