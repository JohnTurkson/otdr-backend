plugins {
    application
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.61"
}

group = "com.johnturkson.test"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-server-cio:1.2.6")
    implementation("io.ktor:ktor-client-cio:1.2.6")
    implementation("io.ktor:ktor-serialization:1.2.6")
}

application {
    mainClassName = "otdr.backend.server.ServerKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    
    wrapper {
        gradleVersion = "6.0.1"
        distributionType = Wrapper.DistributionType.ALL
    }
    
    jar {
        from(sourceSets.main.get().output)
        
        dependsOn(configurations.runtimeClasspath)
        
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map { zipTree(it) }
        })
        
        manifest {
            attributes["Manifest-Version"] = "1.0"
            attributes["Main-Class"] = "otdr.backend.server.ServerKt"
        }
    }
}
