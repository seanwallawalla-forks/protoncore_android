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

package me.proton.core.payment.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityPaymentOptionsBinding
import me.proton.core.payment.presentation.databinding.ItemPaymentMethodBinding
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.CurrentSubscribedPlanDetails
import me.proton.core.payment.presentation.entity.PaymentOptionUIModel
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.viewmodel.BillingCommonViewModel
import me.proton.core.payment.presentation.viewmodel.PaymentOptionsViewModel
import me.proton.core.presentation.ui.adapter.selectableProtonAdapter
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class PaymentOptionsActivity : PaymentsActivity<ActivityPaymentOptionsBinding>(ActivityPaymentOptionsBinding::inflate) {

    private val viewModel by viewModels<PaymentOptionsViewModel>()

    private val input: PaymentOptionsInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val user by lazy {
        UserId(input.userId)
    }
    private var selectedPaymentMethodId: String? = null

    private var amountDue: Long? = null

    private val paymentOptionsAdapter = selectableProtonAdapter(
        getView = { parent, inflater -> ItemPaymentMethodBinding.inflate(inflater, parent, false) },
        onBind = { paymentMethod, selected, position ->
            paymentMethodTitleText.text = paymentMethod.title
            paymentMethodSubtitleText.text = paymentMethod.subtitle
            val paymentOptionType = PaymentMethodType.values()[paymentMethod.type]
            val drawable = when (paymentOptionType) {
                PaymentMethodType.CARD -> ContextCompat.getDrawable(
                    this@PaymentOptionsActivity,
                    R.drawable.ic_credit_card
                )
                PaymentMethodType.PAYPAL -> ContextCompat.getDrawable(this@PaymentOptionsActivity, R.drawable.ic_paypal)
            }.exhaustive
            paymentMethodIcon.setImageDrawable(drawable)
            paymentMethodRadio.isChecked = selected
            if (position == 0 && selectedPaymentMethodId == null) {
                paymentMethodRadio.isChecked = true
                selectedPaymentMethodId = paymentMethod.id
            }
        },
        onItemClick = ::onPaymentMethodClicked,
        diffCallback = PaymentOptionUIModel.DiffCallback
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            paymentMethodsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = paymentOptionsAdapter
            }
            addCreditCardButton.onClick {
                startBilling(
                    input.userId,
                    viewModel.currentPlans.map {
                        CurrentSubscribedPlanDetails(
                            name = it.name,
                            services = it.services,
                            type = it.type
                        )
                    },
                    input.plan.copy(amount = amountDue), input.codes
                )
            }
            selectedPlanDetailsLayout.plan = input.plan
            payButton.apply {
                isEnabled = false
                text = String.format(getString(R.string.payments_pay), selectedPlanDetailsLayout.userReadablePlanAmount)
                onClick {
                    viewModel.subscribe(
                        user,
                        input.plan.name,
                        input.plan.services,
                        input.plan.type,
                        input.codes,
                        input.plan.currency,
                        input.plan.subscriptionCycle,
                        PaymentType.PaymentMethod(selectedPaymentMethodId!!)
                    )
                }
            }
        }
        observe()

        viewModel.getAvailablePaymentMethods(user)
    }

    private fun observe() {
        viewModel.availablePaymentMethodsState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess -> onSuccess(it.availablePaymentMethods)
                    is PaymentOptionsViewModel.State.Error.General -> showError(it.error.getUserMessage(resources))
                    else -> {
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        viewModel.billingCommonViewModel.plansValidationState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is BillingCommonViewModel.PlansValidationState.Success -> {
                        amountDue = it.subscription.amountDue
                        with(binding) {
                            selectedPlanDetailsLayout.plan = input.plan.copy(amount = it.subscription.amountDue)
                            payButton.text = String.format(
                                getString(R.string.payments_pay),
                                selectedPlanDetailsLayout.userReadablePlanAmount
                            )
                        }
                    }
                    is BillingCommonViewModel.PlansValidationState.Error.Message -> showError(it.message)
                    else -> {
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        viewModel.billingCommonViewModel.subscriptionResult
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is BillingCommonViewModel.State.Processing -> showLoading(true)
                    is BillingCommonViewModel.State.Success.SubscriptionCreated -> onPaymentResult(
                        BillingResult(
                            paySuccess = true,
                            token = it.paymentToken,
                            subscriptionCreated = true,
                            amount = it.amount,
                            currency = it.currency,
                            cycle = it.cycle
                        )
                    )
                    is BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded ->
                        onTokenApprovalNeeded(input.userId, it.paymentToken, it.amount)
                    is BillingCommonViewModel.State.Error.General -> showError(it.error.getUserMessage(resources))
                    is BillingCommonViewModel.State.Error.SignUpWithPaymentMethodUnsupported ->
                        showError(getString(R.string.payments_error_signup_paymentmethod))
                    else -> {
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)
    }

    private fun onPaymentMethodClicked(paymentMethod: PaymentOptionUIModel) {
        selectedPaymentMethodId = paymentMethod.id
        paymentOptionsAdapter.notifyItemChanged(0) // invalidate the first option
    }

    override fun onThreeDSApprovalResult(amount: Long, token: String, success: Boolean) {
        if (!success) {
            binding.payButton.setIdle()
            return
        }
        viewModel.onThreeDSTokenApproved(
            user,
            input.plan.name,
            input.plan.services,
            input.plan.type,
            input.codes,
            amount,
            input.plan.currency,
            input.plan.subscriptionCycle,
            token
        )
    }

    private fun onSuccess(availablePaymentMethods: List<PaymentOptionUIModel>) {
        if (availablePaymentMethods.isEmpty()) {
            startBilling(
                input.userId,
                viewModel.currentPlans.map {
                    CurrentSubscribedPlanDetails(
                        name = it.name,
                        services = it.services,
                        type = it.type
                    )
                },
                input.plan.copy(amount = amountDue), input.codes
            )
            return
        }
        viewModel.validatePlan(
            user,
            input.plan.name,
            input.plan.services,
            input.plan.type,
            input.codes,
            input.plan.currency,
            input.plan.subscriptionCycle
        )
        paymentOptionsAdapter.submitList(availablePaymentMethods)
        binding.apply {
            payButton.isEnabled = true
            progressLayout.visibility = View.GONE
        }
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            payButton.setLoading()
        } else {
            payButton.setIdle()
        }
    }

    companion object {
        const val ARG_INPUT = "arg.paymentsOptionsInput"
    }
}
