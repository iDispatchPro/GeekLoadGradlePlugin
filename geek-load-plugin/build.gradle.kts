plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.7.0"
}
repositories {
    mavenCentral()
    google()
}

version = "2023.06.23"
group = "house-of-geeks.geek-load"

gradlePlugin {
    website.set("https://geekload.io")
    vcsUrl.set("https://github.com/iDispatchPro/GeekLoadGradlePlugin")

    plugins {
        create("GeetLoad") {
            id = group.toString()
            implementationClass = "com.houseofgeeks.gradle.geekload.GeekLoadPlugin"
            displayName = "GeekLoad plugin"
            description = "Performing load tests during project build"
            tags.set(listOf("load", "test", "javascript"))
        }
    }
}

//apply<GreetingPlugin>()