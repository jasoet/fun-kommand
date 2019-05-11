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

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import kotlin.test.assertFailsWith

object KommandSpec : Spek({

    describe("Command Extension") {

        describe("Executing Command") {
            it("should return zero for success command") {
                runBlocking {
                    val result = listOf("ls", "-alh").execute().asString()
                    result.shouldNotBeNull()

                    "ls -alh".execute().asString().shouldNotBeNull()
                }
            }

            it("should throw exception for non exist command") {
                assertFailsWith(IOException::class) {
                    runBlocking {
                        listOf("notExistCommand", "-alh").execute()
                    }
                }

                assertFailsWith(IOException::class) {
                    runBlocking {
                        "notExistCommand -alh".execute()
                    }
                }

            }
        }

        describe("Handling wrong input/output type") {

            it("should throw IllegalArgumentException when receive wrong input type") {
                assertFailsWith(IllegalArgumentException::class) {
                    runBlocking {
                        listOf("ls", "-alh").execute(input = 12)
                    }
                }
            }
        }

        describe("Handling input") {
            val tmpDir: String = System.getProperty("java.io.tmpdir")
            val path = Paths.get(tmpDir, UUID.randomUUID().toString())

            val fileContent = """
                    it("should throw IllegalArgumentException when receive wrong output type") {
                        assertFailsWith(IllegalArgumentException::class) {
                            listOf("ls", "-alh").execute(output = 12)
                        }
                        val tryReturn = listOf("ls", "-alh").tryExecute(output = 24)
                        tryReturn.isSuccess() shouldEqualTo false
                    }
            """.trimIndent()

            Files.write(path, fileContent.toByteArray())
            val inputFile = path.toFile()

            it("should able to process string input") {
                runBlocking {
                    val result = listOf("cat").execute(input = fileContent).asString()
                    result.shouldNotBeNull()
                }
            }

            it("should able to process file input") {
                runBlocking {
                    val result = listOf("cat").execute(input = inputFile).asString()
                    result.shouldNotBeNull()
                }
            }

            it("should able to process InputStream input") {
                runBlocking {
                    val result = listOf("cat").execute(input = FileInputStream(inputFile)).asString()
                    result.shouldNotBeNull()
                }
            }

        }

    }

})
