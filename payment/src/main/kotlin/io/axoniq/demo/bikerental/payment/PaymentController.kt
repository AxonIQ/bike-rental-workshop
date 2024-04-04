package io.axoniq.demo.bikerental.payment

import io.axoniq.demo.bikerental.coreapi.payment.ConfirmPaymentCommand
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.query
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
class PaymentController(
    private val queryGateway: QueryGateway,
    private val commandGateway: CommandGateway
) {

    @GetMapping("/status/{paymentId}")
    fun getStatus(@PathVariable("paymentId") paymentId: String): CompletableFuture<PaymentStatus> =
        queryGateway.query("getStatus", paymentId)

    @GetMapping("/findPayment")
    fun findPaymentId(@RequestParam("reference") paymentReference: String): CompletableFuture<String> =
        queryGateway.query("getPaymentId", paymentReference)

    @PostMapping("/acceptPayment")
    fun confirmPayment(@RequestParam("id") paymentId: String): CompletableFuture<Void> =
        commandGateway.send(ConfirmPaymentCommand(paymentId))

    @PostMapping("/rejectPayment")
    fun rejectPayment(@RequestParam("id") paymentId: String): CompletableFuture<Void> =
        commandGateway.send(RejectPaymentCommand(paymentId))

    @GetMapping("/status")
    fun getStatus(@RequestParam(required = false) status: PaymentStatus.Status?): CompletableFuture<List<PaymentStatus>> =
        queryGateway.queryMany(status?.let { GetPaymentsByStatus(status) } ?: GetAllPayments)
}
