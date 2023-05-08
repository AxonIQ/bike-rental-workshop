package io.axoniq.demo.bikerental.rental.query;

import org.springframework.stereotype.Component;

@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository) {
        this.bikeStatusRepository = bikeStatusRepository;
    }

    // TODO Implement relevant Query and Event Handlers

}
