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
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))
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
