# Building event-driven applications using DDD, CQRS and Event Sourcing, with Axon Framework

Welcome to this workshop! You will build a "bike rental" sample application
with [Axon Framework and Axon Server](https://developer.axoniq.io/).

## Installation

The following software must be installed in your local environment:

* JDK version 17 (or higher).

* Docker Compose

* An IDE such as [Jetbrains IDEA](https://www.jetbrains.com/idea/) is recommended.

Starting Postgres Server
  - From root of projection run `docker compose up -d`.  This will start container running Postgres on port 5432.


## Part 1: Building Rental monolith

The workshop consists of a number of exercises to be completed in sequence, thus building the `rental` module and
connecting its process to the `payment` module, which has already been built.

In this exercise, the command model of the `rental` boundary context will be implemented.

An event-sourced aggregate `Bike` (representing the domain entity "bike") accepts and validates incoming commands,
publishing corresponding events that are persisted in the Axon Server event store.

Follow the steps outlined below.

### Exercise 1 - Creating the Bike aggregate

The (failing) `shouldRegisterBike` test in `BikeTest` attempts to send a `RegisterBikeCommand` to (a newly
created) `Bike` aggregate, expecting
a `BikeRegisteredEvent` to be published as a result.

* Annotate the `Bike` class as an aggregate via `@Aggregate`.

* The aggregate needs a unique identifier; use the `@AggregateIdentifier` annotation on a `String` member field
  called `bikeId`.

* Create a _command handling constructor_, annotated with `CommandHandler`, that accepts `RegisterBikeCommand`; also add
  a no-arg constructor (required).

* Publish a `BikeRegisteredEvent` in the command handling constructor via the statically imported `apply` function.

* Create an _event sourcing handler_ that accepts `BikeRegisteredEvent` and performs the actual state change; in this
  case, setting the aggregate's identifier. An event sourcing handler is a method (choose any name) that is annotated
  with the `@EventSourcingHandler` annotation.

> For more information on implementing aggregates,
> see [here](https://docs.axoniq.io/reference-guide/v/4.6/axon-framework/axon-framework-commands/modeling/aggregate).

Run the test to confirm that you've created the constructor properly.

#### Implementing all Commands and Events

The remaining unit tests cover the other commands and events related to `Bike`.

Implement the required Command Handlers and Event Sourcing handlers to make all the tests pass.

> Note
> 
> Unlike in the first exercise, these handlers should act on an existing aggregate instance, rather than creating
> a new one. Therefore, create a regular instance method (the ones with a name and return value) instead of a constructor.
> The best practice is to use `void` as return type, unless you explicitly expect to return a value from the command's
> execution.

Note that the `RequestBikeCommand` expects a return value. This is the "rental reference", which must be a unique
value to be able to refer to a specific attempt to rent a bike. This reference will later be used to link this request
to the payments. You should return the same value that is also used in the `BikeRequestedEvent`.

#### Connecting the Commands to the UI controller

Commands and queries are sent from the `RentalController` using a `CommandGateway` and `QueryGateway` respectively.

Implement the (POST) `/requestBike` and `/returnBike` endpoints by sending the corresponding commands; see
the `generateBikes` method for how to use `CommandGateway`.

### Exercise 2 - Projections

Queries are handled by a query model (a.k.a. _projections_). A projection is updated when events are published, and it
implements so-called _query handlers_ that receive and respond to queries (sent via the `QueryGateway`).

A `BikeStatusProjection` class exists that needs the following:

* Event handlers for each of the events currently supported; an event handler is annotated with `@EventHandler` and
  updates bike statuses in an embedded database.

  > Don't confuse `@EventHandler` with `@EventSourcingHandler`! The latter only exists on the command-side.

  `BikeStatusRepository` offers methods for persisting (`save`) and retrieving instances (`findById`, `findAll`)
  of `BikeStatus`.

  > `BikeStatus` contains methods to set relevant fields; you can find and update a particular bike status via
  >
  >       bikeStatusRepository.findById(someBikeId).ifPresent(
  >        bikeStatus -> { 
  >           // update bikeStatus...
  >        }
  >       )`

* Query handlers are annotated with `@QueryHandler(queryName = "someQuery")` and use `BikeStatusRepository` to retrieve
  and return results (that conform to what is expected in `BikeController`).

In the `RentalController`, implement the (GET) `/bikes` and `/bikes/{bikeId}` endpoints by sending the corresponding
queries:

* GET `/bikes`:

  `queryGateway.query("findAll", null, ResponseTypes.multipleInstancesOf(BikeStatus.class));`

* GET `/bikes/{bikeId}`:

  `queryGateway.query("findOne", bikeId, BikeStatus.class);`

> View
>
the [JavaDoc](https://apidocs.axoniq.io/latest/org/axonframework/queryhandling/QueryGateway.html#query-java.lang.String-Q-org.axonframework.messaging.responsetypes.ResponseType-)
> for sending queries.

Perform the following tasks and inspect the Rental.domain_event_entry table to see the resulting events:

The requests to send are available in the `requests.http` file. You may have to change the parameter values for each
request.

* Register a number of bikes.

* Get all registered bikes.

* Request a specific bike.

* Get the status of the requested bike. It should show that it has been requested.

Close the Rental application; delete all data in in Rental database by running .

### Exercise 3: Connecting the Payment processing

In order to successfully rent a bike, a payment must be made before the bike request is approved. Since this is a
transaction that spans both `rental` and `payment` modules, a _process_ (also called _saga_) must be initiated.

The payment process is implemented in `PaymentSaga`; it starts and ends upon receiving a `BikeRequestedEvent`
and `PaymentConfirmedEvent` respectively.

Implement the process as follows:

* Annotate a member function that accepts a `BikeRequestedEvent` with

        @StartSaga
        @SagaEventHandler(associationId = "bikeId")

* In this event handler:

  Store the bike ID as process state (simply a member field, which will be used later).

  Generate a new payment ID (for example using `UUID.randomUUID.toString()`) and associated it with the process via:

        SagaLifecycle.associatedWith("paymentId", paymentId)

  Send a `PreparePaymentCommand` via the `CommandGateway`. The handler for this command is implemented in the Payment
  Application.

* Annotate a member function that accepts a `PaymentConfirmedEvent` with

        @EndSaga
        @SagaEventHandler(associationId = "paymentId")

  Send a `ApproveBikeRequestCommand`.

> Why can't the `@EndSaga` event handler be associated with `bikeId`?

> Why didn't we need a "prepare payment" endpoint in `PaymentController`?

#### Running the Application

Start  the `RentalApplication`

Perform the following tasks and inspect the _Search_ (for events), _Commands_ and _Queries_ sections:

The requests to send are available in the `requests.http` file. You may have to change the parameter values for each
request.

* Register a number of bikes.

* Get all registered bikes.

* Request a specific bike. Note the rental reference.

* Find the payment ID for the rental reference and confirm that payment.

* Verify that the payment has been approved and the bike is now in use.

* Return the bike in a new location

## When you're done

Congratulations! You've implemented the core concepts. But there is a lot more to explore. In these labs, we've
only covered the basics of the functional components of Axon Framework. There are a lot of non-functional configuration
items hidden in this application.

You can take a look at the [postgres-singleapp-solution branch](https://github.com/AxonIQ/bike-rental-workshop/tree/postgres-singleapp-solution) for
the full implementation, including deadlines.