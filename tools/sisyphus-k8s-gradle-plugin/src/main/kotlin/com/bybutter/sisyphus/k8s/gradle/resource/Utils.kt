package com.bybutter.sisyphus.k8s.gradle.resource

import io.kubernetes.client.openapi.models.V1ObjectMeta
import java.time.LocalDateTime

internal fun patchRestartMetadata(metadata: V1ObjectMeta?) {
    metadata?.putAnnotationsItem(
        "kubectl.kubernetes.io/restartedAt",
        LocalDateTime.now().toString(),
    )
}
