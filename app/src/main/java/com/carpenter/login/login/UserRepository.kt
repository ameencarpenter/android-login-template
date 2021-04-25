package com.carpenter.login.login

import android.content.Context
import com.carpenter.login.R
import com.carpenter.login.utils.await
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor() {

    suspend fun reloadUser() = withContext(IO) {
        Firebase.auth.currentUser?.reload()?.await()
    }

    suspend fun loginWithGoogle(idToken: String): Unit = withContext(IO) {
        Firebase.auth
            .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .await()
    }

    suspend fun signUpWithEmail(email: String, password: String): Unit = withContext(IO) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        sendEmailVerification()
    }

    suspend fun signInWithEmail(email: String, password: String): Unit = withContext(IO) {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
        sendEmailVerification()
    }

    suspend fun sendPasswordResetEmail(email: String): Unit = withContext(IO) {
        Firebase.auth.sendPasswordResetEmail(email).await()
    }

    suspend fun sendEmailVerification() = withContext(IO) {
        Firebase.auth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun signInWithCredential(credential: AuthCredential): Unit = withContext(IO) {
        Firebase.auth.signInWithCredential(credential).await()
    }

    suspend fun signOut(context: Context) = withContext(IO) {
        if (GoogleSignIn.getLastSignedInAccount(context) != null) signOutFromGoogle(context)

        signOutFromFacebook()

        Firebase.auth.signOut()
    }

    fun signOutFromFacebook() = LoginManager.getInstance().logOut()

    private suspend fun signOutFromGoogle(context: Context) = withContext(IO) {
        val googleSignInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_cloud_server_client_id))
            .requestEmail().build()
        GoogleSignIn.getClient(context, googleSignInOptions).signOut().await()
    }

    fun isUserVerified(): Boolean = Firebase.auth.currentUser?.isVerified() == true
}

private fun FirebaseUser.isVerified(): Boolean {
    //provider[0] will always be "firebase"

    //verified: more than 1 provider
    if (providerData.size > 2) return true

    //only email provider, either verified or not verified
    if (providerData[1].providerId == "password") return isEmailVerified

    //some provider other than email
    return true
}