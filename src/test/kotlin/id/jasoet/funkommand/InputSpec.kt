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
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object InputSpec : Spek({

    given("Input Extension") {

        on("Handling Standard Input") {

            it("should return false when checking stdIn availability on test environment  ") {
                standardInputAvailable() shouldBe false
            }

            it("should return empty seq when getting stdIn  on test environment  ") {
                standardInput.toList().shouldBeEmpty()
            }
        }
    }

})
