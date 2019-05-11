/*
 * Copyright (C)2018 - Deny Prasetyo <jasoet87@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package id.jasoet.funkommand

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.full.isSuperclassOf

suspend fun List<String>.execute(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = pwd(),
        combineOutput: Boolean = false): Process {
    val command = this
    return coroutineScope {
        val log = LoggerFactory.getLogger("Kommand.build")

        if (input != null && supportedInput.none { it.isSuperclassOf(input.javaClass.kotlin) }) {
            throw IllegalArgumentException("Input ${input.javaClass} is not supported!")
        }

        val processBuilder = ProcessBuilder(command)

        val env = processBuilder.environment()
        env.putAll(environment)

        processBuilder.directory(File(directory))
        processBuilder.redirectErrorStream(combineOutput)

        when (input) {
            is File -> {
                log.debug("Redirect input from File")
                processBuilder.redirectInput(input)
                processBuilder.start()
            }
            is InputStream -> {
                log.debug("Pipe Input from InputStream")
                processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
                val process = processBuilder.start()
                launch {
                    copyStream(input, process.outputStream)
                }
                process
            }
            is String -> {
                log.debug("Pipe Input from String Stream")
                processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
                val stringStream = IOUtils.toInputStream(input, Charsets.UTF_8)
                val process = processBuilder.start()
                launch {
                    copyStream(stringStream, process.outputStream)
                }
                process
            }
            else -> processBuilder.start()
        }
    }
}

suspend fun String.executeShell(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = pwd(),
        combineOutput: Boolean = false
): Process {
    val shellCommand = listOf("/bin/sh", "-c", this)
    return shellCommand.execute(input, environment, directory, combineOutput)
}

suspend fun String.execute(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = pwd(),
        combineOutput: Boolean = false
): Process {
    return this.split("\\s+".toRegex()).execute(input, environment, directory, combineOutput)
}

suspend fun String.executeToString(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = pwd(),
        combineOutput: Boolean = false
): String {
    return this.split("\\s+".toRegex()).execute(input, environment, directory, combineOutput).asString()
}

suspend fun String.executeShellToString(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = pwd(),
        combineOutput: Boolean = false
): String {
    return this.executeShell(input, environment, directory, combineOutput).asString()
}

private val supportedInput = listOf(File::class, InputStream::class, String::class)

internal fun pwd(): String {
    return System.getProperty("user.dir")
}

internal fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
    outputStream.use { o ->
        inputStream.use { i ->
            IOUtils.copy(i, o)
        }
    }
}
