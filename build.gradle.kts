import org.gradle.api.*
import java.text.SimpleDateFormat
import java.util.*

version = GeekLoadPlugin.version
group = "io.houseofgeeks"
val pluginId = "$group.geekload"

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.8.21"
}

repositories {
    mavenCentral()
}

gradlePlugin {
    website.set("https://geekload.io")
    vcsUrl.set("https://github.com/iDispatchPro/GeekLoadGradlePlugin")

    plugins {
        create("geekload") {
            id = pluginId
            implementationClass = "GeekLoadPlugin"
            displayName = "GeekLoad plugin"
            description = "Performing load tests during project build"
            tags.set(listOf("load", "test", "javascript"))
        }
    }
}

class GeekLoadPlugin : Plugin<Project>
{
    companion object
    {
        val version : String = SimpleDateFormat("yyyy.M.d").format(Date())
    }

    private val rootName = "/projects"

    override fun apply(project : Project)
    {
        project.task("geek-load") {
            group = "test"

            doLast {
                fun cmd(command : String) : Int
                {
                    return project.exec {
                        if (System.getProperty("os.name").lowercase().contains("win"))
                            commandLine("cmd", "/c", command)
                        else
                            commandLine("bash", "-c", command)
                    }.exitValue
                }

                val container = "geekload4gradle"

                fun isContainerExists(name : String) =
                    ProcessBuilder("docker", "inspect", "--format='{{.State.Running}}'", name)
                        .redirectErrorStream(true)
                        .start()
                        .inputStream
                        .bufferedReader()
                        .readText()
                        .trim() == "'true'"

                fun cleanUp()
                {
                    if (isContainerExists(container))
                        cmd("docker rm -f $container")
                }

                cleanUp()

                project.fileTree(mapOf("dir" to "src/test", "include" to "**/*.gl-js")).forEach { file ->
                    try
                    {
                        val pathDelim = "/"
                        val path = file.absolutePath.replace("\\", pathDelim)
                        val root = path.split(pathDelim).first() + pathDelim

                        cmd("docker run -d --name $container -v $root:$rootName geekload/geekload-bundle:$version")
                        cmd("docker exec $container java -jar application.jar -Xmx8g -run=\"${path.replace(root, "$rootName/")}\" -reportSet=medium -warningAsOk")
                    }
                    finally
                    {
                        cleanUp()
                    }
                }
            }
        }
    }
}

// apply<GeekLoadPlugin>()