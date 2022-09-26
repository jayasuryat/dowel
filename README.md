
<div align="center">

  <img src="https://user-images.githubusercontent.com/37530409/192136328-7f566157-6a3f-46b9-8703-780ea1da625f.svg#gh-dark-mode-only" alt="Dowel" height="200"/>

  <img src="https://user-images.githubusercontent.com/37530409/192136329-18987de1-1d36-4c62-a608-2b02d4b92e83.svg#gh-light-mode-only" alt="Dowel" height="200"/>

  <p align="center">
    <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
    <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  </p>

  <h2>A Kotlin Symbol Processor to generate Compose PreviewParameterProviders</h2>

</div>


<details>
  <summary><h3>1. What are PreviewParameterProviders to begin with?<h3/></summary>

In [`Jetpack Compose`](https://developer.android.com/jetpack/compose), we use something called [`Previews`](https://developer.android.com/jetpack/compose/tooling), which are `Composable` functions written specifically to preview (or interact with) the UI rendered by `Compose` in the editor itself without needing to run the app on a device.

And `Composable` methods generally have some parameters based on which the UI is rendered. More often than not, `Composable` methods have a significant amount of data inputs which are needed to be passed from the preview methods for the previews to render.

In cases like these [`PreviewParameterProvider`](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider) (a class from the Compose tooling library) can be used to provide data for the previews.

`PreviewParameterProvider` makes the `Preview` methods less verbose and easy to read by abstracting away the input data construction logic. And these providers can be reused across various `Preview` methods to render different previews.

</details>

## 2. Why Dowel?

Due to the amount of sheer verbosity involved in writing `PreviewParameterProvider` *by hand*, it becomes tedious to write `PreviewParameterProvider` for each and every UI model. And as writing `PreviewParameterProvider` for every model becomes an uninteresting task it becomes a barrier to entry for writing `Previews` for all the `Composables`.

#### That is where `Dowel` comes in and takes care of generating all of the boilerplate `PreviewParameterProvider` logic for your UI models.

This makes writing `Previews` simple and hence encourages writing more `Previews` for `Composables` in general. Apart from that, with `Dowel` you can also *`Fuzz test`* your `Composables` with all of the random values of random length or range being generated for all of the properties of the inputs. 

> **Note** : These random lengths or ranges can also be regulated, read more at *"4. How do I use Dowel?"* section.

## 3. Gradle setup

#### 3.1. Add KSP plugin to your **module's** `build.gradle` file
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.7.0-1.0.6"
}
```
> **Note** : Make sure your project's `Kotlin` version and `KSP version` are the same. Learn more about the available versions [here](https://github.com/google/ksp/releases)

#### 3.2. Add `jitpack.io` to the `repositories` blocks in your project's `settings.gradle` file
```gradle
pluginManagement {
    repositories {
        // Other repos
        maven { url 'https://jitpack.io' } // <----- This is the line to add
    }
}
dependencyResolutionManagement {
    repositories {
        // Other repos
        maven { url 'https://jitpack.io' } // <----- This is the line to add
    }
}
```

#### 3.3. Add the `Dowel` dependencies in your **module's** `build.gradle` file
```gradle
dependencies {
    implementation("com.github.jayasuryat.dowel:dowel:<version>")
    ksp("com.github.jayasuryat.dowel:dowel-processor:<version>")
}
```

#### 3.4. Add `KSP` generated files as sources in your **module's** `build.gradle` file
```gradle
kotlin {
    sourceSets.configureEach {
        kotlin.srcDir("$buildDir/generated/ksp/$name/kotlin/")
    }
}
```

## 4. How?
`Dowel` uses [`Kotlin Symbol Processing API`](https://kotlinlang.org/docs/ksp-overview.html) under the hood to read, parse, and process source code to generate appropriate `PreviewParameterProviders`.

The primary entry point into `Dowel` is with [`@Dowel`](https://github.com/JayaSuryaT/Dowel/blob/main/dowel-annotation/src/main/java/com/jayasuryat/dowel/annotation/Dowel.kt) annotation.

`Dowel` goes through all the classes annotated with `@Dowel` annotation and generates `PreviewParameterProvider` for each class.

### For example

```kotlin
// File : NewsArticle.kt
import androidx.compose.runtime.State
import com.jayasuryat.dowel.annotation.Dowel
import kotlinx.coroutines.flow.Flow

@Dowel(count = 2)
data class NewsArticle(
    val title: String,
    val description: String,
    val likes: Int,
    val authors: List<String>,
    val liveComments: Flow<List<String>>,
    val isExpanded: State<Boolean>,
    val status: Status,
    val onArticleClicked: () -> Unit,
) {

    enum class Status { Draft, Accepted, Posted }
}
```

### Generates 👇

```kotlin
// File in generated sources : NewsArticlePreviewParamProvider.kt
package com.yourapp.module

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlin.sequences.Sequence
import kotlinx.coroutines.flow.flowOf

