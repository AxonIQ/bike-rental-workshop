package io.axoniq.demo.bikerental.coreapi.rental

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class BikeStatus(
    @Id
    var bikeId: String,
    val bikeType: String,
    val location: String,
    val status: RentalStatus = RentalStatus.AVAILABLE,
    val renter: String? = null,
) {

    fun returnedAt(location: String) = this.copy(
        location = location,
        status = RentalStatus.AVAILABLE,
        renter = null
    )

    fun requestedBy(renter: String) = this.copy(
        status = RentalStatus.REQUESTED,
        renter = renter
    )

    fun rentedBy(renter: String) = this.copy(
        status = RentalStatus.RENTED,
        renter = renter
    )

    val description: String
        get() = when (status) {
            RentalStatus.RENTED -> "Bike $bikeId was rented by $renter in $location"
            RentalStatus.AVAILABLE -> "Bike $bikeId is available for rental in $location."
            RentalStatus.REQUESTED -> "Bike $bikeId is requested by $renter in $location"
        }
}

enum class RentalStatus {
    AVAILABLE,
    REQUESTED,
    RENTED
}
