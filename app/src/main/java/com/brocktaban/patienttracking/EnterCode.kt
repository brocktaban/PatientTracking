package com.brocktaban.patienttracking

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.fragment_enter_code.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import org.jetbrains.anko.wtf

class EnterCode : Fragment(), AnkoLogger {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_enter_code, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        v.et_patient_code.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(5))

        v.et_patient_code.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val code = v.et_patient_code.text.toString()

                checkCode(code, v)

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        v.btn_track_patient.setOnClickListener {
            val code = v.et_patient_code.text.toString()

            checkCode(code, v)
        }

        return v
    }

    private fun checkCode(code: String, v: View) {
        v.btn_track_patient.isEnabled = false

        val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputManager.hideSoftInputFromWindow(
            activity?.currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )

        db.collection("patients").document(code).get().addOnCompleteListener { task ->
            v.btn_track_patient.isEnabled = true

            if (!task.isSuccessful) {
                wtf("Could not get patients document", task.exception)
                return@addOnCompleteListener
            }

            if (!task.result!!.exists()) {
                v.main.snackbar("Code is not exist")
                return@addOnCompleteListener
            }

            addToPatientList(code)

            (activity as MainActivity).changeFragment(Info(code), "Patient Info")
            (activity as MainActivity).activateNavItem(1)
        }
    }

    private fun addToPatientList(code: String) {

        val userMap = HashMap<String, Any>()


        userMap["timestamp"] = FieldValue.serverTimestamp()
        userMap["code"] = code

        db
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("patients")
            .document(code).set(userMap, SetOptions.merge())
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser

        if (currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        info( "signInAnonymously:success")
                    } else {
                        wtf(  "signInAnonymously:failure", task.exception)
                        activity?.toast("Authentication failed.")
                    }
                }
        }
    }
}
