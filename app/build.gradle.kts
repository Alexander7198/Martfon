import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.martfon"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.martfon"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin { // <-- Новый блок kotlin на верхнем уровне
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) // <-- Переносим сюда
        }
    }

    buildFeatures {
        compose = true
    }
}

// app/build.gradle.kts
dependencies {
    // Основные зависимости Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // Jetpack Compose для UI
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Для ViewModel в Compose
    implementation("androidx.navigation:navigation-compose:2.7.7") // Для навигации между экранами

    // База данных Room (для локального хранения сообщений)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") // Поддержка Kotlin coroutines
    ksp("androidx.room:room-compiler:2.6.1") // Обработка аннотаций Room (нужен плагин KSP)

    // Сеть (пока для простоты, позже заменим на ваш протокол)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Kotlin Coroutines для асинхронных операций
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Для загрузки изображений (опционально, для аватарок)
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Retrofit с Gson
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

}

private fun DependencyHandlerScope.ksp(string: String) {}

private fun String.set(jvm11: JvmTarget) {}
