plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  implementation("org.gradlex:java-module-dependencies:1.9.1")
  implementation("org.gradlex:java-module-testing:1.7")
  implementation("org.gradlex:jvm-dependency-conflict-resolution:2.4")
  implementation("com.github.iherasymenko.jlink:jlink-plugin:0.7")
}
