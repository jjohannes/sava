plugins {
  id("maven-publish")
}

val backportJavaVersion = providers.gradleProperty("backportJavaVersion")

if (backportJavaVersion.isPresent) {
  val backportArtifactName = "${project.name}-jdk${backportJavaVersion.get()}"
  publishing {
    publications.withType<MavenPublication>().configureEach {
      artifactId = backportArtifactName
      suppressAllPomMetadataWarnings()
    }
  }
  listOf("apiElements", "runtimeElements").forEach { publishedVariant ->
    configurations.named(publishedVariant) {
      outgoing.capability("${project.group}:${backportArtifactName}:${project.version}") // preserve default capability
      outgoing.capability("${project.group}:${project.name}:${project.version}") // tip release capability
    }
  }
}
