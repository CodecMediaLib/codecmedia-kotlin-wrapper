plugins {
    kotlin("jvm") version "2.1.20"
    `java-library`
    `maven-publish`
    signing
}

group   = "me.tamkungz.codecmedia"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // CodecMedia Java core
    api("me.tamkungz.codecmedia:codecmedia:1.1.0")

    // Kotlin stdlib is added automatically by the Kotlin plugin
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

// ---------------------------------------------------------------------------
// Publishing (optional – fill in credentials when publishing to Maven Central)
// ---------------------------------------------------------------------------
publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            artifactId = "codecmedia-kotlin"
            from(components["java"])

            pom {
                name.set("CodecMedia Kotlin")
                description.set("Idiomatic Kotlin DSL wrapper for the CodecMedia Java library.")
                url.set("https://github.com/CodecMediaLib/codecmedia-kotlin")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("tamkungz")
                        name.set("TamKungZ_")
                        email.set("kittiwut.pimpromma@gmail.com")
                        timezone.set("Asia/Bangkok")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/CodecMediaLib/codecmedia-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com:CodecMediaLib/codecmedia-kotlin.git")
                    url.set("https://github.com/CodecMediaLib/codecmedia-kotlin")
                }
            }
        }
    }

    repositories {
        // Publish locally for testing:  ./gradlew publishToMavenLocal
        mavenLocal()

        // Maven Central (Sonatype Central Portal publisher API)
        maven {
            name = "sonatype"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = providers.gradleProperty("sonatypeUsername").orNull
                password = providers.gradleProperty("sonatypePassword").orNull
            }
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signingKey").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull

    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenKotlin"])
    }
}
