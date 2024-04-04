package io.axoniq.demo.bikerental.rental.command

import io.axoniq.demo.bikerental.coreapi.rental.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class Bike {

    @AggregateIdentifier
    private var bikeId: String? = null

    private var isAvailable = false
    private var reservedBy: String? = null

    constructor()

    @CommandHandler
    constructor(command: RegisterBikeCommand) {
        AggregateLifecycle.apply(BikeRegisteredEvent(command.bikeId, command.bikeType, command.location))
    }

    @CommandHandler
    fun handle(command: RequestBikeCommand): String {
        check(this.isAvailable) { "Is already requested!" }

        val event = BikeRequestedEvent(command.bikeId, command.renter, rentalReference = UUID.randomUUID().toString())
        AggregateLifecycle.apply(event)

        return event.rentalReference
    }

    @CommandHandler
    fun handle(command: ApproveRequestCommand) {
        if (command.renter != reservedBy) {
            return
        }

        AggregateLifecycle.apply(BikeInUseEvent(command.bikeId, command.renter))
    }

    @CommandHandler
    fun handle(command: RejectRequestCommand) {
        if (command.renter != reservedBy) {
            return
        }

        AggregateLifecycle.apply(RequestRejectedEvent(command.bikeId))
    }

    @CommandHandler
    fun handle(command: ReturnBikeCommand) {
        check(!this.isAvailable) { "Bike was already returned" }
        AggregateLifecycle.apply(BikeReturnedEvent(command.bikeId, command.location))
    }

    @EventSourcingHandler
    private fun handle(event: BikeRegisteredEvent) {
        this.bikeId = event.bikeId
        this.isAvailable = true
    }

    @EventSourcingHandler
    private fun handle(event: BikeRequestedEvent) {
        this.reservedBy = event.renter
        this.isAvailable = false
    }

    @EventSourcingHandler
    private fun handle(event: BikeReturnedEvent) {
        this.reservedBy = null
        this.isAvailable = true
    }

    @EventSourcingHandler
    private fun handle(event: RequestRejectedEvent) {
        this.reservedBy = null
        this.isAvailable = true
    }

    @EventSourcingHandler
    private fun handle(event: BikeInUseEvent) {
        this.isAvailable = false
    }
}
