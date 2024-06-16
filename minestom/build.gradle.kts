plugins {
    java
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "me.honkling.commando"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io/")
}

dependencies {
    api(project(":common"))
    compileOnly("net.minestom:minestom-snapshots:7320437640")
    compileOnly(kotlin("stdlib-jdk8"))
}

tasks.withType<ProcessResources> {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

kotlin {
    jvmToolchain(17)
}
