plugins {
  id("java")
}

@Suppress("UnstableApiUsage")
val mainJavaVersion = providers.fileContents(
  isolated.rootProject.projectDirectory.file("gradle/main-jdk.txt")).asText.map { it.trim() }
val backportJavaVersion =
  providers.gradleProperty("backportJavaVersion")
val jlv = JavaLanguageVersion.of(backportJavaVersion.orElse(mainJavaVersion).get())

java {
  toolchain.languageVersion = jlv
}
