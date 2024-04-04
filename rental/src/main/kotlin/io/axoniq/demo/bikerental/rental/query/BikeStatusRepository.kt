package io.axoniq.demo.bikerental.rental.query

import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus
import io.axoniq.demo.bikerental.coreapi.rental.RentalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BikeStatusRepository : JpaRepository<BikeStatus, String> {
    fun findAllByBikeTypeAndStatus(bikeType: String, status: RentalStatus): List<BikeStatus>
}
