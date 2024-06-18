/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent.reference

import android.app.Activity
import android.content.Context
import com.chartboost.core.ChartboostCoreLogger
import com.chartboost.core.consent.*
import com.chartboost.core.consent.reference.sdk.ReferenceConsentSdk
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.initialization.Module
import com.chartboost.core.initialization.ModuleConfiguration
import org.json.JSONObject

class ReferenceConsentAdapter() : ConsentAdapter, Module {

    companion object {
        const val moduleId = "reference_consent"
        const val moduleVersion = BuildConfig.CHARTBOOST_CORE_REFERENCE_CONSENT_ADAPTER_VERSION
        const val REFERENCE_CONSENT_STATUS_KEY = "reference_consent_status"
    }

    override fun updateCredentials(context: Context, credentials: JSONObject) {
        // Use this method to update any state that you expect to get from the server.
    }

    override val moduleId: String = Companion.moduleId

    override val moduleVersion: String = Companion.moduleVersion

    override val shouldCollectConsent: Boolean
        get() = ReferenceConsentSdk.shouldShowConsentDialog

    override val consents: Map<ConsentKey, ConsentValue>
        get() = mutableConsents

    private val mutableConsents = mutableMapOf<ConsentKey, ConsentValue>()

    override val sharedPreferencesIabStrings: MutableMap<String, String> = mutableMapOf()
    override val sharedPreferenceChangeListener: ConsentAdapter.IabSharedPreferencesListener =
        ConsentAdapter.IabSharedPreferencesListener(sharedPreferencesIabStrings)

    override var listener: ConsentAdapterListener? = null
        set(value) {
            field = value
            sharedPreferenceChangeListener.listener = value
        }

    override suspend fun showConsentDialog(
        activity: Activity, dialogType: ConsentDialogType
    ): Result<Unit> {
        return when (dialogType) {
            ConsentDialogType.CONCISE -> {
                ReferenceConsentSdk.showConciseDialog(activity)
                Result.success(Unit)
            }

            ConsentDialogType.DETAILED -> {
                ReferenceConsentSdk.showDetailedDialog(activity)
                Result.success(Unit)
            }

            else -> {
                ChartboostCoreLogger.d("Unexpected consent dialog type: $dialogType")
                Result.failure(ChartboostCoreException(ChartboostCoreError.ConsentError.DialogShowError))
            }
        }
    }

    override suspend fun grantConsent(
        context: Context, statusSource: ConsentSource
    ): Result<Unit> {
        val previousConsentStatus = ReferenceConsentSdk.referenceConsentStatus
        ReferenceConsentSdk.grantConsent()
        mutableConsents[REFERENCE_CONSENT_STATUS_KEY] = ReferenceConsentSdk.referenceConsentStatus
        // Only notify if anything changed
        if (previousConsentStatus != ReferenceConsentSdk.referenceConsentStatus) {
            listener?.onConsentChange(REFERENCE_CONSENT_STATUS_KEY)
        }
        return Result.success(Unit)
    }

    override suspend fun denyConsent(
        context: Context, statusSource: ConsentSource
    ): Result<Unit> {
        val previousConsentStatus = ReferenceConsentSdk.referenceConsentStatus
        ReferenceConsentSdk.denyConsent()
        mutableConsents[REFERENCE_CONSENT_STATUS_KEY] = ReferenceConsentSdk.referenceConsentStatus
        // Only notify if anything changed
        if (previousConsentStatus != ReferenceConsentSdk.referenceConsentStatus) {
            listener?.onConsentChange(REFERENCE_CONSENT_STATUS_KEY)
        }
        return Result.success(Unit)
    }

    override suspend fun resetConsent(context: Context): Result<Unit> {
        val previousConsentStatus = ReferenceConsentSdk.referenceConsentStatus
        // Some consent management platforms use this to clear all consents and re-fetch them.
        // Sometimes it's also a good idea to automatically re-initialize the CMP.
        ReferenceConsentSdk.reset()
        ReferenceConsentSdk.initialize()
        mutableConsents[REFERENCE_CONSENT_STATUS_KEY] = ReferenceConsentSdk.referenceConsentStatus
        if (previousConsentStatus != ReferenceConsentSdk.referenceConsentStatus) {
            listener?.onConsentChange(REFERENCE_CONSENT_STATUS_KEY)
        }
        return Result.success(Unit)
    }

    override suspend fun initialize(context: Context, moduleConfiguration: ModuleConfiguration): Result<Unit> {
        // Initialize the underlying consent management platform
        ReferenceConsentSdk.initialize()
        mutableConsents[REFERENCE_CONSENT_STATUS_KEY] = ReferenceConsentSdk.referenceConsentStatus
        return Result.success(Unit)
    }
}
