import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)

    alias(libs.plugins.paper)
    alias(libs.plugins.paper.run)

    alias(libs.plugins.yml)
}

repositories {
    mavenCentral()
}

dependencies {
    library(kotlin("stdlib"))
    implementation(libs.metrics)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform)

    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

version = ProcessBuilder("git", "describe", "--tags", "--always", "--dirty")
    .directory(project.projectDir)
    .start()
    .inputStream
    .bufferedReader()
    .readText()
    .trim()

tasks {
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        from("LICENSE")

        filePermissions {
            user.read = true
            user.write = true
            user.execute = false

            group.read = true
            group.write = false
            group.execute = false

            other.read = true
            other.write = false
            other.execute = false
        }

        dirPermissions {
            user.read = true
            user.write = true
            user.execute = true

            group.read = true
            group.write = false
            group.execute = true

            other.read = false
            other.write = false
            other.execute = true
        }
    }

    withType<ShadowJar> {
        from("assets/text/licenses") {
            into("licenses")
        }

        archiveClassifier = ""

        enableAutoRelocation = true
        relocationPrefix = "io.gitlab.shdima.throwables.dependencies"

        minimizeJar = true
    }

    jar {
        enabled = false
    }

    runServer {
        val noProxy = (project.findProperty("noProxy") as? String)?.toBoolean() == true

        if (!noProxy) {
            downloadPlugins {
                github("playit-cloud", "playit-minecraft-plugin", "v0.1.4", "playit-minecraft-plugin.jar")
            }
        }
    }
}

bukkit {
    name = "ThrowableSpawnEggs"
    description = "A Minecraft Paper plugin that makes every spawn egg throwable for a more fun way to spawn mobs"

    main = "io.gitlab.shdima.throwables.ThrowableSpawnEggs"
    apiVersion = "1.20"
    version = project.version.toString()

    authors = listOf(
        "Esoteric Enderman",
        "rolyPolyVole",
    )

    website = "https://github.com/sh-dima/throwable-spawn-eggs"
}
