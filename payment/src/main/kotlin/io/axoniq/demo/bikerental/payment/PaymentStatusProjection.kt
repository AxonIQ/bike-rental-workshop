package io.axoniq.demo.bikerental.payment

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.PENDING
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.extensions.kotlin.emit
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class PaymentStatusProjection(
    private val paymentStatusRepository: PaymentStatusRepository,
    private val updateEmitter: QueryUpdateEmitter
) {

    @QueryHandler(queryName = "getStatus")
    fun getStatus(paymentId: String): PaymentStatus? =
        paymentStatusRepository.findByIdOrNull(paymentId)

    @QueryHandler(queryName = "getPaymentId")
    fun getPaymentId(paymentReference: String): String? =
        paymentStatusRepository
            .findByReferenceAndStatus(paymentReference, PENDING)
            .map(PaymentStatus::id)
            .orElse(null)

    @QueryHandler(queryName = "getAllPayments")
    fun findByStatus(status: PaymentStatus.Status): Iterable<PaymentStatus> =
        paymentStatusRepository.findAllByStatus(status)

    @QueryHandler(queryName = "getAllPayments")
    fun findAll(): Iterable<PaymentStatus> =
        paymentStatusRepository.findAll()

    @EventHandler
    fun handle(event: PaymentPreparedEvent) {
        paymentStatusRepository.save(PaymentStatus(event.paymentId, event.amount, event.paymentReference))
        updateEmitter.emit(event.paymentId) { reference: String -> reference == event.paymentReference }
    }

    @EventHandler
    fun handle(event: PaymentConfirmedEvent) {
        paymentStatusRepository
            .findById(event.paymentId)
            .map { it.approved() }
            .ifPresent { paymentStatusRepository.save(it) }
    }

    @EventHandler
    fun handle(event: PaymentRejectedEvent) {
        paymentStatusRepository
            .findById(event.paymentId)
            .map { it.rejected() }
            .ifPresent { paymentStatusRepository.save(it) }
    }
}
