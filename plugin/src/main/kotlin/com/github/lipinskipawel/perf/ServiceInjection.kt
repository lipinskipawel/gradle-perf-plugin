package com.github.lipinskipawel.perf

import org.gradle.api.file.ArchiveOperations
import javax.inject.Inject

interface ServiceInjection {
    @Inject
    fun getArchiveOperations(): ArchiveOperations
}
