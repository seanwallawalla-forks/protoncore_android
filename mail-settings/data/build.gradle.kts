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
import studio.forface.easygradle.dsl.`coroutines-core`
import studio.forface.easygradle.dsl.`serialization-json`
import studio.forface.easygradle.dsl.android.`android-work-runtime`
import studio.forface.easygradle.dsl.android.`retrofit-kotlin-serialization`
import studio.forface.easygradle.dsl.android.`room-ktx`
import studio.forface.easygradle.dsl.android.androidTestImplementation
import studio.forface.easygradle.dsl.android.retrofit
import studio.forface.easygradle.dsl.implementation
import studio.forface.easygradle.dsl.testImplementation

plugins {
    protonAndroidLibrary
    protonDagger
    kotlin("plugin.serialization")
}

proton {
    apiModeDisabled()
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

dependencies {

    implementation(
        project(Module.kotlinUtil),
        project(Module.data),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.network),
        project(Module.mailSettingsDomain),
        project(Module.userData),
        project(Module.eventManagerDomain),

        // Kotlin
        `serialization-json`,
        `coroutines-core`,

        // Other
        `android-work-runtime`,
        `javax-inject`,
        `okHttp-logging`,
        `retrofit`,
        `retrofit-kotlin-serialization`,
        `room-ktx`,
        `store4`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}
