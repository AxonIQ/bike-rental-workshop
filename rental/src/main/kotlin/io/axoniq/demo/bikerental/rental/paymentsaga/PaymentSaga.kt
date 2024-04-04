package io.axoniq.demo.bikerental.rental.paymentsaga

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class PaymentSaga{
    @Autowired @Transient lateinit var commandGateway: CommandGateway

    private var bikeId: String? = null
    private var renter: String? = null

    @StartSaga
    @SagaEventHandler(associationProperty = "bikeId")
    fun start(event: BikeRequestedEvent) {
        val paymentReference = event.rentalReference
        SagaLifecycle.associateWith("paymentReference", paymentReference)

        this.bikeId = event.bikeId
        this.renter = event.renter

        commandGateway.send<Any>(PreparePaymentCommand(200, paymentReference))
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentReference")
    fun end(event: PaymentConfirmedEvent) {
        commandGateway.send<Any>(ApproveRequestCommand(bikeId!!, renter!!))
    }
}
