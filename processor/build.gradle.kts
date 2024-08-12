plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("kotlin")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}


dependencies {
    api(project(":annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.0-1.0.8")
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoetKsp)
}