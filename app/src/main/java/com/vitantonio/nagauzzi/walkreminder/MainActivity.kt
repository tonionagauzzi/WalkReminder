package com.vitantonio.nagauzzi.walkreminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.vitantonio.nagauzzi.walkreminder.service.*
import com.vitantonio.nagauzzi.walkreminder.ui.WalkReminderTheme

class MainActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        GoogleSignInResultContract()
    ) { account: GoogleSignInAccount? ->
        Log.d(LOG_TAG, "Account: ${account?.email}")
        if (account == null) {
            return@registerForActivityResult
        }
        val options = fitnessOptions
        GoogleSignIn.requestPermissions(this, PERMISSION_REQUEST_CODE, account, options)
    }

    private fun authGoogleFit(signInLauncher: ActivityResultLauncher<Intent>) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        signInLauncher.launch(client.signInIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalkReminderTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isAuthenticatedGoogleFit) {
            authGoogleFit(signInLauncher)
            return
        }

        // Get steps.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(LOG_TAG, "Auth Google Fit: Success.")
            } else {
                Log.d(LOG_TAG, "Auth Google Fit: resultCode=$resultCode")
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WalkReminderTheme {
        Greeting("Android")
    }
}
