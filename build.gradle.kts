import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.3.30"
val commonsIoVersion = "2.6"
val slf4jApiVersion = "1.7.26"

val jUnitVersion = "5.4.2"
val spekVersion = "2.0.2"
val kluentVersion = "1.49"
val easyRandomVersion = "4.0.0.RC1"
val logbackVersion = "1.2.3"
val mockKVersion = "1.9.3"

fun findProperty(s: String) = project.findProperty(s) as String?

val binaryVersion = findProperty("version")
val bintrayUser = System.getenv("BINTRAY_USER") ?: findProperty("bintrayUser")
val bintrayApiKey = System.getenv("BINTRAY_API_KEY") ?: findProperty("bintrayApiKey")

plugins {
    `maven-publish`
    jacoco

    kotlin("jvm") version "1.3.21"
    id("io.gitlab.arturbosch.detekt").version("1.0.0.RC9")
    id("com.jfrog.bintray") version "1.8.4"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

group = "id.jasoet"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockKVersion")
    testImplementation("org.jeasy:easy-random-core:$easyRandomVersion")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
}

jacoco {
    toolVersion = "0.8.3"
}

tasks.jacocoTestReport {
    group = "Reporting"
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        csv.isEnabled = false
    }
}

detekt {
    toolVersion = "1.0.0-RC14"
    config = files("$rootDir/detekt.yml")
    filters = ".*test.*,.*/resources/.*,.*/tmp/.*"
}

tasks.test {
    finalizedBy(tasks.detekt, tasks.jacocoTestReport)

    useJUnitPlatform {
        includeEngines("junit-jupiter", "spek2")
    }

    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "failed", "skipped")
    }
}

tasks.withType<KotlinCompile> {

    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"

    kotlinOptions {
        jvmTarget = "1.8"
        apiVersion = "1.3"
        languageVersion = "1.3"
        allWarningsAsErrors = true
    }
}

val sourceJar by tasks.creating(Jar::class) {
    archiveClassifier.set("source")
    from(sourceSets.main.get().allSource)
    dependsOn(tasks.classes)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
    dependsOn(tasks.javadoc)
}

artifacts {
    archives(sourceJar)
    archives(javadocJar)
}

publishing {
    publications {
        create<MavenPublication>("FunPublication") {
            from(components["java"])
            artifactId = project.name
            groupId = "id.jasoet"
            version = binaryVersion
            artifact(sourceJar)
            artifact(javadocJar)
            pom {
                name.set("fun-kommand")
                description.set("Simple command-line wrapper for Kotlin")
                url.set("https://github.com/jasoet/fun-kommand")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://github.com/jasoet/fun-kommand")
                    }
                }
                developers {
                    developer {
                        id.set("jasoet")
                        name.set("Deny Prasetyo")
                        email.set("jasoet87@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/jasoet/fun-kommand")
                }
            }
        }
    }
}

bintray {
    user = bintrayUser.toString()
    key = bintrayApiKey.toString()
    setPublications("FunPublication")
    publish = true
    override = false

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        name = project.name
        desc = "Simple command-line wrapper for Kotlin"
        repo = "fun"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/jasoet/fun-kommand"
        issueTrackerUrl = "https://github.com/jasoet/fun-kommand/issues"
        setLabels("kotlin", "fun", "dsl")
        publicDownloadNumbers = true
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = "${project.version}"
            vcsTag = "${project.version}"
            desc = "Fun Kommand version ${project.version}."
        })
    })
}

tasks.wrapper {
    gradleVersion = "5.3.1"
}
