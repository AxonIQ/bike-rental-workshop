package io.axoniq.demo.bikerental;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.modelling.saga.repository.jpa.SagaEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {PaymentStatus.class, BikeStatus.class, DomainEventEntry.class, SagaEntry.class, TokenEntry.class})
@SpringBootApplication
public class RentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Autowired
    public void configureSerializers(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                                           ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
    }

    @Autowired
    public void configure(EventProcessingConfigurer eventProcessing) {
        eventProcessing.registerPooledStreamingEventProcessor(
                "PaymentSagaProcessor",
                Configuration::eventStore,
                (c, b) -> b.workerExecutor(workerExecutorService())
                           .batchSize(100)
                           .initialToken(StreamableMessageSource::createHeadToken)
        );
        eventProcessing.registerPooledStreamingEventProcessor(
                "io.axoniq.demo.bikerental.rental.query",
                Configuration::eventStore,
                (c, b) -> b.workerExecutor(workerExecutorService())
                           .batchSize(100)

        );
    }

    @Bean
    public DeadlineManager deadlineManager(Configuration configuration) {
        return SimpleDeadlineManager.builder().scopeAwareProvider(configuration.scopeAwareProvider()).build();
    }

}
