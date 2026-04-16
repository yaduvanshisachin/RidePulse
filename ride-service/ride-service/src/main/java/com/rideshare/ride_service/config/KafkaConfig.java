package com.rideshare.ride_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    //Topic where ride-service publishes RideRequestedEvent
    //Matching service subscribes to this topic to consume new ride requests and find nearby drivers

    @Bean
    public NewTopic rideRequestedTopic() {
        return TopicBuilder.name("ride-requested")
                .partitions(3)
                .replicas(1)
                .build();
    }

    //Topic where Matching-service publishes match results
    //Ride service subscribes to this topic to consume match results and update ride status accordingly

    @Bean
    public NewTopic rideMatchedTopic() {
        return TopicBuilder.name("ride-matched")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
