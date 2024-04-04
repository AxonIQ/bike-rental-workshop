package io.axoniq.demo.bikerental.rental.ui

import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus
import io.axoniq.demo.bikerental.coreapi.rental.RegisterBikeCommand
import io.axoniq.demo.bikerental.coreapi.rental.RequestBikeCommand
import io.axoniq.demo.bikerental.coreapi.rental.ReturnBikeCommand
import io.axoniq.demo.bikerental.rental.query.FindAllQuery
import io.axoniq.demo.bikerental.rental.query.FindOneQuery
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.query
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom


@RestController
@RequestMapping("/")
class RentalController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    @PostMapping
    fun generateBikes(
        @RequestParam("bikes") bikeCount: Int,
        @RequestParam("bikeType") bikeType: String
    ): CompletableFuture<Void> = CompletableFuture.allOf(
        *(0 until bikeCount)
            .map {
                commandGateway.send<Any>(
                    RegisterBikeCommand(bikeId = UUID.randomUUID().toString(), bikeType, randomLocation())
                )
            }
            .toTypedArray()
    )

    @PostMapping("/requestBike")
    fun requestBike(
        @RequestParam("bikeId") bikeId: String,
        @RequestParam("renter") renter: String
    ): CompletableFuture<String> =
        commandGateway.send(RequestBikeCommand(bikeId, renter))

    @PostMapping("/returnBike")
    fun returnBike(
        @RequestParam("bikeId") bikeId: String,
        @RequestParam("location") location: String
    ): CompletableFuture<String> =
        commandGateway.send(ReturnBikeCommand(bikeId, location))

    @GetMapping("/bikes")
    fun findAll(): CompletableFuture<List<BikeStatus>> =
        queryGateway.queryMany(FindAllQuery)

    @GetMapping("/bikes/{bikeId}")
    fun findStatus(@PathVariable("bikeId") bikeId: String): CompletableFuture<BikeStatus> =
        queryGateway.query(FindOneQuery(bikeId))

    private fun randomLocation(): String =
        LOCATIONS[ThreadLocalRandom.current().nextInt(LOCATIONS.size)]

    companion object {
        private val LOCATIONS = listOf(
            "Amsterdam",
            "Paris",
            "Vilnius",
            "Barcelona",
            "London",
            "New York",
            "Toronto",
            "Berlin",
            "Milan",
            "Rome",
            "Belgrade"
        )
    }
}
