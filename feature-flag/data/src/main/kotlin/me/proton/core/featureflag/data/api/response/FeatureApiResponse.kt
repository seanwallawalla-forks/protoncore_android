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

package me.proton.core.featureflag.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.entity.FeatureFlagEntity

@Serializable
internal data class FeaturesApiResponse(
    @SerialName("Code")
    val resultCode: Int,
    @SerialName("Features")
    val features: List<FeatureApiResponse>
)

@Serializable
internal data class FeatureApiResponse(
    @SerialName("Code")
    val featureId: String,
    @SerialName("Global")
    val isGlobal: Boolean,
    @SerialName("DefaultValue")
    val defaultValue: Boolean,
    @SerialName("Value")
    val value: Boolean
) {
    internal fun toEntity(userId: UserId?) = FeatureFlagEntity(
        featureId = featureId,
        userId = userId,
        isGlobal = isGlobal,
        defaultValue = defaultValue,
        value = value
    )
}
