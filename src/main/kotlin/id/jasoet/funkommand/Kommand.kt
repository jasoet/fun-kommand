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

import arrow.core.Try
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
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
    val inputStream = this.execute(input, null, environment, directory, config)
    return if (inputStream != null) {
        inputStream.use {
            IOUtils.toString(it, "UTF-8")
        }
    } else {
        ""
    }
}

/**
 * Split `String`, execute and wrap it inside Try<BufferedInputStream?> monad
 * @see tryExecute
 *
 * @author Deny Prasetyo
 * @since 1.0.0
 */
fun String.tryExecute(
    input: Any? = null,
    output: Any? = null,
    environment: Map<String, String> = emptyMap(),
    directory: String = homeDir(),
    config: (ProcessBuilder) -> Unit = {}
): Try<BufferedInputStream?> {
    return this.trim().split("\\s+".toRegex()).tryExecute(input, output, environment, directory, config)
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
): BufferedInputStream? {
    return this.trim().split("\\s+".toRegex()).execute(input, output, environment, directory, config)
}

/**
 * Execute command in form of List<String> and wrap it on Try<BufferedInputStream?> monad.
 * @see execute
 *
 * @author Deny Prasetyo
 * @since 1.0.0
 */
fun List<String>.tryExecute(
    input: Any? = null,
    output: Any? = null,
    environment: Map<String, String> = emptyMap(),
    directory: String = homeDir(),
    config: (ProcessBuilder) -> Unit = {}
): Try<BufferedInputStream?> {
    return Try {
        this.execute(input, output, environment, directory, config)
    }
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
): BufferedInputStream? {

    if (input != null && supportedInput.none { it.isSuperclassOf(input.javaClass.kotlin) }) {
        throw IllegalArgumentException("Input ${input.javaClass} is not supported!")
    }
    if (output != null && supportedOutput.none { it.isSuperclassOf(output.javaClass.kotlin) }) {
        throw IllegalArgumentException("Output ${output.javaClass} is not supported!")
    }

    val log = LoggerFactory.getLogger("CommandExtension")

    log.debug("Command to Execute ${this.joinToString(" ")}")

    val tmpDir: String = System.getProperty("java.io.tmpdir")
    val processBuilder = ProcessBuilder(this)

    val env = processBuilder.environment()
    env.putAll(environment)

    processBuilder.directory(File(directory))

    config(processBuilder)

    when (input) {
        is File -> {
            log.debug("Accept File Input")
            processBuilder.redirectInput(input)
        }
        is InputStream -> {
            log.debug("Accept InputStream Input")
            val inputFile = File(tmpDir, UUID.randomUUID().toString())
            FileUtils.copyInputStreamToFile(input, inputFile)
            processBuilder.redirectInput(inputFile)
        }
        is String -> {
            log.debug("Accept String Input")
            val inputFile = File(tmpDir, UUID.randomUUID().toString())
            FileUtils.writeStringToFile(inputFile, input, "UTF-8")
            processBuilder.redirectInput(inputFile)
        }
    }

    return when (output) {
        is File -> {
            processBuilder.redirectOutput(output)
            val process = processBuilder.start()
            process.waitFor()
            null
        }
        is OutputStream -> {
            val process = processBuilder.start()
            process.waitFor()
            IOUtils.copy(process.inputStream, output)
            null
        }
        else -> {
            val process = processBuilder.start()
            process.waitFor()
            BufferedInputStream(process.inputStream)
        }
    }
}

private val supportedInput = listOf(File::class, InputStream::class, String::class)
private val supportedOutput = listOf(File::class, OutputStream::class)

private fun homeDir(): String {
    return System.getProperty("user.home")
}


