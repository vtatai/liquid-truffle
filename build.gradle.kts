plugins {
	kotlin("jvm") version "2.0.21"
	application
	jacoco
}

group = "io.github.liquidTruffle"
version = "0.1.0"

kotlin {
	jvmToolchain(18)
}

repositories {
	mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
	implementation("org.graalvm.truffle:truffle-api:24.0.2")
	compileOnly("org.graalvm.truffle:truffle-dsl-processor:24.0.2")
	annotationProcessor("org.graalvm.truffle:truffle-dsl-processor:24.0.2")
    implementation("org.graalvm.polyglot:polyglot:24.0.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
	testImplementation("org.assertj:assertj-core:3.25.3")
}

application {
	mainClass.set("io.github.liquidTruffle.Main")
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}
