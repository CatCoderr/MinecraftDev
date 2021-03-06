/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2018 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit

import com.demonwav.mcdev.buildsystem.BuildSystem
import com.demonwav.mcdev.platform.ProjectConfiguration
import com.demonwav.mcdev.platform.bukkit.data.LoadOrder
import com.demonwav.mcdev.util.runWriteTask
import com.intellij.ide.util.EditorHelper
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager

class BukkitProjectConfiguration : ProjectConfiguration() {

    var loadOrder = LoadOrder.POSTWORLD
    val loadBefore = mutableListOf<String>()
    val dependencies = mutableListOf<String>()
    val softDependencies = mutableListOf<String>()
    var prefix = ""
    var minecraftVersion = ""

    fun hasPrefix() = prefix.isNotBlank()

    fun hasLoadBefore() = listContainsAtLeastOne(loadBefore)
    fun setLoadBefore(string: String) {
        loadBefore.clear()
        loadBefore.addAll(commaSplit(string))
    }

    fun hasDependencies() = listContainsAtLeastOne(dependencies)
    fun setDependencies(string: String) {
        dependencies.clear()
        dependencies.addAll(commaSplit(string))
    }

    fun hasSoftDependencies() = listContainsAtLeastOne(softDependencies)
    fun setSoftDependencies(string: String) {
        softDependencies.clear()
        softDependencies.addAll(commaSplit(string))
    }

    fun hasWebsite() = !website.isNullOrBlank()

    override fun create(project: Project, buildSystem: BuildSystem, indicator: ProgressIndicator) {
        runWriteTask {
            indicator.text = "Writing main class"
            // Create plugin main class
            var file = buildSystem.sourceDirectory
            val files = mainClass.split(".").toTypedArray()
            val className = files.last()

            val packageName = mainClass.substring(0, mainClass.length - className.length - 1)
            file = getMainClassDirectory(files, file)

            val mainClassFile = file.findOrCreateChildData(this, className + ".java")

            BukkitTemplate.applyMainClassTemplate(project, mainClassFile, packageName, className)
            val pluginYml = buildSystem.resourceDirectory.findOrCreateChildData(this, "plugin.yml")
            BukkitTemplate.applyPluginDescriptionFileTemplate(project, pluginYml, this, buildSystem)

            // Set the editor focus on the main class
            PsiManager.getInstance(project).findFile(mainClassFile)?.let { mainClassPsi ->
                EditorHelper.openInEditor(mainClassPsi)
            }
        }
    }
}
