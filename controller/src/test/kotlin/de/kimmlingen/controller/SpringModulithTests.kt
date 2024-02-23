package de.kimmlingen.controller

import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import kotlin.test.Test

class SpringModulithTests {
    private var modules: ApplicationModules = ApplicationModules.of("de.kimmlingen")

    @Test
    fun shouldBeCompliant() {
        modules.verify()
    }

    @Test
    fun writeDocumentationSnippets() {
        Documenter(modules)
            .writeModuleCanvases()
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
    }
}
