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

package me.proton.core.test.android.uitests.tests

import android.app.Application
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import me.proton.android.core.coreexample.BuildConfig
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.auth.presentation.testing.ProtonTestEntryPoint
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.android.instrumented.utils.Shell.setupDeviceForAutomation
import me.proton.core.test.android.plugins.Quark
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.plugins.data.User.Users
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import org.junit.After
import org.junit.BeforeClass

open class BaseTest(
    private val logoutAllAfterTest: Boolean = true,
    defaultTimeout: Long = 30_000L,
) : ProtonTest(MainActivity::class.java, defaultTimeout, 2) {

    @After
    fun logoutUsers() {
        if (logoutAllAfterTest) {
            Log.d(testTag, "Logging out users")
            authHelper.logoutAll()
        }
    }

    fun login(user: User) {
        Log.d(testTag, "Login user: ${user.name}")
        authHelper.login(user.name, user.password)
        Log.d(testTag, "Login done.")
        AddAccountRobot().back<CoreexampleRobot>().verify { accountSwitcherDisplayed() }
    }

    companion object {
        val users = Users("sensitive/users.json")
        val quark = Quark(BuildConfig.HOST, BuildConfig.PROXY_TOKEN, "sensitive/internal_apis.json")
        val authHelper = ProtonTestEntryPoint.provide(ApplicationProvider.getApplicationContext<Application>())

        @JvmStatic
        @BeforeClass
        fun prepare() {
            setupDeviceForAutomation(true)
            authHelper.logoutAll()
            Plan.Dev.text = Plan.Plus.text
            Plan.Dev.planName = Plan.Plus.planName
        }
    }
}
