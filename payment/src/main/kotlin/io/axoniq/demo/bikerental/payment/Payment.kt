package io.axoniq.demo.bikerental.payment

import io.axoniq.demo.bikerental.coreapi.payment.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class Payment {
    @AggregateIdentifier
    private var id: String? = null

    private var closed = false
    private var paymentReference: String? = null

    constructor()

    @CommandHandler
    constructor(command: PreparePaymentCommand) {
        val paymentId = UUID.randomUUID().toString()
        AggregateLifecycle.apply(PaymentPreparedEvent(paymentId, command.amount, command.paymentReference))
    }

    @CommandHandler
    fun handle(command: ConfirmPaymentCommand) {
        if (!closed) {
            AggregateLifecycle.apply(PaymentConfirmedEvent(command.paymentId, paymentReference!!))
        }
    }

    @CommandHandler
    fun handle(command: RejectPaymentCommand) {
        if (!closed) {
            AggregateLifecycle.apply(PaymentRejectedEvent(command.paymentId, paymentReference!!))
        }
    }

    @EventSourcingHandler
    private fun on(event: PaymentPreparedEvent) {
        this.id = event.paymentId
        this.paymentReference = event.paymentReference
    }

    @EventSourcingHandler
    private fun on(event: PaymentConfirmedEvent) {
        this.closed = true
    }

    @EventSourcingHandler
    private fun on(event: PaymentRejectedEvent) {
        this.closed = true
    }
}
