import com.autonomousapps.DependencyAnalysisSubExtension
import com.autonomousapps.tasks.ProjectHealthTask

plugins {
  id("base")
  id("com.autonomousapps.dependency-analysis")
}

configure<DependencyAnalysisSubExtension> {
  issues { onAny { severity("fail") } }
}

tasks.check {
  dependsOn(tasks.withType<ProjectHealthTask>())
}
