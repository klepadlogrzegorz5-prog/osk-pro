  signingConfigs {
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
    val keystoreFile = file("${rootDir}/release.keystore")
    if (keystoreFile.exists()) {
      create("release") {
        storeFile = keystoreFile
        storePassword = "android123"
        keyAlias = "release"
        keyPassword = "android123"
        enableV1Signing = true
        enableV2Signing = true
        enableV3Signing = true
        enableV4Signing = true
      }
    }
  }
  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = if (file("${rootDir}/release.keystore").exists()) signingConfigs.getByName("release") else signingConfigs.getByName("debugConfig")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
