import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.tans.tuiutils"
    compileSdk = properties["ANDROID_COMPILE_SDK"].toString().toInt()

    defaultConfig {
        minSdk = properties["ANDROID_MIN_SDK"].toString().toInt()
        version = properties["VERSION_NAME"].toString()

        buildConfigField("String", "VERSION", "\"${properties["VERSION_NAME"]}\"")

        consumerProguardFiles("consumer-rules.pro")
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
    kotlin {
        jvmToolchain(11)
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    compileOnly(libs.androidx.swiperefreshlayout)

    compileOnly(libs.rxjava3)
    compileOnly(libs.rxandroid3)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.core.jvm)
    implementation(libs.coroutines.android)

    testImplementation(libs.junit)
}

dependencies {
    implementation(libs.androidx.appcompat)
}

val publishProperties = Properties()
publishProperties.load(File(projectDir, "publish.properties").inputStream())

publishing {
    repositories {
        maven {
            name = "MavenCentralRelease"
            credentials {
                username = publishProperties.getProperty("MAVEN_USERNAME")
                password = publishProperties.getProperty("MAVEN_PASSWORD")
            }
            url = uri(publishProperties.getProperty("RELEASE_REPOSITORY_URL"))
        }
        maven {
            name = "MavenCentralSnapshot"
            credentials {
                username = publishProperties.getProperty("MAVEN_USERNAME")
                password = publishProperties.getProperty("MAVEN_PASSWORD")
            }
            url = uri(publishProperties.getProperty("SNAPSHOT_REPOSITORY_URL"))
        }
        maven {
            name = "MavenLocal"
            url = uri(File(rootProject.projectDir, "maven"))
        }
    }

    publications {
        val defaultPublication = this.create("Default", MavenPublication::class.java)
        with(defaultPublication) {
            groupId = publishProperties.getProperty("GROUP_ID")
            artifactId = publishProperties.getProperty("ARTIFACT_ID")
            version = publishProperties.getProperty("VERSION_NAME")

            afterEvaluate {
                artifact(tasks.getByName("bundleReleaseAar"))
            }
            val sourceCode by tasks.registering(Jar::class) {
                archiveClassifier.convention("sources")
                archiveClassifier.set("sources")
                from(android.sourceSets.getByName("main").java.srcDirs)
            }
            artifact(sourceCode)
            pom {
                name = "tUiUtils"
                description = "Android UI utils library."
                url = "https://github.com/Tans5/tUiUtils.git"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "tanpengcheng"
                        name = "tans5"
                        email = "tans.tan096@gmail.com"
                    }
                }
                scm {
                    url.set("https://github.com/tans5/tUiUtils.git")
                }
            }

            pom.withXml {
                val dependencies = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.all {
                    val dependency = this
                    if (dependency.group == null || dependency.version == null || dependency.name == "unspecified") {
                        return@all
                    }
                    val dependencyNode = dependencies.appendNode("dependency")
                    dependencyNode.appendNode("groupId", dependency.group)
                    dependencyNode.appendNode("artifactId", dependency.name)
                    dependencyNode.appendNode("version", dependency.version)
                    dependencyNode.appendNode("scope", "implementation")
                }
            }
        }
    }
}


signing {
    sign(publishing.publications.getByName("Default"))
}