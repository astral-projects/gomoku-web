import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.8.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

group = "pt.isel.daw"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    test {
        useJUnitPlatform()
        environment("DB_URL", System.getenv("DB_URL"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // for Spring web
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.0")

    // for Spring validation
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.4")

    // for jackson json library
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

    // for kotlin reflection that other tools need
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")

    // for JDBI
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.5.4")

    // to use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // to get password encode
    implementation("org.springframework.security:spring-security-core:6.0.2")

    // to use in SpringBootTest
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")

    // to use WebTestClient on tests
    testImplementation("org.springframework.boot:spring-boot-starter-webflux:3.0.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/**
 * DB related tasks
 * - To run `psql` inside the container, do
 *      docker exec -ti db-tests psql -d db -U dbuser -W
 *   and provide it with the same password as define on `tests/Dockerfile-nginx-db-test`
 */
task<Exec>("dbTestsUp") {
    commandLine("docker-compose", "up", "-d", "--build", "--force-recreate", "db-tests")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "db-tests", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbTestsUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker-compose", "down")
}

tasks.named("check") {
    dependsOn("dbTestsWait")
    finalizedBy("dbTestsDown")
}

task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree("$buildDir/libs/jvm-$version.jar"))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

task<Exec>("composeUp") {
    commandLine("docker-compose", "up", "--build", "--force-recreate")
    dependsOn("extractUberJar")
}

task<Exec>("composeDown") {
    commandLine("docker-compose", "down")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.9"
}
