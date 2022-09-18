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

/**
 * The [Either] type represents values with two possibilities. A value of type [Either] is either
 * [Either.Left] or [Either.Right], where both types are object wrappers with generic type parameters.
 *
 * The [Either] type is mostly used to represent a value which is either correct or an error;
 * by convention, the [Left] type is used to hold an error value and the [Right] is used to hold a
 * correct value. Mnemonic: "right" also means "correct".
 */
public sealed class Either<out L, out R> {

    public class Left<T>(
        public val value: T,
    ) : Either<T, Nothing>()

    public class Right<T>(
        public val value: T,
    ) : Either<Nothing, T>()

    public fun rightOrNull(): R? = when (this) {
        is Left -> null
        is Right -> this.value
    }

    public companion object
}

/**
 * Short-hand method to wrap the receiver object with [Either.Left]
 */
public fun <T> T.left(): Either<T, Nothing> = Either.Left(this)

/**
 * Short-hand method to wrap the receiver object with [Either.Right]
 */
public fun <T> T.right(): Either<Nothing, T> = Either.Right(this)
