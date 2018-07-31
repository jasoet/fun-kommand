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
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBeNullOrBlank
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import kotlin.test.assertFailsWith

object KommandSpec : Spek({

    given("Command Extension") {

        on("Executing Command") {
            it("should return zero for success command") {
                val returnCode = listOf("ls", "-alh").execute()
                returnCode.shouldEqualTo(0)
                val tryReturn = listOf("ls", "-alh").tryExecute()
                tryReturn.isSuccess() shouldEqualTo true
            }

            it("should throw exception for non exist command") {
                assertFailsWith(IOException::class) {
                    listOf("notExistCommand", "-alh").execute()
                }

                val tryReturn = listOf("notExistCommand", "-alh").tryExecute()
                tryReturn.isFailure() shouldEqualTo true
            }
        }

        on("Handling wrong input/output type") {

            it("should throw IllegalArgumentException when receive wrong input type") {
                assertFailsWith(IllegalArgumentException::class) {
                    listOf("ls", "-alh").execute(input = 12)
                }
                val tryReturn = listOf("ls", "-alh").tryExecute(input = 24)
                tryReturn.isSuccess() shouldEqualTo false
            }

            it("should throw IllegalArgumentException when receive wrong output type") {
                assertFailsWith(IllegalArgumentException::class) {
                    listOf("ls", "-alh").execute(output = 12)
                }
                val tryReturn = listOf("ls", "-alh").tryExecute(output = 24)
                tryReturn.isSuccess() shouldEqualTo false
            }
        }

        on("Handling input") {
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
                val returnCode = listOf("cat").execute(input = fileContent, output = System.out)
                returnCode shouldEqualTo 0
            }

            it("should able to process file input") {
                val returnCode = listOf("cat").execute(input = inputFile, output = System.out)
                returnCode shouldEqualTo 0
            }

            it("should able to process InputStream input") {
                val returnCode = listOf("cat").execute(input = FileInputStream(inputFile), output = System.out)
                returnCode shouldEqualTo 0
            }
        }


        on("Handling output") {
            val tmpDir: String = System.getProperty("java.io.tmpdir")

            it("should able to process file output") {
                val outputFile = Paths.get(tmpDir, UUID.randomUUID().toString()).toFile()
                val returnCode = listOf("ls", "-alh").execute(output = outputFile)
                returnCode shouldEqualTo 0
                outputFile.exists() shouldBe true
            }

            it("should able to process OutputStream output") {
                val byteOutputStream = ByteArrayOutputStream()
                val returnCode = listOf("ls", "-alh").execute(output = byteOutputStream)
                returnCode shouldEqualTo 0
                val stringResult = byteOutputStream.toString(Charsets.UTF_8.name())
                println(stringResult)
                stringResult.shouldNotBeNullOrBlank()
            }

        }

    }

})