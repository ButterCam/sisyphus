middleware

plugins {
    `java-library`
}

dependencies {
    implementation(project(":lib:sisyphus-dto"))
    implementation(project(":lib:sisyphus-jackson"))

    api(Dependencies.Spring.Boot.amqp)
}
