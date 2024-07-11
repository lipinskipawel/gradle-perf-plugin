package com.github.lipinskipawel.perf

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import org.gradle.process.JavaExecSpec
import java.io.File
import javax.inject.Inject

abstract class PerfBytecodeGeneratorTask : DefaultTask(), WithJavaToolchain {

    @Inject
    abstract fun getExecOperations(): ExecOperations

    @Classpath
    abstract fun getPerfClasspath(): ConfigurableFileCollection

    @Classpath
    abstract fun getRuntimeClasspath(): ConfigurableFileCollection

    @Classpath
    abstract fun getClassesDirsToProcess(): ConfigurableFileCollection

    @Input
    abstract fun getJvmArgs(): ListProperty<String?>

    @Input
    abstract fun getGeneratorType(): Property<String?>

    @OutputDirectory
    abstract fun getGeneratedSourcesDir(): DirectoryProperty

    @OutputDirectory
    abstract fun getGeneratedResourcesDir(): DirectoryProperty

    @TaskAction
    fun runTask() {
        cleanup(getGeneratedSourcesDir().get().asFile)

        for (classesDir in getClassesDirsToProcess()) {
            getExecOperations().javaexec { spec: JavaExecSpec ->
                spec.mainClass.set("org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator")
                spec.classpath(getPerfClasspath(), getRuntimeClasspath(), getClassesDirsToProcess())
                spec.args(
                    classesDir,
                    getGeneratedSourcesDir().get().asFile,
                    getGeneratedResourcesDir().get().asFile,
                    getGeneratorType().get()
                )
                spec.jvmArgs(getJvmArgs().get())
                val javaLauncher: Provider<JavaLauncher> = getJavaLauncher()
                if (javaLauncher.isPresent) {
                    spec.executable(javaLauncher.get().executablePath.asFile)
                }
            }
        }
    }

    private fun cleanup(file: File) {
        if (file.exists()) {
            val listing = file.listFiles()
            if (listing != null) {
                for (sub in listing) {
                    cleanup(sub)
                }
            }
            file.delete()
        }
    }
}
