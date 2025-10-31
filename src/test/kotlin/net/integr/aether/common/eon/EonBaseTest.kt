/*
 * Copyright © 2025 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.aether.common.eon

import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class EonBaseTest {
    @Test
    fun `Encoded and decoded by bytes should equal to the same data`() {
        val eonWriter = EonWriter.begin()
            .int(42)
            .string("Hello")
            .enum(Color.GREEN)
            .list(listOf("Hello", "World", "My", "Friends")) { item ->
                string(item)
            }
            .list(listOf(1, 2, 3)) {
                int(it)
            }

        val bytes = eonWriter.bake()
        val eonReader = EonReader.begin(bytes)

        val int = eonReader.int()
        val string = eonReader.string()
        val enum = eonReader.enum<Color>()
        val stringList = eonReader.list { string() }
        val intList = eonReader.list { int() }

        Assertions.assertEquals(42, int)
        Assertions.assertEquals("Hello", string)
        Assertions.assertEquals(Color.GREEN, enum)
        Assertions.assertEquals(listOf("Hello", "World", "My", "Friends"), stringList)
        Assertions.assertEquals(listOf(1, 2, 3), intList)
    }
}

enum class Color {
    RED, GREEN, BLUE
}