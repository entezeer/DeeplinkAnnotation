// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.0-1.0.8")
    }
}


plugins {
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false

    id("org.jetbrains.kotlin.jvm") version "1.8.0" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}