plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
    // Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// DB
	runtimeOnly("com.mysql:mysql-connector-j")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Swagger
	// https://mvnrepository.com/artifact/io.swagger.core.v3/swagger-gradle-plugin
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	// Spring AOP
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.retry:spring-retry")

	// Redis
	implementation ("org.redisson:redisson-spring-boot-starter:3.24.3")
	testImplementation("com.github.codemonstur:embedded-redis:1.0.0")

	testImplementation ("org.testcontainers:junit-jupiter:1.19.7")

	// https://mvnrepository.com/artifact/io.micrometer/micrometer-registry-prometheus
	implementation("io.micrometer:micrometer-registry-prometheus:1.14.5")
	implementation("io.micrometer:micrometer-core")

}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
	systemProperty("spring.profiles.active", "test")
}
