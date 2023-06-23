package com.houseofgeeks.gradle.geekload

import org.gradle.api.*
import org.gradle.api.Project

class GeekLoadPlugin : Plugin<Project>
{
    override fun apply(project : Project)
    {
        project.task("geek-load") {
            it.doLast {
                project.fileTree(mapOf("dir" to "src/test", "include" to "**/*.gl-js")).forEach { file ->
                    println(file)
                }
            }
        }
    }
}