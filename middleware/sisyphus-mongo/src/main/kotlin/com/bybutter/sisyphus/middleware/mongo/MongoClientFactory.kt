package com.bybutter.sisyphus.middleware.mongo

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients

interface MongoClientFactory {
    fun createClient(qualifier: Class<*>, property: MongoDatabaseProperty): MongoClient
}

class DefaultMongoClientFactory : MongoClientFactory {
    override fun createClient(qualifier: Class<*>, property: MongoDatabaseProperty): MongoClient {
        return MongoClients.create(property.url)
    }
}
