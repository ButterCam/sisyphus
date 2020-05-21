package com.bybutter.sisyphus.middleware.elastic

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient

open class DefaultElasticClientFactory : ElasticClientFactory {
    override fun createClient(property: ElasticProperty): RestClient {
        return createElasticClient(property.host, property.port, property)
    }

    protected fun createElasticClient(
        host: String,
        port: Int,
        property: ElasticProperty
    ): RestClient {
        val credentialsProvider = property.userName?.let {
            BasicCredentialsProvider().apply {
                setCredentials(AuthScope.ANY, UsernamePasswordCredentials(property.userName, property.password))
            }
        }

        return RestClient.builder(
                HttpHost(host, port, property.protocol)
        ).setHttpClientConfigCallback {
            if (credentialsProvider != null) {
                it.setDefaultCredentialsProvider(credentialsProvider)
            }
            it
        }.build()
    }
}
