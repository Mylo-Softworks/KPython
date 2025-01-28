plugins {
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

group = "com.mylosoftworks"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            groupId = "com.mylosoftworks"
            artifactId = "KPython"
            version = "1.0"
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("net.java.dev.jna:jna:5.14.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}