val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.10"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21"
}

group = "saketh"
version = "0.0.1"

application {
    mainClass.set("saketh.linkora.localization.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}
ktor {
    fatJar {
        archiveFileName.set("localizationServer.jar")
    }
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:${logback_version}")
    implementation("io.ktor:ktor-client-core:$kotlin_version")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    implementation("io.github.sakethpathike:kapsule:0.0.2")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
}

val availableLanguagesTask = tasks.register("availableLanguages") {
    doLast {
        val destinationFile = file("src/main/resources/raw/availableLanguages.txt")
        if (!destinationFile.exists()) {
            destinationFile.createNewFile()
        }
        destinationFile.writeText(text = "")
        file(path = "src/main/resources/raw").listFiles().filter {
            it.extension == "json"
        }.forEach {
            destinationFile.appendText(text = "${it.name}, ")
        }
    }
}

tasks.named<ProcessResources>("processResources"){
    dependsOn(availableLanguagesTask)
}