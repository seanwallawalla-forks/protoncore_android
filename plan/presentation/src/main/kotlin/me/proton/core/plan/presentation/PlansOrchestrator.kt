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

package me.proton.core.plan.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.UpgradeResult
import me.proton.core.plan.presentation.ui.StartPlanChooser
import javax.inject.Inject

class PlansOrchestrator @Inject constructor() {

    private var plansLauncher: ActivityResultLauncher<PlanInput>? = null

    private var onUpgradeResultListener: ((result: UpgradeResult?) -> Unit)? = {}

    fun setOnUpgradeResult(block: (result: UpgradeResult?) -> Unit) {
        onUpgradeResultListener = block
    }

    private fun registerPlanResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<PlanInput> =
        caller.registerForActivityResult(
            StartPlanChooser()
        ) {
            onUpgradeResultListener?.invoke(it)
        }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        plansLauncher = registerPlanResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        plansLauncher?.unregister()
        plansLauncher = null
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call PlansOrchestrator.register(context) before starting workflow!" }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun startSignUpPlanChooserWorkflow() {
        checkRegistered(plansLauncher).launch(
            PlanInput()
        )
    }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun showCurrentPlanWorkflow(userId: UserId) {
        checkRegistered(plansLauncher).launch(
            PlanInput(userId = userId.id, showSubscription = true)
        )
    }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun startUpgradeWorkflow(userId: UserId) {
        checkRegistered(plansLauncher).launch(
            PlanInput(userId = userId.id, showSubscription = false)
        )
    }
}

fun PlansOrchestrator.onUpgradeResult(
    block: (result: UpgradeResult?) -> Unit
): PlansOrchestrator {
    setOnUpgradeResult { block(it) }
    return this
}
