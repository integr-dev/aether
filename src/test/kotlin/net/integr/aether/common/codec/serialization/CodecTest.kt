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

package net.integr.aether.common.codec.serialization

import kotlinx.serialization.Serializable
import net.integr.aether.common.codec.CodecReader
import net.integr.aether.common.codec.CodecWriter
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class CodecTest {
    @Test
    fun `Encoded by bytes should equal to the same data`() {
        val testData = TestData(
            number = 123,
            text = "Hello, World!",
            flag = true,
            items = listOf("one", "two", "three")
        )

        val bytes = Codec.encode(testData)

        val codecReader = CodecReader.begin(bytes)
        val number = codecReader.int()
        val text = codecReader.string()
        val flag = codecReader.boolean()
        val items = codecReader.list { string() }

        Assertions.assertEquals(testData.number, number)
        Assertions.assertEquals(testData.text, text)
        Assertions.assertEquals(testData.flag, flag)
        Assertions.assertEquals(testData.items, items)
    }

    @Test
    fun `Decoded from bytes should equal to the same data`() {
        val codecWriter = CodecWriter.begin()
            .int(123)
            .string("Hello, World!")
            .boolean(true)
            .list(listOf("one", "two", "three")) { item ->
                string(item)
            }

        val bytes = codecWriter.bake()

        val decodedData = Codec.decode<TestData>(bytes)

        Assertions.assertEquals(123, decodedData.number)
        Assertions.assertEquals("Hello, World!", decodedData.text)
        Assertions.assertEquals(true, decodedData.flag)
        Assertions.assertEquals(listOf("one", "two", "three"), decodedData.items)
    }

    @Test
    fun `Advanced Encode and decode should equal to the same data`() {
        val originalData = AdvancedTestData(
            id = 9876543210,
            name = "Advanced Test",
            isActive = false,
            scores = mapOf("math" to 95, "science" to 88),
            nestedData = TestData(
                number = 456,
                text = "Nested Data",
                flag = false,
                items = listOf("alpha", "beta")
            ),
            advancedList = listOf(
                listOf("a1", "a2", "a3"),
                listOf("b1", "b2")
            )
        )

        val encodedBytes = Codec.encode(originalData)
        val decodedData = Codec.decode<AdvancedTestData>(encodedBytes)

        Assertions.assertEquals(originalData.id, decodedData.id)
        Assertions.assertEquals(originalData.name, decodedData.name)
        Assertions.assertEquals(originalData.isActive, decodedData.isActive)
        Assertions.assertEquals(originalData.scores, decodedData.scores)

        Assertions.assertEquals(originalData.nestedData.number, decodedData.nestedData.number)
        Assertions.assertEquals(originalData.nestedData.text, decodedData.nestedData.text)
        Assertions.assertEquals(originalData.nestedData.flag, decodedData.nestedData.flag)
        Assertions.assertEquals(originalData.nestedData.items, decodedData.nestedData.items)

        Assertions.assertEquals(originalData.advancedList, decodedData.advancedList)
    }
}

@Serializable
data class AdvancedTestData(
    val id: Long,
    val name: String,
    val isActive: Boolean,
    val scores: Map<String, Int>,
    val nestedData: TestData,
    val advancedList: List<List<String>>
)

@Serializable
data class TestData(
    val number: Int,
    val text: String,
    val flag: Boolean,
    val items: List<String>
)