package com.bybutter.sisyphus.middleware.kafka

import com.bybutter.sisyphus.reflect.uncheckedCast
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
    private val kafkaLoggers: List<KafkaLogger>,
    private val exceptionHandler: KafkaExceptionHandler
) : SmartLifecycle {

    private var working = false

    private var running = false

    private lateinit var worker: Thread

    private val logger = LoggerFactory.getLogger(listener.javaClass)

    override fun isRunning(): Boolean {
        return running
    }

    private fun MutableMap<TopicPartition, OffsetAndMetadata>.commit(record: ConsumerRecord<*, *>) {
        this[TopicPartition(record.topic(), record.partition())] = OffsetAndMetadata(
            record.offset() + 1,
            record.leaderEpoch(),
            ""
        )
    }

    override fun start() {
        working = true

        worker = thread(name = consumer.groupMetadata().groupId()) {
            runBlocking {
                running = true
                while (working) {
                    try {
                        val offsets = mutableMapOf<TopicPartition, OffsetAndMetadata>()
                        val records = consumer.poll(Duration.ofMinutes(1))

                        for (it in records) {
                            try {
                                handleRecord(it)
                            } catch (e: Exception) {
                                when (exceptionHandler.onException(it, e)) {
                                    KafkaExceptionPolicy.RETRY -> break
                                    KafkaExceptionPolicy.SKIP -> {}
                                    KafkaExceptionPolicy.STOP -> {
                                        logger.info("Starting to stop kafka due to exception policy.", e)
                                        working = false
                                        break
                                    }
                                }
                            }
                            offsets.commit(it)
                        }

                        if (offsets.isNotEmpty()) {
                            consumer.commitSync(offsets)
                        }
                    } catch (e: Exception) {
                        logger.error("Error occurred when handle kafka messages.", e)
                    }
                }
                running = false

                listener.shutdown()
                consumer.close()

                logger.info("Kafka consumer stopped.")
            }
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
        if (!running) return
        logger.info("Starting to stop kafka consumer.")
        working = false
        worker.join()
    }

    override fun stop(callback: Runnable) {
        if (!running) return
        thread(name = "kafka-consumer-shutdown") {
            stop()
            callback.run()
        }
    }
}
