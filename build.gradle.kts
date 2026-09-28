plugins {
    kotlin("jvm") version "1.9.24"
    application
}

// Los repositorios se declaran en settings.gradle.kts (FAIL_ON_PROJECT_REPOS),
// por eso aquí NO va el bloque repositories { }.

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
