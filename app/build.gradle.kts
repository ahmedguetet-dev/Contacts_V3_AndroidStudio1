import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Cle Google Maps : lue dans local.properties (non publie), puis gradle.properties
// (par ex. ~/.gradle/gradle.properties), puis variable d'environnement MAPS_API_KEY.
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val mapsApiKey: String = listOf(
    localProps.getProperty("MAPS_API_KEY"),
    project.findProperty("MAPS_API_KEY") as String?,
    System.getenv("MAPS_API_KEY")
).firstOrNull { !it.isNullOrBlank() }.orEmpty()

android {
    namespace = "com.example.contacts"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.contacts"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    testImplementation("junit:junit:4.13.2")
}
