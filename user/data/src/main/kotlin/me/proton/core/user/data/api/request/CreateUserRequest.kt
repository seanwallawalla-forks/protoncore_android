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

package me.proton.core.user.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.key.data.api.request.AuthRequest

@Serializable
data class CreateUserRequest(
    @SerialName("Username")
    val username: String,
    @SerialName("Email")
    val email: String?,
    @SerialName("Phone")
    val phone: String?,
    @SerialName("Referrer")
    val referrer: String?,
    @SerialName("Type")
    val type: Int,
    @SerialName("Auth")
    val auth: AuthRequest,
    @SerialName("Domain")
    val domain: String?,
    @SerialName("Payload")
    val payload: ChallengePayload
)
