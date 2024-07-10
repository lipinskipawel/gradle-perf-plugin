package com.github.lipinskipawel.perf

import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePerfPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin 'com.github.lipinskipawel.perf'")
            }
        }
    }
}
