package com.vitantonio.nagauzzi.walkreminder.backend

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.vitantonio.nagauzzi.walkreminder.GOOGLE_FIT_PACKAGE_NAME
import com.vitantonio.nagauzzi.walkreminder.LOG_TAG

val fitnessOptions: FitnessOptions
    get() = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

val Context.googleAccount: GoogleSignInAccount?
    get() = GoogleSignIn.getLastSignedInAccount(this)

val Context.isInstalledGoogleFit
    get() = try {
        packageManager.getPackageInfo(GOOGLE_FIT_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

val Context.isAuthenticatedGoogleFit
    get() = isInstalledGoogleFit &&
            GoogleSignIn.hasPermissions(googleAccount, fitnessOptions) &&
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

class GoogleSignInResultContract : ActivityResultContract<Intent, GoogleSignInAccount?>() {
    override fun createIntent(context: Context, input: Intent): Intent = input

    override fun parseResult(resultCode: Int, intent: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            return task.getResult(ApiException::class.java)?.also {
                Log.d(LOG_TAG, "GoogleSignIn: id=" + it.id)
            }
        } catch (e: ApiException) {
            Log.e(LOG_TAG, "GoogleSignIn: statusCode=${e.statusCode}")
            e.printStackTrace()
        }
        return null
    }
}
