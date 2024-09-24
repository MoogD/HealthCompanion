plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":utils"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.test)
}
