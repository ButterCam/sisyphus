package com.bybutter.sisyphus.starter.grpc

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration
import org.springframework.stereotype.Component

/**
 *  When Sisyphus calls other service interfaces, It will give priority to see if the service is referenced locally,
 *  if requested, request the local service interfaces.
 *  The remote request will be initiated if the local request is not available.
 *  Requesting local services has little overhead for network requests,
 *  So it may be inaccurate to calculate the average time of the request。
 *  Use two [Timer] to count remote request and all requests separately.
 * */
@Component
class MicrometerRegistrar constructor(private var registry: MeterRegistry) {
    /**
     *  Only record remote requests
     * */
    private val remoteRequestTimer: Timer by lazy {
        registry.timer("sisyphus_remote_request")
    }
    /**
     * Log all requests
     * */
    private val allRequestTimer: Timer by lazy {
        registry.timer("sisyphus_all_request")
    }

    @Synchronized fun incrRemoteRequest(duration: Duration) {
        remoteRequestTimer.record(duration)
    }

    @Synchronized fun incrAllRequest(duration: Duration) {
        allRequestTimer.record(duration)
    }
}
