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

package me.proton.core.humanverification.presentation.ui.verification

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationEnterCodeBinding
import me.proton.core.humanverification.presentation.ui.hv2.HV2DialogFragment
import me.proton.core.humanverification.presentation.ui.hv2.HV2DialogFragment.Companion.ARG_TOKEN_CODE
import me.proton.core.humanverification.presentation.ui.hv2.HV2DialogFragment.Companion.KEY_VERIFICATION_DONE
import me.proton.core.humanverification.presentation.utils.registerRequestNewCodeDialogResultLauncher
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationEnterCodeViewModel
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.protectScreen
import me.proton.core.presentation.utils.successSnack
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.presentation.viewmodel.onError
import me.proton.core.presentation.viewmodel.onProcessing
import me.proton.core.presentation.viewmodel.onSuccess

@AndroidEntryPoint
class HumanVerificationEnterCodeFragment : ProtonDialogFragment(R.layout.fragment_human_verification_enter_code) {

    private val screenProtector by protectScreen()

    private val viewModel by viewModels<HumanVerificationEnterCodeViewModel>()
    private val binding by viewBinding(FragmentHumanVerificationEnterCodeBinding::bind)

    private lateinit var requestNewCodeDialogResultLauncher: FragmentDialogResultLauncher<String>

    private val sessionId: SessionId? by lazy {
        requireArguments().getString(ARG_SESSION_ID)?.let { SessionId(it) }
    }

    private val destination: String? by lazy {
        requireArguments().get(ARG_DESTINATION) as String?
    }

    private val tokenType: TokenType by lazy {
        val type = requireArguments().getString(ARG_TOKEN_TYPE)
        TokenType.fromString(type)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestNewCodeDialogResultLauncher =
            parentFragmentManager.registerRequestNewCodeDialogResultLauncher(this@HumanVerificationEnterCodeFragment) {
                destination?.let { viewModel.resendCode(sessionId, it, tokenType) }
            }

        destination?.let {
            binding.title.text = if (tokenType == TokenType.EMAIL) {
                String.format(getString(R.string.human_verification_enter_code_subtitle_email), destination)
            } else {
                String.format(getString(R.string.human_verification_enter_code_subtitle), destination)
            }
        } ?: run {
            binding.title.text = getString(R.string.human_verification_enter_code_subtitle_already_have_code)
            binding.requestReplacementButton.isVisible = false
        }

        binding.apply {
            // this should go inside the custom edit text input view (validation also with error text below the view)
            toolbar.apply {
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_proton_arrow_back)
                setNavigationOnClickListener { onBackPressed() }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_help -> {
                            childFragmentManager.showHelp()
                            true
                        }
                        else -> false
                    }
                }
            }
            verifyButton.onClick {
                hideKeyboard()
                verificationCodeEditText.validate()
                    .onFailure { verificationCodeEditText.setInputError() }
                    .onSuccess { code ->
                        val token = viewModel.getToken(destination, code)
                        viewModel.validateToken(sessionId, token, tokenType)
                    }
            }
            requestReplacementButton.onClick {
                destination?.let { requestNewCodeDialogResultLauncher.show(it) }
            }
        }

        viewModel.verificationCodeResendState
            .onSuccess { showCodeResent() }
            .onError { showError(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.validationState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onProcessing { showLoading() }
            .onSuccess { tokenCodeValidated(it) }
            .onError { showError(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showLoading() {
        binding.verifyButton.setLoading()
    }

    private fun showError(error: Throwable?) {
        binding.verifyButton.setIdle()
        error?.message?.let { view?.errorSnack(it) }
    }

    private fun showCodeResent() {
        binding.verifyButton.setIdle()
        view?.successSnack(R.string.human_verification_resent_code)
    }

    private fun tokenCodeValidated(tokenCode: String) {
        parentFragmentManager.setFragmentResult(
            KEY_VERIFICATION_DONE,
            bundleOf(
                ARG_TOKEN_CODE to tokenCode,
                HV2DialogFragment.ARG_TOKEN_TYPE to tokenType.value
            )
        )
    }

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }

    companion object {
        private const val ARG_SESSION_ID = "arg.sessionId"
        private const val ARG_DESTINATION = "arg.destination"
        private const val ARG_TOKEN_TYPE = "arg.enter-code-token-type"

        operator fun invoke(
            sessionId: String?,
            tokenType: TokenType,
            destination: String?
        ) = HumanVerificationEnterCodeFragment().apply {
            arguments = bundleOf(
                ARG_SESSION_ID to sessionId,
                ARG_DESTINATION to destination,
                ARG_TOKEN_TYPE to tokenType.value
            )
        }
    }
}
