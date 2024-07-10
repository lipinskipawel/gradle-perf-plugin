plugins {
    `java-library`
    id("com.github.lipinskipawel.perf") version ("0.1.0")
}

repositories {
    mavenCentral()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.assertJ)
            }
        }
    }
}
