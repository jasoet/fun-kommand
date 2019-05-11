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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream

val sequenceInput: Sequence<String> by lazy { generateSequence { readLine() } }

val standardInput: InputStream by lazy { System.`in` }

fun standardInputAvailable(): Boolean {
    return System.`in`.available() > 0
}

suspend fun Process.pipe(command: String,
                         environment: Map<String, String> = emptyMap(),
                         directory: String = pwd()): Process {
    return command.execute(this.inputStream, environment, directory)
}

suspend fun Process.pipeShell(command: String,
                              environment: Map<String, String> = emptyMap(),
                              directory: String = pwd()): Process {
    return command.executeShell(this.inputStream, environment, directory)
}

suspend operator fun Process.invoke(
        errorHandler: suspend CoroutineScope.(InputStream) -> Unit = {},
        outputHandler: suspend CoroutineScope.(InputStream) -> Unit = {}
): Process {
    return this.handle(errorHandler, outputHandler)
}

suspend fun Process.handle(
        errorHandler: suspend CoroutineScope.(InputStream) -> Unit = {},
        outputHandler: suspend CoroutineScope.(InputStream) -> Unit = {}
): Process {
    val process = this
    return coroutineScope {
        launch {
            outputHandler(process.inputStream)
        }

        launch {
            errorHandler(process.errorStream)
        }

        process
    }
}

fun Process.asString(): String {
    val result = IOUtils.toString(this.inputStream, Charsets.UTF_8)
    this.waitFor()
    return result
}

fun Process.asTempFile(): File {
    val result = createTempFile()
    FileUtils.copyInputStreamToFile(this.inputStream, result)
    this.waitFor()
    return result
}

fun Process.toFile(file: File): Int {
    FileUtils.copyInputStreamToFile(this.inputStream, file)
    return this.waitFor()
}

fun Process.toOutput(output: OutputStream): Int {
    IOUtils.copy(this.inputStream, output)
    return this.waitFor()
}

fun Process.print(): Int {
    this.toOutput(System.out)
    return this.waitFor()
}
