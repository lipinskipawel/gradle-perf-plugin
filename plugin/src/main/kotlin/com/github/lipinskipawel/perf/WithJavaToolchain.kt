package com.github.lipinskipawel.perf

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.jvm.toolchain.JavaLauncher

interface WithJavaToolchain {

    @Nested
    @Optional
    fun getJavaLauncher(): Property<JavaLauncher>

}
