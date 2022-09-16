/*
 * Copyright 2022 Jaya Surya Thotapalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayasuryat.either

import org.junit.Test

class EitherTest {

    @Test
    fun test_bind_all_right() {

        val a: Either<String, Int> = 2.right()
        val b: Either<String, Int> = 3.right()
        val c: Either<String, String> = "Hello".right()

        val value = either {
            val multiple: Int = a.bind() * b.bind()
            val value: String = c.bind() + multiple
            value
        }

        assert(value is Either.Right<String>)
        assert((value as Either.Right).value == "Hello6")
    }

    @Test
    fun test_bind_left_and_right() {

        val errorMessage = "Something went wrong"
        val a: Either<String, Int> = 2.right()
        val b: Either<String, Int> = 3.right()
        val c: Either<String, String> = errorMessage.left()

        val value: Either<String, String> = either {
            val multiple: Int = a.bind() * b.bind()
            val value: String = c.bind() + multiple
            value
        }

        assert(value is Either.Left<String>)
        assert((value as Either.Left).value == errorMessage)
    }

    @Test
    fun test_bind_left_null() {

        val a: Either<String, Int> = 2.right()
        val b: Either<String, Int> = 3.right()
        val c: Either<Any?, String> = null.left()

        val value: Either<Any?, String> = either {
            val multiple: Int = a.bind() * b.bind()
            val value: String = c.bind() + multiple
            value
        }

        assert(value is Either.Left)
        assert((value as Either.Left).value == null)
    }
}
