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
 * This is a utility method to safely and easily operate on multiple [Either] types at once.
 * Provides access to the [ValueBinder.bind] method via the [logic] lambda, which should be used to
 * safely unwrap an [Either] type as [Either.Right] and operate on the result directly, instead
 * of checking if / when conditions for every [Either] object to check if it is [Either.Right] or not.
 *
 * If any [Either] is bound with [ValueBinder.bind] and the value turns out to be [Either.Left], then
 * the whole logic will *short-circuit* and the value of that [Either.Left] would be returned as the result.
 *
 * Note : [Either] objects being bound have a upper limit on the [Either.Left] type, but there are no
 * type restrictions on the [Either.Right] type of the objects being bound. So, objects with any right
 * type could be read inside the [logic] lambda.
 *
 * @param L Left type; Type of left branch ([Either.Left]) of all the [Either] objects being bound
 * in the [logic] lambda (upper limit)
 * @param T Right type; Type of the object being computed and returned if all of the [Either] objects
 * being bound in the [logic] lambda are [Either.Right]
 * @return Returns an [Either] with [L] and [T] as their [Either.Left] and [Either.Right] types
 * respectively. If any of the [Either] objects being bound in the [logic] lambda is an [Either.Left]
 * then the function would return value of that [Either.Left] as [Either.Left] at the first occurrence
 * of such object, otherwise if all objects being bound are [Either.Right], then the result of
 * [logic] lambda would be returned as [Either.Right].
 */
public inline fun <L, T> either(
    logic: ValueBinder<L>.() -> T,
): Either<L, T> {

    return try {
        ValueBinder<L>().logic().right()
    } catch (error: ValueBindError) {
        @Suppress("UNCHECKED_CAST") (error.value as L).left()
    }
}

public class ValueBinder<L> @PublishedApi internal constructor() {

    public fun <R> Either<L, R>.bind(): R {
        return when (val either = this) {
            is Either.Left -> throw ValueBindError(either.value)
            is Either.Right -> either.value
        }
    }
}

@PublishedApi
internal class ValueBindError internal constructor(
    @PublishedApi internal val value: Any?,
) : Error()
