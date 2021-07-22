package com.bybutter.sisyphus.middleware.cache.dsl

import java.util.concurrent.TimeUnit

enum class CacheScenes(val duration: Long, val unit: TimeUnit) {
    /**
     * Cache for almost disposable values in temporary activities , it has 30 days cache time.
     */
    TEMPORORY(30, TimeUnit.DAYS),

    /**
     * Cache for almost immutable value, it has 12 hours cache time.
     */
    IMMUTABLE(12, TimeUnit.HOURS),

    /**
     * Cache for generic scenes, it has 30 minutes cache time.
     */
    NORMAL(30, TimeUnit.MINUTES),

    /**
     * Cache for unstable value, it has 1 minutes cache time.
     */
    UNSTABLE(1, TimeUnit.MINUTES),

    /**
     * No pain cache for value, short cache time just for dense operation, it has 10 seconds cache time.
     */
    NO_PAIN(10, TimeUnit.SECONDS),

    /**
     * Cache for debug, it will not cache anything.
     */
    DEBUG(0, TimeUnit.MILLISECONDS)
}
