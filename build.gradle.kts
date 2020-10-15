/*
 * Copyright (c) 2020 Proton Technologies AG
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

/**
 * Registered tasks:
 * * `allTest` ( 'me.proton.tests' plugin )
 * * `detekt` ( 'me.proton.detekt' plugin )
 * * `multiModuleDetekt` ( 'me.proton.detekt' plugin )
 * * `publishAll` ( 'me.proton.publish-libraries' plugin )
 * * `dokka`
 */
plugins {
    id("core")
    id("me.proton.detekt")
    id("me.proton.kotlin")
    id("me.proton.publish-libraries")
    id("me.proton.tests")
}

buildscript {
    repositories.google()

    dependencies {
        val kotlinVersion = "1.4.10" // Sep 09, 2020
        val agpVersion = "4.2.0-alpha13"
        val hiltVersion = "2.29.1-alpha" // Sep 10, 2020

        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("serialization", kotlinVersion))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
        classpath("com.android.tools.build:gradle:$agpVersion")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
    }
}

kotlinCompilerArgs(
    "-XXLanguage:+NewInference",
    "-Xuse-experimental=kotlin.Experimental",
    // Enables inline classes
    "-XXLanguage:+InlineClasses",
    // Enables experimental Coroutines from coroutines-test artifact, like `runBlockingTest`
    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
)
// setupDokka()

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
