plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  implementation("com.gradleup.nmcp:nmcp:0.1.5")
  implementation("com.github.iherasymenko.jlink:jlink-plugin:0.7")
}
