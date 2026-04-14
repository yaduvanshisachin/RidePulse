package com.rideshare.location_service.service;

import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.dto.NearbyDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis key for storing driver locations using GEO commands
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    /**
     * update driver location in Redis using GEOADD command
     * Called in every 3 seconds by driver app to update location
     * Maps to Redis GEOADD command: GEOADD drivers:locations longitude latitude driverId
     */

    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {
        log.info("Updating location for driverId: {}", driverLocationRequest.getDriverId());

        //IMPORTANT: longitude FIRST, latitude SECOND - GeoSpatial Standard
        Point driverPoint = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationRequest.getDriverId()
        );
    }

    /**
     * Find nearby drivers using GEORADIUS command
     * Called by matching service when a ride is requested to find nearby drivers
     * Maps to Redis GEORADIUS command: GEORADIUS drivers:locations longitude latitude radius
     */

    public List<NearbyDriverResponse> findNearbyDrivers(
            double latitude, double longitude, double radiusInKm) {

        log.info("Finding nearby drivers for location: ({}, {}) within radius: {} km", latitude, longitude, radiusInKm);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults =
                redisTemplate.opsForGeo().radius(
                    DRIVERS_GEO_KEY,
                    searchArea,
                    RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(10) // limit to top 10 closest drivers
                );

        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();

        if(geoResults != null) {
            geoResults.getContent().forEach(result -> {

                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearbyDrivers.add(new NearbyDriverResponse(
                        location.getName(), // driverId
                        location.getPoint().getY(), // latitude
                        location.getPoint().getX(), // longitude
                        result.getDistance().getValue() // distance in km
                ));
            });
        }

        log.info("Found {} nearby drivers", nearbyDrivers.size());
        return nearbyDrivers;
    }

    /**
     * Remove driver location from Redis when driver goes offline or logs out
     * Maps to Redis ZREM command: ZREM drivers:locations driverId
     */

    public void removeDriver(String driverId) {
        log.info("Removing driverId: {} from Redis", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
    }
}




















