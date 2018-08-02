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

fun InputStream?.pipe(ops: (InputStream) -> InputStream?): InputStream? {
    return if (this != null) {
        ops(this)
    } else {
        null
    }
}

fun InputStream?.pipe(command: String): InputStream? {
    return if (this != null) {
        command.execute(this)
    } else {
        null
    }
}

fun InputStream?.asString(): String {
    return if (this != null) {
        IOUtils.toString(this, Charsets.UTF_8)
    } else {
        ""
    }
}

fun InputStream?.asTempFile(): File? {
    return this?.let {
        val tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
        it.toFile(tempFile)
        tempFile
    }
}

fun InputStream?.toOutput(output: OutputStream) {
    this?.use {
        IOUtils.copy(it, output)
    }
}

fun InputStream?.toFile(file: File) {
    this?.let {
        FileUtils.copyInputStreamToFile(it, file)
    }
}

fun InputStream?.print() {
    this.toOutput(System.out)
}
