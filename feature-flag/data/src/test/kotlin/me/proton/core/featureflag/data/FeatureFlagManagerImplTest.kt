/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.featureflag.data

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FeatureFlagManagerImplTest {

    private val repository = mockk<FeatureFlagRepository>()

    private lateinit var manager: FeatureFlagManager

    @Before
    fun setUp() {
        manager = FeatureFlagManagerImpl(repository)
    }

    @Test
    fun observeReturnsValueFromRepository() = runBlockingTest {
        // Given
        val repositoryFeatureFlag = FeatureFlag(FeatureFlagTestData.featureId, true)
        every { repository.observe(any(), any(), false) } returns flowOf(repositoryFeatureFlag)

        // When
        val actual = manager.observe(UserIdTestData.userId, FeatureFlagTestData.featureId).first()

        // Then
        verify { repository.observe(UserIdTestData.userId, FeatureFlagTestData.featureId, false) }
        assertEquals(repositoryFeatureFlag, actual)
    }

    @Test
    fun getReturnsValueFromRepository() = runBlockingTest {
        // Given
        val repositoryFeatureFlag = FeatureFlag(FeatureFlagTestData.featureId, true)
        coEvery { repository.get(any(), any()) } returns repositoryFeatureFlag

        // When
        val actual = manager.get(UserIdTestData.userId, FeatureFlagTestData.featureId)

        // Then
        coVerify { repository.get(UserIdTestData.userId, FeatureFlagTestData.featureId) }
        assertEquals(repositoryFeatureFlag, actual)
    }

    @Test
    fun prefetchCallsRepository() = runBlockingTest {
        // Given
        coEvery { repository.prefetch(any(), any()) } just Runs

        // When
        val featureIds = listOf(FeatureFlagTestData.featureId, FeatureFlagTestData.featureId1)
        manager.prefetch(UserIdTestData.userId, featureIds)

        // Then
        coVerify { repository.prefetch(UserIdTestData.userId, featureIds) }
    }
}
