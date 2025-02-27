/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    protonAndroidLibrary
    kotlin("plugin.serialization")
}

proton {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    implementation(
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        project(Module.networkData),
        project(Module.data),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.userDomain),
        project(Module.eventManagerDomain),
        project(Module.cryptoCommon),
        project(Module.authData),
        project(Module.authDomain),
        project(Module.challengeData),
        project(Module.challengeDomain),

        // Features
        project(Module.key),
        project(Module.account),

        // Kotlin
        `serialization-json`,
        `coroutines-core`,

        // Other
        `javax-inject`,
        `okHttp-logging`,
        `retrofit`,
        `retrofit-kotlin-serialization`,
        `room-ktx`,
        `store4`,
        `googleTink`
    )

    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        project(Module.domain),
        project(Module.auth),
        project(Module.account),
        project(Module.accountManager),
        project(Module.accountManagerDataDb),
        project(Module.cryptoAndroid),
        project(Module.gopenpgp),
        project(Module.userSettings),
        project(Module.contact),
        project(Module.eventManager),
        project(Module.label),
        project(Module.featureFlag),
    )
}
