/*
 * Copyright 2024 Jaya Surya Thotapalli
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
package com.jayasuryat.dowel.processor.model

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import com.jayasuryat.dowel.processor.Names
import com.jayasuryat.dowel.processor.util.unsafeLazy

/**
 * This class provides access to various Kotlin Symbol (KSType) declarations for commonly used types.
 * @param resolver The resolver used for symbol resolution.
 */
internal class ExistingDeclarations(
    private val resolver: Resolver,
) {

    private val builtIns: KSBuiltIns = resolver.builtIns

    internal val list: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableListName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    internal val set: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableSetName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    internal val map: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableMapName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    internal val persistentList: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.persistentList.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.unitType
    }
    internal val persistentSet: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.persistentSet.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.unitType
    }
    internal val persistentMap: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.persistentMap.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.unitType
    }
    internal val mutableStateFlow: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableStateFlowName.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.unitType
    }
    internal val pair: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Pair::class.qualifiedName!!)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    internal val state: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.stateName.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.nothingType
    }
    internal val color: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.colorName.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.nothingType
    }
}
