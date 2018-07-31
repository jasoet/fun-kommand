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
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.reflect.full.isSuperclassOf


/**
 * Execute command in form of List<String> and wrap it on Try<T> monad.
 * @see execute
 */
fun List<String>.tryExecute(
    input: Any? = null,
    output: Any? = null,
    environment: Map<String, String> = emptyMap(),
    directory: String = homeDir(),
    config: (ProcessBuilder) -> Unit = {}
): Try<Int> {
    return Try {
        this.execute(input, output, environment, directory, config)
    }
}

/**
 * Execute command in form of List<String>.
 * Able to handle standard input source from File, InputStream and String
 * Able to handle standard output source to File and OutputStream
 *
 * Note:
 * - InputStream will be closed after execution
 * - OutputStream will remain open after execution
 *
 * @param input Standard input for command, able to receive File, InputStream and String input.
 * @param output Standard output for command, able to send output to File and OutputStream.
 * @return 0 if command executed successfully
 * @throws java.io.IOException if command return non-zero
 * @author Deny Prasetyo
 */

fun List<String>.execute(
    input: Any? = null,
    output: Any? = null,
    environment: Map<String, String> = emptyMap(),
    directory: String = homeDir(),
    config: (ProcessBuilder) -> Unit = {}
): Int {

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
            processBuilder.start().waitFor()
        }
        is OutputStream -> {
            val inputFile = File(tmpDir, UUID.randomUUID().toString())
            processBuilder.redirectOutput(inputFile)
            val exitCode = processBuilder.start().waitFor()

            inputFile.copyTo(output)

            exitCode
        }
        else -> {
            processBuilder.start().waitFor()
        }
    }
}

private val supportedInput = listOf(File::class, InputStream::class, String::class)
private val supportedOutput = listOf(File::class, OutputStream::class)

private fun homeDir(): String {
    return System.getProperty("user.home")
}

private fun File.copyTo(output: OutputStream) {
    FileInputStream(this).use { fis ->
        IOUtils.copy(fis, output)
    }
}

