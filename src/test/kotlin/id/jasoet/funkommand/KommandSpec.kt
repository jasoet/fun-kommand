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

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldNotBeNullOrBlank
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.ByteArrayOutputStream
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
                val inputStream = listOf("ls", "-alh").execute()
                inputStream.shouldNotBeNull()

                "ls -alh".execute().shouldNotBeNull()
            }

            it("should throw exception for non exist command") {
                assertFailsWith(IOException::class) {
                    listOf("notExistCommand", "-alh").execute()
                }

                assertFailsWith(IOException::class) {
                    "notExistCommand -alh".execute()
                }

            }
        }

        describe("Handling wrong input/output type") {

            it("should throw IllegalArgumentException when receive wrong input type") {
                assertFailsWith(IllegalArgumentException::class) {
                    listOf("ls", "-alh").execute(input = 12)
                }
            }

            it("should throw IllegalArgumentException when receive wrong output type") {
                assertFailsWith(IllegalArgumentException::class) {
                    listOf("ls", "-alh").execute(output = 12)
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
                val inputStream = listOf("cat").execute(input = fileContent)
                inputStream.shouldNotBeNull()
            }

            it("should able to process file input") {
                val inputStream = listOf("cat").execute(input = inputFile)
                inputStream.shouldNotBeNull()
            }

            it("should able to process InputStream input") {
                val inputStream = "cat".execute(input = FileInputStream(inputFile))
                inputStream.shouldNotBeNull()
            }

            it("should able to process file input and return String") {
                val inputStream = "cat".executeToString(input = inputFile)
                inputStream.shouldNotBeNullOrBlank()
            }
        }


        describe("Handling output") {
            val tmpDir: String = System.getProperty("java.io.tmpdir")

            it("should able to redirect output to standard out") {
                val inputStream = listOf("ls", "-alh").execute(output = System.out)
                inputStream.shouldBeNull()
            }

            it("should able to process file output") {
                val outputFile = Paths.get(tmpDir, UUID.randomUUID().toString()).toFile()
                val inputStream = listOf("ls", "-alh").execute(output = outputFile)
                inputStream.shouldBeNull()
                outputFile.exists() shouldBe true
            }

            it("should able to process OutputStream output") {
                val byteOutputStream = ByteArrayOutputStream()
                val inputStream = listOf("ls", "-alh").execute(output = byteOutputStream)
                inputStream.shouldBeNull()
                val stringResult = byteOutputStream.use {
                    it.toString(Charsets.UTF_8.name())
                }
                println(stringResult)
                stringResult.shouldNotBeNullOrBlank()
            }

        }

    }

})
