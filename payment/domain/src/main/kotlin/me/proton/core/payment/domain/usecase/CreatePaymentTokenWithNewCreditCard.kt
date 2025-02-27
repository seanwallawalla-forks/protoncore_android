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

package me.proton.core.payment.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

/**
 * Creates new payment token.
 * Only for new PayPal payments methods provided with [PaymentType.CreditCard].
 * For payment tokens with new Credit Card payment method @see [CreatePaymentTokenWithNewPayPal].
 * For payment tokens with existing payment method @see [CreatePaymentTokenWithExistingPaymentMethod].
 */
class CreatePaymentTokenWithNewCreditCard @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {
    suspend operator fun invoke(
        userId: UserId?,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType.CreditCard,
    ): PaymentToken.CreatePaymentTokenResult {
        require(amount >= 0)

        return paymentsRepository.createPaymentTokenNewCreditCard(
            userId,
            amount,
            currency,
            paymentType
        )
    }
}
