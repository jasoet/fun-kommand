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
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldNotBeNullOrBlank
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

object InputSpec : Spek({

    given("Input Extension") {

        val fileContent = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    Morbi eu suscipit orci. Morbi eleifend erat erat, ut fringilla sapien tempus sed.
                    Maecenas molestie et lorem quis egestas.
                    Aliquam ut lacus quis nulla auctor efficitur nec id leo.
                    Vivamus blandit est varius feugiat eleifend.
                    Etiam condimentum quis lorem in cursus.
                    Morbi scelerisque eget urna fringilla porttitor.
                    Proin ex nisi, accumsan et pretium in, euismod vitae lorem.
                """.trimIndent()

        on("Wrapping standard *nix command") {
            val tmpDir: String = System.getProperty("java.io.tmpdir")
            val path = Paths.get(tmpDir, UUID.randomUUID().toString())

            Files.write(path, fileContent.toByteArray())
            val inputFile = path.toFile()
            it("Should able to count line from file using wc command") {
                val countOnString = "wc -l".executeToString(input = inputFile)
                countOnString.trim().toInt() shouldEqualTo 7
            }
        }

        on("Handling Standard Input") {

            it("should return false when checking stdIn availability on test environment  ") {
                standardInputAvailable() shouldBe false
            }

            it("should return empty seq when getting stdIn  on test environment  ") {
                sequenceInput.toList().shouldBeEmpty()
            }

            it("standardInput should not be null") {
                standardInput.shouldNotBeNull()
            }
        }

        on("piping some commands") {

            it("should able to pipe more than one commands") {

                val result = "cat".execute(input = fileContent)
                    .pipe("echo")
                    .pipe("wc")
                    .toString()

                result.shouldNotBeNullOrBlank()
            }

            it("should able to pipe more than one commands with lamba parameter") {

                val result = "cat".execute(input = fileContent)
                    .pipe {
                        "echo".execute(input = it)
                    }
                    .pipe {
                        "wc".execute(it)
                    }
                    .toString()

                result.shouldNotBeNullOrBlank()
            }
        }
    }

})
