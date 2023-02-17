package io.axoniq.demo.bikerental.rental.query;

import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;
    private final QueryUpdateEmitter updateEmitter;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository, QueryUpdateEmitter updateEmitter) {
        this.bikeStatusRepository = bikeStatusRepository;
        this.updateEmitter = updateEmitter;
    }

    // TODO Implement relevant Query and Event Handlers

}
