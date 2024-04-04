package io.axoniq.demo.bikerental.coreapi.rental

data class BikeRegisteredEvent(
    val bikeId: String,
    val bikeType: String,
    val location: String
)

data class BikeRequestedEvent(
    val bikeId: String,
    val renter: String,
    val rentalReference: String
)

data class BikeInUseEvent(
    val bikeId: String,
    val renter: String
)

data class BikeReturnedEvent(
    val bikeId: String,
    val location: String
)

data class RequestRejectedEvent(
    val bikeId: String
)
