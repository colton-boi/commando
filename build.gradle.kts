plugins {
    java
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

val projectVersion = "0.1.0"
group = "me.honkling.commando"
version = projectVersion

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "me.honkling.commando"
                artifactId = project.name
                version = projectVersion

                from(components["java"])
            }
        }
    }
}