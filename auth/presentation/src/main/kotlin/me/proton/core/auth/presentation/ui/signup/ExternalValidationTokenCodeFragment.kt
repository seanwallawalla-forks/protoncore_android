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

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupValidationTokenCodeBinding
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationEnterCodeViewModel
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.showToast
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class ExternalValidationTokenCodeFragment : SignupFragment(R.layout.fragment_signup_validation_token_code) {

    private val viewModel by viewModels<HumanVerificationEnterCodeViewModel>()
    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupValidationTokenCodeBinding::bind)

    private val destination: String by lazy {
        requireArguments().get(ARG_DESTINATION) as String
    }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            title.text = String.format(getString(R.string.human_verification_enter_code_subtitle), destination)

            verifyButton.onClick {
                hideKeyboard()
                verificationCodeEditText.validate()
                    .onFailure { verificationCodeEditText.setInputError() }
                    .onSuccess { code ->
                        viewModel.validateToken(
                            sessionId = null,
                            token = viewModel.getToken(destination, code),
                            tokenType = TokenType.EMAIL,
                        )
                    }
            }
            requestReplacementButton.onClick {
                hideKeyboard()
                viewModel.resendCode(
                    sessionId = null,
                    destination = destination,
                    tokenType = TokenType.EMAIL
                )
            }
        }

        viewModel.validationState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is ViewModelResult.None -> Unit
                    is ViewModelResult.Processing -> showLoading()
                    is ViewModelResult.Error -> onValidationError(it.throwable?.getUserMessage(resources))
                    is ViewModelResult.Success -> onValidationSuccess()
                }.exhaustive
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.verificationCodeResendState.onEach {
            when (it) {
                is ViewModelResult.None -> Unit
                is ViewModelResult.Error -> showError(it.throwable?.getUserMessage(resources))
                is ViewModelResult.Processing -> showLoading(true)
                is ViewModelResult.Success -> {
                    showLoading(false)
                    context?.showToast(getString(R.string.auth_signup_token_code_resent))
                }
            }.exhaustive
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        signupViewModel.userCreationState.onEach {
            // this fragment is only interested in HV and error states, all other are handled by the activity
            when (it) {
                is SignupViewModel.State.Error.HumanVerification,
                is SignupViewModel.State.Error.PlanChooserCancel,
                is SignupViewModel.State.Error.Message -> showLoading(false)
                is SignupViewModel.State.Idle,
                is SignupViewModel.State.Processing,
                is SignupViewModel.State.Success -> Unit
            }.exhaustive
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onValidationError(message: String?) {
        showLoading(false)
        showError(message)
    }

    private fun onValidationSuccess() {
        signupViewModel.setExternalAccountEmailValidationDone()
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            verifyButton.setLoading()
        } else {
            verifyButton.setIdle()
        }
    }

    companion object {
        private const val ARG_DESTINATION = "arg.destination"

        operator fun invoke(
            destination: String
        ) = ExternalValidationTokenCodeFragment().apply {
            arguments = bundleOf(
                ARG_DESTINATION to destination
            )
        }
    }
}
