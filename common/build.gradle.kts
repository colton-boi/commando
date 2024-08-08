plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "me.honkling.commando"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))
}

kotlin {
    jvmToolchain(17)
}