middleware

plugins {
    `java-library`
}

dependencies {
    api(project(":lib:sisyphus-dto"))
    api(project(":lib:sisyphus-jackson"))
    api(Dependencies.hbase)
    implementation(project(":lib:sisyphus-common"))
}
