package io.axoniq.demo.bikerental.rental

import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.xstream.XStream
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus
import org.axonframework.config.Configuration
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.deadline.DeadlineManager
import org.axonframework.deadline.SimpleDeadlineManager
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@EntityScan(basePackageClasses = [BikeStatus::class, SagaEntry::class, TokenEntry::class])
@SpringBootApplication
open class RentalApplication {

    @Bean(destroyMethod = "shutdown")
    open fun workerExecutorService(): ScheduledExecutorService =
        Executors.newScheduledThreadPool(4)

    @Autowired
    fun configureSerializers(xStream: XStream, objectMapper: ObjectMapper) {
        xStream.allowTypesByWildcard(arrayOf("io.axoniq.demo.bikerental.coreapi.**"))
        objectMapper.activateDefaultTyping(
            objectMapper.polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT
        )
    }

    @Autowired
    fun configure(config: EventProcessingConfigurer) {
        config.registerPooledStreamingEventProcessor(
            "PaymentSagaProcessor",
            { it.eventStore() },
            { _, builder ->
                builder.workerExecutor(workerExecutorService())
                    .batchSize(100)
                    .initialToken { source -> source.createHeadToken() }
            }
        )
        config.registerPooledStreamingEventProcessor(
            "io.axoniq.demo.bikerental.rental.query",
            { it.eventStore() },
            { _, builder ->
                builder.workerExecutor(workerExecutorService())
                    .batchSize(100)
            }

        )
    }

    @Bean
    open fun deadlineManager(configuration: Configuration): DeadlineManager =
        SimpleDeadlineManager.builder().scopeAwareProvider(configuration.scopeAwareProvider()).build()
}

fun main(args: Array<String>) {
    SpringApplication.run(RentalApplication::class.java, *args)
}
