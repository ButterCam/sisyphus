package com.bybutter.sisyphus.middleware.kafka

enum class KafkaExceptionPolicy {
    /**
     * Don't commit the offset, retry current message in next poll.
     */
    RETRY,

    /**
     * Skip current message, commit the offset.
     */
    SKIP,

    /**
     * Don't commit the offset, stop the consumer.
     */
    STOP
}
