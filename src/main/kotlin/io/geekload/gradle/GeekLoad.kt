package io.geekload.gradle

import org.gradle.api.*
import java.text.SimpleDateFormat
import java.util.*
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.task
import java.io.ByteArrayOutputStream
import java.lang.Math.max

open class GeekLoadTask : DefaultTask()
{
    @TaskAction
    fun action()
    {
        val exitCode = try
        {
            execute()
        }
        catch (e : Exception)
        {
            logger.error(e.toString())
            throw RuntimeException("Error preparing the load test")
        }

        when (exitCode)
        {
            4    -> throw VerifyError("Load test failed")
            0    -> Unit

            else -> throw UnknownError("Unknown result of test")
        }
    }

    private val rootName = "/projects"

    private fun execute() : Int
    {
        fun cmd(command : String, noError : Boolean = false) : Int
        {
            return project.exec {
                if (System.getProperty("os.name").lowercase().contains("win")) commandLine("cmd", "/c", command)
                else commandLine("bash", "-c", command)

                isIgnoreExitValue = noError

                if (!noError)
                {
                    val output = ByteArrayOutputStream()

                    standardOutput = output
                    errorOutput = output
                }
            }.exitValue
        }

        val container = "geekload4gradle"

        fun isContainerExists(name : String) =
            ProcessBuilder("docker", "inspect", "--format='{{.State.Running}}'", name).redirectErrorStream(true).start().inputStream.bufferedReader().readText().trim() == "'true'"

        fun cleanUp()
        {
            if (isContainerExists(container)) cmd("docker rm -f $container")
        }

        cleanUp()

        var code = 0

        project.fileTree(mapOf("dir" to "src/test", "include" to "**/*.gl-js")).forEach { file ->
            try
            {
                val pathDelim = "/"
                val path = file.absolutePath.replace("\\", pathDelim)
                val root = path.split(pathDelim).first() + pathDelim

                cmd("docker run -q -d --name $container -v $root:$rootName geekload/geekload-bundle:${GeekLoad.version}")
                code = code.coerceAtLeast(cmd("docker exec $container java -jar application.jar -Xmx8g -run=\"${path.replace(root, "$rootName/")}\" -reportSet=medium -warningAsOk", true))
            }
            finally
            {
                cleanUp()
            }
        }

        return code
    }
}

class GeekLoad : Plugin<Project>
{
    companion object
    {
        val version : String = SimpleDateFormat("yyyy.M.d").format(Date())
    }

    override fun apply(project : Project)
    {
        project.task<GeekLoadTask>("geek-load") {
            group = "test"
        }
    }
}