public class NewsArticlePreviewParamProvider : PreviewParameterProvider<NewsArticle> {
  public override val values: Sequence<NewsArticle> = sequenceOf(
    NewsArticle(
      title = "AdipiscingDuis ac porttitor et",
      description = "Phasellusmassa suscipit iaculi",
      likes = 94,
      authors = listOf(
        "Velamet ultricies malesuada co",
        "Consequatmassa malesuada sapie",
        "Ameta et at bibendum ut neque ",
        "Mimollis ac consectetur Praese",
        "Namimperdiet massa bibendum po",
      ),
      liveComments = flowOf(listOf(
        "Malesuadasit Duis dapibus cong",
        "Metusluctus nec congue congue ",
        "InPraesent est tempus ac ultri",
        "Malesuadaquis est Lorem sapien",
        "FinibusCras mattis imperdiet n",
      )),
      isExpanded = mutableStateOf(false),
      status = NewsArticle.Status.values().random(),
      onArticleClicked = {},
    ),
    NewsArticle(
      title = "Tempuspurus congue elit euismo",
      description = "Conguemetus Duis enim tincidun",
      likes = 3,
      authors = listOf(
        "Namcondimentum lobortis et ali",
        "Congueeu ultrices lacinia sed ",
        "Lectussuscipit nisi eu quis se",
        "Utnisi sapien mi ex magna magn",
        "Proinipsum malesuada enim sed ",
      ),
      liveComments = flowOf(listOf(
        "CongueProin nec metus metus ma",
        "Antenisi consectetur ac purus ",
        "Eualiquet malesuada turpis rho",
        "LobortisDuis mollis ac a lacus",
        "Magnaet Donec libero Lorem sap",
      )),
      isExpanded = mutableStateOf(false),
      status = NewsArticle.Status.values().random(),
      onArticleClicked = {},
    ),
  )

}

```

## 5. How do I use `Dowel`?
There are only 3 `Dowel` annotations you need to know about:
1. [`@Dowel`](https://github.com/JayaSuryaT/Dowel/blob/main/dowel-annotation/src/main/java/com/jayasuryat/dowel/annotation/Dowel.kt) : The primary entry point into `Dowel`, triggeres generation `PreviewParameterProvider` for that class.
2. [`@DowelList`](https://github.com/JayaSuryaT/Dowel/blob/main/dowel-annotation/src/main/java/com/jayasuryat/dowel/annotation/DowelList.kt) : Same as `@Dowel`, but generates a `PreviewParameterProvider` of type `List<T>` where `T` is the class annotated with `@DowelList` annotation. Rest of the behavior is same as the `@Dowel` annotation.
3. [`@ConsiderForDowel`](https://github.com/JayaSuryaT/Dowel/blob/main/dowel-annotation/src/main/java/com/jayasuryat/dowel/annotation/ConsiderForDowel.kt) : If you want to add support for an unsupported type, or override provider logic for a particular type, then you can do that with `@ConsiderForDowel` annotation.

Apart from that if you want to controll range / legnth / size of the values being generated, you can do that with `androidx.annotations`. Currently these 3 are the only supported ones:
* `androidx.annotation.IntRange` : Control the range of `Int` and `Long` properties
* `androidx.annotation.FloatRange` : Control the range of `Float` and `Double` properties
* `androidx.annotation.Size` : Control the size of `String`, `List` and `Map` properties


## 6. What all is possible?
`Dowel` is quite flexible with the types it already supports, but there are certain limits on what all types are supported, and in general how `Dowel` works :

- Classes annotated with any of the `Dowel` annotations (`@Dowel`, `@DowelList` or `@ConsdierForDowel`) should be concrete (non-abstract)
- Primary constructors of classes annotated with any of the `Dowel` annotations should not be private
- Only classes extending `androidx.compose.ui.tooling.preview.PreviewParameterProvider` can be annotated with `@ConsiderForDowel`
- Only classes already annotated with `@Dowel` can be annotated with `@DowelList`
- All of the properties listed in the primary constructor of class annotated with `@Dowel` can only be of the following types:
  - Primitives (`Int`, `Long`, `Float`, `Double`, `Char`, `Boolean`, `String`)
  - `androidx.compose.runtime.State`
  - `kotlinx.coroutines.flow.Flow`
  - Functional types (high-order functions, lambdas)
  - `@Dowel` classes (`@Dowel` classes can be nested. A `@Dowel` annotated class can have properties of the type of classes which are again annotated with `@Dowel`)
  - Types for which a user-defined `PreviewParameterProvider` exist (via the `@ConsiderForDowel` annotation)
  - `Sealed` types
  - Kotlin Objects
  - `Enum`
  - `List`
  - `Map`
  - `Pair`
  - Properties with **unsupported** types which are nullable are allowed, and the generated value would always be null
  - Properties with default values can have *any type*, as they are not considered while generating code
  - Types in the above mentioned list having generic type parameters (like `List` and `Map`) can only have `@Dowel` supported types as their type parameters. Like `List<String>`, `Map<String, @Dowel class>`
- As far as a type is in above mentioned supported list, there are no practical limitations on how many times they may be nested.
Like `List<Map<String, List<@Dowel class>>>`

## `Dowel` ships with `lint` rules
`Dowel` ships with `lint` rules which cover all of the basic scenarios and will warn you even before you might compile the code.

And for the things that `lint` doesn't catch, like issues with unsupported types of properties, meaningful error messages will be logged from KSP to nudge you in the right direction.

## License
```
 Copyright 2022 Jaya Surya Thotapalli

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
