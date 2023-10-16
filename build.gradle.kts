plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }

    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots") // For snapshot builds

    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")

    maven(url = "https://maven.nucleoid.xyz/") { name = "Nucleoid" } // Server GUI
    maven { url= uri("https://maven.nucleoid.xyz") }

    maven("https://maven.impactdev.net/repository/development/") // Impactor
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")

    mappings("net.fabricmc:yarn:${property("yarn_mappings")}")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    modImplementation("com.cobblemon:fabric:1.4.0+1.20.1-SNAPSHOT")

    modImplementation("eu.pb4:sgui:1.2.2+1.20")
    include("eu.pb4:sgui:1.2.2+1.20")

    implementation("cloud.commandframework", "cloud-core", "1.8.4")
    implementation("net.impactdev.impactor.api:economy:5.1.1-SNAPSHOT")
}

tasks {

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        // select the repositories you want to publish to
        repositories {
            // uncomment to publish to the local maven
            // mavenLocal()
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}



// configure the maven publication