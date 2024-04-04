package io.axoniq.demo.bikerental.coreapi.payment

import org.axonframework.commandhandling.RoutingKey
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class PreparePaymentCommand(
    val amount: Int,
    @RoutingKey val paymentReference: String
)

data class ConfirmPaymentCommand(
    @TargetAggregateIdentifier val paymentId: String
)

data class RejectPaymentCommand(
    @TargetAggregateIdentifier val paymentId: String
)
