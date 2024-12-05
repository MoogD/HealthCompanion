plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
    alias(libs.plugins.mockposable)
    alias(libs.plugins.room)
}

android {
    namespace = "com.dom.healthcompanion"
    compileSdkVersion(rootProject.extra["compileSdkVersion"] as Int)

    defaultConfig {
        applicationId = "com.dom.healthcompanion"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    tasks.withType<Test> {
        maxParallelForks = 5
        useJUnitPlatform()
        enabled = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // internal modules
    implementation(project(":timer"))
    implementation(project(":utils"))
    implementation(project(":android-utils"))
    implementation(project(":logger"))
    testImplementation(project(":test-utils"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.navigation)
    implementation(libs.compose.constraint.layout)

    // DI
    implementation(libs.hilt)
    implementation(libs.hilt.compose)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.android.compiler)

    // unit tests
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform)
    testRuntimeOnly(libs.junit.vintage)
    testImplementation(libs.mockk)
    testImplementation(libs.hamcrest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // compose tests
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.robolectric)
    // needed to resolve activity with roboletics unit tests
    debugImplementation(libs.fragment.testing)
    testImplementation(libs.androidx.navigation.testing)
    kspTest(libs.hilt.android.compiler)
    testImplementation(libs.hilt.testing)

    // room
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)
    implementation(libs.gson)

    // kover report
    kover(project(":timer"))
    kover(project(":utils"))
    kover(project(":android-utils"))
}
mockposable {
    plugins = listOf("mockk", "compose-ui")
    composeCompilerPluginVersion = libs.versions.kotlin.get()
}
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*BuildConfig",
                    // exclude DI
                    "dagger.hilt.internal.aggregatedroot.codegen.*",
                    "hilt_aggregated_deps.*",
                    "com.dom.healthcompanion.di.*",
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*_Factory*",
                    "*_MembersInjector*",
                    "*_Impl*",
                    "*ComposableSingletons*",
                    // exclude ui specifics
                    "com.dom.healthcompanion.ui.theme.*",
                    "com.dom.healthcompanion.HealthCompanionApplication",
                    "com.dom.healthcompanion.ui.main.MainActivity*",
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }
    }
}
