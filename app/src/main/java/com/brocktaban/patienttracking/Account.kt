package com.brocktaban.patienttracking


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast
import org.jetbrains.anko.wtf

class Account(val intentValue: String? = null) : Fragment(), AnkoLogger {

    private lateinit var mAuth: FirebaseAuth
    private val filename = "email-file"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_account, container, false)


        mAuth = FirebaseAuth.getInstance()

        wtf(mAuth.currentUser?.email != null)

        if (intentValue != null) {

            context?.openFileInput(filename).use {
                val email = String(it!!.readBytes())

                val credential = EmailAuthProvider.getCredentialWithLink(email, intentValue)

                if (mAuth.currentUser != null) {
                    mAuth.currentUser!!.linkWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                context?.toast("Success")
                                val result = task.result
                                // You can access the new user via result.getUser()
                                // Additional user info profile *not* available via:
                                // result.getAdditionalUserInfo().getProfile() == null
                                // You can check if the user is new or existing:
                                // result.getAdditionalUserInfo().isNewUser()

                                context?.deleteFile(filename)
                            } else {
                                wtf("Error signing in with email link", task.exception)
                            }
                        }
                } else {
                    mAuth.signInWithEmailLink(email, intentValue)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                context?.toast("Success")
                                val result = task.result
                                // You can access the new user via result.getUser()
                                // Additional user info profile *not* available via:
                                // result.getAdditionalUserInfo().getProfile() == null
                                // You can check if the user is new or existing:
                                // result.getAdditionalUserInfo().isNewUser()

                                context?.deleteFile(filename)
                            } else {
                                wtf("Error signing in with email link", task.exception)
                            }
                        }
                }
            }
        }


        v.sign_in.setOnClickListener {

            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("http://patient-tracker-d7c77.firebaseapp.com/app")
                .setHandleCodeInApp(true)
//            .setIOSBundleId("com.example.ios")
                .setAndroidPackageName(
                    "com.brocktaban.patienttracking",
                    true,
                    null)
                .build()

            val email = v.et_email.text.toString()

            v.sign_in.isEnabled = false

            context?.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it?.write(email.toByteArray())
            }

            mAuth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        v.main.snackbar("Email sent.")
                    } else {
                        wtf("email failed", task.exception)
                    }

                    v.sign_in.isEnabled = true
                }
        }


        return v
    }
}
