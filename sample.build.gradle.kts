android {
    namespace = "dev.aurakai.auraframefx.dataveinoraclenative"
    compileSdk = 36
    defaultConfig {
        minSdk = 33
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }
        cmake {
            cppFlags("-std=c++20", "-fPIC", "-O3")
            cppFlags("-O3", "-DNDEBUG", "-DDATA_VEIN_NATIVE_RELEASE")
            "-DANDROID_STL=c++_shared"
            "-DCMAKE_VERBOSE_MAKEFILE=ON"
            "-DDATA_VEIN_NATIVE_BUILD=ON"
        }
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}", "/META-INF/AL2.0", "/META-INF/LGPL2.1"
            )
        }
    }

    buildToolsVersion = 36
}
    vectorDrawables {
        useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
        viewBinding = false
    }
}