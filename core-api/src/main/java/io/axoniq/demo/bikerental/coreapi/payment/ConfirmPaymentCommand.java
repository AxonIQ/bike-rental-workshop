package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ConfirmPaymentCommand(@TargetAggregateIdentifier String paymentId) {

}