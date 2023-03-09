middleware

plugins {
    `java-library`
}

description = "Middleware for using ElasticSearch in Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    api(libs.elastic.rest)
}
