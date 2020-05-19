lib

plugins {
    `java-library`
}

description = "Common lib of Sisyphus Project"

dependencies {
    compileOnly(Dependencies.Spring.Boot.boot)
}
