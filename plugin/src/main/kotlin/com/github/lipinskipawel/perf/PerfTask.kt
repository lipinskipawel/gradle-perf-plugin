package com.github.lipinskipawel.perf

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class PerfTask : DefaultTask() {

    @Inject
    abstract fun getExecOperations(): ExecOperations

    @Inject
    abstract fun getObjects(): ObjectFactory

    @Classpath
    abstract fun getPerfClasspath(): ConfigurableFileCollection

    @InputFile
    abstract fun getJarArchive(): RegularFileProperty

    @TaskAction
    fun runTask() {
        getExecOperations().javaexec { spec ->
            spec.setClasspath(computeClasspath())
            spec.mainClass.set("org.openjdk.jmh.Main")
        }
    }

    private fun computeClasspath(): FileCollection {
        val classpath: ConfigurableFileCollection = getObjects().fileCollection()
        classpath.from(getPerfClasspath())
        classpath.from(getJarArchive())
        return classpath
    }
}
