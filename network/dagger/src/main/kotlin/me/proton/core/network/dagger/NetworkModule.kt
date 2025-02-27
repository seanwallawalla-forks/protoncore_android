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

package me.proton.core.network.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.data.client.ClientVersionValidatorImpl
import me.proton.core.network.data.cookie.DiskCookieStorage
import me.proton.core.network.data.cookie.MemoryCookieStorage
import me.proton.core.network.domain.client.ClientVersionValidator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NetworkModule {
    @Provides
    @Singleton
    fun provideCookieJar(@ApplicationContext context: Context): ProtonCookieStore = ProtonCookieStore(
        persistentStorage = DiskCookieStorage(context, ProtonCookieStore.DISK_COOKIE_STORAGE_NAME),
        sessionStorage = MemoryCookieStorage()
    )

    @Provides
    fun provideClientVersionValidator(): ClientVersionValidator = ClientVersionValidatorImpl()
}
