plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.typewritermc.module-plugin") version "1.1.2"
}

group = "dev.arctic"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://maven.typewritermc.com/releases") {
        name = "typewriter"
    }
    maven("https://maven.typewritermc.com/beta")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

typewriter {
    namespace = "arcticdev"

    extension {
        name = "ValidTW"
        shortDescription = "Extension to validate typewriter files and nodes."
        description =
            "This extension is used to validate typewriter files and nodes for circular dependencies when referencing NPCs, tasks, and such."
        engineVersion = "0.8.0-beta-153"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper {}
    }
}
