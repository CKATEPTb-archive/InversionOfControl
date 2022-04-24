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

val rootPackage = "${project.group}.ioc"
val internal = "${rootPackage}.internal"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks {
    shadowJar {
        relocate("com", "${internal}.com")
        relocate("javax", "${internal}.javax")
        relocate("org", "${internal}.org")
        excludes.remove("module-info.class")
    }
    register<ProGuardTask>("shrink") {
        dependsOn(shadowJar)
        injars(shadowJar.get().outputs.files)
        outjars("${project.buildDir}/libs/${project.name}-${project.version}.jar")

        ignorewarnings()

        libraryjars("${System.getProperty("java.home")}/jmods")

        keep("class module-info")
        keep(mapOf("includedescriptorclasses" to true), "public class !${internal}.** { *; }")
        keepattributes("Module*,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations")

        dontobfuscate()
        dontoptimize()
    }
    build {
//        dependsOn("shrink")
    }
    publish {
        dependsOn("shrink")
    }
    withType<JavaCompile> {
        modularity.inferModulePath.set(true)
        options.encoding = "UTF-8"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications {
        publications.create<MavenPublication>("maven") {
            artifacts {
                from(components["java"])
//                artifact(tasks.getByName("shrink").outputs.files.singleFile)
            }
        }
    }
}