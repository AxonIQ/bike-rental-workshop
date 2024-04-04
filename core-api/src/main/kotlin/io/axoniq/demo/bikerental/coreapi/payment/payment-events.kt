package io.axoniq.demo.bikerental.coreapi.payment

data class PaymentConfirmedEvent(
    val paymentId: String,
    val paymentReference: String
)

data class PaymentPreparedEvent(
    val paymentId: String,
    val amount: Int,
    val paymentReference: String
)

data class PaymentRejectedEvent(
    val paymentId: String,
    val paymentReference: String
)

