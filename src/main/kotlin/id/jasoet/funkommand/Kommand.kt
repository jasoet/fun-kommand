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

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.full.isSuperclassOf


/**
 * Split `String`, execute it and return String.
 * @see executeToString
 *
 * @author Deny Prasetyo
 * @since 1.0.0
 */
fun String.executeToString(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = homeDir(),
        config: (ProcessBuilder) -> Unit = {}
): String {
    return this.trim().split("\\s+".toRegex()).executeToString(input, environment, directory, config)
}

/**
 * Accept command as `List<String>`, execute it and return String.
 * @see execute
 *
 * @author Deny Prasetyo
 * @since 1.0.0
 */
fun List<String>.executeToString(
        input: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = homeDir(),
        config: (ProcessBuilder) -> Unit = {}
): String {
    val (exitCode, inputStream) = this.execute(input, null, environment, directory, config)
    return if (inputStream != null) {
        inputStream.use {
            IOUtils.toString(it, "UTF-8")
        }
    } else {
        throw IllegalStateException("ExitCode [$exitCode], Input Stream should not be Null!")
    }
}

/**
 * Split `String` and execute it
 * @see execute
 *
 * @author Deny Prasetyo
 * @since 1.0.0
 */
fun String.execute(
        input: Any? = null,
        output: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = homeDir(),
        config: (ProcessBuilder) -> Unit = {}
): CommandOutput {
    return this.trim().split("\\s+".toRegex()).execute(input, output, environment, directory, config)
}

/**
 * Execute command in form of List<String>.
 * Redirect standard input from File, InputStream and String
 * Redirect standard output source to File and OutputStream
 *
 * Note:
 * - InputStream will be closed after execution
 * - OutputStream will remain open after execution
 *
 * @param input Standard input for command, able to receive File, InputStream and String input.
 * @param output Standard output for command, able to send output to File and OutputStream.
 * @param environment Environment variable supplied to command
 *
 * @return BufferedInputStream if output not defined or null if otherwise
 * @throws java.io.IOException if command return non-zero
 * @author Deny Prasetyo
 * @since 1.0.0
 */

fun List<String>.execute(
        input: Any? = null,
        output: Any? = null,
        environment: Map<String, String> = emptyMap(),
        directory: String = homeDir(),
        config: (ProcessBuilder) -> Unit = {}
): CommandOutput {

    if (input != null && supportedInput.none { it.isSuperclassOf(input.javaClass.kotlin) }) {
        throw IllegalArgumentException("Input ${input.javaClass} is not supported!")
    }
    if (output != null && supportedOutput.none { it.isSuperclassOf(output.javaClass.kotlin) }) {
        throw IllegalArgumentException("Output ${output.javaClass} is not supported!")
    }

    val log = LoggerFactory.getLogger("CommandExtension")

    log.debug("Command to Execute ${this.joinToString(" ")}")

    val processBuilder = ProcessBuilder(this)

    val env = processBuilder.environment()
    env.putAll(environment)

    processBuilder.directory(File(directory))

    config(processBuilder)

    val process = when (input) {
        is File -> {
            log.debug("Redirect input from File")
            processBuilder.redirectInput(input)
            processBuilder.start()
        }
        is InputStream -> {
            log.debug("Pipe Input from InputStream")
            processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
            val process = processBuilder.start()
            copyStream(input, process.outputStream)
            process
        }
        is String -> {
            log.debug("Pipe Input from String Stream")
            val stringStream = IOUtils.toInputStream(input, Charsets.UTF_8)
            processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
            val process = processBuilder.start()
            copyStream(stringStream, process.outputStream)
            process
        }
        else -> processBuilder.start()
    }

    return when (output) {
        is File -> {
            copyStream(process.inputStream, FileOutputStream(output))
            val exitStatus = process.waitFor()
            if (exitStatus == 0) {
                exitStatus to null
            } else {
                exitStatus to process.errorStream
            }
        }
        is OutputStream -> {
            copyStream(process.inputStream, output)
            val exitStatus = process.waitFor()
            if (exitStatus == 0) {
                exitStatus to null
            } else {
                exitStatus to process.errorStream
            }
        }
        else -> {
            val exitStatus = process.waitFor()
            if (exitStatus == 0) {
                exitStatus to process.inputStream
            } else {
                exitStatus to process.errorStream
            }
        }
    }
}

private val supportedInput = listOf(File::class, InputStream::class, String::class)
private val supportedOutput = listOf(File::class, OutputStream::class)

private fun homeDir(): String {
    return System.getProperty("user.home")
}

private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
    outputStream.use { o ->
        inputStream.use { i ->
            IOUtils.copy(i, o)
        }
    }
}


