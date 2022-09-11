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
package com.jayasuryat.dowel.sample.ui.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jayasuryat.dowel.annotation.ConsiderForDowel

@ConsiderForDowel
internal class LocationCustomPreviewParamProvider : PreviewParameterProvider<Location> {

    override val values: Sequence<Location> = sequenceOf(
        Location(
            lat = 1000000000,
            lon = 1000000000,
        ),
        Location(
            lat = 1000000000,
            lon = 1000000000,
        ),
        Location(
            lat = 1000000000,
            lon = 1000000000,
        ),
        Location(
            lat = 1000000000,
            lon = 1000000000,
        ),
    )
}