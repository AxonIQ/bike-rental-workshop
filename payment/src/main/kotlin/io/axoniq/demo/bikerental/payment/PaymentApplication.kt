package io.axoniq.demo.bikerental.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.xstream.XStream
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@EntityScan(basePackageClasses = [PaymentStatus::class, TokenEntry::class])
@SpringBootApplication
open class PaymentApplication {

    @Bean(destroyMethod = "shutdown")
    open fun workerExecutorService(): ScheduledExecutorService =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())

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
            "io.axoniq.demo.bikerental.payment",
            { it.eventStore() },
            { _, builder ->
                builder.workerExecutor(workerExecutorService())
                    .batchSize(100)
            }
        )
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(PaymentApplication::class.java, *args)
}
