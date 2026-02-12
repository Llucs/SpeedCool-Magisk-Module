plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.llucs.speedcool"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.llucs.speedcool"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)  // ← Mais moderno que kotlinOptions { jvmTarget = "17" }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // kotlinCompilerExtensionVersion = "1.5.15" 

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")  // Opcional: crie um baseline depois
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.01.01")  // ← Atualize para latest 2026
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.0")  // Pode atualizar para 2.8.1 se disponível
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // libsu: atualize para latest (6.0.0) - fixes e melhorias
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    // Se precisar de root service (recomendado para apps Magisk/root):
    // implementation("com.github.topjohnwu.libsu:service:6.0.0")
}