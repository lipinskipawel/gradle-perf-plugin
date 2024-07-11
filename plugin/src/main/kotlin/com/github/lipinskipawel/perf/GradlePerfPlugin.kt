package com.github.lipinskipawel.perf

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy.INCLUDE
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaToolchainService

class GradlePerfPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)

        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)

        val perfSourceSet = sourceSets.create("perf") { perf ->
            perf.compileClasspath += main.output
            perf.runtimeClasspath += main.output
        }

        val perfConfiguration = project.configurations.findByName("perfImplementation")
            ?.extendsFrom(project.configurations.findByName("implementation"))
        perfConfiguration?.isCanBeResolved = true

        addPerfDependency(perfConfiguration, project)

        val runtimeConfiguration = project.configurations.findByName("perfRuntimeClasspath")
        runtimeConfiguration?.isCanBeConsumed = false
        runtimeConfiguration?.setVisible(false)
        runtimeConfiguration?.extendsFrom(project.configurations.getByName("perfImplementation"))
        runtimeConfiguration?.extendsFrom(project.configurations.getByName("runtimeClasspath"))

        val perfGeneratedSourcesDir = project.layout.buildDirectory.dir("perf-generated-sources")
        val perfGeneratedClassesDir = project.layout.buildDirectory.dir("perf-generated-classes")
        val perfGeneratedResourcesDir = project.layout.buildDirectory.dir("perf-generated-resources")

        val java = project.extensions.getByType(JavaPluginExtension::class.java)
        val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)

        createPerfRunBytecodeGeneratorTask(
            project,
            perfSourceSet,
            perfGeneratedSourcesDir,
            perfGeneratedResourcesDir,
            java,
            toolchainService
        )
        createPerfCompileGeneratedClassesTask(
            project,
            perfSourceSet,
            perfGeneratedSourcesDir,
            perfGeneratedClassesDir,
            java,
            toolchainService
        )

        val perfJar = createStandardPerfJar(
            project,
            main,
            perfSourceSet,
            perfGeneratedSourcesDir,
            perfGeneratedClassesDir,
            perfGeneratedResourcesDir,
            runtimeConfiguration
        )

        project.tasks.register("perf", PerfTask::class.java) { task ->
            task.getPerfClasspath().from(perfConfiguration)
            task.getJarArchive().set(perfJar.flatMap { it.archiveFile })
        }

        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin 'com.github.lipinskipawel.perf'")
            }
        }
    }

    private fun createStandardPerfJar(
        project: Project,
        main: SourceSet,
        perf: SourceSet,
        perfGeneratedSourcesDir: Provider<Directory>,
        perfGeneratedClassesDir: Provider<Directory>,
        perfGeneratedResourcesDir: Provider<Directory>,
        runtimeConfiguration: Configuration?
    ): TaskProvider<Jar> {
        return project.tasks.register("perfJar", Jar::class.java) { spec ->
            val archives = project.objects.newInstance(ServiceInjection::class.java).getArchiveOperations()
            spec.javaClass
            spec.inputs.files(main.output)
            spec.inputs.files(perf.output)
            spec.from(perf.output)
            spec.from(main.output)
            spec.from(perfGeneratedSourcesDir)
            spec.from(perfGeneratedClassesDir)
            spec.from(perfGeneratedResourcesDir)
            spec.dependsOn("perfCompileGeneratedClasses")
            spec.duplicatesStrategy = INCLUDE

            val metaInfoExcludes = listOf("module-info.class", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
            spec.from(runtimeConfiguration?.elements?.map { elements ->
                elements
                    .map { it.asFile }
                    .filter { it.isDirectory || it.name.lowercase().endsWith(".jar") }
                    .map {
                        if (it.isDirectory) {
                            it
                        } else archives.zipTree(it)
                    }
                    .toMutableSet()
            }).exclude(metaInfoExcludes)

            spec.archiveBaseName.set("perf")
            spec.manifest {
                it.attributes["Main-Class"] = "org.openjdk.jmh.Main"
            }
            spec.isZip64 = true
        }
    }

    private fun addPerfDependency(perfConfiguration: Configuration?, project: Project) {
        perfConfiguration?.dependencies?.addAll(
            listOf(
                project.dependencies.create("org.openjdk.jmh:jmh-core:1.37"),
                project.dependencies.create("org.openjdk.jmh:jmh-generator-bytecode:1.37")
            )
        )
    }

    private fun createPerfRunBytecodeGeneratorTask(
        project: Project,
        perfSourceSet: SourceSet,
        perfGeneratedSourcesDir: Provider<Directory>,
        perfGeneratedResourcesDir: Provider<Directory>,
        java: JavaPluginExtension,
        toolchainService: JavaToolchainService
    ) {
        project.tasks.register("perfRunBytecodeGenerator", PerfBytecodeGeneratorTask::class.java) { spec ->
            spec.group = "perf"
            spec.getPerfClasspath().from(project.configurations.findByName("perfImplementation"))
            spec.getGeneratorType().convention("default")
            spec.getGeneratedSourcesDir().set(perfGeneratedSourcesDir)
            spec.getGeneratedResourcesDir().set(perfGeneratedResourcesDir)
            spec.getRuntimeClasspath().from(perfSourceSet.runtimeClasspath)
            spec.getClassesDirsToProcess().from(perfSourceSet.output.classesDirs)
            spec.getJavaLauncher().convention(toolchainService.launcherFor(java.toolchain))
        }
    }

    private fun createPerfCompileGeneratedClassesTask(
        project: Project,
        perfSourceSet: SourceSet,
        perfGeneratedSourcesDir: Provider<Directory>,
        perfGeneratedClassesDir: Provider<Directory>,
        java: JavaPluginExtension,
        toolchainService: JavaToolchainService
    ) {
        project.tasks.register("perfCompileGeneratedClasses", JavaCompile::class.java) { spec ->
            spec.group = "perf"
            spec.dependsOn("perfRunBytecodeGenerator")

            spec.classpath = perfSourceSet.runtimeClasspath
            spec.source(perfGeneratedSourcesDir)
            spec.destinationDirectory.set(perfGeneratedClassesDir)
            spec.javaCompiler.convention(toolchainService.compilerFor(java.toolchain))
        }
    }
}
