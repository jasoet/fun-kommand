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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

val sequenceInput: Sequence<String> by lazy { generateSequence { readLine() } }

val standardInput: InputStream by lazy { System.`in` }

fun standardInputAvailable(): Boolean {
    return System.`in`.available() > 0
}

typealias CommandOutput = Pair<Int, InputStream?>

fun CommandOutput.pipe(ops: (CommandOutput) -> CommandOutput): CommandOutput {
    return if (this.second != null) {
        ops(this)
    } else {
        this.first to null
    }
}

fun CommandOutput.pipe(command: String): CommandOutput {
    return if (this.second != null) {
        command.execute(this.second)
    } else {
        this.first to null
    }
}

fun CommandOutput.asString(): String {
    return if (this.second != null) {
        IOUtils.toString(this.second, Charsets.UTF_8)
    } else {
        ""
    }
}

fun CommandOutput.asTempFile(): File? {
    return this.second?.let {
        val tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
        this.toFile(tempFile)
        tempFile
    }
}

fun CommandOutput.toOutput(output: OutputStream) {
    this.second?.use {
        IOUtils.copy(it, output)
    }
}

fun CommandOutput.toFile(file: File) {
    this.second?.let {
        FileUtils.copyInputStreamToFile(it, file)
    }
}

fun CommandOutput.print() {
    this.toOutput(System.out)
}
