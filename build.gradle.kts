plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.7.1" apply false
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.fembuncollective"
version = "1.0.0"
description = "A library plugin for MissAnilka's projects"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/") // GeyserMC/Floodgate
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    
    // JetBrains Annotations
    compileOnly("org.jetbrains:annotations:24.1.0")
    
    // Floodgate API (optional integration)
    compileOnly("org.geysermc.floodgate:api:2.2.2-SNAPSHOT")
    
    // PlaceholderAPI (optional integration)
    compileOnly("me.clip:placeholderapi:2.11.6")
    
    // LuckPerms API (optional integration)
    compileOnly("net.luckperms:api:5.4")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    
    processResources {
        val props = mapOf(
            "version" to version,
            "description" to description
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    
    jar {
        archiveBaseName.set("RecreatorLib")
    }
    
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("RecreatorLib")
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    runServer {
        minecraftVersion("1.21")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("RecreatorLib")
                description.set(project.description)
                
                developers {
                    developer {
                        id.set("MissAnilka")
                        name.set("MissAnilka")
                    }
                }
            }
        }
    }
}
