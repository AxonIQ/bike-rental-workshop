package io.axoniq.demo.bikerental.payment

import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentStatusRepository : CrudRepository<PaymentStatus, String> {
    fun findAllByStatus(status: PaymentStatus.Status): List<PaymentStatus>
    fun findByReferenceAndStatus(reference: String, status: PaymentStatus.Status): Optional<PaymentStatus>
}
