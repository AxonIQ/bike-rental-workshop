package io.axoniq.demo.bikerental.rental.paymentsaga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class PaymentSaga {

    @Autowired
    private transient CommandGateway commandGateway;

}
