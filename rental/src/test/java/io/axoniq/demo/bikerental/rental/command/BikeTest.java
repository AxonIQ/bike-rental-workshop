package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeInUseEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeReturnedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RegisterBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.ReturnBikeCommand;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.axonframework.test.matchers.Matchers.andNoMore;
import static org.axonframework.test.matchers.Matchers.exactSequenceOf;
import static org.axonframework.test.matchers.Matchers.matches;
import static org.axonframework.test.matchers.Matchers.messageWithPayload;

class BikeTest {

    private AggregateTestFixture<Bike> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Bike.class);
    }

    @Test
    void shouldRegisterBike() {
        fixture.givenNoPriorActivity()
               .when(new RegisterBikeCommand("bikeId", "city", "Amsterdam"))
               .expectEvents(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"));
    }

    @Test
    void shouldRequestAvailableBike() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"))
               .when(new RequestBikeCommand("bikeId", "rider"))
               .expectResultMessagePayloadMatching(matches(String.class::isInstance))
               .expectEventsMatching(exactSequenceOf(
                       messageWithPayload(matches((BikeRequestedEvent e) ->
                                                          e.bikeId().equals("bikeId")
                                                                  && e.renter().equals("rider"))),
                       andNoMore()));
    }

    @Test
    void shouldNotRequestAlreadyRequestedBike() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when(new RequestBikeCommand("bikeId", "rider"))
               .expectNoEvents()
               .expectException(IllegalStateException.class);

    }

    @Test
    void shouldApproveRequestedBike() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when(new ApproveRequestCommand("bikeId", "rider"))
               .expectEvents(new BikeInUseEvent("bikeId", "rider"));
    }

    @Test
    void shouldRejectRequestedBike() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when(new RejectRequestCommand("bikeId", "rider"))
               .expectEvents(new RequestRejectedEvent("bikeId"));
    }

    @Test
    void shouldNotRejectRequestedForWrongRequester() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when(new RejectRequestCommand("bikeId", "otherRider"))
               .expectSuccessfulHandlerExecution()
               .expectNoEvents();
    }

    @Test
    void shouldNotApproveRequestedForAnotherRider() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when(new ApproveRequestCommand("bikeId", "otherRider"))
               .expectNoEvents()
               .expectSuccessfulHandlerExecution();
    }

    @Test
    void shouldReturnedBikeInUse() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new BikeInUseEvent("bikeId", "rider"))
               .when(new ReturnBikeCommand("bikeId", "NewLocation"))
               .expectEvents(new BikeReturnedEvent("bikeId", "NewLocation"));
    }

    @Test
    void shouldNotRequestBikeInUse() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new BikeInUseEvent("bikeId", "rider"))
               .when(new RequestBikeCommand("bikeId", "otherRenter"))
               .expectNoEvents()
               .expectException(IllegalStateException.class);
    }

    @Test
    void shouldRequestReturnedBike() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new BikeInUseEvent("bikeId", "rider"),
                      new BikeReturnedEvent("bikeId", "NewLocation"))
               .when(new RequestBikeCommand("bikeId", "newRider"))
               .expectEventsMatching(exactSequenceOf(
                       messageWithPayload(matches((BikeRequestedEvent e) ->
                                                          e.bikeId().equals("bikeId")
                                                                  && e.renter().equals("newRider"))),
                       andNoMore()));
    }

    @Test
    void shouldRequestRejectedBike() {
        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new RequestRejectedEvent("bikeId"))
               .when(new RequestBikeCommand("bikeId", "newRider"))
               .expectEventsMatching(exactSequenceOf(
                       messageWithPayload(matches((BikeRequestedEvent e) ->
                                                          e.bikeId().equals("bikeId")
                                                                  && e.renter().equals("newRider"))),
                       andNoMore()));


    }
}