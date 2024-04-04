package io.axoniq.demo.bikerental.coreapi.rental

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ApproveRequestCommand(
    @TargetAggregateIdentifier val bikeId: String,
    val renter: String
)

data class RegisterBikeCommand(
    @TargetAggregateIdentifier val bikeId: String,
    val bikeType: String,
    val location: String
)

data class RejectRequestCommand(
    @TargetAggregateIdentifier val bikeId: String,
    val renter: String
)

data class RequestBikeCommand(
    @TargetAggregateIdentifier val bikeId: String,
    val renter: String
)

data class ReturnBikeCommand(
    @TargetAggregateIdentifier val bikeId: String,
    val location: String
)

