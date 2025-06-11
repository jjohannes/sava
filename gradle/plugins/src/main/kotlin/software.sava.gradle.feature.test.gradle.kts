import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
  id("java")
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed", "standardOut", "standardError")
    exceptionFormat = FULL
    showStandardStreams = true
  }
}

val projectHasTests = project.layout.projectDirectory.dir("src/test/java").asFile.isDirectory

if (projectHasTests) {
  dependencies {
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  }

  // access catalog for junit version
  // https://github.com/gradle/gradle/issues/15383
  configurations.testImplementation {
    withDependencies {
      val libs = the<VersionCatalogsExtension>().named("libs")
      val junitVersion = libs.findLibrary("junit-jupiter").get().get().version
      // TODO jupiter-api should be in catalog
      add(project.dependencies.create("org.junit.jupiter:junit-jupiter-api:$junitVersion"))
    }
  }
}
