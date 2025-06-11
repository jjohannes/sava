plugins {
  id("software.sava.gradle.feature.jlink")
}

dependencies {
  implementation(project(":sava-core"))
}

jlinkApplication {
  applicationName = "vanity"
  mainClass = "software.sava.vanity.Entrypoint"
  mainModule = "software.sava.vanity"
}
