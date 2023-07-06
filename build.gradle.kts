import java.text.SimpleDateFormat
import java.util.*

version = SimpleDateFormat("yyyy.M.d").format(Date())
group = "io.geekload"

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
}

gradlePlugin {
    website.set("https://geekload.io/documentation/integration#gradle-plugin")
    vcsUrl.set("https://github.com/iDispatchPro/GeekLoadGradlePlugin")

    plugins {
        create("geekload") {
            id = "$group.gradle"
            implementationClass = "io.geekload.gradle.GeekLoad"
            displayName = "GeekLoad plugin"
            description = "Performing load tests during project build"
            tags.set(listOf("load", "test", "javascript"))
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}