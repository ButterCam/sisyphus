package com.bybutter.sisyphus.middleware.kafka

import com.bybutter.sisyphus.reflect.uncheckedCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import java.time.Duration
import kotlin.concurrent.thread

class KafkaConsumerLifecycle(
    private val consumer: KafkaConsumer<*, *>,
    private val listener: KafkaListener<Any, Any>,
    private val kafkaLoggers: List<KafkaLogger>
) : SmartLifecycle {

    private var working = false

    private var running = false

    private lateinit var worker: Deferred<Unit>

    private val logger = LoggerFactory.getLogger(listener.javaClass)

    @OptIn(DelicateCoroutinesApi::class)
    private val consumerContext by lazy {
        newSingleThreadContext("kafka-consumer-${consumer.groupMetadata().groupId()}")
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun start() {
        working = true

        worker = CoroutineScope(consumerContext).async {
            running = true
            while (working) {
                val offsets = mutableMapOf<TopicPartition, OffsetAndMetadata>()
                try {
                    val records = consumer.poll(Duration.ofMinutes(1))

                    records.forEach {
                        handleRecord(it)
                        offsets[TopicPartition(it.topic(), it.partition())] = OffsetAndMetadata(
                            it.offset() + 1,
                            it.leaderEpoch(),
                            "Consumed by $listener at ${System.currentTimeMillis()}"
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error occurred when polling message.", e)
                } finally {
                    if (offsets.isNotEmpty()) {
                        consumer.commitSync(offsets)
                    }
                }
            }
            running = false
        }
    }

    private suspend fun handleRecord(record: ConsumerRecord<*, *>) {
        val start = System.nanoTime()
        try {
            listener.consumeMessage(record.uncheckedCast())
            kafkaLoggers.forEach { logger ->
                logger.log(listener, consumer, record, System.nanoTime() - start, null)
            }
        } catch (e: Exception) {
            kafkaLoggers.forEach { logger ->
                logger.log(listener, consumer, record, System.nanoTime() - start, e)
            }
            throw e
        }
    }

    override fun stop() {
        logger.info("Starting to stop kafka consumer.")
        runBlocking {
            working = false
            worker.await()
            listener.shutdown()
            consumer.close()
            consumerContext.close()
        }
        logger.info("Kafka consumer stopped.")
    }

    override fun stop(callback: Runnable) {
        thread(name = "kafka-consumer-shutdown") {
            stop()
            callback.run()
        }
        logger.info("Kafka consumer stopped.")
    }
}
