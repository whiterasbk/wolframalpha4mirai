plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.12.3"
}

group = "bot.query.wolframalpha.whiter"
version = "1.5"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    api("org.json:json:20220320")
//    api("io.ktor:ktor-client-core:2.1.0")
//    api("io.ktor:ktor-client-okhttp:2.1.0")
}