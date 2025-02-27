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

package me.proton.core.challenge.data.repository

import io.mockk.InternalPlatformDsl.toArray
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import junit.framework.TestCase
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.challenge.data.ChallengeManagerImpl
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.challenge.data.db.ChallengeFramesDao
import me.proton.core.challenge.data.entity.ChallengeFrameEntity
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.repository.ChallengeRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

public class ChallengeRepositoryImplTest {

    private val dao = mockk<ChallengeFramesDao>(relaxed = true)
    private val db = mockk<ChallengeDatabase>(relaxed = true)

    private lateinit var repository: ChallengeRepository

    @Before
    public fun beforeEveryTest() {
        every { db.challengeFramesDao() } returns dao
        repository = ChallengeRepositoryImpl(db)

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { db.inTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    public fun afterEveryTest() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    public fun `insert new frame details works correctly`(): Unit = runBlockingTest {
        // Given
        val flow = "test-flow"
        val frame = "test-frame"
        val frameDetails = ChallengeFrameDetails(
            flow, frame, 0, 0, emptyList(), emptyList(), emptyList()
        )
        coEvery { dao.getByFlowAndFrame(flow, frame) } returns null
        // When
        repository.insertFrameDetails(frameDetails)
        // Then
        val entity = ChallengeFrameEntity(
            frame, flow, 0, 0, emptyList(), emptyList(), emptyList()
        )
        coVerify { dao.insertOrUpdate(entity) }
    }

    @Test
    public fun `update frame details works correctly`(): Unit = runBlockingTest {
        // Given
        val flow = "test-flow"
        val frame = "test-frame"
        val frameDetails = ChallengeFrameDetails(
            flow, frame, 2, 2, listOf("new copy"), listOf("new paste"), listOf("h")
        )
        coEvery { dao.getByFlowAndFrame(flow, frame) } returns ChallengeFrameEntity(
            frame, flow, 1, 1, listOf("copy"), listOf("paste"), listOf("c")
        )
        // When
        repository.insertFrameDetails(frameDetails)
        // Then
        val entity = ChallengeFrameEntity(
            frame, flow, 3, 3, listOf("copy", "new copy"), listOf("paste", "new paste"), listOf("c", "h")
        )
        coVerify { dao.insertOrUpdate(entity) }
    }
}
