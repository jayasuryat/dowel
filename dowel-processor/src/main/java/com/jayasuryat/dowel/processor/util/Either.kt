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
package com.jayasuryat.dowel.processor.util

internal sealed interface Either<out L, out R> {

    class Left<T>(
        val value: T,
    ) : Either<T, Nothing>

    class Right<T>(
        val value: T,
    ) : Either<Nothing, T>

    companion object
}

internal fun <T> T.left(): Either<T, Nothing> = Either.Left(this)

internal fun <T> T.right(): Either<Nothing, T> = Either.Right(this)

internal fun <L, R, T> Either<L, R>.fold(logic: (R) -> T): Either<L, T> {

    if (this is Either.Left) return this

    return when (this) {
        is Either.Left -> this
        is Either.Right -> logic(this.value).right()
    }
}

internal inline fun <L, R, T> Either.Companion.combine(
    either: Either<L, R>,
    either2: Either<L, R>,
    transform: (either: R, either2: R) -> T,
): Either<L, T> {

    val val1 = when (either) {
        is Either.Left -> return either
        is Either.Right -> either.value
    }

    val val2 = when (either2) {
        is Either.Left -> return either2
        is Either.Right -> either.value
    }

    return transform(val1, val2).right()
}
