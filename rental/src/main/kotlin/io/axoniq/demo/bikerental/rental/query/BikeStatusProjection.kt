package io.axoniq.demo.bikerental.rental.query

import io.axoniq.demo.bikerental.coreapi.rental.*
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class BikeStatusProjection(
    private val bikeStatusRepository: BikeStatusRepository
) {

    @EventHandler
    fun on(event: BikeRegisteredEvent) {
        bikeStatusRepository.save(BikeStatus(event.bikeId, event.bikeType, event.location))
    }

    @EventHandler
    fun handle(event: BikeRequestedEvent) {
        bikeStatusRepository
            .findById(event.bikeId)
            .map { it.requestedBy(event.renter) }
            .ifPresent { bikeStatusRepository.save(it) }
    }

    @EventHandler
    fun on(event: BikeInUseEvent) {
        bikeStatusRepository
            .findById(event.bikeId)
            .map { it.rentedBy(event.renter) }
            .ifPresent { bikeStatusRepository.save(it) }
    }

    @EventHandler
    fun on(event: BikeReturnedEvent) {
        bikeStatusRepository
            .findById(event.bikeId)
            .map { it.returnedAt(event.location) }
            .ifPresent { bikeStatusRepository.save(it) }
    }

    @EventHandler
    fun on(event: RequestRejectedEvent) {
        bikeStatusRepository
            .findById(event.bikeId)
            .map { it.returnedAt(it.location) }
            .ifPresent { bikeStatusRepository.save(it) }
    }

    @QueryHandler
    fun findAll(query: FindAllQuery): Iterable<BikeStatus> =
        bikeStatusRepository.findAll()

    @QueryHandler
    fun findOne(query: FindOneQuery): BikeStatus? =
        bikeStatusRepository.findByIdOrNull(query.bikeId)
}

object FindAllQuery

data class FindOneQuery(val bikeId: String)
