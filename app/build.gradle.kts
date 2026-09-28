plugins {
    id("com.android.application")
}

android {
    namespace = "cn.rkbkosp.oppopushguard"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.rkbkosp.oppopushguard"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
}

android.packaging.resources.merges += "META-INF/xposed/*"
