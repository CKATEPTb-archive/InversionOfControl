import proguard.gradle.ProGuardTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.2.1")
    }
}

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow").version("7.1.0")
}

group = "ru.ckateptb.commons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks {
    shadowJar {
        minimize()
        archiveFileName.set("${project.name}-${project.version}-full.jar")
    }
    register<ProGuardTask>("shrink") {

    }
    build {
        dependsOn("shrink")
    }
    publish {
        dependsOn("shrink")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<ProGuardTask> {
        dependsOn(shadowJar)
        injars(shadowJar)
        dontobfuscate()
        dontoptimize()
        ignorewarnings()
        keepattributes("RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations")
        keep("class ${project.group}.** { *; }")
        libraryjars("${System.getProperty("java.home")}/lib/rt.jar")
        libraryjars("${System.getProperty("java.home")}/lib/jce.jar")
        libraryjars(sourceSets.main.get().compileClasspath)
        outjars("${project.buildDir}/libs/${project.name}-${project.version}-shrink.jar")
    }
    named<Copy>("processResources") {
        from("LICENSE") {
            rename { "${project.name.toUpperCase()}_${it}" }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

publishing {
    publications {
        publications.create<MavenPublication>("maven") {
            artifacts {
                artifact(tasks.shadowJar) {
                    classifier = "full"
                }
                artifact(tasks["sourcesJar"])
                artifact(tasks.getByName("shrink").outputs.files.singleFile)
            }
        }
    }
}