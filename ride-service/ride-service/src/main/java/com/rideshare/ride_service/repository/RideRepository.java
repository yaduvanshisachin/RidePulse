package com.rideshare.ride_service.repository;

import com.rideshare.ride_service.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, String> {

    List<Ride> findByRiderIdOrderByCreatedDesc(String riderId);
}
