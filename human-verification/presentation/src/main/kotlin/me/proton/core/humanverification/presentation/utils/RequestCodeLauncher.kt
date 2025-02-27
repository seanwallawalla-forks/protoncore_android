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

package me.proton.core.humanverification.presentation.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher
import me.proton.core.presentation.ui.alert.ProtonCancellableAlertDialog

fun FragmentManager.registerRequestNewCodeDialogResultLauncher(
    fragment: Fragment,
    onResult: () -> Unit,
): FragmentDialogResultLauncher<String> {
    setFragmentResultListener(ProtonCancellableAlertDialog.KEY_ACTION_DONE, fragment) { _, _ ->
        onResult()
    }
    return FragmentDialogResultLauncher(
        requestKey = ProtonCancellableAlertDialog.KEY_ACTION_DONE,
        show = { input -> showRequestNewCodeDialog(fragment.requireContext(), input) }
    )
}
