package com.example.postsharingapp.data

import com.example.postsharingapp.data.model.LoggedInUser
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private var auth: FirebaseAuth

    init {
        auth = FirebaseAuth.getInstance()
    }

    fun login(username: String, password: String, callback: (Result<LoggedInUser>) -> Unit) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    callback(Result.Success(LoggedInUser(user?.uid ?: "", username)))
                } else {
                    callback(Result.Error(IOException("Login failed", task.exception)))
                }
            }
    }


    fun logout() {
        // TODO: revoke authentication
    }
}