package com.bybutter.sisyphus.k8s.gradle

class KubernetesResource<T>(val cluster: KubernetesCluster, val kind: String, val namespace: String, val name: String)
