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

package me.proton.core.auth.presentation.ui.signup

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.domain.entity.Product
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher
import me.proton.core.presentation.ui.alert.ProtonCancellableAlertDialog
import me.proton.core.presentation.utils.inTransaction

private const val TAG_USERNAME_CHOOSER = "username_chooser_fragment"
private const val TAG_PASSWORD_CHOOSER = "password_chooser_fragment"
private const val TAG_RECOVERY_CHOOSER = "recovery_chooser_fragment"
private const val TAG_SKIP_RECOVERY_DIALOG = "skip_recovery_dialog"
private const val TAG_EXTERNAL_ACCOUNT_ENTER_CODE = "external_account_enter_code_fragment"
private const val TAG_EMAIL_RECOVERY_FRAGMENT = "email_recovery_fragment"
private const val TAG_SMS_RECOVERY_FRAGMENT = "skip_recovery_fragment"
private const val TAG_TERMS_CONDITIONS_FRAGMENT = "terms_conditions_fragment"

fun FragmentManager.registerSkipRecoveryDialogResultLauncher(
    fragment: Fragment,
    onResult: () -> Unit
): FragmentDialogResultLauncher<Unit> {
    setFragmentResultListener(ProtonCancellableAlertDialog.KEY_ACTION_DONE, fragment) { _, _ ->
        onResult()
    }
    return FragmentDialogResultLauncher(
        requestKey = ProtonCancellableAlertDialog.KEY_ACTION_DONE,
        show = { showSkipRecoveryDialog(fragment.requireContext()) }
    )
}


internal fun FragmentManager.showEmailRecoveryMethodFragment(
    containerId: Int = android.R.id.content,
) = findFragmentByTag(TAG_EMAIL_RECOVERY_FRAGMENT) ?: run {
    val emailRecoveryFragment = RecoveryEmailFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, emailRecoveryFragment)
    }
    emailRecoveryFragment
}

internal fun FragmentManager.showSMSRecoveryMethodFragment(
    containerId: Int = android.R.id.content,
) = findFragmentByTag(TAG_SMS_RECOVERY_FRAGMENT) ?: run {
    val smsRecoveryFragment = RecoverySMSFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, smsRecoveryFragment)
    }
    smsRecoveryFragment
}

internal fun FragmentManager.showTermsConditions() {
    val termsConditionsDialogFragment = TermsConditionsDialogFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(termsConditionsDialogFragment, TAG_TERMS_CONDITIONS_FRAGMENT)
        addToBackStack(TAG_TERMS_CONDITIONS_FRAGMENT)
    }
}

internal fun FragmentManager.showUsernameChooser(
    containerId: Int = android.R.id.content,
    requiredAccountType: AccountType
) = findFragmentByTag(TAG_USERNAME_CHOOSER) ?: run {
    val chooserUsernameFragment = ChooseUsernameFragment(requiredAccountType = requiredAccountType)
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, chooserUsernameFragment, TAG_USERNAME_CHOOSER)
        addToBackStack(TAG_USERNAME_CHOOSER)
    }
    chooserUsernameFragment
}

internal fun FragmentManager.showPasswordChooser(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_PASSWORD_CHOOSER) ?: run {
    val chooserPasswordFragment = ChoosePasswordFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, chooserPasswordFragment, TAG_PASSWORD_CHOOSER)
        addToBackStack(TAG_PASSWORD_CHOOSER)
    }
    chooserPasswordFragment
}

internal fun FragmentManager.showExternalAccountEnterCode(
    containerId: Int = android.R.id.content,
    destination: String
) = findFragmentByTag(TAG_EXTERNAL_ACCOUNT_ENTER_CODE) ?: run {
    val enterCodeFragment = ExternalValidationTokenCodeFragment(destination)
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, enterCodeFragment)
        addToBackStack(TAG_EXTERNAL_ACCOUNT_ENTER_CODE)
    }
    enterCodeFragment
}

internal fun FragmentManager.showRecoveryMethodChooser(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_RECOVERY_CHOOSER) ?: run {
    val recoveryMethodFragment = RecoveryMethodFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, recoveryMethodFragment, TAG_RECOVERY_CHOOSER)
        addToBackStack(TAG_RECOVERY_CHOOSER)
    }
    recoveryMethodFragment
}

internal fun FragmentManager.showSkipRecoveryDialog(
    context: Context
) {
    findFragmentByTag(TAG_SKIP_RECOVERY_DIALOG) ?: run {
        val updateDialogFragment = ProtonCancellableAlertDialog(
            title = context.getString(R.string.auth_signup_skip_recovery_title),
            description = context.getString(R.string.auth_signup_skip_recovery_description),
            positiveButton = context.getString(R.string.auth_signup_skip_recovery),
            negativeButton = context.getString(R.string.auth_signup_set_recovery)
        )
        updateDialogFragment.show(this, TAG_SKIP_RECOVERY_DIALOG)
    }
}
