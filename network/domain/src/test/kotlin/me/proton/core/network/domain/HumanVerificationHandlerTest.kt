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

package me.proton.core.network.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.network.domain.handlers.HumanVerificationNeededHandler
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

/**
 * Tests for Human Verification domain handler.
 */
class HumanVerificationHandlerTest {

    private val sessionId = mockk<SessionId>(relaxed = true)
    private val clientId = mockk<ClientId>(relaxed = true)

    private val clientIdProvider = mockk<ClientIdProvider>()
    private val humanVerificationListener = mockk<HumanVerificationListener>()

    private val apiBackend = mockk<ApiBackend<Any>>()

    private var time = 0L

    @BeforeTest
    fun beforeTest() {
        coEvery { clientIdProvider.getClientId(any()) } returns clientId
        coEvery { humanVerificationListener.notifyHumanVerificationProcessFinished(any()) } answers {}
        // Assume no token has been refreshed between each tests.
        runBlocking { HumanVerificationNeededHandler.reset(clientId) }
    }

    @Test
    fun `test human verification called`() = runBlockingTest {
        val humanVerificationDetails =
            HumanVerificationAvailableMethods(listOf(VerificationMethod.CAPTCHA, VerificationMethod.EMAIL), "test")
        val apiResult = ApiResult.Error.Http(
            422,
            "Human Verification required",
            ApiResult.Error.ProtonData(
                9001,
                "Human Verification required",
                humanVerificationDetails
            )
        )

        coEvery {
            humanVerificationListener.onHumanVerificationNeeded(
                clientId,
                any()
            )
        } returns HumanVerificationListener.HumanVerificationResult.Success

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")

        val humanVerificationHandler =
            HumanVerificationNeededHandler<Any>(sessionId, clientIdProvider, humanVerificationListener, ::time)

        val result = humanVerificationHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) {
            humanVerificationListener.onHumanVerificationNeeded(clientId, humanVerificationDetails)
        }
    }

    @Test
    fun `test human verification called but retry api failed`() = runBlockingTest {
        val humanVerificationDetails =
            HumanVerificationAvailableMethods(listOf(VerificationMethod.CAPTCHA, VerificationMethod.EMAIL), "test")
        val apiResult = ApiResult.Error.Http(
            422,
            "Human Verification required",
            ApiResult.Error.ProtonData(
                9001,
                "Human Verification required",
                humanVerificationDetails
            )
        )

        coEvery {
            humanVerificationListener.onHumanVerificationNeeded(
                clientId,
                any()
            )
        } returns HumanVerificationListener.HumanVerificationResult.Success

        coEvery { apiBackend.invoke<Any>(any()) } returns apiResult

        val humanVerificationHandler =
            HumanVerificationNeededHandler<Any>(sessionId, clientIdProvider, humanVerificationListener, ::time)

        val result = humanVerificationHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) {
            humanVerificationListener.onHumanVerificationNeeded(clientId, humanVerificationDetails)
        }
    }

    @Test
    fun `test human verification not called on other errors`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            ApiResult.Error.ProtonData(
                9000,
                "Some error"
            )
        )

        val humanVerificationHandler =
            HumanVerificationNeededHandler<Any>(sessionId, clientIdProvider, humanVerificationListener, ::time)

        val result = humanVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            humanVerificationListener.onHumanVerificationNeeded(clientId, any())
        }
    }

    @Test
    fun `test no proton data does not crash the handler`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            null
        )

        val humanVerificationHandler =
            HumanVerificationNeededHandler<Any>(sessionId, clientIdProvider, humanVerificationListener, ::time)

        val result = humanVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            humanVerificationListener.onHumanVerificationNeeded(clientId, any())
        }
    }

    @Test
    fun `test connectivity error does not invoke human verification`() = runBlockingTest {
        val apiResult = ApiResult.Error.Connection(
            false
        )

        val humanVerificationHandler =
            HumanVerificationNeededHandler<Any>(sessionId, clientIdProvider, humanVerificationListener, ::time)

        val result = humanVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            humanVerificationListener.onHumanVerificationNeeded(clientId, any())
        }
    }
}
