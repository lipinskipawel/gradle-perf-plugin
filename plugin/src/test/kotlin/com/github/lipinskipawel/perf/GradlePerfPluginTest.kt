package com.github.lipinskipawel.perf

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class GradlePerfPluginTest {

    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.github.lipinskipawel.perf")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }
}
