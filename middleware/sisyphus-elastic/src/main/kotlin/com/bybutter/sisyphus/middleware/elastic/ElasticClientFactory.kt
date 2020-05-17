package com.bybutter.sisyphus.middleware.elastic

import org.elasticsearch.client.RestClient

interface ElasticClientFactory {
    fun createClient(property: ElasticProperty): RestClient
}
