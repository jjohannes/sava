plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  implementation("com.autonomousapps:dependency-analysis-gradle-plugin:2.18.0")
  implementation("com.github.iherasymenko.jlink:jlink-plugin:0.7")
}
