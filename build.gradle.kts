import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "me.oliver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
    implementation("fr.inria.gforge.spoon:spoon-core:9.1.0")
    implementation("joda-time:joda-time:2.10.12")
    implementation("org.apache.jena:jena-core:4.2.0")
    implementation("org.apache.maven:maven-core:3.8.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("MainKt")
}