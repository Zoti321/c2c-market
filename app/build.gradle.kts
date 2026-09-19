import java.util.Properties
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoReportBase

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    jacoco
}

android {
    namespace = "com.zoti321.c2cmarket"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zoti321.c2cmarket"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "com.zoti321.c2cmarket.HiltTestRunner"
    }

    signingConfigs {
        create("release") {
            val propsFile = rootProject.file("keystore.properties")
            if (propsFile.exists()) {
                val props = Properties().apply { load(propsFile.inputStream()) }
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val propsFile = rootProject.file("keystore.properties")
            if (propsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    lint {
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = false
        lintConfig = file("lint.xml")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/config/detekt.yml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

val jacocoExcludes = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*\$Lambda\$*.*",
    "**/*\$inlined\$*.*",
    "**/Hilt_*.*",
    "**/*_HiltModules*.*",
    "**/*_MembersInjector*.*",
    "**/dagger/**",
    "**/*Module*.*",
    "**/*_Factory*.*",
    "**/*_Impl*.*",
    "**/*ComposableSingletons*.*",
    "**/di/**",
)

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class.java) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

fun configureJacocoSources(task: JacocoReportBase) {
    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(jacocoExcludes)
    }
    task.classDirectories.setFrom(debugTree)
    task.sourceDirectories.setFrom(files("src/main/java"))
    task.executionData.setFrom(
        layout.buildDirectory.file(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
        ),
    )
}

tasks.register<JacocoReport>("jacocoDebugUnitTestReport") {
    description = "Generate JaCoCo coverage report for debug unit tests"
    dependsOn("testDebugUnitTest")
    configureJacocoSources(this)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoDebugUnitTestCoverageVerification") {
    description = "Verify debug unit test line coverage meets the minimum threshold"
    dependsOn("jacocoDebugUnitTestReport")
    configureJacocoSources(this)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = 0.10.toBigDecimal()
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    doFirst {
        val propsFile = rootProject.file("keystore.properties")
        if (!propsFile.exists()) {
            throw GradleException(
                "Release 构建需要 keystore.properties。请复制 keystore.properties.example 并填写签名信息。",
            )
        }
    }
}
