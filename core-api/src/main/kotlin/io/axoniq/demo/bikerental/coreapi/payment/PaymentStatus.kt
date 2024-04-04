package io.axoniq.demo.bikerental.coreapi.payment

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class PaymentStatus(
    @Id var id: String,
    val amount: Int,
    val reference: String,
    val status: Status = Status.PENDING,
) {
    fun approved() = this.copy(status = Status.APPROVED)
    fun rejected() = this.copy(status = Status.REJECTED)

    enum class Status { PENDING, APPROVED, REJECTED }
}
